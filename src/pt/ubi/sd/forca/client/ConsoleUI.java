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

    // Menu de entrada
    public static int showMainMenu(Scanner scanner) {
        String option = "";

        while (true) {
            System.out.println("\n" + "═".repeat(45));
            System.out.println("       🎮 JOGO DA FORCA MULTIJOGADOR 🎮       ");
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
                    System.out.println("  A sair... Obrigado por jogares!");
                    return 0; // Código para terminar o programa
                default:
                    System.out.println("  ⚠️ Opção inválida! Tenta novamente.");
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

    /**
     * Desenha o boneco da forca com base no número de tentativas RESTANTES.
     * O jogo começa com 6 tentativas, então:
     * 6 tentativas → boneco vazio
     * 0 tentativas → boneco completo (perdeu)
     */
    public static void drawHangman(int attemptsLeft) {
        // Calculamos quantas partes do boneco já foram "penduradas"
        int errors = 6 - attemptsLeft;

        String head = errors >= 1 ? "O" : " ";
        String body = errors >= 2 ? "|" : " ";
        String lArm = errors >= 3 ? "/" : " ";
        String rArm = errors >= 4 ? "\\" : " ";
        String lLeg = errors >= 5 ? "/" : " ";
        String rLeg = errors >= 6 ? "\\" : " ";

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
        System.out.println("\n" + SEPARATOR);
        System.out.println("  🎮  BEM-VINDO AO JOGO DA FORCA (UBI)");
        System.out.println(SEPARATOR);
        System.out.println("  O teu ID: Jogador " + playerId);
        System.out.println("  Jogadores no lobby: " + totalPlayers);
        System.out.println("  A aguardar o início do jogo...");
        System.out.println(SEPARATOR);
    }

    /** Mostrado ao receber START */
    public static void showStart(String mask, int attempts, int timeoutMs) {
        System.out.println("\n" + SEPARATOR);
        System.out.println("  🚀  JOGO INICIADO!");
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
        
        System.out.println("\n" + SEPARATOR);
        System.out.println("  📍  RONDA " + round + "   |   ⏳ Tempo: 30s");
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
    }

    /** Mostrado ao receber STATE (atualização intermédia) */
    public static void showState(String mask, int attempts, String usedLetters) {
        System.out.println("\n  [ESTADO] Palavra: " + formatMask(mask)
                + "  |  Tentativas: " + attempts
                + (usedLetters.isBlank() ? "" : "  |  Usadas: " + usedLetters));
    }

    /** Mostrado ao receber END WIN */
    public static void showWin(String winnerIds, String word, int myId) {
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
            System.out.println("  🏆  PARABÉNS! GANHASTE!");
        } else {
            System.out.println("  🎉  FIM DO JOGO — Alguém ganhou!");
            System.out.println("  Vencedor(es): Jogador(es) " + winnerIds);
        }

        System.out.println("  A palavra era: " + word);
        System.out.println(SEPARATOR);
    }

    /** Mostrado ao receber END LOSE */
    public static void showLose(String word) {
        System.out.println("\n" + SEPARATOR);
        System.out.println("  💀  PERDERAM! Não há mais tentativas.");
        System.out.println("  A palavra era: " + word);
        drawHangman(0); // Boneco completo
        System.out.println(SEPARATOR);
    }

    /** Mostrado ao receber FULL */
    public static void showFull() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("  ⛔  Servidor cheio! Tenta mais tarde.");
        System.out.println(SEPARATOR);
    }

    /** Mostrado ao receber PLAYER_LEFT (jogador desconectou-se a meio do jogo) */
    public static void showPlayerLeft(int playerId) {
        System.out.println("\n" + SEPARATOR);
        System.out.println("  ❌  JOGO TERMINADO — Jogador " + playerId + " desconectou-se.");
        System.out.println(SEPARATOR);
    }

    /** Mostrado ao receber CANCELLED (lobby cancelado por falta de jogadores) */
    public static void showCancelled() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("  ⏱  LOBBY CANCELADO — Jogadores insuficientes.");
        System.out.println("  O jogo precisa de pelo menos 2 jogadores.");
        System.out.println(SEPARATOR);
    }

    // ─── Leitura de input ──────────────────────────────────────

    private static volatile boolean waitingForInput = false;

    public static String readGuess(Scanner scanner) {
        waitingForInput = true;
        
        Thread timer = new Thread(() -> {
            int time = 30; // 30 segundos
            while (waitingForInput && time > 0) {
                try {
                    Thread.sleep(1000);
                    time--;
                    if (waitingForInput) {
                        String timeStr = String.format("%02d", time);
                        // Guarda o cursor, sobe N linhas, reescreve a linha inteira da Ronda, desce/restaura o cursor
                        System.out.print("\033[s"); // save cursor
                        System.out.print("\033[" + linesToMoveUp + "A"); // move cursor up
                        System.out.print("\r  📍  RONDA " + currentRound + "   |   ⏳ Tempo: " + timeStr + "s \033[K");
                        System.out.print("\033[u"); // restore cursor
                        System.out.flush();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        timer.setDaemon(true);
        timer.start();

        String result = null;
        if (scanner.hasNextLine()) {
            result = scanner.nextLine().trim();
        }
        
        waitingForInput = false;
        timer.interrupt();
        
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