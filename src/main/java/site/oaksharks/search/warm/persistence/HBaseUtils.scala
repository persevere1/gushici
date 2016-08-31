package site.oaksharks.search.warm.persistence

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client._
import org.jsoup.Jsoup
import org.testng.annotations.{BeforeTest, Test}

/**
  * Created by Administrator on 2016/8/9.
  */
object HBaseUtils {

  // hbase 操作工具
  private var admin:HBaseAdmin = null
  private var conf:Configuration = null  ;
  private var conn:Connection = null ;
  /**
    * DESC: 创建表
    *
    */
  def createPersonTable() = {
    val ha = getHbaseAdmin
    val tabName = "article"
    if(ha.tableExists(tabName)){
      ha.disableTable(tabName)
      ha.deleteTable(tabName)
    }
    val psn = new HTableDescriptor(TableName.valueOf(tabName))
    val base = new HColumnDescriptor("base")
    val tags = new HColumnDescriptor("tags")
    base.setBlockCacheEnabled(true)
    base.setMaxVersions(1)
    base.setInMemory(true)
    psn.addFamily(base)
    psn.addFamily(tags)
    ha.createTable(psn)
  }


  /**
    * DESC: 获取一个表对象
    *
    * @param tabName
    */
  def getTable(tabName:String)={
    getConn.getTable(TableName.valueOf(tabName))
  }


  def getHbaseAdmin ={
    if(admin != null){
       admin
    }else{
      val admin_ = getConn.getAdmin
      admin=admin_.asInstanceOf[HBaseAdmin]
      admin
    }
  }

  val getConf = {
    if(conf != null){
      conf
    }else{
      val conf_ = new Configuration
      conf_.set("hbase.zookeeper.quorum","node1")
      conf=conf_
      conf
    }
  }

  def getConn ={
    if(conn != null){
      conn
    }else{
      val conn_ =  ConnectionFactory.createConnection(getConf);
      conn=conn_
      conn
    }
  }

  val getStr  = (cell:Cell) => new String(CellUtil.cloneValue(cell),"UTF-8")

  val getCell = (res:Result,family:String , cellName :String) => res.getColumnLatestCell(family.getBytes,cellName.getBytes)

  def main(args: Array[String]) {
    createPersonTable()
  }
}

