package mara.mybox.controller;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import mara.mybox.controller.base.ImageManufactureController;
import mara.mybox.data.DoublePoint;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureTextController extends ImageManufactureController {

    final protected String ImageFontSizeKey, ImageFontFamilyKey, ImageTextColorKey, ImageTextShadowKey;
    protected int waterX, waterY, fontSize, waterShadow, waterAngle;
    protected float fontOpacity = 0.5f;
    protected Font waterFont;
    protected String fontFamily, fontName;
    protected Color fontColor;
    protected java.awt.Font font;
    protected FontPosture fontPosture;
    protected FontWeight fontWeight;

    @FXML
    protected ToolBar textBar;
    @FXML
    protected TextField waterInput;
    @FXML
    protected ComboBox waterSizeBox, opacityBox, waterShadowBox, waterAngleBox;
    @FXML
    protected ChoiceBox waterStyleBox, waterFamilyBox;
    @FXML
    protected CheckBox outlineCheck, verticalCheck;
    @FXML
    private ImageView maskImageView, textTipsView;

    public ImageManufactureTextController() {
        ImageFontSizeKey = "ImageFontSizeKey";
        ImageFontFamilyKey = "ImageFontFamilyKey";
        ImageTextColorKey = "ImageTextColorKey";
        ImageTextShadowKey = "ImageTextShadowKey";
    }

    @Override
    public void initializeNext2() {
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
            tabPane.getSelectionModel().select(textTab);

            if (values.getImageInfo() != null
                    && CommonValues.NoAlphaImages.contains(values.getImageInfo().getImageFormat())) {
                opacityBox.setDisable(true);
                opacityBox.getSelectionModel().select("1.0");
            } else {
                opacityBox.setDisable(false);
            }

            waterX = (int) (imageView.getImage().getWidth() / 2);
            waterY = (int) (imageView.getImage().getHeight() / 2);

            maskImageView.setVisible(false);
            pickColorButton.setSelected(false);

            isSettingValues = false;

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initTextTab() {
        try {

            FxmlControl.setTooltip(textTipsView, new Tooltip(message("TextComments")));

            fontFamily = AppVaribles.getUserConfigValue(ImageFontFamilyKey, "Arial");
            fontWeight = FontWeight.NORMAL;
            fontPosture = FontPosture.REGULAR;
            fontSize = 24;
            fontColor = Color.RED;
            fontOpacity = 1.0f;
            waterShadow = 0;
            waterAngle = 0;

            waterInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    if (!newValue) {
                        setAction();
                    }
                }
            });

            waterFamilyBox.getItems().addAll(Font.getFamilies());
            waterFamilyBox.getSelectionModel().select(fontFamily);
            waterFamilyBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    fontFamily = newValue;
                    AppVaribles.setUserConfigValue(ImageFontFamilyKey, newValue);
                    setAction();
                }
            });

            List<String> styles = Arrays.asList(message("Regular"), message("Bold"), message("Italic"), message("Bold Italic"));
            waterStyleBox.getItems().addAll(styles);
            waterStyleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (AppVaribles.message("Bold").equals(newValue)) {
                        fontWeight = FontWeight.BOLD;
                        fontPosture = FontPosture.REGULAR;

                    } else if (AppVaribles.message("Italic").equals(newValue)) {
                        font = new java.awt.Font(fontFamily, java.awt.Font.ITALIC, fontSize);
                        fontWeight = FontWeight.NORMAL;
                        fontPosture = FontPosture.ITALIC;

                    } else if (AppVaribles.message("Bold Italic").equals(newValue)) {
                        fontWeight = FontWeight.BOLD;
                        fontPosture = FontPosture.ITALIC;

                    } else {
                        fontWeight = FontWeight.NORMAL;
                        fontPosture = FontPosture.REGULAR;

                    }
                    setAction();
                }
            });
            waterStyleBox.getSelectionModel().select(0);

            List<String> sizes = Arrays.asList(
                    "72", "18", "15", "9", "10", "12", "14", "17", "24", "36", "48", "64", "96");
            waterSizeBox.getItems().addAll(sizes);
            waterSizeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            fontSize = v;
                            setAction();
                            FxmlControl.setEditorNormal(waterSizeBox);
                        } else {
                            FxmlControl.setEditorBadStyle(waterSizeBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(waterSizeBox);
                    }
                }
            });
            waterSizeBox.getSelectionModel().select(0);

            colorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> observable,
                        Color oldValue, Color newValue) {
                    fontColor = newValue;
                    AppVaribles.setUserConfigValue(ImageTextColorKey, newValue.toString());
                    setAction();
                }
            });
            colorPicker.setValue(Color.web(AppVaribles.getUserConfigValue(ImageTextColorKey, "#FF0000")));

            opacityBox.getItems().addAll(Arrays.asList("1.0", "0.5", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            opacityBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        float f = Float.valueOf(newValue);
                        if (f >= 0.0f && f <= 1.0f) {
                            fontOpacity = f;
                            FxmlControl.setEditorNormal(opacityBox);
                            setAction();
                        } else {
                            FxmlControl.setEditorBadStyle(opacityBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(opacityBox);
                    }
                }
            });
            opacityBox.getSelectionModel().select(0);

            waterShadowBox.getItems().addAll(Arrays.asList("0", "4", "5", "3", "2", "1", "6"));
            waterShadowBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            waterShadow = v;
                            AppVaribles.setUserConfigValue(ImageTextShadowKey, newValue);
                            FxmlControl.setEditorNormal(waterShadowBox);
                            setAction();
                        } else {
                            FxmlControl.setEditorBadStyle(waterShadowBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(waterShadowBox);
                    }
                }
            });
            waterShadowBox.getSelectionModel().select(AppVaribles.getUserConfigValue(ImageTextShadowKey, "0"));

            waterAngleBox.getItems().addAll(Arrays.asList("0", "90", "180", "270", "45", "135", "225", "315",
                    "60", "150", "240", "330", "15", "105", "195", "285", "30", "120", "210", "300"));
            waterAngleBox.setVisibleRowCount(10);
            waterAngleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            waterAngle = v;
                            FxmlControl.setEditorNormal(waterAngleBox);
                            setAction();
                        } else {
                            FxmlControl.setEditorBadStyle(waterAngleBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(waterAngleBox);
                    }
                }
            });
            waterAngleBox.getSelectionModel().select(0);

            outlineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    setAction();
                }
            });

            verticalCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    setAction();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void paneClicked(MouseEvent event) {
        if (imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        DoublePoint p = getImageXY(event, imageView);
        if (p == null) {
            return;
        }
        if (pickColorButton.isSelected()) {
            PixelReader pixelReader = imageView.getImage().getPixelReader();
            Color color = pixelReader.getColor((int) Math.round(p.getX()), (int) Math.round(p.getY()));
            colorPicker.setValue(color);

        } else {

            imageView.setCursor(Cursor.HAND);
            waterX = (int) Math.round(p.getX());
            waterY = (int) Math.round(p.getY());

            setAction();
        }

    }

    @FXML
    @Override
    public void recoverAction() {
        super.recoverAction();
        setAction();
    }

    public void setAction() {
        if (isSettingValues || imageView.getImage() == null
                || waterX < 0 || waterY < 0
                || waterX >= imageView.getImage().getWidth()
                || waterY >= imageView.getImage().getHeight()) {
            return;
        }
        maskImageView.setVisible(false);
        task = new Task<Void>() {
            private Image newImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {
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
                newImage = FxmlImageManufacture.addText(imageView.getImage(), waterInput.getText(),
                        font, fontColor, waterX, waterY,
                        fontOpacity, waterShadow, waterAngle,
                        outlineCheck.isSelected(), verticalCheck.isSelected());
                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            maskImageView.setImage(newImage);
                            maskImageView.setFitWidth(imageView.getFitWidth());
                            maskImageView.setFitHeight(imageView.getFitHeight());
                            maskImageView.setLayoutX(imageView.getLayoutX());
                            maskImageView.setLayoutY(imageView.getLayoutY());
                            maskImageView.setVisible(true);
                        }
                    });
                }
            }

        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    @Override
    public void okAction() {
        if (isSettingValues || imageView.getImage() == null) {
            return;
        }
        values.setUndoImage(imageView.getImage());
        values.setCurrentImage(maskImageView.getImage());
        imageView.setImage(maskImageView.getImage());
        setImageChanged(true);
        maskImageView.setVisible(false);

    }

}
