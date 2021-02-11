package mara.mybox.controller;

import java.awt.Font;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-10
 * @License Apache License Version 2.0
 */
public class ImageManufactureTextController extends ImageManufactureOperationController {

    protected float opacity;
    protected int x, y, fontSize, shadow, angle;
    protected float fontOpacity = 0.5f;
    protected Font font;
    protected String fontFamily, fontName;
    protected FontPosture fontPosture;
    protected FontWeight fontWeight;

    @FXML
    protected TextField textInput, xInput, yInput;
    @FXML
    protected ComboBox<String> sizeBox, opacityBox, styleBox, familyBox, angleBox, shadowBox;
    @FXML
    protected CheckBox verticalCheck, outlineCheck;
    @FXML
    protected ColorSet colorSetController;
    @FXML
    protected FlowPane setBox;
    @FXML
    protected HBox opBox;
    @FXML
    protected Label commentsLabel;

    @Override
    public void initPane() {
        try {
            isSettingValues = true;

            fontFamily = AppVariables.getUserConfigValue(baseName + "TextFontFamily", "Arial");
            fontWeight = FontWeight.NORMAL;
            fontPosture = FontPosture.REGULAR;
            fontSize = 24;
            fontOpacity = 1.0f;
            shadow = 0;
            angle = 0;

            colorSetController.init(this, baseName + "TextColor", Color.ORANGE);
            colorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    write(true);
                }
            });

            familyBox.getItems().addAll(javafx.scene.text.Font.getFamilies());
            familyBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    fontFamily = newValue;
                    AppVariables.setUserConfigValue(baseName + "TextFontFamily", newValue);
                    write(true);
                }
            });
            familyBox.getSelectionModel().select(fontFamily);

            List<String> styles = Arrays.asList(message("Regular"), message("Bold"), message("Italic"), message("Bold Italic"));
            styleBox.getItems().addAll(styles);
            styleBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (AppVariables.message("Bold").equals(newValue)) {
                        fontWeight = FontWeight.BOLD;
                        fontPosture = FontPosture.REGULAR;

                    } else if (AppVariables.message("Italic").equals(newValue)) {
                        fontWeight = FontWeight.NORMAL;
                        fontPosture = FontPosture.ITALIC;

                    } else if (AppVariables.message("Bold Italic").equals(newValue)) {
                        fontWeight = FontWeight.BOLD;
                        fontPosture = FontPosture.ITALIC;

                    } else {
                        fontWeight = FontWeight.NORMAL;
                        fontPosture = FontPosture.REGULAR;

                    }
                    write(true);
                }
            });
            styleBox.getSelectionModel().select(0);

            List<String> sizes = Arrays.asList(
                    "72", "18", "15", "9", "10", "12", "14", "17", "24", "36", "48", "64", "96");
            sizeBox.getItems().addAll(sizes);
            sizeBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            fontSize = v;
                            AppVariables.setUserConfigInt(baseName + "TextFontSize", v);
                            write(true);
                            FxmlControl.setEditorNormal(sizeBox);
                        } else {
                            FxmlControl.setEditorBadStyle(sizeBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(sizeBox);
                    }
                }
            });
            sizeBox.getSelectionModel().select(AppVariables.getUserConfigInt(baseName + "TextFontSize", 72) + "");

            opacityBox.getItems().addAll(Arrays.asList("1.0", "0.5", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            opacityBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        float f = Float.valueOf(newValue);
                        if (f >= 0.0f && f <= 1.0f) {
                            fontOpacity = f;
                            AppVariables.setUserConfigValue(baseName + "TextFontOpacity", f + "");
                            FxmlControl.setEditorNormal(opacityBox);
                            write(true);
                        } else {
                            FxmlControl.setEditorBadStyle(opacityBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(opacityBox);
                    }
                }
            });
            opacityBox.getSelectionModel().select(AppVariables.getUserConfigValue(baseName + "TextFontOpacity", "1.0"));

            shadowBox.getItems().addAll(Arrays.asList("0", "4", "5", "3", "2", "1", "6"));
            shadowBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            shadow = v;
                            AppVariables.setUserConfigInt(baseName + "TextShadow", v);
                            FxmlControl.setEditorNormal(shadowBox);
                            write(true);
                        } else {
                            FxmlControl.setEditorBadStyle(shadowBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(shadowBox);
                    }
                }
            });
            shadowBox.getSelectionModel().select(AppVariables.getUserConfigInt(baseName + "TextShadow", 0) + "");

            angleBox.getItems().addAll(Arrays.asList("0", "90", "180", "270", "45", "135", "225", "315",
                    "60", "150", "240", "330", "15", "105", "195", "285", "30", "120", "210", "300"));
            angleBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            angle = v;
                            AppVariables.setUserConfigInt(baseName + "TextAngle", v);
                            FxmlControl.setEditorNormal(angleBox);
                            write(true);
                        } else {
                            FxmlControl.setEditorBadStyle(angleBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(angleBox);
                    }
                }
            });
            angleBox.getSelectionModel().select(AppVariables.getUserConfigInt(baseName + "TextAngle", 0) + "");

            outlineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "TextOutline", outlineCheck.isSelected());
                    write(true);
                }
            });
            outlineCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "TextOutline", false));

            verticalCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    AppVariables.setUserConfigValue(baseName + "TextVertical", verticalCheck.isSelected());
                    write(true);
                }
            });
            verticalCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "TextVertical", false));

            isSettingValues = false;

            textInput.setText(AppVariables.getUserConfigValue(baseName + "TextValue", "MyBox"));
            xInput.setText((int) (imageView.getImage().getWidth() / 2) + "");
            yInput.setText((int) (imageView.getImage().getHeight() / 2) + "");
            goAction();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        imageController.resetImagePane();
        imageController.hideScopePane();
        imageController.showImagePane();
    }

    @FXML
    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
        if (imageView.getImage() == null || p == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (imageController.isPickingColor || scopeController.isPickingColor
                || event.getButton() == MouseButton.SECONDARY) {
            return;
        }
        imageView.setCursor(Cursor.HAND);
        xInput.setText((int) Math.round(p.getX()) + "");
        yInput.setText((int) Math.round(p.getY()) + "");
        goAction();
    }

    public void write(boolean editing) {
        if (isSettingValues || x < 0 || y < 0
                || x >= imageView.getImage().getWidth()
                || y >= imageView.getImage().getHeight()
                || textInput.getText().isBlank()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    if (fontWeight == FontWeight.BOLD) {
                        if (fontPosture == FontPosture.REGULAR) {
                            font = new java.awt.Font(fontFamily, java.awt.Font.BOLD, fontSize);
                        } else {
                            font = new java.awt.Font(fontFamily, java.awt.Font.BOLD + java.awt.Font.ITALIC, fontSize);
                        }
                    } else {
                        if (fontPosture == FontPosture.REGULAR) {
                            font = new java.awt.Font(fontFamily, java.awt.Font.PLAIN, fontSize);
                        } else {
                            font = new java.awt.Font(fontFamily, java.awt.Font.ITALIC, fontSize);
                        }
                    }
                    newImage = FxmlImageManufacture.addText(imageView.getImage(), textInput.getText().trim(),
                            font, (Color) colorSetController.rect.getFill(), x, y,
                            fontOpacity, shadow, angle,
                            outlineCheck.isSelected(), verticalCheck.isSelected());
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    if (editing) {
                        maskView.setImage(newImage);
                        maskView.setOpacity(1);
                        maskView.setVisible(true);
                        imageView.setVisible(false);
                        imageView.toBack();

                    } else {
                        imageController.popSuccessful();
                        imageController.updateImage(ImageOperation.Text, textInput.getText().trim(), null, newImage, cost);
                    }
                }

            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public boolean check() {
        if (imageView.getImage() == null) {
            return false;
        }
        try {
            int v = Integer.parseInt(xInput.getText().trim());
            if (v < 0 || v >= imageView.getImage().getWidth()) {
                xInput.setStyle(badStyle);
                return false;
            } else {
                x = v;
                xInput.setStyle(null);
            }
        } catch (Exception e) {
            xInput.setStyle(badStyle);
            return false;
        }
        try {
            int v = Integer.parseInt(yInput.getText().trim());
            if (v < 0 || v >= imageView.getImage().getHeight()) {
                yInput.setStyle(badStyle);
                return false;
            } else {
                y = v;
                yInput.setStyle(null);
            }
        } catch (Exception e) {
            yInput.setStyle(badStyle);
            return false;
        }
        if (textInput.getText().isBlank()) {
            textInput.setStyle(badStyle);
            return false;
        } else {
            textInput.setStyle(null);
            AppVariables.setUserConfigValue(baseName + "TextValue", textInput.getText().trim());
        }
        return true;
    }

    @FXML
    @Override
    public void goAction() {
        if (!check()) {
            popError(message("InvalidParameters"));
            return;
        }
        write(true);
    }

    @FXML
    @Override
    public void okAction() {
        write(false);
    }

    @FXML
    @Override
    public void cancelAction() {
        imageController.resetImagePane();
    }
}
