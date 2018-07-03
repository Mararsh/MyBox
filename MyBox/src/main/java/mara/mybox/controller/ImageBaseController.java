package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.objects.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class ImageBaseController extends BaseController {

    protected ImageFileInformation imageInformation;
    protected BufferedImage bufferImage;
    protected Image image;
    protected File sourceFile;

    protected class ProcessParameters {

        public File sourceFile;
        public int fromPage, toPage, startPage, acumFrom, acumStart, acumDigit;
        public String password, status, targetPath, targetPrefix, targetRootPath, finalTargetName;
        public Date startTime, endTime;
        public int currentPage, currentNameNumber, currentFileIndex, currentTotalHandled;
        boolean fill, aDensity, aColor, aCompression, aQuality, isBatch, createSubDir;
    }

    @FXML
    protected TextField sourceFileInput;
    @FXML
    protected TextField targetPathInput, targetPrefixInput, statusLabel;

    public ImageBaseController() {
    }

    @Override
    protected void initializeNext() {
        try {
            fileExtensionFilter = CommonValues.ImageExtensionFilter;
            if (sourceFileInput != null) {
                sourceFileInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (newValue == null || newValue.isEmpty()) {
                            sourceFileInput.setStyle(badStyle);
                            return;
                        }
                        final File file = new File(newValue);
                        if (!file.exists()) {
                            sourceFileInput.setStyle(badStyle);
                            return;
                        }
                        sourceFileInput.setStyle(null);
                        sourceFileChanged(file);
                        if (file.isDirectory()) {
                            AppVaribles.setConfigValue("imageSourcePath", file.getPath());
                        } else {
                            AppVaribles.setConfigValue("imageSourcePath", file.getParent());
                            if (targetPathInput != null && targetPathInput.getText().isEmpty()) {
                                targetPathInput.setText(AppVaribles.getConfigValue("imageTargetPath", System.getProperty("user.home")));
                            }
                            if (targetPrefixInput != null) {
                                targetPrefixInput.setText(FileTools.getFilePrefix(file.getName()));
                            }
                        }
                    }
                });
            }

            initializeNext2();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initializeNext2() {

    }

    @FXML
    protected void selectSourceFile() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue("imageSourcePath", System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            sourceFile = fileChooser.showOpenDialog(getMyStage());
            if (sourceFileInput != null) {
                sourceFileInput.setText(sourceFile.getAbsolutePath());
            } else {
                sourceFileChanged(sourceFile);
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    protected void sourceFileChanged(final File file) {

    }

    public void loadImage(final File file, final boolean onlyInformation) {
        sourceFile = file;
        final String fileName = file.getPath();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                imageInformation = ImageFileReaders.readImageMetaData(fileName);
                bufferImage = null;
                String format = FileTools.getFileSuffix(fileName).toLowerCase();
                if (!"raw".equals(format) && !onlyInformation) {
                    bufferImage = ImageIO.read(file);
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        afterImageLoaded();
                    }
                });
                return null;
            }
        };
        openLoadingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    protected void afterImageLoaded() {

    }

    public ImageFileInformation getImageInformation() {
        return imageInformation;
    }

    public void setImageInformation(ImageFileInformation imageInformation) {
        this.imageInformation = imageInformation;
    }

    public BufferedImage getBufferImage() {
        return bufferImage;
    }

    public void setBufferImage(BufferedImage bufferImage) {
        this.bufferImage = bufferImage;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public TextField getSourceFileInput() {
        return sourceFileInput;
    }

    public void setSourceFileInput(TextField sourceFileInput) {
        this.sourceFileInput = sourceFileInput;
    }

    public TextField getTargetPathInput() {
        return targetPathInput;
    }

    public void setTargetPathInput(TextField targetPathInput) {
        this.targetPathInput = targetPathInput;
    }

    public TextField getTargetPrefixInput() {
        return targetPrefixInput;
    }

    public void setTargetPrefixInput(TextField targetPrefixInput) {
        this.targetPrefixInput = targetPrefixInput;
    }

    public TextField getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(TextField statusLabel) {
        this.statusLabel = statusLabel;
    }

}
