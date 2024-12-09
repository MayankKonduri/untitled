import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.sql.*;

public class MyJDBC extends JPanel {

    public JFrame frame = new JFrame("Database Operation Results");
    public JTextArea textArea = new JTextArea();

    public MyJDBC(JFrame frame){
        // Create the main frame for the GUI
        createGUI();

        String userName = System.getProperty("user.name");

        try {
            // Connect to the database
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://192.168.1.104:3306/setup",
                    "root",
                    "password"
            );

            // Step 1: Check if the login_id exists in the column
            String checkLoginIdQuery = "SELECT COUNT(*) AS count FROM setup.login_id_initial WHERE login_id = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkLoginIdQuery)) {
                checkStatement.setString(1, userName); // Set the value of the login_id
                ResultSet checkResult = checkStatement.executeQuery();

                if (checkResult.next()) {
                    int count = checkResult.getInt("count"); // Get the count of rows where the login_id matches
                    if (count > 0) {
                        // If the login_id exists, retrieve and display the username
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
                        // If the login_id does not exist, prompt the user for a desired username
                        textArea.append("Login ID " + userName + " is not registered. Adding it now.\n");
                        String desiredUsername = JOptionPane.showInputDialog(frame, "Enter your desired Username:");

                        // Step 2: Insert the login_id and username into the table
                        String insertLoginIdQuery = "INSERT INTO setup.login_id_initial (login_id, username) VALUES (?, ?)";
                        try (PreparedStatement insertStatement = connection.prepareStatement(insertLoginIdQuery)) {
                            insertStatement.setString(1, userName); // Set the value of the login_id
                            insertStatement.setString(2, desiredUsername); // Set the value of the username
                            int rowsInserted = insertStatement.executeUpdate(); // Execute the INSERT query

                            // Confirm if the login_id and username were successfully added
                            if (rowsInserted > 0) {
                                textArea.append("Login ID " + userName + " with Username " + desiredUsername + " successfully added.\n");
                            } else {
                                textArea.append("Failed to add Login ID " + userName + " with Username.\n");
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                // Handle any SQL exceptions
                textArea.append("Error during username check: " + e.getMessage() + "\n");
                e.printStackTrace();
            }

            // Optional: Retrieve and print the data to verify changes
            textArea.append("Data in the login_id_initial table:\n");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM setup.login_id_initial");
            while (resultSet.next()) {
                textArea.append("Login ID: " + resultSet.getString("login_id") +
                        ", Username: " + resultSet.getString("username") + "\n");
            }

        } catch (SQLException e) {
            // Handle connection exceptions
            textArea.append("Error connecting to the database: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Database Operation Results");
            new MyJDBC(frame); // Create an instance of MyJDBC
        });
    }

    public void createGUI() {
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent closing on 'X'
        frame.setSize(700, 400);

        // Create a JTextArea to display the database operation results
        textArea.setEditable(false); // Make the text area read-only
        JScrollPane scrollPane = new JScrollPane(textArea); // Add a scroll pane for large outputs

        frame.add(scrollPane, BorderLayout.CENTER); // Add the scroll pane to the frame
        frame.setVisible(true); // Make the frame visible

        // Add a WindowListener to minimize to system tray when 'X' is clicked
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                minimizeToTray(frame); // Minimize to tray instead of closing
            }
        });
    }

    public static void minimizeToTray(JFrame frame) {
        if (!SystemTray.isSupported()) {
            System.out.println("System Tray not supported");
            return;
        }

        // Minimize the JFrame and hide it from the taskbar
        frame.setExtendedState(JFrame.ICONIFIED); // Minimize the window to the taskbar
        frame.setVisible(false); // Hide the window from the taskbar

        // Create and add the system tray icon
        SystemTray systemTray = SystemTray.getSystemTray();
        TrayIcon trayIcon = createDefaultTrayIcon();
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("Error adding to system tray: " + e.getMessage());
        }

        // Set up the tray menu
        PopupMenu menu = new PopupMenu();
        MenuItem exitItem = new MenuItem("Exit");

        exitItem.addActionListener(e -> {
            systemTray.remove(trayIcon); // Remove the tray icon
            System.exit(0); // Exit the application
        });

        menu.add(exitItem);
        trayIcon.setPopupMenu(menu);

        // Add MouseListener to TrayIcon to restore the window when clicked
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {  // Left click to restore
                    frame.setVisible(true); // Show the frame again
                    frame.setExtendedState(JFrame.NORMAL); // Restore the window
                    systemTray.remove(trayIcon); // Remove the tray icon
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
