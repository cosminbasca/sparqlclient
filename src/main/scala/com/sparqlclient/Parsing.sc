import java.net.{URI, URL}

import com.sparqlclient.rdf.{Literal, BNode, URIRef, RdfTerm}
import com.sparqlclient.{RequestMethod, DataFormat, SparqlClient}

import scala.xml.{XML, Elem}

val dbpedia = new SparqlClient(new URL("http://dbpedia.org/sparql"))
dbpedia.setQuery( """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?label ?p
    WHERE { <http://dbpedia.org/resource/Asturias> ?p ?label }
    LIMIT 100
                  """)
//dbpedia.setReturnFormat(DataFormat.JSON)
//dbpedia.setReturnFormat(DataFormat.XML)
dbpedia.setReturnFormat(DataFormat.CSV)
//for (results <- dbpedia.query) {
//  for (res <- results) {
//    println(res.n3)
//  }
//}

dbpedia.rawResults()
