package Game;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

public class Board {

    private double width;
    private double height;
    private int size;
    private int positions;
    private String human;
    private String ai;
    private String currentPlayer;
    private String startingPlayer;
    private Pane root;
    private Scene scene;
    private Tile[][] board;
    private int maxDepth;
    private boolean finished;
    private Button reset;
    private Text gameoverMessage;
    private Text difficultyMessage;
    private ChoiceBox<String> difficulty;
    private Map<String, Integer> difficultyMap;

    public Board(double width, double height, int size) {
        this.width = width;
        this.height = height;
        this.size = size;
        this.human = "X";
        this.ai = "O";
        if (size <= 3) {
            this.maxDepth = 5;
        } else {
            this.maxDepth = 1;
        }
        this.startingPlayer = human;
        this.root = new Pane();
        this.scene = new Scene(root, width, height);
        this.root.setPrefSize(width, height);
        this.board = new Tile[size][size];
        this.initialiseBoard();
        this.createResetButton();
        this.createGameoverMessage();
        this.initialiseDifficulties();
    }

    private void initialiseDifficulties() {
        difficulty = new ChoiceBox<>();
        difficulty.getItems().add("Normal");
        difficulty.getItems().add("Impossible");
        difficulty.setValue("Impossible");
        difficulty.setOnAction(this::difficultyHandler);
        difficulty.setFocusTraversable(false);
        double w = 110;
        difficulty.setPrefWidth(w);
        difficulty.layoutXProperty().bind(scene.widthProperty().subtract(w).divide(2));
        difficulty.setLayoutY(height/1.7);
        difficultyMap = new HashMap<>();
        difficultyMap.put("Normal", 1);
        difficultyMap.put("Impossible", 5);
        difficultyMessage = new Text();
        difficultyMessage.setText("Change difficulty?");
        difficultyMessage.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 30));
        difficultyMessage.setLayoutY(height/1.9);
        difficultyMessage.layoutXProperty().bind(scene.widthProperty().subtract(difficultyMessage.prefWidth(-1)).divide(2));
    }

    private void difficultyHandler(ActionEvent actionEvent) {
        maxDepth = difficultyMap.get(difficulty.getValue());
    }

    private void initialiseBoard() {
        this.root.getChildren().clear();
        this.finished = false;
        this.positions = size * size;
        double widthOffset = width / size;
        double heightOffset = height / size;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = new Tile(width / size);
                board[i][j].setTranslateX(i * widthOffset);
                board[i][j].setTranslateY(j * heightOffset);
                board[i][j].setOnMousePressed(this::handleClicked);
                board[i][j].setOnMouseReleased(this::handleReleased);
                board[i][j].setOnMouseEntered(this::handleEntered);
                board[i][j].setOnMouseExited(this::handleExited);
                root.getChildren().add(board[i][j]);
            }
        }
        currentPlayer = startingPlayer;
        if (currentPlayer.equals(ai)) {
            aiRandomFirstMove();
        }
    }

    private void handleEntered(MouseEvent event) {
        Tile tile = selectedTile(event);
        if (tile != null && !finished) {
            if (tile.getText().equals("")) {
                tile.setFill(Paint.valueOf("DEDEDE"));
            }
        }
    }

    private void handleExited(MouseEvent event) {
        Tile tile = selectedTile(event);
        if (tile != null) {
            if (tile.getText().equals("")) {
                tile.resetColour();
            }
        }
    }

    public Scene getScene() {
        return scene;
    }

    private void handleClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY || finished || currentPlayer.equals(ai)) {
            return;
        }
        humanMove(event);
    }

    private void handleReleased(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY || finished || currentPlayer.equals(human)) {
            return;
        }
        aiMove();
    }

    private void humanMove(MouseEvent event) {
        Tile tile = selectedTile(event);
        if (tile != null) {
            if (tile.getText().equals("")) {
                tile.draw(human);
                tile.setTextColour(Paint.valueOf("darkblue"));
                tile.resetColour();
                positions--;
                checkGameStatus();
                currentPlayer = ai;
            }
        }
    }

    private Tile selectedTile(MouseEvent event) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Tile tile = board[i][j];
                if (tile == event.getSource()) {
                    return tile;
                }
            }
        }
        return null;
    }

    private void checkGameStatus() {
        String check = checkWinner();
        if (check.equals(human)) {
            endScreen("Congratulations, you won!");
        } else if (check.equals(ai)) {
            endScreen("Unlucky, the ai won!");
        } else if (positions == 0) {
            endScreen("Draw!");
        }
    }

    private int minimax(int positions, int depth, int alpha, int beta, boolean maximisingPlayer) {

        String check = checkWinner();

        if (check.equals(ai)) {
            return 1 + positions;
        } else if (check.equals(human)) {
            return -1;
        } else if (positions == 0 || depth == maxDepth) {
            return 0;
        }

        if (maximisingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (board[i][j].getText().equals("")) {
                        board[i][j].draw(ai);
                        int eval = minimax(positions - 1, depth + 1, alpha, beta, false);
                        board[i][j].draw("");
                        maxEval = Math.max(maxEval, eval);
                        alpha = Math.max(alpha, eval);
                        if (beta <= alpha) {
                            break;
                        }
                    }
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (board[i][j].getText().equals("")) {
                        board[i][j].draw(human);
                        int eval = minimax(positions - 1, depth + 1, alpha, beta, true);
                        board[i][j].draw("");
                        minEval = Math.min(minEval, eval);
                        beta = Math.min(beta, eval);
                        if (beta <= alpha) {
                            break;
                        }
                    }
                }
            }
            return minEval;
        }

    }

    private void aiMove() {

        int bestScore = Integer.MIN_VALUE;
        int r = 0;
        int c = 0;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j].getText().equals("")) {
                    board[i][j].draw(ai);
                    int score = minimax(positions - 1, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                    board[i][j].draw("");
                    if (score > bestScore) {
                        bestScore = score;
                        r = i;
                        c = j;
                    }
                }
            }
        }

        board[r][c].draw(ai);
        board[r][c].setTextColour(Paint.valueOf("darkred"));
        positions--;
        checkGameStatus();
        currentPlayer = human;

    }

    private String checkWinner() {

        for (int i = 0; i < size; i++) {

            if (board[i][0].getText().equals("")) {
                continue;
            }

            boolean win = true;

            for (int j = 0; j < size - 1; j++) {
                if (!board[i][j].getText().equals(board[i][j+1].getText())) {
                    win = false;
                    break;
                }
            }

            if (win) {
                return board[i][0].getText();
            }

        }

        for (int j = 0; j < size; j++) {

            if (board[0][j].getText().equals("")) {
                continue;
            }

            boolean win = true;

            for (int i = 0; i < size - 1; i++) {
                if (!board[i][j].getText().equals(board[i+1][j].getText())) {
                    win = false;
                    break;
                }
            }

            if (win) {
                return board[0][j].getText();
            }

        }

        boolean win = true;

        if (!board[0][0].getText().equals("")) {
            for (int i = 0; i < size - 1; i++) {
                if (!board[i][i].getText().equals(board[i+1][i+1].getText())) {
                    win = false;
                }
            }

            if (win) {
                return board[0][0].getText();
            }
        }

        win = true;

        if (!board[0][size - 1].getText().equals("")) {
            for (int i = 0; i < size - 1; i++) {
                if (!board[i][size - i - 1].getText().equals(board[i+1][size - i - 2].getText())) {
                    win = false;
                }
            }

            if (win) {
                return board[0][size - 1].getText();
            }
        }

        return "";
    }

    private void endScreen(String msg) {
        finished = true;
        adjustGameoverMessage(msg);
        root.getChildren().addAll(gameoverMessage, reset, difficultyMessage, difficulty);
        makeBoardTransparent();
    }

    private void makeBoardTransparent() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j].makeTransparent();
            }
        }
    }

    private void createResetButton() {
        reset = new Button();
        reset.setText("Click here to play again!");
        reset.layoutXProperty().bind(scene.widthProperty().subtract(reset.widthProperty()).divide(2));
        reset.setLayoutY(height / 2.8);
        reset.setOnAction(actionEvent -> initialiseBoard());
        reset.setFocusTraversable(false);
    }

    private void createGameoverMessage() {
        gameoverMessage = new Text();
        gameoverMessage.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 30));
        gameoverMessage.setLayoutY(height/3.5);
    }

    private void adjustGameoverMessage(String msg) {
        gameoverMessage.setText(msg);
        gameoverMessage.layoutXProperty().bind(scene.widthProperty().subtract(gameoverMessage.prefWidth(-1)).divide(2));
    }

    private void aiRandomFirstMove() {
        int i = (int) (Math.random() * size);
        int j = (int) (Math.random() * size);
        board[i][j].draw(ai);
        board[i][j].setTextColour(Paint.valueOf("darkred"));
        positions--;
        currentPlayer = human;
    }
}
