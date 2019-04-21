package mara.mybox.controller.base;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.controller.ImageManufactureMarginsController;
import mara.mybox.controller.ImageViewerController;
import mara.mybox.data.DoublePoint;
import mara.mybox.db.TableImageHistory;
import mara.mybox.db.TableImageInit;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.data.ImageHistory;
import mara.mybox.data.ImageInformation;
import mara.mybox.data.ImageManufactureValues;
import mara.mybox.fxml.ColorCell;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.ImageManufacture;
import mara.mybox.image.ImageColor;
import mara.mybox.image.ImageScope.ColorScopeType;
import mara.mybox.image.ImageScope.ScopeType;
import mara.mybox.image.PixelsOperation.OperationType;
import mara.mybox.image.ImageScope;
import mara.mybox.image.PixelsOperation;
import mara.mybox.tools.SystemTools;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class ImageManufactureController extends ImageViewerController {

    protected String ImageTipsKey;

    protected ImageManufactureValues values;
    protected boolean isSwitchingTab;
    protected String initTab;
    protected String imageHistoriesPath;
    protected List<String> imageHistories;
    protected ImageScope scope;

    public static class ImageOperationType {

        public static int Load = 0;
        public static int Arc = 1;
        public static int Color = 2;
        public static int Crop = 3;
        public static int Text = 4;
        public static int Effects = 5;
        public static int Shadow = 8;
        public static int Size = 9;
        public static int Transform = 10;
        public static int Cut_Margins = 11;
        public static int Add_Margins = 12;
        public static int Mosaic = 13;
        public static int Convolution = 14;
        public static int Doodle = 15;
        public static int Blur_Margins = 16;
        public static int Picture = 13;
    }

    @FXML
    protected ToolBar fileBar, hotBar;
    @FXML
    protected Tab fileTab, viewTab, colorTab, textTab, doodleTab, mosaicTab, cropTab,
            arcTab, shadowTab, effectsTab, sizeTab, refTab,
            browseTab, transformTab, marginsTab;
    @FXML
    protected ScrollPane refPane, scopePane;
    @FXML
    protected AnchorPane refMaskPane, scopeMaskPane;
    @FXML
    protected ImageView refView, scopeView;
    @FXML
    protected VBox displayBox, refBox, scopeBox;
    @FXML
    protected FlowPane scopeSetBox;
    @FXML
    protected Label promptLabel, refLabel, imageTipsLabel,
            scopePointsLabel, scopeColorsLabel, scopeMatchLabel, scopeDistanceLabel;
    @FXML
    protected ComboBox<String> scopePointsBox, scopeMatchBox, scopeDistanceBox;
    @FXML
    protected ComboBox<Color> scopeColorsBox;
    @FXML
    protected Button selectRefButton, undoButton, redoButton, popButton, refButton,
            scopeDeleteButton, scopeClearButton, polygonWithdrawButton, polygonClearButton;
    @FXML
    protected CheckBox showRefCheck, showScopeCheck, saveCheck, areaExcludedCheck, colorExcludedCheck;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected HBox hotBox, refLabelBox, imageSetBox, areaSetBox;
    @FXML
    protected ComboBox<String> hisBox, scopeListBox;
    @FXML
    protected ColorPicker colorPicker, scopeColorPicker;
    @FXML
    protected ToggleButton pickColorButton;
    @FXML
    protected Text scopeXYText;

    public ImageManufactureController() {
        baseTitle = AppVaribles.getMessage("ImageManufacture");

        TipsLabelKey = "ImageManufactureTips";
        ImageSelectKey = "ImageManufactureSelectKey";
        ImageRulerXKey = "ImageManufactureRulerXKey";
        ImageRulerYKey = "ImageManufactureRulerYKey";
        ImagePopCooridnateKey = "ImageManufacturePopCooridnateKey";

        ImageTipsKey = null;
    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        if (event.isControlDown()) {
            String key = event.getText();
            if (key == null || key.isEmpty()) {
                return;
            }
            switch (key) {
                case "r":
                case "R":
                    if (!recoverButton.isDisabled()) {
                        recoverAction();
                    }
                    break;
                case "z":
                case "Z":
                    if (!undoButton.isDisabled()) {
                        undoAction();
                    }
                    break;
                case "y":
                case "Y":
                    if (!redoButton.isDisabled()) {
                        redoAction();
                    }
                    break;
                case "v":
                case "V":
                    pasteClipImage();
                    break;
                case "h":
                case "H":
                    if (!hisBox.isDisabled()) {
                        hisBox.show();
                    }
                    break;
                case "x":
                    if (!scopeClearButton.isDisabled()) {
                        clearScope();
                    }
                    break;
                case "f":
                    if (!refButton.isDisabled()) {
                        refAction();
                    }
                    break;
                case "p":
                    if (!popButton.isDisabled()) {
                        popAction();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    protected void initCommon() {
        try {
            values = new ImageManufactureValues();
            values.setRefSync(true);

            displayBox.setVisible(false);
            fileTab.setDisable(true);
            browseTab.setDisable(true);
            viewTab.setDisable(true);
            colorTab.setDisable(true);
            effectsTab.setDisable(true);
            sizeTab.setDisable(true);
            refTab.setDisable(true);
            transformTab.setDisable(true);
            doodleTab.setDisable(true);
            textTab.setDisable(true);
            mosaicTab.setDisable(true);
            arcTab.setDisable(true);
            shadowTab.setDisable(true);
            marginsTab.setDisable(true);
            cropTab.setDisable(true);

            hotBar.setDisable(true);

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> observable,
                        Tab oldTab, Tab newTab) {
                    if (isSettingValues) {
                        return;
                    }
                    hidePopup();
                    switchTab(newTab);
                }
            });

            Tooltip tips = new Tooltip(getMessage("ImageRefTips"));
            tips.setFont(new Font(16));
            FxmlControl.setComments(showRefCheck, tips);
            showRefCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    checkReferencePane();
                }
            });

//            tips = new Tooltip(getMessage("ImageHisComments"));
//            tips.setFont(new Font(16));
//            FxmlTools.setComments(hisBox, tips);
            hisBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    int index = newValue.intValue();
                    if (index < 0 || hisBox.getItems() == null) {
                        return;
                    }
                    if (getMessage("SettingsDot").equals(hisBox.getItems().get(index))) {
                        BaseController c = openStage(CommonValues.SettingsFxml);
                        c.parentController = myController;
                        c.parentFxml = myFxml;
                        return;
                    } else if (getMessage("OpenPathDot").equals(hisBox.getItems().get(index))) {
                        try {
                            Desktop.getDesktop().browse(new File(AppVaribles.getImageHisPath()).toURI());
                        } catch (Exception e) {
                            logger.error(e.toString());
                        }
                    }
                    if (imageHistories == null || imageHistories.size() <= index) {
                        return;
                    }
//                    logger.debug(index + " " + imageHistories.get(index) + "  " + hisBox.getSelectionModel().getSelectedItem());
                    loadImageHistory(index);
                }
            });
            hisBox.setVisibleRowCount(15);
            int max = AppVaribles.getUserConfigInt("MaxImageHistories", 20);
            hisBox.setDisable(max <= 0);

            FxmlControl.quickTooltip(saveButton, new Tooltip("CTRL+s"));

            FxmlControl.quickTooltip(recoverButton, new Tooltip("CTRL+r"));

            FxmlControl.quickTooltip(redoButton, new Tooltip("CTRL+y"));

            FxmlControl.quickTooltip(undoButton, new Tooltip("CTRL+z"));

            FxmlControl.quickTooltip(hisBox, new Tooltip("CTRL+h"));

            FxmlControl.quickTooltip(refButton, new Tooltip("CTRL+f"));

            FxmlControl.quickTooltip(popButton, new Tooltip("CTRL+p"));

            if (ImageTipsKey != null) {
                FxmlControl.quickTooltip(imageTipsLabel, new Tooltip(getMessage(ImageTipsKey)));
            } else {
                imageSetBox.getChildren().remove(imageTipsLabel);
                imageTipsLabel.setVisible(false);
            }

            if (showScopeCheck != null && scopeBox != null) {
                initScopeControls();
            }

            if (pickColorButton != null) {
                FxmlControl.quickTooltip(pickColorButton, new Tooltip(getMessage("ColorPickerComments")));
            }

            initMaskControls(false);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            isSettingValues = true;

            sourceFile = values.getSourceFile();
            image = values.getImage();
            imageInformation = values.getImageInfo();
            imageData = values.getImageData();
            scope = new ImageScope(image);

            displayBox.setVisible(true);
            fileTab.setDisable(false);
            cropTab.setDisable(false);
            colorTab.setDisable(false);
            effectsTab.setDisable(false);
            arcTab.setDisable(false);
            shadowTab.setDisable(false);
            sizeTab.setDisable(false);
            refTab.setDisable(false);
            hotBar.setDisable(false);
            transformTab.setDisable(false);
            doodleTab.setDisable(false);
            textTab.setDisable(false);
            mosaicTab.setDisable(false);
            marginsTab.setDisable(false);
            viewTab.setDisable(false);
            browseTab.setDisable(false);

            undoButton.setDisable(true);
            redoButton.setDisable(true);

            imageView.setPreserveRatio(true);
            imageView.setImage(values.getCurrentImage());
            imageView.setCursor(Cursor.OPEN_HAND);

            setImageChanged(values.isImageChanged());

            updateHisBox();
            showRefCheck.setSelected(values.isShowRef());
            isSettingValues = false;

            checkReferencePane();

            imageView.requestFocus();
            if (values.getImageViewHeight() > 0) {
                imageView.setFitWidth(values.getImageViewWidth());
                imageView.setFitHeight(values.getImageViewHeight());
            } else {
                fitSize();
            }

            setMaskStroke();

            checkScopeType();

            if (pickColorButton != null) {
                pickColorButton.setSelected(false);
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @Override
    protected void initImageView() {
        if (imageView == null) {
            return;
        }
        super.initImageView();

        if (refView == null) {
            return;
        }
        refView.fitWidthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                refMoveCenter();
            }
        });
        refView.fitHeightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                refMoveCenter();
            }
        });
        refPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                refMoveCenter();
            }
        });

    }

    public void refMoveCenter() {
        if (refView == null || refView.getImage() == null
                || !splitPane.getItems().contains(refBox) || !refBox.isVisible()) {
            return;
        }
        FxmlControl.moveXCenter(refPane, refView);
    }

    protected void initScopeControls() {
        try {

            FxmlControl.setComments(showScopeCheck, new Tooltip(getMessage("ShowScopeComments")));
            showScopeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    checkScopeType();
                }
            });

            scopePane.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    FxmlControl.moveXCenter(scopePane, scopeView);
                }
            });
            scopeView.fitWidthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    FxmlControl.moveXCenter(scopePane, scopeView);
                }
            });
            scopeView.fitHeightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    FxmlControl.moveXCenter(scopePane, scopeView);
                }
            });

            List<String> scopeList = Arrays.asList(getMessage("All"), getMessage("Matting"),
                    getMessage("Rectangle"), getMessage("Circle"), getMessage("Ellipse"), getMessage("Polygon"),
                    getMessage("ColorMatching"), getMessage("RectangleColor"), getMessage("CircleColor"),
                    getMessage("EllipseColor"), getMessage("PolygonColor"));
            scopeListBox.getItems().addAll(scopeList);
            scopeListBox.setVisibleRowCount(scopeList.size());
            scopeListBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    checkScopeType();
                }
            });

            scopeColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> ov, Color old_val, Color new_val) {
                    isSettingValues = true;
                    scope.addColor(ImageColor.converColor(new_val));
                    scopeColorsBox.getItems().add(new_val);
                    scopeColorsBox.getSelectionModel().select(scopeColorsBox.getItems().size() - 1);
                    isSettingValues = false;
                    indicateScope();
                }
            });

            scopeMatchBox.getItems().addAll(Arrays.asList(
                    getMessage("Color"), getMessage("Hue"), getMessage("Red"), getMessage("Green"),
                    getMessage("Blue"), getMessage("Brightness"), getMessage("Saturation")
            ));
            scopeMatchBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    AppVaribles.setUserConfigValue("ImageScopeMatchType", newValue);
                    checkMatchType();
                }
            });
            FxmlControl.setComments(scopeMatchBox, new Tooltip(getMessage("ColorMatchComments")));
            scopeMatchBox.getSelectionModel().select(AppVaribles.getUserConfigValue("ImageScopeMatchType", getMessage("Color")));

            scopeDistanceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    AppVaribles.setUserConfigValue("ImageScopeMatchDistance", newValue);
                    if (checkDistanceValue()) {
                        indicateScope();
                    }
                }
            });

            scopeColorsBox.setButtonCell(new ColorCell());
            scopeColorsBox.setCellFactory(new Callback<ListView<Color>, ListCell<Color>>() {
                @Override
                public ListCell<Color> call(ListView<Color> p) {
                    return new ColorCell();
                }
            });

            areaExcludedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    scope.setAreaExcluded(newValue);
                    indicateScope();
                }
            });
            colorExcludedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    scope.setColorExcluded(newValue);
                    indicateScope();
                }
            });

            scopeListBox.getSelectionModel().select(0);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkScopeType() {
        try {
            if (isSettingValues || showScopeCheck == null || values == null
                    || scope == null || scopeBox == null) {
                return;
            }

            scope.setImage(imageView.getImage());
            scope.clearColors();
            scope.clearPoints();
            checkCoordinate();

            String selected = scopeListBox.getSelectionModel().getSelectedItem();
            if (imageView.getImage() == null || AppVaribles.getMessage("All").equals(selected)) {
                scope.setScopeType(ImageScope.ScopeType.All);
                hideScopePane();

            } else {

                if (AppVaribles.getMessage("Matting").equals(selected)) {
                    scope.setScopeType(ImageScope.ScopeType.Matting);

                } else if (getMessage("Rectangle").equals(selected)) {
                    scope.setScopeType(ImageScope.ScopeType.Rectangle);

                } else if (getMessage("Circle").equals(selected)) {
                    scope.setScopeType(ImageScope.ScopeType.Circle);

                } else if (getMessage("Ellipse").equals(selected)) {
                    scope.setScopeType(ImageScope.ScopeType.Ellipse);

                } else if (getMessage("Polygon").equals(selected)) {
                    scope.setScopeType(ImageScope.ScopeType.Polygon);

                } else if (getMessage("ColorMatching").equals(selected)) {
                    scope.setScopeType(ImageScope.ScopeType.Color);

                } else if (getMessage("RectangleColor").equals(selected)) {
                    scope.setScopeType(ImageScope.ScopeType.RectangleColor);

                } else if (getMessage("CircleColor").equals(selected)) {
                    scope.setScopeType(ImageScope.ScopeType.CircleColor);

                } else if (getMessage("EllipseColor").equals(selected)) {
                    scope.setScopeType(ImageScope.ScopeType.EllipseColor);

                } else if (getMessage("PolygonColor").equals(selected)) {
                    scope.setScopeType(ImageScope.ScopeType.PolygonColor);

                }

                showScopePane();

            }

            extraScopeCheck();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void showScopePane() {
        try {
            if (showScopeCheck == null || scope.getScopeType() == null
                    || values == null || scope == null || scopeSetBox == null) {
                hideScopePane();
                return;
            }
            isSettingValues = true;
            showScopeCheck.setDisable(false);
            showScopeCheck.setSelected(true);
            scopePointsBox.getItems().clear();
            scopeColorsBox.getItems().clear();
            isSettingValues = false;

            initMaskControls(false);
            imageBox.getChildren().remove(areaSetBox);
            areaSetBox.getChildren().clear();

            scopeSetBox.getChildren().clear();
            scopeView.setImage(scope.getImage());
            scopeBox.setVisible(true);
            if (!splitPane.getItems().contains(scopeBox)) {
                splitPane.getItems().add(0, scopeBox);
            }

            switch (scope.getScopeType()) {
                case Matting:
                    scopeSetBox.getChildren().addAll(scopePointsLabel,
                            scopePointsBox, scopeDeleteButton, scopeClearButton,
                            scopeMatchLabel, scopeMatchBox, scopeDistanceLabel, scopeDistanceBox,
                            colorExcludedCheck);
                    scopeDeleteButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            int index = scopePointsBox.getSelectionModel().getSelectedIndex();
                            if (index >= 0) {
                                isSettingValues = true;
                                scope.getPoints().remove(index);
                                scopePointsBox.getItems().remove(index);
                                int size = scopePointsBox.getItems().size();
                                if (size > 0) {
                                    if (index > size - 1) {
                                        scopePointsBox.getSelectionModel().select(index - 1);
                                    } else {
                                        scopePointsBox.getSelectionModel().select(index);
                                    }
                                }
                                isSettingValues = false;
                                indicateScope();
                            }
                        }
                    });
                    scopeClearButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            isSettingValues = true;
                            scope.getPoints().clear();
                            scopePointsBox.getItems().clear();
                            isSettingValues = false;
                            indicateScope();
                        }
                    });
                    checkMatchType();
                    promptLabel.setText(getMessage("ClickImagesSetPoints"));
                    break;

                case Rectangle:
                    initMaskRectangleLine(true);
                    areaSetBox.getChildren().add(areaExcludedCheck);
                    imageBox.getChildren().add(1, areaSetBox);
                    promptLabel.setText(getMessage("SetAreaInRightPane"));
                    break;

                case Circle:
                    initMaskCircleLine(true);
                    areaSetBox.getChildren().add(areaExcludedCheck);
                    imageBox.getChildren().add(1, areaSetBox);
                    promptLabel.setText(getMessage("SetAreaInRightPane"));
                    break;

                case Ellipse:
                    initMaskEllipseLine(true);
                    areaSetBox.getChildren().add(areaExcludedCheck);
                    imageBox.getChildren().add(1, areaSetBox);
                    promptLabel.setText(getMessage("SetAreaInRightPane"));
                    break;

                case Polygon:
                    initMaskPolygonLine(true);
                    areaSetBox.getChildren().addAll(polygonWithdrawButton, polygonClearButton, areaExcludedCheck);
                    imageBox.getChildren().add(1, areaSetBox);
                    promptLabel.setText(getMessage("SetPolygonInRightPane"));
                    break;

                case Color:
                    scopeSetBox.getChildren().addAll(scopeColorsLabel, scopeColorsBox, scopeColorPicker,
                            scopeDeleteButton, scopeClearButton,
                            scopeMatchLabel, scopeMatchBox, scopeDistanceLabel, scopeDistanceBox,
                            colorExcludedCheck);
                    scopeDeleteButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            int index = scopeColorsBox.getSelectionModel().getSelectedIndex();
                            if (index >= 0) {
                                isSettingValues = true;
                                scope.getColors().remove(index);
                                scopeColorsBox.getItems().remove(index);
                                int size = scopeColorsBox.getItems().size();
                                if (size > 0) {
                                    if (index > size - 1) {
                                        scopeColorsBox.getSelectionModel().select(index - 1);
                                    } else {
                                        scopeColorsBox.getSelectionModel().select(index);
                                    }
                                }
                                isSettingValues = false;
                                indicateScope();
                            }
                        }
                    });
                    scopeClearButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            isSettingValues = true;
                            scope.getColors().clear();
                            scopeColorsBox.getItems().clear();
                            isSettingValues = false;
                            indicateScope();
                        }
                    });
                    checkMatchType();
                    promptLabel.setText(getMessage("ClickImagesSetColors"));
                    break;

                case RectangleColor:
                    scopeSetBox.getChildren().addAll(scopeColorsLabel, scopeColorsBox, scopeColorPicker,
                            scopeDeleteButton, scopeClearButton,
                            scopeMatchLabel, scopeMatchBox, scopeDistanceLabel, scopeDistanceBox,
                            colorExcludedCheck);
                    scopeDeleteButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            int index = scopeColorsBox.getSelectionModel().getSelectedIndex();
                            if (index >= 0) {
                                isSettingValues = true;
                                scope.getColors().remove(index);
                                scopeColorsBox.getItems().remove(index);
                                int size = scopeColorsBox.getItems().size();
                                if (size > 0) {
                                    if (index > size - 1) {
                                        scopeColorsBox.getSelectionModel().select(index - 1);
                                    } else {
                                        scopeColorsBox.getSelectionModel().select(index);
                                    }
                                }
                                isSettingValues = false;

                                indicateScope();
                            }
                        }
                    });
                    scopeClearButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            isSettingValues = true;
                            scope.getColors().clear();
                            scopeColorsBox.getItems().clear();
                            isSettingValues = false;

                            indicateScope();
                        }
                    });
                    initMaskRectangleLine(true);
                    areaSetBox.getChildren().add(areaExcludedCheck);
                    imageBox.getChildren().add(1, areaSetBox);
                    checkMatchType();
                    promptLabel.setText(getMessage("SetColorsInLeftPane"));
                    break;

                case CircleColor:
                    scopeSetBox.getChildren().addAll(scopeColorsLabel, scopeColorsBox, scopeColorPicker,
                            scopeDeleteButton, scopeClearButton,
                            scopeMatchLabel, scopeMatchBox, scopeDistanceLabel, scopeDistanceBox,
                            colorExcludedCheck);
                    scopeDeleteButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            int index = scopeColorsBox.getSelectionModel().getSelectedIndex();
                            if (index >= 0) {
                                isSettingValues = true;
                                scope.getColors().remove(index);
                                scopeColorsBox.getItems().remove(index);
                                int size = scopeColorsBox.getItems().size();
                                if (size > 0) {
                                    if (index > size - 1) {
                                        scopeColorsBox.getSelectionModel().select(index - 1);
                                    } else {
                                        scopeColorsBox.getSelectionModel().select(index);
                                    }
                                }
                                isSettingValues = false;
                                indicateScope();
                            }
                        }
                    });
                    scopeClearButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            isSettingValues = true;
                            scope.getColors().clear();
                            scopeColorsBox.getItems().clear();
                            isSettingValues = false;
                            indicateScope();
                        }
                    });
                    initMaskCircleLine(true);
                    areaSetBox.getChildren().add(areaExcludedCheck);
                    imageBox.getChildren().add(1, areaSetBox);
                    checkMatchType();
                    promptLabel.setText(getMessage("SetColorsInLeftPane"));
                    break;

                case EllipseColor:
                    scopeSetBox.getChildren().addAll(scopeColorsLabel, scopeColorsBox, scopeColorPicker,
                            scopeDeleteButton, scopeClearButton,
                            scopeMatchLabel, scopeMatchBox, scopeDistanceLabel, scopeDistanceBox,
                            colorExcludedCheck);
                    scopeDeleteButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            int index = scopeColorsBox.getSelectionModel().getSelectedIndex();
                            if (index >= 0) {
                                isSettingValues = true;
                                scope.getColors().remove(index);
                                scopeColorsBox.getItems().remove(index);
                                int size = scopeColorsBox.getItems().size();
                                if (size > 0) {
                                    if (index > size - 1) {
                                        scopeColorsBox.getSelectionModel().select(index - 1);
                                    } else {
                                        scopeColorsBox.getSelectionModel().select(index);
                                    }
                                }
                                isSettingValues = false;
                                indicateScope();
                            }
                        }
                    });
                    scopeClearButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            isSettingValues = true;
                            scope.getColors().clear();
                            scopeColorsBox.getItems().clear();
                            isSettingValues = false;
                            indicateScope();
                        }
                    });
                    initMaskEllipseLine(true);
                    areaSetBox.getChildren().add(areaExcludedCheck);
                    imageBox.getChildren().add(1, areaSetBox);
                    checkMatchType();
                    promptLabel.setText(getMessage("SetColorsInLeftPane"));
                    break;

                case PolygonColor:
                    scopeSetBox.getChildren().addAll(scopeColorsLabel, scopeColorsBox, scopeColorPicker,
                            scopeDeleteButton, scopeClearButton,
                            scopeMatchLabel, scopeMatchBox, scopeDistanceLabel, scopeDistanceBox,
                            colorExcludedCheck);
                    scopeDeleteButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            int index = scopeColorsBox.getSelectionModel().getSelectedIndex();
                            if (index >= 0) {
                                isSettingValues = true;
                                scope.getColors().remove(index);
                                scopeColorsBox.getItems().remove(index);
                                int size = scopeColorsBox.getItems().size();
                                if (size > 0) {
                                    if (index > size - 1) {
                                        scopeColorsBox.getSelectionModel().select(index - 1);
                                    } else {
                                        scopeColorsBox.getSelectionModel().select(index);
                                    }
                                }
                                isSettingValues = false;
                                indicateScope();
                            }
                        }
                    });
                    scopeClearButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            isSettingValues = true;
                            scope.getColors().clear();
                            scopeColorsBox.getItems().clear();
                            isSettingValues = false;
                            indicateScope();
                        }
                    });
                    initMaskPolygonLine(true);
                    areaSetBox.getChildren().addAll(polygonWithdrawButton, polygonClearButton, areaExcludedCheck);
                    imageBox.getChildren().add(1, areaSetBox);
                    checkMatchType();
                    promptLabel.setText(getMessage("SetColorsPolygon"));
                    break;

                default:
                    hideScopePane();
            }

            adjustSplitPane();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void hideScopePane() {
        try {
            initMaskControls(false);
            if (showScopeCheck != null) {
                showScopeCheck.setSelected(false);
                imageBox.getChildren().remove(areaSetBox);
                promptLabel.setText("");
            }
            if (scopeBox == null) {
                return;
            }
            if (splitPane.getItems().contains(scopeBox)) {
                splitPane.getItems().remove(scopeBox);
            }
            scopeBox.setVisible(false);

            adjustSplitPane();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void extraScopeCheck() {

    }

    protected void checkMatchType() {
        try {
            if (showScopeCheck == null || values == null
                    || scope == null || scopeBox == null) {
                return;
            }
            String matchType = (String) scopeMatchBox.getSelectionModel().getSelectedItem();
            int max = 255;
            if (getMessage("Color").equals(matchType)) {
                scope.setColorScopeType(ColorScopeType.Color);

            } else if (getMessage("Hue").equals(matchType)) {
                scope.setColorScopeType(ColorScopeType.Hue);
                max = 360;

            } else if (getMessage("Red").equals(matchType)) {
                scope.setColorScopeType(ColorScopeType.Red);

            } else if (getMessage("Green").equals(matchType)) {
                scope.setColorScopeType(ColorScopeType.Green);

            } else if (getMessage("Blue").equals(matchType)) {
                scope.setColorScopeType(ColorScopeType.Blue);

            } else if (getMessage("Brightness").equals(matchType)) {
                scope.setColorScopeType(ColorScopeType.Brightness);
                max = 100;

            } else if (getMessage("Saturation").equals(matchType)) {
                scope.setColorScopeType(ColorScopeType.Saturation);
                max = 100;
            }
            Tooltip tips = new Tooltip("0~" + max);
            FxmlControl.quickTooltip(scopeDistanceBox, tips);

            List<String> values = new ArrayList();
            for (int i = 0; i <= max; i += 10) {
                values.add(i + "");
            }
            isSettingValues = true;
            scopeDistanceBox.getItems().clear();
            scopeDistanceBox.getItems().addAll(values);
            isSettingValues = false;
            scopeDistanceBox.getSelectionModel().select(max / 2 + "");

            if (checkDistanceValue()) {
                indicateScope();
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected boolean checkDistanceValue() {
        if (scope.getColorScopeType() == null
                || scopeDistanceBox.getSelectionModel().getSelectedItem() == null) {
            return false;
        }
        try {
            int distance = Integer.valueOf(scopeDistanceBox.getSelectionModel().getSelectedItem());
            boolean valid = true;
            switch (scope.getColorScopeType()) {
                case Hue:
                    if (distance >= 0 && distance <= 360) {
                        FxmlControl.setEditorNormal(scopeDistanceBox);
                        scope.setHsbDistance(distance / 360.0f);
                    } else {
                        FxmlControl.setEditorBadStyle(scopeDistanceBox);
                        valid = false;
                    }
                    break;
                case Brightness:
                case Saturation:
                    if (distance >= 0 && distance <= 100) {
                        FxmlControl.setEditorNormal(scopeDistanceBox);
                        scope.setHsbDistance(distance / 100.0f);
                    } else {
                        FxmlControl.setEditorBadStyle(scopeDistanceBox);
                        valid = false;
                    }
                    break;
                default:
                    if (distance >= 0 && distance <= 255) {
                        FxmlControl.setEditorNormal(scopeDistanceBox);
                        scope.setColorDistance(distance);
                    } else {
                        FxmlControl.setEditorBadStyle(scopeDistanceBox);
                        valid = false;
                    }
            }
            return valid;
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(scopeDistanceBox);
            logger.debug(e.toString());
            return false;
        }
    }

    protected void indicateScope() {
        if (isSettingValues || scope == null || null == scope.getScopeType()
                || showScopeCheck == null || !showScopeCheck.isSelected()
                || scopeBox == null || !scopeBox.isVisible()) {
            return;
        }
        task = new Task<Void>() {
            private Image scopedImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {

                PixelsOperation pixelsOperation = PixelsOperation.newPixelsOperation(imageView.getImage(),
                        scope, OperationType.ShowScope);
                scopedImage = pixelsOperation.operateFxImage();
                if (task == null || task.isCancelled()) {
                    return null;
                }
                scope.setImage(scopedImage);

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
                            scopeView.setImage(scopedImage);
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
    public void polygonClearAction() {
        if (scope.getScopeType() != ScopeType.Polygon
                && scope.getScopeType() != ScopeType.PolygonColor
                || maskPolygonData == null || maskPolygonLine == null) {
            return;
        }
        maskPolygonData.clear();
        drawMaskPolygonLine();
    }

    @FXML
    public void polygonWithdrawAction() {
        if (scope.getScopeType() != ScopeType.Polygon
                && scope.getScopeType() != ScopeType.PolygonColor
                || maskPolygonData == null || maskPolygonLine == null) {
            return;
        }
        maskPolygonData.removeLast();
        drawMaskPolygonLine();
    }

    @Override
    public void afterImageLoaded() {
        try {
            super.afterImageLoaded();

            if (imageInformation != null && imageInformation.isIsSampled()) {
                hotBar.setDisable(false);
                showRefCheck.setDisable(true);
                hisBox.setDisable(true);
                undoButton.setDisable(true);
                redoButton.setDisable(true);
                recoverButton.setDisable(true);
                saveButton.setDisable(true);

                browseTab.setDisable(true);
                viewTab.setDisable(true);
                colorTab.setDisable(true);
                effectsTab.setDisable(true);
                sizeTab.setDisable(true);
                refTab.setDisable(true);
                transformTab.setDisable(true);
                doodleTab.setDisable(true);
                textTab.setDisable(true);
                mosaicTab.setDisable(true);
                arcTab.setDisable(true);
                shadowTab.setDisable(true);
                marginsTab.setDisable(true);
                cropTab.setDisable(true);

            }
            isSettingValues = true;
            if (values == null) {
                values = new ImageManufactureValues();
            }
            values.setSourceFile(sourceFile);
            values.setImage(image);
            values.setImageInfo(imageInformation);
            values.setCurrentImage(image);
            values.setRefImage(image);
            values.setRefInfo(imageInformation);
            scope = new ImageScope(image);
            isSettingValues = false;
            if (image == null
                    || (imageInformation != null && imageInformation.isIsSampled())) {
                return;
            }
            recordImageHistory(ImageOperationType.Load, image);
            imageData = values.getImageData();

            if (initTab != null) {
                switchTab(initTab);
            } else {
                initInterface();
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public ImageManufactureController refresh() {
        ImageManufactureValues oldV = values;
        File oldfile = sourceFile;
        ImageInformation oldInfo = imageInformation;
        Map<String, Object> oldMeta = imageData;
        Image oldImage = image;
        Tab oldTab = tabPane.getSelectionModel().getSelectedItem();
        boolean changed = imageChanged;

        ImageManufactureController c = (ImageManufactureController) refreshBase();
        if (c == null) {
            return null;
        }
        c.setInitTab(findTabName(oldTab));
        if (changed) {
            if (oldV != null) {
                c.loadImage(oldV.getSourceFile());
            } else if (oldfile != null) {
                c.loadImage(oldfile);
            }
        } else if (oldV != null) {
            c.loadImage(oldV.getSourceFile(), oldV.getImage(), oldV.getImageInfo(), oldV.getImageData());
        } else if (oldfile != null && oldImage != null && oldInfo != null) {
            if (oldMeta != null) {
                c.loadImage(oldfile, oldImage, oldInfo, oldMeta);
            } else {
                c.loadImage(oldfile, oldImage, oldInfo);
            }
        } else if (oldInfo != null) {
            c.loadImage(oldInfo);
        } else if (oldfile != null) {
            c.loadImage(oldfile);
        } else if (oldImage != null) {
            c.loadImage(oldImage);
        }

        return c;
    }

    public void setImage(final File file) {

        task = new Task<Void>() {
            private Image newImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                BufferedImage bufferImage = ImageFileReaders.readImage(file);
                if (task == null || task.isCancelled()) {
                    return null;
                }
                newImage = SwingFXUtils.toFXImage(bufferImage, null);
                if (task == null || task.isCancelled()) {
                    return null;
                }
//                recordImageHistory(ImageOperationType.Load, newImage);

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
                            values.setUndoImage(imageView.getImage());

                            imageView.setImage(newImage);
                            setImageChanged(true);
                            resetMaskControls();
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

    public void showRef() {
        showRefCheck.setSelected(true);
    }

    protected void checkReferencePane() {
        try {
            values.setShowRef(showRefCheck.isSelected());

            if (values.isShowRef()) {

                if (values.getRefFile() == null) {
                    values.setRefFile(values.getSourceFile());
                }
                if (values.getRefImage() == null) {
                    loadReferenceImage();
                } else {
                    refView.setImage(values.getRefImage());
                    if (values.getRefInfo() != null) {
//                            logger.debug(scrollPane.getHeight() + " " + refInfo.getyPixels());
                        if (refPane.getHeight() < values.getRefInfo().getHeight()) {
                            refView.setFitWidth(refPane.getWidth() - 1);
                            refView.setFitHeight(refPane.getHeight() - 5); // use attributes of scrollPane but not refPane
                        } else {
                            refView.setFitWidth(values.getRefInfo().getWidth());
                            refView.setFitHeight(values.getRefInfo().getHeight());
                        }
                    }
                }

                if (!splitPane.getItems().contains(refBox)) {
                    refBox.setVisible(true);
                    splitPane.getItems().add(0, refBox);
                }

            } else {

                if (refBox != null && splitPane.getItems().contains(refBox)) {
                    splitPane.getItems().remove(refBox);
                    refBox.setVisible(false);
                }

            }

            adjustSplitPane();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void loadReferenceImage() {
        if (values.getRefFile() == null || values.getSourceFile() == null) {
            return;
        }
        if (values.getRefFile().getAbsolutePath().equals(values.getSourceFile().getAbsolutePath())) {
            values.setRefImage(image);
            values.setRefInfo(values.getImageInfo());
            refView.setImage(image);
            if (scrollPane.getHeight() < (int) values.getImage().getWidth()) {
                refView.setFitWidth(scrollPane.getWidth() - 1);
                refView.setFitHeight(scrollPane.getHeight() - 5); // use attributes of scrollPane but not refPane
            } else {
                refView.setFitWidth((int) values.getImage().getWidth());
                refView.setFitHeight((int) values.getImage().getHeight());
            }
            return;
        }
        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                values.setRefInfo(ImageFileReaders.readImageFileMetaData(values.getRefFile().getAbsolutePath()).getImageInformation());
                if (task == null || task.isCancelled()) {
                    return null;
                }
                values.setRefImage(SwingFXUtils.toFXImage(ImageFileReaders.readImage(values.getRefFile()), null));
                if (task == null || task.isCancelled()) {
                    return null;
                }

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
                            refView.setImage(values.getRefImage());
                            if (refPane.getHeight() < values.getRefInfo().getHeight()) {
                                refView.setFitWidth(refPane.getWidth() - 1);
                                refView.setFitHeight(refPane.getHeight() - 5);
                            } else {
                                refView.setFitWidth(values.getRefInfo().getWidth());
                                refView.setFitHeight(values.getRefInfo().getHeight());
                            }
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

    @Override
    public void setImageChanged(boolean imageChanged) {
        this.imageChanged = imageChanged;
        values.setImageChanged(imageChanged);

        if (imageChanged) {
            saveButton.setDisable(false);
            recoverButton.setDisable(false);
            undoButton.setDisable(false);
            redoButton.setDisable(true);

            loadData();

        } else {
            saveButton.setDisable(true);
            recoverButton.setDisable(true);
            if (values.getSourceFile() != null) {
                getMyStage().setTitle(getBaseTitle() + "  " + values.getSourceFile().getAbsolutePath());
            }

        }
        updateLabelTitle();

        indicateScope();

    }

    public void updateHisBox() {
        int max = AppVaribles.getUserConfigInt("MaxImageHistories", 20);
        if (max <= 0 || values.getSourceFile() == null) {
            hisBox.setDisable(true);
            return;
        }
        hisBox.setDisable(false);
        hisBox.getItems().clear();
        String fname = values.getSourceFile().getAbsolutePath();
        List<ImageHistory> his = TableImageHistory.read(fname);
        List<String> hisStrings = new ArrayList<>();
        imageHistories = new ArrayList<>();
        for (ImageHistory r : his) {
            String s;
            if (r.getUpdate_type() == ImageOperationType.Load) {
                s = AppVaribles.getMessage("Load");
            } else if (r.getUpdate_type() == ImageOperationType.Add_Margins) {
                s = AppVaribles.getMessage("AddMargins");
            } else if (r.getUpdate_type() == ImageOperationType.Arc) {
                s = AppVaribles.getMessage("Arc");
            } else if (r.getUpdate_type() == ImageOperationType.Color) {
                s = AppVaribles.getMessage("Color");
            } else if (r.getUpdate_type() == ImageOperationType.Crop) {
                s = AppVaribles.getMessage("Crop");
            } else if (r.getUpdate_type() == ImageOperationType.Cut_Margins) {
                s = AppVaribles.getMessage("CutMargins");
            } else if (r.getUpdate_type() == ImageOperationType.Effects) {
                s = AppVaribles.getMessage("Effects");
            } else if (r.getUpdate_type() == ImageOperationType.Convolution) {
                s = AppVaribles.getMessage("Convolution");
            } else if (r.getUpdate_type() == ImageOperationType.Shadow) {
                s = AppVaribles.getMessage("Shadow");
            } else if (r.getUpdate_type() == ImageOperationType.Size) {
                s = AppVaribles.getMessage("Size");
            } else if (r.getUpdate_type() == ImageOperationType.Transform) {
                s = AppVaribles.getMessage("Transform");
            } else if (r.getUpdate_type() == ImageOperationType.Text) {
                s = AppVaribles.getMessage("Text");
            } else if (r.getUpdate_type() == ImageOperationType.Mosaic) {
                s = AppVaribles.getMessage("Mosaic");
            } else if (r.getUpdate_type() == ImageOperationType.Picture) {
                s = AppVaribles.getMessage("Picture");
            } else if (r.getUpdate_type() == ImageOperationType.Doodle) {
                s = AppVaribles.getMessage("Doodle");
            } else {
                continue;
            }
            s = DateTools.datetimeToString(r.getOperation_time()) + " " + s;
            hisStrings.add(s);
            imageHistories.add(r.getHistory_location());
        }
        ImageHistory init = TableImageInit.read(fname);
        if (init != null) {
            String s = DateTools.datetimeToString(init.getOperation_time()) + " " + AppVaribles.getMessage("Load");
            hisStrings.add(s);
            imageHistories.add(init.getHistory_location());
        }
        hisStrings.add(AppVaribles.getMessage("OpenPathDot"));
        hisStrings.add(AppVaribles.getMessage("SettingsDot"));
        hisBox.getItems().addAll(hisStrings);
    }

    protected void recordImageHistory(final int updateType, final Image newImage) {
        int max = AppVaribles.getUserConfigInt("MaxImageHistories", 20);
        if (values == null || values.getSourceFile() == null
                || max <= 0 || updateType < 0 || newImage == null) {
            return;
        }
        if (imageHistoriesPath == null) {
            imageHistoriesPath = AppVaribles.getImageHisPath();
        }
        Task saveTask = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                try {
                    final BufferedImage bufferedImage = ImageManufacture.getBufferedImage(newImage);
                    String filename = imageHistoriesPath + File.separator
                            + FileTools.getFilePrefix(values.getSourceFile().getName())
                            + "_" + (new Date().getTime()) + "_" + updateType
                            + "_" + new Random().nextInt(1000) + ".png";
                    while (new File(filename).exists()) {
                        filename = imageHistoriesPath + File.separator
                                + FileTools.getFilePrefix(values.getSourceFile().getName())
                                + "_" + (new Date().getTime()) + "_" + updateType
                                + "_" + new Random().nextInt(1000) + ".png";
                    }
                    filename = new File(filename).getAbsolutePath();
                    ImageFileWriters.writeImageFile(bufferedImage, "png", filename);
                    if (updateType == ImageOperationType.Load) {
                        TableImageInit.write(values.getSourceFile().getAbsolutePath(), filename);
                    } else {
                        TableImageHistory.add(values.getSourceFile().getAbsolutePath(), updateType, filename);
                    }

                } catch (Exception e) {
                    logger.debug(e.toString());
                }
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
                            updateHisBox();
                        }
                    });
                }
            }
        };
        Thread thread = new Thread(saveTask);
        thread.setDaemon(true);
        thread.start();
    }

    protected boolean loadImageHistory(final int index) {
        if (values == null || values.getSourceFile() == null
                || imageHistories == null
                || index < 0 || index > imageHistories.size() - 1) {
            return false;
        }
        String filename = imageHistories.get(index);
        try {
            File file = new File(filename);
            if (file.exists()) {
                setImage(file);
                return true;
            }
        } catch (Exception e) {
        }
        imageHistories.remove(index);
        TableImageHistory.clearHistory(values.getSourceFile().getAbsolutePath(), filename);
        return false;
    }

    public void switchTab(Tab newTab) {

        String tabName = findTabName(newTab);
        switchTab(tabName);
    }

    public void switchTab(String tabName) {
        try {
            isSwitchingTab = true;
            String fxml = null;
            values.setIsPaste(false);
            String trueName = tabName;
            switch (tabName) {
                case "file":
                    fxml = CommonValues.ImageManufactureFileFxml;
                    break;
                case "size":
                    fxml = CommonValues.ImageManufactureSizeFxml;
                    break;
                case "crop":
                    fxml = CommonValues.ImageManufactureCropFxml;
                    break;
                case "color":
                    fxml = CommonValues.ImageManufactureColorFxml;
                    break;
                case "effects":
                    fxml = CommonValues.ImageManufactureEffectsFxml;
                    break;
                case "doodle":
                    fxml = CommonValues.ImageManufactureDoodleFxml;
                    break;
                case "doodlePaste":
                    fxml = CommonValues.ImageManufactureDoodleFxml;
                    trueName = "doodle";
                    values.setIsPaste(true);
                    break;
                case "text":
                    fxml = CommonValues.ImageManufactureTextFxml;
                    break;
                case "mosaic":
                    fxml = CommonValues.ImageManufactureMosaicFxml;
                    break;
                case "arc":
                    fxml = CommonValues.ImageManufactureArcFxml;
                    break;
                case "shadow":
                    fxml = CommonValues.ImageManufactureShadowFxml;
                    break;
                case "transform":
                    fxml = CommonValues.ImageManufactureTransformFxml;
                    break;
                case "margins":
                    fxml = CommonValues.ImageManufactureMarginsFxml;
                    break;
                case "view":
                    fxml = CommonValues.ImageManufactureViewFxml;
                    break;
                case "ref":
                    fxml = CommonValues.ImageManufactureRefFxml;
                    break;
                case "browse":
                    fxml = CommonValues.ImageManufactureBrowseFxml;
                    break;
            }

            if (fxml != null) {
                values.setCurrentImage(imageView.getImage());
                values.setImageViewWidth((int) imageView.getFitWidth());
                values.setImageViewHeight((int) imageView.getFitHeight());
                values.setImageData(imageData);

                ImageManufactureController controller
                        = (ImageManufactureController) loadScene(fxml);
                if (controller == null) {
                    return;
                }

                controller.setValues(values);
                controller.setTab(trueName);
                controller.xZoomStep = xZoomStep;
                controller.yZoomStep = yZoomStep;
                controller.initInterface();
                controller.loadData(imageData);

            }
            isSwitchingTab = false;
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void setTab(String tabName) {
        try {
            Tab tab = findTab(tabName);
            if (tab == null) {
                return;
            }
            isSettingValues = true;
            tabPane.getSelectionModel().select(tab);
            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected String findTabName(Tab tab) {
        String tabName = null;
        if (fileTab.equals(tab)) {
            tabName = "file";
        } else if (sizeTab.equals(tab)) {
            tabName = "size";
        } else if (cropTab.equals(tab)) {
            tabName = "crop";
        } else if (effectsTab.equals(tab)) {
            tabName = "effects";
        } else if (colorTab.equals(tab)) {
            tabName = "color";
        } else if (doodleTab.equals(tab)) {
            tabName = "doodle";
        } else if (textTab.equals(tab)) {
            tabName = "text";
        } else if (mosaicTab.equals(tab)) {
            tabName = "mosaic";
        } else if (arcTab.equals(tab)) {
            tabName = "arc";
        } else if (shadowTab.equals(tab)) {
            tabName = "shadow";
        } else if (transformTab.equals(tab)) {
            tabName = "transform";
        } else if (marginsTab.equals(tab)) {
            tabName = "margins";
        } else if (viewTab.equals(tab)) {
            tabName = "view";
        } else if (refTab.equals(tab)) {
            tabName = "ref";
        } else if (browseTab.equals(tab)) {
            tabName = "browse";
        }
        return tabName;
    }

    public Tab findTab(String tabName) {
        switch (tabName) {
            case "file":
                return fileTab;
            case "size":
                return sizeTab;
            case "crop":
                return cropTab;
            case "color":
                return colorTab;
            case "effects":
                return effectsTab;
            case "doodle":
                return doodleTab;
            case "text":
                return textTab;
            case "mosaic":
                return mosaicTab;
            case "arc":
                return arcTab;
            case "shadow":
                return shadowTab;
            case "transform":
                return transformTab;
            case "margins":
                return marginsTab;
            case "view":
                return viewTab;
            case "ref":
                return refTab;
            case "browse":
                return browseTab;
        }
        return null;
    }

    //  Hotbar Methods
    @FXML
    @Override
    public void saveAction() {
        if (saveButton.isDisabled()) {
            return;
        }
        if (values.getSourceFile() == null) {
            saveAsAction();
            return;
        }
        if (values.isIsConfirmBeforeSave()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("SureOverrideFile"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("Save"));
            ButtonType buttonSaveAs = new ButtonType(AppVaribles.getMessage("SaveAs"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonCancel) {
                return;
            } else if (result.get() == buttonSaveAs) {
                saveAsAction();
                return;
            }

        }

        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                String format = "png";
                if (values.getImageInfo() != null) {
                    format = values.getImageInfo().getImageFormat();
                }
                final BufferedImage bufferedImage = ImageManufacture.getBufferedImage(imageView.getImage());
                if (bufferedImage == null || task == null || task.isCancelled()) {
                    return null;
                }
                ok = ImageFileWriters.writeImageFile(bufferedImage, format, values.getSourceFile().getAbsolutePath());
                if (!ok || task == null || task.isCancelled()) {
                    return null;
                }
                imageInformation = ImageFileReaders.readImageFileMetaData(values.getSourceFile().getAbsolutePath()).getImageInformation();

                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            image = imageView.getImage();
                            values.setImage(image);
                            values.setImageInfo(imageInformation);
                            values.setCurrentImage(image);
                            imageView.setImage(image);
                            setImageChanged(false);
                            popInformation(AppVaribles.getMessage("Saved"));
                        } else {
                            popInformation(AppVaribles.getMessage("Failed"));
                        }

                    }
                });
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

    @FXML
    public void createAction() {
        ImageManufactureMarginsController c
                = (ImageManufactureMarginsController) loadScene(CommonValues.ImageManufactureMarginsFxml);
        if (c == null) {
            return;
        }
        Image newImage = ImageManufacture.createImage(200, 200, Color.WHITE);
        c.loadImage(newImage);
        c.setDragMode();

    }

    @FXML
    @Override
    public void zoomIn() {
        try {
            super.zoomIn();

            if (values.isRefSync() && refBox != null && refBox.isVisible()) {
                refView.setFitWidth(imageView.getFitWidth());
                refView.setFitHeight(imageView.getFitHeight());
            }
            if (scopeBox != null && scopeBox.isVisible()) {
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
        if (values.isRefSync() && refBox != null && refBox.isVisible()) {
            refView.setFitWidth(imageView.getFitWidth());
            refView.setFitHeight(imageView.getFitHeight());
        }
        if (scopeBox != null && scopeBox.isVisible()) {
            scopeView.setFitWidth(imageView.getFitWidth());
            scopeView.setFitHeight(imageView.getFitHeight());
        }
    }

    @FXML
    @Override
    public void loadedSize() {
        super.loadedSize();
        if (values.isRefSync() && refBox != null && refBox.isVisible()) {
            refView.setFitWidth(refView.getImage().getWidth());
            refView.setFitHeight(refView.getImage().getHeight());
        }
        if (scopeBox != null && scopeBox.isVisible()) {
            scopeView.setFitWidth(scopeView.getImage().getWidth());
            scopeView.setFitHeight(scopeView.getImage().getHeight());
        }
    }

    @FXML
    @Override
    public void paneSize() {
        super.paneSize();
        if (values.isRefSync() && refBox != null && refBox.isVisible()) {
            refView.setFitWidth(refPane.getWidth() - 5);
            refView.setFitHeight(refPane.getHeight() - 5);
        }
        if (scopeBox != null && scopeBox.isVisible()) {
            scopeView.setFitWidth(scopePane.getWidth() - 5);
            scopeView.setFitHeight(scopePane.getHeight() - 5);
        }
    }

    @FXML
    @Override
    public void paneClicked(MouseEvent event) {

        if (imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (colorPicker != null && pickColorButton != null && pickColorButton.isSelected()) {

            DoublePoint p = getImageXY(event, imageView);
            if (p == null) {
                return;
            }
            PixelReader pixelReader = imageView.getImage().getPixelReader();
            Color color = pixelReader.getColor((int) Math.round(p.getX()), (int) Math.round(p.getY()));
            colorPicker.setValue(color);

        } else {

            super.paneClicked(event);
            if (scopeBox == null || !scopeBox.isVisible() || scope == null
                    || (scope.getScopeType() != ScopeType.Matting && scope.getScopeType() != ScopeType.Color)) {
                return;
            }
            // This event is defined against MaskPane and coordinate is based on MaskPane.
            DoublePoint p = getImageXY(event, imageView);
            if (p == null) {
                return;
            }
            colorPicked(p.getX(), p.getY());
        }
    }

    @FXML
    public void scopeViewClicked(MouseEvent event) {
        if (scopeView == null || values == null
                || imageView.getImage() == null
                || scopeBox == null || !scopeBox.isVisible() || scope == null) {
            return;
        }
        scopeView.setCursor(Cursor.HAND);

        // This event is defined against ImageView and the coordinate is based on ImageView itself
        double x = event.getX() * scopeView.getImage().getWidth() / scopeView.getBoundsInParent().getWidth();
        double y = event.getY() * scopeView.getImage().getHeight() / scopeView.getBoundsInParent().getHeight();

        if (pickColorButton != null && pickColorButton.isSelected()) {
            PixelReader pixelReader = imageView.getImage().getPixelReader();
            Color color = pixelReader.getColor((int) Math.round(x), (int) Math.round(y));
            colorPicker.setValue(color);
        } else {
            colorPicked(x, y);
        }

    }

    @Override
    protected void checkCoordinate() {
        super.checkCoordinate();
        if (scopeXYText != null) {
            scopeXYText.setVisible(coordinateCheck.isSelected());
        }
    }

    @Override
    public void setMaskStroke() {
        super.setMaskStroke();
        if (scopeXYText != null) {
            scopeXYText.setFill(Color.web(AppVaribles.getUserConfigValue("StrokeColor", "#FF0000")));
            scopeXYText.setText("");
        }
    }

    @FXML
    public void scopeShowXY(MouseEvent event) {
        if (scopeXYText == null || !scopeXYText.isVisible()) {
            return;
        }
        DoublePoint p = getImageXY(event, scopeView);
        if (p == null) {
            scopeXYText.setText("");
            return;
        }
        String s = (int) Math.round(p.getX()) + "," + (int) Math.round(p.getY());
        scopeXYText.setText(s);
        scopeXYText.setX(event.getX() + 10);
        scopeXYText.setY(event.getY());
    }

    public void colorPicked(double x, double y) {
        int ix = (int) Math.round(x);
        int iy = (int) Math.round(y);
        switch (scope.getScopeType()) {
            case Color:
            case RectangleColor:
            case CircleColor:
            case EllipseColor:
            case PolygonColor:
                PixelReader pixelReader = imageView.getImage().getPixelReader();
                Color color = pixelReader.getColor(ix, iy);
                isSettingValues = true;
                scope.addColor(ImageColor.converColor(color));
                scopeColorsBox.getItems().add(color);
                scopeColorsBox.getSelectionModel().select(scopeColorsBox.getItems().size() - 1);
                scopeColorsBox.setVisibleRowCount(15);
                isSettingValues = false;
                indicateScope();
                break;

            case Matting:
                isSettingValues = true;
                scope.addPoints(ix, iy);
                scopePointsBox.getItems().add(ix + "," + iy);
                scopePointsBox.getSelectionModel().select(scopePointsBox.getItems().size() - 1);
                scopePointsBox.setVisibleRowCount(15);
                isSettingValues = false;
                indicateScope();
                break;

            default:
                break;
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        boolean sizeChanged = imageView.getImage().getWidth() != image.getWidth()
                || imageView.getImage().getWidth() != image.getHeight();
        imageView.setImage(values.getImage());
        values.setUndoImage(imageView.getImage());
        setImageChanged(false);
        if (sizeChanged) {
            resetMaskControls();
        }
        undoButton.setDisable(false);
        redoButton.setDisable(true);
        loadData();
    }

    @FXML
    public void undoAction() {
        boolean sizeChanged = imageView.getImage().getWidth() != image.getWidth()
                || imageView.getImage().getWidth() != image.getHeight();
        if (values.getUndoImage() == null) {
            undoButton.setDisable(true);
        }
        values.setRedoImage(imageView.getImage());
        imageView.setImage(values.getUndoImage());
        setImageChanged(true);
        if (sizeChanged) {
            resetMaskControls();
        }
        undoButton.setDisable(true);
        redoButton.setDisable(false);
    }

    @FXML
    public void redoAction() {
        boolean sizeChanged = imageView.getImage().getWidth() != image.getWidth()
                || imageView.getImage().getWidth() != image.getHeight();
        if (values.getRedoImage() == null) {
            redoButton.setDisable(true);
        }
        values.setUndoImage(imageView.getImage());
        imageView.setImage(values.getRedoImage());
        setImageChanged(true);
        if (sizeChanged) {
            resetMaskControls();
        }
        undoButton.setDisable(false);
        redoButton.setDisable(true);
    }

    @FXML
    public void refAction() {
        values.setRefImage(imageView.getImage());
        values.setRefInfo(null);
        if (!showRefCheck.isSelected()) {
            showRefCheck.setSelected(true);
        } else if (refView != null) {
            refView.setImage(imageView.getImage());
        }
    }

    @FXML
    public void popAction() {
        ImageViewerController controller
                = (ImageViewerController) openStage(CommonValues.ImageViewerFxml);
        controller.loadImage(sourceFile, imageView.getImage(), imageInformation, imageData);
    }

    @FXML
    public void clearScope() {
        scope.clearColors();
        scope.clearPoints();

        switch (scope.getScopeType()) {
            case All:
                break;

            case Color:
                indicateScope();
                break;

            case Matting:
                indicateScope();
                break;

            case Rectangle:

                indicateScope();

                break;
            case Circle:

                indicateScope();

                break;

            default:
                break;
        }
    }

    protected void pasteClipImage() {
        Image clipImage = SystemTools.fetchImageInClipboard(false);
        if (clipImage != null) {
            switchTab("doodlePaste");
        }
    }

    @Override
    public boolean drawMaskRectangleLine() {
        if (!super.drawMaskRectangleLine()) {
            return false;
        }

        if (scopeBox != null && splitPane.getItems().contains(scopeBox)) {

            if (scope.getScopeType() == ImageScope.ScopeType.Rectangle
                    || scope.getScopeType() == ImageScope.ScopeType.RectangleColor) {
                scope.setRectangle(maskRectangleData);
                indicateScope();
            }

        }

        return true;
    }

    @Override
    public boolean drawMaskCircleLine() {
        if (!super.drawMaskCircleLine()) {
            return false;
        }

        if (scopeBox != null && splitPane.getItems().contains(scopeBox)) {

            if (scope.getScopeType() == ImageScope.ScopeType.Circle
                    || scope.getScopeType() == ImageScope.ScopeType.CircleColor) {
                scope.setCircle(maskCircleData);
                indicateScope();
            }

        }

        return true;
    }

    @Override
    public boolean drawMaskEllipseLine() {
        if (!super.drawMaskEllipseLine()) {
            return false;
        }

        if (scopeBox != null && splitPane.getItems().contains(scopeBox)) {

            if (scope.getScopeType() == ImageScope.ScopeType.Ellipse
                    || scope.getScopeType() == ImageScope.ScopeType.EllipseColor) {
                scope.setEllipse(maskEllipseData);
                indicateScope();
            }

        }

        return true;
    }

    @Override
    public boolean drawMaskPolygonLine() {
        if (!super.drawMaskPolygonLine()) {
            return false;
        }

        if (scopeBox != null && splitPane.getItems().contains(scopeBox)) {

            if (scope.getScopeType() == ImageScope.ScopeType.Polygon
                    || scope.getScopeType() == ImageScope.ScopeType.PolygonColor) {
                scope.setPolygon(maskPolygonData);
                indicateScope();
            }

        }

        return true;
    }

    @Override
    public boolean checkSavingForNextAction() {
        if (isSwitchingTab || imageView.getImage() == null || !imageChanged) {
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setContentText(AppVaribles.getMessage("ImageChanged"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("Save"));
        ButtonType buttonSaveAs = new ButtonType(AppVaribles.getMessage("SaveAs"));
        ButtonType buttonNotSave = new ButtonType(AppVaribles.getMessage("NotSave"));
        ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
        alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonNotSave, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonSave) {
            saveAction();
            return true;
        } else if (result.get() == buttonNotSave) {
            return true;
        } else if (result.get() == buttonSaveAs) {
            saveAsAction();
            return true;
        } else {
            return false;
        }

    }

    public String getInitTab() {
        return initTab;
    }

    public void setInitTab(String initTab) {
        this.initTab = initTab;
    }

    public ImageManufactureValues getValues() {
        return values;
    }

    public void setValues(final ImageManufactureValues values) {
        this.values = values;
    }

}
