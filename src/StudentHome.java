import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class StudentHome extends JPanel {
    private JFrame frame;
    private DatabaseManager databaseManager;
    private String userName; //= System.getProperty("user.name");
    private Timer countdownTimer;
    private Timer positionUpdateTimer;
    public String questionTableName;
    public String waitTimeOfClass;
    private int minutesUntilEndOfClass; // Variable to store minutes left
    private Timer timer; // Timer for updating every 60 seconds
    private JPanel topBar;  // Declare a topBar reference at the class level
    public int periodNumber;
    JLabel positionLabel;
    JLabel classLabel;
    int labelWidth;
    int labelHeight;
    int xPosition;
    int yPosition;
    JPanel waitTimePanel = new JPanel();
    JLabel waitTimeLabel;
    int waitTimeLabelWidth;
    int waitTimeLabelHeight;
    int waitTimeXPosition;
    int waitTimeYPosition;
    JPanel positionPanel = new JPanel();
    int positionLabelWidth;
    int positionLabelHeight;
    int positionXPosition;
    int positionYPosition;
    JPanel buttonPanel = new JPanel();  // Create a new panel for buttons
    JButton addQuestionButton = new JButton("Add Question");
    JButton removeQuestionButton = new JButton("Remove Question");
    String[] columnNames;
    String[][] rowData;
    JTable questionTable;
    private Thread refreshThread;
    private volatile boolean running = true;
    public boolean successful_1;
    String className;

    public StudentHome(JFrame frame, String userName) throws SQLException {
        this.frame = frame;
        this.userName = userName;
        this.databaseManager = new DatabaseManager(userName);  // Initialize database manager

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
                    // Database connection details
                    String url = "jdbc:mysql://192.168.1.14/qclient1"; // Replace with your database URL
                    String user = "root"; // Replace with your DB username
                    String password = "password"; // Replace with your DB password
                    String tableName = userName + "_waitTime"; // Concatenate userName with "_waitTime"

                    // SQL query to create the table if it doesn't exist
                    String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                            + "ID INT AUTO_INCREMENT PRIMARY KEY, "
                            + "WaitTime_1 INT, "
                            + "WaitTime_2 INT, "
                            + "WaitTime_3 INT, "
                            + "WaitTime_4 INT, "
                            + "WaitTime_5 INT, "
                            + "WaitTime_6 INT, "
                            + "WaitTime_7 INT"
                            + ")";

                    // Establish connection and execute the query
                    try (Connection connection = DriverManager.getConnection(url, user, password);
                         Statement statement = connection.createStatement()) {

                        // Execute the SQL query to create the table if it doesn't already exist
                        statement.executeUpdate(createTableSQL);
                        System.out.println("Table '" + tableName + "' created successfully (if not exists).");

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
            ArrayList<String[]> results = databaseManager.checkNameInStudentsTables(userName);
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
                    // Format the class display string
                    String formattedDisplayString = formatClassString(results.get(i)[0], results.get(i)[1]);

                    // Set the layout to null for absolute positioning
                    setLayout(null);

                    // Create the label with the formatted class display string
                    classLabel = new JLabel(formattedDisplayString);
                    classLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
                    classLabel.setForeground(Color.BLACK);  // Set text color for the class name

                    // Calculate the position for top-center alignment
                    labelWidth = classLabel.getPreferredSize().width;
                    labelHeight = classLabel.getPreferredSize().height;

                    // For a frame size of 400x325, calculate the x-position to center the label
                    xPosition = (400 - labelWidth) / 2;  // Center horizontally
                    yPosition = 50;  // Position the class label 32px from the top (after the top bar)

                    // Set the position and size of the label
                    classLabel.setBounds(xPosition, yPosition, labelWidth, labelHeight);
                    add(classLabel);

                    // Create the panel for the light blue bar (background)
                    waitTimePanel.setBackground(new Color(255, 182, 193)); // Light red (pinkish) color
                    waitTimePanel.setLayout(new BorderLayout());

                    // Create a label for the Wait Time text
                    waitTimeLabel = new JLabel("Wait Time: 0 seconds", SwingConstants.CENTER);
                    waitTimeLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
                    waitTimeLabel.setForeground(Color.BLACK);  // Set text color
                    waitTimePanel.setBorder(new LineBorder(Color.BLACK, 2)); // Black border with thickness of 2
                    // Add the waitTimeLabel to the panel
                    waitTimePanel.add(waitTimeLabel, BorderLayout.CENTER);

                    // Calculate the position to place the Wait Time bar below the class label
                    waitTimeLabelWidth = waitTimeLabel.getPreferredSize().width;
                    waitTimeLabelHeight = waitTimeLabel.getPreferredSize().height;

                    // Set the panel size and position
                    waitTimeXPosition = (400 - waitTimeLabelWidth) / 2;  // Center horizontally
                    waitTimeYPosition = yPosition + labelHeight + 10;  // 10px gap below class label

                    waitTimePanel.setBounds(waitTimeXPosition - 123, waitTimeYPosition + 20, waitTimeLabelWidth + 58, waitTimeLabelHeight + 20);

                    // Add the panel to the frame
                    add(waitTimePanel);

                    // Create the panel for the light blue bar (background)
                    positionPanel.setLayout(new BorderLayout());

// Create a label for the Position text
                    positionLabel = new JLabel("Position: ", SwingConstants.CENTER);
                    positionLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
                    positionLabel.setForeground(Color.BLACK);  // Set text color
                    positionPanel.setBorder(new LineBorder(Color.BLACK, 2)); // Black border with thickness of 2
// Add the positionLabel to the panel
                    positionPanel.add(positionLabel, BorderLayout.CENTER);

// Set the same width as the waitTimePanel
                    positionLabelWidth = waitTimePanel.getWidth();
                    positionLabelHeight = positionLabel.getPreferredSize().height;

// Calculate the position to place the Position panel to the right of the waitTimePanel
                    positionXPosition = waitTimePanel.getX() + waitTimePanel.getWidth() + 4; // 10px gap to the right
                    positionYPosition = waitTimePanel.getY();  // Align vertically with waitTimePanel

// Set the panel size and position
                    positionPanel.setBounds(positionXPosition, positionYPosition, positionLabelWidth - 6, positionLabelHeight + 20);

// Add the panel to the frame
                    add(positionPanel);

                    // Revalidate and repaint the panel to ensure changes are reflected
                    revalidate();
                    repaint();


                    //keepPositionUpdated(questionTableName, userName, positionLabel);


                    //------------------------- Header --------------------------//
                    //-------------------- Question Table -----------------------//

                    // Add buttons below the table
                    buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));  // Center the buttons horizontally
                    buttonPanel.setBounds(15, yPosition + labelHeight + 140, 360, 40);  // Set the position of the button panel

// Create "Add Question" and "Remove Question" buttons
                    addQuestionButton.setFont(new Font("Georgia", Font.PLAIN, 12));
                    removeQuestionButton.setFont(new Font("Georgia", Font.PLAIN, 12));
                    addQuestionButton.setBounds(15, yPosition + labelHeight + 140, 360, 25);
                    removeQuestionButton.setBounds(15, yPosition + labelHeight + 140, 360, 25);

                    add(addQuestionButton);
                    add(removeQuestionButton);
                    // Add buttons to the button panel
                    //buttonPanel.add(addQuestionButton);
                    //buttonPanel.add(removeQuestionButton);

// Add the button panel to the main panel
                    //add(buttonPanel);

                    // Column headers for the table
                    columnNames = new String[]{"Question Summary"};
                    rowData = new String[][]{
                            {""}
                    };
// Create the JTable with sample data and column names
                    questionTable = new JTable(rowData, columnNames);

                    questionTable.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 14));  // Bolder font for header

// Set the row height to a larger value
                    int rowHeight = 40; // Adjust this value as needed
                    questionTable.setRowHeight(rowHeight);  // Set the row height

// Set the font of the table to Georgia
                    questionTable.setFont(new Font("Georgia", Font.PLAIN, 12));

// Create a cell renderer to center the text in all columns
                    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                    centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

// Apply the center renderer to all columns
                    for (int ii = 0; ii < questionTable.getColumnCount(); ii++) {
                        questionTable.getColumnModel().getColumn(ii).setCellRenderer(centerRenderer);
                    }

// Add the table to a JScrollPane for scrollability
                    JScrollPane tableScrollPane = new JScrollPane(questionTable);
                    tableScrollPane.setBounds(15, yPosition + labelHeight + 70, 360, 70);  // Position it below the class label

// Set the preferred size of the table for proper display
                    questionTable.setPreferredScrollableViewportSize(new Dimension(300, 100));  // Adjust as needed

// Add the scroll pane with the table to the panel
                    add(tableScrollPane);
                }}


//            JLabel messageLabel = new JLabel("Not a Teacher", JLabel.CENTER);
//            messageLabel.setFont(georgiaFont);
//            messageLabel.setForeground(Color.GREEN); // Green color for non-teacher
//            this.add(messageLabel, BorderLayout.CENTER); // Add the message label to the panel

            if (results.isEmpty()) {
                System.out.println("No records found for the student.");
            } else {
                for (String[] result : results) {
                    System.out.println("Main Table: " + result[0]);
                    System.out.println("Class Name: " + result[1]);
                    System.out.println("Start Time: " + result[2]);
                    System.out.println("End Time: " + result[3]);
                    System.out.println("Wait Time: " + result[4]);
                    System.out.println("-------------------------");
                }
            }
            // Get the current time
            currentTime = LocalTime.now();

            // Extract hours and minutes
            hour = currentTime.getHour();
            minute = currentTime.getMinute();

            temporary = 0;
            bigToPrint = "";

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
                    className = results.get(i)[1];
                    System.out.println("Current time is within the class time: " + results.get(i)[1]);
                    int currentTimeInMinutes = hour * 60 + minute;

                    // Convert end time to minutes since midnight
                    int endTimeInMinutes = endHour * 60 + endMinute;

                    // Calculate minutes left until the end of class
                    minutesUntilEndOfClass = endTimeInMinutes - currentTimeInMinutes;
                    initializeStudentDashboard(results.get(i)[0], results.get(i)[1]);
                    initializeTimer(minutesUntilEndOfClass);

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
                                initializeTimer(minutesUntilEndOfClass);
                            } else {
                                // Stop the timer if class time is over
                                timer.stop();
                                System.out.println(results.get(finalI)[0] + " " + results.get(finalI)[1] + " is not currently going on");
                                String msg =formatClassString(results.get(finalI)[0], results.get(finalI)[1]);
                                JOptionPane.showMessageDialog(frame, msg + " is Over");
                                StudentHome studentHome = null;
                                try {
                                    studentHome = new StudentHome(frame, userName);
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
                //stopCountdown();  // Stop the countdown timer
                HomePage homePage = null;
                try {
                    homePage = new HomePage(frame, userName);
                } catch (IOException ex) {


                }
                stopAutoRefreshThread();
                frame.getContentPane().removeAll();
                frame.getContentPane().add(homePage);
                frame.revalidate();
                frame.repaint();
                frame.setSize(400, 225);  // Resize the frame to fit the home page
            }
        });

        startAutoRefreshThread();
    }
    private void startAutoRefreshThread() {
        refreshThread = new Thread(() -> {
            while (running) {
                try {
                    updateStudentDashboard();
                    Thread.sleep(3000); // Adjust interval
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        refreshThread.start();
    }

    private void stopAutoRefreshThread() {
        running = false; // Stop the loop
        if (refreshThread != null) {
            refreshThread.interrupt(); // Interrupt the sleep if needed
        }
    }

    public void updateStudentDashboard() {
        // Retrieve and update the current wait time from the database
        ArrayList<String[]> results = null;
        try {
            results = databaseManager.checkNameInStudentsTables(userName);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        if (results.isEmpty()) {
            System.out.println("No records found for the student.");
        } else {
            for (int i = 0; i < results.size(); i++) {
                try {
                    // Parse the times and update wait time and position dynamically
                    LocalTime startTime = LocalTime.parse((String) results.get(i)[2]);
                    LocalTime endTime = LocalTime.parse((String) results.get(i)[3]);

                    // Check if current time is within the range and update values accordingly
                    if (LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime)) {
                        String result = (String) results.get(i)[0]; // Get the record identifier
                        String waitTime = (String) results.get(i)[4];  // Get the wait time value
                        waitTimeOfClass = waitTime;
                        periodNumber = extractNumberFromTableName(result);
                        questionTableName = result.replace("_main", "_questions");

                        int position = databaseManager.getQuestionPosition(questionTableName, userName);
                        positionLabel.setText("Position: " + position); // Update position label
                        if(position==-1)
                        {
                            positionLabel.setText("Position: N/A");
                        }
                        // Check if the student has an active question and update the table accordingly
                        String input = databaseManager.getQuestionStudent(questionTableName, userName);
                        if (input.equals("")) {
                            //positionLabel.setText("Position: N/A");
                            // No active question
                            questionTable.setValueAt("No Active Question", 0, 0);
                            removeQuestionButton.setVisible(false);
                            addQuestionButton.setVisible(true);

//                            // Update the wait time label and panel color based on the current wait time
//                            waitTimeLabel.setText("Wait Time: " + waitTime + " seconds");
//                            if (Integer.parseInt(waitTime) <= 10) {
//                                waitTimePanel.setBackground(new Color(144, 238, 144)); // Light green color
//                            } else {
//                                waitTimePanel.setBackground(new Color(255, 182, 193)); // Light pink color
//                            }
                        } else {
                            // Active question exists
                            questionTable.setValueAt(input, 0, 0);
                            removeQuestionButton.setVisible(true);
                            addQuestionButton.setVisible(false);
                            //waitTimeLabel.setText("Wait Time: 0 seconds");
                            waitTimePanel.setBackground(new Color(144, 238, 144)); // Light green color
                        }

                        // Revalidate and repaint to ensure changes are reflected
                        revalidate();
                        repaint();
                    }
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid time format in results: " + e.getMessage());
                }
            }
        }
    }


    private void initializeTimer(int minutesTillEndOfClass) {
        //System.out.println(minutesTillEndOfClass);  // Debugging line to check if the value is being updated

        // Use SwingUtilities.invokeLater to ensure UI updates happen on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
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

                // Remove previous label and add the new one
                topBar.removeAll();  // Remove any previous label (important for updates)
                topBar.add(minutesLeftLabel);  // Add the new label

                // Revalidate and repaint to ensure the UI is updated
                topBar.revalidate();
                topBar.repaint();
            }
        });
    }


    private void initializeStudentWaitingScreen(ArrayList<String> resultList) {
        resultList.add("");
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
    private void initializeStudentDashboard(String classInfo, String className) {

        ArrayList<String[]> results1 = null;
        try {
            results1 = databaseManager.checkNameInStudentsTables(userName);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        if (results1.isEmpty()) {
            System.out.println("No records found for the student.");
        }


        ArrayList<String[]> results = null;
        try {
            results = databaseManager.checkNameInStudentsTables(userName);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        if (results.isEmpty()) {
            System.out.println("No records found for the student.");
        }
        for (int i = 0; i < results.size(); i++) {
            try {
                // Convert the strings in results[2] and results[3] to LocalTime
                LocalTime startTime = LocalTime.parse((String) results.get(i)[2]);  // Parse the start time string
                LocalTime endTime = LocalTime.parse((String) results.get(i)[3]);    // Parse the end time string

                // Check if the current time is within the range
                if (LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime)) {
                    String result = (String) results.get(i)[0]; // Get the value from the array (ensure it's a String)
                    String waitTime = (String) results.get(i)[4];
                    waitTimeOfClass = waitTime;
                    periodNumber = extractNumberFromTableName(result);
                    // Check if the result matches the pattern "something_X_main" and modify it
                    if (result.matches(".*_\\d+_main")) {  // Regex to match something_X_main
                        questionTableName = result.replace("_main", "_questions"); // Replace "_main" with "_questions"
                    }
                    // Assuming DatabaseManager.addRecordToTable(String tableName, String... values) works this way

                }
            } catch (DateTimeParseException e1) {
                // Handle the case where the string cannot be parsed into a LocalTime
                System.out.println("Invalid time format in results: " + e1.getMessage());
            }
        }
        int position = databaseManager.getQuestionPosition(questionTableName, userName);
        positionLabel.setText("Position: " + position);
        if(position==-1)
        {
            positionLabel.setText("Position: N/A");
        }
        waitTimeLabel.setText("Wait Time: " + 0 + " seconds");
        waitTimePanel.setBackground(new Color(144, 238, 144)); // Light green color
        System.out.println("Test: " + waitTimeOfClass);
        String input = databaseManager.getQuestionStudent(questionTableName, userName);
        if(input.equals("")){
            positionLabel.setText("Position: N/A");
            waitTimeLabel.setText("Wait Time: ## seconds");
            // Sample data for the table (you can replace this with your actual data)
            rowData[0][0] = "";
            addQuestionButton.setVisible(true);
            removeQuestionButton.setVisible(false);
            String tableString = userName + "_" + "waittime";
            String columnName = "WaitTime_" + periodNumber;
            int loadWaitTime = databaseManager.getWaitTimeFromStudent(tableString,columnName);
            System.out.println("Wait Time Received: " + loadWaitTime);
            if(!(loadWaitTime==0)) {
                addQuestionButton.setEnabled(false);
                if(loadWaitTime<=10){
                    waitTimePanel.setBackground(new Color(144, 238, 144)); // Light green color
                }
                else{
                    waitTimePanel.setBackground(new Color(255, 182, 193)); // Light red (pinkish) color
                }
                startCountdown(waitTimeLabel, loadWaitTime, waitTimePanel, addQuestionButton);
            }
            else{
                addQuestionButton.setEnabled(true);
                waitTimeLabel.setText("Wait Time: 0 seconds");
                waitTimePanel.setBackground(new Color(144, 238, 144)); // Light green color
            }
        }
        else{
            rowData[0][0] = input;
            addQuestionButton.setVisible(false);
            removeQuestionButton.setVisible(true);
            positionLabel.setText("Position: " + databaseManager.getQuestionPosition(questionTableName, userName));
        }


// If the table is empty, set the default value to "No Active Question"
        if(questionTable.getValueAt(0,0).equals("")){
            questionTable.setValueAt("No Active Question", 0, 0);
        }

// Add action listeners for the buttons
        ArrayList<String[]> finalResults = results;
        addQuestionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create panel with GridBagLayout for better alignment
                JPanel panel = new JPanel(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 10, 5, 10); // Padding between components
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1; // Allow resizing horizontally

                Font font = new Font("Georgia", Font.PLAIN, 14);

                // Question Summary Input (TextArea + ScrollPane)
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.NORTHWEST;
                panel.add(createStyledLabel("Enter Question Summary:", font), gbc);

                gbc.gridx = 1;
                gbc.gridwidth = 2; // Span two columns
                gbc.fill = GridBagConstraints.BOTH; // Expand both horizontally and vertically
                JTextArea questionSummaryArea = new JTextArea(3, 30);
                questionSummaryArea.setFont(font);
                questionSummaryArea.setLineWrap(true);
                questionSummaryArea.setWrapStyleWord(true);
                JScrollPane questionScrollPane = new JScrollPane(questionSummaryArea);
                questionScrollPane.setPreferredSize(new Dimension(400,60));
                questionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                panel.add(questionScrollPane, gbc);

                // Reset layout adjustments
                gbc.gridwidth = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;

                // File Upload Section
                gbc.gridx = 0;
                gbc.gridy = 1;
                panel.add(createStyledLabel("Code File:", font), gbc);

                gbc.gridx = 1;
                JButton uploadButton = new JButton("Upload Code File");
                uploadButton.setFont(font);
                uploadButton.setEnabled(false);
                panel.add(uploadButton, gbc);



                gbc.gridy = 2;
                JLabel fileLabel = new JLabel("No file selected");
                fileLabel.setForeground(Color.RED);
                fileLabel.setFont(font);
                fileLabel.setFont(font.deriveFont(Font.BOLD)); // Make text bold
                panel.add(fileLabel, gbc);

                final File[] selectedFile = new File[1]; // Final .zip file
                ArrayList<File> selectedFilesList = new ArrayList<>();
                uploadButton.setText("Upload File");





                // Console Error Output (TextArea + ScrollPane)
                gbc.gridx = 0;
                gbc.gridy = 3;
                panel.add(createStyledLabel("Console (Error) Output:", font), gbc);

                gbc.gridx = 1;
                gbc.gridwidth = 2;
                gbc.fill = GridBagConstraints.BOTH;
                JTextArea consoleErrorArea = new JTextArea(5, 30);
                consoleErrorArea.setLineWrap(true);
                consoleErrorArea.setWrapStyleWord(true);
                consoleErrorArea.setFont(font);
                JScrollPane consoleScrollPane = new JScrollPane(consoleErrorArea);
                consoleScrollPane.setPreferredSize(new Dimension(400, 100)); // Fix size
                consoleScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                panel.add(consoleScrollPane, gbc);
                consoleErrorArea.setForeground(Color.RED);
                consoleErrorArea.setEnabled(false);
                consoleErrorArea.setBackground(Color.lightGray);

                // Set the initial placeholder
                questionSummaryArea.setText("You Will Lose Inputted Data Attempting to Add Question Without Question Summary");
                questionSummaryArea.setForeground(Color.GRAY); // Grey color for placeholder

// Add a focus listener to handle user interaction with the text area
                questionSummaryArea.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        // When the user clicks on the text area, clear the placeholder
                        if (questionSummaryArea.getText().equals("You Will Lose Inputted Data Attempting to Add Question Without Question Summary")) {
                            questionSummaryArea.setText("");
                            questionSummaryArea.setForeground(Color.BLACK); // Reset text color to black when typing
                        }
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        // If the user leaves the text area empty, restore the placeholder text
                        if (questionSummaryArea.getText().trim().isEmpty()) {
                            questionSummaryArea.setText("You Will Lose Inputted Data Attempting to Add Question Without Question Summary");
                            questionSummaryArea.setForeground(Color.GRAY); // Set color to grey for placeholder
                        }
                    }
                });

                frame.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        // Check if the mouse was pressed outside the questionSummaryArea
                        if (!questionSummaryArea.contains(e.getPoint())) {
                            System.out.println("Clicked outside the questionSummaryArea");
                            if (questionSummaryArea.getText().trim().isEmpty()) {
                                questionSummaryArea.setText("You Will Lose Inputted Data Attempting to Add Question Without Question Summary");
                                questionSummaryArea.setForeground(Color.RED);  // Placeholder text color
                            }
                        } else {
                            System.out.println("Clicked inside the questionSummaryArea");
                        }
                    }
                });

// Update the button state based on whether the text area is empty
                questionSummaryArea.getDocument().addDocumentListener(new DocumentListener() {
                    private void updateButtonState() {
                        boolean isEmpty = questionSummaryArea.getText().trim().isEmpty();
                        uploadButton.setEnabled(!isEmpty);
                        consoleErrorArea.setEnabled(!isEmpty);

                        // Show red warning if the area is empty when attempting to submit
                        if (isEmpty) {
                            questionSummaryArea.setForeground(Color.RED);
                        } else {
                            questionSummaryArea.setForeground(Color.BLACK); // Reset to normal color
                        }
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateButtonState();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateButtonState();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateButtonState();
                    }
                });




                uploadButton.addActionListener(e1 -> {
                    JFileChooser fileChooser = new JFileChooser();
                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        File chosenFile = fileChooser.getSelectedFile();
                        selectedFilesList.add(chosenFile);

                        // File preview logic (same as before)
                        try (BufferedReader reader = new BufferedReader(new FileReader(chosenFile))) {
                            StringBuilder previewText = new StringBuilder("<html><body style='background-color:#1e1e1e; color:#00ff00; font-family:Courier New; padding:10px;'>");
                            String line;
                            int maxLineLength = 0;

                            while ((line = reader.readLine()) != null) {
                                previewText.append("&nbsp;&nbsp;").append(line.replace(" ", "&nbsp;")).append("<br>");
                                maxLineLength = Math.max(maxLineLength, line.length());
                            }
                            previewText.append("</body></html>");

                            JLabel previewLabel = new JLabel(previewText.toString());
                            JScrollPane previewScrollPane = new JScrollPane(previewLabel);
                            previewScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                            previewScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                            int panelWidth = Math.min(800, maxLineLength * 8 + 50);
                            int panelHeight = 400;

                            previewScrollPane.setPreferredSize(new Dimension(panelWidth, panelHeight));
                            previewScrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

                            JPanel previewPanel = new JPanel(new BorderLayout());
                            previewPanel.setBackground(Color.BLACK);
                            previewPanel.add(previewScrollPane, BorderLayout.CENTER);

                            JOptionPane.showMessageDialog(null, previewPanel, "📜 Code Preview", JOptionPane.PLAIN_MESSAGE);

                            successful_1 = true;

                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(null, "Error reading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            successful_1 = false;
                            selectedFilesList.remove(selectedFilesList.size()-1);
                        }

                        if(successful_1) {
                            // Update label with selected file names
                            String fileNames = selectedFilesList.stream()
                                    .map(File::getName)
                                    .collect(Collectors.joining(", "));
                            fileLabel.setText("File Name(s): " + fileNames);

                            // Change button text
                            uploadButton.setText("Upload Another File");
                            consoleErrorArea.setForeground(Color.BLACK);
                            consoleErrorArea.setEnabled(true);
                            consoleErrorArea.setBackground(Color.WHITE);
                        }
                    }
                });

                // Show Dialog
                int result = JOptionPane.showConfirmDialog(null, panel, "Add Question", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String questionSummary = questionSummaryArea.getText().trim();
                    String consoleErrorOutput = consoleErrorArea.getText().trim();
                    byte[] fileBytes = null; // To store file as bytes
// Check if the questionSummary is empty
                    if (questionSummary.isEmpty() || questionSummary.equals("You Will Lose Inputted Data Attempting to Add Question Without Question Summary")) {
                        // Show an error message
                        JOptionPane.showMessageDialog(null, "Question summary cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);

                        // Ensure data is preserved (no need to reset fields if user hasn't changed anything)
                        questionSummaryArea.setText(questionSummary);
                        consoleErrorArea.setText(consoleErrorOutput);

                        // Return early to avoid further processing, but keep OptionPane open
                        return; // OptionPane stays open, allowing the user to fill out the summary again
                    }

                    try {
                        Pattern pattern1 = Pattern.compile("\\b(\\d+)(st|nd|rd|th)\\b");
                        Matcher matcher1 = pattern1.matcher(className);

                        if (matcher1.find()) {
                            periodNumber = Integer.parseInt(matcher1.group(1)); // Convert to int
                            System.out.println("Period Number: " + periodNumber);
                        } else {
                            System.out.println("Period number not found.");
                        }
                        String tableName = "Test";
                        for (int i = 0; i < finalResults.size(); i++) {
                            try {
                                LocalTime startTime = LocalTime.parse(finalResults.get(i)[2]);
                                LocalTime endTime = LocalTime.parse(finalResults.get(i)[3]);

                                if (LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime)) {
                                    tableName = finalResults.get(i)[0];

                                    if (tableName.matches(".*_\\d+_main")) {
                                        tableName = tableName.replace("_main", "_students");
                                    }
                                }
                            } catch (DateTimeParseException e1) {
                                System.out.println("Invalid time format in results: " + e1.getMessage());
                            }
                        }

                        String nName = databaseManager.getStudentName(userName, tableName);


//                    if (selectedFilesList.isEmpty()) {
//                        JOptionPane.showMessageDialog(null, "No files selected.", "Warning", JOptionPane.WARNING_MESSAGE);
//                        return;
//                    }
                        // Create a temporary ZIP file
                        File zipFile = File.createTempFile(nName + "_", ".zip");
                        //File zipFile = new File(userName + ".zip"); //FIX THISSS!!!
                        try (FileOutputStream fos = new FileOutputStream(zipFile);
                             ZipOutputStream zipOut = new ZipOutputStream(fos)) {

                            for (File file : selectedFilesList) {
                                try (FileInputStream fis = new FileInputStream(file)) {
                                    ZipEntry zipEntry = new ZipEntry(file.getName());
                                    zipOut.putNextEntry(zipEntry);

                                    byte[] bytes = new byte[1024];
                                    int length;
                                    while ((length = fis.read(bytes)) >= 0) {
                                        zipOut.write(bytes, 0, length);
                                    }
                                    zipOut.closeEntry();
                                }
                            }
                        }

                        // Set the final .zip file to selectedFile[0]
                        selectedFile[0] = zipFile;
                        //JOptionPane.showMessageDialog(null, "Files zipped successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error creating ZIP file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }

                    if (selectedFile[0] != null) {
                        try {
                            fileBytes = Files.readAllBytes(selectedFile[0].toPath()); // Read file as byte array
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(null, "Failed to read file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    if (!questionSummary.isEmpty()) {
                        questionTable.setValueAt(questionSummary, 0, 0);
                        System.out.println("Question Added: " + questionSummary);

                        ArrayList<String[]> results = null;
                        try {
                            results = databaseManager.checkNameInStudentsTables(userName);
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }

                        if (results.isEmpty()) {
                            System.out.println("No records found for the student.");
                        }

                        for (int i = 0; i < results.size(); i++) {
                            try {
                                LocalTime startTime = LocalTime.parse(results.get(i)[2]);
                                LocalTime endTime = LocalTime.parse(results.get(i)[3]);

                                if (LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime)) {
                                    String tableName = results.get(i)[0];

                                    if (tableName.matches(".*_\\d+_main")) {
                                        tableName = tableName.replace("_main", "_questions");
                                    }

                                    if (selectedFilesList.isEmpty()) {
                                        // Insert into database
                                        DatabaseManager.addRecordToTable(tableName, userName, questionSummary, null, consoleErrorOutput, "No File(s) Attached");
                                        positionLabel.setText("Position: " + databaseManager.getQuestionPosition(tableName, userName));
                                    } else{
                                        // Insert into database
                                        DatabaseManager.addRecordToTable(tableName, userName, questionSummary, fileBytes, consoleErrorOutput, selectedFile[0].getName());
                                        positionLabel.setText("Position: " + databaseManager.getQuestionPosition(tableName, userName));
                                    }
                                }
                            } catch (DateTimeParseException e1) {
                                System.out.println("Invalid time format in results: " + e1.getMessage());
                            }
                        }
                    }
                }
                addQuestionButton.setVisible(false);
                removeQuestionButton.setVisible(true);
            }
        });

        removeQuestionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Check the value in the table
                String currentQuestion = (String) questionTable.getValueAt(0, 0); // Assuming only one row in the table
                if ("No Active Question".equals(currentQuestion)) {
                    // Create a custom panel with a JLabel for the message
                    JPanel panel = new JPanel();
                    JLabel label = new JLabel("No Question to Remove.", JLabel.CENTER);
                    label.setFont(new Font("Georgia", Font.BOLD, 14));  // Set font to Georgia
                    panel.add(label);

                    // Show the dialog with the custom panel
                    JOptionPane.showMessageDialog(null, panel, "Error", JOptionPane.ERROR_MESSAGE);
                } else {

                ArrayList<String[]> results2 = null;
                try {
                    results2 = databaseManager.checkNameInStudentsTables(userName);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                if (results2.isEmpty()) {
                    System.out.println("No records found for the student.");
                }
                for (int i = 0; i < results2.size(); i++) {
                    try {
                        // Convert the strings in results[2] and results[3] to LocalTime
                        LocalTime startTime = LocalTime.parse((String) results2.get(i)[2]);  // Parse the start time string
                        LocalTime endTime = LocalTime.parse((String) results2.get(i)[3]);    // Parse the end time string

                        // Check if the current time is within the range
                        if (LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime)) {
                            String result = (String) results2.get(i)[0]; // Get the value from the array (ensure it's a String)

                            // Check if the result matches the pattern "something_X_main" and modify it
                            if (result.matches(".*_\\d+_main")) {  // Regex to match something_X_main
                                result = result.replace("_main", "_questions"); // Replace "_main" with "_questions"
                            }

                            // Assuming DatabaseManager.addRecordToTable(String tableName, String... values) works this way
                            //DatabaseManager.deactivateQuestion(result, userName, currentQuestion);
                            databaseManager.updateQuestionsTable(userName,result, "Student Took Back Question");
                            positionLabel.setText("Position: N/A");
                        }
                    } catch (DateTimeParseException e1) {
                        // Handle the case where the string cannot be parsed into a LocalTime
                        System.out.println("Invalid time format in results: " + e1.getMessage());
                    }
                }

                    // Handle the Remove Question action if there is an active question
                    System.out.println("Remove Question button clicked");

                    // Remove the question from the table (set it back to "No Active Question")
                    questionTable.setValueAt("No Active Question", 0, 0);

                    // Optional: If you want to show a confirmation message
                    JPanel panel = new JPanel();
                    JLabel label = new JLabel("Question has been removed.", JLabel.CENTER);
                    label.setFont(new Font("Georgia", Font.BOLD, 14));  // Set font to Georgia
                    panel.add(label);

                    // Show confirmation dialog
                    JOptionPane.showMessageDialog(null, panel, "Success", JOptionPane.INFORMATION_MESSAGE);

                }
                addQuestionButton.setVisible(true);
                removeQuestionButton.setVisible(false);
                startCountdown(waitTimeLabel, Integer.parseInt(waitTimeOfClass), waitTimePanel, addQuestionButton);
                addQuestionButton.setEnabled(false);
            }
        });

// Revalidate and repaint the panel to ensure changes are reflected
        revalidate();
        repaint();

// ListSelectionListener to highlight row selection
        questionTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = questionTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        // Highlight the selected row
                        // Set the selection background to the system's default selection color
                        Color selectionBackground = UIManager.getColor("Table.selectionBackground");
                        questionTable.setSelectionBackground(selectionBackground);
                        questionTable.setSelectionBackground(selectionBackground); // Change the background color for selection
                    } else {
                        // Remove highlight if no row is selected
                        questionTable.setSelectionBackground(Color.WHITE);  // Set to default when nothing is selected
                    }
                }
            }
        });

// Add MouseListener to the parent panel to deselect the row if anywhere else is clicked
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Deselect the row when clicking anywhere else (not on the table)
                if (questionTable.getSelectedRow() != -1) {
                    questionTable.clearSelection();
                    questionTable.setSelectionBackground(Color.WHITE);  // Set the background to white when deselected
                }
            }
        });

    }

    /*private void keepPositionUpdated(String questionTableName, String userName, JLabel positionLabel){
        positionUpdateTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                positionLabel.setText("Position: " + databaseManager.getQuestionPosition(questionTableName, userName));
            }
        });

        // Start the countdown timer
        positionUpdateTimer.start();
    }*/
    private static JLabel createStyledLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font.deriveFont(Font.BOLD)); // Make text bold
        return label;
    }


    // Method to start the countdown and update the label
    // Start Countdown method
    private void startCountdown(JLabel waitTimeLabel, int startTimeInSeconds, JPanel waitTimePanel, JButton addQuestionButton) {
        // Create and store the Timer so it can be stopped later
        countdownTimer = new Timer(1000, new ActionListener() {
            private int remainingTime = startTimeInSeconds;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (remainingTime >= 0) {
                    if (remainingTime > 10) {
                        waitTimePanel.setBackground(new Color(255, 182, 193)); // Light red (pinkish) color
                    }

                    // Update the label with the current remaining time
                    waitTimeLabel.setText("Wait Time: " + remainingTime + " seconds");

                    // Change the text color to red when the time is 10 seconds
                    if (remainingTime == 10) {
                        waitTimePanel.setBackground(new Color(144, 238, 144)); // Light green color
                    }

                    remainingTime--;  // Decrement the time by 1 second
                    String tableString = userName + "_" + "waittime";
                    String columnName = "WaitTime_" + periodNumber;
                    databaseManager.insertOrUpdateWaitTime(tableString, columnName, remainingTime);
                } else {
                    // Stop the timer when the countdown reaches zero
                    ((Timer) e.getSource()).stop();
                    waitTimeLabel.setText("Wait Time: 0 seconds");  // Optional: Show "0 seconds" at the end
                    String tableString = userName + "_" + "waittime";
                    String columnName = "WaitTime_" + periodNumber;
                    databaseManager.insertOrUpdateWaitTime(tableString, columnName, 0);
                    // Optional: Reset the text color back to black after countdown ends
                    waitTimeLabel.setForeground(Color.BLACK);
                    addQuestionButton.setEnabled(true);  // Enable the add question button when timer ends
                }
            }
        });

        // Start the countdown timer
        countdownTimer.start();
    }

    // Method to stop the countdown timer (e.g., triggered by the Home button)
    private void stopCountdown() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();  // Stop the countdown timer
        }
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

    public static int extractNumberFromTableName(String tableName) {
        // Split the table name by underscores
        String[] parts = tableName.split("_");

        // The second element (index 1) will be the number part
        if (parts.length >= 3) {
            try {
                return Integer.parseInt(parts[1]); // Return the second part as an integer
            } catch (NumberFormatException e) {
                System.out.println("Error: The value between underscores is not a valid number.");
                return -1;
            }
        } else {
            System.out.println("Invalid table name format: " + tableName);
            return -1; // Return -1 if the format is incorrect
        }
    }
}
