package mara.mybox.controller;

import com.recognition.software.jdeskew.ImageDeskew;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import mara.mybox.bufferedimage.ImageBinary;
import mara.mybox.bufferedimage.ImageContrast;
import mara.mybox.bufferedimage.ImageContrast.ContrastAlgorithm;
import mara.mybox.bufferedimage.ImageConvolution;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.bufferedimage.ScaleTools;
import mara.mybox.bufferedimage.TransformTools;
import mara.mybox.db.data.ConvolutionKernel;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.OCRTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import net.sourceforge.tess4j.util.ImageHelper;

/**
 * @Author Mara
 * @CreateDate 2019-9-17
 * @License Apache License Version 2.0
 */
public class ImageOCRProcessController extends BaseImageController {

    protected ImageOCRController OCRController;
    protected float scale;
    protected int threshold, rotate;

    @FXML
    protected ComboBox<String> rotateSelector, binarySelector, scaleSelector;
    @FXML
    protected Button demoButton;
    @FXML
    protected CheckBox loadCheck;

    public ImageOCRProcessController() {
        TipsLabelKey = "OCRPreprocessComment";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            scale = 1.0f;
            scaleSelector.getItems().addAll(Arrays.asList(
                    "1.0", "1.5", "2.0", "2.5", "3.0", "5.0", "10.0"
            ));
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (newV == null || newV.isEmpty()) {
                            return;
                        }
                        float f = Float.parseFloat(newV);
                        if (f > 0) {
                            scale = f;
                            scaleSelector.getEditor().setStyle(null);
                        } else {
                            scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });
            scaleSelector.getSelectionModel().select(0);

            threshold = 0;
            binarySelector.getItems().addAll(Arrays.asList(
                    "65", "50", "75", "45", "30", "80", "85", "15"
            ));
            binarySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (newV == null || newV.isEmpty()) {
                            return;
                        }
                        int i = Integer.parseInt(newV);
                        if (i > 0) {
                            threshold = i;
                            binarySelector.getEditor().setStyle(null);
                        } else {
                            binarySelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        binarySelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            rotate = 0;
            rotateSelector.getItems().addAll(Arrays.asList(
                    "0", "90", "45", "15", "30", "60", "75", "180", "105", "135", "120", "150", "165", "270", "300", "315"
            ));
            rotateSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String oldV, String newV) {
                    try {
                        if (newV == null || newV.isEmpty()) {
                            return;
                        }
                        rotate = Integer.parseInt(newV);
                    } catch (Exception e) {

                    }
                }
            });

            loadCheck.setSelected(UserConfig.getBoolean(baseName + "Load", false));
            loadCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean oldV, Boolean newV) {
                    UserConfig.setBoolean(baseName + "Load", loadCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void loadImage(Image image) {
        OCRController.textArea.clear();
        OCRController.regionsTableController.clear();
        OCRController.wordsTableController.clear();
        OCRController.htmlController.clear();
        OCRController.resultLabel.setText("");
        super.loadImage(image);
    }

    @FXML
    @Override
    public void recoverAction() {
        loadImage(OCRController.sourceFile, OCRController.imageInformation, OCRController.imageView.getImage(), false);
    }

    @FXML
    @Override
    public void previousAction() {
        OCRController.previousAction();
    }

    @FXML
    @Override
    public void nextAction() {
        OCRController.nextAction();
    }

    @FXML
    protected void scale() {
        if (isSettingValues || imageView.getImage() == null || scale <= 0) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image changedImage;

            @Override
            protected boolean handle() {
                try {
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                    bufferedImage = ScaleTools.scaleImageByScale(bufferedImage, scale);
                    changedImage = SwingFXUtils.toFXImage(bufferedImage, null);
                    return changedImage != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadImage(changedImage);
            }

        };
        start(task);
    }

    @FXML
    protected void binary() {
        if (isSettingValues || imageView.getImage() == null || threshold <= 0) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image ocrImage;

            @Override
            protected boolean handle() {
                try {
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                    ImageBinary imageBinary = new ImageBinary();
                    imageBinary.setImage(bufferedImage)
                            .setIntPara1(threshold);
                    bufferedImage = imageBinary.operateImage();
                    ocrImage = SwingFXUtils.toFXImage(bufferedImage, null);
                    return ocrImage != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadImage(ocrImage);

            }

        };
        start(task);
    }

    @FXML
    protected void popAlgorithmsMenu(MouseEvent mouseEvent) {
        try {
            List<String> algorithms = new ArrayList<>();
            algorithms.addAll(Arrays.asList(message("Deskew"), message("Invert"),
                    message("EdgeDetection") + "-" + message("EightNeighborLaplaceInvert"),
                    message("EdgeDetection") + "-" + message("EightNeighborLaplace"),
                    message("HSBHistogramEqualization"), message("GrayHistogramEqualization"),
                    message("GrayHistogramStretching"), message("GrayHistogramShifting"),
                    message("UnsharpMasking"),
                    message("Enhancement") + "-" + message("EightNeighborLaplace"),
                    message("Enhancement") + "-" + message("FourNeighborLaplace"),
                    message("GaussianBlur"), message("AverageBlur")
            ));

            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;
            for (String algorithm : algorithms) {
                menu = new MenuItem(algorithm);
                menu.setOnAction((ActionEvent event) -> {
                    algorithm(algorithm);
                });
                items.add(menu);
            }

            items.add(new SeparatorMenuItem());

            popEventMenu(mouseEvent, items);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void algorithm(String algorithm) {
        if (algorithm == null || isSettingValues || imageView.getImage() == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image ocrImage;

            @Override
            protected boolean handle() {
                try {
                    if (message("Deskew").equals(algorithm)) {
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                        ImageDeskew id = new ImageDeskew(bufferedImage);
                        double imageSkewAngle = id.getSkewAngle();
                        if ((imageSkewAngle > OCRTools.MINIMUM_DESKEW_THRESHOLD
                                || imageSkewAngle < -(OCRTools.MINIMUM_DESKEW_THRESHOLD))) {
                            bufferedImage = ImageHelper.rotateImage(bufferedImage, -imageSkewAngle);
                        }
                        ocrImage = SwingFXUtils.toFXImage(bufferedImage, null);

                    } else if (message("Invert").equals(algorithm)) {
                        PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(imageView.getImage(),
                                null, PixelsOperation.OperationType.RGB, PixelsOperation.ColorActionType.Invert);
                        ocrImage = pixelsOperation.operateFxImage();

                    } else if (message("GrayHistogramEqualization").equals(algorithm)) {
                        ImageContrast imageContrast = new ImageContrast()
                                .setAlgorithm(ContrastAlgorithm.Gray_Histogram_Equalization);
                        ocrImage = imageContrast.setImage(imageView.getImage())
                                .setTask(this).operateFxImage();

                    } else if (message("GrayHistogramStretching").equals(algorithm)) {
                        ImageContrast imageContrast = new ImageContrast()
                                .setAlgorithm(ContrastAlgorithm.Gray_Histogram_Stretching);
                        ocrImage = imageContrast.setImage(imageView.getImage())
                                .setIntPara1(50).setIntPara2(50)
                                .setTask(this).operateFxImage();

                    } else if (message("GrayHistogramShifting").equals(algorithm)) {
                        ImageContrast imageContrast = new ImageContrast()
                                .setAlgorithm(ContrastAlgorithm.Gray_Histogram_Shifting);
                        ocrImage = imageContrast.setImage(imageView.getImage())
                                .setIntPara1(10)
                                .setTask(this).operateFxImage();

                    } else if (message("HSBHistogramEqualization").equals(algorithm)) {
                        ImageContrast imageContrast = new ImageContrast()
                                .setAlgorithm(ContrastAlgorithm.HSB_Histogram_Equalization);
                        ocrImage = imageContrast.setImage(imageView.getImage())
                                .setTask(this).operateFxImage();

                    } else if (message("UnsharpMasking").equals(algorithm)) {
                        ConvolutionKernel kernel = ConvolutionKernel.makeUnsharpMasking(3);
                        ImageConvolution imageConvolution = ImageConvolution.create().
                                setImage(imageView.getImage()).setKernel(kernel);
                        ocrImage = imageConvolution.operateFxImage();

                    } else if ((message("Enhancement") + "-" + message("EightNeighborLaplace")).equals(algorithm)) {
                        ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                        ImageConvolution imageConvolution = ImageConvolution.create().
                                setImage(imageView.getImage()).setKernel(kernel);
                        ocrImage = imageConvolution.operateFxImage();

                    } else if ((message("Enhancement") + "-" + message("FourNeighborLaplace")).equals(algorithm)) {
                        ConvolutionKernel kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                        ImageConvolution imageConvolution = ImageConvolution.create().
                                setImage(imageView.getImage()).setKernel(kernel);
                        ocrImage = imageConvolution.operateFxImage();

                    } else if (message("GaussianBlur").equals(algorithm)) {
                        ConvolutionKernel kernel = ConvolutionKernel.makeGaussBlur(2);
                        ImageConvolution imageConvolution = ImageConvolution.create().
                                setImage(imageView.getImage()).setKernel(kernel);
                        ocrImage = imageConvolution.operateFxImage();

                    } else if (message("AverageBlur").equals(algorithm)) {
                        ConvolutionKernel kernel = ConvolutionKernel.makeAverageBlur(1);
                        ImageConvolution imageConvolution = ImageConvolution.create().
                                setImage(imageView.getImage()).setKernel(kernel);
                        ocrImage = imageConvolution.operateFxImage();

                    } else if ((message("EdgeDetection") + "-" + message("EightNeighborLaplaceInvert")).equals(algorithm)) {
                        ConvolutionKernel kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplaceInvert().setGray(true);
                        ImageConvolution imageConvolution = ImageConvolution.create().
                                setImage(imageView.getImage()).setKernel(kernel);
                        ocrImage = imageConvolution.operateFxImage();

                    } else if ((message("EdgeDetection") + "-" + message("EightNeighborLaplace")).equals(algorithm)) {
                        ConvolutionKernel kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace().setGray(true);
                        ImageConvolution imageConvolution = ImageConvolution.create().
                                setImage(imageView.getImage()).setKernel(kernel);
                        ocrImage = imageConvolution.operateFxImage();

                    }

                    return ocrImage != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadImage(ocrImage);

            }

        };
        start(task);
    }

    @FXML
    protected void demo() {
        if (imageView.getImage() == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private List<String> files;

            @Override
            protected boolean handle() {

                try {
                    files = new ArrayList<>();
                    BufferedImage image = SwingFXUtils.fromFXImage(OCRController.imageView.getImage(), null);
                    image = ScaleTools.demoImage(image);

                    ConvolutionKernel kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplaceInvert().setGray(true);
                    ImageConvolution imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
                    BufferedImage bufferedImage = imageConvolution.operateImage();
                    String tmpFile = FileTmpTools.generateFile(message("EdgeDetection")
                            + "-" + message("EightNeighborLaplaceInvert"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(this, bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeEdgeDetectionEightNeighborLaplace().setGray(true);
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
                    bufferedImage = imageConvolution.operateImage();
                    tmpFile = FileTmpTools.generateFile(message("EdgeDetection")
                            + "-" + message("EightNeighborLaplace"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(this, bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    ImageContrast imageContrast = new ImageContrast()
                            .setAlgorithm(ContrastAlgorithm.HSB_Histogram_Equalization);
                    bufferedImage = imageContrast.setImage(image).setTask(this).operateImage();
                    tmpFile = FileTmpTools.generateFile(message("HSBHistogramEqualization"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(this, bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    imageContrast = new ImageContrast()
                            .setAlgorithm(ContrastAlgorithm.Gray_Histogram_Equalization);
                    bufferedImage = imageContrast.setImage(image).setTask(this).operateImage();
                    tmpFile = FileTmpTools.generateFile(message("GrayHistogramEqualization"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(this, bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    imageContrast = new ImageContrast()
                            .setAlgorithm(ContrastAlgorithm.Gray_Histogram_Stretching);
                    bufferedImage = imageContrast.setImage(image).setTask(this)
                            .setIntPara1(100).setIntPara2(100).operateImage();
                    tmpFile = FileTmpTools.generateFile(message("GrayHistogramStretching"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(this, bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    imageContrast = new ImageContrast()
                            .setAlgorithm(ContrastAlgorithm.Gray_Histogram_Shifting);
                    bufferedImage = imageContrast.setImage(image).setTask(this)
                            .setIntPara1(40).operateImage();
                    tmpFile = FileTmpTools.generateFile(message("GrayHistogramShifting"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(this, bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeUnsharpMasking(3);
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
                    bufferedImage = imageConvolution.setTask(this).operateImage();
                    tmpFile = FileTmpTools.generateFile(message("UnsharpMasking"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(this, bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    kernel = ConvolutionKernel.MakeSharpenFourNeighborLaplace();
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
                    bufferedImage = imageConvolution.setTask(this).operateImage();
                    tmpFile = FileTmpTools.generateFile(message("Enhancement")
                            + "-" + message("FourNeighborLaplace"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(this, bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    kernel = ConvolutionKernel.MakeSharpenEightNeighborLaplace();
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
                    bufferedImage = imageConvolution.setTask(this).operateImage();
                    tmpFile = FileTmpTools.generateFile(message("Enhancement")
                            + "-" + message("EightNeighborLaplace"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(this, bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeGaussBlur(3);
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
                    bufferedImage = imageConvolution.setTask(this).operateImage();
                    tmpFile = FileTmpTools.generateFile(message("GaussianBlur"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(this, bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    kernel = ConvolutionKernel.makeAverageBlur(2);
                    imageConvolution = ImageConvolution.create().
                            setImage(image).setKernel(kernel);
                    bufferedImage = imageConvolution.setTask(this).operateImage();
                    tmpFile = FileTmpTools.generateFile(message("AverageBlur"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(this, bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(imageView.getImage(),
                            null, PixelsOperation.OperationType.RGB, PixelsOperation.ColorActionType.Invert);
                    bufferedImage = pixelsOperation.setTask(this).operateImage();
                    tmpFile = FileTmpTools.generateFile(message("Invert"), "png").getAbsolutePath();
                    if (ImageFileWriters.writeImageFile(this, bufferedImage, tmpFile)) {
                        files.add(tmpFile);
                        task.setInfo(tmpFile);
                    }

                    return !files.isEmpty();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (files != null && !files.isEmpty()) {
                    ImagesBrowserController b
                            = (ImagesBrowserController) WindowTools.openStage(Fxmls.ImagesBrowserFxml);
                    b.loadFiles(files);
                }
            }

        };
        start(task);
    }

    @FXML
    protected void rotate() {
        if (isSettingValues || imageView.getImage() == null || rotate == 0) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Image ocrImage;

            @Override
            protected boolean handle() {
                try {
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
                    bufferedImage = TransformTools.rotateImage(this, bufferedImage, rotate);
                    ocrImage = SwingFXUtils.toFXImage(bufferedImage, null);
                    return ocrImage != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadImage(ocrImage);

            }

        };
        start(task);
    }

}
