package org.opennms.xsdconverter;

import org.junit.jupiter.api.Test;

import java.io.File;

class XSDConverterTest_OBSOLETE {

    @Test
    void parseXSD() {
        XSDConverter_OBSOLETE converter = new XSDConverter_OBSOLETE(new File("testSchema.xsd"));
        converter.parseXSD();
    }
}