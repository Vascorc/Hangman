package pt.ubi.sd.forca.server;

import java.io.*;
import java.net.Socket;
import pt.ubi.sd.forca.shared.Protocol;

/*
 * classe que estende Thread. Cada jogador terá a sua, lê o que o socket recebe e "pergunta" à lógica do jogo o que fazer.
 */

public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameManager manager;
    private int playerId;

    public ClientHandler(Socket socket, GameManager manager) {
        this.socket = socket;
        this.manager = manager;
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Falha ao abrir fluxos I/O do cliente.");
        }
    }

    public void setPlayerId(int id) {
        this.playerId = id;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(30000); // 30 segundos timeout por ronda

            while (true) {
                try {
                    String line = in.readLine();
                    if (line == null)
                        break; // Desconexão limpa
                    if (line.startsWith(Protocol.GUESS)) {
                        String move = line.substring(6).trim(); // Remove "GUESS "
                        manager.handlePlayerMove(playerId, move);
                    }
                } catch (java.net.SocketTimeoutException e) {
                    if (manager.isGameStarted()) {
                        manager.handlePlayerMove(playerId, ""); // Jogada vazia no timeout
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Jogador " + playerId + " desconectou-se.");
        } finally {
            manager.playerDisconnected(this);
            closeConnection();
        }
    }

    public void sendMessage(String msg) {
        if (out != null)
            out.println(msg);
    }

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
        }
    }
}