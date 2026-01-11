package project_hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class HousekeepingFrame extends JFrame {

    private RoomDAO roomDAO = new RoomDAO();
    private ActivityLogDAO logDAO = new ActivityLogDAO();
    private User currentUser;

    private JTable table;
    private DefaultTableModel model;

    public HousekeepingFrame(User user) {
        this.currentUser = user;

        setTitle("Housekeeping - Cleaning Required Rooms");
        setSize(550, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        initUI();
        loadRooms();
    }

    private void initUI() {
        model = new DefaultTableModel(
                new String[]{"Room ID", "Room Number", "Status"},
                0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        JButton btnClean = new JButton("Mark As Cleaned");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnRefresh);
        bottom.add(btnClean);

        add(bottom, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadRooms());
        btnClean.addActionListener(e -> markClean());
    }

    private void loadRooms() {
        model.setRowCount(0);

        List<Room> list = roomDAO.findByStatus("Cleaning Required");

        for (Room r : list) {
            model.addRow(new Object[]{
                    r.getRoomId(),
                    r.getRoomNumber(),
                    r.getStatus()
            });
        }
    }

    private void markClean() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a room first.");
            return;
        }

        int roomId = (Integer) model.getValueAt(row, 0);

        boolean success = false;

        try {
            success = roomDAO.updateRoomStatus(roomId, "Available");
        } catch (Exception ignored) {
            try {
                roomDAO.updateStatus(roomId, "Available");
                success = true;
            } catch (Exception ignored2) {
                success = false;
            }
        }

        if (success) {
            logDAO.log(currentUser.getUserId(), "Room " + roomId + " marked as cleaned");
            JOptionPane.showMessageDialog(this, "Room marked as cleaned.");
            loadRooms();
        } else {
            JOptionPane.showMessageDialog(this,
                    "ERROR: RoomDAO missing updateRoomStatus() or updateStatus() method",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
