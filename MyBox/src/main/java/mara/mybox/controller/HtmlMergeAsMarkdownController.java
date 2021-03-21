package mara.mybox.controller;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.FileInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-10-21
 * @License Apache License Version 2.0
 */
public class HtmlMergeAsMarkdownController extends BaseBatchFileController {

    protected FileWriter writer;
    protected FlexmarkHtmlConverter mdConverter;

    @FXML
    protected CheckBox deleteCheck;
    @FXML
    protected ControlFileSelecter targetFileController;

    public HtmlMergeAsMarkdownController() {
        baseTitle = AppVariables.message("HtmlMergeAsMarkdown");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            targetFileController.label(message("TargetFile"))
                    .isDirectory(false).isSource(false).mustExist(false).permitNull(false)
                    .defaultValue("_" + message("Merge"))
                    .name(baseName + "TargetFile", false).type(VisitHistory.FileType.Markdown);

            targetFileInput = targetFileController.fileInput;

            mdConverter = FlexmarkHtmlConverter.builder(new MutableDataSet()).build();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html, VisitHistory.FileType.Markdown);
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            targetFile = targetFileController.file;
            if (targetFile == null) {
                return false;
            }
            writer = new FileWriter(targetFile, Charset.forName("utf-8"));
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
            String html = FileTools.readTexts(srcFile);
            String md = mdConverter.convert(html);
            writer.write(md + "\n");
            return AppVariables.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    @Override
    public void afterHandleFiles() {
        try {
            writer.flush();
            writer.close();
            targetFileGenerated(targetFile);
            if (deleteCheck.isSelected()) {
                List<FileInformation> sources = new ArrayList<>();
                sources.addAll(tableData);
                for (int i = sources.size() - 1; i >= 0; --i) {
                    try {
                        FileInformation source = sources.get(i);
                        FileTools.delete(source.getFile());
                        tableData.remove(i);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
