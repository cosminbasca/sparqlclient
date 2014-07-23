package com.sparqlclient

import java.net.URI

import com.sparqlclient.rdf.{Literal, BNode, URIRef, Node}

import scala.util.parsing.json.JSON

/**
 * Created by basca on 23/07/14.
 */
package object convert {
  def json(content: String): Iterator[Seq[Node]] = {
    def getNode(jsonRepr: Map[String, String]): Node = {
      jsonRepr.get("type") match {
        case Some("uri") => URIRef(jsonRepr("value"))
        case Some("bnode") => BNode(jsonRepr("value"))
        case Some("literal") => new Literal(jsonRepr("value"), language = jsonRepr.get("xml:lang"))
        case Some("typed-literal") => Literal(jsonRepr("value"), new URI(jsonRepr("datatype")))
        case None => throw new NoSuchFieldException("json response is badly formatted, field \"type\" not found")
      }
    }
    val json: Option[Any] = JSON.parseFull(content)
    json match {
      case None => Iterator.empty
      case Some(sparqlJsonResults: Map[String, Any]) =>
        val header: Map[String, List[String]] = sparqlJsonResults("head").asInstanceOf[Map[String, List[String]]]
        val results: Map[String, List[Map[String, Map[String, String]]]] =
          sparqlJsonResults("results").asInstanceOf[Map[String, List[Map[String, Map[String, String]]]]]
        for (binding <- results("bindings").iterator) yield
          for (column <- header("vars")) yield
            getNode(binding(column))
    }
  }
}
