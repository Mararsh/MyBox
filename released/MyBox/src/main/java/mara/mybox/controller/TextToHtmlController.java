package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-10-24
 * @License Apache License Version 2.0
 */
public class TextToHtmlController extends BaseBatchFileController {

    @FXML
    protected TextArea headArea;

    public TextToHtmlController() {
        baseTitle = Languages.message("TextToHtml");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text, VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            String head
                    = "    <head>\n"
                    + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
                    + "         <style type=\"text/css\">\n"
                    + "		  .article {width: 900px; margin:0 auto;font-size:20px;font-weight:normal;}\n"
                    + "        </style>\n"
                    + "        <title>####title####</title>\n"
                    + "    </head>";
            headArea.setText(head);

            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return Languages.message("Skip");
            }
            String text = TextFileTools.readTexts(srcFile);
            String body = HtmlWriteTools.stringToHtml(text);;
            String filePrefix = FileNameTools.getFilePrefix(target);
            String html = "<!DOCTYPE html><html>\n"
                    + headArea.getText().replace("####title####", filePrefix) + "\n"
                    + "    <body class=\"article\">\n"
                    + body
                    + "    </body>\n"
                    + "</html>\n";
            TextFileTools.writeFile(target, html, Charset.forName("utf-8"));
            targetFileGenerated(target);
            return Languages.message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return Languages.message("Failed");
        }
    }

    @Override
    public File makeTargetFile(File sourceFile, File targetPath) {
        try {
            String namePrefix = FileNameTools.getFilePrefix(sourceFile.getName());
            String nameSuffix = "";
            if (sourceFile.isFile()) {
                nameSuffix = ".html";
            }
            return makeTargetFile(namePrefix, nameSuffix, targetPath);
        } catch (Exception e) {
            return null;
        }
    }

}
