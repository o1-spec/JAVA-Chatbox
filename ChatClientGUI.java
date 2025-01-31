import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ChatClientGUI {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String clientName;

    private JFrame frame;
    private JEditorPane chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private StringBuilder chatHistory;

    public ChatClientGUI() {
        String userName = JOptionPane.showInputDialog(null, "Enter your name:", "User Login",
                JOptionPane.PLAIN_MESSAGE);
        if (userName == null || userName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        clientName = userName;
        frame = new JFrame(clientName + "'s Chat");
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(30, 30, 30));
        JLabel headerLabel = new JLabel("Simple Chat Application", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        chatArea = new JEditorPane("text/html", "");
        chatArea.setEditable(false);
        chatArea.setOpaque(true);
        chatArea.setBackground(new Color(40, 40, 40));

        chatHistory = new StringBuilder(
                "<html><body style='font-family: Segoe UI, sans-serif; color: white; padding: 10px; background-color: #282828;'></body></html>");

        inputField = new JTextField(35);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(new Color(50, 100, 150));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(new Color(70, 130, 180));
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(50, 50, 50));
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        frame.setLayout(new BorderLayout());
        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);
        frame.setVisible(true);

        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            if (in.readLine().equals("PROMPT_NAME")) {
                out.println(clientName);
            }

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.equals("YOU_ENTERED")) {
                            addCenteredMessage("✅ You entered the chat");
                        } else if (message.startsWith("🔔")) {
                            addJoinMessage(message);
                        } else {
                            addMessage(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Failed to connect to server", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        sendButton.addActionListener(_ -> sendMessage());
        inputField.addActionListener(_ -> sendMessage());
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            addMessage("You: " + message);
            inputField.setText("");
        }
    }

    private void addMessage(String message) {
        boolean isMe = message.startsWith("You:");
        String bgColor = isMe ? "#007BFF" : "#0056b3"; // Light blue for sender, dark blue for receiver
        String textColor = "white";

        // Make "You:" or the other person's name bold
        message = message.replaceFirst("^(You:|\\w+:)", "<b>$1</b>");

        // Determine positioning
        String position = isMe ? "right: 0;" : "left: 0;"; // Sender on right, receiver on left
        String textAlignment = isMe ? "right" : "left"; // Align text inside bubble
        String margin = isMe ? "margin-right: 10px;" : "margin-left: 10px;"; // Add some spacing

        // Add the message with absolute positioning
        chatHistory.insert(chatHistory.indexOf("</body>"),
                "<div style='width: 100%; position: relative; margin: 9px 0;'>"
                        + "<div style='max-width: 50%; padding: 8px 12px; border-radius: 15px; background-color: "
                        + bgColor + ";"
                        + " color: " + textColor + "; font-size: 12px; word-wrap: break-word; position: absolute; "
                        + position
                        + " text-align: " + textAlignment + "; " + margin + "'>"
                        + message + "</div></div>");

        chatArea.setText(chatHistory.toString());
    }

    private void addCenteredMessage(String message) {
        chatHistory.insert(chatHistory.indexOf("</body>"),
                "<div style='text-align:center; color: gray; font-style: italic; font-weight: bold;'>"
                        + message + "</div>");
        chatArea.setText(chatHistory.toString());
    }

    private void addJoinMessage(String message) {
        chatHistory.insert(chatHistory.indexOf("</body>"),
                "<div style='text-align:center; color: #FFD700; font-family: Courier New, monospace; font-size: 12px; font-weight: bold; margin-top: 10px;'>"
                        + message + "</div>");
        chatArea.setText(chatHistory.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}
