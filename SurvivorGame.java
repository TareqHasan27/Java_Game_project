package game;

import javax.swing.*;

public class SurvivorGame extends JFrame {

    public SurvivorGame() {
        setTitle("Survivor Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new StartScreenPanel(this));  // Start screen panel
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SurvivorGame::new);
    }

    // Method to switch to the game panel
    public void startGame() {
        remove(getContentPane().getComponent(0)); // Remove the start screen
        WarAreaPanel gamePanel = new WarAreaPanel();
        add(gamePanel);  // Add the game panel
        revalidate();  // Refresh the frame
        repaint();
        gamePanel.requestFocusInWindow(); // Request focus for the game panel
    }
}
