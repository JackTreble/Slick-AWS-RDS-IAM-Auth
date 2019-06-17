package play.api.db.slick
import play.api.Configuration

package object iam {

  val SLICK    = "slick.dbs.default"
  val PROFILE  = s"$SLICK.profile"
  val SLICK_DB = s"$SLICK.db"
  val USER     = s"$SLICK_DB.user"
  val REGION   = s"$SLICK_DB.region"
  val HOSTNAME = s"$SLICK_DB.hostname"
  val SCHEMA   = s"$SLICK_DB.schema"
  val PORT     = s"$SLICK_DB.port"
  val URL      = s"$SLICK_DB.url"
  val PASSWORD = s"$SLICK_DB.password"

  def getStringConf: Configuration => String => String =
    configuration => path => configuration.get[String](path)

  def getIntConf: Configuration => String => Int =
    configuration => path => configuration.get[Int](path)
}
