/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.ejb;

import com.soekm.entity.ArticlecategoryTable;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author atjkm
 */
@Stateless
public class ArticlecategoryTableFacade extends AbstractFacade<ArticlecategoryTable> {

    @PersistenceContext(unitName = "soekm2018PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ArticlecategoryTableFacade() {
        super(ArticlecategoryTable.class);
    }
    
}
