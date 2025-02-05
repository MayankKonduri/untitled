import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class StudentHome extends JPanel {
    private JFrame frame;
    private DatabaseManager databaseManager;
    private String userName = System.getProperty("user.name");

    public StudentHome(JFrame frame) throws SQLException {
        this.frame = frame;
        this.databaseManager = new DatabaseManager();  // Initialize database manager

        // Set layout manager for the panel
        this.setLayout(new BorderLayout());

        // Create a custom font (Georgia)
        Font georgiaFont = new Font("Georgia", Font.BOLD, 16);

        // Check if the teacher exists in the database
        if (databaseManager.checkTeacherExists(userName)) {
            frame.setSize(400, 225);  // Ensure frame is resized
            System.out.println("Already a Teacher");

            // Create a JLabel to display the message on the screen
            JLabel messageLabel = new JLabel("Hello! You are a Teacher, not a Student", JLabel.CENTER);
            messageLabel.setFont(georgiaFont);
            messageLabel.setForeground(Color.RED); // Set the color of the text
            this.add(messageLabel, BorderLayout.CENTER); // Add the message label to the panel
        } else {
<<<<<<< Updated upstream
            JLabel messageLabel = new JLabel("Not a Teacher", JLabel.CENTER);
            messageLabel.setFont(georgiaFont);
            messageLabel.setForeground(Color.GREEN); // Green color for non-teacher
            this.add(messageLabel, BorderLayout.CENTER); // Add the message label to the panel
=======
                    // Database connection details
                    //String url = "jdbc:mysql://192.168.1.14/qclient"; // Replace with your database URL
                    String url = "jdbc:mysql://10.66.223.162/qclient1";
                    String user = "root"; // Replace with your DB username
                    String password = "password"; // Replace with your DB password
                    String tableName = userName + "_waitTime"; // Concatenate userName with "_waitTime"
>>>>>>> Stashed changes

            System.out.println(databaseManager.checkNameInStudentsTables(userName));
        }

        // Home button and panel
        JPanel homePanel = new JPanel();
        homePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton homeButton = new JButton("Home");
        homeButton.setFont(new Font("Georgia", Font.BOLD, 10));  // Set font for button
        homePanel.add(homeButton);

        homePanel.setBounds(10, 10, 100, 40);
        add(homePanel, BorderLayout.NORTH); // Add it at the top of the panel

        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HomePage homePage = new HomePage(frame);
                frame.getContentPane().removeAll();
                frame.getContentPane().add(homePage);
                frame.revalidate();
                frame.repaint();
                frame.setSize(400, 225);  // Resize the frame to fit the home page
            }
        });
    }
}
