package org.astri.snds.encsearch.test

import java.net.URL
import java.nio.file.Paths

import org.scalatest._
import scala.collection.JavaConversions.{mapAsScalaMap, collectionAsScalaIterable}

import org.astri.snds.encsearch._

class FullSpec extends FlatSpec with Matchers {
  val rawkey = Array(1, 1, 1).map(_.toByte)
  val server = new URL("http://localhost:8080/EncSearchServer/rest")
  def clean() {
    // may not be the best idea
    TCommon.getDb().createStatement().execute("DELETE FROM occurs;")
  }
  
  def tryQuery(queryString:String) = {
    val queryEx = new KeywordExtractor(rawkey)
    queryEx.extractFromDocument("query", queryString)
    
    val query = new RemoteQuery(server)
    val result = query.searchKeywords(queryEx.index.keySet())
    
    result.map(Paths.get(_).getFileName.toString())
  }
  
  "Whole system" should "just work" in {
    clean()
      
    // build index
    val kwExtractor = new KeywordExtractor(rawkey)
    val mgr = new DocManager(TCommon.getData("FullSpec"), kwExtractor)
    mgr.search()
    
    val up = new IndexUploader(server)
    up.upload(kwExtractor.index);
    
    // queries
    tryQuery("nose tail")   should contain only ("crocodiles.txt", "mouse.txt")
    tryQuery("scary") should contain only ("crocodiles.txt")
    tryQuery("cute") should have size(0)
    tryQuery("whiskers") should contain only ("mouse.txt")
  }
  
}