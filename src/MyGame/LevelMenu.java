package MyGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LevelMenu extends JFrame {

    private JButton startButton;
    private JTextPane hallOfFameArea;

    public LevelMenu() {
        setTitle("Start Level");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));

        startButton = new JButton("Start Level 1");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 48));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startLevel1();
            }
        });

        mainPanel.add(startButton, BorderLayout.NORTH);

        // Using JTextPane for HTML content
        hallOfFameArea = new JTextPane();
        hallOfFameArea.setContentType("text/html");
        hallOfFameArea.setEditable(false);

        loadHallOfFame(); // Load hall of fame data from the file

        JScrollPane scrollPane = new JScrollPane(hallOfFameArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private void loadHallOfFame() {
        StringBuilder hallOfFameContent = new StringBuilder();
        hallOfFameContent.append("<html><body>");
        hallOfFameContent.append("<table style='width:100%;'><tr>");
        hallOfFameContent.append("<td style='vertical-align:top;width:50%;'>");

        List<Record> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("hall_of_fame.txt"))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String name = parts[0].trim(); // name
                    String time = parts[1].trim();  // time in seconds
                    int level = Integer.parseInt(parts[2].trim().replaceAll("Level ", "")); // Parse level as integer
                    int stage = Integer.parseInt(parts[3].trim().replaceAll("Stage ", "")); // Parse stage as integer

                    // Add the record to the list
                    records.add(new Record(name, time, level, stage));
                }
            }

            records.sort(Comparator.comparingInt(Record::getLevel)
                    .thenComparingInt(Record::getStage)
                    .thenComparing(Comparator.comparing(Record::getTime).reversed()));

            int currentLevel = -1;
            int currentStage = -1;
            List<String[]> topTimes = new ArrayList<>(); // List for each level-stage group

            boolean switchedToRightColumn = false; // To track when we switch to the right column

            for (Record record : records) {
                // Start a new group if the level or stage has changed
                if (record.getLevel() != currentLevel || record.getStage() != currentStage) {
                    // Append previous group's times if present
                    if (!topTimes.isEmpty()) {
                        appendTopTimes(hallOfFameContent, topTimes, currentLevel, currentStage);
                    }

                    // Switch to the right column after levels 1 and 2
                    if (!switchedToRightColumn && record.getLevel() == 3) {
                        hallOfFameContent.append("</td><td style='vertical-align:top;width:50%;'>"); // Move to the right column (aligned to top)
                        switchedToRightColumn = true;
                    }

                    // Start a new level-stage group
                    currentLevel = record.getLevel();
                    currentStage = record.getStage();
                    topTimes.clear(); // Clear list for the new level and stage
                }

                // Add the current record to the topTimes list
                topTimes.add(new String[]{record.getName(), record.getTime()});
            }

            // Append the last set of top times (if any)
            if (!topTimes.isEmpty()) {
                appendTopTimes(hallOfFameContent, topTimes, currentLevel, currentStage);
            }

        } catch (IOException e) {
            hallOfFameContent.append("<p>No Hall of Fame records available.</p>");
            e.printStackTrace();
        }

        hallOfFameContent.append("</td></tr></table>"); // Close the table
        hallOfFameContent.append("</body></html>");
        hallOfFameArea.setText(hallOfFameContent.toString());
    }




    // Helper method to append the top times for each level and stage to the hall of fame content
    private void appendTopTimes(StringBuilder content, List<String[]> topTimes, int level, int stage) {
        content.append("<h2 style='font-size:14pt;'><b>Level ").append(level).append(", Stage ").append(stage).append("</b></h2>");
        content.append("<ol>");
        for (String[] record : topTimes) {
            String name = record[0];
            String time = record[1];
            content.append("<li>").append(name).append(" - ").append(time).append("</li>"); // Display name and time
        }
        content.append("</ol>");
        content.append("<hr>"); // Add a separator between stages
    }

    private void startLevel1() {
        System.out.println("Starting Level 1");
        this.dispose();
        new Game(); // Assuming you have a MyGame.Game class that starts the game
    }
}