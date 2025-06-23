package mara.mybox.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MainMenuController_Image extends MainMenuController_Document {

    @FXML
    protected void openImageEditor(ActionEvent event) {
        openScene(Fxmls.ImageEditorFxml);
    }

    @FXML
    protected void openImagesBrowser(ActionEvent event) {
        openScene(Fxmls.ImagesBrowserFxml);
    }

    @FXML
    protected void openImageAnalyse(ActionEvent event) {
        openScene(Fxmls.ImageAnalyseFxml);
    }

    @FXML
    protected void openImageConverterBatch(ActionEvent event) {
        openScene(Fxmls.ImageConverterBatchFxml);
    }

    @FXML
    protected void openImageSizeBatch(ActionEvent event) {
        openScene(Fxmls.ImageSizeBatchFxml);
    }

    @FXML
    protected void openImageCropBatch(ActionEvent event) {
        openScene(Fxmls.ImageCropBatchFxml);
    }

    @FXML
    protected void openImagePasteBatch(ActionEvent event) {
        openScene(Fxmls.ImagePasteBatchFxml);
    }

    @FXML
    protected void openImageAdjustColorBatch(ActionEvent event) {
        openScene(Fxmls.ImageAdjustColorBatchFxml);
    }

    @FXML
    protected void openImageReduceColorsBatch(ActionEvent event) {
        openScene(Fxmls.ImageReduceColorsBatchFxml);
    }

    @FXML
    protected void openImageReplaceColorBatch(ActionEvent event) {
        openScene(Fxmls.ImageReplaceColorBatchFxml);
    }

    @FXML
    protected void openImageBlendColorBatch(ActionEvent event) {
        openScene(Fxmls.ImageBlendColorBatchFxml);
    }

    @FXML
    protected void openImageGreyBatch(ActionEvent event) {
        openScene(Fxmls.ImageGreyBatchFxml);
    }

    @FXML
    protected void openImageBlackWhiteBatch(ActionEvent event) {
        openScene(Fxmls.ImageBlackWhiteBatchFxml);
    }

    @FXML
    protected void openImageSepiaBatch(ActionEvent event) {
        openScene(Fxmls.ImageSepiaBatchFxml);
    }

    @FXML
    protected void openImageThresholdingBatch(ActionEvent event) {
        openScene(Fxmls.ImageThresholdingBatchFxml);
    }

    @FXML
    protected void openImageMosaicBatch(ActionEvent event) {
        openScene(Fxmls.ImageMosaicBatchFxml);
    }

    @FXML
    protected void openImageGlassBatch(ActionEvent event) {
        openScene(Fxmls.ImageGlassBatchFxml);
    }

    @FXML
    protected void openImageEdgeBatch(ActionEvent event) {
        openScene(Fxmls.ImageEdgeBatchFxml);
    }

    @FXML
    protected void openImageEmbossBatch(ActionEvent event) {
        openScene(Fxmls.ImageEmbossBatchFxml);
    }

    @FXML
    protected void openImageSharpenBatch(ActionEvent event) {
        openScene(Fxmls.ImageSharpenBatchFxml);
    }

    @FXML
    protected void openImageSmoothBatch(ActionEvent event) {
        openScene(Fxmls.ImageSmoothBatchFxml);
    }

    @FXML
    protected void openImageContrastBatch(ActionEvent event) {
        openScene(Fxmls.ImageContrastBatchFxml);
    }

    @FXML
    protected void openImageConvolutionBatch(ActionEvent event) {
        openScene(Fxmls.ImageConvolutionBatchFxml);
    }

    @FXML
    protected void openImageTextBatch(ActionEvent event) {
        openScene(Fxmls.ImageTextBatchFxml);
    }

    @FXML
    protected void openImageRoundBatch(ActionEvent event) {
        openScene(Fxmls.ImageRoundBatchFxml);
    }

    @FXML
    protected void openImageShadowBatch(ActionEvent event) {
        openScene(Fxmls.ImageShadowBatchFxml);
    }

    @FXML
    protected void openImageShearBatch(ActionEvent event) {
        openScene(Fxmls.ImageShearBatchFxml);
    }

    @FXML
    protected void openImageMirrorBatch(ActionEvent event) {
        openScene(Fxmls.ImageMirrorBatchFxml);
    }

    @FXML
    protected void openImageRotateBatch(ActionEvent event) {
        openScene(Fxmls.ImageRotateBatchFxml);
    }

    @FXML
    protected void openImageMarginsBatch(ActionEvent event) {
        openScene(Fxmls.ImageMarginsBatchFxml);
    }

    @FXML
    protected void openImageSplit(ActionEvent event) {
        openScene(Fxmls.ImageSplitFxml);
    }

    @FXML
    protected void openImageSample(ActionEvent event) {
        openScene(Fxmls.ImageSampleFxml);
    }

    @FXML
    protected void ImagesEditor(ActionEvent event) {
        openScene(Fxmls.ImagesEditorFxml);
    }

    @FXML
    protected void imageScope(ActionEvent event) {
        DataTreeController.imageScope(parentController, false);
    }

    @FXML
    protected void ImagesSplice(ActionEvent event) {
        openScene(Fxmls.ImagesSpliceFxml);
    }

    @FXML
    protected void ImageRepeat(ActionEvent event) {
        openScene(Fxmls.ImageRepeatFxml);
    }

    @FXML
    protected void ImagesPlay(ActionEvent event) {
        openScene(Fxmls.ImagesPlayFxml);
    }

    @FXML
    protected void openImageAlphaExtract(ActionEvent event) {
        openScene(Fxmls.ImageAlphaExtractBatchFxml);
    }

    @FXML
    protected void openImageAlphaAdd(ActionEvent event) {
        openScene(Fxmls.ImageAlphaAddBatchFxml);
    }

    @FXML
    protected void openImageOCR(ActionEvent event) {
        openScene(Fxmls.ImageOCRFxml);
    }

    @FXML
    protected void openImageOCRBatch(ActionEvent event) {
        openScene(Fxmls.ImageOCRBatchFxml);
    }

    @FXML
    protected void openSVGEditor(ActionEvent event) {
        openScene(Fxmls.SvgEditorFxml);
    }

    @FXML
    protected void SvgTypesetting(ActionEvent event) {
        openScene(Fxmls.SvgTypesettingFxml);
    }

    @FXML
    protected void SvgToImage(ActionEvent event) {
        openScene(Fxmls.SvgToImageFxml);
    }

    @FXML
    protected void SvgToPDF(ActionEvent event) {
        openScene(Fxmls.SvgToPDFFxml);
    }

    @FXML
    protected void ImageToSvg(ActionEvent event) {
        openScene(Fxmls.SvgFromImageBatchFxml);
    }

    @FXML
    protected void imageOptions(ActionEvent event) {
        openScene(Fxmls.ImageShapeOptionsFxml);
    }

    @FXML
    protected void ImagesInMyBoxClipboard(ActionEvent event) {
        ImageInMyBoxClipboardController.oneOpen();
    }

    @FXML
    protected void ImagesInSystemClipboard(ActionEvent event) {
        ImageInSystemClipboardController.oneOpen();
    }

    @FXML
    protected void openConvolutionKernelManager(ActionEvent event) {
        openScene(Fxmls.ConvolutionKernelManagerFxml);
    }

    @FXML
    protected void openManageColors(ActionEvent event) {
        ColorsManageController.oneOpen();
    }

    @FXML
    protected void queryColor(ActionEvent event) {
        openScene(Fxmls.ColorQueryFxml);
    }

    @FXML
    protected void blendColors(ActionEvent event) {
        openScene(Fxmls.ColorsBlendFxml);
    }

    @FXML
    protected void openIccProfileEditor(ActionEvent event) {
        openScene(Fxmls.IccProfileEditorFxml);
    }

    @FXML
    protected void openChromaticityDiagram(ActionEvent event) {
        openScene(Fxmls.ChromaticityDiagramFxml);
    }

    @FXML
    protected void openChromaticAdaptationMatrix(ActionEvent event) {
        openScene(Fxmls.ChromaticAdaptationMatrixFxml);
    }

    @FXML
    protected void openColorConversion(ActionEvent event) {
        openScene(Fxmls.ColorConversionFxml);
    }

    @FXML
    protected void openRGBColorSpaces(ActionEvent event) {
        openScene(Fxmls.RGBColorSpacesFxml);
    }

    @FXML
    protected void openRGB2XYZConversionMatrix(ActionEvent event) {
        openScene(Fxmls.RGB2XYZConversionMatrixFxml);
    }

    @FXML
    protected void openRGB2RGBConversionMatrix(ActionEvent event) {
        openScene(Fxmls.RGB2RGBConversionMatrixFxml);
    }

    @FXML
    protected void openIlluminants(ActionEvent event) {
        openScene(Fxmls.IlluminantsFxml);
    }

    @FXML
    protected void openPixelsCalculator(ActionEvent event) {
        openStage(Fxmls.PixelsCalculatorFxml);
    }

    @FXML
    protected void ImageBase64(ActionEvent event) {
        openScene(Fxmls.ImageBase64Fxml);
    }

}
