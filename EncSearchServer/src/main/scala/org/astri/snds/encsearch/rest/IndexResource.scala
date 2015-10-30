package org.astri.snds.encsearch.rest

import java.util.{List, HashMap}
import javax.ws.rs.{Path, POST, GET, PUT, Produces, Consumes}
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement
import javax.json.{Json, JsonObject, JsonValue, JsonNumber}

import scala.collection.JavaConversions.{mapAsScalaMap, collectionAsScalaIterable}

import org.astri.snds.encsearch.rest.params.{IndexAddParams}


@Path("/index")
class IndexResource {
  
  
  def insertUser(username:String, salt:String) {
    val conn = AppContext.dbConn
    val insertStatement = conn.prepareStatement("INSERT INTO users (username, salt) VALUES (?, ?)")
    insertStatement.setString(1, username)
    insertStatement.setString(2, salt)
    insertStatement.execute()
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
    
    if (req.keywords != null) req.keywords.foreach{kw => 
        kw.value.foreach{occur =>
          insertStatement.setString(1, kw.keyword)
          insertStatement.setString(2, req.doc_ids.get(Integer parseInt occur.doc_id))
          insertStatement.setInt(3, occur.count)
          insertStatement.addBatch()
        }
      }
    insertStatement.executeBatch()
    
    val response = Json.createObjectBuilder.
      add("result", true);
    
    // add or check user salt
    UserResource.userExists(req.username) match {
      case None => insertUser(req.username, req.salt)
      case Some(existingSalt) => if (existingSalt != req.salt) response.add("warning", "salt_mismatch")
    }
    
    response.build()
  }
  
}