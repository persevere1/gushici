package site.oaksharks.search.enter

import java.net.{URI, URLEncoder}
import java.nio.charset.Charset
import java.util

import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.client.methods.{HttpGet, HttpPost }
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.slf4j.LoggerFactory
import site.oaksharks.search.enter.HTTP_METHOD.HTTP_METHOD
import site.oaksharks.search.warm.CONS
import java.util.{HashMap => JavaHashMap}


import scala.collection.JavaConversions._
/**
  * DESC: Restful 风格api
  *
  *
  * Created by Administrator on 2016/8/28.
  */
class IndexManagerRest {

    val logger = LoggerFactory.getLogger(classOf[IndexManagerRest])


    def searchByKey(key:String ,value:String ,callBack:(Map[Object,Object]) => Unit) = {
      val url = CONS.INDEX_API + "/_search"
      ajax(url,HTTP_METHOD.Post,null,buildBody(key,value),data => {
        // 转换为json
        val mapper = new ObjectMapper
        val json = mapper.readValue(data,classOf[util.Map[Object,Object]])
        callBack(json.toMap[Object,Object])
      })
    }


  /**
    * DESC: 异步ajax
    *
    *
    */
  def ajax(url:String ,method:HTTP_METHOD , data:Map[String,String] , body:String, success: (String) => Unit) = {

    val client = new DefaultHttpClient()
    val req = buildRequest(method,url,data,body)
    // 异步
    val resp = client.execute(req).getEntity
    val data_ = EntityUtils.toString(resp)
      // 回调
    success(data_)

  }

  /**
    * DESC: 创建请求数据内容
    */
  def buildBody(key:String,value:String) = {

    s"""
      |{
      | "from" : 0 ,
      | "size" : 30 ,
      |	"query": {
      |        "bool": {
      |           "should": [
      |             { "match": { "$key":  "$value" } }
      |           ]
      |         }
      |    },
      |	 "highlight" : {
      |        "pre_tags" : ["<font class=highcolor>"],
      |        "post_tags" : ["</font>"],
      |        "fields" : {
      |            "content" : {}
      |        }
      |    }
      |}
    """.stripMargin

  }

  /**
    * 创建请求对象
    *@param body 发送的http 消息
    *
    */
  def buildRequest(method:HTTP_METHOD,url:String,data:Map[String,String],body:String)={
    val params = encodingUrl(data)
    var uri:URI = null
    if( params != None){
      uri = URI.create(url +"?" +params)
    }else{
      uri = URI.create(url)
    }

    logger.info("access url = [{}]" , uri.toURL.toString)
    logger.info("body = [{}]" , body)
    method match {
      case HTTP_METHOD.Get => {
        new HttpGet(uri)
      }
      case HTTP_METHOD.Post => {
        // 请求参数放到head 中
        val req = new HttpPost(uri)
        req.setEntity(new StringEntity(body,Charset.forName("UTF-8")))
        req

      }
      case _ => {
        throw new IllegalArgumentException(s"$method not knows !")
      }
    }
  }

  /**
    * 对URL进行编码
    *
    * @param data  url 请求参数
    */
  def encodingUrl(data:Map[String,String]) = {
    if(data != null){
      val sb = new StringBuilder
      data.map(e => {
        val s = e._2
        val s_ = URLEncoder.encode(s,"UTF-8")
        s"&${e._1}=$s_"
      }).foreach(sb.append(_))
      sb.substring(1)
    }else{
      None
    }
  }

}
object HTTP_METHOD extends Enumeration{

  type  HTTP_METHOD = Value

  val Get = Value("GET")

  val Post = Value("POST")

  val Put = Value("PUT")

  val Delete = Value("DELETE")

}

object IndexManagerRest{


  def main(args: Array[String]): Unit = {
    val im = new IndexManagerRest

    im.searchByKey("content","窗前明月",(data) => {
        println(data)
    })
  }
}
