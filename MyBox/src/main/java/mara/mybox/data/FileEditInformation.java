package mara.mybox.data;

import mara.mybox.value.AppVaribles;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.tools.TextTools.bomBytes;
import static mara.mybox.tools.TextTools.bomSize;
import static mara.mybox.tools.TextTools.checkCharsetByBom;
import thridparty.EncodingDetect;

/**
 * @Author Mara
 * @CreateDate 2018-12-10 13:06:33
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public abstract class FileEditInformation extends FileInformation {

    protected Edit_Type editType;
    protected boolean withBom, totalNumberRead;
    protected Charset charset;
    protected long objectsNumber, currentPageLineStart, currentPageLineEnd;
    protected long linesNumber, pageSize, pagesNumber, currentPage;
    protected long currentPageObjectStart, currentPageObjectEnd;
    protected String findString, replaceString;
    protected String[] filterStrings;
    protected long currentFound;
    protected Line_Break lineBreak;
    protected Filter_Type filterType;
    protected int lineBreakWidth, currentPosition, currentLine;
    protected String lineBreakValue;

    public enum Edit_Type {
        Text, Bytes
    }

    public enum Line_Break {
        LF, // Liunx/Unix
        CR, // IOS
        CRLF, // Windows
        Width,
        Value,
        Auto
    }

    public enum Filter_Type {
        IncludeAll, IncludeOne, NotIncludeAll, NotIncludeAny
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
        filterType = Filter_Type.IncludeOne;
        withBom = totalNumberRead = false;
        charset = Charset.defaultCharset();
        objectsNumber = linesNumber = -1;
        currentPage = pagesNumber = 1;
        pageSize = 100000;
        currentPageObjectStart = currentPageObjectEnd = -1;
        currentFound = currentPosition = currentLine = -1;
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

    public static FileEditInformation newEditInformationFull(FileEditInformation sourceInfo) {
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
        newInformation.setFindString(sourceInfo.getFindString());
        newInformation.setReplaceString(sourceInfo.getReplaceString());
        newInformation.setCurrentFound(sourceInfo.getCurrentFound());
        newInformation.setPageSize(sourceInfo.getPageSize());
        newInformation.setCurrentPage(sourceInfo.getCurrentPage());
        newInformation.setCurrentPageObjectStart(sourceInfo.getCurrentPageObjectStart());
        newInformation.setCurrentPageObjectEnd(sourceInfo.getCurrentPageObjectEnd());
        newInformation.setCurrentPageLineStart(sourceInfo.getCurrentPageLineEnd());
        return newInformation;
    }

    public static FileEditInformation newEditInformationMajor(FileEditInformation sourceInfo) {
        FileEditInformation newInformation
                = FileEditInformation.newEditInformation(sourceInfo.getEditType(), sourceInfo.getFile());
        newInformation.setCharset(sourceInfo.getCharset());
        newInformation.setWithBom(sourceInfo.isWithBom());
        newInformation.setLineBreak(sourceInfo.getLineBreak());
        newInformation.setLineBreakValue(sourceInfo.getLineBreakValue());
        newInformation.setLineBreakWidth(sourceInfo.getLineBreakWidth());
        newInformation.setFilterStrings(sourceInfo.getFilterStrings());
        newInformation.setFilterType(sourceInfo.getFilterType());
        newInformation.setFindString(sourceInfo.getFindString());
        newInformation.setReplaceString(sourceInfo.getReplaceString());
        newInformation.setPageSize(sourceInfo.getPageSize());
        return newInformation;
    }

    public static FileEditInformation newEditInformation(FileEditInformation sourceInfo, boolean full) {
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
            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] header = new byte[4];
                if ((inputStream.read(header, 0, 4) != -1)) {
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
            logger.debug(e.toString());
            return false;
        }
    }

    public boolean convertCharset(FileEditInformation targetInfo) {
        try {
            if (file == null || charset == null
                    || targetInfo == null || targetInfo.getFile() == null || targetInfo.getCharset() == null) {
                return false;
            }
            try (FileInputStream inputStream = new FileInputStream(file);
                    InputStreamReader reader = new InputStreamReader(inputStream, charset);
                    FileOutputStream outputStream = new FileOutputStream(targetInfo.getFile());
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream, targetInfo.getCharset())) {
                if (withBom) {
                    inputStream.skip(bomSize(charset.name()));
                }
                if (targetInfo.isWithBom()) {
                    byte[] bytes = bomBytes(targetInfo.getCharset().name());
                    outputStream.write(bytes);
                }
                char[] buf = new char[IO_BUF_LENGTH];
                int count;
                while ((count = reader.read(buf)) != -1) {
                    String text = new String(buf, 0, count);
                    writer.write(text);
                }
            }
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }
    }

    public abstract boolean readTotalNumbers();

    public abstract String readPage();

    public abstract String readPage(long pageNumber);

    public abstract boolean writeObject(String text);

    public abstract boolean writePage(FileEditInformation sourceInfo, String text);

    public abstract boolean writePage(FileEditInformation sourceInfo, long pageNumber, String text);

    public abstract String findFirst();

    public abstract String findNext();

    public abstract String findPrevious();

    public abstract String findLast();

    public abstract String locateLine();

    public abstract int replaceAll();

    public abstract int count();

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

    public String filterTypeName() {
        switch (filterType) {
            case IncludeOne:
                return AppVaribles.getMessage("IncludeOne");
            case IncludeAll:
                return AppVaribles.getMessage("IncludeAll");
            case NotIncludeAny:
                return AppVaribles.getMessage("NotIncludeAny");
            case NotIncludeAll:
                return AppVaribles.getMessage("NotIncludeAll");
            default:
                return "";
        }

    }

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

    public String getFindString() {
        return findString;
    }

    public void setFindString(String findString) {
        this.findString = findString;
    }

    public String getReplaceString() {
        return replaceString;
    }

    public void setReplaceString(String replaceString) {
        this.replaceString = replaceString;
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

    public Filter_Type getFilterType() {
        return filterType;
    }

    public void setFilterType(Filter_Type filterType) {
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

    public long getLinesNumber() {
        return linesNumber;
    }

    public void setLinesNumber(long linesNumber) {
        this.linesNumber = linesNumber;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
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

    public long getCurrentFound() {
        return currentFound;
    }

    public void setCurrentFound(long currentFound) {
        this.currentFound = currentFound;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
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

    public int getCurrentLine() {
        return currentLine;
    }

    public void setCurrentLine(int currentLine) {
        this.currentLine = currentLine;
    }

}
