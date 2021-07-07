package mara.mybox.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlWindow;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2021-6-5
 * @License Apache License Version 2.0
 */
public class ImagesInMyBoxClipboardController extends ImageViewerController {

    @FXML
    protected ControlImagesClipboard clipsController;

    public ImagesInMyBoxClipboardController() {
        baseTitle = message("ImagesInMyBoxClipboard");
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

    protected void loadClipboard() {
        clipsController.loadTableData();
    }

    public void selectClip() {
        if (clipsController.tableData.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

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
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    /*
        static methods
     */
    public static ImagesInMyBoxClipboardController oneOpen() {
        ImagesInMyBoxClipboardController controller = null;
        Stage stage = FxmlWindow.findStage(message("ImagesInMyBoxClipboard"));
        if (stage != null && stage.getUserData() != null) {
            try {
                controller = (ImagesInMyBoxClipboardController) stage.getUserData();
            } catch (Exception e) {
            }
        }
        if (controller == null) {
            controller = (ImagesInMyBoxClipboardController) FxmlWindow.openStage(CommonValues.ImagesInMyBoxClipboardFxml);
        }
        return controller;
    }

}
