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
    private EnemyManager enemyManager; // Declare enemy manager

    public WarAreaPanel() {
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        player = new Player(400, 300, "src/game/picture/Player.png");
        projectileManager = new ProjectileManager();
        score = new Score(); // Initialize score
        enemyManager = new EnemyManager(getWidth(), getHeight()); // Initialize enemy manager with dimensions

        // Load the background image
        backgroundImage = new ImageIcon("src/game/picture/background.png").getImage();

        // Set up a timer to call actionPerformed every 16 milliseconds (about 60 FPS)
        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the background image
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        }

        // Draw the player, projectiles, and enemies
        player.draw(g);
        projectileManager.draw(g);
        enemyManager.draw(g); // Draw enemies using enemy manager

        g.setFont(new Font("Arial", Font.BOLD, 24)); // Change the size to 24 or desired size
        g.setColor(Color.WHITE); // Set the color for the score
        g.drawString("Score: " + score.getValue(), 10, 25); // Display the score at the top-left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Ensure the screen dimensions are available before spawning enemies
        if (enemyManager.getEnemies().size() < EnemyManager.MAX_ENEMIES) {
            enemyManager.spawnEnemy(getWidth(), getHeight());
        }

        // Update the projectiles' positions and enemies, and check for collisions
        projectileManager.update(getWidth(), getHeight());
        enemyManager.updateEnemies(player.getX(), player.getY()); // Update enemies

        // Check for collisions and update score
        CollisionDetector.checkCollisions(projectileManager.getProjectiles(), enemyManager.getEnemies(), score, enemyManager, getWidth(), getHeight());

        repaint();
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
    public void mouseDragged(MouseEvent e) {
        // Update the player's position based on mouse dragging within screen boundaries
        player.setPosition(e.getX() - player.getWidth() / 2, e.getY() - player.getHeight() / 2, getWidth(), getHeight());
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        int startX = player.getX() + player.getWidth() / 2;
        int startY = player.getY() + player.getHeight() / 2;
        int dx = 0, dy = 0;

        // Determine direction of projectile based on key press
        if (key == KeyEvent.VK_LEFT) {
            dx = -5; // Projectiles move left
        } else if (key == KeyEvent.VK_RIGHT) {
            dx = 5; // Projectiles move right
        } else if (key == KeyEvent.VK_UP) {
            dy = -5; // Projectiles move up
        } else if (key == KeyEvent.VK_DOWN) {
            dy = 5; // Projectiles move down
        }

        // Fire projectile if a valid key was pressed
        if (dx != 0 || dy != 0) {
            projectileManager.fireProjectile(startX, startY, dx, dy);
            repaint(); // Refresh the panel to show new projectile
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
