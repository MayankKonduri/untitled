import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;

public class StudentHome extends JPanel {
    private JFrame frame;
    private DatabaseManager databaseManager;
    private String userName = System.getProperty("user.name");

    public String questionTableName;
    private int minutesUntilEndOfClass; // Variable to store minutes left
    private Timer timer; // Timer for updating every 60 seconds
    private JPanel topBar;  // Declare a topBar reference at the class level


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

        // Format the class display string
        String formattedDisplayString = formatClassString(classInfo, className);

        // Set the layout to null for absolute positioning
        setLayout(null);

        // Create the label with the formatted class display string
        JLabel classLabel = new JLabel(formattedDisplayString);
        classLabel.setFont(new Font("Georgia", Font.PLAIN, 14));
        classLabel.setForeground(Color.BLACK);  // Set text color for the class name

        // Calculate the position for top-center alignment
        int labelWidth = classLabel.getPreferredSize().width;
        int labelHeight = classLabel.getPreferredSize().height;

        // For a frame size of 400x325, calculate the x-position to center the label
        int xPosition = (400 - labelWidth) / 2;  // Center horizontally
        int yPosition = 32;  // Position the class label 32px from the top (after the top bar)

        // Set the position and size of the label
        classLabel.setBounds(xPosition, yPosition, labelWidth, labelHeight);
        add(classLabel);

        // Revalidate and repaint the panel to ensure changes are reflected
        revalidate();
        repaint();

        //------------------------- Header --------------------------//
        //-------------------- Question Table -----------------------//

        // Add buttons below the table
        JPanel buttonPanel = new JPanel();  // Create a new panel for buttons
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));  // Center the buttons horizontally
        buttonPanel.setBounds(15, yPosition + labelHeight + 140, 360, 40);  // Set the position of the button panel

// Create "Add Question" and "Remove Question" buttons
        JButton addQuestionButton = new JButton("Add Question");
        addQuestionButton.setFont(new Font("Georgia", Font.PLAIN, 12));
        JButton removeQuestionButton = new JButton("Remove Question");
        removeQuestionButton.setFont(new Font("Georgia", Font.PLAIN, 12));
        addQuestionButton.setBounds(15,yPosition+labelHeight+140,360,25);
        removeQuestionButton.setBounds(15,yPosition+labelHeight+140,360,25);

        add(addQuestionButton);
        add(removeQuestionButton);
        // Add buttons to the button panel
        //buttonPanel.add(addQuestionButton);
        //buttonPanel.add(removeQuestionButton);

// Add the button panel to the main panel
        //add(buttonPanel);


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

        String input = databaseManager.getQuestionStudent(questionTableName, userName);
        String[][] rowData = {
                {""}
        };
        if(input.equals("")){
            // Sample data for the table (you can replace this with your actual data)
            rowData[0][0] = "";
            addQuestionButton.setVisible(true);
            removeQuestionButton.setVisible(false);
        }
        else{
            rowData[0][0] = input;
            addQuestionButton.setVisible(false);
            removeQuestionButton.setVisible(true);
        }


// Column headers for the table
        String[] columnNames = {"Question Summary"};

// Create the JTable with sample data and column names
        JTable questionTable = new JTable(rowData, columnNames);

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
        for (int i = 0; i < questionTable.getColumnCount(); i++) {
            questionTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

// Add the table to a JScrollPane for scrollability
        JScrollPane tableScrollPane = new JScrollPane(questionTable);
        tableScrollPane.setBounds(15, yPosition + labelHeight + 70, 360, 70);  // Position it below the class label

// Set the preferred size of the table for proper display
        questionTable.setPreferredScrollableViewportSize(new Dimension(300, 100));  // Adjust as needed

// Add the scroll pane with the table to the panel
        add(tableScrollPane);

// If the table is empty, set the default value to "No Active Question"
        if(questionTable.getValueAt(0,0).equals("")){
            questionTable.setValueAt("No Active Question", 0, 0);
        }

// Add action listeners for the buttons
        addQuestionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Show a dialog asking for the Question Summary
                String questionSummary = JOptionPane.showInputDialog(null, "Enter Question Summary:", "Add Question", JOptionPane.PLAIN_MESSAGE);

                // Check if the input is not null or empty
                if (questionSummary != null && !questionSummary.trim().isEmpty()) {
                    // If the user provided a question summary
                    questionTable.setValueAt(questionSummary, 0, 0); // Set the new question summary in the table
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
                                // Convert the strings in results[2] and results[3] to LocalTime
                                LocalTime startTime = LocalTime.parse((String) results.get(i)[2]);  // Parse the start time string
                                LocalTime endTime = LocalTime.parse((String) results.get(i)[3]);    // Parse the end time string

                                // Check if the current time is within the range
                                if (LocalTime.now().isAfter(startTime) && LocalTime.now().isBefore(endTime)) {
                                    String result = (String) results.get(i)[0]; // Get the value from the array (ensure it's a String)

                                    // Check if the result matches the pattern "something_X_main" and modify it
                                    if (result.matches(".*_\\d+_main")) {  // Regex to match something_X_main
                                        result = result.replace("_main", "_questions"); // Replace "_main" with "_questions"
                                    }

                                    // Assuming DatabaseManager.addRecordToTable(String tableName, String... values) works this way
                                    DatabaseManager.addRecordToTable(result, userName, questionSummary);
                                }
                            } catch (DateTimeParseException e1) {
                                // Handle the case where the string cannot be parsed into a LocalTime
                                System.out.println("Invalid time format in results: " + e1.getMessage());
                            }
                        }

                } else {
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
                            DatabaseManager.deactivateQuestion(result, userName, currentQuestion);
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
