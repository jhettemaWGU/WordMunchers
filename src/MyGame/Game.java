package MyGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.Timer;
import java.util.List;

public class Game extends JFrame {

    private String playerName;
    private JPanel boardPanel;
    private JLabel scoreLabel, livesLabel, levelLabel, stageLabel, clueLabel, timerLabel;
    private int score = 0;
    private int currentLevel = 1;
    private int currentStage = 1;
    private int lives = 3;
    private int playerRow = 0;
    private int playerCol = 0;
    private final int ROWS = 5;
    private final int COLS = 6;
    private String[][] wordGrid;
    private boolean canMove = true;
    private Timer gameTimer;
    private int timeElapsed;
    private long stageStartTime;
    private List<Integer> stageTimes = new ArrayList<>();
    private Timer enemyTimer;
    private Timer enemySpawnTimer;
    private List<Enemy> enemies;
    private int maxEnemies = 1;
    private static final int TOTAL_STAGES = 3;
    private static final int TOTAL_LEVELS = 3;
    private boolean crossedOneHundred = false;
    private boolean crossedOneThousand = false;

    private final String[][] wordBank = {
            {"bell", "fell", "tell", "sell", "bed", "dead", "head", "yell", "smell", "mall",
                    "ball", "call", "tall", "stall", "small", "wall", "stall", "fall", "hall", "hill",
                    "pill", "mill", "drill", "still", "kill", "chill", "sit", "pit", "cold", "told"},

            {"cat", "bat", "hat", "rat", "man", "can", "fan", "tap", "map", "nap", "ball", "call", "tall", "stall", "small", "wall",
                    "hot", "pot", "dot", "mop", "hop", "top", "rock", "sock", "stop", "job", "slope", "hope", "soap", "spoke",
                    "cute", "tube", "dune", "rule", "mule", "cube", "blue", "glue", "prune", "flute", "sun", "fun", "gun", },

            {"happy", "brave", "silly", "quiet", "shiny", "tall", "gentle", "curious", "bright", "fuzzy",
                    "jump", "play", "read", "write", "dance", "think", "laugh", "climb", "draw", "swim",
                    "teacher", "friend", "library", "mountain", "garden", "puzzle", "bicycle", "sundae", "ocean", "castle"}

    };
    private final String[][] correctWords = {
            {"bell", "fell", "tell", "sell", "yell", "smell", "bed", "dead", "head"},
            {"small", "wall", "stall", "call", "tall", "mall", "hall", "fall", "stall", "ball"},
            {"hill", "pill", "mill", "drill", "still", "kill", "chill", "sit", "pit"},

            {"cat", "bat", "hat", "rat", "man", "can", "fan", "tap", "map", "nap"},
            {"hot", "pot", "dot", "mop", "hop", "top", "rock", "sock", "stop", "job"},
            {"cute", "tube", "dune", "rule", "mule", "cube", "blue", "glue", "prune", "flute"},

            {"happy", "brave", "silly", "quiet", "shiny", "tall", "gentle", "curious", "bright", "fuzzy"},
            {"jump", "play", "read", "write", "dance", "think", "laugh", "climb", "draw", "swim"},
            {"teacher", "friend", "library", "mountain", "garden", "puzzle", "bicycle", "sundae", "ocean", "castle"}
    };
    private String[] clues = {
            "short 'e' sound as in bell",
            "short 'a' sound as in ball",
            "short 'i' sound as in sit",

            "short 'a' sound as in cat",
            "short 'o' sound as in hot",
            "long 'u' sound as in mule",

            "adjective",
            "verb",
            "noun"
    };

    private Color[] levelColors = {
            new Color(240, 240, 240),
            new Color(230, 255, 230),
            new Color(230, 230, 255),
    };


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
        levelLabel = new JLabel("Level: " + currentLevel);
        clueLabel = new JLabel(showClue(1, 1));
        stageLabel = new JLabel("Stage: " + currentStage);
        timerLabel = new JLabel("Time: " + timeElapsed);

        scoreLabel.setFont(labelFont);
        livesLabel.setFont(labelFont);
        levelLabel.setFont(labelFont);
        stageLabel.setFont(labelFont);
        timerLabel.setFont(labelFont);
        clueLabel.setFont(new Font("Monospaced", Font.BOLD, 36));


        JPanel cluePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cluePanel.add(clueLabel);
        infoPanel.add(cluePanel, BorderLayout.NORTH);

        JPanel eastPanel = new JPanel(new GridLayout(2, 1));
        eastPanel.add(levelLabel);
        eastPanel.add(stageLabel);

        JPanel westPanel = new JPanel(new GridLayout(3, 1));
        westPanel.add(livesLabel);
        westPanel.add(scoreLabel);
        westPanel.add(timerLabel);

        infoPanel.add(westPanel, BorderLayout.WEST);
        infoPanel.add(eastPanel, BorderLayout.EAST);
        add(infoPanel, BorderLayout.NORTH);

        wordGrid = new String[ROWS][COLS];
        initializeWorldGrid();
        stageStartTime = System.currentTimeMillis();
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
        timeElapsed++;

        if (timeElapsed % 10 == 0) {
            int secondsElapsed = timeElapsed / 10;
            timerLabel.setText("Time: " + secondsElapsed);
        }
        if (!canMove) {
            canMove = true;
        }
    }

    private void initializeWorldGrid() {
        Random rand = new Random();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                wordGrid[row][col] = wordBank[currentLevel - 1][rand.nextInt(wordBank[currentLevel - 1].length)];
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
                    break;
                case KeyEvent.VK_DELETE:
                    winStage();
                    break;
            }
            detectCollision();
            canMove = false;
            updateBoard();
        }

    }

    private boolean isWordCorrect(String currentWord, String[][] correctWords) {
        for (String word : correctWords[(currentLevel - 1) * 3 + (currentStage - 1)]) {
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
            if (score > 90 && crossedOneHundred == false) {
                lives += 1;
                livesLabel.setText("Lives: " + lives);
                crossedOneHundred = true;
                playSound("src/Resources/439889__simonbay__lushlife_levelup.wav");
            }

            if (score > 990 && crossedOneThousand == false) {
                lives += 1;
                livesLabel.setText("Lives: " + lives);
                crossedOneThousand = true;
                // play sound for extra life
            }
            playSound("src/Resources/524609__clearwavsound__bone-crunch.wav");
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
        playSound("src/Resources/79431__kyster__bell-010.wav");

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
            new LevelMenu();
        }
    }

    private void displayCollisionText() {
        gameTimer.stop();
        enemyTimer.stop();
        lives -= 1;
        livesLabel.setText("Lives: " + lives);
        canMove = false;
        playSound("src/Resources/79431__kyster__bell-010.wav");

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

    private String showClue(int currentLevel, int currentStage) {
        return clues[(currentLevel - 1) * 3 + (currentStage - 1)];
    }

    private void detectCollision() {
        for (Enemy enemy : enemies) {
            if (enemy.getRow() == playerRow && enemy.getCol() == playerCol) {
                playSound("src/Resources/608118__crazybeatsinc__light-crunch.wav");
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

            if (wordGrid[enemy.row][enemy.col] != "") {
                wordGrid[enemy.row][enemy.col] = wordBank[currentLevel - 1][rand.nextInt(wordBank[currentLevel - 1].length)]; // check all uses of wordBank. I'm tired and can't remember which goes first for the 2d array.
                checkVictory();
            }


            if (newRow == playerRow && newCol == playerCol) {
                detectCollision();
            }

            for (Enemy otherEnemy : enemies) {
                if (otherEnemy != enemy && otherEnemy.getRow() == newRow && otherEnemy.getCol() == newCol) {
                    playSound("src/Resources/608118__crazybeatsinc__light-crunch.wav");
                    enemyIterator.remove();
                    break;
                }
            }
        }
        updateBoard();
    }

    private void spawnNewEnemyIfNeeded() {
        if (enemies.size() < maxEnemies) {
            playSound("src/Resources/331668__nicola_ariutti__brass_bell_01_take2.wav");
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
        if (!hasRemainingWords(correctWords[(currentLevel - 1) * 3 + (currentStage - 1)])) {
            gameTimer.stop();
            enemyTimer.stop();
            int stageTime = (int) ((System.currentTimeMillis() - stageStartTime) / 1000);
            stageTimes.add(stageTime);

            if (isNewRecord(timeElapsed, currentStage)) {
                playSound("src/Resources/428156__higgs01__yay.wav");
                playerName = promptForName();
                saveStageTime(currentLevel, currentStage, stageTime, playerName);
                updateHallOfFame(playerName, stageTime, currentLevel, currentStage);
            } else {
                playSound("src/Resources/413614__pjcohen__orchestral_concert_tamtam_gong_06.wav");
            }

            if (currentStage < TOTAL_STAGES && currentLevel <= TOTAL_LEVELS) {
                currentStage++;
                JOptionPane.showMessageDialog(
                        this,
                        "Stage " + currentStage + " begins! Get ready for a new challenge!",
                        "Stage Complete!",
                        JOptionPane.INFORMATION_MESSAGE
                );
                startNewStage();
            } else if (currentStage == TOTAL_STAGES && currentLevel <= TOTAL_LEVELS) {
                JOptionPane.showMessageDialog(
                        this,
                        "Congratulations! You have completed all stages. On to the next level...",
                        "Level Complete",
                        JOptionPane.INFORMATION_MESSAGE
                );
                startNewLevel();
            } else {
                new LevelMenu();
            }
        }
    }

    private void startNewStage() {
        updateStageSettings();
        initializeWorldGrid();
        respawnPlayer();
        resetEnemies();
        startEnemyMovement();
        updateBoard();
        stageStartTime = System.currentTimeMillis();
        timeElapsed = 0;
        timerLabel.setText("Time: 0");
        gameTimer.start();
    }

    private void updateStageSettings() {
        switch (currentStage) {
            case 2:
                maxEnemies = 2;
                clueLabel.setText(showClue(currentLevel, currentStage));
                stageLabel.setText("Stage: " + currentStage);
                setEnemySpeedForStage();
                break;
            case 3:
                maxEnemies = 3;
                clueLabel.setText(showClue(currentLevel, currentStage));
                stageLabel.setText("Stage: " + currentStage);
                setEnemySpeedForStage();
                break;
            default:
                maxEnemies = 1;
                clueLabel.setText(showClue(currentLevel, currentStage));
                stageLabel.setText("Stage: " + currentStage);
                setEnemySpeedForStage();
                break;
        }
    }

    private void startNewLevel() {
        if (currentLevel < TOTAL_LEVELS) {
            currentLevel += 1;
            currentStage = 1;
            levelLabel.setText("Level: " + currentLevel);

            if (currentLevel - 1 < levelColors.length) {
                boardPanel.setBackground(levelColors[currentLevel - 1]);
            } else {
                boardPanel.setBackground(new Color(240, 240, 240));
            }

            startNewStage();
        } else {
            this.dispose();
            new LevelMenu();
        }

    }

    private void resetEnemies() {
        enemies.clear();
        initializeEnemies();
    }

    private void winStage() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                for (String correctWord : correctWords[(currentLevel -1 ) * 3 + (currentStage - 1)]) {
                    if (wordGrid[row][col].equals(correctWord)) {
                        wordGrid[row][col] = "";
                    }
                }
            }
        }
        checkVictory();
    }

    private void saveStageTime(int level, int stage, long time, String name) {
        try(FileWriter writer = new FileWriter("hall_of_fame.txt", true)) {
            writer.write(name + "," + time + "," + level + "," + stage + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isNewRecord(long time, int stage) {
        List<Long> topTimes = loadTopTimesForStage(currentLevel, currentStage);
        return topTimes.size() < 3 || time < Collections.max(topTimes);
    }

    private String promptForName() {
        return JOptionPane.showInputDialog(null, "New Record! Enter your name:");
    }

    private void updateHallOfFame(String name, long time, int level, int stage) {
        // Load all existing hall of fame data
        List<String[]> hallOfFameData = loadHallOfFameData();

        // Add the new record if it doesn't already exist
        String[] newRecord = new String[]{name, String.valueOf(time), String.valueOf(level), String.valueOf(stage)};

        // Find and filter the relevant entries for the current level and stage
        List<String[]> stageEntries = new ArrayList<>();
        List<String[]> otherEntries = new ArrayList<>();

        for (String[] entry : hallOfFameData) {
            int entryLevel = Integer.parseInt(entry[2].trim());
            int entryStage = Integer.parseInt(entry[3].trim());

            if (entryLevel == level && entryStage == stage) {
                stageEntries.add(entry);  // Add relevant entries for this level and stage
            } else {
                otherEntries.add(entry);  // Keep other entries intact
            }
        }
        boolean duplicateFound = false;
        for (String[] entry : stageEntries) {
            if (Arrays.equals(entry, newRecord)) {
                duplicateFound = true; // Prevent adding a duplicate record
                break;
            }
        }

        if (!duplicateFound) {
            stageEntries.add(newRecord); // Add only if no duplicate was found
        }

        // Sort and keep top 3 for the current stage/level
        stageEntries = getTopThreePerStage(stageEntries);

        // Combine the other entries and updated stage entries
        List<String[]> updatedHallOfFameData = new ArrayList<>();
        updatedHallOfFameData.addAll(otherEntries);  // Add back all the other entries
        updatedHallOfFameData.addAll(stageEntries);  // Add the updated stage entries

        // Save the updated data back to the file
        saveHallOfFameData(updatedHallOfFameData);
    }

    private List<String[]> loadHallOfFameData() {
        List<String[]> hallOfFameData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("hall_of_fame.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    hallOfFameData.add(parts);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hallOfFameData;
    }



    private List<Long> loadTopTimesForStage(int level, int stage) {
        List<Long> topTimes = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("hall_of_fame.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4 && Integer.parseInt(parts[2]) == level && Integer.parseInt(parts[3]) == stage) {
                    topTimes.add(Long.parseLong(parts[1]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return topTimes;
    }

    private List<String[]> getTopThreePerStage(List<String[]> stageEntries) {
        // Sort entries based on time (second element of each entry)
        stageEntries.sort(Comparator.comparingLong(e -> Long.parseLong(e[1])));

        // Return the top three entries (or fewer if there aren't 3)
        return stageEntries.size() > 3 ? stageEntries.subList(0, 3) : stageEntries;
    }

    private void saveHallOfFameData(List<String[]> data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("hall_of_fame.txt", false))) {
            for (String[] entry : data) {
                writer.println(String.join(",", entry));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void playSound(String soundFilePath) {
        try {
            // Obtain an AudioInputStream from the sound file
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundFilePath));
            AudioFormat baseFormat = audioInputStream.getFormat();

            // Check if the format is supported directly
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,  // Use 16-bit instead of 24-bit
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2, // Frame size
                    baseFormat.getSampleRate(),
                    false // Set to big-endian if needed
            );

            // Convert the stream if needed
            AudioInputStream decodedAudioStream = AudioSystem.getAudioInputStream(decodedFormat, audioInputStream);

            // Open and play the audio
            Clip clip = AudioSystem.getClip();
            clip.open(decodedAudioStream);
            clip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
