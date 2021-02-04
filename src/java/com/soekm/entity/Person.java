/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author atjkm
 */
@Entity
@Table(name = "person")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Person.findAll", query = "SELECT p FROM Person p")
    , @NamedQuery(name = "Person.findById", query = "SELECT p FROM Person p WHERE p.id = :id")
    , @NamedQuery(name = "Person.findByFirstname", query = "SELECT p FROM Person p WHERE p.firstname = :firstname")
    , @NamedQuery(name = "Person.findByLastname", query = "SELECT p FROM Person p WHERE p.lastname = :lastname")
    , @NamedQuery(name = "Person.findByEmail", query = "SELECT p FROM Person p WHERE p.email = :email")
    , @NamedQuery(name = "Person.findByAddress", query = "SELECT p FROM Person p WHERE p.address = :address")
    , @NamedQuery(name = "Person.findByCity", query = "SELECT p FROM Person p WHERE p.city = :city")
    , @NamedQuery(name = "Person.findByPassword", query = "SELECT p FROM Person p WHERE p.password = :password")
    , @NamedQuery(name = "Person.findByDtype", query = "SELECT p FROM Person p WHERE p.dtype = :dtype")
    , @NamedQuery(name = "Person.findByImage", query = "SELECT p FROM Person p WHERE p.image = :image")
    , @NamedQuery(name = "Customer.findByExpiryDate", query = "SELECT c FROM Customer c WHERE c.expiryDate = :expiryDate")
    , @NamedQuery(name = "Person.findByStatus", query = "SELECT p FROM Person p WHERE p.status = :status")
    , @NamedQuery(name = "Person.findByDateRegistered", query = "SELECT p FROM Person p WHERE p.dateRegistered = :dateRegistered")})
public class Person implements Serializable, HttpSessionBindingListener  {

    //@EmbeddedId
    //protected com.soekm.entity.ProfilePK profilePK;
    
    @Basic(optional = false)
   // @NotNull
    @Column(name = "DATE_REGISTERED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateRegistered;
    
    // All logins.
    private static Map<Person, HttpSession> logins = new ConcurrentHashMap<>();

    //@EmbeddedId
    //protected com.soekm.entity.PersonPK personPK;
    @Column(name = "ENABLED")
    private Boolean enabled;

    // ptotected variables restricted to the sub class Customer
    protected static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    //@NotNull
    @Column(name = "ID")
    protected Integer id;
    @Basic(optional = false)
    //@NotNull
    //@Size(min = 1, max = 50)
    @Column(name = "FIRSTNAME")
    protected String firstname;
    @Basic(optional = false)
    //@NotNull
    //@Size(min = 1, max = 100)
    @Column(name = "LASTNAME")
    protected String lastname;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Basic(optional = false)
   // @NotNull
    //@Size(min = 1, max = 45)
    @Column(name = "EMAIL")
    protected String email;
    @Basic(optional = false)
    //@NotNull
    //@Size(min = 1, max = 45)
    @Column(name = "ADDRESS")
    protected String address;
    @Basic(optional = false)
    //@NotNull
    //@Size(min = 1, max = 45)
    @Column(name = "CITY")
    protected String city;
    //@Size(max = 100)
    @Column(name = "PASSWORD")
    protected String password;
    
    @Column(name = "SECRET")
    protected String secret;
    @Column(name = "EXPIRYDATE")
    @Temporal(javax.persistence.TemporalType.DATE)
    protected Date expiryDate;
    @Column(name = "TOKEN")
    protected String token;
    @Column(name = "EMAIL_VERIFICATION_ATTEMPTS")
    protected Integer email_verification_attempts;
    @Column(name = "EMAIL_VERIFICATION_HASH")
    protected String email_verification_hash;
    @Column(name = "STATUS")
    protected String status;
    //@Size(max = 31)
    @Column(name = "DTYPE")
    protected String dtype;
    @Lob
    @Column(name = "AVATAR")
    protected byte[] avatar;
    //@Size(max = 45)
    @Column(name = "IMAGE", length=45)
    protected String image;
    
    @JoinTable(name = "PERSON_GROUPS", joinColumns = {
        @JoinColumn(name = "EMAIL", referencedColumnName = "EMAIL")}, inverseJoinColumns = {
        @JoinColumn(name = "GROUPS_ID", referencedColumnName = "ID")})
    //@ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER )
    @ManyToMany(mappedBy = "personList")//, cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    protected List<Groups> groupsList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "personId")
    protected List<CommentTable> commentTableList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "personId")
    protected List<ArticleTable> articleTableList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "personId")
    //private List<Profile> profileList;

    // token will be valid for 24 hours
    private static final int EXPIRATION = 60 * 24;
    public Person() {
        this.groupsList = new ArrayList<Groups>();
    }

    public Person(Integer id) {
        this.id = id;
        this.groupsList = new ArrayList<Groups>();
    }

    public Person(Integer id, String firstname, String lastname, String email, String address, String city, String token, String Status, Date dateRegistered) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.address = address;
        this.city = city;
        this.enabled = false;
        //this.expiryDate = calculateExpiryDate(EXPIRATION);
        this.token = token;
        this.status = "new";
        this.dateRegistered = dateRegistered;
        this.groupsList = new ArrayList<Groups>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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


    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getEmail_verification_attempts() {
        return email_verification_attempts;
    }

    public void setEmail_verification_attempts(Integer email_verification_attempts) {
        this.email_verification_attempts = email_verification_attempts;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    /**
     * Add XmlTransient annotation to this field for security reasons. 
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }
    
    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @XmlTransient
    public List<Groups> getGroupsList() {
        return groupsList;
    }

    public void setGroupsList(List<Groups> groupsList) {
        this.groupsList = groupsList;
    }

    @XmlTransient
    public List<CommentTable> getCommentTableList() {
        return commentTableList;
    }

    public void setCommentTableList(List<CommentTable> commentTableList) {
        this.commentTableList = commentTableList;
    }

    @XmlTransient
    public List<ArticleTable> getArticleTableList() {
        return articleTableList;
    }

    public void setArticleTableList(List<ArticleTable> articleTableList) {
        this.articleTableList = articleTableList;
    }
    
    /*@XmlTransient
    public List<Profile> getProfileList() {
        return profileList;
    }

    public void setProfileList(List<Profile> profileList) {
        this.profileList = profileList;
    }*/
    
    private Date calculateExpiryDate(final int expiryTimeInMinutes) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
    
    public void updateToken(final String token) {
        this.token = token;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Person)) {
            return false;
        }
        Person other = (Person) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.soekm.entity.Person[ id=" + id + " ]";
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    
    /**
     *
     * @param event
     * the following two methods are for invalidating sessions when a user tries to 
     * login twice in deferent locations e.g. using two browsers, two computers etc.
     * using HttpSessionBindingListener 
     */
    @Override
    public void valueBound(HttpSessionBindingEvent event) {
        HttpSession session = logins.remove(this);
        if (session != null) {
            session.invalidate();
        }
        logins.put(this, event.getSession());
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        logins.remove(this);
    }

    /*public com.soekm.entity.ProfilePK getProfilePK() {
        return profilePK;
    }

    public void setCustomerPK(com.soekm.entity.ProfilePK profilePK) {
        this.profilePK = profilePK;
    }*/

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }
    
}
