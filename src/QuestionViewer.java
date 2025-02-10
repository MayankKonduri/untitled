import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class QuestionViewer extends JPanel {
    private JFrame frame;
    private String userName;

    public QuestionViewer(JFrame frame, String userName) {
        this.frame = frame;
        this.userName = userName;

        setLayout(null); // Use null layout for absolute positioning

        // Home button and panel
        JPanel homePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton homeButton = new JButton("Home");
        homeButton.setFont(new Font("Georgia", Font.BOLD, 10));
        homePanel.add(homeButton);
        homePanel.setBounds(10, 10, 100, 40);
        add(homePanel);

        // Title Label
        JLabel titleLabel = new JLabel("Questions Viewer");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 18));
        titleLabel.setBounds(0, 20, 400, 20);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel); // Add directly to the QuestionViewer panel

        // Navigation Bar
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        navBar.setBounds(50, 60, 300, 31);
        navBar.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        navBar.setBackground(Color.LIGHT_GRAY);

        JButton prevButton = new JButton("<");
        prevButton.setBounds(55,63,50,25);
        prevButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        prevButton.setBackground(Color.WHITE);
        JButton nextButton = new JButton(">");
        nextButton.setBounds(295,63,50,25);
        nextButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        nextButton.setBackground(Color.WHITE);
        JLabel pageLabel = new JLabel("1");
        pageLabel.setForeground(Color.black);
        pageLabel.setFont(new Font("Georgia", Font.BOLD, 16));
        prevButton.setFont(new Font("Georgia", Font.BOLD,16));
        nextButton.setFont(new Font("Georgia", Font.BOLD,16));

        add(prevButton);
        navBar.add(pageLabel);
        add(nextButton);
        add(navBar); // Add directly to the QuestionViewer panel


        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setBounds(50, 105, 300, 230);
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        add(contentPanel); // Add directly to the QuestionViewer panel

        // Page Number Logic
        final int[] currentPage = {1};
        final int maxPages = 7; // Store the maximum number of pages

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage[0] = (currentPage[0] == 1) ? maxPages : currentPage[0] - 1;
                pageLabel.setText(String.valueOf(currentPage[0]));
                updateContent(contentPanel, currentPage[0]);
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPage[0] = (currentPage[0] == maxPages) ? 1 : currentPage[0] + 1;
                pageLabel.setText(String.valueOf(currentPage[0]));
                updateContent(contentPanel, currentPage[0]);
            }
        });

        homeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TeacherHome teacherHome = null;
                teacherHome = new TeacherHome(frame, userName);
                frame.getContentPane().removeAll();
                frame.getContentPane().add(teacherHome);
                frame.revalidate();
                frame.repaint();
                frame.setSize(400, 325);
            }
        });

        updateContent(contentPanel, currentPage[0]); // Initialize content

        setVisible(true);
    }

    private static void updateContent(JPanel contentPanel, int pageNumber) {
        Color color;
        switch (pageNumber) {
            case 1: color = Color.RED; break;
            case 2: color = Color.BLUE; break;
            case 3: color = Color.GREEN; break;
            case 4: color = Color.ORANGE; break;
            case 5: color = Color.CYAN; break;
            case 6: color = Color.MAGENTA; break;
            case 7: color = Color.YELLOW; break;
            default: color = Color.GRAY; break;
        }
        contentPanel.setBackground(color);
    }
}