package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
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
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.PdfInformation;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageManufacture;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
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
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfViewController extends ImageViewerController {

    private PdfInformation pdfInformation;
    private int currentPage, currentPageTmp, percent, dpi;
    protected SimpleBooleanProperty infoLoaded;
    private boolean isTransparent, scrollEnd, scrollStart, scrolledSet;
    private Task outlineTask, thumbTask;
    private String password;

    @FXML
    protected Label pageLabel;
    @FXML
    protected TextField pageInput;
    @FXML
    protected ComboBox<String> sizeBox, dpiBox;
    @FXML
    protected CheckBox transCheck, outlineCheck, thumbCheck;
    @FXML
    protected HBox pageNavBox;
    @FXML
    protected SplitPane viewPane;
    @FXML
    protected ScrollPane thumbScrollPane, outlineScrollPane;
    @FXML
    protected VBox thumbBox;
    @FXML
    protected TreeView outlineTree;

    public PdfViewController() {
        baseTitle = AppVaribles.message("PdfView");

        SourceFileType = VisitHistory.FileType.PDF;
        SourcePathType = VisitHistory.FileType.PDF;
        TargetFileType = VisitHistory.FileType.Image;
        TargetPathType = VisitHistory.FileType.Image;

        sourcePathKey = "PdfSourcePath";
        TipsLabelKey = "PdfViewTips";
        ImageSelectKey = "PdfViewSelectKey";
        ImageRulerXKey = "PdfViewRulerXKey";
        ImageRulerYKey = "PdfViewRulerYKey";
        ImagePopCooridnateKey = "PdfViewPopCooridnateKey";

        sourceExtensionFilter = CommonValues.PdfExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initializeNext2() {
        try {
            infoLoaded = new SimpleBooleanProperty(false);

            operation1Box.disableProperty().bind(Bindings.not(infoLoaded));
            pageNavBox.disableProperty().bind(Bindings.not(infoLoaded));

            transCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    isTransparent = transCheck.isSelected();
                    loadPage();
                }
            });
            FxmlControl.setTooltip(transCheck, new Tooltip(AppVaribles.message("OnlyForTexts")));

            sizeBox.getItems().addAll(Arrays.asList("100", "75", "50", "125", "150", "200", "80", "25", "30", "15"));
            sizeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            percent = v;
                            setSize(percent);
                            FxmlControl.setEditorNormal(sizeBox);
                        } else {
                            FxmlControl.setEditorBadStyle(sizeBox);
                        }

                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(sizeBox);
                    }
                }
            });

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

            pageInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (pageNavBox.isDisabled()) {
                        currentPageTmp = 0;
                        pageInput.setStyle(null);
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

            operation3Box.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );

            viewPane.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );

            initViewPane();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initOperation2Box() {

        operation2Box.disableProperty().bind(
                Bindings.isNull(imageView.imageProperty())
        );

    }

    protected void initViewPane() {
        outlineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                checkOutline();
            }
        });
        outlineCheck.setSelected(true);

        thumbCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                checkThumbs();
            }
        });
        thumbCheck.setSelected(false);

        thumbScrollPane.vvalueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                loadThumbs();
            }
        });

    }

    private void checkOutline() {
        if (outlineCheck.isSelected()) {
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

    private void checkThumbs() {
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

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        FxmlControl.setTooltip(nextButton, new Tooltip(message("NextPage") + "\nENTER / PAGE DOWN"));
        FxmlControl.setTooltip(previousButton, new Tooltip(message("PreviousPage") + "\nPAGE UP"));
        FxmlControl.setTooltip(firstButton, new Tooltip(message("FirstPage") + "\nCTRL+HOME"));
        FxmlControl.setTooltip(lastButton, new Tooltip(message("LastPage") + "\nCTRL+END"));
    }

    private void setSize(int percent) {
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
            infoLoaded.set(false);
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
//        bottomLabel.setText(AppVaribles.getMessage("CountingTotalNumber"));
        backgroundTask = new Task<Void>() {
            private boolean ok, pop;

            @Override
            protected Void call() throws Exception {
                try {
                    try (PDDocument doc = PDDocument.load(sourceFile, inPassword, AppVaribles.pdfMemUsage)) {
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
                            dialog.setContentText(AppVaribles.message("Password"));
                            Optional<String> result = dialog.showAndWait();
                            if (result.isPresent()) {
                                loadInformation(result.get());
                            }
                        }
                    });
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (!ok) {
                    return;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        bottomLabel.setText("");
                        pageLabel.setText("/" + pdfInformation.getNumberOfPages());
                        loadPage();
                        checkOutline();
                        checkThumbs();
                    }
                });
            }
        };
        Thread thread = new Thread(backgroundTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadPage(int pageNumber) {
        currentPage = pageNumber;
        loadPage();
    }

    private void loadPage() {
        if (pdfInformation == null) {
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
        task = new Task<Void>() {
            private boolean ok;

            @Override
            protected Void call() throws Exception {
                ImageType type = ImageType.RGB;
                if (isTransparent) {
                    type = ImageType.ARGB;
                }
                BufferedImage bufferedImage = PdfTools.page2image(sourceFile, password, currentPage, dpi, type);
                image = SwingFXUtils.toFXImage(bufferedImage, null);

                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setPreserveRatio(true);
                            imageView.setImage(image);
                            if (percent == 0) {
                                paneSize();
                            } else {
                                setSize(percent);
                            }
                            refinePane();
                            setMaskStroke();
                            checkSelect();
                            if (!infoLoaded.get()) {
                                loadInformation(null);
                            }
                            setImageChanged(false);
                            imageView.requestFocus();
                        }
                    });
                }
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadOutline() {
        if (!infoLoaded.get() || outlineTree.getRoot() != null) {
            return;
        }
        outlineTask = new Task<Void>() {
            private PDDocument doc;

            @Override
            protected Void call() throws Exception {
                try {
                    doc = PDDocument.load(sourceFile, password, AppVaribles.pdfMemUsage);
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (doc != null) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                PDDocumentOutline outline = doc.getDocumentCatalog().getDocumentOutline();
                                TreeItem outlineRoot = new TreeItem<>(AppVaribles.message("PDFOutline"));
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
                }
            }
        };
        openHandlingStage(outlineTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(outlineTask);
        thread.setDaemon(true);
        thread.start();

    }

    private void loadOutlineItem(PDOutlineNode parentOutlineItem, TreeItem parentTreeItem) {
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

    private void loadThumbs() {
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
        thumbTask = new Task<Void>() {
            private boolean ok;
            private Map<Integer, Image> images;
            private int end;

            @Override
            protected Void call() throws Exception {
                try {
                    try (PDDocument doc = PDDocument.load(sourceFile, password, AppVaribles.pdfMemUsage)) {
                        PDFRenderer renderer = new PDFRenderer(doc);
                        images = new HashMap();
                        end = Math.min(pos + 20, pdfInformation.getNumberOfPages());
                        for (int i = pos; i < end; i++) {
                            ImageView view = (ImageView) thumbBox.getChildren().get(2 * i);
                            if (view.getImage() != null) {
                                continue;
                            }
                            BufferedImage bufferedImage = renderer.renderImageWithDPI(i, 20, ImageType.RGB);
                            if (bufferedImage.getWidth() > 200) {
                                bufferedImage = ImageManufacture.scaleImageWidthKeep(bufferedImage, 200);
                            }
                            Image thumb = SwingFXUtils.toFXImage(bufferedImage, null);
                            images.put(i, thumb);
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
                }
            }
        };
        openHandlingStage(thumbTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(thumbTask);
        thread.setDaemon(true);
        thread.start();

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
        sizeBox.getSelectionModel().select(percent + "");
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
