package org.astri.snds.encsearch.rest

import java.util.{List, HashMap}
import javax.ws.rs.{Path, POST, GET, PUT, Produces, Consumes, PathParam}
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement
import javax.json.{Json, JsonObject, JsonValue, JsonNumber}

import scala.collection.JavaConversions.{mapAsScalaMap, collectionAsScalaIterable}

import org.astri.snds.encsearch.rest.params.{EncHeaderParams}


@Path("/encheaders")
class EncHeaderResource { 
  
  @PUT
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.APPLICATION_JSON))  
  def insertHeaders(enc:EncHeaderParams){  
    val conn = AppContext.dbConn
    val insertStatement = conn.prepareStatement("INSERT INTO metadata (doc_name,iv,version,salt,iterations,hmac,name_hmac) VALUES (?, ?, ?, ?, ?, ?, ?)")
    insertStatement.setString(1,enc.docName)
    insertStatement.setString(2,enc.iv)
    insertStatement.setInt(3,enc.ver)
    insertStatement.setString(4,enc.salt)
    insertStatement.setInt(5,enc.iterations)
    insertStatement.setString(6, enc.hmac)
    insertStatement.setString(7,enc.name_hmac)
    insertStatement.execute()   
    
     val response = Json.createObjectBuilder.add("result",true);
     response.build(); 
  }
  
  @GET
  @Path("/search/{param}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def searchEncHeaders(@PathParam("param") doc_name:String) = {  
    val conn = AppContext.dbConn
    val searchStatement = conn.prepareStatement("SELECT doc_name,iv,version,salt,iterations,hmac,name_hmac FROM metadata WHERE doc_name = ?")
    searchStatement.setString(1, doc_name)
    val result = searchStatement.executeQuery()
    val response = Json.createObjectBuilder()
    result.next() match {
      case false => {
        response.add("result","error")
        response.add("err_kind", "record_not_found")
      }
      case true => {
        response.add("result","true")
        response.add("record", Json.createObjectBuilder().
            add("doc_name", result.getString("doc_name")).
            add("iv", result.getString("iv")).
            add("version", result.getString("version")).
            add("salt", result.getString("salt")).
            add("iterations", result.getString("iterations")).
            add("hmac", result.getString("hmac")).
            add("name_hmac", result.getString("name_hmac")))
      }     
    }
    response.build()
  }
  
  @GET
  @Path("/docs/{param}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Consumes(Array(MediaType.APPLICATION_JSON))
  def searchDocs(@PathParam("param") mySalt:String) = {
    val conn = AppContext.dbConn
    
    val searchStatement = conn.prepareStatement("SELECT doc_name,iv,version,salt,iterations,hmac,name_hmac FROM metadata WHERE salt = ?")
    searchStatement.setString(1, mySalt)
    val resultSet = searchStatement.executeQuery()
    val response = Json.createObjectBuilder()
    val jsonArray = Json.createArrayBuilder()
    val jfinal = Json.createObjectBuilder
    
    while (resultSet.next()){
      jsonArray.add(
            response.add("doc_name", resultSet.getString("doc_name")).
            add("iv", resultSet.getString("iv")).
            add("version", resultSet.getString("version")).
            add("salt", resultSet.getString("salt")).
            add("iterations", resultSet.getString("iterations")).
            add("hmac", resultSet.getString("hmac")).
            add("name_hmac", resultSet.getString("name_hmac")))
    } 
        
    jfinal.add("records",jsonArray)
    jfinal.build()
  }
}
  
