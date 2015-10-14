package org.astri.snds.encsearch;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import org.astri.snds.encsearch.rest.ServiceJsonError;

public class IndexUploader {
	private URI host;

	public IndexUploader(URL host_) throws URISyntaxException {
		host = host_.toURI();
	}

	private JsonObject buildJsonRequest(HashMap<String, HashMap<String, Integer>> index) {
		JsonObjectBuilder gen = Json.createObjectBuilder();
		JsonArrayBuilder docIdsJson = Json.createArrayBuilder();

		// documents are referenced by an ID to reduce JSON size
		HashMap<String, Integer> docIds = new HashMap<String, Integer>();
		int lastDocIx = 0;
		for (HashMap<String, Integer> occ : index.values()) {
			for (String doc : occ.keySet()) {
				if (docIds.containsKey(doc)) continue;
				
				docIds.put(doc, lastDocIx);
				docIdsJson.add(doc);
				lastDocIx += 1;
			}
		}
		gen.add("doc_ids", docIdsJson);
		
		JsonObjectBuilder kwsJson = Json.createObjectBuilder();

		// start outputting the index
		index.forEach((kw, docs) -> {
			JsonObjectBuilder json = Json.createObjectBuilder();
			docs.forEach((doc, cnt) -> json.add(docIds.get(doc).toString(), cnt));
			kwsJson.add(kw, json);
		});

		gen.add("keywords", kwsJson);
		return gen.build();
	}
	
	public void upload(HashMap<String, HashMap<String, Integer>> index) throws ServiceJsonError {
		JsonObject requestJson = buildJsonRequest(index);
		
		WebTarget target = ClientBuilder.newClient().target(host).path("index/add");
		JsonObject responseJson = target.request().post(Entity.json(requestJson), JsonObject.class);

		if (responseJson.get("result").equals(true)) {
			return;  // okey
		
		} else {
			throw new ServiceJsonError(responseJson);
		}
	}
}
