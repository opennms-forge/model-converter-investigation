package org.opennms.xsdconverter.openapi;

import java.util.Formatter;

public class SchemaWriter {
    private StringBuffer buf = new StringBuffer();
    private Formatter sformat = new Formatter(buf);

    private boolean autoCommas = true;
    private int previousLineLevel = -1;
    private String previousLine = "";

    public SchemaWriter(boolean automaticJsonCommas) {
        autoCommas = automaticJsonCommas;
    }

    public void writeentry(int level, String str) {
        if (previousLineLevel != -1) {
            String comma;
            if (autoCommas && (level == previousLineLevel)) {
                comma = ",";
            } else {
                comma = "";
            }
            if (previousLineLevel == 0) {
                sformat.format("%s\n", previousLine + comma);
            } else {
                sformat.format("%-" + (2 * previousLineLevel) + "s%s\n", " ", previousLine + comma);
            }
        }
        previousLine = str;
        previousLineLevel = level;
    }

    private void closeWriting() {
        if (previousLine != null && !previousLine.isEmpty()) {
            writeentry(-1, "");
        }
    }

    public String getResult() {
        closeWriting();
        return buf.toString();
    }
}
