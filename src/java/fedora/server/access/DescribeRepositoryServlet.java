package fedora.server.access;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.net.URLDecoder;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import com.icl.saxon.expr.StringValue;

import fedora.server.Context;
import fedora.server.Logging;
import fedora.server.ReadOnlyContext;
import fedora.server.Server;
import fedora.server.errors.InitializationException;
import fedora.server.errors.GeneralException;
import fedora.server.errors.ServerException;
import fedora.server.errors.StreamIOException;
import fedora.server.storage.DOManager;
import fedora.server.storage.types.MIMETypedStream;
import fedora.server.storage.types.Property;
import fedora.server.utilities.DateUtility;

/**
 * <p><b>Title: </b>DescribeRepositoryServlet.java</p>
 * <p><b>Description: </b>Implements the "describeRepository" functionality
 * of the Fedora Access LITE (API-A-LITE) interface using a
 * java servlet front end. The syntax defined by API-A-LITE has for getting
 * a description of the repository has the following binding:
 * <ol>
 * <li>describeRepository URL syntax:
 * http://hostname:port/fedora/describe[?xml=BOOLEAN]
 * This syntax requests information about the repository.
 * The xml parameter determines the type of output returned.
 * If the parameter is omitted or has a value of "false", a MIME-typed stream
 * consisting of an html table is returned providing a browser-savvy means
 * of viewing the object profile. If the value specified is "true", then
 * a MIME-typed stream consisting of XML is returned.</li>
 * <ul>
 * <li>hostname - required hostname of the Fedora server.</li>
 * <li>port - required port number on which the Fedora server is running.</li>
 * <li>fedora - required name of the Fedora access service.</li>
 * <li>describe - required verb of the Fedora service.</li>
 * <li>xml - an optional parameter indicating the requested output format.
 *           A value of "true" indicates a return type of text/xml; the
 *           absence of the xml parameter or a value of "false"
 *           indicates format is to be text/html.</li>
 * </ul>
 *
 * -----------------------------------------------------------------------------
 *
 * <p><b>License and Copyright: </b>The contents of this file are subject to the
 * Mozilla Public License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
 *
 * <p>Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.</p>
 *
 * <p>The entire file consists of original code.  Copyright &copy; 2002, 2003 by The
 * Rector and Visitors of the University of Virginia and Cornell University.
 * All rights reserved.</p>
 *
 * -----------------------------------------------------------------------------
 *
 * @author rlw@virginia.edu
 * @version $Id$
 */
public class DescribeRepositoryServlet extends HttpServlet implements Logging
{
  /** Content type for html. */
  private static final String CONTENT_TYPE_HTML = "text/html; charset=UTF-8";

  /** Content type for xml. */
  private static final String CONTENT_TYPE_XML  = "text/xml; charset=UTF-8";

  /** Instance of the Fedora server. */
  private static Server s_server = null;

  /** Instance of the access subsystem. */
  private static Access s_access = null;

  /** Instance of DOManager. */
  private static DOManager m_manager = null;

  /** userInputParm hashtable */
  private Hashtable h_userParms = new Hashtable();

  /** Initial URL request by client */
  private String requestURL = null;

  /** Portion of initial request URL from protocol up to query string */
  private String requestURI = null;

  /** Instance of URLDecoder */
  private URLDecoder decoder = new URLDecoder();

  /** Host name of the Fedora server **/
  private static String fedoraServerHost = null;

  /** Port number on which the Fedora server is running. **/
  private static String fedoraServerPort = null;


  /**
   * <p>Process Fedora Access Request. Parse and validate the servlet input
   * parameters and then execute the specified request.</p>
   *
   * @param request  The servlet request.
   * @param response servlet The servlet response.
   * @throws ServletException If an error occurs that effects the servlet's
   *         basic operation.
   * @throws IOException If an error occurrs with an input or output operation.
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    String action = null;
    Property[] userParms = null;
    long servletStartTime = new Date().getTime();
    boolean isDescribeRequest = false;
    boolean xml = false;

    HashMap h=new HashMap();
    h.put("application", "apia");
    h.put("useCachedObject", "true");
    h.put("userId", "fedoraAdmin");
    h.put("host", request.getRemoteAddr());
    ReadOnlyContext context = new ReadOnlyContext(h);

    requestURI = request.getRequestURL().toString() + "?"
        + request.getQueryString();

    // Parse servlet URL.
    String[] URIArray = request.getRequestURL().toString().split("/");
    if (URIArray.length == 5)
    {
      if (URIArray[4].equalsIgnoreCase("describe"))
      {
        logFinest("[DescribeRepositoryServlet] Describe Repository Syntax "
            + "Encountered: "+ requestURI);
        isDescribeRequest = true;
      }
      else if (URIArray.length > 5)
      {
        String message = "Request Syntax Error: The expected "
            + "syntax for Describe requests is: \""
            + URIArray[0] + "//" + URIArray[2] + "/"
            + URIArray[3] + "/" + URIArray[4]
            + "[?ENCODE_XML] \"  "
            + " ----- Submitted request was: \"" + requestURI + "\"  .  ";
        logWarning(message);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
        return;
      }
      logFinest("[DescribeRepositoryServlet] Describe Repository Syntax "
          + "Encountered");
      isDescribeRequest = true;
    } else
    {
      // Bad syntax; redirect to syntax documentation page.
      response.sendRedirect("/userdocs/client/browser/apialite/index.html");
      return;
    }

    // Separate out servlet parameters from method parameters
    Hashtable h_userParms = new Hashtable();
    for ( Enumeration e = request.getParameterNames(); e.hasMoreElements();)
    {
      String name = decoder.decode((String)e.nextElement(), "UTF-8");
      if (isDescribeRequest && name.equalsIgnoreCase("xml"))
      {
        xml = new Boolean(request.getParameter(name)).booleanValue();
      }
      else
      {
        String value = decoder.decode(request.getParameter(name), "UTF-8");
        h_userParms.put(name,value);
      }
    }

    // API-A interface requires user-supplied parameters to be of type
    // Property[] so create Property[] from hashtable of user parameters.
    int userParmCounter = 0;
    userParms = new Property[h_userParms.size()];
    for ( Enumeration e = h_userParms.keys(); e.hasMoreElements();)
    {
      Property userParm = new Property();
      userParm.name = (String)e.nextElement();
      userParm.value = (String)h_userParms.get(userParm.name);
      userParms[userParmCounter] = userParm;
      userParmCounter++;
    }

    try
    {
      if (isDescribeRequest)
      {
        describeRepository(context, xml, response);
        long stopTime = new Date().getTime();
        long interval = stopTime - servletStartTime;
        logFiner("[DescribeRepositoryServlet] Servlet Roundtrip "
            + "describe: " + interval + " milliseconds.");
      }
    } catch (Throwable th)
      {
        String message = "[DescribeRepositoryServlet] An error has occured in "
            + "accessing the Fedora Access Subsystem. The error was \" "
            + th.getClass().getName()
            + " \". Reason: "  + th.getMessage()
            + "  Input Request was: \"" + request.getRequestURL().toString();
        showURLParms("", "", "", null, new Property[0], response, message);
        logWarning(message);
        th.printStackTrace();
    }
  }

  public void describeRepository(Context context, boolean xml,
    HttpServletResponse response) throws ServerException
  {

    OutputStreamWriter out = null;
    RepositoryInfo repositoryInfo = null;
    PipedWriter pw = null;
    PipedReader pr = null;

    try
    {
      pw = new PipedWriter();
      pr = new PipedReader(pw);
      repositoryInfo = s_access.describeRepository(context);
      if (repositoryInfo != null)
      {
        // Repository info obtained.
        // Serialize the RepositoryInfo object into XML
        new ReposInfoSerializerThread(repositoryInfo, pw).start();
        if (xml)
        {
          // Return results as raw XML
          response.setContentType(CONTENT_TYPE_XML);

          // Insures stream read from PipedReader correctly translates utf-8
          // encoded characters to OutputStreamWriter.
          out = new OutputStreamWriter(response.getOutputStream(),"UTF-8");
          int bufSize = 4096;
          char[] buf=new char[bufSize];
          int len=0;
          while ( (len = pr.read(buf, 0, bufSize)) != -1) {
              out.write(buf, 0, len);
          }
          out.flush();
        } else
        {
          // Transform results into an html table
          response.setContentType(CONTENT_TYPE_HTML);
          out = new OutputStreamWriter(response.getOutputStream(),"UTF-8");
          File xslFile = new File(s_server.getHomeDir(), "access/viewRepositoryInfo.xslt");
          TransformerFactory factory = TransformerFactory.newInstance();
          Templates template = factory.newTemplates(new StreamSource(xslFile));
          Transformer transformer = template.newTransformer();
          Properties details = template.getOutputProperties();
          transformer.transform(new StreamSource(pr), new StreamResult(out));
        }
        out.flush();

      } else
      {
        // Describe request returned nothing.
        String message = "[DescribeRepositoryServlet] No Repository Info returned.";
        logInfo(message);
      }
    } catch (Throwable th)
    {
      String message = "[DescribeRepositoryServlet] An error has occured. "
                     + " The error was a \" "
                     + th.getClass().getName()
                     + " \". Reason: "  + th.getMessage();
      logWarning(message);
      th.printStackTrace();
      throw new GeneralException(message);
    } finally
    {
      try
      {
        if (pr != null) pr.close();
        if (out != null) out.close();
      } catch (Throwable th)
      {
        String message = "[DescribeRepositoryServlet] An error has occured. "
                       + " The error was a \" "
                       + th.getClass().getName()
                     + " \". Reason: "  + th.getMessage();
        throw new StreamIOException(message);
      }
    }
  }

  /**
   * <p> A Thread to serialize an ObjectProfile object into XML.</p>
   *
   */
  public class ReposInfoSerializerThread extends Thread
  {
    private PipedWriter pw = null;
    private RepositoryInfo repositoryInfo = null;

    /**
     * <p> Constructor for ReposInfoSerializerThread.</p>
     *
     * @param repositoryInfo A repository info data structure.
     * @param pw A PipedWriter to which the serialization info is written.
     */
    public ReposInfoSerializerThread(RepositoryInfo repositoryInfo, PipedWriter pw)
    {
      this.pw = pw;
      this.repositoryInfo = repositoryInfo;
    }

    /**
     * <p> This method executes the thread.</p>
     */
    public void run()
    {
      if (pw != null)
      {
        try
        {
          pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
          pw.write("<fedoraRepository "
              + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
              + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
              + " xsi:schemaLocation=\"http://www.fedora.info/definitions/1/0/access/"
              + " http://" + fedoraServerHost + ":" + fedoraServerPort
              + "/fedoraRepository.xsd\">");
          //pw.write("<fedoraRepository "
          //    + " targetNamespace=\"http://www.fedora.info/definitions/1/0/access/\""
          //    + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
          //    + ">");
          //pw.write("<import namespace=\"http://www.fedora.info/definitions/1/0/access/\""
          //    + " location=\"fedoraRepository.xsd\"/>");

          // REPOSITORY INFO FIELDS SERIALIZATION
          pw.write("<repositoryName>" + repositoryInfo.repositoryName + "</repositoryName>");
          pw.write("<repositoryBaseURL>" + repositoryInfo.repositoryBaseURL + "</repositoryBaseURL>");
          pw.write("<repositoryVersion>" + repositoryInfo.repositoryVersion + "</repositoryVersion>");
          pw.write("<repositoryPID>");
          pw.write("    <PID-namespaceIdentifier>"
            + repositoryInfo.repositoryPIDNamespace
            + "</PID-namespaceIdentifier>");
          pw.write("    <PID-delimiter>" + ":"+ "</PID-delimiter>");
          pw.write("    <PID-sample>" + repositoryInfo.samplePID + "</PID-sample>");
          pw.write("</repositoryPID>");
          pw.write("<repositoryOAI-identifier>");
          pw.write("    <OAI-namespaceIdentifier>"
            + repositoryInfo.OAINamespace
            + "</OAI-namespaceIdentifier>");
          pw.write("    <OAI-delimiter>" + ":"+ "</OAI-delimiter>");
          pw.write("    <OAI-sample>" + repositoryInfo.sampleOAIIdentifer + "</OAI-sample>");
          pw.write("</repositoryOAI-identifier>");
          pw.write("<sampleSearch-URL>" + repositoryInfo.sampleSearchURL + "</sampleSearch-URL>");
          pw.write("<sampleAccess-URL>" + repositoryInfo.sampleAccessURL + "</sampleAccess-URL>");
          pw.write("<sampleOAI-URL>" + repositoryInfo.sampleOAIURL + "</sampleOAI-URL>");
          String[] emails = repositoryInfo.adminEmailList;
          for (int i=0; i<emails.length; i++)
          {
            pw.write("<adminEmail>" + emails[i] + "</adminEmail>");
          }
          pw.write("</fedoraRepository>");
          pw.flush();
          pw.close();
        } catch (IOException ioe) {
          System.err.println("WriteThread IOException: " + ioe.getMessage());
        } finally
        {
          try
          {
            if (pw != null) pw.close();
          } catch (IOException ioe)
          {
            System.err.println("WriteThread IOException: " + ioe.getMessage());
          }
        }
      }
    }
  }

  /**
   * <p>For now, treat a HTTP POST request just like a GET request.</p>
   *
   * @param request The servet request.
   * @param response The servlet response.
   * @throws ServletException If thrown by <code>doGet</code>.
   * @throws IOException If thrown by <code>doGet</code>.
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    doGet(request, response);
  }

  /**
   * <p>Initialize servlet.</p>
   *
   * @throws ServletException If the servet cannot be initialized.
   */
  public void init() throws ServletException
  {
    try
    {
      s_server=Server.getInstance(new File(System.getProperty("fedora.home")));
      fedoraServerHost = s_server.getParameter("fedoraServerHost");
      fedoraServerPort = s_server.getParameter("fedoraServerPort");
      m_manager=(DOManager) s_server.getModule("fedora.server.storage.DOManager");
      s_access = (Access) s_server.getModule("fedora.server.access.Access");
    } catch (InitializationException ie)
    {
      throw new ServletException("Unable to get Fedora Server instance."
          + ie.getMessage());
    }
  }

  /**
   * <p>Cleans up servlet resources.</p>
   */
  public void destroy()
  {}

  /**
   * <p>Displays a list of the servlet input parameters. This method is
   * generally called when a service request returns no data. Usually
   * this is a result of an incorrect spelling of either a required
   * URL parameter or in one of the user-supplied parameters. The output
   * from this method can be used to help verify the URL parameters
   * sent to the servlet and hopefully fix the problem.</p>
   *
   * @param PID The persistent identifier of the digital object.
   * @param bDefPID The persistent identifier of the Behavior Definition object.
   * @param methodName the name of the method.
   * @param asOfDateTime The version datetime stamp of the digital object.
   * @param userParms An array of user-supplied method parameters and values.
   * @param response The servlet response.
   * @param message The message text to include at the top of the output page.
   * @throws IOException If an error occurrs with an input or output operation.
   */
  private void showURLParms(String PID, String bDefPID,
                           String methodName, Calendar asOfDateTime,
                           Property[] userParms,
                           HttpServletResponse response,
                           String message)
      throws IOException
  {
    String versDate = DateUtility.convertCalendarToString(asOfDateTime);
    response.setContentType(CONTENT_TYPE_HTML);
    ServletOutputStream out = response.getOutputStream();

    // Display servlet input parameters
    StringBuffer html = new StringBuffer();
    html.append("<html>");
    html.append("<head>");
    html.append("<title>FedoraAccessServlet</title>");
    html.append("</head>");
    html.append("<body>");
    html.append("<br></br><font size='+2'>" + message + "</font>");
    html.append("<br></br><font color='red'>Request Parameters</font>");
    html.append("<br></br>");
    html.append("<table cellpadding='5'>");
    html.append("<tr>");
    html.append("<td><font color='red'>PID</td>");
    html.append("<td> = <td>" + PID + "</td>");
    html.append("</tr>");
    html.append("<tr>");
    html.append("<td><font color='red'>bDefPID</td>");
    html.append("<td> = </td>");
    html.append("<td>" + bDefPID + "</td>");
    html.append("</tr>");
    html.append("<tr>");
    html.append("<td><font color='red'>methodName</td>");
    html.append("<td> = </td>");
    html.append("<td>" + methodName + "</td>");
    html.append("</tr>");
    html.append("<tr>");
    html.append("<td><font color='red'>asOfDateTime</td>");
    html.append("<td> = </td>");
    html.append("<td>" + versDate + "</td>");
    html.append("</tr>");
    html.append("<tr>");
    html.append("</tr>");
    html.append("<tr>");
    html.append("<td colspan='5'><font size='+1' color='blue'>"+
                "Other Parameters Found:</font></td>");
    html.append("</tr>");
    html.append("<tr>");
    html.append("</tr>");

    // List user-supplied parameters if any
    if (userParms != null)
    {
    for (int i=0; i<userParms.length; i++)
    {
      html.append("<tr>");
      html.append("<td><font color='red'>" + userParms[i].name
                  + "</font></td>");
      html.append("<td> = </td>");
      html.append("<td>" + userParms[i].value + "</td>");
        html.append("</tr>");
    }
    }
    html.append("</table></center></font>");
    html.append("</body></html>");
    out.println(html.toString());

    logFinest("PID: " + PID + " bDefPID: " + bDefPID
              + " methodName: " + methodName);
    if (userParms != null)
    {
      for (int i=0; i<userParms.length; i++)
      {
        logFinest("userParm: " + userParms[i].name
        + " userValue: "+userParms[i].value);
      }
    }
    html = null;
  }

  private Server getServer() {
      return s_server;
  }

  /**
   * Logs a SEVERE message, indicating that the server is inoperable or
   * unable to start.
   *
   * @param message The message.
   */
  public final void logSevere(String message) {
      StringBuffer m=new StringBuffer();
      m.append(getClass().getName());
      m.append(": ");
      m.append(message);
      getServer().logSevere(m.toString());
  }

  public final boolean loggingSevere() {
      return getServer().loggingSevere();
  }

  /**
   * Logs a WARNING message, indicating that an undesired (but non-fatal)
   * condition occured.
   *
   * @param message The message.
   */
  public final void logWarning(String message) {
      StringBuffer m=new StringBuffer();
      m.append(getClass().getName());
      m.append(": ");
      m.append(message);
      getServer().logWarning(m.toString());
  }

  public final boolean loggingWarning() {
      return getServer().loggingWarning();
  }

  /**
   * Logs an INFO message, indicating that something relatively uncommon and
   * interesting happened, like server or module startup or shutdown, or
   * a periodic job.
   *
   * @param message The message.
   */
  public final void logInfo(String message) {
      StringBuffer m=new StringBuffer();
      m.append(getClass().getName());
      m.append(": ");
      m.append(message);
      getServer().logInfo(m.toString());
  }

  public final boolean loggingInfo() {
      return getServer().loggingInfo();
  }

  /**
   * Logs a CONFIG message, indicating what occurred during the server's
   * (or a module's) configuration phase.
   *
   * @param message The message.
   */
  public final void logConfig(String message) {
      StringBuffer m=new StringBuffer();
      m.append(getClass().getName());
      m.append(": ");
      m.append(message);
      getServer().logConfig(m.toString());
  }

  public final boolean loggingConfig() {
      return getServer().loggingConfig();
  }

  /**
   * Logs a FINE message, indicating basic information about a request to
   * the server (like hostname, operation name, and success or failure).
   *
   * @param message The message.
   */
  public final void logFine(String message) {
      StringBuffer m=new StringBuffer();
      m.append(getClass().getName());
      m.append(": ");
      m.append(message);
      getServer().logFine(m.toString());
  }

  public final boolean loggingFine() {
      return getServer().loggingFine();
  }

  /**
   * Logs a FINER message, indicating detailed information about a request
   * to the server (like the full request, full response, and timing
   * information).
   *
   * @param message The message.
   */
  public final void logFiner(String message) {
      StringBuffer m=new StringBuffer();
      m.append(getClass().getName());
      m.append(": ");
      m.append(message);
      getServer().logFiner(m.toString());
  }

  public final boolean loggingFiner() {
      return getServer().loggingFiner();
  }

  /**
   * Logs a FINEST message, indicating method entry/exit or extremely
   * verbose information intended to aid in debugging.
   *
   * @param message The message.
   */
  public final void logFinest(String message) {
      StringBuffer m=new StringBuffer();
      m.append(getClass().getName());
      m.append(": ");
      m.append(message);
      getServer().logFinest(m.toString());
  }

  public final boolean loggingFinest() {
      return getServer().loggingFinest();
  }

}