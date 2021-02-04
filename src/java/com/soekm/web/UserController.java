/**
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * You may not modify, use, reproduce, or distribute this software except in
 * compliance with  the terms of the License at:
 * http://java.net/projects/javaeetutorial/pages/BerkeleyLicense
 */
package com.soekm.web;

import com.soekm.ejb.ArticleTableFacade;
import com.soekm.ejb.ArticlecategoryTableFacade;
import com.soekm.ejb.CommentTableFacade;
import com.soekm.entity.Administrator;
import com.soekm.entity.ArticleTable;
import com.soekm.entity.ArticlecategoryTable;
import com.soekm.entity.CommentTable;
import com.soekm.entity.Groups;
import com.soekm.entity.Person;
import com.soekm.qualifiers.LoggedIn;
import com.soekm.web.util.BCrypt;
import com.soekm.web.util.GlobalConstants;
import com.soekm.web.util.JsfUtil;
import com.soekm.web.util.MD5Util;
import com.soekm.web.util.MailUtil;
import com.soekm.web.util.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIInput;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.validator.ValidatorException;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.Part;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.faces.application.FacesMessage;

/**
 *
 * @author markito
 */
@Named(value = "userController")
@SessionScoped
public class UserController implements Serializable {

    private static final String BUNDLE = "bundles.Bundle";
    private final static Logger LOGGER = Logger.getLogger(ArticleController.class.getCanonicalName());
    //private static final long serialVersionUID = -8851462237612818158L;

    Person user;
    //private static Map<Person, HttpSession> logins = new HashMap<Person, HttpSession>();
    @EJB
    private com.soekm.ejb.UserBean getEjbFacade;
    @EJB
    private com.soekm.ejb.PersonFacade getPersonFacade;
    @EJB
    ArticlecategoryTableFacade articlecategoryFacade;
    @EJB
    ArticleTableFacade articleFacade;
    @EJB
    CommentTableFacade commentTableFacade;
    @Inject
    com.soekm.web.CustomerController customerController;

    private List<ArticlecategoryTable> categories;
    private List<ArticleTable> articles;
    private List<CommentTable> comments;
    private ArticlecategoryTable selectedCategory;
    private ArticleTable selectedArticle;
    private List<ArticleTable> categoryProducts;
    private String selectedCategoryId = null;
    private String selectedArticleId = null;
    private int selectedItemIndex;
    private String username;
    private String firstname;
    private String lastname;
    private String address;
    private String password;
    private Integer personId;
    private String city;
    private String passwordConfirm;
    private String newPassword;
    private byte[] avatar;
    private String image;
    private Part filePart;

    private List<ArticleTable> latest5Posts;

    private static final List<String> EXTENSIONS_ALLOWED = new ArrayList<>();

    public static final String HOME_PAGE_REDIRECT
            = "/index.xhtml?faces-redirect=true";
    public static final String LOGOUT_PAGE_REDIRECT
            = "/logout.xhtml?faces-redirect=true";

    static {
        // images only
        EXTENSIONS_ALLOWED.add(".jpg");
        EXTENSIONS_ALLOWED.add(".bmp");
        EXTENSIONS_ALLOWED.add(".png");
        EXTENSIONS_ALLOWED.add(".gif");
    }

    private String getFileName(Part part) {
        String partHeader = part.getHeader("content-disposition");
        LOGGER.log(Level.INFO, "Part Header = {0}", partHeader);
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

    public void upload() {
        LOGGER.info(getFilePart().getName());

        try {
            InputStream is = getFilePart().getInputStream();

            int i = is.available();
            byte[] b = new byte[i];
            is.read(b);

            LOGGER.log(Level.INFO, "Length : {0}", b.length);
            String fileName = getFileName(getFilePart());
            LOGGER.log(Level.INFO, "File name : {0}", fileName);

            // generate *unique* filename 
            final String extension = fileName.substring(fileName.length() - 4);

            if (!EXTENSIONS_ALLOWED.contains(extension)) {
                try {
                    LOGGER.severe("User tried to upload file that's not an image. Upload cancelled.");
                    JsfUtil.addErrorMessage(ResourceBundle.getBundle(BUNDLE).getString("WrongFile"));
                    //ResourceBundle.getBundle(BUNDLE).getString("WrongFile"));
                    //response.sendRedirect("admin/product/List.xhtml?errMsg=Error trying to upload file");
                    return;
                } catch (Exception e) {
                    JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
                    //return;
                }
            }

            // if the bytes are more than the set size inform the user
            /*if (b.length>30240){
                try{
                LOGGER.severe("User tried to upload a file that is too large");
                    JsfUtil.addErrorMessage(ResourceBundle.getBundle(BUNDLE).getString("UploadedFileTooBig"));
                    return;
                }catch (Exception e){
                    JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
                }
            }*/
            if (filePart.getSize() > 30240) {
                try {
                    JsfUtil.addErrorMessage(ResourceBundle.getBundle(BUNDLE).getString("UploadedFileTooBig"));
                    return;
                } catch (Exception e) {
                    JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
                }
            } else if (filePart==null || filePart.getSize()<=0 || filePart.getContentType().isEmpty()) {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle(BUNDLE).getString("NoFileSelected"));
                return;
            }
            List<FacesMessage> message = new ArrayList<FacesMessage>();
            if (!message.isEmpty()) {
                throw new ValidatorException(message);
            }

//            Integer id = current.getId();
//            current = ejbFacade.find(2);
            user.setAvatar(b);
            user.setImage(fileName);
            getPersonFacade.edit(user);
            //userBeanFacade.edit(current);
            //setStep(3);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("ImageSuccessfullyUploaded"));

        } catch (IOException ex) {
            System.out.print(ex);
        }

    }

    /**
     *
     * @param ctx
     * @param comp
     * @param value method for validating the size if uploaded image
     */
    public void validateFile(FacesContext ctx,
            UIComponent comp,
            Object value) {
        List<FacesMessage> message = new ArrayList<FacesMessage>();
        Part file = (Part) value;
        if (file == null || file.getSize() <= 0 || file.getContentType().isEmpty()) {
            message.add(new FacesMessage("Select a valid file"));
        } else if (file.getSize() > 30240) {
            //JsfUtil.addErrorMessage(ResourceBundle.getBundle(BUNDLE).getString("UploadedFileTooBig"));
            message.add(new FacesMessage("The file you attempted to upload is too big. Please reduce the size"));
        }
        /*if (!"text/plain".equals(file.getContentType())) {
            msgs.add(new FacesMessage("not a text file"));
        }*/
        if (!message.isEmpty()) {
            throw new ValidatorException(message);
        }
    }

    /**
     * Creates a new instance of Login
     */
    public UserController() {
    }

    /**
     * getter for confirmation of password
     *
     * @return
     */
    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    /**
     * setter for confirmation of password
     *
     * @param passwordConfirm
     */
    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    /**
     * method for comparing and validating the two passwords entered by the user
     * when registering for the first time.
     *
     * @param context
     * @param component
     * @param value
     */
    public void validatePassword(FacesContext context, UIComponent component,
            Object value) {

        // Retrieve the value passed to this method
        String confirmPassword = (String) value;

        // Retrieve the temporary value from the password field
        UIInput passwordInput = (UIInput) component.findComponent("password");
        String passwordIn = (String) passwordInput.getLocalValue();
        //System.out.print("the password is "+ passwordIn);

        if (passwordIn == null || confirmPassword == null || !passwordIn.equals(confirmPassword)) {
            throw new ValidatorException(new FacesMessage(JsfUtil.getStringFromBundle(BUNDLE, "ConfirmPasswordWrong")));
        }
    }

    public void setLatest5(List<ArticleTable> latest5Posts) {
        this.latest5Posts = latest5Posts;
    }

    public int totalCategories() {
        return articlecategoryFacade.count();
    }

    public int totalArticles() {
        return articleFacade.count();
    }

    public List<ArticlecategoryTable> getCategories() {
        return this.articlecategoryFacade.findAll();
    }

    public void setCategories(List<ArticlecategoryTable> categories) {
        this.categories = categories;
    }

    public List<ArticleTable> getArticles() {
        return articleFacade.findAll();
    }

    public void setArticles(List<ArticleTable> articles) {
        this.articles = articles;
    }

    public ArticlecategoryTable getSelectedCategory() {
        if (selectedCategoryId != null) {
            selectedCategory = articlecategoryFacade.find(Integer.parseInt(selectedCategoryId));
        }
        return selectedCategory == null ? new ArticlecategoryTable() : selectedCategory;
    }

    public void setSelectedCategory(ArticlecategoryTable selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public String getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public void setSelectedCategoryId(String selectedCategoryId) {
        this.selectedCategoryId = selectedCategoryId;
    }

    public Collection<ArticleTable> getCategoryProducts() {
        categoryProducts = getSelectedCategory().getArticleTableList();
        return categoryProducts;
    }

    public void setCategoryProducts(List<ArticleTable> categoryProducts) {
        this.categoryProducts = categoryProducts;
    }

    public String getSelectedArticleId() {
        return selectedArticleId;
    }

    public void setSelectedArticleId(String selectedArticleId) {
        this.selectedArticleId = selectedArticleId;
    }

    public ArticleTable getSelectedArticle() {
        if (selectedArticleId != null) {
            selectedArticle = articleFacade.find(Integer.parseInt(selectedArticleId));
        }
        return selectedArticle == null ? new ArticleTable() : selectedArticle;

    }

    public void setSelectedArticle(ArticleTable selectedArticle) {
        this.selectedArticle = selectedArticle;
    }

    public String clickedArticle(ArticleTable at) {
        if (selectedArticleId != null) {
            selectedArticle = articleFacade.find(Integer.parseInt(selectedArticleId));
        }
        selectedArticle = at;
        return null;
    }

    /**
     * Login method based on <code>HttpServletRequest</code> and security realm
     *
     * @return note: this method needs improving to stop admin viewing admin
     * index when admin account is not activated
     */
    public String login() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String result = null;

        //String inputPassword = BCrypt.hashpw(request.getParameter(this.getPassword()), GlobalConstants.SALT);
        //String inputPassword = this.getPassword();
        //String userName = this.getUsername();
        try {
            request.login(this.getUsername(), this.getPassword());
            this.user = getEjbFacade.getUserByEmail(getUsername());
            if (user.getStatus().equals(GlobalConstants.ACTIVE)) {
                //request.login(this.getUsername(), this.getPassword());
                // add the user to externalContext‘s session map if authenticated
                ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
                Map<String, Object> sessionMap = externalContext.getSessionMap();
                sessionMap.put("user", user);
                //JsfUtil.addSuccessMessage(JsfUtil.getStringFromBundle(BUNDLE, "Login_Success"));

                this.getAuthenticatedUser();

                if (isAdmin() && user.getStatus().equals(GlobalConstants.ACTIVE)) {
                    result = "/admin/index";
                } else if (!isAdmin() && user.getStatus().equals(GlobalConstants.ACTIVE)) {
                    result = "/index";
                }
            } else {
                JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "Accont_Not_Activated"));
                // at this point logout the user and deny any access to the system
                this.logoutAnActivatedUser();
            }

        } catch (ServletException ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "Invalid_Credentials"));
            result = "login";
        }

        return result;

    }

    public String goToUserInforEditInput() {
        //return "/person/userInforEditInput";
        String result = null;
        try {
            this.getAuthenticatedUser();
            if (isAdmin()) {
                result = "/admin/administrator/userInforEditInput";
            } else if (!isAdmin()) {
                result = "/person/userInforEditInput";
            }
        } catch (Exception ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "Invalid_Credentials"));
            result = "/index";
        }
        return result;
    }

    public String goToPersonEdit() {

        return "/person/myAccountEdit";
    }

    public String goToPersonEditProfile() {
        String result = null;
        try {
            this.getAuthenticatedUser();
            if (isAdmin()) {
                result = "/admin/administrator/myAccountPlofile";
            } else if (!isAdmin()) {
                result = "/person/myAccountPlofile";
            }
        } catch (Exception ex) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "Invalid_Credentials"));
            result = "/index";
        }
        return result;
    }

    public String goToLoginPage() {
        return "/login";
    }

    public String goToChangePasswordPage() {
        return "changePassword";
    }

    public String goToTestUserIdPage() {
        return "/person/myAccountEdit";
    }

    public String goToIndexPage() {
        return "/index";
    }

    public String isLoggedInAsUserForwardHome() {
        if (!isAdmin()) {
            return HOME_PAGE_REDIRECT;
        }

        return null;
    }

    /**
     *
     * @return
     */
    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        try {
            this.user = null;
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            Map<String, Object> sessionMap = externalContext.getSessionMap();
            sessionMap.remove("user", user);

            request.logout();
            // clear the session
            ((HttpSession) context.getExternalContext().getSession(false)).invalidate();

            JsfUtil.addSuccessMessage(JsfUtil.getStringFromBundle(BUNDLE, "Logout_Success"));

        } catch (ServletException ex) {

            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "Logout_Failed"));
        } finally {
            return "/index";
        }
    }

    /**
     *
     * @return method for saving new password after user has changed their
     * password
     */
    public String savePassword() {

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String result = null;

        String InputUserName = this.getUsername();
        //String InputPassword = this.getPassword();

        try {
            Person newUser1 = getEjbFacade().getUserByEmail(InputUserName);

            System.out.print("for savePassword  " + newUser1);
            if (newUser1 != null) {
                getEjbFacade().updatePassword(InputUserName, this.getPassword());
                JsfUtil.addSuccessMessage(JsfUtil.getStringFromBundle(BUNDLE, "Password_Saved_Success"));
                return "login";
            }
            /*else {
                JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "EmailDoNotExists"));
            }*/

        } catch (EJBException e) {
            @SuppressWarnings("ThrowableResultIgnored")
            Exception cause = e.getCausedByException();
            if (cause instanceof ConstraintViolationException) {
                @SuppressWarnings("ThrowableResultIgnored")
                ConstraintViolationException cve = (ConstraintViolationException) e.getCausedByException();
                for (Iterator<ConstraintViolation<?>> it = cve.getConstraintViolations().iterator(); it.hasNext();) {
                    ConstraintViolation<? extends Object> v = it.next();
                    System.err.println(v);
                    System.err.println("==>>" + v.getMessage());
                }
            }
            System.out.print(e);
        }
        return null;
    }

    /**
     *
     * @return Method for editing user personal data called in myAccountEdit
     * facelet
     */
    public String changeUserPersonalDetails() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        String InputUserName = this.getAuthenticatedUser().getEmail();
        //firstname = this.getFirstname();

        try {
            Person editPerson = getEjbFacade().getUserByEmail(InputUserName);
            //System.out.print("Id for person being edited is " +newPerson);
            if (editPerson != null) {
                getEjbFacade().editUserPeronalData(InputUserName, user.getFirstname(), this.getLastname(), this.getAddress(), this.getCity());
                JsfUtil.addSuccessMessage(JsfUtil.getStringFromBundle(BUNDLE, "PersonalInformationUpdated"));
            }
        } catch (EJBException e) {
            @SuppressWarnings("ThrowableResultIgnored")
            Exception cause = e.getCausedByException();
            if (cause instanceof ConstraintViolationException) {
                @SuppressWarnings("ThrowableResultIgnored")
                ConstraintViolationException cve = (ConstraintViolationException) e.getCausedByException();
                for (Iterator<ConstraintViolation<?>> it = cve.getConstraintViolations().iterator(); it.hasNext();) {
                    ConstraintViolation<? extends Object> v = it.next();
                    //System.err.println(v);
                    //System.err.println("==>>" + v.getMessage());
                }
            }
            // this message will be displayed if admin attempts to change passward
            JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "AdminCan'tChangePassword"));
            //System.out.print(e);
        }
        return null; //this.goToIndexPage();
    }

    public String updateProfile() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        String InputUserName = this.getAuthenticatedUser().getEmail();
        try {
            customerController.upload();
            //System.out.print("this ia the image uploading now " +user.getId());
            //current.setPassword(MD5Util.generateMD5(password));
            //getEjbFacade().editUserProfileImage(InputUserName, user.getAvatar());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("CustomerUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
            return null;
        }
    }

    /**
     *
     * @return @throws MessagingException method for sending a link to user for
     * changing password
     * @throws javax.mail.internet.AddressException
     * @throws java.io.IOException this method is not in use. Method has been
     * relocated to customerController class
     */
    public String changeUserPassword() throws MessagingException, AddressException, IOException {

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        //String result = null;

        String IputUserName = this.getUsername();

        try {
            Person newUser = getEjbFacade().getUserByEmail(IputUserName);
            //System.out.print("üser in changeUserPassword " +newUser);
            if (newUser != null) {
                String hash = Utils.prepareRandomString(30);
                getEjbFacade().updateEmailVerificationHashForResetPassword(IputUserName, BCrypt.hashpw(hash, GlobalConstants.SALT));
                MailUtil.sendResetPasswordLink(newUser.getId() + "", IputUserName, hash);
                JsfUtil.addSuccessMessage(JsfUtil.getStringFromBundle(BUNDLE, "ResetPassword"));
                return "login";
            } else {
                JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "EmailDoNotExist"));
            }

        } catch (EJBException e) {
            @SuppressWarnings("ThrowableResultIgnored")
            Exception cause = e.getCausedByException();
            if (cause instanceof ConstraintViolationException) {
                @SuppressWarnings("ThrowableResultIgnored")
                ConstraintViolationException cve = (ConstraintViolationException) e.getCausedByException();
                for (Iterator<ConstraintViolation<?>> it = cve.getConstraintViolations().iterator(); it.hasNext();) {
                    ConstraintViolation<? extends Object> v = it.next();
                    //System.err.println(v);
                    //System.err.println("==>>" + v.getMessaggetAuthenticatedUsere());
                }
            }
            // this message will be displayed if admin attempts to change passward
            JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "AdminCan'tChangePassword"));
            //System.out.print(e);
        }
        return null;
    }

    // method for testing how to find user by id for editing 
    // delete this method
    public String testUserIdInput() throws MessagingException, AddressException, IOException {

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        //String result = null;

        Integer IputUserId = this.getUserId();

        try {
            Person newUser = getEjbFacade().getUserById(IputUserId);
            System.out.print("üser in changeUserPassword " + newUser);
            if (newUser != null) {
                String hash = Utils.prepareRandomString(30);
                //getEjbFacade().updateEmailVerificationHashForResetPassword(IputUserName, BCrypt.hashpw(hash, GlobalConstants.SALT));
                //MailUtil.sendResetPasswordLink(newUser.getId() + "", IputUserName, hash);
                JsfUtil.addSuccessMessage(JsfUtil.getStringFromBundle(BUNDLE, "ResetPassword"));
                return "login";
            } else {
                JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "EmailDoNotExist"));
            }

        } catch (EJBException e) {
            @SuppressWarnings("ThrowableResultIgnored")
            Exception cause = e.getCausedByException();
            if (cause instanceof ConstraintViolationException) {
                @SuppressWarnings("ThrowableResultIgnored")
                ConstraintViolationException cve = (ConstraintViolationException) e.getCausedByException();
                for (Iterator<ConstraintViolation<?>> it = cve.getConstraintViolations().iterator(); it.hasNext();) {
                    ConstraintViolation<? extends Object> v = it.next();
                    //System.err.println(v);
                    //System.err.println("==>>" + v.getMessage());
                }
            }
            // this message will be displayed if admin attempts to change passward
            JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "AdminCan'tChangePassword"));
            //System.out.print(e);
        }
        return null;
    }

    /**
     *
     * @return @throws MessagingException
     * @throws AddressException
     * @throws IOException method for editing user personal details not in use
     */
    public String changeUserDetails() throws MessagingException, AddressException, IOException {

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        //String result = null;

        String InputUserName = this.getUsername();
        String InputFirstName = this.getFirstname();
        try {
            Person newUser1 = getEjbFacade().getUserByEmail(InputUserName);
            System.out.print("üser in changeUserPassword " + newUser1);

            if (newUser1 != null) {
                getEjbFacade().updateUserDetails(InputUserName, this.getPassword());
                JsfUtil.addSuccessMessage(JsfUtil.getStringFromBundle(BUNDLE, "User_Updated_Success"));
                return null;
            } else {
                JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "EmailDoNotExists"));
            }

        } catch (EJBException e) {
            @SuppressWarnings("ThrowableResultIgnored")
            Exception cause = e.getCausedByException();
            if (cause instanceof ConstraintViolationException) {
                @SuppressWarnings("ThrowableResultIgnored")
                ConstraintViolationException cve = (ConstraintViolationException) e.getCausedByException();
                for (Iterator<ConstraintViolation<?>> it = cve.getConstraintViolations().iterator(); it.hasNext();) {
                    ConstraintViolation<? extends Object> v = it.next();
                    System.err.println(v);
                    System.err.println("==>>" + v.getMessage());
                }
            }
            System.out.print(e);
        }
        return null;
    }

    /**
     *
     * @return method for login out an activated user because the system logs
     * then in anyway
     */
    public String logoutAnActivatedUser() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        try {
            this.user = null;

            request.logout();
            // clear the session
            ((HttpSession) context.getExternalContext().getSession(false)).invalidate();

            //JsfUtil.addSuccessMessage(JsfUtil.getStringFromBundle(BUNDLE, "Logout_Success"));
        } catch (ServletException ex) {

            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage(JsfUtil.getStringFromBundle(BUNDLE, "Logout_Failed"));
        } finally {
            return null;
        }
    }

    /**
     * @return the ejbFacade
     */
    public com.soekm.ejb.UserBean getEjbFacade() {
        return getEjbFacade;
    }

    public com.soekm.ejb.PersonFacade getPersonFacade() {
        return getPersonFacade;
    }

    public @Produces
    @LoggedIn
    Person getAuthenticatedUser() {
        return user;
    }
// may need deleting

    public Person getSelected() {
        if (user == null) {
            user = new Person();
            selectedItemIndex = -1;
        }
        return user;
    }

    // delete this
    public String update() {
        try {
            //customerController.upload();
            //current.setPassword(MD5Util.generateMD5(password));
            getPersonFacade().edit(user);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("ProfileUpdated"));
            return goToPersonEdit();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public boolean isLogged() {
        return (getUser() == null) ? false : true;
    }

    public boolean isAdmin() {
        for (Groups g : user.getGroupsList()) {
            if (g.getName().equals("ADMINS")) {
                return true;
            }
        }
        return false;
    }

    public String goAdmin() {
        if (isAdmin()) {
            return "/admin/index";
        } else {
            return "index";
        }
    }

    public Part getFilePart() {
        return filePart;
    }

    public void setFilePart(Part filePart) {
        this.filePart = filePart;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return the person id
     */
    public Integer getUserId() {
        return personId;
    }

    /**
     *
     * @param personId
     */
    public void setUserId(Integer personId) {
        this.personId = personId;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getFirstname() {
        return user.getFirstname();
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return user.getLastname();
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getAddress() {
        return user.getAddress();
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return user.getCity();
    }

    public void setCity(String city) {
        this.city = city;
    }

    public byte[] getAvatar() {
        return user.getAvatar();
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    public String getImage() {
        return user.getImage();
    }

    public void setImage(String image) {
        this.image = image;
    }

    /**
     * @return the user
     */
    public Person getUser() {
        return user;
    }
}
