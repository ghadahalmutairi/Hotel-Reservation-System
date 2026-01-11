package project_hotel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener; 

public class LoginFrame extends JFrame {

    private JTextField tfUsername; 
    private JPasswordField pfPassword;
    private UserDAO userDAO = new UserDAO(); 
    private ActivityLogDAO logDAO = new ActivityLogDAO(); 

    public LoginFrame() {
        setTitle("Hotel System - Login"); 
        setSize(350, 200); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        setLocationRelativeTo(null); 

        initUI(); 
    }

    private void initUI() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 

    
        panel.add(new JLabel("Username:")); 
        tfUsername = new JTextField();
        panel.add(tfUsername); 
        panel.add(new JLabel("Password:")); 
        pfPassword = new JPasswordField();
        panel.add(pfPassword); 

        JButton btnLogin = new JButton("Login");
        JButton btnExit = new JButton("Exit"); 
        panel.add(btnLogin);
        panel.add(btnExit);

        add(panel); 
        btnLogin.addActionListener(e -> doLogin()); 
        btnExit.addActionListener(e -> System.exit(0)); 
        pfPassword.addActionListener(e -> doLogin()); 
    }

    private void doLogin() {
        String username = tfUsername.getText().trim(); 
        String pass = new String(pfPassword.getPassword()).trim(); 

        if (username.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username and password", "Error", JOptionPane.ERROR_MESSAGE); 
            return;
        }

        User user = userDAO.findByUsername(username); 

        if (user == null || !userDAO.verifyPassword(user, pass)) { 
            JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE); 
            return;
        }

        logDAO.log(user.getUserId(), "User logged in."); 

        String role = user.getRole(); 
        SwingUtilities.invokeLater(() -> {
            if ("Manager".equalsIgnoreCase(role)) { 
                AdminFrame adminFrame = new AdminFrame(user); 
                adminFrame.setVisible(true); 
            } else {
                MainFrame mainFrame = new MainFrame(user); 
                mainFrame.setVisible(true); 
            }
        });

        dispose(); 
    }
}