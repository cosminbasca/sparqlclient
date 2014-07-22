package com.sparqlclient

import java.net.URL

import scala.collection.mutable
import scala.util.matching.Regex

/**
 * Created by cosmin on 21/07/14.
 */
class SparqlWrapper(val endpoint: URL, val update: Option[URL] = None, val format: String = DataFormat.XML,
                    val defaultGraph: Option[URL] = None, val agent: String = AGENT) {
  val pattern: Regex = new Regex( """(?i)((\s*BASE\s*<.*?>)\s*|(\s*PREFIX\s+.+:\s*<.*?>)\s*)*(CONSTRUCT|SELECT|ASK|DESCRIBE|INSERT|DELETE|CREATE|CLEAR|DROP|LOAD|COPY|MOVE|ADD)""",
    "g0","base", "prefixes", "queryType")

  private val updateEndpoint: URL = update.getOrElse(endpoint)
  private var user: Option[String] = None
  private var pass: Option[String] = None
  private val defaultReturnFormat: String = if (ALLOWED_DATA_FORMATS.contains(format)) {
    format
  } else {
    DataFormat.XML
  }
  private var returnFormat: String = defaultReturnFormat
  private val parameters: mutable.Map[String, String] = mutable.Map.empty[String, String]
  private var method: String = GET
  private var queryType: String = QueryType.SELECT
  private var queryString: String = DEFAULT_SPARQL
  private var timeout: Option[Int] = None
  private var requestMethod: String = RequestMethod.URLENCODED

  resetQuery()


  def resetQuery() = {
    println("BEFORE")
    parameters.clear()
    defaultGraph match {
      case Some(graph) => parameters.put("default-graph-uri", graph.toString)
    }
    returnFormat = defaultReturnFormat
    method = GET
    queryType = QueryType.SELECT
    queryString = DEFAULT_SPARQL
    timeout = None
    requestMethod = RequestMethod.URLENCODED
    println("HERE")
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
      //TODO: implement
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

  def parseQueryType(query: String): String = {
    pattern.findFirstMatchIn(query) match {
      case Some(firstMatch) =>
        val qType: String = firstMatch.group("queryType").toUpperCase
        println(qType)
        if (ALLOWED_QUERY_TYPES.contains(qType)) {
          qType
        } else {
          QueryType.SELECT
        }
      case None => QueryType.SELECT
    }
  }
}
