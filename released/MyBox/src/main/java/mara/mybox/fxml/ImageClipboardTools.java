package mara.mybox.fxml;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ControlImagesClipboard;
import mara.mybox.db.data.ImageClipboard;
import static mara.mybox.fxml.ImageClipboardMonitor.DefaultInterval;
import static mara.mybox.value.AppVariables.imageClipboardMonitor;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-2
 * @License Apache License Version 2.0
 */
public class ImageClipboardTools {


    /*
        monitor
     */
    public static void stopImageClipboardMonitor() {
        if (imageClipboardMonitor != null) {
            imageClipboardMonitor.stop();
            imageClipboardMonitor = null;
        }
    }

    public static void startImageClipboardMonitor(int interval, ImageAttributes attributes, String filePrefix) {
        if (imageClipboardMonitor != null) {
            imageClipboardMonitor.cancel();
            imageClipboardMonitor = null;
        }
        imageClipboardMonitor = new ImageClipboardMonitor().start(interval, attributes, filePrefix);
    }

    public static int getMonitorInterval() {
        int v = UserConfig.getInt("ImageClipboardMonitorInterval", DefaultInterval);
        if (v <= 0) {
            v = DefaultInterval;
        }
        return v;
    }

    public static int setMonitorInterval(int v) {
        if (v <= 0) {
            v = DefaultInterval;
        }
        UserConfig.setInt("ImageClipboardMonitorInterval", v);
        return v;
    }

    public static boolean isMonitoring() {
        return imageClipboardMonitor != null;
    }

    public static int getWidth() {
        return UserConfig.getInt("ImageClipboardMonitorWidth", -1);
    }

    public static int setWidth(int v) {
        UserConfig.setInt("ImageClipboardMonitorWidth", v);
        return v;
    }

    public static boolean isCopy() {
        return UserConfig.getBoolean("CopyImageInSystemClipboard", false);
    }

    public static boolean isMonitoringCopy() {
        return isMonitoring() && isCopy();
    }

    public static void setCopy(boolean value) {
        UserConfig.setBoolean("CopyImageInSystemClipboard", value);
    }

    public static boolean isSave() {
        return UserConfig.getBoolean("SaveImageInSystemClipboard", false);
    }

    public static void setSave(boolean value) {
        UserConfig.setBoolean("SaveImageInSystemClipboard", value);
    }


    /*
        Image in System Clipboard
     */
    public static void copyToSystemClipboard(BaseController controller, Image image) {
        if (controller == null || image == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ClipboardContent cc = new ClipboardContent();
                cc.putImage(image);
                Clipboard.getSystemClipboard().setContent(cc);
                if (isMonitoringCopy()) {
                    controller.popInformation(message("CopiedInClipBoards"));
                    ControlImagesClipboard.updateClipboards();
                } else {
                    controller.popInformation(message("CopiedInSystemClipBoard"));
                }
            }
        });
    }

    public static Image fetchImageInClipboard(boolean clear) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (!clipboard.hasImage()) {
            return null;
        }
        Image image = clipboard.getImage();
        if (clear) {
            clipboard.clear();
        }
        return image;
    }

    /*
         Image in  MyBox Clipboard
     */
    public static void copyToMyBoxClipboard(BaseController controller, Image image, ImageClipboard.ImageSource source) {
        if (controller == null || image == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (ImageClipboard.add(image, source) != null) {
                    controller.popInformation(message("CopiedInMyBoxClipBoard"));
                } else {
                    controller.popFailed();
                }
            }
        });
    }

}
