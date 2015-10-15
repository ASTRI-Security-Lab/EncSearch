package org.astri.snds.encsearch;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import org.astri.snds.encsearch.rest.JsonServiceReqException;

public class RemoteQuery {
	
	private URI host;
	
	public RemoteQuery(URL host_) throws URISyntaxException {
		host = host_.toURI();
	}

	// returns list of docName
	public List<String> searchKeywords(Collection<String> keywords) throws JsonServiceReqException {
		WebTarget target = ClientBuilder.newClient().target(host).path("search");

		JsonArrayBuilder kwJson = Json.createArrayBuilder();
		keywords.forEach((kw) -> kwJson.add(kw));

		JsonObject requestJson = Json.createObjectBuilder()
				.add("username", "cassandra")
				.add("keywords", kwJson.build())
				.build();

		JsonObject responseJson = target.request().put(Entity.json(requestJson), JsonObject.class);
		if (responseJson.get("result").equals(JsonValue.TRUE)) {
			return responseJson.getJsonArray("documents").stream()
				.map((i) -> ((JsonString)i).getString())
				.collect(Collectors.toList());
			
		} else {
			throw new JsonServiceReqException(responseJson);
		}
	}

}
