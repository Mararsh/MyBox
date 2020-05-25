package mara.mybox.fxml;

import javafx.scene.text.Text;
import mara.mybox.data.GeographyCode;

/**
 * @Author Mara
 * @CreateDate 2020-04-18
 * @License Apache License Version 2.0
 */
public class ConditionNode extends Text {

    private GeographyCode code;
    private String title, condition;

    public ConditionNode() {
    }

    public ConditionNode(String text) {
        setText(text);
    }

    public static ConditionNode create(String text) {
        ConditionNode item = new ConditionNode(text);
        return item;
    }

    public GeographyCode getCode() {
        return code;
    }

    /*
    customized get/set
     */
 /*
    get/set
     */
    public ConditionNode setCode(GeographyCode code) {
        this.code = code;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ConditionNode setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getCondition() {
        return condition;
    }

    public ConditionNode setCondition(String condition) {
        this.condition = condition;
        return this;
    }

}
