package io.spring.cloud.samples.animalrescue.backend.fixtures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.spring.cloud.samples.animalrescue.backend.AdoptionRequest;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class AdoptionCenterFixtures implements BeforeAllCallback, AfterAllCallback, AfterEachCallback {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	public static final String RESPONSES_RESOURCE_PATH = "classpath:/wiremock/";
	private final ResourceLoader resourceLoader;
	private final WireMockServer wireMock;

	public AdoptionCenterFixtures(int port) {
		this.resourceLoader = new DefaultResourceLoader();
		this.wireMock = new WireMockServer(options().port(port)
		                                            .usingFilesUnderClasspath(RESPONSES_RESOURCE_PATH)); ;
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		this.wireMock.stop();
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		this.wireMock.resetAll();
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		this.wireMock.start();
	}

	public void stubAddAdoptionWithDefaultResponse() {
		wireMock.stubFor(post(urlEqualTo("/adoption-requests"))
			.willReturn(aResponse()
				.withStatus(201)
				.withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.withBody(readResponseFromFile("add-adoption-response"))));
	}

	public void stubAddAdoptionWithResponse(AdoptionRequest expectedResponse) throws JsonProcessingException {
		wireMock.stubFor(post(urlEqualTo("/adoption-requests"))
			.willReturn(aResponse()
				.withStatus(201)
				.withBody(objectMapper.writeValueAsString(expectedResponse))));
	}

	public void stubAddAdoptionResponse(Consumer<AdoptionRequest> requestCustomizer) throws JsonProcessingException {
		AdoptionRequest response = objectMapper.readValue(readResponseFromFile("add-adoption-response"), new TypeReference<>() {});
		if (requestCustomizer != null) {
			requestCustomizer.accept(response);
		}

		wireMock.stubFor(post(urlEqualTo("/adoption-requests"))
			.willReturn(aResponse()
				.withStatus(201)
				.withBody(objectMapper.writeValueAsString(response))));
	}

	public void stubAddAdoptionWith400Response() {
		wireMock.stubFor(post(urlEqualTo("/adoption-requests"))
			.willReturn(aResponse()
				.withStatus(400)
				.withBody("Error")));
	}

	public AdoptionRequest getDefaultResponse() throws JsonProcessingException {
		return objectMapper.readValue(readResponseFromFile("add-adoption-response"), AdoptionRequest.class);
	}

	public void verifyAddAdoptionCalledWithBody(AdoptionRequest adoptionRequest, Consumer<AdoptionRequest> requestCustomizer) throws JsonProcessingException {
		AdoptionRequest response = objectMapper.convertValue(adoptionRequest, new TypeReference<>() {});
		if (requestCustomizer != null) {
			requestCustomizer.accept(response);
		}
		wireMock.verify(postRequestedFor(urlPathEqualTo("/adoption-requests"))
			.withRequestBody(equalToJson(objectMapper.writeValueAsString(response), true, true))
		);
	}

	public void verifyDeleteAdoptionCalledWith(Long animalId, Long adoptionRequestId, String adopterName) {
		wireMock.verify(deleteRequestedFor(urlPathEqualTo("/adoption-requests/" + adoptionRequestId))
			.withQueryParam("animalId", equalTo(Long.toString(animalId)))
			.withQueryParam("adopter", equalTo(adopterName)));
	}

	private String readResponseFromFile(String filePath) {
		try {
			Resource resource = resourceLoader.getResource(RESPONSES_RESOURCE_PATH + filePath + ".json");
			InputStreamReader reader = new InputStreamReader(resource.getInputStream());
			return new BufferedReader(reader)
				.lines()
				.collect(Collectors.joining("\n"));
		}
		catch (IOException e) {
			throw new RuntimeException("Error loading resource from location " + filePath, e);
		}
	}
}
