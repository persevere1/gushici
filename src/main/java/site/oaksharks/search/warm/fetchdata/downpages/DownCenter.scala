package site.oaksharks.search.warm.fetchdata.downpages

import java.io.{FileReader, BufferedReader}
import java.net.{URLEncoder, URI, URL}

import org.slf4j.LoggerFactory
import site.oaksharks.search.warm.CONS

import scala.actors.{TIMEOUT, Actor}
import scala.collection.immutable.HashMap

/**
  * DESC: 网页下载调度中心
  * Created by Administrator on 2016/8/19.
  */
class DownCenter(threadNum:Int) extends Actor{

  private val log = LoggerFactory.getLogger(classOf[DownCenter])
  /**
    * DESC: 启动工作线程, 每个工作线程负责一个类别的url
    */
  private def startWorker(br: BufferedReader) = {
    var actorMap = new HashMap[String,Actor]()
    val baseUrl = CONS.GUSHI_BASE
    log.info("下载中心: 启动[{}] 个下载线程",threadNum)
    for ( i <- 0.until(threadNum)){
      val name = "downer-"+i
      val downer = new DownActor(name,this)
      downer.start()
      actorMap += (name -> downer)
      val url = makeDownUrlCommond(getUrlSeq(br))
      downer.!(CONS.OPT_DOWN_URL,url)
      log.info("下载中心: 启动线程[{}] , 并发送任务[{}]",name,url)
    }
    actorMap
  }

  /**
    * DESC: 生成下载指令正文
    * @param line
    * @return
    */
  private def makeDownUrlCommond(line:String) ={
    val str = line.split("-")
    // p=1&c=两汉&x=诗 先秦,游侠,诗
    // #用于分割线程名称和url
    val sb = new StringBuilder(line).append("#")
    sb.append(CONS.GUSHI_BASE)
    val CD = URLEncoder.encode(str(0),"UTF-8")
    val XS = URLEncoder.encode(str(1),"UTF-8")
    val LX = URLEncoder.encode(str(2),"UTF-8")
    sb.append("?c=").append(CD).append("&x=").append(XS).
      append("&t=").append(LX).
      append("&p=")
    sb.toString()
  }

  override def act(): Unit = {
    val br = new BufferedReader(new FileReader(CONS.TYPES))

    val actorMap = startWorker(br)
    /*
    * DESC: 当有工作线程的时候持续发送消息 , 没有工作线程就停止
    * */
    while (actorMap.size > 0){
      receiveWithin(CONS.TIME_OUT){
        case (optType:Int,commond:String ) =>{
          optType match {
            case CONS.OPT_DOWN_URL_OK => {
              sendTask(actorMap ,commond,br)
            }
            case CONS.OPT_DOWN_URL_FAIL =>{
              //
              val index_ = commond.indexOf("#")
              val actorName_ = commond.substring(0,index_)
              val url_ = commond.substring(index_ +1,commond.length)
              log.error("下载中心: [{}] 下载URL=[{}],失败",actorName_,url_)
              sendTask(actorMap ,actorName_,br)
            }
          }
        }
        case TIMEOUT =>{
          actorMap.foreach(f => {
            f._2.!(CONS.OPT_TIMEOUT,"下载中心超时")
          })
        }
        case _ =>{
          log.error("下载中心: 无法匹配的模式")
        }
      }
    }


  }

  private def sendTask(actorMap:HashMap[String,Actor],downerName:String,br:BufferedReader) = {
    val atypes = getUrlSeq(br)
    val downer = actorMap(downerName)
    if(atypes == null ){
      actorMap.foreach(f => {
        f._2.!(CONS.OPT_EXIT,"任务结束")
      })
    }else{
      downer.!(CONS.OPT_DOWN_URL,makeDownUrlCommond(atypes))
    }
  }
  var isFileEnd =false

  private def getUrlSeq(fileReader:BufferedReader):String = {
    if(!isFileEnd){
      if(fileReader != null){
        val res = fileReader.readLine()
        if(res != null){
          return res
        }else{
          isFileEnd =true
          return null
        }
      }
    }
    return null
  }
}
