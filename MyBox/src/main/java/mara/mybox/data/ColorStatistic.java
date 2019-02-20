package mara.mybox.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.paint.Color;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2019-2-11 12:53:19
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class ColorStatistic {

    private String type;
    private Color color;
    private int number;
    private float percentage;

    public ColorStatistic() {
    }

    public ColorStatistic(String type, Color color, int number, float percentage) {
        this.type = type;
        this.color = color;
        this.number = number;
        this.percentage = percentage;
    }

    public static List<ColorStatistic> newSort(List<ColorStatistic> values) {
        if (values == null) {
            return null;
        }
        List<ColorStatistic> sorted = new ArrayList<>();
        sorted.addAll(values);
        Collections.sort(sorted, new Comparator<ColorStatistic>() {
            @Override
            public int compare(ColorStatistic p1, ColorStatistic p2) {
                return p1.getNumber() - p2.getNumber();
            }
        });
        return sorted;
    }

    public static void sort(List<ColorStatistic> values) {
        if (values == null) {
            return;
        }
        Collections.sort(values, new Comparator<ColorStatistic>() {
            @Override
            public int compare(ColorStatistic p1, ColorStatistic p2) {
                return p1.getNumber() - p2.getNumber();
            }
        });
    }

    public static Map<String, Object> statistic(List<ColorStatistic> values) {
        Map<String, Object> statistic = new HashMap<>();
        if (values == null) {
            return statistic;
        }
        statistic.put("data", values);
        int sum = 0;
        for (ColorStatistic s : values) {
            sum += s.getNumber();
        }
        statistic.put("sum", new ColorStatistic("Sum", null, sum, 100.0f));
        statistic.put("average", new ColorStatistic("Average", null, (int) (sum / values.size()),
                ValueTools.roundFloat5(100.0f / values.size())));
        List<ColorStatistic> newList = ColorStatistic.newSort(values);
        ColorStatistic s = newList.get(0);
        statistic.put("minimum", new ColorStatistic("Minimum", s.getColor(), s.getNumber(), s.getPercentage()));
        s = newList.get(newList.size() - 1);
        statistic.put("maximum", new ColorStatistic("Maximum", s.getColor(), s.getNumber(), s.getPercentage()));
        ColorStatistic median = new ColorStatistic();
        median.setType("Median");
        if (newList.size() % 2 == 0) {
            ColorStatistic m1 = newList.get(newList.size() / 2);
            ColorStatistic m2 = newList.get(newList.size() / 2 + 1);
            median.setColor(m1.getColor());
            median.setNumber((m1.getNumber() + m2.getNumber()) / 2);
        } else {
            ColorStatistic m = newList.get(newList.size() / 2);
            median.setColor(m.getColor());
            median.setNumber(m.getNumber());
        }
        median.setPercentage(ValueTools.roundFloat5(median.getNumber() * 100.0f / sum));
        statistic.put("median", median);
        return statistic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

}
