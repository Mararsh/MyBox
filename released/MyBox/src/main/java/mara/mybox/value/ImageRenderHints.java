package mara.mybox.value;

import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.ImageHints;

/**
 * @Author Mara
 * @CreateDate 2022-1-17
 * @License Apache License Version 2.0
 */
public class ImageRenderHints {

    public static boolean applyHints() {
        return UserConfig.getBoolean("ApplyImageRenderOptions", false);
    }

    public static boolean applyHints(boolean apply) {
        return UserConfig.setBoolean("ApplyImageRenderOptions", apply);
    }

    public static Map<RenderingHints.Key, Object> loadImageRenderHints() {
        try {
            if (!applyHints()) {
                ImageHints = null;
                return null;
            }
            ImageHints = new HashMap<>();

            String render = UserConfig.getString("ImageRenderHint-" + RenderingHints.KEY_RENDERING.toString(), null);
            if (RenderingHints.VALUE_RENDER_QUALITY.toString().equals(render)) {
                ImageHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            } else if (RenderingHints.VALUE_RENDER_SPEED.toString().equals(render)) {
                ImageHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            } else {
                ImageHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
            }

            String crender = UserConfig.getString("ImageRenderHint-" + RenderingHints.KEY_COLOR_RENDERING.toString(), null);
            if (RenderingHints.VALUE_COLOR_RENDER_QUALITY.toString().equals(crender)) {
                ImageHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            } else if (RenderingHints.VALUE_COLOR_RENDER_SPEED.toString().equals(crender)) {
                ImageHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            } else {
                ImageHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_DEFAULT);
            }

            String inter = UserConfig.getString("ImageRenderHint-" + RenderingHints.KEY_INTERPOLATION.toString(), null);
            if (RenderingHints.VALUE_INTERPOLATION_BILINEAR.toString().equals(inter)) {
                ImageHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            } else if (RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR.toString().equals(inter)) {
                ImageHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            } else {
                ImageHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            }

            String ainter = UserConfig.getString("ImageRenderHint-" + RenderingHints.KEY_ALPHA_INTERPOLATION.toString(), null);
            if (RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY.toString().equals(ainter)) {
                ImageHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            } else if (RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED.toString().equals(ainter)) {
                ImageHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            } else {
                ImageHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
            }

            String anti = UserConfig.getString("ImageRenderHint-" + RenderingHints.KEY_ANTIALIASING.toString(), null);
            if (RenderingHints.VALUE_ANTIALIAS_ON.toString().equals(anti)) {
                ImageHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            } else if (RenderingHints.VALUE_ANTIALIAS_OFF.toString().equals(anti)) {
                ImageHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            } else {
                ImageHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
            }

            String tanti = UserConfig.getString("ImageRenderHint-" + RenderingHints.KEY_TEXT_ANTIALIASING.toString(), null);
            if (RenderingHints.VALUE_TEXT_ANTIALIAS_ON.toString().equals(tanti)) {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_OFF.toString().equals(tanti)) {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_GASP.toString().equals(tanti)) {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB.toString().equals(tanti)) {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR.toString().equals(tanti)) {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB.toString().equals(tanti)) {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB);
            } else if (RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR.toString().equals(tanti)) {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR);
            } else {
                ImageHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
            }

            String fm = UserConfig.getString("ImageRenderHint-" + RenderingHints.KEY_FRACTIONALMETRICS.toString(), null);
            if (RenderingHints.VALUE_FRACTIONALMETRICS_ON.toString().equals(fm)) {
                ImageHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            } else if (RenderingHints.VALUE_FRACTIONALMETRICS_OFF.toString().equals(fm)) {
                ImageHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            } else {
                ImageHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
            }

            String stroke = UserConfig.getString("ImageRenderHint-" + RenderingHints.KEY_STROKE_CONTROL.toString(), null);
            if (RenderingHints.VALUE_STROKE_NORMALIZE.toString().equals(stroke)) {
                ImageHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            } else if (RenderingHints.VALUE_STROKE_PURE.toString().equals(stroke)) {
                ImageHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            } else {
                ImageHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
            }

            String dither = UserConfig.getString("ImageRenderHint-" + RenderingHints.KEY_DITHERING.toString(), null);
            if (RenderingHints.VALUE_DITHER_ENABLE.toString().equals(dither)) {
                ImageHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            } else if (RenderingHints.VALUE_DITHER_DISABLE.toString().equals(dither)) {
                ImageHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            } else {
                ImageHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
            }
            saveImageRenderHints();
            return ImageHints;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static void saveImageRenderHints() {
        if (ImageHints == null) {
            return;
        }
        try {
            for (RenderingHints.Key key : ImageHints.keySet()) {
                UserConfig.setString("ImageRenderHint-" + key.toString(), ImageHints.get(key).toString());
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
