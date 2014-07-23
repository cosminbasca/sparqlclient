package com.sparqlclient

import java.net.URI

import com.sparqlclient.rdf.{Literal, BNode, URIRef, RdfTerm}

import scala.io.Source
import scala.util.parsing.json.JSON
import scala.xml.{Node, Elem, XML, NamespaceBinding}

/**
 * Created by basca on 23/07/14.
 */
package object convert {
  def fromJson(content: String): Iterator[Seq[RdfTerm]] = {
    def getNode(jsonRepr: Map[String, String]): RdfTerm = {
      jsonRepr.get("type") match {
        case Some("uri") => URIRef(jsonRepr("value"))
        case Some("bnode") => BNode(jsonRepr("value"))
        case Some("literal") => new Literal(jsonRepr("value"), language = jsonRepr.get("xml:lang"))
        case Some("typed-literal") => Literal(jsonRepr("value"), new URI(jsonRepr("datatype")))
        case _ | None =>
          throw new NoSuchFieldException("json response is badly formatted, field \"type\" not found")
      }
    }

    JSON.parseFull(content) match {
      case None => Iterator.empty
      case Some(json) =>
        val sparqlJsonResults: Map[String, Any] = json.asInstanceOf[Map[String, Any]]
        val header: Map[String, List[String]] = sparqlJsonResults("head").asInstanceOf[Map[String, List[String]]]
        val results: Map[String, List[Map[String, Map[String, String]]]] =
          sparqlJsonResults("results").asInstanceOf[Map[String, List[Map[String, Map[String, String]]]]]
        for (binding <- results("bindings").iterator) yield
          for (column <- header("vars")) yield
            getNode(binding(column))
    }
  }


  def fromXML(content: String): Iterator[Seq[RdfTerm]] = {
    def getNode(binding: Node): RdfTerm = {
      (binding \ "_").headOption match {
        case Some(node) => node.label match {
          case "uri" => URIRef(node.text.trim)
          case "bnode" => BNode(node.text.trim)
          case "literal" =>
            val dataType: Option[Seq[Node]] = node.attribute("datatype")
            val language: Option[Seq[Node]] = node.attribute(XML_NS, "lang")
            if (language.nonEmpty) {
              Literal(node.text.trim, language.get.head.text)
            } else if (dataType.nonEmpty) {
              Literal(node.text.trim, new URI(dataType.get.head.text))
            } else {
              Literal(node.text.trim)
            }
          case _ =>
            throw new NoSuchFieldException(s"xml node: $binding cannot be parsed into an rdf term")
        }
        case None =>
          throw new NoSuchFieldException(s"xml node: $binding cannot be parsed into an rdf term")
      }
    }

    val sparql: Elem = XML.loadString(content)
    val header: Seq[String] = for (variable <- sparql \ "head") yield (variable \ "@name").text
    for (result <- (sparql \ "results" \ "result").iterator) yield
      for (binding <- result \ "binding") yield
        getNode(binding)
  }


  def fromRDF(content: String): Iterator[Seq[RdfTerm]] = {
    def getNode(binding: Node): RdfTerm = {
      (binding \ "value").headOption match {
        case Some(node) =>
          val dataType: Option[Seq[Node]] = node.attribute("datatype")
          val language: Option[Seq[Node]] = node.attribute(XML_NS, "lang")
          val uri: Option[Seq[Node]] = node.attribute(RDF_NS, "resource")

          if (uri.nonEmpty) {
            URIRef(uri.get.head.text)
          } else if (language.nonEmpty) {
            Literal(node.text.trim, language.get.head.text)
          } else if (dataType.nonEmpty) {
            Literal(node.text.trim, new URI(dataType.get.head.text))
          } else {
            Literal(node.text.trim)
          }
          // TODO: bnode handling is missing here ...
        case None =>
          throw new NoSuchFieldException(s"rdf-xml node: $binding cannot be parsed into an rdf term")
      }
    }

    val rdf: Elem = XML.loadString(content)
    for (result <- (rdf \ "Description" \ "solution").iterator) yield
      for (binding <- result \ "binding") yield
        getNode(binding)
  }


  def fromCSV(content: String): Iterator[Seq[RdfTerm]] = {
    def getNode(term: String): RdfTerm = {
      val value: String = term.replaceAll("\"", "")
      if (value.startsWith("http:") || value.startsWith("https:"))
        URIRef(value)
      else if (value.startsWith("_:"))
        BNode(value)
      else
        Literal(value)
    }

    val src: Source = Source.fromString(content)

    for ((line: String, i: Int) <- src.getLines().zipWithIndex if i > 0) yield
      for (term <- line.split(",").toSeq) yield
        getNode(term)
  }
}
