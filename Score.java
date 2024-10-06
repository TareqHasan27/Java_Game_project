package game;

public class Score {
    private int value;

    public Score() {
        this.value = 0;
    }

    public int getValue() {
        return value;
    }

    public void increase(int amount) {
        value += amount;
    }

    public void reset() {
        value = 0;
    }

    // Method to increase health based on the number of kills
    public void increaseHealthByKill(Player player) {
        if (value > 0 && value % 6 == 0) { // Every 6 kills
            player.increaseHealth(2); // Increase health by 2
        }
    }

    // Method to decrease health based on hits
    public void decreaseHealthByHit(Player player) {
        if (value > 0 && value % 7 == 0) { // Every 7 hits
            player.decreaseHealth(2); // Decrease health by 2
        }
    }

    // Method to give bonus health for killing a NewEnemy
    public void bonusHealthForKillingNewEnemy(Player player) {
        player.increaseHealth(5); // Give bonus health
    }
}
