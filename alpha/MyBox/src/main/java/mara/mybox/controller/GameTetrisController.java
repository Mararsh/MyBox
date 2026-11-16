package mara.mybox.controller;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2026-11-16
 * @License Apache License Version 2.0
 *
 * Assisted by Doubao
 */
public class GameTetrisController extends BaseWebViewController {

    private static final int BLOCK_SIZE = 30;
    private static final int COLS = 10;
    private static final int ROWS = 20;
    private static final int PREVIEW_BLOCK = 24;

    private static final boolean[][][] SHAPES = {
        {{false, true, false}, {true, true, true}, {false, false, false}}, // T 0
        {{true, false, false}, {true, true, true}, {false, false, false}}, // L 1
        {{false, false, true}, {true, true, true}, {false, false, false}}, // J 2
        {{false, true, true}, {true, true, false}, {false, false, false}}, // S 3
        {{true, true, false}, {false, true, true}, {false, false, false}}, // Z 4
        {{true, true}, {true, true}}, // O 5
        {{false, false, false, false}, {true, true, true, true}, {false, false, false, false}} // I 6
    };
    private static final Color[] COLORS = {
        Color.PURPLE, Color.ORANGE, Color.BLUE,
        Color.GREEN, Color.RED, Color.YELLOW, Color.CYAN
    };

    private enum RotState {
        S0, SR, S2, SL
    }

    private static final int[][][] KICK_NORMAL = {
        {{0, 0}, {-1, 0}, {-1, 1}, {0, -2}, {-1, -2}}, // S0 -> SR
        {{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}}, // SR -> S2
        {{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}}, // S2 -> SL
        {{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}} // SL -> S0
    };

    private static final int[][][] KICK_I = {
        {{0, 0}, {-2, 0}, {1, 0}, {-2, -1}, {1, 2}},
        {{0, 0}, {-1, 0}, {2, 0}, {-1, 2}, {2, -1}},
        {{0, 0}, {2, 0}, {-1, 0}, {2, 1}, {-1, -2}},
        {{0, 0}, {1, 0}, {-2, 0}, {1, -2}, {-2, 1}}
    };

    private final boolean[][] board = new boolean[ROWS][COLS];
    private final Color[][] boardColor = new Color[ROWS][COLS];

    private boolean[][] currentShape;
    private Color currentColor;
    private int shapeIndex;
    private RotState rotState;
    private int x, y;
    private int ghostY;
    private float ghostAlpha = 0.2f;
    private int interval = 500, score = 0;
    private Random random;
    protected Timer timer;
    private boolean gameOver = false;

    private boolean[][] nextShape;
    private Color nextColor;
    private int nextShapeIndex;

    private boolean[][] holdShape;
    private Color holdColor;
    private int holdShapeIndex = -1;
    private boolean canHold;

    private Canvas canvas, previewCanvas, holdCanvas;
    private GraphicsContext gc, previewGc, holdGc;

    @FXML
    protected VBox shapesBox, previewBox, holdBox;

    public GameTetrisController() {
        baseTitle = message("GameTetris");
        TipsLabelKey = "GameTetrisTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            random = new Random();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            start();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void start() {
        gameOver = false;
        canvas = new Canvas(COLS * BLOCK_SIZE, ROWS * BLOCK_SIZE);
        gc = canvas.getGraphicsContext2D();
        shapesBox.getChildren().clear();
        shapesBox.getChildren().add(canvas);

        int previewSize = 5 * PREVIEW_BLOCK;
        previewCanvas = new Canvas(previewSize, previewSize);
        previewGc = previewCanvas.getGraphicsContext2D();
        previewBox.getChildren().clear();
        previewBox.getChildren().add(previewCanvas);

        holdCanvas = new Canvas(previewSize, previewSize);
        holdGc = holdCanvas.getGraphicsContext2D();
        holdBox.getChildren().clear();
        holdBox.getChildren().add(holdCanvas);

        genNextShape();
        spawnNewShape();

        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (gameOver) {
                    return;
                }
                moveDown();
                Platform.runLater(() -> {
                    calcGhost();
                    render();
                    renderPreview();
                    renderHold();
                });
            }
        }, interval, interval);

        canvas.setFocusTraversable(true);
        calcGhost();
        render();
        renderPreview();
        renderHold();
    }

    private void genNextShape() {
        nextShapeIndex = random.nextInt(SHAPES.length);
        nextShape = SHAPES[nextShapeIndex];
        nextColor = COLORS[nextShapeIndex];
    }

    private void spawnNewShape() {
        shapeIndex = nextShapeIndex;
        currentShape = nextShape;
        currentColor = nextColor;
        rotState = RotState.S0;
        x = COLS / 2 - currentShape[0].length / 2;
        y = 0;
        canHold = true;

        genNextShape();

        if (checkCollision(0, 0)) {
            MyBoxLog.debug("Game Over! Score:" + score);
            gameOver = true;
            if (timer != null) {
                timer.cancel();
            }
        }
        calcGhost();
    }

    private void calcGhost() {
        if (gameOver) {
            return;
        }
        int testY = y;
        while (!checkCollision(0, testY - y + 1, currentShape)) {
            testY++;
        }
        ghostY = testY;
    }

    private void doHold() {
        if (gameOver || !canHold) {
            return;
        }
        canHold = false;

        int tempIdx = shapeIndex;
        boolean[][] tempShape = currentShape;
        Color tempColor = currentColor;

        if (holdShapeIndex == -1) {
            holdShapeIndex = tempIdx;
            holdShape = tempShape;
            holdColor = tempColor;
            spawnNewShape();
        } else {
            shapeIndex = holdShapeIndex;
            currentShape = holdShape;
            currentColor = holdColor;
            rotState = RotState.S0;
            x = COLS / 2 - currentShape[0].length / 2;
            y = 0;

            holdShapeIndex = tempIdx;
            holdShape = tempShape;
            holdColor = tempColor;
        }
        calcGhost();
    }

    @Override
    public boolean keyLeft() {
        if (gameOver) {
            return true;
        }
        moveLeft();
        calcGhost();
        return true;
    }

    @Override
    public boolean keyRight() {
        if (gameOver) {
            return true;
        }
        moveRight();
        calcGhost();
        return true;
    }

    @Override
    public boolean keyUp() {
        if (gameOver) {
            return true;
        }
        rotateSRS();
        calcGhost();
        return true;
    }

    @Override
    public boolean keyDown() {
        if (gameOver) {
            return true;
        }
        moveDown();
        calcGhost();
        return true;
    }

    @Override
    public boolean keySpace() {
        if (gameOver) {
            return true;
        }
        hardDrop();
        calcGhost();
        return true;
    }

    @Override
    public boolean otherKeys(KeyEvent event) {
        if (gameOver) {
            return false;
        }
        KeyCode code = event.getCode();
        if (code != null && code == KeyCode.C) {
            doHold();
            render();
            renderPreview();
            renderHold();
            return true;
        }
        return false;
    }

    private boolean checkCollision(int dx, int dy, boolean[][] shape) {
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    int nx = x + col + dx;
                    int ny = y + row + dy;
                    if (nx < 0 || nx >= COLS || ny >= ROWS) {
                        return true;
                    }
                    if (ny >= 0 && board[ny][nx]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkCollision(int dx, int dy) {
        return checkCollision(dx, dy, currentShape);
    }

    private void lockShape() {
        for (int row = 0; row < currentShape.length; row++) {
            for (int col = 0; col < currentShape[row].length; col++) {
                if (currentShape[row][col]) {
                    int ny = y + row;
                    int nx = x + col;
                    if (ny >= 0) {
                        board[ny][nx] = true;
                        boardColor[ny][nx] = currentColor;
                    }
                }
            }
        }
        clearFullLines();
        spawnNewShape();
    }

    private void clearFullLines() {
        int linesCleared = 0;
        for (int r = ROWS - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < COLS; c++) {
                if (!board[r][c]) {
                    full = false;
                    break;
                }
            }
            if (full) {
                linesCleared++;
                for (int i = r; i > 0; i--) {
                    System.arraycopy(board[i - 1], 0, board[i], 0, COLS);
                    System.arraycopy(boardColor[i - 1], 0, boardColor[i], 0, COLS);
                }
                for (int c = 0; c < COLS; c++) {
                    board[0][c] = false;
                    boardColor[0][c] = null;
                }
                r++;
            }
        }
        score += linesCleared * 100;
    }

    private boolean[][] rotateMatrix(boolean[][] src) {
        int h = src.length;
        int w = src[0].length;
        boolean[][] dst = new boolean[w][h];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                dst[j][h - 1 - i] = src[i][j];
            }
        }
        return dst;
    }

    private void rotateSRS() {
        if (shapeIndex == 5) {
            return;
        }
        boolean[][] rotated = rotateMatrix(currentShape);
        RotState nextState = switch (rotState) {
            case S0 -> RotState.SR;
            case SR -> RotState.S2;
            case S2 -> RotState.SL;
            case SL -> RotState.S0;
        };

        int[][][] kickTable = shapeIndex == 6 ? KICK_I : KICK_NORMAL;
        int kickIdx = rotState.ordinal();
        int[][] kicks = kickTable[kickIdx];

        for (int[] kick : kicks) {
            int dx = kick[0];
            int dy = kick[1];
            if (!checkCollision(dx, dy, rotated)) {
                currentShape = rotated;
                rotState = nextState;
                x += dx;
                y += dy;
                return;
            }
        }
    }

    private void moveLeft() {
        if (!checkCollision(-1, 0)) {
            x--;
        }
    }

    private void moveRight() {
        if (!checkCollision(1, 0)) {
            x++;
        }
    }

    private void moveDown() {
        if (checkCollision(0, 1)) {
            lockShape();
        } else {
            y++;
        }
    }

    private void hardDrop() {
        while (!checkCollision(0, 1)) {
            y++;
        }
        lockShape();
    }

    private void render() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (board[r][c]) {
                    drawBlock(c, r, boardColor[r][c]);
                }
            }
        }
        if (gameOver) {
            gc.setFill(Color.RED);
            gc.setFont(javafx.scene.text.Font.font(24));
            gc.fillText(message("GameOver"), 20, canvas.getHeight() / 2);
            return;
        }

        gc.setGlobalAlpha(ghostAlpha);
        for (int row = 0; row < currentShape.length; row++) {
            for (int col = 0; col < currentShape[row].length; col++) {
                if (currentShape[row][col]) {
                    int drawX = x + col;
                    int drawY = ghostY + row;
                    if (drawY >= 0) {
                        gc.setFill(currentColor);
                        gc.fillRect(drawX * BLOCK_SIZE, drawY * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
                    }
                }
            }
        }
        gc.setGlobalAlpha(1.0);

        for (int row = 0; row < currentShape.length; row++) {
            for (int col = 0; col < currentShape[row].length; col++) {
                if (currentShape[row][col]) {
                    int drawX = x + col;
                    int drawY = y + row;
                    if (drawY >= 0) {
                        drawBlock(drawX, drawY, currentColor);
                    }
                }
            }
        }
    }

    private void drawBlock(int c, int r, Color color) {
        gc.setFill(color);
        gc.fillRect(c * BLOCK_SIZE, r * BLOCK_SIZE, BLOCK_SIZE - 1, BLOCK_SIZE - 1);
    }

    private void renderPreview() {
        previewGc.setFill(Color.BLACK);
        previewGc.fillRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        if (gameOver) {
            return;
        }
        int offsetX = (int) ((previewCanvas.getWidth() - nextShape[0].length * PREVIEW_BLOCK) / 2);
        int offsetY = (int) ((previewCanvas.getHeight() - nextShape.length * PREVIEW_BLOCK) / 2);

        for (int row = 0; row < nextShape.length; row++) {
            for (int col = 0; col < nextShape[row].length; col++) {
                if (nextShape[row][col]) {
                    previewGc.setFill(nextColor);
                    previewGc.fillRect(
                            offsetX + col * PREVIEW_BLOCK,
                            offsetY + row * PREVIEW_BLOCK,
                            PREVIEW_BLOCK - 1,
                            PREVIEW_BLOCK - 1
                    );
                }
            }
        }
    }

    private void renderHold() {
        holdGc.setFill(Color.BLACK);
        holdGc.fillRect(0, 0, holdCanvas.getWidth(), holdCanvas.getHeight());
        if (gameOver || holdShapeIndex == -1) {
            return;
        }
        int offsetX = (int) ((holdCanvas.getWidth() - holdShape[0].length * PREVIEW_BLOCK) / 2);
        int offsetY = (int) ((holdCanvas.getHeight() - holdShape.length * PREVIEW_BLOCK) / 2);
        for (int row = 0; row < holdShape.length; row++) {
            for (int col = 0; col < holdShape[row].length; col++) {
                if (holdShape[row][col]) {
                    holdGc.setFill(holdColor);
                    holdGc.fillRect(
                            offsetX + col * PREVIEW_BLOCK,
                            offsetY + row * PREVIEW_BLOCK,
                            PREVIEW_BLOCK - 1,
                            PREVIEW_BLOCK - 1
                    );
                }
            }
        }
    }

}
