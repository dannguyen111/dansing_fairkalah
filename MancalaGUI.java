import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class MancalaGUI extends JFrame {
    // Game Objects
    private MancalaNode gameState;
    private MancalaPlayer computerPlayer;
    private Stopwatch stopwatch;
    private long botTimeRemaining;

    // UI Components
    private JButton[] pits = new JButton[14];
    private JLabel statusLabel;
    private JTextArea gameLog; // New Log Area
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
        setSize(900, 600);
        setLayout(new BorderLayout(10, 10));

        // Initialize Bot
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

        controlPanel.add(new JLabel("Difficulty:"));
        String[] thinkTime = {"Easy (5s)", "Medium (30s)", "Hard (300s)"};
        diffSelector = new JComboBox<>(thinkTime);
        controlPanel.add(diffSelector);

        startButton = new JButton("Start Game");
        startButton.addActionListener(e -> startGame());
        controlPanel.add(startButton);

        add(controlPanel, BorderLayout.NORTH);

        // --- 2. Board Panel ---
        JPanel boardPanel = new JPanel(new BorderLayout(10, 10));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Left Store (Player 2 / MIN) - Index 13
        pits[13] = createStoreButton(13, "P2 Store");
        boardPanel.add(pits[13], BorderLayout.WEST);

        // Right Store (Player 1 / MAX) - Index 6
        pits[6] = createStoreButton(6, "P1 Store");
        boardPanel.add(pits[6], BorderLayout.EAST);

        // Center Grid (Play Pits)
        JPanel centerGrid = new JPanel(new GridLayout(2, 6, 10, 10));
        
        // Top Row: Player 2 Pits (Indices 12 down to 7)
        for (int i = 12; i >= 7; i--) {
            pits[i] = createPitButton(i);
            centerGrid.add(pits[i]);
        }

        // Bottom Row: Player 1 Pits (Indices 0 up to 5)
        for (int i = 0; i < 6; i++) {
            pits[i] = createPitButton(i);
            centerGrid.add(pits[i]);
        }
        
        boardPanel.add(centerGrid, BorderLayout.CENTER);
        add(boardPanel, BorderLayout.CENTER);

        // --- 3. Bottom Panel (Status + Log) ---
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Status Label at the top of bottom panel
        statusLabel = new JLabel("Welcome! Select settings and click Start Game.");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        bottomPanel.add(statusLabel, BorderLayout.NORTH);

        // Scrollable Game Log
        gameLog = new JTextArea(6, 40);
        gameLog.setEditable(false);
        gameLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        gameLog.setLineWrap(true);
        gameLog.setWrapStyleWord(true);
        
        JScrollPane logScroll = new JScrollPane(gameLog);
        logScroll.setBorder(BorderFactory.createTitledBorder("Game Log"));
        // Auto-scroll logic
        DefaultCaret caret = (DefaultCaret)gameLog.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        bottomPanel.add(logScroll, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // Disable board initially
        setBoardEnabled(false);
        setVisible(true);
    }

    // Helper to append to log
    private void log(String message) {
        gameLog.append(message + "\n");
    }

    private JButton createPitButton(int index) {
        JButton btn = new JButton("4");
        btn.setFont(new Font("SansSerif", Font.BOLD, 24));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(230, 230, 250));
        btn.addActionListener(e -> handleHumanMove(index));
        return btn;
    }

    private JButton createStoreButton(int index, String title) {
        // Updated with color contrast fix
        String colorStyle = (index == 6) ? "color:blue" : "color:red";
        JButton btn = new JButton("<html><center>" + title + "<br><h1 style='font-size:30px;" + colorStyle + "'>0</h1></center></html>");
        
        btn.setPreferredSize(new Dimension(100, 200));
        btn.setEnabled(true); // Keep enabled so colors show brightly
        btn.setBackground(new Color(200, 200, 200));
        btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        // Remove rollover so it doesn't look clickable
        btn.setRolloverEnabled(false);
        return btn;
    }

    private void startGame() {
        try {
            int boardId = Integer.parseInt(boardInput.getText().trim());
            gameState = new dansing2MancalaNode(boardId);
            
            // Setup sides
            int selection = sideSelector.getSelectedIndex();
            humanPlayerSide = (selection == 0) ? GameNode.MAX : GameNode.MIN;

            // Setup time
            int selection2 = diffSelector.getSelectedIndex();
            botTimeRemaining = (selection2 == 0) ? 5000 : (selection2 == 1) ? 30000 : 300000;
            
            gameInProgress = true;
            gameLog.setText(""); // Clear log
            log("=== NEW GAME STARTED ===");
            log("Board ID: " + boardId);
            log("You are Player " + (humanPlayerSide + 1));
            
            updateBoardUI();

            // Determine turn
            if (gameState.getPlayer() == humanPlayerSide) {
                isHumanTurn = true;
                statusLabel.setText("Your Turn! (You are Player " + (humanPlayerSide + 1) + ")");
                log("It is your turn.");
                setBoardEnabled(true);
            } else {
                isHumanTurn = false;
                statusLabel.setText("Bot is thinking...");
                log("Bot (Player " + (humanPlayerSide == 0 ? 2 : 1) + ") is thinking...");
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

        ArrayList<Integer> legalMoves = gameState.getLegalMoves();
        if (!legalMoves.contains(pitIndex)) {
            statusLabel.setText("Invalid Move!");
            log("Invalid move attempted: Pit " + MancalaNode.moveToString(pitIndex));
            return;
        }

        // Log the move
        log("You played pit " + MancalaNode.moveToString(pitIndex));

        // Perform Move
        gameState.makeMove(pitIndex);
        updateBoardUI();
        checkGameState();
    }

    private void executeBotMove() {
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
                    
                    int move = get(); 
                    // Log the bot's move
                    log("Bot played pit " + MancalaNode.moveToString(move));
                    
                    gameState.makeMove(move);
                    updateBoardUI();
                    checkGameState();

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    statusLabel.setText("Bot Error!");
                    log("Error: Bot failed to return a move.");
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

            String gameResult = pits[6] + " - " + pits[13];
            statusLabel.setText("GAME OVER: " + winner);
            log("=== GAME OVER ===");
            log("Result: " + winner);
            log("Final Score: " + gameResult);
            JOptionPane.showMessageDialog(this, winner + "\nFinal Score: " + gameResult);
            return;
        }

        // Check whose turn it is now
        if (gameState.getPlayer() == humanPlayerSide) {
            isHumanTurn = true;
            statusLabel.setText("Your Turn!");
            // Only log "Your turn" if the previous move wasn't also ours (extra turn)
            // But simple is fine:
            log("Your turn.");
            setBoardEnabled(true);
        } else {
            isHumanTurn = false;
            statusLabel.setText("Bot is thinking...");
            log("Bot is thinking...");
            setBoardEnabled(false);
            executeBotMove();
        }
    }

    private void updateBoardUI() {
        int[] state = gameState.state; 

        for (int i = 0; i < 14; i++) {
            if (i == 6) {
                // P1 Store (Blue)
                pits[i].setText("<html><center>P1 Store<br><h1 style='font-size:30px; color:blue'>" + state[i] + "</h1></center></html>");
            } else if (i == 13) {
                // P2 Store (Red)
                pits[i].setText("<html><center>P2 Store<br><h1 style='font-size:30px; color:red'>" + state[i] + "</h1></center></html>");
            } else {
                pits[i].setText(String.valueOf(state[i]));
            }
            
            if (i != 6 && i != 13) {
                if (state[i] == 0) pits[i].setForeground(Color.GRAY);
                else pits[i].setForeground(Color.BLACK);
            }
        }
        
        if (gameState.getPlayer() == 0) { // Player 1
            pits[6].setBorder(BorderFactory.createLineBorder(Color.GREEN, 4));
            pits[13].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        } else { // Player 2
            pits[13].setBorder(BorderFactory.createLineBorder(Color.GREEN, 4));
            pits[6].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        }
    }

    private void setBoardEnabled(boolean enabled) {
        int start = (humanPlayerSide == 0) ? 0 : 7;
        int end = (humanPlayerSide == 0) ? 5 : 12;

        for (int i = 0; i < 14; i++) {
            if (i == 6 || i == 13) continue;

            if (i < start || i > end) {
                pits[i].setEnabled(false);
                continue;
            }

            if (enabled && gameState.state[i] > 0) {
                pits[i].setEnabled(true);
            } else {
                pits[i].setEnabled(false);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MancalaGUI());
    }
}