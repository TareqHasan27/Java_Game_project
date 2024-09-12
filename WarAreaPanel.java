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
    private Image fierceEnemyImage;
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
    private FierceEnemy fierceEnemy;
    private Timer fierceEnemyRespawnTimer;
    private int lastFierceEnemyScore = 0;
    private double playerHealth = 100.0;
    private int killCount = 0;
    private boolean gameOver = false;
    private long startTime;
    private long survivalTime;
    private Timer survivalTimeTimer;

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
        startTime = System.currentTimeMillis();
        gameOver = false;

        timer = new Timer(16, e -> {
            projectileManager.update();
            enemyProjectileManager.update();
            updateEnemies();
            checkCollisions();
            repaint();
        });
        timer.start();

        enemyRespawnTimer = new Timer(3000, e -> spawnEnemies(5));
        enemyRespawnTimer.start();

        SwingUtilities.invokeLater(() -> spawnEnemies(5));

        // Initialize the survival time timer
        survivalTimeTimer = new Timer(1000, e -> {
            if (!gameOver) {
                survivalTime++;
                repaint(); // Trigger repaint to update the display
            }
        });
        survivalTimeTimer.start();
    }

    private void loadImages() {
        ImageIcon backgroundIcon = new ImageIcon("background.png");
        backgroundImage = backgroundIcon.getImage();

        ImageIcon characterIcon = new ImageIcon("Player.png");
        characterImage = characterIcon.getImage();

        ImageIcon enemyIcon = new ImageIcon("Enemy.png");
        enemyImage = enemyIcon.getImage();

        ImageIcon fierceEnemyIcon = new ImageIcon("FierceEnemy.png");
        fierceEnemyImage = fierceEnemyIcon.getImage();
    }

    private void spawnEnemies(int maxCount) {
        // Limit the number of enemies to 20
        if (enemies.size() >= 20) {
            return; // Prevent spawning more enemies if the limit is reached
        }

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

                    new Timer(random.nextInt(3000) + 2000, e ->
                            enemyProjectileManager.fireProjectileEnemy(enemy.getX() + enemy.getImage().getWidth(null) / 2,
                                    enemy.getY() + enemy.getImage().getHeight(null) / 2,
                                    characterX + characterImage.getWidth(null) / 2,
                                    characterY + characterImage.getHeight(null) / 2)
                    ).start();
                }
            }
        }
    }

    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            enemy.moveTowards(characterX, characterY);
        }

        // Check if a new fierce enemy needs to be spawned
        if (score >= lastFierceEnemyScore + 100 && (fierceEnemy == null || fierceEnemy.isDestroyed())) {
            lastFierceEnemyScore = score;
            fierceEnemy = new FierceEnemy(getWidth() / 2, getHeight() / 2, fierceEnemyImage);

            // Set up respawn timer if the fierce enemy was destroyed
            if (fierceEnemyRespawnTimer != null) {
                fierceEnemyRespawnTimer.stop();
            }
            fierceEnemyRespawnTimer = new Timer(10000, e -> {
                fierceEnemy = new FierceEnemy(getWidth() / 2, getHeight() / 2, fierceEnemyImage);
            });
            fierceEnemyRespawnTimer.setRepeats(false);
            fierceEnemyRespawnTimer.start();
        }

        if (fierceEnemy != null) {
            fierceEnemy.moveTowards(characterX, characterY);
            fierceEnemy.updateProjectiles();
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

                    // Increment kill count and update health if divisible by 2
                    killCount++;
                    if (killCount % 2 == 0) {
                        increaseHealth(5); // Increase health by 5% or your preferred amount
                    }
                    break;
                }
            }
        }

        List<Projectile> enemyProjectiles = enemyProjectileManager.getProjectiles();
        for (int i = enemyProjectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = enemyProjectiles.get(i);
            if (projectile.collidesWith(characterX, characterY, characterImage.getWidth(null), characterImage.getHeight(null))) {
                enemyProjectiles.remove(i);
                decreaseHealth(1 + random.nextInt(2)); // Decrease health by 1-2%
                System.out.println("Character hit!");
            }
        }

        if (fierceEnemy != null) {
            List<Projectile> fierceEnemyProjectiles = fierceEnemy.getProjectiles();
            for (int i = fierceEnemyProjectiles.size() - 1; i >= 0; i--) {
                Projectile projectile = fierceEnemyProjectiles.get(i);
                if (projectile.collidesWith(characterX, characterY, characterImage.getWidth(null), characterImage.getHeight(null))) {
                    fierceEnemy.getProjectiles().remove(i);
                    decreaseHealth(2 + random.nextInt(2)); // Decrease health by 2-3%
                    System.out.println("Character hit by fierce enemy projectile!");
                }
            }

            // Check for collisions between player projectiles and fierce enemy
            for (int i = projectiles.size() - 1; i >= 0; i--) {
                Projectile projectile = projectiles.get(i);
                if (projectile.collidesWith(fierceEnemy.getX(), fierceEnemy.getY(),
                        fierceEnemy.getImage().getWidth(null), fierceEnemy.getImage().getHeight(null))) {
                    projectiles.remove(i);
                    fierceEnemy.hit();
                    if (fierceEnemy.isDestroyed()) {
                        fierceEnemy = null; // Remove the fierce enemy
                        score += 30; // Add bonus points for destroying the fierce enemy
                        increaseHealth(10); // Increase health by 10% when the fierce enemy is destroyed
                    }
                    break;
                }
            }
        }
    }

    private void decreaseHealth(int amount) {
        if (!gameOver) {
            playerHealth -= amount;
            if (playerHealth < 0) {
                playerHealth = 0;
                gameOver = true;
                timer.stop();
                if (fierceEnemyRespawnTimer != null) {
                    fierceEnemyRespawnTimer.stop();
                }
                enemyRespawnTimer.stop();
                survivalTimeTimer.stop();
                System.out.println("Game Over!");
                updateSurvivalTime();
                repaint();
            }
        }
    }

    private void increaseHealth(double percentage) {
        double increaseAmount = 100.0 * percentage / 100.0;
        playerHealth += increaseAmount;
        if (playerHealth > 100.0) {
            playerHealth = 100.0;
        }
    }

    private void updateSurvivalTime() {
        if (!gameOver) {
            survivalTime++;
        }
    }


    private void fireFierceEnemyProjectiles() {
        Timer projectileTimer = new Timer(1000, e -> {
            if (fierceEnemy == null) {
                ((Timer) e.getSource()).stop();
                return;
            }

            int startX = fierceEnemy.getX() + fierceEnemy.getImage().getWidth(null) / 2;
            int startY = fierceEnemy.getY() + fierceEnemy.getImage().getHeight(null) / 2;
            int dx = characterX - startX;
            int dy = characterY - startY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            dx = (int) (dx / distance * 5);
            dy = (int) (dy / distance * 5);

            Projectile projectile = new Projectile(startX, startY, dx, dy, 15, 15);
            projectile.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            fierceEnemy.getProjectiles().add(projectile);
        });
        projectileTimer.start();
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
        if (fierceEnemy != null) {
            g2d.drawImage(fierceEnemy.getImage(), fierceEnemy.getX(), fierceEnemy.getY(), this);
        }
        // Draw projectiles
        g2d.setColor(Color.YELLOW);
        projectileManager.getProjectiles().forEach(p -> g2d.fillOval(p.getX(), p.getY(), p.getWidth(), p.getHeight()));

        g2d.setColor(Color.RED);
        enemyProjectileManager.getProjectiles().forEach(p -> g2d.fillOval(p.getX(), p.getY(), p.getWidth(), p.getHeight()));

        if (fierceEnemy != null) {
            g2d.setColor(Color.MAGENTA);
            fierceEnemy.getProjectiles().forEach(p -> g2d.fillOval(p.getX(), p.getY(), p.getWidth(), p.getHeight()));
        }

        g2d.setFont(new Font("Verdana", Font.BOLD | Font.ITALIC, 18));
        g2d.setColor(Color.BLACK); // Shadow color
        g2d.drawString("Score: " + score, 12, 22);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Score: " + score, 10, 20);

        // Draw LifeBar
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Courier New", Font.BOLD | Font.ITALIC, 14));
        g2d.drawString("LifeBar", getWidth() - 100, 40);

        // Draw background of LifeBar
        g2d.setColor(Color.GRAY);
        g2d.fillRect(getWidth() - 120, 10, 100, 20);

        // Draw foreground of LifeBar
        g2d.setColor(Color.GREEN);
        int healthWidth = (int) (playerHealth);
        g2d.fillRect(getWidth() - 120, 10, healthWidth, 20);

        // Draw border around LifeBar
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(getWidth() - 120, 10, 100, 20);

        // Draw LifeBar percentage
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString(String.format("%.1f%%", playerHealth), getWidth() - 75, 25);

        // Draw survival time
        g2d.setFont(new Font("Courier New", Font.BOLD | Font.ITALIC, 15));
        g2d.setColor(Color.BLACK); // Shadow color
        g2d.drawString("Survival Time: " + survivalTime + "s", 12, 42);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Survival Time: " + survivalTime + "s", 10, 40);

        // Draw GAME OVER
        if (gameOver) {
            // Draw GAME OVER
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.setColor(Color.WHITE); // Shadow color
            g2d.drawString("GAME OVER!", getWidth() / 2 - 152, getHeight() / 2 - 22);
            g2d.setColor(Color.RED); // Text color
            g2d.drawString("GAME OVER!", getWidth() / 2 - 150, getHeight() / 2 - 20);

            // Draw Survival Time
            g2d.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
            g2d.setColor(Color.WHITE); // Shadow color
            g2d.drawString("Survival Time: " + survivalTime + " seconds", getWidth() / 2 - 128, getHeight() / 2 + 25);
            g2d.setColor(Color.BLACK); // Text color
            g2d.drawString("Survival Time: " + survivalTime + " seconds", getWidth() / 2 - 130, getHeight() / 2 + 27);
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

    private class FierceEnemy {
        private int x, y;
        private Image image;
        private List<Projectile> projectiles;
        private Random random;
        private int hitPoints = 10;

        public FierceEnemy(int x, int y, Image image) {
            this.x = x;
            this.y = y;
            this.image = image;
            this.projectiles = new ArrayList<>();
            this.random = new Random();
            fireFierceEnemyProjectiles();
        }

        private void fireFierceEnemyProjectiles() {
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

        public void hit() {
            hitPoints--;
        }

        public boolean isDestroyed() {
            return hitPoints <= 0;
        }
    }
}