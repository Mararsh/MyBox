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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.data.PdfInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.image.ImageManufacture;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
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
public class PdfViewController extends BaseFileImagesViewController {

    protected PdfInformation pdfInformation;
    protected SimpleBooleanProperty infoLoaded;
    protected boolean isTransparent;
    protected Task outlineTask;
    protected String password;

    @FXML
    protected CheckBox transparentBackgroundCheck, bookmarksCheck;
    @FXML
    protected ScrollPane outlineScrollPane;
    @FXML
    protected TreeView outlineTree;

    public PdfViewController() {
        baseTitle = AppVariables.message("PdfView");
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

            if (tipsView != null) {
                FxmlControl.setTooltip(tipsView, new Tooltip(message("PDFComments") + "\n\n" + message("PdfViewTips")));
            }

            if (bookmarksCheck != null) {
                bookmarksCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Bookmarks", true));
                bookmarksCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        checkOutline();
                        AppVariables.setUserConfigValue(baseName + "Bookmarks", bookmarksCheck.isSelected());
                    }
                });
            }

            if (transparentBackgroundCheck != null) {
                isTransparent = AppVariables.getUserConfigBoolean(baseName + "Transparent", false);
                transparentBackgroundCheck.setSelected(isTransparent);
                transparentBackgroundCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        isTransparent = transparentBackgroundCheck.isSelected();
                        AppVariables.setUserConfigValue(baseName + "Transparent", transparentBackgroundCheck.isSelected());
                        loadPage();
                    }
                });
            }

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
        loadFile(file, null, 1);
    }

    public void loadFile(File file, PdfInformation pdfInfo, int page) {
        try {
            initPage(file, page);
            infoLoaded.set(false);
            outlineTree.setRoot(null);
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
                    pageSelector.setValue("1");
                    pageLabel.setText("/" + framesNumber);
                    isSettingValues = false;
                    checkCurrentPage();
                    checkOutline();
                    checkThumbs();
                }

                @Override
                protected void whenFailed() {
                    if (pop) {
                        TextInputDialog dialog = new TextInputDialog();
                        dialog.setContentText(AppVariables.message("UserPassword"));
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
                        popError(AppVariables.message(error));
                    } else {
                        popFailed();
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL, message("LoadingFileInfo"));
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
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
                        TreeItem outlineRoot = new TreeItem<>(AppVariables.message("Bookmarks"));
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
            openHandlingStage(outlineTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(outlineTask);
            thread.setDaemon(false);
            thread.start();
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
                        bufferedImage = ImageManufacture.scaleImageWidthKeep(bufferedImage, ThumbWidth);
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
            final PdfInformationController controller = (PdfInformationController) openStage(CommonValues.PdfInformationFxml);
            controller.setInformation(pdfInformation);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (outlineTask != null && !outlineTask.isDone()) {
            outlineTask.cancel();
            outlineTask = null;
        }
        return super.checkBeforeNextAction();
    }

}
