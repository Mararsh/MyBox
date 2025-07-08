package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.image.tools.ScaleTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
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
public class PdfViewController_Base extends BaseFileImagesController {

    protected String password;
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
    @FXML
    protected FlowPane formatPane;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PDF, VisitHistory.FileType.Image);
    }

    protected void loadBookmarks() {
        if (bookmarksTask != null) {
            bookmarksTask.cancel();
        }
        bookmarksTree.setRoot(null);
        if (!viewBookmarkCheck.isSelected() || !infoLoaded.get()) {
            return;
        }
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
                if (thumbTask == null || !thumbTask.isWorking()) {
                    break;
                }
                ImageView view = (ImageView) thumbBox.getChildren().get(2 * index);
                MyBoxLog.console(2 * index);
                if (view.getImage() != null) {
                    continue;
                }
                try {
                    BufferedImage bufferedImage = renderer.renderImageWithDPI(index, 72, ImageType.RGB);  // 0-based
                    if (bufferedImage.getWidth() > thumbWidth) {
                        bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, thumbWidth);
                    }
                    Image thumb = SwingFXUtils.toFXImage(bufferedImage, null);
                    view.setImage(thumb);
                } catch (Exception e) {
                }
            }
            doc.close();
        } catch (Exception e) {
//            thumbTask.setError(e.toString());
//            MyBoxLog.debug(e);
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

}
