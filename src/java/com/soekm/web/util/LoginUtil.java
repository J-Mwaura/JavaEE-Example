/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.web.util;

import com.soekm.ejb.UserBean;
import com.soekm.entity.Person;
import java.util.Calendar;
import java.util.Date;
import javax.ejb.EJB;

/**
 *
 * @author atjkm
 */
public class LoginUtil {
    
    // token will be valid for 24 hours
    private static final int EXPIRATION = 60 * 24;
    
    @EJB
    private com.soekm.ejb.UserBean userBeanFacade;
    

    public UserBean getUserBeanFacade() {
        return userBeanFacade;
    }
    
    public boolean isUserDuplicated(Person p) {
        return (getUserBeanFacade().getUserByEmail(p.getEmail()) == null) ? false : true;
    }
    
    public Date expiryDate = calculateExpiryDate(EXPIRATION);
    
    public Date calculateExpiryDate(final int expiryTimeInMinutes) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
    
}
