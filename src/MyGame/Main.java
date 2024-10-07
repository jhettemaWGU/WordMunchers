package MyGame;

public class Main {
    private static SoundPlayer soundPlayer;

    public static void main(String[] args) {
        new StartScreen();
        soundPlayer = new SoundPlayer();
        soundPlayer.playMusic("src/Resources/yn-jay-x-flint-type-keys-double-spott_200bpm_C_minor.wav");
    }
}
