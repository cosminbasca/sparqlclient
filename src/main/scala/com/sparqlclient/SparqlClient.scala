//
// author: Cosmin Basca
//
// Copyright 2010 University of Zurich
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.sparqlclient

import java.net.URL
import java.nio.charset.StandardCharsets

import com.ning.http.client.Response
import com.sparqlclient.rdf.RdfTerm

import scala.beans.BeanProperty
import scala.collection.mutable
import scala.concurrent.{Promise, Await}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import scala.util.matching.Regex
import dispatch._, Defaults._
import scala.collection.JavaConversions._

/**
 * Created by cosmin on 21/07/14.
 */

/**
 * a [[http://www.w3.org/TR/sparql11-protocol/ SPARQL protocol]] client. By default (unless specified otherwise) the
 * http method used is [[com.sparqlclient.HttpMethod.GET]] and the request is performed using the [[com.sparqlclient.RequestMethod.UrlEncoded]] method.
 *
 * @param endpointLocation the endpoint's location url
 * @param updateEndpointLocation the endpoints's [[http://www.w3.org/TR/sparql11-update/ SPARQL UPDATE]] url
 *                               (if different from the endpoint's location)
 * @param format the desired returned data format (default is [[com.sparqlclient.DataFormat.Xml]] "xml"). By default the response's
 *               __"Content-type"__ header is used to determine the appropriate data model.
 *               If the server does not provide this header, the data model falls back to this value.
 *               Must be one of [[com.sparqlclient.AllowedDataFormats]]
 * @param defaultGraph the default graph to query against (default not specified,
 *                     the query is executed against the entire graph)
 * @param agent the client agent (default is [[com.sparqlclient.Agent]])
 * @param connectionTimeout the connection timeout in seconds (default is 60 seconds)
 */
class SparqlClient(val endpointLocation: URL, val updateEndpointLocation: Option[URL] = None, val format: DataFormat.Value = DataFormat.Xml,
                   val defaultGraph: Option[URL] = None, val agent: String = Agent, connectionTimeout: Int = 60) {
  // -------------------------------------------------------------------------------------------------------------------
  //
  // internal state
  //
  // -------------------------------------------------------------------------------------------------------------------
  private val pattern: Regex = """(?i)((\s*BASE\s*<.*?>)\s*|(\s*PREFIX\s+.+:\s*<.*?>)\s*)*(CONSTRUCT|SELECT|ASK|DESCRIBE|INSERT|DELETE|CREATE|CLEAR|DROP|LOAD|COPY|MOVE|ADD)""".r
  private val GroupQueryType: Int = 4
  private val base64encoder: sun.misc.BASE64Encoder = new sun.misc.BASE64Encoder()

  private val parameters: mutable.Map[String, Seq[String]] = mutable.Map.empty[String, Seq[String]]
  private val updateEndpoint: URL = updateEndpointLocation.getOrElse(endpointLocation)
  private val defaultReturnFormat: DataFormat.Value = if (AllowedDataFormats.contains(format)) format else DataFormat.Xml
  private var user: Option[String] = None
  private var pass: Option[String] = None
  private var returnFormat: DataFormat.Value = defaultReturnFormat
  private var queryType: QueryType.Value = QueryType.Select
  private var queryString: String = DefaultSparqlQuery
  @BeanProperty
  var httpMethod: HttpMethod.Value = HttpMethod.GET
  @BeanProperty
  var requestMethod: RequestMethod.Value = RequestMethod.UrlEncoded
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
        appendParameter(parameters, "default-graph-uri", graph.toString)
      case None =>
    }
    returnFormat = defaultReturnFormat
    queryType = QueryType.Select
    httpMethod = HttpMethod.GET
    queryString = DefaultSparqlQuery
    requestMethod = RequestMethod.UrlEncoded
  }

  /**
   * set the returned data format.
   * @param format the data format: must be one of [[com.sparqlclient.AllowedDataFormats]]
   */
  def setReturnFormat(format: DataFormat.Value) = {
    if (AllowedDataFormats.contains(format)) {
      returnFormat = format
    }
  }

  /**
   * Append new parameter values. If the parameter does not exist a new Sequence of values (with the given value) is
   * created
   *
   * @param name the name of the parameter
   * @param value the value
   */
  private def appendParameter(requestParameters: mutable.Map[String, Seq[String]], name: String, value: String) = {
    val values: Seq[String] = requestParameters.get(name) match {
      case Some(paramValues) => paramValues ++ Seq(value)
      case None => Seq(value)
    }
    requestParameters(name) = values
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
   * @return true if the parameter was set successfully (must be one of [[SparqlParameters]]), false otherwise
   */
  def addParameter(name: String, value: String): Boolean = {
    if (SparqlParameters.contains(name)) {
      false
    } else {
      appendParameter(parameters, name, value)
      true
    }
  }

  /**
   * get the values associated with this parameter (if any)
   *
   * @param name the parameter name
   * @return a sequence of values, or None if parameter is not set
   */
  def getParameter(name: String): Option[Seq[String]] = {
    parameters.get(name)
  }

  /**
   * clears all the values of the specified parameter (if any)
   *
   * @param name the parameter name
   * @return true if the values were cleared false otherwise
   *         (the method returns false also when the parameter is not found)
   */
  def clearParameter(name: String): Boolean = {
    if (SparqlParameters.contains(name)) {
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
   *         It defaults to [[com.sparqlclient.QueryType.Select]]
   */
  private def parseQueryType(query: String): QueryType.Value = {
    def getQueryType(strType: String): Option[QueryType.Value] = {
      try {
        Some(QueryType.withName(strType))
      } catch {
        case e: NoSuchElementException => None
      }
    }

    pattern.findFirstMatchIn(query) match {
      case Some(firstMatch) =>
        getQueryType(firstMatch.group(GroupQueryType).toUpperCase) match {
          case Some(qType) => qType
          case None => QueryType.Select
        }
      case None => QueryType.Select
    }
  }

  /**
   * test if the request is for an UPDATE operation
   * @return true if an UPDATE operation false otherwise
   */
  private def isSparqlUpdateRequest: Boolean = {
    InsertQueryTypes.contains(queryType)
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
      case QueryType.Select | QueryType.Ask =>
        returnFormat match {
          case DataFormat.Xml => SparqlXmlResultFormats.mkString(",")
          case DataFormat.Json => SparqlJsonResultFormats.mkString(",")
          case _ => AnyDataFormats.mkString(",")
        }
      case QueryType.Insert | QueryType.Delete =>
        MimeType.Any.toString
      case _ =>
        returnFormat match {
          case DataFormat.N3 | DataFormat.Turtle => RdfN3DataFormats.mkString(",")
          case DataFormat.Xml => RdfXmlDataFormats.mkString(",")
          //          case DataFormat.JSONLD => RDF_JSONLD.mkString(",")
          case _ => AnyDataFormats.mkString(",")
        }
    }
  }


  /**
   * create the request according to the guidelines of the [[http://www.w3.org/TR/sparql11-protocol/ SPARQL protocol]]
   * @return the request
   * @throws UnsupportedOperationException if the request method for an update operation is not [[HttpMethod.POST]]
   */
  private def createRequest: Req = {
    // request specific parameters copy
    val requestParameters: mutable.Map[String, Seq[String]] = mutable.Map.empty[String, Seq[String]] ++ parameters
    // return format as defined by various endpoints ... this is not cool, and should be standardised in the future ...
    for (fmt <- ReturnFormatParameters)
      appendParameter(requestParameters, fmt, returnFormat.toString)

    // create the request according to how it was specified
    val request: Req = if (isSparqlUpdateRequest) {
      if (httpMethod != HttpMethod.POST) {
        throw new UnsupportedOperationException("update operations MUST be done by POST")
      }
      requestMethod match {
        case RequestMethod.PostDirectly =>
          url(updateEndpoint.toString).POST.
            addHeader("Content-Type", MimeType.SparqlUpdate.toString).
            setQueryParameters(requestParameters.toMap).
            setBody(queryString)
        case _ =>
          appendParameter(requestParameters, "update", queryString)
          url(updateEndpoint.toString).POST.
            setHeader("Content-Type", MimeType.UrlFormEncoded.toString).
            setParameters(requestParameters.toMap)
      }
    } else {
      httpMethod match {
        case HttpMethod.POST =>
          requestMethod match {
            case RequestMethod.PostDirectly =>
              url(endpointLocation.toString).POST.
                addHeader("Content-Type", MimeType.SparqlQuery.toString).
                setQueryParameters(requestParameters.toMap).
                setBody(queryString)
            case _ =>
              appendParameter(requestParameters, "query", queryString)
              url(endpointLocation.toString).POST.
                setHeader("Content-Type", MimeType.UrlFormEncoded.toString).
                setParameters(requestParameters.toMap)
          }
        case HttpMethod.GET =>
          appendParameter(requestParameters, "query", queryString)
          url(endpointLocation.toString).GET.
            setQueryParameters(requestParameters.toMap)
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
   * auto-detection of the correct returned data format.
   *
   * note: this method may fail if the [[SparqlClient.returnFormat]] does not match the actual returned data format and
   * the server does not set the appropriate __"Content-type"__ header!
   *
   * @param contentTypes the request set __"Content-type"__ header value
   * @return the correctly identified data format (will be one of [[com.sparqlclient.DataFormat]])
   */
  private def detectDataFormat(contentTypes: Seq[String]): DataFormat.Value = {
    def getContentMimeType(strMimeType: String): Option[MimeType.Value] = {
      try {
        Some(MimeType.withName(strMimeType))
      } catch {
        case e: NoSuchElementException => None
      }
    }

    val contentMimeTypes: Seq[MimeType.Value] = contentTypes.
      map(ct => getContentMimeType(ct.split(";").head.trim)).dropWhile(_.isEmpty).map(mt => mt.get)

    val dataFormats: Seq[DataFormat.Value] = contentMimeTypes.
      map(mt => MimeTypesDataFormat.get(mt)).dropWhile(_.isEmpty).map(df => df.get)

    if (dataFormats.nonEmpty) dataFormats.head else returnFormat
  }

  /**
   * the parsed query results
   * this method is non-blocking
   *
   * currently only the following formats are supported:
   * - [[DataFormat.Json]], see [[com.sparqlclient.convert.fromJson]] method for further details
   * - [[DataFormat.Xml]], see [[com.sparqlclient.convert.fromXML]] method for further details
   * - [[DataFormat.Rdf]], see [[com.sparqlclient.convert.fromRDF]] method for further details
   * - [[DataFormat.Csv]], see [[com.sparqlclient.convert.fromCSV]] method for further details
   *
   * @return a [[scala.concurrent.Future]] of the [[scala.collection.Iterator]] over the parsed results
   *         or a [[HttpException]] if the method fails, wrapped in an [[Either]]
   */
  def queryResults: Future[Either[HttpException, (Seq[String], Iterator[Seq[RdfTerm]])]] = {
    val results: Promise[Either[HttpException, (Seq[String], Iterator[Seq[RdfTerm]])]] = Promise[Either[HttpException, (Seq[String], Iterator[Seq[RdfTerm]])]]()
    http(createRequest) onComplete {
      case Success(response: Response) =>
        val resultsIterator:Either[HttpException, (Seq[String], Iterator[Seq[RdfTerm]])] = if (response.getStatusCode / 100 == 2) {
          val results = detectDataFormat(response.getHeaders("Content-type")) match {
            case DataFormat.Json => convert.fromJson(response.getResponseBody)
            case DataFormat.Xml => convert.fromXML(response.getResponseBody)
            case DataFormat.Rdf => convert.fromRDF(response.getResponseBody)
            case DataFormat.Csv => convert.fromCSV(response.getResponseBody)
            case DataFormat.N3 | DataFormat.Turtle | DataFormat.JsonLD =>
              throw new UnsupportedOperationException(s"parsing n3 / turtle / json-ld is not yet supported, change the returnFormat to any fo the following: ${
                List(
                  DataFormat.Json, DataFormat.Xml, DataFormat.Rdf, DataFormat.Csv)
              }")
            case _ => (Seq.empty, Iterator.empty)
          }
          Right(results)
        } else {
          println(s"HTTP Request failed with status code: ${response.getStatusCode} \n response: ${response.toString}")
          Left(HttpException(response))
        }

        results.success(resultsIterator)
      case Failure(exception) =>
        exception
    }
    results.future
  }

  /**
   * the parsed query results
   * this method is blocking
   *
   * @return an [[scala.collection.Iterator]] over the parsed results or an empty iterator if the method fails
   * @throws the corresponding [[HttpException]] if the request did not complete properly
   */
  def queryResults(duration: Int): (Seq[String], Iterator[Seq[RdfTerm]]) = {
    Await.result[Either[HttpException, (Seq[String], Iterator[Seq[RdfTerm]])]](queryResults, Duration(duration, "seconds")) match {
      case Right(results) => results
      case Left(httpException) => throw httpException
    }
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
  def apply(query: String, duration: Int): (Seq[String], Iterator[Seq[RdfTerm]]) = {
    val originalQuery: String = queryString
    setQuery(query)
    val results: (Seq[String], Iterator[Seq[RdfTerm]]) = queryResults(duration)
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
   * @return a [[scala.concurrent.Future]] of the [[scala.collection.Iterator]] over the parsed results
   *         or a [[HttpException]] if the method fails, wrapped in an [[Either]]
   */
  def apply(query: String): Future[Either[HttpException, (Seq[String], Iterator[Seq[RdfTerm]])]] = {
    val originalQuery: String = queryString
    setQuery(query)
    val futureResults: Future[Either[HttpException, (Seq[String], Iterator[Seq[RdfTerm]])]] = queryResults
    setQuery(originalQuery)
    futureResults
  }


  /**
   * the raw results (string representation as returned from the request)
   * this method blocks
   *
   * note: this method fails silently
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
   * note: this method fails silently
   *
   * @return a [[scala.concurrent.Future]] of the raw results
   */
  def rawResults: Future[String] = {
    http(createRequest OK as.String)
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
   * @param format the desired returned data format (by default [[DataFormat.Xml]] due to its wide support)
   * @param defaultGraph the default data graph (by default None)
   * @param httpMethod the http method used (by default [[HttpMethod.POST]])
   * @param requestMethod the request method used (by default [[RequestMethod.UrlEncoded]])
   * @return the [[SparqlClient]] instance
   */
  def apply(endpoint: String, update: Option[String] = None, format: DataFormat.Value = DataFormat.Xml,
            defaultGraph: Option[String] = None,
            httpMethod: HttpMethod.Value = HttpMethod.POST,
            requestMethod: RequestMethod.Value = RequestMethod.UrlEncoded): SparqlClient = {
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
