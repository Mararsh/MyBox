package mara.mybox.controller;

import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import mara.mybox.data.FileInformation;
import mara.mybox.data.MediaInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegConvertMediaStreamsController extends FFmpegConvertMediaFilesController {

    public FFmpegConvertMediaStreamsController() {
        baseTitle = AppVariables.message("FFmpegConvertMediaStreams");

    }

    @Override
    public boolean makeBatchParameters() {
        if (tableData == null || tableData.isEmpty()) {
            actualParameters = null;
            return false;
        }
        for (int i = 0; i < tableData.size(); i++) {
            FileInformation d = tableController.fileInformation(i);
            if (d == null) {
                continue;
            }
            d.setHandled("");
        }
        initLogs();
        totalHandled = 0;
        startTime = new Date();
        return true;
    }

    @Override
    public void doCurrentProcess() {
        try {
            if (currentParameters == null || tableData.isEmpty()) {
                return;
            }
            synchronized (this) {
                if (task != null) {
                    return;
                }
                currentParameters.startTime = new Date();
                currentParameters.currentTotalHandled = 0;
                updateInterface("Started");
                task = new SingletonTask<Void>() {

                    @Override
                    public Void call() {
                        int len = tableData.size();
                        updateTaskProgress(currentParameters.currentIndex, len);

                        for (; currentParameters.currentIndex < len; currentParameters.currentIndex++) {
                            if (isCancelled()) {
                                break;
                            }

                            handleCurrentFile();

                            updateTaskProgress(currentParameters.currentIndex + 1, len);

                            if (isCancelled() || isPreview) {
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
                        quitProcess();
                        task = null;
                    }

                };
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            updateInterface("Failed");
            logger.error(e.toString());
        }
    }

    @Override
    public void handleCurrentFile() {
        String result;
        try {
            MediaInformation info = (MediaInformation) tableData.get(currentParameters.currentIndex);
            String address = info.getAddress();
            showHandling(address);
            tableController.markFileHandling(currentParameters.currentIndex);
            updateLogs(MessageFormat.format(message("HandlingObject"), address), true);
//            String s = message("Duration") + ": " + DateTools.showDuration(info.getDuration());
//            s += "  " + info.getResolution() + "  " + info.getVideoEncoding() + "  " + info.getAudioEncoding();
            updateLogs(info.getInfo(), true);

            String prefix, suffix;
            File file = new File(address);
            if (file.exists()) {
                prefix = FileTools.getFilePrefix(file.getName());
                suffix = FileTools.getFileSuffix(file.getName());
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
            String ext = extensionInput.getText().trim();
            if (ext.isEmpty() || message("OriginalFormat").equals(ext)) {
                ext = suffix;
            }

            File target = makeTargetFile(prefix, "." + ext, targetPath);
            if (target == null) {
                result = AppVariables.message("Skip");
            } else {
                updateLogs(message("TargetFile") + ": " + target, true);
                convert(address, target, info.getDuration());
                actualParameters.finalTargetName = target.getAbsolutePath();
                targetFiles.add(target);
                updateLogs(MessageFormat.format(message("FilesGenerated"), target), true);
                result = AppVariables.message("Successful");
            }
            totalHandled++;
            currentParameters.currentTotalHandled++;
        } catch (Exception e) {
            updateLogs(e.toString(), true);
            result = AppVariables.message("Failed");
        }
        tableController.markFileHandled(currentParameters.currentIndex, result);
    }

    protected void convert(String address, File targetFile, long duration)
            throws Exception {
        convert(UrlInput.fromUrl(address), targetFile, duration);
    }
}
