package com

import com.sparqlclient.BuildInfo
import scala.collection.JavaConversions._

/**
 * Created by basca on 21/07/14.
 */

/**
 * the [[com.sparqlclient]] package holds a number of constants defined such as the data formats to be used,
 * or helpful mime-types that the SPARQL protocol regularly makes use of.
 *
 * This is largely a port from the excellent python [[https://github.com/RDFLib/sparqlwrapper SPARQLWrapper]] library to scala
 *
 * A simple example:
 * {{{
 *   import com.sparqlclient.SparqlClient
 *   import scala.concurrent.Await
 *   import scala.concurrent.duration.Duration
 *
 *   val dbpedia = SparqlClient("http://dbpedia.org/sparql", format = "json")
 *   val query = """
 *      PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
 *      SELECT ?p ?label
 *      WHERE { <http://dbpedia.org/resource/Asturias> ?p ?label }
 *      """
 *   val futureResults = dbpedia(query)
 *
 *   println(s"the results = \n ${Await.result(futureResults, Duration(10, "seconds")).toList}")
 * }}}
 *
 */
package object sparqlclient {

  /**
   * Enumeration of supported ''DataFormats'' (human readable).
   */
  object DataFormat extends Enumeration {
    type DataFormat = String
    val JSON = "json"
    val JSONLD = "json-ld"
    val XML = "xml"
    val TURTLE = "n3"
    val N3 = "n3"
    val RDF = "rdf"
    val CSV = "csv"
  }

  val ALLOWED_DATA_FORMATS: Array[String] = Array(DataFormat.JSON, DataFormat.XML, DataFormat.TURTLE, DataFormat.N3,
    DataFormat.RDF, DataFormat.CSV)

  /**
   * Enumeration of [[http://www.w3.org/TR/rdf-sparql-query/ SPARQL]] Query types
   */
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

  val INSERT_QUERY_TYPE: Array[String] = Array(QueryType.INSERT, QueryType.DELETE, QueryType.CREATE, QueryType.CLEAR,
    QueryType.DROP, QueryType.LOAD, QueryType.COPY, QueryType.MOVE, QueryType.ADD)

  object RequestMethod extends Enumeration {
    type RequestMethod = String
    val URLENCODED = "urlencoded"
    val POSTDIRECTLY = "postdirectly"
  }

  val ALLOWED_REQUESTS_METHODS: Array[String] = Array(RequestMethod.URLENCODED, RequestMethod.POSTDIRECTLY)

  /**
   * Enumeration of useful mime-types for the [[http://www.w3.org/TR/sparql11-protocol/ SPARQL protocol]]
   */
  object MimeType extends Enumeration {
    type MimeType = String
    val ANY = "*/*"
    val SPARQL_RESULTS_XML = "application/sparql-results+xml"
    val SPARQL_RESULTS_JSON = "application/sparql-results+json"
    val SPARQL_UPDATE = "application/sparql-update"
    val URL_FORM_ENCODED = "application/x-www-form-urlencoded"
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
    val CSV = "text/csv"
  }

  val MIME_TYPE_DATA_FORMAT: Map[String, String] = Map(
    MimeType.SPARQL_RESULTS_XML -> DataFormat.XML,
    MimeType.SPARQL_RESULTS_JSON -> DataFormat.JSON,
    MimeType.JSON -> DataFormat.JSON,
    MimeType.RDF_XML -> DataFormat.RDF,
    MimeType.CSV -> DataFormat.CSV,
    MimeType.RDF_N3 -> DataFormat.N3,
    MimeType.RDF_N3_APP -> DataFormat.N3,
    MimeType.RDF_N3_TXT -> DataFormat.N3,
    MimeType.RDF_TURTLE -> DataFormat.TURTLE,
    MimeType.RDF_TURTLE_TXT -> DataFormat.TURTLE
  )

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

  val AGENT: String = s"SparqlClient scala sparql client v${BuildInfo.version}"

  /**
   * Enumeration of [[http://www.w3.org/TR/sparql11-protocol/ SPARQL protocol]] accepted HTTP methods
   */
  object HttpMethod extends Enumeration {
    type HttpMethod = String
    val GET = "GET"
    val POST = "POST"
  }
  
  val ALLOWED_HTTP_METHODS: Array[String] = Array(HttpMethod.GET, HttpMethod.POST)

  val DEFAULT_SPARQL = """SELECT * WHERE{ ?s ?p ?o }"""

  val SPARQL_PARAMS: Array[String] = Array("query")
  val RETURN_FORMAT_PARAMS: Array[String] = Array("format", "output", "results")

  val XML_NS: String = "http://www.w3.org/XML/1998/namespace"
  val RDF_NS: String = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  val SPARQL_RES_NS: String = "http://www.w3.org/2005/sparql-results#"
}
