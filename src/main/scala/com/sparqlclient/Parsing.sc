import java.net.URL

import com.sparqlclient.rdf.Node
import com.sparqlclient.{RequestMethod, DataFormat, SparqlClient}

val dbpedia = new SparqlClient(new URL("http://dbpedia.org/sparql"))
dbpedia.setQuery( """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?label
    WHERE { <http://dbpedia.org/resource/Asturias> rdfs:label ?label }
    LIMIT 100
                  """)
dbpedia.setReturnFormat(DataFormat.JSON)
val str = dbpedia.waitForResults()

import scala.util.parsing.json._

class CC[T] { def unapply(a:Any):Option[T] = Some(a.asInstanceOf[T]) }

object M extends CC[Map[String, Any]]
object L extends CC[List[Any]]
object S extends CC[String]
object D extends CC[Double]
object B extends CC[Boolean]

def jsonResultsIterator(content:String): Iterator[Seq[Node]] = {
  val json:Option[Any] = JSON.parseFull(str)
  json match {
    case None => Iterator.empty[Seq[String]]
    case Some(M(sparqlJsonResults)) =>
      val header:List[String] = sparqlJsonResults("head").asInstanceOf[List[String]]
      val results:Map[String, List[Map[String, Map[String, String]]]] =
        sparqlJsonResults("results").asInstanceOf[Map[String, List[Map[String, Map[String, String]]]]]
      for (binding <- results("bindings")) yield binding
      ???
  }
}