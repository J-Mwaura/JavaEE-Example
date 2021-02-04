/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.entity;

import java.util.ArrayList;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author atjkm
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "Customer.findAll", query = "SELECT c FROM Customer c"),
    @NamedQuery(name = "Customer.findById", query = "SELECT c FROM Customer c WHERE c.id = :id"),
    @NamedQuery(name = "Customer.findByFirstname", query = "SELECT c FROM Customer c WHERE c.firstname = :firstname"),
    @NamedQuery(name = "Customer.findByLastname", query = "SELECT c FROM Customer c WHERE c.lastname = :lastname"),
    @NamedQuery(name = "Customer.findByEmail", query = "SELECT c FROM Customer c WHERE c.email = :email"),
    @NamedQuery(name = "Customer.findByAddress", query = "SELECT c FROM Customer c WHERE c.address = :address"),
    @NamedQuery(name = "Customer.findByCity", query = "SELECT c FROM Customer c WHERE c.city = :city"), 
    @NamedQuery(name = "Customer.findByExpiryDate", query = "SELECT c FROM Customer c WHERE c.expiryDate = :expiryDate"),
    @NamedQuery(name = "Customer.findToRegister", query = "SELECT c FROM Customer c WHERE c.id = :id and c.token = :token"),
    @NamedQuery(name = "Customer.findToUpdateStatus", query = "SELECT c from Customer c WHERE CAST(c.id AS CHAR) = :id AND c.status = :status"),
    @NamedQuery(name = "Customer.findVerifyEmail", query = "SELECT c from Customer c WHERE CAST(c.id AS CHAR) = :id AND c.token = :token"),
    @NamedQuery(name = "Customer.findToResetPassword", query = "SELECT c FROM Customer c WHERE c.email = :email AND c.token = :token"),
    @NamedQuery(name = "Customer.findForNewToken", query = "SELECT c from Customer c WHERE CAST(c.id AS CHAR) = :id"),
    @NamedQuery(name = "Customer.findByverificationAttempts", query = "SELECT c from Customer c WHERE c.email_verification_attempts = :verificationAttempts"),
    @NamedQuery(name = "Customer.IncrementVerificationCount", query = "UPDATE Customer c SET (c.email_verification_attempts = c.email_verification_attempts + 1) WHERE c.id = :id")})
//@NamedQuery(name = "Customer.findByStatus", query = "SELECT c FROM Person c WHERE c.status = :status")

public class Customer extends Person {

    private static final long serialVersionUID = 1L;
    
    public Customer() {
        this.groupsList = new ArrayList<Groups>();
    }
    
    public Customer(Integer id) {
        this.id = id;
        this.groupsList = new ArrayList<Groups>();
    }

    public Customer(Integer id, String firstname, String lastname, String email, String address, String city) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.address = address;
        this.city = city;
        this.groupsList = new ArrayList<Groups>();
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
        if (!(object instanceof Customer)) {
            return false;
        }
        Customer other = (Customer) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.soekm.entity.Customer[ id=" + id + " ]";
    }
    
}
