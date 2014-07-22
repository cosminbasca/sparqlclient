package com.sparqlclient

import java.net.URL
import java.nio.charset.{StandardCharsets, Charset}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.matching.Regex
import dispatch._, Defaults._

/**
 * Created by cosmin on 21/07/14.
 */
class SparqlClient(val endpoint: URL, val update: Option[URL] = None, val format: String = DataFormat.XML,
                   val defaultGraph: Option[URL] = None, val agent: String = AGENT) {
  private val pattern: Regex = """(?i)((\s*BASE\s*<.*?>)\s*|(\s*PREFIX\s+.+:\s*<.*?>)\s*)*(CONSTRUCT|SELECT|ASK|DESCRIBE|INSERT|DELETE|CREATE|CLEAR|DROP|LOAD|COPY|MOVE|ADD)""".r
  private val GROUP_BASE: Int = 2
  private val GROUP_PREFIXE: Int = 3
  private val GROUP_QUERY_TYPE: Int = 4
  private val base64encoder: sun.misc.BASE64Encoder = new sun.misc.BASE64Encoder()

  private val parameters: mutable.Map[String, ListBuffer[String]] = mutable.Map.empty[String, ListBuffer[String]].withDefaultValue(new ListBuffer())
  private val updateEndpoint: URL = update.getOrElse(endpoint)
  private val defaultReturnFormat: String = if (ALLOWED_DATA_FORMATS.contains(format)) {
    format
  } else {
    DataFormat.XML
  }
  private var user: Option[String] = None
  private var pass: Option[String] = None
  private var returnFormat: String = defaultReturnFormat
  private var queryType: String = QueryType.SELECT
  private var queryString: String = DEFAULT_SPARQL
  private var timeout: Option[Int] = None
  private var method: String = GET
  private var requestMethod: String = RequestMethod.URLENCODED

  reset()


  private def reset() = {
    parameters.clear()
    defaultGraph match {
      case Some(graph) =>
        parameters("default-graph-uri").append(graph.toString)
      case None =>
    }
    returnFormat = defaultReturnFormat
    queryType = QueryType.SELECT
    method = GET
    queryString = DEFAULT_SPARQL
    timeout = None
    requestMethod = RequestMethod.URLENCODED
  }

  def setReturnFormat(format: String) = {
    if (ALLOWED_DATA_FORMATS.contains(format)) {
      returnFormat = format
    }
  }

  def setTimeout(timeout: Int) = {
    this.timeout = Some(timeout)
  }

  def setRequestMethod(method: String) = {
    if (ALLOWED_REQUESTS_METHODS.contains(method)) {
      requestMethod = method
    } else {
      println(s"Invalid update method: $method")
    }
  }

  def addParameter(name: String, value: String): Boolean = {
    if (SPARQL_PARAMS.contains(name)) {
      false
    } else {
      parameters(name).append(value)
      true
    }
  }

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

  def setCredentials(user: String, pass: String) = {
    this.user = Some(user)
    this.pass = Some(pass)
  }

  def setQuery(query: String) = {
    queryString = query
    queryType = parseQueryType(query)
  }

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

  def setMethod(method: String) = {
    if (ALLOWED_REQUESTS.contains(method)) {
      this.method = method
    }
  }

  def isSparqlUpdateRequest: Boolean = {
    INSERT_QUERY_TYPE.contains(queryType)
  }

  def isSparqlQueryRequest: Boolean = {
    !isSparqlUpdateRequest
  }

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

  private def createRequest: Req = {
    // return format as defined by various endpoints ... this is not cool, and should be standardised in the future
    for (fmt <- RETURN_FORMAT_PARAMS)
      parameters(fmt).append(returnFormat)


    def get_parameters: Map[String, Seq[String]] = {
      parameters.map {
        case (k, v) => (k, v.toSeq)
      }.toMap
    }

    // create the request according to how it was specified
    val request: Req = if (isSparqlUpdateRequest) {
      if (method != POST) {
        throw new UnsupportedOperationException("update operations MUST be done by POST")
      }

      val req = url(updateEndpoint.toString).POST
      requestMethod match {
        case RequestMethod.POSTDIRECTLY =>
          req.addHeader("Content-Type", MimeType.SPARQL_UPDATE)
          req.setQueryParameters(get_parameters)
          req.setBody(queryString)
        case _ =>
          parameters("update").append(queryString)
          req.setHeader("Content-Type", MimeType.URL_FORM_ENCODED)
          req.setParameters(get_parameters)
      }

      req
    } else {
      method match {
        case POST =>
          val req: Req = url(endpoint.toString).POST
          requestMethod match {
            case RequestMethod.POSTDIRECTLY =>
              req.addHeader("Content-Type", MimeType.SPARQL_UPDATE)
              req.setQueryParameters(get_parameters)
              req.setBody(queryString)
            case _ =>
              parameters("query").append(queryString)
              req.setHeader("Content-Type", MimeType.URL_FORM_ENCODED)
              req.setParameters(get_parameters)
          }
          req
        case GET =>
          var req: Req = url(endpoint.toString)
          parameters("query").append(queryString)
          println("--------------------------------------------------------")
          println(parameters)
          req = req.setQueryParameters(get_parameters)
          println(get_parameters)
          println(req.toRequest.toString)
          println("--------------------------------------------------------")
          req
      }
    }

    request.setHeader("User-Agent", agent)
    request.setHeader("Accept", getAcceptHeader)
    if (user.nonEmpty && pass.nonEmpty) {
      val credentials: String = s"${user.get}:${pass.get}"
      request.setHeader("Authorization", s"Basic ${base64encoder.encode(credentials.getBytes(StandardCharsets.UTF_8))}")
    }
    request
  }

  private def queryRequest: Future[String] = {
    val request: Req = createRequest
    val http: Http = timeout match {
      case Some(tout) => Http.configure(_.setAllowPoolingConnection(true).setConnectionTimeoutInMs(tout / 1000))
      case None => Http.configure(_.setAllowPoolingConnection(true))
    }
    http(request OK as.String)
  }

  def waitForResults(duration:Int=10): String = {
    Await.result[String](queryRequest, Duration(duration, "seconds"))
  }

}
