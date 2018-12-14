package svh;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class Mailer {
	public static void send(String from,ArrayList<String> to,String sub,ArrayList<String> output){  
        //Get properties object    
        Properties props = new Properties();
        
   
        props.put("mail.smtp.host", "webmail.test.com");    
  
        //get Session   
        Session session = Session.getDefaultInstance(props);    
   
        //compose message    
        try {    
        	 DateFormat dateFormate=new SimpleDateFormat("dd-MMM-yyyy");
     		 Date date=new Date();
     		 String d=dateFormate.format(date);
         MimeMessage message = new MimeMessage(session); 
         message.setFrom(new InternetAddress(from));
         for (int i = 0; i < to.size(); i++) {
				// Set To: header field of the header.
				message.addRecipient(Message.RecipientType.TO,
						new InternetAddress((String) to.get(i)));
			}
 
//       message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));    
         message.setSubject(sub+" as on "+d); 
         String out = " ";
         for(String text:output){
				out=out+text+"\n"; 
			}
         
         message
			.setText("\n\nPlease find the User Details for the User list Provided:\n\n"+out+"\n\nThanks\n Team" );

         //send message  
         Transport.send(message);    
         System.out.println("message sent successfully");   
         
         
       
        } catch (MessagingException e) {throw new RuntimeException(e);
        
        }    
        
           
	}
	
}
