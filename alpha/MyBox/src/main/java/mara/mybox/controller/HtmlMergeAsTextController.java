package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import org.jsoup.Jsoup;

/**
 * @Author Mara
 * @CreateDate 2020-10-21
 * @License Apache License Version 2.0
 */
public class HtmlMergeAsTextController extends HtmlToTextController {

    protected FileWriter writer;

    @FXML
    protected CheckBox deleteCheck;

    public HtmlMergeAsTextController() {
        baseTitle = message("HtmlMergeAsText");
    }

    @Override
    public boolean beforeHandleFiles() {
        try {
            writer = new FileWriter(targetFile, Charset.forName("utf-8"));
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            String html = TextFileTools.readTexts(task, srcFile);
            if (html == null || (task != null && !task.isWorking())) {
                return message("Canceled");
            }
            String text = Jsoup.parse(html).wholeText();
            writer.write(text + "\n");
            return message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
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
                        FileDeleteTools.delete(source.getFile());
                        tableData.remove(i);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
