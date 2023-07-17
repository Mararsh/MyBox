package mara.mybox.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-10-7
 * @License Apache License Version 2.0
 */
public class ListKMeans<T> {

    protected List<T> data;
    protected List<T> centers;
    protected List<Integer>[] clusters;
    protected int k = 1, maxIteration = 10000, loopCount;
    protected long cost;
    protected Map<T, T> dataMap;

    public ListKMeans() {
    }

    public static ListKMeans create() {
        return new ListKMeans();
    }

    public void initData() {

    }

    public void initCenters() {
        try {
            centers = new ArrayList<>();
            int dataSize = data.size();
            if (dataSize < k) {
                centers.addAll(data);
                return;
            }
            Random random = new Random();
            while (centers.size() < k) {
                int index = random.nextInt(dataSize);
                T d = data.get(index);
                if (!centers.contains(d)) {
                    centers.add(d);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public double distance(T p1, T p2) {
        return 0;
    }

    public boolean equal(T p1, T p2) {
        return true;
    }

    public T calculateCenters(List<Integer> ps) {
        return null;
    }

    public boolean run() {
        loopCount = 0;
        if (k <= 0) {
            return false;
        }
        if (data == null || data.isEmpty()) {
            initData();
            if (data == null || data.isEmpty()) {
                return false;
            }
        }
        if (centers == null || centers.isEmpty()) {
            initCenters();
            if (centers == null || centers.isEmpty()) {
                return false;
            }
        }
        int dataSize = data.size();
        if (dataSize < k) {
            clusters = new ArrayList[dataSize];
            for (int i = 0; i < dataSize; ++i) {
                clusters[i] = new ArrayList<>();
                clusters[i].add(i);
            }
            return true;
        }
        clusters = new ArrayList[k];
        try {
//            MyBoxLog.console("data: " + data.size() + " k:" + k + "   maxIteration:" + maxIteration + "  loopCount:" + loopCount);
            while (true) {
                for (int i = 0; i < k; ++i) {
                    clusters[i] = new ArrayList<>();
                }
                for (int i = 0; i < dataSize; ++i) {
                    T p = data.get(i);
                    double min = Double.MAX_VALUE;
                    int index = 0;
                    for (int j = 0; j < centers.size(); ++j) {
                        T center = centers.get(j);
                        double distance = distance(center, p);
                        if (distance < min) {
                            min = distance;
                            index = j;
                        }
                    }
                    clusters[index].add(i);
                }
                boolean centerchange = false;
                for (int i = 0; i < k; ++i) {
                    T newCenter = calculateCenters(clusters[i]);
                    T oldCenter = centers.get(i);
                    if (!equal(newCenter, oldCenter)) {
                        centerchange = true;
                        centers.set(i, newCenter);
                    }
                }
                loopCount++;
                if (!centerchange || loopCount >= maxIteration) {
                    break;
                }
            }
//            MyBoxLog.console("loopCount: " + loopCount);
//            MyBoxLog.console("centers: " + centers.size() + "   clusters: " + clusters.length);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean makeMap() {
        if (data == null || centers == null || clusters == null) {
            return false;
        }
        dataMap = new HashMap<>();
        for (int i = 0; i < clusters.length; ++i) {
            List<Integer> cluster = clusters[i];
            T centerData = centers.get(i);
            for (Integer index : cluster) {
                dataMap.put(data.get(index), centerData);
            }
        }
//        MyBoxLog.debug("dataMap: " + dataMap.size());
        return true;
    }

    /*
        get/set
     */
    public List<T> getData() {
        return data;
    }

    public ListKMeans setData(List<T> data) {
        this.data = data;
        return this;
    }

    public List<T> getCenters() {
        return centers;
    }

    public ListKMeans setCenters(List<T> centers) {
        this.centers = centers;
        return this;
    }

    public int getK() {
        return k;
    }

    public ListKMeans setK(int k) {
        this.k = k;
        return this;
    }

    public int getMaxIteration() {
        return maxIteration;
    }

    public ListKMeans setMaxIteration(int maxIteration) {
        this.maxIteration = maxIteration;
        return this;
    }

    public int getLoopCount() {
        return loopCount;
    }

    public void setLoopCount(int loopCount) {
        this.loopCount = loopCount;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public List<Integer>[] getClusters() {
        return clusters;
    }

    public void setClusters(List<Integer>[] clusters) {
        this.clusters = clusters;
    }

    public Map<T, T> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<T, T> dataMap) {
        this.dataMap = dataMap;
    }

}
