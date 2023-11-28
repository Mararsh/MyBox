package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-10
 * @License Apache License Version 2.0
 */
public class ImageManufactureTextController extends ImageManufactureOperationController {

    @FXML
    protected ControlImageText optionsController;
    @FXML
    protected FlowPane setBox;
    @FXML
    protected HBox opBox;

    @Override
    public void initPane() {
        try {
            super.initPane();

            optionsController.setParameters(this, imageView);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void paneExpanded() {
        editor.showRightPane();
        editor.resetImagePane();
        editor.imageTab();
        editor.imageLabel.setText(message("ImageTextComments"));
    }

    @FXML
    @Override
    public void paneClicked(MouseEvent event, DoublePoint p) {
        translateTo(event, p);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        DoublePoint p = ImageViewTools.getImageXY(event, imageView);
        translateTo(event, p);
    }

    public void translateTo(MouseEvent event, DoublePoint p) {
        if (imageView.getImage() == null || p == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (editor.isPickingColor || scopeController.isPickingColor
                || event.getButton() == MouseButton.SECONDARY) {
            return;
        }
        imageView.setCursor(Cursor.HAND);
        optionsController.setLocation((int) Math.round(p.getX()), (int) Math.round(p.getY()));
        goAction();
    }

    public void write(boolean editing) {
        String text = optionsController.text();
        if (isSettingValues || optionsController.x < 0 || optionsController.y < 0
                || optionsController.x >= imageView.getImage().getWidth()
                || optionsController.y >= imageView.getImage().getHeight()
                || text.isEmpty()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image newImage = null;

            @Override
            protected boolean handle() {
//                newImage = FxImageTools.addText(imageView.getImage(), optionsController);
                if (task == null || isCancelled()) {
                    return false;
                }
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                if (editing) {
                    maskView.setImage(newImage);
                    maskView.setOpacity(1);
                    maskView.setVisible(true);
                    imageView.setVisible(false);
                    imageView.toBack();

                } else {
                    editor.popSuccessful();
                    editor.updateImage(ImageOperation.Text, null, null, newImage, cost);
                }
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void goAction() {
        if (!optionsController.pickValues()) {
            popError(Languages.message("InvalidParameters"));
            return;
        }
        write(true);
    }

    @FXML
    @Override
    public void okAction() {
        write(false);
    }

    @FXML
    @Override
    public void cancelAction() {
        editor.resetImagePane();
    }

}
