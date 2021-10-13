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
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-10-21
 * @License Apache License Version 2.0
 */
public class HtmlMergeAsMarkdownController extends FilesMergeController {

    protected FileWriter writer;
    protected FlexmarkHtmlConverter mdConverter;

    @FXML
    protected CheckBox deleteCheck;

    public HtmlMergeAsMarkdownController() {
        baseTitle = message("HtmlMergeAsMarkdown");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

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
    public boolean matchType(File file) {
        String suffix = FileNameTools.getFileSuffix(file.getName());
        if (suffix == null) {
            return false;
        }
        suffix = suffix.trim().toLowerCase();
        return "html".equals(suffix) || "htm".equals(suffix);
    }

    @Override
    protected boolean openWriter() {
        try {
            writer = new FileWriter(targetFile, Charset.forName("utf-8"));
            return true;
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
        }
    }

    @Override
    public String handleFile(File file) {
        try {
            if (task == null || task.isCancelled()) {
                return message("Canceled");
            }
            if (file == null || !file.isFile() || !match(file)) {
                return message("Skip" + ": " + file);
            }
            String html = TextFileTools.readTexts(file);
            String md = mdConverter.convert(html);
            writer.write(md + "\n");
            return message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return message("Failed");
        }
    }

    @Override
    protected boolean closeWriter() {
        try {
            writer.flush();
            writer.close();
            if (deleteCheck.isSelected()) {
                List<FileInformation> sources = new ArrayList<>();
                sources.addAll(tableData);
                for (int i = sources.size() - 1; i >= 0; --i) {
                    try {
                        FileInformation source = sources.get(i);
                        FileDeleteTools.delete(source.getFile());
                        tableData.remove(i);
                    } catch (Exception e) {
                    }
                }
            }
            return true;
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
        }
    }

}
