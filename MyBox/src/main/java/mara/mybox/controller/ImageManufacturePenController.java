package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
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
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolyline;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.image.ImageMosaic.MosaicType;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @License Apache License Version 2.0
 */
public class ImageManufacturePenController extends ImageManufactureOperationController {

    protected PenType opType;
    protected int strokeWidth, arcWidth, intensity, defaultStrokeWidth;
    protected double lastX, lastY;
    protected float opacity;
    protected String strokeWidthKey;

    @FXML
    protected ToggleGroup typeGroup, eraserGroup;
    @FXML
    protected RadioButton polylineRadio, linesRadio, eraserRadio,
            rectangleRadio, circleRadio, ellipseRadio, polygonRadio,
            mosaicRadio, frostedRadio, shapeCircleRadio, shapeRectangleRadio;
    @FXML
    protected FlowPane strokeWidthPane, strokeColorPane, fillPane, rectArcPane, intensityPane,
            shapePane, opacityPane, okPane;
    @FXML
    protected VBox setBox;
    @FXML
    protected ComboBox<String> strokeWidthBox, strokeTypeBox, arcBox, intensityBox, opacityBox;
    @FXML
    protected Label commentLabel;
    @FXML
    protected Button withdrawButton, paletteButton, paletteFillButton;
    @FXML
    protected CheckBox fillCheck, dottedCheck;
    @FXML
    protected Rectangle strokeRect, fillRect;

    public enum PenType {
        Polyline, DrawLines, Erase, Rectangle, Circle, Ellipse, Polygon, Frosted,
        Mosaic
    }

    public ImageManufacturePenController() {
        baseTitle = AppVariables.message("ImageManufacturePen");
        operation = ImageOperation.Pen;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            myPane = penPane;

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    public void initPane(ImageManufactureController parent) {
        try {
            super.initPane(parent);
            if (parent == null) {
                return;
            }

            lastX = lastY = -1;
            imageController.maskLineData = new DoublePolyline();
            imageController.maskLineLines = new ArrayList<>();
            imageController.maskPenData = new DoubleLines();
            imageController.maskPenLines = new ArrayList<>();
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

            String c = AppVariables.getUserConfigValue("ImagePenStrokeColor", Color.RED.toString());
            strokeRect.setFill(Color.web(c));
            FxmlControl.setTooltip(strokeRect, FxmlColor.colorNameDisplay((Color) strokeRect.getFill()));

            c = AppVariables.getUserConfigValue("ImagePenFillColor", Color.WHITE.toString());
            fillRect.setFill(Color.web(c));
            FxmlControl.setTooltip(fillRect, FxmlColor.colorNameDisplay((Color) fillRect.getFill()));

            dottedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    AppVariables.setUserConfigValue("ImagePenDotted", dottedCheck.isSelected());
                    updateMask();
                }
            });
            dottedCheck.setSelected(AppVariables.getUserConfigBoolean("ImagePenDotted", false));

            fillCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    AppVariables.setUserConfigValue("ImagePenFill", fillCheck.isSelected());
                    updateMask();
                }
            });
            fillCheck.setSelected(AppVariables.getUserConfigBoolean("ImagePenFill", false));

            opacityBox.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            opacityBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        float f = Float.valueOf(newValue);
                        if (opacity >= 0.0f && opacity <= 1.0f) {
                            opacity = f;
                            AppVariables.setUserConfigInt("ImagePenOpacity", (int) (f * 100));
                            FxmlControl.setEditorNormal(opacityBox);
                            updateMask();
                        } else {
                            FxmlControl.setEditorBadStyle(opacityBox);
                        }
                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(opacityBox);
                    }
                }
            });
            opacityBox.getSelectionModel().select((AppVariables.getUserConfigInt("ImagePenOpacity", 100) / 100f) + "");

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

            checkPenType();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void checkPenType() {
        try {
            imageController.clearValues();

            setBox.getChildren().clear();
            imageController.initMaskControls(false);
            maskView.setImage(null);
            maskView.setOpacity(1);
            imageController.maskPane.getChildren().removeAll(imageController.maskLineLines);
            imageController.maskLineLines.clear();
            imageController.maskLineData.clear();
            for (List<Line> penline : imageController.maskPenLines) {
                imageController.maskPane.getChildren().removeAll(penline);
            }
            imageController.maskPenLines.clear();
            imageController.maskPenData.clear();

            if (typeGroup.getSelectedToggle() == null) {
                opType = null;
                imageController.imageLabel.setText("");
                commentLabel.setText("");
                return;
            }

            RadioButton selected = (RadioButton) typeGroup.getSelectedToggle();
            if (rectangleRadio.equals(selected)) {
                opType = PenType.Rectangle;
                imageController.initMaskRectangleLine(true);
                imageController.maskRectangleLine.setOpacity(0);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, dottedCheck, fillPane, rectArcPane, opacityPane, okPane);
                imageController.imageLabel.setText(message("PenShapeTips"));
                commentLabel.setText(message("PenShapeTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (circleRadio.equals(selected)) {
                opType = PenType.Circle;
                imageController.initMaskCircleLine(true);
                imageController.maskCircleLine.setOpacity(0);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, dottedCheck, fillPane, opacityPane, okPane);
                imageController.imageLabel.setText(message("PenShapeTips"));
                commentLabel.setText(message("PenShapeTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (ellipseRadio.equals(selected)) {
                opType = PenType.Ellipse;
                imageController.initMaskEllipseLine(true);
                imageController.maskEllipseLine.setOpacity(0);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, dottedCheck, fillPane, opacityPane, okPane);
                imageController.imageLabel.setText(message("PenShapeTips"));
                commentLabel.setText(message("PenShapeTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (polygonRadio.equals(selected)) {
                opType = PenType.Polygon;
                imageController.initMaskPolygonLine(true);
                imageController.maskEllipseLine.setOpacity(0);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, dottedCheck, fillPane, opacityPane, withdrawButton, okPane);
                imageController.imageLabel.setText(message("PolygonTips"));
                commentLabel.setText(message("PolygonTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (polylineRadio.equals(selected)) {
                opType = PenType.Polyline;
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, dottedCheck, opacityPane, withdrawButton, okPane);
                imageController.imageLabel.setText(message("PolylineTips"));
                commentLabel.setText(message("PolylineTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (linesRadio.equals(selected)) {
                opType = PenType.DrawLines;
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, dottedCheck, opacityPane, withdrawButton, okPane);
                imageController.imageLabel.setText(message("PenLinesTips"));
                commentLabel.setText(message("PenLinesTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (eraserRadio.equals(selected)) {
                opType = PenType.Erase;
                setBox.getChildren().addAll(strokeWidthPane, withdrawButton, okPane);
                imageController.imageLabel.setText(message("PenLinesTips"));
                commentLabel.setText(message("PenLinesTips"));
                strokeWidthKey = "ImagePenEraserWidth";
                defaultStrokeWidth = 50;

            } else if (frostedRadio.equals(selected)) {
                opType = PenType.Frosted;
                setBox.getChildren().addAll(strokeWidthPane, intensityPane, shapePane, okPane);
                imageController.imageLabel.setText(message("PenMosaicTips"));
                commentLabel.setText(message("PenMosaicTips"));
                strokeWidthKey = "ImagePenMosaicWidth";
                defaultStrokeWidth = 80;

            } else if (mosaicRadio.equals(selected)) {
                opType = PenType.Mosaic;
                setBox.getChildren().addAll(strokeWidthPane, intensityPane, shapePane, okPane);
                imageController.imageLabel.setText(message("PenMosaicTips"));
                commentLabel.setText(message("PenMosaicTips"));
                strokeWidthKey = "ImagePenMosaicWidth";
                defaultStrokeWidth = 80;

            }
            strokeWidthBox.getSelectionModel().select(
                    AppVariables.getUserConfigInt(strokeWidthKey, defaultStrokeWidth) + "");

            FxmlControl.refreshStyle(setBox);
            updateMask();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public boolean setColor(Control control, Color color) {
        if (control == null || color == null) {
            return false;
        }

        if (paletteButton.equals(control)) {
            strokeRect.setFill(color);
            FxmlControl.setTooltip(strokeRect, FxmlColor.colorNameDisplay(color));
            AppVariables.setUserConfigValue("ImagePenStrokeColor", color.toString());

        } else if (paletteFillButton.equals(control)) {
            fillRect.setFill(color);
            FxmlControl.setTooltip(fillRect, FxmlColor.colorNameDisplay(color));
            AppVariables.setUserConfigValue("ImagePenFillColor", color.toString());
        }
        updateMask();
        return true;
    }

    @FXML
    @Override
    public void showPalette(ActionEvent event) {
        showPalette(paletteButton, message("StrokeColor"), true);
    }

    @FXML
    public void showFillPalette(ActionEvent event) {
        showPalette(paletteFillButton, message("Fill"), true);
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
                drawPolyline();
                break;
            case DrawLines:
                drawLines();
                break;
            case Erase:
                imageController.drawMaskPenLines(strokeWidth, Color.TRANSPARENT, false, 1.0f);
                break;

        }

    }

    public void drawRectangle() {
        if (isSettingValues || opType != PenType.Rectangle
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxmlImageManufacture.drawRectangle(imageView.getImage(),
                            imageController.maskRectangleData, (Color) strokeRect.getFill(), strokeWidth,
                            arcWidth, dottedCheck.isSelected(),
                            fillCheck.isSelected(), (Color) fillRect.getFill(), opacity);
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    maskView.setImage(newImage);
                    maskView.setFitWidth(imageView.getFitWidth());
                    maskView.setFitHeight(imageView.getFitHeight());
                    maskView.setLayoutX(imageView.getLayoutX());
                    maskView.setLayoutY(imageView.getLayoutY());
                    imageController.drawMaskRectangleLineAsData();
                }

            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void drawCircle() {
        if (isSettingValues || opType != PenType.Circle
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxmlImageManufacture.drawCircle(imageView.getImage(),
                            imageController.maskCircleData, (Color) strokeRect.getFill(), strokeWidth,
                            dottedCheck.isSelected(),
                            fillCheck.isSelected(), (Color) fillRect.getFill(), opacity);
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    maskView.setImage(newImage);
                    maskView.setFitWidth(imageView.getFitWidth());
                    maskView.setFitHeight(imageView.getFitHeight());
                    maskView.setLayoutX(imageView.getLayoutX());
                    maskView.setLayoutY(imageView.getLayoutY());
                    imageController.drawMaskCircleLineAsData();
                }

            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void drawEllipse() {
        if (isSettingValues || opType != PenType.Ellipse
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxmlImageManufacture.drawEllipse(imageView.getImage(),
                            imageController.maskEllipseData, (Color) strokeRect.getFill(), strokeWidth,
                            dottedCheck.isSelected(),
                            fillCheck.isSelected(), (Color) fillRect.getFill(), opacity);
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    maskView.setImage(newImage);
                    maskView.setFitWidth(imageView.getFitWidth());
                    maskView.setFitHeight(imageView.getFitHeight());
                    maskView.setLayoutX(imageView.getLayoutX());
                    maskView.setLayoutY(imageView.getLayoutY());
                    imageController.drawMaskEllipseLineAsData();
                }

            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
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
        if (imageController.maskPolygonData.getSize() <= 2) {
            imageController.drawMaskPolygonLineAsData();
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxmlImageManufacture.drawPolygon(imageView.getImage(),
                            imageController.maskPolygonData, (Color) strokeRect.getFill(), strokeWidth,
                            dottedCheck.isSelected(),
                            fillCheck.isSelected(), (Color) fillRect.getFill(), opacity);
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    maskView.setImage(newImage);
                    maskView.setFitWidth(imageView.getFitWidth());
                    maskView.setFitHeight(imageView.getFitHeight());
                    maskView.setLayoutX(imageView.getLayoutX());
                    maskView.setLayoutY(imageView.getLayoutY());
                    imageController.maskPolygonLine.setOpacity(0);
                    imageController.polygonP1.setOpacity(0);
                    imageController.polygonP2.setOpacity(0);
                }

            };
            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void drawPolyline() {
        if (opType != PenType.Polyline || imageView == null || imageView.getImage() == null) {
            return;
        }

        imageController.drawMaskLine(strokeWidth, (Color) strokeRect.getFill(), dottedCheck.isSelected(), opacity);

    }

    public void drawLines() {
        if (opType != PenType.DrawLines || imageView == null || imageView.getImage() == null) {
            return;
        }
        imageController.drawMaskPenLines(strokeWidth, (Color) strokeRect.getFill(), dottedCheck.isSelected(), opacity);
    }

    public void mosaic(MosaicType mosaicType, int x, int y) {
        if (isSettingValues || mosaicType == null
                || imageView == null || imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
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

    @FXML
    @Override
    public void paneClicked(MouseEvent event) {
        if (null == opType || imageView == null || imageView.getImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        switch (opType) {
            case Polyline: {
                if (event.getButton() != MouseButton.SECONDARY) {
                    imageView.setCursor(Cursor.OPEN_HAND);
                    return;
                }
                DoublePoint p = imageController.getImageXY(event, imageView);
                if (p == null) {
                    return;
                }
                DoublePoint p0 = imageController.maskLineData.get(0);
                double offsetX = p.getX() - p0.getX();
                double offsetY = p.getY() - p0.getY();
                if (offsetX != 0 || offsetY != 0) {
                    imageController.maskLineData = imageController.maskLineData.move(offsetX, offsetY);
                    drawPolyline();
                }
            }
            break;
            case DrawLines:
            case Erase: {
                if (event.getButton() != MouseButton.SECONDARY) {
                    imageView.setCursor(Cursor.OPEN_HAND);
                    return;
                }
                DoublePoint p = imageController.getImageXY(event, imageView);
                if (p == null) {
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
                DoublePoint p = imageController.getImageXY(event, imageView);
                if (p == null) {
                    return;
                }
                mosaic(MosaicType.Mosaic, (int) p.getX(), (int) p.getY());
            }
            break;
            case Frosted: {
                DoublePoint p = imageController.getImageXY(event, imageView);
                if (p == null) {
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
        DoublePoint p = imageController.getImageXY(event, imageView);
        imageController.showXY(event, p);

        if (event.getButton() == MouseButton.SECONDARY || p == null) {
            return;
        }

        switch (opType) {
            case Polyline:
                if (lastX == event.getX() && lastY == event.getY()) {
                    return;
                }
                imageController.scrollPane.setPannable(false);
                imageController.maskLineData.add(p);
                lastX = event.getX();
                lastY = event.getY();
                drawPolyline();
                break;
            case DrawLines:
            case Erase:
//            case Mosaic:
//            case Frosted:
                imageController.scrollPane.setPannable(false);
                imageController.maskPenData.startLine(p);
                lastX = event.getX();
                lastY = event.getY();
                updateMask();
        }

    }

    @FXML
    @Override
    public void mouseDragged(MouseEvent event) {
        if (null == opType || imageView == null || imageView.getImage() == null) {
            return;
        }
        DoublePoint p = imageController.getImageXY(event, imageView);
        imageController.showXY(event, p);

        if (event.getButton() == MouseButton.SECONDARY || p == null) {
            return;
        }

        switch (opType) {
            case Polyline:
                if (lastX == event.getX() && lastY == event.getY()) {
                    return;
                }
                imageController.scrollPane.setPannable(false);
                imageController.maskLineData.add(p);
                lastX = event.getX();
                lastY = event.getY();
                drawPolyline();
                break;
            case DrawLines:
            case Erase:
//            case Mosaic:
//            case Frosted:
                imageController.scrollPane.setPannable(false);
                imageController.maskPenData.addPoint(p);
                lastX = event.getX();
                lastY = event.getY();
                updateMask();
        }

    }

    @FXML
    @Override
    public void mouseReleased(MouseEvent event) {
        if (null == opType || imageView == null || imageView.getImage() == null) {
            return;
        }
        imageController.scrollPane.setPannable(true);
        DoublePoint p = imageController.getImageXY(event, imageView);
        imageController.showXY(event, p);

        if (event.getButton() == MouseButton.SECONDARY || p == null) {
            return;
        }

        switch (opType) {
            case Polyline:
                if (lastX == event.getX() && lastY == event.getY()) {
                    return;
                }
                imageController.maskLineData.add(p);
                lastX = event.getX();
                lastY = event.getY();
                drawPolyline();
                break;
            case DrawLines:
            case Erase:
//            case Mosaic:
//            case Frosted:
                imageController.maskPenData.endLine(p);
                lastX = event.getX();
                lastY = event.getY();
                updateMask();
        }

    }

    @FXML
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
                imageController.maskLineData.removeLast();
                drawPolyline();
                break;
            case DrawLines:
            case Erase:
                imageController.maskPenData.removeLastLine();
                updateMask();
                break;
        }
    }

    @FXML
    @Override
    public void clearAction() {
        if (null == opType || imageView == null || imageView.getImage() == null) {
            return;
        }
        switch (opType) {
            case Polyline:
                imageController.maskPane.getChildren().removeAll(imageController.maskLineLines);
                imageController.maskLineLines.clear();
                imageController.maskLineData.clear();
                break;
            case DrawLines:
            case Erase:
                for (List<Line> penline : imageController.maskPenLines) {
                    imageController.maskPane.getChildren().removeAll(penline);
                }
                imageController.maskPenLines.clear();
                imageController.maskPenData.clear();
                break;
            case Mosaic:
            case Frosted:
                maskView.setImage(imageView.getImage());
                break;
            default:
                imageController.initMaskControls(false);
                typeGroup.selectToggle(null);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (okButton.isDisabled()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                private Image newImage;

                @Override
                protected boolean handle() {

                    newImage = null;

                    switch (opType) {
                        case Rectangle:
                        case Circle:
                        case Ellipse:
                        case Polygon:
                        case Mosaic:
                        case Frosted:
                            newImage = maskView.getImage();
                            break;
                        case Polyline:
                            if (imageController.maskLineData == null && imageController.maskLineData.getSize() < 2) {
                                return false;
                            }
                            newImage = FxmlImageManufacture.drawLines(imageView.getImage(),
                                    imageController.maskLineData, (Color) strokeRect.getFill(), strokeWidth,
                                    dottedCheck.isSelected(), opacity);
                            break;
                        case DrawLines:
                            if (imageController.maskPenData == null && imageController.maskPenData.getPointsSize() == 0) {
                                return false;
                            }
                            newImage = FxmlImageManufacture.drawLines(imageView.getImage(),
                                    imageController.maskPenData, (Color) strokeRect.getFill(), strokeWidth,
                                    dottedCheck.isSelected(), opacity);
                            break;
                        case Erase:
                            if (imageController.maskPenData == null && imageController.maskPenData.getPointsSize() == 0) {
                                return false;
                            }
                            newImage = FxmlImageManufacture.drawLines(imageView.getImage(),
                                    imageController.maskPenData, Color.TRANSPARENT, strokeWidth,
                                    false, 1.0f);
                            break;
                        default:
                            return false;
                    }

                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    parent.updateImage(ImageOperation.Pen, opType.name(), null, newImage, cost);
                    typeGroup.selectToggle(null);

                }

            };

            parent.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void quitPane() {
        clearAction();
        imageController.maskRectangleLine.setOpacity(1);
        imageController.maskCircleLine.setOpacity(1);
        imageController.maskEllipseLine.setOpacity(1);
        imageController.maskEllipseLine.setOpacity(1);

    }

}
