import java.sql.*;
import javax.swing.*;
import java.awt.*;

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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1920, 1040);

        // Create a JTextArea to display the database operation results
        textArea.setEditable(false); // Make the text area read-only
        JScrollPane scrollPane = new JScrollPane(textArea); // Add a scroll pane for large outputs

        frame.add(scrollPane, BorderLayout.CENTER); // Add the scroll pane to the frame
        frame.setVisible(true); // Make the frame visible
    }
}
