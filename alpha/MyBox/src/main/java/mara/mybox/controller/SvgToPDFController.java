package mara.mybox.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import mara.mybox.db.data.VisitHistory;
import static mara.mybox.value.Languages.message;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;

/**
 * @Author Mara
 * @CreateDate 2023-6-17
 * @License Apache License Version 2.0
 */
public class SvgToPDFController extends BaseBatchFileController {

    public SvgToPDFController() {
        baseTitle = message("SvgToPDF");
        targetFileSuffix = "pdf";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.SVG, VisitHistory.FileType.PDF);
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        File target = makeTargetFile(srcFile, targetPath);
        if (target == null) {
            return message("Skip");
        }
        try (FileInputStream inputStream = new FileInputStream(srcFile);
                BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(target))) {
            PDFTranscoder transcoder = new PDFTranscoder();
            TranscoderInput input = new TranscoderInput(inputStream);
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(input, output);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            updateLogs(e.toString());
            return message("Failed");
        }
        if (target.exists() && target.length() > 0) {
            targetFileGenerated(target);
            return message("Successful");
        } else {
            return message("Failed");
        }
    }

}
