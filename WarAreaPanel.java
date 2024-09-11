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
    private boolean mousePressed = false;
    private Timer timer;
    private ProjectileManager projectileManager;
    private ProjectileManager enemyProjectileManager;
    private List<Enemy> enemies;
    private Random random;
    private Timer enemyRespawnTimer;
    private boolean firingLeft, firingRight, firingUp, firingDown;

    public WarAreaPanel() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        projectileManager = new ProjectileManager();
        enemyProjectileManager = new ProjectileManager();
        enemies = new ArrayList<>();
        random = new Random();

        loadImages(); // Load the images

        timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                projectileManager.update();
                enemyProjectileManager.update();
                updateEnemies();
                checkCollisions();
                repaint();
            }
        });
        timer.start();

        enemyRespawnTimer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Ensure there are always 10 enemies
                spawnEnemies(5);
            }
        });
        enemyRespawnTimer.start();

        // Spawn enemies after the panel has been properly sized
        SwingUtilities.invokeLater(() -> spawnEnemies(5));
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

    private void spawnEnemies(int maxCount) {
        int currentEnemyCount = enemies.size();

        // Ensure we don't spawn more than the max count (e.g., 10 enemies)
        int enemiesToSpawn = maxCount - currentEnemyCount;

        if (enemiesToSpawn > 0) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();

            // Ensure that the panel has been properly sized before spawning enemies
            if (panelWidth > 0 && panelHeight > 0) {
                for (int i = 0; i < enemiesToSpawn; i++) {
                    int startX = 0, startY = 0;
                    int side = random.nextInt(4);
                    switch (side) {
                        case 0: // Left
                            startX = 0;
                            startY = random.nextInt(panelHeight);
                            break;
                        case 1: // Right
                            startX = panelWidth - enemyImage.getWidth(null);
                            startY = random.nextInt(panelHeight);
                            break;
                        case 2: // Top
                            startX = random.nextInt(panelWidth);
                            startY = 0;
                            break;
                        case 3: // Bottom
                            startX = random.nextInt(panelWidth);
                            startY = panelHeight - enemyImage.getHeight(null);
                            break;
                    }
                    Enemy enemy = new Enemy(startX, startY, enemyImage);
                    enemies.add(enemy);

                    // Schedule enemy projectile firing
                    new Timer(random.nextInt(3000) + 2000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            enemyProjectileManager.fireProjectileEnemy(enemy.getX() + enemy.getImage().getWidth(null) / 2,
                                    enemy.getY() + enemy.getImage().getHeight(null) / 2,
                                    characterX + characterImage.getWidth(null) / 2,
                                    characterY + characterImage.getHeight(null) / 2);
                        }
                    }).start();
                }
            }
        }
    }

    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            enemy.moveTowards(characterX, characterY);
        }
    }

    private void checkCollisions() {
        List<Projectile> projectiles = projectileManager.getProjectiles();
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = projectiles.get(i);
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (projectile.collidesWith(enemy)) {
                    projectiles.remove(i);
                    enemies.remove(j);
                    break;
                }
            }
        }

        // Check for collisions between enemy projectiles and the character
        List<Projectile> enemyProjectiles = enemyProjectileManager.getProjectiles();
        for (int i = enemyProjectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = enemyProjectiles.get(i);
            if (projectile.collidesWith(characterX, characterY, characterImage.getWidth(null), characterImage.getHeight(null))) {
                enemyProjectiles.remove(i);
                // Handle character being hit (e.g., reduce health, end game, etc.)
                System.out.println("Character hit!");
            }
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

        // Draw enemy projectiles
        g2d.setColor(Color.RED);
        for (Projectile projectile : enemyProjectileManager.getProjectiles()) {
            g2d.fillOval(projectile.getX(), projectile.getY(), projectile.getWidth(), projectile.getHeight());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
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
        // Update character position based on mouse drag
        characterX = e.getX() - characterImage.getWidth(null) / 2;
        characterY = e.getY() - characterImage.getHeight(null) / 2;
        // Ensure character stays within panel boundaries
        if (characterX < 0) characterX = 0;
        if (characterY < 0) characterY = 0;
        if (characterX > getWidth() - characterImage.getWidth(null))
            characterX = getWidth() - characterImage.getWidth(null);
        if (characterY > getHeight() - characterImage.getHeight(null))
            characterY = getHeight() - characterImage.getHeight(null);
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Determine direction based on key pressed
        if (key == KeyEvent.VK_LEFT) {
            firingLeft = true;
        } else if (key == KeyEvent.VK_RIGHT) {
            firingRight = true;
        } else if (key == KeyEvent.VK_UP) {
            firingUp = true;
        } else if (key == KeyEvent.VK_DOWN) {
            firingDown = true;
        }

        // Fire projectile based on direction
        if (firingLeft || firingRight || firingUp || firingDown) {
            int startX = characterX + characterImage.getWidth(null) / 2;
            int startY = characterY + characterImage.getHeight(null) / 2;
            int dx = 0, dy = 0;

            if (firingLeft) {
                dx = -5;
            }
            if (firingRight) {
                dx = 5;
            }
            if (firingUp) {
                dy = -5;
            }
            if (firingDown) {
                dy = 5;
            }

            projectileManager.fireProjectile(startX, startY, dx, dy);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        // Stop firing in a direction when the key is released
        if (key == KeyEvent.VK_LEFT) {
            firingLeft = false;
        } else if (key == KeyEvent.VK_RIGHT) {
            firingRight = false;
        } else if (key == KeyEvent.VK_UP) {
            firingUp = false;
        } else if (key == KeyEvent.VK_DOWN) {
            firingDown = false;
        }
    }

    private class ProjectileManager {
        private List<Projectile> projectiles;

        public ProjectileManager() {
            projectiles = new ArrayList<>();
        }

        public void fireProjectile(int startX, int startY, int dx, int dy) {
            projectiles.add(new Projectile(startX, startY, dx, dy, 10, 10));
        }

        public void fireProjectileEnemy(int startX, int startY, int targetX, int targetY) {
            int dx = targetX - startX;
            int dy = targetY - startY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            projectiles.add(new Projectile(startX, startY, (int) (dx / distance * 5), (int) (dy / distance * 5), 10, 10));
        }

        public void update() {
            for (int i = projectiles.size() - 1; i >= 0; i--) {
                Projectile projectile = projectiles.get(i);
                projectile.update();
                if (projectile.isOutOfBounds(getWidth(), getHeight())) {
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
        private int dx, dy;
        private int width, height;

        public Projectile(int x, int y, int dx, int dy, int width, int height) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.width = width;
            this.height = height;
        }

        public void update() {
            x += dx;
            y += dy;
        }

        public boolean isOutOfBounds(int panelWidth, int panelHeight) {
            return x < 0 || x > panelWidth || y < 0 || y > panelHeight;
        }

        public boolean collidesWith(Enemy enemy) {
            return x < enemy.getX() + enemy.getImage().getWidth(null) &&
                    x + width > enemy.getX() &&
                    y < enemy.getY() + enemy.getImage().getHeight(null) &&
                    y + height > enemy.getY();
        }

        public boolean collidesWith(int targetX, int targetY, int targetWidth, int targetHeight) {
            return x < targetX + targetWidth &&
                    x + width > targetX &&
                    y < targetY + targetHeight &&
                    y + height > targetY;
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
    }

    private class Enemy {
        private int x, y;
        private Image image;

        public Enemy(int x, int y, Image image) {
            this.x = x;
            this.y = y;
            this.image = image;
        }

        public void moveTowards(int targetX, int targetY) {
            int dx = targetX - x;
            int dy = targetY - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            x += (int) (dx / distance * 2); // Adjust speed
            y += (int) (dy / distance * 2); // Adjust speed
        }

        public Image getImage() {
            return image;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
