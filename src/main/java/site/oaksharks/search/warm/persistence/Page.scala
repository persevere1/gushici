package site.oaksharks.search.warm.persistence

import scala.beans.BeanProperty

/**
  * Created by Administrator on 2016/8/15.
  */
class Page(@BeanProperty val webId:String) extends java.io.Serializable{

  @BeanProperty
  var author:String = null
  @BeanProperty
  var dynasty:String = null
  @BeanProperty
  var title:  String= null
  @BeanProperty
  var content:String= null
  @BeanProperty
  var tags:String= null

  def this(author:String, dynasty:String,title:  String, webId:String,content:String,tags:String) = {
    // 需要调用类的构造方法
    this(webId)
    this.author = author
    this.dynasty = dynasty
    this.title = title
    this.content = content
    this.tags = tags
  }


  def toRowkey = webId+"-"+author+"-"+title

  override def toString = s"Page($author, $dynasty, $title, $content, $tags, $webId, $toRowkey)"

  def serialize = s"$author\t$dynasty\t$title\t$content\t$tags\t$webId"


  def toJson ={
    this.content = content.replaceAll("\"","\\\\\"")
    s"""
         {
            "webId": "$webId",
            "author": "$author",
            "dynasty": "$dynasty",
            "title": "$title",
            "content": "$content",
            "tags":"$tags"
         }
     """.stripMargin
  }

}

