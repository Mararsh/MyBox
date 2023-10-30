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
        MenuItem imageViewer = new MenuItem(message("ImageViewer"));
        imageViewer.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageViewerFxml);
        });

        MenuItem EditImage = new MenuItem(message("EditImage"));
        EditImage.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageEditorFxml);
        });

        MenuItem imagesBrowser = new MenuItem(message("ImagesBrowser"));
        imagesBrowser.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesBrowserFxml);
        });

        MenuItem ImageAnalyse = new MenuItem(message("ImageAnalyse"));
        ImageAnalyse.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageAnalyseFxml);
        });

        MenuItem ImagesPlay = new MenuItem(message("ImagesPlay"));
        ImagesPlay.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImagesPlayFxml);
        });

        MenuItem imageScope = new MenuItem(message("ImageScope"));
        imageScope.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageScopeFxml);
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
        items.addAll(Arrays.asList(imageViewer, EditImage,
                imageManufactureMenu(), manufactureBatchMenu(),
                imageScope, ImageAnalyse, ImagesPlay, imagesBrowser, svgMenu(), new SeparatorMenuItem(),
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

        MenuItem imageOCR = new MenuItem(message("ImageOCR"));
        imageOCR.setOnAction((ActionEvent event1) -> {
            loadScene(Fxmls.ImageOCRFxml);
        });

        Menu manufactureMenu = new Menu(message("ImageManufacture"));

        manufactureMenu.getItems().addAll(
                ImagesEditor, ImageRepeat, ImagesSplice, ImageSplit, ImageSample, new SeparatorMenuItem(),
                imageOCR);

        return manufactureMenu;

    }

    private Menu manufactureBatchMenu() {
        MenuItem imageSizeMenu = new MenuItem(message("Size"));
        imageSizeMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchSizeFxml);
        });

        MenuItem imageCropMenu = new MenuItem(message("Crop"));
        imageCropMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchCropFxml);
        });

        MenuItem imagePasteMenu = new MenuItem(message("Paste"));
        imagePasteMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchPasteFxml);
        });

        MenuItem imageColorMenu = new MenuItem(message("Color"));
        imageColorMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchColorFxml);
        });

        MenuItem imageEffectsMenu = new MenuItem(message("Effects"));
        imageEffectsMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchEffectsFxml);
        });

        MenuItem imageEnhancementMenu = new MenuItem(message("Enhancement"));
        imageEnhancementMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchEnhancementFxml);
        });

        MenuItem imageReplaceColorMenu = new MenuItem(message("ReplaceColor"));
        imageReplaceColorMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchReplaceColorFxml);
        });

        MenuItem imageTextMenu = new MenuItem(message("Text"));
        imageTextMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchTextFxml);
        });

        MenuItem imageArcMenu = new MenuItem(message("Arc"));
        imageArcMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchArcFxml);
        });

        MenuItem imageShadowMenu = new MenuItem(message("Shadow"));
        imageShadowMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchShadowFxml);
        });

        MenuItem imageTransformMenu = new MenuItem(message("Transform"));
        imageTransformMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchTransformFxml);
        });

        MenuItem imageMarginsMenu = new MenuItem(message("Margins"));
        imageMarginsMenu.setOnAction((ActionEvent event) -> {
            loadScene(Fxmls.ImageManufactureBatchMarginsFxml);
        });

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

        Menu manufactureBatchMenu = new Menu(message("ImageManufactureBatch"));
        manufactureBatchMenu.getItems().addAll(imageSizeMenu, imageCropMenu, imagePasteMenu,
                imageColorMenu, imageEffectsMenu, imageEnhancementMenu, imageReplaceColorMenu,
                imageTextMenu, imageArcMenu, imageShadowMenu, imageTransformMenu, imageMarginsMenu, new SeparatorMenuItem(),
                imageConverterBatch, imageAlphaExtract, imageAlphaAdd, SvgFromImage, new SeparatorMenuItem(),
                imageOCRBatch);
        return manufactureBatchMenu;

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
