package fedora.test.integration;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;
import fedora.server.config.ServerConfiguration;
import fedora.server.config.ServerConfigurationParser;
import fedora.test.FedoraConfigurationTestSetup;
import fedora.test.FedoraTestCase;
import fedora.test.SuiteQuux;
import fedora.utilities.FileComparator;

/**
 * A specialized JUnit TestCase for end-to-end testing of multiple Fedora 
 * server configurations.
 * 
 * This class uses FedoraConfigurationTestSetup to run a given TestSuite through 
 * a series of ServerConfiguration objects.
 * 
 * The ServerConfigurations are generated by applying override values defined in
 * Properties files whose names begin with "junit".
 * 
 * @author Edwin Shin
 */
public class TestFedoraConfigurations extends FedoraTestCase {
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        
        // Add the TestSuite that should be run through once for each 
        // configuration
        addTestToSuite(suite, SuiteQuux.suite());
        
        return suite;
    }
    
    private static void addTestToSuite(TestSuite suite, Test test) throws Exception {
        Iterator configs = getConfigurations().iterator();
        while (configs.hasNext()) {
            suite.addTest(new FedoraConfigurationTestSetup(test, 
                    (ServerConfiguration)configs.next()));
        }
    }
    
    private static List getConfigurations() throws Exception {
        FileInputStream fis = new FileInputStream(FCFG_SRC);
        ServerConfigurationParser scp = new ServerConfigurationParser(fis);
        ServerConfiguration config = scp.parse();
        
        List configs = new ArrayList();
        configs.add(config);
        File propDir = new File(FCFG_SRC_DIR);
        if (propDir.exists() && propDir.isDirectory()) {
            File[] files = propDir.listFiles();
            Arrays.sort(files, new FileComparator());
            int count = files.length;
            for (int i = 0; i < count; i++) { //for each file:
                File f = files[i];
                if (f.isFile() && 
                    f.getName().endsWith(".properties") && 
                    f.getName().startsWith("junit")) {
                    
                    FileInputStream pfis = new FileInputStream(f);
                    Properties props = new Properties();
                    props.load(pfis);
                    ServerConfiguration copy = config.copy();
                    copy.applyProperties(props);
                    configs.add(copy);
                }
            }//next file
        }
        return configs;
    }
    
}