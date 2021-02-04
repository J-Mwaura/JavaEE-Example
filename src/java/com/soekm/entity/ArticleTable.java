/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author atjkm
 */
@Entity
@Table(name = "article_table")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ArticleTable.findAll", query = "SELECT a FROM ArticleTable a")
    , @NamedQuery(name = "ArticleTable.findByArticleiId", query = "SELECT a FROM ArticleTable a WHERE a.articleiId = :articleiId")
    , @NamedQuery(name = "ArticleTable.findByArticleTitle", query = "SELECT a FROM ArticleTable a WHERE a.articleTitle = :articleTitle")
    , @NamedQuery(name = "ArticleTable.findByArticlePreface", query = "SELECT a FROM ArticleTable a WHERE a.articlePreface = :articlePreface")
    , @NamedQuery(name = "ArticleTable.findByViews", query = "SELECT a FROM ArticleTable a WHERE a.views = :views")
    , @NamedQuery(name = "ArticleTable.findByImage", query = "SELECT a FROM ArticleTable a WHERE a.image = :image")
    , @NamedQuery(name = "ArticleTable.findByDateCreated", query = "SELECT a FROM ArticleTable a WHERE a.dateCreated = :dateCreated")
    , @NamedQuery(name = "ArticleTable.findByDateUpdated", query = "SELECT a FROM ArticleTable a WHERE a.dateUpdated = :dateUpdated")})
public class ArticleTable implements Serializable {

    @Lob
    @Column(name = "IMAGE_MAIN")
    private byte[] imageMain;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    //@NotNull
    @Column(name = "ARTICLEI_ID")
    private Integer articleiId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 300)
    @Column(name = "ARTICLE_TITLE")
    private String articleTitle;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 300)
    @Column(name = "ARTICLE_PREFACE")
    private String articlePreface;
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 2147483647)
    @Column(name = "ARTICLE_BODY")
    private String articleBody;
    @Column(name = "VIEWS")
    private Integer views;
    @Size(max = 45)
    @Column(name = "IMAGE")
    private String image;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DATE_CREATED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated = new Date();
    @Basic(optional = false)
    @NotNull
    @Column(name = "DATE_UPDATED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateUpdated = new Date();
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articleiId")
    private List<CommentTable> commentTableList;
    @JoinColumn(name = "PERSON_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Person personId;
    @JoinColumn(name = "ARTICLECATEGORY_ID", referencedColumnName = "ARTICLECATEGORY_ID")
    @ManyToOne(optional = false)
    private ArticlecategoryTable articlecategoryId;

    public ArticleTable() {
    }

    public ArticleTable(Integer articleiId) {
        this.articleiId = articleiId;
    }

    public ArticleTable(Integer articleiId, String articleTitle, String articlePreface, String articleBody, Date dateCreated, Date dateUpdated) {
        this.articleiId = articleiId;
        this.articleTitle = articleTitle;
        this.articlePreface = articlePreface;
        this.articleBody = articleBody;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
    }

    public Integer getArticleiId() {
        return articleiId;
    }

    public void setArticleiId(Integer articleiId) {
        this.articleiId = articleiId;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public String getArticlePreface() {
        return articlePreface;
    }

    public void setArticlePreface(String articlePreface) {
        this.articlePreface = articlePreface;
    }

    public String getArticleBody() {
        return articleBody;
    }

    public void setArticleBody(String articleBody) {
        this.articleBody = articleBody;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public byte[] getImageMain() {
        return imageMain;
    }

    public void setImageMain(byte[] imageMain) {
        this.imageMain = imageMain;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @XmlTransient
    public List<CommentTable> getCommentTableList() {
        return commentTableList;
    }

    public void setCommentTableList(List<CommentTable> commentTableList) {
        this.commentTableList = commentTableList;
    }

    public Person getPersonId() {
        return personId;
    }

    public void setPersonId(Person personId) {
        this.personId = personId;
    }

    public ArticlecategoryTable getArticlecategoryId() {
        return articlecategoryId;
    }

    public void setArticlecategoryId(ArticlecategoryTable articlecategoryId) {
        this.articlecategoryId = articlecategoryId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (articleiId != null ? articleiId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ArticleTable)) {
            return false;
        }
        ArticleTable other = (ArticleTable) object;
        if ((this.articleiId == null && other.articleiId != null) || (this.articleiId != null && !this.articleiId.equals(other.articleiId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.soekm.entity.ArticleTable[ articleiId=" + articleiId + " ]";
    }
    
}
