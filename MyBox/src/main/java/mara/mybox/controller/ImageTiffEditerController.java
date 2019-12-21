package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import javafx.stage.Modality;
import mara.mybox.data.VisitHistory;
import mara.mybox.image.file.ImageTiffFile;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-11-16
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageTiffEditerController extends ImagesListController {

    public ImageTiffEditerController() {
        baseTitle = AppVariables.message("ImageTiffEditer");

        SourceFileType = VisitHistory.FileType.Tif;
        SourcePathType = VisitHistory.FileType.Tif;
        TargetFileType = VisitHistory.FileType.Tif;
        TargetPathType = VisitHistory.FileType.Tif;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        sourceExtensionFilter = CommonFxValues.TiffExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initOptionsSection() {
        try {
            tableBox.setDisable(true);

            saveButton.disableProperty().bind(
                    Bindings.isEmpty(tableData)
            );

            saveAsButton.disableProperty().bind(
                    saveButton.disableProperty()
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void saveFileDo(final File outFile) {

        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    error = ImageTiffFile.writeTiffImagesWithInfo(tableData, null, outFile);
                    return error.isEmpty();
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessul();
                    if (outFile.equals(sourceFile)) {
                        setImageChanged(false);
                    }
                    if (viewCheck.isSelected()) {
                        final ImageFramesViewerController controller
                                = (ImageFramesViewerController) openStage(CommonValues.ImageFramesViewerFxml);
                        controller.selectSourceFile(outFile);
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        }
    }

}
