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
            addAll(Arrays.asList("ALARM_CLOCK",
                    "COLOR", "COLOR_PALETTE", "COLOR_PALETTE_NAME", "CONVOLUTION_KERNEL",
                    "DATA2D_CELL", "DATA2D_COLUMN", "DATA2D_DEFINITION", "DATA2D_STYLE",
                    "FILE_BACKUP", "FLOAT_MATRIX", "GEOGRAPHY_CODE",
                    "IMAGE_CLIPBOARD", "IMAGE_EDIT_HISTORY",
                    "MEDIA", "MEDIA_LIST", "MYBOX_LOG", "NAMED_VALUES",
                    "PATH_CONNECTION", "QUERY_CONDITION",
                    "STRING_VALUE", "STRING_VALUES", "SYSTEM_CONF",
                    "TAG", "TEXT_CLIPBOARD", "TREE_NODE", "TREE_NODE_TAG",
                    "USER_CONF", "VISIT_HISTORY", "WEB_HISTORY"
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
