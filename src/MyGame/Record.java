package MyGame;

public class Record {
    private final String name;
    private final String time;
    private final int level;
    private final int stage;

    public Record(String name, String time, int level, int stage) {
        this.name = name;
        this.time = time;
        this.level = level;
        this.stage = stage;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public int getLevel() {
        return level;
    }

    public int getStage() {
        return stage;
    }
}
