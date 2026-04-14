package pt.ubi.sd.forca.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import pt.ubi.sd.forca.shared.Config;

/**
 * Ponto de entrada do cliente.
 * Responsável por pedir o IP ao utilizador, estabelecer a ligação TCP ao
 * servidor
 * e lançar a ServerListener (thread de escuta) e o loop de input do utilizador.
 */
public class ClientMain {

    // Objetos de comunicação partilhados com a ServerListener
    private static PrintWriter out;
    private static volatile boolean gameRunning = true;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Mostra o menu e guarda a escolha
        int choice = ConsoleUI.showMainMenu(scanner);

        if (choice == 0) {
            scanner.close();
            System.exit(0); // Termina o programa se o utilizador escolher sair
        }

        // Se escolheu 1, o código continua normalmente para pedir o IP
        System.out.println("  💡 Caso queira jogar localmente apenas aperte ENTER");
        System.out.print("  Endereço do servidor: ");
        String input = scanner.nextLine().trim();
        String serverIP = input.isEmpty() ? Config.SERVER_IP : input;

        System.out.println("A ligar a " + serverIP + ":" + Config.PORT + "...");

        try (Socket socket = new Socket(serverIP, Config.PORT)) {
            System.out.println("Ligação estabelecida!");

            // Preparar os streams de comunicação
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Lançar a thread que ouve o servidor em background
            ServerListener listener = new ServerListener(in, ClientMain::onGameEnd);
            listener.start();

            // Loop principal: lê input do utilizador e envia para o servidor
            while (gameRunning) {
                if (listener.isMyTurn()) {
                    String guess = ConsoleUI.readGuess(scanner);
                    if (guess != null && !guess.isBlank()) {
                        out.println("GUESS " + guess.trim());
                        listener.setMyTurn(false); // Aguarda próxima ronda
                    }
                } else {
                    // Pequena espera para não consumir CPU em busy-wait
                    Thread.sleep(100);
                }
            }

        } catch (IOException e) {
            System.err.println("Erro de ligação: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            scanner.close();
        }
    }

    /** Chamado pela ServerListener quando o jogo termina */
    public static void onGameEnd() {
        gameRunning = false;
    }
}