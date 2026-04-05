package pt.ubi.sd.forca.server;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import pt.ubi.sd.forca.shared.Config;

/*
 * Classe "mãe" do servidor
 */

public class ServerMain {

    public static void main(String[] args) {
    	try {
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("Srv IP: " + localhost.getHostAddress()); 
        } catch (UnknownHostException e) {
            System.out.println("Não foi possível determinar o IP local.");
        }
    	
        // Criar o gestor do jogo que vai controlar a lógica e os jogadores
        GameManager manager = new GameManager();
        
        System.out.println("=== SERVIDOR JOGO DA FORCA ===");
        
        // Abrir o ServerSocket no porto definido na Config
        try (ServerSocket serverSocket = new ServerSocket(Config.PORT)) {
            System.out.println("Servidor iniciado no porto: " + Config.PORT);
            System.out.println("A aguardar ligações de jogadores...");

            // Loop infinito para aceitar múltiplos jogadores
            while (true) {
                // O programa "para" aqui até que um cliente se ligue
                Socket clientSocket = serverSocket.accept();
                System.out.println("Novo cliente detetado! IP: " + clientSocket.getInetAddress());

                // Criar uma Thread (ClientHandler) para este jogador específico
                ClientHandler handler = new ClientHandler(clientSocket, manager);
                
                // Passar o controlo para o Manager (que decide se o aceita ou se está FULL)
                manager.addPlayer(handler);
                
                // Iniciar a thread para este jogador começar a "ouvir" as jogadas dele
                handler.start();
            }
            
        } catch (IOException e) {
            System.err.println("Erro crítico no servidor: " + e.getMessage());
        }
    }
}