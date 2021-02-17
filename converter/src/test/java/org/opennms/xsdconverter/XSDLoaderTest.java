package org.opennms.xsdconverter;

import org.junit.jupiter.api.Test;
import org.opennms.xsdconverter.XSDLoader;
import org.opennms.xsdconverter.openapi.SchemaHolder;

import java.io.File;

public class XSDLoaderTest {
    @Test
    public void testProcessVacuumd() {
        XSDLoader loader = new XSDLoader();
        SchemaHolder processedSchema = loader.loadXSD(new File("src/test/models/testVacuumdSchema.xsd"));
        System.out.println("Result:\n" + processedSchema.generateJSONOpenapiDefinitions());
    }

    @Test
    public void testProcessPoller() {
        XSDLoader loader = new XSDLoader();
        SchemaHolder processedSchema = loader.loadXSD(new File("src/test/models/testPollerSchema.xsd"));
        System.out.println("Result:\n" + processedSchema.generateJSONOpenapiDefinitions());
    }

}