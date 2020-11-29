package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.PdfInformation;
import mara.mybox.data.VisitHistory;
import mara.mybox.data.tools.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
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
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfViewController extends ImageViewerController {

    protected PdfInformation pdfInformation;
    protected int currentPage, currentPageTmp, percent, orcPage;
    protected SimpleBooleanProperty infoLoaded;
    protected boolean isTransparent, scrollEnd, scrollStart, scrolledSet;
    protected Task outlineTask, thumbTask;
    protected String password;
    protected LoadingController loading;
    protected Process process;

    @FXML
    protected ComboBox<String> percentBox, dpiBox, pageSelector;
    @FXML
    protected CheckBox transparentBackgroundCheck, bookmarksCheck, thumbCheck;
    @FXML
    protected SplitPane mainPane;
    @FXML
    protected ScrollPane thumbScrollPane, outlineScrollPane;
    @FXML
    protected VBox thumbBox;
    @FXML
    protected TreeView outlineTree;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab imageTab, ocrTab, ocrOptionsTab;
    @FXML
    protected TextArea ocrArea;
    @FXML
    protected Label pageLabel, resultLabel;
    @FXML
    protected ImageOCROptionsController ocrOptionsController;

    public PdfViewController() {
        baseTitle = AppVariables.message("PdfView");

        SourceFileType = VisitHistory.FileType.PDF;
        SourcePathType = VisitHistory.FileType.PDF;
        TargetFileType = VisitHistory.FileType.Image;
        TargetPathType = VisitHistory.FileType.Image;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.PDF);
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Image);
        TipsLabelKey = "PdfViewTips";

        sourceExtensionFilter = CommonFxValues.PdfExtensionFilter;
        targetExtensionFilter = CommonFxValues.ImageExtensionFilter;
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
    public void initControls() {
        try {
            super.initControls();

            if (ocrOptionsController != null) {
                ocrOptionsController.setValues(this, false, false);
            }

            if (tipsView != null) {
                FxmlControl.setTooltip(tipsView, new Tooltip(message("PDFComments") + "\n\n" + message("PdfViewTips")));
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initOperationBox() {
        try {
            super.initOperationBox();

            if (percentBox != null) {
                percentBox.getItems().addAll(Arrays.asList("100", "75", "50", "125", "150", "200", "80", "25", "30", "15"));
                percentBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue,
                            String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                percent = v;
                                setPrecent(percent);
                                FxmlControl.setEditorNormal(percentBox);
                            } else {
                                FxmlControl.setEditorBadStyle(percentBox);
                            }

                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(percentBox);
                        }
                    }
                });
            }

            if (pageSelector != null) {
                pageSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        try {
                            int v = Integer.valueOf(newValue) - 1;
                            if (v >= 0 && v < pdfInformation.getNumberOfPages()) {
                                currentPageTmp = v;
                                pageSelector.getEditor().setStyle(null);
                                goButton.setDisable(false);
                            } else {
                                pageSelector.getEditor().setStyle(badStyle);
                                goButton.setDisable(true);
                            }
                        } catch (Exception e) {
                            pageSelector.getEditor().setStyle(badStyle);
                            goButton.setDisable(true);
                        }
                    }
                });
            }

            if (bookmarksCheck != null) {
                bookmarksCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        checkOutline();
                        AppVariables.setUserConfigValue(baseName + "Bookmarks", bookmarksCheck.isSelected());
                    }
                });
                bookmarksCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Bookmarks", true));
            }

            if (thumbCheck != null) {
                thumbCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        checkThumbs();
                        AppVariables.setUserConfigValue(baseName + "Thumbnails", thumbCheck.isSelected());
                    }
                });
                thumbCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Thumbnails", false));
            }

            if (thumbScrollPane != null) {
                thumbScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number oldValue,
                            Number newValue) {
                        loadThumbs();
                    }
                });
            }

            if (transparentBackgroundCheck != null) {
                transparentBackgroundCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        isTransparent = transparentBackgroundCheck.isSelected();
                        AppVariables.setUserConfigValue(baseName + "Transparent", transparentBackgroundCheck.isSelected());
                        loadPage();
                    }
                });
                transparentBackgroundCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Transparent", false));
                FxmlControl.setTooltip(transparentBackgroundCheck, new Tooltip(message("OnlyForTexts")));
            }

            if (dpiBox != null) {
                dpiBox.getItems().addAll(Arrays.asList("96", "72", "120", "160", "240", "300", "400", "600"));
                dpiBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue,
                            String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                dpi = v;
                                loadPage();
                                FxmlControl.setEditorNormal(dpiBox);
                            } else {
                                FxmlControl.setEditorBadStyle(dpiBox);
                            }

                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(dpiBox);
                        }
                    }
                });
                dpiBox.getSelectionModel().select(0);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void initViewPane() {
        try {
            super.initViewPane();

            if (imageView != null) {
                mainPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }
            if (scrollPane != null) {
                scrollPane.addEventHandler(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>() {
                    @Override
                    public void handle(ScrollEvent event) {
                        double deltaY = event.getDeltaY();
                        if (event.isControlDown()) {
//                        event.consume();
//                        MyBoxLog.debug(event.isConsumed());
//                        if (deltaY > 0) {
//                            zoomIn();
//                        } else {
//                            zoomOut();
//                        }
                        } else {
                            if (deltaY > 0) {
                                if (scrollPane.getVvalue() == scrollPane.getVmin()) {
                                    event.consume();
                                    previousAction();
                                }
                            } else {

                                if (scrollPane.getHeight() >= imageView.getFitHeight()
                                        || scrollPane.getVvalue() == scrollPane.getVmax()) {
                                    event.consume();
                                    nextAction();
                                }
                            }
                        }

                    }
                });
            }
            if (tabPane != null) {
                tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue ov, Tab oldValue,
                            Tab newValue) {
                        if (!ocrTab.equals(newValue) || orcPage == currentPage
                                || imageView.getImage() == null) {
                            return;
                        }
                        startOCR();
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

    protected void checkThumbs() {
        if (!infoLoaded.get()) {
            return;
        }
        if (thumbCheck.isSelected()) {
            if (!mainPane.getItems().contains(thumbScrollPane)) {
                mainPane.getItems().add(0, thumbScrollPane);
            }
            loadThumbs();

        } else {
            if (mainPane.getItems().contains(thumbScrollPane)) {
                mainPane.getItems().remove(thumbScrollPane);
            }
        }
        adjustSplitPane();

    }

    protected void adjustSplitPane() {
        try {
            int size = mainPane.getItems().size();
            switch (size) {
                case 1:
                    mainPane.setDividerPositions(1);
                    break;
                case 2:
                    mainPane.setDividerPosition(0, 0.3);
                    break;
                case 3:
                    mainPane.setDividerPosition(0, 0.2);
                    mainPane.setDividerPosition(1, 0.5);
                    break;
            }
            mainPane.layout();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setPrecent(int percent) {
        if (imageView.getImage() == null) {
            return;
        }
        scrolledSet = true;
        double w = imageView.getImage().getWidth();
        imageView.setFitWidth(w * percent / 100);
        double h = imageView.getImage().getHeight();
        imageView.setFitHeight(h * percent / 100);
    }

    @Override
    public void sourceFileChanged(final File file) {
        if (file == null) {
            return;
        }
        loadFile(file, null, 0);
    }

    public void loadFile(File file, PdfInformation pdfInfo, int page) {
        try {
            imageView.setImage(null);
            imageView.setTranslateX(0);
            pdfInformation = null;
            currentPage = page;
            orcPage = -1;
            infoLoaded.set(pdfInfo != null);
            pageSelector.setValue("1");
            pageLabel.setText("");
            percent = 0;
            thumbBox.getChildren().clear();
            outlineTree.setRoot(null);
            if (file == null) {
                return;
            }
            sourceFile = file;
            getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath());
            if (pdfInfo != null) {
                pdfInformation = pdfInfo;
                loadPage();
            } else {
                pdfInformation = new PdfInformation(sourceFile);
                loadInformation(null);
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void loadInformation(final String inPassword) {
        if (pdfInformation == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                protected boolean pop;

                @Override
                protected boolean handle() {
                    try {
                        try ( PDDocument doc = PDDocument.load(sourceFile, inPassword, AppVariables.pdfMemUsage)) {
                            password = inPassword;
                            pdfInformation.setUserPassword(inPassword);
                            pdfInformation.readInfo(doc);
                            infoLoaded.set(true);
                            doc.close();
                            ok = true;
                        }
                    } catch (InvalidPasswordException e) {
                        pop = true;
                    } catch (IOException e) {
                    }
                    if (pop) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                TextInputDialog dialog = new TextInputDialog();
                                dialog.setContentText(AppVariables.message("Password"));
                                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                                stage.setAlwaysOnTop(true);
                                stage.toFront();
                                Optional<String> result = dialog.showAndWait();
                                if (result.isPresent()) {
                                    loadInformation(result.get());
                                }
                            }
                        });
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    bottomLabel.setText("");
                    int size = pdfInformation.getNumberOfPages();
                    pageSelector.getItems().clear();
                    List<String> pages = new ArrayList<>();
                    for (int i = 1; i <= size; i++) {
                        pages.add(i + "");
                    }
                    pageSelector.getItems().setAll(pages);
                    pageSelector.setValue(size + "");
                    pageLabel.setText("/" + pdfInformation.getNumberOfPages());
                    loadPage();
                    checkOutline();
                    checkThumbs();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL, message("LoadingFileInfo"));
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void loadPage(int pageNumber) {
        currentPage = pageNumber;
        loadPage();
    }

    // currentPage is 0-based
    protected void loadPage() {
        if (pdfInformation == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            if (currentPage < 0) {
                currentPage = 0;
            } else if (infoLoaded.get() && currentPage >= pdfInformation.getNumberOfPages()) {
                currentPage = pdfInformation.getNumberOfPages() - 1;
            }
            pageSelector.setValue((currentPage + 1) + "");
            previousButton.setDisable(currentPage <= 0);
            nextButton.setDisable(!infoLoaded.get() || currentPage >= (pdfInformation.getNumberOfPages() - 1));
            bottomLabel.setText("");
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    ImageType type = ImageType.RGB;
                    if (isTransparent) {
                        type = ImageType.ARGB;
                    }
                    BufferedImage bufferedImage = PdfTools.page2image(sourceFile, password, currentPage, dpi, type);
                    image = SwingFXUtils.toFXImage(bufferedImage, null);
                    return image != null;
                }

                @Override
                protected void whenSucceeded() {
                    imageView.setPreserveRatio(true);
                    imageView.setImage(image);
                    if (percent == 0) {
                        paneSize();
                    } else {
                        setPrecent(percent);
                    }
                    refinePane();
                    setMaskStroke();
                    checkSelect();
                    setImageChanged(false);
                    imageView.requestFocus();
                    if (tabPane.getSelectionModel().getSelectedItem() == ocrTab) {
                        startOCR();
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL, MessageFormat.format(message("LoadingPageNumber"), (currentPage + 1) + ""));
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
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
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void loadOutlineItem(PDOutlineNode parentOutlineItem,
            TreeItem parentTreeItem) {
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

    protected void loadThumbs() {
        if (!infoLoaded.get()
                || (thumbTask != null && thumbTask.isRunning())) {
            return;
        }
        if (thumbBox.getChildren().isEmpty()) {
            for (int i = 0; i < pdfInformation.getNumberOfPages(); ++i) {
                ImageView view = new ImageView();
                view.setFitHeight(50);
                view.setPreserveRatio(true);
                thumbBox.getChildren().add(view);
                thumbBox.getChildren().add(new Label((i + 1) + ""));
            }
        }
        final int pos = Math.max(0,
                (int) (pdfInformation.getNumberOfPages() * thumbScrollPane.getVvalue() / thumbScrollPane.getVmax()) - 1);
        ImageView view = (ImageView) thumbBox.getChildren().get(pos * 2);
        if (view.getImage() != null) {
            return;
        }
        synchronized (this) {
            if (thumbTask != null) {
                thumbTask.cancel();
            }
            thumbTask = new SingletonTask<Void>() {

                protected Map<Integer, Image> images;
                protected int end;

                @Override
                protected boolean handle() {
                    try {
                        try ( PDDocument doc = PDDocument.load(sourceFile, password, AppVariables.pdfMemUsage)) {
                            PDFRenderer renderer = new PDFRenderer(doc);
                            images = new HashMap<>();
                            end = Math.min(pos + 20, pdfInformation.getNumberOfPages());
                            for (int i = pos; i < end; ++i) {
                                ImageView view = (ImageView) thumbBox.getChildren().get(2 * i);
                                if (view.getImage() != null) {
                                    continue;
                                }
                                try {
                                    BufferedImage bufferedImage = renderer.renderImageWithDPI(i, 20, ImageType.RGB);  // 0-based
                                    if (bufferedImage.getWidth() > 200) {
                                        bufferedImage = ImageManufacture.scaleImageWidthKeep(bufferedImage, 200);
                                    }
                                    Image thumb = SwingFXUtils.toFXImage(bufferedImage, null);
                                    images.put(i, thumb);
                                } catch (Exception e) {
                                    MyBoxLog.debug(e.toString());
                                }
                            }
                            doc.close();
                        }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.debug(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    for (int i = pos; i < end; ++i) {
                        ImageView view = (ImageView) thumbBox.getChildren().get(2 * i);
                        if (view.getImage() != null) {
                            continue;
                        }
                        view.setImage(images.get(i));
                        view.setFitHeight(view.getImage().getHeight());
                        final int p = i;
                        view.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent event) {
                                loadPage(p);
                            }
                        });
                    }
                    thumbBox.layout();
                    adjustSplitPane();
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
            openHandlingStage(thumbTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(thumbTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void setImageChanged(boolean imageChanged) {
        this.imageChanged = imageChanged;
        updateLabelTitle();
    }

    @Override
    public void setDafultMaskRectangleValues() {
        if (imageView == null || maskPane == null || maskRectangleLine == null) {
            return;
        }
        if (maskRectangleData == null
                || maskRectangleData.getWidth() > imageView.getImage().getWidth()
                || maskRectangleData.getHeight() > imageView.getImage().getHeight()) {
            maskRectangleData = new DoubleRectangle(0, 0,
                    imageView.getImage().getWidth() - 1, imageView.getImage().getHeight() - 1);
        }
    }

    @Override
    public PdfViewController refresh() {
        File oldfile = sourceFile;
        PdfInformation oldInfo = pdfInformation;
        int oldPage = currentPage;

        PdfViewController c = (PdfViewController) refreshBase();
        if (c == null) {
            return null;
        }
        c.loadFile(oldfile, oldInfo, oldPage);
        return c;
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

    @FXML
    @Override
    public void nextAction() {
        if (pdfInformation == null
                || currentPage >= pdfInformation.getNumberOfPages() - 1) {
            return;
        }
        currentPage++;
        loadPage();
    }

    @FXML
    @Override
    public void previousAction() {
        if (pdfInformation == null || currentPage <= 0) {
            return;
        }
        currentPage--;
        loadPage();
    }

    @FXML
    @Override
    public void firstAction() {
        if (pdfInformation == null) {
            return;
        }
        currentPage = 0;
        loadPage();
    }

    @FXML
    @Override
    public void lastAction() {
        if (pdfInformation == null) {
            return;
        }
        currentPage = pdfInformation.getNumberOfPages() - 1;
        loadPage();

    }

    @FXML
    protected void goPageAction() {
        currentPage = currentPageTmp;
        loadPage();
    }

    public void setSizeBox() {
        scrolledSet = true;
        percent = (int) (imageView.getFitHeight() * 100 / imageView.getImage().getHeight());
        isSettingValues = true;
        percentBox.getSelectionModel().select(percent + "");
        isSettingValues = false;
    }

    @FXML
    @Override
    public void loadedSize() {
        scrolledSet = true;
        super.loadedSize();
        setSizeBox();
    }

    @FXML
    @Override
    public void paneSize() {
        scrolledSet = true;
        super.paneSize();
        setSizeBox();
    }

    @FXML
    @Override
    public void zoomIn() {
        scrolledSet = true;
        super.zoomIn();
        setSizeBox();
    }

    @FXML
    @Override
    public void zoomOut() {
        scrolledSet = true;
        super.zoomOut();
        setSizeBox();
    }

    /*
        OCR
     */
    @FXML
    public void startOCR() {
        if (imageView.getImage() == null) {
            return;
        }
        ocrOptionsController.setLanguages();
        File dataPath = ocrOptionsController.dataPathController.file;
        if (!dataPath.exists()) {
            popError(message("InvalidParameters"));
            ocrOptionsController.dataPathController.fileInput.setStyle(badStyle);
            return;
        }
        if (ocrOptionsController.embedRadio.isSelected()) {
            embedded();
        } else {
            command();
        }
    }

    protected void command() {
        if (imageView.getImage() == null || timer != null || process != null) {
            return;
        }
        File tesseract = ocrOptionsController.tesseractPathController.file;
        if (!tesseract.exists()) {
            popError(message("InvalidParameters"));
            ocrOptionsController.tesseractPathController.fileInput.setStyle(badStyle);
            return;
        }
        loading = openHandlingStage(Modality.WINDOW_MODAL);
        new Thread() {
            private String outputs = "";

            @Override
            public void run() {
                try {
                    Image selected = cropImage();
                    if (selected == null) {
                        selected = imageView.getImage();
                    }
                    String imageFile = FileTools.getTempFile(".png").getAbsolutePath();
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(selected, null);
                    bufferedImage = ImageManufacture.removeAlpha(bufferedImage);
                    ImageFileWriters.writeImageFile(bufferedImage, "png", imageFile);

                    int version = ocrOptionsController.tesseractVersion();
                    String fileBase = FileTools.getTempFile().getAbsolutePath();
                    List<String> parameters = new ArrayList<>();
                    parameters.addAll(Arrays.asList(
                            tesseract.getAbsolutePath(),
                            imageFile, fileBase,
                            "--tessdata-dir", ocrOptionsController.dataPathController.file.getAbsolutePath(),
                            version > 3 ? "--psm" : "-psm", ocrOptionsController.psm + ""
                    ));
                    if (ocrOptionsController.selectedLanguages != null) {
                        parameters.addAll(Arrays.asList("-l", ocrOptionsController.selectedLanguages));
                    }
                    File configFile = FileTools.getTempFile();
                    String s = "tessedit_create_txt 1\n";
                    Map<String, String> p = ocrOptionsController.checkParameters();
                    if (p != null) {
                        for (String key : p.keySet()) {
                            s += key + "\t" + p.get(key) + "\n";
                        }
                    }
                    FileTools.writeFile(configFile, s, Charset.forName("utf-8"));
                    parameters.add(configFile.getAbsolutePath());

                    ProcessBuilder pb = new ProcessBuilder(parameters).redirectErrorStream(true);
                    long start = new Date().getTime();
                    process = pb.start();
                    try ( BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = inReader.readLine()) != null) {
                            outputs += line + "\n";
                        }
                    } catch (Exception e) {
                        outputs += e.toString() + "\n";
                    }
                    process.waitFor();

                    String texts;
                    File txtFile = new File(fileBase + ".txt");
                    if (txtFile.exists()) {
                        texts = FileTools.readTexts(txtFile);
                        FileTools.delete(txtFile);
                    } else {
                        texts = null;
                    }
                    if (process != null) {
                        process.destroy();
                        process = null;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (loading != null) {
                                loading.closeStage();
                                loading = null;
                            }
                            if (texts != null) {
                                ocrArea.setText(texts);
                                resultLabel.setText(MessageFormat.format(message("OCRresults"),
                                        texts.length(), DateTools.datetimeMsDuration(new Date().getTime() - start)));
                                orcPage = currentPage;
                                tabPane.getSelectionModel().select(ocrTab);
                            } else {
                                if (outputs != null && !outputs.isBlank()) {
                                    alertError(outputs);
                                } else {
                                    popFailed();
                                }
                            }
                        }
                    });

                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (loading != null) {
                                loading.closeStage();
                                loading = null;
                            }
                        }
                    });
                }
            }
        }.start();
    }

    protected void embedded() {
        if (imageView.getImage() == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String texts;

                @Override
                protected boolean handle() {
                    try {
                        ITesseract instance = new Tesseract();
                        instance.setTessVariable("user_defined_dpi", "96");
                        instance.setTessVariable("debug_file", "/dev/null");
                        instance.setPageSegMode(ocrOptionsController.psm);
                        Map<String, String> p = ocrOptionsController.checkParameters();
                        if (p != null && !p.isEmpty()) {
                            for (String key : p.keySet()) {
                                instance.setTessVariable(key, p.get(key));
                            }
                        }
                        instance.setDatapath(ocrOptionsController.dataPathController.file.getAbsolutePath());
                        if (ocrOptionsController.selectedLanguages != null) {
                            instance.setLanguage(ocrOptionsController.selectedLanguages);
                        }
                        Image selected = cropImage();
                        if (selected == null) {
                            selected = imageView.getImage();
                        }
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(selected, null);
                        bufferedImage = ImageManufacture.removeAlpha(bufferedImage);
                        if (task == null || isCancelled() || bufferedImage == null) {
                            return false;
                        }
                        texts = instance.doOCR(bufferedImage);
                        return texts != null;
                    } catch (Exception e) {
                        error = e.toString();
                        MyBoxLog.debug(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (texts != null) {
                        ocrArea.setText(texts);
                        resultLabel.setText(MessageFormat.format(message("OCRresults"),
                                texts.length(), DateTools.datetimeMsDuration(new Date().getTime() - startTime.getTime())));
                        orcPage = currentPage;
                        tabPane.getSelectionModel().select(ocrTab);
                    } else {
                        popText(message("OCRMissComments"), 5000, "white", "1.1em", null);
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @FXML
    public void editOCR() {
        if (ocrArea.getText().isEmpty()) {
            return;
        }
        TextEditerController controller = (TextEditerController) FxmlStage.openStage(CommonValues.TextEditerFxml);
        controller.hideRightPane();
        controller.mainArea.setText(ocrArea.getText());
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (outlineTask != null && outlineTask.isRunning()) {
            outlineTask.cancel();
            outlineTask = null;
        }
        if (thumbTask != null && thumbTask.isRunning()) {
            thumbTask.cancel();
            thumbTask = null;
        }
        if (process != null) {
            process.destroy();
            process = null;
        }
        if (loading != null) {
            loading.closeStage();
            loading = null;
        }
        return true;
    }

}
