/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.ejb;

import com.soekm.entity.Customer;
import com.soekm.entity.Groups;
import com.soekm.web.util.JsfUtil;
import java.util.ResourceBundle;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author atjkm
 */
@Stateless
public class CustomerFacade extends AbstractFacade<Customer> {
    private static final String BUNDLE = "bundles.Bundle";

    @PersistenceContext(unitName = "soekm2018PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CustomerFacade() {
        super(Customer.class);
    }
    
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
                JsfUtil.addErrorMessage(ResourceBundle.getBundle(BUNDLE).getString("NoGroupInDatabase"));
        }
    }
    
}
