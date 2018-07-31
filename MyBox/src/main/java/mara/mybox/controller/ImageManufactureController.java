package mara.mybox.controller;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.image.ImageConverter;
import mara.mybox.image.ImageGrayTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import mara.mybox.tools.FxmlTools.ImageManufactureType;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureController extends ImageViewerController {

    protected int saturateStep = 5, brightnessStep = 5, hueStep = 5, percent = 50;
    protected int saturateOffset = 0, brightnessOffset = 0, hueOffset = 0;
    protected File nextFile, lastFile, refFile;
    protected String ImageSortTypeKey, ImageOpenAfterSaveAsKey, ImageReferenceDisplayKey, ImageSaveConfirmKey, FontSizeKey, FontFamilyKey;
    protected ScrollPane refPane;
    protected ImageView refView;
    protected Image refImage, undoImage, redoImage;
    protected ImageFileInformation refInfo;
    private boolean noRatio, isScale, isPickingOriginalColor, isPickingNewColor, isPickingPosition;
    private float scale = 1.0f, shearX, shearY = 0, waterTransparent = 0.5f;
    private int width, height, colorDistance, opacity = 100, colorMatchType, hueDistance, waterX, waterY, waterSize;
    public SimpleBooleanProperty imageChanged;

    @FXML
    protected ToolBar fileBar, navBar, refBar, hotBar, scaleBar, replaceColorBar, watermarkBar, transformBar;
    @FXML
    protected Tab fileTab, zoomTab, hueTab, saturateTab, brightnessTab, filtersTab, watermarkTab;
    @FXML
    protected Tab replaceColorTab, pixelsTab, refTab, browseTab, opacityTab, transformTab, edgesTab;
    @FXML
    protected Slider zoomSlider, angleSlider, hueSlider, saturateSlider, brightnessSlider, binarySlider, opacitySlider;
    @FXML
    protected Label zoomValue, hueValue, saturateValue, brightnessValue, binaryValue, tipsLabel, promptLabel, opacityValue;
    @FXML
    protected ToggleGroup sortGroup, pixelsGroup, colorMatchGroup;
    @FXML
    protected Button nextButton, lastButton, origImageButton, selectRefButton, pixelsOkButton, saveButton, leftButton, rightButton;
    @FXML
    protected Button pickOldColorButton, pickNewColorButton, transparentForNewButton, recoverButton, colorOkButton, opacityOkButton;
    @FXML
    protected Button waterPositionButton, waterAddButton, undoButton, redoButton;
    @FXML
    protected CheckBox openCheck, saveCheck, displayRefCheck, refSyncCheck, keepRatioCheck, excludedCheck;
    @FXML
    protected CheckBox edgesTopCheck, edgesBottomCheck, edgesLeftCheck, edgesRightCheck;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected TextField widthInput, heightInput, scaleInput, colorDistanceInput, hueDistanceInput;
    @FXML
    protected TextField shearXInput, shearYInput, waterInput, waterXInput, waterYInput;
    @FXML
    protected ChoiceBox ratioBox, waterStyleBox, waterFamilyBox;
    @FXML
    private TabPane tabPane;
    @FXML
    protected ColorPicker newColorPicker, waterColorPicker, edgesColorPicker;
    @FXML
    protected ComboBox originalColorsBox, angleBox, waterSizeBox, waterTransparentBox;

    public static class ColorMatchType {

        public static int AccurateMatch = 0;
        public static int ColorDistance = 1;
        public static int HueDistance = 2;

    }

    public ImageManufactureController() {
        ImageSortTypeKey = "ImageSortType";
        ImageOpenAfterSaveAsKey = "ImageOpenAfterSaveAs";
        ImageSaveConfirmKey = "ImageSaveConfirmKey";
        ImageReferenceDisplayKey = "ImageReferenceDisplay";
        FontSizeKey = "FontSizeKey";
        FontFamilyKey = "FontFamilyKey";
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initFileTab();
            initViewTab();
            initBrowseTab();
            initPixelsTab();
            initHueTab();
            initSaturateTab();
            initBrightnessTab();
            initOpacityTab();
            initFiltersTab();
            initTransformTab();
            initReplaceColorTab();
            initWatermarkTab();
            initReferenceTab();
            initEdgesTab();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initCommon() {
        try {
            attributes = new ImageAttributes();

            fileBar.setDisable(true);
            browseTab.setDisable(true);
            navBar.setDisable(true);
            zoomTab.setDisable(true);
            hueTab.setDisable(true);
            saturateTab.setDisable(true);
            brightnessTab.setDisable(true);
            opacityTab.setDisable(true);
            filtersTab.setDisable(true);
            replaceColorTab.setDisable(true);
            pixelsTab.setDisable(true);
            refTab.setDisable(true);
            transformTab.setDisable(true);
            watermarkTab.setDisable(true);
            edgesTab.setDisable(true);
            hotBar.setDisable(true);

            Tooltip tips = new Tooltip(getMessage("ImageManufactureTips"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(tipsLabel, tips);

            if (AppVaribles.showComments) {
                tips = new Tooltip(getMessage("ImageRefTips"));
                tips.setFont(new Font(16));
                FxmlTools.setComments(displayRefCheck, tips);
            }

            imageChanged = new SimpleBooleanProperty(false);
            imageChanged.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (imageChanged.getValue()) {
                        saveButton.setDisable(false);
                        recoverButton.setDisable(false);
                        undoButton.setDisable(false);
                        redoButton.setDisable(true);
                        getMyStage().setTitle(AppVaribles.getMessage("AppTitle") + "  " + sourceFile.getAbsolutePath() + "*");
                    } else {
                        saveButton.setDisable(true);
                        recoverButton.setDisable(true);
                        getMyStage().setTitle(AppVaribles.getMessage("AppTitle") + "  " + sourceFile.getAbsolutePath());
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initFileTab() {
        try {
            openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setConfigValue(ImageOpenAfterSaveAsKey, openCheck.isSelected());
                }
            });
            openCheck.setSelected(AppVaribles.getConfigBoolean(ImageOpenAfterSaveAsKey));

            saveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setConfigValue(ImageSaveConfirmKey, saveCheck.isSelected());
                }
            });
            saveCheck.setSelected(AppVaribles.getConfigBoolean(ImageSaveConfirmKey));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initViewTab() {
        try {

            zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    zoomStep = newValue.intValue();
                    zoomValue.setText(zoomStep + "%");
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initBrowseTab() {
        try {
            sortGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkNevigator();
                    RadioButton selected = (RadioButton) sortGroup.getSelectedToggle();
                    AppVaribles.setConfigValue(ImageSortTypeKey, selected.getText());
                }
            });
            FxmlTools.setRadioSelected(sortGroup, AppVaribles.getConfigValue(ImageSortTypeKey, getMessage("FileName")));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initPixelsTab() {
        try {

            widthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkPixelsWidth();
                }
            });
            checkPixelsWidth();

            heightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkPixelsHeight();
                }
            });
            checkPixelsHeight();

            keepRatioCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    if (keepRatioCheck.isSelected()) {
                        checkRatio();
                    }
                }
            });

            pixelsOkButton.disableProperty().bind(
                    widthInput.styleProperty().isEqualTo(badStyle)
                            .or(heightInput.styleProperty().isEqualTo(badStyle))
                            .or(scaleInput.styleProperty().isEqualTo(badStyle))
            );

            pixelsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    RadioButton selected = (RadioButton) pixelsGroup.getSelectedToggle();
                    if (getMessage("Pixels").equals(selected.getText())) {
                        scaleBar.setDisable(false);
                        isScale = false;
                        scaleInput.setStyle(null);
                    } else {
                        scaleBar.setDisable(true);
                        isScale = true;
                        widthInput.setStyle(null);
                        heightInput.setStyle(null);
                    }
                }
            });

            ratioBox.getItems().addAll(FXCollections.observableArrayList(getMessage("BaseOnLarger"),
                    getMessage("BaseOnWidth"), getMessage("BaseOnHeight"), getMessage("BaseOnSmaller")));
            ratioBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        checkRatioAdjustion(newValue);
                    }
                }
            });
            ratioBox.getSelectionModel().select(0);

            scaleInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkPixelsScale();
                }
            });
            checkPixelsScale();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkPixelsWidth() {
        try {
            width = Integer.valueOf(widthInput.getText());
            if (width > 0) {
                widthInput.setStyle(null);
                checkRatio();
            } else {
                widthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            widthInput.setStyle(badStyle);
        }
    }

    private void checkPixelsHeight() {
        try {
            height = Integer.valueOf(heightInput.getText());
            if (height > 0) {
                heightInput.setStyle(null);
                checkRatio();
            } else {
                heightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            heightInput.setStyle(badStyle);
        }
    }

    private void checkPixelsScale() {
        try {
            scale = Float.valueOf(scaleInput.getText());
            if (scale > 0) {
                scaleInput.setStyle(null);
                final Image currentImage = imageView.getImage();
                if (currentImage != null) {
                    noRatio = true;
                    widthInput.setText(Math.round(currentImage.getWidth() * scale) + "");
                    heightInput.setText(Math.round(currentImage.getHeight() * scale) + "");
                    noRatio = false;
                }
            } else {
                scaleInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            scaleInput.setStyle(badStyle);
        }
    }

    protected void initHueTab() {
        try {
            hueSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    hueStep = newValue.intValue();
                    hueValue.setText(hueStep + "");
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initSaturateTab() {
        try {
            saturateSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    saturateStep = newValue.intValue();
                    saturateValue.setText(saturateStep + "%");
                }
            });
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initBrightnessTab() {
        try {
            brightnessSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    brightnessStep = newValue.intValue();
                    brightnessValue.setText(brightnessStep + "%");
                }
            });
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initOpacityTab() {
        try {
            opacitySlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    opacity = newValue.intValue();
                    opacityValue.setText(opacity + "");
                }
            });
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initFiltersTab() {
        try {
            binarySlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    percent = newValue.intValue();
                    binaryValue.setText(percent + "%");
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initTransformTab() {
        try {

            if (AppVaribles.showComments) {
                Tooltip tips = new Tooltip(getMessage("transformComments"));
                tips.setFont(new Font(16));
                FxmlTools.setComments(transformBar, tips);
            }

            shearXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkShearX();
                }
            });
            checkShearX();

//            shearYInput.textProperty().addListener(new ChangeListener<String>() {
//                @Override
//                public void changed(ObservableValue<? extends String> observable,
//                        String oldValue, String newValue) {
//                    checkShearY();
//                }
//            });
//            checkShearY();
            angleSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    rotateAngle = newValue.intValue();
                    angleBox.getEditor().setText(rotateAngle + "");
                    leftButton.setDisable(false);
                    rightButton.setDisable(false);
                }
            });

            ObservableList<String> angles = FXCollections.observableArrayList(
                    "90", "0", "180", "45", "30", "60", "75", "120", "135");
            angleBox.getItems().addAll(angles);
            angleBox.getSelectionModel().select(0);
            angleBox.setVisibleRowCount(angles.size());
            angleBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        rotateAngle = Integer.valueOf(newValue);
                        angleSlider.setValue(rotateAngle);
                        angleBox.getEditor().setStyle(null);
                        leftButton.setDisable(false);
                        rightButton.setDisable(false);
                    } catch (Exception e) {
                        rotateAngle = 0;
                        angleBox.getEditor().setStyle(badStyle);
                        leftButton.setDisable(true);
                        rightButton.setDisable(true);
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkShearX() {
        try {
            shearX = Float.valueOf(shearXInput.getText());
            shearXInput.setStyle(null);
//            if (shearX >= -1.0 && shearX <= 1.0) {
//                shearXInput.setStyle(null);
//            } else {
//                shearXInput.setStyle(badStyle);
//            }
        } catch (Exception e) {
            shearXInput.setStyle(badStyle);
        }
    }

    private void checkShearY() {
//        try {
//            shearY = Float.valueOf(shearYInput.getText());
//            if (shearY >= -1.0 && shearY <= 1.0) {
//                shearYInput.setStyle(null);
//            } else {
//                shearYInput.setStyle(badStyle);
//            }
//        } catch (Exception e) {
//            shearYInput.setStyle(badStyle);
//        }
    }

    protected void initReplaceColorTab() {
        try {
            colorMatchGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkMatchType();
                }
            });
            checkMatchType();

            FxmlTools.quickTooltip(colorDistanceInput, new Tooltip("0 ~ 255"));
            FxmlTools.quickTooltip(hueDistanceInput, new Tooltip("0 ~ 360"));
            if (AppVaribles.showComments) {
                Tooltip tips = new Tooltip(getMessage("ReplaceColorComments"));
                tips.setFont(new Font(16));
                FxmlTools.setComments(replaceColorBar, tips);
            }

            colorDistanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColorDistance();
                }
            });
            checkColorDistance();

            hueDistanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkHueDistance();
                }
            });
            checkHueDistance();

            originalColorsBox.setCellFactory(new Callback<ListView<Color>, ListCell<Color>>() {
                @Override
                public ListCell<Color> call(ListView<Color> p) {
                    return new ListCell<Color>() {
                        private final Rectangle rectangle;

                        {
                            setContentDisplay(ContentDisplay.LEFT);
                            rectangle = new Rectangle(10, 10);
                        }

                        @Override
                        protected void updateItem(Color item, boolean empty) {
                            super.updateItem(item, empty);

                            if (item == null || empty) {
                                setGraphic(null);
                            } else {
                                rectangle.setFill(item);
                                setGraphic(rectangle);
                                setText(item.toString());
                            }
                        }
                    };
                }
            });

            originalColorsBox.setVisibleRowCount(0);
            colorOkButton.disableProperty().bind(
                    colorDistanceInput.styleProperty().isEqualTo(badStyle)
                            .or(originalColorsBox.visibleRowCountProperty().isEqualTo(0))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkMatchType() {
        RadioButton selected = (RadioButton) colorMatchGroup.getSelectedToggle();
        if (getMessage("ColorDistance").equals(selected.getText())) {
            colorMatchType = ColorMatchType.ColorDistance;
            colorDistanceInput.setDisable(false);
            hueDistanceInput.setDisable(true);
            hueDistanceInput.setStyle(null);

        } else if (getMessage("HueDistance").equals(selected.getText())) {
            colorMatchType = ColorMatchType.HueDistance;
            hueDistanceInput.setDisable(false);
            colorDistanceInput.setDisable(true);
            colorDistanceInput.setStyle(null);

        } else {
            colorMatchType = ColorMatchType.AccurateMatch;
            colorDistanceInput.setDisable(true);
            colorDistanceInput.setStyle(null);
            hueDistanceInput.setDisable(true);
            hueDistanceInput.setStyle(null);

        }

    }

    private void checkColorDistance() {
        try {
            colorDistance = Integer.valueOf(colorDistanceInput.getText());
            if (colorDistance >= 0 && colorDistance <= 255) {
                colorDistanceInput.setStyle(null);
            } else {
                colorDistanceInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            colorDistanceInput.setStyle(badStyle);
        }
    }

    private void checkHueDistance() {
        try {
            hueDistance = Integer.valueOf(hueDistanceInput.getText());
            if (hueDistance >= 0 && hueDistance <= 360) {
                hueDistanceInput.setStyle(null);
            } else {
                hueDistanceInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            hueDistanceInput.setStyle(badStyle);
        }
    }

    protected void initWatermarkTab() {
        try {

            if (AppVaribles.showComments) {
                Tooltip tips = new Tooltip(getMessage("watermarkComments"));
                tips.setFont(new Font(16));
                FxmlTools.setComments(watermarkBar, tips);
            }

            waterXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkWaterX();
                }
            });
            checkWaterX();

            waterYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkWaterY();
                }
            });
            checkWaterY();

            ObservableList<String> sizes = FXCollections.observableArrayList(
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

            ObservableList<String> transparents = FXCollections.observableArrayList(
                    "0.5", "1.0", "0.3", "0.1", "0.8");
            waterTransparentBox.getItems().addAll(transparents);
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

            ObservableList<String> styles = FXCollections.observableArrayList(
                    getMessage("Regular"), getMessage("Bold"), getMessage("Italic"), getMessage("Bold Italic"));
            waterStyleBox.getItems().addAll(styles);
            waterStyleBox.getSelectionModel().select(0);

            GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] fontNames = e.getAvailableFontFamilyNames();
            waterFamilyBox.getItems().addAll(Arrays.asList(fontNames));
            waterFamilyBox.getSelectionModel().select(AppVaribles.getConfigValue(FontFamilyKey, fontNames[0]));
            waterFamilyBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    AppVaribles.setConfigValue(FontFamilyKey, newValue);
                }
            });

            waterAddButton.disableProperty().bind(
                    waterXInput.styleProperty().isEqualTo(badStyle)
                            .or(waterYInput.styleProperty().isEqualTo(badStyle))
                            .or(waterSizeBox.getEditor().styleProperty().isEqualTo(badStyle))
                            .or(waterTransparentBox.getEditor().styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(waterInput.textProperty()))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkWaterX() {
        try {
            waterX = Integer.valueOf(waterXInput.getText());
            waterXInput.setStyle(null);
            if (waterX >= 0 && waterX <= imageInformation.getxPixels()) {
                waterXInput.setStyle(null);
            } else {
                waterXInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            waterXInput.setStyle(badStyle);
        }
    }

    private void checkWaterY() {
        try {
            waterY = Integer.valueOf(waterYInput.getText());
            waterYInput.setStyle(null);
            if (waterY >= 0 && waterY <= imageInformation.getyPixels()) {
                waterYInput.setStyle(null);
            } else {
                waterYInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            waterYInput.setStyle(badStyle);
        }
    }

    protected void initReferenceTab() {
        try {
            displayRefCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    checkReferenceImage();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initEdgesTab() {
        try {
            displayRefCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    checkReferenceImage();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    protected void selectSourceFile(ActionEvent event) {
        if (image != null && imageChanged.getValue()) {
            if (!checkSavingBeforeExit()) {
                return;
            }
        }
        super.selectSourceFile(event);
    }

    @Override
    public void afterImageLoaded() {
        super.afterImageLoaded();
        if (image != null) {
            fileBar.setDisable(false);
            navBar.setDisable(false);
            zoomTab.setDisable(false);
            hueTab.setDisable(false);
            saturateTab.setDisable(false);
            brightnessTab.setDisable(false);
            filtersTab.setDisable(false);
            replaceColorTab.setDisable(false);
            pixelsTab.setDisable(false);
            refTab.setDisable(false);
            hotBar.setDisable(false);
            browseTab.setDisable(false);
            if (CommonValues.NoAlphaImages.contains(imageInformation.getImageFormat())) {
                opacityTab.setDisable(true);
//                transparentForNewButton.setDisable(true);
            } else {
                opacityTab.setDisable(false);
                transparentForNewButton.setDisable(false);
            }
            transformTab.setDisable(false);
            watermarkTab.setDisable(false);
            edgesTab.setDisable(false);

            widthInput.setText(imageInformation.getxPixels() + "");
            heightInput.setText(imageInformation.getyPixels() + "");
            attributes.setSourceWidth(imageInformation.getxPixels());
            attributes.setSourceHeight(imageInformation.getyPixels());

            waterXInput.setText(imageInformation.getxPixels() / 2 + "");
            waterYInput.setText(imageInformation.getyPixels() / 2 + "");

            checkNevigator();
            straighten();
            showLabel();

            imageChanged.set(false);
            undoButton.setDisable(true);
            redoButton.setDisable(true);
            isPickingOriginalColor = false;
            isPickingNewColor = false;
            imageView.setCursor(Cursor.OPEN_HAND);
            promptLabel.setText("");
            getMyStage().setTitle(AppVaribles.getMessage("AppTitle") + "  " + sourceFile.getAbsolutePath());
        }
    }

    @FXML
    public void recovery() {
        undoImage = imageView.getImage();
        imageView.setImage(image);
        imageChanged.set(false);
        undoButton.setDisable(false);
        redoButton.setDisable(true);
        showLabel();
    }

    @FXML
    public void increaseSaturate() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.changeSaturate(currentImage, saturateStep / 100.0f);
                saturateOffset += saturateStep;
                if (saturateOffset > 100) {
                    saturateOffset = 100;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
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

    @FXML
    public void decreaseSaturate() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.changeSaturate(currentImage, 0.0f - saturateStep / 100.0f);
                saturateOffset -= saturateStep;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
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

    @FXML
    public void increaseHue() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.changeHue(currentImage, hueStep);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
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

    @FXML
    public void decreaseHue() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.changeHue(currentImage, 0 - hueStep);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
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

    @FXML
    public void increaseBrightness() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.changeBrightness(currentImage, brightnessStep / 100.0f);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
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

    @FXML
    public void decreaseBrightness() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.changeBrightness(currentImage, 0.0f - brightnessStep / 100.0f);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
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

    @FXML
    public void setInvert() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.manufactureImage(currentImage, ImageManufactureType.Invert);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
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

    @FXML
    public void setGray() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.manufactureImage(currentImage, ImageManufactureType.Gray);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
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

    @FXML
    public void setBinary() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.makeBinary(currentImage, percent);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
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

    @FXML
    public void pixelsCalculator() {
        try {
            attributes.setKeepRatio(keepRatioCheck.isSelected());
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.PixelsCalculatorFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            PixelsCalculationController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            controller.setMyStage(stage);
            controller.setSource(attributes, widthInput, heightInput);

            Scene scene = new Scene(pane);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(getMyStage());
            stage.setTitle(AppVaribles.getMessage("PixelsCalculator"));
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setScene(scene);
            stage.show();
            noRatio = true;
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    noRatio = false;
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void setOriginalSize() {
        noRatio = true;
        if (imageInformation.getxPixels() > 0) {
            widthInput.setText(imageInformation.getxPixels() + "");
        }
        if (imageInformation.getyPixels() > 0) {
            heightInput.setText(imageInformation.getyPixels() + "");
        }
        noRatio = false;
    }

    @FXML
    protected void pixelsAction() {
        if (isScale) {
            setScale();
        } else {
            setPixels();
        }
    }

    @FXML
    public void save() {
        if (saveCheck.isSelected()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle(AppVaribles.getMessage("AppTitle"));
            alert.setContentText(AppVaribles.getMessage("SureOverrideFile"));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK) {
                return;
            }
        }

        final Image currentImage = imageView.getImage();
        Task saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String format = imageInformation.getImageFormat();
                final BufferedImage bufferedImage = FxmlTools.getWritableData(currentImage, format);
                ImageFileWriters.writeImageFile(bufferedImage, format, sourceFile.getAbsolutePath());
                imageInformation = ImageFileReaders.readImageMetaData(sourceFile.getAbsolutePath());
                image = currentImage;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageChanged.set(false);
                        showLabel();
                    }
                });
                return null;
            }
        };
        openHandlingStage(saveTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(saveTask);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    public void saveAs() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(targetPathKey, System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            AppVaribles.setConfigValue(targetPathKey, file.getParent());

            final Image currentImage = imageView.getImage();
            Task saveTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String format = FileTools.getFileSuffix(file.getName());
                    final BufferedImage bufferedImage = FxmlTools.getWritableData(currentImage, imageInformation.getImageFormat());
                    ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (openCheck.isSelected()) {
                                showImageManufacture(file.getAbsolutePath());
                            }
                        }
                    });
                    return null;
                }
            };
            openHandlingStage(saveTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(saveTask);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    public void next() {
        if (!checkSavingBeforeExit()) {
            return;
        }
        if (nextFile != null) {
            loadImage(nextFile.getAbsoluteFile(), false);
        }
    }

    @FXML
    public void last() {
        if (!checkSavingBeforeExit()) {
            return;
        }
        if (lastFile != null) {
            loadImage(lastFile.getAbsoluteFile(), false);
        }
    }

    @FXML
    public void browseAction() {
        try {
            if (!stageReloading()) {
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImagesViewerFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            final ImagesViewerController controller = fxmlLoader.getController();
            controller.setMyStage(myStage);
            myStage.setScene(new Scene(pane));
            myStage.setTitle(getMessage("AppTitle"));
            controller.loadImages(sourceFile.getParentFile(), 10);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    public void pickColorForOriginal() {
        isPickingOriginalColor = true;
        isPickingNewColor = false;
        isPickingPosition = false;
        imageView.setCursor(Cursor.HAND);
        promptLabel.setText(getMessage("PickColorComments"));
    }

    @FXML
    public void pickColorForNew() {
        isPickingOriginalColor = false;
        isPickingNewColor = true;
        isPickingPosition = false;
        imageView.setCursor(Cursor.HAND);
        promptLabel.setText(getMessage("PickColorComments"));
    }

    @FXML
    public void transparentForNew() {
        newColorPicker.setValue(new Color(0, 0, 0, 0));
    }

    @FXML
    public void calculateThreshold() {
        int threshold = ImageGrayTools.calculateThreshold(sourceFile);
        percent = threshold * 100 / 256;
        binarySlider.setValue(percent);
    }

    @FXML
    @Override
    public void zoomIn() {
        imageView.setFitHeight(imageView.getFitHeight() * (1 + zoomStep / 100.0f));
        imageView.setFitWidth(imageView.getFitWidth() * (1 + zoomStep / 100.0f));
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            refView.setFitHeight(refView.getFitHeight() * (1 + zoomStep / 100.0f));
            refView.setFitWidth(refView.getFitWidth() * (1 + zoomStep / 100.0f));
        }
    }

    @FXML
    @Override
    public void zoomOut() {
        imageView.setFitHeight(imageView.getFitHeight() * (1 - zoomStep / 100.0f));
        imageView.setFitWidth(imageView.getFitWidth() * (1 - zoomStep / 100.0f));
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            refView.setFitHeight(refView.getFitHeight() * (1 - zoomStep / 100.0f));
            refView.setFitWidth(refView.getFitWidth() * (1 - zoomStep / 100.0f));
        }
    }

    @FXML
    @Override
    public void imageSize() {
        final Image currentImage = imageView.getImage();
        imageView.setFitWidth(currentImage.getWidth());
        imageView.setFitHeight(currentImage.getHeight());
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            refView.setFitHeight(refInfo.getyPixels());
            refView.setFitWidth(refInfo.getxPixels());
        }
    }

    @FXML
    @Override
    public void paneSize() {
        imageView.setFitHeight(scrollPane.getHeight() - 5);
        imageView.setFitWidth(scrollPane.getWidth() - 1);
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            refView.setFitHeight(refPane.getHeight() - 5);
            refView.setFitWidth(refPane.getWidth() - 1);
        }
    }

    @FXML
    @Override
    public void moveRight() {
        FxmlTools.setScrollPane(scrollPane, -40, scrollPane.getVvalue());
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            FxmlTools.setScrollPane(refPane, -40, refPane.getVvalue());
        }
    }

    @FXML
    @Override
    public void moveLeft() {
        FxmlTools.setScrollPane(scrollPane, 40, scrollPane.getVvalue());
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            FxmlTools.setScrollPane(refPane, 40, refPane.getVvalue());
        }
    }

    @FXML
    @Override
    public void moveUp() {
        FxmlTools.setScrollPane(scrollPane, scrollPane.getHvalue(), 40);
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            FxmlTools.setScrollPane(refPane, refPane.getHvalue(), 40);
        }
    }

    @FXML
    @Override
    public void moveDown() {
        FxmlTools.setScrollPane(scrollPane, scrollPane.getHvalue(), -40);
        if (refSyncCheck.isSelected() && displayRefCheck.isSelected()) {
            FxmlTools.setScrollPane(refPane, refPane.getHvalue(), -40);
        }
    }

    @FXML
    public void rightRotate() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.rotateImage(currentImage, rotateAngle);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        showLabel();
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

    @FXML
    public void leftRotate() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.rotateImage(currentImage, 360 - rotateAngle);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        showLabel();
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

    @FXML
    public void selectReference() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(sourcePathKey, System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            refFile = file;
            AppVaribles.setConfigValue("LastPath", sourceFile.getParent());
            AppVaribles.setConfigValue(sourcePathKey, sourceFile.getParent());

            loadReferenceImage();

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    public void originalImage() {
        refFile = sourceFile;
        loadReferenceImage();
    }

    @FXML
    public void showLabel() {
        final Image currentImage = imageView.getImage();
        if (imageInformation == null || currentImage == null) {
            return;
        }
        String str = AppVaribles.getMessage("Format") + ":" + imageInformation.getImageFormat() + "  "
                + AppVaribles.getMessage("Pixels") + ":" + imageInformation.getxPixels() + "x" + imageInformation.getyPixels() + "  "
                + AppVaribles.getMessage("Size") + ":" + FileTools.showFileSize(imageInformation.getFile().length()) + "  "
                + AppVaribles.getMessage("ModifyTime") + ":" + DateTools.datetimeToString(imageInformation.getFile().lastModified()) + "  "
                + AppVaribles.getMessage("CurrentPixels") + ":" + (int) currentImage.getWidth() + "x" + (int) currentImage.getHeight();
        bottomLabel.setText(str);
    }

    @FXML
    public void popRefInformation() {
        showImageInformation(refInfo);
    }

    @FXML
    public void popRefMeta() {
        showImageMetaData(refInfo);
    }

    @FXML
    public void clickImage(MouseEvent event) {
        if (isPickingOriginalColor || isPickingNewColor) {
            if (event.getClickCount() > 1) {
                isPickingOriginalColor = false;
                isPickingNewColor = false;
                imageView.setCursor(Cursor.OPEN_HAND);
                promptLabel.setText("");

            } else {
                double imageX = event.getX() * image.getWidth() / imageView.getBoundsInLocal().getWidth();
                double imageY = event.getY() * image.getHeight() / imageView.getBoundsInLocal().getHeight();

                PixelReader pixelReader = image.getPixelReader();
                Color color = pixelReader.getColor((int) Math.round(imageX), (int) Math.round(imageY));
                if (isPickingOriginalColor) {
                    originalColorsBox.setVisibleRowCount(20);
                    originalColorsBox.getItems().add(color);
                    originalColorsBox.getSelectionModel().select(color);
                } else {
                    newColorPicker.setValue(color);
                }
            }

        } else if (isPickingPosition) {

            waterXInput.setText((int) (event.getX() * image.getWidth() / imageView.getBoundsInLocal().getWidth()) + "");
            waterYInput.setText((int) (event.getY() * image.getHeight() / imageView.getBoundsInLocal().getHeight()) + "");

            isPickingOriginalColor = false;
            isPickingNewColor = false;
            isPickingPosition = false;
            imageView.setCursor(Cursor.OPEN_HAND);
            promptLabel.setText("");
        }

    }

    @FXML
    public void replaceColorAction() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Color> colors = originalColorsBox.getItems();
                final Image newImage;
                if (colorMatchType == ColorMatchType.ColorDistance) {

                    if (excludedCheck.isSelected()) {
                        newImage = FxmlTools.replaceColorsExcluded(currentImage, colors, newColorPicker.getValue(), colorDistance);
                    } else {
                        newImage = FxmlTools.replaceColorsIncluded(currentImage, colors, newColorPicker.getValue(), colorDistance);
                    }

                } else if (colorMatchType == ColorMatchType.HueDistance) {

                    if (excludedCheck.isSelected()) {
                        newImage = FxmlTools.replaceHuesExcluded(currentImage, colors, newColorPicker.getValue(), hueDistance);
                    } else {
                        newImage = FxmlTools.replaceHuesIncluded(currentImage, colors, newColorPicker.getValue(), hueDistance);
                    }

                } else {

                    if (excludedCheck.isSelected()) {
                        newImage = FxmlTools.replaceColorsUnMatched(currentImage, colors, newColorPicker.getValue());
                    } else {
                        newImage = FxmlTools.replaceColorsMatched(currentImage, colors, newColorPicker.getValue());
                    }

                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        showLabel();
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

    @FXML
    public void clearOriginalColorsAction() {
        originalColorsBox.getItems().clear();
        originalColorsBox.setVisibleRowCount(0);
    }

    @FXML
    public void opacityAction() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.opcityImage(currentImage, opacity);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        showLabel();
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

    @FXML
    public void horizontalAction() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.horizontalImage(currentImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        showLabel();
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

    @FXML
    public void verticalAction() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.verticalImage(currentImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        showLabel();
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

    @FXML
    public void shearAction() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.shearImage(currentImage, shearX, 0);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        showLabel();
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

    @FXML
    public void waterPositionAction() {
        isPickingOriginalColor = false;
        isPickingNewColor = false;
        isPickingPosition = true;
        imageView.setCursor(Cursor.HAND);
        promptLabel.setText(getMessage("PickPositionComments"));
    }

    @FXML
    public void waterAddAction() {
        final Image currentImage = imageView.getImage();
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
                final Image newImage = FxmlTools.addWatermark(currentImage, waterInput.getText(),
                        font, waterColorPicker.getValue(), waterX, waterY, waterTransparent);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        showLabel();
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

    @FXML
    public void edgesTransparentAction() {
        edgesColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void edgesBlackAction() {
        edgesColorPicker.setValue(Color.BLACK);
    }

    @FXML
    public void edgesWhiteAction() {
        edgesColorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void edgesOkAction() {
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image newImage = FxmlTools.cutEdges(currentImage, waterColorPicker.getValue(),
                            edgesTopCheck.isSelected(), edgesBottomCheck.isSelected(),
                            edgesLeftCheck.isSelected(), edgesRightCheck.isSelected());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            undoImage = currentImage;
                            imageView.setImage(newImage);
                            imageChanged.set(true);
                            showLabel();
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void undoAction() {
        if (undoImage == null) {
            undoButton.setDisable(true);
        }
        redoImage = imageView.getImage();
        imageView.setImage(undoImage);
        imageChanged.set(true);
        undoButton.setDisable(true);
        redoButton.setDisable(false);
    }

    @FXML
    public void redoAction() {
        if (redoImage == null) {
            redoButton.setDisable(true);
        }
        undoImage = imageView.getImage();
        imageView.setImage(redoImage);
        imageChanged.set(true);
        undoButton.setDisable(false);
        redoButton.setDisable(true);
    }

    private void checkNevigator() {
        if (sourceFile == null) {
            lastFile = null;
            lastButton.setDisable(true);
            nextFile = null;
            nextButton.setDisable(true);
            return;
        }
        File path = sourceFile.getParentFile();
        List<File> sortedFiles = new ArrayList<>();
        File[] files = path.listFiles();
        for (File file : files) {
            if (file.isFile() && FileTools.isSupportedImage(file)) {
                sortedFiles.add(file);
            }
        }
        RadioButton sort = (RadioButton) sortGroup.getSelectedToggle();
        if (getMessage("OriginalFileName").equals(sort.getText())) {
            FileTools.sortFiles(sortedFiles, FileTools.FileSortType.FileName);

        } else if (getMessage("CreateTime").equals(sort.getText())) {
            FileTools.sortFiles(sortedFiles, FileTools.FileSortType.CreateTime);

        } else if (getMessage("ModifyTime").equals(sort.getText())) {
            FileTools.sortFiles(sortedFiles, FileTools.FileSortType.ModifyTime);

        } else if (getMessage("Size").equals(sort.getText())) {
            FileTools.sortFiles(sortedFiles, FileTools.FileSortType.Size);
        }

        for (int i = 0; i < sortedFiles.size(); i++) {
            if (sortedFiles.get(i).getAbsoluteFile().equals(sourceFile.getAbsoluteFile())) {
                if (i < sortedFiles.size() - 1) {
                    nextFile = sortedFiles.get(i + 1);
                    nextButton.setDisable(false);
                } else {
                    nextFile = null;
                    nextButton.setDisable(true);
                }
                if (i > 0) {
                    lastFile = sortedFiles.get(i - 1);
                    lastButton.setDisable(false);
                } else {
                    lastFile = null;
                    lastButton.setDisable(true);
                }
                return;
            }
        }
        lastFile = null;
        lastButton.setDisable(true);
        nextFile = null;
        nextButton.setDisable(true);
    }

    private void checkReferenceImage() {
        try {
            if (displayRefCheck.isSelected()) {
                if (splitPane.getItems().size() == 1) {
                    if (refPane == null) {
                        refPane = new ScrollPane();
                        refPane.setPannable(true);
                        refPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                        VBox.setVgrow(refPane, Priority.ALWAYS);
                        HBox.setHgrow(refPane, Priority.ALWAYS);
                    }
                    if (refView == null) {
                        refView = new ImageView();
                        refView.setPreserveRatio(true);
                        refView.setOnMouseEntered(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent event) {
                                if (refInfo == null) {
                                    return;
                                }
                                String str = AppVaribles.getMessage("Format") + ":" + refInfo.getImageFormat() + "  "
                                        + AppVaribles.getMessage("Pixels") + ":" + refInfo.getxPixels() + "x" + refInfo.getyPixels() + "  "
                                        + AppVaribles.getMessage("Size") + ":" + FileTools.showFileSize(refInfo.getFile().length()) + "  "
                                        + AppVaribles.getMessage("ModifyTime") + ":" + DateTools.datetimeToString(refInfo.getFile().lastModified());
                                bottomLabel.setText(str);
                            }
                        });
                    }
                    refPane.setContent(refView);
                    splitPane.getItems().add(0, refPane);
                    splitPane.setDividerPositions(0.5);
                    refBar.setDisable(false);

                    if (refFile == null) {
                        refFile = sourceFile;
                    }
                    if (refImage == null) {
                        loadReferenceImage();
                    }

                }
            } else {
                if (splitPane.getItems().size() == 2) {
                    splitPane.getItems().remove(0);
                    refBar.setDisable(true);
                }
            }

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    private void loadReferenceImage() {
        if (refFile == null || sourceFile == null) {
            return;
        }
        if (refFile.getAbsolutePath().equals(sourceFile.getAbsolutePath())) {
            refImage = image;
            refInfo = imageInformation;
            refView.setImage(refImage);
            if (refPane.getHeight() < refInfo.getyPixels()) {
                refView.setFitHeight(scrollPane.getHeight() - 5); // use attributes of scrollPane but not refPane
                refView.setFitWidth(scrollPane.getWidth() - 1);
            } else {
                refView.setFitHeight(refInfo.getyPixels());
                refView.setFitWidth(refInfo.getxPixels());
            }
            return;
        }
        Task refTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                refInfo = ImageFileReaders.readImageMetaData(refFile.getAbsolutePath());
                refImage = SwingFXUtils.toFXImage(ImageIO.read(refFile), null);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refView.setImage(refImage);
                        if (refPane.getHeight() < refInfo.getyPixels()) {
                            refView.setFitHeight(refPane.getHeight() - 5);
                            refView.setFitWidth(refPane.getWidth() - 1);
                        } else {
                            refView.setFitHeight(refInfo.getyPixels());
                            refView.setFitWidth(refInfo.getxPixels());
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(refTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(refTask);
        thread.setDaemon(true);
        thread.start();
    }

    protected void checkRatioAdjustion(String s) {
        try {
            if (getMessage("BaseOnWidth").equals(s)) {
                attributes.setRatioAdjustion(ImageConverter.KeepRatioType.BaseOnWidth);
            } else if (getMessage("BaseOnHeight").equals(s)) {
                attributes.setRatioAdjustion(ImageConverter.KeepRatioType.BaseOnHeight);
            } else if (getMessage("BaseOnLarger").equals(s)) {
                attributes.setRatioAdjustion(ImageConverter.KeepRatioType.BaseOnLarger);
            } else if (getMessage("BaseOnSmaller").equals(s)) {
                attributes.setRatioAdjustion(ImageConverter.KeepRatioType.BaseOnSmaller);
            } else {
                attributes.setRatioAdjustion(ImageConverter.KeepRatioType.None);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkRatio() {
        try {
            width = Integer.valueOf(widthInput.getText());
            height = Integer.valueOf(heightInput.getText());
            attributes.setTargetWidth(width);
            attributes.setTargetHeight(height);
            int sourceX = imageInformation.getxPixels();
            int sourceY = imageInformation.getyPixels();
            if (noRatio || !keepRatioCheck.isSelected() || sourceX <= 0 || sourceY <= 0) {
                return;
            }
            long ratioX = Math.round(width * 1000 / sourceX);
            long ratioY = Math.round(height * 1000 / sourceY);
            if (ratioX == ratioY) {
                return;
            }
            switch (attributes.getRatioAdjustion()) {
                case ImageConverter.KeepRatioType.BaseOnWidth:
                    heightInput.setText(Math.round(width * sourceY / sourceX) + "");
                    break;
                case ImageConverter.KeepRatioType.BaseOnHeight:
                    widthInput.setText(Math.round(height * sourceX / sourceY) + "");
                    break;
                case ImageConverter.KeepRatioType.BaseOnLarger:
                    if (ratioX > ratioY) {
                        heightInput.setText(Math.round(width * sourceY / sourceX) + "");
                    } else {
                        widthInput.setText(Math.round(height * sourceX / sourceY) + "");
                    }
                    break;
                case ImageConverter.KeepRatioType.BaseOnSmaller:
                    if (ratioX > ratioY) {
                        widthInput.setText(Math.round(height * sourceX / sourceY) + "");
                    } else {
                        heightInput.setText(Math.round(width * sourceY / sourceX) + "");
                    }
                    break;
                default:
                    break;
            }
            width = Integer.valueOf(widthInput.getText());
            height = Integer.valueOf(heightInput.getText());
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    protected void setScale() {
        if (scale == 1.0 || scale <= 0) {
            return;
        }
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.scaleImage(currentImage, imageInformation.getImageFormat(), scale);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        showLabel();
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

    protected void setPixels() {
        if (width <= 0 || height <= 0) {
            return;
        }
        final Image currentImage = imageView.getImage();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlTools.scaleImage(currentImage, imageInformation.getImageFormat(), width, height);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        showLabel();
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

    @Override
    public boolean stageReloading() {
//        logger.debug("stageReloading");
        return checkSavingBeforeExit();
    }

    @Override
    public boolean stageClosing() {
//        logger.debug("stageClosing");
        if (!checkSavingBeforeExit()) {
            return false;
        }
        return super.stageClosing();
    }

    public boolean checkSavingBeforeExit() {
        if (imageChanged.getValue()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(AppVaribles.getMessage("AppTitle"));
            alert.setContentText(AppVaribles.getMessage("ImageChanged"));
            ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("Save"));
            ButtonType buttonNotSave = new ButtonType(AppVaribles.getMessage("NotSave"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                save();
                return true;
            } else if (result.get() == buttonNotSave) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
