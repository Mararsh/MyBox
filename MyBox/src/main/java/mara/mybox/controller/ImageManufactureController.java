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
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
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
import javafx.scene.control.Label;
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
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.image.ImageConvertionTools;
import mara.mybox.image.ImageGrayTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageAttributes;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.objects.ImageScope;
import mara.mybox.objects.ImageScope.AreaScopeType;
import mara.mybox.objects.ImageScope.OperationType;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.tools.FxmlImageTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureController extends ImageViewerController {

    protected File refFile;
    final protected String ImageSortTypeKey, ImageOpenAfterSaveAsKey, ImageReferenceDisplayKey, ImageSaveConfirmKey;
    final protected String ImageFontSizeKey, ImageFontFamilyKey, ImageWatermarkColorKey, ImageWatermarkShadowKey;
    final protected String ImageCutMarginsTypeKey, ImageShadowKey, ImageArcKey;
    protected ScrollPane refPane, scopePane;
    protected ImageView refView, scopeView;
    protected Label refLabel;
    protected VBox refBox, scopeBox;
    protected Image refImage, undoImage, redoImage, cropImage, currentImage, scopeImage;
    protected ImageFileInformation refInfo, scopeInfo;
    protected boolean noRatio, isScale, isSettingValues, areaValid, cutMarginsByWidth, scopePaneValid;
    protected float scale = 1.0f, shearX, waterTransparent = 0.5f;
    protected int width, height, colorOperationType, filtersOperationType, shadow, arc, waterX, waterY, waterSize, waterShadow;
    protected int pixelPickingType, colorValue, cropLeftX, cropLeftY, cropRightX, cropRightY, binaryThreshold, addMarginWidth, cutMarginWidth;
    protected int waterAngle, replaceColorScopeType;
    protected ImageScope currentScope, colorScope, filtersScope, replaceColorScope, cropScope;
    protected SimpleBooleanProperty imageChanged;
    protected String initTab;
    protected TextField scopeText;

    @FXML
    protected ToolBar fileBar, refBar, hotBar, scaleBar, replaceColorBar, watermarkBar, transformBar;
    @FXML
    protected Tab fileTab, viewTab, colorTab, filtersTab, watermarkTab, cropTab, arcTab, shadowTab;
    @FXML
    protected Tab replaceColorTab, sizeTab, refTab, browseTab, transformTab, cutMarginsTab, addMarginsTab;
    @FXML
    protected Slider zoomSlider, angleSlider, colorSlider, binarySlider;
    @FXML
    protected Label zoomValue, colorUnit, tipsLabel, binaryValue, thresholdLabel;
    @FXML
    protected ToggleGroup pixelsGroup, filtersGroup, colorGroup, cutMarginGroup, replaceScopeGroup;
    @FXML
    protected Button origImageButton, selectRefButton, pixelsOkButton, saveButton, leftButton, rightButton;
    @FXML
    protected Button pickNewColorButton, recoverButton, replaceColorOkButton;
    @FXML
    protected Button waterPositionButton, waterAddButton, undoButton, redoButton, cropOkButton, filtersScopeButton, shearButton;
    @FXML
    protected Button colorScopeButton, binaryCalculateButton, binaryOkButton, colorDecreaseButton, colorIncreaseButton, addMarginsOkButton;
    @FXML
    protected Button cutMarginsWhiteButton, cutMarginsBlackButton, cutMarginsOkButton;
    @FXML
    protected Button transForScopeButton, transForAddMarginsButton, transForNewButton, transForArcButton, transShadowButton, cutMarginsTrButton;
    @FXML
    protected CheckBox openCheck, saveCheck, keepRatioCheck, refSyncCheck, showRefCheck, showScopeCheck;
    @FXML
    protected CheckBox cutMarginsTopCheck, cutMarginsBottomCheck, cutMarginsLeftCheck, cutMarginsRightCheck;
    @FXML
    protected CheckBox addMarginsTopCheck, addMarginsBottomCheck, addMarginsLeftCheck, addMarginsRightCheck;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected TextField widthInput, heightInput, colorInput;
    @FXML
    protected TextField waterInput, waterXInput, waterYInput;
    @FXML
    protected TextField cropLeftXInput, cropLeftYInput, cropRightXInput, cropRightYInput;
    @FXML
    protected ChoiceBox ratioBox, waterStyleBox, waterFamilyBox;
    @FXML
    private TabPane tabPane;
    @FXML
    protected ColorPicker waterColorPicker, cutMarginsColorPicker, addMarginsColorPicker, arcColorPicker, shadowColorPicker;
    @FXML
    protected ColorPicker newColorPicker, scopeColorPicker;
    @FXML
    protected ComboBox angleBox, colorBox, shearBox, scaleBox, shadowBox, arcBox, addMarginBox, cutMarginBox;
    @FXML
    protected ComboBox waterSizeBox, waterTransparentBox, waterShadowBox, waterAngleBox;
    @FXML
    protected RadioButton opacityRadio, redRadio, cutMarginsByWidthRadio, cutMarginsByColorRadio;
    @FXML
    protected HBox hotBox;
    @FXML
    protected VBox imageBox;

    public static class ColorOperationType {

        public static int Brightness = 0;
        public static int Sauration = 1;
        public static int Hue = 2;
        public static int Opacity = 3;
        public static int Red = 4;
        public static int Green = 5;
        public static int Blue = 6;

    }

    public static class FiltersOperationType {

        public static int Gray = 0;
        public static int Invert = 1;
        public static int BlackOrWhite = 2;
        public static int Red = 3;
        public static int Green = 4;
        public static int Blue = 5;

    }

    public static class PixelPickingType {

        public static int None = 0;
        public static int ReplaceColor = 1;
        public static int Watermark = 2;
        public static int MarginColor = 6;
    }

    public static class ReplaceColorScopeType {

        public static int Color = 0;
        public static int Hue = 1;
        public static int Settings = 2;
    }

    public ImageManufactureController() {
        ImageSortTypeKey = "ImageSortType";
        ImageOpenAfterSaveAsKey = "ImageOpenAfterSaveAs";
        ImageSaveConfirmKey = "ImageSaveConfirmKey";
        ImageReferenceDisplayKey = "ImageReferenceDisplay";
        ImageFontSizeKey = "ImageFontSizeKey";
        ImageFontFamilyKey = "ImageFontFamilyKey";
        ImageWatermarkColorKey = "ImageWatermarkColorKey";
        ImageWatermarkShadowKey = "ImageWatermarkShadowKey";
        ImageShadowKey = "ImageShadowKey";
        ImageArcKey = "ImageArcKey";
        ImageCutMarginsTypeKey = "ImageCutMarginsTypeKey";
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initFileTab();
            initViewTab();
            initBrowseTab();
            initPixelsTab();
            initColorTab();
            initFiltersTab();
            initTransformTab();
            initReplaceColorTab();
            initWatermarkTab();
            initArcTab();
            initShadowTab();
            initReferenceTab();
            initCutMarginsTab();
            initAddMarginsTab();
            initCropTab();

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
            viewTab.setDisable(true);
            colorTab.setDisable(true);
            filtersTab.setDisable(true);
            replaceColorTab.setDisable(true);
            sizeTab.setDisable(true);
            refTab.setDisable(true);
            transformTab.setDisable(true);
            watermarkTab.setDisable(true);
            arcTab.setDisable(true);
            shadowTab.setDisable(true);
            cutMarginsTab.setDisable(true);
            addMarginsTab.setDisable(true);
            cropTab.setDisable(true);
            hotBar.setDisable(true);

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> observable,
                        Tab oldValue, Tab newValue) {
                    imageView.setImage(currentImage);
                    imageView.setCursor(Cursor.OPEN_HAND);

                    Tab tab = tabPane.getSelectionModel().getSelectedItem();
                    pixelPickingType = PixelPickingType.None;
                    hidePopup();
                    if (watermarkTab.equals(tab)) {
                        pixelPickingType = PixelPickingType.Watermark;
                        popInformation(getMessage("ClickImageForPosition"));

                    } else if (replaceColorTab.equals(tab)) {
                        checkReplaceColorScope();

                    } else if (cutMarginsTab.equals(tab)) {
                        checkCutMarginType();

                    }
                    setScopePane();
                }
            });

            Tooltip tips = new Tooltip(getMessage("ImageManufactureTips"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(tipsLabel, tips);

            if (AppVaribles.showComments) {
                tips = new Tooltip(getMessage("ImageRefTips"));
                tips.setFont(new Font(16));
                FxmlTools.setComments(showRefCheck, tips);

                tips = new Tooltip(getMessage("ShowScopeComments"));
                tips.setFont(new Font(16));
                FxmlTools.setComments(showScopeCheck, tips);
            }

            showScopeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    setScopePane();
                }
            });

            imageChanged = new SimpleBooleanProperty(false);
            imageChanged.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (imageChanged.getValue()) {
                        saveButton.setDisable(false);
                        recoverButton.setDisable(false);
                        undoButton.setDisable(false);
                        redoButton.setDisable(true);
                        getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath() + "*");
                    } else {
                        saveButton.setDisable(true);
                        recoverButton.setDisable(true);
                        if (sourceFile != null) {
                            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
                        }
                    }
                    setBottomLabel();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    // File Methods
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
        try {
            super.afterImageLoaded();
            if (image == null) {
                return;
            }
            fileBar.setDisable(false);
            navBar.setDisable(false);
            viewTab.setDisable(false);
            colorTab.setDisable(false);
            filtersTab.setDisable(false);
            arcTab.setDisable(false);
            shadowTab.setDisable(false);
            replaceColorTab.setDisable(false);
            sizeTab.setDisable(false);
            refTab.setDisable(false);
            hotBar.setDisable(false);
            browseTab.setDisable(false);
            if (CommonValues.NoAlphaImages.contains(imageInformation.getImageFormat())) {
                opacityRadio.setDisable(true);
                transForScopeButton.setDisable(true);
                transForAddMarginsButton.setDisable(true);
                transForNewButton.setDisable(true);
                transForArcButton.setDisable(true);
                transShadowButton.setDisable(true);
                cutMarginsTrButton.setDisable(true);
                waterTransparentBox.setDisable(true);
                waterTransparentBox.getSelectionModel().select("1.0");

            } else {
                opacityRadio.setDisable(false);
                transForScopeButton.setDisable(false);
                transForAddMarginsButton.setDisable(false);
                transForNewButton.setDisable(false);
                transForArcButton.setDisable(false);
                transShadowButton.setDisable(false);
                cutMarginsTrButton.setDisable(false);
                waterTransparentBox.setDisable(false);
            }
            transformTab.setDisable(false);
            watermarkTab.setDisable(false);
            cutMarginsTab.setDisable(false);
            addMarginsTab.setDisable(false);
            cropTab.setDisable(false);

            isSettingValues = true;

            currentImage = image;
            imageChanged.set(false);

            widthInput.setText(imageInformation.getxPixels() + "");
            heightInput.setText(imageInformation.getyPixels() + "");
            attributes.setSourceWidth(imageInformation.getxPixels());
            attributes.setSourceHeight(imageInformation.getyPixels());

            waterXInput.setText(imageInformation.getxPixels() / 2 + "");
            waterYInput.setText(imageInformation.getyPixels() / 2 + "");

            colorScope = new ImageScope();
            colorScope.setOperationType(OperationType.Color);
            colorScope.setAllColors(true);
            colorScope.setAreaScopeType(AreaScopeType.AllArea);

            filtersScope = new ImageScope();
            filtersScope.setOperationType(OperationType.Filters);
            filtersScope.setAllColors(true);
            filtersScope.setAreaScopeType(AreaScopeType.AllArea);

            cropRightXInput.setText(imageInformation.getxPixels() * 3 / 4 + "");
            cropRightYInput.setText(imageInformation.getyPixels() * 3 / 4 + "");
            cropLeftXInput.setText(imageInformation.getxPixels() / 4 + "");
            cropLeftYInput.setText(imageInformation.getyPixels() / 4 + "");

            cropScope = new ImageScope();
            cropScope.setOperationType(OperationType.Crop);
            cropScope.setAreaScopeType(AreaScopeType.Rectangle);
            cropScope.setIndicateScope(true);

            arcBox.getItems().clear();
            arcBox.getItems().addAll(Arrays.asList(imageInformation.getxPixels() / 6 + "",
                    imageInformation.getxPixels() / 8 + "",
                    imageInformation.getxPixels() / 4 + "",
                    imageInformation.getxPixels() / 10 + "",
                    "0", "15", "30", "50", "150", "300", "10", "3"));
            arcBox.getSelectionModel().select(0);

            shadowBox.getItems().clear();
            shadowBox.getItems().addAll(Arrays.asList(imageInformation.getxPixels() / 100 + "",
                    imageInformation.getxPixels() / 50 + "",
                    imageInformation.getxPixels() / 200 + "",
                    imageInformation.getxPixels() / 30 + "",
                    "0", "4", "5", "3", "2", "1", "6"));
            shadowBox.getSelectionModel().select(0);

//            if (image.getWidth() > 2000 || image.getHeight() > 2000) {
//                waterShadowBox.setDisable(true);
//            } else {
//                waterShadowBox.setDisable(false);
//            }
            pixelPickingType = PixelPickingType.None;
            imageView.setCursor(Cursor.OPEN_HAND);

            if (initTab != null) {
                switch (initTab) {
                    case "size":
                        tabPane.getSelectionModel().select(sizeTab);
                        break;
                    case "crop":
                        tabPane.getSelectionModel().select(cropTab);
                        break;
                    case "color":
                        tabPane.getSelectionModel().select(colorTab);
                        break;
                    case "filters":
                        tabPane.getSelectionModel().select(filtersTab);
                        break;
                    case "replaceColor":
                        tabPane.getSelectionModel().select(replaceColorTab);
                        break;
                    case "watermark":
                        tabPane.getSelectionModel().select(watermarkTab);
                        break;
                    case "arc":
                        tabPane.getSelectionModel().select(arcTab);
                        break;
                    case "shadow":
                        tabPane.getSelectionModel().select(shadowTab);
                        break;
                    case "transform":
                        tabPane.getSelectionModel().select(transformTab);
                        break;
                    case "cutMargins":
                        tabPane.getSelectionModel().select(cutMarginsTab);
                        break;
                    case "addMargins":
                        tabPane.getSelectionModel().select(addMarginsTab);
                        break;
                    case "view":
                        tabPane.getSelectionModel().select(viewTab);
                        break;
                    case "reference":
                        tabPane.getSelectionModel().select(refTab);
                        break;
                    case "browse":
                        tabPane.getSelectionModel().select(browseTab);
                        break;
                }
            }

            isSettingValues = false;

            checkImageNevigator();
            straighten();
            setBottomLabel();

            undoButton.setDisable(true);
            redoButton.setDisable(true);
            getMyStage().setTitle(AppVaribles.getMessage("ImageManufacture") + "  " + sourceFile.getAbsolutePath());
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    // View Methods
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

    @FXML
    @Override
    public void moveRight() {
        FxmlTools.setScrollPane(scrollPane, -40, scrollPane.getVvalue());
        if (refSyncCheck.isSelected() && refPane != null) {
            FxmlTools.setScrollPane(refPane, -40, refPane.getVvalue());
        }
        if (scopePane != null) {
            FxmlTools.setScrollPane(scopePane, -40, scopePane.getVvalue());
        }
    }

    @FXML
    @Override
    public void moveLeft() {
        FxmlTools.setScrollPane(scrollPane, 40, scrollPane.getVvalue());
        if (refSyncCheck.isSelected() && refPane != null) {
            FxmlTools.setScrollPane(refPane, 40, refPane.getVvalue());
        }
        if (scopePane != null) {
            FxmlTools.setScrollPane(scopePane, 40, scopePane.getVvalue());
        }
    }

    @FXML
    @Override
    public void moveUp() {
        FxmlTools.setScrollPane(scrollPane, scrollPane.getHvalue(), 40);
        if (refSyncCheck.isSelected() && refPane != null) {
            FxmlTools.setScrollPane(refPane, refPane.getHvalue(), 40);
        }
        if (scopePane != null) {
            FxmlTools.setScrollPane(scopePane, scopePane.getHvalue(), 40);
        }
    }

    @FXML
    @Override
    public void moveDown() {
        FxmlTools.setScrollPane(scrollPane, scrollPane.getHvalue(), -40);
        if (refSyncCheck.isSelected() && refPane != null) {
            FxmlTools.setScrollPane(refPane, refPane.getHvalue(), -40);
        }
        if (scopePane != null) {
            FxmlTools.setScrollPane(scopePane, scopePane.getHvalue(), -40);
        }
    }

    // Browse Methods
    protected void initBrowseTab() {
        try {
            sortGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkImageNevigator();
                    RadioButton selected = (RadioButton) sortGroup.getSelectedToggle();
                    AppVaribles.setConfigValue(ImageSortTypeKey, selected.getText());
                }
            });
            FxmlTools.setRadioSelected(sortGroup, AppVaribles.getConfigValue(ImageSortTypeKey, getMessage("FileName")));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void nextAction() {
        if (!checkSavingBeforeExit()) {
            return;
        }
        if (nextFile != null) {
            loadImage(nextFile.getAbsoluteFile(), false);
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (!checkSavingBeforeExit()) {
            return;
        }
        if (previousFile != null) {
            loadImage(previousFile.getAbsoluteFile(), false);
        }
    }

    // Pixels methods
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
                    checkRatio();
                }
            });
            checkRatio();

            pixelsOkButton.disableProperty().bind(
                    widthInput.styleProperty().isEqualTo(badStyle)
                            .or(heightInput.styleProperty().isEqualTo(badStyle))
                            .or(scaleBox.getEditor().styleProperty().isEqualTo(badStyle))
            );

            pixelsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    RadioButton selected = (RadioButton) pixelsGroup.getSelectedToggle();
                    if (getMessage("Pixels").equals(selected.getText())) {
                        scaleBar.setDisable(false);
                        isScale = false;
                        scaleBox.getEditor().setStyle(null);
                    } else {
                        scaleBar.setDisable(true);
                        isScale = true;
                        widthInput.setStyle(null);
                        heightInput.setStyle(null);
                    }
                }
            });

            ratioBox.getItems().addAll(Arrays.asList(getMessage("BaseOnLarger"),
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

            scaleBox.getItems().addAll(Arrays.asList("1.0", "0.5", "2.0", "0.8", "0.1", "1.5", "3.0", "10.0", "0.01", "5.0", "0.3"));
            scaleBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        scale = Float.valueOf(newValue);
                        if (scale > 0) {
                            scaleBox.getEditor().setStyle(null);
                            if (currentImage != null) {
                                noRatio = true;
                                widthInput.setText(Math.round(currentImage.getWidth() * scale) + "");
                                heightInput.setText(Math.round(currentImage.getHeight() * scale) + "");
                                noRatio = false;
                            }
                        } else {
                            scaleBox.getEditor().setStyle(badStyle);
                        }

                    } catch (Exception e) {
                        scale = 0;
                        scaleBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            scaleBox.getSelectionModel().select(0);

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

    protected void checkRatioAdjustion(String s) {
        try {
            if (getMessage("BaseOnWidth").equals(s)) {
                attributes.setRatioAdjustion(ImageConvertionTools.KeepRatioType.BaseOnWidth);
            } else if (getMessage("BaseOnHeight").equals(s)) {
                attributes.setRatioAdjustion(ImageConvertionTools.KeepRatioType.BaseOnHeight);
            } else if (getMessage("BaseOnLarger").equals(s)) {
                attributes.setRatioAdjustion(ImageConvertionTools.KeepRatioType.BaseOnLarger);
            } else if (getMessage("BaseOnSmaller").equals(s)) {
                attributes.setRatioAdjustion(ImageConvertionTools.KeepRatioType.BaseOnSmaller);
            } else {
                attributes.setRatioAdjustion(ImageConvertionTools.KeepRatioType.None);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkRatio() {
        if (!keepRatioCheck.isSelected()) {
            return;
        }
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
                case ImageConvertionTools.KeepRatioType.BaseOnWidth:
                    heightInput.setText(Math.round(width * sourceY / sourceX) + "");
                    break;
                case ImageConvertionTools.KeepRatioType.BaseOnHeight:
                    widthInput.setText(Math.round(height * sourceX / sourceY) + "");
                    break;
                case ImageConvertionTools.KeepRatioType.BaseOnLarger:
                    if (ratioX > ratioY) {
                        heightInput.setText(Math.round(width * sourceY / sourceX) + "");
                    } else {
                        widthInput.setText(Math.round(height * sourceX / sourceY) + "");
                    }
                    break;
                case ImageConvertionTools.KeepRatioType.BaseOnSmaller:
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

    @FXML
    public void pixelsCalculator() {
        try {
            attributes.setKeepRatio(keepRatioCheck.isSelected());
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.PixelsCalculatorFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            final PixelsCalculationController controller = fxmlLoader.getController();
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
                    if (!controller.stageClosing()) {
                        event.consume();
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void pixelsAction() {
        if (isScale) {
            setScale();
        } else {
            setPixels();
        }
    }

    protected void setScale() {
        if (scale <= 0) {
            return;
        }
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.scaleImage(currentImage, imageInformation.getImageFormat(), scale);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        setBottomLabel();
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
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.scaleImage(currentImage, imageInformation.getImageFormat(), width, height);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        setBottomLabel();
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

    // Color Methods
    protected void initColorTab() {
        try {
            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkColorOperationType();
                }
            });
            checkColorOperationType();

            colorSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    colorValue = newValue.intValue();
                    colorInput.setText(colorValue + "");
                }
            });

            colorInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColorInput();
                }
            });
            checkColorInput();

            if (AppVaribles.showComments) {
                Tooltip stips = new Tooltip(getMessage("ScopeComments"));
                stips.setFont(new Font(16));
                FxmlTools.setComments(colorScopeButton, stips);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkColorOperationType() {
        RadioButton selected = (RadioButton) colorGroup.getSelectedToggle();
        if (getMessage("Brightness").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Brightness;
            colorSlider.setMax(100);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            colorInput.setText("5");
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Saturation").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Sauration;
            colorSlider.setMax(100);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            colorInput.setText("5");
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Hue").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Hue;
            colorSlider.setMax(360);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText(getMessage("Degree"));
            colorInput.setText("5");
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Opacity").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Opacity;
            colorSlider.setMax(100);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("%");
            colorInput.setText("50");
            colorDecreaseButton.setVisible(false);
            colorIncreaseButton.setText(getMessage("OK"));
        } else if (getMessage("Red").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Red;
            colorSlider.setMax(255);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            colorInput.setText("5");
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Green").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Green;
            colorSlider.setMax(255);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            colorInput.setText("5");
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        } else if (getMessage("Blue").equals(selected.getText())) {
            colorOperationType = ColorOperationType.Blue;
            colorSlider.setMax(255);
            colorSlider.setMin(0);
            colorSlider.setBlockIncrement(1);
            colorUnit.setText("");
            colorInput.setText("5");
            colorDecreaseButton.setVisible(true);
            colorIncreaseButton.setText(getMessage("Increase"));
        }
    }

    private void checkColorInput() {
        try {
            colorValue = Integer.valueOf(colorInput.getText());
            if (colorValue >= 0 && colorValue <= colorSlider.getMax()) {
                colorInput.setStyle(null);
                colorSlider.setValue(colorValue);
            } else {
                colorInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            colorInput.setStyle(badStyle);
        }
    }

    @FXML
    public void increaseColor() {
        if (colorOperationType == ColorOperationType.Brightness) {
            increaseBrightness();
        } else if (colorOperationType == ColorOperationType.Sauration) {
            increaseSaturate();
        } else if (colorOperationType == ColorOperationType.Hue) {
            increaseHue();
        } else if (colorOperationType == ColorOperationType.Opacity) {
            setOpacity();
        } else if (colorOperationType == ColorOperationType.Red) {
            increaseRed();
        } else if (colorOperationType == ColorOperationType.Green) {
            increaseGreen();
        } else if (colorOperationType == ColorOperationType.Blue) {
            increaseBlue();
        }
    }

    @FXML
    public void decreaseColor() {
        if (colorOperationType == ColorOperationType.Brightness) {
            decreaseBrightness();
        } else if (colorOperationType == ColorOperationType.Sauration) {
            decreaseSaturate();
        } else if (colorOperationType == ColorOperationType.Hue) {
            decreaseHue();
        } else if (colorOperationType == ColorOperationType.Red) {
            decreaseRed();
        } else if (colorOperationType == ColorOperationType.Green) {
            decreaseGreen();
        } else if (colorOperationType == ColorOperationType.Blue) {
            decreaseBlue();
        }
    }

    @FXML
    public void setColorScope() {
        setScope(colorScope);
    }

    @FXML
    public void wholeColorScope() {
        colorScope = new ImageScope();
        colorScope.setOperationType(OperationType.Color);
        colorScope.setAllColors(true);
        colorScope.setAreaScopeType(AreaScopeType.AllArea);
        setScopePane();

    }

    public void increaseHue() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeHue(currentImage, colorValue, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void decreaseHue() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeHue(currentImage, 0 - colorValue, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void increaseSaturate() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeSaturate(currentImage, colorValue / 100.0f, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void decreaseSaturate() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeSaturate(currentImage, 0.0f - colorValue / 100.0f, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void increaseBrightness() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeBrightness(currentImage, colorValue / 100.0f, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void decreaseBrightness() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeBrightness(currentImage, 0.0f - colorValue / 100.0f, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void setOpacity() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.setOpacity(currentImage, colorValue, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void increaseRed() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeRed(currentImage, colorValue, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void increaseGreen() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeGreen(currentImage, colorValue, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void increaseBlue() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeBlue(currentImage, colorValue, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void decreaseRed() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeRed(currentImage, 0 - colorValue, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void decreaseGreen() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeGreen(currentImage, 0 - colorValue, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void decreaseBlue() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.changeBlue(currentImage, 0 - colorValue, colorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    // Filters Methods
    protected void initFiltersTab() {
        try {
            filtersGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkFiltersOperationType();
                }
            });
            checkFiltersOperationType();

            binarySlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    binaryThreshold = newValue.intValue();
                    binaryValue.setText(binaryThreshold + "%");
                }
            });
            binarySlider.setValue(50);

            if (AppVaribles.showComments) {
                Tooltip stips = new Tooltip(getMessage("ScopeComments"));
                stips.setFont(new Font(16));
                FxmlTools.setComments(filtersScopeButton, stips);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkFiltersOperationType() {
        RadioButton selected = (RadioButton) filtersGroup.getSelectedToggle();
        if (getMessage("BlackOrWhite").equals(selected.getText())) {
            filtersOperationType = FiltersOperationType.BlackOrWhite;
            binarySlider.setDisable(false);
            binaryValue.setDisable(false);
            thresholdLabel.setDisable(false);
            binaryCalculateButton.setDisable(false);
        } else {
            if (getMessage("Gray").equals(selected.getText())) {
                filtersOperationType = FiltersOperationType.Gray;
            } else if (getMessage("Invert").equals(selected.getText())) {
                filtersOperationType = FiltersOperationType.Invert;
            } else if (getMessage("Red").equals(selected.getText())) {
                filtersOperationType = FiltersOperationType.Red;
            } else if (getMessage("Green").equals(selected.getText())) {
                filtersOperationType = FiltersOperationType.Green;
            } else if (getMessage("Blue").equals(selected.getText())) {
                filtersOperationType = FiltersOperationType.Blue;
            }
            binarySlider.setDisable(true);
            binaryValue.setDisable(true);
            thresholdLabel.setDisable(true);
            binaryCalculateButton.setDisable(true);
        }
    }

    @FXML
    public void setFiltersScope() {
        setScope(filtersScope);
    }

    @FXML
    public void wholeFiltersScope() {
        filtersScope = new ImageScope();
        filtersScope.setOperationType(OperationType.Filters);
        filtersScope.setAllColors(true);
        filtersScope.setAreaScopeType(AreaScopeType.AllArea);
        setScopePane();
    }

    @FXML
    public void calculateThreshold() {
        int threshold = ImageGrayTools.calculateThreshold(sourceFile);
        binaryThreshold = threshold * 100 / 256;
        binarySlider.setValue(binaryThreshold);
    }

    @FXML
    public void filtersAction() {
        if (filtersOperationType == FiltersOperationType.Gray) {
            setGray();
        } else if (filtersOperationType == FiltersOperationType.Invert) {
            setInvert();
        } else if (filtersOperationType == FiltersOperationType.BlackOrWhite) {
            setBinary();
        } else if (filtersOperationType == FiltersOperationType.Red) {
            setRed();
        } else if (filtersOperationType == FiltersOperationType.Green) {
            setGreen();
        } else if (filtersOperationType == FiltersOperationType.Blue) {
            setBlue();
        }
    }

    public void setInvert() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.makeInvert(currentImage, filtersScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void setGray() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.makeGray(currentImage, filtersScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void setBinary() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.makeBinary(currentImage, binaryThreshold, filtersScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void setRed() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.keepRed(currentImage, filtersScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void setGreen() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.keepGreen(currentImage, filtersScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    public void setBlue() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.keepBlue(currentImage, filtersScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    // Transform Methods
    protected void initTransformTab() {
        try {

            if (AppVaribles.showComments) {
                Tooltip tips = new Tooltip(getMessage("transformComments"));
                tips.setFont(new Font(16));
                FxmlTools.setComments(transformBar, tips);
            }

            List<String> shears = Arrays.asList(
                    "0.5", "-0.5", "0.4", "-0.4", "0.2", "-0.2", "0.1", "-0.1",
                    "0.7", "-0.7", "0.9", "-0.9", "0.8", "-0.8", "1", "-1",
                    "1.5", "-1.5", "2", "-2");
            shearBox.getItems().addAll(shears);
            shearBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        shearX = Float.valueOf(newValue);
                        shearBox.getEditor().setStyle(null);
                        shearButton.setDisable(false);
                    } catch (Exception e) {
                        shearX = 0;
                        shearBox.getEditor().setStyle(badStyle);
                        shearButton.setDisable(true);
                    }
                }
            });
            shearBox.getSelectionModel().select(0);

            angleSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    rotateAngle = newValue.intValue();
                    angleBox.getEditor().setText(rotateAngle + "");
                    leftButton.setDisable(false);
                    rightButton.setDisable(false);
                }
            });

            angleBox.getItems().addAll(Arrays.asList("90", "180", "45", "30", "60", "15", "75", "120", "135"));
            angleBox.setVisibleRowCount(10);
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
            angleBox.getSelectionModel().select(0);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void rightRotate() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.rotateImage(currentImage, rotateAngle);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        setBottomLabel();
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
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.rotateImage(currentImage, 360 - rotateAngle);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        setBottomLabel();
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
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.horizontalImage(currentImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        setBottomLabel();
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
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.verticalImage(currentImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        setBottomLabel();
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
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.shearImage(currentImage, shearX, 0);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        setBottomLabel();
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

    //  Replace Colors Methods
    protected void initReplaceColorTab() {
        try {

            replaceScopeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkReplaceColorScope();
                }
            });

            scopeColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> ov,
                        Color oldValue, Color newValue) {
                    setScopeColor(newValue);
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkReplaceColorScope() {
        try {
            RadioButton selected = (RadioButton) replaceScopeGroup.getSelectedToggle();
            if (AppVaribles.getMessage("Hue").equals(selected.getText())) {
                replaceColorScopeType = ReplaceColorScopeType.Hue;
                pixelPickingType = PixelPickingType.ReplaceColor;
                popInformation(getMessage("ClickForReplaceColor"));
                setScopeColor(scopeColorPicker.getValue());
            } else if (AppVaribles.getMessage("Settings").equals(selected.getText())) {
                replaceColorScopeType = ReplaceColorScopeType.Settings;
                pixelPickingType = PixelPickingType.None;
                setScope(replaceColorScope);
            } else {
                replaceColorScopeType = ReplaceColorScopeType.Color;
                pixelPickingType = PixelPickingType.ReplaceColor;
                popInformation(getMessage("ClickForReplaceColor"));
                setScopeColor(scopeColorPicker.getValue());
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void setScopeColor(Color color) {
        try {
            if (replaceColorScopeType == ReplaceColorScopeType.Settings) {
                return;
            }
            replaceColorScope = new ImageScope();
            replaceColorScope.setOperationType(OperationType.ReplaceColor);
            replaceColorScope.setAllColors(false);
            replaceColorScope.setAreaScopeType(AreaScopeType.AllArea);
            if (replaceColorScopeType == ReplaceColorScopeType.Color) {
                replaceColorScope.setMatchColor(true);
                replaceColorScope.setColorDistance(0);
                replaceColorScope.setColorExcluded(false);
            } else if (replaceColorScopeType == ReplaceColorScopeType.Hue) {
                replaceColorScope.setMatchColor(false);
                replaceColorScope.setHueDistance(0);
            } else {
                return;
            }
            replaceColorScope.setColorExcluded(false);
            List<Color> colors = new ArrayList<>();
            colors.add(color);
            replaceColorScope.setColors(colors);
            setScopePane();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void transparentForNew() {
        newColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void whiteForNew() {
        newColorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void blackForNew() {
        newColorPicker.setValue(Color.BLACK);
    }

    @FXML
    public void transparentForScope() {
        scopeColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void whiteForScope() {
        scopeColorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void blackForScope() {
        scopeColorPicker.setValue(Color.BLACK);
    }

    @FXML
    public void replaceColorAction() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.replaceColors(currentImage, newColorPicker.getValue(), replaceColorScope);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    // Watermark Methods
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
                            AppVaribles.setConfigValue(ImageWatermarkShadowKey, newValue);
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
            waterShadowBox.getSelectionModel().select(AppVaribles.getConfigValue(ImageWatermarkShadowKey, "0"));

            List<String> styles = Arrays.asList(getMessage("Regular"), getMessage("Bold"), getMessage("Italic"), getMessage("Bold Italic"));
            waterStyleBox.getItems().addAll(styles);
            waterStyleBox.getSelectionModel().select(0);

            GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] fontNames = e.getAvailableFontFamilyNames();
            waterFamilyBox.getItems().addAll(Arrays.asList(fontNames));
            waterFamilyBox.getSelectionModel().select(AppVaribles.getConfigValue(ImageFontFamilyKey, fontNames[0]));
            waterFamilyBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    AppVaribles.setConfigValue(ImageFontFamilyKey, newValue);
                }
            });

            waterColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> observable,
                        Color oldValue, Color newValue) {
                    AppVaribles.setConfigValue(ImageWatermarkColorKey, newValue.toString());
                }
            });
            waterColorPicker.setValue(Color.web(AppVaribles.getConfigValue(ImageWatermarkColorKey, "#FFFFFF")));

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
            if (waterX >= 0 && waterX <= currentImage.getWidth()) {
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
            if (waterY >= 0 && waterY <= currentImage.getHeight()) {
                waterYInput.setStyle(null);
            } else {
                waterYInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            waterYInput.setStyle(badStyle);
        }
    }

    @FXML
    public void waterAddAction() {
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
//        final Image newImage = FxImageTools.addWatermarkFx(currentImage, waterInput.getText(),
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
                final Image newImage = FxmlImageTools.addWatermark(currentImage, waterInput.getText(),
                        font, waterColorPicker.getValue(), waterX, waterY,
                        waterTransparent, waterShadow, waterAngle);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
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

    // Reference Methods
    protected void initReferenceTab() {
        try {
            showRefCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    checkReferenceImage();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkReferenceImage() {
        try {

            if (showRefCheck.isSelected()) {

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
                                    + AppVaribles.getMessage("Pixels") + ":" + refInfo.getxPixels() + "x" + refInfo.getyPixels();
                            if (refInfo.getFile() != null) {
                                str += "  " + AppVaribles.getMessage("Size") + ":" + FileTools.showFileSize(refInfo.getFile().length()) + "  "
                                        + AppVaribles.getMessage("ModifyTime") + ":" + DateTools.datetimeToString(refInfo.getFile().lastModified());
                            }
                            bottomLabel.setText(str);
                        }
                    });
                    refPane.setContent(refView);
                }

                if (refBox == null) {
                    refBox = new VBox();
                    VBox.setVgrow(refBox, Priority.ALWAYS);
                    HBox.setHgrow(refBox, Priority.ALWAYS);
                    refLabel = new Label();
                    refLabel.setText(getMessage("Reference"));
                    refLabel.setAlignment(Pos.CENTER);
                    VBox.setVgrow(refLabel, Priority.NEVER);
                    HBox.setHgrow(refLabel, Priority.ALWAYS);
                    refBox.getChildren().add(0, refLabel);
                    refBox.getChildren().add(1, refPane);
                }

                if (refFile == null) {
                    refFile = sourceFile;
                }
                if (refImage == null) {
                    loadReferenceImage();
                } else {
                    refView.setImage(refImage);
                    if (refInfo != null) {
//                            logger.debug(scrollPane.getHeight() + " " + refInfo.getyPixels());
                        if (scrollPane.getHeight() < refInfo.getyPixels()) {
                            refView.setFitHeight(scrollPane.getHeight() - 5); // use attributes of scrollPane but not refPane
//                                refView.setFitWidth(scrollPane.getWidth() - 1);
                        } else {
                            refView.setFitHeight(refInfo.getyPixels());
//                                refView.setFitWidth(refInfo.getxPixels());
                        }
                    }
                }

                if (!splitPane.getItems().contains(refBox)) {
                    splitPane.getItems().add(0, refBox);
                }

                refBar.setDisable(false);

            } else {

                if (refBox != null && splitPane.getItems().contains(refBox)) {
                    splitPane.getItems().remove(refBox);
                }
                refBar.setDisable(true);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

        setSplitPane();

    }

    private void loadReferenceImage() {
        if (refFile == null || sourceFile == null) {
            return;
        }
        if (refFile.getAbsolutePath().equals(sourceFile.getAbsolutePath())) {
            refImage = image;
            refInfo = imageInformation;
            refView.setImage(refImage);
            if (scrollPane.getHeight() < refInfo.getyPixels()) {
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
                        setBottomLabel();
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
            AppVaribles.setConfigValue(LastPathKey, sourceFile.getParent());
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
    public void popRefInformation() {
        showImageInformation(refInfo);
    }

    @FXML
    public void popRefMeta() {
        showImageMetaData(refInfo);
    }

    // Cut Margins Methods
    protected void initCutMarginsTab() {
        try {
            cutMarginGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkCutMarginType();
                }
            });
            FxmlTools.setRadioSelected(cutMarginGroup, AppVaribles.getConfigValue(ImageCutMarginsTypeKey, getMessage("ByWidth")));
            cutMarginsByWidth = cutMarginsByWidthRadio.isSelected();

            cutMarginBox.getItems().addAll(Arrays.asList("5", "10", "2", "15", "20", "30", "1"));
            cutMarginBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkCutMarginWidth();
                }
            });
            cutMarginBox.getSelectionModel().select(0);

            cutMarginsOkButton.disableProperty().bind(
                    cutMarginBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkCutMarginType() {
        RadioButton selected = (RadioButton) cutMarginGroup.getSelectedToggle();
        AppVaribles.setConfigValue(ImageCutMarginsTypeKey, selected.getText());
        if (getMessage("ByWidth").equals(selected.getText())) {
            pixelPickingType = PixelPickingType.None;
            cutMarginBox.setDisable(false);
            checkCutMarginWidth();
            cutMarginsTrButton.setDisable(true);
            cutMarginsWhiteButton.setDisable(true);
            cutMarginsBlackButton.setDisable(true);
            cutMarginsColorPicker.setDisable(true);
            cutMarginsByWidth = true;
        } else {
            pixelPickingType = PixelPickingType.MarginColor;
            if (currentImage != null) {
                popInformation(getMessage("ClickImageForColor"));
            }
            cutMarginBox.setDisable(true);
            cutMarginBox.getEditor().setStyle(null);
            if (imageInformation != null
                    && !CommonValues.NoAlphaImages.contains(imageInformation.getImageFormat())) {
                cutMarginsTrButton.setDisable(false);
            }
            cutMarginsWhiteButton.setDisable(false);
            cutMarginsBlackButton.setDisable(false);
            cutMarginsColorPicker.setDisable(false);
            cutMarginsByWidth = false;
        }
    }

    private void checkCutMarginWidth() {
        try {
            cutMarginWidth = Integer.valueOf((String) cutMarginBox.getSelectionModel().getSelectedItem());
            if (cutMarginWidth > 0) {
                cutMarginBox.getEditor().setStyle(null);
            } else {
                cutMarginWidth = 0;
                cutMarginBox.getEditor().setStyle(badStyle);
            }

        } catch (Exception e) {
            cutMarginWidth = 0;
            cutMarginBox.getEditor().setStyle(badStyle);
        }
    }

    @FXML
    public void cutMarginsTransparentAction() {
        cutMarginsColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void cutMarginsBlackAction() {
        cutMarginsColorPicker.setValue(Color.BLACK);
    }

    @FXML
    public void cutMarginsWhiteAction() {
        cutMarginsColorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void cutMarginsAction() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image newImage;
                    if (cutMarginsByWidth) {
                        newImage = FxmlImageTools.cutMarginsByWidth(currentImage, cutMarginWidth,
                                cutMarginsTopCheck.isSelected(), cutMarginsBottomCheck.isSelected(),
                                cutMarginsLeftCheck.isSelected(), cutMarginsRightCheck.isSelected());
                    } else {
                        newImage = FxmlImageTools.cutMarginsByColor(currentImage, cutMarginsColorPicker.getValue(),
                                cutMarginsTopCheck.isSelected(), cutMarginsBottomCheck.isSelected(),
                                cutMarginsLeftCheck.isSelected(), cutMarginsRightCheck.isSelected());
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            undoImage = currentImage;
                            currentImage = newImage;
                            imageView.setImage(newImage);
                            imageChanged.set(true);
                            setBottomLabel();
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
    // Add Margins Methods

    protected void initAddMarginsTab() {
        try {
            addMarginBox.getItems().addAll(Arrays.asList("5", "10", "2", "15", "20", "30", "1"));
            addMarginBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        addMarginWidth = Integer.valueOf(newValue);
                        if (addMarginWidth > 0) {
                            addMarginBox.getEditor().setStyle(null);
                        } else {
                            addMarginWidth = 0;
                            addMarginBox.getEditor().setStyle(badStyle);
                        }

                    } catch (Exception e) {
                        addMarginWidth = 0;
                        addMarginBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            addMarginBox.getSelectionModel().select(0);

            addMarginsOkButton.disableProperty().bind(
                    addMarginBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void addMarginsTransparentAction() {
        addMarginsColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void addMarginsBlackAction() {
        addMarginsColorPicker.setValue(Color.BLACK);
    }

    @FXML
    public void addMarginsWhiteAction() {
        addMarginsColorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void addMarginsAction() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.addMarginsFx(currentImage,
                        addMarginsColorPicker.getValue(), addMarginWidth,
                        addMarginsTopCheck.isSelected(), addMarginsBottomCheck.isSelected(),
                        addMarginsLeftCheck.isSelected(), addMarginsRightCheck.isSelected());
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        setBottomLabel();
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

    // Arc Methods
    protected void initArcTab() {
        try {
            arcBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        arc = Integer.valueOf(newValue);
                        if (arc >= 0) {
                            arcBox.getEditor().setStyle(null);
                            AppVaribles.setConfigValue(ImageArcKey, newValue);
                        } else {
                            arc = 0;
                            arcBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        arc = 0;
                        arcBox.getEditor().setStyle(badStyle);
                    }
                }
            });

            arcColorPicker.setValue(Color.TRANSPARENT);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void arcTransparentAction() {
        arcColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void arcWhiteAction() {
        arcColorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void arcBlackAction() {
        arcColorPicker.setValue(Color.BLACK);
    }

    @FXML
    public void arcAction() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.addArc(currentImage, arc, arcColorPicker.getValue());
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        setBottomLabel();
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
    public void arcAction2() {
        if (arc <= 0) {
            return;
        }
        final Image newImage = FxmlImageTools.addArcFx(currentImage, arc, arcColorPicker.getValue());
        if (newImage == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("ErrorForBigImage"));
            Optional<ButtonType> result = alert.showAndWait();
            return;
        }

        undoImage = currentImage;
        currentImage = newImage;
        imageView.setImage(newImage);
        imageChanged.set(true);
        setBottomLabel();

    }

    // Shadow Methods
    protected void initShadowTab() {
        try {
            shadowBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        shadow = Integer.valueOf(newValue);
                        if (shadow >= 0) {
                            shadowBox.getEditor().setStyle(null);
                            AppVaribles.setConfigValue(ImageShadowKey, newValue);
                        } else {
                            shadow = 0;
                            shadowBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        shadow = 0;
                        shadowBox.getEditor().setStyle(badStyle);
                    }
                }
            });

            shadowColorPicker.setValue(Color.BLACK);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void shadowTransparentAction() {
        shadowColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void shadowWhiteAction() {
        shadowColorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void shadowBlackAction() {
        shadowColorPicker.setValue(Color.BLACK);
    }

    @FXML
    public void shadowAction() {
        if (shadow <= 0) {
            return;
        }
        try {
            Image newImage = FxmlImageTools.addShadowFx(currentImage, shadow, shadowColorPicker.getValue());
            if (newImage != null) {
                undoImage = currentImage;
                currentImage = newImage;
                imageView.setImage(newImage);
                imageChanged.set(true);
                setBottomLabel();
                return;
            }
        } catch (Exception e) {

        }

        Image newImage = FxmlImageTools.addShadowBigFx(currentImage, shadow, shadowColorPicker.getValue());
        if (newImage != null) {
            undoImage = currentImage;
            currentImage = newImage;
            imageView.setImage(newImage);
            imageChanged.set(true);
            setBottomLabel();
            return;
        }

        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.addShadow(currentImage, shadow, shadowColorPicker.getValue());
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        undoImage = currentImage;
                        currentImage = newImage;
                        imageView.setImage(newImage);
                        imageChanged.set(true);
                        setBottomLabel();
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

    // Crop Methods
    protected void initCropTab() {
        try {

            cropLeftXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCropValues();
                }
            });
            cropLeftYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCropValues();
                }
            });
            cropRightXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCropValues();
                }
            });
            cropRightYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCropValues();
                }
            });

            cropOkButton.disableProperty().bind(
                    cropLeftXInput.styleProperty().isEqualTo(badStyle)
                            .or(cropLeftYInput.styleProperty().isEqualTo(badStyle))
                            .or(cropRightXInput.styleProperty().isEqualTo(badStyle))
                            .or(cropRightYInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(cropLeftXInput.textProperty()))
                            .or(Bindings.isEmpty(cropLeftYInput.textProperty()))
                            .or(Bindings.isEmpty(cropRightXInput.textProperty()))
                            .or(Bindings.isEmpty(cropRightYInput.textProperty()))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkCropValues() {
        areaValid = true;
        try {
            cropLeftX = Integer.valueOf(cropLeftXInput.getText());
            cropLeftXInput.setStyle(null);
            if (cropLeftX >= 0 && cropLeftX <= currentImage.getWidth()) {
                cropLeftXInput.setStyle(null);
            } else {
                cropLeftXInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            cropLeftXInput.setStyle(badStyle);
            areaValid = false;
        }

        try {
            cropLeftY = Integer.valueOf(cropLeftYInput.getText());
            cropLeftYInput.setStyle(null);
            if (cropLeftY >= 0 && cropLeftY <= currentImage.getHeight()) {
                cropLeftYInput.setStyle(null);
            } else {
                cropLeftYInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            cropLeftYInput.setStyle(badStyle);
            areaValid = false;
        }

        try {
            cropRightX = Integer.valueOf(cropRightXInput.getText());
            cropRightXInput.setStyle(null);
            if (cropRightX >= 0 && cropRightX <= currentImage.getWidth()) {
                cropRightXInput.setStyle(null);
            } else {
                cropRightXInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            cropRightXInput.setStyle(badStyle);
            areaValid = false;
        }

        try {
            cropRightY = Integer.valueOf(cropRightYInput.getText());
            cropRightYInput.setStyle(null);
            if (cropRightY >= 0 && cropRightY <= currentImage.getHeight()) {
                cropRightYInput.setStyle(null);
            } else {
                cropRightYInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            cropRightYInput.setStyle(badStyle);
            areaValid = false;
        }

        if (cropLeftX >= cropRightX) {
            cropLeftXInput.setStyle(badStyle);
            cropRightXInput.setStyle(badStyle);
            areaValid = false;
        }

        if (cropLeftY >= cropRightY) {
            cropLeftYInput.setStyle(badStyle);
            cropRightYInput.setStyle(badStyle);
            areaValid = false;
        }

        if (!isSettingValues) {
            if (!areaValid) {
                popError(getMessage("InvalidRectangle"));
                return;
            }
            showCropScope();
        }

    }

    private void showCropScope() {
        if (!areaValid || !showScopeCheck.isSelected()) {
            imageView.setImage(currentImage);
            popInformation(getMessage("CropComments"));
            return;
        }

        cropScope.setLeftX(cropLeftX);
        cropScope.setLeftY(cropLeftY);
        cropScope.setRightX(cropRightX);
        cropScope.setRightY(cropRightY);

        if (task != null && task.isRunning()) {
            return;
        }
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image newImage = FxmlImageTools.indicateAreaFx(currentImage, cropScope,
                            Color.RED, (int) currentImage.getWidth() / 100);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImage(newImage);
                            popInformation(AppVaribles.getMessage("CropComments"));
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
    public void cropAction() {
        pixelPickingType = PixelPickingType.None;
        imageView.setCursor(Cursor.OPEN_HAND);

        Task cropTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image newImage = FxmlImageTools.cropImage(currentImage,
                            cropLeftX, cropLeftY, cropRightX, cropRightY);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            undoImage = currentImage;
                            currentImage = newImage;
                            imageView.setImage(newImage);
                            imageChanged.set(true);

                            isSettingValues = true;
                            cropRightXInput.setText((int) currentImage.getWidth() + "");
                            cropRightYInput.setText((int) currentImage.getHeight() + "");
                            cropLeftXInput.setText("0");
                            cropLeftYInput.setText("0");
                            isSettingValues = false;
                            setBottomLabel();

                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(cropTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(cropTask);
        thread.setDaemon(true);
        thread.start();
    }

    //  Hotbar Methods
    @FXML
    public void recovery() {
        imageView.setImage(image);
        undoImage = currentImage;
        currentImage = image;
        imageChanged.set(false);
        undoButton.setDisable(false);
        redoButton.setDisable(true);
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
    public void save() {
        if (saveCheck.isSelected()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("SureOverrideFile"));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK) {
                return;
            }
        }

        Task saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String format = imageInformation.getImageFormat();
                final BufferedImage bufferedImage = FxmlImageTools.getWritableData(currentImage, format);
                ImageFileWriters.writeImageFile(bufferedImage, format, sourceFile.getAbsolutePath());
                imageInformation = ImageFileReaders.readImageMetaData(sourceFile.getAbsolutePath());
                image = currentImage;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageChanged.set(false);
                        setBottomLabel();
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
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue(targetPathKey, file.getParent());

            Task saveTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String format = FileTools.getFileSuffix(file.getName());
                    final BufferedImage bufferedImage = FxmlImageTools.getWritableData(currentImage, format);
                    ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (openCheck.isSelected()) {
                                openImageManufactureInNew(file.getAbsolutePath());
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
    @Override
    public void zoomIn() {
        try {
            super.zoomIn();
            if (refSyncCheck.isSelected() && refView != null) {
                refView.setFitWidth(imageView.getFitWidth());
                refView.setFitHeight(imageView.getFitWidth());
            }
            if (scopeView != null) {
                scopeView.setFitWidth(imageView.getFitWidth());
                scopeView.setFitHeight(imageView.getFitHeight());
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void zoomOut() {
        super.zoomOut();
        if (refSyncCheck.isSelected() && refView != null) {
            refView.setFitWidth(imageView.getFitWidth());
            refView.setFitHeight(imageView.getFitWidth());
        }
        if (scopeView != null) {
            scopeView.setFitWidth(imageView.getFitWidth());
            scopeView.setFitHeight(imageView.getFitHeight());
        }
    }

    @FXML
    @Override
    public void imageSize() {
        imageView.setFitHeight(-1);
        imageView.setFitWidth(-1);
        if (refSyncCheck.isSelected() && refView != null) {
            refView.setFitHeight(-1);
            refView.setFitWidth(-1);
        }
        if (scopeView != null) {
            scopeView.setFitHeight(-1);
            scopeView.setFitWidth(-1);
        }
    }

    @FXML
    @Override
    public void paneSize() {
        imageView.setFitWidth(scrollPane.getWidth() - 5);
        imageView.setFitHeight(scrollPane.getHeight() - 5);
        if (refSyncCheck.isSelected() && refView != null) {
            refView.setFitWidth(scrollPane.getWidth() - 5);
            refView.setFitHeight(scrollPane.getHeight() - 5);
        }
        if (scopeView != null) {
            scopeView.setFitWidth(scrollPane.getWidth() - 5);
            scopeView.setFitHeight(scrollPane.getHeight() - 5);
        }
    }

    // Common Methods
    @FXML
    public void setBottomLabel() {
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
    public void clickImage(MouseEvent event) {
        if (currentImage == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        imageView.setCursor(Cursor.HAND);

        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (cropTab.equals(tab)) {
            int x = (int) Math.round(event.getX() * currentImage.getWidth() / imageView.getBoundsInLocal().getWidth());
            int y = (int) Math.round(event.getY() * currentImage.getHeight() / imageView.getBoundsInLocal().getHeight());

            if (event.getButton() == MouseButton.SECONDARY) {

                isSettingValues = true;
                cropRightXInput.setText(x + "");
                cropRightYInput.setText(y + "");
                isSettingValues = false;

                if (!areaValid) {
                    popError(getMessage("InvalidRectangle"));
                } else if (task == null || !task.isRunning()) {
                    showCropScope();
                }

            } else if (event.getButton() == MouseButton.PRIMARY) {

                isSettingValues = true;
                cropLeftXInput.setText(x + "");
                cropLeftYInput.setText(y + "");
                isSettingValues = false;

                if (!areaValid) {
                    popError(getMessage("InvalidRectangle"));
                } else if (task == null || !task.isRunning()) {
                    showCropScope();
                }
            }

        } else if (replaceColorTab.equals(tab)) {

            if (pixelPickingType != PixelPickingType.ReplaceColor) {
                imageView.setCursor(Cursor.OPEN_HAND);
                return;
            }

            int x = (int) Math.round(event.getX() * currentImage.getWidth() / imageView.getBoundsInLocal().getWidth());
            int y = (int) Math.round(event.getY() * currentImage.getHeight() / imageView.getBoundsInLocal().getHeight());
            PixelReader pixelReader = currentImage.getPixelReader();
            Color color = pixelReader.getColor(x, y);

            if (event.getButton() == MouseButton.PRIMARY) {
                scopeColorPicker.setValue(color);

            } else if (event.getButton() == MouseButton.SECONDARY) {
                newColorPicker.setValue(color);
            }

            popInformation(getMessage("ClickForReplaceColor"));

        } else if (watermarkTab.equals(tab)) {

            int x = (int) Math.round(event.getX() * currentImage.getWidth() / imageView.getBoundsInLocal().getWidth());
            int y = (int) Math.round(event.getY() * currentImage.getHeight() / imageView.getBoundsInLocal().getHeight());

            waterXInput.setText(x + "");
            waterYInput.setText(y + "");
            popInformation(getMessage("ContinueClickPosition"));

        } else if (cutMarginsTab.equals(tab)) {

            if (pixelPickingType != PixelPickingType.MarginColor) {
                imageView.setCursor(Cursor.OPEN_HAND);
                return;
            }

            int x = (int) Math.round(event.getX() * currentImage.getWidth() / imageView.getBoundsInLocal().getWidth());
            int y = (int) Math.round(event.getY() * currentImage.getHeight() / imageView.getBoundsInLocal().getHeight());

            PixelReader pixelReader = currentImage.getPixelReader();
            Color color = pixelReader.getColor(x, y);
            cutMarginsColorPicker.setValue(color);
            popInformation(getMessage("ContinueClickColor"));

        }
    }

    @FXML
    public void undoAction() {
        if (undoImage == null) {
            undoButton.setDisable(true);
        }
        redoImage = currentImage;
        currentImage = undoImage;
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
        undoImage = currentImage;
        currentImage = redoImage;
        imageView.setImage(redoImage);
        imageChanged.set(true);
        undoButton.setDisable(false);
        redoButton.setDisable(true);
    }

    public void setScope(ImageScope imageScope) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImageScopeFxml), AppVaribles.CurrentBundle);
            Pane pane = fxmlLoader.load();
            final ImageScopeController controller = fxmlLoader.getController();
            Stage stage = new Stage();
            controller.setMyStage(stage);
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    if (!controller.stageClosing()) {
                        event.consume();
                    }
                }
            });

            Scene scene = new Scene(pane);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(getMyStage());
            stage.getIcons().add(CommonValues.AppIcon);
            stage.setScene(scene);
            stage.show();

            imageScope.setImage(currentImage);
            String title = AppVaribles.getMessage("ImageManufactureScope");
            switch (imageScope.getOperationType()) {
                case OperationType.Color:
                    title += " - " + AppVaribles.getMessage("Color");
                    break;
                case OperationType.ReplaceColor:
                    title += " - " + AppVaribles.getMessage("ReplaceColor");
                    break;
                case OperationType.Filters:
                    title += " - " + AppVaribles.getMessage("Filters");
                    break;
                case OperationType.Crop:
                    title += " - " + AppVaribles.getMessage("Crop");
                    break;
                default:
                    break;
            }
            controller.loadImage(this, imageScope, title);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void setScopePane() {
        try {
            Tab tab = tabPane.getSelectionModel().getSelectedItem();
            showScopeCheck.setDisable(true);
            currentScope = null;
            scopePaneValid = false;
            if (colorTab.equals(tab)) {
                showScopeCheck.setDisable(false);
                currentScope = colorScope;
                scopePaneValid = true;

            } else if (replaceColorTab.equals(tab)) {
                showScopeCheck.setDisable(false);
                currentScope = replaceColorScope;
                if (replaceColorScopeType == ReplaceColorScopeType.Settings) {
                    scopePaneValid = true;
                } else {
                    scopePaneValid = false;
                }

            } else if (filtersTab.equals(tab)) {
                showScopeCheck.setDisable(false);
                currentScope = filtersScope;
                scopePaneValid = true;

            } else if (cropTab.equals(tab)) {
                showScopeCheck.setDisable(false);
                currentScope = cropScope;
                showCropScope();

            }

            if (currentScope == null) {
                scopeImage = null;
                scopeInfo = null;

            } else {
                if (currentScope.isAll()) {
                    scopeImage = currentImage;
                } else {
                    scopeImage = currentScope.getImage();
                }
                scopeInfo = new ImageFileInformation();
                scopeInfo.setImageFormat(imageInformation.getImageFormat());
                scopeInfo.setxPixels(imageInformation.getxPixels());
                scopeInfo.setyPixels(imageInformation.getyPixels());
            }

            if (scopePaneValid && showScopeCheck.isSelected()) {

                if (scopePane == null) {
                    scopePane = new ScrollPane();
                    scopePane.setPannable(true);
                    scopePane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    VBox.setVgrow(scopePane, Priority.ALWAYS);
                    HBox.setHgrow(scopePane, Priority.ALWAYS);
                }
                if (scopeView == null) {
                    scopeView = new ImageView();
                    scopeView.setPreserveRatio(true);
                    scopeView.setOnMouseEntered(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            String str = AppVaribles.getMessage("Format") + ":" + scopeInfo.getImageFormat() + "  "
                                    + AppVaribles.getMessage("Pixels") + ":" + scopeInfo.getxPixels() + "x" + scopeInfo.getyPixels();
                            if (scopeInfo.getFile() != null) {
                                str += "  " + AppVaribles.getMessage("Size") + ":" + FileTools.showFileSize(scopeInfo.getFile().length()) + "  "
                                        + AppVaribles.getMessage("ModifyTime") + ":" + DateTools.datetimeToString(scopeInfo.getFile().lastModified());
                            }
                            bottomLabel.setText(str);
                        }
                    });
                    scopePane.setContent(scopeView);
                }

                if (scopeBox == null) {
                    scopeBox = new VBox();
                    VBox.setVgrow(scopeBox, Priority.ALWAYS);
                    HBox.setHgrow(scopeBox, Priority.ALWAYS);
                    scopeText = new TextField();
                    scopeText.setAlignment(Pos.CENTER_LEFT);
                    scopeText.setEditable(false);
                    VBox.setVgrow(scopeText, Priority.NEVER);
                    HBox.setHgrow(scopeText, Priority.ALWAYS);
                    scopeBox.getChildren().add(0, scopeText);
                    scopeBox.getChildren().add(1, scopePane);
                }
                scopeText.setText(getMessage("CurrentScope") + ":"
                        + ImageScopeController.getScopeText(currentScope));

                Tooltip stips = new Tooltip(getMessage("ScopeImageComments"));
                stips.setFont(new Font(16));
                FxmlTools.quickTooltip(scopeBox, stips);

                scopeView.setImage(scopeImage);

                if (!splitPane.getItems().contains(scopeBox)) {
                    splitPane.getItems().add(0, scopeBox);

                }

            } else {
                if (scopeBox != null && splitPane.getItems().contains(scopeBox)) {
                    splitPane.getItems().remove(scopeBox);
                }
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

        setSplitPane();

    }

    private void setSplitPane() {
        switch (splitPane.getItems().size()) {
            case 3:
                splitPane.getDividers().get(0).setPosition(0.33333);
                splitPane.getDividers().get(1).setPosition(0.66666);
//                splitPane.setDividerPositions(0.33, 0.33, 0.33); // This way not work!
                break;
            case 2:
                splitPane.getDividers().get(0).setPosition(0.5);
//               splitPane.setDividerPositions(0.5, 0.5); // This way not work!
                break;
            default:
                splitPane.setDividerPositions(1);
                break;
        }
        splitPane.layout();
        paneSize();
    }

    public void scopeDetermined(ImageScope imageScope) {
        currentScope = imageScope;
        switch (imageScope.getOperationType()) {
            case OperationType.Color:
                colorScope = imageScope;
                break;
            case OperationType.ReplaceColor:
                replaceColorScope = imageScope;
                break;
            case OperationType.Filters:
                filtersScope = imageScope;
                break;
            default:
                break;
        }
        setScopePane();
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
            alert.setTitle(getMyStage().getTitle());
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

    public String getInitTab() {
        return initTab;
    }

    public void setInitTab(String initTab) {
        this.initTab = initTab;
    }

}
