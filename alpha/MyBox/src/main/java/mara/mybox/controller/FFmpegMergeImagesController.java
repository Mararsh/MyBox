package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import mara.mybox.image.data.ImageInformation;
import mara.mybox.image.tools.ScaleTools;
import mara.mybox.data.MediaInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegMergeImagesController extends BaseBatchFFmpegController {

    protected ObservableList<MediaInformation> audiosData;

    @FXML
    protected Tab imagesTab, audiosTab;
    @FXML
    protected ControlFFmpegAudiosTable audiosTableController;

    public FFmpegMergeImagesController() {
        baseTitle = message("FFmpegMergeImagesInformation");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image, VisitHistory.FileType.Media);
        targetExtensionFilter = FileFilters.FFmpegMediaExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            audiosTableController.parentController = this;
            audiosTableController.parentFxml = myFxml;

            audiosData = audiosTableController.tableData;

            ffmpegOptionsController.durationBox.setVisible(true);

            ffmpegOptionsController.extensionInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        checkExt();
                    });
            checkExt();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(ffmpegOptionsController.extensionInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void checkExt() {
        String ext = ffmpegOptionsController.extensionInput.getText();
        if (ext == null || ext.isBlank() || message("OriginalFormat").equals(ext)) {
            return;
        }
        String v = targetFileController.text();
        if (v == null || v.isBlank()) {
            targetFileController.input(FileTmpTools.generateFile(ext).getAbsolutePath());
        } else if (!v.endsWith("." + ext)) {
            targetFileController.input(FileNameTools.replaceExt(v, ext));
        }
    }

    @Override
    public void doCurrentProcess() {
        try {
            targetFile = targetFileController.pickFile();
            if (currentParameters == null || tableData.isEmpty() || targetFile == null) {
                popError(message("InvalidParameters"));
                return;
            }
            if (ffmpegOptionsController.width <= 0) {
                ffmpegOptionsController.width = 720;
            }
            if (ffmpegOptionsController.height <= 0) {
                ffmpegOptionsController.height = 480;
            }
            String ext = ffmpegOptionsController.extensionInput.getText().trim();
            if (ext.isEmpty() || message("OriginalFormat").equals(ext)) {
                ext = FileNameTools.ext(targetFile.getName());
            }
            final File videoFile = makeTargetFile(FileNameTools.prefix(targetFile.getName()),
                    "." + ext, targetFile.getParentFile());
            if (videoFile == null) {
                return;
            }
            if (task != null) {
                task.cancel();
            }
            showLogs(message("TargetFile") + ": " + videoFile);
            processStartTime = new Date();
            totalFilesHandled = 0;
            updateInterface("Started");
            task = new FxSingletonTask<Void>(this) {

                @Override
                public Void call() {
                    try {
                        File imagesListFile = handleImages(this);
                        if (imagesListFile == null) {
                            return null;
                        }
                        File audiosListFile = handleAudios(this);
                        merge(imagesListFile, audiosListFile, videoFile);

                    } catch (Exception e) {
                        showLogs(e.toString());
                    }
                    ok = true;
                    return null;
                }

                @Override
                public void succeeded() {
                    super.succeeded();
                    updateInterface("Done");
                }

                @Override
                public void cancelled() {
                    super.cancelled();
                    updateInterface("Canceled");
                }

                @Override
                public void failed() {
                    super.failed();
                    updateInterface("Failed");
                }

                @Override
                protected void finalAction() {
                    super.finalAction();
                    closeTask(ok);
                }

            };
            start(task, false);
        } catch (Exception e) {
            updateInterface("Failed");
            MyBoxLog.error(e);
        }
    }

    @Override
    public void disableControls(boolean disable) {
        super.disableControls(disable);
        audiosTableController.thisPane.setDisable(disable);
    }

    //  https://trac.ffmpeg.org/wiki/Slideshow
    protected File handleImages(FxTask currentTask) {
        try {
            StringBuilder s = new StringBuilder();
            File lastFile = null;
            boolean selected = tableController.selectedItem() != null;
            for (int i = 0; i < tableData.size(); ++i) {
                if (currentTask == null || currentTask.isCancelled()) {
                    showLogs(message("TaskCancelled"));
                    return null;
                }
                if (selected && !tableView.getSelectionModel().isSelected(i)) {
                    continue;
                }
                ImageInformation info = (ImageInformation) tableData.get(i);
                totalFilesHandled++;
                if (info.getFile() != null) {
                    if (info.getIndex() >= 0) {
                        showLogs(message("Reading") + ": " + info.getFile() + "  "
                                + message("Frame") + info.getIndex());
                    } else {
                        showLogs(message("Reading") + ": " + info.getFile());
                    }
                }
                try {
                    BufferedImage bufferedImage = ImageInformation.readBufferedImage(currentTask, info);
                    if (bufferedImage == null) {
                        continue;
                    }
                    if (currentTask == null || currentTask.isCancelled()) {
                        showLogs(message("TaskCancelled"));
                        return null;
                    }
                    BufferedImage fitImage = ScaleTools.fitSize(bufferedImage,
                            ffmpegOptionsController.width, ffmpegOptionsController.height);
                    File tmpFile = FileTmpTools.getTempFile(".png");
                    if (ImageFileWriters.writeImageFile(currentTask, fitImage, tmpFile) && tmpFile.exists()) {
                        lastFile = tmpFile;
                        s.append("file '").append(lastFile.getAbsolutePath()).append("'\n");
                        s.append("duration  ").append(info.getDuration() / 1000.00f).append("\n");
                    }
                } catch (Exception e) {
                    MyBoxLog.debug(e);
                }
            }
            if (lastFile == null) {
                showLogs(message("InvalidData"));
                return null;
            }
            s.append("file '").append(lastFile.getAbsolutePath()).append("'\n");
            File imagesListFile = FileTmpTools.getTempFile(".txt");
            TextFileTools.writeFile(imagesListFile, s.toString(), Charset.forName("utf-8"));
            return imagesListFile;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected File handleAudios(FxTask currentTask) {
        try {
            StringBuilder s = new StringBuilder();
            boolean selected = audiosTableController.selectedItem() != null;
            for (int i = 0; i < audiosData.size(); ++i) {
                if (currentTask == null || currentTask.isCancelled()) {
                    showLogs(message("TaskCancelled"));
                    return null;
                }
                if (selected && !audiosTableController.tableView.getSelectionModel().isSelected(i)) {
                    continue;
                }
                MediaInformation info = audiosData.get(i);
                File file = info.getFile();
                if (file == null) {
                    continue;
                }
                totalFilesHandled++;
                showLogs(message("Handling") + ": " + file);
                s.append("file '").append(file.getAbsolutePath()).append("'\n");
            }
            String ss = s.toString();
            if (ss.isEmpty()) {
                return null;
            }
            File audiosListFile = FileTmpTools.getTempFile(".txt");
            TextFileTools.writeFile(audiosListFile, s.toString(), Charset.forName("utf-8"));
            return audiosListFile;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected void merge(File imagesListFile, File audiosListFile, File videoFile) {
        if (imagesListFile == null || videoFile == null) {
            return;
        }
        try {
            List<String> parameters = new ArrayList<>();
            parameters.add("-f");
            parameters.add("concat");
            parameters.add("-safe");
            parameters.add("0");
            parameters.add("-i");
            parameters.add(imagesListFile.getAbsolutePath());
            if (audiosListFile != null) {
                parameters.add("-f");
                parameters.add("concat");
                parameters.add("-safe");
                parameters.add("0");
                parameters.add("-i");
                parameters.add(audiosListFile.getAbsolutePath());
            }

            parameters.add("-c");
            parameters.add("copy");

            if (ffmpegOptionsController.shortestCheck.isSelected()) {
                parameters.add("-shortest");
            }
            parameters.add("-s");
            parameters.add(ffmpegOptionsController.width + "x" + ffmpegOptionsController.height);

//            parameters.add("-pix_fmt");
//            parameters.add("yuv420p");
            parameters = ffmpegOptionsController.makeParameters(parameters);

            Process process = ffmpegOptionsController.startProcess(this, parameters, videoFile);
            try (BufferedReader inReader = process.inputReader(Charset.defaultCharset())) {
                String line;
                while ((line = inReader.readLine()) != null) {
                    if (verboseCheck.isSelected()) {
                        updateLogs(line + "\n");
                    }
                }
            }
            process.waitFor();

            targetFileGenerated(videoFile);
            showLogs(message("Size") + ": " + FileTools.showFileSize(videoFile.length()));
        } catch (Exception e) {
            showLogs(e.toString());
        }
    }

    /*
        static methods
     */
    public static FFmpegMergeImagesController open(List<ImageInformation> images) {
        try {
            FFmpegMergeImagesController controller = (FFmpegMergeImagesController) WindowTools.openStage(Fxmls.FFmpegMergeImagesFxml);
            controller.tableController.tableData.setAll(images);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
