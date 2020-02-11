package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import mara.mybox.data.ColorData;
import mara.mybox.data.StringTable;
import mara.mybox.db.TableColorData;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.blueText;
import static mara.mybox.fxml.FxmlControl.darkRedText;
import mara.mybox.image.ImageColor;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-07-24
 * @Description
 * @License Apache License Version 2.0
 */
public class ColorPaletteController extends BaseController {

    public Control control;
    protected int rectSize = 15;
    protected Rectangle clickedRect, enteredRect;
    protected DropShadow shadowEffect;
    protected double baseHeight;

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
    public ToggleButton pickColorButton;
    @FXML
    protected TextArea selectedArea;
    @FXML
    protected Rectangle selectedRect;
    @FXML
    protected Label sizeLabel, promptLabel, titleLabel;
    @FXML
    protected Button htmlButton;
    @FXML
    protected TextField nameInput;

    public ColorPaletteController() {
        baseTitle = AppVariables.message("ColorPalette");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            shadowEffect = new DropShadow();
            colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> ov,
                        Color oldVal, Color newVal) {
                    if (isSettingValues) {
                        return;
                    }
                    setColor(newVal);
                }
            });

            promptLabel.setStyle(darkRedText);
            pickColorButton.setVisible(false);

            deleteButton.disableProperty().bind(
                    selectedArea.textProperty().isEmpty()
            );
            selectedRect.visibleProperty().bind(
                    selectedArea.textProperty().isNotEmpty()
            );
            saveButton.disableProperty().bind(Bindings.isEmpty(nameInput.textProperty())
                    .or(Bindings.isEmpty(selectedArea.textProperty()))
            );
            htmlButton.disableProperty().bind(Bindings.isEmpty(colorsPane.getChildren()));

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void toFront() {
        baseHeight = headerBox.getHeight() + barBox.getHeight()
                + promptLabel.getHeight() + closeBox.getHeight() + 110;
        colorsPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov,
                    Number oldVal, Number newVal) {
                if (!isSettingValues && Math.abs(newVal.doubleValue() - oldVal.doubleValue()) > 10) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            adjustHeight();
                        }
                    });
                }
            }
        });

        load();
        adjustHeight();
        super.toFront();
    }

    protected void adjustHeight() {
        try {
            isSettingValues = true;
            myStage.setHeight(baseHeight + scrollPane.getHeight());
            isSettingValues = false;
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void load() {
        colorsPane.getChildren().clear();
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

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
                            logger.error(e.toString());
                        }
                    }
                    isSettingValues = false;
                    checkPane();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void checkPane() {
        if (isSettingValues) {
            return;
        }
        int size = colorsPane.getChildren().size();
        sizeLabel.setText(AppVariables.message("Count") + ": " + size);
        sizeLabel.setStyle(blueText);
    }

    public void init(BaseController parent, Control control, String title,
            boolean pickColor) {
        parentController = parent;
        this.control = control;
//        logger.debug(control.getClass() + " " + control.getId());
        Rectangle2D screenBounds = FxmlControl.getScreen();
        double controlX = FxmlControl.getX(control);
        double controlY = FxmlControl.getY(control);
        int offset = 20;
//        logger.debug(control.getClass() + " " + control.getId() + " " + controlX + " " + controlY);
        if (controlX + control.getWidth() + getMyStage().getWidth() > screenBounds.getWidth()) {
            getMyStage().setX(Math.max(0, controlX - getMyStage().getWidth() - offset));
        } else {
            getMyStage().setX(Math.min(screenBounds.getWidth() - offset, controlX + control.getWidth() + offset));
        }
        if (controlY + getMyStage().getHeight() > screenBounds.getHeight()) {
            getMyStage().setY(Math.max(0, screenBounds.getHeight() - getMyStage().getHeight()));
        } else {
            getMyStage().setY(Math.max(0, controlY - offset));
        }
        titleLabel.setText(title);

        if (pickColor) {
            pickColorButton.setVisible(true);
            isPickingColor.bind(pickColorButton.selectedProperty());
            isPickingColor.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean oldVal, Boolean newVal) {
                    if (newVal) {
                        promptLabel.setText(AppVariables.message("PickingColorsNow"));
                    } else {
                        promptLabel.setText("");
                    }
                }
            });
            parentController.getIsPickingColor().bind(isPickingColor);
        } else {
            parentController.getIsPickingColor().unbind();
            pickColorButton.setVisible(false);
        }
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
                    colorPicker.setValue(data.getColor());
                    isSettingValues = false;
                    if (parentController != null) {
                        parentController.setColor(control, color);
                        if (saveCloseCheck.isSelected()) {
                            closeStage();
                        }
                    }
                    Rectangle rect1 = (Rectangle) event.getSource();
                    isSettingValues = true;
                    if (clickedRect != null) {
                        clickedRect.setEffect(null);
                        clickedRect.setWidth(15);
                        clickedRect.setHeight(15);
                    }
                    rect1.setEffect(shadowEffect);
                    rect1.setWidth(25);
                    rect1.setHeight(25);
                    clickedRect = rect1;
                    selectedRect.setFill(color);
                    selectedRect.setUserData(rect1.getUserData());
                    try {
                        ColorData data1 = (ColorData) rect1.getUserData();
                        nameInput.setText(data1.getColorName());
                        selectedArea.setText(data1.display());
                    } catch (Exception e) {
                    }
                    isSettingValues = false;

                });
            });
            rect.setOnMouseEntered((MouseEvent event) -> {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Rectangle rect = (Rectangle) event.getSource();
                        if (rect.equals(enteredRect) || rect.equals(clickedRect)) {
                            return;
                        }
                        isSettingValues = true;
                        if (enteredRect != null && !enteredRect.equals(clickedRect)) {
                            enteredRect.setEffect(null);
                            enteredRect.setWidth(15);
                            enteredRect.setHeight(15);
                        }
                        rect.setEffect(shadowEffect);
                        rect.setWidth(20);
                        rect.setHeight(20);
                        enteredRect = rect;
                        isSettingValues = false;
                    }
                });
            });

            if (ahead) {
                colorsPane.getChildren().add(0, rect);
            } else {
                colorsPane.getChildren().add(rect);
            }
            checkPane();
            return rect;
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    public void commonColorsAction() {
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    List<Color> colors = new ArrayList<>();
                    for (Node node : colorsPane.getChildren()) {
                        Rectangle rect = (Rectangle) node;
                        colors.add((Color) rect.getFill());
                    }
                    List<Color> commonColors = FxmlColor.commonColors();
                    for (int i = commonColors.size() - 1; i >= 0; --i) {
                        Color color = commonColors.get(i);
                        if (!colors.contains(color)) {
                            colors.add(0, color);
                        }
                    }
                    TableColorData.updatePaletteColor(colors);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    load();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void saveAction() {
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
                if (!pickColorButton.isVisible() && pos < colorsPane.getChildren().size()) {
                    Rectangle rect = (Rectangle) colorsPane.getChildren().get(pos);
                    FxmlControl.fireMouseClicked(rect);
                }

            }
            isSettingValues = false;
            adjustHeight();

        } catch (Exception e) {
            logger.error(e.toString());
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
            adjustHeight();
            TableColorData.clearPalette();
        } catch (Exception e) {
            logger.error(e.toString());
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
                row.addAll(Arrays.asList((id++) + "", name, color.toString(),
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
            logger.error(e.toString());
        }
    }

    @FXML
    public void dataAction() {
        openStage(CommonValues.ManageColorsFxml);
    }

    @FXML
    public void exitPane() {
        if (enteredRect != null && !enteredRect.equals(clickedRect)) {
            enteredRect.setEffect(null);
            enteredRect.setWidth(15);
            enteredRect.setHeight(15);
            enteredRect = null;
        }
    }

    @Override
    public boolean leavingScene() {
        if (!super.leavingScene()) {
            return false;
        }
        pickColorButton.setSelected(false);
        return true;
    }

}
