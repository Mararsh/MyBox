/*
 * Apache License Version 2.0
 */
package mara.mybox.data;

import java.io.File;
import java.net.URI;
import javafx.scene.media.MediaException;
import mara.mybox.value.Languages;

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

    public boolean ready2() {
//        MyBoxLog.console(finish + " "
//                + (videoEncoding == null ? "null" : videoEncoding) + " "
//                + (audioEncoding == null ? "null" : audioEncoding));
        return getURI() != null && finish;
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
