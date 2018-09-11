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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.util.Callback;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.ImageScope;
import mara.mybox.objects.ImageScope.AreaScopeType;
import mara.mybox.objects.ImageScope.OperationType;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.tools.FxmlImageTools;

/**
 * @Author Mara
 * @CreateDate 2018-8-1
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageScopeController extends BaseController {

    private ImageManufactureController imageController;
    private ImageScope imageScope;
    private int pixelPickingType, leftX, leftY, rightX, rightY, centerX, centerY, radius;
    private int colorDistance, hueDistance;
    private boolean areaValid, isSettingValue;
    private double opacity;

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
    private Button okButton;
    @FXML
    private ToggleGroup distanceGroup, areaGroup, colorGroup;
    @FXML
    private TextField leftXInput, leftYInput, rightXInput, rightYInput, colorDistanceInput, hueDistanceInput;
    @FXML
    private TextField centerXInput, centerYInput, radiusInput;
    @FXML
    private RadioButton allColorsRadio, selectedColorsRadio, allAreaRadio, selectedRectangleRadio, selectedCircleRadio;
    @FXML
    private RadioButton colorDistanceRadio, hueDistanceRadio;
    @FXML
    private SplitPane splitPane;
    @FXML
    private ScrollPane scrollPane, refPane;
    @FXML
    private ImageView imageView, refView;
    @FXML
    private Label titleLabel, opacityLabel, opacityComments;

    @Override
    protected void initializeNext() {
        try {

            initAreaTab();

            initColorTab();

            List<Double> values = Arrays.asList(0.1, 0.5, 0.2, 0.3, 0.6, 0.4, 0.7, 0.8, 0.9, 1.0);
            opacityBox.getItems().addAll(values);
            opacityBox.setVisibleRowCount(values.size());
            opacityBox.valueProperty().addListener(new ChangeListener<Double>() {
                @Override
                public void changed(ObservableValue ov, Double oldValue, Double newValue) {
                    if (newValue >= 0 && newValue <= 1.0) {
                        opacity = newValue;
                        if (!isSettingValue) {
                            showScope();
                        }
                    } else {
                        opacity = 0.1;
                    }
                }
            });

            okButton.disableProperty().bind(
                    leftXInput.styleProperty().isEqualTo(badStyle)
                            .or(leftYInput.styleProperty().isEqualTo(badStyle))
                            .or(rightXInput.styleProperty().isEqualTo(badStyle))
                            .or(rightYInput.styleProperty().isEqualTo(badStyle))
                            .or(colorDistanceInput.styleProperty().isEqualTo(badStyle))
                            .or(hueDistanceInput.styleProperty().isEqualTo(badStyle))
                            .or(colorsBox.visibleRowCountProperty().isEqualTo(0)));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initAreaTab() {
        try {
            if (AppVaribles.showComments) {
                Tooltip stips = new Tooltip(getMessage("ScopeComments"));
                stips.setFont(new Font(16));
                FxmlTools.setComments(areaBar, stips);
                FxmlTools.setComments(rectangleExcludedCheck, stips);
                FxmlTools.setComments(circleExcludedCheck, stips);
            }

            areaGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    imageView.setCursor(Cursor.OPEN_HAND);
                    pixelPickingType = ImageManufactureController.PixelPickingType.None;
                    RadioButton selected = (RadioButton) areaGroup.getSelectedToggle();
                    if (getMessage("AllArea").equals(selected.getText())) {
                        imageScope.setAreaScopeType(AreaScopeType.AllArea);
                        rectangleBar.setDisable(true);
                        circleBar.setDisable(true);
                        leftXInput.setStyle(null);
                        leftYInput.setStyle(null);
                        rightXInput.setStyle(null);
                        rightYInput.setStyle(null);
                        centerXInput.setStyle(null);
                        centerYInput.setStyle(null);
                        radiusInput.setStyle(null);
                        areaValid = true;
                    } else if (getMessage("SelectedRectangle").equals(selected.getText())) {
                        imageScope.setAreaScopeType(AreaScopeType.Rectangle);
                        rectangleBar.setDisable(false);
                        circleBar.setDisable(true);
                        centerXInput.setStyle(null);
                        centerYInput.setStyle(null);
                        radiusInput.setStyle(null);
                        boolean alreadyInSetting = isSettingValue;
                        isSettingValue = true;
                        checkRectangleValues();
                        if (!alreadyInSetting) {
                            isSettingValue = false;
                        }
                    } else if (getMessage("SelectedCircle").equals(selected.getText())) {
                        imageScope.setAreaScopeType(AreaScopeType.Circle);
                        rectangleBar.setDisable(true);
                        circleBar.setDisable(false);
                        leftXInput.setStyle(null);
                        leftYInput.setStyle(null);
                        rightXInput.setStyle(null);
                        rightYInput.setStyle(null);
                        boolean alreadyInSetting = isSettingValue;
                        isSettingValue = true;
                        checkCircleValues();
                        if (!alreadyInSetting) {
                            isSettingValue = false;
                        }
                    }
                    if (!isSettingValue) {
                        showScope();
                    }

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
                    if (!isSettingValue) {
                        showScope();
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
                    if (!isSettingValue) {
                        showScope();
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initColorTab() {
        try {
            if (AppVaribles.showComments) {
                Tooltip stips = new Tooltip(getMessage("ScopeComments"));
                stips.setFont(new Font(16));
                FxmlTools.setComments(colorBar, stips);
                FxmlTools.setComments(colorExcludedCheck, stips);
            }

            colorGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    imageView.setCursor(Cursor.OPEN_HAND);
                    pixelPickingType = ImageManufactureController.PixelPickingType.None;
                    RadioButton selected = (RadioButton) colorGroup.getSelectedToggle();
                    if (getMessage("AllColors").equals(selected.getText())) {
                        imageScope.setAllColors(true);
                        colorBar1.setDisable(true);
                        colorBar2.setDisable(true);
                        colorDistanceInput.setStyle(null);
                        hueDistanceInput.setStyle(null);
                        colorsBox.setVisibleRowCount(20);
                    } else {
                        imageScope.setAllColors(false);
                        colorBar1.setDisable(false);
                        colorBar2.setDisable(false);
                        boolean alreadyInSetting = isSettingValue;
                        isSettingValue = true;
                        checkMatchType();
//                        if (colorsBox.getItems().isEmpty()) {
//                            colorsBox.setVisibleRowCount(0);
//                        }
                        if (!alreadyInSetting) {
                            isSettingValue = false;
                        }
                    }
                    if (!isSettingValue) {
                        showScope();
                    }
                }
            });

            distanceGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkMatchType();
                }
            });

            FxmlTools.quickTooltip(colorDistanceInput, new Tooltip("0 ~ 255"));
            FxmlTools.quickTooltip(hueDistanceInput, new Tooltip("0 ~ 360"));
            if (AppVaribles.showComments) {
                Tooltip tips = new Tooltip(getMessage("ColorMatchComments"));
                tips.setFont(new Font(16));
                FxmlTools.setComments(colorBar1, tips);
            }

            colorDistanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkColorDistance();
                }
            });

            hueDistanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkHueDistance();
                }
            });

            colorsBox.setCellFactory(new Callback<ListView<Color>, ListCell<Color>>() {
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

            colorExcludedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable,
                        Boolean oldValue, Boolean newValue) {
                    if (!isSettingValue) {
                        showScope();
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkRectangleValues() {
        final Image currentImage = imageView.getImage();
        areaValid = true;

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

        if (imageScope.getAreaScopeType() == AreaScopeType.AllArea) {
            leftXInput.setStyle(null);
            leftYInput.setStyle(null);
            rightXInput.setStyle(null);
            rightYInput.setStyle(null);
            return;
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
            if (!isSettingValue) {
                showScope();
            }
        } else {
            FxmlTools.popError(imageView, getMessage("InvalidRectangle"));
        }

    }

    private void checkCircleValues() {
        final Image currentImage = imageView.getImage();
        areaValid = true;

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

        try {
            radius = Integer.valueOf(radiusInput.getText());
            radiusInput.setStyle(null);
            if (radius >= 0
                    && radius <= currentImage.getHeight() && radius <= currentImage.getWidth()) {
                radiusInput.setStyle(null);
            } else {
                radiusInput.setStyle(badStyle);
                areaValid = false;
            }
        } catch (Exception e) {
            radiusInput.setStyle(badStyle);
            areaValid = false;
        }

        if (imageScope.getAreaScopeType() == AreaScopeType.AllArea) {
            centerXInput.setStyle(null);
            centerYInput.setStyle(null);
            radiusInput.setStyle(null);
            areaValid = true;
            return;
        }

        if (areaValid) {
            if (!isSettingValue) {
                showScope();
            }
        } else {
            FxmlTools.popError(imageView, getMessage("InvalidCircle"));
        }

    }

    private void checkMatchType() {
        RadioButton selected = (RadioButton) distanceGroup.getSelectedToggle();
        if (getMessage("ColorDistance").equals(selected.getText())) {
            imageScope.setMatchColor(true);
            colorDistanceInput.setDisable(false);
            hueDistanceInput.setDisable(true);
            hueDistanceInput.setStyle(null);
            checkColorDistance();

        } else if (getMessage("HueDistance").equals(selected.getText())) {
            imageScope.setMatchColor(false);
            hueDistanceInput.setDisable(false);
            colorDistanceInput.setDisable(true);
            colorDistanceInput.setStyle(null);
            checkHueDistance();
        }

    }

    private boolean checkColorDistance() {
        try {
            colorDistance = Integer.valueOf(colorDistanceInput.getText());
            if (colorDistance >= 0 && colorDistance <= 255) {
                colorDistanceInput.setStyle(null);
                if (!isSettingValue) {
                    showScope();
                }
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

    private boolean checkHueDistance() {
        try {
            hueDistance = Integer.valueOf(hueDistanceInput.getText());
            if (hueDistance >= 0 && hueDistance <= 360) {
                hueDistanceInput.setStyle(null);
                if (!isSettingValue) {
                    showScope();
                }
                return true;
            } else {
                hueDistanceInput.setStyle(badStyle);
                return false;
            }
        } catch (Exception e) {
            hueDistanceInput.setStyle(badStyle);
            return false;
        }
    }

    public void loadImage(ImageManufactureController controller,
            ImageScope imageScope, String title) {
        try {
            this.imageController = controller;
            this.imageScope = imageScope;
            if (controller == null || imageScope == null || imageScope.getImage() == null) {
                getMyStage().close();
                return;
            }
            titleLabel.setText(title);
            getMyStage().setTitle(title);

            imageView.setPreserveRatio(true);
            refView.setPreserveRatio(true);
            if (scrollPane.getHeight() < imageScope.getImage().getHeight()) {
                paneSize();
            } else {
                imageSize();
            }
            isSettingValue = true;
            imageView.setImage(imageScope.getImage());
            refView.setImage(imageScope.getImage());

            List<Color> colors = imageScope.getColors();
            if (colors != null && !colors.isEmpty()) {
                colorsBox.getItems().addAll(colors);
                colorsBox.setVisibleRowCount(20);
                colorsBox.getSelectionModel().select(0);
            }
            if (imageScope.isMatchColor()) {
                colorDistanceRadio.setSelected(true);
            } else {
                hueDistanceRadio.setSelected(true);
            }
            colorDistanceInput.setText(imageScope.getColorDistance() + "");
            hueDistanceInput.setText(imageScope.getHueDistance() + "");
            colorExcludedCheck.setSelected(imageScope.isColorExcluded());
            if (imageScope.isAllColors()) {
                allColorsRadio.setSelected(true);
            } else {
                selectedColorsRadio.setSelected(true);
            }

            switch (imageScope.getAreaScopeType()) {
                case AreaScopeType.AllArea:
                    allAreaRadio.setSelected(true);
                    break;
                case AreaScopeType.Rectangle:
                    selectedRectangleRadio.setSelected(true);
                    break;
                case AreaScopeType.Circle:
                    selectedCircleRadio.setSelected(true);
                    break;
                default:
                    allAreaRadio.setSelected(true);
            }
            if (imageScope.getRightX() < 0) {
                rightXInput.setText((int) (imageScope.getImage().getWidth() * 3 / 4) + "");
            } else {
                rightXInput.setText(imageScope.getRightX() + "");
            }
            if (imageScope.getRightY() < 0) {
                rightYInput.setText((int) (imageScope.getImage().getHeight() * 3 / 4) + "");
            } else {
                rightYInput.setText(imageScope.getRightY() + "");
            }
            if (imageScope.getLeftX() < 0) {
                leftXInput.setText((int) (imageScope.getImage().getWidth() / 4) + "");
            } else {
                leftXInput.setText(imageScope.getLeftX() + "");
            }
            if (imageScope.getLeftY() < 0) {
                leftYInput.setText((int) (imageScope.getImage().getHeight() / 4) + "");
            } else {
                leftYInput.setText(imageScope.getLeftY() + "");
            }
            rectangleExcludedCheck.setSelected(imageScope.isRectangleExcluded());

            if (imageScope.getCenterX() < 0) {
                centerXInput.setText((int) (imageScope.getImage().getWidth() / 2) + "");
            } else {
                centerXInput.setText(imageScope.getCenterX() + "");
            }
            if (imageScope.getCenterY() < 0) {
                centerYInput.setText((int) (imageScope.getImage().getHeight() / 2) + "");
            } else {
                centerYInput.setText(imageScope.getCenterY() + "");
            }
            if (imageScope.getRadius() < 0) {
                radiusInput.setText((int) (imageScope.getImage().getHeight() / 4) + "");
            } else {
                radiusInput.setText(imageScope.getRadius() + "");
            }
            circleExcludedCheck.setSelected(imageScope.isCircleExcluded());

            if (imageScope.getOpacity() >= 0) {
                opacityBox.getSelectionModel().select(imageScope.getOpacity());
            } else {
                opacityBox.getSelectionModel().select(0);
            }
            imageScope.setIndicateScope(true);

            switch (imageScope.getOperationType()) {
                case OperationType.ReplaceColor:
//                    allColorsRadio.setVisible(false);
                case OperationType.Color:
                case OperationType.Filters:
                    tabPane.getSelectionModel().select(colorTab);
                    break;
                case OperationType.Crop:
                    tabPane.getSelectionModel().select(areaTab);
                    colorTab.setDisable(true);
                    allAreaRadio.setVisible(false);
                    break;
                default:
            }

            isSettingValue = false;

            if (imageScope.getAreaScopeType() != AreaScopeType.AllArea) {
                showScope();
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @FXML
    private void setTransparent(ActionEvent event) {
        Color alpha = new Color(0, 0, 0, 0);
        if (!colorsBox.getItems().contains(alpha)) {
            colorsBox.getItems().add(alpha);
            colorsBox.getSelectionModel().select(alpha);
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
    private void pickColors(ActionEvent event) {
        pixelPickingType = ImageManufactureController.PixelPickingType.OriginalColor;
        imageView.setCursor(Cursor.HAND);
        refView.setCursor(Cursor.HAND);
        FxmlTools.popInformation(imageView, getMessage("PickColorComments"));
    }

    @FXML
    private void clearColors(ActionEvent event) {
        colorsBox.getItems().clear();
        showScope();
//        colorsBox.setVisibleRowCount(0);
    }

    @FXML
    private void pickLeft(ActionEvent event) {
        pixelPickingType = ImageManufactureController.PixelPickingType.RectangleLeft;
        imageView.setCursor(Cursor.HAND);
        refView.setCursor(Cursor.HAND);
        FxmlTools.popInformation(imageView, getMessage("PickPositionComments"));
    }

    @FXML
    private void pickRight(ActionEvent event) {
        pixelPickingType = ImageManufactureController.PixelPickingType.RectangleRight;
        imageView.setCursor(Cursor.HAND);
        refView.setCursor(Cursor.HAND);
        FxmlTools.popInformation(imageView, getMessage("PickPositionComments"));
    }

    @FXML
    private void pickCenter(ActionEvent event) {
        pixelPickingType = ImageManufactureController.PixelPickingType.CircleCenter;
        imageView.setCursor(Cursor.HAND);
        refView.setCursor(Cursor.HAND);
        FxmlTools.popInformation(imageView, getMessage("PickCenterComments"));
    }

    @FXML
    private void setRadius(ActionEvent event) {
        pixelPickingType = ImageManufactureController.PixelPickingType.CircleRadius;
        imageView.setCursor(Cursor.HAND);
        refView.setCursor(Cursor.HAND);
        FxmlTools.popInformation(imageView, getMessage("SetRadiusComments"));
    }

    @FXML
    public void clickImage(MouseEvent event) {
        try {
            final Image currentImage = imageView.getImage();
            if (currentImage == null || event.getButton() == MouseButton.SECONDARY
                    || pixelPickingType == ImageManufactureController.PixelPickingType.None) {

                pixelPickingType = ImageManufactureController.PixelPickingType.None;
                imageView.setCursor(Cursor.OPEN_HAND);
                refView.setCursor(Cursor.OPEN_HAND);
                return;
            }

            int x = (int) Math.round(event.getX() * currentImage.getWidth() / imageView.getBoundsInLocal().getWidth());
            int y = (int) Math.round(event.getY() * currentImage.getHeight() / imageView.getBoundsInLocal().getHeight());

            handleClick(x, y);

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @FXML
    public void clickRef(MouseEvent event) {
        try {
            final Image currentImage = refView.getImage();
            if (currentImage == null || event.getButton() == MouseButton.SECONDARY
                    || pixelPickingType == ImageManufactureController.PixelPickingType.None) {

                pixelPickingType = ImageManufactureController.PixelPickingType.None;
                imageView.setCursor(Cursor.OPEN_HAND);
                refView.setCursor(Cursor.OPEN_HAND);
                return;
            }

            int x = (int) Math.round(event.getX() * currentImage.getWidth() / refView.getBoundsInLocal().getWidth());
            int y = (int) Math.round(event.getY() * currentImage.getHeight() / refView.getBoundsInLocal().getHeight());

            handleClick(x, y);

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    public void handleClick(int x, int y) {
        try {
            final Image currentImage = imageView.getImage();
            if (pixelPickingType == ImageManufactureController.PixelPickingType.OriginalColor) {

                PixelReader pixelReader = currentImage.getPixelReader();
                Color color = pixelReader.getColor(x, y);
                if (!colorsBox.getItems().contains(color)) {
                    colorsBox.setVisibleRowCount(20);
                    colorsBox.getItems().add(color);
                    colorsBox.getSelectionModel().select(color);
                    showScope();
                }
                FxmlTools.popInformation(imageView, getMessage("ContinueClickColor"));

            } else if (pixelPickingType == ImageManufactureController.PixelPickingType.RectangleLeft) {

                isSettingValue = true;
                leftXInput.setText(x + "");
                leftYInput.setText(y + "");
                isSettingValue = false;

                if (!areaValid) {
                    FxmlTools.popError(imageView, getMessage("InvalidRectangle"));
                } else if (task == null || !task.isRunning()) {
                    showScope();
                    FxmlTools.popInformation(imageView, getMessage("ContinueClickLeft"));
                } else {
                    FxmlTools.popInformation(imageView, getMessage("PickPositionComments"));
                }

            } else if (pixelPickingType == ImageManufactureController.PixelPickingType.RectangleRight) {

                isSettingValue = true;
                rightXInput.setText(x + "");
                rightYInput.setText(y + "");
                isSettingValue = false;

                if (!areaValid) {
                    FxmlTools.popError(imageView, getMessage("InvalidRectangle"));
                } else if (task == null || !task.isRunning()) {
                    showScope();
                    FxmlTools.popInformation(imageView, getMessage("ContinueClickRight"));
                } else {
                    FxmlTools.popInformation(imageView, getMessage("PickPositionComments"));
                }

            } else if (pixelPickingType == ImageManufactureController.PixelPickingType.CircleCenter) {

                isSettingValue = true;
                centerXInput.setText(x + "");
                centerYInput.setText(y + "");
                isSettingValue = false;

                if (!areaValid) {
                    FxmlTools.popError(imageView, getMessage("InvalidCircle"));
                } else if (task == null || !task.isRunning()) {
                    showScope();
                    FxmlTools.popInformation(imageView, getMessage("ContinueClickCenter"));
                } else {
                    FxmlTools.popInformation(imageView, getMessage("PickCenterComments"));
                }

            } else if (pixelPickingType == ImageManufactureController.PixelPickingType.CircleRadius) {

                isSettingValue = true;
                long r = Math.round(Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY)));
                radiusInput.setText(r + "");
                isSettingValue = false;

                if (!areaValid) {
                    FxmlTools.popError(imageView, getMessage("InvalidCircle"));
                } else if (task == null || !task.isRunning()) {
                    showScope();
                    FxmlTools.popInformation(imageView, getMessage("ContinueClickRadius"));
                } else {
                    FxmlTools.popInformation(imageView, getMessage("SetRadiusComments"));
                }

            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @FXML
    private void okAction(ActionEvent event) {
        writeValues();

        imageScope.setImage(imageView.getImage());

        getMyStage().close();

        imageController.scopeDetermined(imageScope);

    }

    @FXML
    public void zoomIn() {
        imageView.setFitHeight(imageView.getFitHeight() * (1 + 5 / 100.0f));
        imageView.setFitWidth(imageView.getFitWidth() * (1 + 5 / 100.0f));
        refView.setFitHeight(refView.getFitHeight() * (1 + 5 / 100.0f));
        refView.setFitWidth(refView.getFitWidth() * (1 + 5 / 100.0f));
    }

    @FXML
    public void zoomOut() {
        imageView.setFitHeight(imageView.getFitHeight() * (1 - 5 / 100.0f));
        imageView.setFitWidth(imageView.getFitWidth() * (1 - 5 / 100.0f));
        refView.setFitHeight(refView.getFitHeight() * (1 - 5 / 100.0f));
        refView.setFitWidth(refView.getFitWidth() * (1 - 5 / 100.0f));

    }

    @FXML
    public void imageSize() {
        imageView.setFitHeight(imageScope.getImage().getWidth());
        imageView.setFitWidth(imageScope.getImage().getHeight());
        refView.setFitHeight(imageScope.getImage().getWidth());
        refView.setFitWidth(imageScope.getImage().getHeight());
    }

    @FXML
    public void paneSize() {
        imageView.setFitHeight(scrollPane.getHeight() - 5);
        imageView.setFitWidth(scrollPane.getWidth() - 1);
        refView.setFitHeight(refPane.getHeight() - 5);
        refView.setFitWidth(refPane.getWidth() - 1);
    }

    private void writeValues() {
        List<Color> colors = colorsBox.getItems();
        if (colors == null || colors.isEmpty()) {
            if (!imageScope.isAllColors()) {
                return;
            }
        }
        imageScope.setColors(colors);
        imageScope.setColorExcluded(colorExcludedCheck.isSelected());
        imageScope.setColorDistance(colorDistance);
        imageScope.setHueDistance(hueDistance);

        imageScope.setLeftX(leftX);
        imageScope.setLeftY(leftY);
        imageScope.setRightX(rightX);
        imageScope.setRightY(rightY);
        imageScope.setOpacity(opacity);
        imageScope.setRectangleExcluded(rectangleExcludedCheck.isSelected());

        imageScope.setCenterX(centerX);
        imageScope.setCenterY(centerY);
        imageScope.setRadius(radius);
        imageScope.setCircleExcluded(circleExcludedCheck.isSelected());

    }

    private void showScope() {
        if (isSettingValue || !areaValid
                || task != null && task.isRunning()) {
            return;
        }
        if (imageScope.isAll() || !imageScope.isIndicateScope()) {
            imageView.setImage(imageScope.getImage());
            return;
        }

        writeValues();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    final Image newImage = FxmlImageTools.indicateScopeFx(imageScope.getImage(), imageScope);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImage(newImage);
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

    public ImageManufactureController getImageController() {
        return imageController;
    }

    public void setImageController(ImageManufactureController imageController) {
        this.imageController = imageController;
    }

    public ImageScope getImageScope() {
        return imageScope;
    }

    public void setImageScope(ImageScope imageScope) {
        this.imageScope = imageScope;
    }

}
