package com.soekm.web;

import com.soekm.ejb.CustomerFacade;
import com.soekm.ejb.PersonFacade;
import javax.mail.internet.InternetAddress;
import javax.mail.Session;
import javax.mail.Transport;
import com.soekm.entity.Customer;
import com.soekm.web.util.JsfUtil;
import com.soekm.web.util.PaginationHelper;
import com.soekm.ejb.UserBean;
import com.soekm.entity.Person;
import com.soekm.entity.Status;
import com.soekm.web.util.BCrypt;
import com.soekm.web.util.GlobalConstants;
import com.soekm.web.util.LoginUtil;
import com.soekm.web.util.MD5Util;
import com.soekm.web.util.MailUtil;
import com.soekm.web.util.PageNavigation;
import com.soekm.web.util.Utils;
import java.io.IOException;
import java.io.InputStream;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.ejb.EJBException;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.validation.ConstraintViolationException;

import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.servlet.http.Part;
import javax.validation.ConstraintViolation;

@Named("customerController")
@SessionScoped
public class CustomerController implements Serializable {

    private final static Logger LOGGER = Logger.getLogger(CustomerController.class.getCanonicalName());
    private static final String BUNDLE = "bundles.Bundle";
    Transport transport = null;
    Properties props = System.getProperties();
    InternetAddress[] addrs = null;
    InternetAddress from;

    // create some properties and get a Session
    Session session = Session.getInstance(props, null);

    Person user;
    private Customer current;
    private Status status;
    private DataModel items = null;
    @EJB
    private com.soekm.ejb.PersonFacade peronFacade;
    @EJB
    private com.soekm.ejb.UserBean userBeanFacade;
    @EJB
    private com.soekm.ejb.CustomerFacade customerFacade;
    @Inject
    private com.soekm.web.UserController userController;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private Part filePart;
    private String output;
    private String password;
    private LoginUtil loginUtil;
    private String username;
    private String firstname;
    private String lastname;
    private String address;
    private Integer personId;
    private String city;
    private String passwordConfirm;
    private String newPassword;
    private byte[] avatar;
    private String image;
    private String selectedArticleId = null;
    /*@Inject
    transient private VerificationTokenRepositoryImpl tokenRepository;
    @Inject
    transient private PasswordResetTokenRepositoryImpl passwordTokenRepository;*/

    public static final String TOKEN_INVALID = "invalidToken";
    public static final String TOKEN_EXPIRED = "expired";
    public static final String TOKEN_VALID = "valid";

    private static final List<String> EXTENSIONS_ALLOWED = new ArrayList<>();

    static String msgText = "This is a message body.\nHere's the second line.";
    static String msgText2 = "\nThis was sent by transport.java demo program.";

    // token will be valid for 24 hours
    private static final int EXPIRATION = 60 * 24;

    private Date expiryDate = calculateExpiryDate(EXPIRATION);

    private Date calculateExpiryDate(final int expiryTimeInMinutes) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

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
    
    public String goToSignUpPage() {
        return "/Create";
    }
    
    public String forFirstRate(){
        return "firstRateContact";
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

//            Integer id = current.getId();
//            current = ejbFacade.find(2);
            current.setAvatar(b);
            current.setImage(fileName);
            userBeanFacade.edit(current);
            //setStep(3);
            JsfUtil.addSuccessMessage("Your image was successfuly uploaded!");

        } catch (IOException ex) {
            System.out.print(ex);
        }
    }

    public void validateFile(FacesContext ctx,
            UIComponent comp,
            Object value) {
        List<FacesMessage> msgs = new ArrayList<FacesMessage>();
        Part file = (Part) value;
        if (file.getSize() > 1024) {
            msgs.add(new FacesMessage("file too big"));
        }
        if (!"text/plain".equals(file.getContentType())) {
            msgs.add(new FacesMessage("not a text file"));
        }
        if (!msgs.isEmpty()) {
            throw new ValidatorException(msgs);
        }
    }

    /**
     * @return the filePart
     */
    public Part getFilePart() {
        return filePart;
    }

    /**
     * @param filePart the filePart to set
     */
    public void setFilePart(Part filePart) {
        this.filePart = filePart;
    }

    public CustomerController() {
    }

    public Customer getSelected() {
        if (current == null) {
            current = new Customer();
            selectedItemIndex = -1;
        }
        return current;
    }

    private UserBean getFacade() {
        return userBeanFacade;
    }

    public PersonFacade getPeronFacade() {
        return peronFacade;
    }

    public CustomerFacade getCustomerFacade() {
        return customerFacade;
    }

    public void setCustomerFacade(CustomerFacade customerFacade) {
        this.customerFacade = customerFacade;
    }

    public void setPeronFacade(PersonFacade peronFacade) {
        this.peronFacade = peronFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Customer) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Customer();
        selectedItemIndex = -1;
        return "Create";
    }

    private boolean isUserDuplicated(Person p) {
        return (getFacade().getUserByEmail(p.getEmail()) == null) ? false : true;
    }

    // add new person row to person table
    public PageNavigation create() {
        try {
            // check if email exists
            if (!isUserDuplicated(current)) {
                //upload();
                // default status is equal to new
                current.setStatus("new");
                current.setExpiryDate(expiryDate);
                // add user if email does not exist
                // password encrypt
                // generate hash for password
                //current.setPassword(BCrypt.hashpw(current.getPassword(),GlobalConstants.SALT));
                current.setPassword(MD5Util.generateMD5(current.getPassword()));
                // generate hash code for email verification
                String hash = Utils.prepareRandomString(30);

                // generate hash for password
                current.setToken(BCrypt.hashpw(hash, GlobalConstants.SALT));

                // save details to database
                getFacade().create(current);
                // send verification email
                MailUtil.sendEmailRegistrationLink(current.getId().toString(), current.getEmail(), hash);
                //status.setId(0);
                JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("CustomerCreatedAndEmailSent"));
                output = Utils.toJson(status);

            } else {
                //status.setId(-1);
                //output = Utils.toJson(status);
                JsfUtil.addErrorMessage(ResourceBundle.getBundle(BUNDLE).getString("EmailAlreadyExists"));
                //status.setId(-1);
                //output = Utils.toJson(status);
                return PageNavigation.CREATE;
            }
            //return null;
        }  catch (final ConstraintViolationException e) {
            
            //Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, e);
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("CustomerCreationError"));
            //System.out.print(e);
        } catch (MessagingException ex) {
            Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return PageNavigation.INDEX;
    }

    /*public PageNavigation create() {
        try {
            if (!isUserDuplicated(current)) {
                if (!isUserDuplicated(current)) {
                    // password encrypt
                    current.setPassword(MD5Util.generateMD5(current.getPassword()));
                    current.setStatus("new");
                    current.setExpiryDate(expiryDate);
                    // generate hash code for email verification
                    String hash = Utils.prepareRandomString(30);

                    // generate hash for password
                    current.setToken(BCrypt.hashpw(hash, GlobalConstants.SALT));
                    getCustomerFacade().create(current);
                    // send verification email
                    MailUtil.sendEmailRegistrationLink(current.getId().toString(), current.getEmail(), hash);
                    //status.setId(0);
                    JsfUtil.addSuccessMessage(
                            ResourceBundle.getBundle(BUNDLE).getString(
                                    "CustomerCreated"));
                } else {
                    JsfUtil.addErrorMessage(
                            ResourceBundle.getBundle(BUNDLE).getString(
                                    "DuplicatedCustomerId"));
                }

            } else {
                JsfUtil.addErrorMessage(
                        ResourceBundle.getBundle(BUNDLE).getString(
                                "DuplicatedCustomerError"));
            }

            //return prepareCreate();
            return PageNavigation.VIEW;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(
                    e,
                    ResourceBundle.getBundle(BUNDLE).getString(
                            "CustomerCreationError"));
            return null;
        }
    }*/
    
    public String prepareEdit() {
        current = (Customer) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    /*public String prepareEditAuthUser() {
        Person person = userController.getAuthenticatedUser();
        //person = (Customer) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }*/
    public String update() {
        try {
            //current.setPassword(MD5Util.generateMD5(password));
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("CustomerUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (Customer) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("CustomerDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(userBeanFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(userBeanFacade.findAll(), true);
    }

    public Customer getCustomer(java.lang.Integer id) {
        return userBeanFacade.find(id);
    }

    public void saveRegisteredUser(final Person user) {
        userBeanFacade.createUser(current);
    }

    public void deleteUser(final Person user) {

        getFacade().remove(current);
        //userRepository.delete(user);
    }

    @FacesConverter(forClass = Customer.class)
    public static class CustomerControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CustomerController controller = (CustomerController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "customerController");
            return controller.getCustomer(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Customer) {
                Customer o = (Customer) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Customer.class.getName());
            }
        }

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = user.getAvatar();
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /**
     *
     * @return @throws MessagingException method for sending a link to user for
     * changing password
     * @throws javax.mail.internet.AddressException
     * @throws java.io.IOException
     */
    public String changeUserPassword() throws MessagingException, AddressException, IOException {

        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        //String result = null;

        String IputUserName = this.getUsername();

        try {
            Person newUser = getFacade().getUserByEmail(IputUserName);
            //System.out.print("üser in changeUserPassword " +newUser);
            if (newUser != null) {
                String hash = Utils.prepareRandomString(30);
                getFacade().updateEmailVerificationHashForResetPassword(IputUserName, BCrypt.hashpw(hash, GlobalConstants.SALT));
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

}
