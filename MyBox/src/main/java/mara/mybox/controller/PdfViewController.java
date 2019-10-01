package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
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
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.PdfInformation;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.image.ImageManufacture;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonImageValues;
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
    protected int currentPage, currentPageTmp, percent, dpi, orcPage;
    protected SimpleBooleanProperty infoLoaded;
    protected boolean isTransparent, scrollEnd, scrollStart, scrolledSet;
    protected Task outlineTask, thumbTask;
    protected String password;
    protected String selectedLanguages;

    @FXML
    protected Label pageLabel;
    @FXML
    protected TextField pageInput;
    @FXML
    protected ComboBox<String> percentBox, dpiBox;
    @FXML
    protected CheckBox transparentBackgroundCheck, bookmarksCheck, thumbCheck;
    @FXML
    protected SplitPane viewPane;
    @FXML
    protected ScrollPane thumbScrollPane, outlineScrollPane;
    @FXML
    protected VBox thumbBox;
    @FXML
    protected TreeView outlineTree;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab imageTab, ocrTab;
    @FXML
    protected TextArea ocrArea;
    @FXML
    protected Label setOCRLabel, resultLabel, currentOCRFilesLabel;
    @FXML
    protected ComboBox<String> langSelector;

    public PdfViewController() {
        baseTitle = AppVariables.message("PdfView");

        SourceFileType = VisitHistory.FileType.PDF;
        SourcePathType = VisitHistory.FileType.PDF;
        TargetFileType = VisitHistory.FileType.Image;
        TargetPathType = VisitHistory.FileType.Image;

        sourcePathKey = "PdfFilePath";
        TipsLabelKey = "PdfViewTips";

        sourceExtensionFilter = CommonImageValues.PdfExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            infoLoaded = new SimpleBooleanProperty(false);

            percentBox.getItems().addAll(Arrays.asList("100", "75", "50", "125", "150", "200", "80", "25", "30", "15"));
            percentBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
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

            pageInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.valueOf(pageInput.getText()) - 1;
                        if (v >= 0 && v < pdfInformation.getNumberOfPages()) {
                            currentPageTmp = v;
                            pageInput.setStyle(null);
                            goButton.setDisable(false);
                        } else {
                            pageInput.setStyle(badStyle);
                            goButton.setDisable(true);
                        }
                    } catch (Exception e) {
                        pageInput.setStyle(badStyle);
                        goButton.setDisable(true);
                    }
                }
            });

            bookmarksCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOutline();
                    AppVariables.setUserConfigValue("PDFBookmarks", bookmarksCheck.isSelected());
                }
            });
            bookmarksCheck.setSelected(AppVariables.getUserConfigBoolean("PDFBookmarks", true));

            thumbCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkThumbs();
                    AppVariables.setUserConfigValue("PDFThumbnails", thumbCheck.isSelected());
                }
            });
            thumbCheck.setSelected(AppVariables.getUserConfigBoolean("PDFThumbnails", false));

            thumbScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    loadThumbs();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void initializeNext2() {
        try {
            operation1Box.disableProperty().bind(Bindings.not(infoLoaded));

            transparentBackgroundCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    isTransparent = transparentBackgroundCheck.isSelected();
                    loadPage();
                }
            });
            FxmlControl.setTooltip(transparentBackgroundCheck, new Tooltip(AppVariables.message("OnlyForTexts")));

            dpiBox.getItems().addAll(Arrays.asList("96", "72", "120", "160", "240", "300", "400", "600"));
            dpiBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
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

            scrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    if (scrolledSet) {
                        scrolledSet = false;
                        return;
                    }
                    if (scrollPane.getVvalue() == scrollPane.getVmax()) {
                        if (scrollEnd) {
                            scrollEnd = false;
                            nextAction();
                        } else {
                            scrollEnd = true;
                            scrolledSet = true;
                            scrollPane.setVvalue(0.99);
                        }
                    } else {
                        scrollEnd = false;
                    }

                    if (scrollPane.getVvalue() == scrollPane.getVmin()) {
                        if (scrollStart) {
                            scrollStart = false;
                            previousAction();
                        } else {
                            scrollStart = true;
                            scrolledSet = true;
                            scrollPane.setVvalue(0.01);
                        }
                    } else {
                        scrollStart = false;
                    }
                }
            });

            viewPane.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );

            tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                @Override
                public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                    if (!ocrTab.equals(newValue) || orcPage == currentPage
                            || imageView.getImage() == null) {
                        return;
                    }
                    startOCR();
                }
            });

            checkLanguages();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkLanguages() {
        selectedLanguages = AppVariables.getUserConfigValue("ImageOCRLanguages", null);
        if (selectedLanguages != null && !selectedLanguages.isEmpty()) {
            currentOCRFilesLabel.setText(
                    MessageFormat.format(message("CurrentDataFiles"), selectedLanguages));
        } else {
            currentOCRFilesLabel.setText(
                    MessageFormat.format(message("CurrentDataFiles"), ""));
        }
    }

    @FXML
    public void startOCR() {
        checkLanguages();
        if (imageView.getImage() == null
                || selectedLanguages == null || selectedLanguages.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String result;
                private long cost;

                @Override
                protected boolean handle() {
                    try {
                        cost = new Date().getTime();
                        ITesseract instance = new Tesseract();
                        String path = AppVariables.getUserConfigValue("TessDataPath", null);
                        if (path != null) {
                            instance.setDatapath(path);
                        }
                        if (selectedLanguages != null) {
                            instance.setLanguage(selectedLanguages);
                        }

                        Image selected = cropImage();
                        if (selected == null) {
                            selected = imageView.getImage();
                        }
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(selected, null);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        result = instance.doOCR(bufferedImage);
                        cost = new Date().getTime() - cost;
                        return result != null;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (result.length() == 0) {
                        popText(message("OCRMissComments"), 5000, "white", "1.1em", null);
                    }
                    ocrArea.setText(result);
                    resultLabel.setText(MessageFormat.format(message("OCRresults"), result.length(), cost));
                    orcPage = currentPage;
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @FXML
    public void settingsAction() {
        SettingsController controller = (SettingsController) openStage(CommonValues.SettingsFxml);
        controller.setParentController(this);
        controller.setParentFxml(myFxml);
        controller.tabPane.getSelectionModel().select(controller.ocrTab);
    }

    @Override
    public void moreAction() {
        if (moreButton == null || contentBox == null) {
            return;
        }
        if (moreButton.isSelected()) {
            if (!contentBox.getChildren().contains(operation2Box)) {
                contentBox.getChildren().add(1, operation2Box);
            }

        } else {
            if (contentBox.getChildren().contains(operation2Box)) {
                contentBox.getChildren().remove(operation2Box);
            }

        }
        FxmlControl.refreshStyle(contentBox);
    }

    protected void checkOutline() {
        if (!infoLoaded.get()) {
            return;
        }
        if (bookmarksCheck.isSelected()) {
            if (!viewPane.getItems().contains(outlineScrollPane)) {
                viewPane.getItems().add(viewPane.getItems().size() - 1, outlineScrollPane);
            }
            loadOutline();

        } else {
            if (viewPane.getItems().contains(outlineScrollPane)) {
                viewPane.getItems().remove(outlineScrollPane);
            }
        }
        adjustSplitPane();

    }

    protected void checkThumbs() {
        if (!infoLoaded.get()) {
            return;
        }
        if (thumbCheck.isSelected()) {
            if (!viewPane.getItems().contains(thumbScrollPane)) {
                viewPane.getItems().add(0, thumbScrollPane);
            }
            loadThumbs();

        } else {
            if (viewPane.getItems().contains(thumbScrollPane)) {
                viewPane.getItems().remove(thumbScrollPane);
            }
        }
        adjustSplitPane();

    }

    @Override
    protected void adjustSplitPane() {
        try {
            int size = viewPane.getItems().size();
            switch (size) {
                case 1:
                    viewPane.setDividerPositions(1);
                    break;
                case 2:
                    viewPane.setDividerPosition(0, 0.3);
                    break;
                case 3:
                    viewPane.setDividerPosition(0, 0.2);
                    viewPane.setDividerPosition(1, 0.5);
                    break;
            }
            viewPane.layout();
        } catch (Exception e) {
            logger.error(e.toString());
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
            pageInput.setText("1");
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
            logger.debug(e.toString());
        }
    }

    public void loadInformation(final String inPassword) {
        if (pdfInformation == null) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {
                protected boolean pop;

                @Override
                protected boolean handle() {
                    try {
                        try (PDDocument doc = PDDocument.load(sourceFile, inPassword, AppVariables.pdfMemUsage)) {
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
                    pageLabel.setText("/" + pdfInformation.getNumberOfPages());
                    loadPage();
                    checkOutline();
                    checkThumbs();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL, message("LoadingFileInfo"));
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
            if (task != null) {
                return;
            }
            if (currentPage < 0) {
                currentPage = 0;
            } else if (infoLoaded.get() && currentPage >= pdfInformation.getNumberOfPages()) {
                currentPage = pdfInformation.getNumberOfPages() - 1;
            }
            pageInput.setText((currentPage + 1) + "");
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
            outlineTask = new Task<Void>() {
                protected PDDocument doc;

                @Override
                protected Void call() {
                    try {
                        doc = PDDocument.load(sourceFile, password, AppVariables.pdfMemUsage);
                    } catch (Exception e) {
                        logger.debug(e.toString());
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    outlineTask = null;
                    if (doc != null) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
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
                                    logger.debug(e.toString());
                                }
                            }
                        });
                    } else {
                        popFailed();
                    }
                }

                @Override
                protected void failed() {
                    super.failed();
                    outlineTask = null;
                }

                @Override
                protected void cancelled() {
                    super.cancelled();
                    outlineTask = null;
                }
            };
            openHandlingStage(outlineTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(outlineTask);
            thread.setDaemon(true);
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
            logger.debug(e.toString());
        }

    }

    protected void loadThumbs() {
        if (!infoLoaded.get()
                || (thumbTask != null && thumbTask.isRunning())) {
            return;
        }
        if (thumbBox.getChildren().isEmpty()) {
            for (int i = 0; i < pdfInformation.getNumberOfPages(); i++) {
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
            thumbTask = new Task<Void>() {
                protected boolean ok;
                protected Map<Integer, Image> images;
                protected int end;

                @Override
                protected Void call() {
                    try {
                        try (PDDocument doc = PDDocument.load(sourceFile, password, AppVariables.pdfMemUsage)) {
                            PDFRenderer renderer = new PDFRenderer(doc);
                            images = new HashMap<>();
                            end = Math.min(pos + 20, pdfInformation.getNumberOfPages());
                            for (int i = pos; i < end; i++) {
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
                                    logger.debug(e.toString());
                                }
                            }
                            doc.close();
                        }
                        ok = true;
                    } catch (Exception e) {
                        logger.debug(e.toString());
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    thumbTask = null;
                    if (ok && images != null) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = pos; i < end; i++) {
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
                        });
                    } else {
                        popFailed();
                    }
                }

                @Override
                protected void failed() {
                    super.failed();
                    thumbTask = null;
                }

                @Override
                protected void cancelled() {
                    super.cancelled();
                    thumbTask = null;
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
            logger.error(e.toString());
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

    @FXML
    @Override
    public void saveAction() {
        saveAsAction();
    }

    @Override
    public String saveAsPrefix() {
        if (sourceFile != null) {
            return FileTools.getFilePrefix(sourceFile.getName()) + "_p" + (currentPage + 1);
        } else {
            return "";
        }
    }

    @FXML
    public void popSaveOCR(MouseEvent event) { //
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistory.getRecentPath(VisitHistory.FileType.Text);
            }

            @Override
            public void handleSelect() {
                saveOCR();
            }

            @Override
            public void handleFile(String fname) {

            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                AppVariables.setUserConfigValue("TextFilePath", fname);
                handleSelect();
            }

        }.pop();
    }

    @FXML
    public void saveOCR() {
        if (ocrArea.getText().isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            String name = saveAsPrefix();
            final File file = chooseSaveFile(AppVariables.getUserConfigPath("TextFilePath"),
                    name, CommonImageValues.TextExtensionFilter, true);
            if (file == null) {
                return;
            }
            recordFileWritten(file);

            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    ok = FileTools.writeFile(file, ocrArea.getText()) != null;
                    return true;
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
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
        return true;
    }

}
