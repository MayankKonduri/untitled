import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeacherPeriodView extends JPanel {
    public JFrame frame;
    public String period;
    public JButton addButton = new JButton("Add");
    public JButton importButton = new JButton("Import");
    public JButton removeButton = new JButton("Remove");
    public JButton editButton = new JButton("Edit");
    public JButton infoButton = new JButton("Info");
    DatabaseManager dbManager = new DatabaseManager();
    String userName = System.getProperty("user.name");
    JTextField classNameField;
    JFormattedTextField startTimeField;
    JFormattedTextField endTimeField;
    JTable studentTable = new JTable();
    DefaultTableModel tableModel;

    String studentId;
    String firstName;
    String lastName;
    String nickname;
    public TeacherPeriodView(JFrame jFrame, int period) {


        this.frame = jFrame;
        this.period = String.valueOf(period);
        setLayout(null);

        // Home button and panel
        JPanel homePanel = new JPanel();
        homePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton homeButton = new JButton("Home");
        homeButton.setFont(new Font("Georgia", Font.BOLD, 10));
        homePanel.add(homeButton);

        homePanel.setBounds(10, 10, 100, 40);
        add(homePanel);

        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Check if the fields and table are empty or not
                boolean isClassNameEmpty = classNameField.getText().isEmpty();
                boolean isStartTimeEmpty = startTimeField.getText().trim().isEmpty() || startTimeField.getText().equals("  :  ");
                boolean isEndTimeEmpty = endTimeField.getText().trim().isEmpty() || endTimeField.getText().equals("  :  ");
                boolean isTableEmpty = tableModel.getRowCount() == 0;

                // Debugging output
                System.out.println(isClassNameEmpty);
                System.out.println(isStartTimeEmpty);
                System.out.println(isEndTimeEmpty);
                System.out.println(isTableEmpty);

                // Check if all fields are either empty or all are full
                if ((isClassNameEmpty && isStartTimeEmpty && isEndTimeEmpty && isTableEmpty) ||
                        (!isClassNameEmpty && !isStartTimeEmpty && !isEndTimeEmpty && !isTableEmpty)) {
                    // If none or all fields are filled, proceed to the new page
                    TeacherCourses teacherCourses = new TeacherCourses(frame);
                    frame.getContentPane().removeAll();
                    frame.getContentPane().add(teacherCourses);
                    frame.revalidate();
                    frame.repaint();
                    System.out.println("New page loaded.");
                } else {
                    // If some fields are filled but not all of them, do not proceed
                    if (!isClassNameEmpty && !isStartTimeEmpty && !isEndTimeEmpty) {
                        // All three fields have values, proceed to new page
                        TeacherCourses teacherCourses = new TeacherCourses(frame);
                        frame.getContentPane().removeAll();
                        frame.getContentPane().add(teacherCourses);
                        frame.revalidate();
                        frame.repaint();
                        System.out.println("New page loaded.");
                    } else if (!isTableEmpty && !isClassNameEmpty && !isStartTimeEmpty && !isEndTimeEmpty) {
                        // If table has value and all fields are filled, proceed to new page
                        TeacherCourses teacherCourses = new TeacherCourses(frame);
                        frame.getContentPane().removeAll();
                        frame.getContentPane().add(teacherCourses);
                        frame.revalidate();
                        frame.repaint();
                        System.out.println("New page loaded.");
                    } else {
                        // If conditions aren't met (not all fields or table are filled), do nothing
                        System.out.println("Cannot proceed to new page. Fields are not valid.");
                    }
                }
            }
        });

        // Title and input fields
        JLabel titleLabel = new JLabel("Period " + period + " - Class Name:");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 12));
        titleLabel.setBounds(110, 25, 200, 30);
        add(titleLabel);

        classNameField = new JTextField(15);
        classNameField.setFont(new Font("Georgia", Font.BOLD, 8));
        classNameField.setBounds(260, 30, 95, 25);
        add(classNameField);

        JLabel durationLabel = new JLabel("Duration:");
        durationLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
        durationLabel.setBounds(80, 60, 60, 25);
        add(durationLabel);

        startTimeField = createTimeField();
        startTimeField.setBounds(150, 60, 60, 25);
        add(startTimeField);

        JLabel toLabel = new JLabel("to");
        toLabel.setFont(new Font("Georgia", Font.PLAIN, 12));
        toLabel.setBounds(220, 60, 20, 25);
        add(toLabel);

        endTimeField = createTimeField();
        endTimeField.setBounds(250, 60, 60, 25);
        add(endTimeField);

        // Table setup
        // Create a table model with 4 columns: StudentID, FirstName, LastName, and Nickname
        // Create the table model with 2 columns: Student ID and Nickname
// Create the table model with 2 columns: Student ID and Nickname
        tableModel = new DefaultTableModel(new Object[]{"Student ID", "Nickname"}, 0) {
            // Override the isCellEditable method to make all cells non-editable
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Return false for every cell to make them non-editable
            }
        };// Set the model for the studentTable
        studentTable.setModel(tableModel);

// Set font for the table
        studentTable.setFont(new Font("Georgia", Font.PLAIN, 12));

// Set row height
        studentTable.setRowHeight(20);

// Set the font and appearance of the table header
        studentTable.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 12));
        studentTable.getTableHeader().setBackground(Color.LIGHT_GRAY);
        studentTable.getTableHeader().setForeground(Color.BLACK);

// Center-align both columns: Student ID and Nickname
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

// Apply center alignment to both columns
        studentTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);  // Student ID column
        studentTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);  // Nickname column

// Add the table to a JScrollPane for scrolling functionality
        JScrollPane tableScrollPane = new JScrollPane(studentTable);
        tableScrollPane.setBounds(55, 100, 300, 125);
        add(tableScrollPane);

        String[] temp = dbManager.getTeacher(userName); // Fetch teacher details
        String teacherName = temp[1];
        teacherName = teacherName.replaceAll("^(Mr\\.\\s*|Ms\\.\\s*|Mrs\\.\\s*)", "").trim();
        String studentTableName = teacherName + "_" + period + "_Students";

        try {
            // Fetch the student data from the database (StudentID, FirstName, LastName, Nickname)
            String[][] students = dbManager.getTeacherStudents(studentTableName);

            if (students != null) {
                for (String[] student : students) {
                    // Assuming student[0] is StudentID (index 0), student[1] is FirstName, student[2] is LastName, student[3] is Nickname
                    String studentId = student[0];  // Extract StudentID
                    System.out.println("Type" + student[3]);
                    // Add each student ID to the table model (can add other fields if needed)
                    tableModel.addRow(new Object[]{studentId, student[3]});
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Error fetching students: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }


        // Button setup
        addButton.setFont(new Font("Georgia", Font.PLAIN, 12));
        addButton.setBounds(55, 240, 145, 30);
        add(addButton);

        importButton.setFont(new Font("Georgia", Font.PLAIN, 12));
        importButton.setBounds(210, 240, 145, 30);
        add(importButton);

        infoButton.setFont(new Font("Georgia", Font.PLAIN, 12));
        infoButton.setBounds(55, 240, 145, 30);
        add(infoButton);

        removeButton.setFont(new Font("Georgia", Font.PLAIN, 12));
        removeButton.setBounds(210, 240, 145, 30);
        removeButton.setVisible(false);
        add(removeButton);

        // List selection listener for the table to handle row selection
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (studentTable.getSelectedRow() == -1) {
                    // No row selected: Show Add and Import buttons
                    addButton.setVisible(true);
                    importButton.setVisible(true);
                    editButton.setVisible(false);
                    removeButton.setVisible(false);
                } else {
                    // Row selected: Show Edit and Remove buttons
                    addButton.setVisible(false);
                    importButton.setVisible(false);
                    infoButton.setVisible(true);
                    removeButton.setVisible(true);

                    // Get the selected row's Student ID (first column)
                    int selectedRow = studentTable.getSelectedRow();
                    String studentId = (String) studentTable.getValueAt(selectedRow, 0);  // 0 is the index of the "Student ID" column

                    // Remove the previous action listener (if there was one)
                    for (ActionListener al : infoButton.getActionListeners()) {
                        infoButton.removeActionListener(al);
                    }

                    // Add a new action listener to the infoButton
                    infoButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // Pass the studentId to the infoButtonPopUp method
                            infoButtonPopUp(studentId, studentTableName);
                            studentTable.clearSelection(); // Unselect the row
                        }
                    });
                }
            }
        });


        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] temp1 = dbManager.getTeacher(userName);
                String teacherName = temp1[1];
                teacherName = teacherName.replaceAll("^(Mr\\.\\s*|Ms\\.\\s*|Mrs\\.\\s*)", "").trim();
                String mainTable = teacherName + "_" + period + "_Students";

                // Show an input dialog to get the Student ID, First Name, Last Name, and Nickname
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                // Create a font for the components
                Font georgiaFont = new Font("Georgia", Font.PLAIN, 12);
                Font boldGeorgiaFont = new Font("Georgia", Font.BOLD, 12);  // For bold text

                // Create and set font for the labels and text fields
                JLabel studentIdLabel = new JLabel("Enter Student ID:");
                studentIdLabel.setFont(boldGeorgiaFont);  // Bold for label
                JTextField studentIdField = new JTextField(20);
                studentIdField.setFont(georgiaFont);  // Plain font for text field

                JLabel firstNameLabel = new JLabel("Enter First Name:");
                firstNameLabel.setFont(boldGeorgiaFont);
                JTextField firstNameField = new JTextField(20);
                firstNameField.setFont(georgiaFont);

                JLabel lastNameLabel = new JLabel("Enter Last Name:");
                lastNameLabel.setFont(boldGeorgiaFont);
                JTextField lastNameField = new JTextField(20);
                lastNameField.setFont(georgiaFont);

                JLabel nicknameLabel = new JLabel("Enter Nickname:");
                nicknameLabel.setFont(boldGeorgiaFont);
                JTextField nicknameField = new JTextField(20);
                nicknameField.setFont(georgiaFont);

                // Add components to the panel
                panel.add(studentIdLabel);
                panel.add(studentIdField);
                panel.add(firstNameLabel);
                panel.add(firstNameField);
                panel.add(lastNameLabel);
                panel.add(lastNameField);
                panel.add(nicknameLabel);
                panel.add(nicknameField);

                // Show the dialog box with the panel
                int option = JOptionPane.showConfirmDialog(frame, panel, "Add Student", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (option == JOptionPane.OK_OPTION) {
                    // Retrieve the input values
                    String studentId = studentIdField.getText().trim();
                    String firstName = firstNameField.getText().trim();
                    String lastName = lastNameField.getText().trim();
                    String nickname = nicknameField.getText().trim();

                    // Check if the input is valid (not null or empty)
                    if (!studentId.isEmpty() && !firstName.isEmpty() && !lastName.isEmpty() && !nickname.isEmpty()) {
                        try {
                            // Check if the student already exists in the table
                            Boolean doubleStudent = dbManager.checkValueInTable(mainTable, studentId);
                            if (!doubleStudent) {
                                // Add the student ID to the table model (GUI table)
                                tableModel.addRow(new Object[]{studentId, nickname});

                                // Call the method to update the database with the student details
                                try {
                                    dbManager.updateTeacherStudents(mainTable, studentId, firstName, lastName, nickname);
                                } catch (SQLException ex) {
                                    throw new RuntimeException("Error inserting student into database", ex);
                                }
                            } else {
                                // Show an error message if the student already exists
                                JOptionPane.showMessageDialog(frame, "Student ID Already Exists in Table", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            throw new RuntimeException("Error checking student in database", ex);
                        }
                    } else {
                        // Show an error message if any of the fields are empty
                        JOptionPane.showMessageDialog(frame, "All fields must be filled.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });



        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String[]> studentsImported = importStudents();

                for(String[] t :studentsImported){
                    tableModel.addRow(new Object[]{t[0].trim(), t[3].trim()});
                    String[] temp1 = dbManager.getTeacher(userName);
                    String teacherName = temp1[1];
                    teacherName = teacherName.replaceAll("^(Mr\\.\\s*|Ms\\.\\s*|Mrs\\.\\s*)", "").trim();
                    String mainTable = teacherName + "_" + period + "_Students";
                    try {
                        dbManager.updateTeacherStudents(mainTable, t[0].trim(), t[1].trim(), t[2].trim(), t[3].trim());
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = studentTable.getSelectedRow();
                if (selectedRow != -1) {
                    String studentId = studentTable.getValueAt(selectedRow,0).toString().trim();
                    tableModel.removeRow(selectedRow);
                    toggleButtonVisibility(false); // Show Add/Import buttons again
                    String[] temp2 = dbManager.getTeacher(userName);
                    String teacherName = temp2[1];
                    teacherName = teacherName.replaceAll("^(Mr\\.\\s*|Ms\\.\\s*|Mrs\\.\\s*)", "").trim();
                    String mainTable1 = teacherName + "_" + period + "_Students";
                    try {
                        dbManager.removeTeacherStudents(mainTable1, studentId);
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });



        // Deselect rows when clicking anywhere outside the table
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (!studentTable.getBounds().contains(evt.getPoint())) {
                    studentTable.clearSelection();
                    addButton.setVisible(true);
                    importButton.setVisible(true);
                    editButton.setVisible(false);
                    removeButton.setVisible(false);
                }
            }
        });

        // Prefill data from the database
        String[] temp3 = dbManager.getTeacher(userName);
        String teacherName3 = temp3[1];
        teacherName3 = teacherName3.replaceAll("^(Mr\\.\\s*|Ms\\.\\s*|Mrs\\.\\s*)", "").trim();
        String mainTable = teacherName3 + "_" + period + "_Main";

        String[] mainData = dbManager.getTeacherMainData(mainTable);
        if (mainData != null) {
            classNameField.setText(mainData[0]);
            startTimeField.setText(mainData[1]);
            endTimeField.setText(mainData[2]);
            classNameField.setCaretPosition(0);
        }

        // Add listeners to update the database
        addUpdateListeners(classNameField, startTimeField, endTimeField, mainTable);
    }

    private void infoButtonPopUp(String studentId, String studentTableName) {
        try {
            // Fetch the student data from the database (StudentID, FirstName, LastName, Nickname)
            String[][] students = dbManager.getTeacherStudents(studentTableName);

            if (students != null) {
                for (String[] student : students) {
                    // Assuming student[0] is StudentID, student[1] is FirstName, student[2] is LastName, student[3] is Nickname
                    String studentId1 = student[0];  // Extract StudentID
                    if (studentId1.equals(studentId)) {
                        // If the Student ID matches, create the message for the popup
                        String lastName = student[2];  // LastName at index 2
                        String firstName = student[1]; // FirstName at index 1
                        String nickname = student[3];  // Nickname at index 3

                        // Create the formatted message using JTextPane and StyledDocument
                        JTextPane textPane = new JTextPane();
                        textPane.setContentType("text/plain");
                        textPane.setEditable(false);
                        textPane.setFont(new Font("Georgia", Font.PLAIN, 12)); // Set Georgia font for the entire text

                        // Create a styled document to apply different styles to different parts
                        StyledDocument doc = textPane.getStyledDocument();

                        // Define the styles
                        SimpleAttributeSet boldStyle = new SimpleAttributeSet();
                        StyleConstants.setBold(boldStyle, true);
                        StyleConstants.setFontFamily(boldStyle, "Georgia");

                        SimpleAttributeSet regularStyle = new SimpleAttributeSet();
                        StyleConstants.setBold(regularStyle, false);
                        StyleConstants.setFontFamily(regularStyle, "Georgia");

                        // Insert the message parts with different styles
                        doc.insertString(doc.getLength(), "ID: ", boldStyle);  // Bold "ID:"
                        doc.insertString(doc.getLength(), studentId1 + "\n", regularStyle); // Normal Student ID

                        doc.insertString(doc.getLength(), "Full Name: ", boldStyle);  // Bold "Full Name:"
                        doc.insertString(doc.getLength(), lastName + ", " + firstName + "\n", regularStyle); // Normal Full Name

                        doc.insertString(doc.getLength(), "Nickname: ", boldStyle);  // Bold "Nickname:"
                        doc.insertString(doc.getLength(), nickname, regularStyle); // Normal Nickname

                        // Show the popup with the student details in the JTextPane
                        JOptionPane.showMessageDialog(frame, new JScrollPane(textPane), "Student Info", JOptionPane.INFORMATION_MESSAGE);

                        // Deselect the row after showing the popup
                        studentTable.clearSelection();

                        return; // Exit after displaying the popup
                    }
                }
                // If no matching student is found
                JOptionPane.showMessageDialog(frame, "Student ID not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException | BadLocationException ex) {
            JOptionPane.showMessageDialog(frame, "Error fetching students: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }




    private void toggleButtonVisibility(boolean rowSelected) {
        if (rowSelected) {
            addButton.setVisible(false);
            importButton.setVisible(false);
            editButton.setVisible(true);
            removeButton.setVisible(true);
        } else {
            addButton.setVisible(true);
            importButton.setVisible(true);
            editButton.setVisible(false);
            removeButton.setVisible(false);
        }
    }

    private JFormattedTextField createTimeField() {
        JFormattedTextField timeField = null;
        try {
            timeField = new JFormattedTextField(new javax.swing.text.MaskFormatter("##:##"));
            timeField.setFont(new Font("Georgia", Font.PLAIN, 12));
            timeField.setHorizontalAlignment(JTextField.CENTER);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeField;
    }

    private void addUpdateListeners(JTextField classNameField, JFormattedTextField startTimeField, JFormattedTextField endTimeField, String tableName) {
        classNameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                dbManager.updateTeacherMain(tableName, classNameField.getText(), startTimeField.getText(), endTimeField.getText());
            }
        });

        startTimeField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String startTime = startTimeField.getText().trim();
                String endTime = endTimeField.getText().trim();

                // Check if both start time and end time are valid (not just empty or default format "  :  ")
                if (!startTime.isEmpty() && !startTime.equals("  :  ") && !endTime.isEmpty() && !endTime.equals("  :  ")) {
                    // Compare times if both are valid
                    if (isStartTimeAfterEndTime(startTime, endTime)) {
                        // Show error popup if start time is after end time
                        JOptionPane.showMessageDialog(null, "Start Time cannot be after End Time.", "Time Error", JOptionPane.ERROR_MESSAGE);
                        startTimeField.setText(""); // Clear the start time field
                    } else {
                        // Call the update method only if both fields have valid values
                        dbManager.updateTeacherMain(tableName, classNameField.getText(), startTime, endTime);
                    }
                } else {
                    System.out.println("Start Time or End Time is empty or invalid. Not updating.");
                }
            }
        });

        endTimeField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String startTime = startTimeField.getText().trim();
                String endTime = endTimeField.getText().trim();

                // Check if both start time and end time are valid (not just empty or default format "  :  ")
                if (!startTime.isEmpty() && !startTime.equals("  :  ") && !endTime.isEmpty() && !endTime.equals("  :  ")) {
                    // Compare times if both are valid
                    if (isStartTimeAfterEndTime(startTime, endTime)) {
                        // Show error popup if start time is after end time
                        JOptionPane.showMessageDialog(null, "Start Time cannot be after End Time, Values Have Been Erased Try Again", "Time Error", JOptionPane.ERROR_MESSAGE);
                        endTimeField.setText(""); // Clear the end time field
                    } else {
                        // Call the update method only if both fields have valid values
                        dbManager.updateTeacherMain(tableName, classNameField.getText(), startTime, endTime);
                    }
                } else {
                    System.out.println("Start Time or End Time is empty or invalid. Not updating.");
                }
            }
        });
    }

    // Helper method to compare startTime and endTime
    private boolean isStartTimeAfterEndTime(String startTime, String endTime) {
        try {
            // Parse the times into LocalTime objects
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm"); // Assuming time format is HH:mm
            LocalTime start = LocalTime.parse(startTime, formatter);
            LocalTime end = LocalTime.parse(endTime, formatter);

            // Return true if startTime is after endTime, otherwise false
            return start.isAfter(end);
        } catch (Exception e) {
            // If there's an error parsing the time, return false
            return false;
        }
    }

    private ArrayList<String[]> importStudents() {
        ArrayList<String[]> tempStudents = new ArrayList<>();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a CSV File");

        // Set file filter to allow only .csv files
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(".csv");
            }

            @Override
            public String getDescription() {
                return "CSV Files (.csv)";
            }
        });

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            if (selectedFile.getName().toLowerCase().endsWith(".csv")) {
                try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                    List<List<String>> rows = new ArrayList<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] values = line.split(",");
                        rows.add(Arrays.asList(values)); // Each row is a list of values
                    }

                    List<Integer> selectedColumns = showColumnSelectionDialog(rows);

                    if (selectedColumns != null) {
                        boolean previewConfirmed = showPreviewDialog(rows, selectedColumns);
                        if (previewConfirmed) {
                            for (List<String> row : rows) {
                                List<String> selectedRow = new ArrayList<>();
                                for (int colIndex : selectedColumns) {
                                    if (colIndex < row.size()) {
                                        selectedRow.add(row.get(colIndex));
                                    }
                                }
                                tempStudents.add(selectedRow.toArray(new String[0]));
                            }
                            return tempStudents;
                        } else {
                            // If preview was canceled, restart the column selection dialog
                            return importStudents(); // Recursively call to restart the column selection
                        }
                    }

                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error reading the file: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select a valid CSV file.",
                        "Invalid File", JOptionPane.ERROR_MESSAGE);
            }
        }
        return tempStudents;
    }

    private boolean showPreviewDialog(List<List<String>> rows, List<Integer> selectedColumns) {
        List<String[]> previewData = new ArrayList<>();
        int maxRows = Math.min(3, rows.size());
        for (int i = 0; i < maxRows; i++) {
            List<String> row = rows.get(i);
            String[] previewRow = new String[2];
            previewRow[0] = row.size() > 0 ? row.get(0) : "";  // First column (Student ID)
            previewRow[1] = row.size() > 3 ? row.get(3) : "";  // Fourth column (Nickname)
            previewData.add(previewRow);
        }

        // Add "..." for the 4th row if there are more than 3 rows
        if (rows.size() > 3) {
            String[] previewRow = new String[2];
            previewRow[0] = "...";
            previewRow[1] = "...";
            previewData.add(previewRow);
        }

        // Create the table for preview with Georgia font
        String[] columnHeaders = {"Student ID", "Nickname"};
        JTable previewTable = new JTable(previewData.toArray(new String[0][]), columnHeaders);
        previewTable.setPreferredScrollableViewportSize(new java.awt.Dimension(400, 64)); // Fit the table height to the rows
        previewTable.setFillsViewportHeight(true);

        // Center the content in the table
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        previewTable.setDefaultRenderer(Object.class, centerRenderer);
        previewTable.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 12));
        previewTable.setFont(new Font("Georgia", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(previewTable);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Arrange components vertically
        panel.add(scrollPane);

        // Add 10 units of space between the table and the disclaimer
        panel.add(Box.createVerticalStrut(10));

        // Add the red disclaimer at the bottom with Georgia font
        JLabel disclaimer = new JLabel("<html><font color='red' face='Georgia'>Disclaimer: First Name and Last Name can be found using the 'Info' button.</font></html>");
        panel.add(disclaimer);

        // Show the preview dialog with the table and disclaimer
        int option = JOptionPane.showConfirmDialog(null, panel, "Preview the Data", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.CANCEL_OPTION) {
            // If canceled (or 'x' pressed), show column selection again
            return false; // Indicating preview was canceled
        }

        return option == JOptionPane.OK_OPTION;
    }

    private List<Integer> showColumnSelectionDialog(List<List<String>> rows) {
        List<String> columnHeaders = rows.get(0);

        JCheckBox[] checkBoxes = new JCheckBox[columnHeaders.size()];
        for (int i = 0; i < columnHeaders.size(); i++) {
            checkBoxes[i] = new JCheckBox(columnHeaders.get(i));
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Arrange components vertically
        for (JCheckBox checkBox : checkBoxes) {
            panel.add(checkBox);
        }

        List<Integer> selectedColumns = null;

        // Keep prompting the user until they select exactly 4 columns
        while (selectedColumns == null || selectedColumns.size() != 4) {
            int option = JOptionPane.showConfirmDialog(null, panel, "Select Exactly 4 Columns to Import", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                selectedColumns = new ArrayList<>();
                for (int i = 0; i < checkBoxes.length; i++) {
                    if (checkBoxes[i].isSelected()) {
                        selectedColumns.add(i);
                    }
                }

                // Check if exactly 4 columns are selected
                if (selectedColumns.size() != 4) {
                    JOptionPane.showMessageDialog(null, "Please select exactly 4 columns.", "Invalid Selection", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // User pressed cancel, return null
                return null;
            }
        }
        return selectedColumns;
    }

}