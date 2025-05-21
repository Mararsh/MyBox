package mara.mybox.controller;

//import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import java.io.File;
import java.text.MessageFormat;
import mara.mybox.data.MediaInformation;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileNameTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegConvertMediaStreamsController extends FFmpegConvertMediaFilesController {

    public FFmpegConvertMediaStreamsController() {
        baseTitle = message("FFmpegConvertMediaStreams");

    }

    @Override
    public String handleFile(FxTask currentTask, File srcFile, File targetPath) {
        String result;
        try {
            MediaInformation info = (MediaInformation) currentParameters.currentSourceFile;
            String address = info.getAddress();
            countHandling(address);
            showLogs(MessageFormat.format(message("HandlingObject"), address));
            showLogs(info.getInfo());

            String prefix, suffix;
            File file = new File(address);
            if (file.exists()) {
                prefix = FileNameTools.prefix(file.getName());
                suffix = FileNameTools.ext(file.getName());
            } else {
                int posSlash = address.lastIndexOf('/');

                if (posSlash < 0) {
                    prefix = address;
                } else {
                    prefix = address.substring(posSlash);
                }
                int posDot = prefix.lastIndexOf('.');
                if (posDot >= 0) {
                    prefix = prefix.substring(0, posDot);
                    suffix = prefix.substring(posDot);
                } else {
                    suffix = "";
                }
            }
            String ext = ffmpegOptionsController.extensionInput.getText().trim();
            if (ext.isEmpty() || message("OriginalFormat").equals(ext)) {
                ext = suffix;
            }

            File target = makeTargetFile(prefix, "." + ext, targetPath);
            if (target == null) {
                result = message("Skip");
            } else {
                updateLogs(message("TargetFile") + ": " + target, true);
                convert(currentTask, address, target);
                result = message("Successful");
            }
        } catch (Exception e) {
            showLogs(e.toString());
            result = message("Failed");
        }
        return result;
    }

}
