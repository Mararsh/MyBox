package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import mara.mybox.data.VisitHistory;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
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
        baseTitle = AppVariables.message("ImageFramesViewer");
        SourceFileType = VisitHistory.FileType.MultipleFrames;
        SourcePathType = VisitHistory.FileType.MultipleFrames;
        TargetFileType = VisitHistory.FileType.MultipleFrames;
        TargetPathType = VisitHistory.FileType.MultipleFrames;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        sourceExtensionFilter = new ArrayList<>() {
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

        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private List<ImageInformation> infos;

                @Override
                protected boolean handle() {
                    infos = new ArrayList<>();
                    final String fileName = file.getPath();
                    ImageFileInformation finfo = ImageFileReaders.readImageFileMetaData(fileName);
                    if (finfo == null || finfo.getImageInformation() == null) {
                        return false;
                    }
                    imageInformation = finfo.getImageInformation();

                    String format = finfo.getImageFormat();
                    if ("raw".equals(format)) {
                        return false;
                    }
                    List<BufferedImage> bufferImages = ImageFileReaders.readFrames(format, fileName, finfo.getImagesInformation());
                    if (bufferImages == null || bufferImages.isEmpty()) {
                        error = "FailedReadFile";
                        return false;
                    }
                    for (int i = 0; i < bufferImages.size(); ++i) {
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        ImageInformation minfo = finfo.getImagesInformation().get(i);
                        Image image = SwingFXUtils.toFXImage(bufferImages.get(i), null);
                        minfo.setImage(image);
                        infos.add(minfo);
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    tableData.addAll(infos);
                    tableView.refresh();
                    isSettingValues = false;
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void dataChanged() {
        setImageChanged(false);
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
                    AppVariables.getUserConfigPath(targetPathKey),
                    FileTools.getFilePrefix(sourceFile.getName()),
                    CommonFxValues.ImageExtensionFilter, true);
            if (tFile == null) {
                return;
            }
            recordFileWritten(tFile);

            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private List<String> filenames;

                    @Override
                    protected boolean handle() {
                        String format = FileTools.getFileSuffix(tFile.getAbsolutePath()).toLowerCase();
                        String filePrefix = FileTools.getFilePrefix(tFile.getAbsolutePath());
                        String filename;
                        int digit = (selectedImages.size() + "").length();
                        filenames = new ArrayList<>();
                        for (int i = 0; i < selectedImages.size(); ++i) {
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            filename = filePrefix + "-" + StringTools.fillLeftZero(i, digit) + "." + format;
                            BufferedImage bufferedImage = ImageFileReaders.getBufferedImage(selectedImages.get(i));
                            if (bufferedImage == null) {
                                continue;
                            }
                            ImageFileWriters.writeImageFile(bufferedImage, format, filename);
                            filenames.add(filename);
                        }
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        multipleFilesGenerated(filenames);
                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
