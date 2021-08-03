package mara.mybox.controller;

import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.pdf.converter.PdfConverterExtension;
import com.vladsch.flexmark.profile.pegdown.Extensions;
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.data.DataHolder;
import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-10-17
 * @License Apache License Version 2.0
 */
public class HtmlToPdfController extends BaseBatchFileController {

    protected DataHolder pdfOptions;

    @FXML
    protected ControlTTFSelecter ttfController;
    @FXML
    protected TextArea cssArea;

    public HtmlToPdfController() {
        baseTitle = Languages.message("HtmlToPdf");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html, VisitHistory.FileType.PDF);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            ttfController.name(baseName);
            ttfController.ttfSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue == null || newValue.isBlank()) {
                        return;
                    }
                    checkTtf();
                }
            });
            checkTtf();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkTtf() {
        String value = ttfController.ttfSelector.getValue();
        int pos = value.indexOf("    ");
        String ttf;
        if (pos < 0) {
            ttf = value;
        } else {
            ttf = value.substring(0, pos);
        }
        String css = "@font-face {\n"
                + "  font-family: 'myFont';\n"
                + "  src: url('file:///" + ttf.replaceAll("\\\\", "/") + "');\n"
                + "  font-weight: normal;\n"
                + "  font-style: normal;\n"
                + "}\n"
                + " body { font-family:  'myFont';}";
        cssArea.setText(css);
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
        String suffix = FileNameTools.getFileSuffix(file.getName());
        if (suffix == null) {
            return false;
        }
        suffix = suffix.trim().toLowerCase();
        return "html".equals(suffix) || "htm".equals(suffix);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return Languages.message("Skip");
            }
            String html = TextFileTools.readTexts(srcFile);
            String result = html2pdf(html, target);
            if (Languages.message("Successful").equals(result)) {
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
                    return Languages.message("Failed");
                } else if (target.length() == 0) {
                    FileDeleteTools.delete(target);
                    return Languages.message("Failed");
                }
                return Languages.message("Successful");
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
            String namePrefix = FileNameTools.getFilePrefix(sourceFile.getName());
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
