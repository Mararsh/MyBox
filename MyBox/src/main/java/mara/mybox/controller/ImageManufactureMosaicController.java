package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureController;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageScope.ScopeType;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.fxml.ImageManufacture;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-11-05
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureMosaicController extends ImageManufactureController {

    private ScopeType shapeType;
    private boolean isMosaic;
    private int intensity;

    @FXML
    private ToggleGroup shapeGroup, typeGroup;
    @FXML
    private ImageView shapeTipsView;
    @FXML
    private ComboBox<String> intensityBox;
    @FXML
    private CheckBox excludeCheck;

    public ImageManufactureMosaicController() {
    }

    @Override
    public void initializeNext2() {
        try {
            initCommon();
            initMosaicTab();
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
            tabPane.getSelectionModel().select(mosaicTab);

            intensityBox.getItems().clear();
            List<String> sizeList = new ArrayList();
            int max = (int) Math.round(image.getWidth() / 10);
            int step = (int) Math.round(image.getWidth() / 100);
            for (int s = 10; s <= max; s += step) {
                sizeList.add(s + "");
            }
            intensityBox.getItems().addAll(sizeList);
            isSettingValues = false;

            intensityBox.getSelectionModel().select(3);
            checkShape();
            checkType();
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initMosaicTab() {
        try {
            shapeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkShape();
                }
            });

            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkType();
                }
            });

            intensityBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            intensity = v;
                            FxmlControl.setEditorNormal(intensityBox);
                        } else {
                            FxmlControl.setEditorBadStyle(intensityBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intensityBox);
                    }
                }
            });

            okButton.disableProperty().bind(
                    intensityBox.getEditor().styleProperty().isEqualTo(badStyle)
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkShape() {
        try {
            initMaskControls(false);
            promptLabel.setText("");

            RadioButton selected = (RadioButton) shapeGroup.getSelectedToggle();
            if (getMessage("Rectangle").equals(selected.getText())) {
                shapeType = ScopeType.Rectangle;
                initMaskRectangleLine(true);

            } else if (getMessage("Circle").equals(selected.getText())) {
                shapeType = ScopeType.Circle;
                initMaskCircleLine(true);

            } else if (getMessage("Ellipse").equals(selected.getText())) {
                shapeType = ScopeType.Ellipse;
                initMaskEllipseLine(true);

            } else if (getMessage("Polygon").equals(selected.getText())) {
                shapeType = ScopeType.Polygon;
                initMaskPolygonLine(true);
                promptLabel.setText(getMessage("PolygonComments"));

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkType() {
        try {
            RadioButton selected = (RadioButton) typeGroup.getSelectedToggle();
            isMosaic = getMessage("Mosaic").equals(selected.getText());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (okButton.isDisabled() || shapeType == null || intensity <= 0) {
            return;
        }

        task = new Task<Void>() {
            private Image newImage;

            @Override
            protected Void call() throws Exception {

                switch (shapeType) {
                    case Rectangle:
                        newImage = ImageManufacture.makeMosaic(imageView.getImage(),
                                maskRectangleData, intensity, isMosaic, excludeCheck.isSelected());
                        break;
                    case Circle:
                        newImage = ImageManufacture.makeMosaic(imageView.getImage(),
                                maskCircleData, intensity, isMosaic, excludeCheck.isSelected());
                        break;
                    case Ellipse:
                        newImage = ImageManufacture.makeMosaic(imageView.getImage(),
                                maskEllipseData, intensity, isMosaic, excludeCheck.isSelected());
                        break;
                    case Polygon:
                        newImage = ImageManufacture.makeMosaic(imageView.getImage(),
                                maskPolygonData, intensity, isMosaic, excludeCheck.isSelected());
                        break;
                    default:
                        return null;
                }

                if (task == null || task.isCancelled()) {
                    return null;
                }
                recordImageHistory(ImageOperationType.Mosaic, newImage);

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(imageView.getImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                        switch (shapeType) {
                            case Rectangle: {
                                double offset = maskRectangleData.getWidth() / 4;
                                maskRectangleData = maskRectangleData.move(offset);
                                drawMaskRectangleLine();
                                break;
                            }
                            case Circle: {
                                double offset = maskCircleData.getRadius() / 4;
                                maskCircleData = maskCircleData.move(offset);
                                drawMaskCircleLine();
                                break;
                            }
                            case Ellipse: {
                                double offset = maskEllipseData.getRadiusX() / 4;
                                maskEllipseData = maskEllipseData.move(offset);
                                drawMaskEllipseLine();
                                break;
                            }
                            case Polygon: {
                                double offset = imageView.getFitWidth() / 10;
                                maskPolygonData = maskPolygonData.move(offset);
                                drawMaskPolygonLine();
                                break;
                            }
                            default:
                                break;
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

}
