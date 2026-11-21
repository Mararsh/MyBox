package mara.mybox.controller;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2026-11-21
 * @License Apache License Version 2.0
 *
 * Assisted by Doubao
 */
public class GameSnakeController extends BaseGameController {

    private int blockSize, interval, width, height, dx, dy;
    private Color foodColor, snakeColor;

    private final LinkedList<int[]> snake = new LinkedList<>();
    private int[] food;

    private Canvas canvas;

    @FXML
    protected CheckBox wallCheck;
    @FXML
    protected ComboBox<String> intervalSelector, blockSelector, widthSelector, heightSelector;
    @FXML
    protected ControlColorSet foodColorController, snakeColorController;

    public GameSnakeController() {
        baseTitle = message("GameSnake");
        TipsLabelKey = "GameSnakeTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            gameOver = true;
            paused = false;
            random = new Random();
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initSize();
            initSound();
            initSpeed();
            initColors();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initSpeed() {
        try {
            interval = UserConfig.getInt(baseName + "Interval", 120);
            if (interval < 10) {
                interval = 120;
            }
            intervalSelector.getItems().addAll(Arrays.asList(
                    "120", "200", "100", "70", "180", "300", "500", "240", "50", "260")
            );
            intervalSelector.setValue(interval + "");
            intervalSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String o, String n) {
                    try {
                        int iv = (int) Double.parseDouble(n);
                        if (iv >= 10) {
                            interval = iv;
                            UserConfig.setInt(baseName + "Interval", interval);
                            if (!gameOver) {
                                restartTimer();
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            });

            wallCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "CrossWall", wallCheck.isSelected());
                }
            });
            wallCheck.setSelected(UserConfig.getBoolean(baseName + "CrossWall", false));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void initSize() {
        try {
            blockSize = UserConfig.getInt(baseName + "BlockSize", 20);
            if (blockSize < 0) {
                blockSize = 20;
            }
            blockSelector.getItems().addAll(Arrays.asList(
                    "20", "30", "10", "25", "35", "40", "45", "15", "50", "60", "55")
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

            width = UserConfig.getInt(baseName + "Width", 400);
            if (width < 100) {
                width = 400;
            }
            widthSelector.getItems().addAll(Arrays.asList(
                    "400", "800", "1000", "600", "300", "200", "2000", "1500", "1200")
            );
            widthSelector.setValue(width + "");
            widthSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String o, String n) {
                    try {
                        int iv = (int) Double.parseDouble(n);
                        if (iv > 0) {
                            width = iv;
                            UserConfig.setInt(baseName + "Width", width);
                        }
                    } catch (Exception e) {
                    }
                }
            });

            height = UserConfig.getInt(baseName + "Height", 400);
            if (height < 100) {
                height = 400;
            }
            heightSelector.getItems().addAll(Arrays.asList(
                    "400", "800", "1000", "600", "300", "200", "2000", "1500", "1200")
            );
            heightSelector.setValue(height + "");
            heightSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String o, String n) {
                    try {
                        int iv = (int) Double.parseDouble(n);
                        if (iv > 0) {
                            height = iv;
                            UserConfig.setInt(baseName + "Height", height);
                        }
                    } catch (Exception e) {
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void initColors() {
        try {
            super.initColors();

            foodColorController.init(this, baseName + "FoodColor", Color.MAGENTA);
            foodColorController.setNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    foodColor = foodColorController.color();
                }
            });
            foodColor = foodColorController.color();

            snakeColorController.init(this, baseName + "SnakeColor", Color.LIME);
            snakeColorController.setNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    snakeColor = snakeColorController.color();
                }
            });
            snakeColor = snakeColorController.color();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void startGame() {
        try {
            if (blockSize < 1 || width < blockSize || height < blockSize) {
                popError(message("InvalidParameters"));
                return;
            }
            super.startGame();

            score = 0;
            dx = blockSize;
            dy = 0;
            snake.clear();
            snake.add(new int[]{blockSize * 5, blockSize * 5});
            snake.add(new int[]{blockSize * 4, blockSize * 5});
            snake.add(new int[]{blockSize * 3, blockSize * 5});
            spawnFood();

            canvas = new Canvas(width, height);
            gameContext = canvas.getGraphicsContext2D();
            gameBox.getChildren().clear();
            gameBox.getChildren().add(canvas);
            gameBox.requestFocus();
            canvas.setFocusTraversable(true);

            restartTimer();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void restartTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (gameOver || paused) {
                    return;
                }
                Platform.runLater(() -> {
                    update();
                    draw();
                });
            }
        }, interval, interval);
    }

    private void update() {
        int[] head = snake.getFirst();
        int newX = head[0] + dx;
        int newY = head[1] + dy;

        if (wallCheck.isSelected()) {
            if (newX < 0) {
                newX = width - blockSize;
            }
            if (newX >= width) {
                newX = 0;
            }
            if (newY < 0) {
                newY = height - blockSize;
            }
            if (newY >= height) {
                newY = 0;
            }
        } else {
            if (newX < 0 || newX >= width || newY < 0 || newY >= height) {
                stopGame();
                return;
            }
        }

        for (int[] seg : snake) {
            if (seg[0] == newX && seg[1] == newY) {
                stopGame();
                return;
            }
        }

        snake.addFirst(new int[]{newX, newY});

        if (newX == food[0] && newY == food[1]) {
            score += 10;
            spawnFood();
            playSound(false);
        } else {
            snake.removeLast();
        }
    }

    private void draw() {
        gameContext.setFill(bgColor);
        gameContext.fillRect(0, 0, width, height);

        gameContext.setGlobalAlpha(0.25f);
        gameContext.setStroke(gridColor);
        gameContext.setLineWidth(0.5);
        for (int x = 0; x <= width; x += blockSize) {
            gameContext.strokeLine(x, 0, x, height);
        }
        for (int y = 0; y <= height; y += blockSize) {
            gameContext.strokeLine(0, y, width, y);
        }
        gameContext.setGlobalAlpha(1.0);

        for (int i = 0; i < snake.size(); i++) {
            int[] seg = snake.get(i);
            double ratio = 1.0 - (double) i / snake.size();
            gameContext.setGlobalAlpha(ratio);
            gameContext.setFill(snakeColor);
            gameContext.fillRect(seg[0], seg[1], blockSize - 1, blockSize - 1);
        }
        gameContext.setGlobalAlpha(1.0);

        gameContext.setFill(foodColor);
        gameContext.fillRect(food[0], food[1], blockSize - 1, blockSize - 1);

        writeScore();
    }

    private void writeScore() {
        scoreLabel.setText(message("Score") + ": " + score);
    }

    private void spawnFood() {
        int x, y;
        boolean overlap;
        do {
            overlap = false;
            x = random.nextInt(width / blockSize) * blockSize;
            y = random.nextInt(height / blockSize) * blockSize;
            for (int[] seg : snake) {
                if (seg[0] == x && seg[1] == y) {
                    overlap = true;
                    break;
                }
            }
        } while (overlap);
        food = new int[]{x, y};
    }

    @Override
    public boolean keyLeft() {
        if (gameOver || paused) {
            return false;
        }
        if (dx == 0) {
            dx = -blockSize;
            dy = 0;
        }
        return true;
    }

    @Override
    public boolean keyRight() {
        if (gameOver || paused) {
            return false;
        }
        if (dx == 0) {
            dx = blockSize;
            dy = 0;
        }
        return true;
    }

    @Override
    public boolean keyUp() {
        if (gameOver || paused) {
            return false;
        }
        if (dy == 0) {
            dx = 0;
            dy = -blockSize;
        }
        return true;
    }

    @Override
    public boolean keyDown() {
        if (gameOver || paused) {
            return false;
        }
        if (dy == 0) {
            dx = 0;
            dy = blockSize;
        }
        return true;
    }

    @Override
    public boolean keySpace() {
        if (gameOver) {
            return false;
        }
        if (paused) {
            resumeGame();
        } else {
            pauseGame();
        }
        return true;
    }

}
