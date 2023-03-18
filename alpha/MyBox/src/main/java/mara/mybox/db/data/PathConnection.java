package mara.mybox.db.data;

import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2023-3-17
 * @License Apache License Version 2.0
 */
public class PathConnection extends BaseData {

    protected long pcnid;
    protected Type type;
    protected String title, host, rootpath, path, username, password;
    protected boolean hostKeyCheck;
    protected int port, timeout, retry;
    protected Date modifyTime;

    public static enum Type {
        SFTP, HTTPS, HTTP, FTP
    }

    public PathConnection() {
        init();
    }

    final public void init() {
        pcnid = -1;
        title = null;
        host = null;
        path = null;
        username = null;
        password = null;
        type = null;
        port = -1;
        timeout = 5000;
        retry = 3;
        hostKeyCheck = false;
        modifyTime = new Date();
    }

    public PathConnection copy() {
        try {
            return (PathConnection) super.clone();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    /*
        Static methods
     */
    public static PathConnection create() {
        return new PathConnection();
    }

    public static boolean setValue(PathConnection data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "pcnid":
                    data.setPcnid(value == null ? null : (long) value);
                    return true;
                case "type":
                    data.setType(value == null ? null : Type.valueOf((String) value));
                    return true;
                case "title":
                    data.setTitle(value == null ? null : (String) value);
                    return true;
                case "host":
                    data.setHost(value == null ? null : (String) value);
                    return true;
                case "username":
                    data.setUsername(value == null ? null : (String) value);
                    return true;
                case "password":
                    data.setPassword(value == null ? null : (String) value);
                    return true;
                case "path":
                    data.setPath(value == null ? null : (String) value);
                    return true;
                case "port":
                    data.setPort(value == null ? -1 : (int) value);
                    return true;
                case "timeout":
                    data.setTimeout(value == null ? 5000 : (int) value);
                    return true;
                case "retry":
                    data.setRetry(value == null ? 3 : (int) value);
                    return true;
                case "host_key_check":
                    data.setHostKeyCheck(value == null ? null : (boolean) value);
                    return true;
                case "modify_time":
                    data.setModifyTime(value == null ? null : (Date) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(PathConnection data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "pcnid":
                return data.getPcnid();
            case "type":
                Type type = data.getType();
                return type == null ? null : type.name();
            case "title":
                return data.getTitle();
            case "host":
                return data.getHost();
            case "username":
                return data.getUsername();
            case "password":
                return data.getPassword();
            case "path":
                return data.getPath();
            case "port":
                return data.getPort();
            case "timeout":
                return data.getTimeout();
            case "retry":
                return data.getRetry();
            case "host_key_check":
                return data.isHostKeyCheck();
            case "modify_time":
                return data.getModifyTime();
        }
        return null;
    }

    public static boolean valid(PathConnection data) {
        return data != null && data.getPath() != null;
    }

    /*
        set
     */
    public void setPcnid(long pcnid) {
        this.pcnid = pcnid;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setRootpath(String rootpath) {
        this.rootpath = rootpath;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setHostKeyCheck(boolean hostKeyCheck) {
        this.hostKeyCheck = hostKeyCheck;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    /*
        get
     */
    public long getPcnid() {
        return pcnid;
    }

    public Type getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getHost() {
        return host;
    }

    public String getRootpath() {
        return rootpath;
    }

    public String getPath() {
        return path;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isHostKeyCheck() {
        return hostKeyCheck;
    }

    public int getPort() {
        return port;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getRetry() {
        return retry;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

}
