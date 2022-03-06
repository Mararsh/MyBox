package mara.mybox.data2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author Mara
 * @CreateDate 2022-2-12
 * @License Apache License Version 2.0
 */
public class DataInternalTable extends DataTable {

    public static List<String> InternalTables = new ArrayList<String>() {
        {
            addAll(Arrays.asList("ALARM_CLOCK", "BLOB_VALUE", "COLOR", "COLOR_PALETTE", "COLOR_PALETTE_NAME",
                    "CONVOLUTION_KERNEL", "DATA2D_CELL", "DATA2D_COLUMN", "DATA2D_DEFINITION", "DATASET",
                    "EPIDEMIC_REPORT", "FILE_BACKUP", "FLOAT_MATRIX", "GEOGRAPHY_CODE", "IMAGE_CLIPBOARD",
                    "IMAGE_EDIT_HISTORY", "IMAGE_SCOPE", "LOCATION_DATA", "MEDIA", "MEDIA_LIST", "MYBOX_LOG",
                    "NOTE", "NOTEBOOK", "NOTE_TAG", "QUERY_CONDITION", "STRING_VALUE", "STRING_VALUES", "SYSTEM_CONF",
                    "TAG", "TEXT_CLIPBOARD", "TREE", "USER_CONF", "VISIT_HISTORY", "WEB_FAVORITE", "WEB_HISTORY"
            ));
        }
    };

    public DataInternalTable() {
        type = Type.InternalTable;
    }

    @Override
    public int type() {
        return type(Type.InternalTable);
    }

}
