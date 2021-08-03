package mara.mybox.controller;

import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import mara.mybox.data.MediaInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegConvertMediaStreamsController extends FFmpegConvertMediaFilesController {

    public FFmpegConvertMediaStreamsController() {
        baseTitle = Languages.message("FFmpegConvertMediaStreams");

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
                task = new SingletonTask<Void>() {

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
                    protected void taskQuit() {
                        super.taskQuit();
                        quitProcess();
                        task = null;
                    }

                };
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
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
            updateLogs(MessageFormat.format(Languages.message("HandlingObject"), address), true);
            updateLogs(info.getInfo(), true);
//            String s = message("Duration") + ": " + DateTools.showDuration(info.getDuration());
//            s += "  " + info.getResolution() + "  " + info.getVideoEncoding() + "  " + info.getAudioEncoding();

            String prefix, suffix;
            File file = new File(address);
            if (file.exists()) {
                prefix = FileNameTools.getFilePrefix(file.getName());
                suffix = FileNameTools.getFileSuffix(file.getName());
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
            if (ext.isEmpty() || Languages.message("OriginalFormat").equals(ext)) {
                ext = suffix;
            }

            File target = makeTargetFile(prefix, "." + ext, targetPath);
            if (target == null) {
                result = Languages.message("Skip");
            } else {
                updateLogs(Languages.message("TargetFile") + ": " + target, true);
                convert(address, target, info.getDuration());
                targetFileGenerated(target);
                result = Languages.message("Successful");
            }
        } catch (Exception e) {
            updateLogs(e.toString(), true);
            result = Languages.message("Failed");
        }
        tableController.markFileHandled(currentParameters.currentIndex, result);
    }

    protected void convert(String address, File targetFile, long duration)
            throws Exception {
        convert(UrlInput.fromUrl(address), targetFile, duration);
    }
}
