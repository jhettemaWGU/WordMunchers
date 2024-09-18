import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartScreen extends JFrame{
    public StartScreen() {
        setTitle("WordMunchers");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("WordMunchers", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));
        titleLabel.setForeground(Color.GREEN);
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("SansSerif", Font.PLAIN, 24));
        startButton.setFocusPainted(false);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });
        buttonPanel.add(startButton);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.setBackground(Color.BLACK);
        buttonPanel.setBackground(Color.BLACK);
        add(panel);
        setVisible(true);
    }

    private void startGame() {
        System.out.println("Starting Game");
        new LevelMenu();
        this.dispose();
    }
}
