package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import mara.mybox.data.StringTable;
import mara.mybox.db.TableSRGB;
import mara.mybox.db.TableStringValues;
import mara.mybox.fxml.FxmlColor;
import static mara.mybox.fxml.FxmlColor.colorName;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.blueText;
import static mara.mybox.fxml.FxmlControl.darkRedText;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-07-24
 * @Description
 * @License Apache License Version 2.0
 */
public class ColorPaletteController extends BaseController {

    public Control control;
    protected List<Color> colors;
    protected int rectSize = 15;
    protected Rectangle clickedRect, enteredRect;
    protected DropShadow shadowEffect;
    protected double baseHeight;

    @FXML
    protected VBox headerBox;
    @FXML
    protected HBox barBox, closeBox;
    @FXML
    protected ScrollPane colorsPane;
    @FXML
    protected FlowPane colorsBox;
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
    protected CheckBox saveCloseCheck;
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
            shadowEffect = new DropShadow();

            saveCloseCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    AppVariables.setUserConfigValue("ColorPaletteSaveClose", saveCloseCheck.isSelected());
                }
            });
            saveCloseCheck.setSelected(AppVariables.getUserConfigBoolean("ColorPaletteSaveClose", false));

            colorsBox.getChildren().addListener(new ListChangeListener<Node>() {
                @Override
                public void onChanged(Change<? extends Node> c) {
                    int size = colorsBox.getChildren().size();
                    sizeLabel.setText(AppVariables.message("Count") + ": " + size);
                    sizeLabel.setStyle(blueText);
                    htmlButton.setDisable(size == 0);
                }
            });
            htmlButton.setDisable(true);

            colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> ov, Color oldVal, Color newVal) {
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

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void toFront() {
        baseHeight = headerBox.getHeight() + barBox.getHeight()
                + promptLabel.getHeight() + closeBox.getHeight() + 110;
        colorsBox.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
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
        isSettingValues = true;
        colors = new ArrayList<>();
        List<String> saveColors = TableStringValues.read("ColorPalette");
        for (String c : saveColors) {
            try {
                String cc = c.trim();
                if (cc.isEmpty()) {
                    continue;
                }
                Color color = Color.web(cc);
                addColor(color, false);
            } catch (Exception e) {
                logger.error(e.toString());
            }
        }
        isSettingValues = false;

        adjustHeight();
        super.toFront();
    }

    protected void adjustHeight() {
        try {
            isSettingValues = true;
            myStage.setHeight(baseHeight + colorsPane.getHeight());
            isSettingValues = false;
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void init(BaseController parent, Control control, String title, boolean pickColor) {
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
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
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
        Rectangle rect = addColor(color, true);
        if (rect != null) {
            FxmlControl.fireMouseClicked(rect);
            TableStringValues.add("ColorPalette", color.toString());
            return true;
        } else {
            selectedArea.setText("");
            selectedRect.setFill(null);
            return false;
        }
    }

    protected Rectangle addColor(Color color, boolean ahead) {
        try {
            if (color == null || colors.contains(color)) {
                return null;
            }
            Rectangle rect = new Rectangle(rectSize, rectSize);
            rect.setFill(color);
            rect.setStroke(Color.BLACK);
            rect.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (parentController != null) {
                                parentController.setColor(control, color);
                            }
                            if (saveCloseCheck.isSelected()) {
                                closeStage();

                            } else {
                                Rectangle rect = (Rectangle) event.getSource();
                                isSettingValues = true;
                                if (clickedRect != null) {
                                    clickedRect.setEffect(null);
                                    clickedRect.setWidth(15);
                                    clickedRect.setHeight(15);
                                }
                                rect.setEffect(shadowEffect);
                                rect.setWidth(25);
                                rect.setHeight(25);
                                clickedRect = rect;
                                selectedRect.setFill(color);
                                String name = FxmlColor.colorName(color);
                                String display = FxmlColor.colorDisplay(color);
                                nameInput.setText(name);
                                if (name == null) {
                                    selectedArea.setText(display);
                                } else {
                                    selectedArea.setText(name + "\n" + display);
                                }
                                isSettingValues = false;
                            }
                        }
                    });

                }
            });
            rect.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
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
                            FxmlControl.setTooltip(rect,
                                    new Tooltip(FxmlColor.colorNameDisplay(color)));

                        }
                    });
                }
            });

            int size = colors.size();
            if (size >= 32672 / 11) {
                colors.remove(size - 1);
                colorsBox.getChildren().remove(size - 1);
            }
            if (ahead) {
                colors.add(0, color);
                colorsBox.getChildren().add(0, rect);
            } else {
                colors.add(color);
                colorsBox.getChildren().add(rect);
            }

            return rect;
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    public void commonColorsAction() {
        try {
            isSettingValues = true;
            List<Color> commonColors = FxmlColor.commonColors();
            List<String> values = new ArrayList<>();
            for (Color color : commonColors) {
                addColor(color, false);
                values.add(color.toString());
            }
            isSettingValues = false;
            adjustHeight();

            TableStringValues.add("ColorPalette", values);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (nameInput.getText().isEmpty() || selectedRect.getFill() == null) {
            return;
        }
        if (TableSRGB.name(((Color) selectedRect.getFill()).toString(), nameInput.getText())) {
            Color color = (Color) clickedRect.getFill();
            String s = FxmlColor.colorNameDisplay(color);
            selectedArea.setText(s);
            FxmlControl.setTooltip(clickedRect, s);
            popSuccessul();
        } else {
            popFailed();
        }

    }

    @FXML
    @Override
    public void deleteAction() {
        try {
            isSettingValues = true;
            Color c = (Color) selectedRect.getFill();
            if (!colors.remove(c)) {
                return;
            }
            selectedRect.setFill(null);
            selectedArea.setText("");
            if (clickedRect != null) {
                int pos = colorsBox.getChildren().indexOf(clickedRect);
                if (pos >= 0) {
                    colorsBox.getChildren().remove(pos);
                    clickedRect = null;
                }
                if (!pickColorButton.isVisible() && pos < colorsBox.getChildren().size()) {
                    Rectangle rect = (Rectangle) colorsBox.getChildren().get(pos);
                    FxmlControl.fireMouseClicked(rect);
                }

            }
            isSettingValues = false;
            adjustHeight();
            TableStringValues.delete("ColorPalette", c.toString());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void clearAction() {
        try {
            isSettingValues = true;
            colors.clear();
            colorsBox.getChildren().clear();
            clickedRect = null;
            enteredRect = null;
            isSettingValues = false;
            adjustHeight();
            TableStringValues.clear("ColorPalette");
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void htmlAction() {
        try {
            if (colors == null || colors.isEmpty()) {
                return;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("ID"), message("Name"), "RGBA", "RGB",
                    message("Red"), message("Green"), message("Blue"), message("Opacity"),
                    message("Hue"), message("Brightness"), message("Saturation")
            ));
            StringTable table = new StringTable(names, message("ColorPalette"), 3);
            int id = 1;
            for (Color color : colors) {
                List<String> row = new ArrayList<>();
                String name = colorName(color);
                if (name == null) {
                    name = "";
                }
                int red = (int) Math.round(color.getRed() * 255);
                int green = (int) Math.round(color.getGreen() * 255);
                int blue = (int) Math.round(color.getBlue() * 255);
//                float alpha = (float) color.getOpacity();
//                String cString = "rgba(" + red + "," + green + "," + blue + "," + alpha + ")";
                String cString = "#" + color.toString().substring(2, 8);
                row.addAll(Arrays.asList((id++) + "", name, color.toString(), cString,
                        red + " ", green + " ", blue + " ",
                        (int) Math.round(color.getOpacity() * 100) + "%",
                        Math.round(color.getHue()) + " ",
                        Math.round(color.getSaturation() * 100) + "%",
                        Math.round(color.getBrightness() * 100) + "%"
                ));
                table.add(row);
            }
            table.display();

        } catch (Exception e) {
            logger.error(e.toString());
        }
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

    @FXML
    public void closeAction() {
        this.closeStage();
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
