import java.net.URL

import com.sparqlclient.{RequestMethod, DataFormat, SparqlClient}

val dbpedia = new SparqlClient(new URL("http://dbpedia.org/sparql"))
dbpedia.setQuery( """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    SELECT ?label
    WHERE { <http://dbpedia.org/resource/Asturias> rdfs:label ?label }
    LIMIT 100
                  """)
dbpedia.setReturnFormat(DataFormat.CSV)
val str = dbpedia.waitForResults()

