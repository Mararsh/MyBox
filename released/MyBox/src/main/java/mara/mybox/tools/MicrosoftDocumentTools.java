package mara.mybox.tools;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import mara.mybox.dev.MyBoxLog;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.extractor.MainExtractorFactory;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.extractor.ole2.OLE2ScratchpadExtractorFactory;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFPictureShape;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.ooxml.extractor.POIXMLExtractorFactory;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xslf.usermodel.XSLFSlideShowFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.w3c.dom.Document;

/**
 * @Author Mara
 * @CreateDate 2020-2-22
 * @License Apache License Version 2.0
 */
public class MicrosoftDocumentTools {

    public static String cellString(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return cell.getNumericCellValue() + "";
            case BOOLEAN:
                return cell.getBooleanCellValue() + "";
        }
        return null;
    }

    public static void copyRow(Row sourceRow, Row targetRow) {
        if (sourceRow == null || targetRow == null) {
            return;
        }
        for (int col = sourceRow.getFirstCellNum(); col < sourceRow.getLastCellNum(); col++) {
            Cell sourceCell = sourceRow.getCell(col);
            if (sourceCell == null) {
                continue;
            }
            CellType type = sourceCell.getCellType();
            if (type == null) {
                type = CellType.STRING;
            }
            Cell targetCell = targetRow.createCell(col, type);
            copyCell(sourceCell, targetCell, type);
        }
    }

    public static void copyCell(Cell sourceCell, Cell targetCell, CellType type) {
        if (sourceCell == null || targetCell == null || type == null) {
            return;
        }
        try {
            switch (type) {
                case STRING:
                    targetCell.setCellValue(sourceCell.getStringCellValue());
                    break;
                case NUMERIC:
                    targetCell.setCellValue(sourceCell.getNumericCellValue());
                    break;
                case BLANK:
                    targetCell.setCellValue("");
                    break;
                case BOOLEAN:
                    targetCell.setCellValue(sourceCell.getBooleanCellValue());
                    break;
                case ERROR:
                    targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
                    break;
                case FORMULA:
                    targetCell.setCellFormula(sourceCell.getCellFormula());
                    break;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static void setCell(Cell targetCell, CellType type, String value) {
        if (value == null || targetCell == null || type == null) {
            return;
        }
        try {
            if (type == CellType.NUMERIC) {
                try {
                    long v = Long.parseLong(value);
                    targetCell.setCellValue(v);
                    return;
                } catch (Exception e) {
                }
                try {
                    double v = Double.parseDouble(value);
                    targetCell.setCellValue(v);
                    return;
                } catch (Exception e) {
                }
            }
            targetCell.setCellValue(value);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static boolean createXLSX(File file, List<String> columns, List<List<String>> rows) {
        try {
            if (file == null || columns == null || rows == null || columns.isEmpty()) {
                return false;

            }
            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFSheet sheet = wb.createSheet("sheet1");
//            sheet.setDefaultColumnWidth(20);

            XSSFRow titleRow = sheet.createRow(0);
            XSSFCellStyle horizontalCenter = wb.createCellStyle();
            horizontalCenter.setAlignment(HorizontalAlignment.CENTER);
            for (int i = 0; i < columns.size(); i++) {
                XSSFCell cell = titleRow.createCell(i);
                cell.setCellValue(columns.get(i));
                cell.setCellStyle(horizontalCenter);
            }
            for (int i = 0; i < rows.size(); i++) {
                XSSFRow row = sheet.createRow(i + 1);
                List<String> values = rows.get(i);
                for (int j = 0; j < values.size(); j++) {
                    XSSFCell cell = row.createCell(j);
                    cell.setCellValue(values.get(j));
                }
            }
            for (int i = 0; i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            try ( OutputStream fileOut = new FileOutputStream(file)) {
                wb.write(fileOut);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean createXLS(File file, List<String> columns, List<List<String>> rows) {
        try {
            if (file == null || columns == null || rows == null || columns.isEmpty()) {
                return false;

            }
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("sheet1");
//            sheet.setDefaultColumnWidth(40);

            HSSFRow titleRow = sheet.createRow(0);
            HSSFCellStyle horizontalCenter = wb.createCellStyle();
            horizontalCenter.setAlignment(HorizontalAlignment.CENTER);
            for (int i = 0; i < columns.size(); i++) {
                HSSFCell cell = titleRow.createCell(i);
                cell.setCellValue(columns.get(i));
                cell.setCellStyle(horizontalCenter);
            }
            for (int i = 1; i <= rows.size(); i++) {
                HSSFRow row = sheet.createRow(0);
                List<String> values = rows.get(i);
                for (int j = 0; j < values.size(); j++) {
                    HSSFCell cell = row.createCell(j);
                    cell.setCellValue(values.get(j));
                }
            }
            try ( OutputStream fileOut = new FileOutputStream(file)) {
                wb.write(fileOut);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }

    }

    public static Document word97ToDoc(File srcFile) {
        Document doc = null;
        try ( HWPFDocument wordDocument = new HWPFDocument(new FileInputStream(srcFile))) {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            WordToHtmlConverter converter = new WordToHtmlConverter(doc);
            converter.processDocument(wordDocument);
            doc = converter.getDocument();
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
        return doc;
    }

    public static String word97Tohtml(File srcFile, Charset charset) {
        try {
            Document doc = word97ToDoc(srcFile);
            if (doc == null) {
                return null;
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.setOutputProperty(OutputKeys.ENCODING, charset == null ? "UTF-8" : charset.name());
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(doc), new StreamResult(baos));
            baos.flush();
            baos.close();
            return baos.toString(charset);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File word2HtmlFile(File srcFile, Charset charset) {
        try {
            String suffix = FileNameTools.getFileSuffix(srcFile.getName());
            String html;
            if ("doc".equalsIgnoreCase(suffix)) {
                html = word97Tohtml(srcFile, charset);
            } else if ("docx".equalsIgnoreCase(suffix)) {
                String text = extractText(srcFile);
                if (text == null) {
                    return null;
                }
                html = HtmlWriteTools.textToHtml(text);
            } else {
                return null;
            }
            return HtmlWriteTools.writeHtml(html);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String word2Html(File srcFile, Charset charset) {
        try {
            File htmlFile = word2HtmlFile(srcFile, charset);
            return TextFileTools.readTexts(htmlFile, charset);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static HSLFPictureShape imageShape(HSLFSlideShow ppt, BufferedImage image, String format) {
        try {
            byte[] bytes = ByteTools.imageToBytes(image, format);
            PictureType type;
            if ("png".equalsIgnoreCase(format)) {
                type = PictureType.PNG;
            } else if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
                type = PictureType.JPEG;
            } else if ("bmp".equalsIgnoreCase(format)) {
                type = PictureType.BMP;
            } else if ("gif".equalsIgnoreCase(format)) {
                type = PictureType.GIF;
            } else if ("tif".equalsIgnoreCase(format) || "tiff".equalsIgnoreCase(format)) {
                type = PictureType.TIFF;
            } else {
                type = PictureType.PNG;
            }
            HSLFPictureData pd = ppt.addPicture(bytes, type);
            return new HSLFPictureShape(pd);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }

    }

    public static String extractText(File srcFile) {
        String text = null;
        try ( POITextExtractor extractor = ExtractorFactory.createExtractor(srcFile)) {
            text = extractor.getText();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return text;
    }

    // https://github.com/Mararsh/MyBox/issues/1100
    public static void registryFactories() {
        SlideShowFactory.addProvider(new HSLFSlideShowFactory());
        SlideShowFactory.addProvider(new XSLFSlideShowFactory());

        ExtractorFactory.addProvider(new MainExtractorFactory());
        ExtractorFactory.addProvider(new OLE2ScratchpadExtractorFactory());
        ExtractorFactory.addProvider(new POIXMLExtractorFactory());

        WorkbookFactory.addProvider(new HSSFWorkbookFactory());
        WorkbookFactory.addProvider(new XSSFWorkbookFactory());
    }

}
