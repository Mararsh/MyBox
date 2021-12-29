/*
 * Apache License Version 2.0
 */
package mara.mybox.data;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.Track;
import javafx.util.Duration;
import mara.mybox.db.table.TableMedia;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 *
 * @author mara
 */
public class MediaInformation extends FileInformation {

    protected String address, resolution, info, videoEncoding, audioEncoding;
    protected boolean isCurrent = false, finish = false;
    protected int width, height;
    protected String html;

    public MediaInformation(File file) {
        super(file);
        if (file != null) {
            address = file.getAbsolutePath();
        }
    }

    public MediaInformation(String address) {
        this.address = address;
    }

    public MediaInformation(URI uri) {
        this.address = uri.toString();
    }

    public static String exceptionMessage(MediaException exception) {
        String msg = "";
        switch (exception.getType()) {
            case MEDIA_UNSUPPORTED:
                String errorMsg = exception.getMessage();
                if (errorMsg.contains("ERROR_MEDIA_AUDIO_FORMAT_UNSUPPORTED")) {
                    msg = Languages.message("AUDIO_FORMAT_UNSUPPORTED");
                } else if (errorMsg.contains("ERROR_MEDIA_VIDEO_FORMAT_UNSUPPORTED")) {
                    msg = Languages.message("VIDEO_FORMAT_UNSUPPORTED");
                } else if (errorMsg.contains("ERROR_MEDIA_AUDIO_FORMAT_UNSUPPORTED")) {
                    msg = Languages.message("MEDIA_UNSUPPORTED");
                }
                break;
            case MEDIA_CORRUPTED:
            case MEDIA_INACCESSIBLE:
            case MEDIA_UNAVAILABLE:
            case MEDIA_UNSPECIFIED:
            case OPERATION_UNSUPPORTED:
                msg = Languages.message(exception.getType().name());
                break;
            default:
                msg = Languages.message("PLAYBACK_ERROR");
                break;
        }
        return msg;
    }

    public URI getURI() {
        try {
            if (file != null) {
                return file.toURI();
            } else {
                File f = new File(address);
                if (f.exists()) {
                    setFileAttributes(f);
                    return file.toURI();
                } else {
                    return new URI(address);
                }
            }
        } catch (Exception e) {
            return null;
        }
    }

    public boolean readMediaInfo(Media media) {
        try {
            if (address == null || media == null) {
                return false;
            }
            StringBuilder s = new StringBuilder();
            s.append(message("Address")).append(getURI().toString()).append("\n");

            Duration dur = media.getDuration();
            if (dur != null) {
                setDuration(Math.round(dur.toMillis()));
                s.append(message("Duration")).append(": ").append(DateTools.datetimeMsDuration(getDuration())).append("\n");
            }

            if (media.getWidth() > 0 && media.getHeight() > 0) {
                setWidth(media.getWidth());
                setHeight(media.getHeight());
                s.append(message("Resolution")).append(": ").append(getResolution()).append("\n");
            }
            if (getFileSize() > 0) {
                s.append(message("Size")).append(": ").append(FileTools.showFileSize(getFileSize())).append("\n");
            }

            Map<String, Object> meta = media.getMetadata();
            if (meta != null && !meta.isEmpty()) {
                for (String mk : meta.keySet()) {
                    s.append(mk).append(": ").append(meta.get(mk).toString()).append("\n");
                }
            }
            Map<String, Duration> markers = media.getMarkers();
            if (markers != null && !markers.isEmpty()) {
                for (String mk : markers.keySet()) {
                    s.append(mk).append(": ").append(markers.get(mk).toString()).append("\n");
                }
            }
            List<Track> tracks = media.getTracks();

            if (tracks != null && !tracks.isEmpty()) {
                for (Track track : tracks) {
                    String name = "";
                    s.append("Track: ").append(track.getTrackID()).append("\n");

                    if (track.getName() != null) {
                        s.append("Name: ").append(track.getName()).append("\n");
                        name = track.getName();
                    }
                    if (track.getLocale() != null) {
                        s.append("Locale: ").append(track.getLocale().getDisplayName()).append("\n");
                    }
                    Map<String, Object> trackMeta = track.getMetadata();
                    if (trackMeta != null && !trackMeta.isEmpty()) {
                        for (String mk : trackMeta.keySet()) {
                            s.append(mk).append(": ").append(trackMeta.get(mk).toString()).append("\n");
                            if (mk.toLowerCase().contains("encoding")) {
                                if (name.toLowerCase().contains("audio")) {
                                    setAudioEncoding(name + " " + trackMeta.get(mk).toString());
                                } else if (name.toLowerCase().contains("video")) {
                                    setVideoEncoding(name + " " + trackMeta.get(mk).toString());
                                }
                            }
                        }
                    }
                }
            }
            setInfo(s.toString());
            setFinish(true);
            makeHtml(media);
            TableMedia.write(this);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            setFinish(true);
            return false;
        }

    }

    public void makeHtml(Media media) {
        if (info == null) {
            return;
        }
        StringBuilder s = new StringBuilder();
        s.append("<h1  class=\"center\">").append(getAddress()).append("</h1>\n");
        s.append("<hr>\n");
        List<String> names = new ArrayList<>();
        names.addAll(Arrays.asList(message("Name"), message("Value")));
        StringTable table = new StringTable(names);
        List<String> row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Duration"), DateTools.datetimeMsDuration(getDuration())));
        table.add(row);
        row = new ArrayList<>();
        row.addAll(Arrays.asList(message("Resolution"), getResolution()));
        table.add(row);
        if (getFileSize() > 0) {
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("Size"), FileTools.showFileSize(getFileSize())));
            table.add(row);
        }
        if (getVideoEncoding() != null) {
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("VideoEncoding"), getVideoEncoding()));
            table.add(row);
        }
        if (getAudioEncoding() != null) {
            row = new ArrayList<>();
            row.addAll(Arrays.asList(message("AudioEncoding"), getAudioEncoding()));
            table.add(row);
        }
        s.append(StringTable.tableDiv(table));

        if (media == null) {
            setHtml(s.toString());
            return;
        }
        Map<String, Object> meta = media.getMetadata();
        if (meta != null && !meta.isEmpty()) {
            s.append("<h2  class=\"center\">").append("MetaData").append("</h2>\n");
            names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Meta"), message("Value")));
            table = new StringTable(names);
            for (String mk : meta.keySet()) {
                row = new ArrayList<>();
                row.addAll(Arrays.asList(mk, meta.get(mk).toString()));
                table.add(row);
            }
            s.append(StringTable.tableDiv(table));
        }

        Map<String, Duration> markers = media.getMarkers();
        if (markers != null && !markers.isEmpty()) {
            s.append("<h2  class=\"center\">").append("Markers").append("</h2>\n");
            names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Marker"), message("Value")));
            table = new StringTable(names);
            for (String mk : markers.keySet()) {
                row = new ArrayList<>();
                row.addAll(Arrays.asList(mk, markers.get(mk).toString()));
                table.add(row);
            }
            s.append(StringTable.tableDiv(table));
        }

        List<Track> tracks = media.getTracks();
        if (tracks != null && !tracks.isEmpty()) {
            s.append("<h2  class=\"center\">").append("Tracks").append("</h2>\n");
            for (Track track : tracks) {
                s.append("<h3  class=\"center\">").append("trackID:").append(track.getTrackID()).append("</h3>\n");
                names = new ArrayList<>();
                names.addAll(Arrays.asList(message("Name"), message("Value")));
                table = new StringTable(names);
                if (track.getName() != null) {
                    row = new ArrayList<>();
                    row.addAll(Arrays.asList("Name", track.getName()));
                    table.add(row);
                }
                if (track.getLocale() != null) {
                    row = new ArrayList<>();
                    row.addAll(Arrays.asList("Locale", track.getLocale().getDisplayName()));
                    table.add(row);
                }
                Map<String, Object> trackMeta = track.getMetadata();
                if (trackMeta != null && !trackMeta.isEmpty()) {
                    for (String mk : trackMeta.keySet()) {
                        row = new ArrayList<>();
                        row.addAll(Arrays.asList(mk, trackMeta.get(mk).toString()));
                        table.add(row);
                    }
                }
                s.append(StringTable.tableDiv(table));
            }
        }
        setHtml(s.toString());

    }

    /*
        get/set
     */
    public String getVideoEncoding() {
        if (videoEncoding != null) {
            return videoEncoding;
        } else {
            return "";
        }
    }

    public void setVideoEncoding(String videoEncoding) {
        this.videoEncoding = videoEncoding;
    }

    public String getAudioEncoding() {
        if (audioEncoding != null) {
            return audioEncoding;
        } else {
            return "";
        }
    }

    public void setAudioEncoding(String audioEncoding) {
        this.audioEncoding = audioEncoding;
    }

    public String getResolution() {
        if (width > 0 && height > 0) {
            return width + "x" + height;
        } else {
            return "";
        }
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getAddress() {
        return address;
    }

    public boolean isIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public String getInfo() {
        if (info != null) {
            return info;
        } else {
            return "";
        }
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getHtml() {
        if (html != null) {
            return html;
        } else {
            return "";
        }
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

}
