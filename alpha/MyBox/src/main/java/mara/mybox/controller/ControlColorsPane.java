package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2020-09-04
 * @License Apache License Version 2.0
 */
public class ControlColorsPane extends BaseController {

    protected ColorsManageController manageController;
    protected TableColorPalette tableColorPalette;
    protected Rectangle clickedRect, enteredRect;
    protected DropShadow shadowEffect;
    protected double rectSize;
    protected SimpleBooleanProperty clickNotify;

    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected FlowPane colorsPane;

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableColorPalette = new TableColorPalette();
            clickNotify = new SimpleBooleanProperty(false);
            shadowEffect = new DropShadow();
            rectSize = AppVariables.iconSize * 0.8;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public synchronized void loadPalette(long paletteid) {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {
            List<ColorData> colors;

            @Override
            protected boolean handle() {
                colors = tableColorPalette.colors(paletteid);
                return true;
            }

            @Override
            protected void whenSucceeded() {
                loadColors(colors);
                scrollPane.setVvalue(1.0);
            }

        };
        start(task);
    }

    public synchronized void loadColors(List<ColorData> colors) {
        if (colors == null || colors.isEmpty()) {
            colorsPane.getChildren().clear();
            return;
        }
        List<Rectangle> rects = new ArrayList<>();
        for (ColorData data : colors) {
            Rectangle rect = makeColorRect(data);
            if (rect != null) {
                rects.add(rect);
            }
        }
        colorsPane.getChildren().setAll(rects);
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
            if (manageController != null && !manageController.isAllColors()) {
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
        if (event == null || manageController == null) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        if (targetRect == null || manageController.isAllColors()) {
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
        task = new SingletonTask<Void>(this) {

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
                    manageController.tableColorPalette.write(manageController.currentPalette.getCpnid(), colors, true, false);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                manageController.refreshPalette();
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
