package pt.ubi.sd.forca.client;

import java.io.*;

/**
 * Thread que corre em background e ouve continuamente as mensagens enviadas
 * pelo servidor.
 * Trata todas as mensagens do protocolo definido:
 * WELCOME, START, ROUND, STATE, END WIN, END LOSE, FULL, CANCELLED
 *
 * Quando é a vez do jogador, acende a flag myTurn para que o ClientMain
 * passe a aceitar input do teclado.
 */
public class ServerListener extends Thread {

    private final BufferedReader in;
    private final Runnable onGameEnd; // Callback para avisar o ClientMain que o jogo acabou
    private volatile boolean myTurn = false; // Flag que controla quando o jogador pode jogar
    private volatile boolean gameEnded = false; // Garante que onGameEnd é chamado apenas uma vez

    private int myId = -1; // ID atribuído pelo servidor no WELCOME

    public ServerListener(BufferedReader in, Runnable onGameEnd) {
        this.in = in;
        this.onGameEnd = onGameEnd;
        setDaemon(true); // Termina automaticamente quando o main thread acaba
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                handleMessage(line.trim());
            }
        } catch (IOException e) {
            // Ligação fechada pelo servidor — comportamento normal no fim do jogo
            System.out.println("\n[INFO] Ligação ao servidor encerrada.");
        } finally {
            notifyGameEnd();
        }
    }

    /**
     * Interpreta cada mensagem recebida do servidor de acordo com o protocolo.
     * Formato das mensagens:
     * WELCOME <id> <players_total>
     * START <mask> <attempts> <round_timeout_ms>
     * ROUND <k> <mask> <attempts> <used_letters>
     * STATE <mask> <attempts> <used_letters>
     * END WIN <winner_ids> <word>
     * END LOSE <word>
     * FULL
     * CANCELLED
     */
    private void handleMessage(String message) {
        if (message.startsWith("WELCOME")) {
            // Ex: "WELCOME 0 2"
            String[] parts = message.split(" ");
            myId = Integer.parseInt(parts[1]);
            int totalPlayers = Integer.parseInt(parts[2]);
            ConsoleUI.showWelcome(myId, totalPlayers);

        } else if (message.startsWith("LOBBY_UPDATE")) {
            // Ex: "LOBBY_UPDATE 2 20"
            String[] parts = message.split(" ");
            int totalPlayers = Integer.parseInt(parts[1]);
            int timeLeft = Integer.parseInt(parts[2]);
            ConsoleUI.updateLobby(totalPlayers, timeLeft);

        } else if (message.startsWith("START")) {
            // Ex: "START __S____ 6 30000"
            String[] parts = message.split(" ");
            String mask = parts[1];
            int attempts = Integer.parseInt(parts[2]);
            int timeoutMs = Integer.parseInt(parts[3]);
            ConsoleUI.showStart(mask, attempts, timeoutMs);
            myTurn = true; // Primeira ronda começa agora

        } else if (message.startsWith("ROUND")) {
            // Formato: ROUND <k> <mask> <attempts> <used_letters>
            String[] parts = message.split(" ");
            int round = Integer.parseInt(parts[1]);
            String mask = parts[2];
            int attempts = Integer.parseInt(parts[3]);
            String usedLetters = parts.length > 4 ? parts[4] : "";
            ConsoleUI.showRound(round, mask, attempts, usedLetters);
            myTurn = true;

        } else if (message.startsWith("STATE")) {
            // Ex: "STATE __S____ 5 AS" — atualização intermédia
            String[] parts = message.split(" ", 4);
            String mask = parts[1];
            int attempts = Integer.parseInt(parts[2]);
            String usedLetters = parts.length > 3 ? parts[3] : "";
            ConsoleUI.showState(mask, attempts, usedLetters);

        } else if (message.startsWith("END WIN")) {
            // Ex: "END WIN 0,1 SISTEMAS"
            String rest = message.substring("END WIN".length()).trim();
            int lastSpace = rest.lastIndexOf(' ');
            String winners = lastSpace >= 0 ? rest.substring(0, lastSpace) : rest;
            String word = lastSpace >= 0 ? rest.substring(lastSpace + 1) : "?";
            myTurn = false;
            ConsoleUI.showWin(winners, word, myId);
            notifyGameEnd();

        } else if (message.startsWith("END LOSE")) {
            // Ex: "END LOSE SISTEMAS"
            String word = message.substring("END LOSE".length()).trim();
            myTurn = false;
            ConsoleUI.showLose(word);
            notifyGameEnd();

        } else if (message.equals("FULL")) {
            ConsoleUI.showFull();
            notifyGameEnd();

        } else if (message.equals("CANCELLED")) {
            ConsoleUI.showCancelled();
            notifyGameEnd();

        } else if (message.startsWith("PLAYER_LEFT")) {
            String[] parts = message.split(" ");
            int leftId = Integer.parseInt(parts[1]);
            ConsoleUI.showPlayerLeft(leftId);
            notifyGameEnd();

        } else {
            // Mensagem desconhecida — mostra para debug
            System.out.println("[SERVIDOR] " + message);
        }
    }

    /** Garante que onGameEnd é chamado apenas uma vez */
    private void notifyGameEnd() {
        if (!gameEnded) {
            gameEnded = true;
            onGameEnd.run();
        }
    }

    // --- Getters e setters para sincronização com o ClientMain ---

    public boolean isMyTurn() {
        return myTurn;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }
}
