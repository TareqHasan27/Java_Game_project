package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class WarAreaPanel extends JPanel implements KeyListener, MouseListener, MouseMotionListener, ActionListener {
    private Player player;
    private ProjectileManager projectileManager;
    private Image backgroundImage;
    private Timer timer;
    private Score score;
    private EnemyManager enemyManager;
    private boolean hasPlayerMoved = false;
    private long startTime;
    private boolean survivalTimeStarted = false;
    private int enemiesKilled = 0;
    private boolean gameOver = false;
    private long survivalTime = 0;

    public WarAreaPanel() {
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        player = new Player(400, 300, "src/game/picture/Player.png");
        projectileManager = new ProjectileManager();
        score = new Score();
        enemyManager = new EnemyManager(getWidth(), getHeight());

        backgroundImage = new ImageIcon("src/game/picture/background.png").getImage();

        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        }

        if (!gameOver) {
            player.draw(g);
            projectileManager.draw(g);
            enemyManager.draw(g, player.getX(), player.getY());

            int scoreValue = score.getValue();
            g.setColor(Color.BLACK);
            g.setFont(new Font("Verdana", Font.BOLD | Font.ITALIC, 18));
            g.drawString("Score: " + scoreValue, 12, 22);
            g.setColor(Color.WHITE);
            g.drawString("Score: " + scoreValue, 10, 20);

            drawHealthBar(g, getWidth() - 120, 10, 100);

            if (survivalTimeStarted) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsedTime / 1000);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.drawString("Survival Time: " + seconds + "s", 10, 40);
            }
        } else {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.setColor(Color.WHITE);
            g2d.drawString("GAME OVER!", getWidth() / 2 - 152, getHeight() / 2 - 22);
            g2d.setColor(Color.RED);
            g2d.drawString("GAME OVER!", getWidth() / 2 - 150, getHeight() / 2 - 20);

            g2d.setFont(new Font("Comic Sans MS", Font.BOLD, 18));
            g2d.setColor(Color.BLACK);
            g2d.drawString("Survival Time: " + survivalTime + " seconds", getWidth() / 2 - 112, getHeight() / 2 + 18);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Survival Time: " + survivalTime + " seconds", getWidth() / 2 - 114, getHeight() / 2 + 20);

            g2d.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
            g2d.setColor(Color.BLACK);
            g2d.drawString("Final Score: " + score.getValue(), getWidth() / 2 - 60, getHeight() / 2 + 48);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Final Score: " + score.getValue(), getWidth() / 2 - 62, getHeight() / 2 + 50);
        }
    }

    private void drawHealthBar(Graphics g, int x, int y, int width) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial Black", Font.BOLD, 11));
        g2d.drawString("HealthBar", x + 20, y + 30);

        g2d.setColor(Color.GRAY);
        g2d.fillRect(x, y, width, 20);

        g2d.setColor(Color.GREEN);
        int playerHealth = (int) ((player.getHealth() / (float) player.getMaxHealth()) * width);
        g2d.fillRect(x, y, playerHealth, 20);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(x, y, width, 20);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString(String.format("%.1f%%", (player.getHealth() / (float) player.getMaxHealth()) * 100), x + 25, y + 15);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (hasPlayerMoved && enemyManager.getEnemies().size() < EnemyManager.MAX_ENEMIES) {
            enemyManager.spawnEnemy(getWidth(), getHeight());
        }

        projectileManager.update(getWidth(), getHeight());
        enemyManager.updateEnemies(player.getX(), player.getY());

        // Update this line to include the WarAreaPanel instance
        CollisionDetector.checkCollisions(
                projectileManager.getProjectiles(),
                enemyManager.getEnemies(),
                score,
                enemyManager,
                getWidth(),
                getHeight(),
                player,
                this // Add this line to pass the WarAreaPanel instance
        );

        if (!player.isAlive()) {
            gameOver();
        }

        // Health restoration logic after killing enemies
        if (enemiesKilled >= 5) { // You can change this to 7 based on your preference
            restoreHealth(4); // Increase health by 3%
            enemiesKilled = 0; // Reset the counter after restoring health
        }

        repaint();
    }

    private void restoreHealth(int percentage) {
        int restoreAmount = (player.getMaxHealth() * percentage) / 100;
        player.increaseHealth(restoreAmount);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }

        // Optionally, print the health increase for debugging purposes
        System.out.println("Health restored! Current health: " + player.getHealth());
    }
    public void enemyKilled() {
        enemiesKilled++;
        System.out.println("Enemies killed: " + enemiesKilled); // Optional: For debugging
    }

    private void gameOver() {
        gameOver = true;
        timer.stop();
        survivalTime = (System.currentTimeMillis() - startTime) / 1000;
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        player.setPosition(e.getX() - player.getWidth() / 2, e.getY() - player.getHeight() / 2, getWidth(), getHeight());

        if (!survivalTimeStarted) {
            startTime = System.currentTimeMillis();
            survivalTimeStarted = true;
        }

        hasPlayerMoved = true;
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        int startX = player.getX() + player.getWidth() / 2;
        int startY = player.getY() + player.getHeight() / 2;
        int dx = 0, dy = 0;

        // Determine direction of projectile based on key press
        switch (key) {
            case KeyEvent.VK_LEFT -> dx = -5; // Projectiles move left
            case KeyEvent.VK_RIGHT -> dx = 5; // Projectiles move right
            case KeyEvent.VK_UP -> dy = -5; // Projectiles move up
            case KeyEvent.VK_DOWN -> dy = 5; // Projectiles move down
        }

        // Fire projectile if a direction key was pressed
        if (dx != 0 || dy != 0) {
            projectileManager.fireProjectile(startX, startY, dx, dy, Color.YELLOW);
            hasPlayerMoved = true;  // Mark that the player has moved
            repaint();  // Refresh the panel to show the new projectile
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
