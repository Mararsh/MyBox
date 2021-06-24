package mara.mybox.controller;

import com.github.jaiimageio.impl.plugins.gif.GIFImageMetadata;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageConvert;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.image.file.ImageGifFile;
import mara.mybox.image.file.ImageTiffFile;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
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

    protected int digit;
    protected String imagesFormat;
    protected int gifWidth, pptWidth, pptHeight, pptMargin;
    protected boolean gifKeepSize;
    protected List<ImageInformation> images;
    protected LoadingController loading;

    @FXML
    protected TitledPane gifPane, pdfPane, pptPane, convertPane;
    @FXML
    protected ToggleGroup saveGroup;
    @FXML
    protected RadioButton imagesRadio, pdfRadio, pptRadio, tifFileRadio, gifFileRadio;
    @FXML
    protected Label saveImagesLabel;
    @FXML
    protected ControlImageFormat formatController;
    @FXML
    protected ToggleGroup gifSizeGroup;
    @FXML
    protected TextField gifWidthInput, pptWidthInput, pptHeightInput, pptMarginInput;
    @FXML
    protected CheckBox gifLoopCheck, pptMarginCheck;
    @FXML
    protected ControlPdfWriteOptions pdfOptionsController;

    public ControlImagesSave() {
        baseTitle = message("ImagesEditor");
        TipsLabelKey = "ImagesEditorTips";
    }

    @Override
    public void setFileType() {
        setFileType(FileType.Image);
    }

    public void setParameters(BaseController parent) {
        try {
            baseName = parent.baseName;
            parentController = parent;

            initSavePane();
            initGifPane();
            initPptPane();
            formatController.setParameters(this, false);
            pdfOptionsController.set(baseName, true);

            setImages(null);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setImages(List<ImageInformation> images) {
        this.images = images;
        saveButton.setDisable(images == null || images.isEmpty());
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
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkSaveType() {
        gifPane.setExpanded(false);
        pdfPane.setExpanded(false);
        pptPane.setExpanded(false);
        convertPane.setExpanded(false);
        if (imagesRadio.isSelected()) {
            setTargetFileType(VisitHistory.FileType.Image);
            saveImagesLabel.setVisible(true);
            convertPane.setExpanded(true);
        } else if (tifFileRadio.isSelected()) {
            setTargetFileType(VisitHistory.FileType.Tif);
            saveImagesLabel.setVisible(false);
        } else if (gifFileRadio.isSelected()) {
            setTargetFileType(VisitHistory.FileType.Gif);
            saveImagesLabel.setVisible(false);
            gifPane.setExpanded(true);
        } else if (pdfRadio.isSelected()) {
            setTargetFileType(VisitHistory.FileType.PDF);
            saveImagesLabel.setVisible(false);
            pdfPane.setExpanded(true);
        } else if (pptRadio.isSelected()) {
            setTargetFileType(VisitHistory.FileType.PPT);
            saveImagesLabel.setVisible(false);
            pptPane.setExpanded(true);
        }
    }

    public void initGifPane() {
        try {
            gifSizeGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) -> {
                        checkGifSizeType();
                    });

            gifWidth = AppVariables.getUserConfigInt(baseName + "GifWidth", 600);
            gifWidth = gifWidth <= 0 ? 600 : gifWidth;
            gifWidthInput.setText(gifWidth + "");
            gifWidthInput.textProperty().addListener(
                    (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        checkGifSize();
                    });

            checkGifSizeType();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkGifSizeType() {
        RadioButton button = (RadioButton) gifSizeGroup.getSelectedToggle();
        if (message("KeepImagesSize").equals(button.getText())) {
            gifKeepSize = true;
            gifWidthInput.setStyle(null);
        } else {
            gifKeepSize = false;
            checkGifSize();
        }
    }

    protected void checkGifSize() {
        try {
            int v = Integer.valueOf(gifWidthInput.getText());
            if (v > 0) {
                gifWidth = v;
                gifWidthInput.setStyle(null);
                AppVariables.setUserConfigInt(baseName + "GifWidth", v);
            } else {
                gifWidthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            gifWidthInput.setStyle(badStyle);
        }
    }

    public void initPptPane() {
        try {
            pptWidth = AppVariables.getUserConfigInt(baseName + "PptWidth", 1024);
            pptWidth = pptWidth <= 0 ? 1024 : pptWidth;
            pptWidthInput.setText(pptWidth + "");
            pptWidthInput.textProperty().addListener(
                    (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        checkPptWidth();
                    });

            pptHeight = AppVariables.getUserConfigInt(baseName + "PptHeight", 768);
            pptHeight = pptHeight <= 0 ? 768 : pptHeight;
            pptHeightInput.setText(pptHeight + "");
            pptHeightInput.textProperty().addListener(
                    (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        checkPptHeight();
                    });

            pptMargin = AppVariables.getUserConfigInt(baseName + "PptMargin", 20);
            pptMargin = pptHeight <= 0 ? 20 : pptMargin;
            pptMarginInput.setText(pptMargin + "");
            pptMarginInput.textProperty().addListener(
                    (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                        checkPptMargin();
                    });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checkPptWidth() {
        try {
            int v = Integer.valueOf(pptWidthInput.getText());
            if (v > 0) {
                pptWidth = v;
                pptWidthInput.setStyle(null);
                AppVariables.setUserConfigInt(baseName + "PptWidth", v);
            } else {
                pptWidthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            pptWidthInput.setStyle(badStyle);
        }
    }

    protected void checkPptHeight() {
        try {
            int v = Integer.valueOf(pptHeightInput.getText());
            if (v > 0) {
                pptHeight = v;
                pptHeightInput.setStyle(null);
                AppVariables.setUserConfigInt(baseName + "PptHeight", v);
            } else {
                pptHeightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            pptHeightInput.setStyle(badStyle);
        }
    }

    protected void checkPptMargin() {
        try {
            int v = Integer.valueOf(pptMarginInput.getText());
            if (v > 0) {
                pptMargin = v;
                pptMarginInput.setStyle(null);
                AppVariables.setUserConfigInt(baseName + "PptMargin", v);
            } else {
                pptMarginInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            pptMarginInput.setStyle(badStyle);
        }
    }

    @FXML
    public void pptMaxSize() {
        if (images == null || images.isEmpty()) {
            parentController.popError(message("NoData"));
            return;
        }
        int maxW = 0, maxH = 0;
        for (ImageInformation info : images) {
            if (info.getWidth() > maxW) {
                maxW = info.getWidth();
            }
            if (info.getHeight() > maxH) {
                maxH = info.getHeight();
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
    public void playAction() {

    }

    @FXML
    @Override
    public void saveAction() {
        saveAsAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (images == null || images.isEmpty()) {
            parentController.popError(message("NoData"));
            return;
        }
        digit = (images.size() + "").length();

        if (TargetFileType == VisitHistory.FileType.Image) {
            imagesFormat = formatController.attributes.getImageFormat();
            targetFile = chooseSaveFile(TargetFileType, new Date().getTime() + "." + imagesFormat);
            if (targetFile == null) {
                return;
            }
            saveAsImages();

        } else {
            targetFile = chooseSaveFile();
            if (targetFile == null) {
                return;
            }

            if (TargetFileType == VisitHistory.FileType.PDF) {

                saveAsPdf();

            } else if (TargetFileType == VisitHistory.FileType.Tif) {
                saveAsTiff();

            } else if (TargetFileType == VisitHistory.FileType.PPT) {
                saveAsPPT();

            } else if (TargetFileType == VisitHistory.FileType.Gif) {
                saveAsGif();
            }
        }

    }

    protected BufferedImage image(int index) {
        try {
            ImageInformation info = images.get(index);
            Image image;
            if (info.getRegion() != null) {
                image = info.loadRegion(-1);
            } else {
                image = info.loadImage();
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
                loading.setProgress(number * 1f / images.size());
            }
        });
    }

    protected void saveAsImages() {
        synchronized (this) {
            if (targetFile == null || images == null || images.isEmpty()) {
                return;
            }
            if (task != null && !task.isQuit()) {
                task.cancel();
                loading = null;
            }

            if (imagesFormat == null) {
                parentController.popError(message("InvalidParameters"));
                return;
            }
            task = new SingletonTask<Void>() {
                List<String> fileNames;

                @Override
                protected boolean handle() {
                    fileNames = new ArrayList<>();
                    try {
                        String imagesFilePrefix = targetFile.getParent() + File.separator + FileTools.getFilePrefix(targetFile.getName());
                        for (int i = 0; i < images.size(); ++i) {
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            BufferedImage bufferedImage = image(i);
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            String filename = imagesFilePrefix + "-" + StringTools.fillLeftZero(i, digit) + "." + imagesFormat;;
                            BufferedImage converted = ImageConvert.convertColorSpace(bufferedImage, formatController.attributes);
                            ImageFileWriters.writeImageFile(converted, formatController.attributes, filename);
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            fileNames.add(filename);
                            String msg = MessageFormat.format(AppVariables.message("NumberFileGenerated"),
                                    (i + 1) + "/" + images.size(), "\"" + filename + "\"");
                            updateLabel(msg, i + 1);
                        }
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                    }
                    return !fileNames.isEmpty();
                }

                @Override
                protected void whenSucceeded() {
                    recordFileWritten(targetFile.getParent());
                    multipleFilesGenerated(fileNames);
                }

            };
            loading = parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected void saveAsPdf() {
        synchronized (this) {
            if (targetFile == null || images == null || images.isEmpty()) {
                return;
            }
            if (task != null && !task.isQuit()) {
                task.cancel();
                loading = null;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    File tmpFile = FileTools.getTempFile();
                    try ( PDDocument document = new PDDocument(AppVariables.pdfMemUsage)) {
                        PDDocumentInformation info = new PDDocumentInformation();
                        info.setCreationDate(Calendar.getInstance());
                        info.setModificationDate(Calendar.getInstance());
                        info.setProducer("MyBox v" + CommonValues.AppVersion);
                        info.setAuthor(pdfOptionsController.authorInput.getText());
                        document.setDocumentInformation(info);
                        document.setVersion(1.0f);

                        int count = 0;
                        for (int i = 0; i < images.size(); ++i) {
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            BufferedImage bufferedImage = image(i);
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            String sourceFormat = images.get(i).getImageFormat();
                            PdfTools.writePage(document, sourceFormat, bufferedImage, ++count, images.size(), pdfOptionsController);
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            String msg = MessageFormat.format(AppVariables.message("NumberPageWritten"), (i + 1) + "/" + images.size());
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

                        return FileTools.rename(tmpFile, targetFile);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    parentController.popSuccessful();
                    recordFileWritten(targetFile);
                    FxmlStage.openPdfViewer(null, targetFile);
                }

            };
            loading = parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected void saveAsTiff() {
        synchronized (this) {
            if (targetFile == null || images == null || images.isEmpty()) {
                return;
            }
            if (task != null && !task.isQuit()) {
                task.cancel();
                loading = null;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    System.gc();
                    File tmpFile = FileTools.getTempFile();
                    try ( ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                        ImageWriter writer = ImageTiffFile.getWriter();
                        writer.setOutput(out);
                        writer.prepareWriteSequence(null);
                        ImageWriteParam param = ImageTiffFile.getPara(null, writer);
                        for (int i = 0; i < images.size(); ++i) {
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            BufferedImage bufferedImage = image(i);
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            IIOMetadata metaData = ImageTiffFile.getWriterMeta(null, bufferedImage, writer, param);
                            writer.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            String msg = MessageFormat.format(AppVariables.message("NumberImageWritten"), (i + 1) + "/" + images.size());
                            updateLabel(msg, i + 1);
                        }
                        writer.endWriteSequence();
                        writer.dispose();
                        out.flush();
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return FileTools.rename(tmpFile, targetFile);

                }

                @Override
                protected void whenSucceeded() {
                    parentController.popSuccessful();
                    recordFileWritten(targetFile);
                    ImageViewerController controller = (ImageViewerController) openStage(CommonValues.ImageViewerFxml);
                    controller.selectSourceFile(targetFile);
                    controller.toFront();
                }

            };
            loading = parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected void saveAsGif() {
        synchronized (this) {
            if (targetFile == null || images == null || images.isEmpty()) {
                return;
            }
            if (task != null && !task.isQuit()) {
                task.cancel();
                loading = null;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    System.gc();
                    File tmpFile = FileTools.getTempFile();
                    try ( ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
                        ImageWriter gifWriter = ImageGifFile.getWriter();
                        ImageWriteParam param = gifWriter.getDefaultWriteParam();
                        GIFImageMetadata metaData = (GIFImageMetadata) gifWriter.getDefaultImageMetadata(
                                ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB), param);
                        gifWriter.setOutput(out);
                        gifWriter.prepareWriteSequence(null);
                        for (int i = 0; i < images.size(); ++i) {
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            BufferedImage bufferedImage = image(i);
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            if (!gifKeepSize) {
                                bufferedImage = ImageManufacture.scaleImageWidthKeep(bufferedImage, gifWidth);
                            }
                            ImageGifFile.getParaMeta(images.get(i).getDuration(), gifLoopCheck.isSelected(), param, metaData);
                            gifWriter.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            String msg = MessageFormat.format(AppVariables.message("NumberImageWritten"), (i + 1) + "/" + images.size());
                            updateLabel(msg, i + 1);
                        }
                        gifWriter.endWriteSequence();
                        gifWriter.dispose();
                        out.flush();
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return FileTools.rename(tmpFile, targetFile);

                }

                @Override
                protected void whenSucceeded() {
                    parentController.popSuccessful();
                    recordFileWritten(targetFile);
                    ImageViewerController controller = (ImageViewerController) openStage(CommonValues.ImageViewerFxml);
                    controller.selectSourceFile(targetFile);
                    controller.toFront();
                }

            };
            loading = parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected void saveAsPPT() {
        synchronized (this) {
            if (targetFile == null || images == null || images.isEmpty()) {
                return;
            }
            if (task != null && !task.isQuit()) {
                task.cancel();
                loading = null;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    System.gc();
                    File tmpFile = FileTools.getTempFile();
                    try ( HSLFSlideShow ppt = new HSLFSlideShow()) {
                        for (int i = 0; i < images.size(); ++i) {
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            ppt.setPageSize(new java.awt.Dimension(pptWidth, pptHeight));
                            BufferedImage image = ImageConvert.convertToPNG(image(i));;
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            HSLFPictureShape shape = MicrosoftDocumentTools.imageShape(ppt, image, "png");
                            shape.setAnchor(new java.awt.Rectangle(pptMargin, pptMargin, image.getWidth(), image.getHeight()));
                            HSLFSlide slide = ppt.createSlide();
                            slide.addShape(shape);
                            if (task == null || task.isCancelled()) {
                                return false;
                            }
                            String msg = MessageFormat.format(AppVariables.message("NumberImageWritten"), (i + 1) + "/" + images.size());
                            updateLabel(msg, i + 1);
                        }
                        ppt.write(tmpFile);
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                        return false;
                    }
                    return FileTools.rename(tmpFile, targetFile);
                }

                @Override
                protected void whenSucceeded() {
                    parentController.popSuccessful();
                    recordFileWritten(targetFile);
                    PptViewController controller = (PptViewController) openStage(CommonValues.PptViewFxml);
                    controller.sourceFileChanged(targetFile);
                    controller.toFront();
                }

            };
            loading = parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

}
