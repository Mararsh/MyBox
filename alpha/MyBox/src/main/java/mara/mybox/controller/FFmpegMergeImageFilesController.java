package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.data.ImageInformation;
import mara.mybox.image.file.ImageFileReaders;
import static mara.mybox.image.file.ImageFileReaders.getReader;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.image.tools.ScaleTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import thridparty.image4j.ICODecoder;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegMergeImageFilesController extends FFmpegMergeImagesController {

    protected File lastFile;
    protected StringBuilder imageFileString;

    public FFmpegMergeImageFilesController() {
        baseTitle = message("FFmpegMergeImagesFiles");
    }

    @Override
    protected File handleImages(FxTask currentTask) {
        try {
            imageFileString = new StringBuilder();
            lastFile = null;
            boolean selected = tableController.selectedItem() != null;
            for (int i = 0; i < tableData.size(); ++i) {
                if (currentTask == null || !currentTask.isWorking()) {
                    showLogs(message("TaskCancelled"));
                    return null;
                }
                if (selected && !tableView.getSelectionModel().isSelected(i)) {
                    continue;
                }
                FileInformation info = tableData.get(i);
                tableController.markFileHandling(info);
                File file = info.getFile();
                if (file == null) {
                    continue;
                }
                String result;
                if (!file.exists()) {
                    result = message("NotFound");
                } else if (file.isFile()) {
                    result = handleFile(currentTask, file, info.getDuration());
                } else if (file.isDirectory()) {
                    result = handleDirectory(currentTask, file, info.getDuration());
                } else {
                    result = message("NotFound");
                }
                tableController.markFileHandled(currentParameters.currentSourceFile, result);
            }
            if (lastFile == null) {
                updateLogs(message("InvalidData"), true);
                return null;
            }
            imageFileString.append("file '").append(lastFile.getAbsolutePath()).append("'\n");
            File imagesListFile = FileTmpTools.getTempFile(".txt");
            TextFileTools.writeFile(imagesListFile, imageFileString.toString(), Charset.forName("utf-8"));
            return imagesListFile;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public String handleDirectory(FxTask currentTask, File directory, long duration) {
        try {
            if (directory == null || !directory.isDirectory()) {
                return message("Failed");
            }
            File[] files = directory.listFiles();
            if (files == null) {
                return message("Done");
            }
            for (File srcFile : files) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return message("Canceled");
                }
                if (srcFile.isFile()) {
                    handleFile(currentTask, srcFile, duration);
                } else if (srcFile.isDirectory()) {
                    handleDirectory(currentTask, srcFile, duration);
                }
            }
            return message("Done");
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
        }
    }

    public String handleFile(FxTask currentTask, File file, long duration) {
        if (file == null) {
            return message("Failed");
        }
        if (verboseCheck == null || verboseCheck.isSelected()) {
            updateLogs(message("Handling") + ": " + file, true);
        }
        String format = FileNameTools.ext(file.getName()).toLowerCase();
        if ("ico".equals(format) || "icon".equals(format)) {
            try {
                List<BufferedImage> imageSrc = ICODecoder.read(file);
                for (BufferedImage image : imageSrc) {
                    if (currentTask == null || !currentTask.isWorking()) {
                        return message("Canceled");
                    }
                    handleImage(currentTask, image, duration);
                }
                totalFilesHandled++;
                return message("Done");
            } catch (Exception e) {
                MyBoxLog.error(e);
                return message("Failed");
            }
        }
        try (ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            ImageReader reader = getReader(iis, format);
            if (reader == null) {
                return message("Failed");
            }
            reader.setInput(iis, false, true);
            ImageReadParam param = reader.getDefaultReadParam();
            int num = reader.getNumImages(true);
            ImageInformation imageInfo = new ImageInformation(file);
            imageInfo.setImageFormat(format);
            for (int i = 0; i < num; i++) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return message("Canceled");
                }
                if (num > 1) {
                    updateLogs(message("Reading") + ": " + file + "-" + (i + 1), true);
                } else {
                    updateLogs(message("Reading") + ": " + file, true);
                }
                BufferedImage frame;
                try {
                    frame = reader.read(i, param);
                } catch (Exception e) {
                    if (e.toString().contains("java.lang.IndexOutOfBoundsException")) {
                        break;
                    }
                    frame = ImageFileReaders.readBrokenImage(currentTask, e, imageInfo.setIndex(i));
                }
                if (currentTask == null || !currentTask.isWorking()) {
                    return message("Canceled");
                }
                if (frame != null) {
                    handleImage(currentTask, frame, duration);
                } else {
                    break;
                }
            }
            reader.dispose();
            totalFilesHandled++;
            return message("Done");
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
        }
    }

    public boolean handleImage(FxTask currentTask, BufferedImage image, long duration) {
        try {
            BufferedImage fitImage = ScaleTools.fitSize(image,
                    ffmpegOptionsController.width, ffmpegOptionsController.height);
            File tmpFile = FileTmpTools.getTempFile(".png");
            if (ImageFileWriters.writeImageFile(currentTask, fitImage, tmpFile) && tmpFile.exists()) {
                lastFile = tmpFile;
                imageFileString.append("file '").append(lastFile.getAbsolutePath()).append("'\n");
                imageFileString.append("duration  ").append(duration / 1000.00f).append("\n");
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
