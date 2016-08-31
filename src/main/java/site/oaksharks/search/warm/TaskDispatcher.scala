package site.oaksharks.search.warm

import java.awt.print.PageFormat
import java.io.{BufferedReader, File, FileReader}

import site.oaksharks.search.enter.IndexManager
import site.oaksharks.search.warm.fetchdata.{PageAnaylizerCenter, PageIdAnaylizer}
import site.oaksharks.search.warm.fetchdata.downpages.{DownPages, DownCenter}

import scala.actors.Actor

/**
  * DESC: 线程调度和持久化
  * Created by Administrator on 2016/8/16.
  */
object TaskDispatcher {

  def main(args: Array[String]): Unit = {
    printf("Uage: " +
      "Follow Commond  is aiavalbe:" +
      " \n '1' => 生成类别文件 " +
      " \n '2' => 下载索引页面 " +
      " \n '3' => 分析页面id" +
      " \n '4' => 下载详情页面" +
      " \n '5' => 分析详情并写入结果到文件" +
      " \n '6' => 写入数据到es" +
      " \n '7' => 帮助 \n")

    val commond = Console.readInt()
    commond match {
      case 1 => {
        new DownPages().generateTypesFile
      }
      case 2 =>{
        new DownCenter(10).start()
      }
      case 3 =>{
        val pageIdAnay = new PageIdAnaylizer()
        pageIdAnay.doAnaylize()
      }
      case 4 =>{
        new DownPages().downDetailPage()

      }
      case 5 =>{
        new PageAnaylizerCenter(5).start()
      }
      case 6 =>{
        new IndexManager().wirite2ES()

      }
      case 7 =>{
        println("帮助: 先生成古诗文的所有类别, " +
          "根据类别下载索引文件,然后分析索引文件中的id," +
          "根据id下载古诗文页面,最后分析出所有的古诗文持久化到DB")
      }
      case _ => {
        println("输入错误,请重来!")
      }
      println("################################")

    }
  }
}
