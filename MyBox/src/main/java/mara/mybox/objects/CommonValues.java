package mara.mybox.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:04:05
 * @Description
 * @License Apache License Version 2.0
 */
public class CommonValues {

    public static final double AppVersion = 2.3;
    public static final String AppVersionDate = "2018-07-18";

    public static final String UserFilePath = System.getProperty("user.home") + "/mybox";
    public static final String UserConfigFile = UserFilePath + "/.conf.properties";
    public static final String AlarmClocksFile = UserFilePath + "/.alarmClocks";

    public static final Image AppIcon = new Image("img/mybox.png");

    public static final String MyboxFxml = "/fxml/MyBox.fxml";
    public static final String PdfExtractImagesFxml = "/fxml/PdfExtractImages.fxml";
    public static final String PdfExtractImagesBatchFxml = "/fxml/PdfExtractImagesBatch.fxml";
    public static final String PdfExtractTextsFxml = "/fxml/PdfExtractTexts.fxml";
    public static final String PdfExtractTextsBatchFxml = "/fxml/PdfExtractTextsBatch.fxml";
    public static final String PdfConvertImagesFxml = "/fxml/PdfConvertImages.fxml";
    public static final String PdfConvertImagesBatchFxml = "/fxml/PdfConvertImagesBatch.fxml";
    public static final String PdfInformationFxml = "/fxml/PdfInformation.fxml";
    public static final String FileFxml = "/fxml/PdfConvertImages.fxml";
    public static final String AboutFxml = "/fxml/About.fxml";
    public static final String LoadingFxml = "/fxml/Loading.fxml";
    public static final String ImageInformationFxml = "/fxml/ImageInformation.fxml";
    public static final String ImageViewerFxml = "/fxml/ImageViewer.fxml";
    public static final String ImageViewerIFxml = "/fxml/ImageViewerI.fxml";
    public static final String ImagesViewerFxml = "/fxml/ImagesViewer.fxml";
    public static final String ImageConverterFxml = "/fxml/ImageConverter.fxml";
    public static final String ImageConverterBatchFxml = "/fxml/ImageConverterBatch.fxml";
    public static final String ImageManufactureFxml = "/fxml/ImageManufacture.fxml";
    public static final String ImageMetaDataFxml = "/fxml/ImageMetaData.fxml";
    public static final String PixelsCalculatorFxml = "/fxml/PixelsCalculator.fxml";
    public static final String FilesRenameFxml = "/fxml/FilesRename.fxml";
    public static final String DirectoriesRenameFxml = "/fxml/DirectoriesRename.fxml";
    public static final String DirectorySynchronizeFxml = "/fxml/DirectorySynchronize.fxml";
    public static final String FilesArrangementFxml = "/fxml/FilesArrange.fxml";
    public static final String AlarmClockFxml = "/fxml/AlarmClock.fxml";
    public static final String AlarmClockRunFxml = "/fxml/AlarmClockRun.fxml";

    public static final Locale LocaleZhCN = new Locale("zh", "CN");
    public static final Locale LocaleEnUS = new Locale("en", "US");
    public static final Locale LocaleFrFR = new Locale("fr", "FR");
    public static final Locale LocaleEsES = new Locale("es", "ES");
    public static final Locale LocaleRuRU = new Locale("ru", "RU");

    public static final ResourceBundle BundleZhCN = ResourceBundle.getBundle("bundles/Messages", LocaleZhCN);
    public static final ResourceBundle BundleEnUS = ResourceBundle.getBundle("bundles/Messages", LocaleEnUS);
    public static final ResourceBundle BundleFrFR = ResourceBundle.getBundle("bundles/Messages", LocaleFrFR);
    public static final ResourceBundle BundleEsES = ResourceBundle.getBundle("bundles/Messages", LocaleEsES);
    public static final ResourceBundle BundleRuRU = ResourceBundle.getBundle("bundles/Messages", LocaleRuRU);
    public static final ResourceBundle BundleDefault = ResourceBundle.getBundle("bundles/Messages", Locale.getDefault());

    public static final TimeZone zoneUTC = TimeZone.getTimeZone("GMT+0"); // 世界标准时,UTC
    public static final TimeZone zoneZhCN = TimeZone.getTimeZone("GMT+8"); // 北京时区,东八区 UTC+0800
    public static final String DatetimeFormat = "yyyy-MM-dd HH:mm:ss";
    public static final String DatetimeFormat2 = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DatetimeFormat3 = "yyyy-MM-dd-HH-mm-ss-SSS";

    public static final int InvalidValue = -9999999;

    public static List<FileChooser.ExtensionFilter> ImageExtensionFilter = new ArrayList() {
        {
            add(new FileChooser.ExtensionFilter("images", "*.png", "*.jpg", "*.jpeg", "*.bmp",
                    "*.tif", "*.tiff", "*.gif", "*.pcx", "*.pnm", "*.wbmp"));
            add(new FileChooser.ExtensionFilter("png", "*.png"));
            add(new FileChooser.ExtensionFilter("jpg", "*.jpg", "*.jpeg"));
            add(new FileChooser.ExtensionFilter("bmp", "*.bmp"));
            add(new FileChooser.ExtensionFilter("tif", "*.tif", "*.tiff"));
            add(new FileChooser.ExtensionFilter("gif", "*.gif"));
            add(new FileChooser.ExtensionFilter("pcx", "*.pcx"));
            add(new FileChooser.ExtensionFilter("pnm", "*.pnm"));
            add(new FileChooser.ExtensionFilter("wbmp", "*.wbmp"));
        }
    };

    public static List<String> SupportedImages = new ArrayList() {
        {
            add("png");
            add("jpg");
            add("jpeg");
            add("bmp");
            add("tif");
            add("tiff");
            add("gif");
            add("pcx");
            add("pnm");
            add("wbmp");
        }
    };

    public static List<FileChooser.ExtensionFilter> SoundExtensionFilter = new ArrayList() {
        {
            add(new FileChooser.ExtensionFilter("sound", "*.wav", "*.mp3"));
            add(new FileChooser.ExtensionFilter("wav", "*.wav"));
            add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));
        }
    };

}
