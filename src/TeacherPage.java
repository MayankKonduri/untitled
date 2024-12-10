import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class TeacherPage {
    public TeacherPage(JFrame frame, String studentId, String teacherName) {
        JPanel welcomePanel = new JPanel();
        JLabel welcomeLabel = new JLabel("Welcome, " + teacherName + "!");
        welcomeLabel.setFont(new Font("Georgia", Font.BOLD, 16));
        welcomePanel.add(welcomeLabel);
        frame.add(welcomePanel);
        frame.setVisible(true);
    }
}
