package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleEllipse;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.IntPoint;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.db.table.TableColorData;
import mara.mybox.db.table.TableImageScope;
import mara.mybox.dev.MyBoxLog;
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
import static mara.mybox.tools.DoubleTools.scale;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2020-9-15
 * @License Apache License Version 2.0
 */
public class ImageManufactureScopeController extends ImageViewerController {

    protected ImageManufactureController imageController;
    protected float opacity;
    protected BufferedImage outlineSource;

    @FXML
    protected ImageView scopeView;
    @FXML
    protected ToggleGroup scopeTypeGroup, matchGroup;
    @FXML
    protected VBox scopeEditBox, scopeSetBox, scopePointsBox, scopeColorsBox, colorMatchBox,
            scopeOutlineBox, rectangleBox, circleBox;
    @FXML
    protected ListView<ImageScope> scopesList;
    @FXML
    protected ComboBox<String> scopeDistanceSelector, opacitySelector;
    @FXML
    protected ListView<Image> outlinesList;
    @FXML
    protected ColorSetController colorSetController;
    @FXML
    protected ListView<Color> colorsList;
    @FXML
    protected ListView<String> pointsList;
    @FXML
    protected CheckBox areaExcludedCheck, colorExcludedCheck, scopeOutlineKeepRatioCheck, eightNeighborCheck,
            ignoreTransparentCheck;
    @FXML
    protected TextField scopeNameInput, rectLeftTopXInput, rectLeftTopYInput, rightBottomXInput, rightBottomYInput,
            circleCenterXInput, circleCenterYInput, circleRadiusInput;
    @FXML
    protected Button saveScopeButton, deleteScopesButton, useScopeButton,
            deletePointsButton, clearPointsButton,
            scopeOutlineFileButton, scopeOutlineShrinkButton, scopeOutlineExpandButton,
            clearColorsButton, deleteColorsButton, saveColorsButton;
    @FXML
    protected RadioButton scopeAllRadio, scopeMattingRadio, scopeRectangleRadio, scopeCircleRadio,
            scopePolygonRadio, scopeColorRadio, scopeRectangleColorRadio, scopeCircleColorRadio,
            scopeEllipseColorRadio, scopePolygonColorRadio, scopeOutlineRadio, scopeEllipseRadio,
            colorRGBRadio, colorGreenRadio, colorRedRadio, colorBlueRadio,
            colorSaturationRadio, colorHueRadio, colorBrightnessRadio;
    @FXML
    protected Label scopeTips, scopePointsLabel, scopeColorsLabel, pointsSizeLabel, colorsSizeLabel,
            rectangleLabel;

    public ImageManufactureScopeController() {
        baseTitle = AppVariables.message("ImageManufacture");
        needNotContextMenu = true;
    }

    /*
        init
     */
    public void initController(ImageManufactureController parent) {
        this.parentController = parent;
        imageController = parent;
        baseName = imageController.baseName;
        baseTitle = imageController.baseTitle;
        sourceFile = imageController.sourceFile;
        imageInformation = imageController.imageInformation;
        image = imageController.image;

        initScopeView();
        initAreaBox();
        initColorBox();
        initMatchBox();
        initOpacitySelector();
        initSavedScopesBox();
        refreshStyle();

        loadImage(sourceFile, imageInformation, imageController.image);
        checkScopeType();
        scopeAllRadio.fire();
        loadScopes();
    }

    protected void initSplitPane() {
        try {
            String mv = AppVariables.getUserConfigValue(baseName + "ScopePanePosition", "0.5");
            splitPane.setDividerPositions(Double.parseDouble(mv));

            splitPane.getDividers().get(0).positionProperty().addListener(
                    (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                        AppVariables.setUserConfigValue(baseName + "ScopePanePosition", newValue.doubleValue() + "");
                    });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initScopeView() {
        try {
            scopeView.visibleProperty().bind(scopeEditBox.visibleProperty());
            imageView.toBack();

            scopeTypeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkScopeType();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initRectangleBox() {
        try {

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initAreaBox() {
        try {
            pointsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            pointsList.getItems().addListener(new ListChangeListener<String>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends String> c) {
                    int size = pointsList.getItems().size();
                    pointsSizeLabel.setText(message("Count") + ": " + size);
                    if (size > 100) {
                        pointsSizeLabel.setStyle(redText);
                    } else {
                        pointsSizeLabel.setStyle(blueText);
                    }
                    clearPointsButton.setDisable(size == 0);
                }
            });
            clearPointsButton.setDisable(true);
            deletePointsButton.disableProperty().bind(pointsList.getSelectionModel().selectedItemProperty().isNull());

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
            MyBoxLog.error(e.toString());
        }
    }

    public void initColorBox() {
        try {
            colorsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            colorsList.setCellFactory(new Callback<ListView<Color>, ListCell<Color>>() {
                @Override
                public ListCell<Color> call(ListView<Color> p) {
                    return new ListColorCell();
                }
            });
            colorsList.getItems().addListener(new ListChangeListener<Color>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends Color> c) {
                    int size = colorsList.getItems().size();
                    colorsSizeLabel.setText(message("Count") + ": " + size);
                    if (size > 100) {
                        colorsSizeLabel.setStyle(redText);
                    } else {
                        colorsSizeLabel.setStyle(blueText);
                    }
                    clearColorsButton.setDisable(size == 0);
                }
            });

            clearColorsButton.setDisable(true);
            deleteColorsButton.disableProperty().bind(colorsList.getSelectionModel().selectedItemProperty().isNull());
            saveColorsButton.disableProperty().bind(colorsList.getSelectionModel().selectedItemProperty().isNull());

            colorSetController.init(this, baseName + "Color", Color.THISTLE);
            colorSetController.hideRect();
            colorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    addColor((Color) newValue);
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

            ignoreTransparentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues || scope == null) {
                        return;
                    }
                    indicateScope();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void initMatchBox() {
        try {
            matchGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkMatchType();
                }
            });

            scopeDistanceSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
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
            MyBoxLog.error(e.toString());
        }
    }

    public void initOutlineBox() {
        try {
            List<Image> prePixList = Arrays.asList(
                    new Image("img/ww1.png"), new Image("img/jade.png"),
                    new Image("img/ww3.png"), new Image("img/ww4.png"), new Image("img/ww6.png"),
                    new Image("img/ww7.png"), new Image("img/ww8.png"), new Image("img/ww9.png"),
                    new Image("img/About.png"), new Image("img/MyBox.png"), new Image("img/DataTools.png"),
                    new Image("img/RecentAccess.png"), new Image("img/FileTools.png"), new Image("img/ImageTools.png"),
                    new Image("img/PdfTools.png"), new Image("img/MediaTools.png"), new Image("img/NetworkTools.png"),
                    new Image("img/zz1.png")
            );
            outlinesList.getItems().addAll(prePixList);
            outlinesList.setCellFactory(new Callback<ListView<Image>, ListCell<Image>>() {
                @Override
                public ListCell<Image> call(ListView<Image> param) {
                    return new ListImageCell();
                }
            });
            outlinesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Image>() {
                @Override
                public void changed(ObservableValue ov, Image oldValue, Image newValue) {
                    if (isSettingValues || newValue == null) {
                        return;
                    }
                    loadOutlineSource(newValue);
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void initOpacitySelector() {
        try {
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
                            scopeView.setOpacity(opacity);
                            FxmlControl.setEditorNormal(opacitySelector);
                            AppVariables.setUserConfigValue(baseName + "ScopeTransparency", newVal);
                        } else {
                            FxmlControl.setEditorBadStyle(opacitySelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(opacitySelector);
                    }
                }
            });

            opacitySelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue(baseName + "ScopeTransparency", message("ScopeTransparency0.5")));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        events
     */
    @Override
    public void viewSizeChanged(double change) {
        if (isSettingValues || imageView.getImage() == null) {
            return;
        }
        super.viewSizeChanged(change);
        if (isSettingValues || scope == null || scope.getScopeType() == null
                //                || change < sizeChangeAware
                || !scopeView.isVisible()) {
            return;
        }
        // Following handlers can conflict with threads' status changes which must check variables carefully
        switch (scope.getScopeType()) {
            case Operate:
                scopeView.setFitWidth(imageView.getFitWidth());
                scopeView.setFitHeight(imageView.getFitHeight());
                scopeView.setLayoutX(imageView.getLayoutX());
                scopeView.setLayoutY(imageView.getLayoutY());
                break;
            case Outline:
                makeOutline();
                break;
            default:
                indicateScope();
                break;
        }
    }

    @Override
    public void setImageChanged(boolean imageChanged) {
        try {
            this.imageChanged = imageChanged;

            if (imageChanged) {
                indicateScope();
                drawMaskControls();
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    protected List<MenuItem> makeImageContextMenu() {
        return null;
    }

    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
        if (p == null || imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (isPickingColor) {
            Color color = FxmlControl.imagePixel(p, imageView);
            if (color != null) {
                addColor(color);
            }
            return;
        }

        if (!scopeView.isVisible()) {
            return;
        }
        if (scope.getScopeType() == ScopeType.Matting) {
            int ix = (int) Math.round(p.getX());
            int iy = (int) Math.round(p.getY());
            scope.addPoint(ix, iy);
            pointsList.getItems().add(ix + "," + iy);
            pointsList.getSelectionModel().selectLast();
            indicateScope();
        } else {
            super.imageClicked(event, p);
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
                        pointsList.getItems().clear();
                        for (DoublePoint mp : maskPolygonData.getPoints()) {
                            pointsList.getItems().add((int) mp.getX() + "," + (int) mp.getY());
                        }
                        scope.setPolygon(maskPolygonData.cloneValues());
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

    /*
        scope
     */
    public void checkScopeType() {
        if (isSettingValues) {
            return;
        }
        try {
            clearScope();
            if (scopeTypeGroup.getSelectedToggle() == null) {
                scope.setScopeType(ScopeType.All);
            } else {
                RadioButton selected = (RadioButton) scopeTypeGroup.getSelectedToggle();
                if (selected.equals(scopeAllRadio)) {
                    scope.setScopeType(ScopeType.All);

                } else if (selected.equals(scopeMattingRadio)) {
                    scope.setScopeType(ScopeType.Matting);

                } else if (selected.equals(scopeRectangleRadio)) {
                    scope.setScopeType(ScopeType.Rectangle);

                } else if (selected.equals(scopeCircleRadio)) {
                    scope.setScopeType(ScopeType.Circle);

                } else if (selected.equals(scopeEllipseRadio)) {
                    scope.setScopeType(ScopeType.Ellipse);

                } else if (selected.equals(scopePolygonRadio)) {
                    scope.setScopeType(ScopeType.Polygon);

                } else if (selected.equals(scopeColorRadio)) {
                    scope.setScopeType(ScopeType.Color);

                } else if (selected.equals(scopeRectangleColorRadio)) {
                    scope.setScopeType(ScopeType.RectangleColor);

                } else if (selected.equals(scopeCircleColorRadio)) {
                    scope.setScopeType(ScopeType.CircleColor);

                } else if (selected.equals(scopeEllipseColorRadio)) {
                    scope.setScopeType(ScopeType.EllipseColor);

                } else if (selected.equals(scopePolygonColorRadio)) {
                    scope.setScopeType(ScopeType.PolygonColor);

                } else if (selected.equals(scopeOutlineRadio)) {
                    scope.setScopeType(ScopeType.Outline);
                }
            }

            setScopeControls();
            setScopeValues();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkMatchType() {
        if (isSettingValues || scope == null || matchGroup.getSelectedToggle() == null) {
            return;
        }
        try {
            int max = 255;
            RadioButton selected = (RadioButton) matchGroup.getSelectedToggle();
            if (selected.equals(colorRGBRadio)) {
                scope.setColorScopeType(ColorScopeType.Color);

            } else if (selected.equals(colorRedRadio)) {
                scope.setColorScopeType(ColorScopeType.Red);

            } else if (selected.equals(colorGreenRadio)) {
                scope.setColorScopeType(ColorScopeType.Green);

            } else if (selected.equals(colorBlueRadio)) {
                scope.setColorScopeType(ColorScopeType.Blue);

            } else if (selected.equals(colorSaturationRadio)) {
                scope.setColorScopeType(ColorScopeType.Saturation);
                max = 100;

            } else if (selected.equals(colorHueRadio)) {
                scope.setColorScopeType(ColorScopeType.Hue);
                max = 360;

            } else if (selected.equals(colorBrightnessRadio)) {
                scope.setColorScopeType(ColorScopeType.Brightness);
                max = 100;

            }

            FxmlControl.setTooltip(scopeDistanceSelector, new Tooltip("0~" + max));

            List<String> vList = new ArrayList<>();
            for (int i = 0; i <= max; i += 10) {
                vList.add(i + "");
            }
            isSettingValues = true;
            scopeDistanceSelector.getItems().clear();
            scopeDistanceSelector.getItems().addAll(vList);
            scopeDistanceSelector.getSelectionModel().select("20");
            isSettingValues = false;

            if (checkDistanceValue()) {
                indicateScope();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected boolean checkDistanceValue() {
        if (scope.getColorScopeType() == null
                || scopeDistanceSelector.getSelectionModel().getSelectedItem() == null) {
            return false;
        }
        boolean valid = true;
        try {
            int distance = Integer.valueOf(scopeDistanceSelector.getSelectionModel().getSelectedItem());
            switch (scope.getColorScopeType()) {
                case Hue:
                    if (distance >= 0 && distance <= 360) {
                        FxmlControl.setEditorNormal(scopeDistanceSelector);
                        scope.setHsbDistance(distance / 360.0f);
                    } else {
                        FxmlControl.setEditorBadStyle(scopeDistanceSelector);
                        valid = false;
                    }
                    break;
                case Brightness:
                case Saturation:
                    if (distance >= 0 && distance <= 100) {
                        FxmlControl.setEditorNormal(scopeDistanceSelector);
                        scope.setHsbDistance(distance / 100.0f);
                    } else {
                        FxmlControl.setEditorBadStyle(scopeDistanceSelector);
                        valid = false;
                    }
                    break;
                default:
                    if (distance >= 0 && distance <= 255) {
                        FxmlControl.setEditorNormal(scopeDistanceSelector);
                        scope.setColorDistance(distance);
                    } else {
                        FxmlControl.setEditorBadStyle(scopeDistanceSelector);
                        valid = false;
                    }
            }
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(scopeDistanceSelector);
            MyBoxLog.debug(e.toString());
            valid = false;
        }
        return valid;
    }

    protected void setScopeControls() {
        try {
            scopeEditBox.setVisible(scope != null && scope.getScopeType() != ScopeType.All);
            scopeSetBox.getChildren().clear();
            scopeTips.setText("");
            if (image == null || scope == null) {
                return;
            }
            isSettingValues = true;
            switch (scope.getScopeType()) {
                case All:
                    scopeTips.setText(message("WholeImage"));
                    break;
                case Matting:
                    scopeTips.setText(message("ScopeMattingTips"));
                    scopeSetBox.getChildren().addAll(scopePointsBox, eightNeighborCheck, colorMatchBox);
                    if (opacity == 0) {
                        opacitySelector.getSelectionModel().select(0);
                    }
                    break;

                case Rectangle:
                    scopeTips.setText(message("ScopeRectangleTips"));
                    scopeSetBox.getChildren().addAll(rectangleBox, areaExcludedCheck);
                    rectangleLabel.setText(message("Rectangle"));
                    break;

                case Circle:
                    scopeTips.setText(message("ScopeCircleTips"));
                    scopeSetBox.getChildren().addAll(circleBox, areaExcludedCheck);
                    break;

                case Ellipse:
                    scopeTips.setText(message("ScopeEllipseTips"));
                    scopeSetBox.getChildren().addAll(rectangleBox, areaExcludedCheck);
                    rectangleLabel.setText(message("Ellipse"));
                    break;

                case Polygon:
                    scopeSetBox.getChildren().addAll(scopePointsBox, areaExcludedCheck);
                    scopeTips.setText(message("ScopePolygonTips"));
                    break;

                case Color:
                    scopeTips.setText(message("ScopeColorTips"));
                    scopeSetBox.getChildren().addAll(scopeColorsBox, colorMatchBox);
                    if (opacity == 0) {
                        opacitySelector.getSelectionModel().select(0);
                    }
                    break;

                case RectangleColor:
                    scopeTips.setText(message("ScopeRectangleColorsTips"));
                    scopeSetBox.getChildren().addAll(rectangleBox, areaExcludedCheck, scopeColorsBox, colorMatchBox);
                    rectangleLabel.setText(message("Rectangle"));
                    if (opacity == 0) {
                        opacitySelector.getSelectionModel().select(0);
                    }
                    break;

                case CircleColor:
                    scopeTips.setText(message("ScopeCircleColorsTips"));
                    scopeSetBox.getChildren().addAll(circleBox, areaExcludedCheck, scopeColorsBox, colorMatchBox);
                    if (opacity == 0) {
                        opacitySelector.getSelectionModel().select(0);
                    }
                    break;

                case EllipseColor:
                    scopeTips.setText(message("ScopeEllipseColorsTips"));
                    scopeSetBox.getChildren().addAll(rectangleBox, areaExcludedCheck, scopeColorsBox, colorMatchBox);
                    rectangleLabel.setText(message("Ellipse"));
                    break;

                case PolygonColor:
                    scopeTips.setText(message("ScopePolygonColorsTips"));
                    scopeSetBox.getChildren().addAll(scopePointsBox, areaExcludedCheck, scopeColorsBox, colorMatchBox);
                    if (opacity == 0) {
                        opacitySelector.getSelectionModel().select(0);
                    }
                    break;

                case Outline:
                    scopeTips.setText(message("ScopeOutlineTips"));
                    scopeSetBox.getChildren().addAll(scopeOutlineBox, areaExcludedCheck);
                    if (outlinesList.getItems().isEmpty()) {
                        initOutlineBox();
                    }
                    break;

                default:
                    return;
            }
            setScopeName();
            FxmlControl.refreshStyle(scopeSetBox);
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void setScopeValues() {
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
                    scope.setRectangle(maskRectangleData.cloneValues());
                    indicateScope();
                    break;

                case Circle:
                    initMaskCircleLine(true);
                    scope.setCircle(maskCircleData.cloneValues());
                    indicateScope();
                    break;

                case Ellipse:
                    initMaskEllipseLine(true);
                    scope.setEllipse(maskEllipseData.cloneValues());
                    indicateScope();
                    break;

                case Polygon:
                    initMaskPolygonLine(true);
                    scope.setPolygon(maskPolygonData.cloneValues());
                    indicateScope();
                    break;

                case Color:
                    checkMatchType();
                    break;

                case RectangleColor:
                    initMaskRectangleLine(true);
                    scope.setRectangle(maskRectangleData.cloneValues());
                    checkMatchType();
                    break;

                case CircleColor:
                    initMaskCircleLine(true);
                    scope.setCircle(maskCircleData.cloneValues());
                    checkMatchType();
                    break;

                case EllipseColor:
                    initMaskEllipseLine(true);
                    scope.setEllipse(maskEllipseData.cloneValues());
                    checkMatchType();
                    break;

                case PolygonColor:
                    initMaskPolygonLine(true);
                    scope.setPolygon(maskPolygonData.cloneValues());
                    checkMatchType();
                    break;

                case Outline:
                    if (!outlinesList.getItems().isEmpty()) {
                        outlinesList.getSelectionModel().select(null);
                        outlinesList.getSelectionModel().select(0);
                    }
                    break;
                default:
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected void indicateScope() {
        if (isSettingValues || imageView == null || !scopeView.isVisible()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            final String text = imageController.scopeLabel.getText();
            imageController.scopeLabel.setText(message("Loading"));
            task = new SingletonTask<Void>() {
                private Image scopedImage;

                @Override
                protected boolean handle() {
                    try {
                        PixelsOperation pixelsOperation = PixelsOperation.create(imageView.getImage(),
                                scope, PixelsOperation.OperationType.ShowScope);
                        if (!ignoreTransparentCheck.isSelected()) {
                            pixelsOperation.setSkipTransparent(false);
                        }
                        scopedImage = pixelsOperation.operateFxImage();
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        return scopedImage != null;
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (scope == null) {
                        return;
                    }
                    scopeView.setImage(scopedImage);
                    scopeView.setFitWidth(imageView.getFitWidth());
                    scopeView.setFitHeight(imageView.getFitHeight());
                    scopeView.setLayoutX(imageView.getLayoutX());
                    scopeView.setLayoutY(imageView.getLayoutY());
                }

                @Override
                protected void finalAction() {
                    if (message("Loading").equals(text)) {
                        imageController.scopeLabel.setText(message("Scope"));
                    } else {
                        imageController.scopeLabel.setText(text);
                    }
                }

            };
//            parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void clearScope() {
        try {
            initMaskControls(false);
            isSettingValues = true;
            if (imageView.getImage() != null) {
                scope = new ImageScope(imageView.getImage());
                if (sourceFile != null) {
                    scope.setFile(sourceFile.getAbsolutePath());
                }
            } else {
                scope = new ImageScope();
            }
            scopeView.setImage(null);
            outlineSource = null;

            pointsList.getItems().clear();
            colorsList.getItems().clear();
            scopeDistanceSelector.getItems().clear();
            areaExcludedCheck.setSelected(false);
            colorExcludedCheck.setSelected(false);
            scopeDistanceSelector.getEditor().setStyle(null);
            outlinesList.getSelectionModel().select(null);
            scopeSetBox.getChildren().clear();
            isSettingValues = false;

            pickColorCheck.setSelected(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        shape
     */
    @FXML
    public void okRectangle() {
        try {
            if (scope == null) {
                return;
            }
            double x1, y1, x2, y2;
            try {
                x1 = Double.parseDouble(rectLeftTopXInput.getText());
                rectLeftTopXInput.setStyle(null);
            } catch (Exception e) {
                rectLeftTopXInput.setStyle(badStyle);
                return;
            }
            try {
                y1 = Double.parseDouble(rectLeftTopYInput.getText());
                rectLeftTopYInput.setStyle(null);
            } catch (Exception e) {
                rectLeftTopYInput.setStyle(badStyle);
                return;
            }
            try {
                x2 = Double.parseDouble(rightBottomXInput.getText());
                rightBottomXInput.setStyle(null);
            } catch (Exception e) {
                rightBottomXInput.setStyle(badStyle);
                return;
            }
            try {
                y2 = Double.parseDouble(rightBottomYInput.getText());
                rightBottomYInput.setStyle(null);
            } catch (Exception e) {
                rightBottomYInput.setStyle(badStyle);
                return;
            }
            DoubleRectangle rect = new DoubleRectangle(x1, y1, x2, y2);
            if (!rect.isValid()) {
                popError(message("InvalidData"));
                return;
            }
            switch (scope.getScopeType()) {
                case Rectangle:
                case RectangleColor:
                    maskRectangleData = rect;
                    scope.setRectangle(maskRectangleData.cloneValues());
                    drawMaskRectangleLineAsData();
                    break;
                case Ellipse:
                case EllipseColor:
                    maskEllipseData = new DoubleEllipse(x1, y1, x2, y2);
                    scope.setEllipse(maskEllipseData.cloneValues());
                    drawMaskEllipseLineAsData();
                    break;
                default:
                    return;
            }

            indicateScope();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void okCircle() {
        try {
            if (scope == null) {
                return;
            }
            double x, y, r;
            try {
                x = Double.parseDouble(circleCenterXInput.getText());
                circleCenterXInput.setStyle(null);
            } catch (Exception e) {
                circleCenterXInput.setStyle(badStyle);
                return;
            }
            try {
                y = Double.parseDouble(circleCenterYInput.getText());
                circleCenterYInput.setStyle(null);
            } catch (Exception e) {
                circleCenterYInput.setStyle(badStyle);
                return;
            }
            try {
                r = Double.parseDouble(circleRadiusInput.getText());
                circleRadiusInput.setStyle(null);
            } catch (Exception e) {
                circleRadiusInput.setStyle(badStyle);
                return;
            }
            DoubleCircle circle = new DoubleCircle(x, y, r);
            if (!circle.isValid()) {
                popError(message("InvalidData"));
                return;
            }
            switch (scope.getScopeType()) {
                case Circle:
                case CircleColor:
                    maskCircleData = circle;
                    scope.setCircle(maskCircleData.cloneValues());
                    drawMaskCircleLineAsData();
                    break;
                default:
                    return;
            }
            indicateScope();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean drawMaskRectangleLineAsData() {
        if (maskRectangleLine == null || !maskPane.getChildren().contains(maskRectangleLine)
                || maskRectangleData == null
                || imageView == null || imageView.getImage() == null) {
            return false;
        }
        if (!super.drawMaskRectangleLineAsData()) {
            return false;
        }
        rectLeftTopXInput.setText(scale(maskRectangleData.getSmallX(), 2) + "");
        rectLeftTopYInput.setText(scale(maskRectangleData.getSmallY(), 2) + "");
        rightBottomXInput.setText(scale(maskRectangleData.getBigX(), 2) + "");
        rightBottomYInput.setText(scale(maskRectangleData.getBigY(), 2) + "");
        return true;
    }

    @Override
    public boolean drawMaskCircleLineAsData() {
        if (maskCircleLine == null || !maskCircleLine.isVisible()
                || maskCircleData == null
                || imageView == null || imageView.getImage() == null) {
            return false;
        }
        if (!super.drawMaskCircleLineAsData()) {
            return false;
        }
        circleCenterXInput.setText(scale(maskCircleData.getCenterX(), 2) + "");
        circleCenterYInput.setText(scale(maskCircleData.getCenterY(), 2) + "");
        circleRadiusInput.setText(scale(maskCircleData.getRadius(), 2) + "");
        return true;
    }

    @Override
    public boolean drawMaskEllipseLineAsData() {
        if (maskEllipseLine == null || !maskEllipseLine.isVisible()
                || maskEllipseData == null
                || imageView == null || imageView.getImage() == null) {
            return false;
        }
        if (!super.drawMaskEllipseLineAsData()) {
            return false;
        }
        DoubleRectangle rect = maskEllipseData.getRectangle();
        rectLeftTopXInput.setText(scale(rect.getSmallX(), 2) + "");
        rectLeftTopYInput.setText(scale(rect.getSmallY(), 2) + "");
        rightBottomXInput.setText(scale(rect.getBigX(), 2) + "");
        rightBottomYInput.setText(scale(rect.getBigY(), 2) + "");
        return true;
    }


    /*
        points
     */
    @FXML
    public void deletePoints() {
        if (isSettingValues) {
            return;
        }
        if (scope.getScopeType() == ScopeType.Matting) {
            List<Integer> indices = pointsList.getSelectionModel().getSelectedIndices();
            for (int i = indices.size() - 1; i >= 0; i--) {
                int index = indices.get(i);
                if (index < scope.getPoints().size()) {
                    scope.getPoints().remove(index);
                }
            }
        } else if (scope.getScopeType() == ScopeType.Polygon
                || scope.getScopeType() == ScopeType.PolygonColor) {
            List<Integer> indices = pointsList.getSelectionModel().getSelectedIndices();
            for (int i = indices.size() - 1; i >= 0; i--) {
                maskPolygonData.remove(indices.get(i));
            }
            drawMaskPolygonLine();
            scope.setPolygon(maskPolygonData.cloneValues());
        }
        pointsList.getItems().removeAll(pointsList.getSelectionModel().getSelectedItems());
        indicateScope();
    }

    @FXML
    public void clearPoints() {
        if (isSettingValues) {
            return;
        }
        scope.clearPoints();
        pointsList.getItems().clear();
        if (scope.getScopeType() == ScopeType.Polygon
                || scope.getScopeType() == ScopeType.PolygonColor) {
            maskPolygonData.clear();
            drawMaskPolygonLine();
            scope.setPolygon(maskPolygonData.cloneValues());
        }
        indicateScope();
    }

    /*
        colors
     */
    @Override
    protected void startPickingColor() {
        popInformation(message("PickingColorsForScope"));
        imageController.scopeLabel.setStyle(darkRedText);
        imageController.scopeLabel.setText(message("PickingColorsForScope"));
        imageController.imageLabel.setStyle(darkRedText);
        imageController.imageLabel.setText(message("PickingColorsForScope"));

    }

    @Override
    protected void stopPickingColor() {
        imageController.imageLabel.setStyle(null);
        imageController.scopeLabel.setStyle(null);
        imageController.imageLabel.setText(message("ImagePaneTitle"));
        imageController.scopeLabel.setText(message("ScopePaneTitle"));
    }

    @Override
    protected Color pickColor(DoublePoint p, ImageView view) {
        Color color = FxmlControl.imagePixel(p, imageView);
        return color;
    }

    public boolean addColor(Color color) {
        if (isSettingValues || color == null
                || scope == null || scope.getScopeType() == null
                || colorsList.getItems().contains(color)) {
            return false;
        }
        switch (scope.getScopeType()) {
            case Color:
            case RectangleColor:
            case CircleColor:
            case EllipseColor:
            case PolygonColor:
                scope.addColor(ImageColor.converColor(color));
                colorsList.getItems().add(color);
                indicateScope();
                return true;
            default:
                return false;
        }
    }

    @FXML
    public void deleteColors() {
        if (isSettingValues) {
            return;
        }
        List<Color> colors = colorsList.getSelectionModel().getSelectedItems();
        if (colors == null || colors.isEmpty()) {
            return;
        }
        for (Color color : colors) {
            scope.getColors().remove(ImageColor.converColor(color));
        }
        colorsList.getItems().removeAll(colors);
        indicateScope();
    }

    @FXML
    public void clearColors() {
        if (isSettingValues) {
            return;
        }
        scope.getColors().clear();
        colorsList.getItems().clear();
        indicateScope();
    }

    @FXML
    public void popSaveColorsMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu = new MenuItem(message("SaveInPalette"));
            menu.setOnAction((ActionEvent event) -> {
                saveColorsInPalette();
            });
            popMenu.getItems().add(menu);
            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("SaveInColorsLibrary"));
            menu.setOnAction((ActionEvent event) -> {
                saveColorsInTable();
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void saveColorsInPalette() {
        List<Color> colors = colorsList.getSelectionModel().getSelectedItems();
        if (colors == null || colors.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    TableColorData.addColorsInPalette(colors);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popText(message("Successful"), AppVariables.getCommentsDelay(),
                            "white", "1.5em", saveColorsButton);
                }

            };
            parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void saveColorsInTable() {
        List<Color> colors = colorsList.getSelectionModel().getSelectedItems();
        if (colors == null || colors.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                @Override
                protected boolean handle() {
                    TableColorData.writeColors(colors, false);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popText(message("Successful"), AppVariables.getCommentsDelay(),
                            "white", "1.5em", saveColorsButton);
                }

            };
            parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /*
        Manage scopes
     */
    public void initSavedScopesBox() {
        try {
            scopesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            scopesList.setCellFactory(new Callback<ListView<ImageScope>, ListCell<ImageScope>>() {
                @Override
                public ListCell<ImageScope> call(ListView<ImageScope> param) {
                    return new ImageScopeCell();
                }
            });

            deleteScopesButton.disableProperty().bind(
                    scopesList.getSelectionModel().selectedItemProperty().isNull()
            );
            useScopeButton.disableProperty().bind(deleteScopesButton.disableProperty());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
                MyBoxLog.error(e.toString());
                setText(null);
                setGraphic(null);
            }

        }
    }

    public void initNameBox() {
        try {
            saveScopeButton.disableProperty().bind(scopeNameInput.textProperty().isEmpty()
                    .or(scopeDistanceSelector.visibleProperty()
                            .and(scopeDistanceSelector.getEditor().styleProperty().isEqualTo(badStyle)))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void loadScopes() {
        if (sourceFile == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            scopesList.getItems().clear();
            task = new SingletonTask<Void>() {
                List<ImageScope> list;

                @Override
                protected boolean handle() {
                    list = TableImageScope.read(sourceFile.getAbsolutePath());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (list != null && !list.isEmpty()) {
                        scopesList.getItems().setAll(list);
//                        scopesList.getSelectionModel().selectFirst();
                    }
                }
            };
            parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void setScopeName() {
        if (scope == null) {
            return;
        }
        String name = scope.getName();
        if (name == null || name.isEmpty()) {
            name = scope.getScopeType() + "_" + DateTools.datetimeToString(new Date());
        }
        scopeNameInput.setText(name);
    }

    @FXML
    public void deleteScopes() {
        List<ImageScope> selected = scopesList.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    return TableImageScope.delete(selected);
                }

                @Override
                protected void whenSucceeded() {
                    for (ImageScope scope : selected) {
                        scopesList.getItems().remove(scope);
                    }
                    scopesList.refresh();
//                    loadScopes();
                }
            };
            parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void saveScope() {
        if (scope == null || scope.getFile() == null || saveScopeButton.isDisabled()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String name = scopeNameInput.getText().trim();
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
            parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
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
            if (task != null && !task.isQuit()) {
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
                    scopesList.getItems().clear();
                    scopesList.refresh();
                }
            };
            parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void useScope() {
        ImageScope selected = scopesList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        scope = selected;
        // Force listView to refresh
        // https://stackoverflow.com/questions/13906139/javafx-update-of-listview-if-an-element-of-observablelist-changes?r=SearchResults
        for (int i = 0; i < scopesList.getItems().size(); ++i) {
            scopesList.getItems().set(i, scopesList.getItems().get(i));
        }
        showScope(scope);
    }

    @FXML
    public void refreshScopes() {
        loadScopes();
    }

    public void showScope(ImageScope scope) {
        MyBoxLog.debug("here");
        if (scope == null || scope.getScopeType() == null) {
            return;
        }
        clearScope();
        this.scope = scope;
        setScopeControls();
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
                scopeTypeGroup.selectToggle(null);
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
                            pointsList.getItems().add(p.getX() + "," + p.getY());
                        }
                        pointsList.getSelectionModel().selectLast();
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
                            pointsList.getItems().add(p.getX() + "," + p.getY());
                        }
                        pointsList.getSelectionModel().selectLast();
                    }
                    maskPolygonData = scope.getPolygon();
                    return drawMaskPolygonLineAsData();
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
                        colorsList.getItems().clear();
                        colorsList.getItems().addAll(list);
                        colorsList.getSelectionModel().selectLast();
                    }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
                    colorRGBRadio.fire();
                    break;
                case Red:
                    colorRedRadio.fire();
                    break;
                case Green:
                    colorGreenRadio.fire();
                    break;
                case Blue:
                    colorBlueRadio.fire();
                    break;
                case Hue:
                    colorHueRadio.fire();
                    break;
                case Brightness:
                    colorBrightnessRadio.fire();
                    break;
                case Saturation:
                    colorSaturationRadio.fire();
                    break;
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
            FxmlControl.setTooltip(scopeDistanceSelector, new Tooltip("0~" + max));
            List<String> vList = new ArrayList<>();
            for (int i = 0; i <= max; i += 10) {
                vList.add(i + "");
            }
            scopeDistanceSelector.getItems().clear();
            scopeDistanceSelector.getItems().addAll(vList);
            scopeDistanceSelector.getSelectionModel().select(distance + "");
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
            MyBoxLog.error(e.toString());
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
                int fileNumber = AppVariables.fileRecentNumber * 3 / 4;
                return VisitHistoryTools.getRecentAlphaImages(fileNumber);
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
            if (task != null && !task.isQuit()) {
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
                        MyBoxLog.error(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    loadOutlineSource(bufferedImage);
                }

            };
            parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    public void loadOutlineSource(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            return;
        }
        loadOutlineSource(bufferedImage, new DoubleRectangle(0, 0,
                bufferedImage.getWidth(), bufferedImage.getHeight()));
    }

    public void loadOutlineSource(BufferedImage bufferedImage, DoubleRectangle rect) {
        if (bufferedImage == null || rect == null) {
            return;
        }
        outlineSource = bufferedImage;
        maskRectangleData = rect.cloneValues();
        setMaskRectangleLineVisible(true);
        drawMaskRectangleLineAsData();

        makeOutline();
    }

    public void loadOutlineSource(Image image) {
        if (isSettingValues || image == null) {
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
                if (task != null && !task.isQuit()) {
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
                            MyBoxLog.error(e.toString());
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
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void displayOutline(BufferedImage bufferedImage) {
        if (scope == null || bufferedImage == null || scope.getScopeType() != ScopeType.Outline) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
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
                        MyBoxLog.error(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    scopeView.setImage(outlineImage);
                    double radio = imageView.getBoundsInParent().getWidth() / getImageWidth();
                    double offsetX = maskRectangleData.getSmallX() >= 0 ? 0 : maskRectangleData.getSmallX();
                    double offsetY = maskRectangleData.getSmallY() >= 0 ? 0 : maskRectangleData.getSmallY();
                    scopeView.setLayoutX(imageView.getLayoutX() + offsetX * radio);
                    scopeView.setLayoutY(imageView.getLayoutY() + offsetY * radio);
                    scopeView.setFitWidth(outlineImage.getWidth() * radio);
                    scopeView.setFitHeight(outlineImage.getHeight() * radio);
                }
            };
            parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

}
