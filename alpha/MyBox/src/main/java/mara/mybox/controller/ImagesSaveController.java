package mara.mybox.controller;

import com.github.jaiimageio.impl.plugins.gif.GIFImageMetadata;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import mara.mybox.image.tools.ImageConvertTools;
import mara.mybox.image.data.ImageInformation;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.image.file.ImageGifFile;
import mara.mybox.image.file.ImageTiffFile;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.hslf.usermodel.HSLFPictureShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;

/**
 * @Author Mara
 * @CreateDate 2021-5-27
 * @License Apache License Version 2.0
 */
public class ImagesSaveController extends BaseTaskController {

    protected String imagesFormat;
    protected int pptWidth, pptHeight, pptMargin, savedWidth;
    protected List<ImageInformation> imageInfos;
    protected List<String> fileNames;

    @FXML
    protected Tab imageTab, pptTab, pdfTab, othersTab;
    @FXML
    protected ToggleGroup saveGroup;
    @FXML
    protected RadioButton imagesRadio, spliceRadio, pdfRadio, pptRadio,
            tifFileRadio, gifFileRadio, videoRadio;
    @FXML
    protected VBox setBox, imageFormatBox, pptBox, pdfBox, targetVBox, pathBox, fileBox;
    @FXML
    protected ControlImageFormat formatController;
    @FXML
    protected TextField prefixInput, pptWidthInput, pptHeightInput, pptMarginInput;
    @FXML
    protected CheckBox gifLoopCheck, pptMarginCheck;
    @FXML
    protected ControlPdfWriteOptions pdfOptionsController;
    @FXML
    protected ComboBox<String> savedWidthSelector;
    @FXML
    protected HBox savedWidthBox;
    @FXML
    protected ControlTargetPath pathController;
    @FXML
    protected ControlTargetFile fileController;
    @FXML
    protected Button openTargetButton;

    public ImagesSaveController() {
        baseTitle = message("SaveAs");
    }

    @Override
    public void setFileType() {
        setFileType(FileType.Image);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initFormatPane();
            initImage();
            initGif();
            initPpt();
            formatController.setParameters(this, false);
            pdfOptionsController.set(baseName, true);

            tabPane.getTabs().removeAll(imageTab, pptTab, pdfTab, othersTab);
            setBox.getChildren().clear();
            targetVBox.getChildren().clear();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(File source, List<ImageInformation> infos) {
        try {
            if (infos == null || infos.isEmpty()) {
                close();
                return;
            }
            imageInfos = new ArrayList<>();
            for (ImageInformation info : infos) {
                imageInfos.add(info.cloneAttributes());
            }
            if (source != null) {
                prefixInput.setText(FileNameTools.prefix(source.getName()));
            }
            checkFormatType();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initFormatPane() {
        try {
            saveGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                    checkFormatType();
                }
            });
            checkFormatType();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void checkFormatType() {
        setBox.getChildren().clear();
        targetVBox.getChildren().clear();

        String bname;
        if (imagesRadio.isSelected()) {
            setTargetFileType(VisitHistory.FileType.Image);
            setBox.getChildren().addAll(savedWidthBox, imageFormatBox);
            targetVBox.getChildren().add(pathBox);
            bname = baseName + "ImagesTargetPath";
            pathController.initPathSelecter().type(TargetPathType)
                    .parent(this, bname);
            openTargetButton.setDisable(false);

        } else if (!spliceRadio.isSelected() && !videoRadio.isSelected()) {

            if (tifFileRadio.isSelected()) {
                setTargetFileType(VisitHistory.FileType.Tif);
                setBox.getChildren().addAll(savedWidthBox);
                bname = baseName + "TifTargetFile";

            } else if (gifFileRadio.isSelected()) {
                setTargetFileType(VisitHistory.FileType.Gif);
                setBox.getChildren().addAll(savedWidthBox, gifLoopCheck);
                bname = baseName + "GifTargetFile";

            } else if (pdfRadio.isSelected()) {
                setTargetFileType(VisitHistory.FileType.PDF);
                setBox.getChildren().addAll(savedWidthBox, pdfBox);
                bname = baseName + "PDFTargetFile";

            } else if (pptRadio.isSelected()) {
                setTargetFileType(VisitHistory.FileType.PPT);
                setBox.getChildren().addAll(savedWidthBox, pptBox);
                bname = baseName + "PPTTargetFile";
            } else {
                return;
            }

            targetVBox.getChildren().add(fileBox);
            fileController.initFileSelecter().type(TargetFileType)
                    .parent(this, bname);
            openTargetButton.setDisable(false);
        } else {
            openTargetButton.setDisable(true);
        }
    }

    public void initImage() {
        try {
            savedWidth = UserConfig.getInt(baseName + "SavedWidth", -1);
            List<String> values = Arrays.asList(message("OriginalSize"),
                    "512", "1024", "256", "128", "2048", "100", "80", "4096");
            savedWidthSelector.getItems().addAll(values);
            if (savedWidth <= 0) {
                savedWidthSelector.getSelectionModel().select(0);
            } else {
                savedWidthSelector.setValue(savedWidth + "");
            }
            savedWidthSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkImageWidth();
                }
            });
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected boolean checkImageWidth() {
        int v;
        if (message("OriginalSize").equals(savedWidthSelector.getValue())) {
            v = -1;
        } else {
            try {
                v = Integer.parseInt(savedWidthSelector.getValue());
            } catch (Exception e) {
                popError(message("InvalidParameter") + ": " + message("SavedWidth"));
                ValidationTools.setEditorBadStyle(savedWidthSelector);
                return false;
            }
        }
        savedWidth = v > 0 ? v : -1;
        ValidationTools.setEditorNormal(savedWidthSelector);
        return true;
    }

    public void initGif() {
        try {
            gifLoopCheck.setSelected(UserConfig.getBoolean(baseName + "GifLoop", true));
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void initPpt() {
        try {
            pptWidth = UserConfig.getInt(baseName + "PptWidth", 1024);
            if (pptWidth <= 0) {
                pptWidth = 1024;
            }
            pptWidthInput.setText(pptWidth + "");
            pptWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkPptWidth();
                }
            });

            pptHeight = UserConfig.getInt(baseName + "PptHeight", 768);
            if (pptHeight <= 0) {
                pptHeight = 768;
            }
            pptHeightInput.setText(pptHeight + "");
            pptHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkPptHeight();
                }
            });

            pptMargin = UserConfig.getInt(baseName + "PptMargin", 20);
            if (pptMargin <= 0) {
                pptMargin = 20;
            }
            pptMarginInput.setText(pptMargin + "");
            pptMarginInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkPptMargin();
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected boolean checkPptWidth() {
        int v;
        try {
            v = Integer.parseInt(pptWidthInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            pptWidth = v;
            pptWidthInput.setStyle(null);
            return true;
        } else {
            pptWidthInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("Width"));
            return false;
        }
    }

    protected boolean checkPptHeight() {
        int v;
        try {
            v = Integer.parseInt(pptHeightInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            pptHeight = v;
            pptHeightInput.setStyle(null);
            return true;
        } else {
            pptHeightInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("Height"));
            return false;
        }
    }

    protected boolean checkPptMargin() {
        int v;
        try {
            v = Integer.parseInt(pptMarginInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        if (v > 0) {
            pptMargin = v;
            pptMarginInput.setStyle(null);
            return true;
        } else {
            pptMarginInput.setStyle(UserConfig.badStyle());
            popError(message("InvalidParameter") + ": " + message("Margin"));
            return false;
        }
    }

    @FXML
    public void pptMaxSize() {
        if (imageInfos == null || imageInfos.isEmpty()) {
            popError(message("NoData"));
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

    @Override
    public boolean checkOptions() {
        if (imageInfos == null || imageInfos.isEmpty()) {
            close();
            return false;
        }
        targetPath = null;
        targetFile = null;
        if (imagesRadio.isSelected()) {
            targetPath = pathController.pickFile();
            MyBoxLog.console(targetPath);
            if (targetPath == null) {
                popError(message("InvalidParameter") + ": " + message("TargetPath"));
                return false;
            }
            imagesFormat = formatController.attributes.getImageFormat();
            if (imagesFormat == null || !checkImageWidth()) {
                return false;
            }

        } else if (!spliceRadio.isSelected() && !videoRadio.isSelected()) {
            targetFile = fileController.makeTargetFile();
            if (targetFile == null) {
                popError(message("InvalidParameter") + ": " + message("TargetFile"));
                return false;
            }
            if (pptRadio.isSelected()) {
                if (!checkPptWidth() || !checkPptHeight() || !checkPptMargin()) {
                    return false;
                }
            }
        }
        if (pdfRadio.isSelected()) {
            if (!pdfOptionsController.pickValues()) {
                return false;
            }
        }
        try (Connection conn = DerbyBase.getConnection()) {
            UserConfig.setInt(conn, baseName + "savedWidth", savedWidth);
            UserConfig.setInt(conn, baseName + "PptWidth", pptWidth);
            UserConfig.setInt(conn, baseName + "PptHeight", pptHeight);
            UserConfig.setInt(conn, baseName + "PptMargin", pptMargin);
            UserConfig.setBoolean(conn, baseName + "GifLoop", gifLoopCheck.isSelected());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return true;
    }

    @FXML
    @Override
    public void startAction() {
        if (spliceRadio.isSelected()) {
            saveAsSplice();
            return;
        } else if (videoRadio.isSelected()) {
            saveAsVideo();
            return;
        }
        runTask();
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        if (imagesRadio.isSelected()) {
            return saveAsImages(currentTask);

        } else if (tifFileRadio.isSelected()) {
            return saveAsTiff(currentTask);

        } else if (gifFileRadio.isSelected()) {
            return saveAsGif(currentTask);

        } else if (pdfRadio.isSelected()) {
            return saveAsPdf(currentTask);

        } else if (pptRadio.isSelected()) {
            return saveAsPPT(currentTask);

        }
        return false;
    }

    @Override
    public void afterSuccess() {
        if (imagesRadio.isSelected()) {
            recordFileWritten(targetPath);
            multipleFilesGenerated(fileNames);

        } else if (tifFileRadio.isSelected()) {
            parentController.popSuccessful();
            recordFileWritten(targetFile);
            ImageEditorController.openFile(targetFile);

        } else if (gifFileRadio.isSelected()) {
            parentController.popSuccessful();
            recordFileWritten(targetFile);
            ImageEditorController.openFile(targetFile);

        } else if (pdfRadio.isSelected()) {
            parentController.popSuccessful();
            recordFileWritten(targetFile);
            PdfViewController.open(targetFile);

        } else if (pptRadio.isSelected()) {
            parentController.popSuccessful();
            recordFileWritten(targetFile);
            PptViewController controller = (PptViewController) openStage(Fxmls.PptViewFxml);
            controller.sourceFileChanged(targetFile);
            controller.requestMouse();

        }
    }

    protected BufferedImage image(FxTask currentTask, int index) {
        try {
            if (imageInfos == null || index < 0 || index >= imageInfos.size()) {
                return null;
            }
            String msg = message("Current") + ": " + (index + 1) + "/" + imageInfos.size();
            updateLogs(msg, true);
            ImageInformation info = imageInfos.get(index);
            if (info == null) {
                return null;
            }
            if (info.getFile() != null) {
                msg = message("SourceFile") + ": " + info.getFile();
                updateLogs(msg, true);
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

    protected boolean saveAsImages(FxTask currentTask) {
        fileNames = new ArrayList<>();
        try {
            String prefix = prefixInput.getText();
            prefix = prefix == null ? "" : (prefix + "-");
            for (int i = 0; i < imageInfos.size(); ++i) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                BufferedImage bufferedImage = image(currentTask, i);
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                if (bufferedImage == null) {
                    continue;
                }
                int digit = (imageInfos.size() + "").length();
                File file = pathController.makeTargetFile(prefix + StringTools.fillLeftZero(i, digit),
                        "." + imagesFormat, targetPath);
                if (file == null) {
                    continue;
                }
                String filename = file.getAbsolutePath();
                updateLogs(message("TargetFile") + ": " + filename);
                BufferedImage converted = ImageConvertTools.convertColorSpace(currentTask,
                        bufferedImage, formatController.attributes);
                if (converted == null || currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                if (ImageFileWriters.writeImageFile(currentTask, converted, formatController.attributes, filename)) {
                    fileNames.add(filename);
                    String msg = MessageFormat.format(message("NumberFileGenerated"),
                            (i + 1) + "/" + imageInfos.size(), "\"" + filename + "\"");
                    updateLogs(msg, true);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return !fileNames.isEmpty();
    }

    protected boolean saveAsPdf(FxTask currentTask) {
        File tmpFile = FileTmpTools.getTempFile();
        try (PDDocument document = new PDDocument()) {
            int count = 0;
            for (int i = 0; i < imageInfos.size(); ++i) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                BufferedImage bufferedImage = image(currentTask, i);
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                if (bufferedImage == null) {
                    continue;
                }
                String sourceFormat = imageInfos.get(i).getImageFormat();
                PdfTools.writePage(currentTask,
                        document, sourceFormat, bufferedImage, ++count,
                        imageInfos.size(), pdfOptionsController);
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                String msg = MessageFormat.format(message("NumberPageWritten"), (i + 1) + "/" + imageInfos.size());
                updateLogs(msg, true);
            }

            PdfTools.setAttributes(document,
                    pdfOptionsController.authorInput.getText(),
                    pdfOptionsController.zoom);

            document.save(tmpFile);
            document.close();

            return FileTools.override(tmpFile, targetFile);
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    protected boolean saveAsTiff(FxTask currentTask) {
        File tmpFile = FileTmpTools.getTempFile();
        try (ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
            ImageWriter writer = ImageTiffFile.getWriter();
            writer.setOutput(out);
            writer.prepareWriteSequence(null);
            ImageWriteParam param = ImageTiffFile.getPara(null, writer);
            for (int i = 0; i < imageInfos.size(); ++i) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                BufferedImage bufferedImage = image(currentTask, i);
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                if (bufferedImage == null) {
                    continue;
                }
                IIOMetadata metaData = ImageTiffFile.getWriterMeta(null, bufferedImage, writer, param);
                if (metaData == null) {
                    continue;
                }
                writer.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                String msg = MessageFormat.format(message("NumberImageWritten"), (i + 1) + "/" + imageInfos.size());
                updateLogs(msg, true);
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

    protected boolean saveAsGif(FxTask currentTask) {
        File tmpFile = FileTmpTools.getTempFile();
        try (ImageOutputStream out = ImageIO.createImageOutputStream(tmpFile)) {
            ImageWriter gifWriter = ImageGifFile.getWriter();
            ImageWriteParam param = gifWriter.getDefaultWriteParam();
            GIFImageMetadata metaData = (GIFImageMetadata) gifWriter.getDefaultImageMetadata(
                    ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB), param);
            gifWriter.setOutput(out);
            gifWriter.prepareWriteSequence(null);
            for (int i = 0; i < imageInfos.size(); ++i) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                BufferedImage bufferedImage = image(currentTask, i);
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                if (bufferedImage == null) {
                    continue;
                }
                ImageGifFile.getParaMeta(imageInfos.get(i).getDuration(), gifLoopCheck.isSelected(), param, metaData);
                gifWriter.writeToSequence(new IIOImage(bufferedImage, null, metaData), param);
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                String msg = MessageFormat.format(message("NumberImageWritten"), (i + 1) + "/" + imageInfos.size());
                updateLogs(msg, true);
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

    protected boolean saveAsPPT(FxTask currentTask) {
        File tmpFile = FileTmpTools.getTempFile();
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            for (int i = 0; i < imageInfos.size(); ++i) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                ppt.setPageSize(new java.awt.Dimension(pptWidth, pptHeight));
                BufferedImage image = ImageConvertTools.convertToPNG(image(currentTask, i));
                if (image == null || currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                if (image == null) {
                    continue;
                }
                HSLFPictureShape shape = MicrosoftDocumentTools.imageShape(currentTask, ppt, image, "png");
                if (shape == null || currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                shape.setAnchor(new java.awt.Rectangle(pptMargin, pptMargin, image.getWidth(), image.getHeight()));
                HSLFSlide slide = ppt.createSlide();
                slide.addShape(shape);
                if (currentTask == null || !currentTask.isWorking()) {
                    return false;
                }
                String msg = MessageFormat.format(message("NumberImageWritten"), (i + 1) + "/" + imageInfos.size());
                updateLogs(msg, true);
            }
            ppt.write(tmpFile);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
        return FileTools.override(tmpFile, targetFile);
    }

    protected void saveAsSplice() {
        if (imageInfos == null || imageInfos.isEmpty()) {
            close();
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
            close();
            return;
        }
        List<ImageInformation> infos = new ArrayList<>();
        for (int i = 0; i < imageInfos.size(); ++i) {
            ImageInformation info = imageInfos.get(i).cloneAttributes();
            infos.add(info);
        }
        FFmpegMergeImagesController.open(infos);
    }

    @FXML
    public void editFrames() {
        ImagesEditorController.openImages(imageInfos);
    }

    @FXML
    @Override
    public void openTarget() {
        if (imagesRadio.isSelected()) {
            targetPath = pathController.pickFile();
            if (targetPath == null || !targetPath.exists()) {
                popInformation(message("NotExist"));
                return;
            }
            browseURI(targetPath.toURI());
            recordFileOpened(targetPath);
        } else if (!spliceRadio.isSelected() && !videoRadio.isSelected()) {
            if (targetFile == null || !targetFile.exists()) {
                popInformation(message("NotExist"));
                return;
            }
            ControllerTools.popTarget(myController, targetFile.getAbsolutePath(), true);
        } else {
            popInformation(message("NoData"));
        }
    }

    /*
        static methods
     */
    public static ImagesSaveController saveImages(BaseController parent, List<ImageInformation> infos) {
        try {
            ImagesSaveController controller = (ImagesSaveController) WindowTools.childStage(parent, Fxmls.ImagesSaveFxml);
            controller.setParameters(parent != null ? parent.sourceFile : null, infos);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
