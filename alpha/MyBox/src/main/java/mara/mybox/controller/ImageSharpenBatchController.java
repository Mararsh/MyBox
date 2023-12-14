package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ImageDemoTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @License Apache License Version 2.0
 */
public class ImageSharpenBatchController extends BaseImageEditBatchController {
    
    protected ConvolutionKernel kernel;
    
    @FXML
    protected ControlImageSharpen sharpenController;
    
    public ImageSharpenBatchController() {
        baseTitle = message("ImageManufactureBatchSharpen");
        
    }
    
    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }
        kernel = sharpenController.kernel();
        return kernel != null;
    }
    
    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            return ImageConvolution.create()
                    .setImage(source).setKernel(kernel)
                    .setTask(task)
                    .operate();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }
    
    @Override
    public void makeDemoFiles(List<String> files, BufferedImage demoImage) {
        try {
            ImageConvolution convolution = ImageConvolution.create()
                    .setImage(demoImage);
            ImageDemoTools.sharpen(demoTask, files, convolution);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
}
