package org.opennms.xsdconverter;

import org.junit.jupiter.api.Test;
import org.opennms.xsdconverter.XSDLoader;
import org.opennms.xsdconverter.openapi.SchemaHolder;

import java.io.File;

class XSDLoaderTest {
    @Test
    void processVacuumd() {
        XSDLoader loader = new XSDLoader();
        SchemaHolder processedSchema = loader.loadXSD(new File("test/testVacuumdSchema.xsd"));
        System.out.println("Result:\n" + processedSchema.generateJSONOpenapiDefinitions());
    }

    @Test
    void processPoller() {
        XSDLoader loader = new XSDLoader();
        SchemaHolder processedSchema = loader.loadXSD(new File("test/testPollerSchema.xsd"));
        System.out.println("Result:\n" + processedSchema.generateJSONOpenapiDefinitions());
    }

}