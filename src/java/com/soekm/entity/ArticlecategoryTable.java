/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author atjkm
 */
@Entity
@Table(name = "articlecategory_table")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ArticlecategoryTable.findAll", query = "SELECT a FROM ArticlecategoryTable a")
    , @NamedQuery(name = "ArticlecategoryTable.findByArticlecategoryId", query = "SELECT a FROM ArticlecategoryTable a WHERE a.articlecategoryId = :articlecategoryId")
    , @NamedQuery(name = "ArticlecategoryTable.findByArticlecategoryName", query = "SELECT a FROM ArticlecategoryTable a WHERE a.articlecategoryName = :articlecategoryName")})
public class ArticlecategoryTable implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ARTICLECATEGORY_ID")
    private Integer articlecategoryId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "ARTICLECATEGORY_NAME")
    private String articlecategoryName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articlecategoryId")
    private List<ArticleTable> articleTableList;

    public ArticlecategoryTable() {
    }

    public ArticlecategoryTable(Integer articlecategoryId) {
        this.articlecategoryId = articlecategoryId;
    }

    public ArticlecategoryTable(Integer articlecategoryId, String articlecategoryName) {
        this.articlecategoryId = articlecategoryId;
        this.articlecategoryName = articlecategoryName;
    }

    public Integer getArticlecategoryId() {
        return articlecategoryId;
    }

    public void setArticlecategoryId(Integer articlecategoryId) {
        this.articlecategoryId = articlecategoryId;
    }

    public String getArticlecategoryName() {
        return articlecategoryName;
    }

    public void setArticlecategoryName(String articlecategoryName) {
        this.articlecategoryName = articlecategoryName;
    }

    @XmlTransient
    public List<ArticleTable> getArticleTableList() {
        return articleTableList;
    }

    public void setArticleTableList(List<ArticleTable> articleTableList) {
        this.articleTableList = articleTableList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (articlecategoryId != null ? articlecategoryId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ArticlecategoryTable)) {
            return false;
        }
        ArticlecategoryTable other = (ArticlecategoryTable) object;
        if ((this.articlecategoryId == null && other.articlecategoryId != null) || (this.articlecategoryId != null && !this.articlecategoryId.equals(other.articlecategoryId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.soekm.entity.ArticlecategoryTable[ articlecategoryId=" + articlecategoryId + " ]";
    }
    
}
