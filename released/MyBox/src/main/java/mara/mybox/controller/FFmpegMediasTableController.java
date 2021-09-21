package mara.mybox.controller;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.github.kokorin.jaffree.ffprobe.Format;
import com.github.kokorin.jaffree.ffprobe.Stream;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.MediaInformation;
import mara.mybox.db.table.TableMedia;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-12-8
 * @Description
 * @License Apache License Version 2.0
 */
public class FFmpegMediasTableController extends ControlMediaTable {

    protected BaseBatchFFmpegController parent;

    public FFmpegMediasTableController() {
        sourceExtensionFilter = FileFilters.FFmpegMediaExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            examples = new ArrayList();
            examples.add("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");
            examples.add("http://download.oracle.com/otndocs/products/javafx/JavaRap/prog_index.m3u8");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void readMediaInfo(MediaInformation info, String msg) {
        try {
            parent = (BaseBatchFFmpegController) parentController;

            if (info == null || info.getAddress() == null
                    || parent == null || parent.ffmpegOptionsController.executable == null) {
                return;
            }
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        error = null;
                        try {
                            String address = info.getAddress();
                            if (info.getFile() != null) {
                                address = info.getFile().getAbsolutePath();
                            }
                            FFprobeResult probeResult
                                    = FFprobe.atPath(parent.ffmpegOptionsController.executable.toPath().getParent())
                                            .setShowFormat(true).setShowStreams(true)
                                            .setInput(address)
                                            .execute();
                            Format format = probeResult.getFormat();
                            if (format == null) {
                                error = Languages.message("InvalidData");
                                return true;
                            }
                            StringBuilder s = new StringBuilder();
                            if (format.getDuration() != null) {
                                info.setDuration(Math.round(format.getDuration() * 1000));
                                s.append(Languages.message("Duration")).append(": ").append(
                                        DateTools.timeDuration(Math.round(format.getDuration() * 1000))).append("\n");
                            }
                            if (format.getSize() != null) {
                                info.setFileSize(format.getSize());
                                s.append(Languages.message("Size")).append(": ").append(FileTools.showFileSize(format.getSize())).append("\n");
                            }

                            List<Stream> streams = probeResult.getStreams();
                            if (streams != null) {
                                for (int i = 0; i < streams.size(); ++i) {
                                    Stream stream = streams.get(i);
                                    if (stream.getCodecType() == StreamType.VIDEO) {
                                        info.setVideoEncoding(stream.getCodecLongName());
                                        s.append(Languages.message("VideoEncoding")).append(": ").append(info.getVideoEncoding()).append("\n");
                                    } else if (stream.getCodecType() == StreamType.AUDIO) {
                                        info.setAudioEncoding(stream.getCodecLongName());
                                        s.append(Languages.message("AudioEncoding")).append(": ").append(info.getAudioEncoding()).append("\n");
                                    }
                                    if (stream.getWidth() != null && stream.getHeight() != null) {
                                        String resolution = stream.getWidth() + "x" + stream.getHeight();
                                        info.setWidth(stream.getWidth());
                                        info.setHeight(stream.getHeight());
                                        s.append(Languages.message("Resolution")).append(": ").append(resolution).append("\n");
                                    }
                                }
                            }
                            info.setInfo(s.toString());
                            makeHtml(info, null);

                            TableMedia.write(info);
                        } catch (Exception e) {
                            error = e.toString();
                        }
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (error != null) {
                            popError(error);
                        }
                        tableView.refresh();
                        updateLabel();
                    }
                };
                parentController.start(task, msg);
            }

        } catch (Exception e) {
            popError(Languages.message("FailOpenMedia"));
            MyBoxLog.debug(e.toString());
        }
    }

}
