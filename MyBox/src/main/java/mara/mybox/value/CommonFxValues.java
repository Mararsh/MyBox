package mara.mybox.value;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

/**
 * These values are related to JavaFx and can not be visited before JavaFx
 * thread is started.
 *
 * @author mara
 */
public class CommonFxValues {

    public static final Image AppIcon = new Image("img/MyBox.png");

    public static List<FileChooser.ExtensionFilter> AllExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("*", "*.*"));
//            add(new FileChooser.ExtensionFilter("pdf", "*.pdf", "*.PDF"));
//            add(new FileChooser.ExtensionFilter("images", "*.png", "*.jpg", "*.jpeg", "*.bmp", //                    "*.jpeg2000", "*.jpx", "*.jp2", "*.jpm",
//                    "*.tif", "*.tiff", "*.gif", "*.pcx", "*.pnm", "*.wbmp"));
//            add(new FileChooser.ExtensionFilter("png", "*.png"));
//            add(new FileChooser.ExtensionFilter("jpg", "*.jpg", "*.jpeg"));
//            add(new FileChooser.ExtensionFilter("bmp", "*.bmp"));
//            add(new FileChooser.ExtensionFilter("tif", "*.tif", "*.tiff"));
//            add(new FileChooser.ExtensionFilter("gif", "*.gif"));
//            add(new FileChooser.ExtensionFilter("pcx", "*.pcx"));
//            add(new FileChooser.ExtensionFilter("pnm", "*.pnm"));
//            add(new FileChooser.ExtensionFilter("wbmp", "*.wbmp"));
//            add(new FileChooser.ExtensionFilter("txt", "*.txt", "*.log", "*.ini", "*.cfg", "*.conf", "*.sh"));
//            add(new FileChooser.ExtensionFilter("codes", "*.java", "*.c", "*.h", "*.py", "*.php", "*.fxml", "*.cpp", "*.cc", "*.js", "*.css", "*.bat"));
//            add(new FileChooser.ExtensionFilter("html", "*.html", "*.htm"));
//            add(new FileChooser.ExtensionFilter("xml", "*.xml"));
//            add(new FileChooser.ExtensionFilter("json", "*.json"));
//            add(new FileChooser.ExtensionFilter("sound", "*.wav", "*.mp3"));
//            add(new FileChooser.ExtensionFilter("wav", "*.wav"));
//            add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));
        }
    };

    public static List<FileChooser.ExtensionFilter> ImageExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("images", "*.png", "*.jpg", "*.jpeg", "*.bmp", //                    "*.jpeg2000", "*.jpx", "*.jp2", "*.jpm",
                    "*.tif", "*.tiff", "*.gif", "*.pcx", "*.pnm", "*.wbmp", "*.ico", "*.icon"));
            add(new FileChooser.ExtensionFilter("png", "*.png"));
            add(new FileChooser.ExtensionFilter("jpg", "*.jpg", "*.jpeg"));
            //            add(new FileChooser.ExtensionFilter("jpeg2000", "*.jp2", "*.jpeg2000", "*.jpx", "*.jpm"));
            add(new FileChooser.ExtensionFilter("bmp", "*.bmp"));
            add(new FileChooser.ExtensionFilter("tif", "*.tif", "*.tiff"));
            add(new FileChooser.ExtensionFilter("gif", "*.gif"));
            add(new FileChooser.ExtensionFilter("pcx", "*.pcx"));
            add(new FileChooser.ExtensionFilter("pnm", "*.pnm"));
            add(new FileChooser.ExtensionFilter("wbmp", "*.wbmp"));
            add(new FileChooser.ExtensionFilter("ico", "*.ico", "*.icon"));
        }
    };
    public static List<FileChooser.ExtensionFilter> TextExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("*", "*.*"));
            add(new FileChooser.ExtensionFilter("txt", "*.txt", "*.csv", "*.log", "*.ini", "*.cfg", "*.conf", "*.sh", "*.del"));
            add(new FileChooser.ExtensionFilter("codes", "*.java", "*.c", "*.h", "*.py", "*.php", "*.fxml", "*.cpp", "*.cc", "*.js", "*.css", "*.bat"));
            add(new FileChooser.ExtensionFilter("html", "*.html", "*.htm"));
            add(new FileChooser.ExtensionFilter("xml", "*.xml"));
            add(new FileChooser.ExtensionFilter("json", "*.json"));
            add(new FileChooser.ExtensionFilter("markdown", "*.md"));
            add(new FileChooser.ExtensionFilter("csv", "*.csv"));
        }
    };
    public static List<FileChooser.ExtensionFilter> AlphaImageExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("images", "*.png", "*.tif", "*.tiff", "*.ico", "*.icon"));
            add(new FileChooser.ExtensionFilter("png", "*.png"));
            add(new FileChooser.ExtensionFilter("tif", "*.tif", "*.tiff"));
            add(new FileChooser.ExtensionFilter("ico", "*.ico", "*.icon"));
        }
    };
    public static List<FileChooser.ExtensionFilter> PdfExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("pdf", "*.pdf", "*.PDF"));
        }
    };
    public static List<FileChooser.ExtensionFilter> XmlExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("xml", "*.xml"));
        }
    };

    public static List<FileChooser.ExtensionFilter> HtmlExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("htm", "*.html", "*.htm"));
        }
    };

    public static List<FileChooser.ExtensionFilter> MarkdownExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("markdown", "*.md", "*.MD"));
        }
    };

    public static List<FileChooser.ExtensionFilter> NoAlphaImageExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("images", "*.jpg", "*.jpeg", "*.bmp", //                    "*.jpeg2000", "*.jpx", "*.jp2", "*.jpm",
                    "*.gif", "*.pnm", "*.wbmp"));
            add(new FileChooser.ExtensionFilter("jpg", "*.jpg", "*.jpeg"));
            //            add(new FileChooser.ExtensionFilter("jpeg2000", "*.jp2", "*.jpeg2000", "*.jpx", "*.jpm"));
            add(new FileChooser.ExtensionFilter("bmp", "*.bmp"));
            add(new FileChooser.ExtensionFilter("gif", "*.gif"));
            add(new FileChooser.ExtensionFilter("pnm", "*.pnm"));
            add(new FileChooser.ExtensionFilter("wbmp", "*.wbmp"));
            add(new FileChooser.ExtensionFilter("pcx", "*.pcx"));
        }
    };
    public static List<FileChooser.ExtensionFilter> GifExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("gif", "*.gif"));
        }
    };
    public static List<FileChooser.ExtensionFilter> TiffExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("tif/tiff", "*.tif", "*.tiff"));
        }
    };
    public static List<FileChooser.ExtensionFilter> SoundExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("sound", "*.wav", "*.mp3", "*.m4a", "*.*"));
            add(new FileChooser.ExtensionFilter("wav", "*.wav"));
            add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));
            add(new FileChooser.ExtensionFilter("m4a", "*.m4a"));
            add(new FileChooser.ExtensionFilter("*", "*.*"));
        }
    };
    public static List<FileChooser.ExtensionFilter> Mp3WavExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("sound", "*.wav", "*.mp3"));
            add(new FileChooser.ExtensionFilter("wav", "*.wav"));
            add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));
            add(new FileChooser.ExtensionFilter("*", "*.*"));
        }
    };
    public static List<FileChooser.ExtensionFilter> IccProfileExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("icc", "*.icc", "*.icm"));
            add(new FileChooser.ExtensionFilter("icc", "*.icc"));
            add(new FileChooser.ExtensionFilter("icm", "*.icm"));
        }
    };

    public static List<FileChooser.ExtensionFilter> JdkMediaExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("media", "*.mp4", "*.m4a", "*.m4v", "*.mp3",
                    "*.wav", "*.aif", "*.aiff", "*.m3u8", "*.*"));
            add(new FileChooser.ExtensionFilter("video", "*.mp4", "*.m4v", "*.aif", "*.aiff", "*.m3u8", "*.*"));
            add(new FileChooser.ExtensionFilter("audio", "*.mp4", "*.m4a", "*.mp3",
                    "*.wav", "*.aif", "*.aiff", "*.*"));
            add(new FileChooser.ExtensionFilter("mp4", "*.mp4", "*.m4a", "*.m4v"));
            add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));
            add(new FileChooser.ExtensionFilter("wav", "*.wav"));
            add(new FileChooser.ExtensionFilter("aiff", "*.aif", "*.aiff"));
            add(new FileChooser.ExtensionFilter("hls", "*.m3u8", "*.*"));
            add(new FileChooser.ExtensionFilter("*", "*.*"));
        }
    };

    public static List<FileChooser.ExtensionFilter> Mp4ExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("mp4", "*.mp4", "*.m4a", "*.m4v"));
        }
    };

    public static List<FileChooser.ExtensionFilter> FFmpegMediaExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("media", "*.mp4", "*.m4a", "*.m4v", "*.mp3", "*.flv", "*.mov",
                    "*.wav", "*.aif", "*.aiff", "*.m3u8", "*.*"));
            add(new FileChooser.ExtensionFilter("video", "*.mp4", "*.m4v", "*.aif", "*.aiff", "*.flv", "*.mov", "*.m3u8", "*.*"));
            add(new FileChooser.ExtensionFilter("audio", "*.mp4", "*.m4a", "*.mp3",
                    "*.wav", "*.aif", "*.aiff", "*.*"));
            add(new FileChooser.ExtensionFilter("mp4", "*.mp4", "*.m4a", "*.m4v"));
            add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));
            add(new FileChooser.ExtensionFilter("wav", "*.wav"));
            add(new FileChooser.ExtensionFilter("aiff", "*.aif", "*.aiff"));
            add(new FileChooser.ExtensionFilter("hls", "*.m3u8", "*.*"));
            add(new FileChooser.ExtensionFilter("*", "*.*"));
        }
    };

    public static List<FileChooser.ExtensionFilter> CertificateExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("*", "*.*"));
            add(new FileChooser.ExtensionFilter("DER", "*.cer", "*.crt", "*.rsa"));
            add(new FileChooser.ExtensionFilter("PKCS7", "*.p7b", "*.p7r"));
            add(new FileChooser.ExtensionFilter("CMS", "*.p7c", "*.p7m", "*.p7s"));
            add(new FileChooser.ExtensionFilter("PEM", "*.pem"));
            add(new FileChooser.ExtensionFilter("PKCS10", "*.p10", "*.csr"));
            add(new FileChooser.ExtensionFilter("SPC", "*.pvk", "*.spc"));
        }
    };

    public static List<FileChooser.ExtensionFilter> KeyStoreExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("*", "*.*"));
            add(new FileChooser.ExtensionFilter("JKS", "*.jks", "*.ks"));
            add(new FileChooser.ExtensionFilter("JCEKS", "*.jce"));
            add(new FileChooser.ExtensionFilter("PKCS12", "*.p12", "*.pfx"));
            add(new FileChooser.ExtensionFilter("BKS", "*.bks"));
            add(new FileChooser.ExtensionFilter("UBER", "*.ubr"));
        }
    };

    public static Color TRANSPARENT = new Color(0, 0, 0, 0);

}
