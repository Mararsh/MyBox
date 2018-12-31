package mara.mybox.controller;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.fxml.FxmlImageTools;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureTextController extends ImageManufactureController {

    final protected String ImageFontSizeKey, ImageFontFamilyKey, ImageTextColorKey, ImageTextShadowKey;
    protected int waterX, waterY, waterSize, waterShadow, waterAngle;
    protected float waterTransparent = 0.5f;

    @FXML
    protected ToolBar textBar;
    @FXML
    protected TextField waterInput, waterXInput, waterYInput;
    @FXML
    protected ComboBox waterSizeBox, waterTransparentBox, waterShadowBox, waterAngleBox;
    @FXML
    protected ColorPicker waterColorPicker;
    @FXML
    protected ChoiceBox waterStyleBox, waterFamilyBox;
    @FXML
    protected CheckBox outlineCheck;

    public ImageManufactureTextController() {
        ImageFontSizeKey = "ImageFontSizeKey";
        ImageFontFamilyKey = "ImageFontFamilyKey";
        ImageTextColorKey = "ImageTextColorKey";
        ImageTextShadowKey = "ImageTextShadowKey";
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initTextTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            super.initInterface();

            isSettingValues = true;
            if (CommonValues.NoAlphaImages.contains(values.getImageInfo().getImageFormat())) {
                waterTransparentBox.setDisable(true);
                waterTransparentBox.getSelectionModel().select("1.0");
            } else {
                waterTransparentBox.setDisable(false);
            }

            if (image.getWidth() > 2000 || image.getHeight() > 2000) {
                waterShadowBox.setDisable(true);
            } else {
                waterShadowBox.setDisable(false);
            }

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initTextTab() {
        try {

            Tooltip tips = new Tooltip(getMessage("textComments"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(textBar, tips);

            waterXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkWaterPosition();
                }
            });
            waterYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkWaterPosition();
                }
            });

            List<String> sizes = Arrays.asList(
                    "72", "18", "15", "9", "10", "12", "14", "17", "24", "36", "48", "64", "96");
            waterSizeBox.getItems().addAll(sizes);
            waterSizeBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        waterSize = Integer.valueOf(newValue);
                        waterSizeBox.getEditor().setStyle(null);
                    } catch (Exception e) {
                        waterSize = 15;
                        waterSizeBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            waterSizeBox.getSelectionModel().select(0);

            waterTransparentBox.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            waterTransparentBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        waterTransparent = Float.valueOf(newValue);
                        if (waterTransparent >= 0.0f && waterTransparent <= 1.0f) {
                            waterSizeBox.getEditor().setStyle(null);
                        } else {
                            waterTransparent = 0.5f;
                            waterSizeBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        waterTransparent = 0.5f;
                        waterSizeBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            waterTransparentBox.getSelectionModel().select(0);

            waterShadowBox.getItems().addAll(Arrays.asList("0", "4", "5", "3", "2", "1", "6"));
            waterShadowBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        waterShadow = Integer.valueOf(newValue);
                        if (waterShadow >= 0) {
                            waterShadowBox.getEditor().setStyle(null);
                            AppVaribles.setUserConfigValue(ImageTextShadowKey, newValue);
                        } else {
                            waterShadow = 0;
                            waterShadowBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        waterShadow = 0;
                        waterShadowBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            waterShadowBox.getSelectionModel().select(AppVaribles.getUserConfigValue(ImageTextShadowKey, "0"));

            List<String> styles = Arrays.asList(getMessage("Regular"), getMessage("Bold"), getMessage("Italic"), getMessage("Bold Italic"));
            waterStyleBox.getItems().addAll(styles);
            waterStyleBox.getSelectionModel().select(0);

            GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] fontNames = e.getAvailableFontFamilyNames();
            waterFamilyBox.getItems().addAll(Arrays.asList(fontNames));
            waterFamilyBox.getSelectionModel().select(AppVaribles.getUserConfigValue(ImageFontFamilyKey, fontNames[0]));
            waterFamilyBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    AppVaribles.setUserConfigValue(ImageFontFamilyKey, newValue);
                }
            });

            waterColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> observable,
                        Color oldValue, Color newValue) {
                    AppVaribles.setUserConfigValue(ImageTextColorKey, newValue.toString());
                }
            });
            waterColorPicker.setValue(Color.web(AppVaribles.getUserConfigValue(ImageTextColorKey, "#FFFFFF")));

            waterAngleBox.getItems().addAll(Arrays.asList("0", "90", "180", "45", "30", "60", "15", "75", "120", "135"));
            waterAngleBox.setVisibleRowCount(10);
            waterAngleBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        waterAngle = Integer.valueOf(newValue);
                        waterAngleBox.getEditor().setStyle(null);
                    } catch (Exception e) {
                        waterAngle = 0;
                        waterAngleBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            waterAngleBox.getSelectionModel().select(0);
            waterAngle = 0;

            popInformation(getMessage("ClickImageForText"));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkWaterPosition() {
        try {
            waterX = Integer.valueOf(waterXInput.getText());
            if (waterX >= 0 && waterX <= values.getCurrentImage().getWidth() - 1) {
                waterXInput.setStyle(null);
            } else {
                waterXInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            waterXInput.setStyle(badStyle);
        }

        try {
            waterY = Integer.valueOf(waterYInput.getText());
            if (waterY >= 0 && waterY <= values.getCurrentImage().getHeight() - 1) {
                waterYInput.setStyle(null);
            } else {
                waterYInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            waterYInput.setStyle(badStyle);
        }

        if (!isSettingValues) {
            addText();
        }

    }

    @FXML
    @Override
    public void clickImage(MouseEvent event) {
        if (values.getCurrentImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        imageView.setCursor(Cursor.HAND);

        int x = (int) Math.round(event.getX() * values.getCurrentImage().getWidth() / imageView.getBoundsInLocal().getWidth());
        int y = (int) Math.round(event.getY() * values.getCurrentImage().getHeight() / imageView.getBoundsInLocal().getHeight());

        isSettingValues = true;
        waterXInput.setText(x + "");
        waterYInput.setText(y + "");
        isSettingValues = false;

        addText();

    }

    @FXML
    public void addText() {
        if (waterInput.getText() == null || waterInput.getText().trim().isEmpty()
                || waterY < 0 || waterY > values.getCurrentImage().getHeight() - 1
                || waterX < 0 || waterX > values.getCurrentImage().getWidth() - 1) {
            return;
        }
//        String fontFamily = (String) waterFamilyBox.getSelectionModel().getSelectedItem();
//        String fontStyle = (String) waterStyleBox.getSelectionModel().getSelectedItem();
//        Font font;
//        if (AppVaribles.getMessage("Bold").equals(fontStyle)) {
//            font = Font.font(fontFamily, FontWeight.BOLD, waterSize);
//        } else if (AppVaribles.getMessage("Italic").equals(fontStyle)) {
//            font = Font.font(fontFamily, FontWeight.NORMAL, FontPosture.ITALIC, waterSize);
//        } else if (AppVaribles.getMessage("Bold Italic").equals(fontStyle)) {
//            font = Font.font(fontFamily, FontWeight.BOLD, FontPosture.ITALIC, waterSize);
//        } else {
//            font = Font.font(fontFamily, FontWeight.NORMAL, waterSize);
//        }
//        final Image newImage = FxImageTools.addTextFx(currentImage, waterInput.getText(),
//                font, waterColorPicker.getValue(), waterX, waterY, waterTransparent, waterShadow);
//        if (newImage != null) {
//            undoImage = currentImage;
//            currentImage = newImage;
//            imageView.setImage(newImage);
//            imageChanged.set(true);
//            return;
//        }

        // If JavaFx way fail for big image, then go the way of Java2D
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String fontFamily = (String) waterFamilyBox.getSelectionModel().getSelectedItem();
                java.awt.Font font;
                String fontStyle = (String) waterStyleBox.getSelectionModel().getSelectedItem();
                if (AppVaribles.getMessage("Bold").equals(fontStyle)) {
                    font = new java.awt.Font(fontFamily, java.awt.Font.BOLD, waterSize);
                } else if (AppVaribles.getMessage("Italic").equals(fontStyle)) {
                    font = new java.awt.Font(fontFamily, java.awt.Font.ITALIC, waterSize);
                } else if (AppVaribles.getMessage("Bold Italic").equals(fontStyle)) {
                    font = new java.awt.Font(fontFamily, java.awt.Font.BOLD + java.awt.Font.ITALIC, waterSize);
                } else {
                    font = new java.awt.Font(fontFamily, java.awt.Font.PLAIN, waterSize);
                }
                final Image newImage = FxmlImageTools.addText(values.getCurrentImage(), waterInput.getText(),
                        font, waterColorPicker.getValue(), waterX, waterY,
                        waterTransparent, waterShadow, waterAngle, outlineCheck.isSelected());
                if (task.isCancelled()) {
                    return null;
                }
                recordImageHistory(ImageOperationType.Text, newImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

}
