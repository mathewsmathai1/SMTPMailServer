import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.naming.*;
import javax.naming.directory.*;

public class DirectMail {

    public static void main(String[] args) throws NamingException {
    	
      try {

        
            Properties props = new Properties();
           // props.setProperty("mail.smtp.host", "smtp.gmail.com");  needs authorization
            
           props.setProperty("mail.smtp.host", getMX("gmail.com")[0]); //Just using the first out of the long list returned by MX lookup function getMX
           // props.setProperty("mail.smtp.port", "25");  default anyway
            props.setProperty("mail.debug", "true");  //mail.protocol.user
   
          /*  Below is required if you are using smtp.gmail.com to login to your account
           *  To get it working you need to allow login through insecure apps etc in Google settings
           *  props.put("mail.smtp.ssl.enable", "true");
              props.put("mail.smtp.auth", "true");
           */

           Session session = Session.getInstance(props);
            
        /* You will need the following if you connect to smtp.gmail.com
         * But you are fine if you are connecting to one of Google's mail receiving servers   
         * Session session = Session.getInstance(props, new javax.mail.Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {

                    return new PasswordAuthentication("yourmaildiid", "yourpassword");

                }}); */
           
            MimeMessage message = new MimeMessage(session);
            message.setFrom("mathews@mydomain.com");  
            //you could just set it to anything but if you keep spamming emails Google may eventually block you anyway if you get listed in Spamhaus DB
            message.addRecipient(RecipientType.TO, new InternetAddress("mygmailid")); 
            //gmail since I am connecting to gmail
            message.setSubject("SMTP Test");
            message.setText("Hello There! It's just Me Again.");
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }  
        
    	
    	
    }

    //Just all the MX lookup code. MX as in Mail Exchanger. You could read a little more about it but I was just fine with sending mails for now
    public static String[] getMX(String domainName) throws NamingException {
        Hashtable<String, Object> env = new Hashtable<String, Object>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        env.put(Context.PROVIDER_URL, "dns:");

        DirContext ctx = new InitialDirContext(env);
        Attributes attribute = ctx.getAttributes(domainName, new String[] {"MX"});
        Attribute attributeMX = attribute.get("MX");
        // if there are no MX RRs then default to domainName (see: RFC 974)
        if (attributeMX == null) {
            return (new String[] {domainName});
        }

        // split MX RRs into Preference Values(pvhn[0]) and Host Names(pvhn[1])
        String[][] pvhn = new String[attributeMX.size()][2];
        for (int i = 0; i < attributeMX.size(); i++) {
            pvhn[i] = ("" + attributeMX.get(i)).split("\\s+");
        }

        // sort the MX RRs by RR value (lower is preferred)
        Arrays.sort(pvhn, (o1, o2) -> Integer.parseInt(o1[0]) - Integer.parseInt(o2[0]));

        String[] sortedHostNames = new String[pvhn.length];
        for (int i = 0; i < pvhn.length; i++) {
            sortedHostNames[i] = pvhn[i][1].endsWith(".") ? 
                pvhn[i][1].substring(0, pvhn[i][1].length() - 1) : pvhn[i][1];
        }
        return sortedHostNames;     
    }
}