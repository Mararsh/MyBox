package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.data.ImageAttributes;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.image.tools.ScaleTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @License Apache License Version 2.0
 */
public abstract class BaseImageEditBatchController extends BaseBatchImageController {

    protected ImageAttributes attributes;
    protected String errorString;
    protected FxTask demoTask;

    @FXML
    protected ControlImageFormat formatController;
    @FXML
    protected Button browseButton;
    @FXML
    protected CheckBox handleTransparentCheck;

    protected abstract BufferedImage handleImage(FxTask currentTask, BufferedImage source);

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (formatController != null) {
                formatController.setParameters(this, false);
            }
            if (browseButton != null) {
                browseButton.setDisable(true);
            }
            if (handleTransparentCheck != null) {
                handleTransparentCheck.setDisable(UserConfig.getBoolean(baseName + "HandleTransparent", false));
                handleTransparentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        UserConfig.setBoolean(baseName + "HandleTransparent", nv);
                    }
                });
            }

            previewButton.disableProperty().unbind();
            previewButton.setDisable(false);
            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(tableView.getItems()));

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        if (formatController != null) {
            attributes = formatController.getAttributes();
            targetFileSuffix = attributes.getImageFormat();
        }
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            if (browseButton != null) {
                browseButton.setDisable(targetFiles == null || targetFiles.isEmpty());
            }

            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return Languages.message("Skip");
            }
            BufferedImage sourceImage = ImageFileReaders.readImage(currentTask, srcFile);
            if (sourceImage == null) {
                return Languages.message("Failed");
            }
            BufferedImage targetImage = handleImage(currentTask, sourceImage);
            if (targetImage == null) {
                if (errorString != null) {
                    return errorString;
                } else {
                    return Languages.message("Failed");
                }
            }
            ImageFileWriters.writeImageFile(currentTask,
                    targetImage, attributes, target.getAbsolutePath());

            targetFileGenerated(target);
            if (browseButton != null) {
                browseButton.setDisable(false);
            }
            return Languages.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e);
            return Languages.message("Failed");
        }
    }

    @FXML
    public void demo() {
        if (demoTask != null) {
            demoTask.cancel();
        }
        demoTask = new FxSingletonTask<Void>(this) {
            private List<String> files;

            @Override
            protected boolean handle() {
                try {
                    List<FileInformation> sources = pickSourceFiles(true, false);
                    File demoFile = sources.get(0).getFile();
                    BufferedImage demoImage = ImageFileReaders.readImage(this, demoFile);
                    if (demoTask == null || !demoTask.isWorking()) {
                        return false;
                    }
                    if (demoImage != null) {
                        demoImage = ScaleTools.demoImage(demoImage);
                    }
                    if (demoImage == null || demoTask == null || !demoTask.isWorking()) {
                        return false;
                    }
                    files = new ArrayList<>();
                    makeDemoFiles(this, files, demoFile, demoImage);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (files != null && !files.isEmpty()) {
                    ImagesBrowserController.loadNames(files);
                }
            }

        };
        start(demoTask);
    }

    public void makeDemoFiles(FxTask dTask, List<String> files,
            File demoFile, BufferedImage demoImage) {
    }

}
