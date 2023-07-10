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
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import mara.mybox.bufferedimage.ImageMosaic.MosaicType;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.data.DoubleCircle;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.DoubleShape;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.fximage.PenTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @License Apache License Version 2.0
 */
public class ImageManufacturePenController extends ImageManufactureOperationController {

    protected PenType opType;
    protected int strokeWidth, arcWidth, intensity, defaultStrokeWidth;
    protected DoublePoint lastPoint;
    protected String strokeWidthKey;
    protected List<Line> penLines;

    @FXML
    protected ToggleGroup typeGroup, eraserGroup;
    @FXML
    protected RadioButton polylineRadio, linesRadio, eraserRadio,
            rectangleRadio, circleRadio, ellipseRadio, polygonRadio,
            mosaicRadio, frostedRadio, shapeCircleRadio, shapeRectangleRadio;
    @FXML
    protected FlowPane strokeWidthPane, strokeColorPane, anchorColorPane, anchorSizePane, fillPane,
            rectArcPane, intensityPane, shapePane, opacityPane;
    @FXML
    protected VBox setBox, blendBox;
    @FXML
    protected ComboBox<String> strokeWidthSelector, anchorSizeSelector, arcSelector, intensitySelector;
    @FXML
    protected Label commentsLabel;
    @FXML
    protected CheckBox fillCheck, dottedCheck, coordinatePenCheck;
    @FXML
    protected ControlColorSet strokeColorController, anchorColorController, fillColorController;
    @FXML
    protected ControlImagesBlend blendController;

    public enum PenType {
        Polyline, DrawLines, Erase, Rectangle, Circle, Ellipse, Polygon, Frosted, Mosaic
    }

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
                    checkPenType();
                }
            });

            strokeWidthKey = "ImagePenLineWidth";
            defaultStrokeWidth = 5;
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
            strokeWidth = UserConfig.getInt(strokeWidthKey, defaultStrokeWidth);
            strokeWidthSelector.getSelectionModel().select(strokeWidth + "");
            strokeWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            strokeWidth = v;
                            UserConfig.setInt(strokeWidthKey, strokeWidth);
                            updateMask();
                            ValidationTools.setEditorNormal(strokeWidthSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(strokeWidthSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(strokeWidthSelector);
                    }
                }
            });

            anchorSizeSelector.getItems().setAll(Arrays.asList("10", "15", "20", "25", "30", "40", "50"));
            anchorSizeSelector.setValue(anchorWidth() + "");
            anchorSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
                            UserConfig.setInt("AnchorWidth", v);
                            imageController.setMaskAnchorsStyle();
                            ValidationTools.setEditorNormal(anchorSizeSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(anchorSizeSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(anchorSizeSelector);
                    }
                }
            });

            arcSelector.getItems().clear();
            List<String> as = new ArrayList<>();
            as.addAll(Arrays.asList("0", "20", "50", "100", "10", "15", "30", "150", "200"));
            max = imageWidth / 3;
            step = max / 10;
            for (int a = 10; a < max; a += step) {
                if (!as.contains(a + "")) {
                    as.add(a + "");
                }
            }
            arcSelector.getItems().addAll(as);
            arcSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            arcWidth = v;
                            UserConfig.setInt("ImagePenArcWidth", arcWidth);
                            updateMask();
                            ValidationTools.setEditorNormal(arcSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(arcSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(arcSelector);
                    }
                }
            });
            arcSelector.getSelectionModel().select(UserConfig.getString("ImagePenArcWidth", "0") + "");

            strokeColorController.init(this, "StrokeColor", Color.web(BaseImageController.DefaultStrokeColor));
            strokeColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue v, Paint ov, Paint nv) {
                    updateMask();
                }
            });

            anchorColorController.init(this, "AnchorColor", Color.web(BaseImageController.DefaultAnchorColor));
            anchorColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue v, Paint ov, Paint nv) {
                    imageController.setMaskAnchorsStyle();
                }
            });

            fillColorController.init(this, baseName + "FillColor", Color.WHITE);
            fillColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue v, Paint ov, Paint nv) {
                    updateMask();
                }
            });

            dottedCheck.setSelected(UserConfig.getBoolean("ImagePenDotted", false));
            dottedCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    UserConfig.setBoolean("ImagePenDotted", dottedCheck.isSelected());
                    updateMask();
                }
            });

            fillCheck.setSelected(UserConfig.getBoolean("ImagePenFill", false));
            fillCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_toggle, Boolean new_toggle) {
                    UserConfig.setBoolean("ImagePenFill", fillCheck.isSelected());
                    updateMask();
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

            blendController.setParameters(this);
            blendController.optionChangedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    updateMask();
                }
            });

            intensitySelector.getItems().addAll(Arrays.asList("20", "50", "10", "5", "80", "100", "15", "20", "60"));
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
                            UserConfig.setInt("ImageMosaicIntensity", v);
                        } else {
                            ValidationTools.setEditorBadStyle(intensitySelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(intensitySelector);
                    }
                }
            });
            intensitySelector.getSelectionModel().select(UserConfig.getInt("ImageMosaicIntensity", 20) + "");

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
            imageController.imageTab();
            setBox.getChildren().clear();
            imageController.clearMaskShapes();
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
            setBlender();
            RadioButton selected = (RadioButton) typeGroup.getSelectedToggle();
            if (rectangleRadio.equals(selected)) {
                opType = PenType.Rectangle;
                imageController.showMaskRectangle();
                imageController.maskRectangle.setOpacity(0);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, anchorColorPane, anchorSizePane,
                        dottedCheck, fillPane, rectArcPane, blendBox);
                commentsLabel.setText(message("PenRectangleTips"));
                strokeWidthKey = "ImagePenShapeWidth";
                defaultStrokeWidth = 5;

            } else if (circleRadio.equals(selected)) {
                opType = PenType.Circle;
                imageController.showMaskCircle();
                imageController.maskCircle.setOpacity(0);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, anchorColorPane, anchorSizePane,
                        dottedCheck, fillPane, blendBox);
                commentsLabel.setText(message("PenCircleTips"));
                strokeWidthKey = "ImagePenShapeWidth";
                defaultStrokeWidth = 5;

            } else if (ellipseRadio.equals(selected)) {
                opType = PenType.Ellipse;
                imageController.showMaskEllipse();
                imageController.maskEllipse.setOpacity(0);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, anchorColorPane, anchorSizePane,
                        dottedCheck, fillPane, blendBox);
                commentsLabel.setText(message("PenEllipseTips"));
                strokeWidthKey = "ImagePenShapeWidth";
                defaultStrokeWidth = 5;

            } else if (polygonRadio.equals(selected)) {
                opType = PenType.Polygon;
                imageController.showMaskPolygon();
                imageController.maskPolygon.setOpacity(0);
                withdrawButton.setVisible(true);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, anchorColorPane, anchorSizePane,
                        dottedCheck, fillPane, blendBox);
                commentsLabel.setText(message("PenPolygonTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (polylineRadio.equals(selected)) {
                opType = PenType.Polyline;
                imageController.showMaskPolyline();
                withdrawButton.setVisible(true);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane, anchorColorPane, anchorSizePane,
                        dottedCheck, blendBox);
                commentsLabel.setText(message("PenPolylineTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (linesRadio.equals(selected)) {
                opType = PenType.DrawLines;
                imageController.showMaskPenlines();
                withdrawButton.setVisible(true);
                setBox.getChildren().addAll(strokeWidthPane, strokeColorPane,
                        dottedCheck, blendBox);
                commentsLabel.setText(message("PenLinesTips"));
                strokeWidthKey = "ImagePenLineWidth";
                defaultStrokeWidth = 5;

            } else if (eraserRadio.equals(selected)) {
                opType = PenType.Erase;
                imageController.showMaskPenlines();
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
            isSettingValues = true;
            strokeWidthSelector.getSelectionModel().select(UserConfig.getInt(strokeWidthKey, defaultStrokeWidth) + "");
            isSettingValues = false;

            refreshStyle(setBox);
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
                drawPolyline();
                break;
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
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = PenTools.drawRectangle(imageView.getImage(),
                        imageController.maskRectangleData, (Color) strokeColorController.rect.getFill(), strokeWidth,
                        arcWidth, dottedCheck.isSelected(), fillCheck.isSelected(), (Color) fillColorController.rect.getFill(),
                        blendController.opacity, blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                maskView.setImage(newImage);
                imageController.drawMaskRectangle();
                imageController.maskRectangle.setOpacity(0);
            }

        };
        start(task);
    }

    public void drawCircle() {
        if (isSettingValues || opType != PenType.Circle
                || imageView == null || imageView.getImage() == null
                || imageController.maskCircleData == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = PenTools.drawCircle(imageView.getImage(),
                        imageController.maskCircleData, (Color) strokeColorController.rect.getFill(), strokeWidth,
                        dottedCheck.isSelected(), fillCheck.isSelected(), (Color) fillColorController.rect.getFill(),
                        blendController.opacity, blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                maskView.setImage(newImage);
                imageController.drawMaskCircle();
                imageController.maskCircle.setOpacity(0);
            }

        };
        start(task);
    }

    public void drawEllipse() {
        if (isSettingValues || opType != PenType.Ellipse
                || imageView == null || imageView.getImage() == null
                || imageController.maskEllipseData == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = PenTools.drawEllipse(imageView.getImage(),
                        imageController.maskEllipseData, (Color) strokeColorController.rect.getFill(), strokeWidth,
                        dottedCheck.isSelected(), fillCheck.isSelected(), (Color) fillColorController.rect.getFill(),
                        blendController.opacity, blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                maskView.setImage(newImage);
                imageController.drawMaskEllipse();
                imageController.maskEllipse.setOpacity(0);
            }

        };
        start(task);
    }

    public void drawPolygon() {
        if (isSettingValues || opType != PenType.Polygon
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
                newImage = PenTools.drawPolygon(imageView.getImage(),
                        imageController.maskPolygonData, (Color) strokeColorController.rect.getFill(), strokeWidth,
                        dottedCheck.isSelected(), fillCheck.isSelected(), (Color) fillColorController.rect.getFill(),
                        blendController.opacity, blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                maskView.setImage(newImage);
                imageController.maskPolygon.setOpacity(0);
            }

        };
        start(task);
    }

    public void drawPolyline() {
        if (isSettingValues || opType != PenType.Polyline
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
                newImage = PenTools.drawLines(imageView.getImage(),
                        imageController.maskPolylineData,
                        (Color) strokeColorController.rect.getFill(), strokeWidth, dottedCheck.isSelected(),
                        blendController.opacity, blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                maskView.setImage(newImage);
                imageController.maskPolyline.setOpacity(0);
            }

        };
        start(task);
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

    public void drawLines() {
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
                newImage = imageView.getImage();
                switch (opType) {
                    case Erase:
                        newImage = PenTools.drawErase(imageView.getImage(), imageController.maskPenData, strokeWidth);
                        break;
                    case DrawLines:
                        newImage = PenTools.drawLines(imageView.getImage(),
                                imageController.maskPenData,
                                (Color) strokeColorController.rect.getFill(), strokeWidth, dottedCheck.isSelected(),
                                blendController.opacity, blendController.blender());
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
        start(task);
    }

    public void drawLine(DoublePoint lastPoint, DoublePoint thisPoint) {
        Line penLine;
        if (opType == PenType.Erase) {
            penLine = imageController.drawMaskPenLine(strokeWidth, Color.TRANSPARENT,
                    false, 1.0f, lastPoint, thisPoint);
        } else {
            penLine = imageController.drawMaskPenLine(strokeWidth, (Color) strokeColorController.rect.getFill(),
                    dottedCheck.isSelected(), blendController.opacity, lastPoint, thisPoint);
        }

        if (penLine != null) {
            if (penLines == null) {
                penLines = new ArrayList<>();
            }
            penLines.add(penLine);
        }
    }

    public void clearPenLines() {
        if (penLines != null) {
            for (Line line : penLines) {
                imageController.maskPane.getChildren().remove(line);
            }
            penLines = null;
        }
        lastPoint = null;
    }

    protected void setBlender() {
        blendController.backImage = imageView.getImage();
        blendController.foreImage = FxImageTools.createImage((int) (imageView.getImage().getWidth() / 2), (int) (imageView.getImage().getHeight() / 2),
                strokeColorController.color());
        blendController.x = (int) (blendController.backImage.getWidth() - blendController.foreImage.getWidth()) / 2;
        blendController.y = (int) (blendController.backImage.getHeight() - blendController.foreImage.getHeight()) / 2;
    }

    @FXML
    @Override
    public void withdrawAction() {
        if (null == opType || imageView == null || imageView.getImage() == null) {
            return;
        }
        switch (opType) {
            case Polygon:
                imageController.removeMaskPolygonLastPoint();
                drawPolygon();
                break;
            case Polyline:
                imageController.removeMaskPolylineLastPoint();
                drawPolyline();
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
    public void paneClicked(MouseEvent event, DoublePoint p) {
        if (null == opType || imageView == null || imageView.getImage() == null || p == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (imageController.isPickingColor) {
            Color color = ImageViewTools.imagePixel(p, imageView);
            if (color != null) {
                strokeColorController.setColor(color);
            }
            return;
        }
        switch (opType) {
            case Rectangle:
            case Circle:
            case Ellipse:
            case Polyline:
            case Polygon:
            case DrawLines:
            case Erase: {
                updateMask();
                break;
            }
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
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
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
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
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
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        if (coordinatePenCheck.isSelected()) {
            imageController.showXY(event, p);
        }

        if (event.getButton() == MouseButton.SECONDARY || p == null) {
            return;
        }
        switch (opType) {
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

}
