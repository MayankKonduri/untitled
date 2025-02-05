import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TeacherCourses extends JPanel {
    private JFrame frame;
    private String userName;

    public TeacherCourses(JFrame frame, String userName) {
        this.frame = frame;

        this.userName = userName;
        // Set the layout of the panel to null for absolute positioning
        setLayout(null);

        // Create a panel for the home button with flow layout for left alignment
        JPanel homePanel = new JPanel();
        homePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5)); // Flow layout with left alignment

        // Home Button
        JButton homeButton = new JButton("Home");
        homeButton.setFont(new Font("Georgia", Font.BOLD, 10));
        homePanel.add(homeButton);  // Add the home button to the homePanel

        // Position the home button at the top-left corner
        homePanel.setBounds(10, 10, 100, 40); // Adjusted position and size for home button

        // Add the homePanel to the top of the TeacherCourses panel
        add(homePanel);

        // Action Listener to go back to TeacherHome
        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Replace current panel with TeacherHome
                TeacherHome teacherHome = new TeacherHome(frame, userName);
                frame.getContentPane().removeAll();  // Remove all current components
                frame.getContentPane().add(teacherHome);  // Add TeacherHome panel
                frame.revalidate();  // Revalidate to apply changes
                frame.repaint();  // Repaint to reflect the new content
            }
        });

        // Create a label for the "Courses" title and position it in the center
        JLabel titleLabel = new JLabel("Courses");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 20));
        titleLabel.setBounds(150, 25, 200, 30); // Centered position
        add(titleLabel);

        // Create buttons for Period 1 to Period 7 and position them below the title with gaps
        int yPosition1 = 60; // Start position for the first button

        JButton periodButton1 = new JButton("Period 1");
        periodButton1.setFont(new Font("Georgia", Font.PLAIN, 14));
        periodButton1.setBounds(100, yPosition1, 180, 25);  // Set position and size of the button
        add(periodButton1);

        int yPosition2 = yPosition1 + 30; // Position for the second button
        JButton periodButton2 = new JButton("Period 2");
        periodButton2.setFont(new Font("Georgia", Font.PLAIN, 14));
        periodButton2.setBounds(100, yPosition2, 180, 25);
        add(periodButton2);

        int yPosition3 = yPosition2 + 30; // Position for the third button
        JButton periodButton3 = new JButton("Period 3");
        periodButton3.setFont(new Font("Georgia", Font.PLAIN, 14));
        periodButton3.setBounds(100, yPosition3, 180, 25);
        add(periodButton3);

        int yPosition4 = yPosition3 + 30; // Position for the fourth button
        JButton periodButton4 = new JButton("Period 4");
        periodButton4.setFont(new Font("Georgia", Font.PLAIN, 14));
        periodButton4.setBounds(100, yPosition4, 180, 25);
        add(periodButton4);

        int yPosition5 = yPosition4 + 30; // Position for the fifth button
        JButton periodButton5 = new JButton("Period 5");
        periodButton5.setFont(new Font("Georgia", Font.PLAIN, 14));
        periodButton5.setBounds(100, yPosition5, 180, 25);
        add(periodButton5);

        int yPosition6 = yPosition5 + 30; // Position for the sixth button
        JButton periodButton6 = new JButton("Period 6");
        periodButton6.setFont(new Font("Georgia", Font.PLAIN, 14));
        periodButton6.setBounds(100, yPosition6, 180, 25);
        add(periodButton6);

        int yPosition7 = yPosition6 + 30; // Position for the seventh button
        JButton periodButton7 = new JButton("Period 7");
        periodButton7.setFont(new Font("Georgia", Font.PLAIN, 14));
        periodButton7.setBounds(100, yPosition7, 180, 25);
        add(periodButton7);

        periodButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TeacherPeriodView teacherPeriodView = new TeacherPeriodView(frame, 1, userName);

                frame.getContentPane().removeAll();  // Remove all components from the frame
                frame.revalidate();  // Revalidate the frame layout
                frame.repaint();  // Repaint the frame
                frame.setSize(400, 325);
                frame.add(teacherPeriodView);
                frame.setVisible(true);
            }
        });
        periodButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TeacherPeriodView teacherPeriodView = new TeacherPeriodView(frame, 2, userName);

                frame.getContentPane().removeAll();  // Remove all components from the frame
                frame.revalidate();  // Revalidate the frame layout
                frame.repaint();  // Repaint the frame
                frame.setSize(400, 325);
                frame.add(teacherPeriodView);
                frame.setVisible(true);
            }
        });
        periodButton3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TeacherPeriodView teacherPeriodView = new TeacherPeriodView(frame, 3, userName);

                frame.getContentPane().removeAll();  // Remove all components from the frame
                frame.revalidate();  // Revalidate the frame layout
                frame.repaint();  // Repaint the frame
                frame.setSize(400, 325);
                frame.add(teacherPeriodView);
                frame.setVisible(true);
            }
        });
        periodButton4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TeacherPeriodView teacherPeriodView = new TeacherPeriodView(frame ,4, userName);

                frame.getContentPane().removeAll();  // Remove all components from the frame
                frame.revalidate();  // Revalidate the frame layout
                frame.repaint();  // Repaint the frame
                frame.setSize(400, 325);
                frame.add(teacherPeriodView);
                frame.setVisible(true);
            }
        });
        periodButton5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TeacherPeriodView teacherPeriodView = new TeacherPeriodView(frame, 5, userName);

                frame.getContentPane().removeAll();  // Remove all components from the frame
                frame.revalidate();  // Revalidate the frame layout
                frame.repaint();  // Repaint the frame
                frame.setSize(400, 325);
                frame.add(teacherPeriodView);
                frame.setVisible(true);
            }
        });
        periodButton6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TeacherPeriodView teacherPeriodView = new TeacherPeriodView(frame, 6, userName);

                frame.getContentPane().removeAll();  // Remove all components from the frame
                frame.revalidate();  // Revalidate the frame layout
                frame.repaint();  // Repaint the frame
                frame.setSize(400, 325);
                frame.add(teacherPeriodView);
                frame.setVisible(true);
            }
        });
        periodButton7.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TeacherPeriodView teacherPeriodView = new TeacherPeriodView(frame, 7, userName);

                frame.getContentPane().removeAll();  // Remove all components from the frame
                frame.revalidate();  // Revalidate the frame layout
                frame.repaint();  // Repaint the frame
                frame.setSize(400, 325);
                frame.add(teacherPeriodView);
                frame.setVisible(true);
            }
        });
    }
}
