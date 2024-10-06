package game;

import java.util.List;
import java.util.Iterator;

public class CollisionDetector {

    public static void checkCollisions(List<Projectile> projectiles, List<Enemy> enemies, Score score, EnemyManager enemyManager, int screenWidth, int screenHeight, Player player, WarAreaPanel panel) {
        Iterator<Projectile> projectileIterator = projectiles.iterator();

        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();

            Iterator<Enemy> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();

                if (projectile.getBounds().intersects(enemy.getBounds())) {
                    // Collision detected
                    projectileIterator.remove(); // Remove the projectile

                    if (enemy instanceof NewEnemy) {
                        // NewEnemy logic
                        ((NewEnemy) enemy).hit();
                        score.increase(30);
                        System.out.println("NewEnemy hit! Score increased by 30.");

                        if (((NewEnemy) enemy).isDead()) {
                            System.out.println("NewEnemy defeated!");
                            // Restore 5% health when NewEnemy is defeated
                            int healthRestorePercentage = 7; // Set the health restoration percentage
                            player.increaseHealthByPercentage(healthRestorePercentage); // Call method to increase health
                            enemyManager.removeEnemy(enemy, screenWidth, screenHeight);
                        }
                    }
                    else {
                        // Normal enemy logic
                        score.increase(10);
                        enemyManager.removeEnemy(enemy, screenWidth, screenHeight);
                        panel.enemyKilled(); // Increment the enemy killed counter
                    }

                    break;
                }
            }
        }

        // Check if player is hit by an enemy
        for (Enemy enemy : enemies) {
            if (enemy.getBounds().intersects(player.getBounds())) {
                player.playerHitByBullet(); // Use the new method to handle damage
                score.decreaseHealthByHit(player);
                System.out.println("Player hit! Current health: " + player.getHealth());
            }
        }
    }
}
