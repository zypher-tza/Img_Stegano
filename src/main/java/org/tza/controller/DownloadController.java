package org.tza.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/DownloadController")
public class DownloadController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            byte[] encodedImageBytes = (byte[]) session.getAttribute("encodedImageBytesForDownload");

            if (encodedImageBytes != null && encodedImageBytes.length > 0) {
                response.setContentType("image/png");
                response.setHeader("Content-Disposition", "attachment; filename=zypher_stegano.png");
                response.setContentLength(encodedImageBytes.length);

                try (OutputStream outStream = response.getOutputStream()) {
                    outStream.write(encodedImageBytes);
                    outStream.flush();
                }
                session.removeAttribute("encodedImageBytesForDownload");
            } else {
                response.setContentType("text/html");
                response.getWriter().println("<html>"
                		+ "<body><p style='color:red;'>No encoded image available for download. Please embed a message first.</p><p>"
                		+ "<a href='index.jsp'>Go Back</a></p></body><"
                		+ "/html>");
                
            }
        } else {
            // No session found
            response.setContentType("text/html");
            response.getWriter().println("<html><body>"
            		+ "<p style='color:red;'>Session expired or no image available. Please embed a message first.</p>"
            		+ "<p><a href='index.jsp'>Go Back</a></p></body>"
            		+ "</html>");
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}