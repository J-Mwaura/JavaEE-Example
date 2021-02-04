/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.ejb;

import com.soekm.entity.CommentTable;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author atjkm
 */
@Stateless
public class CommentTableFacade extends AbstractFacade<CommentTable> {

    @PersistenceContext(unitName = "soekm2018PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CommentTableFacade() {
        super(CommentTable.class);
    }
    
}
