package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import mara.mybox.data.FileInformation;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegMergeImageFilesController extends FFmpegMergeImagesController {

    protected File lastFile;
    protected StringBuilder imageFileString;

    public FFmpegMergeImageFilesController() {
        baseTitle = AppVariables.message("FFmpegMergeImagesFiles");
    }

    @Override
    protected File handleImages() {
        try {
            imageFileString = new StringBuilder();
            lastFile = null;
            for (int i = 0; i < tableData.size(); ++i) {
                if (task == null || task.isCancelled()) {
                    updateLogs(message("TaskCancelled"), true);
                    return null;
                }
                tableController.markFileHandling(i);
                FileInformation info = (FileInformation) tableData.get(i);
                File file = info.getFile();
                if (file == null) {
                    continue;
                }
                String result;
                if (!file.exists()) {
                    result = AppVariables.message("NotFound");
                } else if (file.isFile()) {
                    result = handleFile(file, info.getDuration());
                } else if (file.isDirectory()) {
                    result = handleDirectory(file, info.getDuration());
                } else {
                    result = AppVariables.message("NotFound");
                }
                tableController.markFileHandled(currentParameters.currentIndex, result);
            }
            if (lastFile == null) {
                updateLogs(message("InvalidData"), true);
                return null;
            }
            imageFileString.append("file '").append(lastFile.getAbsolutePath()).append("'\n");
            File imagesListFile = FileTools.getTempFile(".txt");
            FileTools.writeFile(imagesListFile, imageFileString.toString(), Charset.forName("utf-8"));
            return imagesListFile;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    @Override
    public boolean matchType(File file) {
        return FileTools.isSupportedImage(file);
    }

    public String handleFile(File file, long duration) {
        try {
            if (!match(file)) {
                return AppVariables.message("Failed");
            }
            try {
                totalFilesHandled++;
                if (verboseCheck == null || verboseCheck.isSelected()) {
                    updateLogs(message("Handling") + ": " + file, true);
                }
                List<BufferedImage> images = ImageFileReaders.readFrames(file);
                if (images == null || images.isEmpty()) {
                    return AppVariables.message("Failed");
                }
                for (int i = 0; i < images.size(); i++) {
                    BufferedImage fitImage = ImageManufacture.fitSize(images.get(i),
                            ffmpegOptionsController.width, ffmpegOptionsController.height);
                    File tmpFile = FileTools.getTempFile(".png");
                    if (ImageFileWriters.writeImageFile(fitImage, tmpFile) && tmpFile.exists()) {
                        lastFile = tmpFile;
                        imageFileString.append("file '").append(lastFile.getAbsolutePath()).append("'\n");
                        imageFileString.append("duration  ").append(duration / 1000.00f).append("\n");
                    }
                }

            } catch (Exception e) {
//                logger.debug(e.toString());
            }
            return AppVariables.message("Done");
        } catch (Exception e) {
            return AppVariables.message("Failed");
        }
    }

    public String handleDirectory(File directory, long duration) {
        try {
            if (directory == null || !directory.isDirectory()) {
                return AppVariables.message("Failed");
            }
            File[] files = directory.listFiles();
            if (files == null) {
                return AppVariables.message("Done");
            }
            for (File srcFile : files) {
                if (task == null || task.isCancelled()) {
                    return AppVariables.message("Canceled");
                }
                if (srcFile.isFile()) {
                    handleFile(srcFile, duration);
                } else if (srcFile.isDirectory()) {
                    handleDirectory(srcFile, duration);
                }
            }
            return AppVariables.message("Done");
        } catch (Exception e) {
            logger.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

}
