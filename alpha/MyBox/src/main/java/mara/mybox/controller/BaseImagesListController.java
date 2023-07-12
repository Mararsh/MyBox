package mara.mybox.controller;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-9-23
 * @License Apache License Version 2.0
 */
public class BaseImagesListController extends ImageViewerController {

    protected ObservableList<ImageInformation> imageInfos = FXCollections.observableArrayList();

    @FXML
    protected ControlImagesSave saveController;

    public BaseImagesListController() {
        baseTitle = Languages.message("ImagesEditor");
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

            if (saveController != null) {
                saveController.setParent(this);
                saveButton = saveController.saveButton;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setImages(List<ImageInformation> infos) {
        imageInfos.clear();
        addImages(infos);
    }

    public void addImages(List<ImageInformation> infos) {
        if (infos != null && !infos.isEmpty()) {
            for (ImageInformation info : infos) {
                imageInfos.add(info.cloneAttributes());
            }
        }
    }

    @Override
    public void cleanPane() {
        try {
            if (imageInfos != null) {
                imageInfos.clear();
                imageInfos = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
