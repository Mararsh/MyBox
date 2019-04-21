package mara.mybox.controller;

import mara.mybox.controller.base.BaseController;
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
import mara.mybox.data.AlarmClock;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.scheduledTasks;
import mara.mybox.value.CommonValues;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:48:15
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBoxController extends BaseController {

    private ContextMenu pdfMenu, imageMenu, fileMenu, desktopMenu, languageMenu,
            networkMenu, recentMenu;

    @FXML
    private VBox imageBox, pdfBox, fileBox, desktopBox, recentBox, networkBox;
    @FXML
    private Label langLabel;

    public MyBoxController() {
        baseTitle = AppVaribles.getMessage("AppTitle");

    }

    @Override
    public void initializeNext() {
        try {
            initPdfToolsMenu();
            initImageToolsMenu();
            initDesktopToolsMenu();
            initFileToolsMenu();
            initNetworkToolsMenu();
            initOtherMenu();

            List<AlarmClock> alarms = AlarmClock.readAlarmClocks();
            if (alarms != null) {
                for (AlarmClock alarm : alarms) {
                    if (alarm.isIsActive()) {
                        AlarmClock.scehduleAlarmClock(alarm);
                    }
                }
                if (scheduledTasks != null && scheduledTasks.size() > 0) {
                    bottomLabel.setText(MessageFormat.format(AppVaribles.getMessage("AlarmClocksRunning"), scheduledTasks.size()));
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    private void initPdfToolsMenu() {
        MenuItem pdfView = new MenuItem(AppVaribles.getMessage("PdfView"));
        pdfView.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfViewFxml);
            }
        });

        MenuItem pdfExtractImages = new MenuItem(AppVaribles.getMessage("PdfExtractImages"));
        pdfExtractImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfExtractImagesFxml);
            }
        });

        MenuItem pdfExtractImagesBatch = new MenuItem(AppVaribles.getMessage("PdfExtractImagesBatch"));
        pdfExtractImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfExtractImagesBatchFxml);
            }
        });

        MenuItem pdfExtractTexts = new MenuItem(AppVaribles.getMessage("PdfExtractTexts"));
        pdfExtractTexts.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfExtractTextsFxml);
            }
        });

        MenuItem pdfExtractTextsBatch = new MenuItem(AppVaribles.getMessage("PdfExtractTextsBatch"));
        pdfExtractTextsBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfExtractTextsBatchFxml);
            }
        });

        MenuItem pdfConvertImages = new MenuItem(AppVaribles.getMessage("PdfConvertImages"));
        pdfConvertImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfConvertImagesFxml);
            }
        });

        MenuItem pdfConvertImagesBatch = new MenuItem(AppVaribles.getMessage("PdfConvertImagesBatch"));
        pdfConvertImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfConvertImagesBatchFxml);
            }
        });

        MenuItem imagesCombinePdf = new MenuItem(AppVaribles.getMessage("ImagesCombinePdf"));
        imagesCombinePdf.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImagesCombinePdfFxml);
            }
        });

        MenuItem pdfCompressImages = new MenuItem(AppVaribles.getMessage("PdfCompressImages"));
        pdfCompressImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfCompressImagesFxml);
            }
        });

        MenuItem pdfCompressImagesBatch = new MenuItem(AppVaribles.getMessage("PdfCompressImagesBatch"));
        pdfCompressImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfCompressImagesBatchFxml);
            }
        });

        MenuItem pdfMerge = new MenuItem(AppVaribles.getMessage("MergePdf"));
        pdfMerge.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfMergeFxml);
            }
        });

        MenuItem pdfSplit = new MenuItem(AppVaribles.getMessage("SplitPdf"));
        pdfSplit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.PdfSplitFxml);
            }
        });

        pdfMenu = new ContextMenu();
        pdfMenu.setAutoHide(true);
        pdfMenu.getItems().addAll(
                pdfView, new SeparatorMenuItem(),
                pdfConvertImages, pdfConvertImagesBatch, new SeparatorMenuItem(),
                pdfExtractImages, pdfExtractImagesBatch, pdfExtractTexts, pdfExtractTextsBatch, new SeparatorMenuItem(),
                imagesCombinePdf, new SeparatorMenuItem(),
                pdfMerge, pdfSplit, new SeparatorMenuItem(),
                pdfCompressImages, pdfCompressImagesBatch
        );

    }

    private void initImageToolsMenu() {
        MenuItem imageViewer = new MenuItem(AppVaribles.getMessage("ImageViewer"));
        imageViewer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageViewerFxml);
            }
        });

        MenuItem imagesBrowser = new MenuItem(AppVaribles.getMessage("ImagesBrowser"));
        imagesBrowser.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImagesBrowserFxml);
            }
        });

        MenuItem ImageManufacture = new MenuItem(AppVaribles.getMessage("ImageManufacture"));
        ImageManufacture.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureFileFxml);
            }
        });

        MenuItem imageConverter = new MenuItem(AppVaribles.getMessage("ImageConverter"));
        imageConverter.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageConverterFxml);
            }
        });

        MenuItem imageConverterBatch = new MenuItem(AppVaribles.getMessage("ImageConverterBatch"));
        imageConverterBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageConverterBatchFxml);
            }
        });

        MenuItem imageStatistic = new MenuItem(AppVaribles.getMessage("ImageStatistic"));
        imageStatistic.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageStatisticFxml);
            }
        });

        MenuItem convolutionKernelManager = new MenuItem(AppVaribles.getMessage("ConvolutionKernelManager"));
        convolutionKernelManager.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ConvolutionKernelManagerFxml);
            }
        });

        MenuItem pixelsCalculator = new MenuItem(AppVaribles.getMessage("PixelsCalculator"));
        pixelsCalculator.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openStage(CommonValues.PixelsCalculatorFxml);
            }
        });

        MenuItem colorPalette = new MenuItem(AppVaribles.getMessage("ColorPalette"));
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

        imageMenu = new ContextMenu();
        imageMenu.setAutoHide(true);
        imageMenu.getItems().addAll(
                imageViewer, imagesBrowser, new SeparatorMenuItem(),
                ImageManufacture, manufactureSubMenu, manufactureBatchMenu, new SeparatorMenuItem(),
                framesMenu, new SeparatorMenuItem(), mergeMenu, new SeparatorMenuItem(), partMenu, new SeparatorMenuItem(),
                imageConverter, imageConverterBatch, new SeparatorMenuItem(),
                //                imageStatistic, new SeparatorMenuItem(),
                convolutionKernelManager, colorPalette, pixelsCalculator);
    }

    private Menu initImageSubToolsMenu() {

        MenuItem imageSizeMenu = new MenuItem(AppVaribles.getMessage("Size"));
        imageSizeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureSizeFxml);

            }
        });

        MenuItem imageCropMenu = new MenuItem(AppVaribles.getMessage("Crop"));
        imageCropMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureCropFxml);

            }
        });

        MenuItem imageColorMenu = new MenuItem(AppVaribles.getMessage("Color"));
        imageColorMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureColorFxml);

            }
        });

        MenuItem imageEffectsMenu = new MenuItem(AppVaribles.getMessage("Effects"));
        imageEffectsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureEffectsFxml);
            }
        });

        MenuItem imageTextMenu = new MenuItem(AppVaribles.getMessage("Text"));
        imageTextMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureTextFxml);

            }
        });

        MenuItem imageTransformMenu = new MenuItem(AppVaribles.getMessage("Transform"));
        imageTransformMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureTransformFxml);

            }
        });

        MenuItem imageMarginsMenu = new MenuItem(AppVaribles.getMessage("Margins"));
        imageMarginsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureMarginsFxml);

            }
        });

        MenuItem imageArcMenu = new MenuItem(AppVaribles.getMessage("Arc"));
        imageArcMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureArcFxml);
            }
        });

        MenuItem imageShadowMenu = new MenuItem(AppVaribles.getMessage("Shadow"));
        imageShadowMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureShadowFxml);
            }
        });

        MenuItem imageMosaicMenu = new MenuItem(AppVaribles.getMessage("Mosaic"));
        imageMosaicMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureMosaicFxml);
            }
        });

        MenuItem imageDoodleMenu = new MenuItem(AppVaribles.getMessage("Doodle"));
        imageDoodleMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureDoodleFxml);
            }
        });

        Menu manufactureSubMenu = new Menu(AppVaribles.getMessage("ImageManufactureSub"));
        manufactureSubMenu.getItems().addAll(imageSizeMenu, imageCropMenu,
                imageColorMenu, imageEffectsMenu,
                imageTextMenu, imageDoodleMenu, imageMosaicMenu, imageArcMenu, imageShadowMenu,
                imageTransformMenu, imageMarginsMenu);
        return manufactureSubMenu;

    }

    private Menu initImageBatchToolsMenu() {

        MenuItem imageSizeMenu = new MenuItem(AppVaribles.getMessage("Size"));
        imageSizeMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchSizeFxml);
            }
        });

        MenuItem imageCropMenu = new MenuItem(AppVaribles.getMessage("Crop"));
        imageCropMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchCropFxml);
            }
        });

        MenuItem imageColorMenu = new MenuItem(AppVaribles.getMessage("Color"));
        imageColorMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchColorFxml);
            }
        });

        MenuItem imageEffectsMenu = new MenuItem(AppVaribles.getMessage("Effects"));
        imageEffectsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchEffectsFxml);
            }
        });

        MenuItem imageReplaceColorMenu = new MenuItem(AppVaribles.getMessage("ReplaceColor"));
        imageReplaceColorMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchReplaceColorFxml);
            }
        });

        MenuItem imageTextMenu = new MenuItem(AppVaribles.getMessage("Text"));
        imageTextMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchTextFxml);
            }
        });

        MenuItem imageArcMenu = new MenuItem(AppVaribles.getMessage("Arc"));
        imageArcMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchArcFxml);
            }
        });

        MenuItem imageShadowMenu = new MenuItem(AppVaribles.getMessage("Shadow"));
        imageShadowMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchShadowFxml);
            }
        });

        MenuItem imageTransformMenu = new MenuItem(AppVaribles.getMessage("Transform"));
        imageTransformMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchTransformFxml);
            }
        });

        MenuItem imageMarginsMenu = new MenuItem(AppVaribles.getMessage("Margins"));
        imageMarginsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageManufactureBatchMarginsFxml);
            }
        });

        Menu manufactureBatchMenu = new Menu(AppVaribles.getMessage("ImageManufactureBatch"));
        manufactureBatchMenu.getItems().addAll(imageSizeMenu, imageCropMenu, imageColorMenu, imageEffectsMenu,
                imageReplaceColorMenu, imageTextMenu, imageArcMenu, imageShadowMenu, imageTransformMenu,
                imageMarginsMenu);
        return manufactureBatchMenu;

    }

    private Menu initImageFramesMenu() {

        MenuItem imageGifViewer = new MenuItem(AppVaribles.getMessage("ImageGifViewer"));
        imageGifViewer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageGifViewerFxml);
            }
        });

        MenuItem imageGifEditer = new MenuItem(AppVaribles.getMessage("ImageGifEditer"));
        imageGifEditer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageGifEditerFxml);
            }
        });

        MenuItem imageTiffEditer = new MenuItem(AppVaribles.getMessage("ImageTiffEditer"));
        imageTiffEditer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageTiffEditerFxml);
            }
        });

        MenuItem imageFramesViewer = new MenuItem(AppVaribles.getMessage("ImageFramesViewer"));
        imageFramesViewer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageFramesViewerFxml);
            }
        });

        Menu manufactureSubMenu = new Menu(AppVaribles.getMessage("MultipleFramesImageFile"));
        manufactureSubMenu.getItems().addAll(imageFramesViewer, imageTiffEditer, imageGifViewer, imageGifEditer);
        return manufactureSubMenu;

    }

    private Menu initImagePartMenu() {

        MenuItem ImageSplit = new MenuItem(AppVaribles.getMessage("ImageSplit"));
        ImageSplit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageSplitFxml);
            }
        });

        MenuItem ImageSample = new MenuItem(AppVaribles.getMessage("ImageSubsample"));
        ImageSample.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImageSampleFxml);
            }
        });

        Menu manufactureSubMenu = new Menu(AppVaribles.getMessage("ImagePart"));
        manufactureSubMenu.getItems().addAll(ImageSplit, ImageSample);
        return manufactureSubMenu;

    }

    private Menu initImageMergeMenu() {

        MenuItem ImageCombine = new MenuItem(AppVaribles.getMessage("ImageCombine"));
        ImageCombine.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImagesCombineFxml);
            }
        });

        MenuItem ImagesBlend = new MenuItem(AppVaribles.getMessage("ImagesBlend"));
        ImagesBlend.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImagesBlendFxml);
            }
        });

        MenuItem imagesCombinePdf = new MenuItem(AppVaribles.getMessage("ImagesCombinePdf"));
        imagesCombinePdf.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.ImagesCombinePdfFxml);
            }
        });

        Menu manufactureSubMenu = new Menu(AppVaribles.getMessage("MergeImages"));
        manufactureSubMenu.getItems().addAll(ImagesBlend, ImageCombine, imagesCombinePdf);
        return manufactureSubMenu;

    }

    private void initDesktopToolsMenu() {
        MenuItem filesRename = new MenuItem(AppVaribles.getMessage("FilesRename"));
        filesRename.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FilesRenameFxml);
            }
        });

        MenuItem dirSynchronize = new MenuItem(AppVaribles.getMessage("DirectorySynchronize"));
        dirSynchronize.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.DirectorySynchronizeFxml);
            }
        });

        MenuItem filesArrangement = new MenuItem(AppVaribles.getMessage("FilesArrangement"));
        filesArrangement.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FilesArrangementFxml);
            }
        });

        MenuItem textEditer = new MenuItem(AppVaribles.getMessage("TextEditer"));
        textEditer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.TextEditerFxml);
            }
        });

        MenuItem textEncodingBatch = new MenuItem(AppVaribles.getMessage("TextEncodingBatch"));
        textEncodingBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.TextEncodingBatchFxml);
            }
        });

        MenuItem textLineBreakBatch = new MenuItem(AppVaribles.getMessage("TextLineBreakBatch"));
        textLineBreakBatch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.TextLineBreakBatchFxml);
            }
        });

        MenuItem bytesEditer = new MenuItem(AppVaribles.getMessage("BytesEditer"));
        bytesEditer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.BytesEditerFxml);
            }
        });

        MenuItem fileCut = new MenuItem(AppVaribles.getMessage("FileCut"));
        fileCut.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FileCutFxml);
            }
        });

        MenuItem fileMerge = new MenuItem(AppVaribles.getMessage("FileMerge"));
        fileMerge.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.FileMergeFxml);
            }
        });

        MenuItem recordImages = new MenuItem(AppVaribles.getMessage("RecordImagesInSystemClipBoard"));
        recordImages.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.RecordImagesInSystemClipboardFxml);
            }
        });

        MenuItem alarmClock = new MenuItem(AppVaribles.getMessage("AlarmClock"));
        alarmClock.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadScene(CommonValues.AlarmClockFxml);
            }
        });

        desktopMenu = new ContextMenu();
        desktopMenu.setAutoHide(true);
        desktopMenu.getItems().addAll(
                textEditer, textEncodingBatch, textLineBreakBatch, new SeparatorMenuItem(),
                bytesEditer, fileCut, fileMerge, new SeparatorMenuItem(),
                filesRename, filesArrangement, dirSynchronize, new SeparatorMenuItem(),
                recordImages, alarmClock);
    }

    private void initFileToolsMenu() {
        fileMenu = new ContextMenu();

    }

    private void initNetworkToolsMenu() {
        MenuItem htmlEditor = new MenuItem(AppVaribles.getMessage("HtmlEditor"));
        htmlEditor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                HtmlEditorController controller
                        = (HtmlEditorController) loadScene(CommonValues.HtmlEditorFxml);
//                controller.switchBroswerTab();
            }
        });

        MenuItem weiboSnap = new MenuItem(AppVaribles.getMessage("WeiboSnap"));
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

    private void initOtherMenu() {
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
        recentMenu = popMenu = getRecentMenu(true);
        recentMenu.setAutoHide(true);
        showMenu(recentBox, recentMenu, event);
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
        if (recentMenu != null) {
            recentMenu.hide();
        }
    }

}
