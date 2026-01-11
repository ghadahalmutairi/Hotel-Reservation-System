package project_hotel;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReportsFrame extends JFrame {

    private JLabel lblTotalRooms = new JLabel();
    private JLabel lblAvailable = new JLabel();
    private JLabel lblOccupied = new JLabel();
    private JLabel lblOccRate = new JLabel();
    private JLabel lblTodayRevenue = new JLabel();
    private JLabel lblADR = new JLabel();
    private JLabel lblCanceled = new JLabel();
    private JLabel lblNoShow = new JLabel();

    public ReportsFrame() {
        setTitle("Reports & Analytics");
        setSize(450, 300);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(9, 1, 5, 5));

        add(lblTotalRooms);
        add(lblAvailable);
        add(lblOccupied);
        add(lblOccRate);
        add(lblTodayRevenue);
        add(lblADR);
        add(lblCanceled);
        add(lblNoShow);

        JButton btnRefresh = new JButton("Refresh");
        add(btnRefresh);

        btnRefresh.addActionListener(e -> loadData());

        loadData();
    }

    private void loadData() {
        try (Connection c = DBUtil.getConnection()) {

            int totalRooms = scalar(c, "SELECT COUNT(*) FROM rooms");
            int available = scalar(c, "SELECT COUNT(*) FROM rooms WHERE status='Available'");
            int occupied = scalar(c, "SELECT COUNT(*) FROM rooms WHERE status='Occupied'");

            lblTotalRooms.setText("Total rooms: " + totalRooms);
            lblAvailable.setText("Available rooms: " + available);
            lblOccupied.setText("Occupied rooms: " + occupied);

            double occRate = totalRooms == 0 ? 0.0 : (occupied * 100.0 / totalRooms);
            lblOccRate.setText(String.format("Occupancy Rate: %.1f%%", occRate));

            double todayRevenue = scalarDouble(c,
                    "SELECT IFNULL(SUM(total_charges),0) FROM invoices WHERE DATE(invoice_date) = CURRENT_DATE");
            lblTodayRevenue.setText("Today's Revenue: " + todayRevenue);

            int todayNights = scalar(c,
                    "SELECT IFNULL(SUM(DATEDIFF(check_out_date, check_in_date)),0) FROM reservations " +
                    "WHERE status='Checked-Out' AND check_out_date = CURRENT_DATE");
            double adr = todayNights == 0 ? 0.0 : (todayRevenue / todayNights);
            lblADR.setText(String.format("ADR (today): %.2f", adr));

            int canceled = scalar(c, "SELECT COUNT(*) FROM reservations WHERE status='Canceled'");
            lblCanceled.setText("Canceled reservations: " + canceled);

            int noShow = scalar(c,
                    "SELECT COUNT(*) FROM reservations " +
                    "WHERE status='Confirmed' AND check_in_date < CURRENT_DATE");
            lblNoShow.setText("No-show reservations: " + noShow);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int scalar(Connection c, String sql) {
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private double scalarDouble(Connection c, String sql) {
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
