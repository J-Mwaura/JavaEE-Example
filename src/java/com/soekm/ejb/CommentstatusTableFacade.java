/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.ejb;

import com.soekm.entity.CommentstatusTable;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author atjkm
 */
@Stateless
public class CommentstatusTableFacade extends AbstractFacade<CommentstatusTable> {

    @PersistenceContext(unitName = "soekm2018PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CommentstatusTableFacade() {
        super(CommentstatusTable.class);
    }
    
}
