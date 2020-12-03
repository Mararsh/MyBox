package mara.mybox.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:04:05
 * @License Apache License Version 2.0
 */
public class CommonValues {

    public static final String AppVersion = "6.3.7";
    public static final String AppVersionDate = "2020-12-03";

    public static final String AppDerbyUser = "mara";
    public static final String AppDerbyPassword = "mybox";

    public static final String MyboxFxml = "/fxml/MyBox.fxml";
    public static final String MyBoxLoadingFxml = "/fxml/MyBoxLoading.fxml";
    public static final String MyBoxSetupFxml = "/fxml/MyBoxSetup.fxml";
    public static final String MyBoxPropertiesFxml = "/fxml/MyBoxProperties.fxml";
    public static final String MyBoxLanguagesFxml = "/fxml/MyBoxLanguages.fxml";
    public static final String MyBoxLogsFxml = "/fxml/MyBoxLogs.fxml";
    public static final String MyBoxLogViewerFxml = "/fxml/MyBoxLogViewer.fxml";
    public static final String ShortcutsFxml = "/fxml/Shortcuts.fxml";
    public static final String PdfHtmlViewerFxml = "/fxml/PdfHtmlViewer.fxml";
    public static final String PdfViewFxml = "/fxml/PdfView.fxml";
    public static final String PdfAttributesFxml = "/fxml/PdfAttributes.fxml";
    public static final String PdfAttributesBatchFxml = "/fxml/PdfAttributesBatch.fxml";
    public static final String PdfExtractImagesBatchFxml = "/fxml/PdfExtractImagesBatch.fxml";
    public static final String PdfExtractTextsBatchFxml = "/fxml/PdfExtractTextsBatch.fxml";
    public static final String PdfConvertHtmlsBatchFxml = "/fxml/PdfConvertHtmlsBatch.fxml";
    public static final String PdfConvertImagesBatchFxml = "/fxml/PdfConvertImagesBatch.fxml";
    public static final String PdfCompressImagesBatchFxml = "/fxml/PdfCompressImagesBatch.fxml";
    public static final String PdfImagesConvertBatchFxml = "/fxml/PdfImagesConvertBatch.fxml";
    public static final String PdfInformationFxml = "/fxml/PdfInformation.fxml";
    public static final String PdfOCRBatchFxml = "/fxml/PdfOCRBatch.fxml";
    public static final String ImagesCombinePdfFxml = "/fxml/ImagesCombinePdf.fxml";
    public static final String PdfMergeFxml = "/fxml/PdfMerge.fxml";
    public static final String PdfSplitBatchFxml = "/fxml/PdfSplitBatch.fxml";
    public static final String AboutFxml = "/fxml/About.fxml";
    public static final String SettingsFxml = "/fxml/Settings.fxml";
    public static final String LoadingFxml = "/fxml/Loading.fxml";
    public static final String DocumentsFxml = "/fxml/Documents.fxml";
    public static final String ImageInformationFxml = "/fxml/ImageInformation.fxml";
    public static final String ImageViewerFxml = "/fxml/ImageViewer.fxml";
    public static final String ImageAnalyseFxml = "/fxml/ImageAnalyse.fxml";
    public static final String ImagesBrowserFxml = "/fxml/ImagesBrowser.fxml";
    public static final String ImageConverterBatchFxml = "/fxml/ImageConverterBatch.fxml";
    public static final String ImageManufactureFxml = "/fxml/ImageManufacture.fxml";
    public static final String ImageManufactureViewFxml = "/fxml/ImageManufactureView.fxml";
    public static final String ImageManufactureCopyFxml = "/fxml/ImageManufactureCopy.fxml";
    public static final String ImageManufactureClipboardFxml = "/fxml/ImageManufactureClipboard.fxml";
    public static final String ImageManufacturePaletteFxml = "/fxml/ImageManufacturePalette.fxml";
    public static final String ImageManufactureSizeFxml = "/fxml/ImageManufactureSize.fxml";
    public static final String ImageManufactureScaleFxml = "/fxml/ImageManufactureScale.fxml";
    public static final String ImageManufactureCropFxml = "/fxml/ImageManufactureCrop.fxml";
    public static final String ImageManufactureColorFxml = "/fxml/ImageManufactureColor.fxml";
    public static final String ImageManufactureEffectsFxml = "/fxml/ImageManufactureEffects.fxml";
    public static final String ImageManufactureEnhancementFxml = "/fxml/ImageManufactureEnhancement.fxml";
    public static final String ImageManufactureTextFxml = "/fxml/ImageManufactureText.fxml";
    public static final String ImageManufactureRichTextFxml = "/fxml/ImageManufactureRichText.fxml";
    public static final String ImageManufactureArcFxml = "/fxml/ImageManufactureArc.fxml";
    public static final String ImageManufacturePenFxml = "/fxml/ImageManufacturePen.fxml";
    public static final String ImageManufactureShadowFxml = "/fxml/ImageManufactureShadow.fxml";
    public static final String ImageManufactureTransformFxml = "/fxml/ImageManufactureTransform.fxml";
    public static final String ImageManufactureMarginsFxml = "/fxml/ImageManufactureMargins.fxml";
    public static final String ImageManufactureBatchFxml = "/fxml/ImageManufactureBatch.fxml";
    public static final String ImageManufactureBatchSizeFxml = "/fxml/ImageManufactureBatchSize.fxml";
    public static final String ImageManufactureBatchCropFxml = "/fxml/ImageManufactureBatchCrop.fxml";
    public static final String ImageManufactureBatchColorFxml = "/fxml/ImageManufactureBatchColor.fxml";
    public static final String ImageManufactureBatchEffectsFxml = "/fxml/ImageManufactureBatchEffects.fxml";
    public static final String ImageManufactureBatchEnhancementFxml = "/fxml/ImageManufactureBatchEnhancement.fxml";
    public static final String ImageManufactureBatchReplaceColorFxml = "/fxml/ImageManufactureBatchReplaceColor.fxml";
    public static final String ImageManufactureBatchTextFxml = "/fxml/ImageManufactureBatchText.fxml";
    public static final String ImageManufactureBatchArcFxml = "/fxml/ImageManufactureBatchArc.fxml";
    public static final String ImageManufactureBatchShadowFxml = "/fxml/ImageManufactureBatchShadow.fxml";
    public static final String ImageManufactureBatchTransformFxml = "/fxml/ImageManufactureBatchTransform.fxml";
    public static final String ImageManufactureBatchMarginsFxml = "/fxml/ImageManufactureBatchMargins.fxml";
    public static final String ImageTextFxml = "/fxml/ImageText.fxml";
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
    public static final String ImageAlphaExtractBatchFxml = "/fxml/ImageAlphaExtractBatch.fxml";
    public static final String ImageAlphaAddBatchFxml = "/fxml/ImageAlphaAddBatch.fxml";
    public static final String ImageOCRFxml = "/fxml/ImageOCR.fxml";
    public static final String ImageOCRBatchFxml = "/fxml/ImageOCRBatch.fxml";
    public static final String ImagePopupFxml = "/fxml/ImagePopup.fxml";
    public static final String ImageTooLargeFxml = "/fxml/ImageTooLarge.fxml";
    public static final String PixelsCalculatorFxml = "/fxml/PixelsCalculator.fxml";
    public static final String ConvolutionKernelManagerFxml = "/fxml/ConvolutionKernelManager.fxml";
    public static final String ManageColorsFxml = "/fxml/ColorsManage.fxml";
    public static final String ColorPaletteManageFxml = "/fxml/ColorPaletteManage.fxml";
    public static final String ColorSetFxml = "/fxml/ColorSet.fxml";
    public static final String ColorPalettePopupFxml = "/fxml/ColorPalettePopup.fxml";
    public static final String ColorInputFxml = "/fxml/ColorInput.fxml";
    public static final String IccProfileEditorFxml = "/fxml/IccProfileEditor.fxml";
    public static final String ChromaticityDiagramFxml = "/fxml/ChromaticityDiagram.fxml";
    public static final String ChromaticAdaptationMatrixFxml = "/fxml/ChromaticAdaptationMatrix.fxml";
    public static final String ColorConversionFxml = "/fxml/ColorConversion.fxml";
    public static final String RGBColorSpacesFxml = "/fxml/RGBColorSpaces.fxml";
    public static final String RGB2XYZConversionMatrixFxml = "/fxml/RGB2XYZConversionMatrix.fxml";
    public static final String RGB2RGBConversionMatrixFxml = "/fxml/RGB2RGBConversionMatrix.fxml";
    public static final String IlluminantsFxml = "/fxml/Illuminants.fxml";
    public static final String MatricesCalculationFxml = "/fxml/MatricesCalculation.fxml";
    public static final String FilesRenameFxml = "/fxml/FilesRename.fxml";
    public static final String DirectorySynchronizeFxml = "/fxml/DirectorySynchronize.fxml";
    public static final String FilesArrangementFxml = "/fxml/FilesArrange.fxml";
    public static final String FilesDeleteEmptyDirFxml = "/fxml/FilesDeleteEmptyDir.fxml";
    public static final String FilesDeleteSysTempFxml = "/fxml/FilesDeleteSysTemp.fxml";
    public static final String FilesDeleteNestedDirFxml = "/fxml/FilesDeleteNestedDir.fxml";
    public static final String AlarmClockFxml = "/fxml/AlarmClock.fxml";
    public static final String AlarmClockRunFxml = "/fxml/AlarmClockRun.fxml";
    public static final String HtmlEditorFxml = "/fxml/HtmlEditor.fxml";
    public static final String HtmlViewerFxml = "/fxml/HtmlViewer.fxml";
    public static final String WeiboSnapFxml = "/fxml/WeiboSnap.fxml";
    public static final String WeiboSnapPostsFxml = "/fxml/WeiboSnapPosts.fxml";
    public static final String WeiboSnapLikeFxml = "/fxml/WeiboSnapLike.fxml";
    public static final String WeiboSnapingInfoFxml = "/fxml/WeiboSnapingInfo.fxml";
    public static final String TextEditerFxml = "/fxml/TextEditer.fxml";
    public static final String TextEncodingBatchFxml = "/fxml/TextEncodingBatch.fxml";
    public static final String TextReplaceBatchFxml = "/fxml/TextReplaceBatch.fxml";
    public static final String TextLineBreakBatchFxml = "/fxml/TextLineBreakBatch.fxml";
    public static final String TextToHtmlFxml = "/fxml/TextToHtml.fxml";
    public static final String BytesEditerFxml = "/fxml/BytesEditer.fxml";
    public static final String FileFilterFxml = "/fxml/FileFilter.fxml";
    public static final String FileCutFxml = "/fxml/FileCut.fxml";
    public static final String FilesMergeFxml = "/fxml/FilesMerge.fxml";
    public static final String FilesDeleteFxml = "/fxml/FilesDelete.fxml";
    public static final String FilesCopyFxml = "/fxml/FilesCopy.fxml";
    public static final String FilesMoveFxml = "/fxml/FilesMove.fxml";
    public static final String FilesFindFxml = "/fxml/FilesFind.fxml";
    public static final String FilesFindBatchFxml = "/fxml/FilesFindBatch.fxml";
    public static final String RecordImagesInSystemClipboardFxml = "/fxml/RecordImagesInSystemClipboard.fxml";
    public static final String BarcodeCreatorFxml = "/fxml/BarcodeCreator.fxml";
    public static final String BarcodeDecoderFxml = "/fxml/BarcodeDecoder.fxml";
    public static final String MarkdownEditorFxml = "/fxml/MarkdownEditor.fxml";
    public static final String MarkdownToHtmlFxml = "/fxml/MarkdownToHtml.fxml";
    public static final String MarkdownToTextFxml = "/fxml/MarkdownToText.fxml";
    public static final String MarkdownToPdfFxml = "/fxml/MarkdownToPdf.fxml";
    public static final String HtmlToMarkdownFxml = "/fxml/HtmlToMarkdown.fxml";
    public static final String HtmlToTextFxml = "/fxml/HtmlToText.fxml";
    public static final String HtmlToPdfFxml = "/fxml/HtmlToPdf.fxml";
    public static final String HtmlSetCharsetFxml = "/fxml/HtmlSetCharset.fxml";
    public static final String HtmlSnapFxml = "/fxml/HtmlSnap.fxml";
    public static final String HtmlMergeAsHtmlFxml = "/fxml/HtmlMergeAsHtml.fxml";
    public static final String HtmlMergeAsMarkdownFxml = "/fxml/HtmlMergeAsMarkdown.fxml";
    public static final String HtmlMergeAsPDFFxml = "/fxml/HtmlMergeAsPDF.fxml";
    public static final String HtmlMergeAsTextFxml = "/fxml/HtmlMergeAsText.fxml";
    public static final String HtmlFramesetFxml = "/fxml/HtmlFrameset.fxml";
    public static final String MessageDigestFxml = "/fxml/MessageDigest.fxml";
    public static final String FilesCompareFxml = "/fxml/FilesCompare.fxml";
    public static final String FilesRedundancyFxml = "/fxml/FilesRedundancy.fxml";
    public static final String FilesRedundancyResultsFxml = "/fxml/FilesRedundancyResults.fxml";
    public static final String FilesArchiveCompressFxml = "/fxml/FilesArchiveCompress.fxml";
    public static final String FilesCompressBatchFxml = "/fxml/FilesCompressBatch.fxml";
    public static final String FileDecompressUnarchiveFxml = "/fxml/FileDecompressUnarchive.fxml";
    public static final String FileDecompressFxml = "/fxml/FileDecompress.fxml";
    public static final String FileUnarchiveFxml = "/fxml/FileUnarchive.fxml";
    public static final String FilesDecompressUnarchiveBatchFxml = "/fxml/FilesDecompressUnarchiveBatch.fxml";
    public static final String FileTTC2TTFFxml = "/fxml/FileTTC2TTF.fxml";
    public static final String WebBrowserFxml = "/fxml/WebBrowser.fxml";
    public static final String WebBrowserBoxFxml = "/fxml/WebBrowserBox.fxml";
    public static final String WebBrowserHistoryFxml = "/fxml/WebBrowserHistory.fxml";
    public static final String HtmlConvertUrlFxml = "/fxml/HtmlConvertUrl.fxml";
    public static final String MediaPlayerFxml = "/fxml/MediaPlayer.fxml";
    public static final String MediaListFxml = "/fxml/MediaList.fxml";
    public static final String FFmpegInformationFxml = "/fxml/FFmpegInformation.fxml";
    public static final String FFmpegProbeMediaInformationFxml = "/fxml/FFmpegProbeMediaInformation.fxml";
    public static final String FFmpegConvertMediaFilesFxml = "/fxml/FFmpegConvertMediaFiles.fxml";
    public static final String FFmpegConvertMediaStreamsFxml = "/fxml/FFmpegConvertMediaStreams.fxml";
    public static final String FFmpegMergeImagesFxml = "/fxml/FFmpegMergeImages.fxml";
    public static final String FFmpegMergeImageFilesFxml = "/fxml/FFmpegMergeImageFiles.fxml";
    public static final String FFmpegScreenRecorderFxml = "/fxml/FFmpegScreenRecorder.fxml";
    public static final String SecurityCertificatesFxml = "/fxml/SecurityCertificates.fxml";
    public static final String SecurityCertificateAddFxml = "/fxml/SecurityCertificateAdd.fxml";
    public static final String SecurityCertificatesBypassFxml = "/fxml/SecurityCertificatesBypass.fxml";
    public static final String DownloadManageFxml = "/fxml/DownloadManage.fxml";
    public static final String DownloadFirstLevelLinksFxml = "/fxml/DownloadFirstLevelLinks.fxml";
    public static final String DownloadFirstLevelLinksSetFxml = "/fxml/DownloadFirstLevelLinksSet.fxml";
    public static final String GameElimniationFxml = "/fxml/GameElimination.fxml";
    public static final String GameMineFxml = "/fxml/GameMine.fxml";
    public static final String GeographyCodeFxml = "/fxml/GeographyCode.fxml";
    public static final String GeographyCodeEditFxml = "/fxml/GeographyCodeEdit.fxml";
    public static final String GeographyCodeImportInternalCSVFxml = "/fxml/GeographyCodeImportInternalCSV.fxml";
    public static final String GeographyCodeImportExternalCSVFxml = "/fxml/GeographyCodeImportExternalCSV.fxml";
    public static final String GeographyCodeImportGeonamesFileFxml = "/fxml/GeographyCodeImportGeonamesFile.fxml";
    public static final String GeographyCodeExportFxml = "/fxml/GeographyCodeExport.fxml";
    public static final String GeographyCodeSelectortFxml = "/fxml/GeographyCodeSelector.fxml";
    public static final String LocationDataFxml = "/fxml/LocationData.fxml";
    public static final String LocationsDataInMapFxml = "/fxml/LocationsDataInMap.fxml";
    public static final String LocationDataEditFxml = "/fxml/LocationDataEdit.fxml";
    public static final String LocationInMapFxml = "/fxml/LocationInMap.fxml";
    public static final String EpidemicReportsFxml = "/fxml/EpidemicReports.fxml";
    public static final String EpidemicReportEditFxml = "/fxml/EpidemicReportEdit.fxml";
    public static final String EpidemicReportsEditFxml = "/fxml/EpidemicReportsEdit.fxml";
    public static final String EpidemicReportsImportBaiduFxml = "/fxml/EpidemicReportsImportBaidu.fxml";
    public static final String EpidemicReportsImportTecentFxml = "/fxml/EpidemicReportsImportTecent.fxml";
    public static final String EpidemicReportsImportInternalCSVFxml = "/fxml/EpidemicReportsImportInternalCSV.fxml";
    public static final String EpidemicReportsImportExternalCSVFxml = "/fxml/EpidemicReportsImportExternalCSV.fxml";
    public static final String EpidemicReportsImport621ExternalFxml = "/fxml/EpidemicReportsImport621External.fxml";
    public static final String EpidemicReportsImportJHUTimeSeriesFxml = "/fxml/EpidemicReportsImportJHUTimeSeries.fxml";
    public static final String EpidemicReportsImportJHUDailyFxml = "/fxml/EpidemicReportsImportJHUDaily.fxml";
    public static final String EpidemicReportsExportFxml = "/fxml/EpidemicReportsExport.fxml";
    public static final String EpidemicReportsStatisticFxml = "/fxml/EpidemicReportsStatistic.fxml";
    public static final String DataQueryFxml = "/fxml/DataQuery.fxml";
    public static final String DataExportFxml = "/fxml/DataExport.fxml";
    public static final String TableManageSelectFxml = "/fxml/TableManageSelect.fxml";
    public static final String TableDataEditFxml = "/fxml/TableDataEdit.fxml";
    public static final String DatasetFxml = "/fxml/Dataset.fxml";
    public static final String DatasetEditFxml = "/fxml/DatasetEdit.fxml";
    public static final String LocationDataImportCSVFxml = "/fxml/LocationDataImportCSV.fxml";
    public static final String LocationDataImportMovebankFxml = "/fxml/LocationDataImportMovebank.fxml";
    public static final String LocationDataExportFxml = "/fxml/LocationDataExport.fxml";
    public static final String LocationToolsFxml = "/fxml/LocationTools.fxml";
    public static final String MessageAuthorFxml = "/fxml/MessageAuthor.fxml";

    public static final Locale LocaleZhCN = new Locale("zh", "CN");
    public static final Locale LocaleEn = new Locale("en");
//    public static final Locale LocaleFrFR = new Locale("fr", "FR");
//    public static final Locale LocaleEsES = new Locale("es", "ES");
//    public static final Locale LocaleRuRU = new Locale("ru", "RU");

    public static final ResourceBundle BundleZhCN = ResourceBundle.getBundle("bundles/Messages", LocaleZhCN);
    public static final ResourceBundle BundleEn = ResourceBundle.getBundle("bundles/Messages", LocaleEn);
//    public static final ResourceBundle BundleFrFR = ResourceBundle.getBundle("bundles/Messages", LocaleFrFR);
//    public static final ResourceBundle BundleEsES = ResourceBundle.getBundle("bundles/Messages", LocaleEsES);
//    public static final ResourceBundle BundleRuRU = ResourceBundle.getBundle("bundles/Messages", LocaleRuRU);

    public static final ResourceBundle TableBundleZhCN = ResourceBundle.getBundle("bundles/TableMessages", LocaleZhCN);
    public static final ResourceBundle TableBundleEn = ResourceBundle.getBundle("bundles/TableMessages", LocaleEn);

    public static final TimeZone zoneUTC = TimeZone.getTimeZone("GMT+0"); // UTC
    public static final TimeZone zoneZhCN = TimeZone.getTimeZone("GMT+8"); // Beijing zone, UTC+0800
    public static final String DatetimeFormat = "yyyy-MM-dd HH:mm:ss";
    public static final String DateFormat = "yyyy-MM-dd";
    public static final String MonthFormat = "yyyy-MM";
    public static final String YearFormat = "yyyy";
    public static final String TimeFormat = "HH:mm:ss";
    public static final String TimeMs = "HH:mm:ss.SSS";
    public static final String DatetimeMs = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DatetimeFormat3 = "yyyy-MM-dd-HH-mm-ss-SSS";
    public static final String DatetimeFormat4 = "yyyyMMddHHmmssSSS";
    public static final String DatetimeFormat6 = "yyyy.MM.dd HH:mm:ss";
    public static final String EraDatetimeEn = "y-M-d H:m:s G";
    public static final String EraDatetimeMsEn = "y-M-d H:m:s.S G";
    public static final String EraDateEn = "y-M-d G";
    public static final String EraMonthEn = "y-M G";
    public static final String EraYearEn = "y G";
    public static final String EraDatetimeZh = "Gy-M-d H:m:s";
    public static final String EraDatetimeMsZh = "Gy-M-d H:m:s.S";
    public static final String EraDateZh = "Gy-M-d";
    public static final String EraMonthZh = "Gy-M";
    public static final String EraYearZh = "Gy";
    public static final String EraDatetime = "y-M-d H:m:s";
    public static final String EraDatetimeMs = "y-M-d H:m:s.S";
    public static final String EraDate = "y-M-d";
    public static final String EraMonth = "y-M";
    public static final String EraYear = "y";
    public static final String EraTime = "H:m:s";
    public static final String EraTimeMs = "H:m:s.S";

    public static final double InvalidDouble = Double.MAX_VALUE;
    public static final long InvalidLong = Long.MIN_VALUE;
    public static final int InvalidInteger = Integer.MIN_VALUE;
    public static final short InvalidShort = Short.MIN_VALUE;

    public static String Indent = "    ";
    public static final int IOBufferLength = 8024;
    public static final String HttpsProtocal = "TLSv1.2";
    public static final String MyBoxSeparator = "##MyBox#";
    public static final String MyBoxInternetDataPath = "https://github.com/Mararsh/MyBox_data";
    public static final String TianDiTuWebKey = "0ddeb917def62b4691500526cc30a9b1";
    public static final String GaoDeMapWebKey = "06b9e078a51325a843dfefd57ffd876c";
    public static final String GaoDeMapServiceKey = "d7444d9a7fae01fa850236d909ad4450";

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

    public static List<String> SupportedImages = new ArrayList<String>() {
        {
            add("png");
            add("jpg");
            add("jpeg");
//            add("jpx");
//            add("jpeg2000");
//            add("jp2");
//            add("jpm");
            add("bmp");
            add("tif");
            add("tiff");
            add("gif");
            add("pcx");
            add("pnm");
            add("wbmp");
            add("ico");
            add("icon");
        }
    };

    public static List<String> NoAlphaImages = new ArrayList<String>() {
        {
            add("jpg");
            add("jpeg");
//            add("jpx");
//            add("jpeg2000");
//            add("jp2");
//            add("jpm");
            add("bmp");
            add("pnm");
            add("gif");
            add("wbmp");
            add("pcx");
        }
    };

    public static List<String> AlphaImages = new ArrayList<String>() {
        {
            add("png");
            add("tif");
            add("tiff");
            add("ico");
            add("icon");
        }
    };

    // PNG does not support premultiplyAlpha
    // https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8229013
    public static List<String> PremultiplyAlphaImages = new ArrayList<String>() {
        {
            add("tif");
            add("tiff");
        }
    };

    public static List<String> CMYKImages = new ArrayList<String>() {
        {
//            add("jpg");
//            add("jpeg");
            add("tif");
            add("tiff");
        }
    };

    public static List<String> MultiFramesImages = new ArrayList<String>() {
        {
            add("gif");
            add("tif");
            add("tiff");
        }
    };

    public static String[] TextFileSuffix = {"txt", "java", "fxml", "xml",
        "json", "log", "js", "css", "csv",
        "c", "cpp", "cxx", "cc", "c++", "h", "php", "py", "perl", "iml",
        "sh", "bat", "tcl", "mf", "md", "properties", "env", "cfg", "conf"};

    public static String[] MediaPlayerSupports = {"mp4", "m4a", "mp3", "wav",
        "aif", "aiff", "m3u8"};

}
