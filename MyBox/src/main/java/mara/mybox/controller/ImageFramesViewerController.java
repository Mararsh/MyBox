package mara.mybox.controller;

import mara.mybox.controller.base.ImageSourcesController;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.data.ImageFileInformation;
import mara.mybox.data.ImageInformation;
import mara.mybox.data.VisitHistory;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.ValueTools;
import static mara.mybox.value.AppVaribles.getMessage;

/**
 * @Author Mara
 * @CreateDate 2018-11-30
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageFramesViewerController extends ImageSourcesController {

    @FXML
    protected Button extractButton, editButton;

    public ImageFramesViewerController() {
        baseTitle = AppVaribles.getMessage("ImageFramesViewer");
        SourceFileType = VisitHistory.FileType.MultipleFrames;
        SourcePathType = VisitHistory.FileType.MultipleFrames;
        TargetFileType = VisitHistory.FileType.MultipleFrames;
        TargetPathType = VisitHistory.FileType.MultipleFrames;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("tif/tiff/gif", "*.tif", "*.tiff", "*.gif"));
                add(new FileChooser.ExtensionFilter("tif", "*.tif", "*.tiff"));
                add(new FileChooser.ExtensionFilter("gif", "*.gif"));
            }
        };
    }

    @Override
    public void initializeNext() {
        try {
            initSourceSection();
            sourcesBox.setDisable(true);
            editButton.setDisable(true);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void checkTableSelected() {
        int selected = sourceTable.getSelectionModel().getSelectedIndex();
        boolean none = (selected < 0);
        infoButton.setDisable(none);
        metaButton.setDisable(none);
        viewButton.setDisable(none);
        extractButton.setDisable(none);
        if (none) {
            bottomLabel.setText("");
        } else {
            bottomLabel.setText(getMessage("DoubleClickToView"));
        }
    }

    @Override
    public void selectSourceFile(File file) {
        try {
            sourceImages.clear();

            sourceFile = file;
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
            sourcesBox.setDisable(false);
            editButton.setDisable(false);
            recordFileOpened(file);

            List<File> files = new ArrayList<>();
            files.add(file);
            showFiles(0, files);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void showFiles(final int index, final List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
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
                for (File file : files) {
                    if (task == null || task.isCancelled()) {
                        return null;
                    }
                    final String fileName = file.getPath();
                    ImageFileInformation finfo = ImageFileReaders.readImageFileMetaData(fileName);
                    String format = finfo.getImageFormat();
                    if ("raw".equals(format)) {
                        continue;
                    }
                    List<BufferedImage> bufferImages = ImageFileReaders.readFrames(format, fileName, finfo.getImagesInformation());
                    if (bufferImages == null || bufferImages.isEmpty()) {
                        ret = "FailedReadFile";
                        break;
                    }
                    for (int i = 0; i < bufferImages.size(); i++) {
                        if (task == null || task.isCancelled()) {
                            return null;
                        }
                        ImageInformation minfo = finfo.getImagesInformation().get(i);
                        if (minfo.isIsSampled()) {
                            hasSampled = true;
                        }
                        image = SwingFXUtils.toFXImage(bufferImages.get(i), null);
                        minfo.setImage(image);
                        infos.add(minfo);
                    }
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
                                    alertWarning(AppVaribles.getMessage("ImageSampled"));
                                    bottomLabel.setText(AppVaribles.getMessage("ImageSampled"));
                                }
                                isSettingValues = true;
                                if (index < 0 || index >= sourceImages.size()) {
                                    sourceImages.addAll(infos);
                                } else {
                                    sourceImages.addAll(index, infos);
                                }
                                sourceTable.refresh();
                                isSettingValues = false;
                                setImageChanged(true);
                            } else {
                                popError(AppVaribles.getMessage(ret));
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
    protected void selectAll() {
        isSettingValues = true;
        sourceTable.getSelectionModel().selectAll();
        isSettingValues = false;
        checkTableSelected();
    }

    @FXML
    protected void unselectAll() {
        isSettingValues = true;
        sourceTable.getSelectionModel().clearSelection();
        isSettingValues = false;
        checkTableSelected();
    }

    @FXML
    protected void editAction() {
        try {
            String format = FileTools.getFileSuffix(sourceFile.getAbsolutePath()).toLowerCase();
            if (format.contains("tif")) {
                final ImageTiffEditerController controller
                        = (ImageTiffEditerController) openStage(CommonValues.ImageTiffEditerFxml);
                controller.loadFile(sourceFile, sourceImages);

            } else if (format.contains("gif")) {
                final ImageGifEditerController controller
                        = (ImageGifEditerController) openStage(CommonValues.ImageGifEditerFxml);
                controller.loadFile(sourceFile, sourceImages);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void extractAction() {
        try {
            if (sourceFile == null || sourceImages.isEmpty()) {
                return;
            }
            final List<ImageInformation> selectedImages = new ArrayList<>();
            selectedImages.addAll(sourceTable.getSelectionModel().getSelectedItems());
            if (selectedImages.isEmpty()) {
                return;
            }

            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(targetPathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(CommonValues.ImageExtensionFilter);
            fileChooser.setTitle(getMessage("FilePrefixInput"));
            final File targetFile = fileChooser.showSaveDialog(getMyStage());
            if (targetFile == null) {
                return;
            }
            AppVaribles.setUserConfigValue(targetPathKey, targetFile.getParent());

            task = new Task<Void>() {
                private List<String> filenames;
                private boolean ok;

                @Override
                protected Void call() throws Exception {
                    String format = FileTools.getFileSuffix(targetFile.getAbsolutePath()).toLowerCase();
                    String filePrefix = FileTools.getFilePrefix(targetFile.getAbsolutePath());
                    String filename;
                    int digit = (selectedImages.size() + "").length();
                    filenames = new ArrayList<>();
                    for (int i = 0; i < selectedImages.size(); i++) {
                        if (task == null || task.isCancelled()) {
                            return null;
                        }
                        filename = filePrefix + "-" + ValueTools.fillLeftZero(i, digit) + "." + format;
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
