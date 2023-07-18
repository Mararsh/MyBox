/*
 * Apache License Version 2.0
 */
package mara.mybox.data;

import java.io.File;
import java.util.Date;
import mara.mybox.fxml.DownloadTask;
import mara.mybox.tools.FileTools;

/**
 *
 * @author mara
 */
public class DownloadItem {

    protected String address;
    protected File targetFile;
    protected long totalSize, currentSize, startTime, endTime, cost;
    protected String status, progress, speed;
    protected DownloadTask task;

    public static DownloadItem create() {
        return new DownloadItem();
    }

    public String getAddress() {
        return address;
    }

    public DownloadItem setAddress(String address) {
        this.address = address;
        return this;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public DownloadItem setTargetFile(File targetFile) {
        this.targetFile = targetFile;
        return this;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public DownloadItem setTotalSize(long totalSize) {
        this.totalSize = totalSize;
        return this;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public DownloadItem setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public DownloadItem setStatus(String status) {
        this.status = status;
        return this;
    }

    public long getStartTime() {
        return startTime;
    }

    public DownloadItem setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getProgress() {
        if (totalSize == 0) {
            return "0%";
        } else {
            int v = (int) (currentSize * 100 / totalSize);
            v = Math.max(0, Math.min(100, v));
            return v + "%";
        }
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getCost() {
        if (endTime > startTime) {
            return endTime - startTime;
        } else {
            long v = new Date().getTime() - startTime;
            return v > 0 ? v : 0;
        }
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public String getSpeed() {
        if (currentSize <= 0 || getCost() <= 0) {
            return "0";
        } else {
            return FileTools.showFileSize(currentSize / getCost()) + "/s";
        }
    }

    public DownloadTask getTask() {
        return task;
    }

    public DownloadItem setTask(DownloadTask task) {
        this.task = task;
        return this;
    }

}
