
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

public class MancalaGUI extends JFrame {

    private MancalaNode gameState;
    private MancalaPlayer computerPlayer;
    private JButton[] pits = new JButton[14];
    private JLabel statusLabel;
    private boolean isPlayerTurn;
    private int playerSide = GameNode.MAX; // Human is MAX (Player 1)

    // Settings
    private int fairKalahIndex = -1; // -1 is random

    public MancalaGUI() {
        super("Dansing FairKalah Bot");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLayout(new BorderLayout());

        // Initialize Bot
        computerPlayer = new dansing2MancalaPlayer();

        // --- Setup UI Components ---
        // Top Control Panel
        JPanel controlPanel = new JPanel();
        JLabel boardLabel = new JLabel("FairKalah Board # (0-253, -1 random):");
        JTextField boardInput = new JTextField("-1", 5);
        String[] sides = {"Play as Player 1 (Go First)", "Play as Player 2 (Go Second)"};
        JComboBox<String> sideSelector = new JComboBox<>(sides);
        JButton startButton = new JButton("Start Game");

        controlPanel.add(boardLabel);
        controlPanel.add(boardInput);
        controlPanel.add(sideSelector);
        controlPanel.add(startButton);
        add(controlPanel, BorderLayout.NORTH);

        // Game Board Panel
        JPanel gamePanel = new JPanel(new GridLayout(1, 3));

        // Store 2 (Left)
        pits[13] = createPitButton(13); // Min Score
        gamePanel.add(pits[13]);

        // Center Pits (6x2 Grid)
        JPanel centerPits = new JPanel(new GridLayout(2, 6));
        // Top Row (Player 2 pits: 12, 11, 10, 9, 8, 7)
        for (int i = 12; i >= 7; i--) {
            pits[i] = createPitButton(i);
            centerPits.add(pits[i]);
        }
        // Bottom Row (Player 1 pits: 0, 1, 2, 3, 4, 5)
        for (int i = 0; i < 6; i++) {
            pits[i] = createPitButton(i);
            centerPits.add(pits[i]);
        }
        gamePanel.add(centerPits);

        // Store 1 (Right)
        pits[6] = createPitButton(6); // Max Score
        gamePanel.add(pits[6]);

        add(gamePanel, BorderLayout.CENTER);

        // Status Bar
        statusLabel = new JLabel("Welcome! Choose settings and click Start.");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(statusLabel, BorderLayout.SOUTH);

        // --- Event Listeners ---
        startButton.addActionListener(e -> {
            try {
                fairKalahIndex = Integer.parseInt(boardInput.getText());
                // Create game with specific FairKalah board
                gameState = new dansing2MancalaNode(fairKalahIndex);

                // Determine sides
                int selectedIndex = sideSelector.getSelectedIndex();
                // If user chose "Play as Player 2", human is MIN.
                playerSide = (selectedIndex == 0) ? GameNode.MAX : GameNode.MIN;

                updateBoardUI();

                // If Human is Player 2 (MIN) and Game starts with Player 1 (MAX), Bot moves first
                if (playerSide == GameNode.MIN) {
                    isPlayerTurn = false;
                    statusLabel.setText("Bot is thinking...");
                    executeBotMove();
                } else {
                    isPlayerTurn = true;
                    statusLabel.setText("Your Turn!");
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number for Board Index.");
            }
        });

        setVisible(true);
    }

    private JButton createPitButton(int index) {
        JButton btn = new JButton("4");
        btn.setFont(new Font("Arial", Font.BOLD, 24));
        // Only allow clicking if it's a play pit, not a store
        if (index != 6 && index != 13) {
            btn.addActionListener(e -> handleHumanMove(index));
        } else {
            btn.setEnabled(false);
            btn.setBackground(Color.LIGHT_GRAY);
        }
        return btn;
    }

    private void handleHumanMove(int pitIndex) {
        if (!isPlayerTurn || gameState.gameOver()) {
            return;
        }

        // Check legality (must be own side and not empty)
        ArrayList<Integer> legalMoves = gameState.getLegalMoves();
        if (!legalMoves.contains(pitIndex)) {
            statusLabel.setText("Invalid Move! Pick a non-empty pit on your side.");
            return;
        }

        // Make Move
        gameState.makeMove(pitIndex);
        updateBoardUI();

        // Check if turn logic kept it as human's turn (e.g. landed in store)
        if (gameState.getPlayer() == playerSide) {
            statusLabel.setText("Go again! (Landed in store)");
        } else {
            isPlayerTurn = false;
            statusLabel.setText("Bot is thinking...");
            executeBotMove();
        }
    }

    private void executeBotMove() {
        // Run in background thread so UI doesn't freeze
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                // Give bot 2 seconds to think max
                return computerPlayer.chooseMove(gameState, 2000);
            }

            @Override
            protected void done() {
                try {
                    int move = get();
                    gameState.makeMove(move);
                    updateBoardUI();

                    if (gameState.gameOver()) {
                        double util = gameState.utility();
                        String res = (util > 0) ? "Player 1 Wins!" : (util < 0) ? "Player 2 Wins!" : "Draw!";
                        statusLabel.setText("Game Over! " + res);
                        JOptionPane.showMessageDialog(MancalaGUI.this, res);
                    } else if (gameState.getPlayer() != playerSide) {
                        // Bot goes again
                        executeBotMove();
                    } else {
                        isPlayerTurn = true;
                        statusLabel.setText("Your Turn!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void updateBoardUI() {
        // Reflection to access the protected 'state' array if needed, 
        // or just rely on toString/getters if available. 
        // Note: Since MancalaNode.state is protected, we might need a getter in MancalaNode 
        // or put this GUI in the same package.
        // Assuming we are in the same package or have access:

        // We have to parse the toString() or change MancalaNode to have public getState()
        // For this example, assuming we added public int[] getState() to MancalaNode:
        // int[] state = gameState.state; // (This requires state to be public or getter)
        // HACK: Since we can't easily change the uploaded GameNode.java right now, 
        // let's rely on the fact that your uploaded classes are in the default package 
        // or simply add "public int[] getState() { return state; }" to MancalaNode.java
        // Visualization update loop (pseudo-code logic)
        // You MUST add `public int[] getState() { return state; }` to MancalaNode.java
        // for this to work cleanly.
        /* int[] state = gameState.getState(); 
        for (int i = 0; i < 14; i++) {
            pits[i].setText(String.valueOf(state[i]));
        }
         */
        statusLabel.setText(isPlayerTurn ? "Your Turn" : "Bot Thinking");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MancalaGUI::new);
    }
}
