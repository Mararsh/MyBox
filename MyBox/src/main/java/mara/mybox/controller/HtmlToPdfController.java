package mara.mybox.controller;

import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.data.DataHolder;
import java.io.File;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import mara.mybox.data.VisitHistory;
import mara.mybox.data.tools.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2020-10-17
 * @License Apache License Version 2.0
 */
public class HtmlToPdfController extends FilesBatchController {

    protected DataHolder pdfOptions;
    protected String ttf;

    @FXML
    protected ComboBox<String> ttfSelector;
    @FXML
    protected TextArea cssArea;

    public HtmlToPdfController() {
        baseTitle = AppVariables.message("HtmlToPdf");

        SourceFileType = VisitHistory.FileType.Html;
        SourcePathType = VisitHistory.FileType.Html;
        TargetPathType = VisitHistory.FileType.PDF;
        TargetFileType = VisitHistory.FileType.PDF;
        AddFileType = VisitHistory.FileType.Html;
        AddPathType = VisitHistory.FileType.Html;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Html);
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.PDF);

        sourceExtensionFilter = CommonFxValues.HtmlExtensionFilter;
        targetExtensionFilter = CommonFxValues.PdfExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            List<String> ttfList = SystemTools.ttfList();
            ttfSelector.getItems().addAll(ttfList);
            ttfSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue == null || newValue.isBlank()) {
                        return;
                    }
                    int pos = newValue.indexOf("    ");
                    if (pos < 0) {
                        ttf = newValue;
                    } else {
                        ttf = newValue.substring(0, pos);
                    }
                    AppVariables.setUserConfigValue(baseName + "TTF", newValue);
                    String css = "@font-face {\n"
                            + "  font-family: 'myFont';\n"
                            + "  src: url('file:///" + ttf.replaceAll("\\\\", "/") + "');\n"
                            + "  font-weight: normal;\n"
                            + "  font-style: normal;\n"
                            + "}\n"
                            + " body { font-family:  'myFont';}";
                    cssArea.setText(css);
                }
            });
            String d = AppVariables.getUserConfigValue(baseName + "TTF", null);
            if (d == null) {
                ttfSelector.getSelectionModel().select(0);
            } else {
                ttfSelector.setValue(d);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public boolean makeMoreParameters() {
        try {
            // https://github.com/vsch/flexmark-java/blob/master/flexmark-java-samples/src/com/vladsch/flexmark/java/samples/PdfConverter.java
            pdfOptions = PegdownOptionsAdapter.flexmarkOptions(Extensions.ALL
                    & ~(Extensions.ANCHORLINKS | Extensions.EXTANCHORLINKS_WRAP), TocExtension.create())
                    .toMutable()
                    .set(TocExtension.LIST_CLASS, PdfConverterExtension.DEFAULT_TOC_LIST_CLASS)
                    .toImmutable();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }

        return super.makeMoreParameters();
    }

    @Override
    public boolean matchType(File file) {
        String suffix = FileTools.getFileSuffix(file.getName());
        if (suffix == null) {
            return false;
        }
        suffix = suffix.trim().toLowerCase();
        return "html".equals(suffix) || "htm".equals(suffix);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }
            String html = FileTools.readTexts(srcFile);
            String result = html2pdf(html, target);
            if (AppVariables.message("Successful").equals(result)) {
                targetFileGenerated(target);
            }
            return result;
        } catch (Exception e) {
            return e.toString();
//            MyBoxLog.error(e.toString());
//            return AppVariables.message("Failed");
        }
    }

    public String html2pdf(String html, File target) {
        try {
            String css = cssArea.getText().trim();
            if (!css.isBlank()) {
                try {
                    html = PdfConverterExtension.embedCss(html, css);
                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                }
            }
            try {
                PdfConverterExtension.exportToPdf(target.getAbsolutePath(), html, "", pdfOptions);
                if (!target.exists()) {
                    return AppVariables.message("Failed");
                } else if (target.length() == 0) {
                    FileTools.delete(target);
                    return AppVariables.message("Failed");
                }
                return AppVariables.message("Successful");
            } catch (Exception e) {
                return e.toString();
            }
        } catch (Exception e) {
            return e.toString();
//            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public File makeTargetFile(File sourceFile, File targetPath) {
        try {
            String namePrefix = FileTools.getFilePrefix(sourceFile.getName());
            String nameSuffix = "";
            if (sourceFile.isFile()) {
                nameSuffix = ".pdf";
            }
            return makeTargetFile(namePrefix, nameSuffix, targetPath);
        } catch (Exception e) {
            return null;
        }
    }

}
