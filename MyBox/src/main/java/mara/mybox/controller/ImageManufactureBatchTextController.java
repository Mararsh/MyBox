package mara.mybox.controller;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.image.ImageConvertTools;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.fxml.FxmlImageTools;
import static mara.mybox.fxml.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchTextController extends ImageManufactureBatchController {

    private final String ImageTextShadowKey, ImageFontFamilyKey, ImageTextColorKey;
    private int waterSize, waterAngle, waterShadow, waterX, waterY, positionType, textWidth, textHeight, margin;
    private float waterTransparent;
    private java.awt.Font font;
    private java.awt.Color color;

    @FXML
    private ChoiceBox<String> waterFamilyBox, waterStyleBox;
    @FXML
    private ComboBox<String> waterSizeBox, waterShadowBox, waterAngleBox, waterTransparentBox;
    @FXML
    private ColorPicker waterColorPicker;
    @FXML
    private ToggleGroup positionGroup;
    @FXML
    private TextField waterInput, waterXInput, waterYInput, marginInput;

    private class PositionType {

        static final int RightBottom = 0;
        static final int RightTop = 1;
        static final int LeftBottom = 2;
        static final int LeftTop = 3;
        static final int Center = 4;
        static final int Custom = 5;
    }

    public ImageManufactureBatchTextController() {
        ImageTextShadowKey = "ImageTextShadowKey";
        ImageFontFamilyKey = "ImageFontFamilyKey";
        ImageTextColorKey = "ImageTextColorKey";

    }

    @Override
    protected void initializeNext2() {
        try {

            operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceFilesInformation))
                    .or(Bindings.isEmpty(waterInput.textProperty()))
                    .or(waterXInput.styleProperty().isEqualTo(badStyle))
                    .or(waterYInput.styleProperty().isEqualTo(badStyle))
                    .or(marginInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    protected void initOptionsSection() {
        try {

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

            positionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkPositionType();
                }
            });
            checkPositionType();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkPositionType() {
        waterXInput.setDisable(true);
        waterXInput.setStyle(null);
        waterYInput.setDisable(true);
        waterYInput.setStyle(null);
        marginInput.setDisable(true);
        marginInput.setStyle(null);

        RadioButton selected = (RadioButton) positionGroup.getSelectedToggle();
        if (getMessage("RightBottom").equals(selected.getText())) {
            positionType = PositionType.RightBottom;
            marginInput.setDisable(false);
            checkMargin();

        } else if (getMessage("RightTop").equals(selected.getText())) {
            positionType = PositionType.RightTop;
            marginInput.setDisable(false);
            checkMargin();

        } else if (getMessage("LeftBottom").equals(selected.getText())) {
            positionType = PositionType.LeftBottom;
            marginInput.setDisable(false);
            checkMargin();

        } else if (getMessage("LeftTop").equals(selected.getText())) {
            positionType = PositionType.LeftTop;
            marginInput.setDisable(false);
            checkMargin();

        } else if (getMessage("Center").equals(selected.getText())) {
            positionType = PositionType.Center;

        } else if (getMessage("Custom").equals(selected.getText())) {
            positionType = PositionType.Custom;
            waterXInput.setDisable(false);
            waterYInput.setDisable(false);
            checkWaterPosition();
        }
    }

    private void checkMargin() {
        try {
            margin = Integer.valueOf(marginInput.getText());
            if (margin >= 0) {
                marginInput.setStyle(null);
            } else {
                marginInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            marginInput.setStyle(badStyle);
        }

    }

    private void checkWaterPosition() {
        try {
            waterX = Integer.valueOf(waterXInput.getText());
            if (waterX >= 0) {
                waterXInput.setStyle(null);
            } else {
                waterXInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            waterXInput.setStyle(badStyle);
        }

        try {
            waterY = Integer.valueOf(waterYInput.getText());
            if (waterY >= 0) {
                waterYInput.setStyle(null);
            } else {
                waterYInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            waterYInput.setStyle(badStyle);
        }

    }

    @Override
    protected void makeMoreParameters() {
        super.makeMoreParameters();

        String fontFamily = (String) waterFamilyBox.getSelectionModel().getSelectedItem();
        String fontStyle = (String) waterStyleBox.getSelectionModel().getSelectedItem();

        Font FxFont;
        if (AppVaribles.getMessage("Bold").equals(fontStyle)) {
            font = new java.awt.Font(fontFamily, java.awt.Font.BOLD, waterSize);
            FxFont = Font.font(fontFamily, FontWeight.BOLD, FontPosture.REGULAR, waterSize);

        } else if (AppVaribles.getMessage("Italic").equals(fontStyle)) {
            font = new java.awt.Font(fontFamily, java.awt.Font.ITALIC, waterSize);
            FxFont = Font.font(fontFamily, FontWeight.NORMAL, FontPosture.ITALIC, waterSize);

        } else if (AppVaribles.getMessage("Bold Italic").equals(fontStyle)) {
            font = new java.awt.Font(fontFamily, java.awt.Font.BOLD + java.awt.Font.ITALIC, waterSize);
            FxFont = Font.font(fontFamily, FontWeight.BOLD, FontPosture.ITALIC, waterSize);

        } else {
            font = new java.awt.Font(fontFamily, java.awt.Font.PLAIN, waterSize);
            FxFont = Font.font(fontFamily, FontWeight.NORMAL, FontPosture.REGULAR, waterSize);
        }

        color = FxmlImageTools.colorConvert(waterColorPicker.getValue());

        final String msg = waterInput.getText().trim();
        final Text text = new Text(msg);
        text.setFont(FxFont);
        textWidth = (int) Math.round(text.getLayoutBounds().getWidth());
        textHeight = (int) Math.round(text.getLayoutBounds().getHeight());

    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            if (waterInput.getText() == null || waterInput.getText().trim().isEmpty()) {
                return null;
            }

            int x, y;
            switch (positionType) {
                case PositionType.Center:
                    x = (source.getWidth() - textWidth) / 2;
                    y = (source.getHeight() + textHeight) / 2;
                    break;
                case PositionType.RightBottom:
                    x = source.getWidth() - textWidth - margin;
                    y = source.getHeight() - margin;
                    break;
                case PositionType.RightTop:
                    x = source.getWidth() - textWidth - margin;
                    y = textHeight + margin;
                    break;
                case PositionType.LeftBottom:
                    x = margin;
                    y = source.getHeight() - margin;
                    break;
                case PositionType.Custom:
                    x = waterX - textWidth / 2;
                    y = waterY + textHeight / 2;
                    break;
                default:
                    x = margin;
                    y = textHeight + margin;
                    break;
            }

            BufferedImage target = ImageConvertTools.addText(source,
                    waterInput.getText().trim(), font, color,
                    x, y, waterTransparent, waterShadow, waterAngle, false);

            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}
