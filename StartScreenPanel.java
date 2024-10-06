package game;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class StartScreenPanel extends JPanel {
    private Image backgroundImage; // Dynamic background image
    private Image playerImage;     // Player image
    private Image enemyImage;
    private Image newEnemyImage;
    private Image introImage;
    private String[] quotes = {    // Survival quotes
            "Survival is not about being fearless; it's about facing your fears.",
            "The rules of survival never change, whether you're in a desert or in an arena.",
            "Stay alive, no matter what occurs.",
            "In the midst of chaos, there is also opportunity.",
            "You only get one chance at life and you have to grab it boldly."
    };
    private String[] gameDetails = { // Game details in simple terms
            "Game Mode: Survival - You must survive as long as possible.",
            "Enemies: 5 types - Each enemy has abilities to kill you.",
            "Objective: Stay alive and defeat as many enemies as you can."
    };

    private Timer timer; // Timer for animation
    private boolean showPlayerImage = true; // Control for showing player image
    private boolean showQuotes = false;  // Control when to show quotes
    private boolean showGameDetails = false;  // Control when to show game details
    private boolean showGameControls = false; // Control when to show game buttons
    private JButton nextButton, playButton, musicOnButton, musicOffButton, backButton;
    private boolean musicOn = false; // Music status
    private Clip backgroundMusicClip; // Clip for playing background music

    public StartScreenPanel(SurvivorGame game) {
        setLayout(null); // Set layout to null for absolute positioning
        setBackground(Color.BLACK);
        loadImages();
        loadBackgroundMusic(); // Load the background music
        setFocusable(true);
        createButtons(game);
        startBackgroundAnimation(); // Start background animation
    }

    private void loadImages() {
        backgroundImage = new ImageIcon("src/game/picture/IntroBackground.png").getImage(); // Load your dynamic background image
        playerImage = new ImageIcon("src/game/picture/Player.png").getImage(); // Load your player image
        enemyImage = new ImageIcon("src/game/picture/Enemyintro.png").getImage();
        newEnemyImage = new ImageIcon("src/game/picture/NewEnemyintro.png").getImage();
        introImage = new ImageIcon("src/game/picture/introline-removebg.png").getImage();
    }

    // Load the background music
    private void loadBackgroundMusic() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("src/game/music/corsairs-studiokolomna-main-version-23542-02-33.wav"));
            backgroundMusicClip = AudioSystem.getClip();
            backgroundMusicClip.open(audioInputStream);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    // Start playing the background music
    private void playBackgroundMusic() {
        if (backgroundMusicClip != null) {
            backgroundMusicClip.setFramePosition(0); // Start from the beginning
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Play continuously
            backgroundMusicClip.start();
        }
    }

    // Stop the background music
    private void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
        }
    }

    // Create all necessary buttons
    private void createButtons(SurvivorGame game) {
        nextButton = new JButton(); // Create an empty button
        ImageIcon nextIcon = new ImageIcon("src/game/picture/next.png"); // Load the next icon image
        nextButton.setIcon(nextIcon); // Set the icon on the button
        nextButton.setFocusPainted(false);
        nextButton.setContentAreaFilled(false);
        nextButton.setBorderPainted(false);
        nextButton.setBounds(350, 500, 100, 50); // Set position and size for nextButton
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPlayerImage) {
                    showPlayerImage = false; // First click: Hide player image
                    showQuotes = true; // Show survival quotes
                } else if (showQuotes) {
                    showQuotes = false; // Second click: Hide quotes
                    showGameDetails = true; // Show game details
                } else if (showGameDetails) {
                    // Third click: Show game control buttons
                    showGameControls = true; // Enable controls
                    showGameDetails = false; // Hide game details
                    toggleGameControls(true); // Show Play and Music buttons
                }
                repaint(); // Trigger re-rendering
            }
        });

        // Create Play button
        playButton = new JButton("Play");
        playButton.setFont(new Font("Arial", Font.BOLD, 10)); // Reduced font size
        playButton.setBounds(350, 100, 100, 40); // Set position and size for playButton
        playButton.addActionListener(e -> {
            if (musicOn) {
                playBackgroundMusic(); // Start the background music
                System.out.println("Background music is playing...");
            } else {
                stopBackgroundMusic(); // Ensure music is off
                System.out.println("Music is off...");
            }
            game.startGame(); // Transition to the game panel (WarAreaPanel)
        });

        // Create Music On button
        musicOnButton = new JButton("Music On");
        musicOnButton.setFont(new Font("Arial", Font.BOLD, 10)); // Reduced font size
        musicOnButton.setBounds(350, 200, 100, 40); // Set position and size for musicOnButton
        musicOnButton.addActionListener(e -> {
            musicOn = true; // Set music status to on
            //  playBackgroundMusic(); // Start music immediately
            System.out.println("Music turned on.");
        });

        // Create Music Off button
        musicOffButton = new JButton("Music Off");
        musicOffButton.setFont(new Font("Arial", Font.BOLD, 10)); // Reduced font size
        musicOffButton.setBounds(350, 300, 100, 40); // Set position and size for musicOffButton
        musicOffButton.addActionListener(e -> {
            musicOn = false; // Set music status to off
            stopBackgroundMusic(); // Stop the music immediately
            System.out.println("Music turned off.");
        });

        // Create Exit button
        backButton = new JButton("Exit");
        backButton.setFont(new Font("Arial", Font.BOLD, 10)); // Reduced font size
        backButton.setBounds(350, 400, 100, 40); // Set position and size for backButton
        backButton.addActionListener(e -> {
            // Close the application when the Exit button is clicked
            System.exit(0);
        });

        // Add the next button to the panel initially
        add(nextButton);
    }

    // Toggle the visibility of game control buttons
    private void toggleGameControls(boolean isVisible) {
        if (isVisible) {
            // Clear existing components and add the new buttons
            removeAll(); // Clear the panel
            add(playButton);
            add(musicOnButton);
            add(musicOffButton);
            add(backButton);
            add(nextButton); // Keep the next button
        } else {
            remove(playButton);
            remove(musicOnButton);
            remove(musicOffButton);
            remove(backButton);
        }
        revalidate();
        repaint();
    }

    private void startBackgroundAnimation() {
        // Set up a timer for any dynamic animation effects
        timer = new Timer(100, e -> repaint());
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this); // Draw background

        if (showPlayerImage) {
            // Initially, show the player image
            g.drawImage(introImage, 100, 50, this);
            g.drawImage(playerImage, 340, 350, this); // Draw player image at a specific location
            g.drawImage(enemyImage, 200, 450, this);
            g.drawImage(enemyImage, 210, 340, this);
            g.drawImage(enemyImage, 500, 320, this);
            g.drawImage(newEnemyImage, 350, 300, this);
            g.drawImage(newEnemyImage, 680, 450, this);
        }

        if (showQuotes) {
            // First click: Show the survival quotes
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 19));

            int quotesYPosition = 200; // Position the quotes lower on the screen
            for (int i = 0; i < quotes.length; i++) {
                g.drawString(quotes[i], 60, quotesYPosition + i * 30);
            }
        }

        if (showGameDetails) {
            // Second click: Show the game details
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 20));

            int detailsYPosition = 200; // Position game details lower
            for (int i = 0; i < gameDetails.length; i++) {
                g.drawString(gameDetails[i], 100, detailsYPosition + i * 30);
            }
        }

        // The game controls will appear in the same background without a new panel
        if (showGameControls) {
            // Hide both the game details and quotes when showing the buttons
            g.setColor(Color.YELLOW);
            g.drawString("Press the buttons below:", 340, 50); // Example instruction line
        }
    }
}
