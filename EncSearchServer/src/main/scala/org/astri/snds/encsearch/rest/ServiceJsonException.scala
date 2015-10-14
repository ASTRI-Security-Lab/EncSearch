package org.astri.snds.encsearch.rest

import javax.json.Json;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response

class ServiceJsonException(status:Int, err_kind:String, cause:Throwable) 
  extends WebApplicationException(cause, Response.status(status).
				`type`(MediaType.APPLICATION_JSON).
				entity(Json.createObjectBuilder()
					.add("result", "error")
					.add("err_kind", err_kind)
				).build())
{	
	def this(status:Int, err_kind:String) = this (status, err_kind, null)
}