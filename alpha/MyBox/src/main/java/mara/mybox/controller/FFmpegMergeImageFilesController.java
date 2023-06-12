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
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.imagefile.ImageFileReaders;
import static mara.mybox.imagefile.ImageFileReaders.getReader;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.FileTmpTools;
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
                    result = message("NotFound");
                } else if (file.isFile()) {
                    result = handleFile(file, info.getDuration());
                } else if (file.isDirectory()) {
                    result = handleDirectory(file, info.getDuration());
                } else {
                    result = message("NotFound");
                }
                tableController.markFileHandled(currentParameters.currentIndex, result);
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
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public String handleDirectory(File directory, long duration) {
        try {
            if (directory == null || !directory.isDirectory()) {
                return message("Failed");
            }
            File[] files = directory.listFiles();
            if (files == null) {
                return message("Done");
            }
            for (File srcFile : files) {
                if (task == null || task.isCancelled()) {
                    return message("Canceled");
                }
                if (srcFile.isFile()) {
                    handleFile(srcFile, duration);
                } else if (srcFile.isDirectory()) {
                    handleDirectory(srcFile, duration);
                }
            }
            return message("Done");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return message("Failed");
        }
    }

    public String handleFile(File file, long duration) {
        if (file == null) {
            return message("Failed");
        }
        if (verboseCheck == null || verboseCheck.isSelected()) {
            updateLogs(message("Handling") + ": " + file, true);
        }
        String format = FileNameTools.suffix(file.getName()).toLowerCase();
        if ("ico".equals(format) || "icon".equals(format)) {
            try {
                List<BufferedImage> imageSrc = ICODecoder.read(file);
                for (BufferedImage image : imageSrc) {
                    handleImage(image, duration);
                }
                totalFilesHandled++;
                return message("Done");
            } catch (Exception e) {
                MyBoxLog.error(e.toString());
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
                    frame = ImageFileReaders.readBrokenImage(e, imageInfo.setIndex(i));
                }
                if (frame != null) {
                    handleImage(frame, duration);
                } else {
                    break;
                }
            }
            reader.dispose();
            totalFilesHandled++;
            return message("Done");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return message("Failed");
        }
    }

    public boolean handleImage(BufferedImage image, long duration) {
        try {
            BufferedImage fitImage = ScaleTools.fitSize(image,
                    ffmpegOptionsController.width, ffmpegOptionsController.height);
            File tmpFile = FileTmpTools.getTempFile(".png");
            if (ImageFileWriters.writeImageFile(fitImage, tmpFile) && tmpFile.exists()) {
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
