package mara.mybox.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.ImageConvertTools;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.imagefile.ImageFileReaders;
import static mara.mybox.value.AppVariables.MyboxDataPath;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class IconTools {

    public static File readIcon(FxTask task, String address, boolean download) {
        try {
            if (address == null) {
                return null;
            }
            URL url = UrlTools.url(address);
            if (url == null) {
                return null;
            }
            String host = url.getHost();
            if (host == null || host.isBlank()) {
                return null;
            }
            File file = FxFileTools.getInternalFile("/icons/" + host + ".png", "icons", host + ".png");

            if (file == null || !file.exists()) {
                file = FxFileTools.getInternalFile("/icons/" + host + ".ico", "icons", host + ".ico");
                if ((file == null || !file.exists()) && download) {
                    file = new File(MyboxDataPath + File.separator + "icons" + File.separator + host + ".ico");
                    file = readIcon(task, address, file);
                }
            }
            return file != null && file.exists() ? file : null;
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return null;
        }
    }

    // https://www.cnblogs.com/luguo3000/p/3767380.html
    public static File readIcon(FxTask task, String address, File targetFile) {
        File actualTarget = readHostIcon(task, address, targetFile);
        if (actualTarget == null) {
            actualTarget = readHtmlIcon(task, address, targetFile);
        }
        if (actualTarget != null) {
            BufferedImage image = ImageFileReaders.readImage(task, actualTarget);
            if (image != null) {
                String name = actualTarget.getAbsolutePath();
                if (name.endsWith(".ico")) {
                    ImageAttributes attributes = new ImageAttributes()
                            .setImageFormat("png").setColorSpaceName("sRGB")
                            .setAlpha(ImageAttributes.Alpha.Keep).setQuality(100);
                    File png = new File(name.substring(0, name.lastIndexOf(".")) + ".png");
                    ImageConvertTools.convertColorSpace(task, actualTarget, attributes, png);
                    if (png.exists()) {
                        FileDeleteTools.delete(actualTarget);
                        actualTarget = png;
                    }
                }
                return actualTarget;
            } else {
                FileDeleteTools.delete(actualTarget);
            }
        }
        return null;
    }

    public static File readHostIcon(FxTask task, String address, File targetFile) {
        try {
            if (address == null || targetFile == null) {
                return null;
            }
            URL url = UrlTools.url(address);
            if (url == null) {
                return null;
            }
            String iconUrl = "https://" + url.getHost() + "/favicon.ico";
            File actualTarget = downloadIcon(task, iconUrl, targetFile);
            if (actualTarget == null) {
                iconUrl = "http://" + url.getHost() + "/favicon.ico";
                actualTarget = downloadIcon(task, iconUrl, targetFile);
            }
            return actualTarget;
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return null;
        }
    }

    public static File readHtmlIcon(FxTask task, String address, File targetFile) {
        try {
            if (address == null) {
                return null;
            }
            String iconUrl = htmlIconAddress(task, address);
            if (iconUrl == null) {
                return null;
            }
            return downloadIcon(task, iconUrl, targetFile);
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return null;
        }
    }

    public static File downloadIcon(FxTask task, String address, File targetFile) {
        try {
            if (address == null || targetFile == null) {
                return null;
            }
            File iconFile = HtmlReadTools.download(task, address, 1000, 1000);
            if (iconFile == null || !iconFile.exists()) {
                return null;
            }
            String suffix = FileNameTools.suffix(address);
            File actualTarget = targetFile;
            if (suffix != null && !suffix.isBlank()) {
                actualTarget = new File(FileNameTools.replaceSuffix(targetFile.getAbsolutePath(), suffix));
            }
            FileTools.rename(iconFile, actualTarget);
            return actualTarget;
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return null;
        }
    }

    public static String htmlIconAddress(FxTask task, String address) {
        try {
            if (address == null) {
                return null;
            }
            String html = HtmlReadTools.url2html(task, address);
            Pattern[] ICON_PATTERNS = new Pattern[]{
                Pattern.compile("rel=[\"']shortcut icon[\"'][^\r\n>]+?((?<=href=[\"']).+?(?=[\"']))"),
                Pattern.compile("((?<=href=[\"']).+?(?=[\"']))[^\r\n<]+?rel=[\"']shortcut icon[\"']")};
            for (Pattern iconPattern : ICON_PATTERNS) {
                Matcher matcher = iconPattern.matcher(html);
                if (matcher.find()) {
                    String iconUrl = matcher.group(1);
                    if (iconUrl.contains("http")) {
                        return iconUrl;
                    }
                    if (iconUrl.charAt(0) == '/') {
                        URL url = UrlTools.url(address);
                        if (url == null) {
                            return null;
                        }
                        iconUrl = url.getProtocol() + "://" + url.getHost() + iconUrl;
                    } else {
                        iconUrl = address + "/" + iconUrl;
                    }
                    return iconUrl;
                }
            }
            return null;
        } catch (Exception e) {
//            MyBoxLog.debug(e);
            return null;
        }
    }

}
