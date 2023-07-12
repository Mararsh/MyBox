package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Line;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.data.DoubleLines;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoublePolygon;
import mara.mybox.data.DoublePolyline;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fximage.PenTools;
import mara.mybox.fxml.SingletonCurrentTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @License Apache License Version 2.0
 */
public class ImageManufacturePenOptionsController extends ControlShape {

    protected ImageManufactureController editor;
    protected ImageView maskView, imageView;
    protected List<Line> penLines;
    protected DoublePoint lastPoint;

    @FXML
    protected Label commentsLabel;
    @FXML
    protected ControlImagesBlend blendController;
    @FXML
    protected CheckBox coordinatePenCheck;

    public void setParameters(ImageManufactureController imageController) {
        try {
            editor = imageController;
            maskView = editor.maskView;
            imageView = editor.imageView;

            super.setParameters(editor);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initShapes() {
        try {
            super.initShapes();

            blendController.setParameters(this);
            blendController.optionChangedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    loadShape();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    public void loadShape() {
        try {
            maskView.setImage(imageView.getImage());
            maskView.setOpacity(1);
            maskView.setVisible(true);
            imageView.setVisible(false);
            imageView.toBack();
            withdrawButton.setVisible(false);
            setBlender();
            RadioButton selected = (RadioButton) typeGroup.getSelectedToggle();
            if (rectangleRadio.equals(selected)) {
                imageController.maskRectangle.setOpacity(0);
                commentsLabel.setText(message("PenRectangleTips"));

            } else if (circleRadio.equals(selected)) {
                imageController.maskCircle.setOpacity(0);
                commentsLabel.setText(message("PenCircleTips"));

            } else if (ellipseRadio.equals(selected)) {
                imageController.maskEllipse.setOpacity(0);
                commentsLabel.setText(message("PenEllipseTips"));

            } else if (polygonRadio.equals(selected)) {
                imageController.maskPolygon.setOpacity(0);
                withdrawButton.setVisible(true);
                commentsLabel.setText(message("PenPolygonTips"));

            } else if (polylineRadio.equals(selected)) {
                withdrawButton.setVisible(true);
                commentsLabel.setText(message("PenPolylineTips"));

            } else if (linesRadio.equals(selected)) {
                withdrawButton.setVisible(true);
                commentsLabel.setText(message("PenLinesTips"));

            }

            super.loadShape();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
                newImage = PenTools.drawRectangle(imageView.getImage(),
                        imageController.maskRectangleData, blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                editor.isSettingValues = true;
                editor.maskView.setImage(newImage);
                editor.showMaskRectangle();
                editor.maskRectangle.setOpacity(0);
                editor.isSettingValues = false;
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
                newImage = PenTools.drawCircle(imageView.getImage(),
                        imageController.maskCircleData, blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                editor.isSettingValues = true;
                maskView.setImage(newImage);
                editor.showMaskCircle();
                editor.maskCircle.setOpacity(0);
                editor.isSettingValues = false;
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
                newImage = PenTools.drawEllipse(imageView.getImage(),
                        imageController.maskEllipseData, blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                editor.isSettingValues = true;
                maskView.setImage(newImage);
                editor.showMaskEllipse();
                editor.maskEllipse.setOpacity(0);
                editor.isSettingValues = false;
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
                newImage = PenTools.drawPolygon(imageView.getImage(),
                        imageController.maskPolygonData, blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                editor.isSettingValues = true;
                maskView.setImage(newImage);
                editor.showMaskPolygon();
                editor.maskPolygon.setOpacity(0);
                editor.isSettingValues = false;
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
                newImage = PenTools.drawPolyLines(imageView.getImage(),
                        imageController.maskPolylineData, blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                editor.isSettingValues = true;
                maskView.setImage(newImage);
                editor.showMaskPolyline();
                editor.maskPolyline.setOpacity(0);
                editor.isSettingValues = false;
            }

        };
        start(task);
    }

    @Override
    public void drawLines() {
        if (isSettingValues || imageView == null || imageView.getImage() == null
                || imageController.maskPenData == null) {
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
                        imageController.maskPenData, blendController.blender());
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                if (isCancelled()) {
                    return;
                }
                editor.isSettingValues = true;
                maskView.setImage(newImage);
                maskView.setOpacity(1);
                maskView.setVisible(true);
                imageView.setVisible(false);
                imageView.toBack();
                clearPenLines();
                editor.isSettingValues = false;
            }

        };
        start(task);
    }

    protected void setBlender() {
        blendController.backImage = imageView.getImage();
        blendController.foreImage = FxImageTools.createImage((int) (imageView.getImage().getWidth() / 2), (int) (imageView.getImage().getHeight() / 2),
                strokeColorController.color());
        blendController.x = (int) (blendController.backImage.getWidth() - blendController.foreImage.getWidth()) / 2;
        blendController.y = (int) (blendController.backImage.getHeight() - blendController.foreImage.getHeight()) / 2;
    }

    public Line drawLine(DoublePoint thisPoint) {
        Line penLine = imageController.drawMaskPenLine(lastPoint, thisPoint);
        if (penLine != null) {
            if (penLines == null) {
                penLines = new ArrayList<>();
            }
            penLines.add(penLine);
            lastPoint = thisPoint;
        }
        return penLine;
    }

    public void drawLine(DoublePoint lastPoint, DoublePoint thisPoint) {
        Line penLine;
        penLine = imageController.drawMaskPenLine(lastPoint, thisPoint);

        if (penLine != null) {
            if (penLines == null) {
                penLines = new ArrayList<>();
            }
            penLines.add(penLine);
        }
    }

    public void clearPenLines() {
        if (null == imageController.maskShape || imageController.maskPane == null) {
            return;
        }
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
        if (null == imageController.maskShape || imageView == null || imageView.getImage() == null) {
            return;
        }
        if (imageController.maskShape instanceof DoublePolyline) {
            imageController.removeMaskPolylineLastPoint();
            drawPolyline();

        } else if (imageController.maskShape instanceof DoublePolygon) {
            imageController.removeMaskPolygonLastPoint();
            drawPolygon();

        } else if (imageController.maskShape instanceof DoubleLines) {
            imageController.maskPenData.removeLastLine();
            drawLines();

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
        switchShape();
    }

    @FXML
    @Override
    public void okAction() {
        if (okButton.isDisabled() || editor.maskShape == null) {
            return;
        }
        editor.popSuccessful();
        editor.updateImage(ImageOperation.Pen,
                imageController.maskShape.getClass().getSimpleName(),
                null, maskView.getImage(), 0);
    }

}
