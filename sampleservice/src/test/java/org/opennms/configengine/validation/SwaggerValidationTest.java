package org.opennms.configengine.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.report.ValidationReport.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
public class SwaggerValidationTest  {
	String sampleSpec = "src/models/simpleSwagger.yaml";
	String vacuumdSpec = "src/models/vacuumd.yaml";
	String sampleVacuumdPayload = "src/test/resources/sampleVacuumd.json";
	
	private String loadFile(String filepath) throws IOException {
		return new String(Files.readAllBytes(Paths.get(filepath))); 
	}
	
	@Test
	public void TestSwaggerUrls() throws IOException {
		String swagger = loadFile(sampleSpec);
		OpenApiInteractionValidator validator = OpenApiInteractionValidator.createForInlineApiSpecification(swagger).build();
		
		final Request goodRequest = SimpleRequest.Builder.get("/v1/pets").build();
	    ValidationReport report = validator.validateRequest(goodRequest);
		assertFalse(report.hasErrors());
		
		final Request badRequest = SimpleRequest.Builder.get("/v1/wrongurl").build();
	    report = validator.validateRequest(badRequest);
		assertTrue(report.hasErrors());
	}
	
	@Test
	public void TestSwaggerValidType() throws IOException {
		String swagger = loadFile(sampleSpec);
		OpenApiInteractionValidator validator = OpenApiInteractionValidator.createForInlineApiSpecification(swagger).build();
		
		String postbody = "{ \"id\": 0, \"name\": \"somestring\", \"tag\": \"sometag\" }";
		final Request request = SimpleRequest.Builder.post("/pets").withBody(postbody).withContentType("application/json").build();
	    ValidationReport report = validator.validateRequest(request);
		assertFalse(report.hasErrors());
	}
	
	@Test
	public void TestSwaggerInvalidType() throws IOException {
		String swagger = loadFile(sampleSpec);
		OpenApiInteractionValidator validator = OpenApiInteractionValidator.createForInlineApiSpecification(swagger).build();
		
		String postbody = "{ \"id\": 0, \"badkey\": \"something\" }";
		final Request request = SimpleRequest.Builder.post("/v1/pets").withBody(postbody).withContentType("application/json").build();
	    ValidationReport report = validator.validateRequest(request);
		assertTrue(report.hasErrors());
	}

	@Test
	public void TestVacuumdModel() throws IOException {
		String swagger = loadFile(vacuumdSpec);
		OpenApiInteractionValidator validator = OpenApiInteractionValidator.createForInlineApiSpecification(swagger).build();
		
		String body = loadFile(sampleVacuumdPayload);
		
		Request request = SimpleRequest.Builder.post("/vacuumd").withBody(body).withContentType("application/json").build();
		ValidationReport report = validator.validateRequest(request);
		checkNoValidationErrors(report);
	}

	private void checkNoValidationErrors(ValidationReport report) {
		if (report.hasErrors()) {
			List<Message> msgs = report.getMessages();
			Iterator<Message> it = msgs.iterator();
			while (it.hasNext()) {
				Message msg = it.next();
				System.out.println("Unexpected error:" + msg.getMessage());
			}
			assertFalse(report.hasErrors());
		}
	}
}
