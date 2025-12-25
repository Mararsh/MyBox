package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.data.PdfInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.menu.MenuTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public class PdfViewController extends PdfViewController_Html {

    public PdfViewController() {
        baseTitle = message("PdfView");
        TipsLabelKey = "PdfViewTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            infoLoaded = new SimpleBooleanProperty(false);
            viewBox.getChildren().clear();
            VBox.setVgrow(viewBox, Priority.ALWAYS);

            formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    showPage();
                }
            });

            viewBookmarkCheck.setSelected(UserConfig.getBoolean(baseName + "Bookmarks", true));
            viewBookmarkCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Bookmarks", viewBookmarkCheck.isSelected());
                    loadBookmarks();
                }
            });

            transparentBackgroundCheck.setSelected(UserConfig.getBoolean(baseName + "Transparent", false));
            transparentBackgroundCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Transparent", transparentBackgroundCheck.isSelected());
                    loadPage();
                }
            });

            leftPane.disableProperty().bind(imageController.imageView.imageProperty().isNull());
            showRightPane();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void sourceFileChanged(File file) {
        if (file == null) {
            return;
        }
        loadFile(file, null, 0);
    }

    public void loadFile(File file, PdfInformation pdfInfo, int page) {
        try {
            initPage(file, page);
            if (task != null) {
                task.cancel();
                task = null;
            }
            if (bookmarksTask != null) {
                bookmarksTask.cancel();
                bookmarksTask = null;
            }
            infoLoaded.set(false);
            bookmarksTree.setRoot(null);
            ocrArea.clear();
            ocrLabel.setText("");
            textsArea.clear();
            textsLabel.setText("");
            htmlController.loadContent(null);
            htmlFile = null;
            if (file == null) {
                return;
            }
            if (pdfInfo != null) {
                pdfInformation = pdfInfo;
                checkCurrentPage();
            } else {
                pdfInformation = new PdfInformation(sourceFile);
                loadInformation();
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void setSourceFile(File file) {
        super.setSourceFile(file);
        orcPage = -1;
        textsPage = -1;
        htmlPage = -1;
    }

    public void loadInformation() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (pdfInformation == null) {
            if (sourceFile == null) {
                return;
            }
            pdfInformation = new PdfInformation(sourceFile);
        }
        bottomLabel.setText("");
        isSettingValues = true;
        pageSelector.getItems().clear();
        isSettingValues = false;
        pageLabel.setText("");
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                setTotalPages(0);
                if (!PdfInformation.readPDF(this, pdfInformation)) {
                    error = pdfInformation.getError();
                    return false;
                }
                password = pdfInformation.getUserPassword();
                infoLoaded.set(true);
                setTotalPages(pdfInformation.getNumberOfPages());
                return framesNumber > 0;
            }

            @Override
            protected void whenSucceeded() {
                List<String> pages = new ArrayList<>();
                for (int i = 1; i <= framesNumber; i++) {
                    pages.add(i + "");
                }
                isSettingValues = true;
                pageSelector.getItems().clear();
                pageSelector.getItems().setAll(pages);
                pageLabel.setText("/" + framesNumber);
                isSettingValues = false;
                initCurrentPage();
                loadPage();
                loadBookmarks();
                refreshThumbs();
            }

        };
        start(task);
    }

    @Override
    protected Image readPageImage() {
        try (PDDocument doc = Loader.loadPDF(sourceFile, password)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage bufferedImage = renderer.renderImageWithDPI(frameIndex, dpi,
                    transparentBackgroundCheck.isSelected() ? ImageType.ARGB : ImageType.RGB);
            doc.close();
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (Exception e) {
            if (task != null) {
                task.setInfo(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void setImage(Image image, int percent) {
        if (imageView == null) {
            return;
        }
        super.setImage(image, percent);
        showPage();
    }

    public void showPage() {
        try {
            viewBox.getChildren().clear();
            if (ocrRadio.isSelected()) {
                viewBox.getChildren().add(ocrBox);
                VBox.setVgrow(ocrBox, Priority.ALWAYS);
                startOCR(false);

            } else if (textsRadio.isSelected()) {
                viewBox.getChildren().add(textsBox);
                VBox.setVgrow(textsBox, Priority.ALWAYS);
                extractTexts(false);

            } else if (htmlRadio.isSelected()) {
                viewBox.getChildren().add(htmlBox);
                VBox.setVgrow(htmlBox, Priority.ALWAYS);
                convertHtml(false);

            } else {
                viewBox.getChildren().add(imageBox);
                VBox.setVgrow(imageBox, Priority.ALWAYS);

            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public boolean infoAction() {
        if (pdfInformation == null) {
            return false;
        }
        try {
            PdfInformationController controller = (PdfInformationController) openStage(Fxmls.PdfInformationFxml);
            controller.setInformation(pdfInformation);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void cleanPane() {
        try {
            if (webEngine != null && webEngine.getLoadWorker() != null) {
                webEngine.getLoadWorker().cancel();
            }
            webEngine = null;
            webView.setUserData(null);
            if (bookmarksTask != null) {
                bookmarksTask.cancel();
                bookmarksTask = null;
            }
            if (htmlTask != null) {
                htmlTask.cancel();
                htmlTask = null;
            }
            if (textsTask != null) {
                textsTask.cancel();
                textsTask = null;
            }
            if (ocrTask != null) {
                ocrTask.cancel();
                ocrTask = null;
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    @Override
    public List<MenuItem> viewMenuItems(Event fevent) {
        try {
            List<MenuItem> items = MenuTools.initMenu(message("View"));

            MenuItem menu = new MenuItem(message("Image") + " - " + message("Pop"), StyleTools.getIconImageView("iconImage.png"));
            menu.setOnAction((ActionEvent event) -> {
                ImagePopController.openImage(myController, imageView.getImage());
            });
            items.add(menu);

            menu = new MenuItem(message("PageDataInHtml") + " - " + message("Pop"), StyleTools.getIconImageView("iconHtml.png"));
            menu.setOnAction((ActionEvent event) -> {
                if (htmlRadio.isSelected()) {
                    HtmlPopController.openFile(myController, htmlFile);
                } else {
                    convertHtml(true);
                }
            });
            items.add(menu);

            menu = new MenuItem(message("PageDataInText") + " - " + message("Pop"), StyleTools.getIconImageView("iconTxt.png"));
            menu.setOnAction((ActionEvent event) -> {
                if (textsRadio.isSelected()) {
                    TextPopController.loadText(textsArea.getText());
                } else {
                    extractTexts(true);
                }
            });
            items.add(menu);

            menu = new MenuItem(message("OCR") + " - " + message("Pop"), StyleTools.getIconImageView("iconOCR.png"));
            menu.setOnAction((ActionEvent event) -> {
                if (ocrRadio.isSelected()) {
                    TextPopController.loadText(ocrArea.getText());
                } else {
                    startOCR(true);
                }
            });
            items.add(menu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        static
     */
    public static PdfViewController open(File file) {
        try {
            PdfViewController controller = (PdfViewController) WindowTools.openStage(Fxmls.PdfViewFxml);
            if (controller != null) {
                controller.requestMouse();
                if (file != null) {
                    controller.sourceFileChanged(file);
                }
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
