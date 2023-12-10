package mara.mybox.controller;

import java.util.Date;
import java.util.Properties;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-10-2
 * @License Apache License Version 2.0
 */
public class MessageAuthorController extends BaseController {

    @FXML
    protected TextArea commentsArea;
    @FXML
    protected TextField titleInput, nameInput, osInput;

    public MessageAuthorController() {
        baseTitle = Languages.message("MessageAuthor");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            osInput.setText(SystemTools.os());
            nameInput.setText(System.getProperty("user.name"));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadMessage(String title, String message) {
        titleInput.setText(title);
        commentsArea.setText(message);
    }

    @FXML
    public void messageAction() {
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.163.com");
                props.put("mail.smtp.port", "25");
                props.put("mail.transport.protocol", "smtp");
                props.put("mail.debug", "false");
                Session session = Session.getDefaultInstance(props, null);
                try (Transport transport = session.getTransport("smtp")) {
                    Message message = new MimeMessage(session);
                    String title = titleInput.getText();
                    if (title.isBlank()) {
                        title = "MyBox message from " + nameInput.getText();
                    }
                    message.setSubject(title);
                    String s = commentsArea.getText() + "<hr><br>"
                            + "OS: " + osInput.getText() + "<br>"
                            + "From: " + nameInput.getText() + "<br>"
                            + DateTools.nowString();
                    message.setContent(s, "text/html;charset=UTF-8");
                    message.setFrom(new InternetAddress("mybox_message@163.com", "MyBox - " + nameInput.getText()));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("mararsh@sina.com"));
                    message.setSentDate(new Date());
                    message.saveChanges();

                    transport.connect("smtp.163.com", "mybox_message@163.com", "ARUPFZMLULNSHFOI");
                    transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));

                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                popInformation(Languages.message("ThanksMessage") + "\n" + DateTools.nowString(), 6000);
                SoundTools.miao5();
            }

        };
        start(task);
    }

}
