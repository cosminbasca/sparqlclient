package com.sparqlclient

import java.net.URL

import scala.util.matching.Regex

/**
 * Created by cosmin on 21/07/14.
 */
class SparqlWrapper(val endpoint: URL, val updateEndpoint: Option[URL] = None, val returnFormat: String = DataFormat.XML,
                    val defaultGraph: Option[URL] = None, val agent: String = AGENT) {
  val pattern: Regex = """
       ((?P<base>(\s*BASE\s*<.*?>)\s*)|(?P<prefixes>(\s*PREFIX\s+.+:\s*<.*?>)\s*))*
       (?P<queryType>(CONSTRUCT|SELECT|ASK|DESCRIBE|INSERT|DELETE|CREATE|CLEAR|DROP|LOAD|COPY|MOVE|ADD))
                       """.r


}
