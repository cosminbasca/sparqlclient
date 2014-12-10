sparqlclient
============

sparqlclient is a scala port of the excellent [SPARQLWrapper](http://rdflib.github.io/sparqlwrapper/) python module

Important Notes
---------------
This software is the product of research carried out at the [University of Zurich](http://www.ifi.uzh.ch/ddis.html) and comes with no warranty whatsoever. Have fun!

TODO's
------
* (more) unit tests
* more documentation
* (more) examples

Gotcha's
--------
Every time the project version information is changed, BuildInfo needs to be regenerated. To do that simply run:

```sh
$ sbt compile
```

Example
-------

```scala
import com.sparqlclient.{DataFormat, SparqlClient}
import com.sparqlclient.rdf.RdfTerm

val dbpedia = SparqlClient("http://dbpedia.org/sparql")
dbpedia.setReturnFormat(DataFormat.Xml)

val query = s"""
select distinct ?Concept where {[] a ?Concept} LIMIT 100
            """
val results:Seq[Seq[RdfTerm]] = dbpedia(query, 10)._2.toSeq
```

Thanks a lot to
---------------
* [University of Zurich](http://www.ifi.uzh.ch/ddis.html) and the [Swiss National Science Foundation](http://www.snf.ch/en/Pages/default.aspx) for generously funding the research that led to this software.
