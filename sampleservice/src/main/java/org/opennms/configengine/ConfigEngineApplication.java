package org.opennms.configengine;

import java.util.HashSet;
import java.util.Set;

import org.opennms.configengine.types.GenericDataConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.converter.Converter;

@SpringBootApplication
public class ConfigEngineApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ConfigEngineApplication.class, args);
	}

	/* @Bean(name = "conversionService")
	public ConversionServiceFactoryBean getConversionService(GenericDataConverter dataConverter) {
		ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();

		Set<Converter> converters = new HashSet<>();

		converters.add(dataConverter);

		System.out.println("---------JER Registered converter");
		bean.setConverters(converters);
		return bean;
	} */
}
