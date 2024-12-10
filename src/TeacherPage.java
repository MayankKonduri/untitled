import javax.swing.*;
import java.awt.*;

public class TeacherPage {
    public TeacherPage(JFrame frame, String studentId, String teacherName) {
        // Set up the main frame panel
        JPanel mainPanel = new JPanel(null);  // Using null layout to manually control component positions

        // Get the dimensions of the frame
        int frameWidth = frame.getWidth();
        int frameHeight = frame.getHeight();

        // Welcome label
        JLabel welcomeLabel = new JLabel("Welcome, " + teacherName + "!");
        welcomeLabel.setFont(new Font("Georgia", Font.BOLD, 16));

        // Calculate the position to center the welcome label
        int welcomeLabelWidth = welcomeLabel.getPreferredSize().width;
        int welcomeLabelHeight = welcomeLabel.getPreferredSize().height;
        welcomeLabel.setBounds((frameWidth - welcomeLabelWidth) / 2, 20, welcomeLabelWidth, welcomeLabelHeight);  // Centering
        mainPanel.add(welcomeLabel);

        // Create the buttons
        JButton questionViewerButton = new JButton("Question Viewer");
        JButton settingsButton = new JButton("Settings (Courses)");
        JButton reportsButton = new JButton("Reports");

        // Set font and size for all buttons
        Font buttonFont = new Font("Georgia", Font.PLAIN, 14);
        questionViewerButton.setFont(buttonFont);
        settingsButton.setFont(buttonFont);
        reportsButton.setFont(buttonFont);

        // Set the preferred size for the buttons
        questionViewerButton.setPreferredSize(new Dimension(250, 40));
        settingsButton.setPreferredSize(new Dimension(250, 40));
        reportsButton.setPreferredSize(new Dimension(250, 40));

        // Calculate the vertical spacing and center the buttons
        int buttonWidth = questionViewerButton.getPreferredSize().width;
        int buttonHeight = questionViewerButton.getPreferredSize().height;
        int buttonSpacing = 10;  // Vertical spacing between buttons

        // Set positions for the buttons, centered horizontally and spaced vertically
        int yPosition = (frameHeight - (buttonHeight * 3 + buttonSpacing * 2)) / 2; // Center buttons vertically
        questionViewerButton.setBounds((frameWidth - buttonWidth) / 2, yPosition, buttonWidth, buttonHeight);
        settingsButton.setBounds((frameWidth - buttonWidth) / 2, yPosition + buttonHeight + buttonSpacing, buttonWidth, buttonHeight);
        reportsButton.setBounds((frameWidth - buttonWidth) / 2, yPosition + 2 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight);

        // Add buttons to the main panel
        mainPanel.add(questionViewerButton);
        mainPanel.add(settingsButton);
        mainPanel.add(reportsButton);

        // Add the main panel to the frame
        frame.add(mainPanel);

        // Final frame setup
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);  // Make the frame visible



        settingsButton.addActionListener(e -> {
            new TeacherCourses(frame, teacherName);
        });
    }
}
