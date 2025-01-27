import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // JDBC URL, username, and password for the local database
    //private static final String DATABASE_URL = "jdbc:mysql://192.168.1.14/qclient"; //Home
    private static final String DATABASE_URL = "jdbc:mysql://10.195.75.116/qclient";//School
    private static final String DATABASE_USER = "root"; // Replace with your MySQL username
    private static final String DATABASE_PASSWORD = "password"; // Replace with your MySQL password

    // Connection object
    private Connection connection;

    // Constructor
    public DatabaseManager() {
        try {
            // Establish connection to the database
            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database.");
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("Error while checking the connection.");
            e.printStackTrace();
            return false;
        }
    }

    //---------------  Teachers ---------------------------------------//

    public void addToTeachers(String teacherName, String teacherID, String sound, int waitTime) {
        String query = "INSERT INTO Teacher (teacher_name, teacher_id, sound, wait_time) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, teacherName);
            stmt.setString(2, teacherID);
            stmt.setString(3, sound);
            stmt.setInt(4, waitTime);
            stmt.executeUpdate();
            System.out.println("Teacher added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding teacher.");
            e.printStackTrace();
        }
    }

    public void deleteTeacher(String teacherID) {
        String query = "DELETE FROM Teacher WHERE teacher_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, teacherID);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Teacher with ID " + teacherID + " deleted successfully!");
            } else {
                System.out.println("No teacher found with the given ID.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting teacher.");
            e.printStackTrace();
        }
    }

    public void updateTeacher(String teacherID, String newName, String newSound, Integer newWaitTime) {
        StringBuilder query = new StringBuilder("UPDATE Teacher SET ");
        boolean firstUpdate = true;

        if (newName != null && !newName.isEmpty()) {
            if (!firstUpdate) {
                query.append(", ");
            }
            query.append("teacher_name = ?");
            firstUpdate = false;
        }

        if (newSound != null && !newSound.isEmpty()) {
            if (!firstUpdate) {
                query.append(", ");
            }
            query.append("sound = ?");
            firstUpdate = false;
        }

        if (newWaitTime != null) {
            if (!firstUpdate) {
                query.append(", ");
            }
            query.append("wait_time = ?");
        }

        query.append(" WHERE teacher_id = ?");

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            int parameterIndex = 1;

            if (newName != null && !newName.isEmpty()) {
                stmt.setString(parameterIndex++, newName);
            }
            if (newSound != null && !newSound.isEmpty()) {
                stmt.setString(parameterIndex++, newSound);
            }
            if (newWaitTime != null) {
                stmt.setInt(parameterIndex++, newWaitTime);
            }

            stmt.setString(parameterIndex, teacherID);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Teacher details updated successfully!");
            } else {
                System.out.println("No teacher found with the given ID.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating teacher.");
            e.printStackTrace();
        }
    }

    // Method to check if a teacher exists based on teacher_id
    public boolean checkTeacherExists(String teacherID) {
        String query = "SELECT COUNT(*) FROM Teacher WHERE teacher_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, teacherID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return true;  // Teacher exists
            } else {
                return false;  // Teacher does not exist
            }
        } catch (SQLException e) {
            System.err.println("Error checking teacher existence.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkTeacherExistsByName(String teacherName) {
        // The input teacherName is already normalized, so we directly use it
        String normalizedTeacherName = teacherName.toLowerCase();  // Ensure it is lowercase

        // SQL query to retrieve teacher_name from the database
        String query = "SELECT teacher_name FROM Teacher";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();

            // Iterate through the result set to check if there's a match
            while (rs.next()) {
                String dbTeacherName = rs.getString("teacher_name");  // Original name from the database

                // Normalize the database teacher name by removing the period from the title and converting to lowercase
                String normalizedDbTeacherName = normalizeFullName(dbTeacherName);

                // Compare the normalized teacher name from the input with the normalized teacher name from the database
                if (normalizedTeacherName.equals(normalizedDbTeacherName)) {
                    return true;  // Teacher exists
                }
            }

            return false;  // Teacher does not exist
        } catch (SQLException e) {
            System.err.println("Error checking teacher existence by name.");
            e.printStackTrace();
            return false;
        }
    }

    // Helper method to normalize the teacher name from the database
    private String normalizeFullName(String fullName) {
        // Split the fullName into title and name parts
        String[] nameParts = fullName.split(" ", 2);  // Split into title and the rest of the name

        if (nameParts.length < 2) {
            return fullName.toLowerCase();  // If no space is found, just return the fullName in lowercase
        }

        // Get the title and name
        String title = nameParts[0].toLowerCase().replace(".", "");  // Convert title to lowercase and remove period
        String name = nameParts[1].toLowerCase();  // Convert the name part to lowercase

        // Return the normalized full name (e.g., "Mr. Tully" becomes "mr.tully")
        return name;
    }




    public String[] getTeacher(String teacherID) {
        String query = "SELECT * FROM Teacher WHERE teacher_id = ?";
        String[] teacherDetails = new String[4];  // Array to store teacher details

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, teacherID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Populate the array with the teacher's details
                teacherDetails[0] = rs.getString("teacher_id");    // teacher_id
                teacherDetails[1] = rs.getString("teacher_name");  // teacher_name
                teacherDetails[2] = rs.getString("sound");        // sound
                teacherDetails[3] = String.valueOf(rs.getInt("wait_time"));  // wait_time
            } else {
                // If no teacher is found, return a message in the array
                teacherDetails = new String[] {"No teacher found with ID: " + teacherID};
            }
        } catch (SQLException e) {
            System.err.println("Error fetching teacher details.");
            e.printStackTrace();
            teacherDetails = new String[] {"Error fetching data"};
        }

        return teacherDetails;
    }

    //---------------  Teachers ---------------------------------------//

    //---------------  Students ---------------------------------------//

    public void addToStudents(String studentID) {
        String query = "INSERT INTO Student (student_id) VALUES (?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, studentID);
            stmt.executeUpdate();
            System.out.println("Student added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding student.");
            e.printStackTrace();
        }
    }

    public void deleteStudent(String studentID) {
        String query = "DELETE FROM Student WHERE student_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, studentID);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Student with ID " + studentID + " deleted successfully.");
            } else {
                System.out.println("Student with ID " + studentID + " not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error while deleting student.");
            e.printStackTrace();
        }
    }

    // Method to check if a student exists based on student_id
    public boolean checkStudentExists(String studentID) {
        String query = "SELECT COUNT(*) FROM Student WHERE student_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, studentID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return true;  // Student exists
            } else {
                return false;  // Student does not exist
            }
        } catch (SQLException e) {
            System.err.println("Error checking student existence.");
            e.printStackTrace();
            return false;
        }
    }

    public String[] getStudent(String studentID) {
        String query = "SELECT * FROM Student WHERE student_id = ?";
        String[] studentDetails = new String[1];  // Array to store student details

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, studentID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Populate the array with the student's details
                studentDetails[0] = rs.getString("student_id");  // student_id
            } else {
                // If no student is found, return a message in the array
                studentDetails = new String[] {"No student found with ID: " + studentID};
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student details.");
            e.printStackTrace();
            studentDetails = new String[] {"Error fetching data"};
        }

        return studentDetails;
    }



    //---------------  Students ---------------------------------------//
    //----

    public void updateTeacherMain(String tableName, String className, String startTime, String endTime) {
        String deleteSQL = "DELETE FROM " + tableName;
        String insertSQL = "INSERT INTO " + tableName + " (ClassName, StartTime, EndTime) VALUES (?, ?, ?)";

        try {
            // Start by clearing the table
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL)) {
                deleteStatement.executeUpdate();
                System.out.println("All rows cleared from " + tableName);
            }

            // Then insert new data
            try (PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {
                insertStatement.setString(1, className);

                // Check if startTime and endTime are empty or null and set them as NULL in the database
                if (startTime == null || startTime.isEmpty()) {
                    insertStatement.setNull(2, java.sql.Types.TIME);
                } else {
                    insertStatement.setString(2, startTime);
                }

                if (endTime == null || endTime.isEmpty()) {
                    insertStatement.setNull(3, java.sql.Types.TIME);
                } else {
                    insertStatement.setString(3, endTime);
                }

                insertStatement.executeUpdate();
                System.out.println("Class details added successfully to " + tableName);
            }

        } catch (SQLException e) {
            System.err.println("Error updating class details in table: " + tableName);
            e.printStackTrace();
        }
    }
    public String[] getTeacherMainData(String tableName) {
        String selectSQL = "SELECT ClassName, StartTime, EndTime FROM " + tableName;
        String[] data = new String[3]; // ClassName, StartTime, EndTime

        try (PreparedStatement statement = connection.prepareStatement(selectSQL);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                data[0] = resultSet.getString("ClassName") != null ? resultSet.getString("ClassName") : "";
                data[1] = resultSet.getTime("StartTime") != null ? resultSet.getTime("StartTime").toString() : "";
                data[2] = resultSet.getTime("EndTime") != null ? resultSet.getTime("EndTime").toString() : "";
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving data from table: " + tableName);
            e.printStackTrace();
        }

        return data;
    }
    public void updateTeacherStudents(String tableName, String studentID) throws SQLException {
        // Ensure table name is safe from SQL injection by validating input
        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        // Build the SQL query with a placeholder for StudentID
        String insertSQL = "INSERT INTO " + tableName + " (StudentID) VALUES (?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            // Set the value for the placeholder
            preparedStatement.setString(1, studentID);

            // Execute the statement
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " row(s) inserted into table " + tableName);
        }
    }

    public boolean checkValueInTable(String tableName, String valueToCheck) throws SQLException {
        // Ensure table name is safe from SQL injection by validating input
        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        // Build the SQL query with a placeholder for the value to check
        String checkSQL = "SELECT COUNT(*) FROM " + tableName + " WHERE StudentID = ?"; // Modify the column name as needed

        try (PreparedStatement preparedStatement = connection.prepareStatement(checkSQL)) {
            // Set the value for the placeholder
            preparedStatement.setString(1, valueToCheck);

            // Execute the query
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    if (count > 0) {
                        System.out.println("Value '" + valueToCheck + "' found in table " + tableName);
                        return true; // Value exists in the table
                    } else {
                        System.out.println("Value '" + valueToCheck + "' not found in table " + tableName);
                        return false; // Value does not exist in the table
                    }
                }
            }
        }
        return false; // Return false if an error occurs or no rows are returned
    }


    public void removeTeacherStudents(String tableName, String studentID) throws SQLException {
        // Ensure table name is safe from SQL injection by validating input
        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        // Build the SQL query with a placeholder for StudentID
        String deleteSQL = "DELETE FROM " + tableName + " WHERE StudentID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {
            // Set the value for the placeholder
            preparedStatement.setString(1, studentID);

            // Execute the statement
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " row(s) deleted from table " + tableName);
        }
    }

    public ArrayList<String[]> checkNameInStudentsTables(String nameToCheck) throws SQLException {
        // Create an ArrayList to store the results
        ArrayList<String[]> resultList = new ArrayList<>();

        // Step 1: Get tables ending with '_students'
        String fetchTablesQuery = "SELECT table_name FROM information_schema.tables WHERE table_name LIKE '%_students';";

        try (Statement tableStmt = connection.createStatement();
             ResultSet tableResultSet = tableStmt.executeQuery(fetchTablesQuery)) {

            // Step 2: Iterate over each table
            while (tableResultSet.next()) {
                String tableName = tableResultSet.getString("table_name");
                System.out.println("Found _students table: " + tableName);

                // Step 2a: Check if 'StudentID' exists in this table and search for the name in 'StudentID'
                String checkNameQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE StudentID = ?";
                try (PreparedStatement nameStmt = connection.prepareStatement(checkNameQuery)) {
                    nameStmt.setString(1, nameToCheck);
                    try (ResultSet nameResultSet = nameStmt.executeQuery()) {
                        if (nameResultSet.next() && nameResultSet.getInt(1) > 0) {
                            System.out.println("Found student '" + nameToCheck + "' in table: " + tableName);

                            // Step 3: After finding the student, get the corresponding 'main' table
                            String baseName = tableName.split("_students")[0]; // Extract the base name (e.g., 'tully_1')
                            String mainTableName = tableName.replace("_students", "_main");
                            System.out.println("Main table corresponding to _students table: " + mainTableName);

                            // Step 4: Query the corresponding main table for ClassName, StartTime, and EndTime
                            String getClassDetailsQuery = "SELECT ClassName, StartTime, EndTime FROM " + mainTableName;
                            try (Statement mainTableStmt = connection.createStatement();
                                 ResultSet classResultSet = mainTableStmt.executeQuery(getClassDetailsQuery)) {

                                if (classResultSet.next()) {
                                    String className = classResultSet.getString("ClassName");
                                    String startTime = classResultSet.getString("StartTime");
                                    String endTime = classResultSet.getString("EndTime");

                                    // Step 5: Search for the teacher in the 'Teachers' table
                                    String teacherLastName = baseName.split("_")[0]; // Extract base name without index
                                    String checkTeacherQuery = "SELECT wait_time FROM Teacher WHERE teacher_name LIKE ?";
                                    try (PreparedStatement teacherStmt = connection.prepareStatement(checkTeacherQuery)) {
                                        teacherStmt.setString(1, "% " + teacherLastName);
                                        try (ResultSet teacherResultSet = teacherStmt.executeQuery()) {
                                            String waitTime = "Not Found";
                                            if (teacherResultSet.next()) {
                                                waitTime = teacherResultSet.getString("wait_time");
                                                System.out.println("Found teacher: " + teacherLastName + " with wait_time: " + waitTime);
                                            } else {
                                                System.out.println("Teacher with last name '" + teacherLastName + "' not found.");
                                            }

                                            // Store the results in the ArrayList
                                            resultList.add(new String[]{
                                                    mainTableName,
                                                    className,
                                                    startTime,
                                                    endTime,
                                                    waitTime
                                            });
                                        }
                                    }
                                } else {
                                    // If no class details are found in the main table
                                    System.out.println("No class details found in main table for " + mainTableName);
                                }
                            }
                        }
                    }
                }
            }
        }
        // Return the result list containing all found occurrences
        return resultList;
    }

    public String[] getTeacherStudents(String tableName) throws SQLException {
        // Ensure table name is safe from SQL injection by validating input
        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        // Build the SQL query
        String selectSQL = "SELECT StudentID FROM " + tableName;

        List<String> students = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {
            // Execute the query
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    // Add each student ID to the list
                    students.add(resultSet.getString("StudentID"));
                }
            }
        }

        // Convert the list to an array and return
        return students.toArray(new String[0]);
    }

    public void deleteTeacherAndAssociatedTables(String teacherName) {
        String deleteTeacherQuery = "DELETE FROM Teacher WHERE teacher_name = ?";
        // Updated regex to handle "Mr. ", "Mrs. ", or "Ms. " with space after the period
        String baseName = teacherName.replaceAll("^(Mr\\.|Mrs\\.|Ms\\.)\\s*", "").trim();

        try (PreparedStatement stmt = connection.prepareStatement(deleteTeacherQuery)) {
            // Delete the teacher from the Teacher table
            stmt.setString(1, teacherName);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Teacher '" + teacherName + "' deleted successfully from Teacher table.");
            } else {
                System.out.println("No teacher found with the name '" + teacherName + "'.");
                return;
            }

            // Delete all tables associated with the teacher
            for (int grade = 1; grade <= 7; grade++) {
                String mainTable = baseName + "_" + grade + "_main";
                String studentsTable = baseName + "_" + grade + "_students";

                try (Statement tableDeletionStmt = connection.createStatement()) {
                    // Drop the main table
                    String dropMainTableQuery = "DROP TABLE IF EXISTS " + mainTable;
                    tableDeletionStmt.executeUpdate(dropMainTableQuery);
                    System.out.println("Deleted table: " + mainTable);

                    // Drop the students table
                    String dropStudentsTableQuery = "DROP TABLE IF EXISTS " + studentsTable;
                    tableDeletionStmt.executeUpdate(dropStudentsTableQuery);
                    System.out.println("Deleted table: " + studentsTable);
                } catch (SQLException e) {
                    System.err.println("Error deleting table: " + mainTable + " or " + studentsTable);
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            System.err.println("Error deleting teacher and associated tables.");
            e.printStackTrace();
        }
    }






}
