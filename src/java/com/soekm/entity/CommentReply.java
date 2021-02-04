/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.entity;

import com.soekm.entity.CommentTable;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author atjkm
 */
@Entity
@Table(name = "comment_reply")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CommentReply.findAll", query = "SELECT c FROM CommentReply c")
    , @NamedQuery(name = "CommentReply.findByReplyId", query = "SELECT c FROM CommentReply c WHERE c.replyId = :replyId")
    , @NamedQuery(name = "CommentReply.findByReplyBody", query = "SELECT c FROM CommentReply c WHERE c.replyBody = :replyBody")
    , @NamedQuery(name = "CommentReply.findByDateCreated", query = "SELECT c FROM CommentReply c WHERE c.dateCreated = :dateCreated")
    , @NamedQuery(name = "CommentReply.findByUrl", query = "SELECT c FROM CommentReply c WHERE c.url = :url")})
public class CommentReply implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "REPLY_ID")
    private Integer replyId;
    @Size(max = 800)
    @Column(name = "REPLY_BODY")
    private String replyBody;
    @Column(name = "DATE_CREATED")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;
    @Size(max = 200)
    @Column(name = "URL")
    private String url;
    @JoinColumn(name = "COMMENT_ID", referencedColumnName = "COMMENT_ID")
    @ManyToOne(optional = false)
    private CommentTable commentId;

    public CommentReply() {
    }

    public CommentReply(Integer replyId) {
        this.replyId = replyId;
    }

    public Integer getReplyId() {
        return replyId;
    }

    public void setReplyId(Integer replyId) {
        this.replyId = replyId;
    }

    public String getReplyBody() {
        return replyBody;
    }

    public void setReplyBody(String replyBody) {
        this.replyBody = replyBody;
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

    public CommentTable getCommentId() {
        return commentId;
    }

    public void setCommentId(CommentTable commentId) {
        this.commentId = commentId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (replyId != null ? replyId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CommentReply)) {
            return false;
        }
        CommentReply other = (CommentReply) object;
        if ((this.replyId == null && other.replyId != null) || (this.replyId != null && !this.replyId.equals(other.replyId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.soekm.entityNew.CommentReply[ replyId=" + replyId + " ]";
    }
    
}
