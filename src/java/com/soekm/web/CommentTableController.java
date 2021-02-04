package com.soekm.web;

import com.soekm.entity.CommentTable;
import com.soekm.web.util.JsfUtil;
import com.soekm.web.util.PaginationHelper;
import com.soekm.ejb.CommentTableFacade;
import com.soekm.entity.CommentstatusTable;
import com.soekm.entity.Person;
import com.soekm.web.util.PageNavigation;

import java.io.Serializable;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

@Named("commentTableController")
@SessionScoped
public class CommentTableController implements Serializable {

    private static final String BUNDLE = "bundles.Bundle";

    Person user;
    private CommentTable current;
    private CommentstatusTable commentStatusTable;
    private DataModel items = null;
    @Inject
    com.soekm.web.UserController userController;
    @Inject
    com.soekm.web.ArticleController articleController;
    @EJB
    private com.soekm.ejb.CommentTableFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private Integer commentstatusId = 1;

    public CommentTableController() {
    }

    public Integer getCommentstatusId() {
        return commentstatusId;
    }

    public void setCommentstatusId(Integer commentstatusId) {
        this.commentstatusId = commentstatusId;
    }

    public CommentTable getSelected() {
        if (current == null) {
            current = new CommentTable();
            selectedItemIndex = -1;
        }
        return current;
    }

    private CommentTableFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (CommentTable) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "selectedArticle";
    }

    public String prepareCreate() {
        current = new CommentTable();
        selectedItemIndex = -1;
        //return "Create";
        return "selectedArticle";
    }

    public void clear() {
        current.setCommentBody(null);
    }

    public String create() {
        try {
            //commentStatusTable.setCommentStatus(commentStatus);
            if (!userController.isLogged()) {
                JsfUtil.addErrorMessage(ResourceBundle.getBundle(BUNDLE).getString("LoginToComment"));
                return null;
            }
            current.setPersonId(userController.getAuthenticatedUser());
            current.setArticleiId(articleController.getSelectedArticle());
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("CommentTableCreated"));           
        //return this.prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
            //return null;
        }
        this.clear();
        return null;
    }

    public String prepareEdit() {
        current = (CommentTable) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("CommentTableUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (CommentTable) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("CommentTableDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public CommentTable getCommentTable(java.lang.Integer id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = CommentTable.class)
    public static class CommentTableControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CommentTableController controller = (CommentTableController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "commentTableController");
            return controller.getCommentTable(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof CommentTable) {
                CommentTable o = (CommentTable) object;
                return getStringKey(o.getCommentId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + CommentTable.class.getName());
            }
        }

    }

}
