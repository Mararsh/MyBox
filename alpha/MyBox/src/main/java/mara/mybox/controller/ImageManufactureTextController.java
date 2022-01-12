package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.value.Languages;

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
    @FXML
    protected Label commentsLabel;

    @Override
    public void initPane() {
        try {
            optionsController.setParameters(this, imageView);

            optionsController.changeNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    write(true);
                }
            });

            goAction();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        imageController.resetImagePane();
        imageController.imageTab();
    }

    @FXML
    @Override
    public void imageClicked(MouseEvent event, DoublePoint p) {
        if (imageView.getImage() == null || p == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        if (imageController.isPickingColor || scopeController.isPickingColor
                || event.getButton() == MouseButton.SECONDARY) {
            return;
        }
        imageView.setCursor(Cursor.HAND);
        optionsController.setLocation((int) Math.round(p.getX()), (int) Math.round(p.getY()));
        goAction();
    }

    public void write(boolean editing) {
        String text = optionsController.getText();
        if (isSettingValues || optionsController.x < 0 || optionsController.y < 0
                || optionsController.x >= imageView.getImage().getWidth()
                || optionsController.y >= imageView.getImage().getHeight()
                || text.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxImageTools.addText(imageView.getImage(), optionsController);
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
                        imageController.popSuccessful();
                        imageController.updateImage(ImageOperation.Text, null, null, newImage, cost);
                    }
                }

            };
            imageController.start(task);
        }
    }

    @FXML
    @Override
    public void goAction() {
        if (!optionsController.checkParameters()) {
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
        imageController.resetImagePane();
    }

}
