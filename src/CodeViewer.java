import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class CodeViewer extends JPanel {
    private JFrame frame;
    private String username;
    private DatabaseManager databaseManager;
    private String questionSummary;
    private String consoleOutput;
    private String fileName;
    private byte[] fileData;
    private String tableName3;
    private String studentID;
    private String studentName;

    private JTabbedPane tabbedPane;

    public CodeViewer(JFrame frame, String userName, String studentID, String studentName, String questionSummary,
                      String consoleOutput, String fileName, byte[] fileData, String tableName3) {
        this.frame = frame;
        this.username = userName;
        this.databaseManager = new DatabaseManager(username);
        this.questionSummary = questionSummary;
        this.consoleOutput = consoleOutput;
        this.fileName = fileName;
        this.fileData = fileData;
        this.tableName3 = tableName3;
        this.studentID = studentID;
        this.studentName = studentName;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 700));


        // Add top section for Student Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

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
        try {
            doc.insertString(doc.getLength(), "Student ID: ", boldStyle);  // Bold "ID:"
            doc.insertString(doc.getLength(), studentID + "\n", regularStyle); // Normal Student ID

            doc.insertString(doc.getLength(), "Nickname: ", boldStyle);  // Bold "Nickname:"
            doc.insertString(doc.getLength(), studentName + "\n", regularStyle); // Normal Nickname

            doc.insertString(doc.getLength(), "Question Summary: ", boldStyle);  // Bold "Question Summary:"
            doc.insertString(doc.getLength(), questionSummary + "\n", regularStyle); // Normal Question Summary
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // Add the JTextPane to the info panel
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(750, 75));  // Set preferred size to fit within the window
        infoPanel.add(scrollPane);

        // Add the info panel to the main panel
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the panel
        add(infoPanel, BorderLayout.NORTH);

        // Add the tabbed pane for code files
        tabbedPane = new JTabbedPane();
        unzipAndDisplayCodeFiles();  // Unzip and display code files in tabs

        // Create the console output area
        JTextArea consoleTextArea = new JTextArea(consoleOutput);
        consoleTextArea.setEditable(false);
        consoleTextArea.setFont(new Font("Consolas", Font.PLAIN, 12)); // Use Arial font
        JScrollPane consoleScrollPane = new JScrollPane(consoleTextArea);
        consoleScrollPane.setPreferredSize(new Dimension(750, 200)); // Adjusted height for console output area

        // Use JSplitPane to allow resizing between code and console sections
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabbedPane, consoleScrollPane);
        splitPane.setResizeWeight(0.8); // Gives more space to the code part initially
        splitPane.setDividerLocation(600); // Adjust this value to your preference
        add(splitPane, BorderLayout.CENTER);

        // Add action buttons
        JPanel buttonPanel = new JPanel();
        JButton comingOverButton = createButton("Coming Over", e -> handleComingOver());
        JButton sendResponseButton = createButton("Send Response", e -> handleSendResponse());
        JButton homeButton = new JButton("Go Back");
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.getContentPane().removeAll();
                frame.getContentPane().add(new QuestionViewer(frame, userName));
                frame.revalidate();
                frame.repaint();
                frame.setSize(400,400);
            }
        });
        comingOverButton.setFont(new Font("Georgia",Font.PLAIN, 14));
        sendResponseButton.setFont(new Font("Georgia",Font.PLAIN, 14));
        homeButton.setFont(new Font("Georgia", Font.PLAIN, 14));

        buttonPanel.add(comingOverButton);
        buttonPanel.add(sendResponseButton);
        buttonPanel.add(homeButton);
        add(buttonPanel, BorderLayout.PAGE_END);
    }

    /**
     * Unzips the code files and displays them in tabs, with line numbers next to the code.
     */
    private void unzipAndDisplayCodeFiles() {
        try {
            // Unzip the fileData into a temporary directory
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "codeFiles");
            tempDir.mkdir();

            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(fileData))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        // Read the file content
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            byteArrayOutputStream.write(buffer, 0, length);
                        }

                        // Add a new tab for each file
                        String fileContent = byteArrayOutputStream.toString();
                        String fileName = entry.getName();
                        JTextPane textPane = new JTextPane();
                        textPane.setText(fileContent);
                        textPane.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Monospaced font for code

                        // Add line numbers to the side of the code
                        JTextArea lineNumbers = new JTextArea(getLineNumbers(fileContent));
                        lineNumbers.setEditable(false);
                        lineNumbers.setFont(new Font("Monospaced", Font.PLAIN, 12));
                        lineNumbers.setBackground(Color.LIGHT_GRAY);
                        lineNumbers.setMargin(new Insets(0, 10, 0, 10));

                        JPanel codePanel = new JPanel(new BorderLayout());
                        codePanel.add(new JScrollPane(lineNumbers), BorderLayout.WEST);
                        codePanel.add(new JScrollPane(textPane), BorderLayout.CENTER);

                        // Wrap the code content in a JScrollPane for scrolling
                        JScrollPane codeScrollPane = new JScrollPane(codePanel);
                        tabbedPane.addTab(fileName, codeScrollPane);
                        tabbedPane.setFont(new Font("Georgia",Font.PLAIN,12));
                    }
                    zis.closeEntry();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate line numbers for the code content.
     *
     * @param code The code content as a string.
     * @return A string representing line numbers.
     */
    private String getLineNumbers(String code) {
        String[] lines = code.split("\n");
        StringBuilder lineNumbers = new StringBuilder();
        for (int i = 1; i <= lines.length; i++) {
            lineNumbers.append(i).append("\n");
        }
        return lineNumbers.toString();
    }

    /**
     * Creates a button with the specified label and action listener.
     *
     * @param text   The text to display on the button.
     * @param action The action listener to handle button clicks.
     * @return The created button.
     */
    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12)); // Use Arial font
        button.addActionListener(action);
        return button;
    }

    /**
     * Handles the "Coming Over" button action.
     */
    private void handleComingOver() {
        System.out.println("Coming Over selected");
        databaseManager.updateQuestionsTable(studentID, tableName3, "Went to Student's Desk");
        frame.getContentPane().removeAll();
        frame.getContentPane().add(new QuestionViewer(frame, username));
        frame.revalidate();
        frame.repaint();
        frame.setSize(400, 400);
    }

    /**
     * Handles the "Send Response" button action.
     */
    private void handleSendResponse() {
        System.out.println("Send Response selected");

        JTextField responseField = new JTextField(20);
        responseField.setFont(new Font("Georgia", Font.PLAIN, 12));

        JLabel promptLabel = new JLabel("Type your response:");
        promptLabel.setFont(new Font("Georgia", Font.PLAIN, 12));

        JPanel panel = new JPanel();
        panel.add(promptLabel);
        panel.add(responseField);

        int option = JOptionPane.showConfirmDialog(frame, panel, "Enter Response", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String userResponse = responseField.getText();
            System.out.println("User response: " + userResponse);
            databaseManager.updateQuestionsTable(studentID, tableName3, userResponse);
            frame.getContentPane().removeAll();
            frame.getContentPane().add(new QuestionViewer(frame, username));
            frame.revalidate();
            frame.repaint();
            frame.setSize(400, 400);
        } else {
            System.out.println("Response input was canceled.");
        }
    }
}
