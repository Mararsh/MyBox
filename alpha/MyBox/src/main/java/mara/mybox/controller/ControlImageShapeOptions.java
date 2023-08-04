package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.data.DoubleShape;
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
public class ControlImageShapeOptions extends ControlShapeOptions {

    protected ImageManufactureShapeController shapeController;
    protected ImageManufactureController editor;
    protected ImageView maskView, imageView;

    @FXML
    protected Label commentsLabel;
    @FXML
    protected ControlImagesBlend blendController;
    @FXML
    protected CheckBox coordinatePenCheck;

    public void setParameters(ImageManufactureShapeController penController) {
        try {
            this.shapeController = penController;
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

            blendController.optionChangedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    redrawShape();
                }
            });

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
                commentsLabel.setText(message("ShapeDragMoveComments"));

            } else if (circleRadio.isSelected()) {
                commentsLabel.setText(message("ShapeDragMoveComments"));

            } else if (ellipseRadio.isSelected()) {
                commentsLabel.setText(message("ShapeDragMoveComments"));

            } else if (polygonRadio.isSelected()) {
                withdrawButton.setDisable(false);
                clearButton.setDisable(false);
                commentsLabel.setText(message("ShapePointsMoveComments"));

            } else if (polylineRadio.isSelected()) {
                withdrawButton.setDisable(false);
                clearButton.setDisable(false);
                commentsLabel.setText(message("ShapePointsMoveComments"));

            } else if (linesRadio.isSelected()) {
                withdrawButton.setDisable(false);
                clearButton.setDisable(false);
                commentsLabel.setText(message("MultipleLinesTips"));

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
    public void shapeDataChanged() {
        setShapeControls();
        redrawShape();
    }

    @Override
    public void redrawShape() {
        if (imageController == null || shapeType == null) {
            return;
        }
        DoubleShape shapeData = editor.currentMaskShapData();
        if (shapeData == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private Image newImage;

            @Override
            protected boolean handle() {
                newImage = ShapeTools.drawShape(imageView.getImage(),
                        shapeData, style, blendController.blender());
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
                editor.drawMaskShape();
                editor.hideMaskShape();
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void withdrawAction() {
        if (null == shapeType || imageView == null || imageView.getImage() == null) {
            return;
        }
        switch (shapeType) {
            case Polyline:
            case Polygon:
                pointsController.tableData.remove(pointsController.tableData.size() - 1);
                break;
            case Lines:
                linesController.tableData.remove(linesController.tableData.size() - 1);
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
        if (null == shapeType || imageView == null || imageView.getImage() == null) {
            return;
        }
        switch (shapeType) {
            case Polyline:
            case Polygon:
                pointsController.tableData.clear();
                break;
            case Lines:
                linesController.tableData.clear();
                break;
        }
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
