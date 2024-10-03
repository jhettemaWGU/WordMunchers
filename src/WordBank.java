import java.util.*;

public class WordBank {
    private List<String> words;

    public WordBank(List<String> predefinedWords) {
        this.words = new ArrayList<>(predefinedWords);
    }

    public List<String> getWords() {
        return words;
    }
}
