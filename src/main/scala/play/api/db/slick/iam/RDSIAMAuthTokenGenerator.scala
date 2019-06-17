package play.api.db.slick.iam
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.rds.auth.{GetIamAuthTokenRequest, RdsIamAuthTokenGenerator}
import play.api.Configuration

object RDSIAMAuthTokenGenerator {

  def generate(configuration: Configuration): String = {
    getGenerator(configuration)()
  }

  def getGenerator(configuration: Configuration): () => String = {
    val stringConf = getStringConf(configuration)
    val intConf    = getIntConf(configuration)
    generator(buildRdsIamAuthTokenGenerator(stringConf(REGION)),
              buildGetIamAuthTokenRequest(stringConf(HOSTNAME), intConf(PORT), stringConf(USER)))
  }

  private val generator: (RdsIamAuthTokenGenerator, GetIamAuthTokenRequest) => () => String =
    (generator, request) => () => generator.getAuthToken(request)

  private def buildRdsIamAuthTokenGenerator(region: String): RdsIamAuthTokenGenerator =
    RdsIamAuthTokenGenerator
      .builder()
      .credentials(new DefaultAWSCredentialsProviderChain())
      .region(region)
      .build()

  private def buildGetIamAuthTokenRequest(hostName: String,
                                  port: Int,
                                  username: String): GetIamAuthTokenRequest =
    GetIamAuthTokenRequest
      .builder()
      .hostname(hostName)
      .port(port)
      .userName(username)
      .build()

}
