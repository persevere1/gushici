package site.oaksharks.search.warm.fetchdata

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import site.oaksharks.search.warm.CONS

import scala.actors.{TIMEOUT, Actor}

/**
  * DESC: 页面解析器
  *
  * @param dispatcher 总调度器
  */
class PageFetcher(val dispatcher: PageAnaylizerCenter, val name:String) extends Actor {

  val log = LoggerFactory.getLogger(classOf[PageFetcher])

/**
  *
  * DESC: 提供一个服务给n个线程读取id
  *
  * */
  override def act(): Unit = {
    val pageAnaylizer = new PageAnaylizer()
    var isFinshed = false
    var total = 0
    var errTotal = 0
    while (!isFinshed) {
      val s = receiveWithin(CONS.TIME_OUT) {
        case (optType: Int, commond: String) => {
          optType match {
            // 收到退出信号
            case CONS.OPT_PAGEID => {
              // 需要把生成的对象发送到 调度线程中 , 调度线程负责持久化
                val p = pageAnaylizer.fetcher(commond)
                var res = "成功"
                if (p != null) {
                  dispatcher !(CONS.FETCH_OK, name, p)
                  total = total + 1
                } else {
                  dispatcher !(CONS.FETCH_ERROR, name,commond)
                  errTotal = errTotal + 1
                  res = "失败"
                }
            }
                //log.info("工作线程:[{}] -分析网页完成 ,网页id =[{}],分析结果为[{}]", name, commond, res)
            case CONS.OPT_EXIT => {
                  isFinshed = true
                  val subInfo = String.valueOf(total) + ":" + String.valueOf(errTotal)
                  // 发消息给调度中心 告知下线 ,并告知自己完成的任务量
                  log.info("工作线程: [{}] ,收到收工指令,完成任务成功和失败的数量,[{}]",name,subInfo)
                  dispatcher !(CONS.OPT_EXIT, name, subInfo)
            }
            case CONS.OPT_TIMEOUT => {
              isFinshed = true
              val subInfo = String.valueOf(total) + ":" + String.valueOf(errTotal)
              // 发消息给调度中心 告知下线 ,并告知自己完成的任务量
              log.info("工作线程: [{}] ,收到调度器超时指令,完成任务成功和失败的数量,[{}]",name,subInfo)
              // 发送确认信息
              dispatcher !(CONS.OPT_EXIT, name, subInfo)
            }
          }
       }
       case TIMEOUT => {
         val subInfo = String.valueOf(total) + ":" + String.valueOf(errTotal)
         log.error("工作线程: [{}] 等待超时,完成任务成功和失败的数量为=[{}] ,将要退出...", name,subInfo)
         dispatcher !(CONS.OPT_TIMEOUT, name, String.valueOf(total) + ":" + String.valueOf(errTotal))
         isFinshed=true
       }
       // else 的情况
       case _ => {
            log.error("工作线程: [{}] 无法匹配的模式", name)
          }
       }
      }
    }
}
