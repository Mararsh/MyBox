package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.util.Callback;
import static mara.mybox.controller.BaseController.logger;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.ImageScope;
import mara.mybox.objects.ImageScope.AreaScopeType;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.fxml.FxmlScopeTools;
import mara.mybox.objects.ImageScope.ColorScopeType;
import mara.mybox.objects.Rectangle;

/**
 * @Author Mara
 * @CreateDate 2018-8-1
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageScopeController extends BaseController {

    private ImageManufactureController imageController;
    private ImageScope inScope, scope;
    private boolean areaValid, isSettingValue;
    private OperationType operationType;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab colorTab, areaTab;
    @FXML
    private CheckBox colorExcludedCheck, rectangleExcludedCheck, circleExcludedCheck;
    @FXML
    private ToolBar rectangleBar, circleBar, colorBar1, colorBar2, hotBar, areaBar, colorBar;
    @FXML
    private ComboBox colorsBox, opacityBox;
    @FXML
    private Button okButton, inButton, outButton, paneButton, imageButton;
    @FXML
    private ToggleGroup areaGroup, colorGroup;
    @FXML
    private TextField leftXInput, leftYInput, rightXInput, rightYInput, colorDistanceInput;
    @FXML
    private TextField centerXInput, centerYInput, radiusInput, currentInput;
    @FXML
    private RadioButton allColorsRadio, matchHueRadio, matchColorRadio, allAreaRadio, selectedRectangleRadio, selectedCircleRadio;
    @FXML
    private SplitPane splitPane;
    @FXML
    private ScrollPane imagePane, scopePane;
    @FXML
    private ImageView imageView, scopeView;
    @FXML
    private Label titleLabel, colorTipsLabel, scopeTipsLabel;

    public enum OperationType {
        AllArea, Rectangle, Circle, AllColors, Color, Hue
    }

    @Override
    protected void initializeNext() {
        try {

            initAreaTab();
            initColorTab();

            Tooltip tips = new Tooltip(getMessage("ScopeComments"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(scopeTipsLabel, tips);

            tips = new Tooltip(getMessage("ColorMatchComments"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(colorTipsLabel, tips);

            tips = new Tooltip(getMessage("CTRL+a"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(okButton, tips);

            tips = new Tooltip(getMessage("CTRL+1"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(imageButton, tips);

            tips = new Tooltip(getMessage("CTRL+2"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(paneButton, tips);

            tips = new Tooltip(getMessage("CTRL+3"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(inButton, tips);

            tips = new Tooltip(getMessage("CTRL+4"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(outButton, tips);

            List<Double> values = Arrays.asList(0.1, 0.5, 0.2, 0.3, 0.6, 0.4, 0.7, 0.8, 0.9);
            opacityBox.getItems().addAll(values);
            opacityBox.setVisibleRowCount(values.size());
            opacityBox.valueProperty().addListener(new ChangeListener<Double>() {
                @Override
                public void changed(ObservableValue ov, Double oldValue, Double newValue) {
                    if (newValue >= 0 && newValue <= 1.0) {
                        scope.setOpacity(newValue);
                        if (!isSettingValue) {
                            showScope();
                        }
                    }
                }
            });

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue<? extends Tab> observable,
                        Tab oldValue, Tab newValue) {
                    Tab tab = tabPane.getSelectionModel().getSelectedItem();
                    if (colorTab.equals(tab)) {
                        if (null != scope.getColorScopeType()) {
                            switch (scope.getColorScopeType()) {
                                case AllColor:
                                    allColorsRadio.setSelected(true);
                                    break;
                                case Color:
                                case ColorExcluded:
                                    matchColorRadio.setSelected(true);
                                    break;
                                case Hue:
                                case HueExcluded:
                                    matchHueRadio.setSelected(true);
                                    break;
                                default:
                                    break;
                            }
                        }
                    } else if (areaTab.equals(tab)) {
                        if (null != scope.getAreaScopeType()) {
                            switch (scope.getAreaScopeType()) {
                                case AllArea:
                                    allAreaRadio.setSelected(true);
                                    break;
                                case Circle:
                                case CircleExcluded:
                                    selectedCircleRadio.setSelected(true);
                                    break;
                                case Rectangle:
                                case RectangleExlcuded:
                                    selectedRectangleRadio.setSelected(true);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                }
            });

            okButton.disableProperty().bind(
                    leftXInput.styleProperty().isEqualTo(badStyle)
                            .or(leftYInput.styleProperty().isEqualTo(badStyle))
                            .or(rightXInput.styleProperty().isEqualTo(badStyle))
                            .or(rightYInput.styleProperty().isEqualTo(badStyle))
                            .or(colorDistanceInput.styleProperty().isEqualTo(badStyle))
                            .or(colorsBox.visibleRowCountProperty().isEqualTo(0)));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initAreaTab() {
        try {

            areaGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkAreaType();
                }
            });

            leftXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkRectangleValues();
                }
            });
            leftYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkRectangleValues();
                }
            });
            rightXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkRectangleValues();
                }
            });
            rightYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkRectangleValues();
                }
            });

            rectangleExcludedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    if (selectedRectangleRadio.isSelected()) {
                        if (rectangleExcludedCheck.isSelected()) {
                            scope.setAreaScopeType(AreaScopeType.RectangleExlcuded);
                        } else {
                            scope.setAreaScopeType(AreaScopeType.Rectangle);
                        }
                    }
                }
            });

            centerXInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCircleValues();
                }
            });

            centerYInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCircleValues();
                }
            });

            radiusInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkCircleValues();
                }
            });

            circleExcludedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    if (selectedCircleRadio.isSelected()) {
                        if (circleExcludedCheck.isSelected()) {
                            scope.setAreaScopeType(AreaScopeType.CircleExcluded);
                        } else {
                            scope.setAreaScopeType(AreaScopeType.Circle);
                        }
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkAreaType() {
        leftXInput.setStyle(null);
        leftYInput.setStyle(null);
        rightXInput.setStyle(null);
        rightYInput.setStyle(null);
        centerXInput.setStyle(null);
        centerYInput.setStyle(null);
        radiusInput.setStyle(null);
        RadioButton selected = (RadioButton) areaGroup.getSelectedToggle();
        if (getMessage("AllArea").equals(selected.getText())) {
            operationType = OperationType.AllArea;
            scope.setAreaScopeType(AreaScopeType.AllArea);
            rectangleBar.setDisable(true);
            circleBar.setDisable(true);
            areaValid = true;
            showScope();

        } else if (getMessage("SelectedRectangle").equals(selected.getText())) {
            operationType = OperationType.Rectangle;
            if (rectangleExcludedCheck.isSelected()) {
                scope.setAreaScopeType(AreaScopeType.RectangleExlcuded);
            } else {
                scope.setAreaScopeType(AreaScopeType.Rectangle);
            }
            rectangleBar.setDisable(false);
            circleBar.setDisable(true);
            checkRectangleValues();

        } else if (getMessage("SelectedCircle").equals(selected.getText())) {
            operationType = OperationType.Circle;
            if (circleExcludedCheck.isSelected()) {
                scope.setAreaScopeType(AreaScopeType.CircleExcluded);
            } else {
                scope.setAreaScopeType(AreaScopeType.Circle);
            }
            rectangleBar.setDisable(true);
            circleBar.setDisable(false);
            checkCircleValues();
        }

    }

    private void checkRectangleValues() {
        if (leftXInput.isDisable() || isSettingValue) {
            return;
        }
        final Image currentImage = imageView.getImage();
        areaValid = true;

        int leftX = -1, leftY = -1, rightX = -1, rightY = -1;
        try {
            leftX = Integer.valueOf(leftXInput.getText());
            leftXInput.setStyle(null);
            if (leftX >= 0 && leftX <= currentImage.getWidth()) {
                leftXInput.setStyle(null);
            } else {
                leftXInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            leftXInput.setStyle(badStyle);
            areaValid = false;
        }

        try {
            leftY = Integer.valueOf(leftYInput.getText());
            leftYInput.setStyle(null);
            if (leftY >= 0 && leftY <= currentImage.getHeight()) {
                leftYInput.setStyle(null);
            } else {
                leftYInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            leftYInput.setStyle(badStyle);
            areaValid = false;
        }

        try {
            rightX = Integer.valueOf(rightXInput.getText());
            rightXInput.setStyle(null);
            if (rightX >= 0 && rightX <= currentImage.getWidth()) {
                rightXInput.setStyle(null);
            } else {
                rightXInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            rightXInput.setStyle(badStyle);
            areaValid = false;
        }

        try {
            rightY = Integer.valueOf(rightYInput.getText());
            rightYInput.setStyle(null);
            if (rightY >= 0 && rightY <= currentImage.getHeight()) {
                rightYInput.setStyle(null);
            } else {
                rightYInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            rightYInput.setStyle(badStyle);
            areaValid = false;
        }

        if (leftX >= rightX) {
            leftXInput.setStyle(badStyle);
            rightXInput.setStyle(badStyle);
            areaValid = false;
        }

        if (leftY >= rightY) {
            leftYInput.setStyle(badStyle);
            rightYInput.setStyle(badStyle);
            areaValid = false;
        }

        if (areaValid) {
            scope.setRectangle(new Rectangle(leftX, leftY, rightX, rightY));
            showScope();
        } else {
            popError(getMessage("InvalidRectangle"));
        }

    }

    private void checkCircleValues() {
        if (centerXInput.isDisable() || isSettingValue) {
            return;
        }
        areaValid = true;
        int centerX = -1, centerY = -1, radius = -1;
        final Image currentImage = imageView.getImage();
        try {
            centerX = Integer.valueOf(centerXInput.getText());
            centerXInput.setStyle(null);
            if (centerX >= 0 && centerX <= currentImage.getWidth()) {
                centerXInput.setStyle(null);
            } else {
                centerXInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            centerXInput.setStyle(badStyle);
            areaValid = false;
        }

        try {
            centerY = Integer.valueOf(centerYInput.getText());
            centerYInput.setStyle(null);
            if (centerY >= 0 && centerY <= currentImage.getHeight()) {
                centerYInput.setStyle(null);
            } else {
                centerYInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            centerYInput.setStyle(badStyle);
            areaValid = false;
        }

        if (areaValid) {
            scope.setCircleCenter(centerX, centerY);
        }

        try {
            radius = Integer.valueOf(radiusInput.getText());
            radiusInput.setStyle(null);
            if (radius >= 0
                    && radius <= currentImage.getHeight() && radius <= currentImage.getWidth()) {
                radiusInput.setStyle(null);
                scope.setCircleRadius(radius);
            } else {
                radiusInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            radiusInput.setStyle(badStyle);
            areaValid = false;
        }

        if (areaValid) {
            showScope();
        } else {
            popError(getMessage("InvalidCircle"));
        }

    }

    protected void initColorTab() {
        try {

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkColorType();
                }
            });

            colorDistanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColorDistance();
                }
            });
            colorDistanceInput.setText("20");

            colorExcludedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    if (colorExcludedCheck.isSelected()) {
                        if (matchColorRadio.isSelected()) {
                            scope.setColorScopeType(ColorScopeType.ColorExcluded);
                        } else if (matchHueRadio.isSelected()) {
                            scope.setColorScopeType(ColorScopeType.HueExcluded);
                        }
                    } else {
                        if (matchColorRadio.isSelected()) {
                            scope.setColorScopeType(ColorScopeType.Color);
                        } else if (matchHueRadio.isSelected()) {
                            scope.setColorScopeType(ColorScopeType.Hue);
                        }
                    }
                    showScope();
                }
            });

            colorsBox.setCellFactory(new Callback<ListView<Color>, ListCell<Color>>() {
                @Override
                public ListCell<Color> call(ListView<Color> p) {
                    return new ListCell<Color>() {
                        private final javafx.scene.shape.Rectangle rectangle;

                        {
                            setContentDisplay(ContentDisplay.LEFT);
                            rectangle = new javafx.scene.shape.Rectangle(10, 10);
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

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkColorType() {
        RadioButton selected = (RadioButton) colorGroup.getSelectedToggle();
        if (getMessage("AllColors").equals(selected.getText())) {
            operationType = OperationType.AllColors;
            scope.setColorScopeType(ColorScopeType.AllColor);
            scope.clearColors();
            colorBar1.setDisable(true);
            colorBar2.setDisable(true);
            colorDistanceInput.setStyle(null);
            colorsBox.setVisibleRowCount(20);
            showScope();

        } else if (getMessage("MatchingColor").equals(selected.getText())) {
            operationType = OperationType.Color;
            if (colorExcludedCheck.isSelected()) {
                scope.setColorScopeType(ColorScopeType.ColorExcluded);
            } else {
                scope.setColorScopeType(ColorScopeType.Color);
            }
            colorBar1.setDisable(false);
            colorBar2.setDisable(false);
            checkColorDistance();

        } else if (getMessage("MatchingHue").equals(selected.getText())) {
            operationType = OperationType.Hue;
            if (colorExcludedCheck.isSelected()) {
                scope.setColorScopeType(ColorScopeType.HueExcluded);
            } else {
                scope.setColorScopeType(ColorScopeType.Hue);
            }
            colorBar1.setDisable(false);
            colorBar2.setDisable(false);
            checkColorDistance();
        }
    }

    private boolean checkColorDistance() {
        try {
            int colorDistance = Integer.valueOf(colorDistanceInput.getText());
            int max = 255;
            if (scope.getColorScopeType() == ColorScopeType.Hue
                    || scope.getColorScopeType() == ColorScopeType.HueExcluded) {
                max = 360;
            }
            if (colorDistance >= 0 && colorDistance <= max) {
                colorDistanceInput.setStyle(null);
                scope.setColorDistance(colorDistance);
                showScope();
                return true;
            } else {
                colorDistanceInput.setStyle(badStyle);
                return false;
            }
        } catch (Exception e) {
            colorDistanceInput.setStyle(badStyle);
            return false;
        }
    }

    public void loadImage(ImageManufactureController controller,
            ImageScope imageScope, String title) {
        try {
            if (controller == null
                    || imageScope == null || imageScope.getImage() == null) {
                closeStage();
                return;
            }
            imageController = controller;
            inScope = imageScope;
            scope = inScope.cloneValues();

            titleLabel.setText(title);
            getMyStage().setTitle(title);

            imageView.setPreserveRatio(true);
            scopeView.setPreserveRatio(true);
            if (imagePane.getHeight() < scope.getImage().getHeight()) {
                paneSize();
            } else {
                imageSize();
            }

            isSettingValue = true;
            imageView.setImage(scope.getImage());
            scopeView.setImage(scope.getImage());

            tabPane.getSelectionModel().select(colorTab);

            switch (scope.getAreaScopeType()) {
                case AllArea:
                    allAreaRadio.setSelected(true);
                    break;
                case Rectangle:
                    selectedRectangleRadio.setSelected(true);
                    rectangleExcludedCheck.setSelected(false);
                    break;
                case RectangleExlcuded:
                    selectedRectangleRadio.setSelected(true);
                    rectangleExcludedCheck.setSelected(true);
                    break;
                case Circle:
                    selectedCircleRadio.setSelected(true);
                    circleExcludedCheck.setSelected(false);
                    break;
                case CircleExcluded:
                    selectedCircleRadio.setSelected(true);
                    circleExcludedCheck.setSelected(true);
                    break;
                default:
                    allAreaRadio.setSelected(true);
            }

            if (scope.getRectangle().getLeftX() < 0) {
                rightXInput.setText((int) (scope.getImage().getWidth() * 3 / 4) + "");
            } else {
                rightXInput.setText(scope.getRectangle().getRightX() + "");
            }
            if (scope.getRectangle().getRightY() < 0) {
                rightYInput.setText((int) (scope.getImage().getHeight() * 3 / 4) + "");
            } else {
                rightYInput.setText(scope.getRectangle().getRightY() + "");
            }
            if (scope.getRectangle().getLeftX() < 0) {
                leftXInput.setText((int) (scope.getImage().getWidth() / 4) + "");
            } else {
                leftXInput.setText(scope.getRectangle().getLeftX() + "");
            }
            if (scope.getRectangle().getLeftY() < 0) {
                leftYInput.setText((int) (scope.getImage().getHeight() / 4) + "");
            } else {
                leftYInput.setText(scope.getRectangle().getLeftY() + "");
            }

            if (scope.getCircle().getCenterX() < 0) {
                centerXInput.setText((int) (scope.getImage().getWidth() / 2) + "");
            } else {
                centerXInput.setText(scope.getCircle().getCenterX() + "");
            }
            if (scope.getCircle().getCenterY() < 0) {
                centerYInput.setText((int) (scope.getImage().getHeight() / 2) + "");
            } else {
                centerYInput.setText(scope.getCircle().getCenterY() + "");
            }
            if (scope.getCircle().getRadius() < 0) {
                radiusInput.setText((int) (scope.getImage().getHeight() / 4) + "");
            } else {
                radiusInput.setText(scope.getCircle().getRadius() + "");
            }

            switch (scope.getColorScopeType()) {
                case AllColor:
                    allColorsRadio.setSelected(true);
                    break;
                case Color:
                    matchColorRadio.setSelected(true);
                    colorExcludedCheck.setSelected(false);
                    colorDistanceInput.setText(scope.getColorDistance() + "");
                    break;
                case ColorExcluded:
                    matchColorRadio.setSelected(true);
                    colorExcludedCheck.setSelected(true);
                    colorDistanceInput.setText(scope.getColorDistance() + "");
                    break;
                case Hue:
                    matchHueRadio.setSelected(true);
                    colorExcludedCheck.setSelected(false);
                    colorDistanceInput.setText(scope.getHueDistance() + "");
                    break;
                case HueExcluded:
                    matchHueRadio.setSelected(true);
                    colorExcludedCheck.setSelected(true);
                    colorDistanceInput.setText(scope.getHueDistance() + "");
                    break;
                default:
                    allColorsRadio.setSelected(true);
            }

            List<Color> colors = scope.getColors();
            if (colors != null && !colors.isEmpty()) {
                colorsBox.getItems().addAll(colors);
                colorsBox.setVisibleRowCount(15);
                colorsBox.getSelectionModel().select(0);
            }

            if (inScope.getOpacity() >= 0) {
                opacityBox.getSelectionModel().select(inScope.getOpacity());
            } else {
                opacityBox.getSelectionModel().select(0);
            }

            isSettingValue = false;

            showScope();

        } catch (Exception e) {
            logger.debug(e.toString());
            closeStage();
        }

    }

    @FXML
    private void setTransparent(ActionEvent event) {
        if (!colorsBox.getItems().contains(Color.TRANSPARENT)) {
            colorsBox.getItems().add(Color.TRANSPARENT);
            colorsBox.getSelectionModel().select(Color.TRANSPARENT);
            showScope();
        }
        colorsBox.setVisibleRowCount(20);
    }

    @FXML
    private void setBlack(ActionEvent event) {
        Color color = Color.BLACK;
        if (!colorsBox.getItems().contains(color)) {
            colorsBox.getItems().add(color);
            colorsBox.getSelectionModel().select(color);
            showScope();
        }
        colorsBox.setVisibleRowCount(20);
    }

    @FXML
    private void setWhite(ActionEvent event) {
        Color color = Color.WHITE;
        if (!colorsBox.getItems().contains(color)) {
            colorsBox.getItems().add(color);
            colorsBox.getSelectionModel().select(color);
            showScope();
        }
        colorsBox.setVisibleRowCount(20);
    }

    @FXML
    private void clearColors(ActionEvent event) {
        colorsBox.getItems().clear();
        showScope();
    }

    @FXML
    public void clickImage(MouseEvent event) {
        handleClick(imageView, event);
    }

    @FXML
    public void clickRef(MouseEvent event) {
        handleClick(scopeView, event);
    }

    public void handleClick(ImageView view, MouseEvent event) {
        try {
            final Image currentImage = imageView.getImage();
            if (currentImage == null || null == operationType) {
                imageView.setCursor(Cursor.OPEN_HAND);
                scopeView.setCursor(Cursor.OPEN_HAND);
                return;
            }

            int x = (int) Math.round(event.getX() * currentImage.getWidth() / view.getBoundsInLocal().getWidth());
            int y = (int) Math.round(event.getY() * currentImage.getHeight() / view.getBoundsInLocal().getHeight());
            imageView.setCursor(Cursor.HAND);
            scopeView.setCursor(Cursor.HAND);

            switch (operationType) {
                case Color:
                case Hue:
                    PixelReader pixelReader = currentImage.getPixelReader();
                    Color color = pixelReader.getColor(x, y);
                    if (!colorsBox.getItems().contains(color)) {
                        colorsBox.setVisibleRowCount(20);
                        colorsBox.getItems().add(color);
                        colorsBox.getSelectionModel().select(color);
                        showScope();
                    }
                    break;
                case Rectangle:
                    if (event.getButton() == MouseButton.PRIMARY) {
                        isSettingValue = true;
                        leftXInput.setText(x + "");
                        leftYInput.setText(y + "");
                        isSettingValue = false;

                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        isSettingValue = true;
                        rightXInput.setText(x + "");
                        rightYInput.setText(y + "");
                        isSettingValue = false;

                    }
                    checkRectangleValues();
                    break;
                case Circle:
                    if (event.getButton() == MouseButton.PRIMARY) {

                        isSettingValue = true;
                        centerXInput.setText(x + "");
                        centerYInput.setText(y + "");
                        isSettingValue = false;

                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        isSettingValue = true;
                        int cx = scope.getCircle().getCenterX();
                        int cy = scope.getCircle().getCenterY();
                        long r = Math.round(Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy)));
                        radiusInput.setText(r + "");
                        isSettingValue = false;

                    }
                    checkCircleValues();
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @FXML
    public void zoomIn() {
        imageView.setFitHeight(imageView.getFitHeight() * (1 + 5 / 100.0f));
        imageView.setFitWidth(imageView.getFitWidth() * (1 + 5 / 100.0f));
        scopeView.setFitHeight(scopeView.getFitHeight() * (1 + 5 / 100.0f));
        scopeView.setFitWidth(scopeView.getFitWidth() * (1 + 5 / 100.0f));
    }

    @FXML
    public void zoomOut() {
        imageView.setFitHeight(imageView.getFitHeight() * (1 - 5 / 100.0f));
        imageView.setFitWidth(imageView.getFitWidth() * (1 - 5 / 100.0f));
        scopeView.setFitHeight(scopeView.getFitHeight() * (1 - 5 / 100.0f));
        scopeView.setFitWidth(scopeView.getFitWidth() * (1 - 5 / 100.0f));

    }

    @FXML
    public void imageSize() {
        imageView.setFitHeight(scope.getImage().getWidth());
        imageView.setFitWidth(scope.getImage().getHeight());
        scopeView.setFitHeight(scope.getImage().getWidth());
        scopeView.setFitWidth(scope.getImage().getHeight());
    }

    @FXML
    public void paneSize() {
        imageView.setFitHeight(imagePane.getHeight() - 5);
        imageView.setFitWidth(imagePane.getWidth() - 1);
        scopeView.setFitHeight(imagePane.getHeight() - 5);
        scopeView.setFitWidth(imagePane.getWidth() - 1);
    }

    @Override
    protected void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        String key = event.getText();
        if (key == null || key.isEmpty()) {
            return;
        }
        if (event.isControlDown()) {
            switch (key) {
                case "a":
                case "A":
                    if (!okButton.isDisabled()) {
                        okAction();
                    }
                    break;
                case "1":
                    paneSize();
                    break;
                case "2":
                    imageSize();
                    break;
                case "3":
                    zoomIn();
                    break;
                case "4":
                    zoomOut();
                    break;
                default:
                    break;
            }
        }
    }

    private void showScope() {
        scope.setColors(colorsBox.getItems());
        showScopeText();
        if (isSettingValue || !areaValid
                || task != null && task.isRunning()) {
            return;
        }
        if (scope.getScopeType() == ImageScope.ScopeType.All) {
            scopeView.setImage(scope.getImage());
            return;
        }

        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image newImage = FxmlScopeTools.scopeImage(imageView.getImage(), scope);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            scopeView.setImage(newImage);
//                            popInformation();
                            showScopeText();
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

    private void showScopeText() {
        currentInput.setText(scope.getScopeText());
    }

    @FXML
    private void okAction() {
        scope.setImage(scopeView.getImage());
        if (scope.getAreaScopeType() == AreaScopeType.AllArea
                && scope.getColorScopeType() == ColorScopeType.AllColor) {
            scope.setScopeType(ImageScope.ScopeType.All);
        }
        closeStage();
        imageController.scopeDetermined(scope);
    }

    public ImageManufactureController getImageController() {
        return imageController;
    }

    public void setImageController(ImageManufactureController imageController) {
        this.imageController = imageController;
    }

    public ImageScope getScope() {
        return scope;
    }

    public void setScope(ImageScope scope) {
        this.scope = scope;
    }

}
