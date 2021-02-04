/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author atjkm
 */
@Entity
@Table(name = "profile")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Profile.findAll", query = "SELECT p FROM Profile p")
    , @NamedQuery(name = "Profile.findById", query = "SELECT p FROM Profile p WHERE p.profilePK.id = :id")
    , @NamedQuery(name = "Profile.findByImg", query = "SELECT p FROM Profile p WHERE p.img = :img")
    , @NamedQuery(name = "Profile.findByPersonId", query = "SELECT p FROM Profile p WHERE p.profilePK.personId = :personId")
    , @NamedQuery(name = "Profile.findByEmail", query = "SELECT p FROM Profile p WHERE p.profilePK.email = :email")})
public class Profile implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ProfilePK profilePK;
    @Lob
    @Column(name = "AVATAR")
    private byte[] avatar;
    @Size(max = 45)
    @Column(name = "IMG")
    private String img;
    @JoinColumns({
        @JoinColumn(name = "PERSON_ID", referencedColumnName = "ID", insertable = false, updatable = false)
        , @JoinColumn(name = "EMAIL", referencedColumnName = "EMAIL", insertable = false, updatable = false)})
    @ManyToOne(optional = false)
    private Customer customer;

    public Profile() {
    }

    public Profile(ProfilePK profilePK) {
        this.profilePK = profilePK;
    }

    public Profile(int id, int personId, String email) {
        this.profilePK = new ProfilePK(id, personId, email);
    }

    public ProfilePK getProfilePK() {
        return profilePK;
    }

    public void setProfilePK(ProfilePK profilePK) {
        this.profilePK = profilePK;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (profilePK != null ? profilePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Profile)) {
            return false;
        }
        Profile other = (Profile) object;
        if ((this.profilePK == null && other.profilePK != null) || (this.profilePK != null && !this.profilePK.equals(other.profilePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.soekm.entity.Profile[ profilePK=" + profilePK + " ]";
    }
    
}
