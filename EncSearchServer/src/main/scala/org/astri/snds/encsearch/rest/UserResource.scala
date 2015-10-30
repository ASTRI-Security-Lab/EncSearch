package org.astri.snds.encsearch.rest

import java.util.{List, HashMap}
import javax.ws.rs.{Path, POST, GET, PUT, Produces, Consumes, PathParam}
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement
import javax.json.{Json, JsonObject, JsonValue, JsonNumber}

import scala.collection.JavaConversions.{mapAsScalaMap, collectionAsScalaIterable}

import org.astri.snds.encsearch.rest.params.{IndexAddParams}


@Path("/user")
class UserResource {
  
  @GET
  @Path("{username}")
	@Produces(Array(MediaType.APPLICATION_JSON))
  def get(@PathParam("username") username:String) = {
    val response = Json.createObjectBuilder()
    
    UserResource.userExists(username) match {
      case None => {
        response.add("result", "error")
        response.add("err_kind", "no_user")
      }
      case Some(salt) => {
        response.add("result", true)
        response.add("user", Json.createObjectBuilder().
            add("username", username).
            add("salt", salt))
      }
    }
    
    response.build()
  }
}

object UserResource {
  def userExists(username:String) = {
    val conn = AppContext.dbConn
    val findUserStatement = conn.prepareStatement("SELECT username, salt FROM users WHERE username = ?")
    findUserStatement.setString(1, username)
    val res = findUserStatement.executeQuery()
    res.next() match {
      case false => None
      case true => Some(res.getString(2))
    }
  }
}