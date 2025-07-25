package mara.mybox.dev;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.controller.BaseController;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-1-5
 * @License Apache License Version 2.0
 */
public class TestCase {

    protected int id;
    protected String object, version, fxml;
    protected Operation operation;
    protected Type type;
    protected Stage stage;
    protected Status Status;
    protected BaseController controller;

    public static enum Type {
        Function, UserInterface, Bundary, Data, API, IO, Exception, Performance, Robustness,
        Usability, Compatibility, Security, Document
    }

    public static enum Operation {
        Handle, OpenInterface, ClickButton, OpenFile, Edit
    }

    public static enum Stage {
        Alpha, Unit, Integration, Verification, Beta
    }

    public static enum Status {
        NotTested, Testing, Success, Fail
    }

    public TestCase() {
        init();
    }

    public TestCase(int id, String object, String fxml) {
        init();
        this.id = id;
        this.object = object;
        this.fxml = fxml;
    }

    private void init() {
        type = Type.UserInterface;
        operation = Operation.OpenInterface;
        version = AppValues.AppVersion;
        stage = Stage.Alpha;
        Status = Status.NotTested;
    }


    /*
        static
     */
    public static List<TestCase> testCases() {
        List<TestCase> cases = new ArrayList<>();
        try {
            int index = 1;
            cases.add(new TestCase(index++, message("TextTree"), Fxmls.DataTreeFxml).setType(Type.Data));
            cases.add(new TestCase(index++, message("HtmlTree"), Fxmls.DataTreeFxml).setType(Type.Data));
            cases.add(new TestCase(index++, message("PdfView"), Fxmls.PdfViewFxml));
            cases.add(new TestCase(index++, message("PdfConvertImagesBatch"), Fxmls.PdfConvertImagesBatchFxml));
            cases.add(new TestCase(index++, message("PdfImagesConvertBatch"), Fxmls.PdfImagesConvertBatchFxml));
            cases.add(new TestCase(index++, message("PdfCompressImagesBatch"), Fxmls.PdfCompressImagesBatchFxml));
            cases.add(new TestCase(index++, message("PdfConvertHtmlsBatch"), Fxmls.PdfConvertHtmlsBatchFxml));
            cases.add(new TestCase(index++, message("PdfExtractImagesBatch"), Fxmls.PdfExtractImagesBatchFxml));
            cases.add(new TestCase(index++, message("PdfExtractTextsBatch"), Fxmls.PdfExtractTextsBatchFxml));
            cases.add(new TestCase(index++, message("PdfOCRBatch"), Fxmls.PdfOCRBatchFxml));
            cases.add(new TestCase(index++, message("PdfSplitBatch"), Fxmls.PdfSplitBatchFxml));
            cases.add(new TestCase(index++, message("MergePdf"), Fxmls.PdfMergeFxml));
            cases.add(new TestCase(index++, message("PDFAttributes"), Fxmls.PdfAttributesFxml));
            cases.add(new TestCase(index++, message("PDFAttributesBatch"), Fxmls.PdfAttributesBatchFxml));
            cases.add(new TestCase(index++, message("MarkdownEditer"), Fxmls.MarkdownEditorFxml));
            cases.add(new TestCase(index++, message("MarkdownToHtml"), Fxmls.MarkdownToHtmlFxml));
            cases.add(new TestCase(index++, message("MarkdownToText"), Fxmls.MarkdownToTextFxml));
            cases.add(new TestCase(index++, message("MarkdownToPdf"), Fxmls.MarkdownToPdfFxml));
            cases.add(new TestCase(index++, message("HtmlEditor"), Fxmls.HtmlEditorFxml));
            cases.add(new TestCase(index++, message("WebFind"), Fxmls.HtmlFindFxml));
            cases.add(new TestCase(index++, message("WebElements"), Fxmls.HtmlElementsFxml));
            cases.add(new TestCase(index++, message("HtmlSnap"), Fxmls.HtmlSnapFxml));
            cases.add(new TestCase(index++, message("HtmlExtractTables"), Fxmls.HtmlExtractTablesFxml));
            cases.add(new TestCase(index++, message("HtmlToMarkdown"), Fxmls.HtmlToMarkdownFxml));
            cases.add(new TestCase(index++, message("HtmlToText"), Fxmls.HtmlToTextFxml));
            cases.add(new TestCase(index++, message("HtmlToPdf"), Fxmls.HtmlToPdfFxml));
            cases.add(new TestCase(index++, message("HtmlSetCharset"), Fxmls.HtmlSetCharsetFxml));
            cases.add(new TestCase(index++, message("HtmlSetStyle"), Fxmls.HtmlSetStyleFxml));
            cases.add(new TestCase(index++, message("HtmlMergeAsHtml"), Fxmls.HtmlMergeAsHtmlFxml));
            cases.add(new TestCase(index++, message("HtmlMergeAsMarkdown"), Fxmls.HtmlMergeAsMarkdownFxml));
            cases.add(new TestCase(index++, message("HtmlMergeAsPDF"), Fxmls.HtmlMergeAsPDFFxml));
            cases.add(new TestCase(index++, message("HtmlMergeAsText"), Fxmls.HtmlMergeAsTextFxml));
            cases.add(new TestCase(index++, message("HtmlFrameset"), Fxmls.HtmlFramesetFxml));
            cases.add(new TestCase(index++, message("TextEditer"), Fxmls.TextEditorFxml));
            cases.add(new TestCase(index++, message("TextConvertSplit"), Fxmls.TextFilesConvertFxml));
            cases.add(new TestCase(index++, message("TextFilesMerge"), Fxmls.TextFilesMergeFxml));
            cases.add(new TestCase(index++, message("TextReplaceBatch"), Fxmls.TextReplaceBatchFxml));
            cases.add(new TestCase(index++, message("TextFilterBatch"), Fxmls.TextFilterBatchFxml));
            cases.add(new TestCase(index++, message("TextToHtml"), Fxmls.TextToHtmlFxml));
            cases.add(new TestCase(index++, message("TextToPdf"), Fxmls.TextToPdfFxml));
            cases.add(new TestCase(index++, message("WordView"), Fxmls.WordViewFxml));
            cases.add(new TestCase(index++, message("WordToHtml"), Fxmls.WordToHtmlFxml));
            cases.add(new TestCase(index++, message("WordToPdf"), Fxmls.WordToPdfFxml));
            cases.add(new TestCase(index++, message("PptView"), Fxmls.PptViewFxml));
            cases.add(new TestCase(index++, message("PptToImages"), Fxmls.PptToImagesFxml));
            cases.add(new TestCase(index++, message("PptToPdf"), Fxmls.PptToPdfFxml));
            cases.add(new TestCase(index++, message("PptExtract"), Fxmls.PptExtractFxml));
            cases.add(new TestCase(index++, message("PptSplit"), Fxmls.PptSplitFxml));
            cases.add(new TestCase(index++, message("PptxMerge"), Fxmls.PptxMergeFxml));
            cases.add(new TestCase(index++, message("ExtractTextsFromMS"), Fxmls.ExtractTextsFromMSFxml));
            cases.add(new TestCase(index++, message("BytesEditer"), Fxmls.BytesEditorFxml));
            cases.add(new TestCase(index++, message("TextInMyBoxClipboard"), Fxmls.TextInMyBoxClipboardFxml));
            cases.add(new TestCase(index++, message("TextInSystemClipboard"), Fxmls.TextInSystemClipboardFxml));

            cases.add(new TestCase(index++, message("ImagesBrowser"), Fxmls.ImagesBrowserFxml));
            cases.add(new TestCase(index++, message("ImageAnalyse"), Fxmls.ImageAnalyseFxml));
            cases.add(new TestCase(index++, message("ImagesPlay"), Fxmls.ImagesPlayFxml));
            cases.add(new TestCase(index++, message("ImageEditor"), Fxmls.ImageEditorFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("Size"), Fxmls.ImageSizeBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("Crop"), Fxmls.ImageCropBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("Paste"), Fxmls.ImagePasteBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("AdjustColor"), Fxmls.ImageAdjustColorBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("ReplaceColor"), Fxmls.ImageReplaceColorBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("ReduceColors"), Fxmls.ImageReduceColorsBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("EdgeDetection"), Fxmls.ImageEdgeBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("Sharpen"), Fxmls.ImageSharpenBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("Smooth"), Fxmls.ImageSmoothBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("Text"), Fxmls.ImageTextBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("Round"), Fxmls.ImageRoundBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("Shadow"), Fxmls.ImageShadowBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("Mirror"), Fxmls.ImageMirrorBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("Shear"), Fxmls.ImageShearBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("Rotate"), Fxmls.ImageRotateBatchFxml));
            cases.add(new TestCase(index++, message("ImageBatch") + "-" + message("Margins"), Fxmls.ImageMarginsBatchFxml));
            cases.add(new TestCase(index++, message("ImagesSplice"), Fxmls.ImagesSpliceFxml));
            cases.add(new TestCase(index++, message("ImageAlphaAdd"), Fxmls.ImageAlphaAddBatchFxml));
            cases.add(new TestCase(index++, message("ImageSplit"), Fxmls.ImageSplitFxml));
            cases.add(new TestCase(index++, message("ImageSubsample"), Fxmls.ImageSampleFxml));
            cases.add(new TestCase(index++, message("ImageAlphaExtract"), Fxmls.ImageAlphaExtractBatchFxml));
            cases.add(new TestCase(index++, message("ImageConverterBatch"), Fxmls.ImageConverterBatchFxml));
            cases.add(new TestCase(index++, message("ImageOCR"), Fxmls.ImageOCRFxml));
            cases.add(new TestCase(index++, message("ImageOCRBatch"), Fxmls.ImageOCRBatchFxml));
            cases.add(new TestCase(index++, message("ManageColors"), Fxmls.ColorsManageFxml));
            cases.add(new TestCase(index++, message("QueryColor"), Fxmls.ColorQueryFxml));
            cases.add(new TestCase(index++, message("ImageScope"), Fxmls.DataTreeFxml).setType(Type.Data));
            cases.add(new TestCase(index++, message("DrawChromaticityDiagram"), Fxmls.ChromaticityDiagramFxml));
            cases.add(new TestCase(index++, message("IccProfileEditor"), Fxmls.IccProfileEditorFxml));
            cases.add(new TestCase(index++, message("RGBColorSpaces"), Fxmls.RGBColorSpacesFxml));
            cases.add(new TestCase(index++, message("LinearRGB2XYZMatrix"), Fxmls.RGB2XYZConversionMatrixFxml));
            cases.add(new TestCase(index++, message("LinearRGB2RGBMatrix"), Fxmls.RGB2RGBConversionMatrixFxml));
            cases.add(new TestCase(index++, message("Illuminants"), Fxmls.IlluminantsFxml));
            cases.add(new TestCase(index++, message("ChromaticAdaptationMatrix"), Fxmls.ChromaticAdaptationMatrixFxml));
            cases.add(new TestCase(index++, message("ImagesInMyBoxClipboard"), Fxmls.ImageInMyBoxClipboardFxml));
            cases.add(new TestCase(index++, message("ImagesInSystemClipboard"), Fxmls.ImageInSystemClipboardFxml));
            cases.add(new TestCase(index++, message("ConvolutionKernelManager"), Fxmls.ConvolutionKernelManagerFxml));
            cases.add(new TestCase(index++, message("PixelsCalculator"), Fxmls.PixelsCalculatorFxml));
            cases.add(new TestCase(index++, message("ImageBase64"), Fxmls.ImageBase64Fxml));

            cases.add(new TestCase(index++, message("DataManufacture"), Fxmls.Data2DManufactureFxml));
            cases.add(new TestCase(index++, message("ManageData"), Fxmls.Data2DManageFxml));
            cases.add(new TestCase(index++, message("CsvConvert"), Fxmls.DataFileCSVConvertFxml));
            cases.add(new TestCase(index++, message("CsvMerge"), Fxmls.DataFileCSVMergeFxml));
            cases.add(new TestCase(index++, message("ExcelConvert"), Fxmls.DataFileExcelConvertFxml));
            cases.add(new TestCase(index++, message("ExcelMerge"), Fxmls.DataFileExcelMergeFxml));
            cases.add(new TestCase(index++, message("TextDataConvert"), Fxmls.DataFileTextConvertFxml));
            cases.add(new TestCase(index++, message("TextDataMerge"), Fxmls.DataFileTextMergeFxml));
            cases.add(new TestCase(index++, message("DataInSystemClipboard"), Fxmls.DataInMyBoxClipboardFxml));
            cases.add(new TestCase(index++, message("DataInMyBoxClipboard"), Fxmls.DataInSystemClipboardFxml));
            cases.add(new TestCase(index++, message("MatricesManage"), Fxmls.MatricesManageFxml));
            cases.add(new TestCase(index++, message("MatrixUnaryCalculation"), Fxmls.MatrixUnaryCalculationFxml));
            cases.add(new TestCase(index++, message("MatricesBinaryCalculation"), Fxmls.MatricesBinaryCalculationFxml));
            cases.add(new TestCase(index++, message("DatabaseTable"), Fxmls.DataTablesFxml));
            cases.add(new TestCase(index++, message("RowExpression"), Fxmls.DataTreeFxml).setType(Type.Data));
            cases.add(new TestCase(index++, message("DataColumn"), Fxmls.DataTreeFxml).setType(Type.Data));
            cases.add(new TestCase(index++, message("DatabaseSQL"), Fxmls.DataTreeFxml).setType(Type.Data));
            cases.add(new TestCase(index++, message("MathFunction"), Fxmls.DataTreeFxml).setType(Type.Data));
            cases.add(new TestCase(index++, message("JShell"), Fxmls.DataTreeFxml).setType(Type.Data));
            cases.add(new TestCase(index++, message("JEXL"), Fxmls.DataTreeFxml).setType(Type.Data));
            cases.add(new TestCase(index++, message("JavaScript"), Fxmls.DataTreeFxml).setType(Type.Data));
            cases.add(new TestCase(index++, message("GeographyCode"), Fxmls.GeographyCodeFxml).setType(Type.Data));
            cases.add(new TestCase(index++, message("ConvertCoordinate"), Fxmls.ConvertCoordinateFxml));
            cases.add(new TestCase(index++, message("BarcodeCreator"), Fxmls.BarcodeCreatorFxml));
            cases.add(new TestCase(index++, message("BarcodeDecoder"), Fxmls.BarcodeDecoderFxml));
            cases.add(new TestCase(index++, message("MessageDigest"), Fxmls.MessageDigestFxml));
            cases.add(new TestCase(index++, message("Base64Conversion"), Fxmls.Base64Fxml));
            cases.add(new TestCase(index++, message("TTC2TTF"), Fxmls.FileTTC2TTFFxml));

            cases.add(new TestCase(index++, message("FilesArrangement"), Fxmls.FilesArrangementFxml));
            cases.add(new TestCase(index++, message("DirectorySynchronize"), Fxmls.DirectorySynchronizeFxml));
            cases.add(new TestCase(index++, message("FileCut"), Fxmls.FileCutFxml));
            cases.add(new TestCase(index++, message("FilesMerge"), Fxmls.FilesMergeFxml));
            cases.add(new TestCase(index++, message("FilesFind"), Fxmls.FilesFindFxml));
            cases.add(new TestCase(index++, message("FilesRedundancy"), Fxmls.FilesRedundancyFxml));
            cases.add(new TestCase(index++, message("FilesCompare"), Fxmls.FilesCompareFxml));
            cases.add(new TestCase(index++, message("FilesRename"), Fxmls.FilesRenameFxml));
            cases.add(new TestCase(index++, message("FilesCopy"), Fxmls.FilesCopyFxml));
            cases.add(new TestCase(index++, message("FilesMove"), Fxmls.FilesMoveFxml));
            cases.add(new TestCase(index++, message("DeleteJavaIOTemporaryPathFiles"), Fxmls.FilesDeleteJavaTempFxml));
            cases.add(new TestCase(index++, message("DeleteEmptyDirectories"), Fxmls.FilesDeleteEmptyDirFxml));
            cases.add(new TestCase(index++, message("FilesDelete"), Fxmls.FilesDeleteFxml));
            cases.add(new TestCase(index++, message("DeleteNestedDirectories"), Fxmls.FilesDeleteNestedDirFxml));
            cases.add(new TestCase(index++, message("FileDecompressUnarchive"), Fxmls.FileDecompressUnarchiveFxml));
            cases.add(new TestCase(index++, message("FilesDecompressUnarchiveBatch"), Fxmls.FilesDecompressUnarchiveBatchFxml));
            cases.add(new TestCase(index++, message("FilesArchiveCompress"), Fxmls.FilesArchiveCompressFxml));
            cases.add(new TestCase(index++, message("FilesCompressBatch"), Fxmls.FilesCompressBatchFxml));

            cases.add(new TestCase(index++, message("MediaPlayer"), Fxmls.MediaPlayerFxml));
            cases.add(new TestCase(index++, message("ManageMediaLists"), Fxmls.MediaListFxml));
            cases.add(new TestCase(index++, message("FFmpegScreenRecorder"), Fxmls.FFmpegScreenRecorderFxml));
            cases.add(new TestCase(index++, message("FFmpegConvertMediaStreams"), Fxmls.FFmpegConvertMediaStreamsFxml));
            cases.add(new TestCase(index++, message("FFmpegConvertMediaFiles"), Fxmls.FFmpegConvertMediaFilesFxml));
            cases.add(new TestCase(index++, message("FFmpegMergeImagesInformation"), Fxmls.FFmpegMergeImagesFxml));
            cases.add(new TestCase(index++, message("FFmpegMergeImagesFiles"), Fxmls.FFmpegMergeImageFilesFxml));
            cases.add(new TestCase(index++, message("FFmpegProbeMediaInformation"), Fxmls.FFmpegProbeMediaInformationFxml));
            cases.add(new TestCase(index++, message("FFmpegInformation"), Fxmls.FFmpegInformationFxml));
            cases.add(new TestCase(index++, message("AlarmClock"), Fxmls.AlarmClockFxml));
            cases.add(new TestCase(index++, message("GameElimniation"), Fxmls.GameElimniationFxml));
            cases.add(new TestCase(index++, message("GameMine"), Fxmls.GameMineFxml));

            cases.add(new TestCase(index++, message("WebBrowser"), Fxmls.WebBrowserFxml));
            cases.add(new TestCase(index++, message("WebFavorite"), Fxmls.DataTreeFxml).setType(Type.Data));
            cases.add(new TestCase(index++, message("WebHistories"), Fxmls.WebHistoriesFxml));
            cases.add(new TestCase(index++, message("QueryNetworkAddress"), Fxmls.NetworkQueryAddressFxml));
            cases.add(new TestCase(index++, message("QueryDNSBatch"), Fxmls.NetworkQueryDNSBatchFxml));
            cases.add(new TestCase(index++, message("ConvertUrl"), Fxmls.NetworkConvertUrlFxml));
            cases.add(new TestCase(index++, message("DownloadHtmls"), Fxmls.DownloadFirstLevelLinksFxml));
            cases.add(new TestCase(index++, message("SecurityCertificates"), Fxmls.SecurityCertificatesFxml));

            cases.add(new TestCase(index++, message("Settings"), Fxmls.SettingsFxml));

            cases.add(new TestCase(index++, message("MyBoxProperties"), Fxmls.MyBoxPropertiesFxml));
            cases.add(new TestCase(index++, message("MyBoxLogs"), Fxmls.MyBoxLogsFxml));
            cases.add(new TestCase(index++, message("RunSystemCommand"), Fxmls.RunSystemCommandFxml));
            cases.add(new TestCase(index++, message("ManageLanguages"), Fxmls.MyBoxLanguagesFxml));
            cases.add(new TestCase(index++, message("MakeIcons"), Fxmls.MyBoxIconsFxml));
            cases.add(new TestCase(index++, message("AutoTesting"), Fxmls.AutoTestingCasesFxml));
            cases.add(new TestCase(index++, message("MessageAuthor"), Fxmls.MessageAuthorFxml));

            cases.add(new TestCase(index++, message("Shortcuts"), Fxmls.ShortcutsFxml));
            cases.add(new TestCase(index++, message("FunctionsList"), Fxmls.FunctionsListFxml));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return cases;
    }

    /*
        get/set
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getOperationName() {
        return message(operation.name());
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Type getType() {
        return type;
    }

    public String getTypeName() {
        return message(type.name());
    }

    public TestCase setType(Type type) {
        this.type = type;
        return this;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Status getStatus() {
        return Status;
    }

    public String getStatusName() {
        return message(Status.name());
    }

    public void setStatus(Status Status) {
        this.Status = Status;
    }

    public BaseController getController() {
        return controller;
    }

    public void setController(BaseController controller) {
        this.controller = controller;
    }

    public String getFxml() {
        return fxml;
    }

    public void setFxml(String fxml) {
        this.fxml = fxml;
    }

}
