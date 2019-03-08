package play.api.db.slick.iam

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.rds.auth.{GetIamAuthTokenRequest, RdsIamAuthTokenGenerator}

object RDSIAM  {

  private val REGION   = "region"
  private val HOSTNAME = "hostname"
  private val PORT     = "port"
  private val USER     = "user"

  def generateAuthToken(path: String, conf: Config): String = {
    generateAuthToken(
      region = conf.getString(s"$path.$REGION"),
      hostName = conf.getString(s"$path.$HOSTNAME"),
      port = conf.getString(s"$path.$PORT"),
      username = conf.getString(s"$path.$USER")
    )
  }

  // TODO: Make this more lightweight
  def generateAuthToken(region: String,
                        hostName: String,
                        port: String,
                        username: String): String = {
    Logger.info("Generating Auth Token")
    val generator = RdsIamAuthTokenGenerator
      .builder()
      .credentials(new DefaultAWSCredentialsProviderChain())
      .region(region)
      .build()

    generator.getAuthToken(
      GetIamAuthTokenRequest
        .builder()
        .hostname(hostName)
        .port(Integer.parseInt(port))
        .userName(username)
        .build())
  }

}
