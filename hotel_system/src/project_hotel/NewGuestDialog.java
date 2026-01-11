package project_hotel;

import javax.swing.*;
import java.awt.*;

public class NewGuestDialog extends JDialog {

    private JTextField tfFirstName;
    private JTextField tfLastName;
    private JTextField tfIdNumber;
    private JTextField tfEmail;
    private JTextField tfPhone;

    private Integer createdGuestId = null; 
    public static Integer showDialog(Frame parent) {
        NewGuestDialog dlg = new NewGuestDialog(parent);
        dlg.setVisible(true);
        return dlg.createdGuestId;
    }

    public NewGuestDialog(Frame parent) {
        super(parent, "New Guest", true); 
        setSize(350, 260);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(5,5));

        JPanel center = new JPanel(new GridLayout(5,2,5,5));
        center.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        center.add(new JLabel("First Name*:"));
        tfFirstName = new JTextField();
        center.add(tfFirstName);

        center.add(new JLabel("Last Name*:"));
        tfLastName = new JTextField();
        center.add(tfLastName);

        center.add(new JLabel("ID Number:"));
        tfIdNumber = new JTextField();
        center.add(tfIdNumber);

        center.add(new JLabel("Email:"));
        tfEmail = new JTextField();
        center.add(tfEmail);

        center.add(new JLabel("Phone:"));
        tfPhone = new JTextField();
        center.add(tfPhone);

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        bottom.add(btnSave);
        bottom.add(btnCancel);
        add(bottom, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> {
            createdGuestId = null;
            dispose();
        });
    }

    private void onSave() {
        String fn = tfFirstName.getText().trim();
        String ln = tfLastName.getText().trim();
        String idn = tfIdNumber.getText().trim();
        String em  = tfEmail.getText().trim();
        String ph  = tfPhone.getText().trim();

        if (fn.isEmpty() || ln.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "First name and Last name are required.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        GuestDAO dao = new GuestDAO();
        int newId = dao.createGuest(fn, ln, idn, em, ph);
        if (newId > 0) {
            createdGuestId = newId;
            JOptionPane.showMessageDialog(this,
                    "Guest created. ID = " + newId);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to create guest.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
