import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LevelMenu extends JFrame {

    private JButton startButton;
    private JTextArea hallOfFameArea;

    public LevelMenu() {
        setTitle("Start Level");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
       mainPanel.setLayout(new BorderLayout(20, 20));

        startButton = new JButton("Start Level 1");
        startButton.setFont(new Font("SansSerif", Font.PLAIN, 24));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startLevel1();
            }
        });

        mainPanel.add(startButton, BorderLayout.NORTH);
        hallOfFameArea = new JTextArea();
        hallOfFameArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
        hallOfFameArea.setEditable(false);
        hallOfFameArea.setLineWrap(true);
        hallOfFameArea.setWrapStyleWord(true);
        loadHallOfFame();

        JScrollPane scrollPane = new JScrollPane(hallOfFameArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private void loadHallOfFame() {
        try (BufferedReader reader = new BufferedReader(new FileReader("hall_of_fame.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                hallOfFameArea.append(line + "\n");
            }
        } catch (IOException e) {
            hallOfFameArea.setText("No Hall of Fame records available.");
            e.printStackTrace();
        }
    }

    private void startLevel1() {
        System.out.println("Starting Level ");
        this.dispose();
        new Game();

    }
}
