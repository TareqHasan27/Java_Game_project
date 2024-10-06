package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

public class Projectile {
    private int x, y; // Position of the projectile
    private int dx, dy; // Direction of the projectile
    private int radius; // Radius of the projectile
    private Color color; // Color of the projectile

    public Projectile(int x, int y, int dx, int dy, int radius, Color color) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.radius = radius; // Initialize the radius
        this.color = color; // Initialize the color
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public void draw(Graphics g) {
        g.setColor(color); // Use the projectile's color
        g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
    }

    public boolean isOutOfBounds(int width, int height) {
        return x < -radius || x > width + radius || y < -radius || y > height + radius;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRadius() {
        return radius;
    }

    public Rectangle getBounds() {
        return new Rectangle(x - radius, y - radius, 2 * radius, 2 * radius);
    }
}
