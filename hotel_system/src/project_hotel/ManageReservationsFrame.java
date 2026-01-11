


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package project_hotel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ManageReservationsFrame extends JFrame {

    private DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Guest ID","Room ID","Check-In","Check-Out","Status"}, 0);
    private JTable table = new JTable(model);

    private ReservationDAO reservationDAO = new ReservationDAO();
    private User currentUser;

    public ManageReservationsFrame(User user) {
        this.currentUser = user;

        setTitle("Manage Reservations");
        setSize(700, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnChangeDates = new JButton("Change Dates");
        JButton btnCancel = new JButton("Cancel Reservation");

        bottom.add(btnRefresh);
        bottom.add(btnChangeDates);
        bottom.add(btnCancel);
        add(bottom, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadReservations());
        btnChangeDates.addActionListener(e -> changeDates());
        btnCancel.addActionListener(e -> cancelReservation());

        loadReservations();
    }

    private void loadReservations() {
        model.setRowCount(0);
        String sql = "SELECT reservation_id, guest_id, room_id, check_in_date, check_out_date, status FROM reservations";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Vector<Object> v = new Vector<>();
                v.add(rs.getInt("reservation_id"));
                v.add(rs.getInt("guest_id"));
                v.add(rs.getInt("room_id"));
                v.add(rs.getDate("check_in_date"));
                v.add(rs.getDate("check_out_date"));
                v.add(rs.getString("status"));
                model.addRow(v);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changeDates() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a reservation first.");
            return;
        }
        int resId = (Integer) model.getValueAt(row, 0);

        String inStr = JOptionPane.showInputDialog(this, "New Check-In (yyyy-mm-dd):");
        String outStr = JOptionPane.showInputDialog(this, "New Check-Out (yyyy-mm-dd):");
        try {
            Date newIn = Date.valueOf(inStr);
            Date newOut = Date.valueOf(outStr);
            boolean ok = reservationDAO.changeDates(resId, newIn, newOut, currentUser.getUserId());
            JOptionPane.showMessageDialog(this, ok ? "Dates updated." : "Failed to update.");
            loadReservations();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid dates.");
        }
    }

    private void cancelReservation() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a reservation first.");
            return;
        }
        int resId = (Integer) model.getValueAt(row, 0);
        boolean ok = reservationDAO.cancelReservation(resId, currentUser.getUserId());
        JOptionPane.showMessageDialog(this, ok ? "Reservation canceled." : "Failed to cancel.");
        loadReservations();
    }
}
