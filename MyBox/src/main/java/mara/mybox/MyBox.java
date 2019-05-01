package mara.mybox;

import javafx.application.Application;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2019-1-22 14:35:50
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBox {

    // To pass arguments to JavaFx GUI
    // https://stackoverflow.com/questions/33549820/javafx-not-calling-mainstring-args-method/33549932#33549932
    public static void main(String[] args) {
        logger.info("Starting Mybox...");
        Application.launch(MainApp.class, args);
    }

}
