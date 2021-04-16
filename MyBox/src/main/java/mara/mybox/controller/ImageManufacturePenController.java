package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
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
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.ImageMosaic.MosaicType;
import mara.mybox.image.PixelBlend;
import mara.mybox.image.PixelBlend.ImagesBlendMode;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @License Apache License Version 2.0
 */
public class ImageManufacturePenController extends ImageManufactureOperationController {

    protected PenType opType;
    protected int strokeWidth, arcWidth, intensity, defaultStrokeWidth;
    protected DoublePoint lastPoint;
    protected ImagesBlendMode blendMode;
    protected float opacity;
    protected String strokeWidthKey;
    protected List<Line> penLines;

    @FXML
    protected ToggleGroup typeGroup, eraserGroup;
    @FXML
    protected RadioButton polylineRadio, linesRadio, eraserRadio,
            rectangleRadio, circleRadio, ellipseRadio, polygonRadio,
            mosaicRadio, frostedRadio, shapeCircleRadio, shapeRectangleRadio;
    @FXML
    protected FlowPane strokeWidthPane, strokeColorPane, fillPane, rectArcPane, intensityPane,
            shapePane, opacityPane;
    @FXML
    protected VBox setBox, blendBox;
    @FXML
    protected ComboBox<String> strokeWidthBox, strokeTypeBox, arcBox, intensityBox, opacitySelector, blendSelector;
    @FXML
    protected Label commentsLabel;
    @FXML
    protected CheckBox fillCheck, dottedCheck, coordinatePenCheck, blendTopCheck;
    @FXML
    protected ColorSet strokeColorSetController;
    @FXML
    protected ColorSet fillColorSetController;
    @FXML
    protected Button demoButton;

    public enum PenType {
        Polyline, DrawLines, Erase, Rectangle, Circle, Ellipse, Polygon, Frosted, Mosaic
    }

    @Override
    public void initPane() {
        try {
            lastPoint = null;
            setBox.getChildren().clear();
            typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkPenType();
                }
            });

            strokeWidthKey = "ImagePenLineWidth";
            defaultStrokeWidth = 5;
            int imageWidth = (int) imageView.getImage().getWidth();
            strokeWidthBox.getItems().clear();
            List<String> ws = new ArrayList<>();
            ws.addAll(Arrays.asList("3", "0", "1", "2", "5", "8", "10", "15", "25", "30", "50", "80", "100", "150", "200", "300", "500"));
            int max = imageWidth / 20;
            int step = max / 10;
            for (int w = 10; w < max; w += step) {
                if (!ws.contains(w + "")) {
                    ws.add(w + "");
                }
            }
            strokeWidthBox.getItems().addAll(ws);
            strokeWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            strokeWidth = v;
                            AppVariables.setUserConfigInt(strokeWidthKey, strokeWidth);
                            updateMask();
                            FxmlControl.setEditorNormal(strokeWidthBox);
                        } else {
                            FxmlControl.setEditorBadStyle(strokeWidthBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(strokeWidthBox);
                    }
                }
            });
            strokeWidthBox.getSelectionModel().select(
                    AppVariables.getUserConfigInt(strokeWidthKey, defaultStrokeWidth) + "");

            arcBox.getItems().clear();
            List<String> as = new ArrayList<>();
            as.addAll(Arrays.asList("0", "20", "50", "100", "10", "15", "30", "150", "200"));
            max = imageWidth / 3;
            step = max / 10;
            for (int a = 10; a < max; a += step) {
                if (!as.contains(a + "")) {
                    as.add(a + "");
                }
            }
            arcBox.getItems().addAll(as);
            arcBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            arcWidth = v;
                            AppVariables.setUserConfigInt("ImagePenArcWidth", arcWidth);
                            updateMask();
                            FxmlControl.setEditorNormal(arcBox);
                        } else {
                            FxmlControl.setEditorBadStyle(arcBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(arcBox);
                    }
                }
            });
            arcBox.getSelectionModel().select(AppVariables.getUserConfigValue("ImagePenArcWidth", "0") + "");

            strokeColorSetController.init(this, baseName + "StrokeColor", Color.RED);
            strokeColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    updateMask();
                }
            });

            fillColorSetController.init(this, baseName + "FillColor", Color.WHITE);
            fillColorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    updateMask();
                }
            });

            dottedCheck.setSelected(AppVariables.getUserConfigBoolean("ImagePenDotted", false));
            dottedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    AppVariables.setUserConfigValue("ImagePenDotted", dottedCheck.isSelected());
                    updateMask();
                }
            });

            fillCheck.setSelected(AppVariables.getUserConfigBoolean("ImagePenFill", false));
            fillCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    AppVariables.setUserConfigValue("ImagePenFill", fillCheck.isSelected());
                    updateMask();
                }
            });

            coordinatePenCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "PenCoordinate", false));
            coordinatePenCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    AppVariables.setUserConfigValue(baseName + "PenCoordinate", coordinatePenCheck.isSelected());
                }
            });

            String mode = AppVariables.getUserConfigValue("ImagePenBlendMode", message("NormalMode"));
            blendMode = PixelBlend.getBlendModeByName(mode);
            blendSelector.getItems().addAll(PixelBlend.allBlendModes());
            blendSelector.setValue(mode);
            blendSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                    String mode = blendSelector.getSelectionModel().getSelectedItem();
                    blendMode = PixelBlend.getBlendModeByName(mode);
                    AppVariables.setUserConfigValue("ImagePenBlendMode", mode);
                    updateMask();
                }
            });

            opacity = AppVariables.getUserConfigInt("ImagePenOpacity", 100) / 100f;
            opacity = (opacity >= 0.0f && opacity <= 1.0f) ? opacity : 1.0f;
            opacitySelector.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            opacitySelector.setValue(opacity + "");
            opacitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        float f = Float.valueOf(newValue);
                        if (opacity >= 0.0f && opacity <= 1.0f) {
                            opacity = f;
                            AppVariables.setUserConfigInt("ImagePenOpacity", (int) (f * 100));
                            FxmlControl.setEditorNormal(opacitySelector);
                            updateMask();
                        } else {
                            FxmlControl.setEditorBadStyle(opacitySelector);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(opacitySelector);
                    }
                }
            });

            blendTopCheck.setSelected(AppVariables.getUserConfigBoolean("ImagePenBlendTop", true));
            blendTopCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    AppVariables.setUserConfigValue("ImagePenBlendTop", blendTopCheck.isSelected());
                    updateMask();
                }
            });

            intensityBox.getItems().addAll(Arrays.asList("20", "50", "10", "5", "80", "100", "15", "20", "60"));
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
                            AppVariables.setUserConfigInt("ImageMosaicIntensity", v);
                        } else {
                            FxmlControl.setEditorBadStyle(intensityBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intensityBox);
                    }
                }
            });
            intensityBox.getSelectionModel().select(AppVariables.getUserConfigInt("ImageMosaicIntensity", 20) + "");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        checkPenType();
    }

    private void checkPenType() {
        try {
            imageController.resetImagePane();
            imageController.hideScopePane();
            imageController.showImagePane();
            setBox.getChildren().clear();
            imageController.initMaskControls(false);
            if (typeGroup.getSelectedToggle() == null) {
                opType = null;
                commentsLabel.setText("");
                return;
            }
            maskView.setImage(imageView.getImage());
            maskView.setOpacity(1);
            maskView.setVisible(true);
            imageView.setVisible(false);
            imageView.toBack();
            withdrawButton.setVisible(false);
            clearPenLines();
            RadioButton selected = (RadioButton) typeGroup.getSelectedToggle();
            if (rectangleRadio.equals(selected)) {
                opType = PenType.Rectangle;
                imageController.initMaskRectangleLine(true);
                imageController.maskRectangleLine.setOpacity(0);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, dottedCheck, fillPane, rectArcPane, blendBox);
                commentsLabel.setText(message("PenRectangleTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (circleRadio.equals(selected)) {
                opType = PenType.Circle;
                imageController.initMaskCircleLine(true);
                imageController.maskCircleLine.setOpacity(0);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, dottedCheck, fillPane, blendBox);
                commentsLabel.setText(message("PenCircleTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (ellipseRadio.equals(selected)) {
                opType = PenType.Ellipse;
                imageController.initMaskEllipseLine(true);
                imageController.maskEllipseLine.setOpacity(0);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, dottedCheck, fillPane, blendBox);
                commentsLabel.setText(message("PenEllipseTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (polygonRadio.equals(selected)) {
                opType = PenType.Polygon;
                imageController.initMaskPolygonLine(true);
                imageController.maskPolygonLine.setOpacity(0);
                withdrawButton.setVisible(true);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, dottedCheck, fillPane, blendBox);
                commentsLabel.setText(message("PenPolygonTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (polylineRadio.equals(selected)) {
                opType = PenType.Polyline;
                imageController.initMaskPolylineLine(true);
                withdrawButton.setVisible(true);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, dottedCheck, blendBox);
                commentsLabel.setText(message("PenPolylineTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (linesRadio.equals(selected)) {
                opType = PenType.DrawLines;
                imageController.initMaskPenlines(true);
                withdrawButton.setVisible(true);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, dottedCheck, blendBox);
                commentsLabel.setText(message("PenLinesTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (eraserRadio.equals(selected)) {
                opType = PenType.Erase;
                imageController.initMaskPenlines(true);
                withdrawButton.setVisible(true);
                setBox.getChildren().addAll(strokeWidthPane);
                commentsLabel.setText(message("PenLinesTips") + "\n" + message("ImageEraserComments"));
                strokeWidthKey = "ImagePenEraserWidth";
                defaultStrokeWidth = 50;

            } else if (frostedRadio.equals(selected)) {
                opType = PenType.Frosted;
                setBox.getChildren().addAll(strokeWidthPane, intensityPane, shapePane);
                commentsLabel.setText(message("PenMosaicTips"));
                strokeWidthKey = "ImagePenMosaicWidth";
                defaultStrokeWidth = 80;

            } else if (mosaicRadio.equals(selected)) {
                opType = PenType.Mosaic;
                setBox.getChildren().addAll(strokeWidthPane, intensityPane, shapePane);
                commentsLabel.setText(message("PenMosaicTips"));
                strokeWidthKey = "ImagePenMosaicWidth";
                defaultStrokeWidth = 80;

            }
            strokeWidthBox.getSelectionModel().select(
                    AppVariables.getUserConfigInt(strokeWidthKey, defaultStrokeWidth) + "");

            FxmlControl.refreshStyle(setBox);
            updateMask();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void updateMask() {
        if (isSettingValues || opType == null
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        switch (opType) {
            case Rectangle:
                drawRectangle();
                break;
            case Circle:
                drawCircle();
                break;
            case Ellipse:
                drawEllipse();
                break;
            case Polygon:
                drawPolygon();
                break;
            case Polyline:
            case DrawLines:
            case Erase:
                drawLines();
                break;
        }
    }

    public void drawRectangle() {
        if (isSettingValues || opType != PenType.Rectangle
                || imageView == null || imageView.getImage() == null
                || imageController.maskRectangleData == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxmlImageManufacture.drawRectangle(imageView.getImage(),
                            imageController.maskRectangleData, (Color) strokeColorSetController.rect.getFill(), strokeWidth,
                            arcWidth, dottedCheck.isSelected(), fillCheck.isSelected(), (Color) fillColorSetController.rect.getFill(),
                            blendMode, opacity, !blendTopCheck.isSelected());
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    maskView.setImage(newImage);
                    imageController.drawMaskRectangleLineAsData();
                }

            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void drawCircle() {
        if (isSettingValues || opType != PenType.Circle
                || imageView == null || imageView.getImage() == null
                || imageController.maskCircleData == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxmlImageManufacture.drawCircle(imageView.getImage(),
                            imageController.maskCircleData, (Color) strokeColorSetController.rect.getFill(), strokeWidth,
                            dottedCheck.isSelected(), fillCheck.isSelected(), (Color) fillColorSetController.rect.getFill(),
                            blendMode, opacity, !blendTopCheck.isSelected());
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    maskView.setImage(newImage);
                    imageController.drawMaskCircleLineAsData();
                }

            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void drawEllipse() {
        if (isSettingValues || opType != PenType.Ellipse
                || imageView == null || imageView.getImage() == null
                || imageController.maskEllipseData == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxmlImageManufacture.drawEllipse(imageView.getImage(),
                            imageController.maskEllipseData, (Color) strokeColorSetController.rect.getFill(), strokeWidth,
                            dottedCheck.isSelected(), fillCheck.isSelected(), (Color) fillColorSetController.rect.getFill(),
                            blendMode, opacity, !blendTopCheck.isSelected());
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    maskView.setImage(newImage);
                    imageController.drawMaskEllipseLineAsData();
                }

            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void drawPolygon() {
        if (isSettingValues || opType != PenType.Polygon
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        if (imageController.maskPolygonData == null || imageController.maskPolygonData.getSize() <= 2) {
            imageController.drawMaskPolygonLineAsData();
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxmlImageManufacture.drawPolygon(imageView.getImage(),
                            imageController.maskPolygonData, (Color) strokeColorSetController.rect.getFill(), strokeWidth,
                            dottedCheck.isSelected(), fillCheck.isSelected(), (Color) fillColorSetController.rect.getFill(),
                            blendMode, opacity, !blendTopCheck.isSelected());
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    maskView.setImage(newImage);
                    imageController.maskPolygonLine.setOpacity(0);
                    imageController.polygonP1.setOpacity(0);
                    imageController.polygonP2.setOpacity(0);
                }

            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void mosaic(MosaicType mosaicType, int x, int y) {
        if (isSettingValues || mosaicType == null
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image newImage;

                @Override
                protected boolean handle() {
                    DoubleShape shape;
                    if (shapeCircleRadio.isSelected()) {
                        shape = new DoubleCircle(x, y, strokeWidth);
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
                    newImage = FxmlImageManufacture.makeMosaic(image,
                            shape, intensity, mosaicType == MosaicType.Mosaic, false);
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    maskView.setImage(newImage);
                }

            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void drawLines() {
        if (isSettingValues || imageView == null || imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                task.cancel();
            }
            task = new SingletonTask<Void>() {
                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = imageView.getImage();
                    switch (opType) {
                        case Polyline:
                            newImage = FxmlImageManufacture.drawLines(imageView.getImage(),
                                    imageController.maskPolylineLineData, (Color) strokeColorSetController.rect.getFill(), strokeWidth,
                                    dottedCheck.isSelected(), blendMode, opacity, !blendTopCheck.isSelected());
                            break;
                        case Erase:
                            newImage = FxmlImageManufacture.drawErase(imageView.getImage(), imageController.maskPenData, strokeWidth);
                            break;
                        case DrawLines:
                            newImage = FxmlImageManufacture.drawLines(imageView.getImage(),
                                    imageController.maskPenData, (Color) strokeColorSetController.rect.getFill(), strokeWidth,
                                    dottedCheck.isSelected(), blendMode, opacity, !blendTopCheck.isSelected());
                            break;
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
                    clearPenLines();
                }

            };

            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void drawLine(DoublePoint lastPoint, DoublePoint thisPoint) {
        Line penLine;
        if (opType == PenType.Erase) {
            penLine = imageController.drawMaskPenLine(strokeWidth, Color.TRANSPARENT,
                    false, 1.0f, lastPoint, thisPoint);
        } else {
            penLine = imageController.drawMaskPenLine(strokeWidth, (Color) strokeColorSetController.rect.getFill(),
                    dottedCheck.isSelected(), opacity, lastPoint, thisPoint);
        }

        if (penLine != null) {
            if (penLines == null) {
                penLines = new ArrayList<>();
            }
            penLines.add(penLine);
        }
    }

    public void clearPenLines() {
        imageController.polygonP1.setOpacity(0);
        if (penLines != null) {
            for (Line line : penLines) {
                imageController.maskPane.getChildren().remove(line);
            }
            penLines = null;
        }
        lastPoint = null;
    }

    @FXML
    @Override
    public void withdrawAction() {
        if (null == opType || imageView == null || imageView.getImage() == null) {
            return;
        }
        switch (opType) {
            case Polygon:
                imageController.maskPolygonData.removeLast();
                drawPolygon();
                break;
            case Polyline:
                imageController.maskPolylineLineData.removeLast();
                drawLines();
                break;
            case DrawLines:
            case Erase:
                imageController.maskPenData.removeLastLine();
                drawLines();
                break;
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
        checkPenType();
    }

    @FXML
    @Override
    public void okAction() {
        if (okButton.isDisabled()) {
            return;
        }
        imageController.popSuccessful();
        imageController.updateImage(ImageOperation.Pen, opType.name(), null, maskView.getImage(), 0);
    }

    @FXML
    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
        if (null == opType || imageView == null || imageView.getImage() == null || p == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (imageController.isPickingColor) {
            Color color = FxmlControl.imagePixel(p, imageView);
            if (color != null) {
                strokeColorSetController.setColor(color);
            }
            return;
        }
        switch (opType) {
            case Polyline: {
                if (event.getButton() != MouseButton.SECONDARY) {
                    imageView.setCursor(Cursor.OPEN_HAND);
                    return;
                }
                DoublePoint p0 = imageController.maskPolylineLineData.get(0);
                double offsetX = p.getX() - p0.getX();
                double offsetY = p.getY() - p0.getY();
                if (offsetX != 0 || offsetY != 0) {
                    imageController.maskPolylineLineData = imageController.maskPolylineLineData.move(offsetX, offsetY);
                    updateMask();
                }
            }
            break;
            case DrawLines:
            case Erase: {
                if (event.getButton() != MouseButton.SECONDARY) {
                    imageView.setCursor(Cursor.OPEN_HAND);
                    return;
                }
                DoublePoint p0 = imageController.maskPenData.getPoint(0);
                double offsetX = p.getX() - p0.getX();
                double offsetY = p.getY() - p0.getY();
                if (offsetX != 0 || offsetY != 0) {
                    imageController.maskPenData = imageController.maskPenData.move(offsetX, offsetY);
                    updateMask();
                }
            }
            break;
            case Rectangle:
            case Circle:
            case Ellipse:
            case Polygon:
                updateMask();
                break;
            case Mosaic: {
                if (event.getButton() == MouseButton.SECONDARY) {
                    imageView.setCursor(Cursor.OPEN_HAND);
                    return;
                }
                mosaic(MosaicType.Mosaic, (int) p.getX(), (int) p.getY());
            }
            break;
            case Frosted: {
                if (event.getButton() == MouseButton.SECONDARY) {
                    imageView.setCursor(Cursor.OPEN_HAND);
                    return;
                }
                mosaic(MosaicType.FrostedGlass, (int) p.getX(), (int) p.getY());
            }
            break;
        }

    }

    @FXML
    @Override
    public void mousePressed(MouseEvent event) {
        if (null == opType || imageView == null || imageView.getImage() == null) {
            return;
        }
        if (imageController.isPickingColor) {
            return;
        }
        DoublePoint p = FxmlControl.getImageXY(event, imageView);
        if (coordinatePenCheck.isSelected()) {
            imageController.showXY(event, p);
        }

        if (event.getButton() == MouseButton.SECONDARY || p == null) {
            return;
        }
        if (lastPoint != null && lastPoint.getX() == p.getX() && lastPoint.getY() == p.getY()) {
            return;
        }
        switch (opType) {
            case Polyline:
                imageController.scrollPane.setPannable(false);
                imageController.maskPolylineLineData.add(p);
                drawLine(lastPoint, p);
                lastPoint = p;
                break;
            case DrawLines:
            case Erase:
                imageController.scrollPane.setPannable(false);
                imageController.maskPenData.addPoint(p);
                drawLine(lastPoint, p);
                lastPoint = p;
                break;
        }
    }

    @FXML
    @Override
    public void mouseDragged(MouseEvent event) {
        if (null == opType || imageView == null || imageView.getImage() == null) {
            return;
        }
        if (imageController.isPickingColor) {
            return;
        }
        DoublePoint p = FxmlControl.getImageXY(event, imageView);
        if (coordinatePenCheck.isSelected()) {
            imageController.showXY(event, p);
        }

        if (event.getButton() == MouseButton.SECONDARY || p == null) {
            return;
        }
        if (lastPoint != null && lastPoint.getX() == p.getX() && lastPoint.getY() == p.getY()) {
            return;
        }
        switch (opType) {
            case Polyline:
                imageController.scrollPane.setPannable(false);
                imageController.maskPolylineLineData.add(p);
                drawLine(lastPoint, p);
                lastPoint = p;
                break;
            case DrawLines:
            case Erase:
                imageController.scrollPane.setPannable(false);
                imageController.maskPenData.addPoint(p);
                drawLine(lastPoint, p);
                lastPoint = p;
                break;
        }

    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
        imageController.scrollPane.setPannable(true);
        if (null == opType || imageView == null || imageView.getImage() == null) {
            return;
        }
        if (imageController.isPickingColor) {
            return;
        }
        DoublePoint p = FxmlControl.getImageXY(event, imageView);
        if (coordinatePenCheck.isSelected()) {
            imageController.showXY(event, p);
        }

        if (event.getButton() == MouseButton.SECONDARY || p == null) {
            return;
        }
        switch (opType) {
            case Polyline:
                if (lastPoint == null || lastPoint.getX() != p.getX() || lastPoint.getY() != p.getY()) {
                    imageController.maskPolylineLineData.add(p);
                    lastPoint = p;
                }
                drawLines();
                break;
            case DrawLines:
            case Erase:
                imageController.maskPenData.endLine(p);
                lastPoint = null;
                drawLines();
                break;
        }
    }

    @Override
    protected void resetOperationPane() {
        checkPenType();
    }

    @FXML
    protected void demo() {
        Image backImage = imageView.getImage();
        Image foreImage = FxmlImageManufacture.createImage((int) (backImage.getWidth() / 2), (int) (backImage.getHeight() / 2),
                (Color) strokeColorSetController.rect.getFill());
        double x = (backImage.getWidth() - foreImage.getWidth()) / 2;
        double y = (backImage.getHeight() - foreImage.getHeight()) / 2;
        BaseImageController.blendDemo(imageController, demoButton,
                foreImage, backImage, (int) x, (int) y, opacity, !blendTopCheck.isSelected());
    }

}
