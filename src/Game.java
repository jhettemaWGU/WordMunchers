import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class Game extends JFrame {

    private JPanel boardPanel;
    private JLabel scoreLabel, livesLabel, levelLabel;
    private int score = 0;
    private int level = 1;
    private int lives = 3;
    private int playerRow = 0;
    private int playerCol = 0;
    private final int ROWS = 5;
    private final int COLS = 6;
    private String[][] wordGrid;
    private final String[] words = {"bell", "fell", "tell", "sell", "mall", "ball", "call", "tall", "small", "wall"};

    public Game() {
        setTitle("Word Munchers");
        setSize(1600, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        boardPanel = new JPanel(new GridLayout(ROWS, COLS));
        add(boardPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new BorderLayout());
        scoreLabel = new JLabel("Score: " + score);
        livesLabel = new JLabel("Lives: " + lives);
        levelLabel = new JLabel("Level: " + level);
        infoPanel.add(scoreLabel, BorderLayout.WEST);
        infoPanel.add(levelLabel, BorderLayout.EAST);
        infoPanel.add(livesLabel, BorderLayout.SOUTH);
        add(infoPanel, BorderLayout.NORTH);

        wordGrid = new String[ROWS][COLS];
        initializeWorldGrid();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        setVisible(true);
    }

    private void initializeWorldGrid() {
        Random rand = new Random();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                wordGrid[row][col] = words[rand.nextInt(words.length)];
            }
        }
        updateBoard();
    }

    private void updateBoard() {
        boardPanel.removeAll();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                JLabel cellLabel = new JLabel(wordGrid[row][col], JLabel.CENTER);
                cellLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                if (row == playerRow && col == playerCol) {
                    cellLabel.setOpaque(true);
                    cellLabel.setBackground(Color.GREEN);
                }
                boardPanel.add(cellLabel);
            }
        }
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void handleKeyPress(KeyEvent e) {
        int keyCode = e.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_UP:
                if (playerRow > 0) playerRow--;
                break;
            case KeyEvent.VK_DOWN:
                if (playerRow < ROWS - 1) playerRow++;
                break;
            case KeyEvent.VK_LEFT:
                if (playerCol > 0) playerCol--;
                break;
            case KeyEvent.VK_RIGHT:
                if (playerCol < COLS - 1) playerCol++;
                break;
            case KeyEvent.VK_SPACE:
                munch();

        }
        //checkWord();
        updateBoard();
    }

    private boolean checkWord() {
        String currentWord = wordGrid[playerRow][playerCol];

        if (currentWord.contains("e")) {
            return true;
            //score += 10;
            //scoreLabel.setText("Score: " + score);
        }
        return false;
    }

    private void munch() {
        if (checkWord()) {
            score += 10;
            scoreLabel.setText("Score: " + score);
        } else {
            if (score >= 10) {
                score -= 10;
                scoreLabel.setText("Score: " + score);
            }

            lives -= 1;
            livesLabel.setText("Lives: " + lives);
            displayLoseText();
        }
        wordGrid[playerRow][playerCol] = "";
        updateBoard();
    }

    private void displayLoseText() {
        if (lives > 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Incorrect word! Try again.",
                    "Oops!",
                    JOptionPane.WARNING_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "You are all out of lives.",
                    "Good try. Give it another shot.",
                    JOptionPane.WARNING_MESSAGE
            );
            this.dispose();
            new StartScreen();
        }
    }
}
