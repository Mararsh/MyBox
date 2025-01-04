package mara.mybox.image.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.FileNameTools;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import thridparty.image4j.ICODecoder;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 * @License Apache License Version 2.0
 */
public class ImageFileInformation extends FileInformation {

    protected String imageFormat, password;
    protected ImageInformation imageInformation;
    protected List<ImageInformation> imagesInformation;
    protected int numberOfImages;

    public ImageFileInformation() {
    }

    public ImageFileInformation(File file) {
        super(file);
        if (file != null) {
            imageFormat = FileNameTools.ext(file.getName()).toLowerCase();
        }
    }

    /*
        static methods
     */
    public static ImageFileInformation create(FxTask task, File file) {
        return create(task, file, null);
    }

    public static ImageFileInformation create(FxTask task, File file, String password) {
        try {
            if (file == null || !file.exists()) {
                return null;
            }
            String suffix = FileNameTools.ext(file.getName());
            if (suffix != null && suffix.equalsIgnoreCase("pdf")) {
                return readPDF(task, file, password);
            } else if (suffix != null && (suffix.equalsIgnoreCase("ppt") || suffix.equalsIgnoreCase("pptx"))) {
                return readPPT(task, file);
            } else if (suffix != null && (suffix.equalsIgnoreCase("ico") || suffix.equalsIgnoreCase("icon"))) {
                return readIconFile(task, file);
            } else {
                return readImageFile(task, file);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ImageFileInformation clone(ImageFileInformation sourceInfo, ImageFileInformation targetInfo) {
        if (sourceInfo == null || targetInfo == null) {
            return null;
        }
        FileInformation.clone(sourceInfo, targetInfo);
        targetInfo.imageFormat = sourceInfo.imageFormat;
        targetInfo.password = sourceInfo.password;
        targetInfo.imageInformation = sourceInfo.imageInformation;
        targetInfo.imagesInformation = sourceInfo.imagesInformation;
        targetInfo.createTime = sourceInfo.createTime;
        targetInfo.numberOfImages = sourceInfo.numberOfImages;
        return targetInfo;
    }

    public static ImageFileInformation readImageFile(FxTask task, File file) {
        return ImageFileReaders.readImageFileMetaData(task, file);
    }

    public static ImageFileInformation readIconFile(FxTask task, File file) {
        ImageFileInformation fileInfo = new ImageFileInformation(file);
        String format = "ico";
        fileInfo.setImageFormat(format);
        try {
            List<BufferedImage> frames = ICODecoder.read(file);
            if (frames != null) {
                int num = frames.size();
                fileInfo.setNumberOfImages(num);
                List<ImageInformation> imagesInfo = new ArrayList<>();
                ImageInformation imageInfo;
                for (int i = 0; i < num; i++) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    BufferedImage bufferedImage = frames.get(i);
                    imageInfo = ImageInformation.create(format, file);
                    imageInfo.setImageFileInformation(fileInfo);
                    imageInfo.setImageFormat(format);
                    imageInfo.setFile(file);
                    imageInfo.setCreateTime(fileInfo.getCreateTime());
                    imageInfo.setModifyTime(fileInfo.getModifyTime());
                    imageInfo.setFileSize(fileInfo.getFileSize());
                    imageInfo.setWidth(bufferedImage.getWidth());
                    imageInfo.setHeight(bufferedImage.getHeight());
                    imageInfo.setIsMultipleFrames(num > 1);
                    imageInfo.setIndex(i);
                    imageInfo.setImageType(BufferedImage.TYPE_INT_ARGB);
                    imageInfo.loadBufferedImage(bufferedImage);
                    imagesInfo.add(imageInfo);
                }
                fileInfo.setImagesInformation(imagesInfo);
                if (!imagesInfo.isEmpty()) {
                    fileInfo.setImageInformation(imagesInfo.get(0));
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return fileInfo;
    }

    public static ImageFileInformation readPDF(FxTask task, File file, String password) {
        ImageFileInformation fileInfo = new ImageFileInformation(file);
        String format = "png";
        fileInfo.setImageFormat(format);
        fileInfo.setPassword(password);
        try (PDDocument doc = Loader.loadPDF(file, password)) {
            int num = doc.getNumberOfPages();
            fileInfo.setNumberOfImages(num);
            List<ImageInformation> imagesInfo = new ArrayList<>();
            ImageInformation imageInfo;

            for (int i = 0; i < num; ++i) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                PDPage page = doc.getPage(i);
                PDRectangle rect = page.getBleedBox();
                imageInfo = ImageInformation.create(format, file);
                imageInfo.setImageFileInformation(fileInfo);
                imageInfo.setImageFormat(format);
                imageInfo.setPassword(password);
                imageInfo.setFile(file);
                imageInfo.setCreateTime(fileInfo.getCreateTime());
                imageInfo.setModifyTime(fileInfo.getModifyTime());
                imageInfo.setFileSize(fileInfo.getFileSize());
                imageInfo.setWidth(Math.round(rect.getWidth()));
                imageInfo.setHeight(Math.round(rect.getHeight()));
                imageInfo.setIsMultipleFrames(num > 1);
                imageInfo.setIndex(i);
                imageInfo.setImageType(BufferedImage.TYPE_INT_RGB);
                ImageInformation.checkMem(task, imageInfo);
                imagesInfo.add(imageInfo);
            }
            fileInfo.setImagesInformation(imagesInfo);
            if (!imagesInfo.isEmpty()) {
                fileInfo.setImageInformation(imagesInfo.get(0));
            }
            doc.close();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return fileInfo;
    }

    public static ImageFileInformation readPPT(FxTask task, File file) {
        ImageFileInformation fileInfo = new ImageFileInformation(file);
        String format = "png";
        fileInfo.setImageFormat(format);
        try (SlideShow ppt = SlideShowFactory.create(file)) {
            List<Slide> slides = ppt.getSlides();
            int num = slides.size();
            slides = null;
            fileInfo.setNumberOfImages(num);
            List<ImageInformation> imagesInfo = new ArrayList<>();
            ImageInformation imageInfo;
            int width = (int) Math.ceil(ppt.getPageSize().getWidth());
            int height = (int) Math.ceil(ppt.getPageSize().getHeight());
            for (int i = 0; i < num; ++i) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                imageInfo = ImageInformation.create(format, file);
                imageInfo.setImageFileInformation(fileInfo);
                imageInfo.setImageFormat(format);
                imageInfo.setFile(file);
                imageInfo.setCreateTime(fileInfo.getCreateTime());
                imageInfo.setModifyTime(fileInfo.getModifyTime());
                imageInfo.setFileSize(fileInfo.getFileSize());
                imageInfo.setWidth(width);
                imageInfo.setHeight(height);
                imageInfo.setIsMultipleFrames(num > 1);
                imageInfo.setIndex(i);
                imageInfo.setImageType(BufferedImage.TYPE_INT_ARGB);
                ImageInformation.checkMem(task, imageInfo);
                imagesInfo.add(imageInfo);
            }
            fileInfo.setImagesInformation(imagesInfo);
            if (!imagesInfo.isEmpty()) {
                fileInfo.setImageInformation(imagesInfo.get(0));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return fileInfo;
    }

    /*
        get/set
     */
    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    public List<ImageInformation> getImagesInformation() {
        return imagesInformation;
    }

    public void setImagesInformation(List<ImageInformation> imagesInformation) {
        this.imagesInformation = imagesInformation;
    }

    public ImageInformation getImageInformation() {
        return imageInformation;
    }

    public void setImageInformation(ImageInformation imageInformation) {
        this.imageInformation = imageInformation;
    }

    public int getNumberOfImages() {
        return numberOfImages;
    }

    public void setNumberOfImages(int numberOfImages) {
        this.numberOfImages = numberOfImages;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
