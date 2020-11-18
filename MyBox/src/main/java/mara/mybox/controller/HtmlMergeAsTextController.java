package mara.mybox.controller;

import com.vladsch.flexmark.util.ast.Node;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.FileInformation;
import mara.mybox.data.VisitHistory;
import mara.mybox.data.tools.VisitHistoryTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2020-10-21
 * @License Apache License Version 2.0
 */
public class HtmlMergeAsTextController extends HtmlToTextController {

    protected FileWriter writer;

    @FXML
    private CheckBox deleteCheck;
    @FXML
    protected ControlFileSelecter targetFileController;

    public HtmlMergeAsTextController() {
        baseTitle = AppVariables.message("HtmlMergeAsText");

        SourceFileType = VisitHistory.FileType.Html;
        SourcePathType = VisitHistory.FileType.Html;
        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;
        AddFileType = VisitHistory.FileType.Html;
        AddPathType = VisitHistory.FileType.Html;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Html);
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Text);

        sourceExtensionFilter = CommonFxValues.HtmlExtensionFilter;
        targetExtensionFilter = CommonFxValues.TextExtensionFilter;
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            targetFileController.label(message("TargetFile"))
                    .isDirectory(false).isSource(false).mustExist(false).permitNull(false)
                    .defaultValue("_" + message("Merge"))
                    .name(baseName + "TargetFile", false).type(VisitHistory.FileType.Text);

            targetFileInput = targetFileController.fileInput;

        } catch (Exception e) {
            logger.error(e.toString());
        }
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
            logger.error(e.toString());
            return false;
        }
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            String html = FileTools.readTexts(srcFile);
            String md = mdConverter.convert(html);
            Node document = textParser.parse(md);
            String text = textCollectingVisitor.collectAndGetText(document);
            writer.write(text + "\n");
            return AppVariables.message("Successful");
        } catch (Exception e) {
            logger.error(e.toString());
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
                        source.getFile().delete();
                        tableData.remove(i);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
