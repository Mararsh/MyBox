package mara.mybox.db;

import java.util.ArrayList;

/**
 * @Author Mara
 * @CreateDate 2020-5-27
 * @License Apache License Version 2.0
 */
public class TableAddressExtension extends DerbyBase {

    public TableAddressExtension() {
        Table_Name = "Address_Extension";
        Keys = new ArrayList<>() {
            {
                add("address");
                add("ext_type");
                add("ext_value");
            }
        };
        Create_Table_Statement
                = " CREATE TABLE Address_Extension ( "
                + "  address BIGINT NOT NULL, "
                + "  ext_type SMALLINT NOT NULL, "
                + "  ext_value VARCHAR(1028) NOT NULL, "
                + "  PRIMARY KEY (address, type, value)"
                + "  FOREIGN KEY (address) REFERENCES Address (adid) ON DELETE CASCADE ON UPDATE RESTRICT"
                + " )";
    }

}
