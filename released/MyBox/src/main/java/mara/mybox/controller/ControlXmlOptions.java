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
            ignoreBlankTextCheck, ignoreBlankCDATACheck, ignoreBlankCommentCheck,
            ignoreBlankInstructionCheck, supportNamespacesCheck, indentCheck;

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
                    XmlTreeNode.ignoreComment = ignoreCommentsCheck.isSelected();
                }
            });

            ignoreBlankTextCheck.setSelected(UserConfig.getBoolean(conn, "XmlIgnoreBlankText", true));
            ignoreBlankTextCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("XmlIgnoreBlankText", ignoreBlankTextCheck.isSelected());
                    XmlTreeNode.ignoreBlankText = ignoreBlankTextCheck.isSelected();
                }
            });

            ignoreBlankCDATACheck.setSelected(UserConfig.getBoolean(conn, "XmlIgnoreBlankCDATA", true));
            ignoreBlankCDATACheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("XmlIgnoreBlankCDATA", ignoreBlankCDATACheck.isSelected());
                    XmlTreeNode.ignoreBlankCDATA = ignoreBlankCDATACheck.isSelected();
                }
            });

            ignoreBlankCommentCheck.setSelected(UserConfig.getBoolean(conn, "XmlIgnoreBlankComment", true));
            ignoreBlankCommentCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("XmlIgnoreBlankComment", ignoreBlankCommentCheck.isSelected());
                    XmlTreeNode.ignoreBlankComment = ignoreBlankCommentCheck.isSelected();
                }
            });

            ignoreBlankInstructionCheck.setSelected(UserConfig.getBoolean(conn, "XmlIgnoreBlankInstruction", true));
            ignoreBlankInstructionCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("XmlIgnoreBlankInstruction", ignoreBlankInstructionCheck.isSelected());
                    XmlTreeNode.ignoreBlankInstrution = ignoreBlankInstructionCheck.isSelected();
                }
            });

            supportNamespacesCheck.setSelected(UserConfig.getBoolean(conn, "XmlSupportNamespaces", false));
            supportNamespacesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("XmlSupportNamespaces", supportNamespacesCheck.isSelected());
                }
            });

            indentCheck.setSelected(UserConfig.getBoolean(conn, "XmlTransformerIndent", true));
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
