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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author atjkm
 */
@Entity
@Table(name = "commentstatus_table")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CommentstatusTable.findAll", query = "SELECT c FROM CommentstatusTable c")
    , @NamedQuery(name = "CommentstatusTable.findByCommentstatusId", query = "SELECT c FROM CommentstatusTable c WHERE c.commentstatusId = :commentstatusId")
    , @NamedQuery(name = "CommentstatusTable.findByCommentStatus", query = "SELECT c FROM CommentstatusTable c WHERE c.commentStatus = :commentStatus")
    , @NamedQuery(name = "CommentstatusTable.findByCommentDescription", query = "SELECT c FROM CommentstatusTable c WHERE c.commentDescription = :commentDescription")})
public class CommentstatusTable implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "COMMENTSTATUS_ID")
    private Integer commentstatusId;
    @Size(max = 45)
    @Column(name = "COMMENT_STATUS")
    private String commentStatus;
    @Size(max = 200)
    @Column(name = "COMMENT_DESCRIPTION")
    private String commentDescription;
    /*@OneToMany(cascade = CascadeType.ALL, mappedBy = "commentstatusId")
    private List<CommentTable> commentTableList;*/

    public CommentstatusTable() {
    }

    public CommentstatusTable(Integer commentstatusId) {
        this.commentstatusId = commentstatusId;
    }

    public Integer getCommentstatusId() {
        return commentstatusId;
    }

    public void setCommentstatusId(Integer commentstatusId) {
        this.commentstatusId = commentstatusId;
    }

    public String getCommentStatus() {
        return commentStatus;
    }

    public void setCommentStatus(String commentStatus) {
        this.commentStatus = commentStatus;
    }

    public String getCommentDescription() {
        return commentDescription;
    }

    public void setCommentDescription(String commentDescription) {
        this.commentDescription = commentDescription;
    }

   /* @XmlTransient
    public List<CommentTable> getCommentTableList() {
        return commentTableList;
    }

    public void setCommentTableList(List<CommentTable> commentTableList) {
        this.commentTableList = commentTableList;
    }*/

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (commentstatusId != null ? commentstatusId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CommentstatusTable)) {
            return false;
        }
        CommentstatusTable other = (CommentstatusTable) object;
        if ((this.commentstatusId == null && other.commentstatusId != null) || (this.commentstatusId != null && !this.commentstatusId.equals(other.commentstatusId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.soekm.entity.CommentstatusTable[ commentstatusId=" + commentstatusId + " ]";
    }
    
}
