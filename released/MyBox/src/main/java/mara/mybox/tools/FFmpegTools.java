package mara.mybox.tools;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegProgress;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResult;
import com.github.kokorin.jaffree.ffmpeg.NullOutput;
import com.github.kokorin.jaffree.ffmpeg.ProgressListener;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2018-7-12
 * @Description
 * @License Apache License Version 2.0
 */
public class FFmpegTools {

    public static FFprobeResult FFprobleFrames(File FFprobleExcuatble, File mediaFile,
            String streams, String intervals) throws Exception {
        FFprobe probe = FFprobe.atPath(FFprobleExcuatble.toPath().getParent())
                .setShowFrames(true)
                .setInput(Paths.get(mediaFile.getAbsolutePath()));
        if (streams != null && !streams.trim().isEmpty()) {
            probe.setSelectStreams(streams.trim());
        }
        if (intervals != null && !intervals.trim().isEmpty()) {
            probe.setReadIntervals(intervals.trim());
        }
        return probe.execute();
    }

    public static FFprobeResult FFproblePackets(File FFprobleExcuatble, File mediaFile,
            String streams, String intervals) throws Exception {
        FFprobe probe = FFprobe.atPath(FFprobleExcuatble.toPath().getParent())
                .setShowPackets(true)
                .setInput(Paths.get(mediaFile.getAbsolutePath()));
        if (streams != null && !streams.trim().isEmpty()) {
            probe.setSelectStreams(streams.trim());
        }
        if (intervals != null && !intervals.trim().isEmpty()) {
            probe.setReadIntervals(intervals.trim());
        }
        return probe.execute();
    }

    public static void convert(File FFmpegExcuatble, File sourceFile, File targetFile) {
        // The most reliable way to get video duration
        // ffprobe for some formats can't detect duration
        final AtomicLong duration = new AtomicLong();
        final FFmpegResult nullResult = FFmpeg.atPath(FFmpegExcuatble.toPath())
                .addInput(UrlInput.fromPath(sourceFile.toPath()))
                .addOutput(new NullOutput())
                .setOverwriteOutput(true)
                .setProgressListener(new ProgressListener() {
                    @Override
                    public void onProgress(FFmpegProgress progress) {
                        duration.set(progress.getTimeMillis());
                    }
                })
                .execute();

        ProgressListener listener = new ProgressListener() {
            private final long lastReportTs = System.currentTimeMillis();

            @Override
            public void onProgress(FFmpegProgress progress) {
                long now = System.currentTimeMillis();
                if (lastReportTs + 1000 < now) {
                    long percent = 100 * progress.getTimeMillis() / duration.get();
                    MyBoxLog.debug("Progress: " + percent + "%");
                }
            }
        };

        FFmpegResult result = FFmpeg.atPath(FFmpegExcuatble.toPath())
                .addInput(UrlInput.fromPath(sourceFile.toPath()))
                .addOutput(UrlOutput.toPath(targetFile.toPath()))
                .setProgressListener(listener)
                .setOverwriteOutput(true)
                .execute();
    }

}
