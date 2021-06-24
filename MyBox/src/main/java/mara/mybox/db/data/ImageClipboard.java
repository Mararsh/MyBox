/*
 * Apache License Version 2.0
 */
package mara.mybox.db.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.Random;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import mara.mybox.controller.ImageManufactureController;
import mara.mybox.db.table.TableImageClipboard;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-9-3
 * @License Apache License Version 2.0
 */
public class ImageClipboard extends BaseData {

    protected long icid;
    protected File imageFile, thumbnailFile;
    protected Image image, thumbnail;
    protected int width, height;
    protected ImageSource source;
    protected String sourceName;
    protected Date createTime;
    protected ImageClipboard self;

    public static enum ImageSource {
        SystemClipBoard, Copy, Crop, File, Example, Unknown
    }

    private void init() {
        source = ImageSource.Unknown;
        createTime = new Date();
        self = this;
    }

    public ImageClipboard() {
        init();
    }

    public Image loadImage() {
        image = loadImage(this);
        return image;
    }

    public Image loadThumb() {
        thumbnail = loadThumb(this);
        return thumbnail;
    }

    /*
        Static methods
     */
    public static ImageClipboard create() {
        return new ImageClipboard();
    }

    public static boolean setValue(ImageClipboard data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "icid":
                    data.setIcid(value == null ? -1 : (long) value);
                    return true;
                case "image_file":
                    data.setImageFile(null);
                    if (value != null) {
                        File f = new File((String) value);
                        if (f.exists()) {
                            data.setImageFile(f);
                        }
                    }
                    return true;
                case "thumbnail_file":
                    data.setThumbnailFile(null);
                    if (value != null) {
                        File f = new File((String) value);
                        if (f.exists()) {
                            data.setThumbnailFile(f);
                        }
                    }
                    return true;
                case "width":
                    data.setWidth(value == null ? CommonValues.InvalidInteger : (int) value);
                    return true;
                case "height":
                    data.setHeight(value == null ? CommonValues.InvalidInteger : (int) value);
                    return true;
                case "source":
                    short s = value == null ? -1 : (short) value;
                    data.setSource(source(s));
                    return true;
                case "create_time":
                    data.setCreateTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(ImageClipboard data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "icid":
                return data.getIcid();
            case "image_file":
                return data.getImageFile() != null ? data.getImageFile().getAbsolutePath() : null;
            case "thumbnail_file":
                return data.getThumbnailFile() != null ? data.getThumbnailFile().getAbsolutePath() : null;
            case "width":
                return data.getWidth();
            case "height":
                return data.getHeight();
            case "source":
                return source(data.getSource());
            case "create_time":
                return data.getCreateTime();
        }
        return null;
    }

    public static boolean valid(ImageClipboard data) {
        return data != null && data.getImageFile() != null;
    }

    public static ImageSource source(short value) {
        for (ImageSource source : ImageSource.values()) {
            if (source.ordinal() == value) {
                return source;
            }
        }
        return ImageSource.Unknown;
    }

    public static String sourceName(short value) {
        for (ImageSource source : ImageSource.values()) {
            if (source.ordinal() == value) {
                return source.name();
            }
        }
        return ImageSource.Unknown.name();
    }

    public static ImageSource source(String name) {
        for (ImageSource source : ImageSource.values()) {
            if (source.name().equals(name)) {
                return source;
            }
        }
        return ImageSource.Unknown;
    }

    public static short source(ImageSource value) {
        if (value == null || value == ImageSource.Unknown) {
            return -1;
        }
        return (short) value.ordinal();
    }

    public static ImageClipboard create(BufferedImage image, ImageSource source) {
        try {
            if (image == null) {
                return null;
            }
            String prefix = AppVariables.getImageClipboardPath() + File.separator
                    + (new Date().getTime()) + "_" + new Random().nextInt(1000);
            String imageFile = prefix + ".png";
            while (new File(imageFile).exists()) {
                prefix = AppVariables.getImageClipboardPath() + File.separator
                        + (new Date().getTime()) + "_" + new Random().nextInt(1000);
                imageFile = prefix + ".png";
            }
            if (!ImageFileWriters.writeImageFile(image, "png", imageFile)) {
                return null;
            }
            String thumbFile = prefix + "_thumbnail.png";
            BufferedImage thumbnail = ImageManufacture.scaleImageWidthKeep(image,
                    AppVariables.getUserConfigInt("ThumbnailWidth", 100));
            if (thumbnail == null) {
                return null;
            }
            if (!ImageFileWriters.writeImageFile(thumbnail, "png", thumbFile)) {
                return null;
            }
            ImageClipboard clip = ImageClipboard.create()
                    .setSource(source)
                    .setImageFile(new File(imageFile))
                    .setThumbnailFile(new File(thumbFile))
                    .setWidth(image.getWidth()).setHeight(image.getHeight())
                    .setThumbnail(SwingFXUtils.toFXImage(thumbnail, null))
                    .setCreateTime(new Date());
            return clip;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static ImageClipboard create(Image image, ImageSource source) {
        try {
            if (image == null) {
                return null;
            }
            return create(SwingFXUtils.fromFXImage(image, null), source);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static ImageClipboard add(Image image, ImageSource source) {
        return add(image, source, true);
    }

    public static ImageClipboard add(Image image, ImageSource source, boolean putSystemClipboard) {
        try {
            return add(SwingFXUtils.fromFXImage(image, null), source, putSystemClipboard);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static ImageClipboard add(File file) {
        return add(file, true);
    }

    public static ImageClipboard add(File file, boolean putSystemClipboard) {
        try {
            BufferedImage bufferImage = ImageFileReaders.readImage(file);
            return add(bufferImage, ImageSource.File, putSystemClipboard);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static ImageClipboard add(BufferedImage image, ImageSource source) {
        return add(image, source, true);
    }

    public static ImageClipboard add(BufferedImage image, ImageSource source, boolean putSystemClipboard) {
        try {
            ImageClipboard clip = ImageClipboard.create(image, source);
            if (clip == null) {
                return null;
            }
            new TableImageClipboard().insertData(clip);

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (putSystemClipboard) {
                        ClipboardContent cc = new ClipboardContent();
                        cc.putImage(SwingFXUtils.toFXImage(image, null));
                        Clipboard.getSystemClipboard().setContent(cc);
                    }
                    ImageManufactureController.refreshClipboards();
                }
            });

            return clip;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static Image loadImage(ImageClipboard clip) {
        try {
            if (clip == null) {
                return null;
            }
            if (clip.getImage() != null) {
                return clip.getImage();
            }
            File imageFile = clip.getImageFile();
            if (imageFile == null || !imageFile.exists()) {
                return clip.getThumbnail();
            }
            BufferedImage bfImage = ImageFileReaders.readImage(imageFile);
            if (bfImage == null) {
                return null;
            }
            Image image = SwingFXUtils.toFXImage(bfImage, null);
            return image;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static Image loadThumb(ImageClipboard clip) {
        try {
            if (clip == null) {
                return null;
            }
            if (clip.getThumbnail() != null) {
                return clip.getThumbnail();
            }
            File thumbFile = clip.getThumbnailFile();
            if (thumbFile == null || !thumbFile.exists()) {
                return clip.getImage();
            }
            BufferedImage bfImage = ImageFileReaders.readImage(thumbFile);
            if (bfImage == null) {
                return null;
            }
            Image thumb = SwingFXUtils.toFXImage(bfImage, null);
            return thumb;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    /*
        get/set
     */
    public File getImageFile() {
        return imageFile;
    }

    public ImageClipboard setImageFile(File imageFile) {
        this.imageFile = imageFile;
        return this;
    }

    public File getThumbnailFile() {
        return thumbnailFile;
    }

    public ImageClipboard setThumbnailFile(File thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
        return this;
    }

    public Image getImage() {
        return image;
    }

    public ImageClipboard setImage(Image image) {
        this.image = image;
        return this;
    }

    public Image getThumbnail() {
        return thumbnail;
    }

    public ImageClipboard setThumbnail(Image thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public ImageClipboard setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public ImageClipboard setHeight(int height) {
        this.height = height;
        return this;
    }

    public ImageSource getSource() {
        return source;
    }

    public ImageClipboard setSource(ImageSource source) {
        this.source = source;
        if (source != null) {
            sourceName = source.name();
        } else {
            sourceName = null;
        }
        return this;
    }

    public String getSourceName() {
        if (source != null) {
            sourceName = source.name();
        }
        return sourceName;
    }

    public ImageClipboard setSourceName(String sourceName) {
        this.sourceName = sourceName;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public ImageClipboard setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public long getIcid() {
        return icid;
    }

    public ImageClipboard setIcid(long icid) {
        this.icid = icid;
        return this;
    }

    public ImageClipboard getSelf() {
        return self;
    }

    public void setSelf(ImageClipboard self) {
        this.self = self;
    }

}
