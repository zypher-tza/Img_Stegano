package org.tza.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.servlet.http.HttpSession; 

import java.io.IOException;
import java.io.InputStream;

import org.tza.model.SteganoData; 
import org.tza.utils.ImageUtil;



@WebServlet({"/embed", "/extract"})
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 50
)
public class EmbedController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		String url = "/index.jsp"; 

		
		request.removeAttribute("successMessage");
		request.removeAttribute("errorMessage");
		request.removeAttribute("encodedImageBytes");
		request.removeAttribute("extractedMessage");

		if (action == null) {
			request.setAttribute("errorMessage", "No action specified.");
			request.getRequestDispatcher(url).forward(request, response);
			return;
		}
		HttpSession session = request.getSession();

		switch (action) {
		    case "embed":
		        Part imagePart = request.getPart("uploadImg"); 
		        String secretMessage = request.getParameter("secretMessage"); 

		       
		        if (imagePart == null || imagePart.getSize() == 0) {
		            request.setAttribute("errorMessage", "Please upload an image for embedding.");
		            break; 
		        }
		        if (secretMessage == null || secretMessage.trim().isEmpty()) {
		            request.setAttribute("errorMessage", "Please provide a secret message to embed.");
		            break; 
		        }

		        try (InputStream imageStream = imagePart.getInputStream()) {
		            byte[] originalImageBytes = imageStream.readAllBytes();
		          
		            byte[] encodedImageBytes = ImageUtil.embedMessage(originalImageBytes, secretMessage);

		            
		            request.setAttribute("encodedImageBytes", encodedImageBytes);
		            session.setAttribute("encodedImageBytesForDownload", encodedImageBytes);

		            request.setAttribute("successMessage", "Message successfully embedded!");

		        } catch (Exception e) {
		            request.setAttribute("errorMessage", "Error during embedding: " + e.getMessage());
		            e.printStackTrace(); 
		        }
		        break;

		    case "extract":
		        Part extractImagePart = request.getPart("uploadImg");

		        if (extractImagePart == null || extractImagePart.getSize() == 0) {
		            request.setAttribute("errorMessage", "Please upload an image for extraction.");
		        } else {
		            try (InputStream extractImageStream = extractImagePart.getInputStream()) {
		                byte[] imageBytesToExtractFrom = extractImageStream.readAllBytes();

		                String extractedMessage = ImageUtil.extractMessage(imageBytesToExtractFrom);
		                System.out.println("DEBUG: Extracted Message from ImageUtil: '" + extractedMessage + "'");

		                request.setAttribute("extractedMessage", extractedMessage);
		                request.setAttribute("successMessage", "Message successfully extracted!");
		                request.getSession().removeAttribute("encodedImageBytesForDownload");

		            } catch (Exception e) {
		                request.setAttribute("errorMessage", "Error during extraction: " + e.getMessage());
		                System.err.println("ERROR: Extraction failed."); 
		                e.printStackTrace(); 
		            }
		        }
		        break;

		    default:
		        request.setAttribute("errorMessage", "Invalid action: " + action);
		        break;
		}
		request.getRequestDispatcher(url).forward(request, response);
	}

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}