/*
 *
 */
package org.oscelot.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 *
 * @author wiley
 */
public class B2DeployTask {

    private Project project;
    private String localFilePath;
    private String remoteFilePath;
    private String uploadUrlStr;
    private String installUrlStr;
    private String host;
    private boolean clean = false;
    private boolean courseOrgAvailable = false;
    private String webAppName = "bb-starting-block-bb_bb60";
    private String uploadServletName = "FileReceiverServlet";
    private String deployServletName = "execute/install";

    public void execute() {
        if (localFilePath == null) {
            throw new BuildException("No local file set.");
        }
        if (host == null) {
            throw new BuildException("No Host defined.");
        }
        System.out.println("Installing: " + localFilePath + " to " +getHost());
        uploadUrlStr = "http://" + getHost() + "/webapps/" + getWebAppName() + "/" + uploadServletName;
        installUrlStr = "http://" + getHost() + "/webapps/" + getWebAppName() + "/" + deployServletName;
        try {
            URL uploadURL = new URL(uploadUrlStr);
            HttpURLConnection conn = (HttpURLConnection) uploadURL.openConnection();
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(1024);
            
            File inFile = new File(localFilePath);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-B2-Filename", inFile.getName());
            conn.setRequestProperty("Content-Disposition","form-data; name=\"binaryFile\"; filename=\"" + inFile.getName() + "\"");
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("Content-Transfer-Encoding","binary");

            FileInputStream fin = new FileInputStream(inFile);
            OutputStream out = conn.getOutputStream();

            byte[] buf = new byte[1024];
            int result = fin.read(buf);
            while (result > 0) {
                out.write(buf, 0, result);
                result = fin.read(buf);
            }

            out.close();
            fin.close();
            
            remoteFilePath = conn.getHeaderField("X-B2-Tmp-Filename");

            if (conn.getResponseCode() != 200) {
                System.out.println("FAILED.  Error uploading file.");
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = reader.readLine();
                while (line != null) {
                    System.out.println(line);
                    line = reader.readLine();
                }
            }
//            else {
//                System.out.println("SUCCESS. Status: " + conn.getResponseCode() + ". Uploaded \"" + localFilePath + "\" to \"" + remoteFilePath + "\"");
//            }



            ///////////////////////////////////////////
            // Now do installation of uploaded Building Block.

            URL installURL = new URL(installUrlStr + "?fileName=" + remoteFilePath + "&clean=" + clean + "&available=" + courseOrgAvailable);
            conn.disconnect();

            conn = (HttpURLConnection) installURL.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = reader.readLine();
            // The Starting block doesn't pass back an error code, so for now
            // we'll just check the first line, which should contain "ERROR"
            if (line.contains("ERROR")) {
                System.err.println("--- Server Side Stacktrace ---");
                while (line != null) {
                    System.err.println(line);
                    line = reader.readLine();
                }
            } else {
                while (line != null) {
                    System.out.println(line);
                    line = reader.readLine();
                }
            }


        } catch (MalformedURLException ex) {
            throw new BuildException(ex);
        } catch (FileNotFoundException ex) {
            throw new BuildException(ex);
        } catch (IOException ex) {
            throw new BuildException(ex);
        }


    }

    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * @param localFilePath the localFilePath to set
     */
    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }


    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the clean
     */
    public boolean isClean() {
        return clean;
    }

    /**
     * @param clean the clean to set
     */
    public void setClean(boolean clean) {
        this.clean = clean;
    }

    /**
     * @return the courseOrgAvailable
     */
    public boolean isCourseOrgAvailable() {
        return courseOrgAvailable;
    }

    /**
     * @param courseOrgAvailable the available to set
     */
    public void setCourseOrgAvailable(boolean courseOrgAvailable) {
        this.courseOrgAvailable = courseOrgAvailable;
    }

    /**
     * @return the webAppName
     */
    public String getWebAppName() {
        return webAppName;
    }

    /**
     * @param webAppName the webAppName to set
     */
    public void setWebAppName(String webAppName) {
        this.webAppName = webAppName;
    }
}
