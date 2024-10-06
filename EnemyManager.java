package game;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.ImageIcon;

public class EnemyManager {
    public static final int MAX_ENEMIES = 3; // Control maximum number of regular enemies
    private List<Enemy> enemies = new ArrayList<>();
    private int enemiesDefeated; // Counter for defeated enemies
    private Image newEnemyImage; // Image for the new enemy
    private boolean newEnemySpawned; // To track if the new enemy has been spawned

    public EnemyManager(int screenWidth, int screenHeight) {
        // Load new enemy image
        newEnemyImage = new ImageIcon("src/game/picture/NewEnemywar.png").getImage();

        // Create initial enemies
        for (int i = 0; i < MAX_ENEMIES; i++) {
            spawnEnemy(screenWidth, screenHeight);
        }
        enemiesDefeated = 0; // Initialize defeated enemies count
        newEnemySpawned = false; // Ensure new enemy hasn't spawned yet
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void updateEnemies(int playerX, int playerY) {
        // Move each enemy towards the player
        for (Enemy enemy : enemies) {
            enemy.moveTowards(playerX, playerY);
        }
    }

    public void removeEnemy(Enemy enemy, int screenWidth, int screenHeight) {
        enemies.remove(enemy);
        enemiesDefeated++; // Increment the defeated enemies count

        // Check if we should spawn a new enemy
        if (enemiesDefeated % 10 == 0) {
            spawnNewEnemy(screenWidth, screenHeight); // Spawn new enemy every 10 kills
            newEnemySpawned = false; // Allow for the new enemy to be spawned again after another 10 kills
        } else {
            spawnEnemy(screenWidth, screenHeight); // Spawn a new enemy to maintain the count
        }
    }

    public void spawnEnemy(int screenWidth, int screenHeight) {
        if (screenWidth > 0 && screenHeight > 0 && enemies.size() < MAX_ENEMIES) {
            Random rand = new Random();
            int x = rand.nextInt(screenWidth);
            int y = rand.nextInt(screenHeight);
            enemies.add(new Enemy("src/game/picture/Enemy.png", x, y));
        }
    }

    private void spawnNewEnemy(int screenWidth, int screenHeight) {
        Random rand = new Random();
        int x = rand.nextInt(screenWidth);
        int y = rand.nextInt(screenHeight);

        // Only spawn if you are not exceeding the limit
        if (enemies.size() < MAX_ENEMIES + 1) { // Allow for one additional enemy
            enemies.add(new NewEnemy("src/game/picture/NewEnemywar.png", x, y)); // Use NewEnemy class
            newEnemySpawned = true; // Mark that the new enemy has been spawned
        }
    }

    // Update this method to accept player coordinates
    public void draw(Graphics g, int playerX, int playerY) {
        for (Enemy enemy : enemies) {
            enemy.draw(g, playerX, playerY); // Pass player's coordinates to each enemy
            // Draw bullets fired by enemies if necessary
            for (Projectile bullet : enemy.getBullets()) {
                bullet.draw(g);
            }
        }
    }
}
