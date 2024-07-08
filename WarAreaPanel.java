package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WarAreaPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

    private Image backgroundImage; // Image for the background
    private Image characterImage; // Image for the character
    private Image enemyImage; // Image for the enemies
    private int characterX = 240; // Initial X position of the character
    private int characterY = 450; // Initial Y position of the character
    private int mouseX, mouseY; // Mouse coordinates
    private boolean mousePressed = false;
    private Timer timer;
    private ProjectileManager projectileManager;
    private List<Enemy> enemies;
    private Random random;

    public WarAreaPanel() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        projectileManager = new ProjectileManager();
        enemies = new ArrayList<>();
        random = new Random();

        loadImages(); // Load the images

        timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                projectileManager.update();
                updateEnemies();
                repaint();
            }
        });
        timer.start();

        // Spawn enemies after the panel has been properly sized
        SwingUtilities.invokeLater(() -> spawnEnemies(10));
    }

    private void loadImages() {
        // Load background image
        ImageIcon backgroundIcon = new ImageIcon("D:/java_new_project/src/picture/background.png");
        backgroundImage = backgroundIcon.getImage();

        // Load character image
        ImageIcon characterIcon = new ImageIcon("D:/java_new_project/src/picture/Player.png");
        characterImage = characterIcon.getImage();

        // Load enemy image
        ImageIcon enemyIcon = new ImageIcon("D:/java_new_project/src/picture/Enemy.png");
        enemyImage = enemyIcon.getImage();
    }

    private void spawnEnemies(int count) {
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // Ensure that the panel has been properly sized before spawning enemies
        if (panelWidth > 0 && panelHeight > 0) {
            for (int i = 0; i < count; i++) {
                int startX = random.nextInt(panelWidth - enemyImage.getWidth(null));
                int startY = random.nextInt(panelHeight / 2);
                enemies.add(new Enemy(startX, startY, enemyImage));
            }
        }
    }

    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            enemy.moveTowards(characterX, characterY);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw background
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        // Draw character
        if (characterImage != null) {
            g2d.drawImage(characterImage, characterX, characterY, this);
        }

        // Draw enemies
        for (Enemy enemy : enemies) {
            g2d.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), this);
        }

        // Draw projectiles
        g2d.setColor(Color.YELLOW);
        for (Projectile projectile : projectileManager.getProjectiles()) {
            g2d.fillOval(projectile.getX(), projectile.getY(), projectile.getWidth(), projectile.getHeight());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        mousePressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        if (mousePressed) {
            int dx = e.getX() - mouseX;
            int dy = e.getY() - mouseY;

            // Update character position
            int newX = characterX + dx;
            int newY = characterY + dy;

            // Check boundaries
            if (newX >= 0 && newX + characterImage.getWidth(this) <= getWidth()) {
                characterX = newX;
            }
            if (newY >= 0 && newY + characterImage.getHeight(this) <= getHeight()) {
                characterY = newY;
            }

            mouseX = e.getX();
            mouseY = e.getY();
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        // Direction variable;

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            projectileManager.fireProjectile(characterX, characterY, -1, 0); // Left
        }

        if (key == KeyEvent.VK_RIGHT) {
            projectileManager.fireProjectile(characterX, characterY, 1, 0); // Right
        }

        if (key == KeyEvent.VK_UP) {
            projectileManager.fireProjectile(characterX, characterY, 0, -1); // Up
        }

        if (key == KeyEvent.VK_DOWN) {
            projectileManager.fireProjectile(characterX, characterY, 0, 1); // Down
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {}

    private class ProjectileManager {
        private List<Projectile> projectiles;

        public ProjectileManager() {
            projectiles = new ArrayList<>();
        }

        public void fireProjectile(int startX, int startY, int dx, int dy) {
            System.out.println("Firing projectile from (" + startX + ", " + startY + ") with direction (" + dx + ", " + dy + ")");
            projectiles.add(new Projectile(startX + characterImage.getWidth(null) / 2,
                    startY + characterImage.getHeight(null) / 2,
                    dx, dy));
        }

        public void update() {
            for (int i = projectiles.size() - 1; i >= 0; i--) {
                Projectile projectile = projectiles.get(i);
                projectile.move();
                if (!projectile.isVisible()) {
                    projectiles.remove(i);
                }
            }
        }

        public List<Projectile> getProjectiles() {
            return projectiles;
        }
    }

    private class Projectile {
        private int x, y;
        private final int width = 10;
        private final int height = 10;
        private final int speed = 10;
        private int dx, dy; // Direction of the projectile

        public Projectile(int startX, int startY, int dx, int dy) {
            this.x = startX - width / 2;
            this.y = startY - height / 2;
            this.dx = dx * speed;
            this.dy = dy * speed;
        }

        public void move() {
            x += dx;
            y += dy;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isVisible() {
            // Check if projectile is still within bounds
            return x >= 0 && x <= getWidth() && y >= 0 && y <= getHeight();
        }
    }

    private class Enemy {
        private int x, y;
        private final int speed = 2;
        private Image image;

        public Enemy(int startX, int startY, Image image) {
            this.x = startX;
            this.y = startY;
            this.image = image;
        }

        public void moveTowards(int targetX, int targetY) {
            int dx = targetX - x;
            int dy = targetY - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance > 0) {
                x += (int) (speed * dx / distance);
                y += (int) (speed * dy / distance);
            }
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Image getImage() {
            return image;
        }
    }
}
