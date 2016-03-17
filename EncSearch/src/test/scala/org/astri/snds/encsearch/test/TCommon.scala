package org.astri.snds.encsearch.test

import java.nio.file.{Paths, Path, Files}
import java.sql.{Connection, DriverManager}
import java.util.function.Consumer

object TCommon {
  def getData(subpath:String) = {
    Paths.get("../data/test", subpath)
  }
  
  def getDb() = {
    Class.forName("org.postgresql.Driver")
    DriverManager.getConnection("jdbc:postgresql:encsearch")
  }
  
  def deleteDir(dir:Path) = {
    Files.walk(dir).forEach(new Consumer[Path] {
      def accept(p:Path) = {
        p.toFile().delete();
      }
    })
  }
}