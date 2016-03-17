package org.astri.snds.encsearch.rest

import org.scalatest._
import java.sql.ResultSet
import javax.json.{Json, JsonObject, JsonString}

import org.astri.snds.encsearch.rest._
import org.astri.snds.encsearch.rest.params._
import org.astri.snds.encsearch.rest.params.IndexAddParams.OccurenceParams

import scala.collection.JavaConversions.{mapAsScalaMap, collectionAsScalaIterable, seqAsJavaList}

class IndexSpec extends FlatSpec with Matchers {
  
  object Fixture {
      val api = new IndexResource()
      val search = new SearchResource()
      val app = new AppContext()
      app.contextInitialized(null)
      val conn = AppContext.dbConn
      
      def clean() = {
        // may not be the best idea
        conn.createStatement().execute("DELETE FROM occurs;")
      }
      
      def trySearch(kws:List[String]) = {
        val res = search.search(new SearchParams(kws))
        assert(res.getBoolean("result") == true)
        res.getJsonArray("documents").map { x => x.asInstanceOf[JsonString].getString }
      }
  }
  
  def fixture = {
    Fixture.clean()
    Fixture
  }
  
  def mkOccur(docId:Int, cnt:Int) = {
    val r = new IndexAddParams.OccurenceParams()
    r.count = cnt
    r.doc_id = docId.toString
    r
  }
  
  def mkKeyword(kw:String, oc:List[IndexAddParams.OccurenceParams]) = {
    val r = new IndexAddParams.KeywordParams()
    r.keyword = kw
    r.value = oc
    r
  }
  
  def readQuery(resultSet:ResultSet) = {
    new Iterator[String] {
      def hasNext = resultSet.next()
      def next() = resultSet.getString(1)
    }.toList
  }
  
  def prepareData(api:IndexResource) = {
    val addReq = new IndexAddParams()
    addReq.username = "tester"
    addReq.salt = "sss"
    addReq.doc_ids = List("birds.txt", "cats.txt")
    val inBird = mkOccur(0, 1)
    val inCat = mkOccur(1, 1)
    
    addReq.keywords = List(
        mkKeyword("claws", List(inBird)),
        mkKeyword("eyes", List(inBird, inCat))
    )

    api.add(addReq)
  }
  
  "The index" should "contain rows that were inserted with API" in {
    val f = fixture
    val inDog = mkOccur(0, 1)
    val inCat = mkOccur(1, 1)

    val addReq = new IndexAddParams()
    addReq.username = "tester"
    addReq.salt = "sss"
    addReq.doc_ids = List("dogs.txt", "cats.txt")
    addReq.keywords = List(
        mkKeyword("ear",List(inDog, inCat)),
        mkKeyword("paw",List(inDog, inCat)),
        mkKeyword("cute",List(inCat)))

    f.api.add(addReq)
    
    val q = AppContext.dbConn.createStatement()
    val rs = readQuery(q.executeQuery("SELECT * FROM occurs WHERE doc_name = 'dogs.txt';"))
    assert(rs == List("ear", "paw"))
    val rs2 = readQuery(q.executeQuery("SELECT * FROM occurs WHERE doc_name = 'cats.txt';"))
    assert(rs2 == List("ear", "paw", "cute"))
  }
  
  "The index" should "search correctly" in {
    val f = fixture
    prepareData(f.api)
    val res = f.trySearch(List("claws"))
    res should equal( List("birds.txt") )
  }
  
  "The index" should "search multiple keywords" in {
    val f = fixture
    prepareData(f.api)
    val res = f.trySearch(List("claws", "eyes"))
    res should equal( List("birds.txt") )
  }
  
  "The index" should "not find not existing keywords" in {
    val f = fixture
    prepareData(f.api)
    val res = f.trySearch(List("wheels"))
    res should equal( List() )
  }
}