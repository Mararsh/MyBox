package mara.mybox.image;

import java.io.File;
import java.util.List;
import mara.mybox.data.FileInformation;
import mara.mybox.tools.FileTools;

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
        imageFormat = FileTools.getFileSuffix(fileName).toLowerCase();
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
