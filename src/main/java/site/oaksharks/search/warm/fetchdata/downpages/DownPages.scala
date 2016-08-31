package site.oaksharks.search.warm.fetchdata.downpages

import java.io._
import java.net.{HttpURLConnection, URL}
import java.util
import java.util.ArrayList
import java.util.regex.Pattern

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import site.oaksharks.search.warm.CONS

import scala.actors.Actor
import scala.util.matching.Regex


/**
  * Created by Administrator on 2016/8/19.
  */
class DownPages {

  private val log = LoggerFactory.getLogger(classOf[DownPages])
  /**
    * DESC: 索引页面下载页面
    */
  def downIndex(urlModel:String,dirName:String) = {
    //获取类别总条目数
    val totalItems = getTotalPage(urlModel+1)
    //求出总页码数
    val totalPage = totalItems/CONS.PAGE_SIZE + 1

    val dir = CONS.DIR_INDEX
    val fullDir_ = dir+"/"+dirName
    if(totalItems > 0){
      val fullDir = new File(fullDir_)
      if(!fullDir.exists()){
        fullDir.mkdirs()
      }else{
        fullDir.delete()
        fullDir.mkdirs()
      }
      // p=1&c=两汉&x=诗
      for (i <- 1.to(totalPage)){
        val indexUrl = urlModel+i
        val filePath = fullDir_ +"/"+i+".html"
        try{
          downPage(indexUrl,filePath)
        }catch{
          case ex:Exception =>{
            val f_file = new File(filePath)
            if(f_file.exists()){
              f_file.delete()
            }
            log.error("下载器: 下载页面失败 ,url=[{}] , 已经删除下载失败的文件=[{}]",indexUrl,filePath,ex)
          }
        }
      }

    }
    /*
    val totalPage:Int = 10
    val threadCount = 5
    val taskArr = cutTask(threadCount,totalPage)
    taskArr.foreach(tu =>{
      Actor.actor({
        for( i <- tu._1.until(tu._2)){
          val indexUrl = urlModel+i
          val filePath = dir+"/"+i+".html"
          downPage(indexUrl,filePath)
          log.info("下载器: 页面url=[{}]",indexUrl)
        }
      })
    })
    */
  }

  /**
    * DESC:
    *
    * 下载所有诗文详情页面
    */
  def downDetailPage() ={
    val br = new BufferedReader(new FileReader(new File(CONS.IDFile)))
    val dir = CONS.DIR_INDEX
    var line = br.readLine()
    val urlModel = CONS.GUSHI_DETAIL

    while (line != null){
      val i_arr = line.split("\t")
      val id = i_arr(0)
      val foldName = i_arr(1)
      val url = urlModel.replace("#",id)
      val saveDir =  new File(dir+"/"+foldName+"/page") ;
      if(!saveDir.exists()){
        saveDir.mkdirs()
      }
      val path = saveDir+"/page-"+id+".html"
      downPage(url,path)
      log.info("下载器: 下载成功id=[{}]",id)
      line = br.readLine()
    }

  }
  /**
    * DESC: 生成排列组合文件
    */
  private def assemble(cd1:util.ArrayList[String],cd2:util.ArrayList[String],cd3:util.ArrayList[String]) = {
    val path = CONS.TYPES
    val bw = new BufferedWriter(new FileWriter(path))
    for ( i <- 0.until(cd1.size())){
      for ( j <- 0.until(cd2.size())){
        for ( k <- 0.until(cd3.size())){
              val str = cd1.get(i)+"-"+cd2.get(j)+"-"+cd3.get(k)+"\r\n"
              bw.write(str)
        }
      }
    }
    bw.flush()
    bw.close()
  }

  /**
    * DESC: 生成文件
    *
    */
  def generateTypesFile() = {
    val CD = loadList(CONS.TYPE_CD)
    val XS = loadList(CONS.TYPE_XS)
    val LX = loadList(CONS.TYPE_LX)
    assemble(CD,XS,LX)
  }
  /**
    * DESC: 读取文件到List
    *
    * @param path
    */
  private def loadList(path:String)= {
    val list = new ArrayList[String]()
    val file = new File(path)
    val br = new BufferedReader(new FileReader(file))

    var s = br.readLine().trim
    while (s != null){
      // 忽略注释字段
      if(!s.startsWith("#")){
        list.add(s)
      }
      s = br.readLine()
    }
    list
  }

  /**
    * DESC: 切分任务
    *
    * @param threadNum
    * @param totalTask
    * @return
    */
  private def cutTask(threadNum:Int,totalTask:Int)={
    // 每个线程的开始和结束任务
    val res = new Array[Tuple2[Int,Int]](threadNum)

    val perToatal = totalTask/threadNum

    for( i <- 0 until(threadNum)){

      val start = i*perToatal+1
      var end = 0
      if(i==threadNum-1){
        // 最后一个
        end = (i+1)*perToatal+1+totalTask%threadNum
      }else{
        end = (i+1)*perToatal+1
      }
      val p = new Pair(start,end)
      res(i) = p
    }
    res
  }

  private def  getTotalPage(url:String) ={
     val body = Jsoup.connect(url).timeout(CONS.TIME_OUT_CONN).get().body()
     val spans = body.select("div.main3 > div.typeleft").select("div.pages > span")
     var i_res = 0
    spans.toArray().foreach(f =>{
       val span = f.asInstanceOf[Element]
       val s_html = span.html()
       val pattern = Pattern.compile(".*共(\\d+)篇.*")
       val m_res = pattern.matcher(s_html)
       if(m_res.matches()){
         //m_res.find()
         m_res.group(1)
         i_res=Integer.parseInt(m_res.group(1))
       }
     })
     //val total = html.substring(1,html.length-1).trim
     //Integer.parseInt(total)
     i_res
  }

  private def downPage(urlStr:String,path:String) = {
    // 先判断文件是否存在再决定是否建立链接

    val file = new File(path)

    if(!file.exists()){

      val url = new URL(urlStr)
      val httpConn = url.openConnection().asInstanceOf[HttpURLConnection]

      httpConn.setDoInput(true)
      httpConn.setDoOutput(true)

      httpConn.setRequestMethod("GET")

      httpConn.setReadTimeout(CONS.TIME_OUT_CONN)

      httpConn.connect()
      val fos = new FileOutputStream(file)

      if(httpConn.getResponseCode == 200){
        val is = httpConn.getInputStream()
        val buffer_len = 1024
        val buffer = new Array[Byte](buffer_len)
        var read_len = 0
        val i = is.read()
        read_len = is.read(buffer)
        while (read_len > 0){
          // byte写入
          fos.write(buffer,0,read_len)
          read_len = is.read(buffer,0,read_len)
        }
        fos.flush()

        if(is != null){
          is.close()
        }
        if(fos != null){
          fos.close()
        }
        // 断开连接
        httpConn.disconnect()
      }else{
        log.error("下载器: 服务器响应失败,响应码=[{}] , 响应消息=[{}]",
          httpConn.getResponseCode,httpConn.getResponseMessage)
        log.error("下载器:  错误URL = [{}]",urlStr)
        val file = new File(path)
        if(file.exists()){
          file.delete()
        }
      }
    }else{
      log.info("下载器: 文件已经存在,不再下载 页面=[{}]",urlStr)
    }

  }

}

