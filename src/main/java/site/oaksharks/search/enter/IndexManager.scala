package site.oaksharks.search.enter

import java.io.{BufferedReader, FileReader}
import java.net.InetAddress

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.xcontent.{XContent, XContentBuilder}
import org.elasticsearch.index.query.QueryBuilders
import org.slf4j.LoggerFactory
import site.oaksharks.search.warm.CONS
import site.oaksharks.search.warm.persistence.Page


/**
  * DESC:  创建es索引
  *
  * Created by Administrator on 2016/8/23.
  */
class IndexManager {
  //
  private val log = LoggerFactory.getLogger(classOf[IndexManager])

  private var client:TransportClient = null

  def pase2Page(line:String)={
    val arr = line.split("\t")
    if(arr.length != 6){
      log.warn("索引管理: 数据不完整 =[{}]" , line)
      null
    }else{
      //　　贾谊	两汉	鵩鸟赋	 fdsafdsa ,写鸟,对话,议论,人生,哲理	47879
      val page = new Page(arr(5))
      page.author=arr(0)
      page.dynasty=arr(1)
      page.title=arr(2)
      page.content=arr(3)
      page.tags=arr(4)
      page
    }
  }



  /**
    * DESC: 创建一首词的索引
    *
    * @param page
    */
  private def createIndex(page:Page)={

    try{
        getClient().prepareIndex("gushici","gushici",page.webId).setSource(page.toJson).execute().actionGet()
    }catch {
      case ex:Exception =>{
        log.error("索引管理: 写入索引失败", ex)
        log.error("索引管理: 写入索引失败=[{}]",page.toJson)
      }
    }

  }


  def wirite2ES() = {
    // 把数据写入索引
    val fr = new FileReader(CONS.File_ALL)
    val br = new BufferedReader(fr)
    var line:String = br.readLine()
    while (line != null){
      val p = pase2Page(line)
      if( p != null){
        val res = createIndex(p)
        log.info("写入索引结果=[{}]",res.toString)
      }
      line=br.readLine()
    }
  }

  /**
    *
    * DESC: 根据作者查找
    *
    * @param author 作者姓名
    */
  def searchByAuthor(author:String) = {
    val client = getClient()

    // 参数为 索引库的名字
    val searchBuild = client.prepareSearch("gushici")

    val queryBuilds = QueryBuilders.fuzzyQuery("author",author)

    val result = searchBuild.execute().actionGet()

    val total = result.getHits.totalHits()
    println(s"total : $total")

    val iterator = result.getHits.iterator()

    while (iterator.hasNext){
      val i = iterator.next()

      val title = i.getSource.keySet()

      println(s"tilte = $title")
    }


  }


  def getClient() = {

    if( client != null ){
      client
    }else{
      // 单节点es
      /*
      val client = new TransportClient()
      client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("node1"),9300))
      */
      /*
      *需要使用自定义集群名称需要 设置cluster.name , 默认的集群是elastic
      * */
      val settings = Settings.settingsBuilder().put("cluster.name","gushici-es").build()

      val getAddr = (ip:String,port:Int) => {
        new InetSocketTransportAddress(InetAddress.getByName(ip),port)
      }
      val addNode = (client_ : TransportClient) => {
        val getAdd_ = getAddr(_ :String,9300)
        client_.addTransportAddress(getAdd_("192.168.112.100"))
        client_.addTransportAddress(getAdd_("192.168.112.101"))
        client_.addTransportAddress(getAdd_("192.168.112.102"))
      }
      // Client 要通过 静态工程类获取
      val client_ = TransportClient.builder().settings(settings).build()
      addNode(client_)
      client = client_
      client
    }
  }
}

