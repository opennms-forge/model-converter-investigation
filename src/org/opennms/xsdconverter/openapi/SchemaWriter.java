package org.opennms.xsdconverter.openapi;

import java.util.Formatter;

public class SchemaWriter {
    StringBuffer buf = new StringBuffer();
    Formatter sformat = new Formatter(buf);

    public SchemaWriter() {
    }

    public void writeentry(int level, String str) {
        if (level == 0) {
            sformat.format("%s\n", str);
        } else {
            sformat.format("%-" + (2 * level) + "s%s\n", " ", str);
        }
    }

    public String getResult() {
        return buf.toString();
    }
}
