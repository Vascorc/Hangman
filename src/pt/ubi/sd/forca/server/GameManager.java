package pt.ubi.sd.forca.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import pt.ubi.sd.forca.shared.Protocol;

/**
 * O GameManager gere a sessão de jogo: lobby, sincronização de rondas
 * e comunicação entre os jogadores e a lógica (GameEngine).
 */

public class GameManager {
    private final List<ClientHandler> players = new ArrayList<>();
    private final GameEngine engine;
    private boolean gameStarted = false;
    private Timer lobbyTimer;
    private int roundNumber = 1;
    
    // Usamos um HashMap normal, vamos proteger o acesso com synchronized(this)
    private final Map<Integer, String> currentRoundMoves = new HashMap<>();

    public GameManager() {
        this.engine = new GameEngine();
    }

    public synchronized boolean isGameStarted() {
        return gameStarted;
    }

    public synchronized void playerDisconnected(ClientHandler player) {
        players.remove(player);
        System.out.println("Removida referência da Player pool.");
        if (gameStarted) {
            currentRoundMoves.remove(player.getPlayerId());
            if (players.isEmpty()) {
                stopGame();
            } else {
                checkRoundCompletion();
            }
        }
    }

    // Adiciona um jogador e verifica se o jogo pode começar
    public void addPlayer(ClientHandler player) {
        synchronized(this) {
            if (players.size() >= 4 || gameStarted) {
                player.sendMessage(Protocol.FULL);
                player.closeConnection();
                return;
            }

            players.add(player);
            int id = players.size() - 1;
            player.setPlayerId(id);
            
            player.sendMessage(Protocol.WELCOME + " " + id + " " + players.size());
            checkLobbyStatus();
        }
    }

    private void checkLobbyStatus() {
        if (players.size() == 1) {
            startLobbyTimer();
        } else if (players.size() == 4) {
            startGame();
        }
    }

    private void startLobbyTimer() {
        lobbyTimer = new Timer();
        lobbyTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (GameManager.this) {
                    if (!gameStarted && players.size() >= 2) {
                        startGame();
                    } else if (!gameStarted) {
                        System.out.println("Lobby cancelado: jogadores insuficientes.");
                        for (ClientHandler p : players) {
                            p.sendMessage(Protocol.END_LOSE + " LOBBY CANCELADO");
                            p.closeConnection();
                        }
                        players.clear();
                    }
                }
            }
        }, 20000); 
    }

    private void startGame() {
        if (lobbyTimer != null) lobbyTimer.cancel();
        gameStarted = true;
        roundNumber = 1;
        
        // Mensagem inicial de acordo com o protocolo
        broadcast(Protocol.START + " " + engine.getMask() + " " + engine.getAttempts() + " 30000");
        nextRound();
    }

    // Inicia uma nova ronda enviando o estado atual a todos
    public void nextRound() {
        synchronized(this) {
            currentRoundMoves.clear(); // Limpa as jogadas da ronda anterior
            String state = Protocol.ROUND + " " + roundNumber + " " + engine.getMask() + " " + engine.getAttempts() + " " + engine.getUsedLetters();
            roundNumber++;
            broadcast(state);
        }
    }

    // Recebe a jogada de um jogador e verifica se todos já jogaram
    public void handlePlayerMove(int playerId, String move) {
        synchronized(this) {
            if (!gameStarted) return;

            // Aceita apenas a primeira jogada de cada utilizador na ronda
            if (!currentRoundMoves.containsKey(playerId)) {
                currentRoundMoves.put(playerId, move);
                System.out.println("Jogador " + playerId + " enviou: '" + move + "'");
                checkRoundCompletion();
            }
        }
    }

    private void checkRoundCompletion() {
        if (gameStarted && !players.isEmpty() && currentRoundMoves.size() == players.size()) {
            processEndOfRound();
        }
    }

    private void processEndOfRound() {
        List<String> winners = new ArrayList<>();
        
        // Identificar os IDs vencedores (adivinhou a palavra ou completou letra que faltava)
        for (Map.Entry<Integer, String> entry : currentRoundMoves.entrySet()) {
            int pId = entry.getKey();
            String m = entry.getValue().toUpperCase();
            
            if (m.equals(engine.getTargetWord())) {
                winners.add(String.valueOf(pId));
            } else if (m.length() == 1) {
                char c = m.charAt(0);
                if (engine.getTargetWord().indexOf(c) >= 0 && !engine.getUsedLetters().contains(String.valueOf(c))) {
                    winners.add(String.valueOf(pId));
                }
            }
        }

        // Processar todas as jogadas na Engine
        for (String move : currentRoundMoves.values()) {
            engine.processGuess(move);
        }

        // Verificar vitória ou derrota
        if (engine.isWin()) {
            if (winners.isEmpty()) winners.add("Nenhum"); // Segurança
            broadcast(Protocol.END_WIN + " " + String.join(",", winners) + " " + engine.getTargetWord());
            stopGame();
        } else if (engine.getAttempts() <= 0) {
            broadcast(Protocol.END_LOSE + " " + engine.getTargetWord());
            stopGame();
        } else {
            // Continua para a próxima ronda
            nextRound();
        }
    }

    private void stopGame() {
        gameStarted = false;
        for (ClientHandler p : players) {
            p.closeConnection();
        }
        players.clear();
    }

    private void broadcast(String message) {
        for (ClientHandler p : players) {
            p.sendMessage(message);
        }
    }
}