import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // JDBC URL, username, and password for the local database
    private static final String DATABASE_URL = "jdbc:mysql:// 10.66.201.75/qclient1";
    private static final String DATABASE_USER = "root"; // Replace with your MySQL username
    private static final String DATABASE_PASSWORD = "password"; // Replace with your MySQL password
    private String userName;

    // Connection object
    private Connection connection;

    // Constructor
    public DatabaseManager(String userName) {

        this.userName = userName;

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
    public void updateTeacherStudents(String tableName, String studentID, String firstN, String lastN, String nickN) throws SQLException {
        // Ensure table name is safe from SQL injection by validating input
        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        // Build the SQL query with placeholders for all fields
        String insertSQL = "INSERT INTO " + tableName + " (StudentID, FirstName, LastName, Nickname) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            // Set the values for each placeholder
            preparedStatement.setString(1, studentID);
            preparedStatement.setString(2, firstN);
            preparedStatement.setString(3, lastN);
            preparedStatement.setString(4, nickN);

            // Execute the statement
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " row(s) inserted into table " + tableName);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;  // Re-throw the exception if you want to propagate it
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


    public String[][] getTeacherStudents(String tableName) throws SQLException {
        // Ensure table name is safe from SQL injection by validating input
        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        // Build the SQL query to select all columns (StudentID, FirstName, LastName, Nickname)
        String selectSQL = "SELECT StudentID, FirstName, LastName, Nickname FROM " + tableName;

        List<String[]> students = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {
            // Execute the query
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    // Retrieve the values for each column and add them to the list as an array
                    String studentId = resultSet.getString("StudentID");
                    String firstName = resultSet.getString("FirstName");
                    String lastName = resultSet.getString("LastName");
                    String nickname = resultSet.getString("Nickname");
                    students.add(new String[]{studentId, firstName, lastName, nickname});
                }
            }
        }

        // Convert the list to a 2D array and return
        return students.toArray(new String[0][0]);
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
                String questionsTable = baseName + "_" + grade + "_questions";

                try (Statement tableDeletionStmt = connection.createStatement()) {
                    // Drop the main table
                    String dropMainTableQuery = "DROP TABLE IF EXISTS " + mainTable;
                    tableDeletionStmt.executeUpdate(dropMainTableQuery);
                    System.out.println("Deleted table: " + mainTable);

                    // Drop the students table
                    String dropStudentsTableQuery = "DROP TABLE IF EXISTS " + studentsTable;
                    tableDeletionStmt.executeUpdate(dropStudentsTableQuery);
                    System.out.println("Deleted table: " + studentsTable);

                    // Drop the students table
                    String dropQuestionsTableQuery = "DROP TABLE IF EXISTS " + questionsTable;
                    tableDeletionStmt.executeUpdate(dropQuestionsTableQuery);
                    System.out.println("Deleted table: " + questionsTable);
                } catch (SQLException e) {
                    System.err.println("Error deleting table: " + mainTable + " or " + studentsTable + " or " + questionsTable);
                    e.printStackTrace();
                }
            }

        } catch (SQLException e) {
            System.err.println("Error deleting teacher and associated tables.");
            e.printStackTrace();
        }
    }

    public static void addRecordToTable(String tableName, String studentID, String questionSummary) {
        // Ensure table name is safe from SQL injection by validating input
        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {
            // SQL query to insert data into the specified table
            String insertSQL = "INSERT INTO " + tableName + " (StudentID, QuestionSummary, TimeStamp, IsQuestionActive) VALUES (?, ?, ?, ?)";

            String DATABASE_URL = "jdbc:mysql:// 10.66.201.75/qclient1"; // Your DB URL
            String DATABASE_USER = "root"; // Replace with your MySQL username
            String DATABASE_PASSWORD = "password"; // Replace with your MySQL password

            // Establish connection to the database
            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);

            // Create a prepared statement to prevent SQL injection
            preparedStatement = connection.prepareStatement(insertSQL);

            // Set parameters for the PreparedStatement
            preparedStatement.setString(1, studentID); // StudentID
            preparedStatement.setString(2, questionSummary); // QuestionSummary

            // Use java.sql.Timestamp to insert the current time correctly in SQL format
            preparedStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now())); // Current timestamp (Timestamp)

            // Set the value for the "IsQuestionActive" column (true in this case)
            preparedStatement.setBoolean(4, true);

            // Execute the insert statement
            int rowsAffected = preparedStatement.executeUpdate();

            // Check if the insertion was successful
            if (rowsAffected > 0) {
                System.out.println("Record successfully added to the table.");
            } else {
                System.out.println("Failed to add record to the table.");
            }

        } catch (SQLException e) {
            // Handle SQL exception
            e.printStackTrace();
        } finally {
            // Close resources to prevent memory leaks
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close(); // Ensure the connection is also closed
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getQuestionStudent(String questionTableName, String studentID) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // SQL query to retrieve the QuestionSummary for the given studentID and where IsQuestionActive is 1
            String selectSQL = "SELECT QuestionSummary FROM " + questionTableName + " WHERE StudentID = ? AND IsQuestionActive = 1";

            String DATABASE_URL = "jdbc:mysql:// 10.66.201.75/qclient1"; // Your DB URL
            String DATABASE_USER = "root"; // Replace with your MySQL username
            String DATABASE_PASSWORD = "password"; // Replace with your MySQL password

            // Establish connection to the database
            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);

            // Create a prepared statement to prevent SQL injection
            preparedStatement = connection.prepareStatement(selectSQL);

            // Set the parameter for the prepared statement (studentID)
            preparedStatement.setString(1, studentID);

            // Execute the query and get the result set
            resultSet = preparedStatement.executeQuery();

            // If there's a result, return the QuestionSummary, otherwise return an empty string
            if (resultSet.next()) {
                System.out.println("Find Successful");
                return resultSet.getString("QuestionSummary");
            } else {
                return "";  // No active question for the given studentID
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "";  // Return empty string in case of any error
        } finally {
            // Close resources to prevent memory leaks
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close(); // Ensure the connection is also closed
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deactivateQuestion(String tableName, String studentID, String questionSummary) {
        // Ensure table name is safe from SQL injection by validating input
        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }

        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {
            // SQL query to update IsQuestionActive to false (0) for the specified studentID and questionSummary
            String updateSQL = "UPDATE " + tableName + " SET IsQuestionActive = ? WHERE StudentID = ? AND QuestionSummary = ?";

            String DATABASE_URL = "jdbc:mysql:// 10.66.201.75/qclient1"; // Your DB URL
            String DATABASE_USER = "root"; // Replace with your MySQL username
            String DATABASE_PASSWORD = "password"; // Replace with your MySQL password

            // Establish connection to the database
            connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);

            // Create a prepared statement to prevent SQL injection
            preparedStatement = connection.prepareStatement(updateSQL);

            // Set parameters for the PreparedStatement
            preparedStatement.setBoolean(1, false); // Set IsQuestionActive to false (0)
            preparedStatement.setString(2, studentID); // Set StudentID
            preparedStatement.setString(3, questionSummary); // Set QuestionSummary

            // Execute the update statement
            int rowsAffected = preparedStatement.executeUpdate();

            // Check if the update was successful
            if (rowsAffected > 0) {
                System.out.println("Question successfully deactivated.");
            } else {
                System.out.println("Failed to deactivate question. No matching record found.");
            }

        } catch (SQLException e) {
            // Handle SQL exception
            e.printStackTrace();
        } finally {
            // Close resources to prevent memory leaks
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close(); // Ensure the connection is also closed
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

        private boolean doesTableExist(String tableName) {
            String query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
            try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD)) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, tableName);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            return resultSet.getInt(1) > 0; // If count > 0, table exists
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        // Method to check if the column exists in the specified table
        private boolean doesColumnExist(String tableName, String columnName) {
            String query = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
            try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD)) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, tableName);
                    statement.setString(2, columnName);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            return resultSet.getInt(1) > 0; // If count > 0, column exists
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

    public void insertOrUpdateWaitTime(String tableString, String columnName, int remainingTime) {

        tableString = tableString.toLowerCase(); // Ensure table name is lowercase
        //System.out.println("Called: " + tableString + " " + columnName + " " + remainingTime);

        // Check if the table exists
        if (!doesTableExist(tableString)) {
            //System.out.println("Table not found: " + tableString);
            return; // Exit the method if the table doesn't exist
        } else {
            //System.out.println("Table found: " + tableString);
        }

        // Check if the column exists
        if (!doesColumnExist(tableString, columnName)) {
            //System.out.println("Column not found: " + columnName);
            return; // Exit the method if the column doesn't exist
        } else {
            //System.out.println("Column found: " + columnName);
        }

        // SQL query to delete all rows in the table
        String deleteQuery = "DELETE FROM " + tableString;

        // Debugging: print the SQL query to check for correctness
        //System.out.println("Executing query: " + deleteQuery);

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD)) {
            // Prepare the statement to delete all rows
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                // Execute the delete
                int rowsDeleted = deleteStatement.executeUpdate();

                // Debugging: check if the delete was successful
                if (rowsDeleted > 0) {
                    //System.out.println("All rows deleted from table: " + tableString);
                } else {
                    //System.out.println("No rows to delete. Table might already be empty.");
                }
            }

            // SQL query to insert the wait time
            String insertQuery = "INSERT INTO " + tableString + " (" + columnName + ") VALUES (?)";

            // Debugging: print the SQL query to check for correctness
            //System.out.println("Executing query: " + insertQuery);

            // Prepare the statement to insert the wait time
            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                // Set the value of remainingTime in the prepared statement
                insertStatement.setInt(1, remainingTime);

                // Execute the insert
                int rowsInserted = insertStatement.executeUpdate();

                // Debugging: check if the insert was successful
                if (rowsInserted > 0) {
                    //System.out.println("Wait time inserted successfully in table: " + tableString + ", column: " + columnName);
                } else {
                    //System.out.println("No rows inserted. Check if the table exists or if the column is correct.");
                }
            }

        } catch (SQLException e) {
            // Print stack trace for debugging
            System.err.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public int getWaitTimeFromStudent(String tableString, String columnName) {
        int result = -1;  // Default value if the column doesn't exist or an error occurs

        // Build the dynamic select query
        String selectQuery = "SELECT " + columnName + " FROM " + tableString;

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD)) {
            // Prepare the statement for the SELECT query
            try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
                // Execute the query and get the result set
                try (ResultSet resultSet = statement.executeQuery()) {
                    // Check if the result set has any rows
                    if (resultSet.next()) {
                        // Retrieve the value from the column (assuming it's an integer)
                        result = resultSet.getInt(columnName); // Get the value from the specific column
                    } else {
                        //System.out.println("No data found in table: " + tableString + ", column: " + columnName);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return the value retrieved from the table
        return result;
    }

        public static int getQuestionPosition(String tableName, String studentID) {
            int position = -1;
            String query = "SELECT StudentID, QuestionSummary, TimeStamp, IsQuestionActive " +
                    "FROM " + tableName + " " +
                    "WHERE IsQuestionActive = 1 " +
                    "ORDER BY TimeStamp";

            try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
                 Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                int rowNumber = 0;

                // Iterate over the result set and find the position of the given studentID
                while (rs.next()) {
                    String currentStudentID = rs.getString("StudentID");
                    if (currentStudentID.equals(studentID)) {
                        rowNumber++;
                        position = rowNumber;
                        break; // Exit after finding the first match
                    }
                    rowNumber++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return position;
        }

    public void removeActiveQuestion(String studentID, String tableName) {
        String query = "UPDATE " + tableName + " SET isQuestionActive = 0 WHERE studentID = ? AND isQuestionActive = 1";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD); // Assuming you have a method to get DB connection
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, studentID);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Successfully deactivated the active question for student ID: " + studentID);
            } else {
                System.out.println("No active question found for student ID: " + studentID);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearQuestionsList(String tableName1) {
        String sql = "UPDATE " + tableName1 + " SET isQuestionActive = 0";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int affectedRows = pstmt.executeUpdate();
            System.out.println("Cleared questions. Rows affected: " + affectedRows);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to clear questions in table: " + tableName1);
        }
    }

    public String getStudentName(String studentID, String tableName) {
        String studentName = null;
        String query = "SELECT nickname FROM " + tableName + " WHERE studentID = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, studentID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                studentName = rs.getString("nickname");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return studentName;
    }

    public void updateQuestionsTable(String studentID, String tableName3, String s) {
        System.out.println(s + " Message Received for Student " + studentID + " in Table " + tableName3);
        String query = "UPDATE " + tableName3 + " SET Response = ?, isQuestionActive = 0 WHERE studentID = ? AND isQuestionActive = 1";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the parameters for the query
            pstmt.setString(1, s); // Set the Response value
            pstmt.setString(2, studentID); // Set the studentID for the row to be updated

            // Execute the update query
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Successfully updated the question for student ID: " + studentID);
            } else {
                System.out.println("No active question found for student ID: " + studentID);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
