package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WarAreaPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

    private Image backgroundImage;
    private Image characterImage;
    private Image enemyImage;
    private Image newEnemyImage;
    private int characterX = 240;
    private int characterY = 450;
    private boolean mousePressed = false;
    private Timer timer;
    private ProjectileManager projectileManager;
    private ProjectileManager enemyProjectileManager;
    private List<Enemy> enemies;
    private Random random;
    private Timer enemyRespawnTimer;
    private boolean firingLeft, firingRight, firingUp, firingDown;
    private int score = 0;
    private NewEnemy newEnemy; // Replaces SpecialEnemy
    private Timer newEnemyRespawnTimer;
    private int lastNewEnemyScore = 0; // Track the last score when the new enemy appeared

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

        loadImages();

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
                spawnEnemies(5);
            }
        });
        enemyRespawnTimer.start();

        SwingUtilities.invokeLater(() -> spawnEnemies(5));
    }

    private void loadImages() {
        ImageIcon backgroundIcon = new ImageIcon("background.png");
        backgroundImage = backgroundIcon.getImage();

        ImageIcon characterIcon = new ImageIcon("Player.png");
        characterImage = characterIcon.getImage();

        ImageIcon enemyIcon = new ImageIcon("Enemy.png");
        enemyImage = enemyIcon.getImage();

        ImageIcon newEnemyIcon = new ImageIcon("NewEnemy.png");
        newEnemyImage = newEnemyIcon.getImage();
    }

    private void spawnEnemies(int maxCount) {
        int currentEnemyCount = enemies.size();
        int enemiesToSpawn = maxCount - currentEnemyCount;

        if (enemiesToSpawn > 0) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();

            if (panelWidth > 0 && panelHeight > 0) {
                for (int i = 0; i < enemiesToSpawn; i++) {
                    int startX = 0, startY = 0;
                    int side = random.nextInt(4);
                    switch (side) {
                        case 0:
                            startX = 0;
                            startY = random.nextInt(panelHeight);
                            break;
                        case 1:
                            startX = panelWidth - enemyImage.getWidth(null);
                            startY = random.nextInt(panelHeight);
                            break;
                        case 2:
                            startX = random.nextInt(panelWidth);
                            startY = 0;
                            break;
                        case 3:
                            startX = random.nextInt(panelWidth);
                            startY = panelHeight - enemyImage.getHeight(null);
                            break;
                    }
                    Enemy enemy = new Enemy(startX, startY, enemyImage);
                    enemies.add(enemy);

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

        // Check if a new enemy needs to be spawned
        if (score >= lastNewEnemyScore + 100 && (newEnemy == null || newEnemy.isDestroyed())) {
            lastNewEnemyScore = score;
            newEnemy = new NewEnemy(getWidth() / 2, getHeight() / 2, newEnemyImage);

            // Set up respawn timer if the new enemy was destroyed
            if (newEnemyRespawnTimer != null) {
                newEnemyRespawnTimer.stop();
            }
            newEnemyRespawnTimer = new Timer(10000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    newEnemy = new NewEnemy(getWidth() / 2, getHeight() / 2, newEnemyImage);
                }
            });
            newEnemyRespawnTimer.setRepeats(false);
            newEnemyRespawnTimer.start();
        }

        if (newEnemy != null) {
            newEnemy.moveTowards(characterX, characterY);
            newEnemy.updateProjectiles();
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
                    score += 10;
                    break;
                }
            }
        }

        List<Projectile> enemyProjectiles = enemyProjectileManager.getProjectiles();
        for (int i = enemyProjectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = enemyProjectiles.get(i);
            if (projectile.collidesWith(characterX, characterY, characterImage.getWidth(null), characterImage.getHeight(null))) {
                enemyProjectiles.remove(i);
                System.out.println("Character hit!");
            }
        }

        if (newEnemy != null) {
            List<Projectile> newEnemyProjectiles = newEnemy.getProjectiles();
            for (int i = newEnemyProjectiles.size() - 1; i >= 0; i--) {
                Projectile projectile = newEnemyProjectiles.get(i);
                if (projectile.collidesWith(characterX, characterY, characterImage.getWidth(null), characterImage.getHeight(null))) {
                    newEnemy.getProjectiles().remove(i);
                    System.out.println("Character hit by new enemy projectile!");
                }
            }

            // Check for collisions between player projectiles and new enemy
            for (int i = projectiles.size() - 1; i >= 0; i--) {
                Projectile projectile = projectiles.get(i);
                if (projectile.collidesWith(newEnemy.getX(), newEnemy.getY(),
                        newEnemy.getImage().getWidth(null), newEnemy.getImage().getHeight(null))) {
                    projectiles.remove(i);
                    newEnemy.hit();
                    if (newEnemy.isDestroyed()) {
                        newEnemy = null; // Remove the new enemy
                        score += 30; // Add bonus points for destroying the new enemy
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        if (characterImage != null) {
            g2d.drawImage(characterImage, characterX, characterY, this);
        }

        for (Enemy enemy : enemies) {
            g2d.drawImage(enemy.getImage(), enemy.getX(), enemy.getY(), this);
        }

        if (newEnemy != null) {
            g2d.drawImage(newEnemy.getImage(), newEnemy.getX(), newEnemy.getY(), this);
        }

        g2d.setColor(Color.YELLOW);
        for (Projectile projectile : projectileManager.getProjectiles()) {
            g2d.fillOval(projectile.getX(), projectile.getY(), projectile.getWidth(), projectile.getHeight());
        }

        g2d.setColor(Color.RED);
        for (Projectile projectile : enemyProjectileManager.getProjectiles()) {
            g2d.fillOval(projectile.getX(), projectile.getY(), projectile.getWidth(), projectile.getHeight());
        }

        if (newEnemy != null) {
            for (Projectile projectile : newEnemy.getProjectiles()) {
                g2d.setColor(projectile.getColor());
                g2d.fillOval(projectile.getX(), projectile.getY(), projectile.getWidth(), projectile.getHeight());
            }
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + score, 10, 20);
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
        characterX = e.getX() - characterImage.getWidth(null) / 2;
        characterY = e.getY() - characterImage.getHeight(null) / 2;
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

        if (key == KeyEvent.VK_LEFT) {
            firingLeft = true;
        } else if (key == KeyEvent.VK_RIGHT) {
            firingRight = true;
        } else if (key == KeyEvent.VK_UP) {
            firingUp = true;
        } else if (key == KeyEvent.VK_DOWN) {
            firingDown = true;
        }

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
        private Color color;

        public Projectile(int x, int y, int dx, int dy, int width, int height) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.width = width;
            this.height = height;
            this.color = Color.YELLOW;
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

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
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
            x += (int) (dx / distance * 2);
            y += (int) (dy / distance * 2);
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

    private class NewEnemy {
        private int x, y;
        private Image image;
        private List<Projectile> projectiles;
        private Random random;
        private int hitPoints = 10; // Number of hits to destroy the NewEnemy

        public NewEnemy(int x, int y, Image image) {
            this.x = x;
            this.y = y;
            this.image = image;
            this.projectiles = new ArrayList<>();
            this.random = new Random();
            fireNewEnemyProjectiles();
        }

        private void fireNewEnemyProjectiles() {
            Timer timer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int startX = x + image.getWidth(null) / 2;
                    int startY = y + image.getHeight(null) / 2;
                    int dx = characterX - startX;
                    int dy = characterY - startY;
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    dx = (int) (dx / distance * 5);
                    dy = (int) (dy / distance * 5);

                    Projectile projectile = new Projectile(startX, startY, dx, dy, 15, 15);
                    projectile.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
                    projectiles.add(projectile);
                }
            });
            timer.start();
        }

        public void moveTowards(int targetX, int targetY) {
            int dx = targetX - x;
            int dy = targetY - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            x += (int) (dx / distance * 2);
            y += (int) (dy / distance * 2);
        }

        public void updateProjectiles() {
            for (int i = projectiles.size() - 1; i >= 0; i--) {
                Projectile projectile = projectiles.get(i);
                projectile.update();
                if (projectile.isOutOfBounds(getWidth(), getHeight())) {
                    projectiles.remove(i);
                }
            }
        }

        public void hit() {
            hitPoints--;
        }

        public boolean isDestroyed() {
            return hitPoints <= 0;
        }

        public List<Projectile> getProjectiles() {
            return projectiles;
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
