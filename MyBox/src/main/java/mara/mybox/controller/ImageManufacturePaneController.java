package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.IntPoint;
import mara.mybox.data.VisitHistory;
import mara.mybox.db.TableImageScope;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import static mara.mybox.fxml.FxmlControl.blueText;
import static mara.mybox.fxml.FxmlControl.darkRedText;
import static mara.mybox.fxml.FxmlControl.redText;
import mara.mybox.fxml.ListColorCell;
import mara.mybox.fxml.ListImageCell;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.image.ImageColor;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.ImageScope;
import mara.mybox.image.ImageScope.ColorScopeType;
import mara.mybox.image.ImageScope.ScopeType;
import mara.mybox.image.PixelsOperation;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-8-15
 * @License Apache License Version 2.0
 */
public class ImageManufacturePaneController extends ImageMaskController {

    protected ImageManufactureController parent;
    protected ImageScope scope;
    protected String tips;
    protected float opacity;
    protected WebView webView;
    protected BufferedImage outlineSource;
    protected SimpleBooleanProperty operating;

    @FXML
    protected CheckBox scopeSetCheck, scopeManageCheck, areaExcludedCheck, colorExcludedCheck,
            scopeOutlineKeepRatioCheck, eightNeighborCheck;
    @FXML
    protected VBox scopeEditBox, scopeManageBox, scopePane;
    @FXML
    protected ComboBox<ImageScope> scopeSelector;
    @FXML
    protected ComboBox<String> scopePointsList, scopeMatchList, scopeDistanceList, opacitySelector;
    @FXML
    private ComboBox<Image> pixBox;
    @FXML
    protected TextField nameInput;
    @FXML
    protected HBox pointsSetBox, opacityBox, scopeCommonBox;
    @FXML
    protected Button scopeSaveButton, scopeDeleteButton,
            scopeUseButton, scopeDeletePointButton, scopeClearPointsButton, scopeDeleteColorButton,
            scopeClearColorsButton, paletteButton,
            scopeOutlineFileButton, scopeOutlineShrinkButton, scopeOutlineExpandButton;
    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton scopeMattingRadio, scopeRectangleRadio, scopeCircleRadio, scopeEllipseRadio, scopePolygonRadio,
            scopeColorRadio, scopeRectangleColorRadio, scopeCircleColorRadio, scopeEllipseColorRadio, scopePolygonColorRadio,
            scopeOutlineRadio;
    @FXML
    protected FlowPane scopeTypeBox, scopeOutlineBox, scopeColorBox, scopeMatchBox, scopeValuesBox;
    @FXML
    protected ComboBox<Color> scopeColorsList;
    @FXML
    protected Label scopePointsLabel, scopeColorsLabel, scopeMatchLabel, pointsSizeLabel, colorsSizeLabel;

    public ImageManufacturePaneController() {
    }

    @Override
    public void initControls() {
        try {
            operating = new SimpleBooleanProperty(false);
            imageView.toBack();

            maskView.visibleProperty().bind(operating
                    .or(scopeSetCheck.selectedProperty().and(typeGroup.selectedToggleProperty().isNotNull()))
            );
            opacityBox.visibleProperty().bind(scopeSetCheck.selectedProperty());

            scopeSetCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    checkViewScope();
                }
            });
            scopeManageCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    checkManageScope();
                }
            });

            isPickingColor.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    checkPickingColor();
                }
            });

            opacitySelector.getItems().addAll(
                    Arrays.asList(message("ScopeTransparency0.5"), message("ScopeTransparency0"), message("ScopeTransparency1"),
                            message("ScopeTransparency0.2"), message("ScopeTransparency0.8"), message("ScopeTransparency0.3"),
                            message("ScopeTransparency0.6"), message("ScopeTransparency0.7"), message("ScopeTransparency0.9"),
                            message("ScopeTransparency0.4"))
            );
            opacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
                    try {
                        if (newVal == null) {
                            return;
                        }
                        float f = Float.valueOf(newVal.substring(0, 3));
                        if (f >= 0 && f <= 1.0) {
                            opacity = 1 - f;
                            maskView.setOpacity(opacity);
                            FxmlControl.setEditorNormal(opacitySelector);
                            AppVariables.setUserConfigValue("ScopeTransparency", newVal);
                        } else {
                            FxmlControl.setEditorBadStyle(opacitySelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(opacitySelector);
                    }
                }
            });
            opacitySelector.getSelectionModel().select(AppVariables.getUserConfigValue("ScopeTransparency", message("ScopeTransparency0.5")));

            initScopesBox();
            initScopeBox();
            initAreaBox();
            initColorBox();
            initMatchBox();

            checkViewScope();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void init(File sourceFile, Image image, String title) {
        try {
            clearOperating();
            super.init(sourceFile, image);
            scopeSetCheck.setSelected(false);
            baseTitle = title;
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void updateImage(Image newImage) {
        try {
            super.init(sourceFile, newImage);
            clearOperating();

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void viewSizeChanged(double change) {
        super.viewSizeChanged(change);
        if (isSettingValues || scope == null || scope.getScopeType() == null
                || !maskView.isVisible() || change < sizeChangeAware) {
            return;
        }
        // Following handlers can conflict with threads' status changes which must check variables carefully
        switch (scope.getScopeType()) {
            case Operate:
                maskView.setFitWidth(imageView.getFitWidth());
                maskView.setFitHeight(imageView.getFitHeight());
                maskView.setLayoutX(imageView.getLayoutX());
                maskView.setLayoutY(imageView.getLayoutY());
                break;
            case Outline:
                makeOutline();
                break;
            default:
                indicateScope();
                break;
        }

    }

    protected void checkViewScope() {
        if (scopeSetCheck.isSelected()) {
            if (!thisPane.getChildren().contains(scopePane)) {
                thisPane.getChildren().add(0, scopePane);
            }
            if (!scopePane.getChildren().contains(scopeEditBox)) {
                scopePane.getChildren().add(scopeEditBox);
            }
            scopeManageCheck.setVisible(sourceFile != null);
            checkManageScope();
            FxmlControl.refreshStyle(scopePane);
            scopeRectangleRadio.fire();
        } else {
            if (thisPane.getChildren().contains(scopePane)) {
                thisPane.getChildren().remove(scopePane);
            }
            scopePane.getChildren().removeAll(scopeManageBox, scopeEditBox);
            scopeManageCheck.setVisible(false);
            clearScopePane();
            typeGroup.selectToggle(null);
            if (paletteController != null) {
                paletteController.closeStage();
                paletteController = null;
            }
        }
    }

    protected void checkManageScope() {
        if (scopeManageCheck.isVisible() && scopeManageCheck.isSelected()) {
            if (!scopePane.getChildren().contains(scopeManageBox)) {
                scopePane.getChildren().add(0, scopeManageBox);
                FxmlControl.refreshStyle(scopeManageBox);
                loadScopes();
                initScopeName();
            }
        } else {
            if (scopePane.getChildren().contains(scopeManageBox)) {
                scopePane.getChildren().remove(scopeManageBox);
            }
        }
    }

    protected void checkScopeType(boolean initValues) {
        try {
            if (isSettingValues) {
                return;
            }
            clearScopePane();

            if (typeGroup.getSelectedToggle() == null) {
                if (scope != null) {
                    scope.setScopeType(ImageScope.ScopeType.All);
                }

            } else {
                RadioButton selected = (RadioButton) typeGroup.getSelectedToggle();
                if (selected.equals(scopeMattingRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.Matting);

                } else if (selected.equals(scopeRectangleRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.Rectangle);

                } else if (selected.equals(scopeCircleRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.Circle);

                } else if (selected.equals(scopeEllipseRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.Ellipse);

                } else if (selected.equals(scopePolygonRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.Polygon);

                } else if (selected.equals(scopeColorRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.Color);

                } else if (selected.equals(scopeRectangleColorRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.RectangleColor);

                } else if (selected.equals(scopeCircleColorRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.CircleColor);

                } else if (selected.equals(scopeEllipseColorRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.EllipseColor);

                } else if (selected.equals(scopePolygonColorRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.PolygonColor);

                } else if (selected.equals(scopeOutlineRadio)) {
                    scope.setScopeType(ImageScope.ScopeType.Outline);
                }

            }
            initScopePane();
            if (initValues) {
                initScopeValues();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkMatchType() {
        try {
            if (scope == null) {
                return;
            }
            String matchType = scopeMatchList.getSelectionModel().getSelectedItem();
            int max = 255;
            if (message("Color").equals(matchType) || "Color".equals(matchType)) {
                scope.setColorScopeType(ColorScopeType.Color);

            } else if (message("Hue").equals(matchType) || "Hue".equals(matchType)) {
                scope.setColorScopeType(ColorScopeType.Hue);
                max = 360;

            } else if (message("Red").equals(matchType) || "Red".equals(matchType)) {
                scope.setColorScopeType(ColorScopeType.Red);

            } else if (message("Green").equals(matchType) || "Green".equals(matchType)) {
                scope.setColorScopeType(ColorScopeType.Green);

            } else if (message("Blue").equals(matchType) || "Blue".equals(matchType)) {
                scope.setColorScopeType(ColorScopeType.Blue);

            } else if (message("Brightness").equals(matchType) || "Brightness".equals(matchType)) {
                scope.setColorScopeType(ColorScopeType.Brightness);
                max = 100;

            } else if (message("Saturation").equals(matchType) || "Saturation".equals(matchType)) {
                scope.setColorScopeType(ColorScopeType.Saturation);
                max = 100;
            }
            FxmlControl.setTooltip(scopeDistanceList, new Tooltip("0~" + max));

            List<String> vList = new ArrayList<>();
            for (int i = 0; i <= max; i += 10) {
                vList.add(i + "");
            }
            isSettingValues = true;
            scopeDistanceList.getItems().clear();
            scopeDistanceList.getItems().addAll(vList);
            scopeDistanceList.getSelectionModel().select("20");
            isSettingValues = false;

            if (checkDistanceValue()) {
                indicateScope();
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected boolean checkDistanceValue() {
        if (scope.getColorScopeType() == null
                || scopeDistanceList.getSelectionModel().getSelectedItem() == null) {
            return false;
        }
        boolean valid = true;
        try {
            int distance = Integer.valueOf(scopeDistanceList.getSelectionModel().getSelectedItem());
            switch (scope.getColorScopeType()) {
                case Hue:
                    if (distance >= 0 && distance <= 360) {
                        FxmlControl.setEditorNormal(scopeDistanceList);
                        scope.setHsbDistance(distance / 360.0f);
                    } else {
                        FxmlControl.setEditorBadStyle(scopeDistanceList);
                        valid = false;
                    }
                    break;
                case Brightness:
                case Saturation:
                    if (distance >= 0 && distance <= 100) {
                        FxmlControl.setEditorNormal(scopeDistanceList);
                        scope.setHsbDistance(distance / 100.0f);
                    } else {
                        FxmlControl.setEditorBadStyle(scopeDistanceList);
                        valid = false;
                    }
                    break;
                default:
                    if (distance >= 0 && distance <= 255) {
                        FxmlControl.setEditorNormal(scopeDistanceList);
                        scope.setColorDistance(distance);
                    } else {
                        FxmlControl.setEditorBadStyle(scopeDistanceList);
                        valid = false;
                    }
            }
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(scopeDistanceList);
            logger.debug(e.toString());
            valid = false;
        }
        return valid;
    }

    public void initScopeBox() {
        try {
            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    checkScopeType(true);
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void initAreaBox() {
        try {
            scopePointsList.getItems().addListener(new ListChangeListener<String>() {
                @Override
                public void onChanged(Change<? extends String> c) {
                    int size = scopePointsList.getItems().size();
                    pointsSizeLabel.setText(message("Count") + ": " + size);
                    if (size > 100) {
                        pointsSizeLabel.setStyle(redText);
                    } else {
                        pointsSizeLabel.setStyle(blueText);
                    }
                }
            });

            areaExcludedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || scope == null) {
                        return;
                    }
                    scope.setAreaExcluded(newValue);
                    if (scope.getScopeType() == ScopeType.Outline) {

                        makeOutline();
                    } else {
                        indicateScope();
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initColorBox() {
        try {

            scopeColorsList.setButtonCell(new ListColorCell());
            scopeColorsList.setCellFactory(new Callback<ListView<Color>, ListCell<Color>>() {
                @Override
                public ListCell<Color> call(ListView<Color> p) {
                    return new ListColorCell();
                }
            });

            scopeColorsList.getItems().addListener(new ListChangeListener<Color>() {
                @Override
                public void onChanged(Change<? extends Color> c) {
                    int size = scopeColorsList.getItems().size();
                    colorsSizeLabel.setText(message("Count") + ": " + size);
                    if (size > 100) {
                        colorsSizeLabel.setStyle(redText);
                    } else {
                        colorsSizeLabel.setStyle(blueText);
                    }
                }
            });

            colorExcludedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || scope == null) {
                        return;
                    }
                    scope.setColorExcluded(newValue);
                    indicateScope();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initMatchBox() {
        try {

            scopeMatchList.getItems().addAll(Arrays.asList(message("Color"), message("Hue"),
                    message("Red"), message("Green"), message("Blue"), message("Brightness"), message("Saturation")
            ));
            scopeMatchList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    AppVariables.setUserConfigValue("ImageScopeMatchType", newValue);
                    checkMatchType();
                }
            });
            scopeMatchList.getSelectionModel().select(AppVariables.getUserConfigValue("ImageScopeMatchType", message("Color")));

            scopeDistanceList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    AppVariables.setUserConfigValue("ImageScopeMatchDistance", newValue);
                    if (checkDistanceValue()) {
                        indicateScope();
                    }
                }
            });

            eightNeighborCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || scope == null) {
                        return;
                    }
                    scope.setEightNeighbor(eightNeighborCheck.isSelected());
                    indicateScope();
                }
            });
            FxmlControl.setTooltip(eightNeighborCheck, new Tooltip(message("EightNeighborCheckComments")));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void initOutlineBox() {
        try {
            List<Image> prePixList = Arrays.asList(
                    new Image("img/About.png"), new Image("img/buttefly1.png"), new Image("img/MyBox.png"),
                    new Image("img/RecentAccess.png"), new Image("img/FileTools.png"), new Image("img/ImageTools.png"),
                    new Image("img/PdfTools.png"), new Image("img/MediaTools.png"), new Image("img/NetworkTools.png"),
                    new Image("img/bee1.png"), new Image("img/flower1.png"), new Image("img/flower2.png"),
                    new Image("img/flower3.png"), new Image("img/insect1.png"), new Image("img/insect2.png"),
                    new Image("img/p1.png"), new Image("img/p2.png"), new Image("img/p3.png")
            );
            pixBox.getItems().addAll(prePixList);
            pixBox.setButtonCell(new ListImageCell());
            pixBox.setCellFactory(new Callback<ListView<Image>, ListCell<Image>>() {
                @Override
                public ListCell<Image> call(ListView<Image> param) {
                    return new ListImageCell();
                }
            });
            pixBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Image>() {
                @Override
                public void changed(ObservableValue ov, Image oldValue, Image newValue) {
                    loadOutlineSource(newValue);
                }
            });

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void clearScopePane() {
        try {
            initMaskControls(false);
            isSettingValues = true;
            if (image != null) {
                scope = new ImageScope(image);
                if (sourceFile != null) {
                    scope.setFile(sourceFile.getAbsolutePath());
                }
                imageView.setImage(image);
            } else {
                scope = new ImageScope();
            }
            maskView.setImage(null);
            outlineSource = null;

            scopePointsList.getItems().clear();
            scopeColorsList.getItems().clear();
            scopeDistanceList.getItems().clear();
            areaExcludedCheck.setSelected(false);
            colorExcludedCheck.setSelected(false);
            scopeDistanceList.getEditor().setStyle(null);
            pixBox.getSelectionModel().select(null);
            scopeEditBox.getChildren().clear();

            tips = "";
            imageLabel.setText("");
            imageLabel.setStyle(FxmlControl.blueText);
            isSettingValues = false;

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initScopePane() {
        try {
            if (image == null) {
                return;
            }
            isSettingValues = true;
            initScopeName();
            scopeEditBox.getChildren().clear();
            scopeEditBox.getChildren().addAll(scopeTypeBox, scopeValuesBox);
            scopeValuesBox.getChildren().clear();
            switch (scope.getScopeType()) {
                case All:
                    if (paletteController != null) {
                        paletteController.closeStage();
                        paletteController = null;
                    }
                    break;
                case Matting:
                    scopeValuesBox.getChildren().addAll(pointsSetBox, eightNeighborCheck, scopeMatchBox);
                    if (paletteController != null) {
                        paletteController.closeStage();
                        paletteController = null;
                    }
                    if (opacity == 0) {
                        opacitySelector.getSelectionModel().select(0);
                    }
                    tips = message("ClickImagesSetPoints");
                    break;

                case Rectangle:
                    scopeValuesBox.getChildren().add(areaExcludedCheck);
                    tips = message("SetRectangle");
                    if (paletteController != null) {
                        paletteController.closeStage();
                        paletteController = null;
                    }
                    break;

                case Circle:
                    scopeValuesBox.getChildren().add(areaExcludedCheck);
                    tips = message("SetCircle");
                    if (paletteController != null) {
                        paletteController.closeStage();
                        paletteController = null;
                    }
                    break;

                case Ellipse:
                    scopeValuesBox.getChildren().add(areaExcludedCheck);
                    tips = message("SetEllipse");
                    if (paletteController != null) {
                        paletteController.closeStage();
                        paletteController = null;
                    }
                    break;

                case Polygon:
                    scopeValuesBox.getChildren().addAll(pointsSetBox, areaExcludedCheck);
                    tips = message("SetPolygon");
                    if (paletteController != null) {
                        paletteController.closeStage();
                        paletteController = null;
                    }
                    break;

                case Color:
                    scopeValuesBox.getChildren().addAll(scopeColorBox, scopeMatchBox);
                    if (opacity == 0) {
                        opacitySelector.getSelectionModel().select(0);
                    }
                    tips = message("SetColors");
                    break;

                case RectangleColor:
                    scopeValuesBox.getChildren().addAll(areaExcludedCheck, scopeColorBox, scopeMatchBox);
                    showPalette(null);
                    if (opacity == 0) {
                        opacitySelector.getSelectionModel().select(0);
                    }
                    tips = message("SetRectangleColors");
                    break;

                case CircleColor:
                    scopeValuesBox.getChildren().addAll(areaExcludedCheck, scopeColorBox, scopeMatchBox);
                    showPalette(null);
                    if (opacity == 0) {
                        opacitySelector.getSelectionModel().select(0);
                    }
                    tips = message("SetCircleColors");
                    break;

                case EllipseColor:
                    scopeValuesBox.getChildren().addAll(areaExcludedCheck, scopeColorBox, scopeMatchBox);
                    showPalette(null);
                    tips = message("SetEllipseColors");
                    break;

                case PolygonColor:
                    scopeValuesBox.getChildren().addAll(pointsSetBox, areaExcludedCheck, scopeColorBox, scopeMatchBox);
                    showPalette(null);
                    if (opacity == 0) {
                        opacitySelector.getSelectionModel().select(0);
                    }
                    tips = message("SetPolygonColors");
                    break;

                case Outline:
                    scopeValuesBox.getChildren().addAll(scopeOutlineBox, areaExcludedCheck);
                    if (pixBox.getItems().isEmpty()) {
                        initOutlineBox();
                    }
                    if (opacity == 0) {
                        opacitySelector.getSelectionModel().select(0);
                    }
                    if (paletteController != null) {
                        paletteController.closeStage();
                        paletteController = null;
                    }
                    tips = message("ScopeOutlineTips");
                    break;

                default:
                    tips = "";
            }

            checkPickingColor();
            FxmlControl.refreshStyle(scopeEditBox);
            isSettingValues = false;

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void initScopeValues() {
        try {
            if (image == null || scope == null) {
                return;
            }
            switch (scope.getScopeType()) {
                case Matting:
                    checkMatchType();
                    break;

                case Rectangle:
                    initMaskRectangleLine(true);
                    indicateScope();
                    break;

                case Circle:
                    initMaskCircleLine(true);
                    indicateScope();
                    break;

                case Ellipse:
                    initMaskEllipseLine(true);
                    indicateScope();
                    break;

                case Polygon:
                    initMaskPolygonLine(true);
                    indicateScope();
                    break;

                case Color:
                    checkMatchType();
                    tips = message("SetColors");
                    break;

                case RectangleColor:
                    initMaskRectangleLine(true);
                    checkMatchType();
                    break;

                case CircleColor:
                    initMaskCircleLine(true);
                    checkMatchType();
                    break;

                case EllipseColor:
                    initMaskEllipseLine(true);
                    checkMatchType();
                    break;

                case PolygonColor:
                    initMaskPolygonLine(true);
                    checkMatchType();
                    break;

                case Outline:
                    pixBox.getSelectionModel().select(0);
                    break;
                default:
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void indicateScope() {
        if (isSettingValues || image == null
                || scope == null || scope.getScopeType() == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image scopedImage;

                @Override
                protected boolean handle() {
                    try {
                        PixelsOperation pixelsOperation = PixelsOperation.create(image,
                                scope, PixelsOperation.OperationType.ShowScope);
                        scopedImage = pixelsOperation.operateFxImage();
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        return scopedImage != null;
                    } catch (Exception e) {
                        logger.error(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (scope == null) {
                        return;
                    }
                    maskView.setImage(scopedImage);
                    maskView.setFitWidth(imageView.getFitWidth());
                    maskView.setFitHeight(imageView.getFitHeight());
                    maskView.setLayoutX(imageView.getLayoutX());
                    maskView.setLayoutY(imageView.getLayoutY());
                }

            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /*
        Manage inputs
     */
    public void checkPickingColor() {
        if (isPickingColor.get()) {
            imageLabel.setStyle(darkRedText);
            imageLabel.setText(message("PickingColorsNow"));
            maskView.setOpacity(opacity - 0.15);
        } else {
            imageLabel.setStyle(blueText);
            imageLabel.setText(tips);
            maskView.setOpacity(opacity);
        }
    }

    @FXML
    @Override
    public void showPalette(ActionEvent event) {
        showPalette(paletteButton, message("ImageManufacture"), true);

    }

    @FXML
    public void deletePoint() {
        if (isSettingValues) {
            return;
        }
        int index = scopePointsList.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            isSettingValues = true;
            scope.getPoints().remove(index);
            scopePointsList.getItems().remove(index);
            int size = scopePointsList.getItems().size();
            if (size > 0) {
                if (index > size - 1) {
                    scopePointsList.getSelectionModel().select(index - 1);
                } else {
                    scopePointsList.getSelectionModel().select(index);
                }
            }
            isSettingValues = false;
            indicateScope();
            if (scope.getScopeType() == ScopeType.Polygon
                    || scope.getScopeType() == ScopeType.PolygonColor) {
                maskPolygonData.remove(index);
                drawMaskPolygonLine();
            }
        }
    }

    @FXML
    public void clearPoints() {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        scope.clearPoints();
        scopePointsList.getItems().clear();
        isSettingValues = false;
        indicateScope();
        if (scope.getScopeType() == ScopeType.Polygon
                || scope.getScopeType() == ScopeType.PolygonColor) {
            maskPolygonData.clear();
            drawMaskPolygonLine();
        }

    }

    @FXML
    public void deleteColor() {
        if (isSettingValues) {
            return;
        }
        int index = scopeColorsList.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            isSettingValues = true;
            scope.getColors().remove(index);
            scopeColorsList.getItems().remove(index);
            int size = scopeColorsList.getItems().size();
            if (size > 0) {
                if (index > size - 1) {
                    scopeColorsList.getSelectionModel().select(index - 1);
                } else {
                    scopeColorsList.getSelectionModel().select(index);
                }
            }
            isSettingValues = false;
            indicateScope();
        }
    }

    @FXML
    public void clearColors() {
        if (isSettingValues) {
            return;
        }
        isSettingValues = true;
        scope.getColors().clear();
        scopeColorsList.getItems().clear();
        isSettingValues = false;
        indicateScope();

    }

    public void colorPicked(Color color) {
        if (isSettingValues || color == null) {
            return;
        }
        setColor(color);
        if (paletteController != null && paletteController.getParentController().equals(this)) {
            paletteController.setColor(color);
        }
        if (parent.operationController != null && parent.operationController.paletteController != null) {
            parent.operationController.paletteController.setColor(color);
        }
    }

    public boolean setColor(Color color) {
        if (isSettingValues || color == null
                || scope == null || scope.getScopeType() == null
                || scopeColorsList.getItems().contains(color)) {
            return false;
        }
        switch (scope.getScopeType()) {
            case Color:
            case RectangleColor:
            case CircleColor:
            case EllipseColor:
            case PolygonColor:
                scope.addColor(ImageColor.converColor(color));
                scopeColorsList.getItems().add(color);
                scopeColorsList.getSelectionModel().selectLast();
                indicateScope();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean setColor(Control control, Color color) {
        return setColor(color);
    }

    @FXML
    @Override
    public void paneClicked(MouseEvent event) {
        if (isPickingColor.get()) {
            IntPoint p = getImageXYint(event, imageView);
            if (p == null) {
                return;
            }
            PixelReader pixelReader = imageView.getImage().getPixelReader();
            Color color = pixelReader.getColor(p.getX(), p.getY());
            colorPicked(color);

        } else if (operating.get()) {
            super.paneClicked(event);
            parent.operationController.paneClicked(event);

        } else if (scope != null && scope.getScopeType() != null) {
            paneClickedForScope(event);

        }

    }

    @FXML
    public void paneClickedForScope(MouseEvent event) {
        switch (scope.getScopeType()) {
            case Color: {
                IntPoint p = getImageXYint(event, imageView);
                if (p == null) {
                    return;
                }
                PixelReader pixelReader = imageView.getImage().getPixelReader();
                Color color = pixelReader.getColor(p.getX(), p.getY());
                colorPicked(color);
            }
            break;
            case Matting: {
                IntPoint p = getImageXYint(event, imageView);
                if (p == null) {
                    return;
                }
                scope.addPoint(p.getX(), p.getY());
                scopePointsList.getItems().add(p.getX() + "," + p.getY());
                scopePointsList.getSelectionModel().selectLast();
                indicateScope();
            }
            break;
            default:
                super.paneClicked(event);
                switch (scope.getScopeType()) {
                    case Rectangle:
                    case RectangleColor:
                        if (!scope.getRectangle().same(maskRectangleData)) {
                            scope.setRectangle(maskRectangleData.cloneValues());
                            indicateScope();
                        }
                        break;
                    case Circle:
                    case CircleColor:
                        if (!scope.getCircle().same(maskCircleData)) {
                            scope.setCircle(maskCircleData.cloneValues());
                            indicateScope();
                        }
                        break;
                    case Ellipse:
                    case EllipseColor:
                        if (!scope.getEllipse().same(maskEllipseData)) {
                            scope.setEllipse(maskEllipseData.cloneValues());
                            indicateScope();
                        }
                        break;
                    case Polygon:
                    case PolygonColor:
                        if (!scope.getPolygon().same(maskPolygonData)) {
                            scope.setPolygon(maskPolygonData.cloneValues());
                            scopePointsList.getItems().clear();
                            for (DoublePoint p : maskPolygonData.getPoints()) {
                                scopePointsList.getItems().add((int) p.getX() + "," + (int) p.getY());
                            }
                            scopePointsList.getSelectionModel().selectLast();
                            indicateScope();
                        }
                        break;
                    case Outline:
                        if (!scope.getRectangle().same(maskRectangleData)) {
                            scope.setRectangle(maskRectangleData.cloneValues());
                            makeOutline();
                        }
                        break;
                }

        }
    }

    @FXML
    public void mousePressed(MouseEvent event) {
        if (operating.get()) {
            parent.operationController.mousePressed(event);
        }
    }

    @FXML
    public void mouseDragged(MouseEvent event) {
        if (operating.get()) {
            super.paneClicked(event);
            parent.operationController.mouseDragged(event);
        }
    }

    @FXML
    public void mouseReleased(MouseEvent event) {
        if (operating.get()) {
            parent.operationController.mouseReleased(event);
        }
    }


    /*
        Manage scopes
     */
    public void initScopesBox() {
        try {

            scopeSelector.setButtonCell(new ImageScopeCell());
            scopeSelector.setCellFactory(new Callback<ListView<ImageScope>, ListCell<ImageScope>>() {
                @Override
                public ListCell<ImageScope> call(ListView<ImageScope> param) {
                    return new ImageScopeCell();
                }
            });
            scopeSelector.setVisibleRowCount(15);

            scopeDeleteButton.disableProperty().bind(
                    scopeSelector.getSelectionModel().selectedItemProperty().isNull()
            );
            scopeUseButton.disableProperty().bind(
                    scopeSelector.getSelectionModel().selectedItemProperty().isNull()
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public class ImageScopeCell extends ListCell<ImageScope> {

        private final ImageView view;

        public ImageScopeCell() {
            setContentDisplay(ContentDisplay.LEFT);
            view = new ImageView();
            view.setPreserveRatio(true);
            view.setFitWidth(20);
        }

        @Override
        protected void updateItem(ImageScope item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item.getScopeType() == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Image icon;
            try {
                switch (item.getScopeType()) {
                    case Rectangle:
                        icon = new Image(ControlStyle.getIcon("iconRectangle.png"));
                        break;
                    case Circle:
                        icon = new Image(ControlStyle.getIcon("iconCircle.png"));
                        break;
                    case Ellipse:
                        icon = new Image(ControlStyle.getIcon("iconEllipse.png"));
                        break;
                    case Polygon:
                        icon = new Image(ControlStyle.getIcon("iconStar.png"));
                        break;
                    case RectangleColor:
                        icon = new Image(ControlStyle.getIcon("iconRectangleFilled.png"));
                        break;
                    case CircleColor:
                        icon = new Image(ControlStyle.getIcon("iconCircleFilled.png"));
                        break;
                    case EllipseColor:
                        icon = new Image(ControlStyle.getIcon("iconEllipseFilled.png"));
                        break;
                    case PolygonColor:
                        icon = new Image(ControlStyle.getIcon("iconStarFilled.png"));
                        break;
                    case Color:
                        icon = new Image(ControlStyle.getIcon("iconColorWheel.png"));
                        break;
                    case Matting:
                        icon = new Image(ControlStyle.getIcon("iconColorFill.png"));
                        break;
                    case Outline:
                        icon = new Image(ControlStyle.getIcon("IconButterfly.png"));
                        break;
                    default:
                        return;
                }
                String s = item.getName();
                if (scope != null && s.equals(scope.getName())) {
                    setStyle("-fx-text-fill: #961c1c; -fx-font-weight: bolder;");
                    s = "** " + message("CurrentScope") + " " + s;
                } else {
                    setStyle("");
                }
                view.setImage(icon);
                setGraphic(view);
                setText(s);
            } catch (Exception e) {
                logger.error(e.toString());
                setText(null);
                setGraphic(null);
            }

        }
    }

    public void initNameBox() {
        try {

            scopeSaveButton.disableProperty().bind(
                    nameInput.textProperty().isEmpty()
                            .or(scopeDistanceList.visibleProperty().and(scopeDistanceList.getEditor().styleProperty().isEqualTo(badStyle)))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void loadScopes() {
        try {
            scopeSelector.getItems().clear();
            if (sourceFile == null) {
                return;
            }
            isSettingValues = true;
            List<ImageScope> list = TableImageScope.read(sourceFile.getAbsolutePath());
            if (list != null && !list.isEmpty()) {
                scopeSelector.getItems().setAll(list);
                scopeSelector.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            logger.debug(e.toString());
        }
        isSettingValues = false;
    }

    protected void initScopeName() {
        if (scope == null || !scopeManageCheck.isVisible() || !scopeManageCheck.isSelected()) {
            return;
        }
        String name = scope.getName();
        if (name == null || name.isEmpty()) {
            name = scope.getScopeType() + "_" + DateTools.datetimeToString(new Date());
        }
        nameInput.setText(name);
    }

    @FXML
    public void deleteScope() {
        ImageScope selected = scopeSelector.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return TableImageScope.removeScope(selected.getFile(), selected.getName());
                }

                @Override
                protected void whenSucceeded() {
                    loadScopes();
                }
            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void saveScope() {
        if (scope == null || scope.getFile() == null || scopeSaveButton.isDisabled()
                || !scopeManageCheck.isSelected()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            String name = nameInput.getText().trim();
            if (name.isEmpty()) {
                return;
            }
            scope.setName(name);
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    TableImageScope.write(scope);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    loadScopes();
                }
            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        }

    }

    @FXML
    public void clearScopes() {
        if (sourceFile == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    TableImageScope.clearScopes(sourceFile.getAbsolutePath());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    loadScopes();
                }

            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void useScope() {
        ImageScope selected = scopeSelector.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        scope = selected;
        // Force listView to refresh
        // https://stackoverflow.com/questions/13906139/javafx-update-of-listview-if-an-element-of-observablelist-changes?r=SearchResults
        for (int i = 0; i < scopeSelector.getItems().size(); i++) {
            scopeSelector.getItems().set(i, scopeSelector.getItems().get(i));
        }
        showScope(scope);
    }

    @FXML
    public void createScope() {
//        clearScopePane();
        typeGroup.selectToggle(null);
//        initScopePane();
    }

    public void showScope(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return;
        }
        clearScopePane();
        this.scope = scope;
        initScopePane();
        isSettingValues = true;
        showScopeType(scope);
        showAreaData(scope);
        showColorData(scope);
        showMatchType(scope);
        showDistanceValue(scope);
        eightNeighborCheck.setSelected(scope.isEightNeighbor());
        isSettingValues = false;
        if (scope.getScopeType() != ScopeType.Outline) {
            indicateScope();
        } else {
            loadOutlineSource(scope.getOutlineSource(), scope.getRectangle());
        }
    }

    public boolean showScopeType(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return false;
        }
        switch (scope.getScopeType()) {
            case All:
                typeGroup.selectToggle(null);
                break;
            case Matting:
                scopeMattingRadio.fire();
                break;
            case Color:
                scopeColorRadio.fire();
                break;
            case Rectangle:
                scopeRectangleRadio.fire();
                break;
            case RectangleColor:
                scopeRectangleColorRadio.fire();
                break;
            case Circle:
                scopeCircleRadio.fire();
                break;
            case CircleColor:
                scopeCircleColorRadio.fire();
                break;
            case Ellipse:
                scopeEllipseRadio.fire();
                break;
            case EllipseColor:
                scopeEllipseColorRadio.fire();
                break;
            case Polygon:
                scopePolygonRadio.fire();
                break;
            case PolygonColor:
                scopePolygonColorRadio.fire();
                break;
            case Outline:
                scopeOutlineRadio.fire();
                break;
        }
        return true;

    }

    public boolean showAreaData(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return false;
        }
        try {
            areaExcludedCheck.setSelected(scope.isAreaExcluded());
            switch (scope.getScopeType()) {
                case Matting: {
                    List<IntPoint> points = scope.getPoints();
                    if (points != null) {
                        for (IntPoint p : points) {
                            scopePointsList.getItems().add(p.getX() + "," + p.getY());
                        }
                        scopePointsList.getSelectionModel().selectLast();
                    }
                    return true;
                }
                case Rectangle:
                case RectangleColor:
                case Outline:
                    setMaskRectangleLineVisible(true);
                    maskRectangleData = scope.getRectangle();
                    return drawMaskRectangleLineAsData();
                case Circle:
                case CircleColor:
                    initMaskCircleLine(true);
                    maskCircleData = scope.getCircle();
                    return drawMaskCircleLineAsData();
                case Ellipse:
                case EllipseColor:
                    initMaskEllipseLine(true);
                    maskEllipseData = scope.getEllipse();
                    return drawMaskEllipseLineAsData();
                case Polygon:
                case PolygonColor: {
                    initMaskPolygonLine(true);
                    List<IntPoint> points = scope.getPoints();
                    if (points != null) {
                        for (IntPoint p : points) {
                            scopePointsList.getItems().add(p.getX() + "," + p.getY());
                        }
                        scopePointsList.getSelectionModel().selectLast();
                    }
                    maskPolygonData = scope.getPolygon();
                    return drawMaskPolygonLineAsData();
                }
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public boolean showColorData(ImageScope scope) {
        if (scope == null || scope.getScopeType() == null) {
            return false;
        }
        try {
            colorExcludedCheck.setSelected(scope.isColorExcluded());
            switch (scope.getScopeType()) {
                case Color:
                case RectangleColor:
                case CircleColor:
                case EllipseColor:
                case PolygonColor:
                    List<java.awt.Color> colors = scope.getColors();
                    if (colors != null) {
                        List<Color> list = new ArrayList<>();
                        for (java.awt.Color color : colors) {
                            list.add(ImageColor.converColor(color));
                        }
                        scopeColorsList.getItems().clear();
                        scopeColorsList.getItems().addAll(list);
                        scopeColorsList.getSelectionModel().selectLast();
                    }
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    protected void showMatchType(ImageScope scope) {
        try {
            if (scope == null) {
                return;
            }
            switch (scope.getColorScopeType()) {
                case Color:
                    scopeMatchList.getSelectionModel().select(message("Color"));
                    break;
                case Hue:
                    scopeMatchList.getSelectionModel().select(message("Hue"));
                    break;
                case Red:
                    scopeMatchList.getSelectionModel().select(message("Red"));
                    break;
                case Green:
                    scopeMatchList.getSelectionModel().select(message("Green"));
                    break;
                case Blue:
                    scopeMatchList.getSelectionModel().select(message("Blue"));
                    break;
                case Brightness:
                    scopeMatchList.getSelectionModel().select(message("Brightness"));
                    break;
                case Saturation:
                    scopeMatchList.getSelectionModel().select(message("Saturation"));
                    break;
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void showDistanceValue(ImageScope scope) {
        try {
            int distance, max = 255;
            switch (scope.getColorScopeType()) {
                case Hue:
                    max = 360;
                    distance = (int) (scope.getHsbDistance() * 360);
                    break;
                case Brightness:
                case Saturation:
                    max = 100;
                    distance = (int) (scope.getHsbDistance() * 100);
                    break;
                default:
                    distance = scope.getColorDistance();
            }
            FxmlControl.setTooltip(scopeDistanceList, new Tooltip("0~" + max));
            List<String> vList = new ArrayList<>();
            for (int i = 0; i <= max; i += 10) {
                vList.add(i + "");
            }
            scopeDistanceList.getItems().clear();
            scopeDistanceList.getItems().addAll(vList);
            scopeDistanceList.getSelectionModel().select(distance + "");
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    /*
        Outline
     */
    @FXML
    public void selectOutlineFile() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVariables.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(CommonFxValues.AlphaImageExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            recordFileOpened(file);
            loadOutlineSource(file);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void popOutlineFile(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                int fileNumber = AppVariables.fileRecentNumber * 2 / 3 + 1;
                return VisitHistory.getRecentAlphaImages(fileNumber);
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePathsBesidesFiles();
            }

            @Override
            public void handleSelect() {
                selectOutlineFile();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                loadOutlineSource(file);
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

        }.pop();
    }

    public void loadOutlineSource(File file) {
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private BufferedImage bufferedImage;

                @Override
                protected boolean handle() {
                    try {
                        bufferedImage = ImageFileReaders.readImage(file);
                        return bufferedImage != null;
                    } catch (Exception e) {
                        logger.error(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    loadOutlineSource(bufferedImage);
                }

            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    public void loadOutlineSource(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            maskView.setImage(null);
            return;
        }
        loadOutlineSource(bufferedImage, new DoubleRectangle(0, 0,
                bufferedImage.getWidth(), bufferedImage.getHeight()));
    }

    public void loadOutlineSource(BufferedImage bufferedImage, DoubleRectangle rect) {
        if (bufferedImage == null || rect == null) {
            maskView.setImage(null);
            return;
        }
        outlineSource = bufferedImage;
        maskRectangleData = rect.cloneValues();
        setMaskRectangleLineVisible(true);
        drawMaskRectangleLineAsData();

        makeOutline();
    }

    public void loadOutlineSource(Image image) {
        if (image == null) {
            maskView.setImage(null);
            return;
        }
        loadOutlineSource(SwingFXUtils.fromFXImage(image, null));
    }

    public void makeOutline() {
        try {
            if (isSettingValues || image == null
                    || scope == null || scope.getScopeType() != ScopeType.Outline
                    || outlineSource == null || maskRectangleData == null) {
                return;
            }
            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {
                    private BufferedImage[] outline;

                    @Override
                    protected boolean handle() {
                        try {
                            outline = ImageManufacture.outline(outlineSource,
                                    maskRectangleData, (int) getImageWidth(), (int) getImageHeight(),
                                    scopeOutlineKeepRatioCheck.isSelected(),
                                    ImageColor.converColor(Color.WHITE), areaExcludedCheck.isSelected());
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            return outline != null;
                        } catch (Exception e) {
                            logger.error(e.toString());
                            return false;
                        }
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (scope == null) {   // this may happen jn quitOpearting()
                            return;
                        }
                        maskRectangleData = new DoubleRectangle(
                                maskRectangleData.getSmallX(), maskRectangleData.getSmallY(),
                                maskRectangleData.getSmallX() + outline[0].getWidth(),
                                maskRectangleData.getSmallY() + outline[0].getHeight());
                        drawMaskRectangleLineAsData();
                        scope.setOutlineSource(outlineSource);
                        scope.setOutline(outline[1]);
                        scope.setRectangle(maskRectangleData.cloneValues());
                        displayOutline(outline[1]);
                    }

                };
                parent.openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void displayOutline(BufferedImage bufferedImage) {
        if (scope == null || bufferedImage == null || scope.getScopeType() != ScopeType.Outline) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image outlineImage;

                @Override
                protected boolean handle() {
                    try {
                        outlineImage = SwingFXUtils.toFXImage(bufferedImage, null);
                        return outlineImage != null;
                    } catch (Exception e) {
                        logger.error(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    maskView.setImage(outlineImage);
                    double radio = imageView.getBoundsInParent().getWidth() / getImageWidth();
                    double offsetX = maskRectangleData.getSmallX() >= 0 ? 0 : maskRectangleData.getSmallX();
                    double offsetY = maskRectangleData.getSmallY() >= 0 ? 0 : maskRectangleData.getSmallY();
                    maskView.setLayoutX(imageView.getLayoutX() + offsetX * radio);
                    maskView.setLayoutY(imageView.getLayoutY() + offsetY * radio);
                    maskView.setFitWidth(outlineImage.getWidth() * radio);
                    maskView.setFitHeight(outlineImage.getHeight() * radio);
                }
            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    /*
       Operate
     */
    public void operatingNeedNotScope() {
        if (scopeSetCheck.isSelected()) {
            scopeSetCheck.setSelected(false);
            scopePane.setDisable(true);
        }
        maskView.setOpacity(1.0f);
        scopeCommonBox.setDisable(true);
        operating.set(true);
        scope = new ImageScope();
        scope.setScopeType(ImageScope.ScopeType.Operate);
    }

    public void clearOperating() {
        if (scopeSetCheck.isSelected()) {
            typeGroup.selectToggle(null);
            scopePane.setDisable(false);
        }
        imageView.setRotate(0);
        scopeCommonBox.setDisable(false);
        imageLabel.setText("");
        operating.set(false);
        scope = null;
        isPickingColor.unbind();
        isPickingColor.set(false);

        maskView.setImage(null);
        maskView.setOpacity(opacity);

        setMaskRectangleLineVisible(false);
        maskRectangleLine.setArcWidth(0);
        maskRectangleLine.setArcHeight(0);
        maskRectangleLine.setFill(Color.TRANSPARENT);
        maskRectangleLine.setOpacity(1);

        if (webView != null) {
            maskPane.getChildren().remove(webView);
            webView = null;
        }

    }

}
