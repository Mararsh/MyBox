package mara.mybox.fxml.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ColorsManageController;
import mara.mybox.controller.DataTreeController;
import mara.mybox.controller.ImageInMyBoxClipboardController;
import mara.mybox.controller.ImageInSystemClipboardController;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-12-9
 * @License Apache License Version 2.0
 */
public class ImageToolsMenu {

    public static List<MenuItem> menusList(BaseController controller) {
        MenuItem EditImage = new MenuItem(message("EditImage"));
        EditImage.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageEditorFxml);
        });

        MenuItem imageScope = new MenuItem(message("ImageScope"));
        imageScope.setOnAction((ActionEvent event) -> {
            DataTreeController.imageScope(controller, true);
        });

        MenuItem imageOptions = new MenuItem(message("ImageOptions"));
        imageOptions.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageShapeOptionsFxml);
        });

        MenuItem ManageColors = new MenuItem(message("ManageColors"));
        ManageColors.setOnAction((ActionEvent event) -> {
            ColorsManageController.oneOpen();
        });

        MenuItem QueryColor = new MenuItem(message("QueryColor"));
        QueryColor.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ColorQueryFxml);
        });

        MenuItem blendColors = new MenuItem(message("BlendColors"));
        blendColors.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ColorsBlendFxml);
        });

        MenuItem ImagesInMyBoxClipboard = new MenuItem(message("ImagesInMyBoxClipboard"));
        ImagesInMyBoxClipboard.setOnAction((ActionEvent event) -> {
            ImageInMyBoxClipboardController.oneOpen();
        });

        MenuItem ImagesInSystemClipboard = new MenuItem(message("ImagesInSystemClipboard"));
        ImagesInSystemClipboard.setOnAction((ActionEvent event) -> {
            ImageInSystemClipboardController.oneOpen();
        });

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(
                EditImage, imageManufactureMenu(controller), imageBatchMenu(controller), svgMenu(controller),
                imageScope, imageOptions, new SeparatorMenuItem(),
                ManageColors, QueryColor, blendColors, colorSpaceMenu(controller), new SeparatorMenuItem(),
                ImagesInMyBoxClipboard, ImagesInSystemClipboard, miscellaneousMenu(controller)));

        return items;

    }

    public static Menu imageManufactureMenu(BaseController controller) {

        MenuItem ImageAnalyse = new MenuItem(message("ImageAnalyse"));
        ImageAnalyse.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageAnalyseFxml);
        });

        MenuItem ImagesEditor = new MenuItem(message("ImagesEditor"));
        ImagesEditor.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImagesEditorFxml);
        });

        MenuItem ImagesSplice = new MenuItem(message("ImagesSplice"));
        ImagesSplice.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImagesSpliceFxml);
        });

        MenuItem ImageSplit = new MenuItem(message("ImageSplit"));
        ImageSplit.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageSplitFxml);
        });

        MenuItem ImageSample = new MenuItem(message("ImageSample"));
        ImageSample.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageSampleFxml);
        });

        MenuItem ImageRepeat = new MenuItem(message("ImageRepeatTile"));
        ImageRepeat.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageRepeatFxml);
        });

        MenuItem ImagesPlay = new MenuItem(message("ImagesPlay"));
        ImagesPlay.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImagesPlayFxml);
        });

        MenuItem imagesBrowser = new MenuItem(message("ImagesBrowser"));
        imagesBrowser.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImagesBrowserFxml);
        });

        MenuItem imageOCR = new MenuItem(message("ImageOCR"));
        imageOCR.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageOCRFxml);
        });

        Menu manufactureMenu = new Menu(message("ImageManufacture"));

        manufactureMenu.getItems().addAll(
                ImageAnalyse, imageOCR, new SeparatorMenuItem(),
                ImageRepeat, ImagesSplice, ImageSplit, ImageSample, new SeparatorMenuItem(),
                ImagesPlay, ImagesEditor, imagesBrowser);

        return manufactureMenu;

    }

    public static Menu imageBatchMenu(BaseController controller) {

        MenuItem imageAlphaAdd = new MenuItem(message("ImageAlphaAdd"));
        imageAlphaAdd.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageAlphaAddBatchFxml);
        });

        MenuItem imageAlphaExtract = new MenuItem(message("ImageAlphaExtract"));
        imageAlphaExtract.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageAlphaExtractBatchFxml);
        });

        MenuItem SvgFromImage = new MenuItem(message("ImageToSvg"));
        SvgFromImage.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.SvgFromImageBatchFxml);
        });

        MenuItem imageConverterBatch = new MenuItem(message("FormatsConversion"));
        imageConverterBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageConverterBatchFxml);
        });

        MenuItem imageOCRBatch = new MenuItem(message("ImageOCRBatch"));
        imageOCRBatch.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageOCRBatchFxml);
        });

        Menu imageBatchMenu = new Menu(message("ImageBatch"));
        imageBatchMenu.getItems().addAll(
                imageColorBatchMenu(controller), imagePixelsBatchMenu(controller), imageModifyBatchMenu(controller), new SeparatorMenuItem(),
                imageConverterBatch, imageAlphaExtract, imageAlphaAdd, SvgFromImage, imageOCRBatch);
        return imageBatchMenu;

    }

    public static Menu imageColorBatchMenu(BaseController controller) {

        MenuItem imageReplaceColorMenu = new MenuItem(message("ReplaceColor"));
        imageReplaceColorMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageReplaceColorBatchFxml);
        });

        MenuItem imageBlendColorMenu = new MenuItem(message("BlendColor"));
        imageBlendColorMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageBlendColorBatchFxml);
        });

        MenuItem imageAdjustColorMenu = new MenuItem(message("AdjustColor"));
        imageAdjustColorMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageAdjustColorBatchFxml);
        });

        MenuItem imageBlackWhiteMenu = new MenuItem(message("BlackOrWhite"));
        imageBlackWhiteMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageBlackWhiteBatchFxml);
        });

        MenuItem imageGreyMenu = new MenuItem(message("Grey"));
        imageGreyMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageGreyBatchFxml);
        });

        MenuItem imageSepiaMenu = new MenuItem(message("Sepia"));
        imageSepiaMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageSepiaBatchFxml);
        });

        MenuItem imageReduceColorsMenu = new MenuItem(message("ReduceColors"));
        imageReduceColorsMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageReduceColorsBatchFxml);
        });

        MenuItem imageThresholdingsMenu = new MenuItem(message("Thresholding"));
        imageThresholdingsMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageThresholdingBatchFxml);
        });

        Menu imageColorBatchMenu = new Menu(message("Color"));
        imageColorBatchMenu.getItems().addAll(
                imageReplaceColorMenu, imageBlendColorMenu, imageAdjustColorMenu,
                imageBlackWhiteMenu, imageGreyMenu, imageSepiaMenu,
                imageReduceColorsMenu, imageThresholdingsMenu
        );
        return imageColorBatchMenu;
    }

    public static Menu imagePixelsBatchMenu(BaseController controller) {
        MenuItem imageMosaicMenu = new MenuItem(message("Mosaic"));
        imageMosaicMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageMosaicBatchFxml);
        });

        MenuItem imageFrostedGlassMenu = new MenuItem(message("FrostedGlass"));
        imageFrostedGlassMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageGlassBatchFxml);
        });

        MenuItem imageShadowMenu = new MenuItem(message("Shadow"));
        imageShadowMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageShadowBatchFxml);
        });

        MenuItem imageSmoothMenu = new MenuItem(message("Smooth"));
        imageSmoothMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageSmoothBatchFxml);
        });

        MenuItem imageSharpenMenu = new MenuItem(message("Sharpen"));
        imageSharpenMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageSharpenBatchFxml);
        });

        MenuItem imageContrastMenu = new MenuItem(message("Contrast"));
        imageContrastMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageContrastBatchFxml);
        });

        MenuItem imageEdgeMenu = new MenuItem(message("EdgeDetection"));
        imageEdgeMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageEdgeBatchFxml);
        });

        MenuItem imageEmbossMenu = new MenuItem(message("Emboss"));
        imageEmbossMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageEmbossBatchFxml);
        });

        MenuItem imageConvolutionMenu = new MenuItem(message("Convolution"));
        imageConvolutionMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageConvolutionBatchFxml);
        });

        Menu imagePixelsMenu = new Menu(message("Pixels"));
        imagePixelsMenu.getItems().addAll(
                imageMosaicMenu, imageFrostedGlassMenu, imageShadowMenu,
                imageSmoothMenu, imageSharpenMenu,
                imageContrastMenu, imageEdgeMenu, imageEmbossMenu, imageConvolutionMenu);
        return imagePixelsMenu;

    }

    public static Menu imageModifyBatchMenu(BaseController controller) {
        MenuItem imageSizeMenu = new MenuItem(message("Size"));
        imageSizeMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageSizeBatchFxml);
        });

        MenuItem imageCropMenu = new MenuItem(message("Crop"));
        imageCropMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageCropBatchFxml);
        });

        MenuItem imagePasteMenu = new MenuItem(message("Paste"));
        imagePasteMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImagePasteBatchFxml);
        });

        MenuItem imageTextMenu = new MenuItem(message("Text"));
        imageTextMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageTextBatchFxml);
        });

        MenuItem imageRoundMenu = new MenuItem(message("Round"));
        imageRoundMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageRoundBatchFxml);
        });

        MenuItem imageRotateMenu = new MenuItem(message("Rotate"));
        imageRotateMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageRotateBatchFxml);
        });

        MenuItem imageMirrorMenu = new MenuItem(message("Mirror"));
        imageMirrorMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageMirrorBatchFxml);
        });

        MenuItem imageShearMenu = new MenuItem(message("Shear"));
        imageShearMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageShearBatchFxml);
        });

        MenuItem imageMarginsMenu = new MenuItem(message("Margins"));
        imageMarginsMenu.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageMarginsBatchFxml);
        });

        Menu imagePixelsMenu = new Menu(message("Modify"));
        imagePixelsMenu.getItems().addAll(
                imageSizeMenu, imageMarginsMenu, imageCropMenu, imageRoundMenu,
                imageRotateMenu, imageMirrorMenu, imageShearMenu,
                imagePasteMenu, imageTextMenu);
        return imagePixelsMenu;

    }

    public static Menu svgMenu(BaseController controller) {
        MenuItem EditSVG = new MenuItem(message("SVGEditor"));
        EditSVG.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.SvgEditorFxml);
        });

        MenuItem SvgTypesetting = new MenuItem(message("SvgTypesetting"));
        SvgTypesetting.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.SvgTypesettingFxml);
        });

        MenuItem SvgToImage = new MenuItem(message("SvgToImage"));
        SvgToImage.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.SvgToImageFxml);
        });

        MenuItem SvgToPDF = new MenuItem(message("SvgToPDF"));
        SvgToPDF.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.SvgToPDFFxml);
        });

        MenuItem SvgFromImage = new MenuItem(message("ImageToSvg"));
        SvgFromImage.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.SvgFromImageBatchFxml);
        });

        Menu svgMenu = new Menu(message("SVG"));
        svgMenu.getItems().addAll(EditSVG, SvgTypesetting, SvgToImage, SvgToPDF, SvgFromImage);
        return svgMenu;

    }

    public static Menu colorSpaceMenu(BaseController controller) {
        MenuItem IccEditor = new MenuItem(message("IccProfileEditor"));
        IccEditor.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.IccProfileEditorFxml);
        });

        MenuItem ChromaticityDiagram = new MenuItem(message("DrawChromaticityDiagram"));
        ChromaticityDiagram.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ChromaticityDiagramFxml);
        });

        MenuItem ChromaticAdaptationMatrix = new MenuItem(message("ChromaticAdaptationMatrix"));
        ChromaticAdaptationMatrix.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ChromaticAdaptationMatrixFxml);
        });

        MenuItem ColorConversion = new MenuItem(message("ColorConversion"));
        ColorConversion.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ColorConversionFxml);
        });

        MenuItem RGBColorSpaces = new MenuItem(message("RGBColorSpaces"));
        RGBColorSpaces.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.RGBColorSpacesFxml);
        });

        MenuItem RGB2XYZConversionMatrix = new MenuItem(message("LinearRGB2XYZMatrix"));
        RGB2XYZConversionMatrix.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.RGB2XYZConversionMatrixFxml);
        });

        MenuItem RGB2RGBConversionMatrix = new MenuItem(message("LinearRGB2RGBMatrix"));
        RGB2RGBConversionMatrix.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.RGB2RGBConversionMatrixFxml);
        });

        MenuItem Illuminants = new MenuItem(message("Illuminants"));
        Illuminants.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.IlluminantsFxml);
        });

        Menu csMenu = new Menu(message("ColorSpace"));
        csMenu.getItems().addAll(ChromaticityDiagram, IccEditor,
                //                ColorConversion,
                RGBColorSpaces, RGB2XYZConversionMatrix, RGB2RGBConversionMatrix,
                Illuminants, ChromaticAdaptationMatrix);
        return csMenu;

    }

    public static Menu miscellaneousMenu(BaseController controller) {

        MenuItem ImageBase64 = new MenuItem(message("ImageBase64"));
        ImageBase64.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ImageBase64Fxml);
        });

        MenuItem convolutionKernelManager = new MenuItem(message("ConvolutionKernelManager"));
        convolutionKernelManager.setOnAction((ActionEvent event) -> {
            controller.openScene(Fxmls.ConvolutionKernelManagerFxml);
        });

        MenuItem pixelsCalculator = new MenuItem(message("PixelsCalculator"));
        pixelsCalculator.setOnAction((ActionEvent event) -> {
            controller.openStage(Fxmls.PixelsCalculatorFxml);
        });

        Menu miscellaneousMenu = new Menu(message("Miscellaneous"));
        miscellaneousMenu.getItems().addAll(
                ImageBase64, convolutionKernelManager, pixelsCalculator
        );

        return miscellaneousMenu;

    }

}
