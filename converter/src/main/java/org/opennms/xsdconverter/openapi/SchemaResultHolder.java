package org.opennms.xsdconverter.openapi;

import java.util.ArrayList;
import java.util.Iterator;

public class SchemaResultHolder {
	ArrayList<LineItem> lines;
	
	private class LineItem {
		int level;
		String msg;
		
		public LineItem(int level, String msg) {
			this.level = level;
			this.msg = msg;
		}
		
		public void write(SchemaWriter writer, int baseLevel) {
			writer.writeentry(baseLevel + level, msg);
		}
	}
	
	public SchemaResultHolder() {
		lines = new ArrayList<LineItem>();
	}
	
	public SchemaResultHolder(SchemaResultHolder parentParams) {
		lines = new ArrayList<LineItem>(parentParams.lines);
	}

	public void addLine(int level, String msg) {
		lines.add(new LineItem(level, msg));
	}
	
	public void writeLines(SchemaWriter writer, int baseLevel) {
		Iterator<LineItem> it = lines.iterator();
		while (it.hasNext()) {
			LineItem item = it.next();
			item.write(writer, baseLevel);
		}
	}

	public boolean isEmpty() {
		return lines.isEmpty();
	}
}
