package org.astri.snds.encsearch.rest

import java.util.{List, HashMap}
import javax.ws.rs.{Path, POST, GET, PUT, Produces, Consumes}
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement
import javax.json.{Json, JsonValue, JsonNumber}

import scala.collection.JavaConversions.{mapAsScalaMap, collectionAsScalaIterable}
import org.astri.snds.encsearch.rest.params.SearchParams;

@Path("/search")
class SearchResource {
  
  val QUERY_MAX_KEYWORDS = 5

  @PUT
	@Produces(Array(MediaType.APPLICATION_JSON))
	@Consumes(Array(MediaType.APPLICATION_JSON))
	def search(req:SearchParams) = {
    val conn = AppContext.dbConn
    if (req.keywords.size() > QUERY_MAX_KEYWORDS) throw new ServiceJsonException(400, "too_many_keywords")
    if (req.keywords.size() == 0) throw new ServiceJsonException(400, "no_keywords")
    
    // build the query by adding keywords successively
    val subQueryTemplate = "SELECT doc_name FROM occurs AS oc_%1$d WHERE (oc_%1$d.keyword = ?) AND (oc_1.doc_name = oc_%1$d.doc_name)"
    //val query = (2 to req.keywords.size).foldLeft[String] (subQueryTemplate format 1) ((a:String, ix:Int) => a + " AND EXISTS " + subQueryTemplate.format(ix))
    val query2 = (1 to req.keywords.size).foldRight[String] ("SELECT true") ((ix:Int, a:String) => subQueryTemplate.format(ix) + " AND EXISTS (" + a + ")")
    val statement = conn.prepareStatement(query2)
    req.keywords.zipWithIndex.foreach{ case (kw, ix) => statement.setString(ix + 1, kw) }
    val resultSet = statement.executeQuery()
    val resultJson = Json.createArrayBuilder()
    
    while (resultSet.next()) {
      resultJson.add(resultSet.getString("doc_name"))
    }
    
    Json.createObjectBuilder().
      add("result", true).
      add("documents", resultJson).
      build()
  }
  
}