package mara.mybox.db.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FloatMatrixTools;
import mara.mybox.tools.FloatTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-11-6 20:28:03
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ConvolutionKernel {

    private String name, description, modifyTime, createTime;
    private int width, height, type, edge;
    private float[][] matrix;
    private boolean invert, gray;

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
        name = "";
        description = "";
        modifyTime = DateTools.datetimeToString(new Date());
        createTime = DateTools.datetimeToString(new Date());
        width = 0;
        height = 0;
        type = 0;
        edge = 0;
        invert = gray = false;
    }

    public static List<ConvolutionKernel> makeExample() {
        List<ConvolutionKernel> ExampleKernels = new ArrayList<>();
        ExampleKernels.add(makeAverageBlur(3));
        ExampleKernels.add(makeGaussBlur(3));
        ExampleKernels.add(makeGaussBlur(5));
        ExampleKernels.add(makeMotionBlur(1));
        ExampleKernels.add(makeMotionBlur(2));
        ExampleKernels.add(makeMotionBlur(3));
        ExampleKernels.add(MakeSharpenFourNeighborLaplace());
        ExampleKernels.add(MakeSharpenEightNeighborLaplace());
        ExampleKernels.add(makeEdgeDetectionFourNeighborLaplace());
        ExampleKernels.add(makeEdgeDetectionEightNeighborLaplace());
        ExampleKernels.add(makeEdgeDetectionFourNeighborLaplaceInvert());
        ExampleKernels.add(makeEdgeDetectionEightNeighborLaplaceInvert());
        ExampleKernels.add(makeUnsharpMasking5());
        ExampleKernels.add(makeEmbossTop3());
        ExampleKernels.add(makeEmbossBottom3());
        ExampleKernels.add(makeEmbossLeft3());
        ExampleKernels.add(makeEmbossRight3());
        ExampleKernels.add(makeEmbossLeftTop3());
        ExampleKernels.add(makeEmbossRightBottom3());
        ExampleKernels.add(makeEmbossLeftBottom3());
        ExampleKernels.add(makeEmbossRightTop3());
        return ExampleKernels;

    }

    public static ConvolutionKernel makeAverageBlur(int radius) {
        int size = 2 * radius + 1;
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(Languages.message("AverageBlur") + " " + size + "*" + size);
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
        kernel.setName(Languages.message("GaussianBlur") + " " + length + "*" + length);
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(length);
        kernel.setHeight(length);
        kernel.setType(Convolution_Type.BLUR);
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
        return FloatMatrixTools.array2Matrix(makeGaussArray(radius), radius * 2 + 1);
    }

    public static ConvolutionKernel makeMotionBlur(int radius) {
        int size = 2 * radius + 1;
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(Languages.message("MotionBlur") + " " + size + "*" + size);
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(size);
        kernel.setHeight(size);
        kernel.setType(Convolution_Type.BLUR);
        kernel.setDescription("");
        float v = 1.0f / size;
        float[][] k = new float[size][size];
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                if (i == j) {
                    k[i][j] = v;
                } else {
                    k[i][j] = 0;
                }
            }
        }
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeMotionAngleBlur(int radius, int angle) {
        ConvolutionKernel kernel = new ConvolutionKernel();
        int length = radius * 2 + 1;
        kernel.setName(Languages.message("MotionBlur") + " " + length + "*" + length);
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
//        MyBoxLog.debug(MatrixTools.print(FloatTools.toDouble(k), 0, 8));
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel MakeSharpenFourNeighborLaplace() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(Languages.message("Sharpen") + "   " + Languages.message("FourNeighborLaplace"));
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
        kernel.setName(Languages.message("Sharpen") + "   " + Languages.message("EightNeighborLaplace"));
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
        kernel.setName(Languages.message("EdgeDetection") + " " + Languages.message("FourNeighborLaplace"));
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
        kernel.setInvert(false);
        return kernel;
    }

    public static ConvolutionKernel makeEdgeDetectionFourNeighborLaplaceInvert() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(Languages.message("EdgeDetection") + " " + Languages.message("FourNeighborLaplaceInvert"));
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
        kernel.setInvert(true);
        return kernel;
    }

    // https://www.javaworld.com/article/2076764/java-se/image-processing-with-java-2d.html
    public static ConvolutionKernel makeEdgeDetectionEightNeighborLaplace() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(Languages.message("EdgeDetection") + " " + Languages.message("EightNeighborLaplace"));
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

    public static ConvolutionKernel makeEdgeDetectionEightNeighborLaplaceInvert() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(Languages.message("EdgeDetection") + " " + Languages.message("EightNeighborLaplaceInvert"));
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
        kernel.setInvert(true);
        return kernel;
    }

    public static ConvolutionKernel makeUnsharpMasking(int radius) {
        ConvolutionKernel kernel = new ConvolutionKernel();
        int length = radius * 2 + 1;
        kernel.setName(Languages.message("UnsharpMasking") + " " + length + "*" + length);
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
//        MyBoxLog.debug(MatrixTools.print(FloatTools.toDouble(k), 0, 8));
        kernel.setMatrix(k);
        return kernel;
    }

    public static ConvolutionKernel makeUnsharpMasking5() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(Languages.message("UnsharpMasking") + " 5*5");
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

    // https://en.wikipedia.org/wiki/Image_embossing
    public static ConvolutionKernel makeEmbossTop3() {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(Languages.message("Emboss") + " " + Languages.message("Top") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(true);
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
        kernel.setName(Languages.message("Emboss") + " " + Languages.message("Bottom") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(true);
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
        kernel.setName(Languages.message("Emboss") + " " + Languages.message("Left") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(true);
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
        kernel.setName(Languages.message("Emboss") + " " + Languages.message("Right") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(true);
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
        kernel.setName(Languages.message("Emboss") + " " + Languages.message("LeftTop") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(true);
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
        kernel.setName(Languages.message("Emboss") + " " + Languages.message("RightBottom") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(true);
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
        kernel.setName(Languages.message("Emboss") + " " + Languages.message("LeftBottom") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(true);
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
        kernel.setName(Languages.message("Emboss") + " " + Languages.message("RightTop") + " 3*3");
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(3);
        kernel.setHeight(3);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(true);
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
        if (direction == BufferedImageTools.Direction.Top) {
            if (size == 3) {
                m = FloatMatrixTools.array2Matrix(embossTopKernel, size);
            } else if (size == 5) {
                m = FloatMatrixTools.array2Matrix(embossTopKernel5, size);
            }
        } else if (direction == BufferedImageTools.Direction.Bottom) {
            if (size == 3) {
                m = FloatMatrixTools.array2Matrix(embossBottomKernel, size);
            } else if (size == 5) {
                m = FloatMatrixTools.array2Matrix(embossBottomKernel5, size);
            }
        } else if (direction == BufferedImageTools.Direction.Left) {
            if (size == 3) {
                m = FloatMatrixTools.array2Matrix(embossLeftKernel, size);
            } else if (size == 5) {
                m = FloatMatrixTools.array2Matrix(embossLeftKernel5, size);
            }
        } else if (direction == BufferedImageTools.Direction.Right) {
            if (size == 3) {
                m = FloatMatrixTools.array2Matrix(embossRightKernel, size);
            } else if (size == 5) {
                m = FloatMatrixTools.array2Matrix(embossRightKernel5, size);
            }
        } else if (direction == BufferedImageTools.Direction.LeftTop) {
            if (size == 3) {
                m = FloatMatrixTools.array2Matrix(embossLeftTopKernel, size);
            } else if (size == 5) {
                m = FloatMatrixTools.array2Matrix(embossLeftTopKernel5, size);
            }
        } else if (direction == BufferedImageTools.Direction.RightBottom) {
            if (size == 3) {
                m = FloatMatrixTools.array2Matrix(embossRightBottomKernel, size);
            } else if (size == 5) {
                m = FloatMatrixTools.array2Matrix(embossRightBottomKernel5, size);
            }
        } else if (direction == BufferedImageTools.Direction.LeftBottom) {
            if (size == 3) {
                m = FloatMatrixTools.array2Matrix(embossLeftBottomKernel, size);
            } else if (size == 5) {
                m = FloatMatrixTools.array2Matrix(embossLeftBottomKernel5, size);
            }
        } else if (direction == BufferedImageTools.Direction.RightTop) {
            if (size == 3) {
                m = FloatMatrixTools.array2Matrix(embossRightTopKernel, size);
            } else if (size == 5) {
                m = FloatMatrixTools.array2Matrix(embossRightTopKernel5, size);
            }
        }
        return m;
    }

    public static ConvolutionKernel makeEmbossKernel(int direction, int size, boolean gray) {
        ConvolutionKernel kernel = new ConvolutionKernel();
        kernel.setName(Languages.message("Emboss") + " "
                + Languages.message("Direction") + ":" + direction + " "
                + Languages.message("Size") + ":" + size);
        kernel.setCreateTime(DateTools.datetimeToString(new Date()));
        kernel.setModifyTime(DateTools.datetimeToString(new Date()));
        kernel.setWidth(size);
        kernel.setHeight(size);
        kernel.setType(Convolution_Type.EMBOSS);
        kernel.setGray(gray);
        kernel.setDescription("");

        float[][] k = makeEmbossMatrix(direction, size);
        kernel.setMatrix(k);
        return kernel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getEdge() {
        return edge;
    }

    public void setEdge(int edge) {
        this.edge = edge;
    }

    public float[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(float[][] matrix) {
        this.matrix = matrix;
    }

    public boolean isGray() {
        return gray;
    }

    public ConvolutionKernel setGray(boolean gray) {
        this.gray = gray;
        return this;
    }

    public boolean isInvert() {
        return invert;
    }

    public void setInvert(boolean invert) {
        this.invert = invert;
    }

}
