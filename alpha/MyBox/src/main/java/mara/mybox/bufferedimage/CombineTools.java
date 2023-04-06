package mara.mybox.bufferedimage;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2018-6-27 18:58:57
 * @License Apache License Version 2.0
 */
public class CombineTools {

    public static Image combineSingleRow(ImageCombine imageCombine, List<ImageInformation> images,
            boolean isPart, boolean careTotal) {
        if (imageCombine == null || images == null) {
            return null;
        }
        try {
            int imageWidth;
            int imageHeight;
            int totalWidth = 0;
            int totalHeight = 0;
            int maxHeight = 0;
            int minHeight = Integer.MAX_VALUE;
            int sizeType = imageCombine.getSizeType();
            if (sizeType == ImageCombine.CombineSizeType.AlignAsBigger) {
                for (ImageInformation imageInfo : images) {
                    imageHeight = (int) imageInfo.getPickedHeight();
                    if (imageHeight > maxHeight) {
                        maxHeight = imageHeight;
                    }
                }
            } else if (sizeType == ImageCombine.CombineSizeType.AlignAsSmaller) {
                for (ImageInformation imageInfo : images) {
                    imageHeight = (int) imageInfo.getPickedHeight();
                    if (imageHeight < minHeight) {
                        minHeight = imageHeight;
                    }
                }
            }
            int x = isPart ? 0 : imageCombine.getMarginsValue();
            int y = isPart ? 0 : imageCombine.getMarginsValue();
            List<Integer> xs = new ArrayList<>();
            List<Integer> ys = new ArrayList<>();
            List<Integer> widths = new ArrayList<>();
            List<Integer> heights = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                ImageInformation imageInfo = images.get(i);
                imageWidth = (int) imageInfo.getPickedWidth();
                imageHeight = (int) imageInfo.getPickedHeight();
                if (sizeType == ImageCombine.CombineSizeType.KeepSize
                        || sizeType == ImageCombine.CombineSizeType.TotalWidth
                        || sizeType == ImageCombine.CombineSizeType.TotalHeight) {
                } else if (sizeType == ImageCombine.CombineSizeType.EachWidth) {
                    imageHeight = (imageHeight * imageCombine.getEachWidthValue()) / imageWidth;
                    imageWidth = imageCombine.getEachWidthValue();
                } else if (sizeType == ImageCombine.CombineSizeType.EachHeight) {
                    imageWidth = (imageWidth * imageCombine.getEachHeightValue()) / imageHeight;
                    imageHeight = imageCombine.getEachHeightValue();
                } else if (sizeType == ImageCombine.CombineSizeType.AlignAsBigger) {
                    imageWidth = (imageWidth * maxHeight) / imageHeight;
                    imageHeight = maxHeight;
                } else if (sizeType == ImageCombine.CombineSizeType.AlignAsSmaller) {
                    imageWidth = (imageWidth * minHeight) / imageHeight;
                    imageHeight = minHeight;
                }
                xs.add(x);
                ys.add(y);
                widths.add(imageWidth);
                heights.add(imageHeight);
                x += imageWidth + imageCombine.getIntervalValue();
                if (imageHeight > totalHeight) {
                    totalHeight = imageHeight;
                }
            }
            totalWidth = x - imageCombine.getIntervalValue();
            if (!isPart) {
                totalWidth += imageCombine.getMarginsValue();
                totalHeight += 2 * imageCombine.getMarginsValue();
            }
            Image newImage = combineImages(images, (int) totalWidth, (int) totalHeight,
                    FxColorTools.toAwtColor(imageCombine.getBgColor()), xs, ys, widths, heights,
                    imageCombine.getTotalWidthValue(), imageCombine.getTotalHeightValue(),
                    careTotal && (sizeType == ImageCombine.CombineSizeType.TotalWidth),
                    careTotal && (sizeType == ImageCombine.CombineSizeType.TotalHeight));
            return newImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Image combineSingleColumn(ImageCombine imageCombine,
            List<ImageInformation> imageInfos, boolean isPart, boolean careTotal) {
        if (imageCombine == null || imageInfos == null) {
            return null;
        }
        try {
            int imageWidth;
            int imageHeight;
            int totalWidth = 0;
            int totalHeight = 0;
            int maxWidth = 0;
            int minWidth = Integer.MAX_VALUE;
            int sizeType = imageCombine.getSizeType();
            if (sizeType == ImageCombine.CombineSizeType.AlignAsBigger) {
                for (ImageInformation imageInfo : imageInfos) {
                    imageWidth = (int) imageInfo.getPickedWidth();
                    if (imageWidth > maxWidth) {
                        maxWidth = imageWidth;
                    }
                }
            } else if (sizeType == ImageCombine.CombineSizeType.AlignAsSmaller) {
                for (ImageInformation imageInfo : imageInfos) {
                    imageWidth = (int) imageInfo.getPickedWidth();
                    if (imageWidth < minWidth) {
                        minWidth = imageWidth;
                    }
                }
            }
            int x = isPart ? 0 : imageCombine.getMarginsValue();
            int y = isPart ? 0 : imageCombine.getMarginsValue();
            List<Integer> xs = new ArrayList<>();
            List<Integer> ys = new ArrayList<>();
            List<Integer> widths = new ArrayList<>();
            List<Integer> heights = new ArrayList<>();
            for (ImageInformation imageInfo : imageInfos) {
                imageWidth = (int) imageInfo.getPickedWidth();
                imageHeight = (int) imageInfo.getPickedHeight();
                if (sizeType == ImageCombine.CombineSizeType.KeepSize
                        || sizeType == ImageCombine.CombineSizeType.TotalWidth
                        || sizeType == ImageCombine.CombineSizeType.TotalHeight) {
                } else if (sizeType == ImageCombine.CombineSizeType.EachWidth) {
                    if (!isPart) {
                        imageHeight = (imageHeight * imageCombine.getEachWidthValue()) / imageWidth;
                        imageWidth = imageCombine.getEachWidthValue();
                    }
                } else if (sizeType == ImageCombine.CombineSizeType.EachHeight) {
                    if (!isPart) {
                        imageWidth = (imageWidth * imageCombine.getEachHeightValue()) / imageHeight;
                        imageHeight = imageCombine.getEachHeightValue();
                    }
                } else if (sizeType == ImageCombine.CombineSizeType.AlignAsBigger) {
                    imageHeight = (imageHeight * maxWidth) / imageWidth;
                    imageWidth = maxWidth;
                } else if (sizeType == ImageCombine.CombineSizeType.AlignAsSmaller) {
                    imageHeight = (imageHeight * minWidth) / imageWidth;
                    imageWidth = minWidth;
                }
                xs.add(x);
                ys.add(y);
                widths.add((int) imageWidth);
                heights.add((int) imageHeight);
                y += imageHeight + imageCombine.getIntervalValue();
                if (imageWidth > totalWidth) {
                    totalWidth = imageWidth;
                }
            }
            totalHeight = y - imageCombine.getIntervalValue();
            if (!isPart) {
                totalWidth += 2 * imageCombine.getMarginsValue();
                totalHeight += imageCombine.getMarginsValue();
            }
            Image newImage = combineImages(imageInfos, (int) totalWidth, (int) totalHeight,
                    FxColorTools.toAwtColor(imageCombine.getBgColor()), xs, ys,
                    widths, heights, imageCombine.getTotalWidthValue(), imageCombine.getTotalHeightValue(),
                    careTotal && (sizeType == ImageCombine.CombineSizeType.TotalWidth),
                    careTotal && (sizeType == ImageCombine.CombineSizeType.TotalHeight));
            return newImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Image combineImages(List<ImageInformation> imageInfos, int totalWidth, int totalHeight, Color bgColor,
            List<Integer> xs, List<Integer> ys, List<Integer> widths, List<Integer> heights,
            int trueTotalWidth, int trueTotalHeight, boolean isTotalWidth, boolean isTotalHeight) {
        if (imageInfos == null || xs == null || ys == null || widths == null || heights == null) {
            return null;
        }
        try {
            BufferedImage target = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = target.createGraphics();
            if (AppVariables.imageRenderHints != null) {
                g.addRenderingHints(AppVariables.imageRenderHints);
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.setColor(bgColor);
            g.fillRect(0, 0, totalWidth, totalHeight);
            for (int i = 0; i < imageInfos.size(); ++i) {
                ImageInformation imageInfo = imageInfos.get(i);
                Image image = imageInfo.loadImage();
                BufferedImage source = SwingFXUtils.fromFXImage(image, null);
                g.drawImage(source, xs.get(i), ys.get(i), widths.get(i), heights.get(i), null);
            }
            if (isTotalWidth) {
                target = ScaleTools.scaleImageBySize(target, trueTotalWidth, (trueTotalWidth * totalHeight) / totalWidth);
            } else if (isTotalHeight) {
                target = ScaleTools.scaleImageBySize(target, (trueTotalHeight * totalWidth) / totalHeight, trueTotalHeight);
            }
            Image newImage = SwingFXUtils.toFXImage(target, null);
            return newImage;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
