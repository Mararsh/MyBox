package mara.mybox.value;

import java.awt.Color;
import java.io.File;
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

    public static final String AppVersion = "4.9";
    public static final String AppVersionDate = "2019-02-04";

    public static final String AppDataRoot = System.getProperty("user.home") + File.separator + "mybox";
    public static final File AppTempPath = new File(AppDataRoot + File.separator + "AppTemp");
    public static final File AppDerbyPath = new File(AppDataRoot + File.separator + "mybox_derby");
    public static List<File> AppDataPaths = new ArrayList() {
        {
            add(AppTempPath);
            add(AppDerbyPath);
        }
    };

    public static final String UserConfigFile = AppDataRoot + File.separator + ".conf.properties";
    public static final String AlarmClocksFile = AppDataRoot + File.separator + ".alarmClocks";

    public static final String userTempPathKey = "TempDir";

    public static final Image AppIcon = new Image("img/mybox.png");
    public static final String MyBoxStyle = "/styles/MyBox.css";
    public static final String DefaultStyle = "/styles/modena/modena.css";
    public static final String WhiteOnBlackStyle = "/styles/modena/whiteOnBlack.css";
    public static final String YellowOnBlackStyle = "/styles/modena/yellowOnBlack.css";
    public static final String WhiteOnGreenStyle = "/styles/modena/whiteOnGreen.css";
    public static final String GreenOnBlackStyle = "/styles/modena/greenOnBlack.css";
    public static final String WhiteOnPurpleStyle = "/styles/modena/whiteOnPurple.css";
    public static final String PinkOnBlackStyle = "/styles/modena/pinkOnBlack.css";
    public static final String WhiteOnBlueStyle = "/styles/modena/whiteOnBlue.css";
    public static final String BlackOnYellowStyle = "/styles/modena/blackOnYellow.css";
    public static final String caspianStyle = "/styles/caspian/caspian.css";

    public static final String MyboxFxml = "/fxml/MyBox.fxml";
    public static final String PdfViewFxml = "/fxml/PdfView.fxml";
    public static final String PdfExtractImagesFxml = "/fxml/PdfExtractImages.fxml";
    public static final String PdfExtractImagesBatchFxml = "/fxml/PdfExtractImagesBatch.fxml";
    public static final String PdfExtractTextsFxml = "/fxml/PdfExtractTexts.fxml";
    public static final String PdfExtractTextsBatchFxml = "/fxml/PdfExtractTextsBatch.fxml";
    public static final String PdfConvertImagesFxml = "/fxml/PdfConvertImages.fxml";
    public static final String PdfConvertImagesBatchFxml = "/fxml/PdfConvertImagesBatch.fxml";
    public static final String PdfCompressImagesFxml = "/fxml/PdfCompressImages.fxml";
    public static final String PdfCompressImagesBatchFxml = "/fxml/PdfCompressImagesBatch.fxml";
    public static final String PdfInformationFxml = "/fxml/PdfInformation.fxml";
    public static final String ImagesCombinePdfFxml = "/fxml/ImagesCombinePdf.fxml";
    public static final String PdfMergeFxml = "/fxml/PdfMerge.fxml";
    public static final String PdfSplitFxml = "/fxml/PdfSplit.fxml";
    public static final String FileFxml = "/fxml/PdfConvertImages.fxml";
    public static final String AboutFxml = "/fxml/About.fxml";
    public static final String SettingsFxml = "/fxml/Settings.fxml";
    public static final String LoadingFxml = "/fxml/Loading.fxml";
    public static final String ImageInformationFxml = "/fxml/ImageInformation.fxml";
    public static final String ImageViewerFxml = "/fxml/ImageViewer.fxml";
    public static final String ImageViewerIFxml = "/fxml/ImageViewerI.fxml";
    public static final String ImagesBrowserFxml = "/fxml/ImagesBrowser.fxml";
    public static final String ImageConverterFxml = "/fxml/ImageConverter.fxml";
    public static final String ImageConverterBatchFxml = "/fxml/ImageConverterBatch.fxml";
    public static final String ImageManufactureFileFxml = "/fxml/ImageManufactureFile.fxml";
    public static final String ImageManufactureSizeFxml = "/fxml/ImageManufactureSize.fxml";
    public static final String ImageManufactureCropFxml = "/fxml/ImageManufactureCrop.fxml";
    public static final String ImageManufactureColorFxml = "/fxml/ImageManufactureColor.fxml";
    public static final String ImageManufactureEffectsFxml = "/fxml/ImageManufactureEffects.fxml";
    public static final String ImageManufactureTextFxml = "/fxml/ImageManufactureText.fxml";
    public static final String ImageManufactureCoverFxml = "/fxml/ImageManufactureCover.fxml";
    public static final String ImageManufactureArcFxml = "/fxml/ImageManufactureArc.fxml";
    public static final String ImageManufactureShadowFxml = "/fxml/ImageManufactureShadow.fxml";
    public static final String ImageManufactureTransformFxml = "/fxml/ImageManufactureTransform.fxml";
    public static final String ImageManufactureMarginsFxml = "/fxml/ImageManufactureMargins.fxml";
    public static final String ImageManufactureAddMarginsFxml = "/fxml/ImageManufactureAddMargins.fxml";
    public static final String ImageManufactureCutMarginsFxml = "/fxml/ImageManufactureCutMargins.fxml";
    public static final String ImageManufactureViewFxml = "/fxml/ImageManufactureView.fxml";
    public static final String ImageManufactureRefFxml = "/fxml/ImageManufactureRef.fxml";
    public static final String ImageManufactureBrowseFxml = "/fxml/ImageManufactureBrowse.fxml";
    public static final String ImageManufactureBatchFxml = "/fxml/ImageManufactureBatch.fxml";
    public static final String ImageManufactureBatchSizeFxml = "/fxml/ImageManufactureBatchSize.fxml";
    public static final String ImageManufactureBatchCropFxml = "/fxml/ImageManufactureBatchCrop.fxml";
    public static final String ImageManufactureBatchColorFxml = "/fxml/ImageManufactureBatchColor.fxml";
    public static final String ImageManufactureBatchEffectsFxml = "/fxml/ImageManufactureBatchEffects.fxml";
    public static final String ImageManufactureBatchReplaceColorFxml = "/fxml/ImageManufactureBatchReplaceColor.fxml";
    public static final String ImageManufactureBatchTextFxml = "/fxml/ImageManufactureBatchText.fxml";
    public static final String ImageManufactureBatchArcFxml = "/fxml/ImageManufactureBatchArc.fxml";
    public static final String ImageManufactureBatchShadowFxml = "/fxml/ImageManufactureBatchShadow.fxml";
    public static final String ImageManufactureBatchTransformFxml = "/fxml/ImageManufactureBatchTransform.fxml";
    public static final String ImageManufactureBatchAddMarginsFxml = "/fxml/ImageManufactureBatchAddMargins.fxml";
    public static final String ImageManufactureBatchCutMarginsFxml = "/fxml/ImageManufactureBatchCutMargins.fxml";
    public static final String ImagesCombineFxml = "/fxml/ImagesCombine.fxml";
    public static final String ImageMetaDataFxml = "/fxml/ImageMetaData.fxml";
    public static final String ImageSplitFxml = "/fxml/ImageSplit.fxml";
    public static final String ImageSampleFxml = "/fxml/ImageSample.fxml";
    public static final String ImagesBlendFxml = "/fxml/ImagesBlend.fxml";
    public static final String ImageGifViewerFxml = "/fxml/ImageGifViewer.fxml";
    public static final String ImageGifEditerFxml = "/fxml/ImageGifEditer.fxml";
    public static final String ImageTiffEditerFxml = "/fxml/ImageTiffEditer.fxml";
    public static final String ImageFramesViewerFxml = "/fxml/ImageFramesViewer.fxml";
    public static final String ImageStatisticFxml = "/fxml/ImageStatistic.fxml";
    public static final String PixelsCalculatorFxml = "/fxml/PixelsCalculator.fxml";
    public static final String ConvolutionKernelManagerFxml = "/fxml/ConvolutionKernelManager.fxml";
    public static final String ColorPaletteFxml = "/fxml/ColorPalette.fxml";
    public static final String FilesRenameFxml = "/fxml/FilesRename.fxml";
    public static final String DirectorySynchronizeFxml = "/fxml/DirectorySynchronize.fxml";
    public static final String FilesArrangementFxml = "/fxml/FilesArrange.fxml";
    public static final String AlarmClockFxml = "/fxml/AlarmClock.fxml";
    public static final String AlarmClockRunFxml = "/fxml/AlarmClockRun.fxml";
    public static final String HtmlEditorFxml = "/fxml/HtmlEditor.fxml";
    public static final String WeiboSnapFxml = "/fxml/WeiboSnap.fxml";
    public static final String WeiboSnapRunFxml = "/fxml/WeiboSnapRun.fxml";
    public static final String WeiboSnapingInfoFxml = "/fxml/WeiboSnapingInfo.fxml";
    public static final String TextEditerFxml = "/fxml/TextEditer.fxml";
    public static final String TextEncodingBatchFxml = "/fxml/TextEncodingBatch.fxml";
    public static final String TextLineBreakBatchFxml = "/fxml/TextLineBreakBatch.fxml";
    public static final String BytesEditerFxml = "/fxml/BytesEditer.fxml";
    public static final String FileFilterFxml = "/fxml/FileFilter.fxml";
    public static final String FileCutFxml = "/fxml/FileCut.fxml";
    public static final String FileMergeFxml = "/fxml/FileMerge.fxml";
    public static final String RecordImagesInSystemClipboardFxml = "/fxml/RecordImagesInSystemClipboard.fxml";

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

    public static List<FileChooser.ExtensionFilter> PdfExtensionFilter = new ArrayList() {
        {
            add(new FileChooser.ExtensionFilter("pdf", "*.pdf", "*.PDF"));
        }
    };

    public static List<FileChooser.ExtensionFilter> TiffExtensionFilter = new ArrayList() {
        {
            add(new FileChooser.ExtensionFilter("tif/tiff", "*.tif", "*.tiff"));
        }
    };

    public static List<FileChooser.ExtensionFilter> GifExtensionFilter = new ArrayList() {
        {
            add(new FileChooser.ExtensionFilter("gif", "*.gif"));
        }
    };

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

    public static List<String> NoAlphaImages = new ArrayList() {
        {
            add("jpg");
            add("jpeg");
            add("bmp");
            add("pnm");
            add("gif");
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

    public static String[] TextFileSuffix = {"txt", "java", "fxml", "xml", "json", "log", "js", "css",
        "c", "cpp", "cxx", "cc", "c++", "h", "php", "py", "perl", "iml",
        "sh", "bat", "tcl", "mf", "md", "properties", "env", "cfg", "conf"};

    public static List<FileChooser.ExtensionFilter> TextExtensionFilter = new ArrayList() {
        {
            add(new FileChooser.ExtensionFilter("txt", "*.txt", "*.log", "*.ini", "*.cfg", "*.conf", "*.sh"));
            add(new FileChooser.ExtensionFilter("codes", "*.java", "*.c", "*.h", "*.py", "*.php", "*.fxml", "*.cpp", "*.cc", "*.js", "*.css", "*.bat"));
            add(new FileChooser.ExtensionFilter("html", "*.html", "*.htm"));
            add(new FileChooser.ExtensionFilter("xml", "*.xml"));
            add(new FileChooser.ExtensionFilter("json", "*.json"));
            add(new FileChooser.ExtensionFilter("*", "*.*"));
        }
    };

    public static Color AlphaColor = new Color(0, 0, 0, 0);

}
