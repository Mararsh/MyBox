package mara.mybox.data;

import java.util.Date;
import mara.mybox.db.TableBase;
import mara.mybox.db.TableDownloadHistory;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2020-10-15
 * @License Apache License Version 2.0
 */
public class DownloadHistory extends TableData {

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

    @Override
    public TableBase getTable() {
        if (table == null) {
            table = new TableDownloadHistory();
        }
        return table;
    }

    @Override
    public boolean valid() {
        return url != null;
    }

    @Override
    public boolean setValue(String column, Object value) {
        if (column == null) {
            return false;
        }
        try {
            switch (column) {
                case "dhid":
                    dhid = value == null ? -1 : (long) value;
                    return true;
                case "url":
                    url = value == null ? null : (String) value;
                    return true;
                case "title":
                    title = value == null ? null : (String) value;
                    return true;
                case "index":
                    index = value == null ? -1 : (int) value;
                    return true;
                case "name":
                    name = value == null ? null : (String) value;
                    return true;
                case "filename":
                    filename = value == null ? null : (String) value;
                    return true;
                case "download_time":
                    downloadTime = value == null ? null : (Date) value;
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @Override
    public Object getValue(String column) {
        if (column == null) {
            return null;
        }
        try {
            switch (column) {
                case "dhid":
                    return dhid;
                case "url":
                    return url == null ? null : (url.length() > 4096 ? url.substring(0, 4096) : url);
                case "title":
                    return title == null ? null : (title.length() > 4096 ? title.substring(0, 4096) : title);
                case "name":
                    return name == null ? null : (name.length() > 4096 ? name.substring(0, 4096) : name);
                case "index":
                    return index;
                case "filename":
                    return filename == null ? null : (filename.length() > 4096 ? filename.substring(0, 4096) : filename);
                case "download_time":
                    return downloadTime;
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
