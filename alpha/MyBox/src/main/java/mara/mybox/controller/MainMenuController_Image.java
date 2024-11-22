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
        loadScene(Fxmls.ImageEditorFxml);
    }

    @FXML
    protected void openImagesBrowser(ActionEvent event) {
        loadScene(Fxmls.ImagesBrowserFxml);
    }

    @FXML
    protected void openImageAnalyse(ActionEvent event) {
        loadScene(Fxmls.ImageAnalyseFxml);
    }

    @FXML
    protected void openImageConverterBatch(ActionEvent event) {
        loadScene(Fxmls.ImageConverterBatchFxml);
    }

    @FXML
    protected void openImageSizeBatch(ActionEvent event) {
        loadScene(Fxmls.ImageSizeBatchFxml);
    }

    @FXML
    protected void openImageCropBatch(ActionEvent event) {
        loadScene(Fxmls.ImageCropBatchFxml);
    }

    @FXML
    protected void openImagePasteBatch(ActionEvent event) {
        loadScene(Fxmls.ImagePasteBatchFxml);
    }

    @FXML
    protected void openImageAdjustColorBatch(ActionEvent event) {
        loadScene(Fxmls.ImageAdjustColorBatchFxml);
    }

    @FXML
    protected void openImageReduceColorsBatch(ActionEvent event) {
        loadScene(Fxmls.ImageReduceColorsBatchFxml);
    }

    @FXML
    protected void openImageReplaceColorBatch(ActionEvent event) {
        loadScene(Fxmls.ImageReplaceColorBatchFxml);
    }

    @FXML
    protected void openImageBlendColorBatch(ActionEvent event) {
        loadScene(Fxmls.ImageBlendColorBatchFxml);
    }

    @FXML
    protected void openImageGreyBatch(ActionEvent event) {
        loadScene(Fxmls.ImageGreyBatchFxml);
    }

    @FXML
    protected void openImageBlackWhiteBatch(ActionEvent event) {
        loadScene(Fxmls.ImageBlackWhiteBatchFxml);
    }

    @FXML
    protected void openImageSepiaBatch(ActionEvent event) {
        loadScene(Fxmls.ImageSepiaBatchFxml);
    }

    @FXML
    protected void openImageThresholdingBatch(ActionEvent event) {
        loadScene(Fxmls.ImageThresholdingBatchFxml);
    }

    @FXML
    protected void openImageMosaicBatch(ActionEvent event) {
        loadScene(Fxmls.ImageMosaicBatchFxml);
    }

    @FXML
    protected void openImageGlassBatch(ActionEvent event) {
        loadScene(Fxmls.ImageGlassBatchFxml);
    }

    @FXML
    protected void openImageEdgeBatch(ActionEvent event) {
        loadScene(Fxmls.ImageEdgeBatchFxml);
    }

    @FXML
    protected void openImageEmbossBatch(ActionEvent event) {
        loadScene(Fxmls.ImageEmbossBatchFxml);
    }

    @FXML
    protected void openImageSharpenBatch(ActionEvent event) {
        loadScene(Fxmls.ImageSharpenBatchFxml);
    }

    @FXML
    protected void openImageSmoothBatch(ActionEvent event) {
        loadScene(Fxmls.ImageSmoothBatchFxml);
    }

    @FXML
    protected void openImageContrastBatch(ActionEvent event) {
        loadScene(Fxmls.ImageContrastBatchFxml);
    }

    @FXML
    protected void openImageConvolutionBatch(ActionEvent event) {
        loadScene(Fxmls.ImageConvolutionBatchFxml);
    }

    @FXML
    protected void openImageTextBatch(ActionEvent event) {
        loadScene(Fxmls.ImageTextBatchFxml);
    }

    @FXML
    protected void openImageRoundBatch(ActionEvent event) {
        loadScene(Fxmls.ImageRoundBatchFxml);
    }

    @FXML
    protected void openImageShadowBatch(ActionEvent event) {
        loadScene(Fxmls.ImageShadowBatchFxml);
    }

    @FXML
    protected void openImageShearBatch(ActionEvent event) {
        loadScene(Fxmls.ImageShearBatchFxml);
    }

    @FXML
    protected void openImageMirrorBatch(ActionEvent event) {
        loadScene(Fxmls.ImageMirrorBatchFxml);
    }

    @FXML
    protected void openImageRotateBatch(ActionEvent event) {
        loadScene(Fxmls.ImageRotateBatchFxml);
    }

    @FXML
    protected void openImageMarginsBatch(ActionEvent event) {
        loadScene(Fxmls.ImageMarginsBatchFxml);
    }

    @FXML
    protected void openImageSplit(ActionEvent event) {
        loadScene(Fxmls.ImageSplitFxml);
    }

    @FXML
    protected void openImageSample(ActionEvent event) {
        loadScene(Fxmls.ImageSampleFxml);
    }

    @FXML
    protected void ImagesEditor(ActionEvent event) {
        loadScene(Fxmls.ImagesEditorFxml);
    }

    @FXML
    protected void imageScope(ActionEvent event) {
        DataTreeController.imageScope(parentController, false);
    }

    @FXML
    protected void ImagesSplice(ActionEvent event) {
        loadScene(Fxmls.ImagesSpliceFxml);
    }

    @FXML
    protected void ImageRepeat(ActionEvent event) {
        loadScene(Fxmls.ImageRepeatFxml);
    }

    @FXML
    protected void ImagesPlay(ActionEvent event) {
        loadScene(Fxmls.ImagesPlayFxml);
    }

    @FXML
    protected void openImageAlphaExtract(ActionEvent event) {
        loadScene(Fxmls.ImageAlphaExtractBatchFxml);
    }

    @FXML
    protected void openImageAlphaAdd(ActionEvent event) {
        loadScene(Fxmls.ImageAlphaAddBatchFxml);
    }

    @FXML
    protected void openImageOCR(ActionEvent event) {
        loadScene(Fxmls.ImageOCRFxml);
    }

    @FXML
    protected void openImageOCRBatch(ActionEvent event) {
        loadScene(Fxmls.ImageOCRBatchFxml);
    }

    @FXML
    protected void openSVGEditor(ActionEvent event) {
        loadScene(Fxmls.SvgEditorFxml);
    }

    @FXML
    protected void SvgTypesetting(ActionEvent event) {
        loadScene(Fxmls.SvgTypesettingFxml);
    }

    @FXML
    protected void SvgToImage(ActionEvent event) {
        loadScene(Fxmls.SvgToImageFxml);
    }

    @FXML
    protected void SvgToPDF(ActionEvent event) {
        loadScene(Fxmls.SvgToPDFFxml);
    }

    @FXML
    protected void ImageToSvg(ActionEvent event) {
        loadScene(Fxmls.SvgFromImageBatchFxml);
    }

    @FXML
    protected void imageOptions(ActionEvent event) {
        loadScene(Fxmls.ImageShapeOptionsFxml);
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
        loadScene(Fxmls.ConvolutionKernelManagerFxml);
    }

    @FXML
    protected void ImageMaterial(ActionEvent event) {
        loadScene(Fxmls.ImageMaterialFxml);
    }

    @FXML
    protected void openManageColors(ActionEvent event) {
        ColorsManageController.oneOpen();
    }

    @FXML
    protected void queryColor(ActionEvent event) {
        ColorQueryController.open();
    }

    @FXML
    protected void openIccProfileEditor(ActionEvent event) {
        loadScene(Fxmls.IccProfileEditorFxml);
    }

    @FXML
    protected void openChromaticityDiagram(ActionEvent event) {
        loadScene(Fxmls.ChromaticityDiagramFxml);
    }

    @FXML
    protected void openChromaticAdaptationMatrix(ActionEvent event) {
        loadScene(Fxmls.ChromaticAdaptationMatrixFxml);
    }

    @FXML
    protected void openColorConversion(ActionEvent event) {
        loadScene(Fxmls.ColorConversionFxml);
    }

    @FXML
    protected void openRGBColorSpaces(ActionEvent event) {
        loadScene(Fxmls.RGBColorSpacesFxml);
    }

    @FXML
    protected void openRGB2XYZConversionMatrix(ActionEvent event) {
        loadScene(Fxmls.RGB2XYZConversionMatrixFxml);
    }

    @FXML
    protected void openRGB2RGBConversionMatrix(ActionEvent event) {
        loadScene(Fxmls.RGB2RGBConversionMatrixFxml);
    }

    @FXML
    protected void openIlluminants(ActionEvent event) {
        loadScene(Fxmls.IlluminantsFxml);
    }

    @FXML
    protected void openPixelsCalculator(ActionEvent event) {
        openStage(Fxmls.PixelsCalculatorFxml);
    }

    @FXML
    protected void ImageBase64(ActionEvent event) {
        loadScene(Fxmls.ImageBase64Fxml);
    }

}
