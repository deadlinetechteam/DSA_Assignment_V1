package utility;

import javax.swing.*;
import java.awt.*;


public class login extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JComboBox<String> roleCombo;

    public login() {
        GlobalManager.init(); 

        setTitle("Library System Login");
        setSize(400, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);

        g.gridx = 0; g.gridy = 0; mainPanel.add(new JLabel("User ID:"), g);
        txtUser = new JTextField(15);
        g.gridx = 1; mainPanel.add(txtUser, g);

        g.gridx = 0; g.gridy = 1; mainPanel.add(new JLabel("Password:"), g);
        txtPass = new JPasswordField(15);
        g.gridx = 1; mainPanel.add(txtPass, g);

        g.gridx = 0; g.gridy = 2; mainPanel.add(new JLabel("Role:"), g);
        roleCombo = new JComboBox<>(new String[]{"Student", "Staff"});
        g.gridx = 1; mainPanel.add(roleCombo, g);

        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(52, 152, 219));
        btnLogin.setForeground(Color.WHITE);
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        mainPanel.add(btnLogin, g);

        add(mainPanel);

    
        btnLogin.addActionListener(e -> {
            String id = txtUser.getText().trim();
            String password = new String(txtPass.getPassword()); 
            String role = (String) roleCombo.getSelectedItem();

            if (id.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean isAuthenticated = false;

            if ("Staff".equals(role)) {
                isAuthenticated = GlobalManager.getStaffManager().authenticate(id, password);
            } else {
                isAuthenticated = GlobalManager.getStudentManager().authenticate(id, password);
            }

            if (isAuthenticated) {
              
                new MainPage(role, id).setVisible(true);
                this.dispose();
            } else {
                // 验证失败
                JOptionPane.showMessageDialog(this, "Invalid ID or Password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static void main(String[] args) {
    
        SwingUtilities.invokeLater(() -> new login().setVisible(true));
    }
}