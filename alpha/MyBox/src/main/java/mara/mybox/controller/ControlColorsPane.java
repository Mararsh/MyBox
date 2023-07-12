package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.SingletonBackgroundTask;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2020-09-04
 * @License Apache License Version 2.0
 */
public class ControlColorsPane extends BaseController {

    protected ColorsManageController manageController;
    protected ColorPaletteName palette;
    protected TableColorPalette tableColorPalette;
    protected Rectangle clickedRect, enteredRect;
    protected DropShadow shadowEffect;
    protected double rectSize;
    protected SimpleBooleanProperty clickNotify, loadedNotify;
    protected boolean canDragDrop;

    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected FlowPane colorsPane;
    @FXML
    protected Label colorsPaneLabel;

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableColorPalette = new TableColorPalette();
            clickNotify = new SimpleBooleanProperty(false);
            loadedNotify = new SimpleBooleanProperty(false);
            shadowEffect = new DropShadow();
            rectSize = AppVariables.iconSize * 0.8;
            canDragDrop = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameter(ColorsManageController manageController, boolean canDragDrop) {
        this.manageController = manageController;
        this.canDragDrop = canDragDrop;
    }

    public boolean canDragDrop() {
        if (!canDragDrop) {
            return false;
        }
        return manageController == null || !manageController.palettesController.isAllColors();
    }

    public void loadPalette(ColorPaletteName palette, boolean scrollEnd) {
        if (palette == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            List<ColorData> colors;

            @Override
            protected boolean handle() {
                colors = tableColorPalette.colors(palette.getCpnid());
                return true;
            }

            @Override
            protected void whenSucceeded() {
                loadColors(palette, colors, scrollEnd);
            }

        };
        start(task);
    }

    public void loadColors(ColorPaletteName palette, List<ColorData> colors) {
        loadColors(palette, colors, false);
    }

    public synchronized void loadColors(ColorPaletteName palette, List<ColorData> colors, boolean scrollEnd) {
        this.palette = palette;
        if (canDragDrop()) {
            if (!thisPane.getChildren().contains(colorsPaneLabel)) {
                thisPane.getChildren().add(0, colorsPaneLabel);
            }
        } else {
            if (thisPane.getChildren().contains(colorsPaneLabel)) {
                thisPane.getChildren().remove(colorsPaneLabel);
            }
        }
        colorsPane.getChildren().clear();
        if (colors == null || colors.isEmpty()) {
            return;
        }
        if (backgroundTask != null) {
            backgroundTask.cancel();
            backgroundTask = null;
        }
        backgroundTask = new SingletonBackgroundTask<Void>(this) {
            List<Rectangle> rects = new ArrayList<>();

            @Override
            protected boolean handle() {
                for (ColorData data : colors) {
                    if (isCancelled()) {
                        return true;
                    }
                    Rectangle rect = makeColorRect(data);
                    if (rect == null) {
                        continue;
                    }
                    rects.add(rect);
                    if (rects.size() >= 50) {
                        List<Rectangle> display = new ArrayList<>();
                        display.addAll(rects);
                        rects.clear();
                        Platform.runLater(() -> {
                            if (isCancelled()) {
                                return;
                            }
                            colorsPane.getChildren().addAll(display);
                            if (scrollEnd) {
                                scrollPane.setVvalue(1.0);
                            }
                        });
                    }
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (isCancelled()) {
                    return;
                }
                if (!rects.isEmpty()) {
                    colorsPane.getChildren().addAll(rects);
                }
                if (scrollEnd) {
                    scrollPane.setVvalue(1.0);
                }
            }

            @Override
            protected void finalAction() {
                loadedNotify.set(!loadedNotify.get());
            }

        };
        start(backgroundTask, false);
    }

    public Rectangle makeColorRect(ColorData data) {
        try {
            if (data == null) {
                return null;
            }
            Rectangle rect = new Rectangle(rectSize, rectSize);
            rect.setUserData(data);
            NodeStyleTools.setTooltip(rect, new Tooltip(data.display()));
            Color color = data.getColor();
            rect.setFill(color);
            rect.setStroke(Color.BLACK);
            rect.setOnMouseClicked((MouseEvent event) -> {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        rectClicked(rect);
                    }
                });
            });
            rect.setOnMouseEntered((MouseEvent event) -> {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        rectEntered(rect);
                    }
                });
            });
            if (canDragDrop()) {
                rect.setOnDragDetected(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        try {
                            Dragboard dragboard = rect.startDragAndDrop(TransferMode.ANY);
                            ClipboardContent content = new ClipboardContent();
                            content.putString(FxColorTools.color2rgba(color));
                            dragboard.setContent(content);
                            event.consume();
                        } catch (Exception e) {
                            MyBoxLog.debug(e.toString());
                        }
                    }
                });
                rect.setOnDragOver(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent event) {
                        try {
                            rectEntered(rect);
                            event.acceptTransferModes(TransferMode.ANY);
                            event.consume();
                        } catch (Exception e) {
                            MyBoxLog.debug(e.toString());
                        }
                    }
                });
                rect.setOnDragDropped(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent event) {
                        colorDropped(event, rect);
                    }
                });
            }
            return rect;
        } catch (Exception e) {
            return null;
        }
    }

    public void rectClicked(Rectangle rect) {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        try {
            ColorData data = (ColorData) rect.getUserData();
            if (clickedRect != null) {
                clickedRect.setEffect(null);
                clickedRect.setWidth(rectSize);
                clickedRect.setHeight(rectSize);
                clickedRect.setStroke(Color.BLACK);
                clickedRect.setUserData(data);
            }
            rect.setEffect(shadowEffect);
            rect.setWidth(rectSize * 1.6);
            rect.setHeight(rectSize * 1.6);
            rect.setStroke(Color.RED);
            clickedRect = rect;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        isSettingValues = false;
        clickNotify.set(!clickNotify.get());
    }

    public void rectEntered(Rectangle rect) {
        if (isSettingValues || rect.equals(enteredRect) || rect.equals(clickedRect)) {
            return;
        }
        isSettingValues = true;
        if (enteredRect != null && !enteredRect.equals(clickedRect)) {
            enteredRect.setEffect(null);
            enteredRect.setWidth(rectSize);
            enteredRect.setHeight(rectSize);
            enteredRect.setStroke(Color.BLACK);
        }
        rect.setEffect(shadowEffect);
        rect.setWidth(rectSize * 1.4);
        rect.setHeight(rectSize * 1.4);
        rect.setStroke(Color.BLUE);
        enteredRect = rect;
        isSettingValues = false;
    }

    public synchronized void colorDropped(DragEvent event, Rectangle targetRect) {
        if (event == null || !canDragDrop()) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        if (targetRect == null) {
            event.setDropCompleted(true);
            event.consume();
            return;
        }
        List<Node> nodes = colorsPane.getChildren();
        if (nodes.isEmpty()) {
            event.setDropCompleted(true);
            event.consume();
            return;
        }
        Color sourceColor = Color.web(event.getDragboard().getString());
        task = new SingletonCurrentTask<Void>(this) {

            private List<ColorData> colors = null;

            @Override
            protected boolean handle() {
                try {
                    ColorData targetData = (ColorData) targetRect.getUserData();
                    int sourceValue = FxColorTools.color2Value(sourceColor);
                    int targetValue = targetData.getColorValue();
                    int sourceIndex = -1, targetIndex = -1;
                    colors = new ArrayList<>();
                    ColorData sourceColor = null;
                    for (int i = 0; i < nodes.size(); i++) {
                        Node node = nodes.get(i);
                        ColorData data = (ColorData) node.getUserData();
                        if (data.getColorValue() == sourceValue) {
                            sourceIndex = i;
                            sourceColor = data;
                        }
                        if (data.getColorValue() == targetValue) {
                            targetIndex = i;
                        }
                        colors.add(data);
                    }
                    if (sourceIndex < 0 || targetIndex < 0) {
                        return true;
                    }
                    float f0 = colors.get(0).getOrderNumner();
                    float fn = colors.get(colors.size() - 1).getOrderNumner();
                    if (f0 == fn) {
                        fn = f0 + 0.0001f;
                    }
                    float offset = (fn - f0) / (colors.size() - 1);
                    colors.remove(sourceIndex);
                    colors.add(sourceIndex < targetIndex ? targetIndex : targetIndex + 1, sourceColor);
                    for (int i = 0; i < colors.size(); i++) {
                        ColorData data = colors.get(i);
                        data.setOrderNumner(f0 + offset * i);
                    }
                    tableColorPalette.write(palette.getCpnid(), colors, true, false);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (manageController != null) {
                    manageController.refreshPalette();
                } else {
                    loadPalette(palette, false);
                }
            }

            @Override
            protected void finalAction() {
                event.setDropCompleted(true);
                event.consume();
                task = null;
            }

        };
        start(task);
    }

    @FXML
    public void exitPane() {
        if (enteredRect != null && !enteredRect.equals(clickedRect)) {
            enteredRect.setEffect(null);
            enteredRect.setWidth(rectSize);
            enteredRect.setHeight(rectSize);
            enteredRect = null;
        }
    }

    public ColorData clickedColor() {
        if (clickedRect == null) {
            return null;
        }
        return (ColorData) clickedRect.getUserData();
    }

}
