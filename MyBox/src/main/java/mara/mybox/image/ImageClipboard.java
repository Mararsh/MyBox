/*
 * Apache License Version 2.0
 */
package mara.mybox.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2019-9-3
 * @License Apache License Version 2.0
 */
public class ImageClipboard {

    public final static String ClipBoardKey = "ImageClipboard";

    protected File imageFile, thumbnailFile;
    protected Image image, thumbnail;
    protected int width, height;
    protected ImageFileInformation info;

    public String size() {
        if (info == null || info.getImageInformation() == null) {
            return "";
        }
        return info.getImageInformation().getPixelsString();
    }

    public Image image() {
        if (imageFile == null) {
            return null;
        }
        if (image != null) {
            return image;
        }
        ImageClipboard clip = clip(imageFile.getAbsolutePath(), true, false);
        if (clip == null) {
            return null;
        }
        image = clip.getImage();
        return image;
    }

    public static ImageClipboard create() {
        return new ImageClipboard();
    }

    public static int max() {
        return AppVariables.getUserConfigInt("ImageClipboardMax", 20);
    }

    public static List<String> read() {
        List<String> names = TableStringValues.max(ClipBoardKey, max());
        File[] files = new File(AppVariables.getImageClipboardPath()).listFiles();
        if (files != null) {
            for (File file : files) {
                String filename = file.getAbsolutePath();
                if (filename.endsWith("_thumbnail.png")) {
                    filename = filename.substring(0, filename.length() - 14) + ".png";
                }
                if (!names.contains(filename)) {
                    FileTools.delete(file);
                }
            }
        }
        return names;
    }

    public static List<ImageClipboard> thumbnails() {
        List<ImageClipboard> thumbnails = new ArrayList<>();
        try {
            List<String> names = read();
            for (String name : names) {
                ImageClipboard thumb = thumbnail(name);
                if (thumb != null) {
                    thumbnails.add(thumb);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return thumbnails;
    }

    public static ImageClipboard clip(String name, boolean readImage, boolean readThumb) {
        try {
            if (name == null) {
                return null;
            }
            File imageFile = new File(name);
            if (!imageFile.exists()) {
                ImageClipboard.delete(name);
                return null;
            }
            ImageFileInformation info = ImageFileReaders.readImageFileMetaData(name);
            Image image = null;
            if (readImage) {
                BufferedImage bfImage = ImageFileReaders.readImage(imageFile);
                if (bfImage == null) {
                    return null;
                }
                image = SwingFXUtils.toFXImage(bfImage, null);
                if (image == null) {
                    return null;
                }
            }

            File thumbFile = new File(name.substring(0, name.length() - 4) + "_thumbnail.png");
            if (!thumbFile.exists()) {
                ImageClipboard.delete(name);
                return null;
            }
            Image thumbImage = null;
            if (readThumb) {
                BufferedImage thumbnail = ImageFileReaders.readImage(thumbFile);
                if (thumbnail == null) {
                    return null;
                }
                thumbImage = SwingFXUtils.toFXImage(thumbnail, null);
                if (thumbImage == null) {
                    return null;
                }
            }
            return ImageClipboard.create().setInfo(info).setImage(image).setImageFile(imageFile).
                    setThumbnailFile(thumbFile).setThumbnail(thumbImage);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageClipboard image(String name) {
        return clip(name, true, false);
    }

    public static ImageClipboard thumbnail(String name) {
        return clip(name, false, true);
    }

    public static ImageClipboard last() {
        try {
            String name = TableStringValues.last(ClipBoardKey);
            if (name == null) {
                return null;
            }
            return clip(name, true, false);
        } catch (Exception e) {
            return null;
        }
    }

    public static String add(Image image) {
        return add(image, AppVariables.getUserConfigBoolean("CopyToSystemClipboard", true));
    }

    public static String add(Image image, boolean putSystemClipboard) {
        try {
            return add(SwingFXUtils.fromFXImage(image, null), putSystemClipboard);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static String add(File file) {
        return add(file, AppVariables.getUserConfigBoolean("CopyToSystemClipboard", true));
    }

    public static String add(File file, boolean putSystemClipboard) {
        try {
            BufferedImage bufferImage = ImageFileReaders.readImage(file);
            return add(bufferImage, true);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    public static String add(BufferedImage image) {
        return add(image, AppVariables.getUserConfigBoolean("CopyToSystemClipboard", true));
    }

    public static String add(BufferedImage image, boolean putSystemClipboard) {
        try {
            String filename = AppVariables.getImageClipboardPath() + File.separator
                    + (new Date().getTime()) + "_" + new Random().nextInt(1000);
            while (new File(filename + ".png").exists()) {
                filename = AppVariables.getImageClipboardPath() + File.separator
                        + (new Date().getTime()) + "_" + new Random().nextInt(1000);
            }
            if (!ImageFileWriters.writeImageFile(image, "png", filename + ".png")) {
                return null;
            }
            BufferedImage thumbnail = ImageManufacture.scaleImageWidthKeep(image,
                    AppVariables.getUserConfigInt("ThumbnailWidth", 100));
            if (thumbnail == null) {
                return null;
            }
            if (!ImageFileWriters.writeImageFile(thumbnail, "png", filename + "_thumbnail.png")) {
                return null;
            }
            TableStringValues.add(ClipBoardKey, filename + ".png");
            TableStringValues.max(ClipBoardKey, max());

            if (putSystemClipboard) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ClipboardContent cc = new ClipboardContent();
                        cc.putImage(SwingFXUtils.toFXImage(image, null));
                        Clipboard.getSystemClipboard().setContent(cc);
                    }
                });
            }

            return filename + ".png";
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }

    }

    public static boolean delete(String name) {
        FileTools.delete(name);
        if (name.endsWith(".png")) {
            FileTools.delete(name.substring(0, name.length() - 4) + "_thumbnail.png");
        }
        return TableStringValues.delete(ClipBoardKey, name);
    }

    public static void clear() {
        try {
            FileTools.clearDir(new File(AppVariables.getImageClipboardPath()));
            TableStringValues.clear(ClipBoardKey);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

    public ImageFileInformation getInfo() {
        return info;
    }

    public ImageClipboard setInfo(ImageFileInformation info) {
        this.info = info;
        return this;
    }

}
