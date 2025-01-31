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
        initializeGUI();
        setupNetworking();
    }

    private void initializeGUI() {
        clientName = getClientName();
        if (clientName == null)
            System.exit(1);

        frame = new JFrame(clientName + "'s Chat App");
        frame.setSize(400, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        frame.add(createHeader(), BorderLayout.NORTH);
        frame.add(createChatArea(), BorderLayout.CENTER);
        frame.add(createInputPanel(), BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private String getClientName() {
        String userName = JOptionPane.showInputDialog(null, "Enter your name:", "User Login",
                JOptionPane.PLAIN_MESSAGE);
        if (userName == null || userName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return userName;
    }

    private JPanel createHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(24, 119, 242));
        JLabel headerLabel = new JLabel("Simple Chat Application", JLabel.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        return headerPanel;
    }

    private JScrollPane createChatArea() {
        chatArea = new JEditorPane("text/html", "");
        chatArea.setEditable(false);
        chatArea.setBackground(Color.WHITE);
        chatHistory = new StringBuilder(
                "<html><body style='font-family: Segoe UI, sans-serif; padding: 10px; background-color: #F0F0F0;'></body></html>");
        return new JScrollPane(chatArea);
    }

    private JPanel createInputPanel() {
        inputField = new JTextField(25);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        sendButton = new JButton("Send");
        styleSendButton();
        sendButton.addActionListener(_ -> sendMessage());
        inputField.addActionListener(_ -> sendMessage());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return panel;
    }

    private void styleSendButton() {
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(new Color(24, 119, 242));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    }

    private void setupNetworking() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            if (in.readLine().equals("PROMPT_NAME"))
                out.println(clientName);

            new Thread(this::listenForMessages).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Failed to connect to server", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void listenForMessages() {
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
        String bgColor = isMe ? "#0084FF" : "#E4E6EB";
        String textColor = isMe ? "white" : "black";
        String align = isMe ? "right" : "left";
        String margin = isMe ? "margin-left: 50%;" : "margin-right: 50%;";

        message = message.replaceFirst("^(You:|\\w+:)", "<b>$1</b>");

        chatHistory.insert(chatHistory.indexOf("</body>"),
                "<div style='text-align: " + align + "; width: 100%; " + margin + " margin-top: 8px;'>"
                        + "<div style='display: inline-block; padding: 6px 9px; border-radius: 20px; background-color: "
                        + bgColor + "; color: " + textColor + "; max-width: 100%; font-size: 12px;'>"
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
                "<div style='text-align:center; color: #444; font-family: Courier New, monospace; font-size: 11px; font-weight: bold; margin-top: 10px;'>"
                        + message + "</div>");
        chatArea.setText(chatHistory.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}
