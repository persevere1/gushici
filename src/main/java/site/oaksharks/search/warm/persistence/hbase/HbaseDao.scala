package site.oaksharks.search.warm.persistence.hbase

import java.util

import org.apache.hadoop.hbase.client.{HTable, Put}
import org.elasticsearch.client.transport.TransportClient
import site.oaksharks.search.warm.persistence.{HBaseUtils, Page, PagePersistence}
/**
  * Created by Administrator on 2016/8/15.
  */
class HbaseDao(val actTab:HTable)  extends PagePersistence
{


  override def write(page: Page): Unit = {

        // 设置写入的rows key
        // id-author-title
        val put = new Put(page.toRowkey.getBytes())

        // 使用偏函数
        val myadd = addColumn(_ :String,_ :String,put)
        myadd("base:webId",page.webId)
        myadd("base:author",page.author)
        myadd("base:dynasty",page.dynasty)
        myadd("base:title",page.title)
        myadd("base:content",page.content)

        myadd("base:tags","")
        var res = ""
        page.tags.foreach(f => {
          res += f+ ","
        })
        if(res.length > 0){
          res =res.substring(0,res.length-1)
        }
        myadd("base:tags",res)
        actTab.put(put)

  }

  def  addColumn(columnName:String,columnValue:String,put:Put): Unit = {

    val arr = columnName.split(":")
    val family = arr(0).getBytes()
    val column  = arr(1).getBytes()
    val value = columnValue.getBytes()
    put.addColumn(family,column,value)
  }
}
object HbaseDao{
  def main(args: Array[String]) {
    val  p= """^<a.*?>(.*?)</a>""".r
    val s = """<a href="/author_600.aspx" target="_blank">元好问</a>"""
    val r1 = p.findFirstMatchIn(s)
    val r2 = p.findFirstIn(s)
    println(r1.get.group(1))
  }
}
