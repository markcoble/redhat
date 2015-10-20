package org.coble.core.camel.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractXmlScenarioTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractXmlScenarioTest.class);

    protected File scenarioInput;
    protected File scenarioControl;

    public AbstractXmlScenarioTest(File scenarioInput, File scenarioControl) {

        this.scenarioInput = scenarioInput;
        this.scenarioControl = scenarioControl;

    }

    protected static void listFilesForFolder(File paramFile)
    {
        for (File localFile : paramFile.listFiles())
            if (localFile.isDirectory())
                listFilesForFolder(localFile);
            else
                LOG.info(localFile.getName());

    }

    protected static String convertFileToString(File paramFile)
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
        return str2;
    }
}
