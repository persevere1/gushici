package site.oaksharks.search.warm.fetchdata.downpages

import org.slf4j.LoggerFactory
import site.oaksharks.search.warm.CONS

import scala.actors.{TIMEOUT, Actor}

/**
  * DESC: 下载线程
  * Created by Administrator on 2016/8/19.
  */
class DownActor(name:String,center: DownCenter) extends Actor{
  private val log = LoggerFactory.getLogger(classOf[DownActor])
  override def act(): Unit = {
      var isFinish = false
      val downPage = new DownPages
      while (!isFinish){
        receiveWithin(CONS.TIME_OUT){
          // 下载网页
          case (optType:Int,commond:String)=>{
            optType match {
              case CONS.OPT_DOWN_URL => {
                // 冒号在http:// 中有了
                val index_ = commond.indexOf("#")
                val dirName = commond.substring(0,index_)
                val destUrl = commond.substring(1+index_,commond.length)
                try{
                  downPage.downIndex(destUrl,dirName)
                  center.!(CONS.OPT_DOWN_URL_OK,name)
                }catch {
                  case ex:Exception => {
                     log.error("下载线程 : 下载URL 出错 ",ex)
                      center.!(CONS.OPT_DOWN_URL_FAIL,name+"#"+destUrl)
                  }
                }
              }
              case CONS.OPT_EXIT => {
                isFinish = true
                // 发消息给调度中心 告知下线 ,并告知自己完成的任务量
                log.info("下载线程: [{}] ,收到收工指令",name)
                center !(CONS.OPT_EXIT, name)
                isFinish=true
              }
              case CONS.OPT_TIMEOUT => {
                isFinish = true
                // 发消息给调度中心 告知下线 ,并告知自己完成的任务量
                log.info("下载线程: [{}] ,收到下载中心超时指令",name)
                center !(CONS.OPT_EXIT, name)
              }
            }
          }
          case TIMEOUT => {
            log.error("下载线程 : [{}] 等待超时 ,将要退出 ... ",name)
            center.!(CONS.OPT_EXIT,name)
            isFinish=true
          }
          case _ => {
            log.error("下载线程: [{}] 无法匹配的模式" ,name)
          }
        }
      }
  }
}
