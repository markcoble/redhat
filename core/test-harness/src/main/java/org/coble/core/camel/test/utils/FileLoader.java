package org.coble.core.camel.test.utils;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 16/07/13
 * Time: 16:03
 * To change this template use File | Settings | File Templates.
 */
public class FileLoader {

    /**
     * Load a file and convert its contents to a string
     * @param fileName
     * @return contents of the file
     * @throws IOException
     */
    public static String loadFileAsString(String fileName) throws IOException {
        InputStream is = FileLoader.class.getResourceAsStream(fileName);
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF-8");
        String string = writer.toString();

        try {
            is.close();
            writer.close();
        } catch (Exception e) {
        }

        return string;
    }

    /**
     * Load a file as a Document object
     * @param filePath
     * @return contents of the file in Document Object
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static Document loadFileAsDocument(String filePath) throws ParserConfigurationException, IOException, SAXException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(loadFileAsString(filePath).getBytes()));
    }
}
