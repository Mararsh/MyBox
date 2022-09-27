package mara.mybox.tools;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2022-5
 * @License Apache License Version 2.0
 */
public class CsvTools {

    public static final char CommentsMarker = '#';

    public static Builder builder(String delimiter) {
        return CSVFormat.Builder.create()
                .setDelimiter(TextTools.delimiterValue(delimiter))
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .setNullString("")
                .setCommentMarker(CommentsMarker)
                .setAllowDuplicateHeaderNames(false)
                .setAutoFlush(true);
    }

    public static CSVFormat csvFormat(String delimiter, boolean hasHeader) {
        Builder builder = builder(delimiter);
        if (hasHeader) {
            builder.setHeader().setSkipHeaderRecord(true);
        } else {
            builder.setSkipHeaderRecord(false);
        }
        return builder.build();
    }

    public static CSVFormat csvFormat(String delimiter) {
        return csvFormat(delimiter, true);
    }

    public static CSVFormat csvFormat() {
        return csvFormat(",", true);
    }

    public static CSVPrinter csvPrinter(File csvFile) {
        try {
            return new CSVPrinter(new FileWriter(csvFile, Charset.forName("UTF-8")), csvFormat());
        } catch (Exception e) {
            return null;
        }
    }

}
