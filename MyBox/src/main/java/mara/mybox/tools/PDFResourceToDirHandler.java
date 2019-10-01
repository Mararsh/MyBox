package mara.mybox.tools;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.fit.pdfdom.resource.HtmlResource;
import org.fit.pdfdom.resource.SaveResourceToDirHandler;

/**
 * @Author Mara
 * @CreateDate 2019-8-31
 * @License Apache License Version 2.0
 */
// https://github.com/radkovo/Pdf2Dom/issues/38
public class PDFResourceToDirHandler extends SaveResourceToDirHandler {

    private final File directory;
    private final List<String> writtenFileNames = new LinkedList<>();

    public PDFResourceToDirHandler(File directory) {
        this.directory = directory;
    }

    @Override
    public String handleResource(HtmlResource resource) throws IOException {
        String dir = DEFAULT_RESOURCE_DIR;
        if (directory != null) {
            dir = directory.getPath() + File.separator;
        }

        String fileName = findNextUnusedFileName(resource.getName());
        String resourcePath = dir + fileName + "." + resource.getFileEnding();

        File file = new File(resourcePath);
        FileUtils.writeByteArrayToFile(file, resource.getData());

        writtenFileNames.add(fileName);

        // write relative path in html
        return new File(dir).getName() + File.separator + fileName + "." + resource.getFileEnding();

//        return "file:///" + resourcePath;  // For FireFox and WebView
    }

    private String findNextUnusedFileName(String fileName) {
        int i = 1;
        String usedName = fileName;
        while (writtenFileNames.contains(usedName)) {
            usedName = fileName + i;
            i++;
        }

        return usedName;
    }

}
