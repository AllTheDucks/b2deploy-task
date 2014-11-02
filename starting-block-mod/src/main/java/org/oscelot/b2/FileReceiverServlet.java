/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oscelot.b2;

import blackboard.platform.plugin.PlugInException;
import blackboard.platform.plugin.PlugInUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author wiley
 */
public class FileReceiverServlet extends HttpServlet {

    public static final String FILENAME_HEADER = "X-B2-Filename";
    public static final String TMP_FILENAME_HEADER = "X-B2-Tmp-Filename";

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here */
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet FileReceiverServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>FileReceiverServlet at " + request.getContextPath() + "</h1>");
            out.println("<p>You must submit your file using a post method</p>");
            out.println("<p>User-Agent: " + request.getHeader("User-Agent") + "</p>");
            out.println("</body>");
            out.println("</html>");

        } finally {
            out.close();
        }
    }

    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        FileOutputStream fos = null;
        InputStream in = request.getInputStream();
//        System.out.println("Bytes Available: "+in.available());
        String filename = request.getHeader(FILENAME_HEADER);
//        for(String headerName : Collections.list(request.getHeaderNames())) {
//            System.out.println(headerName+": "+request.getHeader(headerName));
//        }
        try {
            ArrayList<String> headers = Collections.list(request.getHeaderNames());

            File outDir = null;
            try {
                outDir = PlugInUtil.getConfigDirectory("bb", "starting-block");
                if (outDir.listFiles() != null) {
                    for (File oldFile : outDir.listFiles()) {
                        try {
                            oldFile.delete();
                        } catch (SecurityException ex) {
                            response.setStatus(response.SC_INTERNAL_SERVER_ERROR);
                            out.println("--- Server Side Stacktrace ---");
                            out.println("Couldn't Delete Old file in upload directory.");
                            ex.printStackTrace(out);
                            return;
                        }
                    }
                }
                if (outDir == null) {
                    response.setStatus(response.SC_INTERNAL_SERVER_ERROR);
                    out.println("Couldn't determine upload location.");
                }
            } catch (PlugInException ex) {
                response.setStatus(response.SC_INTERNAL_SERVER_ERROR);
                out.println("--- Server Side Stacktrace ---");
                ex.printStackTrace(out);
                return;
            }

            File outFile = outDir.createTempFile(filename, null, outDir);
            fos = new FileOutputStream(outFile);

            byte[] buf = new byte[1024];
            int result = in.read(buf);
            while (result > 0) {
                fos.write(buf, 0, result);
                result = in.read(buf);
            }

            // Set the X-B2-Tmp-Filename so the client knows where the file is.
            response.setHeader(TMP_FILENAME_HEADER, outFile.getAbsolutePath());

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            if (fos != null) {
                fos.close();
            }
            out.close();
        }
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
