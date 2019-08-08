package mara.mybox.controller;

import java.text.MessageFormat;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mara.mybox.controller.base.BaseController;
import mara.mybox.data.AlarmClock;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.scheduledTasks;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:48:15
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBoxController extends BaseController {

    private ContextMenu pdfMenu, imageMenu, fileMenu, desktopMenu, languageMenu,
            networkMenu, recentMenu, dataMenu;

    @FXML
    private VBox imageBox, pdfBox, fileBox, desktopBox, recentBox, networkBox, dataBox;
    @FXML
    private Label langLabel;

    public MyBoxController() {
        baseTitle = AppVaribles.message("AppTitle");

    }

    @Override
    public void initializeNext() {
        try {
            initPdfToolsMenu();
            initImageToolsMenu();
            initDesktopToolsMenu();
            initFileToolsMenu();
            initNetworkToolsMenu();
            initDataToolsMenu();
            initLanguageMenu();

            List<AlarmClock> alarms = AlarmClock.readAlarmClocks();
            if (alarms != null) {
                for (AlarmClock alarm : alarms) {
                    if (alarm.isIsActive()) {
                        AlarmClock.scehduleAlarmClock(alarm);
                    }
                }
                if (scheduledTasks != null && scheduledTasks.size() > 0) {
                    bottomLabel.setText(MessageFormat.format(AppVaribles.message("AlarmClocksRunning"), scheduledTasks.size()));
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void initPdfToolsMenu() {
        MenuItem pdfView = new MenuItem(AppVaribles.message("PdfView"));
        pdfView.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfViewFxml);
            }
        });

        MenuItem PDFAttributes = new MenuItem(AppVaribles.message("PDFAttributes"));
        PDFAttributes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfAttributesFxml);
            }
        });

        MenuItem PDFAttributesBatch = new MenuItem(AppVaribles.message("PDFAttributesBatch"));
        PDFAttributesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfAttributesBatchFxml);
            }
        });

        MenuItem pdfExtractImagesBatch = new MenuItem(AppVaribles.message("PdfExtractImagesBatch"));
        pdfExtractImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfExtractImagesBatchFxml);
            }
        });

        MenuItem pdfExtractTextsBatch = new MenuItem(AppVaribles.message("PdfExtractTextsBatch"));
        pdfExtractTextsBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfExtractTextsBatchFxml);
            }
        });

        MenuItem pdfConvertImagesBatch = new MenuItem(AppVaribles.message("PdfConvertImagesBatch"));
        pdfConvertImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfConvertImagesBatchFxml);
            }
        });

        MenuItem imagesCombinePdf = new MenuItem(AppVaribles.message("ImagesCombinePdf"));
        imagesCombinePdf.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImagesCombinePdfFxml);
            }
        });

        MenuItem pdfCompressImagesBatch = new MenuItem(AppVaribles.message("PdfCompressImagesBatch"));
        pdfCompressImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfCompressImagesBatchFxml);
            }
        });

        MenuItem pdfMerge = new MenuItem(AppVaribles.message("MergePdf"));
        pdfMerge.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfMergeFxml);
            }
        });

        MenuItem PdfSplitBatch = new MenuItem(AppVaribles.message("PdfSplitBatch"));
        PdfSplitBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfSplitBatchFxml);
            }
        });

        pdfMenu = new ContextMenu();
        pdfMenu.setAutoHide(true);
        pdfMenu.getItems().addAll(
                pdfView, new SeparatorMenuItem(),
                PDFAttributes, PDFAttributesBatch, pdfConvertImagesBatch,
                pdfExtractImagesBatch, pdfExtractTextsBatch, pdfCompressImagesBatch, new SeparatorMenuItem(),
                PdfSplitBatch, pdfMerge, imagesCombinePdf
        );

    }

    private void initImageToolsMenu() {
        MenuItem imageViewer = new MenuItem(AppVaribles.message("ImageViewer"));
        imageViewer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageViewerFxml);
            }
        });

        MenuItem imagesBrowser = new MenuItem(AppVaribles.message("ImagesBrowser"));
        imagesBrowser.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImagesBrowserFxml);
            }
        });

        MenuItem ImageManufacture = new MenuItem(AppVaribles.message("ImageManufacture"));
        ImageManufacture.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureFileFxml);
            }
        });

        MenuItem imageConverterBatch = new MenuItem(AppVaribles.message("ImageConverterBatch"));
        imageConverterBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageConverterBatchFxml);
            }
        });

        MenuItem imageStatistic = new MenuItem(AppVaribles.message("ImageStatistic"));
        imageStatistic.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageStatisticFxml);
            }
        });

        MenuItem convolutionKernelManager = new MenuItem(AppVaribles.message("ConvolutionKernelManager"));
        convolutionKernelManager.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ConvolutionKernelManagerFxml);
            }
        });

        MenuItem pixelsCalculator = new MenuItem(AppVaribles.message("PixelsCalculator"));
        pixelsCalculator.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openStage(CommonValues.PixelsCalculatorFxml);
            }
        });

        MenuItem colorPalette = new MenuItem(AppVaribles.message("ColorPalette"));
        colorPalette.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openStage(CommonValues.ColorPaletteFxml);
            }
        });

        Menu manufactureSubMenu = initImageSubToolsMenu();
        Menu manufactureBatchMenu = initImageBatchToolsMenu();
        Menu framesMenu = initImageFramesMenu();
        Menu partMenu = initImagePartMenu();
        Menu mergeMenu = initImageMergeMenu();
        Menu csMenu = initColorSpaceMenu();

        imageMenu = new ContextMenu();
        imageMenu.setAutoHide(true);
        imageMenu.getItems().addAll(
                imageViewer, imagesBrowser, new SeparatorMenuItem(),
                ImageManufacture, manufactureSubMenu, manufactureBatchMenu, new SeparatorMenuItem(),
                imageConverterBatch, new SeparatorMenuItem(),
                framesMenu, mergeMenu, partMenu, new SeparatorMenuItem(),
                //                imageStatistic, new SeparatorMenuItem(),
                convolutionKernelManager, pixelsCalculator, colorPalette, csMenu);
    }

    private Menu initImageSubToolsMenu() {

        MenuItem imageSizeMenu = new MenuItem(AppVaribles.message("Size"));
        imageSizeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureSizeFxml);

            }
        });

        MenuItem imageCropMenu = new MenuItem(AppVaribles.message("Crop"));
        imageCropMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureCropFxml);

            }
        });

        MenuItem imageColorMenu = new MenuItem(AppVaribles.message("Color"));
        imageColorMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureColorFxml);

            }
        });

        MenuItem imageEffectsMenu = new MenuItem(AppVaribles.message("Effects"));
        imageEffectsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureEffectsFxml);
            }
        });

        MenuItem imageTextMenu = new MenuItem(AppVaribles.message("Text"));
        imageTextMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureTextFxml);

            }
        });

        MenuItem imageTransformMenu = new MenuItem(AppVaribles.message("Transform"));
        imageTransformMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureTransformFxml);

            }
        });

        MenuItem imageMarginsMenu = new MenuItem(AppVaribles.message("Margins"));
        imageMarginsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureMarginsFxml);

            }
        });

        MenuItem imageArcMenu = new MenuItem(AppVaribles.message("Arc"));
        imageArcMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureArcFxml);
            }
        });

        MenuItem imageShadowMenu = new MenuItem(AppVaribles.message("Shadow"));
        imageShadowMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureShadowFxml);
            }
        });

        MenuItem imageMosaicMenu = new MenuItem(AppVaribles.message("Mosaic"));
        imageMosaicMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureMosaicFxml);
            }
        });

        MenuItem imageDoodleMenu = new MenuItem(AppVaribles.message("Doodle"));
        imageDoodleMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureDoodleFxml);
            }
        });

        Menu manufactureSubMenu = new Menu(AppVaribles.message("ImageManufactureSub"));
        manufactureSubMenu.getItems().addAll(imageSizeMenu, imageCropMenu,
                imageColorMenu, imageEffectsMenu,
                imageTextMenu, imageDoodleMenu, imageMosaicMenu, imageArcMenu, imageShadowMenu,
                imageTransformMenu, imageMarginsMenu);
        return manufactureSubMenu;

    }

    private Menu initImageBatchToolsMenu() {

        MenuItem imageSizeMenu = new MenuItem(AppVaribles.message("Size"));
        imageSizeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchSizeFxml);
            }
        });

        MenuItem imageCropMenu = new MenuItem(AppVaribles.message("Crop"));
        imageCropMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchCropFxml);
            }
        });

        MenuItem imageColorMenu = new MenuItem(AppVaribles.message("Color"));
        imageColorMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchColorFxml);
            }
        });

        MenuItem imageEffectsMenu = new MenuItem(AppVaribles.message("Effects"));
        imageEffectsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchEffectsFxml);
            }
        });

        MenuItem imageReplaceColorMenu = new MenuItem(AppVaribles.message("ReplaceColor"));
        imageReplaceColorMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchReplaceColorFxml);
            }
        });

        MenuItem imageTextMenu = new MenuItem(AppVaribles.message("Text"));
        imageTextMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchTextFxml);
            }
        });

        MenuItem imageArcMenu = new MenuItem(AppVaribles.message("Arc"));
        imageArcMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchArcFxml);
            }
        });

        MenuItem imageShadowMenu = new MenuItem(AppVaribles.message("Shadow"));
        imageShadowMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchShadowFxml);
            }
        });

        MenuItem imageTransformMenu = new MenuItem(AppVaribles.message("Transform"));
        imageTransformMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchTransformFxml);
            }
        });

        MenuItem imageMarginsMenu = new MenuItem(AppVaribles.message("Margins"));
        imageMarginsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchMarginsFxml);
            }
        });

        Menu manufactureBatchMenu = new Menu(AppVaribles.message("ImageManufactureBatch"));
        manufactureBatchMenu.getItems().addAll(imageSizeMenu, imageCropMenu, imageColorMenu, imageEffectsMenu,
                imageReplaceColorMenu, imageTextMenu, imageArcMenu, imageShadowMenu, imageTransformMenu,
                imageMarginsMenu);
        return manufactureBatchMenu;

    }

    private Menu initImageFramesMenu() {

        MenuItem imageGifViewer = new MenuItem(AppVaribles.message("ImageGifViewer"));
        imageGifViewer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageGifViewerFxml);
            }
        });

        MenuItem imageGifEditer = new MenuItem(AppVaribles.message("ImageGifEditer"));
        imageGifEditer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageGifEditerFxml);
            }
        });

        MenuItem imageTiffEditer = new MenuItem(AppVaribles.message("ImageTiffEditer"));
        imageTiffEditer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageTiffEditerFxml);
            }
        });

        MenuItem imageFramesViewer = new MenuItem(AppVaribles.message("ImageFramesViewer"));
        imageFramesViewer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageFramesViewerFxml);
            }
        });

        Menu manufactureSubMenu = new Menu(AppVaribles.message("MultipleFramesImageFile"));
        manufactureSubMenu.getItems().addAll(imageFramesViewer, imageTiffEditer, imageGifViewer, imageGifEditer);
        return manufactureSubMenu;

    }

    private Menu initImagePartMenu() {

        MenuItem ImageSplit = new MenuItem(AppVaribles.message("ImageSplit"));
        ImageSplit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageSplitFxml);
            }
        });

        MenuItem ImageSample = new MenuItem(AppVaribles.message("ImageSubsample"));
        ImageSample.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageSampleFxml);
            }
        });

        MenuItem imageAlphaExtract = new MenuItem(AppVaribles.message("ImageAlphaExtract"));
        imageAlphaExtract.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageAlphaExtractBatchFxml);
            }
        });

        Menu manufactureSubMenu = new Menu(AppVaribles.message("ImagePart"));
        manufactureSubMenu.getItems().addAll(ImageSplit, ImageSample, imageAlphaExtract);
        return manufactureSubMenu;

    }

    private Menu initImageMergeMenu() {

        MenuItem ImageCombine = new MenuItem(AppVaribles.message("ImagesCombine"));
        ImageCombine.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImagesCombineFxml);
            }
        });

        MenuItem ImagesBlend = new MenuItem(AppVaribles.message("ImagesBlend"));
        ImagesBlend.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImagesBlendFxml);
            }
        });

        MenuItem imagesCombinePdf = new MenuItem(AppVaribles.message("ImagesCombinePdf"));
        imagesCombinePdf.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImagesCombinePdfFxml);
            }
        });

        MenuItem imageAlphaAdd = new MenuItem(AppVaribles.message("ImageAlphaAdd"));
        imageAlphaAdd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageAlphaAddBatchFxml);
            }
        });

        Menu manufactureSubMenu = new Menu(AppVaribles.message("MergeImages"));
        manufactureSubMenu.getItems().addAll(ImagesBlend, ImageCombine, imagesCombinePdf, imageAlphaAdd);
        return manufactureSubMenu;

    }

    private Menu initColorSpaceMenu() {
        MenuItem IccEditor = new MenuItem(AppVaribles.message("IccProfileEditor"));
        IccEditor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.IccProfileEditorFxml);
            }
        });

        MenuItem ChromaticityDiagram = new MenuItem(AppVaribles.message("DrawChromaticityDiagram"));
        ChromaticityDiagram.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ChromaticityDiagramFxml);
            }
        });

        MenuItem ChromaticAdaptationMatrix = new MenuItem(AppVaribles.message("ChromaticAdaptationMatrix"));
        ChromaticAdaptationMatrix.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ChromaticAdaptationMatrixFxml);
            }
        });

        MenuItem ColorPalette = new MenuItem(AppVaribles.message("ColorPalette"));
        ColorPalette.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openStage(CommonValues.ColorPaletteFxml);
            }
        });

        MenuItem ColorConversion = new MenuItem(AppVaribles.message("ColorConversion"));
        ColorConversion.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ColorConversionFxml);
            }
        });

        MenuItem RGBColorSpaces = new MenuItem(AppVaribles.message("RGBColorSpaces"));
        RGBColorSpaces.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.RGBColorSpacesFxml);
            }
        });

        MenuItem RGB2XYZConversionMatrix = new MenuItem(AppVaribles.message("LinearRGB2XYZMatrix"));
        RGB2XYZConversionMatrix.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.RGB2XYZConversionMatrixFxml);
            }
        });

        MenuItem RGB2RGBConversionMatrix = new MenuItem(AppVaribles.message("LinearRGB2RGBMatrix"));
        RGB2RGBConversionMatrix.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.RGB2RGBConversionMatrixFxml);
            }
        });

        MenuItem Illuminants = new MenuItem(AppVaribles.message("Illuminants"));
        Illuminants.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.IlluminantsFxml);
            }
        });

        Menu csMenu = new Menu(AppVaribles.message("ColorSpace"));
        csMenu.getItems().addAll(ChromaticityDiagram, IccEditor,
                //                ColorConversion,
                RGBColorSpaces, RGB2XYZConversionMatrix, RGB2RGBConversionMatrix,
                Illuminants, ChromaticAdaptationMatrix);
        return csMenu;

    }

    private void initDesktopToolsMenu() {
        MenuItem filesRename = new MenuItem(AppVaribles.message("FilesRename"));
        filesRename.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FilesRenameFxml);
            }
        });

        MenuItem dirSynchronize = new MenuItem(AppVaribles.message("DirectorySynchronize"));
        dirSynchronize.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.DirectorySynchronizeFxml);
            }
        });

        MenuItem filesArrangement = new MenuItem(AppVaribles.message("FilesArrangement"));
        filesArrangement.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FilesArrangementFxml);
            }
        });

        MenuItem textEditer = new MenuItem(AppVaribles.message("TextEditer"));
        textEditer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.TextEditerFxml);
            }
        });

        MenuItem textEncodingBatch = new MenuItem(AppVaribles.message("TextEncodingBatch"));
        textEncodingBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.TextEncodingBatchFxml);
            }
        });

        MenuItem textLineBreakBatch = new MenuItem(AppVaribles.message("TextLineBreakBatch"));
        textLineBreakBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.TextLineBreakBatchFxml);
            }
        });

        MenuItem bytesEditer = new MenuItem(AppVaribles.message("BytesEditer"));
        bytesEditer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.BytesEditerFxml);
            }
        });

        MenuItem fileCut = new MenuItem(AppVaribles.message("FileCut"));
        fileCut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FileCutFxml);
            }
        });

        MenuItem filesMerge = new MenuItem(AppVaribles.message("FilesMerge"));
        filesMerge.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FilesMergeFxml);
            }
        });

        MenuItem filesDelete = new MenuItem(AppVaribles.message("FilesDelete"));
        filesDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FilesDeleteFxml);
            }
        });

        MenuItem recordImages = new MenuItem(AppVaribles.message("RecordImagesInSystemClipBoard"));
        recordImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.RecordImagesInSystemClipboardFxml);
            }
        });

        MenuItem alarmClock = new MenuItem(AppVaribles.message("AlarmClock"));
        alarmClock.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.AlarmClockFxml);
            }
        });

        desktopMenu = new ContextMenu();
        desktopMenu.setAutoHide(true);
        desktopMenu.getItems().addAll(
                filesRename, filesArrangement, dirSynchronize, filesDelete, new SeparatorMenuItem(),
                recordImages, new SeparatorMenuItem(),
                textEditer, textEncodingBatch, textLineBreakBatch, new SeparatorMenuItem(),
                bytesEditer, fileCut, filesMerge, new SeparatorMenuItem(),
                alarmClock);
    }

    private void initFileToolsMenu() {
        fileMenu = new ContextMenu();

    }

    private void initNetworkToolsMenu() {
        MenuItem htmlEditor = new MenuItem(AppVaribles.message("HtmlEditor"));
        htmlEditor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                HtmlEditorController controller
                        = (HtmlEditorController) loadScene(CommonValues.HtmlEditorFxml);
//                controller.switchBroswerTab();
            }
        });

        MenuItem weiboSnap = new MenuItem(AppVaribles.message("WeiboSnap"));
        weiboSnap.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                WeiboSnapController controller
                        = (WeiboSnapController) loadScene(CommonValues.WeiboSnapFxml);
            }
        });

        networkMenu = new ContextMenu();
        networkMenu.setAutoHide(true);
        networkMenu.getItems().addAll(weiboSnap, htmlEditor);

    }

    private void initDataToolsMenu() {

        Menu csMenu = initColorSpaceMenu();

        MenuItem MatricesCalculation = new MenuItem(AppVaribles.message("MatricesCalculation"));
        MatricesCalculation.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.MatricesCalculationFxml);
            }
        });

        dataMenu = new ContextMenu();
        dataMenu.setAutoHide(true);
        dataMenu.getItems().addAll(csMenu,
                new SeparatorMenuItem(), MatricesCalculation);

    }

    private void initLanguageMenu() {
        MenuItem setEnglish = new MenuItem("English");
        setEnglish.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AppVaribles.setLanguage("en");
                loadScene(myFxml);
            }
        });
        MenuItem setChinese = new MenuItem("中文");
        setChinese.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AppVaribles.setLanguage("zh");
                loadScene(myFxml);
            }
        });
        languageMenu = new ContextMenu();
        languageMenu.setAutoHide(true);
        languageMenu.getItems().addAll(setChinese, setEnglish);

    }

    private void showMenu(Region box, ContextMenu menu, MouseEvent event) {
        if (menu == null || menu.isShowing()) {
            return;
        }
        hideMenus(event);
        FxmlControl.locateCenter(box, menu);
    }

    @FXML
    void showPdfMenu(MouseEvent event) {
        showMenu(pdfBox, pdfMenu, event);

    }

    @FXML
    void showImageMenu(MouseEvent event) {
        showMenu(imageBox, imageMenu, event);

    }

    @FXML
    void showFileMenu(MouseEvent event) {
        showMenu(fileBox, fileMenu, event);

    }

    @FXML
    void showNetworkMenu(MouseEvent event) {
        showMenu(networkBox, networkMenu, event);

    }

    @FXML
    void showDesktopMenu(MouseEvent event) {
        showMenu(desktopBox, desktopMenu, event);
    }

    @FXML
    void showLanguageMenu(MouseEvent event) {
        showMenu(langLabel, languageMenu, event);
    }

    @FXML
    void showRecentMenu(MouseEvent event) {
        hideMenus(event);
        recentMenu = popMenu = getRecentMenu();
        recentMenu.setAutoHide(true);
        showMenu(recentBox, recentMenu, event);
    }

    @FXML
    void showDataMenu(MouseEvent event) {
        showMenu(dataBox, dataMenu, event);
    }

    @FXML
    private void showAbout(MouseEvent event) {
        openStage(CommonValues.AboutFxml);
        hideMenus(event);
    }

    @FXML
    private void hideMenus(MouseEvent event) {
        imageMenu.hide();
        fileMenu.hide();
        desktopMenu.hide();
        pdfMenu.hide();
        networkMenu.hide();
        languageMenu.hide();
        dataMenu.hide();
        if (recentMenu != null) {
            recentMenu.hide();
        }
    }

}
