package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-6-5
 * @License Apache License Version 2.0
 */
public class ImageClipSelectController extends BaseImageClipController {

    protected ImagePasteController pasteController;

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    protected void setParameters(ImagePasteController controller) {
        try {
            if (controller == null) {
                close();
            }
            pasteController = controller;

            tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            okButton.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());

            refreshAction();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void itemDoubleClicked() {
        okAction();
    }

    @FXML
    @Override
    public void okAction() {
        if (pasteController == null) {
            close();
        }
        ImageClipboard selected = selectedItem();
        if (selected == null) {
            popError(message("SelectToHandle"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            private Image clip;

            @Override
            protected boolean handle() {
                try {
                    clip = selected.getImage();
                    if (clip == null) {
                        clip = FxImageTools.readImage(selected.getImageFile());
                    }
                    return clip != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                pasteController.setSourceClip(clip);
            }

        };
        start(task);

    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public boolean keyESC() {
        close();
        return false;
    }

    /*
        static methods
     */
    public static ImageClipSelectController open(ImagePasteController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageClipSelectController controller = (ImageClipSelectController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageClipSelectFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
