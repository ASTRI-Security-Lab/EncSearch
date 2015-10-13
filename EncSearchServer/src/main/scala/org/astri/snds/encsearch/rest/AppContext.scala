package org.astri.snds.encsearch.rest

import javax.servlet.{ServletContextListener, ServletContextEvent}
import java.sql.{Connection, DriverManager}

object AppContext {
  var dbConn:Connection = null
  
}

class AppContext extends ServletContextListener {
  
  override def contextInitialized(sce:ServletContextEvent) {
    Class.forName("org.postgresql.Driver")
    AppContext.dbConn = DriverManager.getConnection("jdbc:postgresql:encsearch")
  }

  override def contextDestroyed(sce:ServletContextEvent) {
    if (AppContext.dbConn != null) AppContext.dbConn.close()
    AppContext.dbConn = null
    
  }
}