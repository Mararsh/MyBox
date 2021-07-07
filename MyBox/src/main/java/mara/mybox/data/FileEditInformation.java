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
import mara.mybox.tools.ByteTools;
import mara.mybox.tools.StringTools;
import static mara.mybox.tools.TextTools.bomBytes;
import static mara.mybox.tools.TextTools.bomSize;
import static mara.mybox.tools.TextTools.checkCharsetByBom;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;
import thridparty.EncodingDetect;

/**
 * @Author Mara
 * @CreateDate 2018-12-10 13:06:33
 * @License Apache License Version 2.0
 */
public abstract class FileEditInformation extends FileInformation {

    protected Edit_Type editType;
    protected boolean withBom, totalNumberRead;
    protected Charset charset;
    protected Line_Break lineBreak;
    protected String lineBreakValue;
    protected int objectUnit, pageSize, lineBreakWidth;
    protected long objectsNumber,
            currentPageLineStart, currentPageLineEnd, // 1-based, include end
            linesNumber, pagesNumber, currentPage, currentLine,
            currentPageObjectStart, currentPageObjectEnd; // 0-based, exclude end
    protected String[] filterStrings;
    protected FindReplaceFile findReplace;
    protected StringFilterType filterType;

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
        initValues();
    }

    public FileEditInformation(File file) {
        super(file);
        initValues();
    }

    protected final void initValues() {
        filterType = StringFilterType.IncludeOne;
        withBom = totalNumberRead = false;
        charset = defaultCharset();
        objectsNumber = linesNumber = -1;
        currentPage = pagesNumber = 1;  // 1-based
        pageSize = 100000;
        currentPageObjectStart = currentPageObjectEnd = -1;
        currentLine = -1;
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

    public static FileEditInformation newEditInformation(Edit_Type type) {
        switch (type) {
            case Text:
                return new TextEditInformation();
            case Bytes:
                return new BytesEditInformation();
            default:
                return new TextEditInformation();
        }
    }

    public static FileEditInformation newEditInformation(Edit_Type type, File file) {
        switch (type) {
            case Text:
                return new TextEditInformation(file);
            case Bytes:
                return new BytesEditInformation(file);
            default:
                return new TextEditInformation(file);
        }
    }

    public static FileEditInformation newEditInformationFull(
            FileEditInformation sourceInfo) {
        FileEditInformation newInformation
                = FileEditInformation.newEditInformation(sourceInfo.getEditType(), sourceInfo.getFile());
        newInformation.setCharset(sourceInfo.getCharset());
        newInformation.setWithBom(sourceInfo.isWithBom());
        newInformation.setObjectsNumber(sourceInfo.getObjectsNumber());
        newInformation.setLinesNumber(sourceInfo.getLinesNumber());
        newInformation.setLineBreak(sourceInfo.getLineBreak());
        newInformation.setLineBreakValue(sourceInfo.getLineBreakValue());
        newInformation.setLineBreakWidth(sourceInfo.getLineBreakWidth());
        newInformation.setFilterStrings(sourceInfo.getFilterStrings());
        newInformation.setFilterType(sourceInfo.getFilterType());
        newInformation.setFindReplace(sourceInfo.getFindReplace());
        newInformation.setPageSize(sourceInfo.getPageSize());
        newInformation.setCurrentPage(sourceInfo.getCurrentPage());
        newInformation.setCurrentPageObjectStart(sourceInfo.getCurrentPageObjectStart());
        newInformation.setCurrentPageObjectEnd(sourceInfo.getCurrentPageObjectEnd());
        newInformation.setCurrentPageLineStart(sourceInfo.getCurrentPageLineEnd());
        return newInformation;
    }

    public static FileEditInformation newEditInformationMajor(
            FileEditInformation sourceInfo) {
        FileEditInformation newInformation
                = FileEditInformation.newEditInformation(sourceInfo.getEditType(), sourceInfo.getFile());
        newInformation.setCharset(sourceInfo.getCharset());
        newInformation.setWithBom(sourceInfo.isWithBom());
        newInformation.setLineBreak(sourceInfo.getLineBreak());
        newInformation.setLineBreakValue(sourceInfo.getLineBreakValue());
        newInformation.setLineBreakWidth(sourceInfo.getLineBreakWidth());
        newInformation.setFilterStrings(sourceInfo.getFilterStrings());
        newInformation.setFilterType(sourceInfo.getFilterType());
        newInformation.setPageSize(sourceInfo.getPageSize());
        return newInformation;
    }

    public static FileEditInformation newEditInformation(
            FileEditInformation sourceInfo, boolean full) {
        if (full) {
            return newEditInformationFull(sourceInfo);
        } else {
            return newEditInformationMajor(sourceInfo);
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
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public boolean convertCharset(FileEditInformation targetInfo) {
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
                char[] buf = new char[CommonValues.IOBufferLength];
                int bufLen;
                while ((bufLen = reader.read(buf)) > 0) {
                    writer.write(new String(buf, 0, bufLen));
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public abstract boolean readTotalNumbers();

    public abstract String readPage();

    public abstract String readPage(long pageNumber);

    public abstract boolean writeObject(String text);

    public abstract boolean writePage(FileEditInformation sourceInfo, String text);

    public abstract boolean writePage(FileEditInformation sourceInfo, long pageNumber, String text);

    public abstract String locateLine();

    public abstract File filter(boolean recordLineNumbers);

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

    public long getObjectsNumber() {
        return objectsNumber;
    }

    public void setObjectsNumber(long objectsNumber) {
        this.objectsNumber = objectsNumber;
    }

    public long getCurrentPageLineStart() {
        return currentPageLineStart;
    }

    public void setCurrentPageLineStart(long currentPageLineStart) {
        this.currentPageLineStart = currentPageLineStart;
    }

    public long getCurrentPageLineEnd() {
        return currentPageLineEnd;
    }

    public void setCurrentPageLineEnd(long currentPageLineEnd) {
        this.currentPageLineEnd = currentPageLineEnd;
    }

    public long getCurrentPageObjectStart() {
        return currentPageObjectStart;
    }

    public void setCurrentPageObjectStart(long currentPageObjectStart) {
        this.currentPageObjectStart = currentPageObjectStart;
    }

    public long getCurrentPageObjectEnd() {
        return currentPageObjectEnd;
    }

    public void setCurrentPageObjectEnd(long currentPageObjectEnd) {
        this.currentPageObjectEnd = currentPageObjectEnd;
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

    public long getCurrentLine() {
        return currentLine;
    }

    public void setCurrentLine(long currentLine) {
        this.currentLine = currentLine;
    }

    public long getLinesNumber() {
        return linesNumber;
    }

    public void setLinesNumber(long linesNumber) {
        this.linesNumber = linesNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getPagesNumber() {
        return pagesNumber;
    }

    public void setPagesNumber(long pagesNumber) {
        this.pagesNumber = pagesNumber;
    }

    public long getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(long currentPage) {
        this.currentPage = currentPage;
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

}
