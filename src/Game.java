import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class Game extends JFrame {

    private JPanel boardPanel;
    private JLabel scoreLabel, livesLabel, levelLabel, stageLabel, clueLabel;
    private int score = 0;
    private int level = 1;
    private int stage = 1;
    private int lives = 3;
    private int playerRow = 0;
    private int playerCol = 0;
    private final int ROWS = 5;
    private final int COLS = 6;
    private String[][] wordGrid;
    private final String[] words = {"bell", "fell", "tell", "sell", "bed", "dead", "head", "yell", "smell", "mall", "ball", "call", "tall", "stall", "small", "wall", "stall", "fall", "hall", "hill", "pill", "mill", "drill", "still", "kill", "chill", "sit", "pit"};
    private final String[][] correctWords = {
            {"bell", "fell", "tell", "sell", "yell", "smell", "bed", "dead", "head"},
            {"small", "wall", "stall", "call", "tall", "mall", "hall", "fall", "stall", "ball"},
            {"hill", "pill", "mill", "drill", "still", "kill", "chill", "sit", "pit"}
    };
    private boolean canMove = true;
    private Timer gameTimer;
    private Timer enemyTimer;
    private Timer enemySpawnTimer;
    private List<Enemy> enemies;
    private int maxEnemies = 1;
    private int currentStage = 1;
    private static final int TOTAL_STAGES = 3;


    public Game() {
        enemies = new ArrayList<>();

        setTitle("Word Munchers");
        setSize(1600, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        boardPanel = new JPanel(new GridLayout(ROWS, COLS));
        add(boardPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new BorderLayout());
        Font labelFont = new Font("Monospaced", Font.BOLD, 24);
        scoreLabel = new JLabel("Score: " + score);
        livesLabel = new JLabel("Lives: " + lives);
        levelLabel = new JLabel("Level: " + level);
        clueLabel = new JLabel(showClue(1, 1));
        stageLabel = new JLabel("Stage: " + stage);

        scoreLabel.setFont(labelFont);
        livesLabel.setFont(labelFont);
        levelLabel.setFont(labelFont);
        stageLabel.setFont(labelFont);
        clueLabel.setFont(new Font("Monospaced", Font.BOLD, 36));

        JPanel cluePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cluePanel.add(clueLabel);
        infoPanel.add(cluePanel, BorderLayout.NORTH);

        JPanel eastPanel = new JPanel(new GridLayout(2, 1));
        eastPanel.add(levelLabel);
        eastPanel.add(stageLabel);

        JPanel westPanel = new JPanel(new GridLayout(2, 1));
        westPanel.add(livesLabel);
        westPanel.add(scoreLabel);

        infoPanel.add(westPanel, BorderLayout.WEST);
        infoPanel.add(eastPanel, BorderLayout.EAST);
        //infoPanel.add(clueLabel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.NORTH);

        wordGrid = new String[ROWS][COLS];
        initializeWorldGrid();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });

        gameTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameTick();
            }
        });
        gameTimer.start();

        initializeEnemies();
        startEnemyMovement();

        enemySpawnTimer = new Timer(10000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                spawnNewEnemyIfNeeded();
            }
        });
        enemySpawnTimer.start();

        setVisible(true);
    }

    private void gameTick() {
        if (!canMove) {
            canMove = true;
        }
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
        Font wordFont = new Font("Monospaced", Font.BOLD, 36);
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                JLabel cellLabel = new JLabel(wordGrid[row][col], JLabel.CENTER);
                cellLabel.setFont(wordFont);
                cellLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                if (row == playerRow && col == playerCol) {
                    cellLabel.setOpaque(true);
                    cellLabel.setBackground(Color.GREEN);
                }

                boolean isEnemyHere = false;
                for(Enemy enemy : enemies) {
                    if(enemy.row == row && enemy.col == col) {
                        isEnemyHere = true;
                        break;
                    }
                }

                if (isEnemyHere) {
                    cellLabel.setOpaque(true);
                    cellLabel.setBackground(Color.RED);
                }
                boardPanel.add(cellLabel);
            }
        }
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void handleKeyPress(KeyEvent e) {
        if (canMove) {
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
            detectCollision();
            canMove = false;
            updateBoard();
        }

    }

    private boolean isWordCorrect(String currentWord, String[][] correctWords) {
        for (String word : correctWords[currentStage - 1]) {
            if (currentWord.equals(word)) {
                return true;
            }
        }
        return false;
    }

    private void munch() {
        String currentCell = wordGrid[playerRow][playerCol];
        if (currentCell.equals("")) {
            return;
        }

        if (isWordCorrect(currentCell, correctWords)) {
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

        checkVictory();
    }

    private void displayLoseText() {
        gameTimer.stop();
        enemyTimer.stop();
        canMove = false;

        if (lives > 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Incorrect word! Try again.",
                    "Oops!",
                    JOptionPane.WARNING_MESSAGE
            );
            resumeGame();
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

    private void displayCollisionText() {
        gameTimer.stop();
        enemyTimer.stop();
        lives -= 1;
        livesLabel.setText("Lives: " + lives);
        canMove = false;

        if (lives > 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "You collided with a Dittums! You lost a life.",
                    "A Dittums ate you! Try again.",
                    JOptionPane.WARNING_MESSAGE
            );
            resumeGame();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "You were eaten by a Dittums and have no more lives.",
                    "Sorry. You were eaten are out of lives.",
                    JOptionPane.WARNING_MESSAGE
            );
            this.dispose();
            new StartScreen();
        }
    }

    private void resumeGame() {
        gameTimer.start();
        enemyTimer.start();
        canMove= true;
        respawnPlayer();
    }

    private String showClue(int level, int stage) {
        if (level == 1) {
            if (stage == 1) {
                return "short 'e' sound as in BELL";
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    private void detectCollision() {
        for (Enemy enemy : enemies) {
            if (enemy.getRow() == playerRow && enemy.getCol() == playerCol) {
                handlePlayerCollision();
                return;
            }
        }
    }

    private Position findValidRespawn() {
        Random rand = new Random();

        while (true) {
            int row = rand.nextInt(ROWS);
            int col = rand.nextInt(COLS);
            Position potentialPosition = new Position(row, col);

            boolean isOccupied = false;
            for (Enemy enemy : enemies) {
                if (enemy.getRow() == row && enemy.getCol() == col) {
                    isOccupied = true;
                    break;
                }
            }

            if (isOccupied) {
                continue;
            }

            boolean isAdjacentToEnemy = false;
            for (Enemy enemy : enemies) {
                int enemyRow = enemy.getRow();
                int enemyCol = enemy.getCol();

                if (Math.abs(enemyRow - row) <=1 && Math.abs(enemyCol - col) <= 1) {
                    isAdjacentToEnemy = true;
                    break;
                }
            }

            if (!isAdjacentToEnemy) {
                return potentialPosition;
            }
        }
    }

    private void respawnPlayer() {
        Position position = findValidRespawn();
        playerRow = position.getRow();
        playerCol = position.getCol();
        updateBoard();
    }

    private void handlePlayerCollision() {
        boardPanel.removeAll();
        updateBoard();
        displayCollisionText();
    }

    private void initializeEnemies() {
        Random rand = new Random();
        int row = rand.nextInt(ROWS);
        int col = rand.nextInt(COLS);
        enemies.add(new Enemy(row, col));
    }

    private void startEnemyMovement() {
        enemyTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveEnemies();
            }
        });
        enemyTimer.start();
    }

    private void setEnemySpeedForStage() {
        int baseSpeed = 2000;
        int speedIncrease = 200;
        int enemySpeed = baseSpeed - (speedIncrease * (currentStage - 1));

        enemyTimer.setDelay(enemySpeed);
    }

    private void moveEnemies() {
        Random rand = new Random();
        Iterator<Enemy> enemyIterator = enemies.iterator();

        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            int newRow = enemy.getRow();
            int newCol = enemy.getCol();
            int direction = rand.nextInt(4);

            switch (direction) {
                case 0:
                    newRow = Math.max(0, enemy.getRow() - 1);
                    break;
                case 1:
                    newRow = Math.min(ROWS - 1, enemy.getRow() + 1);
                    break;
                case 2:
                    newCol = Math.max(0, enemy.getCol() - 1);
                    break;
                case 3:
                    newCol = Math.min(COLS - 1, enemy.getCol() + 1);
                    break;
            }

            if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS) {
                enemy.setRow(newRow);
                enemy.setCol(newCol);
            }

            wordGrid[enemy.row][enemy.col] = words[rand.nextInt(words.length)];
            checkVictory();

            if (newRow == playerRow && newCol == playerCol) {
                detectCollision();
            }

            for (Enemy otherEnemy : enemies) {
                if (otherEnemy != enemy && otherEnemy.getRow() == newRow && otherEnemy.getCol() == newCol) {
                    //play crunch .wav file
                    enemyIterator.remove();
                    break;
                }
            }
        }
        updateBoard();
    }

    private void spawnNewEnemyIfNeeded() {
        if (enemies.size() < maxEnemies) {
            Random rand = new Random();
            int newRow = 0, newCol = 0;
            int edge = rand.nextInt(4);

            switch (edge) {
                case 0:
                    newRow = 0;
                    newCol = rand.nextInt(COLS);
                    break;
                case 1:
                    newRow = ROWS - 1;
                    newCol = rand.nextInt(COLS);
                    break;
                case 2:
                    newRow = rand.nextInt(ROWS);
                    newCol = 0;
                    break;
                case 3:
                    newRow = rand.nextInt(ROWS);
                    newCol = COLS - 1;
                    break;
            }
            enemies.add(new Enemy(newRow, newCol));
            updateBoard();
        }
    }

    private boolean hasRemainingWords(String[] correctWords) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                String wordAtCell = wordGrid[row][col];

                for (String correctWord : correctWords) {
                    if (wordAtCell.equals(correctWord)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void checkVictory() {
        if (!hasRemainingWords(correctWords[currentStage - 1])) {
            gameTimer.stop();
            enemyTimer.stop();
            if (currentStage < TOTAL_STAGES) {
                currentStage++;
                JOptionPane.showMessageDialog(
                        this,
                        "Stage " + currentStage + " begins! Get ready for a new challenge!",
                        "Stage Complete!",
                        JOptionPane.INFORMATION_MESSAGE
                );
                startNewStage();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Congratulations! You have completed all stages.",
                        "Level Complete",
                        JOptionPane.INFORMATION_MESSAGE
                );

                this.dispose();
                new LevelMenu();
            }
        }
    }

    private void startNewStage() {
        updateStageSettings();
        initializeWorldGrid();
        respawnPlayer();
        resetEnemies();
        gameTimer.start();
        startEnemyMovement();
        updateBoard();
    }

    private void updateStageSettings() {
        String[] clues = {
                "The 'e' sound in bell (Stage 1)",
                "The 'a' sound in ball (Stage 2)",
                "The 'i' sound in mill (Stage 3)",
        };

        switch (currentStage) {
            case 2:
                maxEnemies = 2;
                clueLabel.setText(clues[currentStage - 1]);
                setEnemySpeedForStage();
                break;
            case 3:
                maxEnemies = 3;
                clueLabel.setText(clues[currentStage - 1]);
                setEnemySpeedForStage();
                break;
            default:
                maxEnemies = 1;
                clueLabel.setText(clues[currentStage - 1]);
                setEnemySpeedForStage();
                break;
        }
    }

    private void resetEnemies() {
        enemies.clear();
        initializeEnemies();
    }

    private void handleVictory() {
        gameTimer.stop();
        enemyTimer.stop();
        JOptionPane.showMessageDialog(
                this,
                "Congratulations! You've completed the stage!",
                "Victory",
                JOptionPane.INFORMATION_MESSAGE
        );
        this.dispose();
        new LevelMenu();
    }
}
