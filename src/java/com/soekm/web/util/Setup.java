/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.web.util;

//import static com.sun.faces.facelets.util.Path.context;

/**
 *
 * @author atjkm
 */
public class Setup {

    // declare some lifetime variables
    public static final String DB_URL = "jdbc:mysql://localhost:3306/soekmdb"; // database name
    public static final String DB_USERNAME = "userName"; // database user name
    public static final String DB_PASSWORD = "password"; // database password
    public static final String MAIL_USERNAME = "youremail@gmail.com"; // sender mail address
    public static final String MAIL_PASSWORD = "password";  // sender mail password here used to log to the above gmail account
    public static final String MAIL_SMTP_HOST = "smtp.gmail.com"; // server hosting sender email 
    public static final String MAIL_REGISTRATION_SITE_LINK = "http://localhost:8080/soekm2018/login.xhtml"; // where user is taken after clicking on emailed registration link.
    public  static final String MAIL_NEW_PASSWORD_SITE_LINK =   "http://localhost:8080/soekm2018/restPasswordExpired.xhtml"; // where user will create a new password   
}
