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
import mara.mybox.bufferedimage.PixelsBlend.ImagesBlendMode;
import mara.mybox.bufferedimage.PixelsBlendFactory;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-10
 * @License Apache License Version 2.0
 */
public class ImageManufactureTextController extends ImageManufactureOperationController {

    protected int x, y, fontSize, shadow, angle;
    protected ImagesBlendMode blendMode;
    protected float opacity;
    protected Font font;
    protected String fontFamily, fontName;
    protected FontPosture fontPosture;
    protected FontWeight fontWeight;

    @FXML
    protected TextField textInput, xInput, yInput;
    @FXML
    protected ComboBox<String> sizeBox, opacitySelector, blendSelector, styleBox, familyBox, angleBox, shadowBox;
    @FXML
    protected CheckBox verticalCheck, outlineCheck, blendTopCheck, ignoreTransparentCheck;
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

            fontFamily = UserConfig.getString(baseName + "TextFontFamily", "Arial");
            fontWeight = FontWeight.NORMAL;
            fontPosture = FontPosture.REGULAR;
            fontSize = 24;
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
                    UserConfig.setString(baseName + "TextFontFamily", newValue);
                    write(true);
                }
            });
            familyBox.getSelectionModel().select(fontFamily);

            List<String> styles = Arrays.asList(Languages.message("Regular"), Languages.message("Bold"), Languages.message("Italic"), Languages.message("Bold Italic"));
            styleBox.getItems().addAll(styles);
            styleBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (Languages.message("Bold").equals(newValue)) {
                        fontWeight = FontWeight.BOLD;
                        fontPosture = FontPosture.REGULAR;

                    } else if (Languages.message("Italic").equals(newValue)) {
                        fontWeight = FontWeight.NORMAL;
                        fontPosture = FontPosture.ITALIC;

                    } else if (Languages.message("Bold Italic").equals(newValue)) {
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
                            UserConfig.setInt(baseName + "TextFontSize", v);
                            write(true);
                            ValidationTools.setEditorNormal(sizeBox);
                        } else {
                            ValidationTools.setEditorBadStyle(sizeBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(sizeBox);
                    }
                }
            });
            sizeBox.getSelectionModel().select(UserConfig.getInt(baseName + "TextFontSize", 72) + "");

            String mode = UserConfig.getString(baseName + "TextBlendMode", Languages.message("NormalMode"));
            blendMode = PixelsBlendFactory.blendMode(mode);
            blendSelector.getItems().addAll(PixelsBlendFactory.blendModes());
            blendSelector.setValue(mode);
            blendSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    String mode = blendSelector.getSelectionModel().getSelectedItem();
                    blendMode = PixelsBlendFactory.blendMode(mode);
                    UserConfig.setString(baseName + "TextBlendMode", mode);
                    write(true);
                }
            });

            opacity = UserConfig.getInt(baseName + "TextOpacity", 100) / 100f;
            opacity = (opacity >= 0.0f && opacity <= 1.0f) ? opacity : 1.0f;
            opacitySelector.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            opacitySelector.setValue(opacity + "");
            opacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        float f = Float.valueOf(newValue);
                        if (opacity >= 0.0f && opacity <= 1.0f) {
                            opacity = f;
                            UserConfig.setInt(baseName + "TextOpacity", (int) (f * 100));
                            ValidationTools.setEditorNormal(opacitySelector);
                            write(true);
                        } else {
                            ValidationTools.setEditorBadStyle(opacitySelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(opacitySelector);
                    }
                }
            });

            blendTopCheck.setSelected(UserConfig.getBoolean(baseName + "TextBlendTop", true));
            blendTopCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    UserConfig.setBoolean(baseName + "TextBlendTop", blendTopCheck.isSelected());
                    write(true);
                }
            });

            ignoreTransparentCheck.setSelected(UserConfig.getBoolean(baseName + "IgnoreTransparent", true));
            ignoreTransparentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    UserConfig.setBoolean(baseName + "IgnoreTransparent", ignoreTransparentCheck.isSelected());
                    write(true);
                }
            });

            shadowBox.getItems().addAll(Arrays.asList("0", "4", "5", "3", "2", "1", "6"));
            shadowBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            shadow = v;
                            UserConfig.setInt(baseName + "TextShadow", v);
                            ValidationTools.setEditorNormal(shadowBox);
                            write(true);
                        } else {
                            ValidationTools.setEditorBadStyle(shadowBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(shadowBox);
                    }
                }
            });
            shadowBox.getSelectionModel().select(UserConfig.getInt(baseName + "TextShadow", 0) + "");

            angleBox.getItems().addAll(Arrays.asList("0", "90", "180", "270", "45", "135", "225", "315",
                    "60", "150", "240", "330", "15", "105", "195", "285", "30", "120", "210", "300"));
            angleBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            angle = v;
                            UserConfig.setInt(baseName + "TextAngle", v);
                            ValidationTools.setEditorNormal(angleBox);
                            write(true);
                        } else {
                            ValidationTools.setEditorBadStyle(angleBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(angleBox);
                    }
                }
            });
            angleBox.getSelectionModel().select(UserConfig.getInt(baseName + "TextAngle", 0) + "");

            outlineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "TextOutline", outlineCheck.isSelected());
                    write(true);
                }
            });
            outlineCheck.setSelected(UserConfig.getBoolean(baseName + "TextOutline", false));

            verticalCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "TextVertical", verticalCheck.isSelected());
                    write(true);
                }
            });
            verticalCheck.setSelected(UserConfig.getBoolean(baseName + "TextVertical", false));

            isSettingValues = false;

            textInput.setText(UserConfig.getString(baseName + "TextValue", "MyBox"));
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
        imageController.imageTab();
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
                    newImage = FxImageTools.addText(imageView.getImage(), textInput.getText().trim(),
                            font, (Color) colorSetController.rect.getFill(), x, y,
                            blendMode, opacity, !blendTopCheck.isSelected(), ignoreTransparentCheck.isSelected(),
                            shadow, angle, outlineCheck.isSelected(), verticalCheck.isSelected());
                    if (task == null || isCancelled()) {
                        return false;
                    }
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
            imageController.handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
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
                xInput.setStyle(NodeStyleTools.badStyle);
                return false;
            } else {
                x = v;
                xInput.setStyle(null);
            }
        } catch (Exception e) {
            xInput.setStyle(NodeStyleTools.badStyle);
            return false;
        }
        try {
            int v = Integer.parseInt(yInput.getText().trim());
            if (v < 0 || v >= imageView.getImage().getHeight()) {
                yInput.setStyle(NodeStyleTools.badStyle);
                return false;
            } else {
                y = v;
                yInput.setStyle(null);
            }
        } catch (Exception e) {
            yInput.setStyle(NodeStyleTools.badStyle);
            return false;
        }
        if (textInput.getText().isBlank()) {
            textInput.setStyle(NodeStyleTools.badStyle);
            return false;
        } else {
            textInput.setStyle(null);
            UserConfig.setString(baseName + "TextValue", textInput.getText().trim());
        }
        return true;
    }

    @FXML
    @Override
    public void goAction() {
        if (!check()) {
            popError(Languages.message("InvalidParameters"));
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
