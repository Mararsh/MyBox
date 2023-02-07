package mara.mybox.bufferedimage;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.PixelsOperation.ColorActionType;
import mara.mybox.bufferedimage.PixelsOperation.OperationType;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-2-13 14:44:03
 * @License Apache License Version 2.0
 */
public class PixelsOperationFactory {

    public static PixelsOperation create(Image image, ImageScope scope, OperationType operationType) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        return create(bufferedImage, scope, operationType);
    }

    public static PixelsOperation create(BufferedImage image, ImageScope scope, OperationType operationType) {
        switch (operationType) {
            case ShowScope:
                return new ShowScope(image, scope);
            case Sepia:
                return new Sepia(image, scope);
            case Thresholding:
                return new Thresholding(image, scope);
            default:
                return null;
        }
    }

    public static PixelsOperation create(BufferedImage image, ImageScope scope,
            OperationType operationType, ColorActionType colorActionType) {
        switch (operationType) {
            case ReplaceColor:
                switch (colorActionType) {
                    case Filter:
                        return new ReplaceFilter(image, scope);
                    default:
                        return new ReplaceColor(image, scope);
                }
            case Color:
                switch (colorActionType) {
                    case Filter:
                        return new FilterColor(image, scope);
                    default:
                        return new SetColor(image, scope);
                }
            case Blend:
                return new BlendColor(image, scope);
            case Opacity:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseOpacity(image, scope);
                    case Decrease:
                        return new DecreaseOpacity(image, scope);
                    case Set:
                    default:
                        return new SetOpacity(image, scope);
                }
            case PreOpacity:
                switch (colorActionType) {
                    case Increase:
                        return new IncreasePreOpacity(image, scope);
                    case Decrease:
                        return new DecreasePreOpacity(image, scope);
                    case Set:
                    default:
                        return new SetPreOpacity(image, scope);
                }
            case Brightness:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseBrightness(image, scope);
                    case Decrease:
                        return new DecreaseBrightness(image, scope);
                    case Set:
                    default:
                        return new SetBrightness(image, scope);
                }
            case Saturation:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseSaturation(image, scope);
                    case Decrease:
                        return new DecreaseSaturation(image, scope);
                    case Set:
                    default:
                        return new SetSaturation(image, scope);
                }
            case Hue:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseHue(image, scope);
                    case Decrease:
                        return new DecreaseHue(image, scope);
                    case Set:
                    default:
                        return new SetHue(image, scope);
                }
            case Red:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseRed(image, scope);
                    case Decrease:
                        return new DecreaseRed(image, scope);
                    case Filter:
                        return new FilterRed(image, scope);
                    case Invert:
                        return new InvertRed(image, scope);
                    case Set:
                    default:
                        return new SetRed(image, scope);
                }
            case Green:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseGreen(image, scope);
                    case Decrease:
                        return new DecreaseGreen(image, scope);
                    case Filter:
                        return new FilterGreen(image, scope);
                    case Invert:
                        return new InvertGreen(image, scope);
                    case Set:
                    default:
                        return new SetGreen(image, scope);
                }
            case Blue:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseBlue(image, scope);
                    case Decrease:
                        return new DecreaseBlue(image, scope);
                    case Filter:
                        return new FilterBlue(image, scope);
                    case Invert:
                        return new InvertBlue(image, scope);
                    case Set:
                    default:
                        return new SetBlue(image, scope);
                }
            case Yellow:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseYellow(image, scope);
                    case Decrease:
                        return new DecreaseYellow(image, scope);
                    case Filter:
                        return new FilterYellow(image, scope);
                    case Invert:
                        return new InvertYellow(image, scope);
                    case Set:
                    default:
                        return new SetYellow(image, scope);
                }
            case Cyan:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseCyan(image, scope);
                    case Decrease:
                        return new DecreaseCyan(image, scope);
                    case Filter:
                        return new FilterCyan(image, scope);
                    case Invert:
                        return new InvertCyan(image, scope);
                    case Set:
                    default:
                        return new SetCyan(image, scope);
                }
            case Magenta:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseMagenta(image, scope);
                    case Decrease:
                        return new DecreaseMagenta(image, scope);
                    case Filter:
                        return new FilterMagenta(image, scope);
                    case Invert:
                        return new InvertMagenta(image, scope);
                    case Set:
                    default:
                        return new SetMagenta(image, scope);
                }
            case RGB:
                switch (colorActionType) {
                    case Increase:
                        return new IncreaseRGB(image, scope);
                    case Decrease:
                        return new DecreaseRGB(image, scope);
                    case Invert:
                        return new InvertRGB(image, scope);
//                    case Set:
//                    default:
//                        return new SetRGB(image, scope);
                }
            default:
                return null;
        }
    }

    public static PixelsOperation create(Image image, ImageScope scope,
            OperationType operationType, ColorActionType colorActionType) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        return create(bufferedImage, scope, operationType, colorActionType);
    }

    public static BufferedImage replaceColor(BufferedImage image, Color oldColor, Color newColor, int distance) {
        PixelsOperation pixelsOperation = replaceColorOperation(image, oldColor, newColor, distance);
        if (pixelsOperation == null) {
            return image;
        }
        return pixelsOperation.operateImage();
    }

    public static PixelsOperation replaceColorOperation(BufferedImage image, Color oldColor, Color newColor, int distance) {
        if (oldColor == null || distance < 0) {
            return null;
        }
        try {
            ImageScope scope = new ImageScope();
            scope.setScopeType(ImageScope.ScopeType.Color);
            scope.setColorScopeType(ImageScope.ColorScopeType.Color);
            scope.getColors().add(oldColor);
            scope.setColorDistance(distance);
            PixelsOperation pixelsOperation = PixelsOperationFactory.create(image,
                    scope, OperationType.ReplaceColor, ColorActionType.Set);
            pixelsOperation.setColorPara1(oldColor);
            pixelsOperation.setColorPara2(newColor);
            return pixelsOperation;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }


    /*
        subclass
     */
    public static class ShowScope extends PixelsOperation {

        public ShowScope(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.ShowScope;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    (int) (scope.getOpacity() * 255));
        }
    }

    public static class Sepia extends PixelsOperation {

        public Sepia(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Sepia;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return ColorConvertTools.pixel2Sepia(color, intPara1);
        }
    }

    public static class Thresholding extends PixelsOperation {

        public Thresholding(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Thresholding;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return ColorConvertTools.thresholdingColor(color, intPara1, intPara2, intPara3);
        }
    }

    public static class ReplaceColor extends PixelsOperation {

        public ReplaceColor(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.ReplaceColor;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return colorPara2;
        }
    }

    public static class ReplaceFilter extends PixelsOperation {

        private float[] paraHSB;

        public ReplaceFilter(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.ReplaceColor;
            this.colorActionType = ColorActionType.Filter;
            this.image = image;
            this.scope = scope;
        }

        @Override
        public PixelsOperation setColorPara2(Color colorPara2) {
            this.colorPara2 = colorPara2;
            paraHSB = ColorConvertTools.color2hsb(colorPara2);
            return this;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb = ColorConvertTools.color2hsb(color);
            return ColorConvertTools.hsb2rgb(paraHSB[0], paraHSB[1], hsb[2]);
        }
    }

    public static class SetColor extends PixelsOperation {

        public SetColor(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Color;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return colorPara1;
        }
    }

    public static class FilterColor extends PixelsOperation {

        private float[] paraHSB;

        public FilterColor(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Color;
            this.colorActionType = ColorActionType.Filter;
            this.image = image;
            this.scope = scope;
        }

        @Override
        public PixelsOperation setColorPara1(Color colorPara1) {
            this.colorPara1 = colorPara1;
            paraHSB = ColorConvertTools.color2hsb(colorPara1);
            return this;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb = ColorConvertTools.color2hsb(color);
            return ColorConvertTools.hsb2rgb(paraHSB[0], paraHSB[1], hsb[2]);
        }
    }

    public static class BlendColor extends PixelsOperation {

        protected PixelsBlend blender;

        public BlendColor(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Blend;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        public void setBlender(PixelsBlend blender) {
            this.blender = blender;
        }

        @Override
        protected Color operateColor(Color color) {
            if (blender == null) {
                return color;
            }
            return blender.blend(colorPara1, color);
        }
    }

    public static class SetOpacity extends PixelsOperation {

        public SetOpacity(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Opacity;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    Math.min(Math.max(intPara1, 0), 255));
        }
    }

    public static class IncreaseOpacity extends PixelsOperation {

        public IncreaseOpacity(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Opacity;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    Math.min(Math.max(color.getAlpha() + intPara1, 0), 255));
        }
    }

    public static class DecreaseOpacity extends PixelsOperation {

        public DecreaseOpacity(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Opacity;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    Math.min(Math.max(color.getAlpha() - intPara1, 0), 255));
        }
    }

    public static class SetPreOpacity extends PixelsOperation {

        public SetPreOpacity(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.PreOpacity;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            int opacity = Math.min(Math.max(intPara1, 0), 255);
            float f = opacity / 255.0f;
            return ColorBlendTools.blendColor(color, f, bkColor);
        }
    }

    public static class IncreasePreOpacity extends PixelsOperation {

        public IncreasePreOpacity(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.PreOpacity;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            int opacity = Math.min(Math.max(color.getAlpha() + intPara1, 0), 255);
            float f = opacity / 255.0f;
            return ColorBlendTools.blendColor(color, f, bkColor);
        }
    }

    public static class DecreasePreOpacity extends PixelsOperation {

        public DecreasePreOpacity(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.PreOpacity;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            int opacity = Math.min(Math.max(color.getAlpha() - intPara1, 0), 255);
            float f = opacity / 255.0f;
            return ColorBlendTools.blendColor(color, f, bkColor);
        }
    }

    public static class SetBrightness extends PixelsOperation {

        public SetBrightness(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Brightness;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ColorConvertTools.color2hsb(color);
            float f = floatPara1;
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ColorConvertTools.hsb2rgb(hsb[0], hsb[1], f);
        }
    }

    public static class IncreaseBrightness extends PixelsOperation {

        public IncreaseBrightness(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Brightness;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ColorConvertTools.color2hsb(color);
            float f = hsb[2] * (1.0f + floatPara1);
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ColorConvertTools.hsb2rgb(hsb[0], hsb[1], f);
        }
    }

    public static class DecreaseBrightness extends PixelsOperation {

        public DecreaseBrightness(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Brightness;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ColorConvertTools.color2hsb(color);
            float f = hsb[2] * (1.0f - floatPara1);
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ColorConvertTools.hsb2rgb(hsb[0], hsb[1], f);
        }
    }

    public static class SetSaturation extends PixelsOperation {

        public SetSaturation(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Saturation;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ColorConvertTools.color2hsb(color);
            float f = floatPara1;
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ColorConvertTools.hsb2rgb(hsb[0], f, hsb[2]);
        }
    }

    public static class IncreaseSaturation extends PixelsOperation {

        public IncreaseSaturation(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Saturation;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ColorConvertTools.color2hsb(color);
            float f = hsb[1] * (1.0f + floatPara1);
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ColorConvertTools.hsb2rgb(hsb[0], f, hsb[2]);
        }
    }

    public static class DecreaseSaturation extends PixelsOperation {

        public DecreaseSaturation(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Saturation;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ColorConvertTools.color2hsb(color);
            float f = hsb[1] * (1.0f - floatPara1);
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ColorConvertTools.hsb2rgb(hsb[0], f, hsb[2]);
        }
    }

    public static class SetHue extends PixelsOperation {

        public SetHue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Hue;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ColorConvertTools.color2hsb(color);
            float f = floatPara1;
            if (f > 1.0f) {
                f = f - 1.0f;
            }
            if (f < 0.0f) {
                f = f + 1.0f;
            }
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ColorConvertTools.hsb2rgb(f, hsb[1], hsb[2]);
        }
    }

    public static class IncreaseHue extends PixelsOperation {

        public IncreaseHue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Hue;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ColorConvertTools.color2hsb(color);
            float f = hsb[0] + floatPara1;
            if (f > 1.0f) {
                f = f - 1.0f;
            }
            if (f < 0.0f) {
                f = f + 1.0f;
            }
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ColorConvertTools.hsb2rgb(f, hsb[1], hsb[2]);
        }
    }

    public static class DecreaseHue extends PixelsOperation {

        public DecreaseHue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Hue;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            float[] hsb;
            hsb = ColorConvertTools.color2hsb(color);
            float f = hsb[0] - floatPara1;
            if (f > 1.0f) {
                f = f - 1.0f;
            }
            if (f < 0.0f) {
                f = f + 1.0f;
            }
            f = Math.min(Math.max(f, 0.0f), 1.0f);
            return ColorConvertTools.hsb2rgb(f, hsb[1], hsb[2]);
        }
    }

    public static class SetRed extends PixelsOperation {

        public SetRed(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Red;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(intPara1, 0), 255),
                    color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static class IncreaseRed extends PixelsOperation {

        public IncreaseRed(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Red;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() + intPara1, 0), 255),
                    color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static class DecreaseRed extends PixelsOperation {

        public DecreaseRed(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Red;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() - intPara1, 0), 255),
                    color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static class FilterRed extends PixelsOperation {

        public FilterRed(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Red;
            this.colorActionType = ColorActionType.Filter;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), 0, 0, color.getAlpha());
        }
    }

    public static class InvertRed extends PixelsOperation {

        public InvertRed(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Red;
            this.colorActionType = ColorActionType.Invert;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(255 - color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static class SetGreen extends PixelsOperation {

        public SetGreen(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Green;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), Math.min(Math.max(intPara1, 0), 255),
                    color.getBlue(), color.getAlpha());
        }
    }

    public static class IncreaseGreen extends PixelsOperation {

        public IncreaseGreen(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Green;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), Math.min(Math.max(color.getGreen() + intPara1, 0), 255),
                    color.getBlue(), color.getAlpha());
        }
    }

    public static class DecreaseGreen extends PixelsOperation {

        public DecreaseGreen(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Green;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), Math.min(Math.max(color.getGreen() - intPara1, 0), 255),
                    color.getBlue(), color.getAlpha());
        }
    }

    public static class FilterGreen extends PixelsOperation {

        public FilterGreen(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Green;
            this.colorActionType = ColorActionType.Filter;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(0, color.getGreen(), 0, color.getAlpha());
        }
    }

    public static class InvertGreen extends PixelsOperation {

        public InvertGreen(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Green;
            this.colorActionType = ColorActionType.Invert;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), 255 - color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static class SetBlue extends PixelsOperation {

        public SetBlue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Blue;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(),
                    Math.min(Math.max(intPara1, 0), 255), color.getAlpha());
        }
    }

    public static class IncreaseBlue extends PixelsOperation {

        public IncreaseBlue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Blue;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(),
                    Math.min(Math.max(color.getBlue() + intPara1, 0), 255), color.getAlpha());
        }
    }

    public static class DecreaseBlue extends PixelsOperation {

        public DecreaseBlue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Blue;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(),
                    Math.min(Math.max(color.getBlue() - intPara1, 0), 255), color.getAlpha());
        }
    }

    public static class FilterBlue extends PixelsOperation {

        public FilterBlue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Blue;
            this.colorActionType = ColorActionType.Filter;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(0, 0, color.getBlue(), color.getAlpha());
        }
    }

    public static class InvertBlue extends PixelsOperation {

        public InvertBlue(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Blue;
            this.colorActionType = ColorActionType.Invert;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(), 255 - color.getBlue(), color.getAlpha());
        }
    }

    public static class SetYellow extends PixelsOperation {

        public SetYellow(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Yellow;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            int v = Math.min(Math.max(intPara1, 0), 255);
            return new Color(v, v, color.getBlue(), color.getAlpha());
        }
    }

    public static class IncreaseYellow extends PixelsOperation {

        public IncreaseYellow(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Yellow;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() + intPara1, 0), 255),
                    Math.min(Math.max(color.getGreen() + intPara1, 0), 255),
                    color.getBlue(), color.getAlpha());
        }
    }

    public static class DecreaseYellow extends PixelsOperation {

        public DecreaseYellow(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Yellow;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() - intPara1, 0), 255),
                    Math.min(Math.max(color.getGreen() - intPara1, 0), 255),
                    color.getBlue(), color.getAlpha());
        }
    }

    public static class FilterYellow extends PixelsOperation {

        public FilterYellow(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Yellow;
            this.colorActionType = ColorActionType.Filter;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), color.getGreen(), 0, color.getAlpha());
        }
    }

    public static class InvertYellow extends PixelsOperation {

        public InvertYellow(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Yellow;
            this.colorActionType = ColorActionType.Invert;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(255 - color.getRed(), 255 - color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static class SetCyan extends PixelsOperation {

        public SetCyan(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Cyan;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            int v = Math.min(Math.max(intPara1, 0), 255);
            return new Color(color.getRed(), v, v, color.getAlpha());
        }
    }

    public static class IncreaseCyan extends PixelsOperation {

        public IncreaseCyan(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Cyan;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(),
                    Math.min(Math.max(color.getGreen() + intPara1, 0), 255),
                    Math.min(Math.max(color.getBlue() + intPara1, 0), 255),
                    color.getAlpha());
        }
    }

    public static class DecreaseCyan extends PixelsOperation {

        public DecreaseCyan(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Cyan;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(),
                    Math.min(Math.max(color.getGreen() - intPara1, 0), 255),
                    Math.min(Math.max(color.getBlue() - intPara1, 0), 255),
                    color.getAlpha());
        }
    }

    public static class FilterCyan extends PixelsOperation {

        public FilterCyan(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Cyan;
            this.colorActionType = ColorActionType.Filter;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(0, color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    public static class InvertCyan extends PixelsOperation {

        public InvertCyan(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Cyan;
            this.colorActionType = ColorActionType.Invert;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
        }
    }

    public static class SetMagenta extends PixelsOperation {

        public SetMagenta(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Magenta;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            int v = Math.min(Math.max(intPara1, 0), 255);
            return new Color(v, color.getGreen(), v, color.getAlpha());
        }
    }

    public static class IncreaseMagenta extends PixelsOperation {

        public IncreaseMagenta(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Magenta;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() + intPara1, 0), 255),
                    color.getGreen(),
                    Math.min(Math.max(color.getBlue() + intPara1, 0), 255), color.getAlpha());
        }
    }

    public static class DecreaseMagenta extends PixelsOperation {

        public DecreaseMagenta(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Magenta;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() - intPara1, 0), 255),
                    color.getGreen(),
                    Math.min(Math.max(color.getBlue() - intPara1, 0), 255), color.getAlpha());
        }
    }

    public static class FilterMagenta extends PixelsOperation {

        public FilterMagenta(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Magenta;
            this.colorActionType = ColorActionType.Filter;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(color.getRed(), 0, color.getBlue(), color.getAlpha());
        }
    }

    public static class InvertMagenta extends PixelsOperation {

        public InvertMagenta(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.Magenta;
            this.colorActionType = ColorActionType.Invert;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(255 - color.getRed(), color.getGreen(), 255 - color.getBlue(), color.getAlpha());
        }
    }

    public static class SetRGB extends PixelsOperation {

        public SetRGB(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.RGB;
            this.colorActionType = ColorActionType.Set;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            int v = Math.min(Math.max(intPara1, 0), 255);
            return new Color(v, v, v, color.getAlpha());
        }
    }

    public static class IncreaseRGB extends PixelsOperation {

        public IncreaseRGB(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.RGB;
            this.colorActionType = ColorActionType.Increase;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() + intPara1, 0), 255),
                    Math.min(Math.max(color.getGreen() + intPara1, 0), 255),
                    Math.min(Math.max(color.getBlue() + intPara1, 0), 255), color.getAlpha());
        }
    }

    public static class DecreaseRGB extends PixelsOperation {

        public DecreaseRGB(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.RGB;
            this.colorActionType = ColorActionType.Decrease;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(Math.min(Math.max(color.getRed() - intPara1, 0), 255),
                    Math.min(Math.max(color.getGreen() - intPara1, 0), 255),
                    Math.min(Math.max(color.getBlue() - intPara1, 0), 255), color.getAlpha());
        }
    }

    public static class InvertRGB extends PixelsOperation {

        public InvertRGB(BufferedImage image, ImageScope scope) {
            this.operationType = OperationType.RGB;
            this.colorActionType = ColorActionType.Invert;
            this.image = image;
            this.scope = scope;
        }

        @Override
        protected Color operateColor(Color color) {
            return new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(), color.getAlpha());
        }
    }
}
