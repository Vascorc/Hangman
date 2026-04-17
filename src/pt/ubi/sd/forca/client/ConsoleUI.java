package pt.ubi.sd.forca.client;

import java.util.Scanner;

/**
 * Responsável por TODA a interação visual com o utilizador:
 * - Desenhar o boneco da forca
 * - Mostrar a máscara da palavra e letras usadas
 * - Ler a jogada do teclado
 * - Mostrar mensagens de início, vitória, derrota, etc.
 *
 * Nenhum outro ficheiro deve ter System.out diretamente — centraliza aqui.
 */
public class ConsoleUI {

    private static volatile boolean inLobby = false;
    private static Thread lobbyTimerThread;
    private static volatile int lobbyTimeLeft = 0;
    private static volatile int lobbyPlayers = 0;

    public static void stopLobbyTimer() {
        inLobby = false;
        if (lobbyTimerThread != null) {
            lobbyTimerThread.interrupt();
        }
    }

    private static volatile boolean roundTimerActive = false;
    private static Thread roundTimerThread;

    public static void stopRoundTimer() {
        roundTimerActive = false;
        if (roundTimerThread != null) {
            roundTimerThread.interrupt();
            roundTimerThread = null;
        }
    }

    public static void startRoundCountdown() {
        stopRoundTimer();
        roundTimerActive = true;
        roundTimerThread = new Thread(() -> {
            int time = 30; // 30 segundos
            while (roundTimerActive && time > 0) {
                try {
                    Thread.sleep(1000);
                    time--;
                    if (roundTimerActive) {
                        String timeStr = String.format("%02d", time);
                        System.out.print("\033[s"); // guarda posição original do cursor
                        System.out.print("\033[" + linesToMoveUp + "A"); // sobe N linhas até a linha RONDA
                        System.out.print("\rRONDA " + currentRound + "   |   Tempo: " + timeStr + "s \033[K");
                        System.out.print("\033[u"); // restaura o cursor
                        System.out.flush();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        roundTimerThread.setDaemon(true);
        roundTimerThread.start();
    }

    public static void clearConsole() {
        stopRoundTimer();
        System.out.print("\033[H\033[2J\033[3J");
        System.out.flush();
    }

    // Menu de entrada
    public static int showMainMenu(Scanner scanner) {
        String option = "";
        clearConsole();

        while (true) {
            System.out.println("\n" + "═".repeat(45));
            System.out.println("       JOGO DA FORCA MULTIJOGADOR       ");
            System.out.println("═".repeat(45));
            System.out.println("  1. Ligar ao Servidor e Jogar");
            System.out.println("  2. Ver Regras do Jogo");
            System.out.println("  0. Sair");
            System.out.println("═".repeat(45));
            System.out.print("  Escolhe uma opção: ");

            option = scanner.nextLine().trim();

            switch (option) {
                case "1":
                    return 1; // Sai do menu e avança para o jogo
                case "2":
                    showRules(scanner); // Mostra as regras e volta a desenhar o menu
                    break;
                case "0":
                    System.out.println("A sair... Obrigado por jogares!");
                    return 0; // Código para terminar o programa
                default:
                    System.out.println("Opção inválida! Tenta novamente.");
            }
        }
    }

    private static void showRules(Scanner scanner) {
        System.out.println("\n--- REGRAS ---");
        System.out.println("1. O jogo suporta até 4 jogadores.");
        System.out.println("2. Têm 6 tentativas em conjunto para adivinhar a palavra.");
        System.out.println("3. Podes tentar uma letra ou a palavra inteira.");
        System.out.println("4. Demorar muito tempo na tua vez gasta uma tentativa!");
        System.out.println("Pressiona [ENTER] para voltar ao menu...");
        scanner.nextLine();
    }

    // Largura da linha de separação para deixar o output mais legível
    private static final String SEPARATOR = "═".repeat(45);

    // ─── Forca ASCII ───────────────────────────────────────────

    private static int maxAttempts = 6; // inicial

    /**
     * Desenha o boneco da forca com base no número de tentativas RESTANTES e o valor máximo.
     */
    public static void drawHangman(int attemptsLeft) {
        // O jogo tem 6 vidas (tentativas) no total, partilhadas por todos.
        int erroCount = maxAttempts - attemptsLeft;
        if (erroCount < 0) erroCount = 0;

        String head = erroCount >= 1 ? "O" : " ";
        String body = erroCount >= 2 ? "|" : " ";
        String rArm = erroCount >= 3 ? "\\" : " "; // Braço direito (visualmente na direita)
        String lArm = erroCount >= 4 ? "/" : " ";  // Braço esquerdo (visualmente na esquerda)
        String lLeg = erroCount >= 5 ? "/" : " ";  // Perna esquerda
        String rLeg = erroCount >= 6 ? "\\" : " "; // Perna direita

        System.out.println("  ┌───┐");
        System.out.println("  │   " + head);
        System.out.println("  │  " + lArm + body + rArm);
        System.out.println("  │  " + lLeg + " " + rLeg);
        System.out.println("  │");
        System.out.println("──┴──");
    }

    // ─── Mensagens do protocolo ────────────────────────────────

    /** Mostrado ao receber WELCOME */
    public static void showWelcome(int playerId, int totalPlayers) {
        clearConsole();
        System.out.println("\n" + SEPARATOR);
        System.out.println("  🎮  BEM-VINDO AO JOGO DA FORCA (UBI)");
        System.out.println(SEPARATOR);
        System.out.println("  O teu ID: Jogador " + playerId);
        System.out.println("  Jogadores no lobby: " + totalPlayers);
        System.out.println("  A aguardar o início do jogo...");
        System.out.println("  Tempo para início de jogo: 20s");
        System.out.println(SEPARATOR);

        inLobby = true;
        lobbyPlayers = totalPlayers;
        lobbyTimeLeft = 20;
        startLobbyCountdown();
    }

    public static void updateLobby(int totalPlayers, int timeLeft) {
        if (!inLobby) return;
        lobbyPlayers = totalPlayers;
        lobbyTimeLeft = timeLeft;
        startLobbyCountdown();
    }

    private static void startLobbyCountdown() {
        if (lobbyTimerThread != null) {
            lobbyTimerThread.interrupt();
        }

        lobbyTimerThread = new Thread(() -> {
            while (inLobby && lobbyTimeLeft > 0) {
                try {
                    Thread.sleep(1000);
                    lobbyTimeLeft--;
                    if (inLobby) {
                        String timeStr = String.format("%02d", lobbyTimeLeft);
                        System.out.print("\033[s"); // guarda posição original do cursor
                        
                        // Sobe 4 linhas para "Jogadores no lobby"
                        System.out.print("\033[4A"); 
                        System.out.print("\r  Jogadores no lobby: " + lobbyPlayers + " \033[K");
                        
                        // Desce 1 linha para "A aguardar o início..."
                        System.out.print("\033[1B"); 
                        System.out.print("\r  A aguardar o início do jogo... \033[K");
                        
                        // Desce 1 linha para "Tempo para início..."
                        System.out.print("\033[1B"); 
                        System.out.print("\r  Tempo para início de jogo: " + timeStr + "s \033[K");
                        
                        System.out.print("\033[u"); // restaura o cursor para baixo
                        System.out.flush();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        lobbyTimerThread.setDaemon(true);
        lobbyTimerThread.start();
    }

    /** Mostrado ao receber START */
    public static void showStart(String mask, int attempts, int timeoutMs) {
        stopLobbyTimer();
        clearConsole();
        maxAttempts = attempts; // Atualiza o limite máximo real baseado nos npcs/players
        System.out.println("\n" + SEPARATOR);
        System.out.println("JOGO INICIADO!");
        System.out.println(SEPARATOR);
        System.out.println("  Palavra: " + formatMask(mask));
        System.out.println("  Tentativas: " + attempts);
        System.out.println("  Tempo por ronda: " + (timeoutMs / 1000) + " segundos");
        drawHangman(attempts);
        System.out.println(SEPARATOR);
    }

    private static int currentRound = 1;
    private static int linesToMoveUp = 11;

    /** Mostrado ao receber ROUND <k> */
    public static void showRound(int round, String mask, int attempts, String usedLetters) {
        currentRound = round;
        clearConsole();
        
        System.out.println("\n" + SEPARATOR);
        System.out.println(" RONDA " + round + "   |   Tempo: 30s");
        System.out.println(SEPARATOR);
        drawHangman(attempts);
        System.out.println("  Palavra: " + formatMask(mask));
        System.out.println("  Tentativas restantes: " + attempts);
        
        linesToMoveUp = 11;
        if (!usedLetters.isBlank()) {
            System.out.println("  Letras usadas: " + usedLetters);
            linesToMoveUp = 12;
        }
        System.out.println(SEPARATOR);
        System.out.print("  A tua jogada (letra ou palavra completa): ");
        startRoundCountdown();
    }

    /** Mostrado ao receber STATE (atualização intermédia) */
    public static void showState(String mask, int attempts, String usedLetters) {
        System.out.println("\n  [ESTADO] Palavra: " + formatMask(mask)
                + "  |  Tentativas: " + attempts
                + (usedLetters.isBlank() ? "" : "  |  Usadas: " + usedLetters));
    }

    /** Mostrado ao receber END WIN */
    public static void showWin(String winnerIds, String word, int myId) {
        clearConsole();
        System.out.println("\n" + SEPARATOR);

        // Verifica se o jogador local está entre os vencedores
        boolean iWon = false;
        for (String id : winnerIds.split(",")) {
            if (id.trim().equals(String.valueOf(myId))) {
                iWon = true;
                break;
            }
        }

        if (iWon) {
            System.out.println(" PARABÉNS! GANHASTE!");
            System.out.println("  Vencedor(es): Jogador(es) " + winnerIds);
        } else {
            System.out.println(" FIM DO JOGO");
            System.out.println("  Vencedor(es): Jogador(es) " + winnerIds);

        }

        System.out.println("  A palavra era: " + word);
        System.out.println(SEPARATOR);
    }

    /** Mostrado ao receber END LOSE */
    public static void showLose(String word) {
        clearConsole();
        System.out.println("\n" + SEPARATOR);
        System.out.println("PERDERAM! Não há mais tentativas.");
        System.out.println("A palavra era: " + word);
        drawHangman(0); // Boneco completo
        System.out.println(SEPARATOR);
    }

    /** Mostrado ao receber FULL */
    public static void showFull() {
        stopLobbyTimer();
        clearConsole();
        System.out.println("\n" + SEPARATOR);
        System.out.println("Servidor cheio! Tenta mais tarde.");
        System.out.println(SEPARATOR);
    }

    /** Mostrado ao receber PLAYER_LEFT (jogador desconectou-se a meio do jogo) */
    public static void showPlayerLeft(int playerId) {
        clearConsole();
        System.out.println("\n" + SEPARATOR);
        System.out.println("JOGO TERMINADO — Jogador " + playerId + " desconectou-se.");
        System.out.println(SEPARATOR);
    }

    /** Mostrado ao receber CANCELLED (lobby cancelado por falta de jogadores) */
    public static void showCancelled() {
        stopLobbyTimer();
        clearConsole();
        System.out.println("\n" + SEPARATOR);
        System.out.println("LOBBY CANCELADO — Jogadores insuficientes.");
        System.out.println("O jogo precisa de pelo menos 2 jogadores.");
        System.out.println(SEPARATOR);
    }

    // ─── Leitura de input ──────────────────────────────────────

    public static String readGuess(Scanner scanner) {
        String result = null;
        if (scanner.hasNextLine()) {
            result = scanner.nextLine().trim();
        }
        
        // Assim que o utilizador carrega no Enter, o terminal cria uma nova linha.
        // Aumentamos o tracking de linhas para que o temporizador atualize no sítio certo.
        linesToMoveUp++;
        System.out.println("  [Jogada submetida! A aguardar pelos outros jogadores...]");
        linesToMoveUp++; // porque o println acabou de inserir uma nova linha

        return result;
    }

    // ─── Utilitários ───────────────────────────────────────────

    /**
     * Formata a máscara substituindo '_' por '_ ' para ficar mais legível.
     * Ex: "S_S__M_S" → "S _ S _ _ M _ S"
     */
    private static String formatMask(String mask) {
        StringBuilder sb = new StringBuilder();
        for (char c : mask.toCharArray()) {
            sb.append(c).append(' ');
        }
        return sb.toString().trim();
    }
}