package site.oaksharks.search.warm.fetchdata

import java.io.{File, FileWriter}
import java.util
import java.util.{ArrayList => JavaList}

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import site.oaksharks.search.warm.CONS

/**
  * DESC: 分析论坛的所有帖子连接
  *
  * param website 论坛的url
  * param pageTotal 取该论坛页面的数量 -1 表示抓取论坛所有数据
  */
class PageIdAnaylizer{

  val log = LoggerFactory.getLogger(classOf[PageIdAnaylizer])

  /**
    * DESC: 分析网页中包含的连接 , 并持久化到文件中
    *
    */
  def doAnaylize() = {

    val url = CONS.GUSHI_WEBSITE
    val idFile = CONS.IDFile
    val pageSize = CONS.PAGE_SIZE
    log.info("链接分析: 目标文件地址=[{}]",idFile)
    val file = new File(idFile)
    if(file.exists()){
      file.delete()
      log.info("链接分析: 目标文件地址存在,已经删除")
    }
    val fw = new FileWriter(file,false)
    // 索引文件
    val fileIndexFolder = new File(CONS.DIR_INDEX)
    val all_index_folder = fileIndexFolder.listFiles()
    // 多线程要考虑并发写
    for (i <- 0.until(all_index_folder.size)){
        log.info("链接分析: 序号=[{}]",i)
        val ids=  anaylizeFolder(all_index_folder(i))
        fw.write(ids)
    }
    fw.flush()
    fw.close()
  }

  /**
    * DESC: 分析索引文件中包含的诗词
    *
    * @param pageFile 诗词详情文件路径
    * @return
    */
  def anaylizePage(pageFile:File) = {
    val idList = new JavaList[String]()
    val doc = Jsoup.parse(pageFile,"UTF-8")
    // 总页数 - 10页是一个分页
    val resultLinks = doc.select("div.main3 > div.typeleft"); //在h3元素之后的a元素
    val desc = resultLinks.select("div.sonsNone")
    desc.toArray().foreach(e => {
      val ele = e.asInstanceOf[Element]
      // 一共有 72227 这么多 ,
      val id  =ele.attr("id")
      if(id.startsWith("shiwen")){
        val dbid = id.replace("shiwen","")
        idList.add(dbid)
      }
    })
    idList
  }

  /**
    * DESC:分析文件夹中所有文件包含的页面
    *
    * @param folder
    */
  def anaylizeFolder(folder:File) = {
    val flodList = new JavaList[String]()
    folder.listFiles().foreach(f => {
      if(f.isFile){
        val l = anaylizePage(f)
        flodList.addAll(l)
      }
    })
    listToStr(flodList,folder.getName)
  }
  /**
    *
    * @param list
    */
  private def listToStr(list:JavaList[String],folderName:String) = {
      val s = new StringBuilder()
      list.toArray().foreach(e => {
        s.append(e).append("\t").append(folderName).append("\r\n")
      })
      s.toString()
  }



}
