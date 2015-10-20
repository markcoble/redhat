package org.coble.core.camel.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import junit.framework.Assert;

import org.apache.camel.*;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.test.blueprint.CamelBlueprintHelper;
import org.apache.camel.test.blueprint.CamelBlueprintTestSupport;
import org.apache.commons.io.IOUtils;
import org.coble.core.camel.test.helpers.JUnitHelper;
import org.junit.runners.Parameterized.Parameters;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamelBlueprintTestBase extends CamelBlueprintTestSupport {

    protected File scenarioInput;
    protected File scenarioControl;


    private static final Logger LOG = LoggerFactory.getLogger(CamelBlueprintTestBase.class);

    public CamelBlueprintTestBase() {

    }

    public CamelBlueprintTestBase(File scenarioInput, File scenarioControl) {

        this.scenarioInput = scenarioInput;
        this.scenarioControl = scenarioControl;

    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext answer = CamelBlueprintHelper.getOsgiService(getBundleContext(), CamelContext.class, 120 * 1000);
        // must override context so we use the correct one in testing
        context = (ModelCamelContext) answer;
        return answer;
    }

    /**
     * This is here to prevent the warnings on the non-generic Dictionary
     * exposed by CamelBlueprintTestSupport
     */
    protected String overrideProperties(Dictionary<String, String> props) throws Exception {
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected String useOverridePropertiesWithConfigAdmin(Dictionary props) throws Exception {
        return overrideProperties(props);
    }

    /**
     * this matches the pattern across line endings and whitespace etc..
     */
    public static boolean matchesAnywhere(String pattern, String valueToMatchAgainst) {
        return JUnitHelper.matchesAnywhere(pattern, valueToMatchAgainst);
    }

    public void stopRouteAndWait(final String routeId) {
        try {
            context.stopRoute(routeId);
            Wait.For(10, new ToHappen() {
                @Override
                public boolean hasHappened() {
                    return context.getRouteDefinition(routeId).getStatus(context).equals(ServiceStatus.Stopped);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void assertContains(String expected, String actual) {
        JUnitHelper.assertContains(expected, actual);
    }

    public void assertNotContains(String expected, String actual) {
        JUnitHelper.assertNotContains(expected, actual);
    }

    public String getResourceAsString(String resourceName) throws IOException {
        return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(resourceName));
    }

    public void writeDebug(String value, String filePath) throws FileNotFoundException, IOException {
        IOUtils.write(value, new FileOutputStream(new File(filePath)));
    }

    public void addMockToRouteEnd(String routeId, final String mockName) throws Exception {
        context.getRouteDefinition(routeId).adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveAddLast().to(mockName);
            }
        });
    }

    public void weaveStubResponse(String routeId, final String outputToStringMatcher, final String response) throws Exception {
        context.getRouteDefinition(routeId).adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                // weaveAddLast().to("mock:result");
                // replace "to" with processor to set the body content
                weaveByToString(".*" + outputToStringMatcher + ".*")
                        .after().process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                exchange.getIn().setBody(response);
                            }
                        });
            }
        });
    }

    public void assertEqualsXml(String expectedXML, String actualXML) throws TransformerException {
        JUnitHelper.assertEqualsXml(expectedXML, actualXML);
    }

    /**
     * Assert XML similar
     * 
     * @param expectedXML
     * @param actualXML
     */
    public static void assertSimilar(String expectedXML, String actualXML) {
        JUnitHelper.assertSimilarXml(expectedXML, actualXML);
    }

    /**
     * Gets a specific camel context using the searchId
     * 
     * @param searchId
     * @return
     * @throws Exception
     */
    public ModelCamelContext getCamelContext(String searchId) throws Exception {
        String filter = "(objectClass=org.apache.camel.CamelContext)";
        ServiceReference[] serviceReferences = getBundleContext().getAllServiceReferences(null, filter);

        for (ServiceReference serviceReference : serviceReferences) {
            Object service = getBundleContext().getService(serviceReference);
            if (service instanceof ModelCamelContext) {
                ModelCamelContext context = (ModelCamelContext) service;
                if (context.getName().equals(searchId)) {
                    return context;
                }
            }
        }

        throw new IllegalArgumentException("Unable to find the required CamelContext of id '" + searchId
                + "' - This may be caused by a race condition");
    }

    /**
     * Get the MockEndpoint for a given camel Context. This is handy when
     * loading more than one blueprint
     * 
     * @param ref
     * the mock endpoint reference.
     * @param contextID
     * the camel context ID.
     * @return
     */
    public MockEndpoint getMockEndpoint(String contextID, String ref) throws Exception {
        return (MockEndpoint) resolveMandatoryEndpoint(getCamelContext(contextID), ref);
    }

    /**
     * Gets Producer template for given Camel Context.
     * 
     * @param contextID
     * @return
     * @throws Exception
     */
    public ProducerTemplate getTemplate(final String contextID) throws Exception {
        return getCamelContext(contextID).createProducerTemplate();
    }

    // example "(Bundle-SymbolicName=*)" "(!(cn=Tim Howes))" "(&(" +
    // Constants.OBJECTCLASS + "=Person)(|(sn=Jensen)(cn=Babs J*)))"
    // "(o=univ*of*mich*)"
    @Override
    protected String getBundleFilter() {
        Collection<String> excludesFromTest = new HashSet<String>();
        excludesFromTest.add("io.fabric8.fabric-zookeeper");
        Collection<String> localExcludes = excludeBundles();
        if (localExcludes != null && !localExcludes.isEmpty()) {
            excludesFromTest.addAll(localExcludes);
        }

        if (excludeCXFBundles()) {
            excludesFromTest.add("org.apache.cxf.*");
        }

        if (excludeDroolsBundles()) {
            excludesFromTest.add("drools-service");
            excludesFromTest.add("drools-spec");
            excludesFromTest.add("org.drools.*");
        }

        if (excludesFromTest.isEmpty()) {
            return super.getBundleFilter();
        } else {
            StringBuilder filterBuilder = new StringBuilder("(&" + super.getBundleFilter());
            for (String symbolicName : excludesFromTest) {
                filterBuilder.append("(!(Bundle-SymbolicName=" + symbolicName + "))");
            }

            filterBuilder.append(")");
            return filterBuilder.toString();
        }
    }

    protected Collection<String> excludeBundles() {
        return null;
    }

    protected boolean excludeCXFBundles() {
        return false;
    }

    protected boolean excludeDroolsBundles() {
        return false;
    }

    /**
     * Load input and control scenarios for execution
     * 
     * @return
     */
//    @Parameters
//    public static Collection<Object[]> getFiles() {
//
//        // get all files in the input folder
//        final File inputFolder = new File(urlInput.getPath());
//
//        LOG.debug("inputFolder: " + inputFolder.getAbsolutePath());
//        TestBase.listFilesForFolder(inputFolder);
//
//        final List<File> inputFileList = Arrays.asList(inputFolder.listFiles());
//
//        // get all files in the control folder
//        final File controlFolder = new File(urlControl.getPath());
//
//        LOG.debug("controlFolder: " + controlFolder.getAbsolutePath());
//
//        TestBase.listFilesForFolder(controlFolder);
//
//        final List<File> controlFileList = Arrays.asList(controlFolder
//                .listFiles());
//
//        Collection<Object[]> params = new ArrayList<Object[]>();
//        for (File scenarioInput : inputFileList) {
//            File scenarioControl = controlFileList.get(inputFileList
//                    .indexOf(scenarioInput));
//            Object[] arr = new Object[] { scenarioInput, scenarioControl };
//            params.add(arr);
//        }
//        return params;
//    }
}
