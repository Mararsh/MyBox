/*
 * Apache License Version 2.0
 */
package mara.mybox.media;

import com.github.kokorin.jaffree.ffprobe.data.Data;
import com.github.kokorin.jaffree.ffprobe.data.FormatParser;
import com.github.kokorin.jaffree.ffprobe.data.LineIterator;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * @Author Mara
 * @CreateDate 2019-12-4
 * @License Apache License Version 2.0
 */
// This class is to be used anonymously
public class FFmpegOriginalOutput implements FormatParser {

    @Override
    public Data parse(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        Iterator<String> lines = new LineIterator(reader);
        while (lines.hasNext()) {
            lineArrived(lines.next());
        }
        end();
        return null;  // do not care about Data
    }

    public void lineArrived(String line) {

    }

    public void end() {

    }

    @Override
    public String getFormatName() {
        return "original";
    }
}
