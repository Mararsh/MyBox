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
            addAll(Arrays.asList(
                    "COLOR", "COLOR_PALETTE", "COLOR_PALETTE_NAME",
                    "IMAGE_EDIT_HISTORY", "IMAGE_SCOPE", "IMAGE_CLIPBOARD", "CONVOLUTION_KERNEL",
                    "GEOGRAPHY_CODE", "EPIDEMIC_REPORT", "LOCATION_DATA", "DATASET", "QUERY_CONDITION",
                    "FILE_BACKUP", "TEXT_CLIPBOARD", "WEB_HISTORY",
                    "MEDIA", "MEDIA_LIST", "ALARM_CLOCK",
                    "DATA2D_DEFINITION", "DATA2D_COLUMN", "DATA2D_CELL", "DATA2D_STYLE",
                    "TREE_NODE", "TAG", "TREE_NODE_TAG",
                    "MYBOX_LOG", "VISIT_HISTORY", "SYSTEM_CONF", "USER_CONF",
                    "STRING_VALUE", "STRING_VALUES", "FLOAT_MATRIX", "NAMED_VALUES", "BLOB_VALUE"
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
