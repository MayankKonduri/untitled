import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

class StudentPage {
    public StudentPage(JFrame frame, String studentId, String studentName) {
        frame.getContentPane().removeAll();

        // Default courses for the new student
        ArrayList<String> studentCourses = new ArrayList<>();

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
                        studentCourses.add(course.trim());
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

        // List of courses
        DefaultListModel<String> coursesModel = new DefaultListModel<>();
        for (String course : studentCourses) {
            coursesModel.addElement(course);
        }
        JList<String> coursesList = new JList<>(coursesModel);
        coursesList.setFont(new Font("Georgia", Font.PLAIN, 14));
        coursesList.setAlignmentX(Component.CENTER_ALIGNMENT);
        JScrollPane coursesScrollPane = new JScrollPane(coursesList);

        // Add course button
        JButton addCourseButton = new JButton("Add Course");
        addCourseButton.setFont(new Font("Georgia", Font.PLAIN, 14));
        addCourseButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        addCourseButton.addActionListener(e -> {
            String newCourse = JOptionPane.showInputDialog(frame, "Enter the course name:");
            if (newCourse != null && !newCourse.trim().isEmpty()) {
                String courseName = newCourse.trim();
                if (!studentCourses.contains(courseName)) {
                    studentCourses.add(courseName);
                    coursesModel.addElement(courseName);

                    // Update the database
                    try {
                        Connection connection = DriverManager.getConnection(
                                "jdbc:mysql://192.168.1.11:3306/setup", "root", "password");
                        String updateQuery = "UPDATE setup.students SET student_courses = ? WHERE student_id = ?";
                        PreparedStatement updateStatement = connection.prepareStatement(updateQuery);
                        updateStatement.setString(1, String.join(",", studentCourses));
                        updateStatement.setString(2, studentId);
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

        // Add the panel to the frame
        frame.add(studentPanel);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }
}
