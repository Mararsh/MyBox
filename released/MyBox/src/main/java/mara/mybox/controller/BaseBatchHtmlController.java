package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-3-13
 * @License Apache License Version 2.0
 */
public abstract class BaseBatchHtmlController extends BaseBatchFileController {

    public abstract String covertHtml(FxTask currentTask, File srcFile, Charset charset);

    public BaseBatchHtmlController() {
        baseTitle = message("Html");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Html);
    }

    public Charset chartset(File srcFile) {
        return TextFileTools.charset(srcFile);
    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            Charset charset = chartset(srcFile);
            String converted = covertHtml(currentTask, srcFile, charset);
            if (currentTask == null || !currentTask.isWorking()) {
                return message("Canceled");
            }
            if (converted == null) {
                return message("Failed");
            }
            TextFileTools.writeFile(target, converted, charset);
            targetFileGenerated(target);
            return message("Successful");
        } catch (Exception e) {
            MyBoxLog.error(e);
            return message("Failed");
        }
    }

    @Override
    public File makeTargetFile(File sourceFile, File targetPath) {
        try {
            String namePrefix = FileNameTools.prefix(sourceFile.getName());
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
