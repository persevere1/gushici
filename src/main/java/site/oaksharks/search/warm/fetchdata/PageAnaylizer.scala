package site.oaksharks.search.warm.fetchdata

import java.io.File
import java.util

import org.jsoup.Jsoup
import org.jsoup.nodes.{Node, Element}
import org.slf4j.LoggerFactory
import site.oaksharks.search.warm.CONS
import java.util.{List => JavaList}

import site.oaksharks.search.warm.persistence.Page

/**
  * DESC: 指定网页文件转换为对象
  *
  * Created by Administrator on 2016/8/18.
  */
class PageAnaylizer {

  val log = LoggerFactory.getLogger(classOf[PageAnaylizer])

  /**
    * DESC: 根据指定路径读取文件生存document
 *
    * @param path 古诗文路径
    * @return
    */
  def fetcher(path:String) = {

    val doc = Jsoup.parse(new File(path),"UTF-8")
    try {
      this.parase2Page(doc.body(), getId(path))
    }
    catch {
      case ex:Exception => {
        log.error("解析器: 解析网页异常",ex)
      }
    }

  }

  /**
    * DESC: html 转换page 对象
    *
    * @param body
    */
  private def parase2Page(body:Element,pageId:String)={
    log.info("解析器: 开始分析网页[{}]",pageId)
    val contentDiv = body.select("div.main3 > div.shileft").get(0)
    // title
    val title = contentDiv.child(0).child(0).html()
    // 主题信息
    // 使用原文 关键字读取 真正的文章 
    val main = contentDiv.child(1)
    val page = new Page(pageId)
    parseMainInfo(main.childNodes(),page)
    // 标签
    val tagsEle = body.select("div.main3 > div.right").select("div.shisonconttag > a")
    val tagsArr = tagsEle.toArray()
    var tags_ = ""
    for(i <- 0.until(tagsArr.length)){
      val s = tagsArr(i)
      val tagName = s.asInstanceOf[Element].html()
      tags_ += tagName+","
    }
    page.title = title
    if(tags_.length > 0){
      tags_ = tags_.substring(0,tags_.length-1)
    }
    page.tags = tags_
    page
  }


  /**
    * DESC: 解析诗文的主体信息
    */
  private def parseMainInfo(nodes:JavaList[Node],page:Page)={

      val size = nodes.size()
      for(i <- 0 until(size)){
        val node = nodes.get(i)
        val html = node.outerHtml()
        // 整个if 控制语句只会被执行一次
        if(html.contains("作者")){
          if(node.childNodeSize() >= 2 ){
            val author = node.childNode(1)
            /*
            val  p= """^<a.*?>(.*?)</a>""".r
            val r1 = p.findFirstMatchIn(author.outerHtml())
            page.author=r1.get.group(1)
            */
            val cds = Jsoup.parse(author.outerHtml()).body().children()
            if(cds != null && cds.size() > 0){
              page.author=cds.get(0).html()
            }else{
              page.author="EMPTY"
            }
          }else{
            page.author="NULL"
          }

        }else if (html.contains("原文")){
          val content = node.outerHtml()
          // 包含原文的一下的所有的节点的内容都是 诗文
          var j = i
          val sb = new StringBuilder
          // 循环不从 带原文的开始
          for (j <- i+1 until(size)){
              val n_ = nodes.get(j)
              sb.append(n_.outerHtml())
          }
          page.content=sb.toString()
        }else if(html.contains("朝代")){
          val caodai = node.childNode(1).outerHtml()
          page.dynasty=caodai
        }
      }
  }
  // D:\data\BBSSearch\index\金朝-词-爱国\page\page-72105.html
  private def getId(path:String) = {
    val index  = path.lastIndexOf("\\")
    val fileName = path.substring(index+1,path.length)
    fileName.substring(5,fileName.length-5)
  }


}
