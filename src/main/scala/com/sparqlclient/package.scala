package com

import dispatch.BuildInfo
import scala.collection.JavaConversions._

/**
 * Created by basca on 21/07/14.
 */
package object sparqlclient {

  object DataFormat extends Enumeration {
    type DataFormat = String
    val JSON = "json"
    val JSONLD = "json-ld"
    val XML = "xml"
    val TURTLE = "n3"
    val N3 = "n3"
    val RDF = "rdf"
  }

  val ALLOWED_DATA_FORMATS: Array[String] = Array(DataFormat.JSON, DataFormat.JSONLD, DataFormat.XML,
    DataFormat.TURTLE, DataFormat.N3, DataFormat.RDF)

  object QueryType extends Enumeration {
    type QueryType = String
    val SELECT = "SELECT"
    val CONSTRUCT = "CONSTRUCT"
    val ASK = "ASK"
    val DESCRIBE = "DESCRIBE"
    val INSERT = "INSERT"
    val DELETE = "DELETE"
    val CREATE = "CREATE"
    val CLEAR = "CLEAR"
    val DROP = "DROP"
    val LOAD = "LOAD"
    val COPY = "COPY"
    val MOVE = "MOVE"
    val ADD = "ADD"
  }

  val ALLOWED_QUERY_TYPES: Array[String] = Array(QueryType.SELECT, QueryType.CONSTRUCT, QueryType.ASK,
    QueryType.DESCRIBE, QueryType.INSERT, QueryType.DELETE, QueryType.CREATE, QueryType.CLEAR, QueryType.DROP,
    QueryType.LOAD, QueryType.COPY, QueryType.MOVE, QueryType.ADD)

  object RequestMethod extends Enumeration {
    type RequestMethod = String
    val URLENCODED = "urlencoded"
    val POSTDIRECTLY = "postdirectly"
  }

  val ALLOWED_REQUESTS_METHODS: Array[String] = Array(RequestMethod.URLENCODED, RequestMethod.POSTDIRECTLY)

  object MimeType extends Enumeration {
    type MimeType = String
    val ANY = "*/*"
    val SPARQL_RESULTS_XML = "application/sparql-results+xml"
    val SPARQL_RESULTS_JSON = "application/sparql-results+json"
    val RDF_XML = "application/rdf+xml"
    val RDF_N3 = "text/rdf+n3"
    val RDF_N3_APP = "application/n3"
    val RDF_N3_TXT = "text/n3"
    val RDF_NTRIPLES = "application/n-triples"
    val RDF_TURTLE = "application/turtle"
    val RDF_TURTLE_TXT = "text/turtle"
    val JAVASCRIPT = "text/javascript"
    val JSON = "application/json"
    val JSON_LD_X = "application/x-json+ld"
    val JSON_LD = "application/ld+json"
  }

  val SPARQL_DEFAULT: Array[String] = Array(MimeType.SPARQL_RESULTS_XML, MimeType.RDF_XML, MimeType.ANY)
  val SPARQL_XML: Array[String] = Array(MimeType.SPARQL_RESULTS_XML)
  val SPARQL_JSON: Array[String] = Array(MimeType.SPARQL_RESULTS_JSON, MimeType.JAVASCRIPT, MimeType.JSON)
  val RDF_XML: Array[String] = Array(MimeType.RDF_XML)
  val RDF_N3: Array[String] = Array(MimeType.RDF_N3, MimeType.RDF_NTRIPLES, MimeType.RDF_TURTLE, MimeType.RDF_N3_APP,
    MimeType.RDF_N3_TXT, MimeType.RDF_TURTLE_TXT)
  val RDF_JSONLD: Array[String] = Array(MimeType.JSON_LD_X, MimeType.JSON_LD)
  val ALL: Array[String] = Array(MimeType.ANY)
  val RDF_POSSIBLE: Array[String] = RDF_XML ++ RDF_N3
  val SPARQL_POSSIBLE: Array[String] = SPARQL_XML ++ SPARQL_JSON ++ RDF_XML ++ RDF_N3

  val AGENT: String = s"sparqlwrapper scala client v${BuildInfo.version}"

  val GET: String = "GET"
  val POST: String = "POST"

  val DEFAULT_SPARQL = """SELECT * WHERE{ ?s ?p ?o }"""

  val SPARQL_PARAMS: Array[String] = Array("query")
}
