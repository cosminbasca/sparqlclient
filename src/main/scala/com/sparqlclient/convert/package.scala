package com.sparqlclient

import java.net.URI

import com.sparqlclient.rdf.{Literal, BNode, URIRef, RdfTerm}

import scala.io.Source
import scala.util.parsing.json.JSON
import scala.xml.{Node, Elem, XML}

/**
 * Created by basca on 23/07/14.
 *
 * The [[com.sparqlclient.convert]] package holds the builtin conversion functions.
 *
 * All functions in here follow the simple pattern:
 * {{{
 *    def conversionFunction(content: String): Iterator[Seq[RdfTerm]]
 * }}}
 */
package object convert {
  /**
   * convert the given [[http://www.w3.org/TR/sparql11-results-json/ SPARQL JSON query results]] content
   *
   * @param content the actual [[http://www.w3.org/TR/sparql11-results-json/ SPARQL JSON query results]] content to parse
   * @return iterator over sequences of [[com.sparqlclient.rdf.RdfTerm]], all sequences have the same length and
   *         is equal to the number of variables returned by the SPARQL SELECT query.
   */
  def fromJson(content: String): (Seq[String], Iterator[Seq[RdfTerm]]) = {
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
      case None => (Seq.empty, Iterator.empty)
      case Some(json) =>
        val sparqlJsonResults: Map[String, Any] = json.asInstanceOf[Map[String, Any]]
        val header: Map[String, List[String]] = sparqlJsonResults("head").asInstanceOf[Map[String, List[String]]]
        val results: Map[String, List[Map[String, Map[String, String]]]] =
          sparqlJsonResults("results").asInstanceOf[Map[String, List[Map[String, Map[String, String]]]]]
        val resultsIterator:Iterator[Seq[RdfTerm]] = for (binding <- results("bindings").iterator) yield
          for (column <- header("vars")) yield
            getNode(binding(column))
        (header("vars"), resultsIterator)
    }
  }


  /**
   * convert the given [[http://www.w3.org/TR/rdf-sparql-XMLres/ SPARQL XML query results]] content
   *
   * @param content the actual [[http://www.w3.org/TR/rdf-sparql-XMLres/ SPARQL XML query results]] content to parse
   * @return iterator over sequences of [[com.sparqlclient.rdf.RdfTerm]], all sequences have the same length and
   *         is equal to the number of variables returned by the SPARQL SELECT query.
   */
  def fromXML(content: String): (Seq[String], Iterator[Seq[RdfTerm]]) = {
    def getNode(binding: Node): RdfTerm = {
      (binding \ "_").headOption match {
        case Some(node) => node.label match {
          case "uri" => URIRef(node.text.trim)
          case "bnode" => BNode(node.text.trim)
          case "literal" =>
            val dataType: Option[Seq[Node]] = node.attribute("datatype")
            val language: Option[Seq[Node]] = node.attribute(Namespaces.Xml.toString, "lang")
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
    val resultsIterator:Iterator[Seq[RdfTerm]] = for (result <- (sparql \ "results" \ "result").iterator) yield
      for (binding <- result \ "binding") yield
        getNode(binding)
    (header, resultsIterator)
  }


  /**
   * convert the given SPARQL RDF query results content
   *
   * @param content the actual SPARQL RDF query results content to parse
   * @return iterator over sequences of [[com.sparqlclient.rdf.RdfTerm]], all sequences have the same length and
   *         is equal to the number of variables returned by the SPARQL SELECT query.
   */
  def fromRDF(content: String): (Seq[String], Iterator[Seq[RdfTerm]]) = {
    def getNode(binding: Node): RdfTerm = {
      (binding \ "value").headOption match {
        case Some(node) =>
          val dataType: Option[Seq[Node]] = node.attribute("datatype")
          val language: Option[Seq[Node]] = node.attribute(Namespaces.Xml.toString, "lang")
          val uri: Option[Seq[Node]] = node.attribute(Namespaces.Rdf.toString, "resource")

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
    val header: Seq[String] = Seq.empty
    val resultsIterator:Iterator[Seq[RdfTerm]] = for (result <- (rdf \ "Description" \ "solution").iterator) yield
      for (binding <- result \ "binding") yield
        getNode(binding)
    (header, resultsIterator)
  }


  /**
   * simple converter for the given [[http://www.w3.org/TR/sparql11-results-csv-tsv/ SPARQL CSV query results]] content
   *
   * note: this function cannot differentiate between different types of [[com.sparqlclient.rdf.Literal]] and will
   * always create simple rdf literals. In addition if will heuristically try to detect URI's (starting with http: or
   * https: ) and blank nodes (starting with _:). Everything else is converted to a plain literal. It is advisable to
   * use other data formats (see json or xml) to get a correct and comprehensive parsing of the query results.
   *
   * @param content the actual [[http://www.w3.org/TR/sparql11-results-csv-tsv/ SPARQL CSV query results]] content to parse
   * @return iterator over sequences of [[com.sparqlclient.rdf.RdfTerm]], all sequences have the same length and
   *         is equal to the number of variables returned by the SPARQL SELECT query.
   */
  def fromCSV(content: String): (Seq[String], Iterator[Seq[RdfTerm]]) = {
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
    val lines: Iterator[String] = src.getLines()
    val header: Seq[String] = lines.next().split(",").toSeq
    val resultsIterator:Iterator[Seq[RdfTerm]] = for (line <- lines) yield
      for (term <- line.split(",").toSeq) yield
        getNode(term)

    (header, resultsIterator)
  }
}
