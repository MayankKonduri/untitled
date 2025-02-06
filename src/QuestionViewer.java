import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;

public class QuestionViewer extends JPanel{
    private JFrame frame;
    private String userName;
    public QuestionViewer(JFrame frame, String userName) {
        this.frame = frame;
        this.userName = userName;

        // Home button and panel
        JPanel homePanel = new JPanel();
        homePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton homeButton = new JButton("Home");
        homeButton.setFont(new Font("Georgia", Font.BOLD, 10));  // Set font for button
        homePanel.add(homeButton);

        homePanel.setBounds(10, 25, 100, 100);
        add(homePanel, BorderLayout.NORTH); // Add it at the top of the panel

        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //stopCountdown();  // Stop the countdown timer
                TeacherHome teacherHome = null;
                teacherHome = new TeacherHome(frame, userName);
                frame.getContentPane().removeAll();
                frame.getContentPane().add(teacherHome);
                frame.revalidate();
                frame.repaint();
                frame.setSize(400, 325);  // Resize the frame to fit the home page
            }
        });
    }
}
