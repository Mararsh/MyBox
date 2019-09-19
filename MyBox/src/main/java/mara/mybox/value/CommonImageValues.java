/*
 * Apache License Version 2.0
 */
package mara.mybox.value;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

/**
 *
 * @author mara
 */
public class CommonImageValues {

    public static final Image AppIcon = new Image("img/MyBox.png");

    public static List<FileChooser.ExtensionFilter> AllExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("*", "*.*"));
            add(new FileChooser.ExtensionFilter("pdf", "*.pdf", "*.PDF"));
            add(new FileChooser.ExtensionFilter("images", "*.png", "*.jpg", "*.jpeg", "*.bmp", //                    "*.jpeg2000", "*.jpx", "*.jp2", "*.jpm",
                    "*.tif", "*.tiff", "*.gif", "*.pcx", "*.pnm", "*.wbmp"));
            add(new FileChooser.ExtensionFilter("png", "*.png"));
            add(new FileChooser.ExtensionFilter("jpg", "*.jpg", "*.jpeg"));
            add(new FileChooser.ExtensionFilter("bmp", "*.bmp"));
            add(new FileChooser.ExtensionFilter("tif", "*.tif", "*.tiff"));
            add(new FileChooser.ExtensionFilter("gif", "*.gif"));
            add(new FileChooser.ExtensionFilter("pcx", "*.pcx"));
            add(new FileChooser.ExtensionFilter("pnm", "*.pnm"));
            add(new FileChooser.ExtensionFilter("wbmp", "*.wbmp"));
            add(new FileChooser.ExtensionFilter("txt", "*.txt", "*.log", "*.ini", "*.cfg", "*.conf", "*.sh"));
            add(new FileChooser.ExtensionFilter("codes", "*.java", "*.c", "*.h", "*.py", "*.php", "*.fxml", "*.cpp", "*.cc", "*.js", "*.css", "*.bat"));
            add(new FileChooser.ExtensionFilter("html", "*.html", "*.htm"));
            add(new FileChooser.ExtensionFilter("xml", "*.xml"));
            add(new FileChooser.ExtensionFilter("json", "*.json"));
            add(new FileChooser.ExtensionFilter("sound", "*.wav", "*.mp3"));
            add(new FileChooser.ExtensionFilter("wav", "*.wav"));
            add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));
        }
    };
    public static List<FileChooser.ExtensionFilter> TxtExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("txt", "*.txt"));
            add(new FileChooser.ExtensionFilter("*", "*.*"));
        }
    };
    public static List<FileChooser.ExtensionFilter> ImageExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("images", "*.png", "*.jpg", "*.jpeg", "*.bmp", //                    "*.jpeg2000", "*.jpx", "*.jp2", "*.jpm",
                    "*.tif", "*.tiff", "*.gif", "*.pcx", "*.pnm", "*.wbmp"));
            add(new FileChooser.ExtensionFilter("png", "*.png"));
            add(new FileChooser.ExtensionFilter("jpg", "*.jpg", "*.jpeg"));
            //            add(new FileChooser.ExtensionFilter("jpeg2000", "*.jp2", "*.jpeg2000", "*.jpx", "*.jpm"));
            add(new FileChooser.ExtensionFilter("bmp", "*.bmp"));
            add(new FileChooser.ExtensionFilter("tif", "*.tif", "*.tiff"));
            add(new FileChooser.ExtensionFilter("gif", "*.gif"));
            add(new FileChooser.ExtensionFilter("pcx", "*.pcx"));
            add(new FileChooser.ExtensionFilter("pnm", "*.pnm"));
            add(new FileChooser.ExtensionFilter("wbmp", "*.wbmp"));
        }
    };
    public static List<FileChooser.ExtensionFilter> TextExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("txt", "*.txt", "*.log", "*.ini", "*.cfg", "*.conf", "*.sh"));
            add(new FileChooser.ExtensionFilter("codes", "*.java", "*.c", "*.h", "*.py", "*.php", "*.fxml", "*.cpp", "*.cc", "*.js", "*.css", "*.bat"));
            add(new FileChooser.ExtensionFilter("html", "*.html", "*.htm"));
            add(new FileChooser.ExtensionFilter("xml", "*.xml"));
            add(new FileChooser.ExtensionFilter("json", "*.json"));
            add(new FileChooser.ExtensionFilter("*", "*.*"));
        }
    };
    public static List<FileChooser.ExtensionFilter> AlphaImageExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("images", "*.png", "*.tif", "*.tiff"));
            add(new FileChooser.ExtensionFilter("png", "*.png"));
            add(new FileChooser.ExtensionFilter("tif", "*.tif", "*.tiff"));
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
            add(new FileChooser.ExtensionFilter("sound", "*.wav", "*.mp3"));
            add(new FileChooser.ExtensionFilter("wav", "*.wav"));
            add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));
        }
    };
    public static List<FileChooser.ExtensionFilter> IccProfileExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("icc", "*.icc", "*.icm"));
            add(new FileChooser.ExtensionFilter("icc", "*.icc"));
            add(new FileChooser.ExtensionFilter("icm", "*.icm"));
        }
    };
    public static Color TRANSPARENT = new Color(0, 0, 0, 0);

}
