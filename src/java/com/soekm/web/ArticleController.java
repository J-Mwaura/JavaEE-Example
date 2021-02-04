package com.soekm.web;

import com.soekm.entity.ArticleTable;
import com.soekm.entity.CommentTable;
import com.soekm.web.util.JsfUtil;
import com.soekm.web.util.PaginationHelper;
import com.soekm.ejb.ArticleTableFacade;
import com.soekm.ejb.ArticlecategoryTableFacade;
import com.soekm.entity.ArticlecategoryTable;
import com.soekm.entity.Person;
import com.soekm.qualifiers.LoggedIn;
import com.soekm.web.util.PageNavigation;
import java.io.InputStream;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.servlet.http.Part;

@Named("articleController")
@SessionScoped
public class ArticleController implements Serializable {

    private final static Logger logger = Logger.getLogger(ArticleController.class.getCanonicalName());
    private static final String BUNDLE = "bundles.Bundle";

    Person user;
    private DataModel items = null;
    @Inject
    com.soekm.web.UserController userController;
    private ArticleTable current;

    @EJB
    private com.soekm.ejb.ArticleTableFacade articleFacade;
    @EJB
    ArticlecategoryTableFacade articlecategoryFacade;
    private List<ArticlecategoryTable> categories;
    private List<ArticleTable> articles;
    private List<CommentTable> comments;
    private List<CommentTable> articleComments;
    private List<ArticleTable> categoryProducts;
    private String selectedCategoryId = null;
    private String selectedArticleId = null;
    private com.soekm.ejb.UserBean getEjbFacade;
    private PaginationHelper pagination;
    private ArticlecategoryTable selectedCategory;
    private ArticleTable selectedArticle;
    private int selectedItemIndex;
    private Part filePart;
    private Integer articleId;
    private int categoryId;

    private static final List<String> EXTENSIONS_ALLOWED = new ArrayList<>();

    static {
        // images only
        EXTENSIONS_ALLOWED.add(".jpg");
        EXTENSIONS_ALLOWED.add(".bmp");
        EXTENSIONS_ALLOWED.add(".png");
        EXTENSIONS_ALLOWED.add(".gif");
    }

    public int totalArticles() {
        return articleFacade.count();
    }

    public ArticleTable find(Integer articleId) {
        return articleFacade.find(articleId);
    }

    public List<CommentTable> getComments() {
        return comments;
    }

    public void setComments(List<CommentTable> comments) {
        this.comments = comments;
    }

    public Collection<CommentTable> getArticleComments() {
        if (selectedArticleId != null) {
            articleComments = getSelectedArticle().getCommentTableList();
        }
        return articleComments == null ? Collections.EMPTY_LIST : articleComments;
    }

    public void setArticleComments(List<CommentTable> articleComments) {
        this.articleComments = articleComments;
    }

    public List<ArticlecategoryTable> getCategories() {
        return this.articlecategoryFacade.findAll();
    }

    public void setCategories(List<ArticlecategoryTable> categories) {
        this.categories = categories;
    }

    public List<ArticleTable> getArticles() {
        return articleFacade.findAll();
    }

    public void setArticles(List<ArticleTable> articles) {
        this.articles = articles;
    }

    public void setCategoryProducts(List<ArticleTable> categoryProducts) {
        this.categoryProducts = categoryProducts;
    }

    public String getSelectedArticleId() {
        return selectedArticleId;
    }

    public void setSelectedArticleId(String selectedArticleId) {
        this.selectedArticleId = selectedArticleId;
    }

    public ArticleTable getSelectedArticle() {
        if (selectedArticleId != null) {
            selectedArticle = articleFacade.find(Integer.parseInt(selectedArticleId));
        }
        return selectedArticle == null ? new ArticleTable() : selectedArticle;
    }

    public void setSelectedArticle(ArticleTable selectedArticle) {
        this.selectedArticle = selectedArticle;
    }

    private String getFileName(Part part) {
        String partHeader = part.getHeader("content-disposition");
        logger.log(Level.INFO, "Part Header = {0}", partHeader);
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;

    }

    public void upload() {
        logger.info(getFilePart().getName());

        try {
            InputStream is = getFilePart().getInputStream();

            int i = is.available();
            byte[] b = new byte[i];
            is.read(b);

            logger.log(Level.INFO, "Length : {0}", b.length);
            String fileName = getFileName(getFilePart());
            logger.log(Level.INFO, "File name : {0}", fileName);

            // generate *unique* filename 
            final String extension = fileName.substring(fileName.length() - 4);

            if (!EXTENSIONS_ALLOWED.contains(extension)) {
                try {
                    logger.severe("User tried to upload file that's not an image. Upload cancelled.");
                    JsfUtil.addErrorMessage(ResourceBundle.getBundle(BUNDLE).getString("WrongFile"));
                    //response.sendRedirect("admin/product/List.xhtml?errMsg=Error trying to upload file");
                    return;
                } catch (Exception e) {
                    JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
                    //return;
                }
            }

            current.setImageMain(b);
            current.setImage(fileName);

            JsfUtil.addSuccessMessage("Article image successfuly uploaded!");

        } catch (Exception ex) {
        }

    }

    /**
     * @return the filePart
     */
    public Part getFilePart() {
        return filePart;
    }

    /**
     * @param filePart the filePart to set
     */
    public void setFilePart(Part filePart) {
        this.filePart = filePart;
    }

    // for use with DisplayImage servlet 
    public byte[] getBytes(Short newsId) {
        return articleFacade.find(articleId).getImageMain();
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public ArticleController() {
    }

    public ArticleTable getSelected() {
        if (current == null) {
            current = new ArticleTable();
            selectedItemIndex = -1;
        }
        return current;
    }

    public String showAll() {
        recreateModel();
        categoryId = 0; // show all products

        return "articleTable/List";
    }

    private ArticleTableFacade getFacade() {
        return articleFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(4) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    if (categoryId != 0) {
                        return new ListDataModel(getFacade().findByCategory(new int[]{getPageFirstItem(),
                            getPageFirstItem() + getPageSize()}, categoryId));
                    }
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(),
                        getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "/articleTable/List";
    }

    public String prepareListOnSelectedArticle() {
        recreateModel();
        return "selectedArticle";
    }

    public ArticleTable findById(int id) {
        return articleFacade.find(id);
    }

    public void dofindArticleById() {

        current = articleFacade.dofindArticleById(current.getArticleiId());
    }

    public PageNavigation prepareView() {
        current = (ArticleTable) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return PageNavigation.VIEW;
    }

    public PageNavigation prepareCreate() {
        current = new ArticleTable();
        selectedItemIndex = -1;
        return PageNavigation.CREATE;
    }

    public PageNavigation create() {
        try {
            upload();
            current.setPersonId(userController.getAuthenticatedUser());
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("ArticleTableCreated"));
            return PageNavigation.CREATE;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public PageNavigation prepareEdit() {
        current = (ArticleTable) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return PageNavigation.EDIT;
    }

    public PageNavigation update() {
        try {
            upload();
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("ArticleTableUpdated"));
            return PageNavigation.VIEW;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle(BUNDLE).getString("PersistenceErrorOccured"));
            System.out.print(e);
            return null;
        }
    }

    public PageNavigation destroy() {
        current = (ArticleTable) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return PageNavigation.LIST;
    }

    public PageNavigation destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return PageNavigation.VIEW;
        } else {
            // all items were removed - go back to list
            recreateModel();
            return PageNavigation.LIST;
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle(BUNDLE).getString("ArticleTableDeleted"));
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

    public PageNavigation next() {
        getPagination().nextPage();
        recreateModel();
        return PageNavigation.LIST;
    }

    public String nextOnIndex() {
        getPagination().nextPage();
        recreateModel();
        return "index";
    }

    public String previousOnIndex() {
        getPagination().previousPage();
        recreateModel();
        return "ïndex";
    }

    public PageNavigation previous() {
        getPagination().previousPage();
        recreateModel();
        return PageNavigation.LIST;
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(articleFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(articleFacade.findAll(), true);
    }

    /**
     * @return the categoryId
     */
    public int getCategoryId() {
        return categoryId;
    }

    /**
     * @param categoryId the categoryId to set
     */
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public ArticleTable getArticleTable(java.lang.Integer id) {
        return articleFacade.find(id);
    }

    @FacesConverter(forClass = ArticleTable.class)
    public static class ArticleTableControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ArticleController controller = (ArticleController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "articleTableController");
            return controller.getArticleTable(getKey(value));
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
            if (object instanceof ArticleTable) {
                ArticleTable o = (ArticleTable) object;
                return getStringKey(o.getArticleiId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + ArticleTable.class.getName());
            }
        }

    }

}
