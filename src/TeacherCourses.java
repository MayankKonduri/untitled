import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class TeacherCourses {

    private JTable coursesTable;
    private DefaultTableModel tableModel;

    public TeacherCourses(JFrame frame, String teacherName) {

        frame.getContentPane().removeAll();

        // Create a JPanel to hold the UI components
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());  // Use BorderLayout for better control

        // Establish SQL connection
        //String url = "jdbc:mysql://192.168.1.11:3306/setup"; // Home  192.168.1.11
        String url = "jdbc:mysql://10.195.75.116/setup"; //School 10.195.75.116
        String username = "root";
        String password = "password";

        // Create table for the teacher dynamically based on teacher's name
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS `" + teacherName + "_courses` ("
                    + "course_name VARCHAR(255), "
                    + "course_period VARCHAR(100), "
                    + "course_start_time TIME, "
                    + "course_end_time TIME "
                    + ");";
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(createTableSQL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Label to show teacher's name
        JLabel teacherLabel = new JLabel("Welcome, " + teacherName + "!");
        teacherLabel.setFont(new Font("Georgia", Font.BOLD, 16));
        teacherLabel.setHorizontalAlignment(SwingConstants.CENTER);  // Center the label

        // Create table model and JTable for displaying courses
        String[] columnNames = {"Course Name", "Course Period", "Start Time", "End Time"};
        tableModel = new DefaultTableModel(columnNames, 0);
        coursesTable = new JTable(tableModel);
        // Disable editing for the entire JTable
        coursesTable.setDefaultEditor(Object.class, null);

        JScrollPane tableScrollPane = new JScrollPane(coursesTable);
        tableScrollPane.setPreferredSize(new Dimension(500, 200));

        // Load existing courses from the database
        loadCoursesFromDatabase(teacherName);

        // Dropdown for course periods (can add other periods as required)
        String[] coursePeriods = {"1", "2", "3", "4", "5", "6", "7"};
        JComboBox<String> periodDropdown = new JComboBox<>(coursePeriods);
        periodDropdown.setPreferredSize(new Dimension(150, 25));

        // Button to add a course
        JButton addCourseButton = new JButton("Add Course");
        addCourseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Pop-up to add course information
                JTextField courseNameField = new JTextField(15);
                JTextField courseStartTimeField = new JTextField(15);
                JTextField courseEndTimeField = new JTextField(15);

                JPanel coursePanel = new JPanel(new GridLayout(4, 2));
                coursePanel.add(new JLabel("Course Name:"));
                coursePanel.add(courseNameField);
                coursePanel.add(new JLabel("Course Period:"));
                coursePanel.add(periodDropdown);
                coursePanel.add(new JLabel("Start Time (HH:MM:SS):"));
                coursePanel.add(courseStartTimeField);
                coursePanel.add(new JLabel("End Time (HH:MM:SS):"));
                coursePanel.add(courseEndTimeField);

                int option = JOptionPane.showConfirmDialog(null, coursePanel, "Enter Course Details", JOptionPane.OK_CANCEL_OPTION);

                if (option == JOptionPane.OK_OPTION) {
                    // Get data from fields
                    String courseName = courseNameField.getText();
                    String coursePeriod = (String) periodDropdown.getSelectedItem();
                    String courseStartTime = courseStartTimeField.getText();
                    String courseEndTime = courseEndTimeField.getText();

                    // Insert the course details into the teacher's table
                    try (Connection connection = DriverManager.getConnection(url, username, password)) {
                        String insertCourseSQL = "INSERT INTO `" + teacherName + "_courses` (course_name, course_period, course_start_time, course_end_time) "
                                + "VALUES (?, ?, ?, ?)";
                        try (PreparedStatement ps = connection.prepareStatement(insertCourseSQL)) {
                            ps.setString(1, courseName);
                            ps.setString(2, coursePeriod);
                            ps.setString(3, courseStartTime);
                            ps.setString(4, courseEndTime);
                            ps.executeUpdate();

                            // Add the new course to the table display
                            tableModel.addRow(new Object[]{courseName, coursePeriod, courseStartTime, courseEndTime});
                            JOptionPane.showMessageDialog(null, "Course added successfully!");
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // Button to edit a selected course
        JButton editCourseButton = new JButton("Edit Course");
        editCourseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the selected row
                int selectedRow = coursesTable.getSelectedRow();

                if (selectedRow != -1) {
                    // Get the course details from the selected row
                    String courseName = (String) tableModel.getValueAt(selectedRow, 0);
                    String coursePeriod = (String) tableModel.getValueAt(selectedRow, 1);
                    String courseStartTime = (String) tableModel.getValueAt(selectedRow, 2);
                    String courseEndTime = (String) tableModel.getValueAt(selectedRow, 3);

                    // Pop-up to edit course information
                    JTextField courseNameField = new JTextField(courseName, 15);
                    JTextField courseStartTimeField = new JTextField(courseStartTime, 15);
                    JTextField courseEndTimeField = new JTextField(courseEndTime, 15);
                    periodDropdown.setSelectedItem(coursePeriod);

                    JPanel coursePanel = new JPanel(new GridLayout(4, 2));
                    coursePanel.add(new JLabel("Course Name:"));
                    coursePanel.add(courseNameField);
                    coursePanel.add(new JLabel("Course Period:"));
                    coursePanel.add(periodDropdown);
                    coursePanel.add(new JLabel("Start Time (HH:MM:SS):"));
                    coursePanel.add(courseStartTimeField);
                    coursePanel.add(new JLabel("End Time (HH:MM:SS):"));
                    coursePanel.add(courseEndTimeField);

                    int option = JOptionPane.showConfirmDialog(null, coursePanel, "Edit Course Details", JOptionPane.OK_CANCEL_OPTION);

                    if (option == JOptionPane.OK_OPTION) {
                        // Get updated data from fields
                        String updatedCourseName = courseNameField.getText();
                        String updatedCoursePeriod = (String) periodDropdown.getSelectedItem();
                        String updatedCourseStartTime = courseStartTimeField.getText();
                        String updatedCourseEndTime = courseEndTimeField.getText();

                        // Update the course details in the database
                        try (Connection connection = DriverManager.getConnection(url, username, password)) {
                            String updateCourseSQL = "UPDATE `" + teacherName + "_courses` SET course_name = ?, course_period = ?, course_start_time = ?, course_end_time = ? "
                                    + "WHERE course_name = ?";
                            try (PreparedStatement ps = connection.prepareStatement(updateCourseSQL)) {
                                ps.setString(1, updatedCourseName);
                                ps.setString(2, updatedCoursePeriod);
                                ps.setString(3, updatedCourseStartTime);
                                ps.setString(4, updatedCourseEndTime);
                                ps.setString(5, courseName);  // Match old course name
                                ps.executeUpdate();

                                // Update the table with the new details
                                tableModel.setValueAt(updatedCourseName, selectedRow, 0);
                                tableModel.setValueAt(updatedCoursePeriod, selectedRow, 1);
                                tableModel.setValueAt(updatedCourseStartTime, selectedRow, 2);
                                tableModel.setValueAt(updatedCourseEndTime, selectedRow, 3);
                                JOptionPane.showMessageDialog(null, "Course updated successfully!");
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a course to edit.");
                }
            }
        });

        // Add components to the main panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // Stack the label and table
        topPanel.add(teacherLabel);
        topPanel.add(Box.createVerticalStrut(10));  // Add vertical space between the label and table
        topPanel.add(tableScrollPane);

        // Create a panel for the buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));  // Center the buttons


        // Create the "Delete Course" button
        JButton deleteCourseButton = new JButton("Delete Course");
        deleteCourseButton.setAlignmentX(Component.CENTER_ALIGNMENT);

// Action listener for the "Delete Course" button
        deleteCourseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the selected row from the table
                int selectedRow = coursesTable.getSelectedRow();

                if (selectedRow != -1) {
                    // Get the course name from the selected row to identify which course to delete
                    String courseName = (String) tableModel.getValueAt(selectedRow, 0);

                    // Confirm with the user if they really want to delete the course
                    int confirmation = JOptionPane.showConfirmDialog(null,
                            "Are you sure you want to delete the course: " + courseName + "?",
                            "Delete Course",
                            JOptionPane.YES_NO_OPTION);

                    if (confirmation == JOptionPane.YES_OPTION) {
                        // Delete the course from the database
                        try (Connection connection = DriverManager.getConnection(url, username, password)) {
                            String deleteCourseSQL = "DELETE FROM `" + teacherName + "_courses` WHERE course_name = ? AND course_period = ? AND course_start_time = ? AND course_end_time = ?";
                            try (PreparedStatement ps = connection.prepareStatement(deleteCourseSQL)) {
                                // Use the full course details to identify it uniquely in the database
                                ps.setString(1, courseName);
                                ps.setString(2, (String) tableModel.getValueAt(selectedRow, 1));
                                ps.setString(3, (String) tableModel.getValueAt(selectedRow, 2));
                                ps.setString(4, (String) tableModel.getValueAt(selectedRow, 3));
                                ps.executeUpdate();

                                // Remove the course from the table display (just remove the selected row)
                                tableModel.removeRow(selectedRow);
                                JOptionPane.showMessageDialog(null, "Course deleted successfully!");
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please select a course to delete.");
                }
            }
        });


// Add the "Delete Course" button to the bottomPanel, to the left of the "Add Course" button
        bottomPanel.add(deleteCourseButton); // Add to the panel first
        bottomPanel.add(addCourseButton);  // Then add the "Add Course" button
        bottomPanel.add(editCourseButton);  // Add the "Edit Course" button

        // Add the top and bottom panels to the main panel
        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Set the main panel as the content pane
        frame.setContentPane(mainPanel);
        frame.revalidate();
        frame.repaint();
    }

    private void loadCoursesFromDatabase(String teacherName) {
        //try (Connection connection = DriverManager.getConnection("jdbc:mysql://192.168.1.11:3306/setup", "root", "password")) { // Home  192.168.1.11
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://10.195.75.116/setup", "root", "password")) { //School 10.195.75.116
                String query = "SELECT * FROM `" + teacherName + "_courses`";
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {

                    while (rs.next()) {
                        String courseName = rs.getString("course_name");
                        String coursePeriod = rs.getString("course_period");
                        String courseStartTime = rs.getString("course_start_time");
                        String courseEndTime = rs.getString("course_end_time");
                        tableModel.addRow(new Object[]{courseName, coursePeriod, courseStartTime, courseEndTime});
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }