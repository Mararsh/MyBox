package mara.mybox.bufferedimage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.FileInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.AppVariables;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageFileInformation extends FileInformation {

    protected String imageFormat;
    protected ImageInformation imageInformation;
    protected List<ImageInformation> imagesInformation;
    protected int numberOfImages;

    public ImageFileInformation() {
    }

    public ImageFileInformation(File file) {
        super(file);
        imageFormat = FileNameTools.getFileSuffix(fileName).toLowerCase();
    }

    /*
        static methods
     */
    public static ImageFileInformation create(File file) {
        try {
            if (file == null || !file.exists()) {
                return null;
            }
            String suffix = FileNameTools.getFileSuffix(file);
            if (suffix != null && suffix.equalsIgnoreCase("pdf")) {
                return readPDF(file);
            } else if (suffix != null && (suffix.equalsIgnoreCase("ppt") || suffix.equalsIgnoreCase("pptx"))) {
                return readPPT(file);
            } else {
                return readImageFile(file);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static ImageFileInformation readImageFile(File file) {
        return ImageFileReaders.readImageFileMetaData(file);
    }

    public static ImageFileInformation readPDF(File file) {
        ImageFileInformation fileInfo = new ImageFileInformation(file);
        String format = "png";
        fileInfo.setImageFormat(format);
        try ( PDDocument doc = PDDocument.load(file, AppVariables.pdfMemUsage)) {
            int num = doc.getNumberOfPages();
            fileInfo.setNumberOfImages(num);
            List<ImageInformation> imagesInfo = new ArrayList<>();
            ImageInformation imageInfo;
            for (int i = 0; i < num; ++i) {
                PDPage page = doc.getPage(i);
                PDRectangle rect = page.getBleedBox();
                imageInfo = ImageInformation.create(format, file);
                imageInfo.setImageFileInformation(fileInfo);
                imageInfo.setImageFormat(format);
                imageInfo.setFileName(fileInfo.getFileName());
                imageInfo.setCreateTime(fileInfo.getCreateTime());
                imageInfo.setModifyTime(fileInfo.getModifyTime());
                imageInfo.setFileSize(fileInfo.getFileSize());
                imageInfo.setWidth(Math.round(rect.getWidth()));
                imageInfo.setHeight(Math.round(rect.getHeight()));
                imageInfo.setIsMultipleFrames(num > 1);
                imageInfo.setIndex(i);
                imageInfo.setImageType(BufferedImage.TYPE_INT_RGB);
                ImageInformation.countMaxWidth(imageInfo);
                imagesInfo.add(imageInfo);
            }
            fileInfo.setImagesInformation(imagesInfo);
            if (!imagesInfo.isEmpty()) {
                fileInfo.setImageInformation(imagesInfo.get(0));
            }
            doc.close();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return fileInfo;
    }

    public static ImageFileInformation readPPT(File file) {
        ImageFileInformation fileInfo = new ImageFileInformation(file);
        String format = "png";
        fileInfo.setImageFormat(format);
        try ( SlideShow ppt = SlideShowFactory.create(file)) {
            List<Slide> slides = ppt.getSlides();
            int num = slides.size();
            slides = null;
            fileInfo.setNumberOfImages(num);
            List<ImageInformation> imagesInfo = new ArrayList<>();
            ImageInformation imageInfo;
            int width = (int) Math.ceil(ppt.getPageSize().getWidth());
            int height = (int) Math.ceil(ppt.getPageSize().getHeight());
            for (int i = 0; i < num; ++i) {
                imageInfo = ImageInformation.create(format, file);
                imageInfo.setImageFileInformation(fileInfo);
                imageInfo.setImageFormat(format);
                imageInfo.setFileName(fileInfo.getFileName());
                imageInfo.setCreateTime(fileInfo.getCreateTime());
                imageInfo.setModifyTime(fileInfo.getModifyTime());
                imageInfo.setFileSize(fileInfo.getFileSize());
                imageInfo.setWidth(width);
                imageInfo.setHeight(height);
                imageInfo.setIsMultipleFrames(num > 1);
                imageInfo.setIndex(i);
                imageInfo.setImageType(BufferedImage.TYPE_INT_ARGB);
                ImageInformation.countMaxWidth(imageInfo);
                imagesInfo.add(imageInfo);
            }
            fileInfo.setImagesInformation(imagesInfo);
            if (!imagesInfo.isEmpty()) {
                fileInfo.setImageInformation(imagesInfo.get(0));
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

}
