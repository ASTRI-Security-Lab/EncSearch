package org.astri.snds.encsearch.test

import java.nio.file.Paths
import org.scalatest._
import org.astri.snds.encsearch.{KeywordExtractor, DocManager}
import org.astri.snds.encsearch.IKeywordExtractor


class ExtractSpec extends FlatSpec with Matchers {
  "Extracted words" should "be normalized lowercase singular" in {
    val ex = new KeywordExtractor(Array(1, 1, 1))
    ex.extractFromDocument("cats.txt", "Claws claws CLAWS claw Claw claW")
    
    // only have indirect ways to verify that
    ex.index should have size(1)
    ex.extractFromDocument("cats.txt", "Paws")
    ex.index should have size(2)
    ex.extractFromDocument("cats.txt", "paws")
  }
  
  val ex = new IKeywordExtractor {
    var received:String = null
    override def extractFromDocument(docName:String, contents:String) = {
      received = contents
    }
  }

  "Document extractor" should "extract TXT correctly" in {
    val doc = new DocManager(Paths.get("."), ex, null)
    doc.accept(TCommon.getData("ExtractSpec.1.txt"))
    ex.received should equal("paws PAWS\nclaws Claw\n")
  }
  
  "Document extractor" should "extract PDF correctly" in {
    val doc = new DocManager(TCommon.getData(""), ex, null)
    doc.accept(TCommon.getData("ExtractSpec.2.pdf"))
    ex.received.replaceAll("\n", "") should equal("Goodbye cruel WorldI dont want to live on this planet anymore")
  }

  "Document extractor" should "extract DOC correctly" in {
    val doc = new DocManager(TCommon.getData(""), ex, null)
    doc.accept(TCommon.getData("ExtractSpec.3.doc"))
    ex.received.replaceAll("\n", "") should equal("Trait FlatSpec is so named because your specification text")
  }

  "Document extractor" should "extract DOCX correctly" in {
    val doc = new DocManager(TCommon.getData(""), ex, null)
    doc.accept(TCommon.getData("ExtractSpec.4.docx"))
    ex.received.replaceAll("\n", "") should equal("Trait FlatSpec is so named because your specification text")
  }
}