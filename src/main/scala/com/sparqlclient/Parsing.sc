import java.net.URL

import com.sparqlclient.{DataFormat, SparqlClient}

val dbpedia = new SparqlClient(new URL("http://dbpedia.org/sparql"))
dbpedia.setMethod("POST")
dbpedia.setQuery("select distinct ?Concept where {[] a ?Concept} LIMIT 100")
dbpedia.setReturnFormat(DataFormat.CSV)
dbpedia.waitForResults()

