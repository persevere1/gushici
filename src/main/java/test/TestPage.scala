package test

import java.util.regex.{Matcher, Pattern}

import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper
import site.oaksharks.search.warm.fetchdata.PageAnaylizer

import scala.actors.Actor
import scala.util.parsing.json.JSON

/**
  * Created by Administrator on 2016/8/15.
  */
class TestPage(val name:String,val age:Int) {


  override def toString = s"TestPage(name=$name\tage=$age)"
}
object TestPage{


  def main(args: Array[String]) {
   /* val t = new TestPage("wu",100)
    val mapper = new ObjectMapper()
    val s = mapper.writeValueAsString(t)
    println(s)*/
    val s =
      """
        |fda
        |fdsa
        |f
        |ds
        |afdsa
      """.stripMargin

    print(s)
  }
}