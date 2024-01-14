package mara.mybox.controller;

import java.sql.Connection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.JsonTreeNode;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-4-30
 * @License Apache License Version 2.0
 */
public class JsonOptionsController extends BaseController {

    @FXML
    protected CheckBox AllowJavaCommentsCheck, AllowYamlCommentsCheck, AllowSingleQuotesCheck, AllowUnquotedFieldNamesCheck,
            AllowUnescapedControlCharsCheck, AllowBackslashEscapingAnyCheck, AllowLeadingZerosForNumbersCheck,
            AllowLeadingPlusSignForNumbersCheck, AllowLeadingDecimalPointForNumbersCheck, AllowTrailingDecimalPointForNumbersCheck,
            AllowNonNumericNumbersCheck, AllowMissingValuesCheck, AllowTrailingCommaCheck,
            quoteFieldNamesCheck, writeNanAsStringsCheck, writeNumbersAsStringsCheck, escapeNonASCIICheck;

    public JsonOptionsController() {
        baseTitle = "JSON";
    }

    @Override
    public void initControls() {
        super.initControls();
        try (Connection conn = DerbyBase.getConnection()) {
            AllowJavaCommentsCheck.setSelected(UserConfig.getBoolean(conn, "JacksonAllowJavaComments", false));
            AllowJavaCommentsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonAllowJavaComments", AllowJavaCommentsCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });
            AllowYamlCommentsCheck.setSelected(UserConfig.getBoolean(conn, "JacksonAllowYamlComments", false));
            AllowYamlCommentsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonAllowYamlComments", AllowYamlCommentsCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });
            AllowSingleQuotesCheck.setSelected(UserConfig.getBoolean(conn, "JacksonAllowSingleQuotes", false));
            AllowSingleQuotesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonAllowSingleQuotes", AllowSingleQuotesCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });
            AllowUnquotedFieldNamesCheck.setSelected(UserConfig.getBoolean(conn, "JacksonAllowUnquotedFieldNames", false));
            AllowUnquotedFieldNamesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonAllowUnquotedFieldNames", AllowUnquotedFieldNamesCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });
            AllowUnescapedControlCharsCheck.setSelected(UserConfig.getBoolean(conn, "JacksonAllowUnescapedControlChars", false));
            AllowUnescapedControlCharsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonAllowUnescapedControlChars", AllowUnescapedControlCharsCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });
            AllowBackslashEscapingAnyCheck.setSelected(UserConfig.getBoolean(conn, "JacksonAllowBackslashEscapingAny", false));
            AllowBackslashEscapingAnyCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonAllowBackslashEscapingAny", AllowBackslashEscapingAnyCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });
            AllowLeadingZerosForNumbersCheck.setSelected(UserConfig.getBoolean(conn, "JacksonAllowLeadingZerosForNumbers", false));
            AllowLeadingZerosForNumbersCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonAllowLeadingZerosForNumbers", AllowLeadingZerosForNumbersCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });
            AllowLeadingPlusSignForNumbersCheck.setSelected(UserConfig.getBoolean(conn, "JacksonAllowLeadingPlusSignForNumbers", false));
            AllowLeadingPlusSignForNumbersCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonAllowLeadingPlusSignForNumbers", AllowLeadingPlusSignForNumbersCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });
            AllowLeadingDecimalPointForNumbersCheck.setSelected(UserConfig.getBoolean(conn, "JacksonAllowLeadingDecimalPointForNumbers", false));
            AllowLeadingDecimalPointForNumbersCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonAllowLeadingDecimalPointForNumbers", AllowLeadingDecimalPointForNumbersCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });
            AllowTrailingDecimalPointForNumbersCheck.setSelected(UserConfig.getBoolean(conn, "JacksonAllowTrailingDecimalPointForNumbers", false));
            AllowTrailingDecimalPointForNumbersCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonAllowTrailingDecimalPointForNumbers", AllowTrailingDecimalPointForNumbersCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });
            AllowNonNumericNumbersCheck.setSelected(UserConfig.getBoolean(conn, "JacksonAllowNonNumericNumbers", false));
            AllowNonNumericNumbersCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonAllowNonNumericNumbers", AllowNonNumericNumbersCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });
            AllowMissingValuesCheck.setSelected(UserConfig.getBoolean(conn, "JacksonAllowMissingValues", false));
            AllowMissingValuesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonAllowMissingValues", AllowMissingValuesCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });
            AllowTrailingCommaCheck.setSelected(UserConfig.getBoolean(conn, "JacksonAllowTrailingComma", false));
            AllowTrailingCommaCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonAllowTrailingComma", AllowTrailingCommaCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });

            quoteFieldNamesCheck.setSelected(UserConfig.getBoolean(conn, "JacksonQuoteFieldNames", true));
            quoteFieldNamesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonQuoteFieldNames", quoteFieldNamesCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });

            writeNanAsStringsCheck.setSelected(UserConfig.getBoolean(conn, "JacksonWriteNanAsStrings", true));
            writeNanAsStringsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonWriteNanAsStrings", writeNanAsStringsCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });

            writeNumbersAsStringsCheck.setSelected(UserConfig.getBoolean(conn, "JacksonWriteNumbersAsStrings", false));
            writeNumbersAsStringsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonWriteNumbersAsStrings", writeNumbersAsStringsCheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });

            escapeNonASCIICheck.setSelected(UserConfig.getBoolean(conn, "JacksonEscapeNonASCII", false));
            escapeNonASCIICheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean("JacksonEscapeNonASCII", escapeNonASCIICheck.isSelected());
                    JsonTreeNode.resetJsonMapper();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static JsonOptionsController open() {
        try {
            JsonOptionsController controller = (JsonOptionsController) WindowTools.popStage(null, Fxmls.JsonOptionsFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
