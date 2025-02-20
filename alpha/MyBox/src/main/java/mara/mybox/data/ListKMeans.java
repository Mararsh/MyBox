package mara.mybox.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2019-10-7
 * @License Apache License Version 2.0
 */
public class ListKMeans<T> {

    protected List<T> centers;
    protected List<Integer>[] clusters;
    protected int k, maxIteration, loopCount;
    protected long cost;
    protected Map<T, T> dataMap;
    protected FxTask task;

    public ListKMeans() {
        k = 1;
        maxIteration = 10000;
    }

    public static ListKMeans create() {
        return new ListKMeans();
    }

    public void initData() {

    }

    public boolean isDataEmpty() {
        return true;
    }

    public int dataSize() {
        return 0;
    }

    public T getData(int index) {
        return null;
    }

    public List<T> allData() {
        return null;
    }

    public int centerSize() {
        return centers != null ? centers.size() : 0;
    }

    public void initCenters() {
        try {
            centers = new ArrayList<>();
            int dataSize = dataSize();
            if (dataSize < k) {
                centers.addAll(allData());
                return;
            }
            int mod = dataSize / k;
            for (int i = 0; i < dataSize; i = i + mod) {
                if (task != null && !task.isWorking()) {
                    return;
                }
                centers.add(getData(i));
                if (centers.size() == k) {
                    return;
                }
            }
            Random random = new Random();
            while (centers.size() < k) {
                if (task != null && !task.isWorking()) {
                    return;
                }
                int index = random.nextInt(dataSize);
                T d = getData(index);
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
        if (isDataEmpty()) {
            initData();
            if (isDataEmpty()) {
                return false;
            }
        }
        if (centers == null || centers.isEmpty()) {
            initCenters();
            if (centers == null || centers.isEmpty()) {
                return false;
            }
        }
        int dataSize = dataSize();
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
                if (task != null && !task.isWorking()) {
                    return false;
                }
                for (int i = 0; i < k; ++i) {
                    clusters[i] = new ArrayList<>();
                }
                for (int i = 0; i < dataSize; ++i) {
                    if (task != null && !task.isWorking()) {
                        return false;
                    }
                    T p = getData(i);
                    double min = Double.MAX_VALUE;
                    int index = 0;
                    for (int j = 0; j < centers.size(); ++j) {
                        if (task != null && !task.isWorking()) {
                            return false;
                        }
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
                    if (task != null && !task.isWorking()) {
                        return false;
                    }
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
        if (isDataEmpty() || centers == null || clusters == null) {
            return false;
        }
        dataMap = new HashMap<>();
        for (int i = 0; i < clusters.length; ++i) {
            if (task != null && !task.isWorking()) {
                return false;
            }
            List<Integer> cluster = clusters[i];
            T centerData = centers.get(i);
            for (Integer index : cluster) {
                if (task != null && !task.isWorking()) {
                    return false;
                }
                dataMap.put(getData(index), centerData);
            }
        }
//        MyBoxLog.debug("dataMap: " + dataMap.size());
        return true;
    }

    public T belongCenter(T value) {
        if (isDataEmpty() || value == null || clusters == null) {
            return value;
        }
        for (int i = 0; i < clusters.length; ++i) {
            if (task != null && !task.isWorking()) {
                return null;
            }
            List<Integer> cluster = clusters[i];
            T centerData = centers.get(i);
            for (Integer index : cluster) {
                if (task != null && !task.isWorking()) {
                    return null;
                }
                if (getData(index) == value) {
                    return centerData;
                }
            }
        }
        return null;
    }

    public T preProcess(T value) {
        return value;
    }

    public T map(T value) {
        try {
            if (value == null) {
                return value;
            }
            if (dataMap == null) {
                return belongCenter(value);
            }
            T targetValue = preProcess(value);
            T mappedValue = dataMap.get(targetValue);
            // Some new colors maybe generated outside regions due to dithering again
            if (mappedValue == null) {
                mappedValue = targetValue;
                double minDistance = Integer.MAX_VALUE;
                for (int i = 0; i < centers.size(); ++i) {
                    if (task != null && !task.isWorking()) {
                        return null;
                    }
                    T centerValue = centers.get(i);
                    double distance = distance(targetValue, centerValue);
                    if (distance < minDistance) {
                        minDistance = distance;
                        mappedValue = centerValue;
                    }
                }
                dataMap.put(targetValue, mappedValue);
            }
            return mappedValue;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    /*
        get/set
     */
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

    public FxTask getTask() {
        return task;
    }

    public ListKMeans setTask(FxTask task) {
        this.task = task;
        return this;
    }

}
