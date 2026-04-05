package pt.ubi.sd.forca.server;
import java.util.HashSet;
import java.util.Set;

// esta classe contém a lista de 100 palavras e as regras do jogo da forca.

public class GameEngine {
    private String targetWord;
    private Set<Character> usedLetters;
    private int attempts;
    private String[] wordBank = {"SISTEMAS", "DISTRIBUIDOS", "JAVA", "SOCKET", "UBI", "COVILHA"}; // Mete aqui as tuas 100 palavras

    public GameEngine() {
        // Escolha aleatória da palavra
        this.targetWord = wordBank[(int) (Math.random() * wordBank.length)].toUpperCase();
        this.usedLetters = new HashSet<>();
        this.attempts = 6; // Conforme o enunciado
    }

    public String getMask() {
        StringBuilder mask = new StringBuilder();
        for (char c : targetWord.toCharArray()) {
            if (usedLetters.contains(c)) mask.append(c);
            else mask.append("_");
        }
        return mask.toString();
    }

    public boolean processGuess(String guess) {
        guess = guess.toUpperCase();
        if (guess.length() == 1) {
            char letter = guess.charAt(0);
            usedLetters.add(letter);
            if (targetWord.indexOf(letter) >= 0) return true;
        } else if (guess.equals(targetWord)) {
            // Se acertar a palavra toda, preenchemos as letras usadas para a máscara ficar completa
            for (char c : targetWord.toCharArray()) usedLetters.add(c);
            return true;
        }
        attempts--;
        return false;
    }

    public int getAttempts() { return attempts; }
    public String getUsedLetters() { return usedLetters.toString().replaceAll("[\\[\\], ]", ""); }
    public String getTargetWord() { return targetWord; }
    public boolean isWin() { return getMask().equals(targetWord); }
}