package mara.mybox.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import mara.mybox.image.ImageManufacture;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.tools.MatrixTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-11-6 20:28:03
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ConvolutionKernel {

    private SimpleStringProperty name, description, modifyTime, createTime;
    private SimpleIntegerProperty width, height, type, gray, edge;
    private float[][] matrix;

    public static List<ConvolutionKernel> ExampleKernels;

    public static class Convolution_Type {

        public static int NONE = 0;
        public static int BLUR = 1;
        public static int SHARPNEN = 2;
        public static int EMBOSS = 3;
        public static int EDGE_DETECTION = 4;

    }

    public static class Edge_Op {

        public static int COPY = 0;
        public static int FILL_ZERO = 1;
        public static int MOD = 2;

    }

    public ConvolutionKernel() {
        name = new SimpleStringProperty("");
        description = new SimpleStringProperty("");
        modifyTime = new SimpleStringProperty(DateTools.datetimeToString(new Date()));
        createTime = new SimpleStringProperty(DateTools.datetimeToString(new Date()));
        width = new SimpleIntegerProperty(0);
        height = new SimpleIntegerProperty(0);
        type = new SimpleIntegerProperty(0);
        gray = new SimpleIntegerProperty(0);
        edge = new SimpleIntegerProperty(0);
    }

    public static void makeExample() {
//        if (ExampleKernels != null && !ExampleKernels.isEmpty()) {
//            return;
//        }
        ExampleKernels = new ArrayList<>();
        ExampleKernels.add(makeAverageBlur(3));
        ExampleKernels.add(makeGaussBlur(3));
        ExampleKernels.add(makeGaussBlur(5));
        ExampleKernels.add(MakeSharpenFourNeighborLaplace());
        ExampleKernels.add(MakeSharpenEightNeighborLaplace());
        ExampleKernels.add(makeEdgeDetectionFourNeighborLaplace());
        ExampleKernels.add(makeEdgeDetectionEightNeighborLaplace());
        ExampleKernels.add(makeEdgeDetection3c());
        ExampleKernels.add(makeUnsharpMasking5());
        ExampleKernels.add(makeMotionBlur3());
        ExampleKernels.add(makeMotionBlur5());
        ExampleKernels.add(makeMotionBlur9());
        ExampleKernels.add(makeEmbossTop3());
        ExampleKernels.add(makeEmbossBottom3());
        ExampleKernels.add(makeEmbossLeft3());
        ExampleKernels.add(makeEmbossRight3());
        ExampleKernels.add(makeEmbossLeftTop3());
        ExampleKernels.add(makeEmbossRightBottom3());
        ExampleKernels.add(makeEmbossLeftBottom3());
        ExampleKernels.add(makeEmbossRightTop3());

    }

    public static ConvolutionKernel makeAverageBlur(int radius) {
        int size = 2 * radius + 1;
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("AverageBlur") + " " + size + "*" + size);
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(size);
        kernel.setHeight(size);
        kernel.setType(Convolution_Type.BLUR);
        kernel.setDescription("");
        float v = 1.0f / (size * size * 1.0f);
        float[][] k = new float[size][size];
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                k[i][j] = v;
            }
        }
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeGaussBlur(int radius) {
        ConvolutionKernel kernel = new ConvolutionKernel();
        int length = radius * 2 + 1;
        kernel.setName(AppVariables.message("GaussianBlur") + " " + length + "*" + length);
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(length);
        kernel.setHeight(length);
        kernel.setType(Convolution_Type.BLUR);
        kernel.setGray(1);
        kernel.setDescription("");
        float[][] k = makeGaussMatrix(radius);
        kernel.setMatrix(k);
        return kernel;
    }

    // https://en.wikipedia.org/wiki/Kernel_(image_processing)
    // https://lodev.org/cgtutor/filtering.html
    public static float[] makeGaussArray(int radius) {
        if (radius < 1) {
            return null;
        }
        float sum = 0.0f;
        int width = radius * 2 + 1;
        int size = width * width;
        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.PI * twoSigmaSquare;
        float[] data = new float[size];
        int index = 0;
        float x, y;
        for (int i = -radius; i <= radius; ++i) {
            for (int j = -radius; j <= radius; ++j) {
                x = i * i;
                y = j * j;
                data[index] = (float) Math.exp(-(x + y) / twoSigmaSquare) / sigmaRoot;
                sum += data[index];
                index++;
            }
        }
        for (int k = 0; k < size; k++) {
            data[k] = FloatTools.roundFloat5(data[k] / sum);
        }
        return data;
    }

    public static float[][] makeGaussMatrix(int radius) {
        return MatrixTools.array2Matrix(makeGaussArray(radius), radius * 2 + 1);

    }

    public static ConvolutionKernel MakeSharpenFourNeighborLaplace() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(message("Sharpen") + "   " + message("FourNeighborLaplace"));
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.SHARPNEN);
        kernel.setDescription("");
        float[][] k = {
            {0.0f, -1.0f, 0.0f},
            {-1.0f, 5.0f, -1.0f},
            {0.0f, -1.0f, 0.0f}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    // https://www.javaworld.com/article/2076764/java-se/image-processing-with-java-2d.html
    // https://en.wikipedia.org/wiki/Kernel_(image_processing)
    public static ConvolutionKernel MakeSharpenEightNeighborLaplace() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("Sharpen") + "   " + message("EightNeighborLaplace"));
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.SHARPNEN);
        kernel.setDescription("");
        float[][] k = {
            {-1.0f, -1.0f, -1.0f},
            {-1.0f, 9.0f, -1.0f},
            {-1.0f, -1.0f, -1.0f}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeEdgeDetectionFourNeighborLaplace() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("EdgeDetection") + " 3*3 a");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EDGE_DETECTION);
        kernel.setDescription("");
        float[][] k = {
            {0.0f, -1.0f, 0.0f},
            {-1.0f, 4.0f, -1.0f},
            {0.0f, -1.0f, 0.0f}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    // https://www.javaworld.com/article/2076764/java-se/image-processing-with-java-2d.html
    public static ConvolutionKernel makeEdgeDetectionEightNeighborLaplace() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("EdgeDetection") + " 3*3 b");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EDGE_DETECTION);
        kernel.setDescription("");
        float[][] k = {
            {-1.0f, -1.0f, -1.0f},
            {-1.0f, 8.0f, -1.0f},
            {-1.0f, -1.0f, -1.0f}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeEdgeDetection3c() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("EdgeDetection") + " 3*3 c");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EDGE_DETECTION);
        kernel.setDescription("");
        float[][] k = {
            {0.0f, 1.0f, 0.0f},
            {1.0f, -4.0f, 1.0f},
            {0.0f, 1.0f, 0.0f}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeUnsharpMasking(int radius) {
        ConvolutionKernel kernel = new ConvolutionKernel();
        int length = radius * 2 + 1;
        kernel.setName(AppVariables.message("UnsharpMasking") + " " + length + "*" + length);
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(length);
        kernel.setHeight(length);
        kernel.setType(Convolution_Type.SHARPNEN);
        kernel.setDescription("");
        float[][] k = makeGaussMatrix(radius);
        for (int i = 0; i < k.length; ++i) {
            for (int j = 0; j < k[i].length; ++j) {
                k[i][j] = 0 - k[i][j];
            }
        }
        k[radius][radius] = 2 + k[radius][radius];
//        logger.debug(MatrixTools.print(FloatTools.toDouble(k), 0, 8));
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeUnsharpMasking5() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("UnsharpMasking") + " 5*5");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(5);
        kernel.setHeight(5);
        kernel.setType(Convolution_Type.SHARPNEN);
        kernel.setDescription("");
        float[][] k = {
            {-1 / 256.0f, -4 / 256.0f, -6 / 256.0f, -4 / 256.0f, -1 / 256.0f},
            {-4 / 256.0f, -16 / 256.0f, -24 / 256.0f, -16 / 256.0f, -4 / 256.0f},
            {-6 / 256.0f, -24 / 256.0f, 476 / 256.0f, -24 / 256.0f, -6 / 256.0f},
            {-4 / 256.0f, -16 / 256.0f, -24 / 256.0f, -16 / 256.0f, -4 / 256.0f},
            {-1 / 256.0f, -4 / 256.0f, -6 / 256.0f, -4 / 256.0f, -1 / 256.0f}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeMotionBlur(int radius, int angle) {
        ConvolutionKernel kernel = new ConvolutionKernel();
        int length = radius * 2 + 1;
        kernel.setName(AppVariables.message("MotionBlur") + " " + length + "*" + length);
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(length);
        kernel.setHeight(length);
        kernel.setType(Convolution_Type.BLUR);
        kernel.setDescription("");

        float[][] k = new float[length][length];
        for (int i = 0; i < length; ++i) {
            for (int j = 0; j < length; ++j) {
                k[i][j] = (float) Math.sin(angle);
            }
        }

        k[radius][radius] = 2 + k[radius][radius];
//        logger.debug(MatrixTools.print(FloatTools.toDouble(k), 0, 8));
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeMotionBlur3() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("MotionBlur") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(5);
        kernel.setHeight(5);
        kernel.setType(Convolution_Type.BLUR);
        kernel.setDescription("");
        float v = 1 / 3.0f;
        float[][] k = {
            {v, 0, 0},
            {0, v, 0},
            {0, 0, v}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeMotionBlur5() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("MotionBlur") + " 5*5");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(5);
        kernel.setHeight(5);
        kernel.setType(Convolution_Type.BLUR);
        kernel.setDescription("");
        float v = 1 / 5.0f;
        float[][] k = {
            {v, 0, 0, 0, 0},
            {0, v, 0, 0, 0},
            {0, 0, v, 0, 0},
            {0, 0, 0, v, 0},
            {0, 0, 0, 0, v}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeMotionBlur9() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("MotionBlur") + " 9*9");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(9);
        kernel.setHeight(9);
        kernel.setType(Convolution_Type.BLUR);
        kernel.setDescription("");
        float v = 1 / 9.0f;
        float[][] k = {
            {v, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, v, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, v, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, v, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, v, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, v, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, v, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, v, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, v}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    // https://en.wikipedia.org/wiki/Image_embossing
    public static ConvolutionKernel makeEmbossTop3() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("Emboss") + " " + AppVariables.message("Top") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(1);
        kernel.setDescription("");
        float[][] k = {
            {0, 1, 0},
            {0, 0, 0},
            {0, -1, 0}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeEmbossBottom3() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("Emboss") + " " + AppVariables.message("Bottom") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(1);
        kernel.setDescription("");
        float[][] k = {
            {0, -1, 0},
            {0, 0, 0},
            {0, 1, 0}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeEmbossLeft3() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("Emboss") + " " + AppVariables.message("Left") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(1);
        kernel.setDescription("");
        float[][] k = {
            {0, 0, 0},
            {1, 0, -1},
            {0, 0, 0}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeEmbossRight3() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("Emboss") + " " + AppVariables.message("Right") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(1);
        kernel.setDescription("");
        float[][] k = {
            {0, 0, 0},
            {-1, 0, 1},
            {0, 0, 0}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeEmbossLeftTop3() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("Emboss") + " " + AppVariables.message("LeftTop") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(1);
        kernel.setDescription("");
        float[][] k = {
            {1, 0, 0},
            {0, 0, 0},
            {0, 0, -1}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeEmbossRightBottom3() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("Emboss") + " " + AppVariables.message("RightBottom") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(1);
        kernel.setDescription("");
        float[][] k = {
            {-1, 0, 0},
            {0, 0, 0},
            {0, 0, 1}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeEmbossLeftBottom3() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("Emboss") + " " + AppVariables.message("LeftBottom") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(1);
        kernel.setDescription("");
        float[][] k = {
            {0, 0, -1},
            {0, 0, 0},
            {1, 0, 0}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeEmbossRightTop3() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("Emboss") + " " + AppVariables.message("RightTop") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(1);
        kernel.setDescription("");
        float[][] k = {
            {0, 0, 1},
            {0, 0, 0},
            {-1, 0, 0}
        };
        kernel.setMatrix(k);
        return kernel;
    }

    public static float[] embossTopKernel = {
        0, 1, 0,
        0, 0, 0,
        0, -1, 0
    };
    public static float[] embossBottomKernel = {
        0, -1, 0,
        0, 0, 0,
        0, 1, 0
    };
    public static float[] embossLeftKernel = {
        0, 0, 0,
        1, 0, -1,
        0, 0, 0
    };
    public static float[] embossRightKernel = {
        0, 0, 0,
        -1, 0, 1,
        0, 0, 0
    };
    public static float[] embossLeftTopKernel = {
        1, 0, 0,
        0, 0, 0,
        0, 0, -1
    };
    public static float[] embossRightBottomKernel = {
        -1, 0, 0,
        0, 0, 0,
        0, 0, 1
    };
    public static float[] embossLeftBottomKernel = {
        0, 0, -1,
        0, 0, 0,
        1, 0, 0
    };
    public static float[] embossRightTopKernel = {
        0, 0, 1,
        0, 0, 0,
        -1, 0, 0
    };

    public static float[] embossTopKernel5 = {
        0, 0, -1, 0, 0,
        0, 0, -1, 0, 0,
        0, 0, 0, 0, 0,
        0, 0, 1, 0, 0,
        0, 0, 1, 0, 0
    };
    public static float[] embossBottomKernel5 = {
        0, 0, 1, 0, 0,
        0, 0, 1, 0, 0,
        0, 0, 0, 0, 0,
        0, 0, -1, 0, 0,
        0, 0, -1, 0, 0
    };
    public static float[] embossLeftKernel5 = {
        0, 0, 0, 0, 0,
        0, 0, 0, 0, 0,
        1, 1, 0, -1, -1,
        0, 0, 0, 0, 0,
        0, 0, 0, 0, 0
    };
    public static float[] embossRightKernel5 = {
        0, 0, 0, 0, 0,
        0, 0, 0, 0, 0,
        -1, -1, 0, 1, 1,
        0, 0, 0, 0, 0,
        0, 0, 0, 0, 0
    };
    public static float[] embossLeftTopKernel5 = {
        1, 0, 0, 0, 0,
        0, 1, 0, 0, 0,
        0, 0, 0, 0, 0,
        0, 0, 0, -1, 0,
        0, 0, 0, 0, -1
    };
    public static float[] embossRightBottomKernel5 = {
        -1, 0, 0, 0, 0,
        0, -1, 0, 0, 0,
        0, 0, 0, 0, 0,
        0, 0, 0, 1, 0,
        0, 0, 0, 0, 1
    };
    public static float[] embossLeftBottomKernel5 = {
        0, 0, 0, 0, -1,
        0, 0, 0, -1, 0,
        0, 0, 0, 0, 0,
        0, 1, 0, 0, 0,
        1, 0, 0, 0, 0
    };
    public static float[] embossRightTopKernel5 = {
        0, 0, 0, 0, 1,
        0, 0, 0, 1, 0,
        0, 0, 0, 0, 0,
        0, -1, 0, 0, 0,
        -1, 0, 0, 0, 0
    };

    public static float[][] makeEmbossMatrix(int direction, int size) {
        float[][] m = null;
        if (direction == ImageManufacture.Direction.Top) {
            if (size == 3) {
                m = MatrixTools.array2Matrix(embossTopKernel, size);
            } else if (size == 5) {
                m = MatrixTools.array2Matrix(embossTopKernel5, size);
            }
        } else if (direction == ImageManufacture.Direction.Bottom) {
            if (size == 3) {
                m = MatrixTools.array2Matrix(embossBottomKernel, size);
            } else if (size == 5) {
                m = MatrixTools.array2Matrix(embossBottomKernel5, size);
            }
        } else if (direction == ImageManufacture.Direction.Left) {
            if (size == 3) {
                m = MatrixTools.array2Matrix(embossLeftKernel, size);
            } else if (size == 5) {
                m = MatrixTools.array2Matrix(embossLeftKernel5, size);
            }
        } else if (direction == ImageManufacture.Direction.Right) {
            if (size == 3) {
                m = MatrixTools.array2Matrix(embossRightKernel, size);
            } else if (size == 5) {
                m = MatrixTools.array2Matrix(embossRightKernel5, size);
            }
        } else if (direction == ImageManufacture.Direction.LeftTop) {
            if (size == 3) {
                m = MatrixTools.array2Matrix(embossLeftTopKernel, size);
            } else if (size == 5) {
                m = MatrixTools.array2Matrix(embossLeftTopKernel5, size);
            }
        } else if (direction == ImageManufacture.Direction.RightBottom) {
            if (size == 3) {
                m = MatrixTools.array2Matrix(embossRightBottomKernel, size);
            } else if (size == 5) {
                m = MatrixTools.array2Matrix(embossRightBottomKernel5, size);
            }
        } else if (direction == ImageManufacture.Direction.LeftBottom) {
            if (size == 3) {
                m = MatrixTools.array2Matrix(embossLeftBottomKernel, size);
            } else if (size == 5) {
                m = MatrixTools.array2Matrix(embossLeftBottomKernel5, size);
            }
        } else if (direction == ImageManufacture.Direction.RightTop) {
            if (size == 3) {
                m = MatrixTools.array2Matrix(embossRightTopKernel, size);
            } else if (size == 5) {
                m = MatrixTools.array2Matrix(embossRightTopKernel5, size);
            }
        }
        return m;
    }

    public static ConvolutionKernel makeEmbossKernel(int direction, int size, boolean gray) {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(AppVariables.message("Emboss") + " "
                + AppVariables.message("Direction") + ":" + direction + " "
                + AppVariables.message("Size") + ":" + size);
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(size);
        kernel.setHeight(size);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(gray ? 1 : 0);
        kernel.setDescription("");

        float[][] k = makeEmbossMatrix(direction, size);
        kernel.setMatrix(k);
        return kernel;
    }

    public String getName() {
        if (name == null) {
            return null;
        } else {
            return name.get();
        }
    }

    public void setName(String name) {
        if (name == null) {
            this.name = null;
        } else {
            this.name = new SimpleStringProperty(name);
        }
    }

    public String getDescription() {
        if (description == null) {
            return "";
        } else {
            return description.get();
        }
    }

    public void setDescription(String description) {
        if (description == null) {
            this.description = new SimpleStringProperty("");
        } else {
            this.description = new SimpleStringProperty(description);
        }
    }

    public int getWidth() {
        if (width == null) {
            return 0;
        } else {
            return width.get();
        }
    }

    public void setWidth(int width) {
        this.width = new SimpleIntegerProperty(width);
    }

    public int getHeight() {
        if (height == null) {
            return 0;
        } else {
            return height.get();
        }
    }

    public void setHeight(int height) {
        this.height = new SimpleIntegerProperty(height);
    }

    public String getModifyTime() {
        if (modifyTime == null) {
            return "";
        } else {
            return modifyTime.get();
        }
    }

    public void setModifyTime(String modifyTime) {
        if (modifyTime == null) {
            this.modifyTime = null;
        } else {
            this.modifyTime = new SimpleStringProperty(modifyTime);
        }
    }

    public String getCreateTime() {
        if (createTime == null) {
            return "";
        } else {
            return createTime.get();
        }
    }

    public void setCreateTime(String createTime) {
        if (createTime == null) {
            this.createTime = null;
        } else {
            this.createTime = new SimpleStringProperty(createTime);
        }
    }

    public float[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(float[][] matrix) {
        this.matrix = matrix;
    }

    public int getType() {
        if (type == null) {
            type = new SimpleIntegerProperty(Convolution_Type.NONE);
        }
        return type.get();
    }

    public void setType(int type) {
        this.type = new SimpleIntegerProperty(type);
    }

    public int getGray() {
        if (gray == null) {
            gray = new SimpleIntegerProperty(0);
        }
        return gray.get();
    }

    public void setGray(int gray) {
        this.gray = new SimpleIntegerProperty(gray);
    }

    public int getEdge() {
        if (edge == null) {
            edge = new SimpleIntegerProperty(0);
        }
        return edge.get();
    }

    public void setEdge(int edge) {
        this.edge = new SimpleIntegerProperty(edge);
    }

}
