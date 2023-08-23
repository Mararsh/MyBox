package mara.mybox.fxml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Map;
import mara.mybox.data.DownloadItem;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.HtmlReadTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.tools.UrlTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public class DownloadTask<Void> extends BaseTask<Void> {

    protected String address;
    protected URL url;
    protected HttpURLConnection connection;
    protected File targetPath, targetFile, tmpFile;
    protected long totalSize, currentSize;
    protected boolean readHead = false;
    protected Map<String, String> head;
    protected int responseCode;

    public static DownloadTask create() {
        DownloadTask task = new DownloadTask();
        return task;
    }

    @Override
    protected boolean initValues() {
        if (address == null) {
            error = Languages.message("InvalidData");
            return false;
        }
        startTime = new Date();
        currentSize = 0;
        try {
            url = UrlTools.url(address);
            return url != null;
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    @Override
    protected boolean handle() {
        if (readHead) {
            readHead();
        } else {
            download();
        }
        return true;
    }

    protected HttpURLConnection getConnection() {
        try {
            return NetworkTools.httpConnection(url);
        } catch (Exception e) {
            error = e.toString();
            MyBoxLog.debug(error);
            return null;
        }
    }

    protected void readHead() {
        try {
            connection = getConnection();
            head = HtmlReadTools.requestHead(connection);
            connection.disconnect();
            connection = null;
        } catch (Exception e) {
            error = e.toString();
            MyBoxLog.debug(error);
        }
    }

    protected boolean download() {
        try {
            String filename = url.getFile().substring(url.getFile().lastIndexOf('/'));
            targetFile = new File(targetPath.getAbsolutePath() + File.separator
                    + FileNameTools.filter(filename.trim()));
            connection = getConnection();
            connection.setRequestMethod("GET");
            responseCode = connection.getResponseCode();
            totalSize = connection.getContentLength();
            error = connection.getResponseMessage();
            connection.disconnect();
            progress();
            if (responseCode >= 400) {
                return false;
            }
            error = null;
            connection = getConnection();
//            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            connection.connect();
            tmpFile = new File(targetFile.getAbsolutePath() + ".downloading");
            progress();
            try (BufferedInputStream inStream = new BufferedInputStream(connection.getInputStream());
                    RandomAccessFile tmpFileStream = new RandomAccessFile(tmpFile, "rw")) {
                currentSize = tmpFile.length();
                if (currentSize > 0) {
                    inStream.skip(currentSize);
                    tmpFileStream.seek(currentSize);
                }
                progress();
                byte[] buf = new byte[AppValues.IOBufferLength];
                int len;
                while ((len = inStream.read(buf)) > 0) {
                    if (isCancelled()) {
                        break;
                    }
                    tmpFileStream.write(buf, 0, len);
                    currentSize += len;
                    progress();
                }
            }
            connection.disconnect();
            responseCode = 0;
            Files.copy(Paths.get(tmpFile.getAbsolutePath()), Paths.get(targetFile.getAbsolutePath()),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            FileDeleteTools.delete(tmpFile);
            return targetFile.exists();
        } catch (Exception e) {
            error = e.toString();
            MyBoxLog.debug(error);
            return false;
        }
    }

    protected void progress() {

    }

    @Override
    protected void taskQuit() {
        super.taskQuit();
        progress();
    }

    protected void assign(DownloadItem item) {
        item.setTask(this);
    }

    /*
        get/set
     */
    public String getAddress() {
        return address;
    }

    public DownloadTask setAddress(String address) {
        this.address = address;
        return this;
    }

    public URL getUrl() {
        return url;
    }

    public DownloadTask setUrl(URL url) {
        this.url = url;
        return this;
    }

    public File getTargetPath() {
        return targetPath;
    }

    public DownloadTask setTargetPath(File targetPath) {
        this.targetPath = targetPath;
        return this;
    }

    public Map<String, String> getHead() {
        return head;
    }

    public DownloadTask setHead(Map<String, String> head) {
        this.head = head;
        return this;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public DownloadTask setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    public File getTmpFile() {
        return tmpFile;
    }

    public DownloadTask setTmpFile(File tmpFile) {
        this.tmpFile = tmpFile;
        return this;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public DownloadTask setTotalSize(long totalSize) {
        this.totalSize = totalSize;
        return this;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public DownloadTask setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
        return this;
    }

    public boolean isReadHead() {
        return readHead;
    }

    public DownloadTask setReadHead(boolean readHead) {
        this.readHead = readHead;
        return this;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public DownloadTask setTargetFile(File targetFile) {
        this.targetFile = targetFile;
        return this;
    }

}
