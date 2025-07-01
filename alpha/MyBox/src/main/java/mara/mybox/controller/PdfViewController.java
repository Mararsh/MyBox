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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import mara.mybox.data.PdfInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.image.tools.ScaleTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @License Apache License Version 2.0
 */
public class PdfViewController extends PdfViewController_Html {

    protected SimpleBooleanProperty infoLoaded;
    protected FxTask bookmarksTask;

    @FXML
    protected ToggleGroup formatGroup;
    @FXML
    protected RadioButton imageRadio, textsRadio, htmlRadio, ocrRadio;
    @FXML
    protected CheckBox transparentBackgroundCheck, viewBookmarkCheck;
    @FXML
    protected ScrollPane bookmarksScrollPane;
    @FXML
    protected TreeView bookmarksTree;
    @FXML
    protected VBox leftBox, viewBox, imageBox, textsBox, htmlBox, ocrBox;

    public PdfViewController() {
        baseTitle = message("PdfView");
        TipsLabelKey = "PdfViewTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PDF, VisitHistory.FileType.Image);
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
            if (webViewController != null) {
                webViewController.loadContent(null);
            }
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
                loadThumbs();
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
                startOCR();

            } else if (textsRadio.isSelected()) {
                viewBox.getChildren().add(textsBox);
                VBox.setVgrow(textsBox, Priority.ALWAYS);
                extractTexts();

            } else if (htmlRadio.isSelected()) {
                viewBox.getChildren().add(htmlBox);
                VBox.setVgrow(htmlBox, Priority.ALWAYS);
                convertHtml();

            } else {
                viewBox.getChildren().add(imageBox);
                VBox.setVgrow(imageBox, Priority.ALWAYS);

            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void loadBookmarks() {
        if (bookmarksTask != null) {
            bookmarksTask.cancel();
        }
        if (!viewBookmarkCheck.isSelected() || !infoLoaded.get()) {
            return;
        }
        bookmarksTree.setRoot(null);
        bookmarksTask = new FxTask<Void>(this) {
            private TreeItem outlineRoot;

            @Override
            protected boolean handle() {
                try (PDDocument doc = Loader.loadPDF(sourceFile, password)) {
                    PDDocumentOutline outline = doc.getDocumentCatalog().getDocumentOutline();
                    if (outline != null) {
                        outlineRoot = new TreeItem<>(message("Bookmarks"));
                        outlineRoot.setExpanded(true);
                        ok = loadOutlineItem(outline, outlineRoot);
                    }
                    doc.close();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return ok;
            }

            @Override
            protected void whenSucceeded() {
                bookmarksTree.setRoot(outlineRoot);
                bookmarksScrollPane.applyCss();
                bookmarksScrollPane.layout();
            }

            @Override
            protected void whenFailed() {
            }

        };
        start(bookmarksTask, false);
    }

    protected boolean loadOutlineItem(PDOutlineNode parentOutlineItem, TreeItem parentTreeItem) {
        try {
            PDOutlineItem childOutlineItem = parentOutlineItem.getFirstChild();
            while (childOutlineItem != null) {
                if (bookmarksTask == null || bookmarksTask.isCancelled()) {
                    return false;
                }
                int pageNumber = 0;
                if (childOutlineItem.getDestination() instanceof PDPageDestination) {
                    PDPageDestination pd = (PDPageDestination) childOutlineItem.getDestination();
                    pageNumber = pd.retrievePageNumber();
                } else if (childOutlineItem.getAction() instanceof PDActionGoTo) {
                    PDActionGoTo gta = (PDActionGoTo) childOutlineItem.getAction();
                    if (gta.getDestination() instanceof PDPageDestination) {
                        PDPageDestination pd = (PDPageDestination) gta.getDestination();
                        pageNumber = pd.retrievePageNumber();
                    }
                }
                Text link = new Text();
                final int p = pageNumber;
                link.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        loadPage(p);
                    }
                });
                link.setText(childOutlineItem.getTitle() + " ... " + (pageNumber + 1));
                TreeItem<Text> treeItem = new TreeItem<>(link);
                treeItem.setExpanded(true);
                parentTreeItem.getChildren().add(treeItem);
                if (bookmarksTask == null || bookmarksTask.isCancelled()) {
                    return false;
                }
                loadOutlineItem(childOutlineItem, treeItem);
                childOutlineItem = childOutlineItem.getNextSibling();
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    protected boolean loadThumbs(List<Integer> missed) {
        try (PDDocument doc = Loader.loadPDF(sourceFile, password)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            for (Integer index : missed) {
                if (thumbTask == null || thumbTask.isCancelled()) {
                    break;
                }
                ImageView view = (ImageView) thumbBox.getChildren().get(2 * index);
                if (view.getImage() != null) {
                    continue;
                }
                BufferedImage bufferedImage = renderer.renderImageWithDPI(index, 72, ImageType.RGB);  // 0-based
                if (bufferedImage.getWidth() > thumbWidth) {
                    bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, thumbWidth);
                }
                Image thumb = SwingFXUtils.toFXImage(bufferedImage, null);
                view.setImage(thumb);
                view.setFitHeight(view.getImage().getHeight());
            }
            doc.close();
        } catch (Exception e) {
            thumbTask.setError(e.toString());
            MyBoxLog.debug(e);
            return false;
        }
        return true;
    }

    @FXML
    public void refreshBookmarks() {
        loadBookmarks();
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

    @FXML
    @Override
    public void playAction() {
        ImagesPlayController.playPDF(sourceFile, password);
    }

    @FXML
    public void permissionAction() {
        PdfAttributesController.open(sourceFile, password);
    }

    @FXML
    protected void exampleAction() {
        File example = HelpTools.pdfExample(Languages.embedFileLang());
        if (example != null && example.exists()) {
            sourceFileChanged(example);
        }
    }

    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        try {

            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (sourceFile != null) {
                menu = new MenuItem(message("Permissions"), StyleTools.getIconImageView("iconPermission.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    permissionAction();
                });
                items.add(menu);

                menu = new MenuItem(message("Information") + "    Ctrl+I " + message("Or") + " Alt+I",
                        StyleTools.getIconImageView("iconInfo.png"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    infoAction();
                });
                items.add(menu);

                items.add(new SeparatorMenuItem());
            }

            menu = new MenuItem(message("Example"), StyleTools.getIconImageView("iconExamples.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                exampleAction();
            });
            items.add(menu);

            if (sourceFile == null) {
                return items;
            }

            menu = new MenuItem(message("OpenDirectory"), StyleTools.getIconImageView("iconOpenPath.png"));
            menu.setOnAction((ActionEvent event) -> {
                openSourcePath();
            });
            items.add(menu);

            menu = new MenuItem(message("BrowseFiles"), StyleTools.getIconImageView("iconList.png"));
            menu.setOnAction((ActionEvent event) -> {
                FileBrowseController.open(this);
            });
            items.add(menu);

            menu = new MenuItem(message("SystemMethod"), StyleTools.getIconImageView("iconSystemOpen.png"));
            menu.setOnAction((ActionEvent event) -> {
                systemMethod();
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
