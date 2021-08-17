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
    protected int number;
    protected final Clipboard clipboard = Clipboard.getSystemClipboard();
    protected TableImageClipboard tableImageClipboard = new TableImageClipboard();
    private String filePrefix;
    private Image lastImage = null;
    protected Connection conn = null;
    protected ImageInSystemClipboardController controller;

    public ImageClipboardMonitor start(int inInterval, ImageAttributes attributes, String filePrefix) {
        int interval = ImageClipboardTools.setMonitorInterval(inInterval);
        this.attributes = attributes;
        this.filePrefix = filePrefix;
        startTime = new Date();
        number = 0;
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
                        if (controller == null && !ImageClipboardTools.isCopy()
                                && (!ImageClipboardTools.isSave() || filePrefix == null || attributes == null)) {
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
                        if (ImageClipboardTools.isCopy()) {
                            copyToMyBoxClipboard(clip);
                        }
                        if (ImageClipboardTools.isSave()) {
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
                int width = ImageClipboardTools.getWidth();
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

}
