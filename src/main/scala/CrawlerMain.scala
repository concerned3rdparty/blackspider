/**
 * Created by duytd on 15/03/2015.
 */

import models.Crawler
import com.mongodb.casbah.commons.MongoDBObject
import config.Constants
import models.{DBQueueDAO, Url}

object CrawlerMain {

  def main(args: Array[String]): Unit = {
    val targetUrls = Constants.TARGET_URLS
    targetUrls.foreach(url=> {
      val queueSize = DBQueueDAO.count(MongoDBObject("rootUrl"->url._1))
      var isResumeMode = false
      if (queueSize > 0) {
        isResumeMode = true
      }
      val crawler = new Crawler(url._1, isResumeMode = isResumeMode)
      crawler.crawl()
    })
  }
}