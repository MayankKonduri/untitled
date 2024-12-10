import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

class StudentPage {
    public StudentPage(JFrame frame, String studentId, String studentName) {
        frame.getContentPane().removeAll();

        // List to store course data
        ArrayList<String[]> studentCourses = new ArrayList<>();

        try {
            // Database connection (adjust the URL, username, and password as per your setup)
            Connection connection = DriverManager.getConnection(
                    //"jdbc:mysql://192.168.1.11:3306/setup", "root", "password"); //Home  192.168.1.11
                    "jdbc:mysql://10.195.75.116/setup", "root", "password"); //School 10.195.75.116

            // Query to get the course columns for the student
            String query = "SELECT * FROM setup.students WHERE student_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, studentId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                // Loop through columns like course_1_teacher, course_1_period, etc.
                int courseIndex = 1;
                while (courseIndex <= 7) {  // Limit the loop to 7 periods
                    String teacherColumn = "course_" + courseIndex + "_teacher";
                    String periodColumn = "course_" + courseIndex + "_period";
                    String nameColumn = "course_" + courseIndex + "_name";

                    String teacher = resultSet.getString(teacherColumn);
                    String period = resultSet.getString(periodColumn);
                    String name = resultSet.getString(nameColumn);

                    // If all values are null, exit the loop
                    if (teacher == null && period == null && name == null) {
                        break;  // No more courses found
                    }

                    // If the course data is not null, add it to the list
                    if (teacher != null && period != null) {
                        studentCourses.add(new String[]{teacher, period, name != null ? name : ""});
                    }

                    courseIndex++;
                }
            } else {
                // Insert a new record if the student is not yet in the table
                String insertQuery = "INSERT INTO setup.students (student_id, student_name) VALUES (?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setString(1, studentId);
                insertStatement.setString(2, studentName);
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
        String[] columnNames = {"Course Name", "Teacher", "Period"};
        String[][] courseData = new String[studentCourses.size()][3];

        // Populate the table data
        for (int i = 0; i < studentCourses.size(); i++) {
            courseData[i][0] = studentCourses.get(i)[2];  // Course Name
            courseData[i][1] = studentCourses.get(i)[0];  // Teacher
            courseData[i][2] = studentCourses.get(i)[1];  // Period
        }

        JTable coursesTable = new JTable(courseData, columnNames);
        coursesTable.setFont(new Font("Georgia", Font.PLAIN, 14));
        coursesTable.setRowHeight(30);
        coursesTable.setDefaultEditor(Object.class, null);
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
                    if (!studentCourses.stream().anyMatch(course -> course[0].equals(teacherLastName) && course[1].equals(classPeriod))) {
                        studentCourses.add(new String[]{teacherLastName, classPeriod, ""});  // Add with empty name

                        // Sort the courses by period again
                        studentCourses.sort(Comparator.comparingInt(course -> Integer.parseInt(course[1])));

                        // Update the table
                        String[][] updatedCourseData = new String[studentCourses.size()][3];
                        for (int i = 0; i < studentCourses.size(); i++) {
                            updatedCourseData[i][0] = studentCourses.get(i)[2];  // Empty course name for now
                            updatedCourseData[i][1] = studentCourses.get(i)[0];
                            updatedCourseData[i][2] = studentCourses.get(i)[1];
                        }
                        coursesTable.setModel(new javax.swing.table.DefaultTableModel(updatedCourseData, columnNames));

                        // Update the database
                        try {
                            Connection connection = DriverManager.getConnection(
                                    //"jdbc:mysql://192.168.1.11:3306/setup", "root", "password"); //Home  192.168.1.11
                                    "jdbc:mysql://10.195.75.116/setup", "root", "password"); //School 10.195.75.116

                            // Determine the next available course index
                            int nextCourseIndex = studentCourses.size();

                            // Add new columns if they don't exist
                            Statement stmt = connection.createStatement();
                            ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM setup.students");

                            boolean teacherColumnExists = false;
                            boolean periodColumnExists = false;
                            boolean nameColumnExists = false;
                            while (rs.next()) {
                                String columnName = rs.getString("Field");
                                if (columnName.equals("course_" + nextCourseIndex + "_teacher")) {
                                    teacherColumnExists = true;
                                }
                                if (columnName.equals("course_" + nextCourseIndex + "_period")) {
                                    periodColumnExists = true;
                                }
                                if (columnName.equals("course_" + nextCourseIndex + "_name")) {
                                    nameColumnExists = true;
                                }
                            }

                            // Add new columns only if they do not exist
                            if (!teacherColumnExists) {
                                String alterQuery = "ALTER TABLE setup.students " +
                                        "ADD COLUMN course_" + nextCourseIndex + "_teacher VARCHAR(255), " +
                                        "ADD COLUMN course_" + nextCourseIndex + "_period VARCHAR(255), " +
                                        "ADD COLUMN course_" + nextCourseIndex + "_name VARCHAR(255) DEFAULT ''";
                                PreparedStatement alterStatement = connection.prepareStatement(alterQuery);
                                alterStatement.executeUpdate();
                                alterStatement.close();
                            }

                            // Now, update the new columns with course details
                            String updateQuery = "UPDATE setup.students SET " +
                                    "course_" + nextCourseIndex + "_teacher = ?, " +
                                    "course_" + nextCourseIndex + "_period = ? " +
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
                            JOptionPane.showMessageDialog(frame, "Error updating database: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Course already added for this teacher and period.");
                    }
                }
            }
        });

        // Add components to the panel
        studentPanel.add(welcomeLabel);
        studentPanel.add(coursesLabel);
        studentPanel.add(coursesScrollPane);
        studentPanel.add(addCourseButton);

        // Add panel to the frame
        frame.add(studentPanel, BorderLayout.CENTER);

        // Refresh the frame
        frame.revalidate();
        frame.repaint();
    }
}
