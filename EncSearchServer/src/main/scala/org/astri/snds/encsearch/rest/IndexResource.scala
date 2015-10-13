package org.astri.snds.encsearch.rest

import javax.ws.rs.{Path, POST, GET}
import javax.json.JsonObject


@Path("/index")
class IndexResource {
  
  @GET
  def main():String = {
    "Hello"
  }

  @POST
  @Path("add")
  def add(req:JsonObject) {
    //AppContext.dbConn.q
     
  }
  
}