package site.oaksharks.search.warm

/**
  * Created by Administrator on 2016/8/16.
  */
object CONS {

  // 抓取指定id 命令
  val OPT_PAGEID = 1
  // 结束程序命令
  val OPT_EXIT = 100
  // 无法识别操作
  val OPT_UNKNOW = 404
  // 请求超时
  val OPT_TIMEOUT = 101

  val OPT_DOWN_URL = 102

  val OPT_DOWN_URL_OK = 1020
  val OPT_DOWN_URL_FAIL = 1021

  // 客户端退出
  val WORK_EXIT = 105

  val FETCH_OK  = 3
  val FETCH_ERROR  = -3

  val GUSHI_WEBSITE = "http://so.gushiwen.org/type.aspx?p="

  val GUSHI_BASE = "http://so.gushiwen.org/type.aspx"

  val GUSHI_DETAIL = "http://so.gushiwen.org/view_#.aspx"
  val IDFile = "D:\\idfile.txt"

  val File_ALL = "D:\\all.txt"

  // 索引文件
  val DIR_INDEX="D:\\data\\BBSSearch\\index"
  // 诗文页面存储位置

  // 诗文类别

  val TYPE_CD = "D:\\ideaworkplace\\BBSSearch\\src\\main\\resources\\gushi_chaodai.txt"
  val TYPE_LX = "D:\\ideaworkplace\\BBSSearch\\src\\main\\resources\\gushi_leixing.txt"
  val TYPE_XS = "D:\\ideaworkplace\\BBSSearch\\src\\main\\resources\\gushi_xingshi.txt"
  val TYPES = "D:\\ideaworkplace\\BBSSearch\\src\\main\\resources\\gushi_types.csv"
  // 不可改变
  val PAGE_SIZE = 10
  // 总网页数量 不可改变
  val TOTAL_COUNT= 72227
  // 分析诗文id 页面
  val THREAD_PAGEID = 5
  // 线程超时
  val TIME_OUT = 60 * 1000
  // 网页连接超时
  val TIME_OUT_CONN = 15*1000


  val INDEX_NODE = "http://node1:9200"
  // 相当于数据库中的一张表
  val INDEX_API = INDEX_NODE +"/gushici/gushici"


  val INDEX_SETTING =
    """
       {
      	"settings":{
      		"number_of_shards":5,
      		"number_of_replicas":0
      	},
      	"mappings":{
      		"doc":{
      			"dynamic":"strict",
      			"properties":{
      				"webId":{"type":"integer","store":"yes"},
      				"author":{"type":"string","store":"yes","index":"analyzed","analyzer": "ik_max_word","search_analyzer": "ik_max_word"},
      				"dynasty":{"type":"string","store":"yes","index":"analyzed","analyzer": "ik_max_word","search_analyzer": "ik_max_word"},
      				"title":{"type":"string","store":"yes","index":"not_analyzied","analyzer": "ik_max_word","search_analyzer": "ik_max_word"}
           		"content":{"type":"string","store":"yes","index":"analyzied","analyzer": "ik_max_word","search_analyzer": "ik_max_word"}
              "tags":{"type":"string","store":"yes","index":"analyzied","analyzer": "ik_max_word","search_analyzer": "ik_max_word"}
      			}
      		}
      	}
      }

    """.stripMargin
}
