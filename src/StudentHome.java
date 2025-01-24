import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

public class StudentHome extends JPanel {
    private JFrame frame;
    private DatabaseManager databaseManager;
    private String userName = System.getProperty("user.name");

    private int minutesUntilEndOfClass; // Variable to store minutes left
    private Timer timer; // Timer for updating every 60 seconds
    private JPanel topBar;  // Declare a topBar reference at the class level
    JTable studentTable = new JTable();
    DefaultTableModel tableModel;

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
//            JLabel messageLabel = new JLabel("Not a Teacher", JLabel.CENTER);
//            messageLabel.setFont(georgiaFont);
//            messageLabel.setForeground(Color.GREEN); // Green color for non-teacher
//            this.add(messageLabel, BorderLayout.CENTER); // Add the message label to the panel

            ArrayList<String[]> results = databaseManager.checkNameInStudentsTables(userName);
            if (results.isEmpty()) {
                String noRecords = "No records found for the student.";

                JLabel messageLabel = new JLabel(noRecords, JLabel.CENTER);  // LEFT for text alignment
                messageLabel.setForeground(Color.RED);  // Set the color of the text
                messageLabel.setFont(new Font("Georgia", Font.BOLD, 14));  // Set the font to Georgia, plain style, size 16
                // Set the position of each label (you can modify x and y as needed)
                messageLabel.setBounds(20, 35, 360, 20);  // x, y, width, height

                // Add the label to the panel
                this.add(messageLabel);

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

            int temporary = 0;
            String bigToPrint = "";

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
                    int currentTimeInMinutes = hour * 60 + minute;

                    // Convert end time to minutes since midnight
                    int endTimeInMinutes = endHour * 60 + endMinute;

                    // Calculate minutes left until the end of class
                    minutesUntilEndOfClass = endTimeInMinutes - currentTimeInMinutes;
                    initializeStudentDashboard(results.get(i)[0], results.get(i)[1], minutesUntilEndOfClass);

                    int finalI = i;
                    timer = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // Recalculate the remaining minutes and update the dashboard
                            LocalTime currentTime1 = LocalTime.now();
                            int hour1 = currentTime1.getHour();
                            int minute1 = currentTime1.getMinute();

                            int currentTimeInMinutes = hour1 * 60 + minute1;

                            // Convert end time to minutes since midnight
                            int endTimeInMinutes = endHour * 60 + endMinute;

                            // Calculate minutes left until the end of class
                            minutesUntilEndOfClass = endTimeInMinutes - currentTimeInMinutes;
                            //System.out.println(minutesUntilEndOfClass);
                            // If the class is still in progress, update the display
                            if (minutesUntilEndOfClass > 0) {
                                //System.out.println("Sending");
                                initializeStudentDashboard(results.get(finalI)[0], results.get(finalI)[1], minutesUntilEndOfClass);
                            } else {
                                // Stop the timer if class time is over
                                timer.stop();
                                System.out.println(results.get(finalI)[0] + " " + results.get(finalI)[1] + " is not currently going on");
                                String msg =formatClassString(results.get(finalI)[0], results.get(finalI)[1]);
                                JOptionPane.showMessageDialog(frame, msg + " is Over");
                                StudentHome studentHome = null;
                                try {
                                    studentHome = new StudentHome(frame);
                                } catch (SQLException ex) {
                                    throw new RuntimeException(ex);
                                }
                                frame.getContentPane().removeAll();
                                frame.revalidate();
                                frame.repaint();
                                frame.setSize(400, 325);
                                frame.add(studentHome);
                                frame.setVisible(true);
                            }
                        }
                    });
                    timer.start();  // Start the timer

                }
                else{
                    int minutesToClassStart = calculateMinutesToClassStart(hour, minute, startHour, startMinute, endHour, endMinute);
                    if(minutesToClassStart == 1000000)
                    {
                        String smallToPrint = formatClassString(results.get(i)[0], results.get(i)[1]) + " is going to resume Tomorrow.";
                        System.out.println(smallToPrint);
                        temporary++;
                        bigToPrint = bigToPrint + smallToPrint + "~";
                        if(temporary == results.size()){
                            System.out.println(bigToPrint);
                            // Split the string by "~"
                            String[] splitArray = bigToPrint.split("~");

                            // Convert the array to an ArrayList
                            ArrayList<String> resultList = new ArrayList<>(Arrays.asList(splitArray));
                            resultList.removeIf(String::isEmpty);
                            initializeStudentWaitingScreen(resultList);
                        }
                    }
                    else {
                        String smallToPrint = formatClassString(results.get(i)[0], results.get(i)[1]) + " is going to resume in " + minutesToClassStart + " minutes.";
                        System.out.println(smallToPrint);
                        temporary++;
                        bigToPrint = bigToPrint + smallToPrint + "~";
                        if(temporary == results.size()){
                            System.out.println(bigToPrint);
                            // Split the string by "~"
                            String[] splitArray = bigToPrint.split("~");

                            // Convert the array to an ArrayList
                            ArrayList<String> resultList = new ArrayList<>(Arrays.asList(splitArray));
                            resultList.removeIf(String::isEmpty);
                            initializeStudentWaitingScreen(resultList);
                        }
                    }
                }
            }
        }

        // Home button and panel
        JPanel homePanel = new JPanel();
        homePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton homeButton = new JButton("Home");
        homeButton.setFont(new Font("Georgia", Font.BOLD, 10));  // Set font for button
        homePanel.add(homeButton);

        homePanel.setBounds(10, 25, 100, 100);
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

    private void initializeStudentWaitingScreen(ArrayList<String> resultList) {
        resultList.add("");
//        // Create a panel with BoxLayout (vertical alignment)
//        JPanel panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // BoxLayout for vertical layout
//
//        // Clear the frame before adding new components
//        frame.getContentPane().removeAll();  // Remove all components from the frame
//
//        // Check if resultList is empty
//        if (resultList == null || resultList.isEmpty()) {
//            JLabel noResultsLabel = new JLabel("No students in the waiting list.");
//            noResultsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//            panel.add(noResultsLabel);
//        } else {
//            // Loop through the resultList and create a label for each element
//            for (String item : resultList) {
//                JLabel label = new JLabel(item);  // Create a new label
//                label.setAlignmentX(Component.CENTER_ALIGNMENT);  // Center the label horizontally
//                panel.add(label);  // Add the label to the panel
//            }
//        }
//
//        // Wrap the panel inside a scroll pane
//        JScrollPane scrollPane = new JScrollPane(panel);
//
//        // Set the preferred size of the scroll pane or panel if necessary
//        panel.setPreferredSize(new Dimension(400, 300));  // Adjust size if needed
//
//        // Add the scroll pane to the frame
//        frame.add(scrollPane);  // Add the panel inside a scroll pane
//
//        // Refresh the frame
//        frame.revalidate();  // Revalidate the frame to reflect the changes
//        frame.repaint();     // Repaint the frame to update the display
//
//        // Make the frame visible
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                frame.setVisible(true); // Ensure frame is visible after all updates
//            }
//        });
        int yPosition = 65;  // Starting position for the first label

        // Loop through the resultList and create a label for each element
        for (String tempResults : resultList) {
            JLabel messageLabel = new JLabel(tempResults, JLabel.CENTER);  // LEFT for text alignment
            messageLabel.setForeground(Color.BLACK);  // Set the color of the text
            messageLabel.setFont(new Font("Georgia", Font.PLAIN, 12));  // Set the font to Georgia, plain style, size 16
            // Set the position of each label (you can modify x and y as needed)
            messageLabel.setBounds(20, yPosition, 360, 20);  // x, y, width, height

            // Add the label to the panel
            this.add(messageLabel);

            // Update the y-position for the next label (add spacing between labels)
            yPosition += 25;  // Adjust the value to change the vertical spacing between labels
        }

        // Refresh the frame
        frame.revalidate();  // Revalidate the frame to reflect the changes
        frame.repaint();     // Repaint the frame to update the display


    }



    // Function to calculate minutes to class start
    public static int calculateMinutesToClassStart(int currentHour, int currentMinute, int startHour, int startMinute, int endHour, int endMinute) {
        // Convert current time, start time, and end time to minutes since midnight
        int currentTotalMinutes = currentHour * 60 + currentMinute;
        int startTotalMinutes = startHour * 60 + startMinute;
        int endTotalMinutes = endHour * 60 + endMinute;

        // Calculate the difference to class start time
        int minutesRemainingToStart = startTotalMinutes - currentTotalMinutes;

        // If class start time is in the future (minutesRemainingToStart > 0)
        if (minutesRemainingToStart > 0) {
            return minutesRemainingToStart;
        } else {
            // If the class has already started, check if it's past the end time
            int minutesRemainingToEnd = endTotalMinutes - currentTotalMinutes;

            // If the current time is also after the end time, return 1,000,000
            if (minutesRemainingToEnd < 0) {
                return 1000000;
            }

            // Otherwise, return the negative value of minutesRemainingToStart
            return 1000000; //currently going on
        }
    }


    private void initializeStudentDashboard(String classInfo, String className, int minutesTillEndOfClass) {

        //System.out.println("Got " + minutesTillEndOfClass);

        // Format the class display string
        String formattedDisplayString = formatClassString(classInfo, className);
        //System.out.println(formattedDisplayString);

        // Set the layout to null for absolute positioning
        setLayout(null);

        // Create the top bar panel with a background color (this will change based on time)
        if (topBar == null) {
            topBar = new JPanel();
            topBar.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center the label in the bar
            topBar.setBounds(0, 0, 400, 25);  // Set size of the top bar (full width, 25px height)
            Border blackBorder = BorderFactory.createLineBorder(Color.BLACK, 2);  // Black border with 2px thickness
            topBar.setBorder(blackBorder);
            add(topBar);
        }

        // Set the background color of the top bar based on the minutes left
        if (minutesTillEndOfClass > 15) {
            topBar.setBackground(Color.GREEN); // More than 15 minutes left
        } else if (minutesTillEndOfClass > 5) {
            topBar.setBackground(Color.YELLOW); // Between 5 and 15 minutes left
        } else {
            topBar.setBackground(Color.RED); // Less than 5 minutes left
        }

        // Create the "minutes left" label (text color will be black)
        JLabel minutesLeftLabel = new JLabel("Minutes left: " + minutesTillEndOfClass);

        // Set the font for the label
        minutesLeftLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
        minutesLeftLabel.setForeground(Color.BLACK);  // Set text color to black

        // Add the label to the top bar panel
        topBar.removeAll(); // Remove any previous label (important for updates)
        topBar.add(minutesLeftLabel);

        // Create the label with the formatted class display string
        JLabel classLabel = new JLabel(formattedDisplayString);
        classLabel.setFont(new Font("Georgia", Font.PLAIN, 14));
        classLabel.setForeground(Color.BLACK);  // Set text color for the class name

        // Calculate the position for top-center alignment
        int labelWidth = classLabel.getPreferredSize().width;
        int labelHeight = classLabel.getPreferredSize().height;

        // For a frame size of 400x325, calculate the x-position to center the label
        int xPosition = (400 - labelWidth) / 2;  // Center horizontally
        int yPosition = 32;  // Position the class label 60px from the top (after the top bar)

        // Set the position and size of the label
        classLabel.setBounds(xPosition, yPosition, labelWidth, labelHeight);
        add(classLabel);

        // Revalidate and repaint the panel to ensure changes are reflected
        revalidate();
        repaint();
    }


    public static String formatClassString(String classInfo, String className) {
        // Split input string based on the "_"
        String[] nameClassParts = classInfo.split("_");
        String name = nameClassParts[0];
        int period = Integer.parseInt(nameClassParts[1]);

        // Determine the correct suffix for the period
        String suffix = getPeriodSuffix(period);

        // Capitalize the first letter of the name and className
        name = capitalize(name);
        className = capitalize(className);

        // Format the final string
        return String.format("%s' %s Period %s", name, suffix, className);
    }

    // Method to determine the correct period suffix (st, nd, rd, th)
    private static String getPeriodSuffix(int period) {
        if (10 <= period % 100 && period % 100 <= 20) {
            return "th"; // Special case for 11, 12, 13, etc.
        } else {
            switch (period % 10) {
                case 1: return "1st";
                case 2: return "2nd";
                case 3: return "3rd";
                case 4: return "4th";
                case 5: return "5th";
                case 6: return "6th";
                case 7: return "7th";
                default: return "";
            }
        }
    }

    // Method to capitalize the first letter of a string
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static int[] splitTime(String timeString) {
        // Split the time string by colon ":"
        String[] timeParts = timeString.split(":");

        // Return an array containing hours and minutes as integers
        return new int[]{Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1])};
    }

}
