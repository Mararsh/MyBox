package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.data.PdfInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
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
    protected SingletonTask outlineTask;

    @FXML
    protected CheckBox transparentBackgroundCheck, bookmarksCheck,
            wrapTextsCheck, refreshSwitchTextsCheck, refreshChangeTextsCheck,
            wrapOCRCheck, refreshChangeOCRCheck, refreshSwitchOCRCheck,
            refreshChangeHtmlCheck, refreshSwitchHtmlCheck;
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
                transparentBackgroundCheck.setSelected(UserConfig.getBoolean(baseName + "Transparent", false));
                transparentBackgroundCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
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
                        if (orcPage != frameIndex && refreshSwitchOCRCheck.isSelected()) {
                            startOCR();
                        }
                    } else if (newValue == textsTab) {
                        if (textsPage != frameIndex && refreshSwitchTextsCheck.isSelected()) {
                            extractTexts();
                        }
                    } else if (newValue == htmlTab) {
                        if (htmlPage != frameIndex && refreshSwitchHtmlCheck.isSelected()) {
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

            refreshSwitchTextsCheck.setSelected(UserConfig.getBoolean(baseName + "RefreshSwitchTexts", true));
            refreshSwitchTextsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "RefreshSwitchTexts", newValue);
                }
            });

            refreshChangeTextsCheck.setSelected(UserConfig.getBoolean(baseName + "RefreshChangeTexts", false));
            refreshChangeTextsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "RefreshChangeTexts", newValue);
                }
            });

            wrapOCRCheck.setSelected(UserConfig.getBoolean(baseName + "WrapOCR", true));
            wrapOCRCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WrapOCR", newValue);
                    ocrArea.setWrapText(newValue);
                }
            });
            ocrArea.setWrapText(wrapTextsCheck.isSelected());

            refreshSwitchOCRCheck.setSelected(UserConfig.getBoolean(baseName + "RefreshSwitchOCR", true));
            refreshSwitchOCRCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "RefreshSwitchOCR", newValue);
                }
            });

            refreshChangeOCRCheck.setSelected(UserConfig.getBoolean(baseName + "RefreshChangeOCR", false));
            refreshChangeOCRCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "RefreshChangeOCR", newValue);
                }
            });

            refreshSwitchHtmlCheck.setSelected(UserConfig.getBoolean(baseName + "RefreshSwitchHtml", true));
            refreshSwitchHtmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "RefreshSwitchHtml", newValue);
                }
            });

            refreshChangeHtmlCheck.setSelected(UserConfig.getBoolean(baseName + "RefreshChangeHtml", false));
            refreshChangeHtmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "RefreshChangeHtml", newValue);
                }
            });

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
            if (outlineTask != null) {
                outlineTask.cancel();
                outlineTask = null;
            }
            infoLoaded.set(false);
            outlineTree.setRoot(null);
            ocrArea.clear();
            ocrLabel.setText("");
            textsArea.clear();
            textsLabel.setText("");
            if (webViewController != null) {
                webViewController.loadContents(null);
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

    public void loadInformation() {
        if (pdfInformation == null) {
            if (sourceFile == null) {
                return;
            }
            pdfInformation = new PdfInformation(sourceFile);
        }
        if (task != null) {
            task.cancel();
        }
        if (outlineTask != null) {
            outlineTask.cancel();
        }
        if (thumbTask != null) {
            thumbTask.cancel();
        }
        bottomLabel.setText("");
        isSettingValues = true;
        pageSelector.getItems().clear();
        isSettingValues = false;
        pageLabel.setText("");
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                setTotalPages(0);
                if (!PdfInformation.readPDF(pdfInformation)) {
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
                checkOutline();
                checkThumbs();
            }

        };
        start(task, message("LoadingFileInfo"));
    }

    @Override
    protected Image readPageImage() {
        try {
            BufferedImage bufferedImage = PdfTools.page2image(sourceFile, password, frameIndex, dpi,
                    transparentBackgroundCheck.isSelected() ? ImageType.ARGB : ImageType.RGB);
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
        if (refreshChangeOCRCheck.isSelected() || tab == ocrTab) {
            startOCR();
        } else if (refreshChangeTextsCheck.isSelected() || tab == textsTab) {
            extractTexts();
        } else if (refreshChangeHtmlCheck.isSelected() || tab == htmlTab) {
            convertHtml();
        }
    }

    protected void loadOutline() {
        if (!infoLoaded.get()) {
            return;
        }
        if (outlineTask != null) {
            outlineTask.cancel();
        }
        outlineTree.setRoot(null);
        TreeItem outlineRoot = new TreeItem<>(message("Bookmarks"));
        outlineRoot.setExpanded(true);
        outlineTask = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (PDDocument doc = PDDocument.load(sourceFile, password, AppVariables.pdfMemUsage)) {
                    PDDocumentOutline outline = doc.getDocumentCatalog().getDocumentOutline();
                    if (outline != null) {
                        loadOutlineItem(outline, outlineRoot);
                    }
                    doc.close();
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.debug(e);
                    return false;
                }
                return true;
            }

            @Override
            protected void whenFailed() {
                if (error != null) {
                    popError(error);
                } else {
                    popFailed();
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                outlineTree.setRoot(outlineRoot);
                outlineScrollPane.applyCss();
                outlineScrollPane.layout();
            }

        };
        start(outlineTask, false);
    }

    protected void loadOutlineItem(PDOutlineNode parentOutlineItem, TreeItem parentTreeItem) {
        try {
            PDOutlineItem childOutlineItem = parentOutlineItem.getFirstChild();
            while (childOutlineItem != null) {
                if (outlineTask == null || outlineTask.isCancelled()) {
                    break;
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
                loadOutlineItem(childOutlineItem, treeItem);
                childOutlineItem = childOutlineItem.getNextSibling();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    @Override
    protected boolean loadThumbs(List<Integer> missed) {
        try (PDDocument doc = PDDocument.load(sourceFile, password, AppVariables.pdfMemUsage)) {
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
                if (bufferedImage.getWidth() > ThumbWidth) {
                    bufferedImage = ScaleTools.scaleImageWidthKeep(bufferedImage, ThumbWidth);
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

    @FXML
    @Override
    public void playAction() {
        ImagesPlayController.playPDF(sourceFile, password);
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
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
