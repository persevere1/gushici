package site.oaksharks.search.common.web

import java.util
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.apache.commons.io.IOUtils
import site.oaksharks.search.enter.IndexManagerRest
import site.oaksharks.search.warm.persistence.Page

import scala.collection.JavaConversions._


/**
  * Created by Administrator on 2016/8/30.
  */

class IndexServlet extends HttpServlet{



  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {



      val opt = req.getParameter("opteration")

      opt match {
        case "_search" => {
          val im = new IndexManagerRest()
          val value =req.getParameter("value")
          val key =req.getParameter("key")
          im.searchByKey(key,value,data => {
            println("响应结果 = " + data)
            val res = data("hits")
            val linkedMap  = res.asInstanceOf[java.util.Map[Object,Object]]
            val total = linkedMap.get("total")
            val hits = linkedMap.get("hits").asInstanceOf[java.util.List[java.util.Map[Object,Object]]].toList

            val dataList = new util.ArrayList[Page]()
            val session = req.getSession
            session.setAttribute("total",total)
            session.setAttribute("dataList",dataList)
            hits.foreach( map =>{
              val pageMap = map.get("_source").asInstanceOf[java.util.Map[Object,Object]]
              var content = ""
              if(map.get("highlight") == null){
                content =  pageMap.get("content").toString
              }else{
                content = map.get("highlight").asInstanceOf[java.util.Map[Object,Object]].get("content").toString
              }

              val webId = pageMap.get("webId").toString
              val author = pageMap.get("author").toString
              val dynasty = pageMap.get("dynasty").toString
              val title = pageMap.get("title").toString
              val tags = pageMap.get("tags").toString
              val p = new Page(author,dynasty,title,webId, content,tags)
              dataList.add(p)
            })
            req.getRequestDispatcher("/list.jsp").forward(req,resp)
          })
        }
      }
  }


  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val is = req.getInputStream
    val content = IOUtils.toString(is)


    print("im serser = " + content)

    resp.getWriter.write("fuck")

  }



}
