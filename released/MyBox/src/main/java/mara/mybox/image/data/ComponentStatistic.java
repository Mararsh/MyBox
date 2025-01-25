package mara.mybox.image.data;

import mara.mybox.calculation.IntStatistic;
import mara.mybox.image.tools.ColorComponentTools.ColorComponent;

/**
 * @Author Mara
 * @CreateDate 2019-10-26
 * @License Apache License Version 2.0
 */
public class ComponentStatistic {

    protected ColorComponent component;
    protected IntStatistic statistic;
    protected int[] histogram;

    public static ComponentStatistic create() {
        return new ComponentStatistic();
    }

    /*
        get/set
     */
    public ColorComponent getComponent() {
        return component;
    }

    public ComponentStatistic setComponent(ColorComponent component) {
        this.component = component;
        return this;
    }

    public IntStatistic getStatistic() {
        return statistic;
    }

    public ComponentStatistic setStatistic(IntStatistic statistic) {
        this.statistic = statistic;
        return this;
    }

    public int[] getHistogram() {
        return histogram;
    }

    public ComponentStatistic setHistogram(int[] histogram) {
        this.histogram = histogram;
        return this;
    }

}
