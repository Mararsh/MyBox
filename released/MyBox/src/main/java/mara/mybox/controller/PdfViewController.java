package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.data.PdfInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
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
    protected boolean isTransparent;
    protected Task outlineTask;

    @FXML
    protected CheckBox transparentBackgroundCheck, bookmarksCheck, wrapTextsCheck, wrapOCRCheck;
    @FXML
    protected ScrollPane outlineScrollPane;
    @FXML
    protected TreeView outlineTree;

    public PdfViewController() {
        baseTitle = message("PdfView");
        TipsLabelKey = "PdfViewTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            infoLoaded = new SimpleBooleanProperty(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PDF, VisitHistory.FileType.Image);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (ocrOptionsController != null) {
                ocrOptionsController.setParameters(this, false, false);
            }
            initTabPane();

            if (bookmarksCheck != null) {
                bookmarksCheck.setSelected(UserConfig.getBoolean(baseName + "Bookmarks", true));
                bookmarksCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        checkOutline();
                        UserConfig.setBoolean(baseName + "Bookmarks", bookmarksCheck.isSelected());
                    }
                });
            }

            if (transparentBackgroundCheck != null) {
                isTransparent = UserConfig.getBoolean(baseName + "Transparent", false);
                transparentBackgroundCheck.setSelected(isTransparent);
                transparentBackgroundCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        isTransparent = transparentBackgroundCheck.isSelected();
                        UserConfig.setBoolean(baseName + "Transparent", transparentBackgroundCheck.isSelected());
                        loadPage();
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initTabPane() {
        try {
            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                    if (imageView.getImage() == null) {
                        return;
                    }
                    if (newValue == ocrTab) {
                        if (orcPage != frameIndex) {
                            startOCR();
                        }
                    } else if (newValue == textsTab) {
                        if (textsPage != frameIndex) {
                            extractTexts();
                        }
                    } else if (newValue == htmlTab) {
                        if (htmlPage != frameIndex) {
                            convertHtml();
                        }
                    }
                }
            });

            wrapTextsCheck.setSelected(UserConfig.getBoolean(baseName + "WrapTexts", true));
            wrapTextsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WrapTexts", newValue);
                    textsArea.setWrapText(newValue);
                }
            });
            textsArea.setWrapText(wrapTextsCheck.isSelected());

            wrapOCRCheck.setSelected(UserConfig.getBoolean(baseName + "WrapOCR", true));
            wrapOCRCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WrapOCR", newValue);
                    ocrArea.setWrapText(newValue);
                }
            });
            ocrArea.setWrapText(wrapTextsCheck.isSelected());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkOutline() {
        if (!infoLoaded.get()) {
            return;
        }
        if (bookmarksCheck.isSelected()) {
            if (!mainPane.getItems().contains(outlineScrollPane)) {
                mainPane.getItems().add(mainPane.getItems().size() - 1, outlineScrollPane);
            }
            loadOutline();

        } else {
            if (mainPane.getItems().contains(outlineScrollPane)) {
                mainPane.getItems().remove(outlineScrollPane);
            }
        }
        adjustSplitPane();

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
            infoLoaded.set(false);
            outlineTree.setRoot(null);
            ocrArea.clear();
            ocrLabel.setText("");
            textsArea.clear();
            textsLabel.setText("");
            webEngine.loadContent("");
            if (file == null) {
                return;
            }
            if (pdfInfo != null) {
                pdfInformation = pdfInfo;
                checkCurrentPage();
            } else {
                pdfInformation = new PdfInformation(sourceFile);
                loadInformation(null);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void setSourceFile(File file) {
        super.setSourceFile(file);
        orcPage = -1;
        textsPage = -1;
        htmlPage = -1;
    }

    public void loadInformation(String inPassword) {
        if (pdfInformation == null) {
            if (sourceFile == null) {
                return;
            }
            pdfInformation = new PdfInformation(sourceFile);
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            bottomLabel.setText("");
            isSettingValues = true;
            pageSelector.getItems().clear();
            isSettingValues = false;
            pageLabel.setText("");
            task = new SingletonTask<Void>() {
                protected boolean pop;

                @Override
                protected boolean handle() {
                    setTotalPages(0);
                    try ( PDDocument doc = PDDocument.load(sourceFile, inPassword, AppVariables.pdfMemUsage)) {
                        password = inPassword;
                        pdfInformation.setUserPassword(inPassword);
                        pdfInformation.readInfo(doc);
                        infoLoaded.set(true);
                        doc.close();
                        ok = true;
                    } catch (InvalidPasswordException e) {
                        pop = true;
                        return false;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
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
                    checkOutline();
                    checkThumbs();
                }

                @Override
                protected void whenFailed() {
                    if (pop) {
                        TextInputDialog dialog = new TextInputDialog();
                        dialog.setContentText(message("UserPassword"));
                        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                        stage.setAlwaysOnTop(true);
                        stage.toFront();
                        Optional<String> result = dialog.showAndWait();
                        if (result.isPresent()) {
                            loadInformation(result.get());
                        }
                        return;
                    }
                    if (error != null) {
                        popError(message(error));
                    } else {
                        popFailed();
                    }
                }

            };
            start(task, message("LoadingFileInfo"));
        }
    }

    @Override
    protected Image readPageImage() {
        try {
            ImageType type = ImageType.RGB;
            if (isTransparent) {
                type = ImageType.ARGB;
            }
            BufferedImage bufferedImage = PdfTools.page2image(sourceFile, password, frameIndex, dpi, type);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (Exception e) {
            MyBoxLog.console(e);
            return null;
        }
    }

    @Override
    public void setImage(Image image, int percent) {
        if (imageView == null) {
            return;
        }
        super.setImage(image, percent);
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == ocrTab) {
            startOCR();
        } else if (tab == textsTab) {
            extractTexts();
        } else if (tab == htmlTab) {
            convertHtml();
        }
    }

    protected void loadOutline() {
        if (!infoLoaded.get() || outlineTree.getRoot() != null) {
            return;
        }
        synchronized (this) {
            if (outlineTask != null) {
                outlineTask.cancel();
            }
            outlineTask = new SingletonTask<Void>() {
                protected PDDocument doc;

                @Override
                protected boolean handle() {
                    try {
                        doc = PDDocument.load(sourceFile, password, AppVariables.pdfMemUsage);
                        return doc != null;
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.debug(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    try {
                        PDDocumentOutline outline = doc.getDocumentCatalog().getDocumentOutline();
                        TreeItem outlineRoot = new TreeItem<>(message("Bookmarks"));
                        outlineRoot.setExpanded(true);
                        outlineTree.setRoot(outlineRoot);
                        if (outline != null) {
                            loadOutlineItem(outline, outlineRoot);
                        }
                        doc.close();
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                }

                @Override
                protected void whenFailed() {
                    if (error != null) {
                        popError(error);
                    } else {
                        popFailed();
                    }
                }

            };
            start(outlineTask);
        }
    }

    protected void loadOutlineItem(PDOutlineNode parentOutlineItem, TreeItem parentTreeItem) {
        try {
            PDOutlineItem childOutlineItem = parentOutlineItem.getFirstChild();
            while (childOutlineItem != null) {
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
                loadOutlineItem(childOutlineItem, treeItem);
                childOutlineItem = childOutlineItem.getNextSibling();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    @Override
    protected Map<Integer, Image> readThumbs(int pos, int end) {
        Map<Integer, Image> images = null;
        try ( PDDocument doc = PDDocument.load(sourceFile, password, AppVariables.pdfMemUsage)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            images = new HashMap<>();
            for (int i = pos; i < end; ++i) {
                ImageView view = (ImageView) thumbBox.getChildren().get(2 * i);
                if (view.getImage() != null) {
                    continue;
                }
                try {
                    BufferedImage bufferedImage = renderer.renderImageWithDPI(i, 72, ImageType.RGB);  // 0-based
                    if (bufferedImage.getWidth() > ThumbWidth) {
                        bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, ThumbWidth);
                    }
                    Image thumb = SwingFXUtils.toFXImage(bufferedImage, null);
                    images.put(i, thumb);
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
            }
            doc.close();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return images;
    }

    @FXML
    @Override
    public void infoAction() {
        if (pdfInformation == null) {
            return;
        }
        try {
            PdfInformationController controller = (PdfInformationController) openStage(Fxmls.PdfInformationFxml);
            controller.setInformation(pdfInformation);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            if (outlineTask != null) {
                outlineTask.cancel();
                outlineTask = null;
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

}
