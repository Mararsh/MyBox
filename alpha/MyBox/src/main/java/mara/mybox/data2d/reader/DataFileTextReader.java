package mara.mybox.data2d.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import mara.mybox.data2d.DataFileText;
import mara.mybox.tools.FileTools;

/**
 * @Author Mara
 * @CreateDate 2022-1-29
 * @License Apache License Version 2.0
 */
public class DataFileTextReader extends Data2DReader {

    protected DataFileText readerText;
    protected BufferedReader textReader;

    public DataFileTextReader() {
    }

    public DataFileTextReader(DataFileText data) {
        readerText = data;
        sourceData = data;
    }

    @Override
    public void scanFile() {
        File validFile = FileTools.removeBOM(task(), sourceFile);
        if (validFile == null || isStopped()) {
            return;
        }
        readerText.checkForLoad();
        try (BufferedReader reader = new BufferedReader(new FileReader(validFile, readerText.getCharset()))) {
            textReader = reader;
            operate.handleData();
            textReader = null;
            reader.close();
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
    }

    protected List<String> readValidLine() {
        return readerText.readValidLine(textReader);
    }

    protected List<String> parseFileLine(String line) {
        return readerText.parseFileLine(line);
    }

    @Override
    public void readColumnNames() {
        try {
            String line, comments;
            while ((line = textReader.readLine()) != null && !isStopped()) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                if (line.startsWith(DataFileText.CommentsMarker)) {
                    comments = line.substring(DataFileText.CommentsMarker.length(), line.length());
                    if (dataComments == null) {
                        dataComments = comments;
                    } else {
                        dataComments += "\n" + comments;
                    }
                    continue;
                }
                sourceRow = parseFileLine(line);
                if (sourceRow == null || sourceRow.isEmpty()) {
                    continue;
                }
                makeHeader();
                return;
            }
        } catch (Exception e) {
            showError(e.toString());
        }
    }

    @Override
    public void readTotal() {
        try {
            String line;
            sourceIndex = 0;
            skipHeader();
            while ((line = textReader.readLine()) != null) {
                if (isStopped()) {
                    sourceIndex = 0;
                    return;
                }
                List<String> row = parseFileLine(line);
                if (row != null && !row.isEmpty()) {
                    ++sourceIndex;
                }
            }
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
    }

    public void skipHeader() {
        if (readerHasHeader) {
            readValidLine();
        }
    }

    // sourceIndex is 1-base while pageStartIndex and pageEndIndex are 0-based
    @Override
    public void readPage() {
        if (textReader == null) {
            return;
        }
        try {
            skipHeader();
            sourceIndex = 0;
            String line;
            while ((line = textReader.readLine()) != null && !isStopped()) {
                sourceRow = parseFileLine(line);
                if (sourceRow == null || sourceRow.isEmpty()) {
                    continue;
                }
                if (sourceIndex++ < pageStartIndex) {
                    continue;
                }
                if (sourceIndex > pageEndIndex) {
                    stop();
                    break;
                }
                makePageRow();
            }
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
    }

    @Override
    public void readRows() {
        try {
            if (textReader == null) {
                return;
            }
            skipHeader();
            sourceIndex = 0;
            long fileIndex = -1;
            long startIndex = sourceData.pagination.startRowOfCurrentPage;
            long endIndex = sourceData.pagination.endRowOfCurrentPage;
            String line;
            while ((line = textReader.readLine()) != null && !isStopped()) {
                sourceRow = parseFileLine(line);
                if (sourceRow == null || sourceRow.isEmpty()) {
                    continue;
                }
                fileIndex++;

                if (fileIndex < startIndex || fileIndex >= endIndex) {
                    ++sourceIndex;
                    handleRow();

                } else if (fileIndex == startIndex) {
                    scanPage();
                }
            }
        } catch (Exception e) {
            showError(e.toString());
            setFailed();
        }
    }

}
