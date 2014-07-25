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

  /**
   * Enumeration of useful mime-types for the [[http://www.w3.org/TR/sparql11-protocol/ SPARQL protocol]]
   */
  object MimeType extends Enumeration {
    type MimeType = Value
    val Any = Value("*/*")
    val SparqlXmlResults = Value("application/sparql-results+xml")
    val SparqlJsonResults = Value("application/sparql-results+json")
    val SparqlUpdate = Value("application/sparql-update")
    val SparqlQuery = Value("application/sparql-query")
    val UrlFormEncoded = Value("application/x-www-form-urlencoded")
    val RdfXml = Value("application/rdf+xml")
    val RdfN3 = Value("text/rdf+n3")
    val RdfN3Application = Value("application/n3")
    val RdfN3Text = Value("text/n3")
    val RdfNtriples = Value("application/n-triples")
    val RdfTurtle = Value("application/turtle")
    val RdfTurtleText = Value("text/turtle")
    val Javascript = Value("text/javascript")
    val Json = Value("application/json")
    val JsonLDx = Value("application/x-json+ld")
    val JsonLD = Value("application/ld+json")
    val Csv = Value("text/csv")
  }

  val MimeTypesDataFormat: Map[MimeType.Value, DataFormat.Value] = Map(
    MimeType.SparqlXmlResults -> DataFormat.Xml,
    MimeType.SparqlJsonResults -> DataFormat.Json,
    MimeType.Json -> DataFormat.Json,
    MimeType.RdfXml -> DataFormat.Rdf,
    MimeType.Csv -> DataFormat.Csv,
    MimeType.RdfN3 -> DataFormat.N3,
    MimeType.RdfN3Application -> DataFormat.N3,
    MimeType.RdfN3Text -> DataFormat.N3,
    MimeType.RdfTurtle -> DataFormat.Turtle,
    MimeType.RdfTurtleText -> DataFormat.Turtle
  )

  val DefaultSparqlResultFormats: Array[MimeType.Value] = Array(MimeType.SparqlXmlResults, MimeType.RdfXml, MimeType.Any)
  val SparqlXmlResultFormats: Array[MimeType.Value] = Array(MimeType.SparqlXmlResults)
  val SparqlJsonResultFormats: Array[MimeType.Value] = Array(MimeType.SparqlJsonResults, MimeType.Javascript, MimeType.Json)
  val RdfXmlDataFormats: Array[MimeType.Value] = Array(MimeType.RdfXml)
  val RdfN3DataFormats: Array[MimeType.Value] = Array(MimeType.RdfN3, MimeType.RdfNtriples, MimeType.RdfTurtle, MimeType.RdfN3Application,
    MimeType.RdfN3Text, MimeType.RdfTurtleText)
  val RdfJsonLDDataFormats: Array[MimeType.Value] = Array(MimeType.JsonLDx, MimeType.JsonLD)
  val AnyDataFormats: Array[MimeType.Value] = Array(MimeType.Any)
  val PossibleRdfDataFormats: Array[MimeType.Value] = RdfXmlDataFormats ++ RdfN3DataFormats
  val PossibleSparqlResultFormats: Array[MimeType.Value] = SparqlXmlResultFormats ++ SparqlJsonResultFormats ++ RdfXmlDataFormats ++ RdfN3DataFormats

  val Agent: String = s"SparqlClient scala sparql client v${BuildInfo.version}"

  /**
   * Enumeration of [[http://www.w3.org/TR/sparql11-protocol/ SPARQL protocol]] accepted HTTP methods
   */
  object HttpMethod extends Enumeration {
    type HttpMethod = Value
    val GET = Value("GET")
    val POST = Value("POST")
  }

  val DefaultSparqlQuery: String = """SELECT * WHERE{ ?s ?p ?o } LIMIT 10"""

  val SparqlParameters: Array[String] = Array("query")
  val ReturnFormatParameters: Array[String] = Array("format", "output", "results")

  /**
   * Enumeration of useful Namespaces
   */
  object Namespaces extends Enumeration {
    type Namespaces = Value
    val Xml = Value("http://www.w3.org/XML/1998/namespace")
    val Rdf = Value("http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    val SparqlResults = Value("http://www.w3.org/2005/sparql-results#")
  }

}
