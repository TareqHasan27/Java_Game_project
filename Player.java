package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

public class Player {
    private int x, y;
    private int health;
    private final int maxHealth;
    private Image image;

    // New fields to track bullet hits
    private int bulletsHitCount = 0;  // Track how many bullets hit the player
    private final int hitsForDamage = 7;  // Number of hits needed to decrease health

    public Player(int x, int y, String imagePath) {
        this.x = x;
        this.y = y;
        this.maxHealth = 100;  // Set max health
        this.health = maxHealth; // Initialize health to max health
        this.image = new ImageIcon(imagePath).getImage();
    }

    // Method to increase health by a percentage
    public void increaseHealthByPercentage(int percentage) {
        int healthToRestore = (maxHealth * percentage) / 100; // Calculate health to restore
        increaseHealth(healthToRestore); // Use existing increaseHealth method
    }

    // Set health method that ensures health is within valid bounds
    public void setHealth(int health) {
        this.health = Math.min(maxHealth, Math.max(0, health)); // Ensure health is within bounds
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, null); // Draw player image
    }

    public void fireProjectile(int targetX, int targetY, ProjectileManager projectileManager) {
        int dx = targetX - (this.x + getWidth() / 2);
        int dy = targetY - (this.y + getHeight() / 2);
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            dx = (int) (5 * (dx / distance)); // Normalize and set speed
            dy = (int) (5 * (dy / distance));
        }

        projectileManager.fireProjectile(this.x + getWidth() / 2, this.y + getHeight() / 2, dx, dy, Color.YELLOW); // Fire projectile
    }

    public int getX() {
        return x; // Get player X coordinate
    }

    public int getY() {
        return y; // Get player Y coordinate
    }

    public void setPosition(int x, int y, int screenWidth, int screenHeight) {
        int width = getWidth();
        int height = getHeight();

        if (x < 0) x = 0; // Prevent going off-screen
        if (y < 0) y = 0;
        if (x + width > screenWidth) x = screenWidth - width;
        if (y + height > screenHeight) y = screenHeight - height;

        this.x = x; // Set position
        this.y = y;
    }

    public int getWidth() {
        return image.getWidth(null); // Get player width
    }

    public int getHeight() {
        return image.getHeight(null); // Get player height
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, getWidth(), getHeight()); // Get bounding box
    }

    public int getHealth() {
        return health; // Get current health
    }

    public int getMaxHealth() {
        return maxHealth; // Get max health
    }

    // Decrease health by a specified amount
    public void decreaseHealth(int amount) {
        health = Math.max(0, health - amount); // Decrease health
    }

    // Increase health by a specified amount
    public void increaseHealth(int amount) {
        health = Math.min(maxHealth, health + amount); // Increase health
    }

    public boolean isAlive() {
        return health > 0; // Check if alive
    }

    // Method to handle bullet hits
    public void playerHitByBullet() {
        bulletsHitCount++;  // Increment the bullet hit counter

        if (bulletsHitCount >= hitsForDamage) {
            double damagePercentage = 0.025;  // 2.5% damage after 6-7 hits
            decreaseHealth((int) (maxHealth * damagePercentage));  // Decrease health
            bulletsHitCount = 0;  // Reset the hit counter
        }
    }
}
