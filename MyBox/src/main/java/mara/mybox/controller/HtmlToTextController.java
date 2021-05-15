package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import org.jsoup.Jsoup;

/**
 * @Author Mara
 * @CreateDate 2020-10-17
 * @License Apache License Version 2.0
 */
public class HtmlToTextController extends BaseBatchFileController {

    public HtmlToTextController() {
        baseTitle = AppVariables.message("HtmlToText");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html, VisitHistory.FileType.Text);
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
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return AppVariables.message("Skip");
            }

            String text = Jsoup.parse(FileTools.readTexts(srcFile)).wholeText();

            FileTools.writeFile(target, text, Charset.forName("utf-8"));
            targetFileGenerated(target);
            return AppVariables.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return AppVariables.message("Failed");
        }
    }

    @Override
    public File makeTargetFile(File sourceFile, File targetPath) {
        try {
            String namePrefix = FileTools.getFilePrefix(sourceFile.getName());
            String nameSuffix = "";
            if (sourceFile.isFile()) {
                nameSuffix = ".txt";
            }
            return makeTargetFile(namePrefix, nameSuffix, targetPath);
        } catch (Exception e) {
            return null;
        }
    }

}
