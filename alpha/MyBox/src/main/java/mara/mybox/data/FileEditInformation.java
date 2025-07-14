package mara.mybox.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.tools.TextTools.bomBytes;
import static mara.mybox.tools.TextTools.bomSize;
import static mara.mybox.tools.TextTools.checkCharsetByBom;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;
import thridparty.EncodingDetect;

/**
 * @Author Mara
 * @CreateDate 2018-12-10 13:06:33
 * @License Apache License Version 2.0
 */
public abstract class FileEditInformation extends FileInformation implements Cloneable {

    protected Edit_Type editType;
    protected boolean withBom, totalNumberRead, charsetDetermined;
    protected Charset charset;
    protected Line_Break lineBreak;
    protected String lineBreakValue;
    protected int objectUnit, lineBreakWidth;
    protected String[] filterStrings;
    protected FindReplaceFile findReplace;
    protected StringFilterType filterType;
    public Pagination pagination;

    public enum Edit_Type {
        Text, Bytes, Markdown
    }

    public enum StringFilterType {
        IncludeAll, IncludeOne, NotIncludeAll, NotIncludeAny,
        MatchRegularExpression, NotMatchRegularExpression,
        IncludeRegularExpression, NotIncludeRegularExpression
    }

    public enum Line_Break {
        LF, // Liunx/Unix
        CR, // IOS
        CRLF, // Windows
        Width,
        Value,
        Auto
    }

    public FileEditInformation() {
        editType = Edit_Type.Text;
    }

    public FileEditInformation(File file) {
        super(file);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            FileEditInformation newInfo = (FileEditInformation) super.clone();
            newInfo.setEditType(editType);
            newInfo.setCharset(charset);
            newInfo.setLineBreak(lineBreak);
            newInfo.setFindReplace(findReplace);
            newInfo.setFilterType(filterType);
            return newInfo;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    protected final void initValues() {
        filterType = StringFilterType.IncludeOne;
        withBom = totalNumberRead = charsetDetermined = false;
        charset = defaultCharset();
        pagination = new Pagination();
        pagination.init(editType == Edit_Type.Bytes
                ? Pagination.ObjectType.Bytes
                : Pagination.ObjectType.Text);
        findReplace = null;
        switch (System.lineSeparator()) {
            case "\r\n":
                lineBreak = Line_Break.CRLF;
                lineBreakValue = "\r\n";
                break;
            case "\r":
                lineBreak = Line_Break.CR;
                lineBreakValue = "\r";
                break;
            default:
                lineBreak = Line_Break.LF;
                lineBreakValue = "\n";
                break;
        }
        lineBreakWidth = 30;
        objectUnit = editType == Edit_Type.Bytes ? 3 : 1;
    }

    public static Charset defaultCharset() {
        //       return Charset.defaultCharset();
        return Charset.forName("UTF-8");
    }

    public static FileEditInformation create(Edit_Type type, File file) {
        switch (type) {
            case Text:
                return new TextEditInformation(file);
            case Bytes:
                return new BytesEditInformation(file);
            default:
                return new TextEditInformation(file);
        }
    }

    public boolean checkCharset() {
        try {
            if (file == null) {
                return false;
            }
            String setName;
            withBom = false;
            try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                byte[] header = new byte[4];
                int bufLen;
                if ((bufLen = inputStream.read(header, 0, 4)) > 0) {
                    header = ByteTools.subBytes(header, 0, bufLen);
                    setName = checkCharsetByBom(header);
                    if (setName != null) {
                        charset = Charset.forName(setName);
                        withBom = true;
                        return true;
                    }
                }
            }
            setName = EncodingDetect.detect(file);
            charset = Charset.forName(setName);
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean convertCharset(FxTask currentTask, FileEditInformation targetInfo) {
        try {
            if (file == null || charset == null
                    || targetInfo == null || targetInfo.getFile() == null || targetInfo.getCharset() == null) {
                return false;
            }
            try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                    InputStreamReader reader = new InputStreamReader(inputStream, charset);
                    BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetInfo.getFile()));
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, targetInfo.getCharset())) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                if (targetInfo.isWithBom()) {
                    byte[] bytes = bomBytes(targetInfo.getCharset().name());
                    outputStream.write(bytes);
                }
                char[] buf = new char[AppValues.IOBufferLength];
                int bufLen;
                while ((bufLen = reader.read(buf)) > 0) {
                    writer.write(new String(buf, 0, bufLen));
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public abstract boolean readTotalNumbers(FxTask currentTask);

    public abstract String readPage(FxTask currentTask, long pageNumber);

    public abstract boolean writeObject(FxTask currentTask, String text);

    public abstract boolean writePage(FxTask currentTask, FileEditInformation sourceInfo, String text);

    public abstract String readLines(FxTask currentTask, long from, long number);

    public abstract String readObjects(FxTask currentTask, long from, long number);

    public abstract File filter(FxTask currentTask, boolean recordLineNumbers);

    public String readLine(FxTask currentTask, long line) {
        return readLines(currentTask, line, 1);
    }

    public String readObject(FxTask currentTask, long index) {
        return readObjects(currentTask, index, 1);
    }

    public boolean isMatchFilters(String string) {
        if (string == null || string.isEmpty()) {
            return false;
        }
        switch (filterType) {
            case IncludeOne:
                return includeOne(string);
            case IncludeAll:
                return includeAll(string);
            case NotIncludeAny:
                return notIncludeAny(string);
            case NotIncludeAll:
                return notIncludeAll(string);
            case MatchRegularExpression:
                return matchRegularExpression(string);
            case NotMatchRegularExpression:
                return notMatchRegularExpression(string);
            case IncludeRegularExpression:
                return includeRegularExpression(string);
            case NotIncludeRegularExpression:
                return notIncludeRegularExpression(string);
            default:
                return false;
        }
    }

    public boolean includeOne(String string) {
        boolean found;
        for (String filter : filterStrings) {
            found = string.contains(filter);
            if (found) {
                return true;
            }
        }
        return false;
    }

    public boolean includeAll(String string) {
        boolean found;
        for (String filter : filterStrings) {
            found = string.contains(filter);
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public boolean notIncludeAny(String string) {
        boolean found;
        for (String filter : filterStrings) {
            found = string.contains(filter);
            if (found) {
                return false;
            }
        }
        return true;
    }

    public boolean notIncludeAll(String string) {
        boolean found;
        for (String filter : filterStrings) {
            found = string.contains(filter);
            if (!found) {
                return true;
            }
        }
        return false;
    }

    public boolean includeRegularExpression(String string) {
        return StringTools.include(string, filterStrings[0], false);
    }

    public boolean notIncludeRegularExpression(String string) {
        return !StringTools.include(string, filterStrings[0], false);
    }

    public boolean matchRegularExpression(String string) {
        return StringTools.match(string, filterStrings[0], false);
    }

    public boolean notMatchRegularExpression(String string) {
        return !StringTools.match(string, filterStrings[0], false);
    }

    public String filterTypeName() {
        return message(filterType.name());
    }

    public String lineBreakName() {
        if (lineBreak == null) {
            return "";
        }
        switch (lineBreak) {
            case Width:
                return message("BytesNumber") + lineBreakWidth;
            case Value:
                return message("BytesHex") + lineBreakWidth;
            case LF:
                return message("LFHex");
            case CR:
                return message("CRHex");
            case CRLF:
                return message("CRLFHex");
            default:
                return "";
        }
    }

    /*
        pagination
     */
    public long getRowsNumber() {
        return pagination != null ? pagination.getRowsNumber() : 0;
    }

    public void setRowsNumber(long v) {
        if (pagination != null) {
            pagination.setRowsNumber(v);
        }
    }

    public long getObjectsNumber() {
        return pagination != null ? pagination.getObjectsNumber() : 0;
    }

    public void setObjectsNumber(long v) {
        if (pagination != null) {
            pagination.setObjectsNumber(v);
        }
    }

    public long getPagesNumber() {
        return pagination != null ? pagination.getPagesNumber() : 0;
    }

    public void setPagesNumber(long v) {
        if (pagination != null) {
            pagination.setPagesNumber(v);
        }
    }

    public int getPageSize() {
        return pagination != null ? pagination.getPageSize() : 0;
    }

    public void setPageSize(int v) {
        if (pagination != null) {
            pagination.setPageSize(v);
        }
    }

    public long getCurrentPage() {
        return pagination != null ? pagination.getCurrentPage() : 0;
    }

    public void setCurrentPage(long v) {
        if (pagination != null) {
            pagination.setCurrentPage(v);
        }
    }

    public long getStartRowOfCurrentPage() {
        return pagination != null ? pagination.getStartRowOfCurrentPage() : 0;
    }

    public void setStartRowOfCurrentPage(long v) {
        if (pagination != null) {
            pagination.setStartRowOfCurrentPage(v);
        }
    }

    public long getEndRowOfCurrentPage() {
        return pagination != null ? pagination.getEndRowOfCurrentPage() : 0;
    }

    public void setEndRowOfCurrentPage(long v) {
        if (pagination != null) {
            pagination.setEndRowOfCurrentPage(v);
        }
    }

    public long getStartObjectOfCurrentPage() {
        return pagination != null ? pagination.getStartObjectOfCurrentPage() : 0;
    }

    public void setStartObjectOfCurrentPage(long v) {
        if (pagination != null) {
            pagination.setStartObjectOfCurrentPage(v);
        }
    }

    public long getEndObjectOfCurrentPage() {
        return pagination != null ? pagination.getEndObjectOfCurrentPage() : 0;
    }

    public void setEndObjectOfCurrentPage(long v) {
        if (pagination != null) {
            pagination.setEndObjectOfCurrentPage(v);
        }
    }

    /*
        get/set
     */
    public boolean isWithBom() {
        return withBom;
    }

    public void setWithBom(boolean withBom) {
        this.withBom = withBom;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Line_Break getLineBreak() {
        return lineBreak;
    }

    public void setLineBreak(Line_Break lineBreak) {
        this.lineBreak = lineBreak;
    }

    public Edit_Type getEditType() {
        return editType;
    }

    public void setEditType(Edit_Type editType) {
        this.editType = editType;
    }

    public String[] getFilterStrings() {
        return filterStrings;
    }

    public void setFilterStrings(String[] filterStrings) {
        this.filterStrings = filterStrings;
    }

    public StringFilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(StringFilterType filterType) {
        this.filterType = filterType;
    }

    public int getLineBreakWidth() {
        return lineBreakWidth;
    }

    public void setLineBreakWidth(int lineBreakWidth) {
        this.lineBreakWidth = lineBreakWidth;
    }

    public String getLineBreakValue() {
        return lineBreakValue;
    }

    public void setLineBreakValue(String lineBreakValue) {
        this.lineBreakValue = lineBreakValue;
    }

    public boolean isTotalNumberRead() {
        return totalNumberRead;
    }

    public void setTotalNumberRead(boolean totalNumberRead) {
        this.totalNumberRead = totalNumberRead;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public FindReplaceFile getFindReplace() {
        return findReplace;
    }

    public void setFindReplace(FindReplaceFile findReplace) {
        this.findReplace = findReplace;
    }

    public int getObjectUnit() {
        return objectUnit;
    }

    public void setObjectUnit(int objectUnit) {
        this.objectUnit = objectUnit;
    }

    public long getSizeWithSubdir() {
        return sizeWithSubdir;
    }

    public void setSizeWithSubdir(long sizeWithSubdir) {
        this.sizeWithSubdir = sizeWithSubdir;
    }

    public long getSizeWithoutSubdir() {
        return sizeWithoutSubdir;
    }

    public void setSizeWithoutSubdir(long sizeWithoutSubdir) {
        this.sizeWithoutSubdir = sizeWithoutSubdir;
    }

    public long getFilesWithSubdir() {
        return filesWithSubdir;
    }

    public void setFilesWithSubdir(long filesWithSubdir) {
        this.filesWithSubdir = filesWithSubdir;
    }

    public long getFilesWithoutSubdir() {
        return filesWithoutSubdir;
    }

    public void setFilesWithoutSubdir(long filesWithoutSubdir) {
        this.filesWithoutSubdir = filesWithoutSubdir;
    }

    public boolean isCharsetDetermined() {
        return charsetDetermined;
    }

    public void setCharsetDetermined(boolean charsetDetermined) {
        this.charsetDetermined = charsetDetermined;
    }

}
