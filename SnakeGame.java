import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    int boardWidth;
    int boardHeight;
    int TileSize = 25;

    Tile snakeHead;
    ArrayList<Tile> snakeBody;
    Tile food;
    Random random;
    Timer gameLoop;
    int velocityX;
    int velocityY;
    boolean gameOver = false;
    String playerName = "Player";
    String playerGender = "Not Specified";
    int playerAge = 0;
    Image backgroundImage;

    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        try {
            backgroundImage = new ImageIcon(getClass().getResource("snake.jpg")).getImage();
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }

        getPlayerDetails();
        startNewGame();
    }

    private void getPlayerDetails() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2));

        JTextField nameField = new JTextField();
        JTextField ageField = new JTextField();
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});

        panel.add(new JLabel("Enter your name:"));
        panel.add(nameField);
        panel.add(new JLabel("Enter your age:"));
        panel.add(ageField);
        panel.add(new JLabel("Select your gender:"));
        panel.add(genderBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Player Details", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            playerName = nameField.getText();
            playerAge = Integer.parseInt(ageField.getText());
            playerGender = (String) genderBox.getSelectedItem();
        }

        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        }
        if (playerAge < 0) {
            playerAge = 0;
        }
        if (playerGender == null) {
            playerGender = "Not Specified";
        }
    }

    private void startNewGame() {
        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<>();

        food = new Tile(10, 10);
        random = new Random();
        placeFood();

        velocityX = 0;
        velocityY = 0;
        gameOver = false;

        if (gameLoop != null) {
            gameLoop.stop();
        }
        gameLoop = new Timer(90, this);
        gameLoop.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
        draw(g);
    }

    private void drawBackground(Graphics g) {
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(food.x * TileSize, food.y * TileSize, TileSize, TileSize);

        g.setColor(Color.BLUE);
        g.fillRect(snakeHead.x * TileSize, snakeHead.y * TileSize, TileSize, TileSize);

        g.setColor(Color.GREEN);
        for (Tile snakePart : snakeBody) {
            g.fillRect(snakePart.x * TileSize, snakePart.y * TileSize, TileSize, TileSize);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        if (gameOver) {
            g.setColor(Color.RED);
            g.drawString("Game Over...Try Next Time, " + playerName + "! Your Score is: " + snakeBody.size(), 10, 20);
        } else {
            g.setColor(Color.WHITE);
            g.drawString("Score: " + snakeBody.size(), 10, 20);
        }
    }

    public void placeFood() {
        food.x = random.nextInt(boardWidth / TileSize);
        food.y = random.nextInt(boardHeight / TileSize);
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    public void move() {
        if (collision(snakeHead, food)) {
            snakeBody.add(new Tile(food.x, food.y));
            placeFood();
        }

        for (int i = snakeBody.size() - 1; i >= 0; i--) {
            Tile snakePart = snakeBody.get(i);
            if (i == 0) {
                snakePart.x = snakeHead.x;
                snakePart.y = snakeHead.y;
            } else {
                Tile prevSnakePart = snakeBody.get(i - 1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }

        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        for (Tile snakePart : snakeBody) {
            if (collision(snakeHead, snakePart)) {
                gameOver = true;
                showGameOverDialog();
                return;
            }
        }

        if (snakeHead.x < 0) {
            snakeHead.x = (boardWidth / TileSize) - 1;
        } else if (snakeHead.x >= boardWidth / TileSize) {
            snakeHead.x = 0;
        }

        if (snakeHead.y < 0) {
            snakeHead.y = (boardHeight / TileSize) - 1;
        } else if (snakeHead.y >= boardHeight / TileSize) {
            snakeHead.y = 0;
        }
    }

    private void showGameOverDialog() {
        JDialog dialog = new JDialog((Frame) null, "Game Over", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(135, 206, 250));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel message = new JLabel("Game Over, ");
        message.setAlignmentX(Component.CENTER_ALIGNMENT);
        message.setFont(new Font("Arial", Font.PLAIN, 18));

        JLabel details = new JLabel("Name: " + playerName + " | Gender: " + playerGender + " | Age: " + playerAge);
        details.setAlignmentX(Component.CENTER_ALIGNMENT);
        details.setFont(new Font("Arial", Font.PLAIN, 16));

        JLabel score = new JLabel("Your Score is: " + snakeBody.size());
        score.setAlignmentX(Component.CENTER_ALIGNMENT);
        score.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton playAgainButton = new JButton("Play Again");
        playAgainButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgainButton.addActionListener(e -> {
            dialog.dispose();
            startNewGame();
        });

        JButton exitButton = new JButton("Exit");
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(message);
        panel.add(details);
        panel.add(score);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(playAgainButton);
        panel.add(exitButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) {
            velocityX = 1;
            velocityY = 0;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
