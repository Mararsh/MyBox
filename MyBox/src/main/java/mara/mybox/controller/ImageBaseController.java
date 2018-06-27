package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class ImageBaseController extends BaseController {

    protected ImageFileInformation imageInformation;
    protected BufferedImage image;
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
            fileExtensionFilter = new ArrayList();
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("images", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.tif", "*.tiff", "*.gif", "*.pcx", "*.pnm", "*.wbmp"));
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("png", "*.png"));
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("jpg", "*.jpg", "*.jpeg"));
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("bmp", "*.bmp"));
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("tif", "*.tif", "*.tiff"));
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("gif", "*.gif"));
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("pcx", "*.pcx"));
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("pnm", "*.pnm"));
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("wbmp", "*.wbmp"));

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
            sourceFileInput.setText(sourceFile.getAbsolutePath());
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    protected void sourceFileChanged(final File file) {

    }

    protected void loadImage(final File file, final boolean onlyInformation) {
        final String fileName = file.getPath();
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                imageInformation = ImageFileReaders.readImageMetaData(fileName);
                image = null;
                String format = FileTools.getFileSuffix(fileName).toLowerCase();
                if (!"raw".equals(format) && !onlyInformation) {
                    image = ImageIO.read(file);
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

}
