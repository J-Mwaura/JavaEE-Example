/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.web.util;


import com.soekm.web.ArticleController;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.Part;

/**
 *
 * @author atjkm
 */
@FacesValidator(value="fileUploadValidator")
public class FileUploadValidator implements Validator {
    
    private static final String BUNDLE = "bundles.Bundle";
    private final static Logger LOGGER = Logger.getLogger(ArticleController.class.getCanonicalName());
    
    @Override
    public void validate(FacesContext context, UIComponent component, Object value)     throws ValidatorException {
        Part file = (Part) value;
 
        FacesMessage message=null;
 
        try {
 
            if (file==null || file.getSize()<=0 || file.getContentType().isEmpty() )
                message=new FacesMessage("Please select a vlid file");
            /*else if (!file.getContentType().endsWith("png") || !file.getContentType().endsWith("jpg")
                    || !file.getContentType().endsWith("bmp") || !file.getContentType().endsWith("gif"))
                message=new FacesMessage("Wrong file selected");*/
            else if (file.getSize()>30240)
                message=new FacesMessage("The file you attempted to upload is too big. Please try a different file.");
            
            if (message!=null && !message.getDetail().isEmpty())
                {
                    message.setSeverity(FacesMessage.SEVERITY_ERROR);
                    throw new ValidatorException(message );
                }
 
        } catch (Exception ex) {
               throw new ValidatorException(new FacesMessage(ex.getMessage()));
        }
 
    }  
}
