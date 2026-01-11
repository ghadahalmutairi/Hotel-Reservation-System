package project_hotel;

import javax.swing.*;
import java.awt.*;

public class CheckInFrame extends JFrame {

    private ReservationDAO reservationDAO = new ReservationDAO();
    private User currentUser;

    public CheckInFrame(User user) {
        this.currentUser = user;

        setTitle("Guest Check-In");
        setSize(300, 180);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 5, 5));

        JTextField tfResId = new JTextField();
        JTextField tfDeposit = new JTextField();

        add(new JLabel("Reservation ID:"));
        add(tfResId);

        add(new JLabel("Deposit Amount:"));
        add(tfDeposit);

        JButton btn = new JButton("Check-In");
        add(new JLabel());
        add(btn);

        btn.addActionListener(e -> {
            try {
                int resId = Integer.parseInt(tfResId.getText());
                double dep = Double.parseDouble(tfDeposit.getText());

                boolean ok = reservationDAO.checkIn(resId, dep, currentUser.getUserId());
                JOptionPane.showMessageDialog(this,
                        ok ? "Checked-In Successfully" : "Failed to Check-In");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input");
            }
        });
    }
}
