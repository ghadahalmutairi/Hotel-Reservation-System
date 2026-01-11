package project_hotel;

import javax.swing.*;
import java.awt.*;

public class AdminFrame extends JFrame {

    private User currentUser;

    public AdminFrame(User user) {
        this.currentUser = user;

        setTitle("Hotel System - Manager (" + user.getUsername() + ")");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        JLabel lbl = new JLabel("Manager Dashboard", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 18));
        add(lbl, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(3, 1, 10, 10));
        JButton btnReports = new JButton("View Reports");
        JButton btnHousekeeping = new JButton("Open Housekeeping");
        JButton btnManageRes = new JButton("Manage Reservations");

        center.add(btnReports);
        center.add(btnHousekeeping);
        center.add(btnManageRes);

        add(center, BorderLayout.CENTER);

        JButton btnLogout = new JButton("Logout");
        add(btnLogout, BorderLayout.SOUTH);

        btnReports.addActionListener(e -> new ReportsFrame().setVisible(true));
        btnHousekeeping.addActionListener(e -> new HousekeepingFrame(currentUser).setVisible(true));
        btnManageRes.addActionListener(e -> new ManageReservationsFrame(currentUser).setVisible(true));

        btnLogout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }
}