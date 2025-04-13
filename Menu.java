import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;

import static java.lang.System.out;

public class Menu {
    private JFrame frame;
    public Color gameColor;
    public Color pathColor;
    private Engine engine;

    public Menu() {
        JFrame myFrame = new JFrame("PathMaster3000 - Menu");
        myFrame.setSize(300, 450);
        myFrame.setLocationRelativeTo(null);
        myFrame.setLayout(null);
        myFrame.setResizable(false);
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Создание метки с текстом
        JLabel gameName = new JLabel("PathMaster3000 Menu");
        gameName.setFont(new Font("Arial", Font.BOLD, 18));
        gameName.setBounds(10, 10, 280, 30);
        myFrame.add(gameName);

        // Поле для ввода размера сетки
        JLabel gridLabel = new JLabel("Grid Size (n x n):");
        gridLabel.setBounds(10, 60, 150, 20);
        myFrame.add(gridLabel);

        JTextField gridSizeInput = new JTextField();
        gridSizeInput.setBounds(160, 60, 50, 25);
        myFrame.add(gridSizeInput);

        // Настройка внешнего вида
        JLabel appearanceLabel = new JLabel("Customize colors:");
        appearanceLabel.setBounds(10, 100, 150, 20);
        myFrame.add(appearanceLabel);

        JButton gameColorButton = new JButton("Game color");
        gameColorButton.setBounds(10, 130, 130, 30);
        myFrame.add(gameColorButton);

        JButton pathColorButton = new JButton("Path color");
        pathColorButton.setBounds(150, 130, 130, 30);
        myFrame.add(pathColorButton);

        // Кнопки управления
        JButton startButton = new JButton("Start game");
        startButton.setBounds(10, 180, 270, 30);
        myFrame.add(startButton);

        JButton loadButton = new JButton("Load game");
        loadButton.setBounds(10, 220, 270, 30);
        myFrame.add(loadButton);

        JButton rulesButton = new JButton("Game Rules");
        rulesButton.setBounds(10, 260, 270, 30);
        myFrame.add(rulesButton);

        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(10, 300, 270, 30);
        myFrame.add(exitButton);

        gameColorButton.addActionListener(e -> {
            gameColor = JColorChooser.showDialog(myFrame, "Choose game color", Color.LIGHT_GRAY);
            if (gameColor != null) {
                out.println("Selected game color: " + gameColor);
            }
        });

        pathColorButton.addActionListener(e -> {
            pathColor = JColorChooser.showDialog(myFrame, "Choose path color", Color.BLUE);
            if (pathColor != null) {
                out.println("Selected path color: " + pathColor);
            }
        });

        startButton.addActionListener(e -> {
            try {
                int gridSize = Integer.parseInt(gridSizeInput.getText());
                if (gridSize < 2) {
                    JOptionPane.showMessageDialog(myFrame, "Grid size must be at least 2x2.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                myFrame.dispose();
                new Engine(gridSize, pathColor, gameColor);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(myFrame, "Invalid input. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        exitButton.addActionListener(e -> System.exit(0));

        loadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text File", "txt");
            fileChooser.setFileFilter(filter);
            fileChooser.setDialogTitle("Load");
            int res = fileChooser.showOpenDialog(frame);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    int gridSize = Integer.parseInt(reader.readLine());
                    int totalPoints = Integer.parseInt(reader.readLine());
                    int moves = Integer.parseInt(reader.readLine());
                    int elapsedTime = Integer.parseInt(reader.readLine());

                    String[] start = reader.readLine().split(",");
                    Point startPoint = new Point(Integer.parseInt(start[0]), Integer.parseInt(start[1]));

                    String[] end = reader.readLine().split(",");
                    Point endPoint = new Point(Integer.parseInt(end[0]), Integer.parseInt(end[1]));

                    String[] current = reader.readLine().split(",");
                    Point currentPosition = new Point(Integer.parseInt(current[0]), Integer.parseInt(current[1]));

                    myFrame.dispose();
                    Engine loadedEngine = new Engine(gridSize, pathColor, gameColor);

                    for (int i = 0; i < gridSize; i++) {
                        for (int j = 0; j < gridSize; j++) {
                            String[] buttonData = reader.readLine().split(",");
                            String text = buttonData[0];
                            boolean enabled = buttonData[1].equals("1");

                            JButton button = loadedEngine.gridButtons[i][j];
                            button.setText(text);
                            button.setEnabled(enabled);
                            if (!enabled) {
                                button.setBackground(pathColor);
                            }
                        }
                    }

                    loadedEngine.totalPoints = totalPoints;
                    loadedEngine.moves = moves;
                    loadedEngine.elapsedTime = elapsedTime;
                    loadedEngine.currentPosition = currentPosition;
                    loadedEngine.startPoint = startPoint;
                    loadedEngine.endPoint = endPoint;

                    loadedEngine.scoreLabel.setText("Score: " + totalPoints / Math.max(1, moves));
                    loadedEngine.movesLabel.setText("Moves: " + moves);
                    loadedEngine.timerLabel.setText("Time: " + elapsedTime + "s");

                    loadedEngine.startGame();

                    JOptionPane.showMessageDialog(null, "Game loaded successfully!", "Load", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException | NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Error loading the game: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        rulesButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(myFrame, "Rules of the game:\n1. Navigate through the grid.\n2. Avoid obstacles.\n3. Reach the end point.\n4 If you want to load game, please use button " +
                    "choose select.", "Game Rules", JOptionPane.INFORMATION_MESSAGE);
        });

        myFrame.setVisible(true);
    }
}