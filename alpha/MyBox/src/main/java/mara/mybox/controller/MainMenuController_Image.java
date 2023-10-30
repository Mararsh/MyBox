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
    protected void openImageViewer(ActionEvent event) {
        loadScene(Fxmls.ImageViewerFxml);
    }

    @FXML
    protected void openImageManufacture(ActionEvent event) {
        loadScene(Fxmls.ImageManufactureFxml);
    }

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
    protected void openImageManufactureBatchSize(ActionEvent event) {
        loadScene(Fxmls.ImageManufactureBatchSizeFxml);
    }

    @FXML
    protected void openImageManufactureBatchCrop(ActionEvent event) {
        loadScene(Fxmls.ImageManufactureBatchCropFxml);
    }

    @FXML
    protected void openImageManufactureBatchPaste(ActionEvent event) {
        loadScene(Fxmls.ImageManufactureBatchPasteFxml);
    }

    @FXML
    protected void openImageManufactureBatchColor(ActionEvent event) {
        loadScene(Fxmls.ImageManufactureBatchColorFxml);
    }

    @FXML
    protected void openImageManufactureBatchEffects(ActionEvent event) {
        loadScene(Fxmls.ImageManufactureBatchEffectsFxml);
    }

    @FXML
    protected void openImageManufactureBatchEnhancement(ActionEvent event) {
        loadScene(Fxmls.ImageManufactureBatchEnhancementFxml);
    }

    @FXML
    protected void openImageManufactureBatchReplaceColor(ActionEvent event) {
        loadScene(Fxmls.ImageManufactureBatchReplaceColorFxml);
    }

    @FXML
    protected void openImageManufactureBatchText(ActionEvent event) {
        loadScene(Fxmls.ImageManufactureBatchTextFxml);
    }

    @FXML
    protected void openImageManufactureBatchArc(ActionEvent event) {
        loadScene(Fxmls.ImageManufactureBatchArcFxml);
    }

    @FXML
    protected void openImageManufactureBatchShadow(ActionEvent event) {
        loadScene(Fxmls.ImageManufactureBatchShadowFxml);
    }

    @FXML
    protected void openImageManufactureBatchTransform(ActionEvent event) {
        loadScene(Fxmls.ImageManufactureBatchTransformFxml);
    }

    @FXML
    protected void openImageManufactureBatchMargins(ActionEvent event) {
        loadScene(Fxmls.ImageManufactureBatchMarginsFxml);
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
    protected void ImageScope(ActionEvent event) {
        loadScene(Fxmls.ImageScopeFxml);
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
        loadScene(Fxmls.ColorQueryFxml);
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
