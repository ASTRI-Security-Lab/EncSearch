package org.astri.snds.encsearch.rest

import java.util.{List, HashMap}
import javax.ws.rs.{Path, POST, GET, PUT, Produces, Consumes}
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement
import javax.json.{Json, JsonObject, JsonValue, JsonNumber}

import scala.collection.JavaConversions.{mapAsScalaMap, collectionAsScalaIterable}

import org.astri.snds.encsearch.rest.params.{TestParams, IndexAddParams}


@Path("/index")
class IndexResource {
  
  @GET
  def main():String = {
    "Hello"
  }
  
  @POST
  @Path("test")  // TODO: maybe should not use verb in REST url
	@Produces(Array(MediaType.APPLICATION_JSON))
	@Consumes(Array(MediaType.APPLICATION_JSON))
	def test(req:TestParams):String = {
    req.toString()
  }
  

  @PUT
	@Produces(Array(MediaType.APPLICATION_JSON))
	@Consumes(Array(MediaType.APPLICATION_JSON))
  def add(req:IndexAddParams) = {
    // insert documents first
    val conn = AppContext.dbConn
    val insertStatement = conn.prepareStatement("""
      INSERT INTO occurs (keyword, doc_name, count)
      VALUES (?, ?, ?)
    """)
    
    req.keywords.foreach{kw => 
      kw.value.foreach{occur =>
        insertStatement.setString(1, kw.keyword)
        insertStatement.setString(2, req.doc_ids.get(Integer parseInt occur.doc_id))
        insertStatement.setInt(3, occur.count)
        insertStatement.addBatch()
      }
    }
    insertStatement.executeBatch()
    
    Json.createObjectBuilder.
      add("result", true).
      build()
  }
  
}