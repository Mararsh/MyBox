package mara.mybox.fxml;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ControlImagesClipboard;
import static mara.mybox.fxml.ImageClipboardMonitor.DefaultInterval;
import static mara.mybox.value.AppVariables.imageClipboardMonitor;
import mara.mybox.value.Languages;
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

    /*
        Image in System Clipboard
     */
    public static void copyToSystemClipboard(BaseController controller, Image image) {
        if (image == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ClipboardContent cc = new ClipboardContent();
                cc.putImage(image);
                Clipboard.getSystemClipboard().setContent(cc);
                controller.popInformation(Languages.message("CopiedInSystemClipBoard"));
                if (UserConfig.getBoolean("MonitorImageClipboard", false)) {
                    ControlImagesClipboard.updateClipboards();
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

}
