package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-5-27
 * @License Apache License Version 2.0
 */
public class ImagesEditorController extends BaseImagesListController {

    protected ObservableList<ImageInformation> tableData;
    protected TableView<ImageInformation> tableView;

    @FXML
    protected ControlImagesTable tableController;

    public ImagesEditorController() {
        baseTitle = Languages.message("ImagesEditor");
        TipsLabelKey = "ImagesEditorTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tableController.parentController = this;
            tableController.parentFxml = myFxml;
            tableData = tableController.tableData;
            tableView = tableController.tableView;

            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkImages();
                }
            });

            tableData.addListener(new ListChangeListener<ImageInformation>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends ImageInformation> change) {
                    checkImages();
                }
            });

            playButton.disableProperty().bind(Bindings.isEmpty(imageInfos));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkImages() {
        List<ImageInformation> selected = tableController.selectedItems();
        if (selected == null || selected.isEmpty()) {
            selected = tableData;
        }
        imageInfos.setAll(selected);
    }

    public void open(File file) {
        tableController.addFile(file);
    }

    public void loadImages(List<ImageInformation> infos) {
        setImages(infos);
        tableData.setAll(imageInfos);
    }

    @FXML
    @Override
    public void playAction() {
        try {
            ImagesPlayController controller = (ImagesPlayController) openStage(Fxmls.ImagesPlayFxml);
            controller.loadImages(imageInfos);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        static methods
     */
    public static ImagesEditorController open(List<File> files) {
        try {
            ImagesEditorController controller = (ImagesEditorController) WindowTools.openStage(Fxmls.ImagesEditorFxml);
            controller.tableController.addFiles(0, files);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
