package mara.mybox.controller;

//import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import mara.mybox.data.MediaInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
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
    public void doCurrentProcess() {
        try {
            if (currentParameters == null || tableData.isEmpty()) {
                return;
            }
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                processStartTime = new Date();
                totalFilesHandled = 0;
                updateInterface("Started");
                task = new SingletonTask<Void>(this) {

                    @Override
                    public Void call() {
                        int len = tableData.size();
                        updateTaskProgress(currentParameters.currentIndex, len);

                        for (; currentParameters.currentIndex < len;
                                currentParameters.currentIndex++) {
                            if (task == null || isCancelled()) {
                                break;
                            }

                            handleCurrentFile();

                            updateTaskProgress(currentParameters.currentIndex + 1, len);

                            if (task == null || isCancelled() || isPreview) {
                                break;
                            }
                        }
                        updateTaskProgress(currentParameters.currentIndex, len);
                        ok = true;

                        return null;
                    }

                    @Override
                    public void succeeded() {
                        super.succeeded();
                        updateInterface("Done");
                    }

                    @Override
                    public void cancelled() {
                        super.cancelled();
                        updateInterface("Canceled");
                    }

                    @Override
                    public void failed() {
                        super.failed();
                        updateInterface("Failed");
                    }

                    @Override
                    protected void finalAction() {
                        super.finalAction();
                        task = null;
                        afterTask();
                    }

                };
                start(task, false);
            }

        } catch (Exception e) {
            updateInterface("Failed");
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void handleCurrentFile() {
        String result;
        try {
            MediaInformation info = (MediaInformation) tableData.get(currentParameters.currentIndex);
            String address = info.getAddress();
            countHandling(address);
            tableController.markFileHandling(currentParameters.currentIndex);
            showLogs(MessageFormat.format(message("HandlingObject"), address));
            showLogs(info.getInfo());

            String prefix, suffix;
            File file = new File(address);
            if (file.exists()) {
                prefix = FileNameTools.prefix(file.getName());
                suffix = FileNameTools.suffix(file.getName());
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
                convert(address, target);
                result = message("Successful");
            }
        } catch (Exception e) {
            showLogs(e.toString());
            result = message("Failed");
        }
        tableController.markFileHandled(currentParameters.currentIndex, result);
    }

}
