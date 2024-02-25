package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 */
public class Data2DPageCSV extends Data2DPageHtml {

    protected String displayDelimiterName;
    protected ChangeListener<Boolean> delimiterListener;

    @FXML
    protected CheckBox wrapCheck;
    @FXML
    protected TextArea textArea;

    public Data2DPageCSV() {
        baseTitle = message("ViewPageDataInCSV");
    }

    @Override
    protected void initMore() {
        try {
            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayTextWrap", true));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "DisplayTextWrap", newValue);
                    textArea.setWrapText(newValue);
                }
            });
            textArea.setWrapText(wrapCheck.isSelected());

            displayDelimiterName = UserConfig.getString(baseName + "DisplayDelimiter", ",");

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @Override
    protected void updatePage() {
        if (formCheck.isSelected()) {
            textInForm();
        } else {
            textInTable();
        }
    }

    protected void textInTable() {
        FxTask<Void> textTask = new FxTask<Void>(this) {
            String text;

            @Override
            protected boolean handle() {
                try {
                    text = data2D.encodeCSV(this, displayDelimiterName,
                            rowCheck.isSelected(), columnCheck.isSelected(), true);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                String title = titleCheck.isSelected() ? data2D.titleName() : null;
                if (title != null && !title.isBlank()) {
                    textArea.setText(title + "\n\n" + text);
                } else {
                    textArea.setText(text);
                }
            }

        };
        start(textTask, false);
    }

    protected void textInForm() {
        StringBuilder s = new StringBuilder();
        if (titleCheck.isSelected()) {
            s.append(data2D.titleName()).append("\n\n");
        }
        for (int r = 0; r < data2D.tableRowsNumber(); r++) {
            if (rowCheck.isSelected()) {
                s.append(data2D.rowName(r)).append("\n");
            }
            List<String> drow = data2D.tableRow(r, false, true);
            if (drow == null) {
                continue;
            }
            for (int col = 0; col < data2D.columnsNumber(); col++) {
                if (columnCheck.isSelected()) {
                    s.append(data2D.columnName(col)).append(": ");
                }
                String v = drow.get(col);
                if (v == null) {
                    continue;
                }
                s.append(StringTools.replaceLineBreak(v, "\\\\n")).append("\n");
            }
            s.append("\n");
            textArea.setText(s.toString());
        }
    }

    @FXML
    @Override
    public void editAction() {
        TextEditorController.edit(textArea.getText());
    }

    @FXML
    @Override
    public void refreshAction() {
        updatePage();
    }

    @FXML
    @Override
    public boolean popAction() {
        TextPopController.openInput(this, textArea);
        return true;
    }

    @FXML
    @Override
    public boolean menuAction() {
        closePopup();
        Point2D localToScreen = textArea.localToScreen(textArea.getWidth() - 80, 80);
        MenuTextEditController.textMenu(myController, textArea, localToScreen.getX(), localToScreen.getY());
        return true;
    }

    @FXML
    public void delimiterActon() {
        TextDelimiterController controller = TextDelimiterController.open(this, displayDelimiterName, true, false);
        controller.okNotify.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                displayDelimiterName = controller.delimiterName;
                UserConfig.setString(baseName + "DisplayDelimiter", displayDelimiterName);
                if (!formCheck.isSelected()) {
                    textInTable();
                }
            }
        });
        if (data2D.isCSV() || data2D.isTexts()) {
            controller.label.setText(message("DelimiterNotAffectSource"));
        }
    }

    @Override
    public void cleanPane() {
        try {
            delimiterListener = null;
            displayDelimiterName = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static Data2DPageCSV open(BaseData2DLoadController tableController) {
        try {
            Data2DPageCSV controller = (Data2DPageCSV) WindowTools.branchStage(
                    tableController, Fxmls.Data2DPageCSVFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
