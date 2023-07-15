package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import mara.mybox.bufferedimage.ImageMosaic.MosaicType;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.EliminateTools;
import mara.mybox.fximage.FxImageTools;
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
    protected DoublePoint lastPoint;
    protected List<Line> currentLine;

    @FXML
    protected ToggleGroup typeGroup;
    @FXML
    protected RadioButton eraserRadio, mosaicRadio, frostedRadio, shapeCircleRadio, shapeRectangleRadio;
    @FXML
    protected FlowPane strokeWidthPane, intensityPane, shapePane;
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

            lastPoint = null;
            setBox.getChildren().clear();
            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkType();
                }
            });

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
                            UserConfig.setInt(interfaceName + "StrokeWidth", strokeWidth);
                            erase();
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
    }

    @Override
    protected void paneUnexpanded() {
    }

    private void checkType() {
        try {
            editor.resetImagePane();
            editor.imageTab();
            editor.clearMask();
            maskView.setImage(imageView.getImage());
            maskView.setOpacity(1);
            maskView.setVisible(true);
            imageView.setVisible(false);
            imageView.toBack();
            clearErase();

            if (eraserRadio.isSelected()) {
                editor.showMaskLines();
                setBox.getChildren().clear();
                withdrawButton.setVisible(true);
                commentsLabel.setText(message("PenLinesTips") + "\n" + message("ImageEraserComments"));

            } else if (frostedRadio.isSelected()) {
                setBox.getChildren().setAll(intensityPane, shapePane);
                withdrawButton.setVisible(false);
                commentsLabel.setText(message("PenMosaicTips"));

            } else if (mosaicRadio.isSelected()) {
                setBox.getChildren().setAll(intensityPane, shapePane);
                withdrawButton.setVisible(false);
                commentsLabel.setText(message("PenMosaicTips"));

            }

            refreshStyle(thisPane);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void mosaic(MosaicType mosaicType, int x, int y) {
        if (isSettingValues || mosaicType == null
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image newImage;

            @Override
            protected boolean handle() {
                DoubleShape shape;
                if (shapeCircleRadio.isSelected()) {
                    shape = new DoubleCircle(x, y, strokeWidth / 2);
                } else {
                    int w = strokeWidth / 2 + ((strokeWidth % 2 == 0) ? 0 : 1);
                    shape = new DoubleRectangle(x - w, y - w, x + w, y + w);
                }
                Image image;
                if (maskView.getImage() == null) {
                    image = imageView.getImage();
                } else {
                    image = maskView.getImage();
                }
                newImage = FxImageTools.makeMosaic(image,
                        shape, intensity, mosaicType == MosaicType.Mosaic, false);
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                maskView.setImage(newImage);
            }

        };
        start(task);
    }

    public void erase() {
        if (isSettingValues || imageView == null || imageView.getImage() == null
                || !eraserRadio.isSelected()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = EliminateTools.drawErase(imageView.getImage(), editor.maskLinesData, strokeWidth);
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
                clearErase();
            }

        };
        start(task);
    }

    public void clearErase() {
        if (currentLine != null) {
            for (Line line : currentLine) {
                editor.maskPane.getChildren().remove(line);
            }
            currentLine = null;
        }
        lastPoint = null;
    }

    @FXML
    @Override
    public void withdrawAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        if (eraserRadio.isSelected()) {
            editor.maskLinesData.removeLastLine();
            erase();
        }
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
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (imageView == null || imageView.getImage() == null || p == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (editor.isPickingColor) {
            return;
        }
        if (eraserRadio.isSelected()) {
            if (event.getButton() != MouseButton.SECONDARY) {
                imageView.setCursor(Cursor.OPEN_HAND);
                return;
            }
            DoublePoint p0 = editor.maskLinesData.getPoint(0);
            double offsetX = p.getX() - p0.getX();
            double offsetY = p.getY() - p0.getY();
            if (DoubleShape.changed(offsetX, offsetY)) {
                editor.maskLinesData = editor.maskLinesData.move(offsetX, offsetY);
                erase();
            }

        } else if (mosaicRadio.isSelected()) {
            if (event.getButton() == MouseButton.SECONDARY) {
                imageView.setCursor(Cursor.OPEN_HAND);
                return;
            }
            mosaic(MosaicType.Mosaic, (int) p.getX(), (int) p.getY());

        } else if (frostedRadio.isSelected()) {
            if (event.getButton() == MouseButton.SECONDARY) {
                imageView.setCursor(Cursor.OPEN_HAND);
                return;
            }
            mosaic(MosaicType.FrostedGlass, (int) p.getX(), (int) p.getY());
        }
    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        erase(event);
    }

    @FXML
    @Override
    public void mouseDragged(MouseEvent event) {
        erase(event);
    }

    public void erase(MouseEvent event) {
        if (imageView == null || imageView.getImage() == null
                || editor.isPickingColor || !eraserRadio.isSelected()) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (coordinatePenCheck.isSelected()) {
            editor.showXY(event, p);
        }

        if (event.getButton() == MouseButton.SECONDARY || p == null
                || editor.maskLinesData == null) {
            return;
        }
        editor.scrollPane.setPannable(false);
        if (lastPoint != null) {
            double offsetX = p.getX() - lastPoint.getX();
            double offsetY = p.getY() - lastPoint.getY();
            if (DoubleShape.changed(offsetX, offsetY)) {
                Line line = editor.drawMaskLinesLine(lastPoint, p);
                if (line != null) {
                    if (currentLine == null) {
                        currentLine = new ArrayList<>();
                    }
                    line.setStroke(Color.RED);
                    line.setStrokeWidth(10);
                    line.getStrokeDashArray().clear();
                    currentLine.add(line);
                }
                editor.maskLinesData.addPoint(p);
            }
        } else {
            editor.maskLinesData.addPoint(p);
        }
        lastPoint = p;
    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
        editor.scrollPane.setPannable(true);
        if (imageView == null || imageView.getImage() == null
                || editor.isPickingColor || !eraserRadio.isSelected()) {
            return;
        }
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (coordinatePenCheck.isSelected()) {
            editor.showXY(event, p);
        }

        if (event.getButton() == MouseButton.SECONDARY || p == null) {
            return;
        }
        if (DoubleShape.changed(lastPoint, p)) {
            editor.maskLinesData.endLine(p);
        } else {
            editor.maskLinesData.endLine(null);
        }
        lastPoint = null;
        erase();
    }

    @Override
    protected void resetOperationPane() {
        checkType();
    }

}
