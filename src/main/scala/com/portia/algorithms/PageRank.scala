package com.portia.algorithms

import com.mongodb.casbah.commons.MongoDBObject
import models.{EdgeDAO, UrlDAO, Edge, Url}

import scala.collection.mutable.ArrayBuffer
import com.mongodb.casbah.Imports._

/**
 * @author qmha
 */
class PageRank {
  val c:Double = 0.85
  var iterations:Int = 10000
  var urls: Array[Url] = getUrls

  def run: Unit = {
    for (i <- 0 until iterations) {
      println("Iteration " + i)

      // For all URL

      (0 until urls.size).par.foreach(i => {
        var pr = 0.15
        // Loop through all pages that link to this one
        val linkers = getEdgesByUrl(urls(i)._id)
        linkers.foreach(linker => {
          // Get the pagerank of the linker
          val linkingpr = linker.pageRank
          // Get the total number of links from the linker
          val linkingcount = getEdgesByUrl(linker._id).size
          // Pagerank
          pr += c * (linkingpr / linkingcount)
        })

        // Update pagerank to database
        updatePageRank(urls(i)._id, pr)
      })
    }
  }

  def updatePageRank(url_id: ObjectId, pageRank:Double) = {
    val url = Url.findById(url_id).get
    val updatedUrl = url.copy(pageRank = pageRank)
    UrlDAO.update(MongoDBObject("_id"->url_id), updatedUrl, upsert = false, multi = false, new WriteConcern)
  }

  def getUrls:Array[Url] = {
    UrlDAO.find(MongoDBObject.empty).toArray
  }

  def getEdgesByUrl(url_id: ObjectId):ArrayBuffer[Url] = {
    var results:ArrayBuffer[Url] = new ArrayBuffer[Url]()
    val edges = EdgeDAO.find(MongoDBObject("$or"->(MongoDBObject("source"->url_id), MongoDBObject("target"->url_id)))).toList
    edges.foreach(edge => {
      if (edge.source == url_id && edge.target != url_id) {
        //println("Found 1! " + edge.target)
        //println(Url.findById(edge.target).get)
        results += Url.findById(edge.target).get
      }

      if (edge.source != url_id && edge.target == url_id) {
        //println("Found 2! " + edge.target)
        //println(Url.findById(edge.source).get)
        results += Url.findById(edge.source).get
      }
    })

    results
  }
}
