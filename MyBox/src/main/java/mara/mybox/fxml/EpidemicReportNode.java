/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.fxml;

import javafx.scene.text.Text;

/**
 *
 * @author mara
 */
public class EpidemicReportNode extends Text {

    private String title, condition;

    public EpidemicReportNode() {
    }

    public EpidemicReportNode(String text) {
        setText(text);
    }

    public static EpidemicReportNode create(String text) {
        EpidemicReportNode item = new EpidemicReportNode(text);
        return item;
    }

    /*
        customized get/set
     */

 /*
        get/set
     */
    public String getTitle() {
        return title;
    }

    public EpidemicReportNode setTitle(String title) {
        this.title = title;
        return this;
    }

}
