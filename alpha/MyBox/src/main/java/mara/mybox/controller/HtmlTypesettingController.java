package mara.mybox.controller;

import java.io.File;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @Author Mara
 * @CreateDate 2023-5-26
 * @License Apache License Version 2.0
 */
public class HtmlTypesettingController extends BaseBatchFileController {

    public HtmlTypesettingController() {
        baseTitle = message("HtmlTypesetting");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }

            Document doc = Jsoup.parse(srcFile);

            TextFileTools.writeFile(target, doc.html(), doc.charset());

            if (target.exists() && target.length() > 0) {
                targetFileGenerated(target);
                return message("Successful");
            } else {
                return message("Failed");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return message("Failed");
        }
    }

}
