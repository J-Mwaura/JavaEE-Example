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
@Table(name = "comment_table")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CommentTable.findAll", query = "SELECT c FROM CommentTable c")
    , @NamedQuery(name = "CommentTable.findByCommentId", query = "SELECT c FROM CommentTable c WHERE c.commentId = :commentId")
    , @NamedQuery(name = "CommentTable.findByCommentBody", query = "SELECT c FROM CommentTable c WHERE c.commentBody = :commentBody")
    , @NamedQuery(name = "CommentTable.findByDateCreated", query = "SELECT c FROM CommentTable c WHERE c.dateCreated = :dateCreated")
    , @NamedQuery(name = "CommentTable.findByUrl", query = "SELECT c FROM CommentTable c WHERE c.url = :url")})
public class CommentTable implements Serializable {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "commentId")
    private List<CommentReply> commentReplyList;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "COMMENT_ID")
    private Integer commentId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 800)
    @Column(name = "COMMENT_BODY")
    private String commentBody;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DATE_CREATED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated = new Date();
    @Size(max = 200)
    @Column(name = "URL")
    private String url;
    @JoinColumn(name = "PERSON_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Person personId;
    @JoinColumn(name = "ARTICLEI_ID", referencedColumnName = "ARTICLEI_ID")
    @ManyToOne(optional = false)
    private ArticleTable articleiId;
    /*@JoinColumn(name = "COMMENTSTATUS_ID", referencedColumnName = "COMMENTSTATUS_ID")
    @ManyToOne(optional = false)
    private CommentstatusTable commentstatusId;
    */

    public CommentTable() {
    }

    public CommentTable(Integer commentId) {
        this.commentId = commentId;
    }

    public CommentTable(Integer commentId, String commentBody, Date dateCreated) {
        this.commentId = commentId;
        this.commentBody = commentBody;
        this.dateCreated = dateCreated;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public String getCommentBody() {
        return commentBody;
    }

    public void setCommentBody(String commentBody) {
        this.commentBody = commentBody;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Person getPersonId() {
        return personId;
    }

    public void setPersonId(Person personId) {
        this.personId = personId;
    }

    public ArticleTable getArticleiId() {
        return articleiId;
    }

    public void setArticleiId(ArticleTable articleiId) {
        this.articleiId = articleiId;
    }
    
    /*
    public CommentstatusTable getCommentstatusId() {
        return commentstatusId;
    }

    public void setCommentstatusId(CommentstatusTable commentstatusId) {
        this.commentstatusId = commentstatusId;
    }*/

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (commentId != null ? commentId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CommentTable)) {
            return false;
        }
        CommentTable other = (CommentTable) object;
        if ((this.commentId == null && other.commentId != null) || (this.commentId != null && !this.commentId.equals(other.commentId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.soekm.entity.CommentTable[ commentId=" + commentId + " ]";
    }

    @XmlTransient
    public List<CommentReply> getCommentReplyList() {
        return commentReplyList;
    }

    public void setCommentReplyList(List<CommentReply> commentReplyList) {
        this.commentReplyList = commentReplyList;
    }
    
}
