package play.api.db.slick.iam

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import play.api.{ApplicationLoader, Configuration, Logger}

import scala.util.Try

object RDSIAMLoader {

  val addPasswordUpdaterModule: Configuration => (String, Seq[String]) = configuration => {
    "play.modules.enabled" -> (configuration.get[Seq[String]]("play.modules.enabled") :+ "aws.RDSIAMPasswordUpdaterModule")
  }

  def buildRDSConfig(configuration: play.api.Configuration): Configuration = {
    val stringConf: String => String = getStringConf(configuration)

    val (subProtocol, defaultPort) = stringConf(PROFILE) match {
      case "slick.jdbc.MySQLProfile$"    => ("mysql", 3306)
      case "slick.jdbc.PostgresProfile$" => ("postgresql", 5432)
      case driver                        => throw new IllegalArgumentException(s"$driver is unsupported")
    }

    val port = configuration.getOptional[Int](PORT).getOrElse(defaultPort)

    val url = s"jdbc:$subProtocol://${stringConf(HOSTNAME)}:$port/${stringConf(SCHEMA)}"

    val authToken = RDSIAMAuthTokenGenerator.generate(configuration)

    Configuration(PORT     -> port,
                  URL      -> url,
                  PASSWORD -> authToken,
                  addPasswordUpdaterModule(configuration))
  }

  def hasAWDCredentials: Boolean =
    Try(DefaultAWSCredentialsProviderChain.getInstance().getCredentials).isSuccess

  def hasRDSConfig(configuration: Configuration): Boolean = {
    configuration.has(PROFILE) &&
    configuration.has(USER) &&
    configuration.has(REGION) &&
    configuration.has(HOSTNAME) &&
    configuration.has(SCHEMA)
  }

  val formatLog: String => String = log =>
    s"\n=============================================\n$log\n============================================="

  def setup(context: ApplicationLoader.Context): Configuration = {
    if (hasAWDCredentials && hasRDSConfig(context.initialConfiguration)) {
      Logger.info(formatLog("AWS Credentials and RDS Config Found"))
      buildRDSConfig(context.initialConfiguration)
    } else {
      Logger.error(formatLog("AWS Credentials and / or RDS Config Not Found"))
      Configuration.empty
    }
  }

}
