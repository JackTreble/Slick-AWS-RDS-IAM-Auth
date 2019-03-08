package play.api.db.slick.iam

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import play.api.inject.guice._
import play.api.{ApplicationLoader, Configuration}

import play.api.Logger
import scala.util.Try

import scala.util.Try

class RDSIAMApplicationLoader extends GuiceApplicationLoader  {

  private val SLICK    = "slick.dbs.default"
  private val SLICKDB  = "slick.dbs.default.db"
  private val PROFILE  = s"$SLICK.profile"
  private val USER     = s"$SLICKDB.user"
  private val REGION   = s"$SLICKDB.region"
  private val HOSTNAME = s"$SLICKDB.hostname"
  private val SCHEMA   = s"$SLICKDB.schema"
  private val PORT     = s"$SLICKDB.port"

  def getStringConf(configuration: Configuration)(path: String): String = {
    configuration.get[String](path)
  }

  def buildRDSConfig(initialConfiguration: play.api.Configuration): Configuration = {
    val stringConf = getStringConf(initialConfiguration)(_)

    val (subProtocol, defaultPort) = stringConf(PROFILE) match {
      case "slick.jdbc.MySQLProfile$"    => ("mysql", "3306")
      case "slick.jdbc.PostgresProfile$" => ("postgresql", "5432")
      case driver                        => throw new IllegalArgumentException(s"$driver is unsupported")
    }

    val port = initialConfiguration.getOptional[String](PORT).getOrElse(defaultPort)

    val url = s"jdbc:$subProtocol://${stringConf(HOSTNAME)}:$port/${stringConf(SCHEMA)}"

    val authToken = RDSIAM.generateAuthToken(SLICKDB, initialConfiguration.underlying)

    Configuration(s"$SLICKDB.url"        -> url) ++
      Configuration(s"$SLICKDB.password" -> authToken) ++
      Configuration(s"$SLICKDB.port"     -> port)
  }

  def hasAWDCredentials: Boolean =
    Try(DefaultAWSCredentialsProviderChain.getInstance().getCredentials).isSuccess

  def hasRDSConfig(configuration: Configuration): Boolean = {
    (for {
      _ <- configuration.getOptional[String](PROFILE)
      _ <- configuration.getOptional[String](USER)
      _ <- configuration.getOptional[String](REGION)
      _ <- configuration.getOptional[String](HOSTNAME)
      _ <- configuration.getOptional[String](SCHEMA)
    } yield true).isDefined
  }

  override def builder(context: ApplicationLoader.Context): GuiceApplicationBuilder = {
    if (hasAWDCredentials && hasRDSConfig(context.initialConfiguration)) {
      Logger.info("AWS Credentials and RDS Config Found")
      val awsRds = buildRDSConfig(context.initialConfiguration)
      Logger.info(context.initialConfiguration.get[String](USER))

      initialBuilder
        .in(context.environment)
        .loadConfig(context.initialConfiguration ++ awsRds)
        .overrides(overrides(context): _*)
    } else {
      Logger.warn("=============================================")
      Logger.warn("AWS Credentials and / or RDS Config Not Found")
      Logger.warn("=============================================")
      initialBuilder
        .in(context.environment)
        .loadConfig(context.initialConfiguration)
        .overrides(overrides(context): _*)
    }
  }
}
