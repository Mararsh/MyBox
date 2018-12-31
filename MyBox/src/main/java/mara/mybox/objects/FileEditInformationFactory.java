package mara.mybox.objects;

import java.io.File;

/**
 * @Author Mara
 * @CreateDate 2018-12-29 11:13:53
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FileEditInformationFactory {

    public enum Edit_Type {
        Text, Bytes
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

}
