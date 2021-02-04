/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.ejb;

import com.soekm.entity.ArticleTable;
import com.soekm.entity.ArticlecategoryTable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author atjkm
 */
@Stateless
public class ArticleTableFacade extends AbstractFacade<ArticleTable> {
    
    private static final Logger logger = 
            Logger.getLogger(ArticleTableFacade.class.getCanonicalName());

    @PersistenceContext(unitName = "soekm2018PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ArticleTableFacade() {
        super(ArticleTable.class);
    }
    
    public ArticleTable dofindArticleById(int id){
        return em.find(ArticleTable.class, id);
    }
    
    /**
     * Use JPA CriteriaBuilder to get all items belonging to a category
     * @param range
     * @param categoryId
     * @return
     */
    public List<ArticleTable> findByCategory(int[] range, int categoryId) {       
         ArticlecategoryTable cat = new ArticlecategoryTable();
         cat.setArticlecategoryId(categoryId);
         
         CriteriaBuilder qb = em.getCriteriaBuilder();
         CriteriaQuery<ArticleTable> query = qb.createQuery(ArticleTable.class);
         Root<ArticleTable> article = query.from(ArticleTable.class);
         query.where(qb.equal(article.get("articlecategoryId"), cat));

         List<ArticleTable> result = this.findRange(range, query);
         
         logger.log(Level.FINEST, "Article List size: {0}", result.size());
         
        return result;
    }
    
}
