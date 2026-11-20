package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2026-11-16
 * @License Apache License Version 2.0
 *
 * Assisted by Doubao
 */
public class GameTetrisController extends BaseGameController {

    private static final boolean[][][] SHAPES = {
        {{false, true, false}, {true, true, true}, {false, false, false}}, // T 0
        {{true, false, false}, {true, true, true}, {false, false, false}}, // L 1
        {{false, false, true}, {true, true, true}, {false, false, false}}, // J 2
        {{false, true, true}, {true, true, false}, {false, false, false}}, // S 3
        {{true, true, false}, {false, true, true}, {false, false, false}}, // Z 4
        {{true, true}, {true, true}}, // O 5
        {{false, false, false, false}, {true, true, true, true}, {false, false, false, false}} // I 6
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

    /**
     * Tetris Guideline Marathon Gravity 预查表 公式：G = 0.01667 * pow(0.8 -
     * ((level-1)*0.007), level-1) （60FPS） 参考：https://tetris.wiki/Gravity
     * 单位：毫秒，Lv11及以上固定50ms
     */
    private static final int[] LEVEL_DELAY = {
        1000, 793, 618, 473, 355,
        262, 193, 144, 106, 77, 50
    };
    private static final int DISCARD_COST = 50;

    private Color[] shapeColors;

    private boolean[][] board;
    private Color[][] boardColor;
    private boolean[][] currentShape, nextShape, holdShape;

    private Color currentColor, nextColor, holdColor,
            backgroundColor = Color.BLACK, borderColor = Color.WHITE;
    private Color gridColor = Color.GRAY.deriveColor(0, 1, 1, 0.25);
    private int linesPerLevel = 10, blockSize, columnsNumber, rowsNumber;
    private int shapeIndex, x, y, ghostY, holdShapeIndex = -1, nextShapeIndex;
    private int dropInterval, score = 0, level = 1, totalLines = 0, softScore = 1, hardScore = 2;
    private boolean gameOver, paused, use7Bag, canHold;
    private float ghostAlpha = 0.2f;
    private final List<Integer> bag = new ArrayList<>();
    private RotState rotState;

    private Canvas canvas, previewCanvas, holdCanvas;
    private GraphicsContext gc, previewGc, holdGc;
    private Rectangle pickedColor;

    @FXML
    protected VBox shapesBox, previewBox, holdBox, colorsBox;
    @FXML
    protected CheckBox quickerCheck, ghostCheck, gridCheck, bordersCheck;
    @FXML
    protected ComboBox<String> intervalSelector, blockSelector, rowsSelector, columnsSelector;

    public GameTetrisController() {
        baseTitle = message("GameTetris");
        TipsLabelKey = "GameTetrisTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            random = new Random();
            gameOver = true;
            paused = false;
            use7Bag = true;
            shapeColors = new Color[]{
                Color.PURPLE, Color.ORANGE, Color.BLUE,
                Color.GREEN, Color.RED, Color.YELLOW, Color.CYAN
            };

            try (Connection conn = DerbyBase.getConnection()) {
                for (int i = 0; i < shapeColors.length; i++) {
                    shapeColors[i] = Color.web(UserConfig.getString(conn, baseName + "Color" + i, (shapeColors[i]).toString()));
                }
            } catch (Exception e) {
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            quickerCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "QuickerWhenLevelHigher", quickerCheck.isSelected());
                }
            });
            quickerCheck.setSelected(UserConfig.getBoolean(baseName + "QuickerWhenLevelHigher", false));

            ghostCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "ShowGhost", ghostCheck.isSelected());
                }
            });
            ghostCheck.setSelected(UserConfig.getBoolean(baseName + "ShowGhost", true));

            gridCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "ShowGrid", gridCheck.isSelected());
                }
            });
            gridCheck.setSelected(UserConfig.getBoolean(baseName + "ShowGrid", true));

            bordersCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "ShowBorders", bordersCheck.isSelected());
                }
            });
            bordersCheck.setSelected(UserConfig.getBoolean(baseName + "ShowBorders", true));

            dropInterval = UserConfig.getInt(baseName + "Interval", 1000);
            if (dropInterval < 80) {
                dropInterval = 1000;
            }
            intervalSelector.getItems().addAll(Arrays.asList(
                    "1000", "500", "1500", "200", "2000", "800", "600", "100", "50", "80")
            );
            intervalSelector.setValue(dropInterval + "");
            intervalSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String o, String n) {
                    try {
                        int iv = (int) Double.parseDouble(n);
                        if (iv >= 80) {
                            dropInterval = iv;
                            UserConfig.setInt(baseName + "Interval", dropInterval);
                            restartTimer();
                        }
                    } catch (Exception e) {
                    }
                }
            });

            rowsNumber = UserConfig.getInt(baseName + "RowsNumber", 20);
            if (rowsNumber < 10) {
                rowsNumber = 20;
            }
            rowsSelector.getItems().addAll(Arrays.asList(
                    "20", "15", "25", "30", "10", "35", "40", "50")
            );
            rowsSelector.setValue(rowsNumber + "");
            rowsSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String o, String n) {
                    try {
                        int iv = (int) Double.parseDouble(n);
                        if (iv >= 10) {
                            rowsNumber = iv;
                            UserConfig.setInt(baseName + "RowsNumber", rowsNumber);
                        }
                    } catch (Exception e) {
                    }
                }
            });

            columnsNumber = UserConfig.getInt(baseName + "ColumnsNumber", 10);
            if (columnsNumber < 6) {
                columnsNumber = 10;
            }
            columnsSelector.getItems().addAll(Arrays.asList(
                    "10", "15", "20", "8", "25", "30", "18", "35", "40")
            );
            columnsSelector.setValue(columnsNumber + "");
            columnsSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String o, String n) {
                    try {
                        int iv = (int) Double.parseDouble(n);
                        if (iv >= 6) {
                            columnsNumber = iv;
                            UserConfig.setInt(baseName + "ColumnsNumber", columnsNumber);
                        }
                    } catch (Exception e) {
                    }
                }
            });

            blockSize = UserConfig.getInt(baseName + "BlockSize", 30);
            if (blockSize < 0) {
                blockSize = 30;
            }
            blockSelector.getItems().addAll(Arrays.asList(
                    "30", "20", "40", "45", "15", "25", "35", "50", "60", "55")
            );
            blockSelector.setValue(blockSize + "");
            blockSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String o, String n) {
                    try {
                        int iv = (int) Double.parseDouble(n);
                        if (iv > 0) {
                            blockSize = iv;
                            UserConfig.setInt(baseName + "BlockSize", blockSize);
                        }
                    } catch (Exception e) {
                    }
                }
            });

            initSound();

            colorsBox.getChildren().clear();
            for (int index = 0; index < SHAPES.length; index++) {
                setColor(index);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    private void setColor(int index) {
        try {
            int previewBlockSize = 20;
            boolean[][] shape = SHAPES[index];
            int rows = shape.length;
            int cols = shape[0].length;
            Canvas colorCanvas = new Canvas(cols * previewBlockSize, rows * previewBlockSize);
            GraphicsContext cgc = colorCanvas.getGraphicsContext2D();
            cgc.clearRect(0, 0, colorCanvas.getWidth(), colorCanvas.getHeight());
            cgc.setFill(shapeColors[index]);
            for (int r = 0; r < shape.length; r++) {
                for (int c = 0; c < shape[r].length; c++) {
                    if (shape[r][c]) {
                        cgc.fillRect(
                                c * previewBlockSize,
                                r * previewBlockSize,
                                previewBlockSize - 1,
                                previewBlockSize - 1
                        );
                    }
                }
            }
            colorCanvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    try {
                        pickedColor = new Rectangle();
                        FXMLLoader fxmlLoader = new FXMLLoader(
                                WindowTools.class.getResource(Fxmls.ColorPalettePopupFxml), AppVariables.CurrentBundle);
                        Pane pane = fxmlLoader.load();
                        ColorPalettePopupController controller = (ColorPalettePopupController) fxmlLoader.getController();
                        controller.load(myController, pickedColor);
                        controller.setNotify.addListener(new ChangeListener<Boolean>() {
                            @Override
                            public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                                shapeColors[index] = (Color) pickedColor.getFill();
                                setColor(index);
                            }
                        });

                        popup = makePopup();
                        popup.getContent().add(pane);
                        LocateTools.locateCenter(colorCanvas, popup);
                    } catch (Exception e) {
                        MyBoxLog.debug(e);
                    }
                }
            });

            if (index > colorsBox.getChildren().size() - 1) {
                colorsBox.getChildren().add(colorCanvas);
            } else {
                colorsBox.getChildren().set(index, colorCanvas);
            }

            UserConfig.setString(baseName + "Color" + index, (shapeColors[index]).toString());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(createButton, message("NewGame") + "\nn / Ctrl+n");

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void startAction() {
        if (gameOver) {
            startGame();
        } else if (paused) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    public void startGame() {
        try {
            if (blockSize < 1 || columnsNumber <= 0 || rowsNumber < 5) {
                popError(message("InvalidParameters"));
                return;
            }
            if (timer != null) {
                timer.cancel();
            }
            StyleTools.setNameIcon(startButton, message("Pause"), "iconPause.png");
            startButton.applyCss();
            gameOver = false;
            paused = false;
            score = 0;
            level = 1;
            totalLines = 0;
            if (dropInterval < 80) {
                dropInterval = LEVEL_DELAY[0];
            }
            bag.clear();
            board = new boolean[rowsNumber][columnsNumber];
            boardColor = new Color[rowsNumber][columnsNumber];
            holdShapeIndex = -1;
            holdShape = null;
            holdColor = null;
            canHold = true;

            canvas = new Canvas(columnsNumber * blockSize, rowsNumber * blockSize);
            gc = canvas.getGraphicsContext2D();
            shapesBox.getChildren().clear();
            shapesBox.getChildren().add(canvas);

            int previewSize = 5 * blockSize;
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
            restartTimer();
            canvas.setFocusTraversable(true);
            calcGhost();
            render();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void stopGame() {
        StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
        startButton.applyCss();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        gameOver = true;
        paused = false;
    }

    public void pauseGame() {
        StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
        startButton.applyCss();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (!gameOver) {
            paused = true;
        }
    }

    public void resumeGame() {
        StyleTools.setNameIcon(startButton, message("Pause"), "iconPause.png");
        startButton.applyCss();
        paused = false;
        restartTimer();
    }

    private void restartTimer() {
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
                Platform.runLater(() -> {
                    moveDown();
                    calcGhost();
                    render();
                });
            }
        }, dropInterval, dropInterval);
    }

    private int getLineScoreExp(int n) {
        if (n < 1) {
            return 0;
        }
        return (int) (10 * Math.pow(4, n - 1));
    }

    private void refillBag() {
        bag.clear();
        for (int i = 0; i < 7; i++) {
            bag.add(i);
        }
        Collections.shuffle(bag, random);
    }

    private int takeFromBag() {
        if (bag.isEmpty()) {
            refillBag();
        }
        return bag.remove(0);
    }

    private void genNextShape() {
        int idx;
        if (use7Bag) {
            idx = takeFromBag();
        } else {
            idx = random.nextInt(SHAPES.length);
        }
        nextShapeIndex = idx;
        nextShape = SHAPES[nextShapeIndex];
        nextColor = shapeColors[nextShapeIndex];
    }

    private void spawnNewShape() {
        shapeIndex = nextShapeIndex;
        currentShape = nextShape;
        currentColor = nextColor;
        rotState = RotState.S0;
        x = columnsNumber / 2 - currentShape[0].length / 2;
        y = 0;

        genNextShape();
        canHold = true;
        if (checkCollision(0, 0)) {
            stopGame();
            Platform.runLater(() -> popError(message("GameOver")));
        } else {
            calcGhost();
        }
    }

    private void calcGhost() {
        if (gameOver || !ghostCheck.isSelected()) {
            return;
        }
        int testY = y;
        while (!checkCollision(0, testY - y + 1, currentShape)) {
            testY++;
        }
        ghostY = testY;
    }

    private void doHold() {
        if (gameOver || paused || !canHold) {
            return;
        }
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
            x = columnsNumber / 2 - currentShape[0].length / 2;
            y = 0;
            holdShapeIndex = tempIdx;
            holdShape = tempShape;
            holdColor = tempColor;
            canHold = false;
        }
        calcGhost();
    }

    private void doDiscard() {
        if (gameOver || paused) {
            return;
        }
        if (score < DISCARD_COST) {
            popError(message("ScoreNotEnough"));
            return;
        }
        score -= DISCARD_COST;
        spawnNewShape();
        calcGhost();
        writeScore();
    }

    @Override
    public boolean keyLeft() {
        if (gameOver || paused) {
            return false;
        }
        moveLeft();
        calcGhost();
        render();
        renderPreview();
        renderHold();
        return true;
    }

    @Override
    public boolean keyRight() {
        if (gameOver || paused) {
            return false;
        }
        moveRight();
        calcGhost();
        render();
        renderPreview();
        renderHold();
        return true;
    }

    @Override
    public boolean keyUp() {
        if (gameOver || paused) {
            return false;
        }
        rotateSRS();
        calcGhost();
        render();
        renderPreview();
        renderHold();
        return true;
    }

    @Override
    public boolean keyDown() {
        if (gameOver || paused) {
            return false;
        }
        if (moveDown()) {
            score += softScore * level;
        }
        calcGhost();
        render();
        renderPreview();
        renderHold();
        return true;
    }

    @Override
    public boolean keySpace() {
        if (gameOver || paused) {
            return false;
        }
        hardDrop();
        calcGhost();
        render();
        renderPreview();
        renderHold();
        return true;
    }

    @Override
    public boolean otherKeys(KeyEvent event) {
        if (event == null || gameOver) {
            return false;
        }
        KeyCode code = event.getCode();
        if (code == null) {
            return false;
        }
        switch (code) {
            case KeyCode.C:
                doHold();
                render();
                renderPreview();
                renderHold();
                return true;
            case KeyCode.X:
                doDiscard();
                render();
                renderPreview();
                renderHold();
                return true;
            case KeyCode.A:
                return keyLeft();
            case KeyCode.W:
                return keyUp();
            case KeyCode.S:
                return keyDown();
            case KeyCode.D:
                return keyRight();
            case KeyCode.R:
                startGame();
                return true;
        }
        return false;
    }

    @Override
    public void createAction() {
        startGame();
    }

    @Override
    public boolean controlAltN() {
        if (targetIsTextInput()) {
            return false;
        }
        createAction();
        return true;
    }

    private boolean checkCollision(int dx, int dy, boolean[][] shape) {
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col]) {
                    int nx = x + col + dx;
                    int ny = y + row + dy;
                    if (nx < 0 || nx >= columnsNumber || ny >= rowsNumber) {
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
        canHold = false;
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
        for (int r = rowsNumber - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < columnsNumber; c++) {
                if (!board[r][c]) {
                    full = false;
                    break;
                }
            }
            if (full) {
                linesCleared++;
                for (int i = r; i > 0; i--) {
                    System.arraycopy(board[i - 1], 0, board[i], 0, columnsNumber);
                    System.arraycopy(boardColor[i - 1], 0, boardColor[i], 0, columnsNumber);
                }
                for (int c = 0; c < columnsNumber; c++) {
                    board[0][c] = false;
                    boardColor[0][c] = null;
                }
                r++;
            }
        }
        if (linesCleared <= 0) {
            writeScore();
            return;
        }
        int added = getLineScoreExp(linesCleared);
        if (quickerCheck.isSelected()) {
            added = added * level;
        }
        score += added;
        totalLines += linesCleared;
        if (linesPerLevel > 0) {
            int newLevel = totalLines / linesPerLevel + 1;
            if (newLevel > level) {
                level = newLevel;
                if (quickerCheck.isSelected()) {
                    int idx = Math.min(level - 1, LEVEL_DELAY.length - 1);
                    dropInterval = LEVEL_DELAY[idx] * dropInterval / 1000;
                    restartTimer();
                }
            }
        }
        writeScore();
        popError("+ " + added);
//        popScore(scoreLabel, linesCleared);
        playSound(linesCleared > 1);

    }

    private void writeScore() {
        scoreLabel.setText(message("Score") + ": " + score + "\n"
                + message("Level") + ": " + level + "\n"
                + message("Lines") + ": " + totalLines);
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

    private boolean moveDown() {
        if (checkCollision(0, 1)) {
            lockShape();
            return false;
        } else {
            y++;
            return true;
        }
    }

    private void hardDrop() {
        while (!checkCollision(0, 1)) {
            y++;
        }
        score += hardScore * level;
        lockShape();
    }

    private void render() {
        gc.setFill(backgroundColor);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (gridCheck.isSelected()) {
            gc.setStroke(gridColor);
            gc.setLineWidth(0.5);
            for (int c = 0; c <= columnsNumber; c++) {
                double xPos = c * blockSize;
                gc.strokeLine(xPos, 0, xPos, canvas.getHeight());
            }
            for (int r = 0; r <= rowsNumber; r++) {
                double yPos = r * blockSize;
                gc.strokeLine(0, yPos, canvas.getWidth(), yPos);
            }
        }

        for (int r = 0; r < rowsNumber; r++) {
            for (int c = 0; c < columnsNumber; c++) {
                if (board[r][c]) {
                    drawBlock(c, r, boardColor[r][c]);
                }
            }
        }
        if (ghostCheck.isSelected()) {
            gc.setGlobalAlpha(ghostAlpha);
            for (int row = 0; row < currentShape.length; row++) {
                for (int col = 0; col < currentShape[row].length; col++) {
                    if (currentShape[row][col]) {
                        int drawY = ghostY + row;
                        if (drawY >= 0) {
                            drawBlock(x + col, drawY, currentColor);
                        }
                    }
                }
            }
            gc.setGlobalAlpha(1.0);
        }

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
        writeScore();
    }

    private void drawBlock(int c, int r, Color color) {
        gc.setFill(color);
        gc.fillRect(c * blockSize, r * blockSize, blockSize - 1, blockSize - 1);
        if (bordersCheck.isSelected()) {
            gc.setStroke(borderColor);
            gc.setLineWidth(1);
            gc.strokeRect(c * blockSize, r * blockSize, blockSize - 1, blockSize - 1);
        }
    }

    private void renderPreview() {
        previewGc.setFill(backgroundColor);
        previewGc.fillRect(0, 0, previewCanvas.getWidth(), previewCanvas.getHeight());
        if (gameOver) {
            return;
        }
        int offsetX = (int) ((previewCanvas.getWidth() - nextShape[0].length * blockSize) / 2);
        int offsetY = (int) ((previewCanvas.getHeight() - nextShape.length * blockSize) / 2);
        for (int row = 0; row < nextShape.length; row++) {
            for (int col = 0; col < nextShape[row].length; col++) {
                if (nextShape[row][col]) {
                    previewGc.setFill(nextColor);
                    previewGc.fillRect(offsetX + col * blockSize, offsetY + row * blockSize,
                            blockSize - 1, blockSize - 1);
                    if (bordersCheck.isSelected()) {
                        previewGc.setStroke(borderColor);
                        previewGc.setLineWidth(1);
                        previewGc.strokeRect(offsetX + col * blockSize, offsetY + row * blockSize,
                                blockSize - 1, blockSize - 1);
                    }
                }
            }
        }
    }

    private void renderHold() {
        holdGc.setFill(backgroundColor);
        holdGc.fillRect(0, 0, holdCanvas.getWidth(), holdCanvas.getHeight());
        if (gameOver || holdShapeIndex == -1) {
            return;
        }
        int offsetX = (int) ((holdCanvas.getWidth() - holdShape[0].length * blockSize) / 2);
        int offsetY = (int) ((holdCanvas.getHeight() - holdShape.length * blockSize) / 2);
        for (int row = 0; row < holdShape.length; row++) {
            for (int col = 0; col < holdShape[row].length; col++) {
                if (holdShape[row][col]) {
                    holdGc.setFill(holdColor);
                    holdGc.fillRect(offsetX + col * blockSize, offsetY + row * blockSize,
                            blockSize - 1, blockSize - 1);
                    if (bordersCheck.isSelected()) {
                        holdGc.setStroke(borderColor);
                        holdGc.setLineWidth(1);
                        holdGc.strokeRect(offsetX + col * blockSize, offsetY + row * blockSize,
                                blockSize - 1, blockSize - 1);
                    }
                }
            }
        }
    }

}
