package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-6-5
 * @License Apache License Version 2.0
 */
public class ImageInMyBoxClipboardController extends ImageViewerController {

    @FXML
    protected ControlImagesClipboard clipsController;

    public ImageInMyBoxClipboardController() {
        baseTitle = Languages.message("ImagesInMyBoxClipboard");
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            clipsController.setParameters(this, false);

            clipsController.tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    selectClip();
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void selectClip() {
        if (clipsController.tableData.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    try {
                        ImageClipboard clip = clipsController.tableView.getSelectionModel().getSelectedItem();
                        if (clip == null) {
                            clip = clipsController.tableData.get(0);
                        }
                        image = ImageClipboard.loadImage(clip);
                        return image != null;
                    } catch (Exception e) {
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    afterImageLoaded();
                    setImageChanged(false);
                }
            };
            start(task);
        }
    }

    /*
        static methods
     */
    public static ImageInMyBoxClipboardController oneOpen() {
        ImageInMyBoxClipboardController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof ImageInMyBoxClipboardController) {
                try {
                    controller = (ImageInMyBoxClipboardController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (ImageInMyBoxClipboardController) WindowTools.openStage(Fxmls.ImageInMyBoxClipboardFxml);
        }
        return controller;
    }

}
