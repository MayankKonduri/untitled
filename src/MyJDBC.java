import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.sql.*;

public class MyJDBC extends JPanel {
    private JFrame frame;
    private JTextField usernameField;
    private JComboBox<String> roleDropdown;
    private JButton registerButton;
    private String userName;

    public MyJDBC(JFrame frame) {
        this.frame = frame;
        userName = System.getProperty("user.name");
        checkIfRegistered();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Question-Client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setSize(500, 300);
            new MyJDBC(frame);
        });
    }

    private void checkIfRegistered() {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://192.168.1.11:3306/setup", "root", "password")) //Home  192.168.1.11
        //"jdbc:mysql://10.195.75.116:3306/setup", "root", "password")) //School 10.195.75.116
        {

            String query = "SELECT username, role FROM setup.login_id_initial WHERE login_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, userName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // User is already registered
                        String username = resultSet.getString("username");
                        String role = resultSet.getString("role");
                        showWelcomePage(username, role);
                    } else {
                        // User is not registered
                        createIntroPage();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createIntroPage() {
        // Main panel
        JPanel mainPanel = new JPanel();
        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel welcomeLabel = new JLabel("Welcome New User, Please Register!");
        welcomeLabel.setFont(new Font("Georgia", Font.BOLD, 16));

        JLabel userIdLabel = new JLabel("User ID:");
        JTextField userIdField = new JTextField(userName);
        userIdField.setEditable(false);
        userIdField.setBackground(Color.LIGHT_GRAY);

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15);

        JLabel roleLabel = new JLabel("Role:");
        String[] roles = {"Teacher", "Student"};
        roleDropdown = new JComboBox<>(roles);
        roleDropdown.setSelectedIndex(-1);

        registerButton = new JButton("Register");
        registerButton.setEnabled(false);
        registerButton.addActionListener(e -> registerUser());

        // Add listeners to enable/disable the button
        usernameField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { toggleRegisterButton(); }
            public void removeUpdate(DocumentEvent e) { toggleRegisterButton(); }
            public void changedUpdate(DocumentEvent e) { toggleRegisterButton(); }
        });

        roleDropdown.addActionListener(e -> toggleRegisterButton());

        // Align components to center
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(welcomeLabel)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(userIdLabel)
                                        .addComponent(usernameLabel)
                                        .addComponent(roleLabel))
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(userIdField)
                                        .addComponent(usernameField)
                                        .addComponent(roleDropdown)))
                        .addComponent(registerButton)
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(welcomeLabel)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(userIdLabel)
                                .addComponent(userIdField))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(usernameLabel)
                                .addComponent(usernameField))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(roleLabel)
                                .addComponent(roleDropdown))
                        .addComponent(registerButton)
        );

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void toggleRegisterButton() {
        boolean isUsernameFilled = !usernameField.getText().trim().isEmpty();
        boolean isRoleSelected = roleDropdown.getSelectedIndex() != -1;
        registerButton.setEnabled(isUsernameFilled && isRoleSelected);
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String role = (String) roleDropdown.getSelectedItem();

        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://192.168.1.11:3306/setup", "root", "password")) //Home  192.168.1.11
        //"jdbc:mysql://10.195.75.116/setup", "root", "password")) //School 10.195.75.116

        {

            String query = "INSERT INTO setup.login_id_initial (login_id, username, role) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, userName);
                statement.setString(2, username);
                statement.setString(3, role);
                int rowsInserted = statement.executeUpdate();

                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(frame, "Registration successful!");
                    showWelcomePage(username, role);
                } else {
                    JOptionPane.showMessageDialog(frame, "Registration failed!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showWelcomePage(String username, String role) {
        if ("Student".equalsIgnoreCase(role)) {
            frame.getContentPane().removeAll();
            frame.repaint();
            new StudentPage(frame, userName, username); // Redirect to the Student page
        } else {
            // Display Teacher Welcome Page
            JPanel welcomePanel = new JPanel();
            JLabel welcomeLabel = new JLabel("Welcome, " + username + " (" + role + ")!");
            welcomeLabel.setFont(new Font("Georgia", Font.BOLD, 16));
            welcomePanel.add(welcomeLabel);
            frame.add(welcomePanel);
            frame.setVisible(true);
        }
    }
}