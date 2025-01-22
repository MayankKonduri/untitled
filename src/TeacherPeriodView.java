import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TeacherPeriodView extends JPanel {
    public JFrame frame;
    public String period;
    public JButton addButton = new JButton("Add");
    public JButton importButton = new JButton("Import");
    public JButton removeButton = new JButton("Remove");
    public JButton editButton = new JButton("Edit");
    DatabaseManager dbManager = new DatabaseManager();
    String userName = System.getProperty("user.name");
    JTextField classNameField;
    JFormattedTextField startTimeField;
    JFormattedTextField endTimeField;
    JTable studentTable = new JTable();
    DefaultTableModel tableModel;
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
        tableModel = new DefaultTableModel(new Object[]{"Student ID"}, 0);
        studentTable.setModel(tableModel);

        studentTable.setFont(new Font("Georgia", Font.PLAIN, 12));
        studentTable.setRowHeight(20);
        studentTable.getTableHeader().setFont(new Font("Georgia", Font.BOLD, 12));
        studentTable.getTableHeader().setBackground(Color.LIGHT_GRAY);
        studentTable.getTableHeader().setForeground(Color.BLACK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        studentTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        JScrollPane tableScrollPane = new JScrollPane(studentTable);
        tableScrollPane.setBounds(55, 100, 300, 125);
        add(tableScrollPane);

        String[] temp = dbManager.getTeacher(userName); // Fetch teacher details
        String teacherName = temp[1];
        teacherName = teacherName.replaceAll("^(Mr\\.\\s*|Ms\\.\\s*|Mrs\\.\\s*)", "").trim();
        String studentTableName = teacherName + "_" + period + "_Students";

        try {
            // Fetch the student IDs from the database
            String[] students = dbManager.getTeacherStudents(studentTableName);
            if (students != null) {
                for (String studentId : students) {
                    // Add each student ID to the table model
                    tableModel.addRow(new Object[]{studentId});
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

        removeButton.setFont(new Font("Georgia", Font.PLAIN, 12));
        removeButton.setBounds(55, 240, 300, 30);
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
                    editButton.setVisible(true);
                    removeButton.setVisible(true);
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

                // Show an input dialog to get the Student ID
                String studentId = JOptionPane.showInputDialog(frame, "Enter Student ID:", "Add Student", JOptionPane.PLAIN_MESSAGE);



                // Check if the input is valid (not null or empty)
                if (studentId != null && !studentId.trim().isEmpty()) {
                    try {
                        Boolean doubleStudent = dbManager.checkValueInTable(mainTable, studentId);
                        if(!doubleStudent){
                            tableModel.addRow(new Object[]{studentId.trim()});
                            try {
                                dbManager.updateTeacherStudents(mainTable, studentId.trim());
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                        else{
                            JOptionPane.showMessageDialog(frame, "Student ID Already Exists in Table", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                    // Add the Student ID to the table
                } else if (studentId != null) {
                    // Show an error message if the input is invalid
                    JOptionPane.showMessageDialog(frame, "Student ID cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> studentsImported = importStudents();

                for(String t :studentsImported){
                    tableModel.addRow(new Object[]{t.trim()});
                    String[] temp1 = dbManager.getTeacher(userName);
                    String teacherName = temp1[1];
                    teacherName = teacherName.replaceAll("^(Mr\\.\\s*|Ms\\.\\s*|Mrs\\.\\s*)", "").trim();
                    String mainTable = teacherName + "_" + period + "_Students";
                    try {
                        dbManager.updateTeacherStudents(mainTable, t.trim());
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

    private ArrayList<String> importStudents(){
        // Create a file chooser
        ArrayList<String> tempStudents = new ArrayList<String>();
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

        // Show the file chooser dialog
        int result = fileChooser.showOpenDialog(null);

        // Check if the user selected a file
        if (result == JFileChooser.APPROVE_OPTION) {
            // Get the selected file
            File selectedFile = fileChooser.getSelectedFile();

            // Read and preview the file content
            if (selectedFile.getName().toLowerCase().endsWith(".csv")) {
                try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                    // Read the CSV content into a list of columns
                    List<List<String>> columns = new ArrayList<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] values = line.split(",");
                        for (int i = 0; i < values.length; i++) {
                            if (columns.size() <= i) {
                                columns.add(new ArrayList<>());
                            }
                            columns.get(i).add(values[i].trim());
                        }
                    }

                    // Open the column preview dialog
                    tempStudents = showColumnPreview(columns);
                    return tempStudents;

                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error reading the file: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select a valid CSV file.",
                        "Invalid File", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("No file selected.");
        }
        return tempStudents;
    }
    private static ArrayList<String> showColumnPreview(List<List<String>> columns) {
        if (columns.isEmpty()) {
            JOptionPane.showMessageDialog(null, "The selected file is empty.",
                    "Empty File", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }

        // Create a modal dialog for column preview
        JDialog dialog = new JDialog((Frame) null, "Column Preview", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(600, 400);
        dialog.setLayout(new BorderLayout());

        // Panel to display column data
        JPanel columnPanel = new JPanel();
        columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(columnPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Panel for navigation buttons
        JPanel buttonPanel = new JPanel();
        JButton prevButton = new JButton("<");
        JButton okButton = new JButton("OK");
        JButton nextButton = new JButton(">");
        buttonPanel.add(prevButton);
        buttonPanel.add(okButton);
        buttonPanel.add(nextButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Display the first column
        int[] currentIndex = {0};
        updateColumnPreview(columnPanel, columns, currentIndex[0]);

        // Add action listener for "Previous" button
        prevButton.addActionListener(e -> {
            currentIndex[0] = (currentIndex[0] - 1 + columns.size()) % columns.size();
            updateColumnPreview(columnPanel, columns, currentIndex[0]);
        });

        // Add action listener for "Next" button
        nextButton.addActionListener(e -> {
            currentIndex[0] = (currentIndex[0] + 1) % columns.size();
            updateColumnPreview(columnPanel, columns, currentIndex[0]);
        });

        // ArrayList to store selected rows
        ArrayList<String> listOfStudentsSelected = new ArrayList<>();

        // Add action listener for "OK" button
        okButton.addActionListener(e -> {
            System.out.println("Selected Rows:");
            for (Component component : columnPanel.getComponents()) {
                if (component instanceof JCheckBox) {
                    JCheckBox checkBox = (JCheckBox) component;
                    if (checkBox.isSelected()) {
                        listOfStudentsSelected.add(checkBox.getText());
                        System.out.println(checkBox.getText());
                    }
                }
            }
            dialog.dispose(); // Close the dialog
        });

        dialog.setVisible(true); // This will block execution until the dialog is closed
        return listOfStudentsSelected; // Return the list of selected rows
    }

    private static void updateColumnPreview(JPanel columnPanel, List<List<String>> columns, int columnIndex) {
        columnPanel.removeAll();
        List<String> columnData = columns.get(columnIndex);

        // Create a checkbox for each row
        for (int i = 0; i < columnData.size(); i++) {
            String row = columnData.get(i);
            JCheckBox checkBox = new JCheckBox(row);
            checkBox.setSelected(true); // Default: All rows selected
            columnPanel.add(checkBox);
        }

        columnPanel.revalidate();
        columnPanel.repaint();
    }
}