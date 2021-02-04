/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.web;

import com.soekm.entity.Customer;
import com.soekm.web.util.BCrypt;
import com.soekm.web.util.GlobalConstants;
import com.soekm.web.util.JsfUtil;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author atjkm
 */
@Named(value = "verifyRegisteredEmailHash")
@RequestScoped
public class VerifyRegisteredEmailHash {

    private static final String BUNDLE = "bundles.Bundle";
    private final static Logger LOGGER = Logger.getLogger(VerifyRegisteredEmailHash.class.getCanonicalName());

    /**
     * Creates a new instance of VerifyRegisteredEmailHash
     */
    @Inject
    com.soekm.web.RegisterEmailUtil registerEmailUtil;
    @Inject
    com.soekm.web.CustomerController customerController;
    @EJB
    private com.soekm.ejb.UserBean userBeanFacade;
    //Person user;
    private Customer customer;

    private Integer userId;
    private String hash;
    private String scope;
    private String email;

    /*FacesContext facesContext = FacesContext.getCurrentInstance();
    ExternalContext ec = facesContext.getExternalContext();*/
    public void verifyEmail() {
        // get user Id and email verification code Hash code 
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());

        userId = Integer.parseInt(request.getParameter("userId"));
        System.out.print("user in verifyEmail " + userId);
        hash = BCrypt.hashpw(request.getParameter("hash"), GlobalConstants.SALT);
        scope = request.getParameter("scope");

        try {

            Calendar cal = Calendar.getInstance();
            if ((userBeanFacade.getUserById(userId).getExpiryDate()
                    .getTime() - cal.getTime()
                            .getTime()) <= 0) {
                JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "Token_Expired"));
                // let user send another token at this point
                userBeanFacade.generateNewToken(userId);
                //userBeanFacade.updateVerificationToken(userId, BCrypt.hashpw(hashcode, GlobalConstants.SALT));

            } else {
                // verify with database
                // use class reference to access static method
                if (userBeanFacade.verifyEmailHash(userId, hash) && scope.equals(GlobalConstants.ACTIVATION)) {
                    // at this point change status to active so if the user remembers their old password they can still use it
                    userBeanFacade.updateStaus(userId, "active");
                    userBeanFacade.updateVerificationToken(userId, null);
                } else {
                    // this section is not in use. Instead the method submitCreatePassword() is used for reseting password
                    if (userBeanFacade.verifyEmailHash(userId, hash) && scope.equals(GlobalConstants.RESET_PASSWORD)) {
                        userBeanFacade.updateStaus(userId, "active");
                        userBeanFacade.updateVerificationToken(userId, null);
                        //put some session for user
                        request.getSession().setAttribute(GlobalConstants.USER, userId);
                        request.getSession().setAttribute(GlobalConstants.IS_RESET_PASSWORD_VERIFIED, GlobalConstants.YES);
                        //FacesContext.getCurrentInstance().getExternalContext().redirect("createPassword.xhtml");
                        //System.out.print("am now redirecting to createPassword.xml ");
                        this.submitCreatePassword();
                    }
                }
            }
        } catch (IOException | EJBException | MessagingException e) {
            LOGGER.log(Level.SEVERE, null, e);
            JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "Email_verification_failed"));
        }
    }

    // prefered method for resseting password
    public void submitCreatePassword() throws IOException {
        // get user Id and email verification code Hash code 
        HttpServletRequest request = (HttpServletRequest) (FacesContext.getCurrentInstance().getExternalContext().getRequest());

        userId = Integer.parseInt(request.getParameter("userId"));
        System.out.print("user in submitCreatePassword " + userId);
        hash = BCrypt.hashpw(request.getParameter("hash"), GlobalConstants.SALT);
        scope = request.getParameter("scope");

        try {

            Calendar cal = Calendar.getInstance();
            if ((userBeanFacade.getUserById(userId).getExpiryDate()
                    .getTime() - cal.getTime()
                            .getTime()) <= 0) {
                JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "Token_Expired"));
                // let user send another token at this point
                userBeanFacade.generateNewTokenForRestPassowrod(userId);
                //userBeanFacade.updateVerificationToken(userId, BCrypt.hashpw(hashcode, GlobalConstants.SALT));

            } else {
                if (userBeanFacade.verifyEmailHash(userId, hash) && scope.equals(GlobalConstants.RESET_PASSWORD)) {
                    // at this point change status to active so if the user remembers their old password they can still use it
                    userBeanFacade.updateStaus(userId, "active");
                    //userBeanFacade.updateVerificationToken(userId, null);
                    //put some session for user
                    request.getSession().setAttribute(GlobalConstants.USER, userId);
                    request.getSession().setAttribute(GlobalConstants.IS_RESET_PASSWORD_VERIFIED, GlobalConstants.YES);
                    String uri = "/createPassword.xhtml";

                    final FacesContext fc = FacesContext.getCurrentInstance();
                    //final Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();
                    final NavigationHandler nav = fc.getApplication().getNavigationHandler();
                nav.handleNavigation(fc, null, uri);
                fc.renderResponse();
                    /*ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                    context.redirect(context.getRequestContextPath() + uri);*/
                }
            }
        } catch (IOException | EJBException | MessagingException e) {
            LOGGER.log(Level.SEVERE, null, e);
            JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "Email_verification_failed"));
        }
        //return "/WEB-INF/createPassword.xhtml";
    }

    public String generateUrlToWebservice() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext exContext = fc.getExternalContext();
        String servername = exContext.getRequestServerName();
        String port = String.valueOf(exContext.getRequestServerPort());
        String appname = exContext.getRequestContextPath();
        String protocol = exContext.getRequestScheme();
        String pagePath = exContext.getInitParameter("pagePath"); //read it from web.xml
        return protocol + "://" + servername + ":" + port + appname + pagePath;
    }

    public String goToLoginPage() {
        return "login";
    }

    public String goToRequestTokenPage() {
        return "sendToken";
    }

    public String getUserEmail() {
        return email;
    }

    public void setUserEmail(String email) {
        this.email = email;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getHash() {
        return hash = BCrypt.hashpw(hash, GlobalConstants.SALT);
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public VerifyRegisteredEmailHash() {
    }

}
