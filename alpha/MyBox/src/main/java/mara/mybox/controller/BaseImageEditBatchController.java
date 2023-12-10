package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
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

    protected abstract BufferedImage handleImage(BufferedImage source);

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

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not()
                    .or(Bindings.isEmpty(tableView.getItems()))
            );

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
    public String handleFile(File srcFile, File targetPath) {
        try {
            if (browseButton != null) {
                browseButton.setDisable(targetFiles == null || targetFiles.isEmpty());
            }

            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return Languages.message("Skip");
            }
            BufferedImage sourceImage = ImageFileReaders.readImage(task, srcFile);
            BufferedImage targetImage = handleImage(sourceImage);
            if (targetImage == null) {
                if (errorString != null) {
                    return errorString;
                } else {
                    return Languages.message("Failed");
                }
            }
            ImageFileWriters.writeImageFile(task,
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
                    BufferedImage demoImage = null;
                    List<File> sources = pickSourceFiles(true, false);
                    if (sources != null && !sources.isEmpty()) {
                        demoImage = ImageFileReaders.readImage(this, sources.get(0));
                        if (demoTask == null || !demoTask.isWorking()) {
                            return false;
                        }
                        if (demoImage != null) {
                            demoImage = ScaleTools.demoImage(demoImage);
                        }
                    }
                    if (demoImage == null) {
                        demoImage = SwingFXUtils.fromFXImage(
                                new Image("img/" + "cover" + AppValues.AppYear + "g9.png"), null);
                    }
                    if (demoImage == null || demoTask == null || !demoTask.isWorking()) {
                        return false;
                    }
                    files = new ArrayList<>();
                    makeDemoFiles(files, demoImage);
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
                    ImagesBrowserController b
                            = (ImagesBrowserController) WindowTools.openStage(Fxmls.ImagesBrowserFxml);
                    b.loadFiles(files);
                    b.setAlwaysOnTop();
                }
            }

        };
        start(demoTask);
    }

    public void makeDemoFiles(List<String> files, BufferedImage demoImage) {
    }

}
