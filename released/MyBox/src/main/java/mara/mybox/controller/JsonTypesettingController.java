package mara.mybox.controller;

import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data.JsonTreeNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-5-26
 * @License Apache License Version 2.0
 */
public class JsonTypesettingController extends BaseBatchFileController {

    protected ObjectMapper objectMapper;
    protected ObjectWriter objectWriter;

    @FXML
    protected ToggleGroup typesettingGroup;
    @FXML
    protected RadioButton indentRadio;
    @FXML
    protected ControlJsonOptions optionsController;

    public JsonTypesettingController() {
        baseTitle = message("JsonTypesetting");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.JSON);
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            objectMapper = JsonTreeNode.jsonMapper();
            if (indentRadio.isSelected()) {
                objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
            } else {
                PrettyPrinter pp = null;
                objectWriter = objectMapper.writer(pp);
            }
            return super.makeMoreParameters();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            File target = makeTargetFile(srcFile, targetPath);
            if (target == null) {
                return message("Skip");
            }
            objectWriter.writeValue(target, objectMapper.readTree(srcFile));

            if (target.exists() && target.length() > 0) {
                targetFileGenerated(target);
                return message("Successful");
            } else {
                return message("Failed");
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return message("Failed");
        }
    }

}
