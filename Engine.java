import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Random;

public class Engine implements ActionListener {
    public boolean isLoading;
    public int gridSize;
    public int totalPoints;
    public int moves;
    public Color pathColor;
    public Color gameColor;
    public JButton[][] gridButtons;
    public Point startPoint;
    public Point endPoint;
    public JFrame gameFrame;
    public JLabel scoreLabel;
    public JLabel movesLabel;
    public JLabel timerLabel;
    public Timer gameTimer;
    public int elapsedTime;
    public Point currentPosition;
    public boolean manualSelection;
    public boolean gameStarted;
    private boolean hintsEnabled;

    public Engine(int gridSize, Color pathColor, Color gameColor) {
        this.gridSize = gridSize;
        this.pathColor = pathColor;
        this.gameColor = gameColor;
        this.totalPoints = 0;
        this.moves = 0;
        this.elapsedTime = 0;
        this.gridButtons = new JButton[gridSize][gridSize];
        this.gameStarted = false;
        this.isLoading = false;
        this.hintsEnabled = false;
        initializeGame();
    }

    private void initializeGame() {
        if (!isLoading) { // Пропустить выбор режима, если игра загружается
            String[] options = {"Random Start/End", "Choose Start/End"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Choose game mode:",
                    "Game Mode Selection",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            manualSelection = choice == 1;
        }

        gameFrame = new JFrame("PathMaster3000");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(600, 750);
        gameFrame.setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(1, 3));
        scoreLabel = new JLabel("Score: " + totalPoints);
        movesLabel = new JLabel("Moves: " + moves);
        timerLabel = new JLabel("Time: 0s");
        infoPanel.add(scoreLabel);
        infoPanel.add(movesLabel);
        infoPanel.add(timerLabel);
        gameFrame.add(infoPanel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(gridSize, gridSize));
        Random random = new Random();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                JButton button = new JButton(String.valueOf(random.nextInt(10)));
                button.setBackground(gameColor);
                button.addActionListener(e -> {
                    if (manualSelection && !gameStarted && !isLoading) {
                        handleManualSelection((JButton) e.getSource());
                    } else {
                        actionPerformed(e);
                    }
                });
                gridButtons[i][j] = button;
                gridPanel.add(button);
            }
        }

        if (!isLoading && !manualSelection) {
            setRandomStartAndEnd(random);
            startGame();
        }

        gameFrame.add(gridPanel, BorderLayout.CENTER);
        addOptionsPanel();
        gameFrame.setVisible(true);
    }

    private void setRandomStartAndEnd(Random random) {
        startPoint = new Point(random.nextInt(gridSize), random.nextInt(gridSize));
        do {
            endPoint = new Point(random.nextInt(gridSize), random.nextInt(gridSize));
        } while (startPoint.equals(endPoint));

        setPoint(startPoint, "Start", Color.GREEN);
        setPoint(endPoint, "End", Color.RED);

        currentPosition = startPoint;


    }

    private void setPoint(Point point, String label, Color color) {
        JButton button = gridButtons[point.x][point.y];
        button.setText(label);
        button.setBackground(color);
        button.setEnabled(!label.equals("Start"));
    }

    private void handleManualSelection(JButton button) {
        Point position = getButtonPosition(button);
        if (startPoint == null) {
            startPoint = position;
            setPoint(startPoint, "Start", Color.GREEN);
            currentPosition = startPoint;
        } else if (endPoint == null) {
            if (!position.equals(startPoint)) {
                endPoint = position;
                setPoint(endPoint, "End", Color.RED);
                startGame();
            } else {
                JOptionPane.showMessageDialog(gameFrame, "End point cannot be the same as the start point.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        if (startPoint != null && endPoint != null) {
            JOptionPane.showMessageDialog(gameFrame, "Start and End set! Begin the game.", "Game Start", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void startGame() {
        gameStarted = true;
        startTimer();
        updateHints();
    }

    private void startTimer() {
        gameTimer = new Timer(1000, e -> {
            elapsedTime++;
            timerLabel.setText("Time: " + elapsedTime + "s");
        });
        gameTimer.start();
    }

    private void addOptionsPanel() {
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> resetGame());

        JButton resizeButton = new JButton("Change Grid Size");
        resizeButton.addActionListener(e -> {
            if (gameTimer != null) gameTimer.stop();

            String input = JOptionPane.showInputDialog(gameFrame, "Enter grid size");

            if (input != null && !input.isEmpty()) {
                try {
                    int newSize = Integer.parseInt(input);
                    if (newSize > 1) {
                        gridSize = newSize;
                        resetGame();
                    } else {
                        JOptionPane.showMessageDialog(gameFrame, "Grid size must be greater than 1", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(gameFrame, "Invalid input. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                if (gameTimer != null) gameTimer.start();
            }
        });

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveGame());

        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(e -> loadGame());

        JButton endButton = new JButton("End");
        endButton.addActionListener(e -> endGame());

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));

        JButton hintButton = new JButton("Hint");
        hintButton.addActionListener(e -> {
            hintsEnabled = !hintsEnabled; // Переключение состояния подсказок
            if (hintsEnabled) {
                updateHints(); // Включить подсказки
                hintButton.setText("Disable Hint");
            } else {
                // Отключить подсказки
                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        gridButtons[i][j].setBorder(null); // Сбросить границы
                    }
                }
                hintButton.setText("Hint");
            }
        });


        optionsPanel.add(hintButton);
        optionsPanel.add(restartButton);
        optionsPanel.add(resizeButton);
        optionsPanel.add(saveButton);
        optionsPanel.add(loadButton);
        optionsPanel.add(endButton);
        optionsPanel.add(exitButton);

        gameFrame.add(optionsPanel, BorderLayout.SOUTH);
    }

    private void endGame() {
        if (!gameStarted) {
            JOptionPane.showMessageDialog(gameFrame, "Game has not started yet!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (gameTimer != null) gameTimer.stop();

        JOptionPane.showMessageDialog(
                gameFrame,
                "Game Over!\nScore: " + totalPoints + "\nMoves: " + moves + "\nTime: " + elapsedTime + "s",
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE
        );

        resetGame();
    }

    private void saveGame() {
        if (gameTimer != null) gameTimer.stop();
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text File", "txt");
        fileChooser.setFileFilter(filter);
        fileChooser.setDialogTitle("Save");
        int res = fileChooser.showSaveDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(gridSize + "\n");
                writer.write(totalPoints + "\n");
                writer.write(moves + "\n");
                writer.write(elapsedTime + "\n");
                writer.write(startPoint.x + "," + startPoint.y + "\n");
                writer.write(endPoint.x + "," + endPoint.y + "\n");
                writer.write(currentPosition.x + "," + currentPosition.y + "\n");

                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        JButton button = gridButtons[i][j];
                        String text = button.getText();
                        String enabled = button.isEnabled() ? "1" : "0";
                        writer.write(text + "," + enabled + "\n");
                    }
                }
                JOptionPane.showMessageDialog(null, "Game saved successfully!", "Save", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error saving the game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        if (gameTimer != null) gameTimer.start();
    }

    public void loadGame() {
        if (gameTimer != null) gameTimer.stop();
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text File", "txt");
        fileChooser.setFileFilter(filter);
        fileChooser.setDialogTitle("Load");
        int res = fileChooser.showOpenDialog(null);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                isLoading = true; // Установить флаг загрузки
                gridSize = Integer.parseInt(reader.readLine());
                totalPoints = Integer.parseInt(reader.readLine());
                moves = Integer.parseInt(reader.readLine());
                elapsedTime = Integer.parseInt(reader.readLine());

                String[] start = reader.readLine().split(",");
                startPoint = new Point(Integer.parseInt(start[0]), Integer.parseInt(start[1]));

                String[] end = reader.readLine().split(",");
                endPoint = new Point(Integer.parseInt(end[0]), Integer.parseInt(end[1]));

                String[] current = reader.readLine().split(",");
                currentPosition = new Point(Integer.parseInt(current[0]), Integer.parseInt(current[1]));

                gameFrame.dispose();
                initializeGame();

                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        String[] buttonData = reader.readLine().split(",");
                        String text = buttonData[0];
                        boolean enabled = buttonData[1].equals("1");

                        JButton button = gridButtons[i][j];
                        button.setText(text);
                        button.setEnabled(enabled);
                        if (!enabled) {
                            button.setBackground(pathColor);
                        }
                    }
                }

                // Обновляем окраску стартовой и конечной точки
                setPoint(startPoint, "Start", Color.GREEN);
                setPoint(endPoint, "End", Color.RED);

                scoreLabel.setText("Score: " + totalPoints);
                movesLabel.setText("Moves: " + moves);
                timerLabel.setText("Time: " + elapsedTime + "s");

                JOptionPane.showMessageDialog(null, "Game loaded successfully!", "Load", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error loading the game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                isLoading = false; // Сбросить флаг загрузки
            }
        }
        if (gameTimer != null) gameTimer.start();
    }


    private void resetGame() {
        if (gameTimer != null) gameTimer.stop();
        totalPoints = 0;
        moves = 0;
        elapsedTime = 0;
        startPoint = null;
        endPoint = null;
        currentPosition = null;
        gameStarted = false;
        gameFrame.dispose();
        initializeGame();
    }

    private Point getButtonPosition(JButton button) {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (gridButtons[i][j] == button) {
                    return new Point(i, j);
                }
            }
        }
        return null;
    }

    private boolean isValidMove(Point clickedPosition) {
        int dx = Math.abs(clickedPosition.x - currentPosition.x);
        int dy = Math.abs(clickedPosition.y - currentPosition.y);

        // Ход возможен только на одну клетку по горизонтали или вертикали.
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton clickedButton = (JButton) e.getSource();
        Point clickedPosition = getButtonPosition(clickedButton);

        if (clickedButton.getText().equals("End")) {
            if (isValidMove(clickedPosition)) { // Проверяем валидность перед завершением
                if (gameTimer != null) gameTimer.stop(); // Остановка таймера
                String scoreMessage = moves == 0 ? "You Win!" : "You Win! Your score: " + totalPoints / moves + "!" + " Your time: " + elapsedTime + "s!" + " Total points: " + totalPoints + "!" + " Path lenght: " + moves + "!" ;
                JOptionPane.showMessageDialog(gameFrame, scoreMessage, "Game Over", JOptionPane.INFORMATION_MESSAGE);
                resetGame();
            } else {
                JOptionPane.showMessageDialog(gameFrame, "Invalid move to 'End'!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        if (isValidMove(clickedPosition)) {
            int value = Integer.parseInt(clickedButton.getText());
            totalPoints += value;
            moves++;
            scoreLabel.setText("Score: " + totalPoints/moves);
            movesLabel.setText("Moves: " + moves);

            clickedButton.setBackground(pathColor);
            clickedButton.setEnabled(false);

            currentPosition = clickedPosition;
            updateHints();
        } else {
            JOptionPane.showMessageDialog(gameFrame, "Invalid move!", "Error", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateHints() {
        if (!hintsEnabled) return; // Не выполнять, если подсказки выключены

        // Сброс границ для всех кнопок
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                gridButtons[i][j].setBorder(null);
            }
        }

        // Если "End" рядом с текущей позицией, подсвечиваем только её
        if (endPoint != null && Math.abs(endPoint.x - currentPosition.x) + Math.abs(endPoint.y - currentPosition.y) == 1) {
            JButton endButton = gridButtons[endPoint.x][endPoint.y];
            endButton.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 3));
            return; // Завершаем выполнение метода, если "End" подсвечена
        }

        int minValue = Integer.MAX_VALUE;
        JButton minButton = null;

        // Находим кнопку с минимальным значением, если "End" не рядом
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                JButton button = gridButtons[i][j];
                Point position = new Point(i, j);

                if (button.isEnabled() &&
                        Math.abs(position.x - currentPosition.x) + Math.abs(position.y - currentPosition.y) == 1) {
                    // Проверяем минимальное значение
                    if (!button.getText().equals("End") && Integer.parseInt(button.getText()) < minValue) {
                        minValue = Integer.parseInt(button.getText());
                        minButton = button;
                    }
                }
            }
        }

        // Устанавливаем границу только для кнопки с минимальным значением
        if (minButton != null) {
            minButton.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
        }
    }
}