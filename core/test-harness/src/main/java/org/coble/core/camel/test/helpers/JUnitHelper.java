package org.coble.core.camel.test.helpers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import junit.framework.Assert;

public class JUnitHelper {

    private static final Logger LOG = LoggerFactory.getLogger(JUnitHelper.class);
    protected File scenarioInput;
    protected File scenarioControl;
    
    public JUnitHelper(File scenarioInput, File scenarioControl){
    this.scenarioInput = scenarioInput;
    this.scenarioControl = scenarioControl;

}
    /**
     * this matches the pattern across line endings and whitespace etc..
     */
    public static boolean matchesAnywhere(String pattern, String valueToMatchAgainst) {
        return Pattern.compile(pattern, Pattern.DOTALL).matcher(valueToMatchAgainst).matches();
    }

    public static void assertContains(String expected, String actual) {
        Assert.assertTrue(
                "The actual did not contain the expected string [expected = " + expected + ", actual = " + actual + "]",
                actual.contains(expected));
    }

    public static void assertNotContains(String expected, String actual) {
        Assert.assertFalse("The actual contained the expected string which it should not have [expected = " + expected
                + ", actual = " + actual + "]",
                actual.contains(expected));
    }

    public static void assertEqualsXml(String expectedXML, String actualXML) throws TransformerException {

        expectedXML = expectedXML.replaceAll("api:", ""); // remove namespace
                                                          // for easy
                                                          // comparison
        actualXML = actualXML.replaceAll("api:", ""); // remove namespace for
                                                      // easy comparison

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreComments(true);
        DetailedDiff diff;
        try {
            diff = new DetailedDiff(XMLUnit.compareXML(expectedXML, actualXML));
            diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
            List<?> allDifferences = diff.getAllDifferences();
            Assert.assertEquals("Differences found: " + diff.toString(), 0, allDifferences.size());
        } catch (SAXException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    public static void assertSimilarXml(String expectedXML, String actualXML) {
        Assert.assertTrue(xmlIs(expectedXML, actualXML, xmlComparison.similar));
    }

    public enum xmlComparison {
        similar, identical
    }

    public static boolean xmlIs(String expectedXML, String actualXML, xmlComparison comparison) {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreComments(true);
        DetailedDiff diff;
        try {
            diff = new DetailedDiff(XMLUnit.compareXML(expectedXML, actualXML));
            diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
            switch (comparison) {
                case identical:
                    return diff.identical();
                case similar:
                    return diff.similar();
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String marshal(Object object) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(object.getClass());
        Marshaller jaxbMarshaller = ctx.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter out = new StringWriter();
        jaxbMarshaller.marshal(object, out);
        return out.toString();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> T unmarshal(InputStream is, Class clazz) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(clazz);
        Unmarshaller m = ctx.createUnmarshaller();
        return (T) m.unmarshal(is);
    }

    /**
     * @param resultType
     * - use XPathConstants.{the-type-you-want}
     */
    @SuppressWarnings("unchecked")
    public <T> T applyXPath(String xml, String xpathExpression, javax.xml.namespace.QName resultType) throws SAXException,
            IOException, ParserConfigurationException, XPathExpressionException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
        return (T) XPathFactory.newInstance().newXPath().evaluate(xpathExpression, doc, resultType);
    }

    public static void listFilesForFolder(File paramFile)
    {
        for (File localFile : paramFile.listFiles())
            if (localFile.isDirectory())
                listFilesForFolder(localFile);
            else
                LOG.info(localFile.getName());

    }

    public static String convertFileToString(File paramFile)
            throws Exception
    {
        BufferedReader localBufferedReader = new BufferedReader(new FileReader(paramFile));
        StringBuilder localStringBuilder = new StringBuilder();
        String str1 = "";
        for (str1 = localBufferedReader.readLine(); str1 != null; str1 = localBufferedReader.readLine())
        {
            str1 = str1.trim();
            localStringBuilder.append(str1);
        }
        String str2 = localStringBuilder.toString();
        localBufferedReader.close();
        return str2;
    }

}
