package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.data.FileInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-10-21
 * @License Apache License Version 2.0
 */
public class HtmlMergeAsHtmlController extends FilesMergeController {

    protected FileWriter writer;

    @FXML
    protected TextArea headArea;
    @FXML
    protected TextField titleInput;
    @FXML
    protected CheckBox deleteCheck;
    @FXML
    protected TextArea cssArea;

    public HtmlMergeAsHtmlController() {
        baseTitle = Languages.message("HtmlMergeAsHtml");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    @Override
    public void initControls() {
        try {
            String head
                    = "    <head>\n"
                    + "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
                    + "        <title>####title####</title>\n"
                    + "    </head>";
            headArea.setText(head);

            targetFileController.notify.addListener(
                    (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        File file = targetFileController.getFile();
                        if (file != null) {
                            String prefix = FileNameTools.prefix(file.getName());
                            if (prefix != null) {
                                titleInput.setText(prefix);
                            }
                        }

                    });
//            titleInput.textProperty().addListener(
//                    (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
//                        if (newValue == null) {
//                            return;
//                        }
//                        headArea.setText(headArea.getText().replace("####title####", newValue));
//                    });

            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected boolean openWriter() {
        try {
            writer = new FileWriter(targetFile, Charset.forName("utf-8"));
            writer.write("<html>\n"
                    + headArea.getText().replace("####title####", titleInput.getText()) + "\n"
                    + "    <body>\n");
            return true;
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
        }
    }

    @Override
    public String handleFile(FxTask currentTask, File file) {
        try {
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            if (file == null || !file.isFile() || !match(file)) {
                return message("Skip" + ": " + file);
            }
            String texts = TextFileTools.readTexts(currentTask, file);
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            if (texts == null) {
                return message("Failed");
            }
            String body = HtmlReadTools.body(texts, false);
            writer.write(body + "\n");
            return message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
        }
    }

    @Override
    protected boolean closeWriter() {
        try {
            writer.write("    </body>\n</html>\n");
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
