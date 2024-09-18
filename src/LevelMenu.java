import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LevelMenu extends JFrame {

    private JButton[] levelButtons;
    private static final int TOTAL_LEVELS = 10;

    public LevelMenu() {
        setTitle("Select Level");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel levelPanel = new JPanel();
        levelPanel.setLayout(new GridLayout(5, 2, 10, 10));

        levelButtons = new JButton[TOTAL_LEVELS];

        for (int i = 0; i < TOTAL_LEVELS; i++) {
            int levelNumber = i + 1;
            levelButtons[i] = new JButton("Level" + levelNumber);
            levelButtons[i].setFont(new Font("SansSerif", Font.PLAIN, 24));

            if (i == 0) {
                levelButtons[i].setEnabled(true);
                levelButtons[i].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //startLevel(levelNumber);
                        startLevel1();
                    }
                });
            } else {
                levelButtons[i].setEnabled(false);
                levelButtons[i].setForeground(Color.GRAY);
            }
            levelPanel.add(levelButtons[i]);
        }

        add(levelPanel);

        setVisible(true);
    }

    private void startLevel1() {
        System.out.println("Starting Level ");
        this.dispose();
        new Game();

    }
}
