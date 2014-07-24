package com.sparqlclient

import java.net.URL
import java.nio.charset.StandardCharsets

import com.sparqlclient.rdf.RdfTerm

import scala.collection.mutable
import scala.concurrent.{Promise, Await}
import scala.concurrent.duration.Duration
import scala.util.matching.Regex
import dispatch._, Defaults._
import scala.collection.JavaConversions._

/**
 * Created by cosmin on 21/07/14.
 */

/**
 * a [[http://www.w3.org/TR/sparql11-protocol/ SPARQL protocol]] client. By default (unless specified otherwise) the
 * http method used is [[com.sparqlclient.HttpMethod.GET]] and the request is performed using the [[com.sparqlclient.RequestMethod.URLENCODED]] method.
 *
 * @param endpointLocation the endpoint's location url
 * @param updateEndpointLocation the endpoints's [[http://www.w3.org/TR/sparql11-update/ SPARQL UPDATE]] url
 *               (if different from the endpoint's location)
 * @param format the desired returned data format (default is [[com.sparqlclient.DataFormat.XML]] "xml"). By default the response's
 *               __"Content-type"__ header is used to determine the appropriate data model.
 *               If the server does not provide this header, the data model falls back to this value.
 *               Must be one of [[com.sparqlclient.ALLOWED_DATA_FORMATS]]
 * @param defaultGraph the default graph to query against (default not specified,
 *                     the query is executed against the entire graph)
 * @param agent the client agent (default is [[com.sparqlclient.AGENT]])
 * @param connectionTimeout the connection timeout in seconds (default is 60 seconds)
 */
class SparqlClient(val endpointLocation: URL, val updateEndpointLocation: Option[URL] = None, val format: String = DataFormat.XML,
                   val defaultGraph: Option[URL] = None, val agent: String = AGENT, connectionTimeout: Int = 60) {
  // -------------------------------------------------------------------------------------------------------------------
  //
  // internal state
  //
  // -------------------------------------------------------------------------------------------------------------------
  private val pattern: Regex = """(?i)((\s*BASE\s*<.*?>)\s*|(\s*PREFIX\s+.+:\s*<.*?>)\s*)*(CONSTRUCT|SELECT|ASK|DESCRIBE|INSERT|DELETE|CREATE|CLEAR|DROP|LOAD|COPY|MOVE|ADD)""".r
  private val GROUP_QUERY_TYPE: Int = 4
  private val base64encoder: sun.misc.BASE64Encoder = new sun.misc.BASE64Encoder()

  private val parameters: mutable.Map[String, Seq[String]] = mutable.Map.empty[String, Seq[String]]
  private val updateEndpoint: URL = updateEndpointLocation.getOrElse(endpointLocation)
  private val defaultReturnFormat: String = if (ALLOWED_DATA_FORMATS.contains(format)) format else DataFormat.XML
  private var user: Option[String] = None
  private var pass: Option[String] = None
  private var returnFormat: String = defaultReturnFormat
  private var queryType: String = QueryType.SELECT
  private var queryString: String = DEFAULT_SPARQL
  private var httpMethod: String = HttpMethod.GET
  private var requestMethod: String = RequestMethod.URLENCODED
  private val http: Http = Http.configure(_.setAllowPoolingConnection(true).setConnectionTimeoutInMs(connectionTimeout / 1000))

  // reset internal state on initialisation
  reset()

  /**
   * reset the internal state of the client. Useful when changing too many attributes is undesirable.
   */
  def reset() = {
    parameters.clear()
    defaultGraph match {
      case Some(graph) =>
        appendParameter("default-graph-uri", graph.toString)
      case None =>
    }
    returnFormat = defaultReturnFormat
    queryType = QueryType.SELECT
    httpMethod = HttpMethod.GET
    queryString = DEFAULT_SPARQL
    requestMethod = RequestMethod.URLENCODED
  }

  /**
   * set the returned data format.
   * @param format the data format: must be one of [[com.sparqlclient.ALLOWED_DATA_FORMATS]]
   */
  def setReturnFormat(format: String) = {
    if (ALLOWED_DATA_FORMATS.contains(format)) {
      returnFormat = format
    }
  }

  /**
   * Set the internal method used to perform the request for querying or update operations,
   * it can be either:  URL encoded [[com.sparqlclient.RequestMethod.URLENCODED]] or
   * POST directly [[com.sparqlclient.RequestMethod.POSTDIRECTLY]]
   * For further reading please visit [[http://www.w3.org/TR/sparql11-protocol/#query-operation]]
   * and [[http://www.w3.org/TR/sparql11-protocol/#update-operation]]
   * @param method the internal request method
   */
  def setRequestMethod(method: String) = {
    if (ALLOWED_REQUESTS_METHODS.contains(method)) {
      requestMethod = method
    } else {
      println(s"Invalid update method: $method")
    }
  }

  /**
   * Append new parameter values. If the parameter does not exist a new Sequence of values (with the given value) is
   * created
   *
   * @param name the name of the parameter
   * @param value the value
   */
  private def appendParameter(name: String, value: String) = {
    val values: Seq[String] = parameters.get(name) match {
      case Some(paramValues) => paramValues ++ Seq(value)
      case None => Seq(value)
    }
    parameters(name) = values
  }

  /**
   * Some SPARQL endpoints allow extra key value parameter pairs.
   * For example in virtuoso, one would add ''should-sponge=soft'' to the query forcing virtuoso to retrieve graphs
   * that are not stored in its local database.
   *
   * Valued are appended to the existing parameter values.
   *
   * @param name the name of the parameter
   * @param value the value of the parameter
   * @return
   */
  def addParameter(name: String, value: String): Boolean = {
    if (SPARQL_PARAMS.contains(name)) {
      false
    } else {
      appendParameter(name, value)
      true
    }
  }

  /**
   * clears all the values of the specified parameter (if any)
   *
   * @param name the parameter name
   * @return true if the values were cleared false otherwise
   *         (the method returns false also when the parameter is not found)
   */
  def clearParameter(name: String): Boolean = {
    if (SPARQL_PARAMS.contains(name)) {
      false
    } else {
      parameters.remove(name) match {
        case Some(value) => true
        case None => false
      }
    }
  }

  /**
   * specify the authentication credentials for secured endpoints.
   *
   * @param user the user name
   * @param pass the password
   */
  def setCredentials(user: String, pass: String) = {
    this.user = Some(user)
    this.pass = Some(pass)
  }

  /**
   * set the current [[http://www.w3.org/TR/rdf-sparql-query/ SPARQL]] query
   * the type of the query is automatically detected and the request constructed accordingly
   *
   * @param query the textual query
   */
  def setQuery(query: String) = {
    queryString = query
    queryType = parseQueryType(query)
  }

  /**
   * detect the type of the given [[http://www.w3.org/TR/rdf-sparql-query/ SPARQL]] query
   * @param query the textual query
   * @return the type of query, will be one of [[com.sparqlclient.QueryType]].
   *         It defaults to [[com.sparqlclient.QueryType.SELECT]]
   */
  private def parseQueryType(query: String): String = {
    pattern.findFirstMatchIn(query) match {
      case Some(firstMatch) =>
        val qType: String = firstMatch.group(GROUP_QUERY_TYPE).toUpperCase
        if (ALLOWED_QUERY_TYPES.contains(qType)) {
          qType
        } else {
          QueryType.SELECT
        }
      case None => QueryType.SELECT
    }
  }

  /**
   * set the HTTP method to use. Must be one of [[com.sparqlclient.HttpMethod]]
   * @param method the http method
   */
  def setHttpMethod(method: String) = {
    if (ALLOWED_HTTP_METHODS.contains(method)) {
      this.httpMethod = method
    }
  }

  /**
   * test if the request is for an UPDATE operation
   * @return true if an UPDATE operation false otherwise
   */
  private def isSparqlUpdateRequest: Boolean = {
    INSERT_QUERY_TYPE.contains(queryType)
  }

  /**
   * test if the request is for a QUERY operation
   * @return true if a QUERY operation false otherwise
   */
  private def isSparqlQueryRequest: Boolean = {
    !isSparqlUpdateRequest
  }

  /**
   * create the __"Accept"__ header for the current request, given the detected query type
   * @return the __"Accept"__ header
   */
  private def getAcceptHeader: String = {
    queryType match {
      case QueryType.SELECT | QueryType.ASK =>
        returnFormat match {
          case DataFormat.XML => SPARQL_XML.mkString(",")
          case DataFormat.JSON => SPARQL_JSON.mkString(",")
          case _ => ALL.mkString(",")
        }
      case QueryType.INSERT | QueryType.DELETE =>
        MimeType.ANY
      case _ =>
        returnFormat match {
          case DataFormat.N3 | DataFormat.TURTLE => RDF_N3.mkString(",")
          case DataFormat.XML => RDF_XML.mkString(",")
          //          case DataFormat.JSONLD => RDF_JSONLD.mkString(",")
          case _ => ALL.mkString(",")
        }
    }
  }

  /**
   * create the request according to the guidelines of the [[http://www.w3.org/TR/sparql11-protocol/ SPARQL protocol]]
   * @return the request
   */
  private def createRequest: Req = {
    // return format as defined by various endpoints ... this is not cool, and should be standardised in the future ...
    for (fmt <- RETURN_FORMAT_PARAMS)
      appendParameter(fmt, returnFormat)

    // create the request according to how it was specified
    val request: Req = if (isSparqlUpdateRequest) {
      if (httpMethod != HttpMethod.POST) {
        throw new UnsupportedOperationException("update operations MUST be done by POST")
      }
      requestMethod match {
        case RequestMethod.POSTDIRECTLY =>
          url(updateEndpoint.toString).POST.addHeader("Content-Type", MimeType.SPARQL_UPDATE).setQueryParameters(parameters.toMap).setBody(queryString)
        case _ =>
          appendParameter("update", queryString)
          url(updateEndpoint.toString).POST.setHeader("Content-Type", MimeType.URL_FORM_ENCODED).setParameters(parameters.toMap)
      }
    } else {
      httpMethod match {
        case HttpMethod.POST =>
          requestMethod match {
            case RequestMethod.POSTDIRECTLY =>
              url(endpointLocation.toString).POST.addHeader("Content-Type", MimeType.SPARQL_UPDATE).setQueryParameters(parameters.toMap).setBody(queryString)
            case _ =>
              appendParameter("query", queryString)
              url(endpointLocation.toString).POST.setHeader("Content-Type", MimeType.URL_FORM_ENCODED).setParameters(parameters.toMap)
          }
        case HttpMethod.GET =>
          appendParameter("query", queryString)
          url(endpointLocation.toString).setQueryParameters(parameters.toMap)
      }
    }

    (if (user.nonEmpty && pass.nonEmpty) {
      val credentials: String = s"${user.get}:${pass.get}"
      request.setHeader("Authorization", s"Basic ${base64encoder.encode(credentials.getBytes(StandardCharsets.UTF_8))}")
    } else {
      request
    }).setHeader("User-Agent", agent).setHeader("Accept", getAcceptHeader)
  }

  override def toString: String = {
    createRequest.toRequest.toString
  }


  /**
   * the raw results (string representation as returned from the request)
   * this method blocks
   *
   * @param duration the duration (in seconds) to wait for a response
   * @return the raw results
   */
  def rawResults(duration: Int): String = {
    Await.result[String](rawResults, Duration(duration, "seconds"))
  }

  /**
   * the raw results (string representation as returned from the request)
   * this method is non-blocking
   *
   * @return a [[scala.concurrent.Future]] of the raw results
   */
  def rawResults: Future[String] = {
    http(createRequest OK as.String)
  }

  /**
   * auto-detection of the correct returned data format.
   *
   * note: this method may fail if the [[SparqlClient.returnFormat]] does not match the actual returned data format and
   * the server does not set the appropriate __"Content-type"__ header!
   *
   * @param contentTypes the request set __"Content-type"__ header value
   * @return the correctly identified data format (will be one of [[com.sparqlclient.DataFormat]])
   */
  private def detectDataFormat(contentTypes: Seq[String]): String = {
    if (contentTypes.nonEmpty) {
      for (contentType <- contentTypes) {
        MIME_TYPE_DATA_FORMAT.get(contentType.split(";").head.trim) match {
          case Some(dataFormat) =>
            return dataFormat
          case None =>
        }
      }
      returnFormat
    } else {
      returnFormat
    }
  }

  /**
   * the parsed query results
   * this method is non-blocking
   *
   * currently only the following formats are supported:
   * - [[DataFormat.JSON]], see [[com.sparqlclient.convert.fromJson]] method for further details
   * - [[DataFormat.XML]], see [[com.sparqlclient.convert.fromXML]] method for further details
   * - [[DataFormat.RDF]], see [[com.sparqlclient.convert.fromRDF]] method for further details
   * - [[DataFormat.CSV]], see [[com.sparqlclient.convert.fromCSV]] method for further details
   *
   * @return a [[scala.concurrent.Future]] of the [[scala.collection.Iterator]] over the parsed results or an empty iterator if the method fails
   */
  def queryResults: Future[Iterator[Seq[RdfTerm]]] = {
    val results: Promise[Iterator[Seq[RdfTerm]]] = Promise[Iterator[Seq[RdfTerm]]]()
    http(createRequest) onSuccess {
      case response =>
        val resultsIterator = if (response.getStatusCode / 100 == 2) {
          detectDataFormat(response.getHeaders("Content-type")) match {
            case DataFormat.JSON => convert.fromJson(response.getResponseBody)
            case DataFormat.XML => convert.fromXML(response.getResponseBody)
            case DataFormat.RDF => convert.fromRDF(response.getResponseBody)
            case DataFormat.CSV => convert.fromCSV(response.getResponseBody)
            case DataFormat.N3 | DataFormat.TURTLE | DataFormat.JSONLD =>
              throw new UnsupportedOperationException(s"parsing n3 / turtle / json-ld is not yet supported, change the returnFormat to any fo the following: ${
                List(
                  DataFormat.JSON, DataFormat.XML, DataFormat.RDF, DataFormat.CSV)
              }")
            case _ => Iterator.empty
          }
        } else {
          Iterator.empty
        }
        results.success(resultsIterator)
    }
    results.future
  }

  /**
   * the parsed query results
   * this method is blocking
   *
   * @return an [[scala.collection.Iterator]] over the parsed results or an empty iterator if the method fails
   */
  def queryResults(duration: Int): Iterator[Seq[RdfTerm]] = {
    Await.result[Iterator[Seq[RdfTerm]]](queryResults, Duration(duration, "seconds"))
  }

  /**
   * the parsed query results
   * this method is blocking and preserves the original query
   *
   * this method is equivalent to:
   * {{{
   *   // save the original query
   *   setQuery(query)
   *   queryResults(duration)
   *   // restore the original query
   * }}}
   *
   * @param query the actual query
   * @param duration the blocking duration
   * @return an [[scala.collection.Iterator]] over the parsed results or an empty iterator if the method fails
   */
  def apply(query: String, duration: Int): Iterator[Seq[RdfTerm]] = {
    val originalQuery: String = queryString
    setQuery(query)
    val results: Iterator[Seq[RdfTerm]] = queryResults(duration)
    setQuery(originalQuery)
    results
  }

  /**
   * the parsed query results
   * this method is non-blocking and preserves the original query
   *
   * this method is equivalent to:
   * {{{
   *   // save the original query
   *   setQuery(query)
   *   queryResults
   *   // restore the original query
   * }}}
   *
   * @param query the actual query
   * @return a [[scala.concurrent.Future]] of the [[scala.collection.Iterator]] over the parsed results or an empty iterator if the method fails
   */
  def apply(query: String): Future[Iterator[Seq[RdfTerm]]] = {
    val originalQuery: String = queryString
    setQuery(query)
    val futureResults: Future[Iterator[Seq[RdfTerm]]] = queryResults
    setQuery(originalQuery)
    futureResults
  }

  /**
   * client shutdown
   *
   * this method takes care of properly shutting down the underlying [[dispatch.Http]] objects
   */
  def shutdown() = {
    http.shutdown()
    Http.shutdown()
  }

}

/**
 * factory for creating SparqlClients
 */
object SparqlClient {
  /**
   * create a ''SPARQL'' client with the following presets
   *
   * @param endpoint the query endpoint location
   * @param update the update endpoint location (by default it is None and it therefore uses the query endpoint location)
   * @param format the desired returned data format (by default [[DataFormat.XML]] due to its wide support)
   * @param defaultGraph the default data graph (by default None)
   * @param httpMethod the http method used (by default [[HttpMethod.POST]]
   * @param requestMethod the request method used (by default [[RequestMethod.POSTDIRECTLY]]
   * @return the [[SparqlClient]] instance
   */
  def apply(endpoint: String, update: Option[String] = None, format: String = DataFormat.XML,
            defaultGraph: Option[String] = None,
            httpMethod: String = HttpMethod.POST, requestMethod: String = RequestMethod.POSTDIRECTLY): SparqlClient = {
    val client = new SparqlClient(
      new URL(endpoint),
      updateEndpointLocation = if (update.nonEmpty) Some(new URL(update.get)) else None,
      format = format,
      defaultGraph = if (defaultGraph.nonEmpty) Some(new URL(defaultGraph.get)) else None
    )
    client.setHttpMethod(httpMethod)
    client.setRequestMethod(requestMethod)
    client
  }
}
