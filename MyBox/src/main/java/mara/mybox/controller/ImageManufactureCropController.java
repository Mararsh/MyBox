package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.ImageManufacture;
import mara.mybox.image.ImageScope.ScopeType;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureCropController extends ImageManufactureController {

    private ScopeType cropType;
    protected int centerX, centerY, radius;

    @FXML
    protected ToggleGroup shapeGroup;
    @FXML
    protected ToolBar cropBar;
    @FXML
    protected HBox setBox, colorBox;
    @FXML
    protected Button withdrawButton, clearButton, cutInsideButton, cutOutsideButton;

    public ImageManufactureCropController() {

    }

    @Override
    public void initializeNext2() {
        try {
            initCommon();
            initCropTab();
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
            tabPane.getSelectionModel().select(cropTab);
            if (values.getImageInfo() != null
                    && CommonValues.NoAlphaImages.contains(values.getImageInfo().getImageFormat())) {
                if (colorBox.getChildren().contains(transparentButton)) {
                    colorBox.getChildren().remove(transparentButton);

                }
                colorPicker.setValue(Color.WHITE);

            } else {
                if (!colorBox.getChildren().contains(transparentButton)) {
                    colorBox.getChildren().add(transparentButton);
                }
                colorPicker.setValue(Color.TRANSPARENT);

            }
            checkCropType();

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initCropTab() {
        try {

            shapeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkCropType();
                }
            });

            maskPolygonLine.getPoints().addListener(new ListChangeListener<Double>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends Double> change) {
                    if (cropType == ScopeType.Polygon) {
                        boolean closed = maskPolygonData.getSize() > 2;
                        cutOutsideButton.setDisable(!closed);
                        cutInsideButton.setDisable(!closed);
                    }
                }
            });

            FxmlControl.quickTooltip(cutOutsideButton, new Tooltip(getMessage("OK") + "\nENTER"));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkCropType() {
        try {
            setBox.getChildren().clear();
            promptLabel.setText("");
            pickColorButton.setSelected(false);

            initMaskControls(false);
            RadioButton selected = (RadioButton) shapeGroup.getSelectedToggle();
            if (getMessage("Rectangle").equals(selected.getText())) {
                cropType = ScopeType.Rectangle;
                initMaskRectangleLine(true);
                cutOutsideButton.setDisable(false);
                cutInsideButton.setDisable(false);

            } else if (getMessage("Circle").equals(selected.getText())) {
                cropType = ScopeType.Circle;
                initMaskCircleLine(true);
                cutOutsideButton.setDisable(false);
                cutInsideButton.setDisable(false);

            } else if (getMessage("Ellipse").equals(selected.getText())) {
                cropType = ScopeType.Ellipse;
                initMaskEllipseLine(true);
                cutOutsideButton.setDisable(false);
                cutInsideButton.setDisable(false);

            } else if (getMessage("Polygon").equals(selected.getText())) {
                cropType = ScopeType.Polygon;
                setBox.getChildren().addAll(clearButton, withdrawButton);
                initMaskPolygonLine(true);
                cutOutsideButton.setDisable(true);
                cutInsideButton.setDisable(true);
                promptLabel.setText(getMessage("PolygonComments"));

            }

        } catch (Exception e) {

        }
    }

    @FXML
    public void setTransparentAction() {
        colorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void setBlackAction() {
        colorPicker.setValue(Color.BLACK);
    }

    @FXML
    public void setWhiteAction() {
        colorPicker.setValue(Color.WHITE);
    }

    @FXML
    @Override
    public void polygonWithdrawAction() {
        maskPolygonData.removeLast();
        drawMaskPolygonLine();
    }

    @FXML
    @Override
    public void polygonClearAction() {
        maskPolygonData.clear();
        drawMaskPolygonLine();
    }

    @FXML
    public void cutInsideAction() {
        imageView.setCursor(Cursor.OPEN_HAND);
        task = new Task<Void>() {
            private Image newImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {

                switch (cropType) {
                    case Rectangle:
                        newImage = ImageManufacture.cropInsideFx(imageView.getImage(),
                                maskRectangleData, colorPicker.getValue());
                        break;
                    case Circle:
                        newImage = ImageManufacture.cropInsideFx(imageView.getImage(),
                                maskCircleData, colorPicker.getValue());
                        break;
                    case Ellipse:
                        newImage = ImageManufacture.cropInsideFx(imageView.getImage(),
                                maskEllipseData, colorPicker.getValue());
                        break;
                    case Polygon:
                        newImage = ImageManufacture.cropInsideFx(imageView.getImage(),
                                maskPolygonData, colorPicker.getValue());
                        break;
                    default:
                        newImage = null;
                        break;
                }
                if (task == null || task.isCancelled()) {
                    return null;
                }
                recordImageHistory(ImageOperationType.Crop, newImage);

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
                            values.setCurrentImage(newImage);
                            imageView.setImage(newImage);
                            setImageChanged(true);
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
        cutOutsideAction();
    }

    @FXML
    public void cutOutsideAction() {
        imageView.setCursor(Cursor.OPEN_HAND);
        task = new Task<Void>() {
            private Image newImage;
            private boolean ok;

            @Override
            protected Void call() throws Exception {

                switch (cropType) {
                    case Rectangle:
                        newImage = ImageManufacture.cropOutsideFx(imageView.getImage(),
                                maskRectangleData, colorPicker.getValue());
                        break;
                    case Circle:
                        newImage = ImageManufacture.cropOutsideFx(imageView.getImage(),
                                maskCircleData, colorPicker.getValue());
                        break;
                    case Ellipse:
                        newImage = ImageManufacture.cropOutsideFx(imageView.getImage(),
                                maskEllipseData, colorPicker.getValue());
                        break;
                    case Polygon:
                        newImage = ImageManufacture.cropOutsideFx(imageView.getImage(),
                                maskPolygonData, colorPicker.getValue());
                        break;
                    default:
                        newImage = null;
                        break;
                }
                if (task == null || task.isCancelled()) {
                    return null;
                }
                recordImageHistory(ImageOperationType.Crop, newImage);

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
                            values.setCurrentImage(newImage);
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

}
