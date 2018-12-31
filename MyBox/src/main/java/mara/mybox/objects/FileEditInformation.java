package mara.mybox.objects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.FileEditInformationFactory.Edit_Type;
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
    protected final int IO_BUF_LENGTH = 4096;
    protected boolean withBom, filterInclude;
    protected Charset charset;
    protected int objectsNumber, linesNumber;
    protected int pageSize, pagesNumber, currentPage, editerCharactersNumber, editerLinesNumber;
    protected int currentPageObjectStart, currentPageObjectEnd;
    protected int currentPageLineStart, currentPageLineEnd;
    protected String findString, replaceString;
    protected String[] filterStrings;
    protected int currentFound, currentPosition;
    protected Line_Break lineBreak;

    public enum Line_Break {
        LF, // Liunx/Unix
        CR, // IOS
        CRLF  // Windows
    }

    public FileEditInformation() {
        editType = Edit_Type.Text;
        initValues();
    }

    public FileEditInformation(File file) {
        super(file);
        editType = Edit_Type.Text;
        initValues();
    }

    protected final void initValues() {
        withBom = false;
        charset = Charset.defaultCharset();
        objectsNumber = linesNumber = -1;
        currentPage = pagesNumber = 1;
        pageSize = 100000;
        currentPageObjectStart = currentPageObjectEnd = -1;
        editerCharactersNumber = editerLinesNumber = -1;
        currentFound = currentPosition = -1;
        switch (System.lineSeparator()) {
            case "\r\n":
                lineBreak = Line_Break.CRLF;
                break;
            case "\r":
                lineBreak = Line_Break.CR;
                break;
            default:
                lineBreak = Line_Break.LF;
                break;
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

    public abstract String readPage(int pageNumber);

    public abstract boolean writeObject(String text);

    public abstract boolean writePage(FileEditInformation sourceInfo, String text);

    public abstract boolean writePage(FileEditInformation sourceInfo, int pageNumber, String text);

    public abstract String findFirst();

    public abstract String findNext();

    public abstract String findPrevious();

    public abstract String findLast();

    public abstract int replaceAll();

    public abstract File filter();

    public boolean isMatchFilters(String string) {
        if (string == null || string.isEmpty()) {
            return false;
        }
        if (filterInclude) {
            return isIncludeFilters(string);
        } else {
            return isNotIncludeFilters(string);
        }
    }

    public boolean isIncludeFilters(String string) {
        boolean found;
        for (String filter : filterStrings) {
            found = string.contains(filter);
            if (found) {
                return true;
            }
        }
        return false;
    }

    public boolean isNotIncludeFilters(String string) {
        boolean found;
        for (String filter : filterStrings) {
            found = string.contains(filter);
            if (found) {
                return false;
            }
        }
        return true;
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

    public int getObjectsNumber() {
        return objectsNumber;
    }

    public void setObjectsNumber(int objectsNumber) {
        this.objectsNumber = objectsNumber;
    }

    public int getCurrentPageObjectStart() {
        return currentPageObjectStart;
    }

    public void setCurrentPageObjectStart(int currentPageObjectStart) {
        this.currentPageObjectStart = currentPageObjectStart;
    }

    public int getCurrentPageObjectEnd() {
        return currentPageObjectEnd;
    }

    public void setCurrentPageObjectEnd(int currentPageObjectEnd) {
        this.currentPageObjectEnd = currentPageObjectEnd;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPagesNumber() {
        return pagesNumber;
    }

    public void setPagesNumber(int pagesNumber) {
        this.pagesNumber = pagesNumber;
    }

    public int getEditerCharactersNumber() {
        return editerCharactersNumber;
    }

    public void setEditerCharactersNumber(int editerCharactersNumber) {
        this.editerCharactersNumber = editerCharactersNumber;
    }

    public int getEditerLinesNumber() {
        return editerLinesNumber;
    }

    public void setEditerLinesNumber(int editerLinesNumber) {
        this.editerLinesNumber = editerLinesNumber;
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

    public int getLinesNumber() {
        return linesNumber;
    }

    public void setLinesNumber(int linesNumber) {
        this.linesNumber = linesNumber;
    }

    public int getCurrentPageLineStart() {
        return currentPageLineStart;
    }

    public void setCurrentPageLineStart(int currentPageLineStart) {
        this.currentPageLineStart = currentPageLineStart;
    }

    public int getCurrentPageLineEnd() {
        return currentPageLineEnd;
    }

    public void setCurrentPageLineEnd(int currentPageLineEnd) {
        this.currentPageLineEnd = currentPageLineEnd;
    }

    public Line_Break getLineBreak() {
        return lineBreak;
    }

    public void setLineBreak(Line_Break lineBreak) {
        this.lineBreak = lineBreak;
    }

    public int getCurrentFound() {
        return currentFound;
    }

    public void setCurrentFound(int currentFound) {
        this.currentFound = currentFound;
    }

    public Edit_Type getEditType() {
        return editType;
    }

    public void setEditType(Edit_Type editType) {
        this.editType = editType;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public String[] getFilterStrings() {
        return filterStrings;
    }

    public void setFilterStrings(String[] filterStrings) {
        this.filterStrings = filterStrings;
    }

    public boolean isFilterInclude() {
        return filterInclude;
    }

    public void setFilterInclude(boolean filterInclude) {
        this.filterInclude = filterInclude;
    }

}
