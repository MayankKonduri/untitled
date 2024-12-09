import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.sql.*;

public class MyJDBC extends JPanel {

    public JFrame frame = new JFrame("Database Operation Results");
    public JTextArea textArea = new JTextArea();

    public MyJDBC(JFrame frame){
        createGUI();

        String userName = System.getProperty("user.name");

        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://192.168.1.104:3306/setup",
                    "root",
                    "password"
            );

            String checkLoginIdQuery = "SELECT COUNT(*) AS count FROM setup.login_id_initial WHERE login_id = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkLoginIdQuery)) {
                checkStatement.setString(1, userName);
                ResultSet checkResult = checkStatement.executeQuery();

                if (checkResult.next()) {
                    int count = checkResult.getInt("count");
                    if (count > 0) {
                        String fetchUsernameQuery = "SELECT username FROM setup.login_id_initial WHERE login_id = ?";
                        try (PreparedStatement fetchStatement = connection.prepareStatement(fetchUsernameQuery)) {
                            fetchStatement.setString(1, userName);
                            ResultSet fetchResult = fetchStatement.executeQuery();
                            if (fetchResult.next()) {
                                String registeredUsername = fetchResult.getString("username");
                                textArea.append("Hello! " + registeredUsername + ", you are already registered with login_id: " + userName + ".\n");
                            }
                        }
                    } else {
                        textArea.append("Login ID " + userName + " is not registered. Adding it now.\n");
                        String desiredUsername = JOptionPane.showInputDialog(frame, "Enter your desired Username:");

                        String insertLoginIdQuery = "INSERT INTO setup.login_id_initial (login_id, username) VALUES (?, ?)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertLoginIdQuery)) {
                            insertStatement.setString(1, userName);
                            insertStatement.setString(2, desiredUsername);
                            int rowsInserted = insertStatement.executeUpdate();

                            if (rowsInserted > 0) {
                                textArea.append("Login ID " + userName + " with Username " + desiredUsername + " successfully added.\n");
                            } else {
                                textArea.append("Failed to add Login ID " + userName + " with Username.\n");
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                textArea.append("Error during username check: " + e.getMessage() + "\n");
                e.printStackTrace();
            }

            textArea.append("Data in the login_id_initial table:\n");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM setup.login_id_initial");
            while (resultSet.next()) {
                textArea.append("Login ID: " + resultSet.getString("login_id") +
                        ", Username: " + resultSet.getString("username") + "\n");
            }

        } catch (SQLException e) {
            textArea.append("Error connecting to the database: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Database Operation Results");
            new MyJDBC(frame);
        });
    }

    public void createGUI() {
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(700, 400);

        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                minimizeToTray(frame);
            }
        });
    }

    public static void minimizeToTray(JFrame frame) {
        if (!SystemTray.isSupported()) {
            System.out.println("System Tray not supported");
            return;
        }

        frame.setExtendedState(JFrame.ICONIFIED);
        frame.setVisible(false);

        SystemTray systemTray = SystemTray.getSystemTray();
        TrayIcon trayIcon = createDefaultTrayIcon();
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("Error adding to system tray: " + e.getMessage());
        }

        PopupMenu menu = new PopupMenu();
        MenuItem exitItem = new MenuItem("Exit");

        exitItem.addActionListener(e -> {
            systemTray.remove(trayIcon);
            System.exit(0);
        });

        menu.add(exitItem);
        trayIcon.setPopupMenu(menu);

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    frame.setVisible(true);
                    frame.setExtendedState(JFrame.NORMAL);
                    systemTray.remove(trayIcon);
                }
            }
        });
    }



    private static TrayIcon createDefaultTrayIcon() {
        ImageIcon icon = new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
        TrayIcon trayIcon = new TrayIcon(icon.getImage(), "Question_Client");
        trayIcon.setToolTip("Question_Client");
        return trayIcon;
    }
}
