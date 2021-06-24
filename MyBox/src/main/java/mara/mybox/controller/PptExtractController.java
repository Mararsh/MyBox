package mara.mybox.controller;

import java.io.File;
import java.io.FileInputStream;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import org.apache.poi.hslf.usermodel.HSLFObjectData;
import org.apache.poi.hslf.usermodel.HSLFObjectShape;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFPictureShape;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSoundData;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFObjectData;
import org.apache.poi.xslf.usermodel.XSLFObjectShape;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;

/**
 * @Author Mara
 * @CreateDate 2021-5-19
 * @License Apache License Version 2.0
 */
public class PptExtractController extends BaseBatchFileController {

    @FXML
    protected CheckBox slidesCheck, notesCheck, masterCheck, commentsCheck,
            imagesCheck, wordCheck, excelCheck, soundsCheck;

    public PptExtractController() {
        baseTitle = AppVariables.message("PptExtract");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PPTS, VisitHistory.FileType.All);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            slidesCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Slides", true));
            slidesCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                        AppVariables.setUserConfigValue(baseName + "Slides", slidesCheck.isSelected());
                    });
            notesCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Notes", true));
            notesCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                        AppVariables.setUserConfigValue(baseName + "Notes", notesCheck.isSelected());
                    });
            masterCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Master", true));
            masterCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                        AppVariables.setUserConfigValue(baseName + "Master", masterCheck.isSelected());
                    });
            commentsCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Comments", true));
            commentsCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                        AppVariables.setUserConfigValue(baseName + "Comments", commentsCheck.isSelected());
                    });
            soundsCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Sounds", true));
            soundsCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                        AppVariables.setUserConfigValue(baseName + "Sounds", soundsCheck.isSelected());
                    });
            imagesCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Images", true));
            imagesCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                        AppVariables.setUserConfigValue(baseName + "Images", imagesCheck.isSelected());
                    });
            wordCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Word", true));
            wordCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                        AppVariables.setUserConfigValue(baseName + "Word", wordCheck.isSelected());
                    });
            excelCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Excel", true));
            excelCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) -> {
                        AppVariables.setUserConfigValue(baseName + "Excel", excelCheck.isSelected());
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean makeActualParameters() {
        if (!slidesCheck.isSelected() && !notesCheck.isSelected()
                && !masterCheck.isSelected() && !commentsCheck.isSelected()
                && !soundsCheck.isSelected() && !imagesCheck.isSelected()
                && !wordCheck.isSelected() && !excelCheck.isSelected()) {
            popError(message("NothingHandled"));
            return false;
        }
        return super.makeActualParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        String suffix = FileTools.getFileSuffix(srcFile);
        if ("pptx".equalsIgnoreCase(suffix)) {
            return handlePPTX(srcFile, targetPath);
        }
        return handlePPT(srcFile, targetPath);
    }

    public String handlePPT(File srcFile, File targetPath) {
        try ( HSLFSlideShow ppt = new HSLFSlideShow(new FileInputStream(srcFile));) {
            SlideShowExtractor extractor;
            StringBuilder textsBuilder = new StringBuilder();
            if (slidesCheck.isSelected() || notesCheck.isSelected() || masterCheck.isSelected() || commentsCheck.isSelected()) {
                extractor = new SlideShowExtractor(ppt);
                extractor.setSlidesByDefault(slidesCheck.isSelected());
                extractor.setMasterByDefault(masterCheck.isSelected());
                extractor.setNotesByDefault(notesCheck.isSelected());
                extractor.setCommentsByDefault(commentsCheck.isSelected());
            } else {
                extractor = null;
            }
            if (extractor != null || imagesCheck.isSelected() || wordCheck.isSelected() || excelCheck.isSelected()) {
                int slideIndex = 0;
                for (HSLFSlide slide : ppt.getSlides()) {
                    slideIndex++;
                    if (extractor != null) {
                        String texts = extractor.getText(slide);
                        if (texts != null && !texts.isBlank()) {
                            textsBuilder.append(message("Slide")).append(" ").append(slideIndex)
                                    .append(texts)
                                    .append("\n----------------------------------\n\n");
                        }
                    }

                    int pixIndex = 0, oleIndex = 0;
                    for (HSLFShape shape : slide.getShapes()) {
                        if (imagesCheck.isSelected() && (shape instanceof HSLFPictureShape)) {
                            HSLFPictureData pictData = ((HSLFPictureShape) shape).getPictureData();
                            targetFile = makeObjectFile(srcFile, slideIndex, ++pixIndex, pictData.getType().extension, targetPath);
                            if (FileTools.writeFile(targetFile, pictData.getData())) {
                                targetFileGenerated(targetFile);
                            }
                        }
                        if ((wordCheck.isSelected() || excelCheck.isSelected())
                                && (shape instanceof HSLFObjectShape)) {
                            HSLFObjectShape object = (HSLFObjectShape) shape;
                            HSLFObjectData data = object.getObjectData();
                            String name = object.getInstanceName();
                            if (name == null) {
                                continue;
                            }
                            String ext;
                            if (excelCheck.isSelected() && "Worksheet".equals(name)) {
                                ext = "xls";
                            } else if (wordCheck.isSelected() && "Document".equals(name)) {
                                ext = "doc";
                            } else {
                                continue;
                            }
                            targetFile = makeObjectFile(srcFile, slideIndex, ++oleIndex, ext, targetPath);
                            byte[] bytes = data.getBytes();
                            if (bytes == null && data.getFileName() != null) {
                                File file = new File(data.getFileName());
                                if (file.exists()) {
                                    bytes = FileTools.readBytes(file);
                                }
                            }
                            if (FileTools.writeFile(targetFile, bytes)) {
                                targetFileGenerated(targetFile);
                            }
                        }
                    }
                }
            }

            if (extractor != null) {
                targetFile = makeObjectFile(srcFile, -1, -1, "txt", targetPath);
                if (FileTools.writeFile(targetFile, textsBuilder.toString()) != null) {
                    targetFileGenerated(targetFile);
                }
            }

            if (soundsCheck.isSelected()) {
                int soundIndex = 0;
                for (HSLFSoundData sound : ppt.getSoundData()) {
                    targetFile = makeObjectFile(srcFile, -1, ++soundIndex, sound.getSoundType(), targetPath);
                    if (FileTools.writeFile(targetFile, sound.getData())) {
                        targetFileGenerated(targetFile);
                    }
                }
            }

        } catch (Exception e) {
            MyBoxLog.console(e);
            return e.toString();
        }
        return message("Successful");
    }

    public String handlePPTX(File srcFile, File targetPath) {
        try ( XMLSlideShow ppt = new XMLSlideShow(new FileInputStream(srcFile))) {
            SlideShowExtractor extractor;
            StringBuilder textsBuilder = new StringBuilder();
            if (slidesCheck.isSelected() || notesCheck.isSelected() || masterCheck.isSelected() || commentsCheck.isSelected()) {
                extractor = new SlideShowExtractor(ppt);
                extractor.setSlidesByDefault(slidesCheck.isSelected());
                extractor.setMasterByDefault(masterCheck.isSelected());
                extractor.setNotesByDefault(notesCheck.isSelected());
                extractor.setCommentsByDefault(commentsCheck.isSelected());
            } else {
                extractor = null;
            }
            if (extractor != null || imagesCheck.isSelected() || wordCheck.isSelected() || excelCheck.isSelected()) {
                int slideIndex = 0;
                for (XSLFSlide slide : ppt.getSlides()) {
                    slideIndex++;
                    if (extractor != null) {
                        String texts = extractor.getText(slide);
                        if (texts != null && !texts.isBlank()) {
                            textsBuilder.append(message("Slide")).append(" ").append(slideIndex)
                                    .append(texts)
                                    .append("\n----------------------------------\n\n");
                        }
                    }

                    int pixIndex = 0, oleIndex = 0;
                    for (XSLFShape shape : slide.getShapes()) {
                        if (imagesCheck.isSelected() && (shape instanceof XSLFPictureShape)) {
                            XSLFPictureData pictData = ((XSLFPictureShape) shape).getPictureData();
                            targetFile = makeObjectFile(srcFile, slideIndex, ++pixIndex, pictData.getType().extension, targetPath);
                            if (FileTools.writeFile(targetFile, pictData.getData())) {
                                targetFileGenerated(targetFile);
                            }
                        }
                        if ((wordCheck.isSelected() || excelCheck.isSelected())
                                && (shape instanceof XSLFObjectShape)) {
                            XSLFObjectShape object = (XSLFObjectShape) shape;
                            XSLFObjectData data = object.getObjectData();
                            String name = object.getFullName();
                            String ext;
                            if (excelCheck.isSelected() && name.contains("Worksheet")) {
                                ext = "xls";
                            } else if (wordCheck.isSelected() && name.contains("Document")) {
                                ext = "doc";
                            } else {
                                continue;
                            }
                            targetFile = makeObjectFile(srcFile, slideIndex, ++oleIndex, ext, targetPath);
                            byte[] bytes = data.getBytes();
                            if (bytes == null && data.getFileName() != null) {
                                File file = new File(data.getFileName());
                                if (file.exists()) {
                                    bytes = FileTools.readBytes(file);
                                }
                            }
                            if (FileTools.writeFile(targetFile, bytes)) {
                                targetFileGenerated(targetFile);
                            }
                        }
                    }
                }

                if (extractor != null) {
                    targetFile = makeObjectFile(srcFile, -1, -1, "txt", targetPath);
                    if (FileTools.writeFile(targetFile, textsBuilder.toString()) != null) {
                        targetFileGenerated(targetFile);
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
            return e.toString();
        }
        return message("Successful");
    }

    public File makeObjectFile(File srcFile, int slideIndex, int objIndex, String ext, File targetPath) {
        return makeObjectFile(srcFile, null, slideIndex, objIndex, ext, targetPath);
    }

    public File makeObjectFile(File srcFile, String prefix, int slideIndex, int objIndex, String ext, File targetPath) {
        try {
            String srcPrefix = FileTools.getFilePrefix(srcFile);
            String filePrefix = srcPrefix
                    + (prefix != null ? "_" + prefix : "")
                    + (slideIndex >= 0 ? "_slide" + slideIndex : "")
                    + (objIndex >= 0 ? "_" + objIndex : "");
            String fileSuffix = (ext.startsWith(".") ? "" : ".") + ext;

            File slidePath = targetPath;
            if (targetSubdirCheck.isSelected()) {
                slidePath = new File(targetPath, srcPrefix);
            }
            return makeTargetFile(filePrefix, fileSuffix, slidePath);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
