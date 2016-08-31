package site.oaksharks.search.warm.persistence

/**
  * DESC: 持久化接口,通过实现该接口把数据落地到不同介质
  * Created by Administrator on 2016/8/15.
  */
trait PagePersistence {


  def write(page:Page)

}
