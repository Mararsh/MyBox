package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ImageClipboardTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-6-5
 * @License Apache License Version 2.0
 */
public class ImageInMyBoxClipboardController extends BaseImageClipController {

    @FXML
    protected ControlImageView imageController;
    @FXML
    protected VBox tableBox, viewBox;

    public ImageInMyBoxClipboardController() {
        baseTitle = message("ImagesInMyBoxClipboard");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            refreshAction();

        } catch (Exception e) {
        }
    }

    @Override
    protected void checkButtons() {
        if (isSettingValues) {
            return;
        }
        super.checkButtons();
        boolean none = isNoneSelected();
        copyToSystemClipboardButton.setDisable(none);
    }

    @Override
    public void itemClicked() {
        ImageClipboard clip = selectedItem();
        if (clip == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            Image image;

            @Override
            protected boolean handle() {
                try {
                    image = ImageClipboard.loadImage(this, clip);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (image != null) {
                    imageController.loadImage(image);
                }
            }
        };
        start(task);
    }

    @Override
    protected int deleteData(FxTask currentTask, List<ImageClipboard> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        for (ImageClipboard clip : data) {
            FileDeleteTools.delete(clip.getImageFile());
            FileDeleteTools.delete(clip.getThumbnailFile());
        }
        return tableDefinition.deleteData(data);
    }

    @Override
    protected void afterClear() {
        super.afterClear();
        FileDeleteTools.clearDir(null, new File(AppPaths.getImageClipboardPath()));
    }

    @FXML
    @Override
    public void refreshAction() {
        loadTableData();
        if (ImageClipboardTools.isMonitoring()) {
            bottomLabel.setText(message("MonitoringImageInSystemClipboard"));
        } else {
            bottomLabel.setText(message("NotMonitoringImageInSystemClipboard"));
        }
    }

    @FXML
    @Override
    public void copyToSystemClipboard() {
        ImageClipboard clip = selectedItem();
        if (clip == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private Image selectedImage;

            @Override
            protected boolean handle() {
                selectedImage = ImageClipboard.loadImage(this, clip);
                return selectedImage != null;
            }

            @Override
            protected void whenSucceeded() {
                ImageClipboardTools.copyToSystemClipboard(parentController, selectedImage);
            }
        };
        parentController.start(task);
    }

    @FXML
    @Override
    public void systemClipBoard() {
        ImageInSystemClipboardController.oneOpen();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (viewBox.isFocused() || viewBox.isFocusWithin()) {
            if (imageController.keyEventsFilter(event)) {
                return true;
            }
        }
        if (super.keyEventsFilter(event)) {
            return true;
        }
        return imageController.keyEventsFilter(event);
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
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (ImageInMyBoxClipboardController) WindowTools.openStage(Fxmls.ImageInMyBoxClipboardFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static void updateClipboards() {
        Platform.runLater(() -> {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (!(window instanceof Stage)) {
                    continue;
                }
                Stage stage = (Stage) window;
                Object controller = stage.getUserData();
                if (controller == null) {
                    continue;
                }
                if (controller instanceof ImageInMyBoxClipboardController) {
                    try {
                        ((ImageInMyBoxClipboardController) controller).refreshAction();
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

    public static void updateClipboardsStatus() {
        Platform.runLater(() -> {
            List<Window> windows = new ArrayList<>();
            windows.addAll(Window.getWindows());
            for (Window window : windows) {
                if (!(window instanceof Stage)) {
                    continue;
                }
                Stage stage = (Stage) window;
                Object controller = stage.getUserData();
                if (controller == null) {
                    continue;
                }
                if (controller instanceof ImageInMyBoxClipboardController) {
                    try {
                        ((ImageInMyBoxClipboardController) controller).updateStatus();
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

}
