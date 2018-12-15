package mara.mybox.objects;

import java.io.File;
import java.nio.charset.Charset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author Mara
 * @CreateDate 2018-12-10 13:06:33
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FileEncoding {

    private static final Logger logger = LogManager.getLogger();

    private File file;
    private boolean withBom;
    private Charset charset;
    private long charactersLength, bytesLength;

    public FileEncoding() {
        withBom = false;
        charset = Charset.defaultCharset();
    }

    public FileEncoding(File file) {
        this.file = file;
        withBom = false;
        charset = Charset.defaultCharset();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
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

    public long getCharactersLength() {
        return charactersLength;
    }

    public void setCharactersLength(long charactersLength) {
        this.charactersLength = charactersLength;
    }

    public long getBytesLength() {
        return bytesLength;
    }

    public void setBytesLength(long bytesLength) {
        this.bytesLength = bytesLength;
    }

}
