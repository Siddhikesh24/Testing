package service;

import java.util.Properties;
import model.ModelMessage;
import javax.mail.*;
import javax.mail.internet.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.ModelUser;

public class Authentication {
    ServiceUser service = new ServiceUser();
    
    public ModelMessage sendMain(String toEmail, String code){
        ModelMessage ms = new ModelMessage(false, "");
        String from = "siddhi242001@gmail.com";
        final String username = "siddhi242001@gmail.com";     // sender Gmail address
        final String password = "zvbi cmts ktxl iwga ";        // use an app-specific password
	Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

	Session session = Session.getInstance(props,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
	
	try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
            );
            message.setSubject("Verify Code");
            message.setText(code);

            Transport.send(message);
            ms.setSuccess(true);

        } catch (MessagingException e) {
	    if(e.getMessage().equals("Invalid Addresses")){
                ms.setMessage("Invalid email");
                ModelUser user = new ModelUser(toEmail,code);
                try {
                    service.deleteuser(user);
                } catch (SQLException ex) {
                    ms.setMessage("Can't delete user");
                }
	    }else{
		ms.setMessage("Error");
	     }		
        }
	return ms;
    } 
}
