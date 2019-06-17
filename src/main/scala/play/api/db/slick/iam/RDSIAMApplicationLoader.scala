package play.api.db.slick.iam

import play.api.ApplicationLoader
import play.api.inject.guice._

class RDSIAMApplicationLoader extends GuiceApplicationLoader {

  override def builder(context: ApplicationLoader.Context): GuiceApplicationBuilder = {
    val rdsIamConfig = RDSIAMLoader.setup(context)

    initialBuilder
      .disableCircularProxies()
      .in(context.environment)
      .loadConfig(context.initialConfiguration ++ rdsIamConfig)
      .overrides(overrides(context): _*)
  }
}
