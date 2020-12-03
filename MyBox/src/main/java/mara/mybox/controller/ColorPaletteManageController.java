package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.data.BaseTask;
import mara.mybox.data.ColorData;
import mara.mybox.data.StringTable;
import mara.mybox.db.TableColorData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageColor;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-07-24
 * @Description
 * @License Apache License Version 2.0
 */
public class ColorPaletteManageController extends BaseController {

    public Control control;
    protected Rectangle clickedRect, enteredRect;
    protected DropShadow shadowEffect;
    protected double rectSize;

    @FXML
    protected VBox headerBox;
    @FXML
    protected HBox barBox, closeBox;
    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected FlowPane colorsPane;
    @FXML
    public ColorPicker colorPicker;
    @FXML
    protected ColorImportController colorImportController;
    @FXML
    protected TextArea selectedArea;
    @FXML
    protected Rectangle selectedRect;
    @FXML
    protected Label sizeLabel;
    @FXML
    protected Button htmlButton, dataButton;
    @FXML
    protected TextField nameInput;

    public ColorPaletteManageController() {
        baseTitle = AppVariables.message("ColorPaletteManage");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            rectSize = AppVariables.iconSize * 0.8;

            colorImportController.setParentController(this);

            shadowEffect = new DropShadow();
            colorPicker.valueProperty().addListener(
                    (ObservableValue<? extends Color> ov, Color oldVal, Color newVal) -> {
                        if (isSettingValues) {
                            return;
                        }
                        setColor(newVal);
                    });

            deleteButton.disableProperty().bind(
                    selectedArea.textProperty().isEmpty()
            );
            selectedRect.visibleProperty().bind(
                    selectedArea.textProperty().isNotEmpty()
            );
            okButton.disableProperty().bind(Bindings.isEmpty(nameInput.textProperty())
                    .or(Bindings.isEmpty(selectedArea.textProperty()))
            );
            htmlButton.disableProperty().bind(Bindings.isEmpty(colorsPane.getChildren()));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public void toFront() {
        load();
        super.toFront();
        FxmlControl.setTooltip(dataButton, message("ManageColors"));
    }

    public void load() {
        colorsPane.getChildren().clear();
        synchronized (this) {
            BaseTask loadTask = new BaseTask<Void>() {

                protected List<ColorData> colors;

                @Override
                protected boolean handle() {
                    colors = TableColorData.readPalette();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    for (ColorData data : colors) {
                        try {
                            addColor(data, false);
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                        }
                    }
                    isSettingValues = false;
                    updateSizeLabel();
                }
            };
            openHandlingStage(loadTask, Modality.WINDOW_MODAL);
            loadTask.setSelf(loadTask);
            Thread thread = new Thread(loadTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void updateSizeLabel() {
        if (isSettingValues) {
            return;
        }
        int size = colorsPane.getChildren().size();
        sizeLabel.setText(AppVariables.message("Count") + ": " + size);

    }

    public boolean setColor(Color color) {
        if (isSettingValues) {
            return false;
        }
        Rectangle rect = findRect(color);
        if (rect == null) {
            rect = addColor(color, true);
        }
        if (rect != null) {
            FxmlControl.fireMouseClicked(rect);
            return true;
        }
        selectedArea.setText("");
        selectedRect.setFill(null);
        return false;

    }

    public Rectangle findRect(Color color) {
        int colorValue = ImageColor.getRGB(color);
        for (Node node : colorsPane.getChildren()) {
            try {
                ColorData data = (ColorData) node.getUserData();
                if (data.getColorValue() == colorValue) {
                    return (Rectangle) node;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public Rectangle findRect(ColorData data) {
        for (Node node : colorsPane.getChildren()) {
            try {
                ColorData nodeData = (ColorData) node.getUserData();
                if (nodeData.getColorValue() == data.getColorValue()) {
                    return (Rectangle) node;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public int findIndex(Color color) {
        int colorValue = ImageColor.getRGB(color);
        List<Node> nodes = colorsPane.getChildren();
        for (int i = 0; i < nodes.size(); i++) {
            try {
                Node node = nodes.get(i);
                ColorData data = (ColorData) node.getUserData();
                if (data.getColorValue() == colorValue) {
                    return i;
                }
            } catch (Exception e) {
            }
        }
        return -1;
    }

    protected Rectangle addColor(Color color, boolean ahead) {
        try {
            if (color == null) {
                return null;
            }
            ColorData data;
            if (ahead) {
                data = TableColorData.frontPalette(color);
            } else {
                data = TableColorData.endPalette(color);
            }
            return addColor(data, ahead);
        } catch (Exception e) {
            return null;
        }
    }

    protected Rectangle addColor(ColorData data, boolean ahead) {
        try {
            if (data == null) {
                return null;
            }
            Rectangle rect = new Rectangle(rectSize, rectSize);
            rect.setUserData(data);
            FxmlControl.setTooltip(rect, new Tooltip(data.display()));
            Color color = data.getColor();
            rect.setFill(color);
            rect.setStroke(Color.BLACK);
            rect.setOnMouseClicked((MouseEvent event) -> {
                Platform.runLater(() -> {
                    isSettingValues = true;
                    try {
                        Rectangle source = (Rectangle) event.getSource();
                        ColorData sourceData = (ColorData) source.getUserData();
                        colorPicker.setValue(sourceData.getColor());
                        if (clickedRect != null) {
                            clickedRect.setEffect(null);
                            clickedRect.setWidth(rectSize);
                            clickedRect.setHeight(rectSize);
                            clickedRect.setStroke(Color.BLACK);
                        }
                        source.setEffect(shadowEffect);
                        source.setWidth(rectSize * 1.6);
                        source.setHeight(rectSize * 1.6);
                        source.setStroke(Color.RED);
                        clickedRect = source;
                        selectedRect.setFill(sourceData.getColor());
                        selectedRect.setUserData(sourceData);
                        nameInput.setText(sourceData.getColorName());
                        selectedArea.setText(sourceData.display());
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                    isSettingValues = false;

                });
            });
            rect.setOnMouseEntered((MouseEvent event) -> {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Rectangle source = (Rectangle) event.getSource();
                        if (source.equals(enteredRect) || source.equals(clickedRect)) {
                            return;
                        }
                        rectEntered(source);
                    }
                });
            });
            rect.setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    try {
                        Dragboard dragboard = rect.startDragAndDrop(TransferMode.ANY);
                        ClipboardContent content = new ClipboardContent();
                        content.putString(FxmlColor.color2rgba(color));
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
                    try {
                        Dragboard dragboard = event.getDragboard();
                        List<Node> nodes = colorsPane.getChildren();
                        Rectangle source = findRect(Color.web(dragboard.getString()));
                        nodes.remove(source);
                        int targetIndex = findIndex(color);
                        nodes.add(targetIndex + 1, source);
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                    event.setDropCompleted(true);
                    event.consume();
                }
            });

            if (ahead) {
                colorsPane.getChildren().add(0, rect);
            } else {
                colorsPane.getChildren().add(rect);
            }
            updateSizeLabel();
            return rect;
        } catch (Exception e) {
            return null;
        }
    }

    public void rectEntered(Rectangle rect) {
        if (rect.equals(enteredRect) || rect.equals(clickedRect)) {
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

    @FXML
    @Override
    public void okAction() {
        String name = nameInput.getText().trim();
        if (name.isEmpty() || selectedRect.getUserData() == null) {
            return;
        }
        ColorData data = (ColorData) clickedRect.getUserData();
        if (TableColorData.setName(data.getRgba(), name)) {
            data.setColorName(name);
            data.setColorDisplay(null);
            String s = data.display();
            selectedArea.setText(s);
            FxmlControl.setTooltip(clickedRect, s);
            popSuccessful();
        } else {
            popFailed();
        }

    }

    @FXML
    @Override
    public void saveAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    List<ColorData> colors = new ArrayList<>();
                    for (Node node : colorsPane.getChildren()) {
                        try {
                            ColorData nodeData = (ColorData) node.getUserData();
                            colors.add(nodeData);
                        } catch (Exception e) {
                        }
                    }
                    TableColorData.setPaletteColors(colors, true);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    updateSizeLabel();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void deleteAction() {
        try {
            isSettingValues = true;
            TableColorData.removeFromPalette((Color) selectedRect.getFill());
            selectedRect.setFill(null);
            selectedArea.setText("");
            if (clickedRect != null) {
                int pos = colorsPane.getChildren().indexOf(clickedRect);
                if (pos >= 0) {
                    colorsPane.getChildren().remove(pos);
                    clickedRect = null;
                }
                if (pos < colorsPane.getChildren().size()) {
                    Rectangle rect = (Rectangle) colorsPane.getChildren().get(pos);
                    FxmlControl.fireMouseClicked(rect);
                }
            }
            isSettingValues = false;
            updateSizeLabel();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void clearAction() {
        try {
            isSettingValues = true;
            colorsPane.getChildren().clear();
            clickedRect = null;
            enteredRect = null;
            isSettingValues = false;
            TableColorData.clearPalette();
            updateSizeLabel();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void refreshAction() {
        load();
    }

    @FXML
    public void htmlAction() {
        try {
            List<Node> nodes = colorsPane.getChildren();
            if (nodes == null || nodes.isEmpty()) {
                return;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("ID"), message("Name"), message("Color"),
                    message("Red"), message("Green"), message("Blue"), message("Opacity"),
                    message("Hue"), message("Brightness"), message("Saturation")
            ));
            StringTable table = new StringTable(names, message("ColorPalette"), 2);
            int id = 1;
            for (Node node : nodes) {
                ColorData data = null;
                try {
                    data = (ColorData) node.getUserData();
                } catch (Exception e) {
                }
                if (data == null) {
                    continue;
                }
                List<String> row = new ArrayList<>();
                String name = data.getColorName();
                if (name == null) {
                    name = "";
                }
                Color color = data.getColor();
                int red = (int) Math.round(color.getRed() * 255);
                int green = (int) Math.round(color.getGreen() * 255);
                int blue = (int) Math.round(color.getBlue() * 255);
//                float alpha = (float) color.getOpacity();
//                String cString = "rgba(" + red + "," + green + "," + blue + "," + alpha + ")";
                row.addAll(Arrays.asList((id++) + "", name, FxmlColor.color2rgba(color),
                        red + " ", green + " ", blue + " ",
                        (int) Math.round(color.getOpacity() * 100) + "%",
                        Math.round(color.getHue()) + " ",
                        Math.round(color.getSaturation() * 100) + "%",
                        Math.round(color.getBrightness() * 100) + "%"
                ));
                table.add(row);
            }
            table.editHtml();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void dataAction() {
        ColorsManageController.oneOpen();
    }

    public boolean addData(List<ColorData> data) {
        List<Color> palette = new ArrayList<>();
        for (Node node : colorsPane.getChildren()) {
            Rectangle rect = (Rectangle) node;
            palette.add((Color) rect.getFill());
        }
        List<ColorData> newColors = new ArrayList<>();
        for (ColorData color : data) {
            if (!palette.contains(color.getColor())) {
                newColors.add(color);
            }
        }
        TableColorData.addDataInPalette(newColors, true);
        Platform.runLater(() -> {
            load();
        });
        return true;
    }

    public void addColors(List<Color> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        synchronized (this) {
            BaseTask addTask = new BaseTask<Void>() {
                @Override
                protected boolean handle() {
                    List<Color> palette = new ArrayList<>();
                    for (Node node : colorsPane.getChildren()) {
                        Rectangle rect = (Rectangle) node;
                        palette.add((Color) rect.getFill());
                    }
                    List<Color> newColors = new ArrayList<>();
                    for (Color color : data) {
                        if (!palette.contains(color)) {
                            newColors.add(color);
                        }
                    }
                    return TableColorData.addColorsInPalette(newColors);
                }

                @Override
                protected void whenSucceeded() {
                    load();
                }
            };
            addTask.setSelf(addTask);
            Thread thread = new Thread(addTask);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @FXML
    public void exitPane() {
        if (enteredRect != null && !enteredRect.equals(clickedRect)) {
            enteredRect.setEffect(null);
            enteredRect.setWidth(rectSize);
            enteredRect.setHeight(rectSize);
            enteredRect.setStroke(Color.BLACK);
            enteredRect = null;
        }
    }

    @FXML
    @Override
    public void closePopup(KeyEvent event) {
        super.closePopup(event);
        colorImportController.closePopup(event);
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (parentController != null && (parentController instanceof ImageBaseController)) {
            ImageBaseController c = (ImageBaseController) parentController;
            if (c.pickColorCheck != null && c.pickColorCheck.isSelected()) {
                c.pickColorCheck.setSelected(false);
            }
        }
        return true;
    }

    public static ColorPaletteManageController oneOpen() {
        ColorPaletteManageController controller = null;
        Stage stage = FxmlStage.findStage(message("ColorPaletteManage"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (ColorPaletteManageController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (ColorPaletteManageController) FxmlStage.openStage(CommonValues.ColorPaletteManageFxml);
        }
        if (controller != null) {
            controller.getMyStage().toFront();
        }
        return controller;
    }

}
