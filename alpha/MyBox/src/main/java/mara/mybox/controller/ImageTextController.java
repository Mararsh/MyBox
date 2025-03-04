package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.ImageViewTools;
import mara.mybox.fxml.image.ShapeDemos;
import mara.mybox.image.tools.ImageTextTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-4
 * @License Apache License Version 2.0
 */
public class ImageTextController extends BaseImageEditController {

    @FXML
    protected ControlImageText optionsController;

    public ImageTextController() {
        baseTitle = message("Text");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Text");

            optionsController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            optionsController.centerRadio.setSelected(true);

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
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
        if (isPickingColor || event.getButton() == MouseButton.SECONDARY) {
            return;
        }
        imageView.setCursor(Cursor.HAND);
        optionsController.setLocation((int) Math.round(p.getX()), (int) Math.round(p.getY()));
        goAction();
    }

    @FXML
    @Override
    public synchronized void goAction() {
        if (isSettingValues || !optionsController.checkValues()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private Image newImage;

            @Override
            protected boolean handle() {
                if (!optionsController.pickValues()) {
                    return false;
                }
                BufferedImage target = ImageTextTools.addText(this,
                        SwingFXUtils.fromFXImage(srcImage(), null),
                        optionsController);
                if (task == null || isCancelled()) {
                    return false;
                }
                newImage = SwingFXUtils.toFXImage(target, null);
                return newImage != null;
            }

            @Override
            protected void whenSucceeded() {
                if (isCancelled()) {
                    return;
                }
                imageView.setImage(newImage);
            }

            @Override
            protected void whenCanceled() {
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(task, okButton);
    }

    @Override
    protected void handleImage(FxTask currentTask) {
        handledImage = imageView.getImage();
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        ShapeDemos.text(currentTask, files,
                SwingFXUtils.fromFXImage(srcImage(), null),
                optionsController, srcFile());
    }

    /*
        static methods
     */
    public static ImageTextController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageTextController controller = (ImageTextController) WindowTools.operationStage(
                    parent, Fxmls.ImageTextFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
