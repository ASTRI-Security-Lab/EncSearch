package org.astri.snds.encsearch.test

import java.nio.file.Paths
import java.sql.{Connection, DriverManager}

object TCommon {
  def getData(subpath:String) = {
    Paths.get("../data/test", subpath)
  }
  
  def getDb() = {
    Class.forName("org.postgresql.Driver")
    DriverManager.getConnection("jdbc:postgresql:encsearch")
  }
}