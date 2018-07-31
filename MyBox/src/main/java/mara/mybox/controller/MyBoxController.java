package mara.mybox.controller;

import java.text.MessageFormat;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import mara.mybox.objects.AlarmClock;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.scheduledTasks;
import mara.mybox.objects.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:48:15
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBoxController extends BaseController {

    private ContextMenu pdfMenus, imageMenu, fileMenu, desktopMenu, languageMenu;

    @FXML
    private VBox imageBox, pdfBox, fileBox, desktopBox, languageBox;

    @Override
    protected void initializeNext() {

        try {

            MenuItem pdfExtractImages = new MenuItem(AppVaribles.getMessage("PdfExtractImages"));
            pdfExtractImages.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.PdfExtractImagesFxml, AppVaribles.getMessage("PdfExtractImages"));
                }
            });
            MenuItem pdfExtractImagesBatch = new MenuItem(AppVaribles.getMessage("PdfExtractImagesBatch"));
            pdfExtractImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.PdfExtractImagesBatchFxml, AppVaribles.getMessage("PdfExtractImagesBatch"));
                }
            });
            MenuItem pdfExtractTexts = new MenuItem(AppVaribles.getMessage("PdfExtractTexts"));
            pdfExtractTexts.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.PdfExtractTextsFxml, AppVaribles.getMessage("pdfExtractTexts"));
                }
            });
            MenuItem pdfExtractTextsBatch = new MenuItem(AppVaribles.getMessage("PdfExtractTextsBatch"));
            pdfExtractTextsBatch.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.PdfExtractTextsBatchFxml, AppVaribles.getMessage("PdfExtractTextsBatch"));
                }
            });
            MenuItem pdfConvertImages = new MenuItem(AppVaribles.getMessage("PdfConvertImages"));
            pdfConvertImages.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.PdfConvertImagesFxml, AppVaribles.getMessage("PdfConvertImages"));
                }
            });
            MenuItem pdfConvertImagesBatch = new MenuItem(AppVaribles.getMessage("PdfConvertImagesBatch"));
            pdfConvertImagesBatch.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.PdfConvertImagesBatchFxml, AppVaribles.getMessage("PdfConvertImagesBatch"));
                }
            });
            pdfMenus = new ContextMenu();
            pdfMenus.getItems().addAll(pdfExtractImages, pdfExtractTexts, pdfConvertImages, new SeparatorMenuItem(),
                    pdfExtractImagesBatch, pdfExtractTextsBatch, pdfConvertImagesBatch);

            MenuItem imageViewer = new MenuItem(AppVaribles.getMessage("ImageViewer"));
            imageViewer.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.ImageViewerFxml, AppVaribles.getMessage("ImageViewer"));
                }
            });
            MenuItem imagesViewer = new MenuItem(AppVaribles.getMessage("MultipleImagesViewer"));
            imagesViewer.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.ImagesViewerFxml, AppVaribles.getMessage("MultipleImagesViewer"));
                }
            });
            MenuItem ImageManufacture = new MenuItem(AppVaribles.getMessage("ImageManufacture"));
            ImageManufacture.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.ImageManufactureFxml, AppVaribles.getMessage("ImageManufacture"));
                }
            });
            MenuItem imageConverter = new MenuItem(AppVaribles.getMessage("ImageConverter"));
            imageConverter.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.ImageConverterFxml, AppVaribles.getMessage("ImageConverter"));
                }
            });
            MenuItem imageConverterBatch = new MenuItem(AppVaribles.getMessage("ImageConverterBatch"));
            imageConverterBatch.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.ImageConverterBatchFxml, AppVaribles.getMessage("ImageConverterBatch"));
                }
            });
            MenuItem pixelsCalculator = new MenuItem(AppVaribles.getMessage("PixelsCalculator"));
            pixelsCalculator.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openStage(CommonValues.PixelsCalculatorFxml, AppVaribles.getMessage("PixelsCalculator"), false, false);
                }
            });
            MenuItem colorPalette = new MenuItem(AppVaribles.getMessage("ColorPalette"));
            colorPalette.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    openStage(CommonValues.ColorPaletteFxml, AppVaribles.getMessage("ColorPalette"), false, false);
                }
            });
            imageMenu = new ContextMenu();
            imageMenu.getItems().addAll(ImageManufacture, imagesViewer, new SeparatorMenuItem(),
                    imageConverter, imageConverterBatch, new SeparatorMenuItem(),
                    colorPalette, pixelsCalculator);

            MenuItem filesRename = new MenuItem(AppVaribles.getMessage("FilesRename"));
            filesRename.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.FilesRenameFxml, AppVaribles.getMessage("FilesRename"));
                }
            });
            MenuItem dirsRename = new MenuItem(AppVaribles.getMessage("DirectoriesRename"));
            dirsRename.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.DirectoriesRenameFxml, AppVaribles.getMessage("DirectoriesRename"));
                }
            });
            MenuItem dirSynchronize = new MenuItem(AppVaribles.getMessage("DirectorySynchronize"));
            dirSynchronize.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.DirectorySynchronizeFxml, AppVaribles.getMessage("DirectorySynchronize"));
                }
            });
            MenuItem filesArrangement = new MenuItem(AppVaribles.getMessage("FilesArrangement"));
            filesArrangement.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.FilesArrangementFxml, AppVaribles.getMessage("FilesArrangement"));
                }
            });
            MenuItem htmlEditor = new MenuItem(AppVaribles.getMessage("HtmlEditor"));
            htmlEditor.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.HtmlEditorFxml, AppVaribles.getMessage("HtmlEditor"));
                }
            });
            MenuItem textEditor = new MenuItem(AppVaribles.getMessage("TextEditor"));
            textEditor.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.TextEditorFxml, AppVaribles.getMessage("TextEditor"));
                }
            });
            fileMenu = new ContextMenu();
            fileMenu.getItems().addAll(htmlEditor, textEditor, new SeparatorMenuItem(),
                    filesRename, filesArrangement, new SeparatorMenuItem(),
                    dirSynchronize, dirsRename);

            MenuItem setEnglish = new MenuItem("English");
            setEnglish.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    AppVaribles.setCurrentBundle("en");
                    reloadStage(myFxml, AppVaribles.getMessage("AppTitle"));
                }
            });
            MenuItem setChinese = new MenuItem("中文");
            setChinese.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    AppVaribles.setCurrentBundle("zh");
                    reloadStage(myFxml, AppVaribles.getMessage("AppTitle"));
                }
            });
            languageMenu = new ContextMenu();
            languageMenu.getItems().addAll(setChinese, setEnglish);

            MenuItem alarmClock = new MenuItem(AppVaribles.getMessage("AlarmClock"));
            alarmClock.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    reloadStage(CommonValues.AlarmClockFxml, AppVaribles.getMessage("AlarmClock"));
                }
            });
            desktopMenu = new ContextMenu();
            desktopMenu.getItems().addAll(alarmClock);

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

    @FXML
    void showPdfMenu(MouseEvent event) {
        if (pdfMenus.isShowing()) {
            return;
        }
        Bounds bounds = pdfBox.localToScreen(pdfBox.getBoundsInLocal());
        pdfMenus.show(pdfBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
        imageMenu.hide();
        fileMenu.hide();
        desktopMenu.hide();
        languageMenu.hide();
    }

    @FXML
    void showImageMenu(MouseEvent event) {
        if (imageMenu.isShowing()) {
            return;
        }
        Bounds bounds = imageBox.localToScreen(imageBox.getBoundsInLocal());
        imageMenu.show(imageBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
        pdfMenus.hide();
        fileMenu.hide();
        desktopMenu.hide();
        languageMenu.hide();

    }

    @FXML
    void showFileMenu(MouseEvent event) {
        if (fileMenu.isShowing()) {
            return;
        }
        Bounds bounds = fileBox.localToScreen(fileBox.getBoundsInLocal());
        fileMenu.show(fileBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
        imageMenu.hide();
        pdfMenus.hide();
        desktopMenu.hide();
        languageMenu.hide();
    }

    @FXML
    void showDesktopMenu(MouseEvent event) {
        if (desktopMenu.isShowing()) {
            return;
        }
        Bounds bounds = desktopBox.localToScreen(desktopBox.getBoundsInLocal());
        desktopMenu.show(desktopBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
        imageMenu.hide();
        fileMenu.hide();
        pdfMenus.hide();
        languageMenu.hide();
    }

    @FXML
    void showLanguageMenu(MouseEvent event) {
        if (languageMenu.isShowing()) {
            return;
        }
        Bounds bounds = languageBox.localToScreen(languageBox.getBoundsInLocal());
        languageMenu.show(languageBox, bounds.getMinX() + bounds.getWidth() / 2, bounds.getMinY() + bounds.getHeight() / 2);
        imageMenu.hide();
        fileMenu.hide();
        desktopMenu.hide();
        pdfMenus.hide();
    }

    @FXML
    private void showAbout(MouseEvent event) {
        openStage(CommonValues.AboutFxml, true);
        imageMenu.hide();
        fileMenu.hide();
        desktopMenu.hide();
        languageMenu.hide();
        pdfMenus.hide();
    }

}
