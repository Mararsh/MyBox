package mara.mybox.controller;

import com.github.jaiimageio.impl.plugins.gif.GIFImageMetadata;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.bufferedimage.ImageConvertTools;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.imagefile.ImageGifFile;
import mara.mybox.imagefile.ImageTiffFile;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.poi.hslf.usermodel.HSLFPictureShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;

/**
 * @Author Mara
 * @CreateDate 2021-5-27
 * @License Apache License Version 2.0
 */
public class ControlImagesSave extends BaseController {

    protected BaseImagesListController listController;
    protected int digit;
    protected String imagesFormat;
    protected int pptWidth, pptHeight, pptMargin, savedWidth;
    protected ObservableList<ImageInformation> imageInfos;
    protected LoadingController loading;

    @FXML
    protected TitledPane gifPane, pdfPane, pptPane, convertPane;
    @FXML
    protected ToggleGroup saveGroup;
    @FXML
    protected RadioButton imagesRadio, spliceRadio, pdfRadio, pptRadio,
            tifFileRadio, gifFileRadio, videoRadio;
    @FXML
    protected Label saveImagesLabel;
    @FXML
    protected ControlImageFormat formatController;
    @FXML
    protected TextField pptWidthInput, pptHeightInput, pptMarginInput;
    @FXML
    protected CheckBox gifLoopCheck, pptMarginCheck;
    @FXML
    protected ControlPdfWriteOptions pdfOptionsController;
    @FXML
    protected Button editFramesButton;
    @FXML
    protected ComboBox<String> savedWidthSelector;
    @FXML
    protected HBox savedWidthBox;

    public ControlImagesSave() {
        baseTitle = message("ImagesEditor");
        TipsLabelKey = "ImagesEditorTips";
    }

    @Override
    public void setFileType() {
        setFileType(FileType.Image);
    }

    public void setParent(BaseImagesListController parent) {
        try {
            baseName = parent.baseName;
            parentController = parent;
            listController = parent;

            initSavePane();
            initGifPane();
            initPptPane();
            formatController.setParameters(this, false);
            pdfOptionsController.set(baseName, true);

            imageInfos = listController.imageInfos;
            imageInfos.addListener(new ListChangeListener<ImageInformation>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends ImageInformation> change) {
                    imageInfosChanged();
                }
            });
            imageInfosChanged();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void imageInfosChanged() {
        editFramesButton.setDisable(imageInfos.isEmpty());
        saveAsButton.setDisable(imageInfos.isEmpty());
    }

    public void initSavePane() {
        try {
            saveGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkSaveType();
                }
            });
            checkSaveType();

            savedWidth = UserConfig.getInt(baseName + "SavedWidth", -1);
            List<String> values = Arrays.asList(message("OriginalSize"),
                    "512", "1024", "256", "128", "2048", "100", "80", "4096");
            savedWidthSelector.getItems().addAll(values);
            if (savedWidth <= 0) {
                savedWidthSelector.getSelectionModel().select(0);
            } else {
                savedWidthSelector.setValue(savedWidth + "");
            }
            savedWidthSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (message("OriginalSize").equals(newValue)) {
                        savedWidth = -1;
                    } else {
                        try {
                            savedWidth = Integer.parseInt(newValue);
                            ValidationTools.setEditorNormal(savedWidthSelector);
                        } catch (Exception e) {
                            ValidationTools.setEditorBadStyle(savedWidthSelector);
                            return;
                        }
                    }
                    UserConfig.setInt(baseName + "SavedWidth", savedWidth);
                }
            });
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void checkSaveType() {
        gifPane.setExpanded(false);
        pdfPane.setExpanded(false);
        pptPane.setExpanded(false);
        convertPane.setExpanded(false);
        saveImagesLabel.setVisible(false);
        if (imagesRadio.isSelected()) {
            setTargetFileType(VisitHistory.FileType.Image);
            saveImagesLabel.setVisible(true);
            convertPane.setExpanded(true);
        } else if (spliceRadio.isSelected()) {
            setTargetFileType(VisitHistory.FileType.Image);
        } else if (tifFileRadio.isSelected()) {
            setTargetFileType(VisitHistory.FileType.Tif);
        } else if (gifFileRadio.isSelected()) {
            setTargetFileType(VisitHistory.FileType.Gif);
            gifPane.setExpanded(true);
        } else if (pdfRadio.isSelected()) {
            setTargetFileType(VisitHistory.FileType.PDF);
            pdfPane.setExpanded(true);
        } else if (pptRadio.isSelected()) {
            setTargetFileType(VisitHistory.FileType.PPT);
            pptPane.setExpanded(true);
        }
        savedWidthBox.setDisable(spliceRadio.isSelected() || videoRadio.isSelected());
    }

    public void initGifPane() {
        try {
            gifLoopCheck.setSelected(UserConfig.getBoolean(baseName + "GifLoop", true));
            gifLoopCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                        UserConfig.setBoolean(baseName + "GifLoop", gifLoopCheck.isSelected());
                    });
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void initPptPane() {
        try {
            pptWidth = UserConfig.getInt(baseName + "PptWidth", 1024);
            pptWidth = pptWidth <= 0 ? 1024 : pptWidth;
            pptWidthInput.setText(pptWidth + "");
            pptWidthInput.textProperty().addListener(
                    (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        checkPptWidth();
                    });

            pptHeight = UserConfig.getInt(baseName + "PptHeight", 768);
            pptHeight = pptHeight <= 0 ? 768 : pptHeight;
            pptHeightInput.setText(pptHeight + "");
            pptHeightInput.textProperty().addListener(
                    (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        checkPptHeight();
                    });

            pptMargin = UserConfig.getInt(baseName + "PptMargin", 20);
            pptMargin = pptHeight <= 0 ? 20 : pptMargin;
            pptMarginInput.setText(pptMargin + "");
            pptMarginInput.textProperty().addListener(
                    (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        checkPptMargin();
                    });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void checkPptWidth() {
        try {
            int v = Integer.parseInt(pptWidthInput.getText());
            if (v > 0) {
                pptWidth = v;
                pptWidthInput.setStyle(null);
                UserConfig.setInt(baseName + "PptWidth", v);
            } else {
                pptWidthInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            pptWidthInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkPptHeight() {
        try {
            int v = Integer.parseInt(pptHeightInput.getText());
            if (v > 0) {
                pptHeight = v;
                pptHeightInput.setStyle(null);
                UserConfig.setInt(baseName + "PptHeight", v);
            } else {
                pptHeightInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            pptHeightInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkPptMargin() {
        try {
            int v = Integer.parseInt(pptMarginInput.getText());
            if (v >= 0) {
                pptMargin = v;
                pptMarginInput.setStyle(null);
                UserConfig.setInt(baseName + "PptMargin", v);
            } else {
                pptMarginInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            pptMarginInput.setStyle(UserConfig.badStyle());
        }
    }

    @FXML
    public void pptMaxSize() {
        if (imageInfos == null || imageInfos.isEmpty()) {
            parentController.popError(message("NoData"));
            return;
        }
        int maxW = 0, maxH = 0;
        for (ImageInformation info : imageInfos) {
            DoubleRectangle region = info.getRegion();
            if (region != null) {
                if (region.getWidth() > maxW) {
                    maxW = (int) region.getWidth();
                }
                if (region.getHeight() > maxH) {
                    maxH = (int) region.getHeight();
                }
            } else {
                if (info.getWidth() > maxW) {
                    maxW = (int) info.getWidth();
                }
                if (info.getHeight() > maxH) {
                    maxH = (int) info.getHeight();
                }
            }
        }
        if (pptMarginCheck.isSelected()) {
            maxW += pptMargin * 2;
            maxH += pptMargin * 2;
        }
        if (maxW != pptWidth) {
            pptWidthInput.setText(maxW + "");
        }
        if (maxH != pptHeight) {
            pptHeightInput.setText(maxH + "");
        }
    }

    @FXML
    @Override
    public void pickSaveAs(Event event) {
        if (spliceRadio.isSelected() || videoRadio.isSelected()) {
            saveAsAction();
            return;
        }
        super.pickSaveAs(event);
    }

    @FXML
    @Override
    public void popSaveAs(Event event) {
        if (spliceRadio.isSelected() || videoRadio.isSelected()) {
            return;
        }
        super.popSaveAs(event);
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (imageInfos == null || imageInfos.isEmpty()) {
            parentController.popError(message("NoData"));
            return;
        }
        digit = (imageInfos.size() + "").length();

        if (imagesRadio.isSelected()) {
            imagesFormat = formatController.attributes.getImageFormat();
            targetFile = chooseSaveFile(defaultTargetPath(TargetFileType),
                    DateTools.nowFileString() + "." + imagesFormat, FileFilters.imageFilter(imagesFormat));
            if (targetFile == null) {
                return;
            }
            saveAsImages();

        } else if (spliceRadio.isSelected()) {
            saveAsSplice();

        } else if (tifFileRadio.isSelected()) {
            targetFile = chooseSaveFile();
            if (targetFile == null) {
                return;
            }
            saveAsTiff();

        } else if (gifFileRadio.isSelected()) {
            targetFile = chooseSaveFile();
            if (targetFile == null) {
                return;
            }
            saveAsGif();

        } else if (pdfRadio.isSelected()) {
            targetFile = chooseSaveFile();
            if (targetFile == null) {
                return;
            }
            saveAsPdf();

        } else if (pptRadio.isSelected()) {
            targetFile = chooseSaveFile();
            if (targetFile == null) {
                return;
            }
            saveAsPPT();

        } else if (videoRadio.isSelected()) {
            saveAsVideo();

        }
    }

    protected BufferedImage image(FxTask currentTask, int index) {
        try {
            if (imageInfos == null || index < 0 || index >= imageInfos.size()) {
                return null;
            }
            ImageInformation info = imageInfos.get(index);
            if (info == null) {
                return null;
            }
            Image image = info.loadThumbnail(currentTask, savedWidth);
            if (image == null) {
                return null;
            }
            return SwingFXUtils.fromFXImage(image, null);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    protected void updateLabel(String msg, int number) {
        if (task == null || task.isQuit() || loading == null) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                loading.setInfo(msg);
                loading.setProgress(number * 1f / imageInfos.size());
            }
        });
    }

    protected void saveAsImages() {
        if (targetFile == null || imageInfos == null || imageInfos.isEmpty()) {
            return;
        }
        if (task != null) {
            task.cancel();
            loading = null;
        }
        if (imagesFormat == null) {
            parentController.popError(message("InvalidParameters"));
            return;
        }
        task = new FxSingletonTask<Void>(this) {
            List<String> fileNames;

            @Override
            protected boolean handle() {
                fileNames = new ArrayList<>();
                try {
                    String imagesFilePrefix = targetFile.getParent() + File.separator + FileNameTools.prefix(targetFile.getName());
                    for (int i = 0; i < imageInfos.size(); ++i) {
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        BufferedImage bufferedImage = image(this, i);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        if (bufferedImage == null) {
                            continue;
                        }
                        String filename = imagesFilePrefix + "-" + StringTools.fillLeftZero(i, digit) + "." + imagesFormat;
                        BufferedImage converted = ImageConvertTools.convertColorSpace(this,
                                bufferedImage, formatController.attributes);
                        ImageFileWriters.writeImageFile(this,
                                converted, formatController.attributes, filename);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        fileNames.add(filename);
                        String msg = MessageFormat.format(message("NumberFileGenerated"),
                                (i + 1) + "/" + imageInfos.size(), "\"" + filename + "\"");
                        updateLabel(msg, i + 1);
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
                return !fileNames.isEmpty();
            }

            @Override
            protected void whenSucceeded() {
                recordFileWritten(targetFile.getParent());
                multipleFilesGenerated(fileNames);
            }

        };
        loading = start(task);
    }

    protected void saveAsSplice() {
        if (imageInfos == null || imageInfos.isEmpty()) {
            return;
        }
        List<ImageInformation> infos = new ArrayList<>();
        for (int i = 0; i < imageInfos.size(); ++i) {
            ImageInformation info = imageInfos.get(i).cloneAttributes();
            infos.add(info);
        }
        ImagesSpliceController.open(infos);
    }

    protected void saveAsVideo() {
        if (imageInfos == null || imageInfos.isEmpty()) {
            return;
        }
        List<ImageInformation> infos = new ArrayList<>();
        for (int i = 0; i < imageInfos.size(); ++i) {
            ImageInformation info = imageInfos.get(i).cloneAttributes();
            infos.add(info);
        }
        FFmpegMergeImagesController.open(infos);
    }

    protected void saveAsPdf() {
        if (targetFile == null || imageInfos == null || imageInfos.isEmpty()) {
            return;
        }
        if (task != null) {
            task.cancel();
            loading = null;
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                File tmpFile = FileTmpTools.getTempFile();
                try (PDDocument document = new PDDocument(AppVariables.PdfMemUsage)) {
                    PDDocumentInformation info = new PDDocumentInformation();
                    info.setCreationDate(Calendar.getInstance());
                    info.setModificationDate(Calendar.getInstance());
                    info.setProducer("MyBox v" + AppValues.AppVersion);
                    info.setAuthor(pdfOptionsController.authorInput.getText());
                    document.setDocumentInformation(info);
                    document.setVersion(1.0f);

                    int count = 0;
                    for (int i = 0; i < imageInfos.size(); ++i) {
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        BufferedImage bufferedImage = image(this, i);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        String sourceFormat = imageInfos.get(i).getImageFormat();
                        PdfTools.writePage(this,
                                document, sourceFormat, bufferedImage, ++count,
                                imageInfos.size(), pdfOptionsController);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        String msg = MessageFormat.format(message("NumberPageWritten"), (i + 1) + "/" + imageInfos.size());
                        updateLabel(msg, i + 1);
                    }

                    PDPage page = document.getPage(0);
                    PDPageXYZDestination dest = new PDPageXYZDestination();
                    dest.setPage(page);
                    dest.setZoom(pdfOptionsController.zoom / 100.0f);
                    dest.setTop((int) page.getCropBox().getHeight());
                    PDActionGoTo action = new PDActionGoTo();
                    action.setDestination(dest);
                    document.getDocumentCatalog().setOpenAction(action);

                    document.save(tmpFile);
                    document.close();

                    return FileTools.override(tmpFile, targetFile);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                parentController.popSuccessful();
                recordFileWritten(targetFile);
                PdfViewController.open(targetFile);
            }

        };
        loading = start(task);
    }

    protected void saveAsTiff() {
        if (targetFile == null || imageInfos == null || imageInfos.isEmpty()) {
            return;
        }
        if (task != null) {
            task.cancel();
            loading = null;
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                System.gc();
                File tmpFile = FileTmpTools.getTempFile();
                try (ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                    ImageWriter writer = ImageTiffFile.getWriter();
                    writer.setOutput(out);
                    writer.prepareWriteSequence(null);
                    ImageWriteParam param = ImageTiffFile.getPara(null, writer);
                    for (int i = 0; i < imageInfos.size(); ++i) {
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        BufferedImage bufferedImage = image(this, i);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        IIOMetadata metaData = ImageTiffFile.getWriterMeta(null, bufferedImage, writer, param);
                        writer.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        String msg = MessageFormat.format(message("NumberImageWritten"), (i + 1) + "/" + imageInfos.size());
                        updateLabel(msg, i + 1);
                    }
                    writer.endWriteSequence();
                    writer.dispose();
                    out.flush();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return FileTools.override(tmpFile, targetFile);

            }

            @Override
            protected void whenSucceeded() {
                parentController.popSuccessful();
                recordFileWritten(targetFile);
                ImageEditorController.openFile(targetFile);
            }

        };
        loading = start(task);
    }

    protected void saveAsGif() {
        if (targetFile == null || imageInfos == null || imageInfos.isEmpty()) {
            return;
        }
        if (task != null) {
            task.cancel();
            loading = null;
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                System.gc();
                File tmpFile = FileTmpTools.getTempFile();
                try (ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                    ImageWriter gifWriter = ImageGifFile.getWriter();
                    ImageWriteParam param = gifWriter.getDefaultWriteParam();
                    GIFImageMetadata metaData = (GIFImageMetadata) gifWriter.getDefaultImageMetadata(
                            ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB), param);
                    gifWriter.setOutput(out);
                    gifWriter.prepareWriteSequence(null);
                    for (int i = 0; i < imageInfos.size(); ++i) {
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        BufferedImage bufferedImage = image(this, i);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        ImageGifFile.getParaMeta(imageInfos.get(i).getDuration(), gifLoopCheck.isSelected(), param, metaData);
                        gifWriter.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        String msg = MessageFormat.format(message("NumberImageWritten"), (i + 1) + "/" + imageInfos.size());
                        updateLabel(msg, i + 1);
                    }
                    gifWriter.endWriteSequence();
                    gifWriter.dispose();
                    out.flush();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return FileTools.override(tmpFile, targetFile);

            }

            @Override
            protected void whenSucceeded() {
                parentController.popSuccessful();
                recordFileWritten(targetFile);
                ImageEditorController.openFile(targetFile);
            }

        };
        loading = start(task);
    }

    protected void saveAsPPT() {
        if (targetFile == null || imageInfos == null || imageInfos.isEmpty()) {
            return;
        }
        if (task != null) {
            task.cancel();
            loading = null;
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                System.gc();
                File tmpFile = FileTmpTools.getTempFile();
                try (HSLFSlideShow ppt = new HSLFSlideShow()) {
                    for (int i = 0; i < imageInfos.size(); ++i) {
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        ppt.setPageSize(new java.awt.Dimension(pptWidth, pptHeight));
                        BufferedImage image = ImageConvertTools.convertToPNG(image(this, i));
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        HSLFPictureShape shape = MicrosoftDocumentTools.imageShape(this, ppt, image, "png");
                        if (shape == null || !isWorking()) {
                            return false;
                        }
                        shape.setAnchor(new java.awt.Rectangle(pptMargin, pptMargin, image.getWidth(), image.getHeight()));
                        HSLFSlide slide = ppt.createSlide();
                        slide.addShape(shape);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        String msg = MessageFormat.format(message("NumberImageWritten"), (i + 1) + "/" + imageInfos.size());
                        updateLabel(msg, i + 1);
                    }
                    ppt.write(tmpFile);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
                return FileTools.override(tmpFile, targetFile);
            }

            @Override
            protected void whenSucceeded() {
                parentController.popSuccessful();
                recordFileWritten(targetFile);
                PptViewController controller = (PptViewController) openStage(Fxmls.PptViewFxml);
                controller.sourceFileChanged(targetFile);
                controller.requestMouse();
            }

        };
        loading = start(task);
    }

    @FXML
    public void editFrames() {
        ImagesEditorController controller = (ImagesEditorController) openStage(Fxmls.ImagesEditorFxml);
        controller.loadImages(imageInfos);
    }

}
