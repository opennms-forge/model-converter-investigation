package org.opennms.configengine.validation;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.report.ValidationReport.Message;

public class SwaggerValidator {
	private OpenApiInteractionValidator openApiValidator;
	public SwaggerValidator(String spec) {
		openApiValidator = OpenApiInteractionValidator.createForInlineApiSpecification(spec).build();
	}
	
	public boolean isRequestValid(HttpServletRequest req, String body) {
		Method method = Request.Method.valueOf(req.getMethod());
		Request request = null;
		
		switch(method) {
			case POST:
				request = SimpleRequest.Builder
									.post(req.getServletPath())
									.withBody(body)
									.withContentType(req.getContentType())
									.build();
				break;
			case PATCH:
				request = SimpleRequest.Builder
						.patch(req.getServletPath())
						.withBody(body)
						.withContentType(req.getContentType())
						.build();
				break;
			case PUT:
				request = SimpleRequest.Builder
						.put(req.getServletPath())
						.withBody(body)
						.withContentType(req.getContentType())
						.build();
				break;
			case GET:
				request = SimpleRequest.Builder
						.get(req.getServletPath())
						.withBody(body)
						.withContentType(req.getContentType())
						.build();
				break;
			case DELETE:
				request = SimpleRequest.Builder
						.delete(req.getServletPath())
						.withBody(body)
						.withContentType(req.getContentType())
						.build();
				break;
		}
		
		ValidationReport report = openApiValidator.validateRequest(request);

		if (report != null) {
			if (!report.hasErrors()) {
				return true;
			} else {
				List<Message> msgs = report.getMessages();
				Iterator<Message> it = msgs.iterator();
				while (it.hasNext()) {
					Message msg = it.next();
					System.out.println("Error: " + msg);
				}
			}
		}

		return false;
	}
}
