package mara.mybox.value;

import java.util.Arrays;
import java.util.List;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class FileExtensions {

    public static List<String> SupportedImages = Arrays.asList(
            "png", "jpg", "jpeg", "bmp", "tif", "tiff", "gif", "pcx", "pnm", "wbmp", "ico", "icon", "webp"
    );

    public static List<String> NoAlphaImages = Arrays.asList(
            "jpg", "jpeg", "bmp", "pnm", "gif", "wbmp", "pcx"
    );

    public static List<String> AlphaImages = Arrays.asList(
            "png", "tif", "tiff", "ico", "icon", "webp"
    );

    // PNG does not support premultiplyAlpha
    // https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8229013
    public static List<String> PremultiplyAlphaImages = Arrays.asList(
            "tif", "tiff"
    );

    public static List<String> CMYKImages = Arrays.asList(
            "tif", "tiff"
    );

    public static List<String> MultiFramesImages = Arrays.asList(
            "gif", "tif", "tiff"
    );

    public static String[] TextFileSuffix = {"txt", "java", "fxml", "xml",
        "json", "log", "js", "css", "csv", "pom", "ini", "del", "svg", "html", "htm",
        "c", "cpp", "cxx", "cc", "c++", "h", "php", "py", "perl", "iml",
        "sh", "bat", "tcl", "mf", "md", "properties", "env", "cfg", "conf"};

    public static String[] MediaPlayerSupports = {"mp4", "m4a", "mp3", "wav",
        "aif", "aiff", "m3u8"};

}
