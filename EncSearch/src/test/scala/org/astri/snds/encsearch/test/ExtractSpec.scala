package org.astri.snds.encsearch.test

import java.nio.file.Paths
import org.scalatest._
import org.astri.snds.encsearch.{KeywordExtractor, DocManager}
import org.astri.snds.encsearch.IKeywordExtractor
import scala.collection.JavaConversions.{mapAsScalaMap, collectionAsScalaIterable}
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.{PBEParametersGenerator}
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.digests.SHA256Digest
import org.apache.commons.codec.binary.Hex

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
  
  "crypto and encoding" should "work correctly" in {
    /*
    val inp = "eFJjTd5nix-EvdBtoe3zUQ=="
    val dec = java.util.Base64.getUrlDecoder()
    val res = dec.decode(inp)
    System.out.println( res.map("%02X" format _).mkString )
    */
    
    val gen = new PKCS5S2ParametersGenerator(new SHA256Digest())
    val salt = java.nio.charset.Charset.forName("UTF-8").encode("salt").array()
    gen.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes("abcd".toCharArray()), salt, 100)
    val derivedKey = gen.generateDerivedParameters(128 * 8).asInstanceOf[KeyParameter].getKey()
    val derivedKeyHex = Hex.encodeHexString(derivedKey)
    
    // from Python pbkdf2 library
    // pbkdf2.pbkdf2_hex('abcd', 'salt', 100, keylen=128, hashfunc=hashlib.sha256)
    val expectedHex = "1106246619c77edecdba5ee54334d69a2814e3e9019a5b0522c71c0b6d4b2c55b7503283b8c3c1a0f373fe303aabeaf4d65f5b74ea4d9696ddfb5fd55caf391e2b783c3c54092efc0b81325f23d7c18436d4c79e80e70f09e2f38ff0c1cd7f75c7dc56d5fd22291446bd415fe39d11469fa350a271a90bfc910a1343477202d6"
    derivedKeyHex should equal(expectedHex)
  }
}