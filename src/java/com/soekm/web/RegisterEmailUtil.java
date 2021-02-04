/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.web;

import com.soekm.web.util.BCrypt;
import com.soekm.web.util.GlobalConstants;
import java.io.Serializable;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.SessionScoped;

/**
 *
 * @author atjkm
 */
@Named(value = "registerEmailUtil")
@SessionScoped
public class RegisterEmailUtil implements Serializable {

    private Integer userId;
    private String hash;
    private String scope;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = BCrypt.hashpw(hash, GlobalConstants.SALT);
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
    
    
    /**
     * Creates a new instance of RegisterEmailUtil
     */
    public RegisterEmailUtil() {
    }
    
}
