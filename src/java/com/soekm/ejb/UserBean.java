package com.soekm.ejb;

import com.soekm.entity.Customer;
import com.soekm.entity.Groups;
import com.soekm.entity.Person;
import com.soekm.web.UserController;
import com.soekm.web.util.BCrypt;
import com.soekm.web.util.GlobalConstants;
import com.soekm.web.util.JsfUtil;
import com.soekm.web.util.MD5Util;
import com.soekm.web.util.MailUtil;
import com.soekm.web.util.Utils;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@Stateless
public class UserBean extends AbstractFacade<Customer> {
    
    @PersistenceContext(name = "soekm2018PU")
    private EntityManager em;
    
    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    Person user;

    // token will be valid for 24 hours
    private static final int EXPIRATION = 60 * 24;
    
    private final Date expiryDate = calculateExpiryDate(EXPIRATION);
    
    private Date calculateExpiryDate(final int expiryTimeInMinutes) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    /**
     * Create a new user verifying if the user already exists TODO: Create
     * custom exceptions ?
     *
     * @param customer
     * @return
     * @throws com.soekm.exception.EmailExistsException
     * method needs improving because getUserByEmail returns null
     */
    public boolean createUser(Customer customer) {

        // check if user exists
        if (getUserByEmail(customer.getEmail()) == null) {
            Query createNamedQuery = getEntityManager().createNamedQuery("Groups.findByName");
            createNamedQuery.setParameter("name", "USERS");
            if (createNamedQuery.getResultList().size() > 0) {
                customer.getGroupsList().add( (Groups)createNamedQuery.getSingleResult());
                super.create(customer);
                    return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    /**
     *
     * @param customer
     */
    @Override
    public void create(Customer customer) {
        try {
            Groups userGroup = (Groups) em.createNamedQuery("Groups.findByName")
                    .setParameter("name", "USERS")
                    .getSingleResult();
            customer.getGroupsList().add(userGroup);
            userGroup.getPersonList().add(customer);
            em.persist(customer);
            em.merge(userGroup);
        } catch (Exception e) {
            System.out.print(e);
        }
    }

    /**
     *
     * @param email
     * @return
     */
    public Person getUserByEmail(String email) {
        Query createNamedQuery = getEntityManager().createNamedQuery("Person.findByEmail");
        
        createNamedQuery.setParameter("email", email);
        
        if (createNamedQuery.getResultList().size() > 0) {
            return (Person) createNamedQuery.getSingleResult();
        } else {
            return null;
        }
    }
    
    public Person getUserById(Integer id) {
        Query createNamedQuery = getEntityManager().createNamedQuery("Person.findById");
        
        createNamedQuery.setParameter("id", id);
        
        if (createNamedQuery.getResultList().size() > 0) {
            return (Person) createNamedQuery.getSingleResult();
        } else {
            return null;
        }
    }

    /**
     *
     * @param id
     * @param hash
     * @return method to find user using token and id parameters provided via
     * http
     */
    public boolean verifyEmailHash(Integer id, String hash) {
        boolean verified = false;
        Query createNamedQuery = getEntityManager().createNamedQuery("Customer.findToRegister");
        createNamedQuery.setParameter("token", hash)
                .setParameter("id", id);
        
        if (createNamedQuery.getResultList().size() > 0) {
            createNamedQuery.getSingleResult();
            verified = true;
        }
        return verified;
    }
    

    /**
     *
     * @param id
     * @throws EJBException
     * @throws MessagingException 
     * method for sending a token if user attempts to
     * verify account using an expired token
     * @throws java.io.IOException
     */
    public void generateNewToken(Integer id) throws EJBException, MessagingException, IOException {
        Query createNamedQuery = getEntityManager().createNamedQuery("Customer.findForNewToken");
        createNamedQuery.setParameter("id", id).getFirstResult();
        try {
            String hash = Utils.prepareRandomString(30);
            Customer customer = new Customer();
            customer.setEmail_verification_attempts(0);
            //em.persist(customer);
            customer = em.find(Customer.class, id);
            //em.persist(customer);
            Customer managedCustomer = em.merge(customer);
            em.persist(customer);
            // generate hash for token
            managedCustomer.setToken(BCrypt.hashpw(hash, GlobalConstants.SALT));
            managedCustomer.setStatus("new");
            //System.out.print(token);
            managedCustomer.setExpiryDate(expiryDate);
            managedCustomer.setEmail_verification_attempts(0);
            MailUtil.sendEmailRegistrationLink(customer.getId().toString(), customer.getEmail(), hash);
            em.flush();
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
        } catch (MessagingException e) {
            System.out.print(e);
        }
    }
    
    /**
     *
     * @param id
     * @throws EJBException
     * @throws MessagingException
     * @throws IOException
     * method for sending a new token if user clicks on a link with an expired token
     */
    public void generateNewTokenForRestPassowrod(Integer id) throws EJBException, MessagingException, IOException {
        Query createNamedQuery = getEntityManager().createNamedQuery("Customer.findForNewToken");
        createNamedQuery.setParameter("id", id).getFirstResult();
        try {
            String hash = Utils.prepareRandomString(30);
            Customer customer = new Customer();
            customer.setEmail_verification_attempts(0);
            //em.persist(customer);
            customer = em.find(Customer.class, id);
            //em.persist(customer);
            Customer managedCustomer = em.merge(customer);
            em.persist(customer);
            // generate hash for token
            managedCustomer.setToken(BCrypt.hashpw(hash, GlobalConstants.SALT));
            managedCustomer.setStatus("new");
            //System.out.print(token);
            managedCustomer.setExpiryDate(expiryDate);
            managedCustomer.setEmail_verification_attempts(0);
            MailUtil.sendResetPasswordLink(customer.getId().toString(), customer.getEmail(), hash);
            em.flush();
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
        } catch (MessagingException e) {
            System.out.print(e);
        }
    }

    /**
     *
     * @param id
     * @param token
     * @throws EJBException method used to delete the token once user is
     * verified
     */
    public void updateVerificationToken(Integer id, String token) throws EJBException {
        Query createNamedQuery = getEntityManager().createNamedQuery("Customer.findVerifyEmail");
        createNamedQuery.setParameter("id", id)
                .setParameter("token", token)
                .getFirstResult();
        try {
            Customer customer = new Customer();
            customer.setEmail_verification_attempts(0); // this colum has no significance in the user row
            customer = em.find(Customer.class, id);
            Customer managedCustomer = em.merge(customer);
            em.persist(customer);
            managedCustomer.setToken(token);
            System.out.print(token);
            managedCustomer.setEmail_verification_attempts(0); // this colum has no significance in the user row

            em.flush();
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
        }
    }
    
    /**
     *
     * @param email is the email entered by the user
     * @param password is the password entered by the user
     * this method is called in the savePassword() method of the UserController 
     * bean to persist the new password
     */
    public void updatePassword(String email, String password){
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String result = null;
        
        try {
            this.user = this.getUserByEmail(email);
            Customer customer = new Customer();
            customer.setEmail_verification_attempts(0); // this colum has no significance in the user row
            customer = em.find(Customer.class, this.getUserByEmail(email).getId());
            Customer managedCustomer = em.merge(customer);
            em.persist(customer);
            managedCustomer.setPassword(MD5Util.generateMD5(password));
            managedCustomer.setStatus(GlobalConstants.ACTIVE);
            //managedCustomer.setToken(null);

            em.flush();
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
        }
        
    }
    
    /**
     *
     * @param email
     * @param id
     * @param firstname
     * @param lastname
     * @param address
     * @param city
     * method for editing user details
     * preferred method
     */
    public void editUserPeronalData(String email, String firstname, String lastname, String address, String city){
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        //Integer id = null;
        try{
            this.user = this.getUserByEmail(email);
            Customer customer = new Customer();
            customer.setEmail_verification_attempts(0); // this colum has no significance in the user row
            customer = em.find(Customer.class, this.getUserByEmail(email).getId());
            Person managedCustomer = em.merge(customer);
            em.persist(customer);
            managedCustomer.setFirstname(firstname);
            managedCustomer.setLastname(lastname);
            managedCustomer.setAddress(address);
            managedCustomer.setCity(city);
        }catch (EJBException e) {
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
        }
    }
    
    /**
     *
     * @param email
     * @param avatar
     * method under construction for updating user profile image
     */
    public void editUserProfileImage(){
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        //Integer id = null;
        String email = null;
        try{
            this.user = this.getUserByEmail(email);
            Customer customer = new Customer();
            customer.setEmail_verification_attempts(0); // this colum has no significance in the user row
            customer = em.find(Customer.class, this.getUserByEmail(email).getId());
            Customer managedCustomer = em.merge(customer);
            
            em.persist(customer);
            managedCustomer.setEmail_verification_attempts(0);
        }catch (EJBException e) {
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
        }
    }
    
    public void updateUserDetails(String email, String password){
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String result = null;
        
        try {
            this.user = this.getUserByEmail(email);
            Customer customer = new Customer();
            customer.setEmail_verification_attempts(0); // this colum has no significance in the user row
            customer = em.find(Customer.class, this.getUserByEmail(email).getId());
            Person managedCustomer = em.merge(customer);
            em.persist(customer);
            //managedCustomer.setFirstname(firstName);
            managedCustomer.setPassword(MD5Util.generateMD5(password));
            managedCustomer.setStatus(GlobalConstants.ACTIVE);

            em.flush();
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
        }
        
    }
    
    /**
     *
     * @param email
     * @param hash
     * method used to save a reset password token and IN_RESET_PASSWORD token
     */
    public void updateEmailVerificationHashForResetPassword(String email,
			String hash){
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String result = null;
        try {
            this.user = this.getUserByEmail(email);
            Customer customer = new Customer();
            customer.setEmail_verification_attempts(0); // this colum has no significance in the user row
            customer = em.find(Customer.class, this.getUserByEmail(email).getId());
            Customer managedCustomer = em.merge(customer);
            em.persist(customer);
            managedCustomer.setToken(hash);
            managedCustomer.setStatus(GlobalConstants.IN_RESET_PASSWORD);

            em.flush();
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
        }
        
    }

    /**
     *
     * @param id
     * @param status
     * @throws EJBException method for updating status of a user from "new" to
     * "active"
     */
    public void updateStaus(Integer id, String status) throws EJBException {
        Query createNamedQuery = getEntityManager().createNamedQuery("Customer.findToUpdateStatus");
        createNamedQuery.setParameter("id", id)
                .setParameter("status", status)
                .getFirstResult();
        try {
            Customer customer = new Customer();
            customer.setStatus(status);
            //find customer by id 
            customer = em.find(Customer.class, id);
            em.persist(customer);
            Customer managedCustomer = em.merge(customer);
            managedCustomer.setStatus(status);
            
            em.flush();
            
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
    }
    
    public UserBean() {
        super(Customer.class);
    }
}
