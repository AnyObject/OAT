/*
    Open Auto Trading : A fully automatic equities trading platform with machine learning capabilities
    Copyright (C) 2015 AnyObject Ltd.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package OAT.util;

import java.io.IOException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import OAT.trading.Account;
import OAT.trading.Main;

/**
 *
 * @author Antonio Yip
 */
public class Mailer {

    private String receipients;
    private String className;
    private String host;
    private String user;
    private String password;
    private String prevMessage;
    private boolean sendLocal;

    public Mailer(String receipients, String className) {
        this.receipients = receipients;
        this.className = className;

        if (Main.isDefaultHost()) {
            sendLocal = true;
            host = "localhost";
        } else {
            //host = "smtp.gmail.com";
        }

        user = Main.p_email_Sender;
        password = "kuma0624";
    }

    public void sendMessage(final String subject, final String content) {
        if (!Main.p_Send_Mail
                || host == null
                || receipients == null
                || Main.getAccount() == Account.DEMO) {
            return;
        }

        for (final String receipient : receipients.split("[,;\\s+]")) {
            if (receipient.isEmpty() || !receipient.contains("@")) {
                continue;
            }
            
            prevMessage = content;

            if (sendLocal) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        StringBuilder sb = new StringBuilder();
                        sb.append("echo \"");
//                    sb.append("<html><body>br<b>");
                        sb.append(content);
//                    sb.append("</i><br>br<i>Italic Text</i>br</body></html>");
                        sb.append("\" | mail -s \"");
                        sb.append(subject).append(" - ").append(className).append("\" ");
                        sb.append(receipient).append(" -f ");
                        sb.append(user);

                        try {
                            SystemUtil.runShellCommand(sb.toString());
                        } catch (IOException ex) {
                        }
                    }
                }).start();

            } else {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        Properties props = new Properties();

                        props.put("mail.smtp.host", host);
                        props.put("mail.smtp.socketFactory.port", "465");
                        props.put("mail.smtp.socketFactory.class",
                                "javax.net.ssl.SSLSocketFactory");
                        props.put("mail.smtp.auth", "true");
                        props.put("mail.smtp.port", "465");

                        javax.mail.Session session = javax.mail.Session.getDefaultInstance(
                                props,
                                new javax.mail.Authenticator() {

                                    @Override
                                    protected PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(user, password);
                                    }
                                });

                        try {
                            Message message = new MimeMessage(session);
                            message.setFrom(new InternetAddress(user));
                            message.setRecipients(Message.RecipientType.TO,
                                    InternetAddress.parse(receipient));
                            message.setSubject(subject);

                            StringBuilder sb = new StringBuilder();

                            sb.append("<p><font size=\"3\" face=\"courier\" >");
                            sb.append(content);
                            sb.append("</font></p>");

                            message.setContent(sb.toString(), "text/html");

                            Transport.send(message);

                        } catch (MessagingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();
            }
        }
    }

    public void status(String content) {
        sendMessage("Status", content);
    }

    public void error(String content) {
        sendMessage("Error", content);
    }

    public void activity(String content) {
        sendMessage("Activity", content);
    }
}
