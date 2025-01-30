import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clients = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket);
                clients.add(client);
                client.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Ask for client's name
                out.println("PROMPT_NAME");
                clientName = in.readLine().trim();
                System.out.println(clientName + " has joined the chat.");

                // Send "You entered the chat" to the client as a centered message
                out.println("YOU_ENTERED");

                // Notify other clients
                broadcast("🔔 " + clientName + " has joined the chat", this);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(clientName + ": " + message);
                    broadcast(clientName + ": " + message, this);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clients.remove(this);
                broadcast("🚪 " + clientName + " has left the chat", null);
            }
        }

        private void broadcast(String message, ClientHandler sender) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    if (client != sender) {
                        client.out.println(message);
                    }
                }
            }
        }
    }
}
