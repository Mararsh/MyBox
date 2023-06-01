package mara.mybox.controller;

import java.sql.Connection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.XmlTreeNode;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-4-30
 * @License Apache License Version 2.0
 */
public class ControlXmlOptions extends BaseController {

    @FXML
    protected CheckBox dtdValidationCheck, ignoreCommentsCheck,
            ignoreBlankStringCheck, supportNamespacesCheck, indentCheck;

    @Override
    public void initControls() {
        super.initControls();
        try (Connection conn = DerbyBase.getConnection()) {
            dtdValidationCheck.setSelected(UserConfig.getBoolean(conn, "XmlDTDValidation", false));
            dtdValidationCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("XmlDTDValidation", dtdValidationCheck.isSelected());
                }
            });

            ignoreCommentsCheck.setSelected(UserConfig.getBoolean(conn, "XmlIgnoreComments", false));
            ignoreCommentsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("XmlIgnoreComments", ignoreCommentsCheck.isSelected());
                }
            });

            XmlTreeNode.ignoreWhite = UserConfig.getBoolean(conn, "XmlIgnoreBlankString", true);
            ignoreBlankStringCheck.setSelected(XmlTreeNode.ignoreWhite);
            ignoreBlankStringCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("XmlIgnoreBlankString", ignoreBlankStringCheck.isSelected());
                    XmlTreeNode.ignoreWhite = ignoreBlankStringCheck.isSelected();
                }
            });

            supportNamespacesCheck.setSelected(UserConfig.getBoolean(conn, "XmlSupportNamespaces", false));
            supportNamespacesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("XmlSupportNamespaces", supportNamespacesCheck.isSelected());
                }
            });

            indentCheck.setSelected(UserConfig.getBoolean(conn, "XmlTransformerIndent", false));
            indentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("XmlTransformerIndent", indentCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
