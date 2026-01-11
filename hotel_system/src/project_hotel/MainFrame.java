package project_hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class MainFrame extends JFrame {

    private User currentUser;
    private RoomDAO roomDAO = new RoomDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();
    private PricingService pricingService = new PricingService();
    private WaitlistDAO waitlistDAO = new WaitlistDAO();
    private ActivityLogDAO logDAO = new ActivityLogDAO();
    private GuestDAO guestDAO = new GuestDAO();   
    private JComboBox<String> cbRoomTypes;
    private JSpinner spCheckIn, spCheckOut;
    private JTable roomTable;
    private DefaultTableModel tableModel;
    private int[] typeIds;

    public MainFrame(User user) {
        this.currentUser = user;

        setTitle("Hotel System - Reception (" + user.getUsername() + ")");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        initMenu();
    }

    private void initUI() {
        JPanel top = new JPanel();

        top.add(new JLabel("Room Type:"));

        List<String[]> types = roomDAO.getRoomTypes();
        typeIds = new int[types.size()];
        String[] typeNames = new String[types.size()];

        for (int i = 0; i < types.size(); i++) {
            typeNames[i] = types.get(i)[0] + " (ID: " + types.get(i)[1] + ")";
            typeIds[i] = Integer.parseInt(types.get(i)[1]);
        }

        cbRoomTypes = new JComboBox<>(typeNames);
        top.add(cbRoomTypes);

        top.add(new JLabel("Check-In:"));
        spCheckIn = new JSpinner(new SpinnerDateModel());
        spCheckIn.setEditor(new JSpinner.DateEditor(spCheckIn, "yyyy-MM-dd"));
        top.add(spCheckIn);

        top.add(new JLabel("Check-Out:"));
        spCheckOut = new JSpinner(new SpinnerDateModel());
        spCheckOut.setEditor(new JSpinner.DateEditor(spCheckOut, "yyyy-MM-dd"));
        top.add(spCheckOut);

        JButton btnSearch = new JButton("Search");
        top.add(btnSearch);

        add(top, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Room ID", "Room Number", "Type ID", "Status"}, 0);
        roomTable = new JTable(tableModel);
        add(new JScrollPane(roomTable), BorderLayout.CENTER);

        JButton btnReserve = new JButton("Create Reservation");
        add(btnReserve, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> searchRooms());
        btnReserve.addActionListener(e -> createReservation());
    }

    private void initMenu() {
        JMenuBar mb = new JMenuBar();
        JMenu m = new JMenu("Operations");

        JMenuItem miCheckIn = new JMenuItem("Check-In");
        JMenuItem miCheckOut = new JMenuItem("Check-Out");
        JMenuItem miHouse = new JMenuItem("Housekeeping");
        JMenuItem miReports = new JMenuItem("Reports");
        JMenuItem miManageRes = new JMenuItem("Manage Reservations");

        miCheckIn.addActionListener(e -> new CheckInFrame(currentUser).setVisible(true));
        miCheckOut.addActionListener(e -> new CheckOutFrame(currentUser).setVisible(true));
        miHouse.addActionListener(e -> new HousekeepingFrame(currentUser).setVisible(true));
        miReports.addActionListener(e -> new ReportsFrame().setVisible(true));
        miManageRes.addActionListener(e -> new ManageReservationsFrame(currentUser).setVisible(true));

        m.add(miCheckIn);
        m.add(miCheckOut);
        m.add(miHouse);
        m.add(miReports);
        m.add(miManageRes);

        mb.add(m);
        setJMenuBar(mb);
    }

    private void searchRooms() {
        int idx = cbRoomTypes.getSelectedIndex();
        if (idx < 0) return;
        int typeId = typeIds[idx];

        java.util.Date in = (java.util.Date) spCheckIn.getValue();
        java.util.Date out = (java.util.Date) spCheckOut.getValue();

        if (!out.after(in)) {
            JOptionPane.showMessageDialog(this, "Check-out MUST be after check-in.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Date sqlIn = new Date(in.getTime());
        Date sqlOut = new Date(out.getTime());

        List<Room> available = roomDAO.findAvailableByType(typeId, sqlIn, sqlOut);

        tableModel.setRowCount(0);
        for (Room r : available) {
            tableModel.addRow(new Object[]{
                    r.getRoomId(),
                    r.getRoomNumber(),
                    r.getTypeId(),
                    r.getStatus()
            });
        }

        if (available.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "No available rooms. Add guest to waitlist?",
                    "Waitlist",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                try {
                    String gStr = JOptionPane.showInputDialog(this, "Enter Guest ID:");
                    if (gStr == null) return;
                    int guestId = Integer.parseInt(gStr);
                    waitlistDAO.addToWaitlist(guestId, typeId, sqlIn, sqlOut);
                    logDAO.log(currentUser.getUserId(),
                            "Added guest " + guestId + " to waitlist for type " + typeId);
                    JOptionPane.showMessageDialog(this, "Guest added to waitlist.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input.");
                }
            }
        }
    }

    private void createReservation() {
        int row = roomTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a room first.");
            return;
        }

        int roomId = (Integer) tableModel.getValueAt(row, 0);
        int typeId = (Integer) tableModel.getValueAt(row, 2);

        String[] options = {"Existing Guest", "New Guest", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Choose guest type:",
                "Guest Selection",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        Integer guestId = null;

        try {
            if (choice == 0) {
                String gStr = JOptionPane.showInputDialog(this, "Enter Guest ID (existing):");
                if (gStr == null) return;
                guestId = Integer.parseInt(gStr);
            } else if (choice == 1) {
                guestId = NewGuestDialog.showDialog(this);
                if (guestId == null || guestId <= 0) {
                    return;
                }
            }

            String discCode = JOptionPane.showInputDialog(this, "Discount code (optional):");

            java.util.Date in = (java.util.Date) spCheckIn.getValue();
            java.util.Date out = (java.util.Date) spCheckOut.getValue();
            if (!out.after(in)) {
                JOptionPane.showMessageDialog(this, "Check-out MUST be after check-in.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Date sqlIn = new Date(in.getTime());
            Date sqlOut = new Date(out.getTime());

            double totalPrice = pricingService.calculateTotalPrice(typeId, sqlIn, sqlOut, discCode);

            boolean ok = reservationDAO.createReservation(
                    guestId,
                    roomId,
                    sqlIn,
                    sqlOut,
                    totalPrice,
                    currentUser.getUserId(),
                    null  
            );

            JOptionPane.showMessageDialog(this,
                    ok ? "Reservation created. Total = " + totalPrice : "Failed to create reservation.");

            if (ok) {
                logDAO.log(currentUser.getUserId(),
                        "Created reservation for guest " + guestId + " room " + roomId);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Guest ID.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
