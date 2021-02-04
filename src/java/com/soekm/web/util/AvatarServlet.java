package com.soekm.web.util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.soekm.ejb.UserBean;
import com.soekm.entity.Person;
import com.soekm.web.util.AvatarServlet;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author atjkm
 */
@WebServlet(urlPatterns = "/avatar/*") // annotation for listening to images
public class AvatarServlet extends HttpServlet {

   @EJB
    UserBean userFacade;
    private static final Logger logger = Logger.getLogger(ImageServlet.class.getCanonicalName());
    // Constants 
    private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
    // Properties 
    private final String UPLOAD_DIR = "/upload/img/";
    //private final String UPLOAD_DIR = ResourceBundle.getBundle("bundles.Bundle").getString("UPLOAD_LOCATION");
    private String imagePath;
    // Actions 
    @Override
    public void init() throws ServletException {
        // In a Windows environment with the Applicationserver running on the
        // c: volume, the above path is exactly the same as "c:\images".
        // In UNIX, it is just straightforward "/images".
        // If you have stored files in the WebContent of a WAR, for example in the
        // "/WEB-INF/images" folder, then you can retrieve the absolute path by:
        // this.imagePath = getServletContext().getRealPath("/WEB-INF/images");
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {// Get requested image by path info.
        String requestedImage = request.getParameter("id"); //request.getPathInfo();
        
        // Check if file name is actually supplied to the request URI.
        if (requestedImage == null) {
            // Do your thing if the image is not supplied to the request URI.
            // Throw an exception, or send 404, or show default/warning image, or just ignore it.

            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
            return;
        }
        Person p = userFacade.find(Integer.parseInt(requestedImage));

        if ((p == null)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
        } else {
            /*
            // Decode the file name (might contain spaces and on) and prepare file object.
            File image = new File(getServletContext().getRealPath(UPLOAD_DIR), URLDecoder.decode(requestedImage, "UTF-8"));
            
            // Check if file actually exists in filesystem.
            if (!image.exists()) {
            // Throw an exception, or send 404, or show default/warning image, or just ignore it.
            logger.info("Uploaded image already exists");
            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
            return;
            }
            
            // Get content type by filename.
            String contentType = getServletContext().getMimeType(image.getName());
            
            // Check if file is actually an image (avoid download of other files by hackers!).
            // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
            if (contentType == null || !contentType.startsWith("image")) {
            // Throw an exception, or send 404, or show default/warning image, or just ignore it.
            logger.info("Uploaded image doesn't look like a real image");
            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
            return;
            }
             */
            // Init servlet response.
            response.reset();
            response.setBufferSize(DEFAULT_BUFFER_SIZE);
            //response.setContentType("image/jpg");
           // response.setHeader("Content-Length", String.valueOf(p.getAvatar().length));
            response.setHeader("Content-Disposition", "inline; filename=\"" + p.getFirstname()+ "\"");

            // Prepare streams.
            ByteArrayInputStream byteInputStream = null;
            BufferedOutputStream output = null;

            try {
                // Open streams.
                //byteInputStream = new ByteArrayInputStream(p.getAvatar());
                output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

              // Write file contents to response.
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int length;
                while ((length = byteInputStream.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
            } finally {
                // close streams.
                close(output);
                close(byteInputStream);
            }
        }
}// Helpers (can be refactored to public utility class) 
    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, 
                        "Problems during image resource manipulation. {0}", 
                        e.getMessage());
            }
        }
    }
}
