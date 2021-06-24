package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.ImageInformation;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2021-5-27
 * @License Apache License Version 2.0
 */
public class ImagesEditorController extends BaseController {

    protected ObservableList<ImageInformation> tableData;
    protected TableView<ImageInformation> tableView;

    @FXML
    protected ControlImagesTable tableController;
    @FXML
    protected ControlImagesSave saveController;

    public ImagesEditorController() {
        baseTitle = message("ImagesEditor");
        TipsLabelKey = "ImagesEditorTips";
    }

    @Override
    public void setFileType() {
        setFileType(FileType.Image);
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

            saveController.setParameters(this);

            playButton.disableProperty().bind(Bindings.isEmpty(tableData));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkImages() {
        List<ImageInformation> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            selected = tableData;
        }
        saveController.setImages(selected);
    }

    public void open(File file) {
        tableController.addFile(sourceFile);
    }

    @FXML
    @Override
    public void playAction() {
        try {
            List<ImageInformation> selected = tableView.getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                selected = tableData;
            }
            List<ImageInformation> infos = new ArrayList<>();
            if (selected != null) {
                for (ImageInformation info : selected) {
                    infos.add(info.base());
                }
            }
            ImagesPlayController controller
                    = (ImagesPlayController) openStage(CommonValues.ImagesPlayFxml);
            controller.loadImages(infos);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadImages(List<ImageInformation> infos) {
        if (infos != null && !infos.isEmpty()) {
            tableData.addAll(infos);
        }
    }

}
