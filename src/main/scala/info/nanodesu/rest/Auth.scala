package info.nanodesu.rest

object Auth {
	var sessionMappings: Map[String, String] = Map.empty
	
	def getUbername(session: String): Option[String] = {
	  sessionMappings.get(session) orElse {
	    // query mapping from ubernet and add it to the sessionMappings
	    None
	  }
	}
}