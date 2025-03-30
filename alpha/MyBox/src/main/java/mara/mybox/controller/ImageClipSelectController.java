package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
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

    @FXML
    @Override
    public boolean popAction() {
        ImageClipboard selected = selectedItem();
        if (selected == null) {
            popError(message("SelectToHandle"));
            return false;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private Image clip;

            @Override
            protected boolean handle() {
                try {
                    clip = ImageClipboard.loadImage(this, selected);
                    return clip != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ImagePopController.openImage(myController, clip);
            }

        };
        start(task);
        return true;
    }

    @Override
    protected void checkButtons() {
        super.checkButtons();
        popButton.setDisable(isNoneSelected());
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
        task = new FxSingletonTask<Void>(this) {

            private Image clip;

            @Override
            protected boolean handle() {
                try {
                    clip = ImageClipboard.loadImage(this, selected);
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
    public void manageAction() {
        ImageInMyBoxClipboardController.oneOpen();
        setIconified(true);
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
            ImageClipSelectController controller = (ImageClipSelectController) WindowTools.childStage(
                    parent, Fxmls.ImageClipSelectFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
