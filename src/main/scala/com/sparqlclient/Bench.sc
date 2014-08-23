import java.net.URI

import com.sparqlclient.rdf.{Literal, BNode, URIRef, RdfTerm}
import net.liftweb.json._

//import com.sparqlclient.{DataFormat, SparqlClient}
//
//val dbpedia = SparqlClient("http://dbpedia.org/sparql", format = DataFormat.Xml)
//val query = """
//    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
//    SELECT ?p ?label
//    WHERE { <http://dbpedia.org/resource/Asturias> ?p ?label }
//    LIMIT 3
//            """
//dbpedia.setQuery(query)
//dbpedia.rawResults(10)

val data =
  """
    |{
    |  "head": { "vars": [ "book" , "title" ]
    |  } ,
    |  "results": {
    |    "bindings": [
    |      {
    |        "book": { "type": "uri" , "value": "http://example.org/book/book6" } ,
    |        "title": { "type": "literal" , "value": "Harry Potter and the Half-Blood Prince" }
    |      } ,
    |      {
    |        "book": { "type": "uri" , "value": "http://example.org/book/book7" } ,
    |        "title": { "type": "literal" , "value": "Harry Potter and the Deathly Hallows" }
    |      } ,
    |      {
    |        "book": { "type": "uri" , "value": "http://example.org/book/book5" } ,
    |        "title": { "type": "literal" , "value": "Harry Potter and the Order of the Phoenix" }
    |      } ,
    |      {
    |        "book": { "type": "uri" , "value": "http://example.org/book/book4" } ,
    |        "title": { "type": "literal" , "value": "Harry Potter and the Goblet of Fire" }
    |      } ,
    |      {
    |        "book": { "type": "uri" , "value": "http://example.org/book/book2" } ,
    |        "title": { "type": "literal" , "value": "Harry Potter and the Chamber of Secrets" }
    |      } ,
    |      {
    |        "book": { "type": "uri" , "value": "http://example.org/book/book3" } ,
    |        "title": { "type": "literal" , "value": "Harry Potter and the Prisoner Of Azkaban" }
    |      } ,
    |      {
    |        "book": { "type": "uri" , "value": "http://example.org/book/book1" } ,
    |        "title": { "type": "literal" , "value": "Harry Potter and the Philosopher's Stone" }
    |      }
    |    ]
    |  }
    |}
  """.stripMargin

implicit val formats = DefaultFormats // Brings in default date formats etc.
case class Binding(`type`: String, `value`: String, `xml:lang`: Option[String], `datatype`: Option[String])
case class Bindings(`bindings`: Map[String, Binding])
def getNode(binding: Binding): RdfTerm = {
  binding.`type` match {
    case "uri" => URIRef(binding.`value`)
    case "bnode" => BNode(binding.`value`)
    case "literal" => new Literal(binding.`value`, language = binding.`xml:lang`)
    case "typed-literal" => Literal(binding.`value`, new URI(binding.`datatype`.get))
    case _ =>
      throw new NoSuchFieldException("json response is badly formatted, field \"type\" not found")
  }
}

val json: JValue = parse(data)
//val header: List[String] = (json \ "head" \ "vars").extract[List[String]]
//val results: List[Bindings] = (json \ "results").extract[List[Bindings]]

