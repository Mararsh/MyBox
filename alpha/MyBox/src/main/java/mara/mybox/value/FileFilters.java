package mara.mybox.value;

import java.util.ArrayList;
import java.util.List;
import javafx.stage.FileChooser;

/**
 * @Author Mara
 * @CreateDate 2021-8-1
 * @License Apache License Version 2.0
 */
public class FileFilters {

    public static List<FileChooser.ExtensionFilter> AllExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("*", "*.*", "*"));
        }
    };

    public static List<FileChooser.ExtensionFilter> TextExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("*", "*.*", "*"));
            add(new FileChooser.ExtensionFilter("txt", "*.txt", "*.csv", "*.log", "*.ini", "*.cfg", "*.conf", "*.sh", "*.del", "*.pom", "*.env", "*.properties"));
            add(new FileChooser.ExtensionFilter("codes", "*.java", "*.c", "*.h", "*.py", "*.php", "*.fxml", "*.cpp", "*.cc", "*.js", "*.css", "*.bat"));
            add(new FileChooser.ExtensionFilter("csv", "*.csv"));
            add(new FileChooser.ExtensionFilter("html", "*.html", "*.htm"));
            add(new FileChooser.ExtensionFilter("xml", "*.xml"));
            add(new FileChooser.ExtensionFilter("json", "*.json"));
            add(new FileChooser.ExtensionFilter("markdown", "*.md"));
            add(new FileChooser.ExtensionFilter("svg", "*.svg"));
        }
    };

    public static List<FileChooser.ExtensionFilter> ImagesExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("images", "*.png", "*.jpg", "*.jpeg", "*.bmp",
                    "*.tif", "*.tiff", "*.gif", "*.pcx", "*.pnm", "*.wbmp", "*.ico", "*.icon"));
        }
    };

    public static List<FileChooser.ExtensionFilter> PngExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("png", "*.png"));
        }
    };
    public static List<FileChooser.ExtensionFilter> JpgExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("jpg", "*.jpg", "*.jpeg"));
        }
    };
    public static List<FileChooser.ExtensionFilter> Jpg2000ExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("jpeg2000", "*.jp2", "*.jpeg2000", "*.jpx", "*.jpm"));
        }
    };
    public static List<FileChooser.ExtensionFilter> TiffExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("tif/tiff", "*.tif", "*.tiff"));
        }
    };
    public static List<FileChooser.ExtensionFilter> GifExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("gif", "*.gif"));
        }
    };
    public static List<FileChooser.ExtensionFilter> BmpExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("bmp", "*.bmp"));
        }
    };
    public static List<FileChooser.ExtensionFilter> PcxExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("pcx", "*.pcx"));
        }
    };
    public static List<FileChooser.ExtensionFilter> PnmExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("pnm", "*.pnm"));
        }
    };
    public static List<FileChooser.ExtensionFilter> wbmpExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("wbmp", "*.wbmp"));
        }
    };
    public static List<FileChooser.ExtensionFilter> icoExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("ico", "*.ico", "*.icon"));
        }
    };

    public static List<FileChooser.ExtensionFilter> ImageExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            addAll(ImagesExtensionFilter);
            addAll(PngExtensionFilter);
            addAll(JpgExtensionFilter);
            addAll(TiffExtensionFilter);
            addAll(GifExtensionFilter);
            addAll(icoExtensionFilter);
            addAll(BmpExtensionFilter);
            addAll(PcxExtensionFilter);
            addAll(PnmExtensionFilter);
            addAll(wbmpExtensionFilter);
        }
    };

    public static List<FileChooser.ExtensionFilter> AlphaImageExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("images", "*.png", "*.tif", "*.tiff", "*.ico", "*.icon"));
            addAll(PngExtensionFilter);
            addAll(TiffExtensionFilter);
            addAll(icoExtensionFilter);
        }
    };
    public static List<FileChooser.ExtensionFilter> NoAlphaImageExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("images", "*.jpg", "*.jpeg", "*.bmp", "*.gif", "*.pnm", "*.wbmp"));
            addAll(JpgExtensionFilter);
            addAll(GifExtensionFilter);
            addAll(BmpExtensionFilter);
            addAll(PcxExtensionFilter);
            addAll(PnmExtensionFilter);
            addAll(wbmpExtensionFilter);
        }
    };

    public static List<FileChooser.ExtensionFilter> MultipleFramesImageExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("tif/tiff/gif", "*.tif", "*.tiff", "*.gif"));
            addAll(TiffExtensionFilter);
            addAll(GifExtensionFilter);
        }
    };

    public static List<FileChooser.ExtensionFilter> PdfExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("pdf", "*.pdf", "*.PDF"));
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

    public static List<FileChooser.ExtensionFilter> SoundExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("sound", "*.wav", "*.mp3", "*.m4a", "*.*"));
            add(new FileChooser.ExtensionFilter("wav", "*.wav"));
            add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));
            add(new FileChooser.ExtensionFilter("m4a", "*.m4a"));
            add(new FileChooser.ExtensionFilter("*", "*.*", "*"));
        }
    };

    public static List<FileChooser.ExtensionFilter> Mp3WavExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("sound", "*.wav", "*.mp3"));
            add(new FileChooser.ExtensionFilter("wav", "*.wav"));
            add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));
            add(new FileChooser.ExtensionFilter("*", "*.*", "*"));
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
            add(new FileChooser.ExtensionFilter("*", "*.*", "*"));
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
            add(new FileChooser.ExtensionFilter("*", "*.*", "*"));
        }
    };

    public static List<FileChooser.ExtensionFilter> CertificateExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("DER", "*.cer", "*.crt", "*.rsa"));
            add(new FileChooser.ExtensionFilter("PKCS7", "*.p7b", "*.p7r"));
            add(new FileChooser.ExtensionFilter("CMS", "*.p7c", "*.p7m", "*.p7s"));
            add(new FileChooser.ExtensionFilter("PEM", "*.pem"));
            add(new FileChooser.ExtensionFilter("PKCS10", "*.p10", "*.csr"));
            add(new FileChooser.ExtensionFilter("SPC", "*.pvk", "*.spc"));
            add(new FileChooser.ExtensionFilter("*", "*.*", "*"));
        }
    };

    public static List<FileChooser.ExtensionFilter> KeyStoreExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("*", "*.*", "*"));
            add(new FileChooser.ExtensionFilter("JKS", "*.jks", "*.ks"));
            add(new FileChooser.ExtensionFilter("JCEKS", "*.jce"));
            add(new FileChooser.ExtensionFilter("PKCS12", "*.p12", "*.pfx"));
            add(new FileChooser.ExtensionFilter("BKS", "*.bks"));
            add(new FileChooser.ExtensionFilter("UBER", "*.ubr"));
        }
    };

    public static List<FileChooser.ExtensionFilter> CertExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("X.509", "*.crt"));
        }
    };

    public static List<FileChooser.ExtensionFilter> TTCExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("ttc", "*.ttc"));
        }
    };

    public static List<FileChooser.ExtensionFilter> TTFExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("ttf", "*.ttf"));
        }
    };

    public static List<FileChooser.ExtensionFilter> ExcelExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("excel", "*.xlsx", "*.xls"));
        }
    };

    public static List<FileChooser.ExtensionFilter> CsvExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("csv", "*.csv"));
            add(new FileChooser.ExtensionFilter("*", "*.*", "*"));
        }
    };

    public static List<FileChooser.ExtensionFilter> DataFileExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("*", "*.csv", "*.xlsx", "*.xls", "*.txt"));
            add(new FileChooser.ExtensionFilter("csv", "*.csv"));
            add(new FileChooser.ExtensionFilter("excel", "*.xlsx", "*.xls"));
            addAll(TextExtensionFilter);
        }
    };

    public static List<FileChooser.ExtensionFilter> SheetExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("excel", "*.xlsx", "*.xls"));
            add(new FileChooser.ExtensionFilter("csv", "*.csv"));
            add(new FileChooser.ExtensionFilter("*", "*.*", "*"));
        }
    };

    public static List<FileChooser.ExtensionFilter> WordExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("word", "*.doc"));
        }
    };

    public static List<FileChooser.ExtensionFilter> WordXExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("word", "*.docx"));
        }
    };

    public static List<FileChooser.ExtensionFilter> WordSExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("word", "*.doc", "*.docx"));
        }
    };

    public static List<FileChooser.ExtensionFilter> PPTExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("ppt", "*.ppt"));
        }
    };

    public static List<FileChooser.ExtensionFilter> PPTXExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("pptx", "*.pptx"));
        }
    };

    public static List<FileChooser.ExtensionFilter> PPTSExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("ppt", "*.ppt", "*.pptx"));
        }
    };

    public static List<FileChooser.ExtensionFilter> JarExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("jar", "*.jar"));
        }
    };

    public static List<FileChooser.ExtensionFilter> JSONExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("json", "*.json"));
        }
    };

    public static List<FileChooser.ExtensionFilter> XMLExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("xml", "*.xml"));
        }
    };

    public static List<FileChooser.ExtensionFilter> SVGExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("svg", "*.svg"));
        }
    };

    public static List<FileChooser.ExtensionFilter> ImagesListExtensionFilter = new ArrayList<FileChooser.ExtensionFilter>() {
        {
            add(new FileChooser.ExtensionFilter("*", "*.png", "*.jpg", "*.jpeg", "*.bmp",
                    "*.tif", "*.tiff", "*.gif", "*.pcx", "*.pnm", "*.wbmp", "*.ico", "*.icon", "*.pdf", "*.ppt", "*.pptx"));
            addAll(ImageExtensionFilter);
            addAll(PdfExtensionFilter);
            addAll(PPTSExtensionFilter);
        }
    };

    public static List<FileChooser.ExtensionFilter> imageFilter(String suffix) {
        if (suffix == null || suffix.isBlank()) {
            return ImageExtensionFilter;
        }
        String s = suffix.toLowerCase();
        switch (s) {
            case "png":
                return PngExtensionFilter;
            case "jpg":
                return JpgExtensionFilter;
            case "tif":
            case "tiff":
                return TiffExtensionFilter;
            case "gif":
                return GifExtensionFilter;
            case "ico":
            case "icon":
                return icoExtensionFilter;
            case "bmp":
                return BmpExtensionFilter;
            case "pcx":
                return PcxExtensionFilter;
            case "pnm":
                return PnmExtensionFilter;
            case "wbmp":
                return wbmpExtensionFilter;
            default:
                return ImageExtensionFilter;
        }
    }

}
