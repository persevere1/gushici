package test

import java.io.File
import java.util.Scanner

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.{Logger, LoggerFactory}
import org.testng.annotations.Test
import site.oaksharks.search.enter.{HTTP_METHOD, IndexManager, IndexManagerRest}

import scala.actors.Actor
import scala.collection.immutable.HashMap
import scala.collection.JavaConversions._
import scala.util.hashing.Hashing.Default

/**7r
  * Created by Administrator on 20dfjp916/8/16.
  */
class MyUnitTest {


  @Test
  def test1 = {
    val doc = Jsoup.connect("http://so.gushiwen.org/type.aspx?p=1").timeout(5000).get()
    // 总页数 - 10页是一个分页
    val totalPage = 72227/10 + 1
    val resultLinks = doc.select("div.main3 > div.typeleft"); //在h3元素之后的a元素
    val desc = resultLinks.select("div.sonsNone")
    desc.toArray().foreach(e => {
      val ele = e.asInstanceOf[Element]
      // 一共有 72227 这么多 ,
      val id  =ele.attr("id")
      if(id.startsWith("shiwen")){
        val dbid = id.replace("shiwen","")
      }
    })

  }

  def test3 = {
    //val la = new LinkAnaylizer()
    //la.doAnaylize()
    println("线程" + Thread.currentThread().getName)
    // 生成一个actor对象
    // 也可以自定义actor对象 ,  本act 只关注 act0 ,和act1 的消息
    val actService = new AnsweringService("act0","act1")
    actService.start()
    // 给足够的时间启动 service
    Thread.sleep(500)
    // (caller:Actor,name:String ,msg:String)]

    // 启动一个act 发消息给自己
/*
    val act0 = Actor.actor({
      val tuple3 = new Tuple3[Actor,String,String](act0,"actor-0","hello servcie")
      actService.!(tuple3)
    })
    act0.start()
    actService.!(actService,"act0","hello servcie")*/
  }

  /**
    * DESC: 单元测试方法的返回值要为Unit 最好显示声明
    * 否则会有返回值 导致不能测试
    */
  @Test
  def test4() : Unit = {

    val a1 = new Actor {
      override def act(): Unit = {
        while (true){
          receive{
            case (s:String) => println("a1打印:"+s)
          }
        }
      }
    }
    val a2 = new Actor {
      override def act(): Unit = {
        while (true){
            receive{
              case (s:String) => println("a2打印:"+s)
            }
          }
        }
    }
    a1.start()
    a2.start()
    Thread.sleep(1000)
    a2.!("我是a2")
    a1.!("我是a1")
    // a2.!("你好")
    // 接受一个字符串作为结束
    new Scanner(System.in).nextLine()
  }

  @Test
  def test5 :Unit= {
    var map  = new HashMap[String,String]()
    map += ("a" -> "1")
    map += ("b" -> "2")
    map += ("c" -> "3")

    printf("keys = %s , values = %s ,size = %d \n",map.keys,map.values ,map.size)
    map-=("c")
    printf("keys = %s , values = %s ,size = %d \n",map.keys,map.values ,map.size)
  }

  def generalSize(x : Any) = x match{
    case s : String => s.length
    case m : Map[_ , _] => m.size
    case _ => 1
  }

  def getFunciton(t:Int) = {
    if( t ==1){
      val f1 = (i:Int,j:Int) => i+j
      f1
    }else{
      val f2 = (i:Int) => i*2
      f2
    }
  }

  /**
    * DESC: 测试模式匹配
    */
  @Test
  def test6 :Unit = {

    val fun1 = (s:String)=> {
      if(s.contains("age")){ "age" }
      else if(s.contains("name")){ "name"}else{
        "other"
      }
    }
    val s1 ="fdsafdsanamefdsa"

    val s2 = fun1(s1)
    println("s2 = "+ s2 )
    s2 match {
      case "name" => println("名字")
      case "age" => println("年龄")
      case "other" => println("其他")
    }
      // 用一个函数返回匹配的字符串
  }

  @Test
  def test7 :Unit = {

   val f1555 = ( f: (Int ,Int)  => Int ) => {
   }
    f1555(_*_)

  }
  @Test
  def test8 :Unit = {
    // 查找王安石
    val indexUtils = new  IndexManager
    val client = indexUtils.getClient()

    //  set  from he set size就是limit
    val res = client.prepareSearch("gushici").setFrom(10).setSize(1000).execute().actionGet()
    val iterator = res.getHits.iterator()
    while (iterator.hasNext){
      val i = iterator.next()
      i.getSource.entrySet().toList.foreach(f => {
        println(s"key = ${f.getKey} , value = ${f.getValue}")
      })


    }

  }

  @Test
  def test9():Unit = {

    val s = Color.BLANK
    val ss = (t:Color) =>{
      t match {
        case Color.BLANK => {
          print("blank ")
        }
        case _ => {println("unknow ")}
      }
    }
    ss(s)
  }

  @Test
  def test10():Unit = {
    val index = new IndexManagerRest
    index.searchByKey("author","李白",print(_))
  }


}
object MyUnitTest{
  def main(args: Array[String]): Unit = {



  }
}
