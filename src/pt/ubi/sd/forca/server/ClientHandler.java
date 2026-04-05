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
    }

    public void setPlayerId(int id) { this.playerId = id; }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith(Protocol.GUESS)) {
                    String move = line.substring(6).trim(); // Remove "GUESS "
                    manager.handlePlayerMove(playerId, move);
                }
            }
        } catch (IOException e) {
            System.out.println("Jogador " + playerId + " desconectou-se.");
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(String msg) {
        if (out != null) out.println(msg);
    }

    public void closeConnection() {
        try { socket.close(); } catch (IOException e) {}
    }
}