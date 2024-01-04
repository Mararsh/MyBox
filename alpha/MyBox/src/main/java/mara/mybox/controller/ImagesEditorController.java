package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-5-27
 * @License Apache License Version 2.0
 */
public class ImagesEditorController extends BaseController {

    @FXML
    protected ControlImagesTable tableController;
    @FXML
    protected BaseImageController viewController;

    public ImagesEditorController() {
        baseTitle = Languages.message("ImagesEditor");
        TipsLabelKey = "ImagesEditorTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            tableController.parentController = this;
            tableController.parentFxml = myFxml;
            tableController.tableView.getColumns().remove(tableController.currentIndexColumn);
            viewController.backgroundLoad = true;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tableController.tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    viewImage();
                }
            });

            playButton.disableProperty().bind(Bindings.isEmpty(tableController.tableData));
            saveAsButton.disableProperty().bind(Bindings.isEmpty(tableController.tableData));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void open(File file) {
        tableController.addFile(file);
    }

    public void loadImages(List<ImageInformation> infos) {
        if (infos == null || infos.isEmpty()) {
            return;
        }
        for (ImageInformation info : infos) {
            tableController.tableData.add(info.cloneAttributes());
        }
    }

    public List<ImageInformation> selected() {
        List<ImageInformation> list = tableController.selectedItems();
        if (list == null || list.isEmpty()) {
            list = tableController.tableData;
        }
        if (list == null || list.isEmpty()) {
            popError(message("NoData"));
        }
        return list;
    }

    public void viewImage() {
        ImageInformation info = tableController.selectedItem();
        if (info == null) {
            return;
        }
        viewController.loadImageInfo(info);
    }

    @FXML
    @Override
    public void playAction() {
        try {
            List<ImageInformation> list = selected();
            if (list == null || list.isEmpty()) {
                return;
            }
            ImagesPlayController.playImages(list);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        try {
            List<ImageInformation> list = selected();
            if (list == null || list.isEmpty()) {
                return;
            }
            ImagesSaveController.saveImages(this, list);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void editFrames() {
        try {
            List<ImageInformation> list = selected();
            if (list == null || list.isEmpty()) {
                return;
            }
            openImages(list);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (tableController != null) {
                return tableController.keyEventsFilter(event);
            }
            return false;
        } else {
            return true;
        }
    }

    /*
        static methods
     */
    public static ImagesEditorController openFiles(List<File> files) {
        try {
            ImagesEditorController controller = (ImagesEditorController) WindowTools.openStage(Fxmls.ImagesEditorFxml);
            controller.tableController.addFiles(0, files);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImagesEditorController openImages(List<ImageInformation> infos) {
        try {
            ImagesEditorController controller = (ImagesEditorController) WindowTools.openStage(Fxmls.ImagesEditorFxml);
            controller.loadImages(infos);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
