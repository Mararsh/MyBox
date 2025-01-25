package mara.mybox.image.tools;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 16:07:27
 * @License Apache License Version 2.0
 */
public class ColorComponentTools {

    public static enum ColorComponent {
        Gray, RedChannel, GreenChannel, BlueChannel, Hue, Saturation, Brightness, AlphaChannel
    }

    public static Map<ColorComponent, Color> ComponentColor;

    public static Color color(ColorComponent c) {
        if (ComponentColor == null) {
            ComponentColor = new HashMap<>();
            ComponentColor.put(ColorComponent.RedChannel, Color.RED);
            ComponentColor.put(ColorComponent.GreenChannel, Color.GREEN);
            ComponentColor.put(ColorComponent.BlueChannel, Color.BLUE);
            ComponentColor.put(ColorComponent.Hue, Color.PINK);
            ComponentColor.put(ColorComponent.Brightness, Color.ORANGE);
            ComponentColor.put(ColorComponent.Saturation, Color.CYAN);
            ComponentColor.put(ColorComponent.AlphaChannel, Color.YELLOW);
            ComponentColor.put(ColorComponent.Gray, Color.GRAY);
        }
        if (c == null) {
            return null;
        }
        return ComponentColor.get(c);
    }

    public static Color color(ColorComponent component, int value) {
        switch (component) {
            case RedChannel:
                return new Color(value, 0, 0, 255);
            case GreenChannel:
                return new Color(0, value, 0, 255);
            case BlueChannel:
                return new Color(0, 0, value, 255);
            case AlphaChannel:
                Color aColor = ColorComponentTools.color(component);
                return new Color(aColor.getRed(), aColor.getGreen(), aColor.getBlue(), value);
            case Gray:
                return new Color(value, value, value, 255);
            case Hue:
                return ColorConvertTools.hsb2rgb(value / 360.0F, 1.0F, 1.0F);
            case Saturation:
                float h1 = ColorConvertTools.getHue(ColorComponentTools.color(component));
                return ColorConvertTools.hsb2rgb(h1, value / 100.0F, 1.0F);
            case Brightness:
                float h2 = ColorConvertTools.getHue(ColorComponentTools.color(component));
                return ColorConvertTools.hsb2rgb(h2, 1.0F, value / 100.0F);
        }
        return null;
    }

    public static float percentage(ColorComponent component, int value) {
        switch (component) {
            case RedChannel:
            case GreenChannel:
            case BlueChannel:
            case AlphaChannel:
            case Gray:
                return value / 255f;
            case Hue:
                return value / 360f;
            case Saturation:
            case Brightness:
                return value / 100f;
        }
        return 0;
    }

    public static Color color(String name, int index) {
        return color(ColorComponentTools.component(name), index);
    }

    public static Color componentColor(String name) {
        return color(component(name));
    }

    public static ColorComponent component(String name) {
        for (ColorComponent c : ColorComponent.values()) {
            if (c.name().equals(name) || Languages.message(c.name()).equals(name)) {
                return c;
            }
        }
        return null;
    }

}
