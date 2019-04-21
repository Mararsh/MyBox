package mara.mybox.data;

import java.io.File;
import java.util.List;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-19
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageFileInformation extends FileInformation {

    private String imageFormat, filename;
    private ImageInformation imageInformation;
    private List<ImageInformation> imagesInformation;
    private int numberOfImages;

    public ImageFileInformation() {
    }

    public ImageFileInformation(File file) {
        super(file);
        filename = fileName.get();
        imageFormat = FileTools.getFileSuffix(filename).toLowerCase();
    }

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

    public String getFilename() {
        if (filename == null && file != null) {
            filename = file.getAbsolutePath();
        }
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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
