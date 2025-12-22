import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

public class MancalaGUI extends JFrame {
    // Game Objects
    private MancalaNode gameState;
    private MancalaPlayer computerPlayer;
    private Stopwatch stopwatch;
    private long botTimeRemaining;

    // UI Components
    private JButton[] pits = new JButton[14];
    private JLabel statusLabel;
    private JButton startButton;
    private JTextField boardInput;
    private JComboBox<String> sideSelector;
    private JComboBox<String> diffSelector;
    
    // Game State Flags
    private boolean isHumanTurn;
    private int humanPlayerSide = GameNode.MAX; // MAX = 0 (Player 1), MIN = 1 (Player 2)
    private boolean gameInProgress = false;

    public MancalaGUI() {
        super("Dansing FairKalah GUI");
        stopwatch = new Stopwatch();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLayout(new BorderLayout(10, 10));

        // Initialize Bot (The one you uploaded)
        computerPlayer = new dansing2MancalaPlayer();

        // --- 1. Top Control Panel ---
        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Game Settings"));
        
        controlPanel.add(new JLabel("FairKalah Board ID (0-253 or -1):"));
        boardInput = new JTextField("-1", 5);
        controlPanel.add(boardInput);

        String[] sides = {"Play as Player 1 (Bottom)", "Play as Player 2 (Top)"};
        sideSelector = new JComboBox<>(sides);
        controlPanel.add(sideSelector);

        controlPanel.add(new JLabel("Difficulty (Total think time):"));
        String[] thinkTime = {"Easy (5s)", "Medium (30s)", "Hard (300s)"};
        diffSelector = new JComboBox<>(thinkTime);
        controlPanel.add(diffSelector);

        startButton = new JButton("Start Game");
        startButton.addActionListener(e -> startGame());
        controlPanel.add(startButton);

        add(controlPanel, BorderLayout.NORTH);

        // --- 2. Board Panel ---
        JPanel boardPanel = new JPanel(new BorderLayout(10, 10));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Left Store (Player 2 / MIN) - Index 13
        pits[13] = createStoreButton(13, "P2 Store");
        boardPanel.add(pits[13], BorderLayout.WEST);

        // Right Store (Player 1 / MAX) - Index 6
        pits[6] = createStoreButton(6, "P1 Store");
        boardPanel.add(pits[6], BorderLayout.EAST);

        // Center Grid (Play Pits)
        JPanel centerGrid = new JPanel(new GridLayout(2, 6, 10, 10)); // 2 Rows, 6 Cols
        
        // Top Row: Player 2 Pits (Indices 12 down to 7)
        // Visually P2 moves Right-to-Left, so index 12 is Top-Left, 7 is Top-Right.
        for (int i = 12; i >= 7; i--) {
            pits[i] = createPitButton(i);
            centerGrid.add(pits[i]);
        }

        // Bottom Row: Player 1 Pits (Indices 0 up to 5)
        // Visually P1 moves Left-to-Right.
        for (int i = 0; i < 6; i++) {
            pits[i] = createPitButton(i);
            centerGrid.add(pits[i]);
        }
        
        boardPanel.add(centerGrid, BorderLayout.CENTER);
        add(boardPanel, BorderLayout.CENTER);

        // --- 3. Status Panel ---
        statusLabel = new JLabel("Welcome! Select settings and click Start Game.");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(statusLabel, BorderLayout.SOUTH);

        // Disable board initially
        setBoardEnabled(false);
        setVisible(true);
    }

    private JButton createPitButton(int index) {
        JButton btn = new JButton("4");
        btn.setFont(new Font("SansSerif", Font.BOLD, 24));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(230, 230, 250)); // Light purple
        btn.addActionListener(e -> handleHumanMove(index));
        return btn;
    }

    private JButton createStoreButton(int index, String title) {
        JButton btn = new JButton("<html><center>" + title + "<br><h1 style='font-size:30px'>0</h1></center></html>");
        btn.setPreferredSize(new Dimension(100, 200));
        btn.setEnabled(false); // Stores are never clickable
        btn.setBackground(new Color(200, 200, 200));
        btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        return btn;
    }

    private void startGame() {
        try {
            int boardId = Integer.parseInt(boardInput.getText().trim());
            // Initialize the game state using your dansing2MancalaNode
            gameState = new dansing2MancalaNode(boardId);
            
            // Determine who is who
            int selection = sideSelector.getSelectedIndex();
            humanPlayerSide = (selection == 0) ? GameNode.MAX : GameNode.MIN;

            int selection2 = diffSelector.getSelectedIndex();
            botTimeRemaining = (selection2 == 0) ? 5000 : (selection2 == 1) ? 30000 : 300000;
            
            gameInProgress = true;
            updateBoardUI();

            // Determine whose turn it is
            // Note: dansing2MancalaNode defaults to Player 0 (MAX) going first
            if (gameState.getPlayer() == humanPlayerSide) {
                isHumanTurn = true;
                statusLabel.setText("Your Turn! (You are Player " + (humanPlayerSide + 1) + ")");
                setBoardEnabled(true);
            } else {
                isHumanTurn = false;
                statusLabel.setText("Bot is thinking...");
                setBoardEnabled(false);
                executeBotMove();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer for the Board ID.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error starting game: " + e.getMessage());
        }
    }

    private void handleHumanMove(int pitIndex) {
        if (!gameInProgress || !isHumanTurn) return;

        // Check if move is valid
        ArrayList<Integer> legalMoves = gameState.getLegalMoves();
        if (!legalMoves.contains(pitIndex)) {
            statusLabel.setText("Invalid Move! You must pick a non-empty pit on your side.");
            return;
        }

        // Perform Move
        gameState.makeMove(pitIndex);
        updateBoardUI();
        checkGameState();
    }

    private void executeBotMove() {
        // Use SwingWorker to prevent UI freezing while bot thinks
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                stopwatch.reset();
                stopwatch.start();
                int move = computerPlayer.chooseMove(gameState, botTimeRemaining);
                long timeTaken = stopwatch.stop();
                botTimeRemaining -= timeTaken;
                return move;
            }

            @Override
            protected void done() {
                try {
                    if (!gameInProgress) return;
                    
                    int move = get(); // Get the move chosen by the bot
                    gameState.makeMove(move);
                    updateBoardUI();
                    checkGameState();

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    statusLabel.setText("Bot Error!");
                }
            }
        }.execute();
    }

    private void checkGameState() {
        if (gameState.gameOver()) {
            gameInProgress = false;
            setBoardEnabled(false);
            double util = gameState.utility();
            String winner;
            if (util > 0) winner = "Player 1 Wins!";
            else if (util < 0) winner = "Player 2 Wins!";
            else winner = "It's a Draw!";
            
            statusLabel.setText("GAME OVER: " + winner);
            JOptionPane.showMessageDialog(this, winner + "\nFinal Score: " + util);
            return;
        }

        // Check whose turn it is now
        if (gameState.getPlayer() == humanPlayerSide) {
            isHumanTurn = true;
            statusLabel.setText("Your Turn!");
            setBoardEnabled(true);
        } else {
            isHumanTurn = false;
            statusLabel.setText("Bot is thinking...");
            setBoardEnabled(false);
            executeBotMove();
        }
    }

    /**
     * Updates the text/numbers on all buttons based on the gameState.
     * CRITICAL: accessing 'state' directly works because we are in the same package (default).
     */
    private void updateBoardUI() {
        // We access the protected 'state' array from MancalaNode directly.
        // This requires MancalaGUI.java to be in the same directory/package as MancalaNode.java.
        int[] state = gameState.state; 

        // Update Pits 0-5 and 7-12
        for (int i = 0; i < 14; i++) {
            // Update Stores differently
            if (i == 6) {
                pits[i].setText("<html><center>P1 Store<br><h1 style='font-size:30px; color: blue'>" + state[i] + "</h1></center></html>");
            } else if (i == 13) {
                pits[i].setText("<html><center>P2 Store<br><h1 style='font-size:30px; color: red'>" + state[i] + "</h1></center></html>");
            } else {
                pits[i].setText(String.valueOf(state[i]));
            }
            
            // Visual Highlight for non-empty pits
            if (i != 6 && i != 13) {
                if (state[i] == 0) pits[i].setForeground(Color.GRAY);
                else pits[i].setForeground(Color.BLACK);
            }
        }
        
        // Highlight active side
        if (gameState.getPlayer() == 0) { // Player 1
            pits[6].setBorder(BorderFactory.createLineBorder(Color.GREEN, 4));
            pits[13].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        } else { // Player 2
            pits[13].setBorder(BorderFactory.createLineBorder(Color.GREEN, 4));
            pits[6].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        }
    }

    private void setBoardEnabled(boolean enabled) {
        // Only enable the buttons for the Human's side
        int start = (humanPlayerSide == 0) ? 0 : 7;
        int end = (humanPlayerSide == 0) ? 5 : 12;

        for (int i = 0; i < 14; i++) {
            // Never enable stores
            if (i == 6 || i == 13) continue;

            // Always disable opponent's pits
            if (i < start || i > end) {
                pits[i].setEnabled(false);
                continue;
            }

            // Enable own pits only if it is human turn AND they have stones
            if (enabled && gameState.state[i] > 0) {
                pits[i].setEnabled(true);
            } else {
                pits[i].setEnabled(false);
            }
        }
    }

    public static void main(String[] args) {
        // Ensure UI Thread safety
        SwingUtilities.invokeLater(() -> new MancalaGUI());
    }
}