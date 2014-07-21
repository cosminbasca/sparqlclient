package com.sparqlclient

import java.net.URL

import scala.collection.mutable
import scala.util.matching.Regex

/**
 * Created by cosmin on 21/07/14.
 */
class SparqlWrapper(val endpoint: URL, val update: Option[URL] = None, val format: String = DataFormat.XML,
                    val defaultGraph: Option[URL] = None, val agent: String = AGENT) {
  val pattern: Regex = """
       ((?P<base>(\s*BASE\s*<.*?>)\s*)|(?P<prefixes>(\s*PREFIX\s+.+:\s*<.*?>)\s*))*
       (?P<queryType>(CONSTRUCT|SELECT|ASK|DESCRIBE|INSERT|DELETE|CREATE|CLEAR|DROP|LOAD|COPY|MOVE|ADD))
                       """.r

  private val updateEndpoint: URL = update.getOrElse(endpoint)
  private val user: Option[String] = None
  private val pass: Option[String] = None
  private val defaultReturnFormat: String = if (ALLOWED_DATA_FORMATS.contains(format)) {
    format
  } else {
    DataFormat.XML
  }
  private var returnFormat: String = defaultReturnFormat
  private val parameters: mutable.Map[String, String] = mutable.Map.empty[String, String]
  private var method: String = GET
  private var queryType: String = QueryType.SELECT
  private var queryString: String = """SELECT * WHERE{ ?s ?p ?o }"""
  private var timeout: Option[Int] = None
  private var requestMethod: String = RequestMethod.URLENCODED

  resetQuery()


  def resetQuery() = {
    parameters.clear()
    defaultGraph match {
      case Some(graph) => parameters.put("default-graph-uri", graph.toString)
    }
    returnFormat = defaultReturnFormat
    method = GET
    queryType = QueryType.SELECT
    queryString = """SELECT * WHERE{ ?s ?p ?o }"""
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
    name match {
      case "query" => false
      case _ =>
        if (parameters.contains(name)) {
          parameters.put(name, )
        } else {

        }
        true
    }
  }
}
