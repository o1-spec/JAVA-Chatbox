import javax.swing.SwingUtilities;

public class ChatClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatClientGUI();
        });
    }
}
