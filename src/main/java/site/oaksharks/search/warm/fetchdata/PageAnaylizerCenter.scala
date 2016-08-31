package site.oaksharks.search.warm.fetchdata

import java.io._

import org.apache.hadoop.hbase.client.HTable
import org.slf4j.LoggerFactory
import site.oaksharks.search.warm.CONS
import site.oaksharks.search.warm.persistence.{PagePersistence, HBaseUtils, Page}
import site.oaksharks.search.warm.persistence.hbase.HbaseDao

import scala.actors.{Actor, TIMEOUT}
import scala.collection.immutable.HashMap

/**
  * Created by Administrator on 2016/8/16.
  */
class PageAnaylizerCenter(val threadNum:Int) extends Actor{

  val log = LoggerFactory.getLogger(classOf[PageAnaylizerCenter])
  var isFileEnd = false

  override def act(): Unit = {
    var actorMap = new HashMap[String,PageFetcher]()
    log.info("调度器: 启动[{}] 个actor ", threadNum)
    log.info("调度器: 打开id文件 [{}] ", CONS.IDFile)
    val fr = new FileReader(new File(CONS.IDFile))
    val br = new BufferedReader(fr) ;
    val fw = new FileWriter(CONS.File_ALL)
    val bw = new BufferedWriter(fw)

    // 启动若干个线程
    for( i <- 0 until(threadNum)){
      val actName = "work-"+i
      val work = new PageFetcher(this,actName)
      actorMap += (actName -> work)
      // 进入待命状态
      work.start()
      sendNewTask(work,actorMap,br)

      log.info("调度器: 启动[{}]]",actName)
        // 发送任务
    }

    /**
      *  DESC: 检测每个工作线程的运行情况 ,
      *  如果所有的线程都已经运行完毕 ,输出每个线程的运行信息
      *  从此actor中打开文件 , 根据接受到的消息 ,发送任务
      */
    while (actorMap.size > 0){
      receiveWithin(CONS.TIME_OUT){
        case (optType:Int,actName:String,page:Page) => {
            optType match {
              // 某个线程执行完毕之后分发新任务,并把执行完毕的结果落地
              case CONS.FETCH_OK => {
                log.info("调度器: 完成任务 来自于小伙伴[{}] , 完成任务序号=[{}]",actName, page.webId)
                log.info("调度器: 写入文件成功 pageId=[{}]",page.webId)
                // 小伙伴辛苦了 还要执行新任务 , 只有收到 来自工作线程的退出消息才会被移除,
                // 即使出现服务端分发完毕客户端尚未完成,此处worker也不会为null
                // 任务为空可以告诉各个小伙伴下班了
                bw.write(page.serialize+"\r\n")
                val worker = actorMap(actName)
                sendNewTask(worker,actorMap,br)
              }
            }
        }
        case (optType:Int ,optName:String,commond:String) => {
          // 协调消息通道
          optType match {
              case CONS.FETCH_ERROR => {
                // 小弟办事不理
                log.warn("调度器: 线程[{}] , 办事不利 分析失败. 失败网页=[{}]",optName,commond)
              }
              // 工作线程成功退出
              case CONS.OPT_EXIT => {
                // 放弃对线程的引用
                actorMap -= (optName)
                log.info("调度器: 线程[{}] 完成任务 , 请求退出,已经处理. 分析网页成功和失败的数量=[{}]",optName,commond)
              }
              case CONS.OPT_TIMEOUT => {
                log.warn("调度器: 工作线程[{}]超时,不再对其发送任务.",optName)
                // 工作线程超时
                actorMap -= (optName)
              }
          }
        }
        case TIMEOUT => {
            // 超时之后线程或许还在运行 , 分别给他们发送退出消息
          shutdown(actorMap)
          log.error("调度器: 调度器超时,通知工作线程下线")
        }
        case _ =>{
          log.warn("调度器:  无法识别的模式 ." )
        }

      }
    }

    // 所有任务执行完毕最后清理资源 , 务必注意关闭的顺序 , 先打开的后关闭
    val ios:List[Closeable] = List( br ,fr, bw,fw)
    ios.foreach(f => {
      if( f !=null ){
        f.close()
      }
    })

  }

/**
  * DESC: pageid 序列,每次返回顶部元素
  *     这个方法只会被当前线程调用 不会产生并发
  */
   def  getPageidSeq(fileReader: BufferedReader):String = {
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

  /**
    * DESC: 给工作线程发送新任务
    */
  private def sendNewTask(worker:PageFetcher,actorMap :HashMap[String,Actor],br: BufferedReader):Unit = {
    val pageInfo = getPageidSeq(br)
    if(pageInfo == null){
      // 任务完成通知所有线程下线
      shutdown(actorMap)
    }else{
        val arr = pageInfo.split("\t")
        val s = CONS.DIR_INDEX+"\\"+arr(1)+"\\page\\page-"+arr(0)+".html"
        val artFile = new File(s)
        if(!artFile.exists()){
          // 递归方法需要手动指明返回值类型
          sendNewTask(worker,actorMap,br)
          log.warn("调度器: 文件不存在 line=[{}] , path=[{}]",pageInfo,s)
        }else{
          worker.!(CONS.OPT_PAGEID,s)
        }
    }
  }

  /**
    * DESC: 通知所有线程下线
    *
    * @param actorMap
    */
  private def shutdown(actorMap :HashMap[String,Actor]) = {
    actorMap.foreach((entry:(String,Actor)) => {
      log.error("调度器: 任务分发完成,通知工作线程[{}]下线",entry._1)
      entry._2.!(CONS.OPT_EXIT,"已经没有任务")
    })
  }
}
