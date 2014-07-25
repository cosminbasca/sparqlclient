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
 * This is largely a port from the excellent python [[https://github.com/RDFLib/sparqlwrapper SPARQLWrapper]] library
 * to scala, although not a complete port.
 *
 * A simple example:
 * {{{
 *   import com.sparqlclient.{SparqlClient, DataFormat}
 *   import scala.concurrent.Await
 *   import scala.concurrent.duration.Duration
 *
 *   val dbpedia = SparqlClient("http://dbpedia.org/sparql", format = DataFormat.JSON)
 *   val query = """
 *      PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
 *      SELECT ?p ?label
 *      WHERE { <http://dbpedia.org/resource/Asturias> ?p ?label }
 *      LIMIT 10
 * """
 * val futureResults = dbpedia(query)
 *
 * println(s"the results = \n ${Await.result(futureResults, Duration(10, "seconds")).toList}")
 * }}}
 *
 */
package object sparqlclient {

  /**
   * Enumeration of supported ''DataFormats'' (human readable).
   */
  object DataFormat extends Enumeration {
    type DataFormat = Value
    val Json = Value("json")
    val JsonLD = Value("json-ld")
    val Xml = Value("xml")
    val Turtle = Value("n3")
    val N3 = Value("n3")
    val Rdf = Value("rdf")
    val Csv = Value("csv")
  }

  val AllowedDataFormats: Array[DataFormat.Value] = Array(
    DataFormat.Json, DataFormat.Xml, DataFormat.Turtle, DataFormat.N3, DataFormat.Rdf, DataFormat.Csv)

  /**
   * Enumeration of [[http://www.w3.org/TR/rdf-sparql-query/ SPARQL]] Query types
   */
  object QueryType extends Enumeration {
    type QueryType = Value
    val Select = Value("SELECT")
    val Construct = Value("CONSTRUCT")
    val Ask = Value("ASK")
    val Describe = Value("DESCRIBE")
    val Insert = Value("INSERT")
    val Delete = Value("DELETE")
    val Create = Value("CREATE")
    val Clear = Value("CLEAR")
    val Drop = Value("DROP")
    val Load = Value("LOAD")
    val Copy = Value("COPY")
    val Move = Value("MOVE")
    val Add = Value("ADD")
  }

  val InsertQueryTypes: Array[QueryType.Value] = Array(QueryType.Insert, QueryType.Delete, QueryType.Create, QueryType.Clear,
    QueryType.Drop, QueryType.Load, QueryType.Copy, QueryType.Move, QueryType.Add)

  object RequestMethod extends Enumeration {
    type RequestMethod = Value
    val URLENCODED, POSTDIRECTLY = Value
  }

  val AllowedRequestMethods: Array[RequestMethod.Value] = Array(RequestMethod.URLENCODED, RequestMethod.POSTDIRECTLY)

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

  val MimeTypesDataFormat: Map[String, DataFormat.Value] = Map(
    MimeType.SPARQL_RESULTS_XML -> DataFormat.Xml,
    MimeType.SPARQL_RESULTS_JSON -> DataFormat.Json,
    MimeType.JSON -> DataFormat.Json,
    MimeType.RDF_XML -> DataFormat.Rdf,
    MimeType.CSV -> DataFormat.Csv,
    MimeType.RDF_N3 -> DataFormat.N3,
    MimeType.RDF_N3_APP -> DataFormat.N3,
    MimeType.RDF_N3_TXT -> DataFormat.N3,
    MimeType.RDF_TURTLE -> DataFormat.Turtle,
    MimeType.RDF_TURTLE_TXT -> DataFormat.Turtle
  )

  val DefaultSparqlResultFormats: Array[String] = Array(MimeType.SPARQL_RESULTS_XML, MimeType.RDF_XML, MimeType.ANY)
  val SparqlXmlResultFormats: Array[String] = Array(MimeType.SPARQL_RESULTS_XML)
  val SparqlJsonResultFormats: Array[String] = Array(MimeType.SPARQL_RESULTS_JSON, MimeType.JAVASCRIPT, MimeType.JSON)
  val RdfXmlDataFormats: Array[String] = Array(MimeType.RDF_XML)
  val RdfN3DataFormats: Array[String] = Array(MimeType.RDF_N3, MimeType.RDF_NTRIPLES, MimeType.RDF_TURTLE, MimeType.RDF_N3_APP,
    MimeType.RDF_N3_TXT, MimeType.RDF_TURTLE_TXT)
  val RdfJsonLDDataFormats: Array[String] = Array(MimeType.JSON_LD_X, MimeType.JSON_LD)
  val AnyDataFormats: Array[String] = Array(MimeType.ANY)
  val PossibleRdfDataFormats: Array[String] = RdfXmlDataFormats ++ RdfN3DataFormats
  val PossibleSparqlResultFormats: Array[String] = SparqlXmlResultFormats ++ SparqlJsonResultFormats ++ RdfXmlDataFormats ++ RdfN3DataFormats

  val Agent: String = s"SparqlClient scala sparql client v${BuildInfo.version}"

  /**
   * Enumeration of [[http://www.w3.org/TR/sparql11-protocol/ SPARQL protocol]] accepted HTTP methods
   */
  object HttpMethod extends Enumeration {
    type HttpMethod = Value
    val GET = Value("GET")
    val POST = Value("POST")
  }

  val AllowedHttpMethods: Array[HttpMethod.Value] = Array(HttpMethod.GET, HttpMethod.POST)

  val DefaultSparqlQuery: String = """SELECT * WHERE{ ?s ?p ?o } LIMIT 10"""

  val SparqlParameters: Array[String] = Array("query")
  val ReturnFormatParameters: Array[String] = Array("format", "output", "results")

  /**
   * Enumeration of useful Namespaces
   */
  object Namespaces extends Enumeration {
    type Namespaces = String
    val XML = "http://www.w3.org/XML/1998/namespace"
    val RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    val SPARQLResults = "http://www.w3.org/2005/sparql-results#"
  }

}
