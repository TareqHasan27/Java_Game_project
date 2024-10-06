
package game;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class NewEnemy extends Enemy {
    private Random random = new Random();

    private int hitCounter; // Counter for how many times this enemy has been hit
    private int deathThreshold; // Random threshold for how many hits to destroy this enemy
    private double health; // Health percentage (0 to 100)

    public NewEnemy(String imagePath, int x, int y) {
        super(imagePath, x, y); // Call the constructor of Enemy
        this.hitCounter = 0; // Initialize hit counter
        this.health = 100.0; // Initialize health to 100%
        this.deathThreshold = 5 + random.nextInt(3); // Randomly set death threshold between 5 and 7
    }

    @Override
    protected void fireBullet(int playerX, int playerY) {
        // Calculate direction towards the player
        int dx = playerX - (getX() + getImageWidth() / 2);
        int dy = playerY - (getY() + getImageHeight() / 2);
        double distance = Math.sqrt(dx * dx + dy * dy);
        dx = (int) (5 * (dx / distance)); // Bullet speed
        dy = (int) (5 * (dy / distance));

        // Fire projectile with random color for NewEnemy
        bullets.add(new Projectile(getX() + getImageWidth() / 2, getY() + getImageHeight() / 2, dx, dy, 8, getRandomColor()));
    }

    public void hit() {
        hitCounter++; // Increment the hit counter
        System.out.println("NewEnemy hit! Total hits: " + hitCounter);

        // Decrease health by 2-3% for every 4-5 hits
        if (hitCounter % 4 == 0) {
            health -= (2 + random.nextInt(2)); // Decrease by 2 or 3 percent
            System.out.println("NewEnemy health decreased to: " + health + "%");
        }
    }

    public boolean isDead() {
        return hitCounter >= deathThreshold; // Check against the death threshold
    }

    // Method to increase health when a normal enemy is killed
    public void increaseHealth() {
        health += (2 + random.nextInt(2)); // Increase health by 2 or 3 percent
        if (health > 100) {
            health = 100; // Cap health at 100%
        }
        System.out.println("NewEnemy health increased to: " + health + "%");
    }

    // Bonus health when NewEnemy is killed
    public void giveBonusHealth() {
        health += 5; // Increase health by 5%
        if (health > 100) {
            health = 100; // Cap health at 100%
        }
        System.out.println("NewEnemy bonus health increased to: " + health + "%");
    }

    public double getHealth() {
        return health; // Return current health
    }

    // Method to draw the health bar
    public void drawHealthBar(Graphics g) {
        int barWidth = 50; // Width of health bar
        int barHeight = 5; // Height of health bar
        int x = getX(); // X position of the enemy
        int y = getY() - 10; // Y position above the enemy

        // Draw the health bar background
        g.setColor(Color.RED);
        g.fillRect(x, y, barWidth, barHeight);

        // Draw the current health
        g.setColor(Color.GREEN);
        int currentBarWidth = (int) (barWidth * (health / 100.0));
        g.fillRect(x, y, currentBarWidth, barHeight);
    }

    private Color getRandomColor() {
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)); // Random color
    }

    private int getImageWidth() {
        return getImage().getWidth(null); // Get width of the enemy's image
    }

    private int getImageHeight() {
        return getImage().getHeight(null); // Get height of the enemy's image
    }
}
