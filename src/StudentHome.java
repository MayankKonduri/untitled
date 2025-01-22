import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;

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
            JLabel messageLabel = new JLabel("Not a Teacher", JLabel.CENTER);
            messageLabel.setFont(georgiaFont);
            messageLabel.setForeground(Color.GREEN); // Green color for non-teacher
            this.add(messageLabel, BorderLayout.CENTER); // Add the message label to the panel

            ArrayList<String[]> results = databaseManager.checkNameInStudentsTables(userName);
            if (results.isEmpty()) {
                System.out.println("No records found for the student.");
            } else {
                for (String[] result : results) {
                    System.out.println("Main Table: " + result[0]);
                    System.out.println("Class Name: " + result[1]);
                    System.out.println("Start Time: " + result[2]);
                    System.out.println("End Time: " + result[3]);
                    System.out.println("-------------------------");
                }
            }
            // Get the current time
            LocalTime currentTime = LocalTime.now();

            // Extract hours and minutes
            int hour = currentTime.getHour();
            int minute = currentTime.getMinute();

            for(int i=0;i<results.size();i++){
                String timePreStart = (results.get(i))[2];
                String timePreEnd = (results.get(i))[3];
                int[] timePostStart = splitTime(timePreStart);
                int[] timePostEnd = splitTime(timePreEnd);

                int startHour = timePostStart[0];
                int startMinute = timePostStart[1];
                int endHour = timePostEnd[0];
                int endMinute = timePostEnd[1];

                boolean isInClassTime = (hour > startHour) || (hour == startHour && minute >= startMinute);
                boolean isBeforeEndTime = (hour < endHour) || (hour == endHour && minute < endMinute);

                if (isInClassTime && isBeforeEndTime) {
                    System.out.println("Current time is within the class time: " + results.get(i)[1]);
                }
                else{
                    System.out.println(results.get(i)[0] + " " + results.get(i)[1] + " is not currently going on");
                }
            }
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
    public static int[] splitTime(String timeString) {
        // Split the time string by colon ":"
        String[] timeParts = timeString.split(":");

        // Return an array containing hours and minutes as integers
        return new int[]{Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1])};
    }

}
