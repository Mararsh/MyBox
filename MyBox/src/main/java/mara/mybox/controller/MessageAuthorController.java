package mara.mybox.controller;

import java.util.Date;
import java.util.Properties;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-10-2
 * @License Apache License Version 2.0
 */
public class MessageAuthorController extends BaseController {

    @FXML
    protected CheckBox miaowCheck, usefulCheck, uselessCheck;
    @FXML
    protected TextArea commentsArea;
    @FXML
    protected TextField nameInput, osInput;

    public MessageAuthorController() {
        baseTitle = AppVariables.message("MessageAuthor");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            osInput.setText(SystemTools.os());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void startAction() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.163.com");
            props.put("mail.smtp.port", "25");
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.debug", "false");
            Session session = Session.getDefaultInstance(props, null);
            Message message = new MimeMessage(session);
            message.setSubject("MyBox message from " + nameInput.getText());
            String s = (miaowCheck.isSelected() ? "喵    " : "")
                    + (usefulCheck.isSelected() ? "MyBox有用    " : "")
                    + (uselessCheck.isSelected() ? "MyBox没用    " : "")
                    + "<hr>" + commentsArea.getText()
                    + osInput.getText() + "<br>"
                    + nameInput.getText() + "<br>"
                    + DateTools.nowString();
            message.setContent(s, "text/html;charset=UTF-8");
            message.setFrom(new InternetAddress("mybox_message@163.com", "MyBox - " + nameInput.getText()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("mararsh@sina.com"));
            message.setSentDate(new Date());
            message.saveChanges();

            try ( Transport transport = session.getTransport("smtp")) {
                transport.connect("smtp.163.com", "mybox_message@163.com", "ARUPFZMLULNSHFOI");
                transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
            }

            popInformation(message("ThanksMessage") + "\n" + DateTools.nowString(), 6000);
            FxmlControl.miao5();
        } catch (Exception e) {

        }

    }

}
