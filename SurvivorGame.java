package game;

import javax.swing.*;

public class SurvivorGame extends JFrame {

    public SurvivorGame() {
        setTitle("Survivor Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new WarAreaPanel());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SurvivorGame::new);
    }
}
