package mara.mybox.data;

import mara.mybox.db.data.BaseData;
import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-10-15
 * @License Apache License Version 2.0
 */
public class DownloadHistory extends BaseData {

    protected long dhid;
    protected String url, title, name, filename;
    protected Date downloadTime;
    protected int index;

    public DownloadHistory() {
    }

    public DownloadHistory(Link link) {
        if (link == null) {
            return;
        }
        if (link.getUrl() != null) {
            url = link.getUrl().toString().toLowerCase();
        } else if (link.getAddress() != null) {
            url = link.getAddress().toLowerCase();
        }
        title = link.getTitle();
        name = link.getName();
        filename = link.getFile();
        downloadTime = new Date();
        index = link.getIndex();
    }

    public DownloadHistory(String url, String title, String name, String filename, int index) {
        this.url = url;
        this.title = title;
        this.name = name;
        this.filename = filename;
        this.index = index;
    }

    /*
        Static methods
     */
    public static boolean valid(DownloadHistory data) {
        return data != null && data.getUrl() != null;
    }

    public static boolean setValue(DownloadHistory data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "dhid":
                    data.setDhid(value == null ? -1 : (long) value);
                    return true;
                case "url":
                    data.setUrl(value == null ? null : (String) value);
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "index":
                    data.setIndex(value == null ? -1 : (int) value);
                    return true;
                case "name":
                    data.setName(value == null ? null : (String) value);
                    return true;
                case "filename":
                    data.setFilename(value == null ? null : (String) value);
                    return true;
                case "download_time":
                    data.setDownloadTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(DownloadHistory data, String column) {
        if (data == null || column == null) {
            return null;
        }
        try {
            switch (column) {
                case "dhid":
                    return data.getDhid();
                case "url":
                    return data.getUrl() == null ? null : (data.getUrl().length() > 4096 ? data.getUrl().substring(0, 4096) : data.getUrl());
                case "title":
                    return data.getTitle() == null ? null : (data.getTitle().length() > 4096 ? data.getTitle().substring(0, 4096) : data.getTitle());
                case "name":
                    return data.getName() == null ? null : (data.getName().length() > 4096 ? data.getName().substring(0, 4096) : data.getName());
                case "index":
                    return data.getIndex();
                case "filename":
                    return data.getFilename() == null ? null : (data.getFilename().length() > 4096 ? data.getFilename().substring(0, 4096) : data.getFilename());
                case "download_time":
                    return data.getDownloadTime();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return null;
    }

    /*
        get/set
     */
    public long getDhid() {
        return dhid;
    }

    public void setDhid(long dhid) {
        this.dhid = dhid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Date getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(Date downloadTime) {
        this.downloadTime = downloadTime;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
