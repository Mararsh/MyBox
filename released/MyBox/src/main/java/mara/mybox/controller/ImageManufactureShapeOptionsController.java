package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fximage.ShapeTools;
import mara.mybox.fxml.SingletonCurrentTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @License Apache License Version 2.0
 */
public class ImageManufactureShapeOptionsController extends ControlShapeOptions {

    protected ImageManufactureShapeController penController;
    protected ImageManufactureController editor;
    protected ImageView maskView, imageView;
    protected List<Line> currentLine;
    protected DoublePoint lastPoint;

    @FXML
    protected Label commentsLabel;
    @FXML
    protected ControlImagesBlend blendController;
    @FXML
    protected CheckBox coordinatePenCheck;

    public void setParameters(ImageManufactureShapeController penController) {
        try {
            this.penController = penController;
            editor = penController.editor;
            maskView = editor.maskView;
            imageView = editor.imageView;

            super.setParameters(editor);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void switchShape() {
        super.switchShape();
        redrawShape();
    }

    @Override
    public void initShapeControls() {
        try {
            super.initShapeControls();

            blendController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    public void setShapeControls() {
        try {
            super.setShapeControls();

            maskView.setImage(imageView.getImage());
            maskView.setOpacity(1);
            maskView.setVisible(true);
            imageView.setVisible(false);
            imageView.toBack();
            setBlender();
            withdrawButton.setDisable(true);
            clearButton.setDisable(true);
            if (rectangleRadio.isSelected()) {
                commentsLabel.setText(message("PenRectangleTips"));

            } else if (circleRadio.isSelected()) {
                commentsLabel.setText(message("PenCircleTips"));

            } else if (ellipseRadio.isSelected()) {
                commentsLabel.setText(message("PenEllipseTips"));

            } else if (polygonRadio.isSelected()) {
                withdrawButton.setDisable(false);
                clearButton.setDisable(false);
                commentsLabel.setText(message("PenPolygonTips"));

            } else if (polylineRadio.isSelected()) {
                withdrawButton.setDisable(false);
                clearButton.setDisable(false);
                commentsLabel.setText(message("PenPolylineTips"));

            } else if (linesRadio.isSelected()) {
                withdrawButton.setDisable(false);
                clearButton.setDisable(false);
                commentsLabel.setText(message("PenLinesTips"));

            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setBlender() {
        blendController.backImage = imageView.getImage();
        blendController.foreImage = FxImageTools.createImage(
                (int) (imageView.getImage().getWidth() / 2), (int) (imageView.getImage().getHeight() / 2),
                strokeColorController.color());
        blendController.x = (int) (blendController.backImage.getWidth() - blendController.foreImage.getWidth()) / 2;
        blendController.y = (int) (blendController.backImage.getHeight() - blendController.foreImage.getHeight()) / 2;
    }

    @Override
    public void drawRectangle() {
        if (isSettingValues || imageView == null || imageView.getImage() == null
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
                newImage = ShapeTools.drawRectangle(imageView.getImage(),
                        imageController.maskRectangleData, style,
                        blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                maskView.setImage(newImage);
                editor.drawMaskRectangle();
                editor.maskRectangle.setOpacity(0);
            }

        };
        start(task);
    }

    @Override
    public void drawCircle() {
        if (isSettingValues || imageView == null || imageView.getImage() == null
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
                newImage = ShapeTools.drawCircle(imageView.getImage(),
                        imageController.maskCircleData, style,
                        blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                maskView.setImage(newImage);
                editor.drawMaskCircle();
                editor.maskCircle.setOpacity(0);
            }

        };
        start(task);
    }

    @Override
    public void drawEllipse() {
        if (isSettingValues || imageView == null || imageView.getImage() == null
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
                newImage = ShapeTools.drawEllipse(imageView.getImage(),
                        imageController.maskEllipseData, style,
                        blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                maskView.setImage(newImage);
                editor.drawMaskEllipse();
                editor.maskEllipse.setOpacity(0);
            }

        };
        start(task);
    }

    @Override
    public void drawPolygon() {
        if (isSettingValues || imageView == null || imageView.getImage() == null
                || imageController.maskPolygonData == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = ShapeTools.drawPolygon(imageView.getImage(),
                        imageController.maskPolygonData, style,
                        blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                maskView.setImage(newImage);
                editor.drawMaskPolygon();
                editor.maskPolygon.setOpacity(0);
            }

        };
        start(task);
    }

    @Override
    public void drawPolyline() {
        if (isSettingValues || imageController.maskPolylineData == null
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
                newImage = ShapeTools.drawPolyLines(imageView.getImage(),
                        imageController.maskPolylineData, style,
                        blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                maskView.setImage(newImage);
                editor.drawMaskPolyline();
                editor.maskPolyline.setOpacity(0);
            }

        };
        start(task);
    }

    @Override
    public void drawLines() {
        if (isSettingValues || imageView == null || imageView.getImage() == null
                || imageController.maskLinesData == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = ShapeTools.drawLines(imageView.getImage(),
                        imageController.maskLinesData, style,
                        blendController.blender());
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

                if (currentLine != null) {
                    for (Line line : currentLine) {
                        imageController.maskPane.getChildren().remove(line);
                    }
                    currentLine = null;
                }
                lastPoint = null;

                updateLinesList();
            }

        };
        start(task);
    }

    public void updateLinesList() {
        linesController.loadList(imageController.maskLinesData.getLinePoints());
    }

    public void drawLinePoint(DoublePoint thisPoint) {
        Line line = imageController.drawMaskLinesLine(lastPoint, thisPoint);
        if (line != null) {
            if (currentLine == null) {
                currentLine = new ArrayList<>();
            }
            line.setStroke(Color.RED);
            line.setStrokeWidth(10);
            line.getStrokeDashArray().clear();
            currentLine.add(line);
        }
        editor.maskLinesData.addPoint(thisPoint);
        updateLinesList();
    }

    @Override
    public void shapeDataChanged() {
        setShapeControls();
        redrawShape();
    }

    @FXML
    @Override
    public void withdrawAction() {
        if (null == shapeType || imageView == null || imageView.getImage() == null) {
            return;
        }
        switch (shapeType) {
            case Polyline:
                imageController.removeMaskPolylineLastPoint();
                drawPolyline();
                break;
            case Polygon:
                imageController.removeMaskPolygonLastPoint();
                drawPolygon();
                break;
            case Lines:
                imageController.maskLinesData.removeLastLine();
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
        if (polygonRadio.isSelected()) {
            editor.maskPolygonData.clear();

        } else if (polylineRadio.isSelected()) {
            editor.maskPolylineData.clear();

        } else if (linesRadio.isSelected()) {
            editor.maskLinesData.clear();

        }
        redrawShape();
    }

    @FXML
    @Override
    public void okAction() {
        if (okButton.isDisabled() || shapeType == null) {
            return;
        }
        editor.popSuccessful();
        editor.updateImage(ImageOperation.Shape, shapeType.name(), null, maskView.getImage(), 0);
    }

}