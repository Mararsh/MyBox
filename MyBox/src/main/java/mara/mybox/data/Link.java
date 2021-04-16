package mara.mybox.data;

import java.io.File;
import java.net.URL;
import java.util.Date;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.HtmlTools;

/**
 * @Author Mara
 * @CreateDate 2020-10-13
 * @License Apache License Version 2.0
 */
public class Link {

    private URL url;
    private String address, addressOriginal, addressPath, addressFile,
            title, name, file, fileParent, filename, html;
    private int index;
    private Date dlTime;

    public enum FilenameType {
        ByLinkName, ByLinkTitle, ByLinkAddress, None
    }

    public Link() {
    }

    public Link(URL url) {
        this.url = url;
        address = url.toString();
    }

    public Link(URL url, String address, String name, String title, int index) {
        this.url = url;
        this.address = address;
        this.name = name;
        this.title = title;
        this.index = index;
    }

    public String pageName(FilenameType nameType) {
        String pageName = null;
        if (nameType == null || nameType == FilenameType.ByLinkName) {
            pageName = name != null && !name.isBlank() ? name : title;
        } else if (nameType == FilenameType.ByLinkTitle) {
            pageName = title != null && !title.isBlank() ? title : name;
        }
        if (pageName == null || pageName.isBlank()) {
            pageName = HtmlTools.filePrefix(getUrl());
        }
        return FileTools.filenameFilter(pageName);
    }

    public String filename(File path, FilenameType nameType) {
        if (url == null || path == null) {
            return null;
        }
        try {
            String pageName = pageName(nameType);
            pageName = (pageName == null || pageName.isBlank()) ? "index" : pageName;
            if (url.getPath().isBlank()) {
                return path + File.separator + pageName + ".html";
            }
            String suffix = null;
            if (!getAddress().endsWith("/")) {
                suffix = HtmlTools.fileSuffix(url);
            }
            suffix = (suffix == null || suffix.isBlank()) ? ".html" : suffix;
            return path + File.separator + pageName + suffix;
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
            return null;
        }
    }

    public static Link create() {
        return new Link();
    }

    /*
        customized get/set
     */
    public String getAddress() {
        if (address == null && url != null) {
            address = url.toString();
        }
        return address;
    }

    public URL getUrl() {
        if (url == null && address != null) {
            try {
                url = new URL(address);
            } catch (Exception e) {
            }
        }
        return url;
    }

    public String getAddressPath() {
        url = getUrl();
        if (url != null) {
            addressPath = HtmlTools.fullPath(url);
        }
        return addressPath;
    }

    public String getAddressFile() {
        if (addressFile == null && getUrl() != null) {
            addressFile = HtmlTools.file(url);
        }
        return addressFile;
    }

    public String getFile() {
        return file;
    }

    public String getFilename() {
        if (filename == null && file != null) {
            filename = new File(file).getName();
        }
        return filename;
    }

    public String getFileParent() {
        if (fileParent == null && file != null) {
            fileParent = new File(file).getParent();
        }
        return fileParent;
    }

    /*
         get/set
     */
    public Link setUrl(URL url) {
        this.url = url;
        return this;
    }

    public Link setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getName() {
        return name;
    }

    public Link setName(String name) {
        this.name = name;
        return this;
    }

    public Link setFile(String file) {
        this.file = file;
        return this;
    }

    public Link setFileName(String fileName) {
        this.filename = fileName;
        return this;
    }

    public Link setAddressFile(String addressFile) {
        this.addressFile = addressFile;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Link setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getIndex() {
        return index;
    }

    public Link setIndex(int index) {
        this.index = index;
        return this;
    }

    public Link setAddressPath(String addressPath) {
        this.addressPath = addressPath;
        return this;
    }

    public String getAddressOriginal() {
        return addressOriginal;
    }

    public Link setAddressOriginal(String addressOriginal) {
        this.addressOriginal = addressOriginal;
        return this;
    }

    public Date getDlTime() {
        return dlTime;
    }

    public Link setDlTime(Date dlTime) {
        this.dlTime = dlTime;
        return this;
    }

    public Link setFilepath(String filepath) {
        this.fileParent = filepath;
        return this;
    }

    public String getHtml() {
        return html;
    }

    public Link setHtml(String html) {
        this.html = html;
        return this;
    }

}
