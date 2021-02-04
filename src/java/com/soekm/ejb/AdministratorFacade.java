/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.ejb;

import com.soekm.entity.Administrator;
import com.soekm.entity.Groups;
import com.soekm.entity.Person;
import com.soekm.web.util.MD5Util;
import java.util.Iterator;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 *
 * @author atjkm
 */
@Stateless
public class AdministratorFacade extends AbstractFacade<Administrator> {

    @PersistenceContext(unitName = "soekm2018PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AdministratorFacade() {
        super(Administrator.class);
    }

    Person person;

    /**
     *
     * @param admin
     */
    @Override
    public void create(Administrator admin) {
        try {
            Groups adminGroup = (Groups) em.createNamedQuery("Groups.findByName")
                    .setParameter("name", "ADMINS")
                    .getSingleResult();
            admin.getGroupsList().add(adminGroup);
            adminGroup.getPersonList().add(admin);
            em.persist(admin);
            em.merge(adminGroup);
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

    /*@Override
    public void remove(Administrator admin) {
        Groups adminGroup = (Groups) em.createNamedQuery("Groups.findByName")
                .setParameter("name", "Administrator")
                .getSingleResult();
        adminGroup.getPersonList().remove(admin);
        em.remove(em.merge(admin));
        em.merge(adminGroup);
    }*/

    public void editDetails(String email, String password) {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String result = null;
        
        try {
            this.person = this.getUserByEmail(email);
            Administrator administrator = new Administrator();
            
            //person.setEmail_verification_attempts(0); // this colum has no significance in the user row
            person = em.find(Administrator.class, this.getUserByEmail(email).getId());
            Administrator managedAdmin = em.merge(administrator);
            em.persist(administrator);
            managedAdmin.setPassword(MD5Util.generateMD5(password));
            //managedAdmin.setStatus(GlobalConstants.ACTIVE);
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

}
