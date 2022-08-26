package mara.mybox.fxml;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageConvertTools;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.controller.ControlImagesClipboard;
import mara.mybox.controller.ImageInSystemClipboardController;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ImageClipboard;
import mara.mybox.db.data.ImageClipboard.ImageSource;
import mara.mybox.db.table.TableImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.IntTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.io.FileUtils;

/**
 * @Author Mara
 * @CreateDate 2021-8-3
 * @License Apache License Version 2.0
 */
public class ImageClipboardMonitor extends Timer {

    public final static int DefaultInterval = 1000;
    protected ImageAttributes attributes;
    protected Date startTime = null;
    protected int recordNumber, savedNumber, copiedNumber;
    protected TableImageClipboard tableImageClipboard;
    private String filePrefix;
    private Image lastImage = null;

    public ImageClipboardMonitor start(int inInterval, ImageAttributes attributes, String filePrefix) {
        int interval = ImageClipboardTools.setMonitorInterval(inInterval);
        this.attributes = attributes;
        this.filePrefix = filePrefix;
        startTime = new Date();
        recordNumber = 0;
        savedNumber = 0;
        copiedNumber = 0;
        lastImage = null;
        schedule(new MonitorTask(), 0, interval);
        Platform.runLater(() -> {
            ImageInSystemClipboardController.updateSystemClipboardStatus();
            ControlImagesClipboard.updateClipboardsStatus();
        });
        MyBoxLog.debug("Image Clipboard Monitor started. Interval:" + interval);
        return this;
    }

    public void stop() {
        cancel();
        Platform.runLater(() -> {
            ImageInSystemClipboardController.updateSystemClipboardStatus();
            ControlImagesClipboard.updateClipboardsStatus();
        });
        MyBoxLog.debug("Image Clipboard Monitor stopped.");
        clearTmpClips();
        attributes = null;
        lastImage = null;
        tableImageClipboard = null;
        startTime = null;
        filePrefix = null;
    }

    class MonitorTask extends TimerTask {

        private Image clip;

        @Override
        public void run() {

            Platform.runLater(new Runnable() {

                @Override
                public synchronized void run() {
                    try {
                        clearTmpClips();
                        ImageInSystemClipboardController controller = ImageInSystemClipboardController.running();
                        if (controller == null && !ImageClipboardTools.isCopy()
                                && (!ImageClipboardTools.isSave() || filePrefix == null || attributes == null)) {
                            ImageClipboardTools.stopImageClipboardMonitor();
                            return;
                        }
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        if (!clipboard.hasImage()) {
                            return;
                        }
                        clip = clipboard.getImage();
                        if (clip == null || FxImageTools.sameImage(lastImage, clip)) {
                            return;
                        }
                        lastImage = clip;
                        recordNumber++;
                        if (controller != null) {
                            controller.loadClip(clip);
                        }
                        if (ImageClipboardTools.isCopy()) {
                            copyToMyBoxClipboard(clip);
                        }
                        if (ImageClipboardTools.isSave()) {
                            if (filePrefix == null || attributes == null) {
                                if (controller != null) {
                                    controller.filesInfo(message("ImageNotSaveDueInvalidPath"));
                                }
                            } else {
                                saveImage(clip);
                            }
                        }
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                }
            });
        }
    }

    public static void clearTmpClips() {
        try {
            System.gc();
            File path = FileTools.javaIOTmpPath();
            File[] files = path.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                try {
                    if (file.isFile() && file.getName().endsWith(".TMP")) {
                        FileUtils.deleteQuietly(file);
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e.toString());
        }
    }

    public void saveImage(Image image) {
        if (image == null || filePrefix == null || attributes == null) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                File file = new File(filePrefix + DateTools.nowFileString() + "-"
                        + IntTools.random(1000) + "." + attributes.getImageFormat());
                while (file.exists()) {
                    file = new File(filePrefix + DateTools.nowFileString() + "-"
                            + IntTools.random(1000) + "." + attributes.getImageFormat());
                }
                String fname = file.getAbsolutePath();
                ImageInSystemClipboardController controller = ImageInSystemClipboardController.running();
                if (controller != null) {
                    Platform.runLater(() -> {
                        controller.filesInfo(message("Saving") + " " + fname);
                    });
                }
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                int width = ImageClipboardTools.getWidth();
                if (width > 0) {
                    bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, width);
                }
                BufferedImage converted = ImageConvertTools.convertColorSpace(bufferedImage, attributes);
                ImageFileWriters.writeImageFile(converted, attributes, fname);
                savedNumber++;
                if (controller != null) {
                    Platform.runLater(() -> {
                        controller.updateNumbers();
                        controller.filesInfo("");
                    });
                }
            }
        }.start();
    }

    public void copyToMyBoxClipboard(Image image) {
        if (image == null) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                try ( Connection conn = DerbyBase.getConnection()) {
                    int width = ImageClipboardTools.getWidth();
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                    if (width > 0) {
                        bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, width);
                    }
                    if (bufferedImage == null) {
                        return;
                    }
                    if (tableImageClipboard == null) {
                        tableImageClipboard = new TableImageClipboard();
                    }
                    tableImageClipboard.insertData(conn, ImageClipboard.create(bufferedImage, ImageSource.SystemClipBoard));
                    conn.commit();
                    ControlImagesClipboard.updateClipboards();
                    copiedNumber++;
                    ImageInSystemClipboardController controller = ImageInSystemClipboardController.running();
                    if (controller != null) {
                        Platform.runLater(() -> {
                            controller.updateNumbers();
                        });
                    }
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
            }
        }.start();
    }

    /*
        get/set
     */
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public int getNumber() {
        return recordNumber;
    }

    public void setNumber(int number) {
        this.recordNumber = number;
    }

    public TableImageClipboard getTableImageClipboard() {
        return tableImageClipboard;
    }

    public void setTableImageClipboard(TableImageClipboard tableImageClipboard) {
        this.tableImageClipboard = tableImageClipboard;
    }

    public String getFilePrefix() {
        return filePrefix;
    }

    public void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    public Image getLastImage() {
        return lastImage;
    }

    public void setLastImage(Image lastImage) {
        this.lastImage = lastImage;
    }

    public ImageAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(ImageAttributes attributes) {
        this.attributes = attributes;
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(int recordNumber) {
        this.recordNumber = recordNumber;
    }

    public int getSavedNumber() {
        return savedNumber;
    }

    public void setSavedNumber(int savedNumber) {
        this.savedNumber = savedNumber;
    }

    public int getCopiedNumber() {
        return copiedNumber;
    }

    public void setCopiedNumber(int copiedNumber) {
        this.copiedNumber = copiedNumber;
    }

}
