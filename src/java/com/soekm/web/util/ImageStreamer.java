/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.web.util;

//import com.jkm.admin.Bean.StoryController;
import com.soekm.ejb.ArticleTableFacade;
import com.soekm.entity.Person;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import javax.inject.Inject;

/**
 *
 * @author atjkm
 */
@Named(value = "imageStreamer")
@ApplicationScoped
public class ImageStreamer {
    String id;
    private com.soekm.entity.Person person;
    @Inject
    com.soekm.web.CustomerController customerController;
    @EJB
    com.soekm.ejb.UserBean userFacade;
    
    public ImageStreamer() {
    }
    
    public StreamedContent getImage() throws IOException {
        // Process faces request and render the corresponding response
        FacesContext context = FacesContext.getCurrentInstance();
        try{
        
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // rendering HTML. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        }
        else {
            // Browser is requesting the image. Return a real StreamedContent with the image bytes.
            id = context.getExternalContext().getRequestParameterMap().get("id");
            person = userFacade.find(Integer.parseInt(id));
            //System.out.println("Person id = " + id);
            return new DefaultStreamedContent(new ByteArrayInputStream(person.getAvatar()));
        }
    }catch (NumberFormatException e){
}
    return null;
}
}
