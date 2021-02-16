package org.opennms.configengine.types;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonToken;

@Component
public class GenericDataConverter implements Converter<Object, List<String>> {

	@Override
	public List<String> convert(Object source) {
		System.out.println("CONVERTING!!!");
		/*List<String> construct = new ArrayList<String>();
		String[] strs = source.split(" ");
		for (int i = 0; i < strs.length; i++) {
			construct.add(strs[i]);
		}
		return construct; */
		return new ArrayList<String>();
	}

}
