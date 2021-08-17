package thridparty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.PdfTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Languages;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * @Author Eduardo Lomonaco
 * https://github.com/eduardohl/Paginated-PDFBox-Table-Sample
 *
 * Changed by Mara Update Date: 2020-12-10
 */
public class PaginatedPdfTable {

    protected File file;
    protected PDDocument doc;

    protected List<String> columns;
    protected List<Integer> columnWidths;
    protected List<List<String>> rows;

    protected PDRectangle pageSize;
    protected String ttf, author, producer, header;
    protected PDFont textFont;
    protected float fontSize, margin, cellMargin, contentWidth, contentHeight, rowHeight;
    protected int rowsPerPage, numberOfPages, defaultZoom, currentPageNumber;
    protected boolean showPageNumber;

    public PaginatedPdfTable() {
        pageSize = PDRectangle.A4;
        margin = 20;
        textFont = PDType1Font.HELVETICA;
        rowHeight = 24;
        cellMargin = 2;
        fontSize = 14;
    }

    public static PaginatedPdfTable create() {
        return new PaginatedPdfTable();
    }

    public boolean createDoc(File file) {
        try {
            doc = null;
            if (file == null) {
                return false;
            }
            this.file = file;
//            MyBoxLog.console("pageSize:" + pageSize.getWidth() + " " + pageSize.getHeight());
            contentWidth = pageSize.getWidth() - 2 * margin;
            contentHeight = pageSize.getHeight() - 2 * margin;
            if (columnWidths == null) {
                columnWidths = new ArrayList<>();
            }
            float totalWidth = 0;
            for (int i = 0; i < columnWidths.size(); i++) {
                totalWidth += columnWidths.get(i);
            }
            if (totalWidth > contentWidth) {
                columnWidths = new ArrayList<>();
            }
            if (columns.size() > columnWidths.size()) {
                int avg = (int) ((contentWidth - totalWidth) / (columns.size() - columnWidths.size()));
                for (int i = columnWidths.size(); i < columns.size(); i++) {
                    columnWidths.add(avg);
                }
            }
            doc = new PDDocument();
            rowHeight = fontSize + 2 * cellMargin;
//            MyBoxLog.console("fontSize:" + fontSize + " cellMargin:" + cellMargin + " rowHeight:" + rowHeight
//                    + " getFontBoundingBox:" + textFont.getFontDescriptor().getFontBoundingBox().getHeight());
            rowsPerPage = Double.valueOf(Math.floor(contentHeight / rowHeight)).intValue() - 1;
            if (ttf != null) {
                textFont = PdfTools.getFont(doc, ttf);
            }
            currentPageNumber = 0;
//            MyBoxLog.console(textFont.getName() + " " + ttf);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void closeDoc() {
        if (doc == null) {
            return;
        }
        try {
            PdfTools.setValues(doc, author, "MyBox v" + AppValues.AppVersion, defaultZoom, 1.0f);
            doc.save(file);
            doc.close();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        doc = null;
    }

    public void writePages() {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        if (doc == null) {
            if (file != null) {
                doc = new PDDocument();
            } else {
                return;
            }
        }
        numberOfPages = Double.valueOf(Math.ceil(rows.size() * 1f / rowsPerPage)).intValue();
//        MyBoxLog.console(rowsPerPage + " " + numberOfPages);
        for (int pageCount = 0; pageCount < numberOfPages; pageCount++) {
            int startRange = pageCount * rowsPerPage;
            int endRange = (pageCount * rowsPerPage) + rowsPerPage;
            if (endRange > rows.size()) {
                endRange = rows.size();
            }
            List<List<String>> pageRows = new ArrayList<>();
            pageRows.addAll(getRows().subList(startRange, endRange));
            writePage(pageRows);
        }
    }

    public boolean writePage(List<List<String>> data) {
        if (doc == null || data == null || data.isEmpty()) {
            return false;
        }
        try {
            PDPage page = new PDPage();
            page.setMediaBox(pageSize);
            doc.addPage(page);
            try ( PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.setFont(textFont, fontSize);
                float tableTopY = pageSize.getHeight() - margin;

                // Draws grid and borders
                float nextY = tableTopY;
                for (int i = 0; i <= data.size() + 1; i++) {
                    writeLine(contentStream, margin, nextY, margin + getWidth(), nextY);
                    nextY -= rowHeight;
                }

                // Draw column lines
                final float tableYLength = rowHeight + (rowHeight * data.size());
                final float tableBottomY = tableTopY - tableYLength;
                float nextX = margin;
                writeLine(contentStream, nextX, tableTopY, nextX, tableBottomY);
                for (int i = 0; i < getNumberOfColumns(); i++) {
                    nextX += columnWidths.get(i);
                    writeLine(contentStream, nextX, tableTopY, nextX, tableBottomY);
                }

                // Position cursor to start drawing content
                float nextTextX = margin + cellMargin;
                // Calculate center alignment for text in cell considering font height
                float nextTextY = tableTopY - (rowHeight / 2)
                        - ((textFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize) / 4);

                // Write column headers
                writeRow(contentStream, columns, nextTextX, nextTextY);
                nextTextY -= rowHeight;
                nextTextX = margin + cellMargin;

                // Write content
                for (int i = 0; i < data.size(); i++) {
                    writeRow(contentStream, data.get(i), nextTextX, nextTextY);
                    nextTextY -= rowHeight;
                    nextTextX = margin + cellMargin;
                }

                if (header != null && !header.trim().isEmpty()) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, page.getTrimBox().getHeight() - margin + 2);
                    contentStream.showText(header.trim());
                    contentStream.endText();
                }

                if (showPageNumber) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(page.getTrimBox().getWidth() - margin * 2 + margin - 80, 5);
                    contentStream.showText(Languages.message("Page") + " " + (++currentPageNumber));
                    contentStream.endText();
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void writeLine(PDPageContentStream contentStream, float startx, float starty, float endx, float endy) {
        try {
            contentStream.moveTo(startx, starty);
            contentStream.lineTo(endx, endy);
            contentStream.stroke();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void writeRow(PDPageContentStream contentStream, List<String> row, float x, float y) {
        try {
            float nextX = x;
            int actualCols = Math.min(getNumberOfColumns(), row.size());
            for (int i = 0; i < actualCols; i++) {
                String text = row.get(i);
                contentStream.beginText();
                contentStream.newLineAtOffset(nextX, y);
                try {
                    contentStream.showText(text != null ? text : "");
                } catch (Exception e) {
//                    MyBoxLog.console(e);
                }
                contentStream.endText();
                nextX += columnWidths.get(i);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        get/set
     */
    public Integer getNumberOfColumns() {
        return this.getColumns().size();
    }

    public float getWidth() {
        float tableWidth = 0f;
        for (int width : columnWidths) {
            tableWidth += width;
        }
        return tableWidth;
    }

    public float getMargin() {
        return margin;
    }

    public PaginatedPdfTable setMargin(float margin) {
        this.margin = margin;
        return this;
    }

    public PDRectangle getPageSize() {
        return pageSize;
    }

    public PaginatedPdfTable setPageSize(PDRectangle pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public PDFont getTextFont() {
        return textFont;
    }

    public PaginatedPdfTable setTextFont(PDFont textFont) {
        this.textFont = textFont;
        return this;
    }

    public float getFontSize() {
        return fontSize;
    }

    public PaginatedPdfTable setFontSize(float fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public List<String> getColumns() {
        return columns;
    }

    public PaginatedPdfTable setColumns(List<String> columns) {
        this.columns = columns;
        return this;
    }

    public List<Integer> getColumnWidths() {
        return columnWidths;
    }

    public PaginatedPdfTable setColumnWidths(List<Integer> columnWidths) {
        this.columnWidths = columnWidths;
        return this;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public PaginatedPdfTable setRows(List<List<String>> rows) {
        this.rows = rows;
        return this;
    }

    public float getContentHeight() {
        return contentHeight;
    }

    public PaginatedPdfTable setHeight(float height) {
        this.contentHeight = height;
        return this;
    }

    public float getRowHeight() {
        return rowHeight;
    }

    public PaginatedPdfTable setRowHeight(float rowHeight) {
        this.rowHeight = rowHeight;
        return this;
    }

    public float getCellMargin() {
        return cellMargin;
    }

    public PaginatedPdfTable setCellMargin(float cellMargin) {
        this.cellMargin = cellMargin;
        return this;
    }

    public PDDocument getDoc() {
        return doc;
    }

    public PaginatedPdfTable setDoc(PDDocument doc) {
        this.doc = doc;
        return this;
    }

    public float getContentWidth() {
        return contentWidth;
    }

    public PaginatedPdfTable setContentWidth(float contentWidth) {
        this.contentWidth = contentWidth;
        return this;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public String getTtf() {
        return ttf;
    }

    public PaginatedPdfTable setTtf(String ttf) {
        this.ttf = ttf;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public PaginatedPdfTable setAuthor(String author) {
        this.author = author;
        return this;
    }

    public String getProducer() {
        return producer;
    }

    public PaginatedPdfTable setProducer(String producer) {
        this.producer = producer;
        return this;
    }

    public int getDefaultZoom() {
        return defaultZoom;
    }

    public PaginatedPdfTable setDefaultZoom(int defaultZoom) {
        this.defaultZoom = defaultZoom;
        return this;
    }

    public String getHeader() {
        return header;
    }

    public PaginatedPdfTable setHeader(String header) {
        this.header = header;
        return this;
    }

    public boolean isShowPageNumber() {
        return showPageNumber;
    }

    public PaginatedPdfTable setShowPageNumber(boolean showPageNumber) {
        this.showPageNumber = showPageNumber;
        return this;
    }

}
