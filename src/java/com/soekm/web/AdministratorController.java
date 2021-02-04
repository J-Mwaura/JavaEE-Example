package com.soekm.web;

import com.soekm.entity.Administrator;
import com.soekm.entity.Status;
import com.soekm.web.util.JsfUtil;
import com.soekm.web.util.PaginationHelper;
import com.soekm.ejb.AdministratorFacade;
import com.soekm.ejb.UserBean;
import com.soekm.entity.Person;
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
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.Part;

@Named("administratorController")
@SessionScoped
public class AdministratorController implements Serializable {

    private final static Logger logger = Logger.getLogger(ArticleController.class.getCanonicalName());
    private static final String BUNDLE = "bundles.Bundle";

    Person user;
    private LoginUtil loginUtil;
    private Administrator current;
    private Status status;
    private DataModel items = null;
    @EJB
    private com.soekm.ejb.AdministratorFacade ejbFacade;
    @EJB
    private com.soekm.ejb.UserBean userBeanFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private Part filePart;
    private String output;
    private String username;
    private String password;
    
    protected String firstname;
    protected String lasstname;
    protected String email;
    protected String address;
    protected String city;
    private byte[] avatar;
    private String image;

    private static final List<String> EXTENSIONS_ALLOWED = new ArrayList<>();

    static {
        // images only
        EXTENSIONS_ALLOWED.add(".jpg");
        EXTENSIONS_ALLOWED.add(".bmp");
        EXTENSIONS_ALLOWED.add(".png");
        EXTENSIONS_ALLOWED.add(".gif");
    }
    
    // token will be valid for 24 hours
    private static final int EXPIRATION = 60 * 24;
    
    private Date expiryDate = calculateExpiryDate(EXPIRATION);
    
    private Date calculateExpiryDate(final int expiryTimeInMinutes) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    private String getFileName(Part part) {
        String partHeader = part.getHeader("content-disposition");
        logger.log(Level.INFO, "Part Header = {0}", partHeader);
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;

    }

    public void upload() {
        logger.info(getFilePart().getName());

        try {
            InputStream is = getFilePart().getInputStream();

            int i = is.available();
            byte[] b = new byte[i];
            is.read(b);

            logger.log(Level.INFO, "Length : {0}", b.length);
            String fileName = getFileName(getFilePart());
            logger.log(Level.INFO, "File name : {0}", fileName);

            // generate *unique* filename 
            final String extension = fileName.substring(fileName.length() - 4);

            if (!EXTENSIONS_ALLOWED.contains(extension)) {
                try {
                    logger.severe("User tried to upload file that's not an image. Upload cancelled.");
                    JsfUtil.addErrorMessage(new Exception("Error trying to upload file"),
                            ResourceBundle.getBundle(BUNDLE).getString("WrongFile"));
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
           ejbFacade.edit(current);
            //setStep(3);
            JsfUtil.addSuccessMessage("Article image successfuly uploaded!");

        } catch (Exception ex) {
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

    public AdministratorController() {
    }

    public Administrator getSelected() {
        if (current == null) {
            current = new Administrator();
            selectedItemIndex = -1;
        }
        return current;
    }

    private AdministratorFacade getFacade() {
        return ejbFacade;
    }

    public UserBean getUserBeanFacade() {
        return userBeanFacade;
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
        current = (Administrator) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Administrator();
        selectedItemIndex = -1;
        return "Create";
    }

    private boolean isUserDuplicated(Person p) {
        return (getUserBeanFacade().getUserByEmail(p.getEmail()) == null) ? false : true;
    }

    public PageNavigation create() {
        try {
            // check if email exists
            if (!isUserDuplicated(current)) {
                // default status is equal to active for administrator
                current.setStatus("active");
                current.setExpiryDate(expiryDate);
                // password encrypt
                current.setPassword(MD5Util.generateMD5(current.getPassword()));
                // generate hash code for email verification
                String hash = Utils.prepareRandomString(30);

                // generate hash for password
                current.setToken(BCrypt.hashpw(hash, GlobalConstants.SALT));
                getFacade().create(current);
                // send verification email
                MailUtil.sendEmailRegistrationLink(current.getId().toString(), current.getEmail(), hash);

                JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("AdminCreatedAndEmailSent"));
                output = Utils.toJson(status);
            } else {
                //status.setId(-1);
                //output = Utils.toJson(status);
                JsfUtil.addErrorMessage(ResourceBundle.getBundle(BUNDLE).getString("EmailAlreadyExists"));
                //status.setId(-1);
                //output = Utils.toJson(status);
            }
            //return prepareCreate();

        } catch (MessagingException e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
            System.out.print(e);
        }
        return PageNavigation.VIEW;
    }

    public String prepareEdit() {
        current = (Administrator) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }
       

    public Person updateAdminDetails() throws MessagingException, AddressException, IOException {
         try{
         //current.setEmail(email);
         current.setFirstname(firstname);
         current.setLastname(lasstname);
         current.setPassword(password);
         current.setAddress(address);
         current.setCity(city);
         
         } catch (Exception e) {
            Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, e);
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("CustomerCreationError"));
            //return null;
        }
        return null;
    }

    public String destroy() {
        current = (Administrator) getItems().getRowData();
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
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("AdministratorDeleted"));
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
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public Administrator getAdministrator(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = Administrator.class)
    public static class AdministratorControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AdministratorController controller = (AdministratorController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "administratorController");
            return controller.getAdministrator(getKey(value));
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
            if (object instanceof Administrator) {
                Administrator o = (Administrator) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Administrator.class.getName());
            }
        }

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
     
     public com.soekm.ejb.UserBean getEjbFacade() {
        return userBeanFacade;
    }

}
