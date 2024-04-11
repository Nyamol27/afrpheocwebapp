package net.pheocnetafr.africapheocnet.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class ImageToBase64 {
    public static void main(String[] args) {
        try {
           
            InputStream emailIllustrationStream = ImageToBase64.class.getResourceAsStream("/static/images/Email-Illustration.png");
            byte[] emailIllustrationData = emailIllustrationStream.readAllBytes();

            
            InputStream pheocNetStream = ImageToBase64.class.getResourceAsStream("/static/images/pheoc-net.png");
            byte[] pheocNetData = pheocNetStream.readAllBytes();

           
            String emailIllustrationBase64 = Base64.getEncoder().encodeToString(emailIllustrationData);
            
            String pheocNetBase64 = Base64.getEncoder().encodeToString(pheocNetData);

           
            System.out.println("Email-Illustration.png as Base64: " + emailIllustrationBase64);
            System.out.println("pheoc-net.png as Base64: " + pheocNetBase64);

            
            emailIllustrationStream.close();
            pheocNetStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
