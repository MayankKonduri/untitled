import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

class StudentPage {
    public StudentPage(JFrame frame, String studentId, String studentName) {
        frame.getContentPane().removeAll();

        // Default courses for the new student
        ArrayList<String[]> studentCourses = new ArrayList<>();

        try {
            // Database connection (adjust the URL, username, and password as per your setup)
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://192.168.1.11:3306/setup", "root", "password");

            // Fetch or initialize the courses for the student
            String query = "SELECT student_courses FROM setup.students WHERE student_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, studentId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String courses = resultSet.getString("student_courses");

                // Split courses into an array (assuming they're stored as a comma-separated string)
                if (courses != null && !courses.isEmpty()) {
                    for (String course : courses.split(",")) {
                        String[] courseDetails = course.trim().split(" \\(Period ");
                        if (courseDetails.length == 2) {
                            String teacher = courseDetails[0];
                            String period = courseDetails[1].replace(")", "").trim();
                            studentCourses.add(new String[]{teacher, period, ""});  // Empty course name initially
                        }
                    }
                }
            } else {
                // Insert a new record if the student is not yet in the table
                String insertQuery = "INSERT INTO setup.students (student_id, student_name, student_courses) VALUES (?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setString(1, studentId);
                insertStatement.setString(2, studentName);
                insertStatement.setString(3, ""); // No courses initially
                insertStatement.executeUpdate();
                insertStatement.close();
            }

            // Close the connection
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Sort courses by period
        studentCourses.sort(Comparator.comparingInt(course -> Integer.parseInt(course[1])));

        // Create the student panel
        JPanel studentPanel = new JPanel();
        studentPanel.setLayout(new BoxLayout(studentPanel, BoxLayout.Y_AXIS));

        // Welcome label
        JLabel welcomeLabel = new JLabel("Welcome, " + studentName + "!");
        welcomeLabel.setFont(new Font("Georgia", Font.BOLD, 16));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Courses label
        JLabel coursesLabel = new JLabel("My Courses:");
        coursesLabel.setFont(new Font("Georgia", Font.PLAIN, 14));
        coursesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create a table to display courses, with the "Course Name" column added
        String[] columnNames = {"Course Name", "Teacher", "Period"};  // Add "Course Name" as the first column
        String[][] courseData = new String[studentCourses.size()][3];  // Add a column for "Course Name"

        // Populate the table data
        for (int i = 0; i < studentCourses.size(); i++) {
            courseData[i][0] = studentCourses.get(i)[2];  // Empty course name for now, or the real course name if available
            courseData[i][1] = studentCourses.get(i)[0];  // Teacher
            courseData[i][2] = studentCourses.get(i)[1];  // Period
        }

        JTable coursesTable = new JTable(courseData, columnNames);
        coursesTable.setFont(new Font("Georgia", Font.PLAIN, 14));
        coursesTable.setRowHeight(30);
        JScrollPane coursesScrollPane = new JScrollPane(coursesTable);

        // Add course button
        JButton addCourseButton = new JButton("Add Course");
        addCourseButton.setFont(new Font("Georgia", Font.PLAIN, 14));
        addCourseButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        addCourseButton.addActionListener(e -> {
            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

            // Teacher last name input
            JTextField teacherLastNameField = new JTextField();
            teacherLastNameField.setFont(new Font("Georgia", Font.PLAIN, 14));
            inputPanel.add(new JLabel("Teacher's Last Name:"));
            inputPanel.add(teacherLastNameField);

            // Class period dropdown
            String[] periods = {"1", "2", "3", "4", "5", "6", "7"};
            JComboBox<String> classPeriodComboBox = new JComboBox<>(periods);
            classPeriodComboBox.setFont(new Font("Georgia", Font.PLAIN, 14));
            inputPanel.add(new JLabel("Class Period:"));
            inputPanel.add(classPeriodComboBox);

            int option = JOptionPane.showConfirmDialog(frame, inputPanel, "Enter Course Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                String teacherLastName = teacherLastNameField.getText().trim();
                String classPeriod = (String) classPeriodComboBox.getSelectedItem();

                if (!teacherLastName.isEmpty() && classPeriod != null) {
                    String newCourse = teacherLastName + " (Period " + classPeriod + ")";
                    if (!studentCourses.contains(newCourse)) {
                        studentCourses.add(new String[]{teacherLastName, classPeriod, ""});  // Add with empty name

                        // Sort the courses by period again
                        studentCourses.sort(Comparator.comparingInt(course -> Integer.parseInt(course[1])));

                        // Update the table
                        String[][] updatedCourseData = new String[studentCourses.size()][3];  // Add "Course Name" column
                        for (int i = 0; i < studentCourses.size(); i++) {
                            updatedCourseData[i][0] = studentCourses.get(i)[2];  // Empty course name for now
                            updatedCourseData[i][1] = studentCourses.get(i)[0];
                            updatedCourseData[i][2] = studentCourses.get(i)[1];
                        }
                        coursesTable.setModel(new javax.swing.table.DefaultTableModel(updatedCourseData, columnNames));

                        // Update the database
                        try {
                            Connection connection = DriverManager.getConnection(
                                    "jdbc:mysql://192.168.1.11:3306/setup", "root", "password");

                            // Check if the columns already exist
                            Statement stmt = connection.createStatement();
                            ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM setup.students");

                            boolean teacherColumnExists = false;
                            boolean periodColumnExists = false;
                            boolean nameColumnExists = false;
                            while (rs.next()) {
                                String columnName = rs.getString("Field");
                                if (columnName.equals("course_" + (studentCourses.size()) + "_teacher")) {
                                    teacherColumnExists = true;
                                }
                                if (columnName.equals("course_" + (studentCourses.size()) + "_period")) {
                                    periodColumnExists = true;
                                }
                                if (columnName.equals("course_" + (studentCourses.size()) + "_name")) {
                                    nameColumnExists = true;
                                }
                            }

                            // Add new columns only if they do not exist
                            if (!teacherColumnExists) {
                                String alterQuery = "ALTER TABLE setup.students " +
                                        "ADD COLUMN course_" + (studentCourses.size()) + "_teacher VARCHAR(255), " +
                                        "ADD COLUMN course_" + (studentCourses.size()) + "_period VARCHAR(255), " +
                                        "ADD COLUMN course_" + (studentCourses.size()) + "_name VARCHAR(255) DEFAULT ''";
                                PreparedStatement alterStatement = connection.prepareStatement(alterQuery);
                                alterStatement.executeUpdate();
                                alterStatement.close();
                            }

                            // Now, update the new columns with course details
                            String updateQuery = "UPDATE setup.students SET " +
                                    "course_" + (studentCourses.size()) + "_teacher = ?, " +
                                    "course_" + (studentCourses.size()) + "_period = ? " +
                                    "WHERE student_id = ?";
                            PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                            updateStatement.setString(1, teacherLastName);
                            updateStatement.setString(2, classPeriod);
                            updateStatement.setString(3, studentId);
                            updateStatement.executeUpdate();
                            updateStatement.close();

                            connection.close();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Course already exists!", "Error", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please fill in all fields!", "Error", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Add components to the panel
        studentPanel.add(welcomeLabel);
        studentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        studentPanel.add(coursesLabel);
        studentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        studentPanel.add(coursesScrollPane);
        studentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        studentPanel.add(addCourseButton);

        // Set the layout for the frame
        frame.getContentPane().add(studentPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }
}
