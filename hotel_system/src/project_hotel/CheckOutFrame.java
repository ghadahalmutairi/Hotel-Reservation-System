package project_hotel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Date;

public class CheckOutFrame extends JFrame {

    private User currentUser;
    private ReservationDAO reservationDAO = new ReservationDAO();
    private ActivityLogDAO logDAO = new ActivityLogDAO();

    private JTextField tfReservationId;
    private JTextField tfAmountPaid;
    private JTextField tfLateFee;
    private JTextField tfDamages;

    public CheckOutFrame(User user) {
        this.currentUser = user;

        setTitle("Check-Out");
        setSize(400, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
    }

    private void initUI() {
        JPanel content = new JPanel(new BorderLayout(10,10));
        content.setBorder(new EmptyBorder(15,15,15,15));
        setContentPane(content);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Check-Out Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        formPanel.add(new JLabel("Reservation ID:"), gbc);
        gbc.gridx = 1;
        tfReservationId = new JTextField(12);
        formPanel.add(tfReservationId, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Amount Paid Now:"), gbc);
        gbc.gridx = 1;
        tfAmountPaid = new JTextField(12);
        formPanel.add(tfAmountPaid, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Late Checkout Fee:"), gbc);
        gbc.gridx = 1;
        tfLateFee = new JTextField(12);
        formPanel.add(tfLateFee, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Damages / Extra:"), gbc);
        gbc.gridx = 1;
        tfDamages = new JTextField(12);
        formPanel.add(tfDamages, gbc);

        content.add(formPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCheckOut = new JButton("Check-Out");
        JButton btnCancel   = new JButton("Cancel");
        buttonsPanel.add(btnCheckOut);
        buttonsPanel.add(btnCancel);

        content.add(buttonsPanel, BorderLayout.SOUTH);

        btnCheckOut.addActionListener(e -> doCheckOut());
        btnCancel.addActionListener(e -> dispose());
    }

    private void doCheckOut() {
        try {
            int reservationId = Integer.parseInt(tfReservationId.getText().trim());

            String paidStr   = tfAmountPaid.getText().trim();
            String lateStr   = tfLateFee.getText().trim();
            String dmgStr    = tfDamages.getText().trim();

            double amountPaid = paidStr.isEmpty() ? 0.0 : Double.parseDouble(paidStr);
            double lateFee    = lateStr.isEmpty() ? 0.0 : Double.parseDouble(lateStr);
            double damages    = dmgStr.isEmpty() ? 0.0 : Double.parseDouble(dmgStr);

            boolean ok = reservationDAO.checkOut(
                    reservationId,
                    amountPaid,
                    lateFee,
                    damages,
                    currentUser.getUserId()
            );

            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Checked-Out Successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                logDAO.log(currentUser.getUserId(),
                        "Checked-Out reservation " + reservationId +
                                " (paid=" + amountPaid +
                                ", lateFee=" + lateFee +
                                ", damages=" + damages + ")");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to Check-Out.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numeric values.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
