package fedora.server.storage.types;

/**
 *
 * <p><b>Title:</b> MethodParmDef.java</p>
 * <p><b>Description:</b> </p>
 *
 * @author payette@cs.cornell.edu
 * @version $Id$
 */
public class MethodParmDef
{

    public static final String PASS_BY_REF = "URL_REF";
    public static final String PASS_BY_VALUE = "VALUE";

    public static final String DATASTREAM_INPUT = "fedora:datastreamInputType";
    public static final String USER_INPUT = "fedora:userInputType";
    public static final String DEFAULT_INPUT = "fedora:defaultInputType";

    public String parmName = null;
    public String parmType = null;
    public String parmDefaultValue = null;
    public String[] parmDomainValues = new String[0];
    public boolean parmRequired = true;
    public String parmLabel = null;
    public String parmPassBy = null;

    public MethodParmDef()
    {

    }
}