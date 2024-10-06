
package game;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Enemy {
    private Image image;
    private int x, y;
    private int health; // Health of the enemy
    protected List<Projectile> bullets = new ArrayList<>(); // Store enemy bullets
    private long lastShotTime = 0; // Time of the last shot
    private static final int SHOOT_INTERVAL = 2000; // Firing Interval
    private static final int MOVE_SPEED = 2; // Speed at which enemy moves towards player
    private int moveSpeed;
    public Enemy(String imagePath, int x, int y) {
        this.x = x;
        this.y = y;
        this.image = new ImageIcon(imagePath).getImage();
        this.health = 100;
        this.moveSpeed = 1;
    }

    protected Image getImage() {
        return image; // Provide access to the enemy's image
    }

    public void moveTowards(int playerX, int playerY) {
        // Calculate direction towards the player
        int dx = playerX - x;
        int dy = playerY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Normalize the direction and move towards the player
        if (distance > 0) {
            x += (int) (MOVE_SPEED * (dx / distance)); // Move x towards player
            y += (int) (MOVE_SPEED * (dy / distance)); // Move y towards player
        }
    }

    protected void fireBullet(int playerX, int playerY) {
        // Calculate direction towards the player
        int dx = playerX - (this.x + image.getWidth(null) / 2);
        int dy = playerY - (this.y + image.getHeight(null) / 2);
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Prevent division by zero
        if (distance != 0) {
            dx = (int) (5 * (dx / distance)); // Bullet speed
            dy = (int) (5 * (dy / distance));

            // Create a projectile representing the enemy bullet
            bullets.add(new Projectile(this.x + image.getWidth(null) / 2, this.y + image.getHeight(null) / 2, dx, dy, 5, Color.RED));
        }
    }

    public void draw(Graphics g, int playerX, int playerY) {
        g.drawImage(image, x, y, null);
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime > SHOOT_INTERVAL) {
            fireBullet(playerX, playerY); // Call fireBullet with player position
            lastShotTime = currentTime;
        }

        // Update bullets
        updateBullets(g);
    }

    private void updateBullets(Graphics g) {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Projectile bullet = bullets.get(i);
            bullet.update();
            bullet.draw(g); // Draw each bullet (assuming you have a draw method in Projectile)
            if (bullet.isOutOfBounds(800, 600)) { // Replace with your screen width/height
                bullets.remove(i);
            }
        }
    }

    public void reduceHealth(int amount) {
        health -= amount;
        if (health <= 0) {
            // Handle enemy death (e.g., remove from game)
            System.out.println("Enemy destroyed");
            // You might want to add logic here to remove this enemy instance from the game
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, image.getWidth(null), image.getHeight(null));
    }

    public List<Projectile> getBullets() {
        return bullets;
    }
}
