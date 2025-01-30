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
        // Prompt for the user's name first
        String userName = JOptionPane.showInputDialog(frame, "Enter your name:");
        if (userName == null || userName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        clientName = userName; // Assign the user's name to the clientName variable
        frame = new JFrame(clientName + "'s Chat Box"); // Dynamic window title with user's name

        // Add the header "Simple Chat Application"
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(50, 205, 50)); // Lime Green for header background
        JLabel headerLabel = new JLabel("Simple Chat Application", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);

        // Set chat background
        chatArea = new JEditorPane("text/html", "");
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(230, 230, 250)); // Light lavender background
        chatArea.setOpaque(true);

        chatHistory = new StringBuilder("<html><body style='font-family: Arial, sans-serif; padding: 10px; "
                + "background-color: #E6E6FA;'>"); // Matches chatArea background

        inputField = new JTextField(35);
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        sendButton = new JButton("Send");

        // 💡 Stylish Send Button
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBackground(new Color(50, 205, 50)); // Lime Green
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        // Add hover effect
        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(new Color(34, 139, 34)); // Darker green on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(new Color(50, 205, 50)); // Back to lime green
            }
        });

        // Panel for input field and button
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        frame.setLayout(new BorderLayout());
        frame.add(headerPanel, BorderLayout.NORTH); // Adding the header
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // CONNECT TO SERVER
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Wait for prompt to enter the name (received from the server)
            if (in.readLine().equals("PROMPT_NAME")) {
                out.println(clientName); // Send the name to the server
            }

            // THREAD TO RECEIVE MESSAGES
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        if (message.equals("YOU_ENTERED")) {
                            addCenteredMessage("✅ You entered the chat");
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
        boolean isMe = message.startsWith("You:"); // Check if the message is from you

        String alignment = isMe ? "right" : "left";
        String bgColor = isMe ? "#DCF8C6" : "#F0F0F0"; // Green for you, gray for others
        String textColor = isMe ? "#000000" : "#333333"; // Darker text for contrast

        chatHistory.append("<div style='text-align: ")
                .append(alignment)
                .append("; margin: 5px 0;'><div style='display: inline-block; padding: 8px 12px; border-radius: 10px; background-color: ")
                .append(bgColor)
                .append("; color: ")
                .append(textColor)
                .append("; max-width: 75%; word-wrap: break-word;'>")
                .append(message)
                .append("</div></div>");

        chatArea.setText(chatHistory.toString());
    }

    private void addCenteredMessage(String message) {
        chatHistory.append("<div style='text-align:center; color: gray; font-style: italic;'>")
                .append(message)
                .append("</div>");

        chatArea.setText(chatHistory.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}
