package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_Image extends MyBoxController_Document {

    @FXML
    public void popImageMenu(Event event) {
        if (UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true)) {
            showImageMenu(event);
        }
    }

    @FXML
    protected void showImageMenu(Event event) {
        MenuItem EditImage = new MenuItem(message("EditImage"));
        EditImage.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageEditorFxml);
        });

        MenuItem imageScope = new MenuItem(message("ImageScope"));
        imageScope.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageScopeFxml);
        });

        MenuItem imageOptions = new MenuItem(message("ImageOptions"));
        imageOptions.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageShapeOptionsFxml);
        });

        MenuItem ManageColors = new MenuItem(message("ManageColors"));
        ManageColors.setOnAction((ActionEvent event1) -> {
            ColorsManageController.oneOpen();
        });

        MenuItem QueryColor = new MenuItem(message("ColorQuery"));
        QueryColor.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ColorQueryFxml);
        });

        MenuItem ImagesInMyBoxClipboard = new MenuItem(message("ImagesInMyBoxClipboard"));
        ImagesInMyBoxClipboard.setOnAction((ActionEvent event1) -> {
            ImageInMyBoxClipboardController.oneOpen();
        });

        MenuItem ImagesInSystemClipboard = new MenuItem(message("ImagesInSystemClipboard"));
        ImagesInSystemClipboard.setOnAction((ActionEvent event1) -> {
            ImageInSystemClipboardController.oneOpen();
        });

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(
                EditImage, imageManufactureMenu(), imageBatchMenu(), svgMenu(), imageOptions, new SeparatorMenuItem(),
                ManageColors, QueryColor, colorSpaceMenu(), new SeparatorMenuItem(),
                ImagesInMyBoxClipboard, ImagesInSystemClipboard, miscellaneousMenu()));

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("MyBoxHomeMenuPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        popCenterMenu(imageBox, items);

    }

    private Menu imageManufactureMenu() {

        MenuItem ImageAnalyse = new MenuItem(message("ImageAnalyse"));
        ImageAnalyse.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageAnalyseFxml);
        });

        MenuItem ImagesEditor = new MenuItem(message("ImagesEditor"));
        ImagesEditor.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesEditorFxml);
        });

        MenuItem ImagesSplice = new MenuItem(message("ImagesSplice"));
        ImagesSplice.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImagesSpliceFxml);
        });

        MenuItem ImageSplit = new MenuItem(message("ImageSplit"));
        ImageSplit.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageSplitFxml);
        });

        MenuItem ImageSample = new MenuItem(message("ImageSubsample"));
        ImageSample.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageSampleFxml);
        });

        MenuItem ImageRepeat = new MenuItem(message("ImageRepeatTile"));
        ImageRepeat.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageRepeatFxml);
        });

        MenuItem ImagesPlay = new MenuItem(message("ImagesPlay"));
        ImagesPlay.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesPlayFxml);
        });

        MenuItem imagesBrowser = new MenuItem(message("ImagesBrowser"));
        imagesBrowser.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesBrowserFxml);
        });

        MenuItem imageOCR = new MenuItem(message("ImageOCR"));
        imageOCR.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageOCRFxml);
        });

        Menu manufactureMenu = new Menu(message("ImageManufacture"));

        manufactureMenu.getItems().addAll(
                ImageAnalyse, imageOCR, new SeparatorMenuItem(),
                ImageRepeat, ImagesSplice, ImageSplit, ImageSample, new SeparatorMenuItem(),
                ImagesPlay, ImagesEditor, imagesBrowser);

        return manufactureMenu;

    }

    private Menu imageBatchMenu() {

        MenuItem imageAlphaAdd = new MenuItem(message("ImageAlphaAdd"));
        imageAlphaAdd.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageAlphaAddBatchFxml);
        });

        MenuItem imageAlphaExtract = new MenuItem(message("ImageAlphaExtract"));
        imageAlphaExtract.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageAlphaExtractBatchFxml);
        });

        MenuItem SvgFromImage = new MenuItem(message("ImageToSvg"));
        SvgFromImage.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.SvgFromImageBatchFxml);
        });

        MenuItem imageConverterBatch = new MenuItem(message("FormatsConversion"));
        imageConverterBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageConverterBatchFxml);
        });

        MenuItem imageOCRBatch = new MenuItem(message("ImageOCRBatch"));
        imageOCRBatch.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageOCRBatchFxml);
        });

        Menu imageBatchMenu = new Menu(message("ImageBatch"));
        imageBatchMenu.getItems().addAll(
                imageColorBatchMenu(), imagePixelsBatchMenu(), imageModifyBatchMenu(), new SeparatorMenuItem(),
                imageConverterBatch, imageAlphaExtract, imageAlphaAdd, SvgFromImage, imageOCRBatch);
        return imageBatchMenu;

    }

    private Menu imageColorBatchMenu() {

        MenuItem imageReplaceColorMenu = new MenuItem(message("ReplaceColor"));
        imageReplaceColorMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageReplaceColorBatchFxml);
        });

        MenuItem imageBlendColorMenu = new MenuItem(message("BlendColor"));
        imageBlendColorMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageBlendColorBatchFxml);
        });

        MenuItem imageAdjustColorMenu = new MenuItem(message("AdjustColor"));
        imageAdjustColorMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageAdjustColorBatchFxml);
        });

        MenuItem imageBlackWhiteMenu = new MenuItem(message("BlackOrWhite"));
        imageBlackWhiteMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageBlackWhiteBatchFxml);
        });

        MenuItem imageGreyMenu = new MenuItem(message("Grey"));
        imageGreyMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageGreyBatchFxml);
        });

        MenuItem imageSepiaMenu = new MenuItem(message("Sepia"));
        imageSepiaMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageSepiaBatchFxml);
        });

        MenuItem imageReduceColorsMenu = new MenuItem(message("ReduceColors"));
        imageReduceColorsMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageReduceColorsBatchFxml);
        });

        MenuItem imageThresholdingsMenu = new MenuItem(message("Thresholding"));
        imageThresholdingsMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageThresholdingBatchFxml);
        });

        Menu imageColorBatchMenu = new Menu(message("Color"));
        imageColorBatchMenu.getItems().addAll(
                imageReplaceColorMenu, imageBlendColorMenu, imageAdjustColorMenu,
                imageBlackWhiteMenu, imageGreyMenu, imageSepiaMenu,
                imageReduceColorsMenu, imageThresholdingsMenu
        );
        return imageColorBatchMenu;
    }

    private Menu imagePixelsBatchMenu() {
        MenuItem imageMosaicMenu = new MenuItem(message("Mosaic"));
        imageMosaicMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageMosaicBatchFxml);
        });

        MenuItem imageFrostedGlassMenu = new MenuItem(message("FrostedGlass"));
        imageFrostedGlassMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageGlassBatchFxml);
        });

        MenuItem imageShadowMenu = new MenuItem(message("Shadow"));
        imageShadowMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageShadowBatchFxml);
        });

        MenuItem imageSmoothMenu = new MenuItem(message("Smooth"));
        imageSmoothMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageSmoothBatchFxml);
        });

        MenuItem imageSharpenMenu = new MenuItem(message("Sharpen"));
        imageSharpenMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageSharpenBatchFxml);
        });

        MenuItem imageContrastMenu = new MenuItem(message("Contrast"));
        imageContrastMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageContrastBatchFxml);
        });

        MenuItem imageEdgeMenu = new MenuItem(message("EdgeDetection"));
        imageEdgeMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageEdgeBatchFxml);
        });

        MenuItem imageEmbossMenu = new MenuItem(message("Emboss"));
        imageEmbossMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageEmbossBatchFxml);
        });

        MenuItem imageConvolutionMenu = new MenuItem(message("Convolution"));
        imageConvolutionMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageConvolutionBatchFxml);
        });

        Menu imagePixelsMenu = new Menu(message("Pixels"));
        imagePixelsMenu.getItems().addAll(
                imageMosaicMenu, imageFrostedGlassMenu, imageShadowMenu,
                imageSmoothMenu, imageSharpenMenu,
                imageContrastMenu, imageEdgeMenu, imageEmbossMenu, imageConvolutionMenu);
        return imagePixelsMenu;

    }

    private Menu imageModifyBatchMenu() {
        MenuItem imageSizeMenu = new MenuItem(message("Size"));
        imageSizeMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageSizeBatchFxml);
        });

        MenuItem imageCropMenu = new MenuItem(message("Crop"));
        imageCropMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageCropBatchFxml);
        });

        MenuItem imagePasteMenu = new MenuItem(message("Paste"));
        imagePasteMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImagePasteBatchFxml);
        });

        MenuItem imageTextMenu = new MenuItem(message("Text"));
        imageTextMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageTextBatchFxml);
        });

        MenuItem imageRoundMenu = new MenuItem(message("Round"));
        imageRoundMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageRoundBatchFxml);
        });

        MenuItem imageRotateMenu = new MenuItem(message("Rotate"));
        imageRotateMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageRotateBatchFxml);
        });

        MenuItem imageMirrorMenu = new MenuItem(message("Mirror"));
        imageMirrorMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageMirrorBatchFxml);
        });

        MenuItem imageShearMenu = new MenuItem(message("Shear"));
        imageShearMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageShearBatchFxml);
        });

        MenuItem imageMarginsMenu = new MenuItem(message("Margins"));
        imageMarginsMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageMarginsBatchFxml);
        });

        Menu imagePixelsMenu = new Menu(message("Pixels"));
        imagePixelsMenu.getItems().addAll(
                imageSizeMenu, imageMarginsMenu, imageCropMenu, imageRoundMenu,
                imageRotateMenu, imageMirrorMenu, imageShearMenu,
                imagePasteMenu, imageTextMenu);
        return imagePixelsMenu;

    }

    private Menu svgMenu() {
        MenuItem EditSVG = new MenuItem(message("SVGEditor"));
        EditSVG.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.SvgEditorFxml);
        });

        MenuItem SvgTypesetting = new MenuItem(message("SvgTypesetting"));
        SvgTypesetting.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.SvgTypesettingFxml);
        });

        MenuItem SvgToImage = new MenuItem(message("SvgToImage"));
        SvgToImage.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.SvgToImageFxml);
        });

        MenuItem SvgToPDF = new MenuItem(message("SvgToPDF"));
        SvgToPDF.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.SvgToPDFFxml);
        });

        MenuItem SvgFromImage = new MenuItem(message("ImageToSvg"));
        SvgFromImage.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.SvgFromImageBatchFxml);
        });

        Menu svgMenu = new Menu(message("SVG"));
        svgMenu.getItems().addAll(EditSVG, SvgTypesetting, SvgToImage, SvgToPDF, SvgFromImage);
        return svgMenu;

    }

    private Menu colorSpaceMenu() {
        MenuItem IccEditor = new MenuItem(message("IccProfileEditor"));
        IccEditor.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.IccProfileEditorFxml);
        });

        MenuItem ChromaticityDiagram = new MenuItem(message("DrawChromaticityDiagram"));
        ChromaticityDiagram.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ChromaticityDiagramFxml);
        });

        MenuItem ChromaticAdaptationMatrix = new MenuItem(message("ChromaticAdaptationMatrix"));
        ChromaticAdaptationMatrix.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ChromaticAdaptationMatrixFxml);
        });

        MenuItem ColorConversion = new MenuItem(message("ColorConversion"));
        ColorConversion.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ColorConversionFxml);
        });

        MenuItem RGBColorSpaces = new MenuItem(message("RGBColorSpaces"));
        RGBColorSpaces.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.RGBColorSpacesFxml);
        });

        MenuItem RGB2XYZConversionMatrix = new MenuItem(message("LinearRGB2XYZMatrix"));
        RGB2XYZConversionMatrix.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.RGB2XYZConversionMatrixFxml);
        });

        MenuItem RGB2RGBConversionMatrix = new MenuItem(message("LinearRGB2RGBMatrix"));
        RGB2RGBConversionMatrix.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.RGB2RGBConversionMatrixFxml);
        });

        MenuItem Illuminants = new MenuItem(message("Illuminants"));
        Illuminants.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.IlluminantsFxml);
        });

        Menu csMenu = new Menu(message("ColorSpace"));
        csMenu.getItems().addAll(ChromaticityDiagram, IccEditor,
                //                ColorConversion,
                RGBColorSpaces, RGB2XYZConversionMatrix, RGB2RGBConversionMatrix,
                Illuminants, ChromaticAdaptationMatrix);
        return csMenu;

    }

    private Menu miscellaneousMenu() {

        MenuItem ImageBase64 = new MenuItem(message("ImageBase64"));
        ImageBase64.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageBase64Fxml);
        });

        MenuItem convolutionKernelManager = new MenuItem(message("ConvolutionKernelManager"));
        convolutionKernelManager.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ConvolutionKernelManagerFxml);
        });

        MenuItem pixelsCalculator = new MenuItem(message("PixelsCalculator"));
        pixelsCalculator.setOnAction((ActionEvent event1) -> {
            openStage(Fxmls.PixelsCalculatorFxml);
        });

        Menu miscellaneousMenu = new Menu(message("Miscellaneous"));
        miscellaneousMenu.getItems().addAll(
                ImageBase64, convolutionKernelManager, pixelsCalculator
        );

        return miscellaneousMenu;

    }

}
