package mara.mybox.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-9-2
 * @License Apache License Version 2.0
 */
public class FunctionUnaryEditor extends JavaScriptEditor {

    protected FunctionUnaryController functionController;

    @FXML
    protected ListView<String> variablesList;

    public FunctionUnaryEditor() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            variablesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            variablesList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    String selected = variablesList.getSelectionModel().getSelectedItem();
                    if (selected != null && !selected.isBlank()) {
                        valueInput.replaceText(valueInput.getSelection(), "#{" + selected + "}");
                    }
                }
            });
            variablesList.getItems().add("x");

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setParameters(FunctionUnaryController functionController) {
        this.functionController = functionController;
        super.setParameters(functionController);
    }

}
