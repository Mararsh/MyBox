package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.bufferedimage.ImageMosaic.MosaicType;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolylines;
import mara.mybox.data.ShapeStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.EliminateTools;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @License Apache License Version 2.0
 */
public class ImageManufactureEliminateController extends ImageManufactureOperationController {

    protected int strokeWidth, intensity;
    protected ChangeListener<Boolean> shapeDataChangeListener;

    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton eraserRadio, mosaicRadio, frostedRadio;
    @FXML
    protected FlowPane strokeWidthPane, intensityPane;
    @FXML
    protected VBox setBox;
    @FXML
    protected ComboBox<String> strokeWidthSelector, intensitySelector;
    @FXML
    protected Label commentsLabel;
    @FXML
    protected CheckBox coordinatePenCheck;

    @Override
    public void initPane() {
        try {
            super.initPane();

            setBox.getChildren().clear();
            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue o, Toggle ov, Toggle nv) {
                    checkType();
                }
            });

            shapeDataChangeListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue o, Boolean ov, Boolean nv) {
                    redraw();
                }
            };

            int imageWidth = (int) imageView.getImage().getWidth();
            strokeWidthSelector.getItems().clear();
            List<String> ws = new ArrayList<>();
            ws.addAll(Arrays.asList("3", "0", "1", "2", "5", "8", "10", "15", "25", "30", "50", "80", "100", "150", "200", "300", "500"));
            int max = imageWidth / 20;
            int step = max / 10;
            for (int w = 10; w < max; w += step) {
                if (!ws.contains(w + "")) {
                    ws.add(w + "");
                }
            }
            strokeWidthSelector.getItems().addAll(ws);
            strokeWidth = UserConfig.getInt(interfaceName + "StrokeWidth", 50);
            if (strokeWidth <= 0) {
                strokeWidth = 50;
            }
            strokeWidthSelector.setValue(strokeWidth + "");
            strokeWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            strokeWidth = v;
                            UserConfig.setInt(interfaceName + "StrokeWidth", v);
                            if (editor.shapeStyle == null) {
                                editor.shapeStyle = new ShapeStyle(interfaceName);
                            }
                            editor.shapeStyle.setStrokeWidth(strokeWidth);
                            redraw();
                            ValidationTools.setEditorNormal(strokeWidthSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(strokeWidthSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(strokeWidthSelector);
                    }
                }
            });

            coordinatePenCheck.setSelected(UserConfig.getBoolean(baseName + "PenCoordinate", false));
            coordinatePenCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    UserConfig.setBoolean(baseName + "PenCoordinate", coordinatePenCheck.isSelected());
                }
            });

            intensitySelector.getItems().addAll(Arrays.asList("20", "50", "10", "5", "80", "100", "15", "20", "60"));
            intensity = UserConfig.getInt(interfaceName + "Intensity", 20);
            if (intensity <= 0) {
                intensity = 20;
            }
            intensitySelector.setValue(intensity + "");
            intensitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            intensity = v;
                            ValidationTools.setEditorNormal(intensitySelector);
                            UserConfig.setInt(interfaceName + "Intensity", v);
                            redraw();
                        } else {
                            ValidationTools.setEditorBadStyle(intensitySelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(intensitySelector);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    protected void paneExpanded() {
        checkType();
        editor.maskShapeDataChanged.addListener(shapeDataChangeListener);
    }

    @Override
    protected void paneUnexpanded() {
        editor.maskShapeDataChanged.removeListener(shapeDataChangeListener);
    }

    private void checkType() {
        try {
            editor.resetImagePane();
            editor.imageTab();
            editor.shapeStyle = new ShapeStyle(interfaceName);
            editor.shapeStyle.setStrokeWidth(strokeWidth);
            editor.showAnchors = false;
            editor.popAnchorMenu = false;
            editor.addPointWhenClick = false;
            editor.popShapeMenu = false;
            editor.supportPath = false;

            maskView.setImage(imageView.getImage());
            maskView.setOpacity(1);
            maskView.setVisible(true);
            imageView.setVisible(false);
            imageView.toBack();

            editor.maskPolylinesData = new DoublePolylines();
            editor.showMaskPolylines();
            setBox.getChildren().clear();

            if (eraserRadio.isSelected()) {
                commentsLabel.setText(message("ShapePolylinesTips") + "\n" + message("ImageEraserComments"));

            } else {
                setBox.getChildren().add(intensityPane);
                commentsLabel.setText(message("ShapePolylinesTips"));
            }

            refreshStyle(thisPane);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void redraw() {
        if (isSettingValues || imageView == null || imageView.getImage() == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image newImage;

            @Override
            protected boolean handle() {
                if (eraserRadio.isSelected()) {
                    newImage = EliminateTools.drawErase(imageView.getImage(),
                            editor.maskPolylinesData, strokeWidth);
                } else if (frostedRadio.isSelected()) {
                    newImage = EliminateTools.drawMosaic(imageView.getImage(),
                            editor.maskPolylinesData, MosaicType.FrostedGlass, strokeWidth, intensity);

                } else if (mosaicRadio.isSelected()) {
                    newImage = EliminateTools.drawMosaic(imageView.getImage(),
                            editor.maskPolylinesData, MosaicType.Mosaic, strokeWidth, intensity);

                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                if (isCancelled()) {
                    return;
                }
                maskView.setImage(newImage);
                maskView.setOpacity(1);
                maskView.setVisible(true);
                imageView.setVisible(false);
                imageView.toBack();
                editor.drawMaskPolylines();
                editor.hideMaskShape();
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void withdrawAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        editor.maskPolylinesData.removeLastLine();
        redraw();
    }

    @FXML
    @Override
    public void cancelAction() {
        withdrawAction();
    }

    @FXML
    @Override
    public void clearAction() {
        checkType();
    }

    @FXML
    @Override
    public void okAction() {
        if (okButton.isDisabled()) {
            return;
        }
        editor.popSuccessful();
        editor.updateImage(ImageOperation.Eliminate,
                ((RadioButton) typeGroup.getSelectedToggle()).getText(),
                null, maskView.getImage(), 0);
    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        mousePoint(event);
    }

    @FXML
    @Override
    public void mouseDragged(MouseEvent event) {
        mousePoint(event);
    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
        mousePoint(event);
    }

    public void mousePoint(MouseEvent event) {
        if (imageView == null || imageView.getImage() == null || editor.isPickingColor) {
            return;
        }
        if (coordinatePenCheck.isSelected()) {
            DoublePoint p = ImageViewTools.getImageXY(event, imageView);
            editor.showXY(event, p);
        }
    }

    @Override
    protected void resetOperationPane() {
        checkType();
    }

}
