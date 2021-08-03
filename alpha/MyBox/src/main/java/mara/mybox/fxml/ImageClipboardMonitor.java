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
import mara.mybox.tools.IntTools;

/**
 * @Author Mara
 * @CreateDate 2021-8-3
 * @License Apache License Version 2.0
 */
public class ImageClipboardMonitor extends Timer {

    public final static int DefaultInterval = 1000;
    protected ImageAttributes attributes;
    protected Date startTime = null;
    protected int number, interval, width = -1;
    protected final Clipboard clipboard = Clipboard.getSystemClipboard();
    protected TableImageClipboard tableImageClipboard = new TableImageClipboard();
    private String filePrefix;
    private Image lastImage = null;
    protected boolean isCopy = false, isSave = false;
    protected Connection conn = null;
    protected ImageInSystemClipboardController controller;

    public ImageClipboardMonitor start(boolean isCopy, boolean isSave,
            ImageAttributes attributes, String filePrefix, int width) {
        interval = ImageClipboardTools.getMonitorInterval();
        this.attributes = attributes;
        this.filePrefix = filePrefix;
        this.width = width;
        startTime = new Date();
        number = 0;
        schedule(new MonitorTask(), 0, this.interval);
        Platform.runLater(() -> {
            ImageInSystemClipboardController.updateSystemClipboard();
//            ControlImageClipboard.updateMyBoxClipboard();
        });
        MyBoxLog.debug("Image Clipboard Monitor started. Interval:" + interval);
        return this;
    }

    public void stop() {
        cancel();
        Platform.runLater(() -> {
            ImageInSystemClipboardController.updateSystemClipboard();
//            ControlImageClipboard.updateMyBoxClipboard();
        });
        MyBoxLog.debug("Image Clipboard Monitor stopped.");
    }

    class MonitorTask extends TimerTask {

        @Override
        public void run() {

            Platform.runLater(new Runnable() {

                @Override
                public synchronized void run() {
                    try {
                        if (!clipboard.hasImage()) {
                            return;
                        }
                        controller = ImageInSystemClipboardController.running();
                        if (!isCopy && !isSave && controller == null) {
                            ImageClipboardTools.stopImageClipboardMonitor();
                            return;
                        }
                        Image clip = clipboard.getImage();
                        if (clip == null || FxImageTools.sameImage(lastImage, clip)) {
                            return;
                        }
                        lastImage = clip;
                        number++;
                        if (controller != null) {
                            controller.loadClip(clip);
                        }
                        if (isCopy) {
                            copyToMyBoxClipboard(clip);
                        }
                        if (isSave) {
                            saveImage(clip);
                        }
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                }
            });
        }
    }

    public void saveImage(Image image) {
        if (image == null || filePrefix == null || attributes == null) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                if (width > 0) {
                    bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, width);
                }
                File file = new File(filePrefix + DateTools.nowString3() + "-"
                        + IntTools.getRandomInt(1000) + "." + attributes.getImageFormat());
                while (file.exists()) {
                    file = new File(filePrefix + DateTools.nowString3() + "-"
                            + IntTools.getRandomInt(1000) + "." + attributes.getImageFormat());
                }
                BufferedImage converted = ImageConvertTools.convertColorSpace(bufferedImage, attributes);
                ImageFileWriters.writeImageFile(converted, attributes, file.getAbsolutePath());
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
                try {
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
                    if (conn == null || conn.isClosed()) {
                        conn = DerbyBase.getConnection();
                    }
                    tableImageClipboard.insertData(conn, ImageClipboard.create(bufferedImage, ImageSource.SystemClipBoard));
                    conn.commit();
                    ControlImagesClipboard.updateClipboards();
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
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isIsCopy() {
        return isCopy;
    }

    public void setIsCopy(boolean isCopy) {
        this.isCopy = isCopy;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public ImageInSystemClipboardController getController() {
        return controller;
    }

    public void setController(ImageInSystemClipboardController controller) {
        this.controller = controller;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
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

    public boolean isIsSave() {
        return isSave;
    }

    public void setIsSave(boolean isSave) {
        this.isSave = isSave;
    }

}
