package org.astri.snds.encsearch.test

import java.net.URL
import java.nio.charset.StandardCharsets
import javax.xml.bind.JAXBContext
import java.io.{FileWriter, FileOutputStream}
import java.nio.file.{Paths, Files, Path}
import java.util.function.Consumer
import org.eclipse.persistence.jaxb.MarshallerProperties
import org.eclipse.persistence.oxm.MediaType

import org.scalatest._
import scala.collection.JavaConversions.{mapAsScalaMap, collectionAsScalaIterable}

import org.astri.snds.encsearch._

class FileCryptoSpec extends FlatSpec with Matchers {

  "Encryption" should "return predefined value" in {
    val salt = Array(1, 1, 1, 1,1, 1, 1, 1,1, 1, 1, 1,1, 1, 1, 1).map(_.toByte)

    val plainPath = TCommon.getData("FileCryptoSpec/plain")
    plainPath.toFile().mkdirs()
    val plaintext = "AABBCCDDEEFFGGHHIIJJKKLLMMNNOOPPQQRRSSTTUUVVWWXXYYZZ"
    val input = plainPath.resolve("input.txt")
    val writer = new FileWriter(input.toFile())
    writer.write(plaintext)
    writer.close()

    val encrPath = TCommon.getData("FileCryptoSpec/encrypted")
    encrPath.toFile().mkdirs()

    val crypter = new FileCrypto("abcd", salt, encrPath)
    val header = new CryptoHeader()
    header.iv = "aaaaaaaaaaaaaaaa".getBytes(StandardCharsets.US_ASCII)
    val output = encrPath.resolve(crypter.encryptName(input, header))
    val outputData = Paths.get(output + FileCrypto.EXT_DATA)
    crypter.encryptFile(header, input, outputData)

    // check expected value

    Base64Adapter.enc.encodeToString(header.hmac) should equal ("A8ZiVDrXOA_sTKdkDxWTKDoRIoZGtk4nzYO2mQrfKDM=")
    val ciphertext = Files.readAllBytes(outputData)
    Base64Adapter.enc.encodeToString(ciphertext) should equal ("71luvPNbiTcqwPToxoegGE6ml30O0gY9ewNRt-BWVJWOb_8nncsR2js4LmDpiYRjHx64pLl8AgPS3OhyJQZFGg==")
    
    // try decrypt, check hmac
    val headerOut = new FileOutputStream(output + FileCrypto.EXT_HEADER)
    val jaxb = JAXBContext.newInstance(classOf[CryptoHeader])
    val marshaller = jaxb.createMarshaller()
    marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON)
    marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false)
    marshaller.marshal(header, headerOut)
    crypter.decryptFile(outputData)
  }
}
