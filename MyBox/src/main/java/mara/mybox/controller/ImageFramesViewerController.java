package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import mara.mybox.controller.base.ImagesListController;
import mara.mybox.data.VisitHistory;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-11-30
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageFramesViewerController extends ImagesListController {

    @FXML
    protected Button extractButton, editButton;

    public ImageFramesViewerController() {
        baseTitle = AppVaribles.message("ImageFramesViewer");
        SourceFileType = VisitHistory.FileType.MultipleFrames;
        SourcePathType = VisitHistory.FileType.MultipleFrames;
        TargetFileType = VisitHistory.FileType.MultipleFrames;
        TargetPathType = VisitHistory.FileType.MultipleFrames;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        sourceExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("tif/tiff/gif", "*.tif", "*.tiff", "*.gif"));
                add(new FileChooser.ExtensionFilter("tif", "*.tif", "*.tiff"));
                add(new FileChooser.ExtensionFilter("gif", "*.gif"));
            }
        };
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initializeNext() {
        try {
            tableBox.setDisable(true);
            editButton.setDisable(true);

            extractButton.disableProperty().bind(Bindings.isEmpty(tableData)
                    .or(Bindings.isEmpty(tableView.getSelectionModel().getSelectedItems()))
            );
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void selectSourceFile(final File file) {
        tableData.clear();
        sourceFile = file;
        getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
        tableBox.setDisable(false);
        editButton.setDisable(false);
        recordFileOpened(file);

        task = new Task<Void>() {
            private List<ImageInformation> infos;
            private String ret;
            private boolean hasSampled;
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                infos = new ArrayList<>();
                ret = "";
                hasSampled = false;
                final String fileName = file.getPath();
                ImageFileInformation finfo = ImageFileReaders.readImageFileMetaData(fileName);
                imageInformation = finfo.getImageInformation();
                String format = finfo.getImageFormat();
                if ("raw".equals(format)) {
                    return null;
                }
                List<BufferedImage> bufferImages = ImageFileReaders.readFrames(format, fileName, finfo.getImagesInformation());
                if (bufferImages == null || bufferImages.isEmpty()) {
                    ret = "FailedReadFile";
                    return null;
                }
                for (int i = 0; i < bufferImages.size(); i++) {
                    if (task == null || task.isCancelled()) {
                        return null;
                    }
                    ImageInformation minfo = finfo.getImagesInformation().get(i);
                    if (minfo.isIsSampled()) {
                        hasSampled = true;
                    }
                    Image image = SwingFXUtils.toFXImage(bufferImages.get(i), null);
                    minfo.setImage(image);
                    infos.add(minfo);
                }

                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ret.isEmpty()) {
                                if (hasSampled) {
                                    alertWarning(AppVaribles.message("ImageSampled"));
                                    bottomLabel.setText(AppVaribles.message("ImageSampled"));
                                }
                                tableData.addAll(infos);
                                tableView.refresh();
                            } else {
                                popError(AppVaribles.message(ret));
                            }

                        }
                    });
                }
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    protected void editAction() {
        try {
            String format = FileTools.getFileSuffix(sourceFile.getAbsolutePath()).toLowerCase();
            if (format.contains("tif")) {
                final ImageTiffEditerController controller
                        = (ImageTiffEditerController) openStage(CommonValues.ImageTiffEditerFxml);
                controller.loadFile(sourceFile, tableData);

            } else if (format.contains("gif")) {
                final ImageGifEditerController controller
                        = (ImageGifEditerController) openStage(CommonValues.ImageGifEditerFxml);
                controller.loadFile(sourceFile, tableData);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        extractAction();
    }

    @FXML
    protected void extractAction() {
        try {
            if (sourceFile == null || tableData.isEmpty()) {
                return;
            }
            final List<ImageInformation> selectedImages = new ArrayList<>();
            selectedImages.addAll(tableView.getSelectionModel().getSelectedItems());
            if (selectedImages.isEmpty()) {
                return;
            }

            final File tFile = chooseSaveFile(message("FilePrefixInput"),
                    AppVaribles.getUserConfigPath(targetPathKey),
                    FileTools.getFilePrefix(sourceFile.getName()),
                    CommonValues.ImageExtensionFilter, true);
            if (tFile == null) {
                return;
            }
            AppVaribles.setUserConfigValue(targetPathKey, tFile.getParent());

            task = new Task<Void>() {
                private List<String> filenames;
                private boolean ok;

                @Override
                protected Void call() throws Exception {
                    String format = FileTools.getFileSuffix(tFile.getAbsolutePath()).toLowerCase();
                    String filePrefix = FileTools.getFilePrefix(tFile.getAbsolutePath());
                    String filename;
                    int digit = (selectedImages.size() + "").length();
                    filenames = new ArrayList<>();
                    for (int i = 0; i < selectedImages.size(); i++) {
                        if (task == null || task.isCancelled()) {
                            return null;
                        }
                        filename = filePrefix + "-" + StringTools.fillLeftZero(i, digit) + "." + format;
                        BufferedImage bufferedImage = ImageFileReaders.getBufferedImage(selectedImages.get(i));
                        if (bufferedImage == null) {
                            continue;
                        }
                        ImageFileWriters.writeImageFile(bufferedImage, format, filename);
                        filenames.add(filename);
                    }
                    ok = true;
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    if (ok) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                multipleFilesGenerated(filenames);
                            }
                        });
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
