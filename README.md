# Play Slick AWS RDS IAM Auth

The Play Slick AWS RDS IAM Auth Module is allows for [IAM Database Authentication for MySQL and PostgreSQL](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/UsingWithRDS.IAMDBAuth.html)


## How to use

You can use this project in two ways:
 - Set the application loader in your configuration file
 - Add RDSIAMLoader to existing application loader

#### application.conf

```hocon
play {
  application {

    ## Application Loader
    # https://www.playframework.com/documentation/latest/ScalaDependencyInjection
    # ~~~~~
    loader = "play.api.db.slick.iam.RDSIAMApplicationLoader"
  }
}
```


#### ApplicationLoader.scala
Example of adding RDSIAM to existing Application Loader
```scala
import play.api.ApplicationLoader
import play.api.inject.guice._

class RDSIAMApplicationLoader extends GuiceApplicationLoader {
  override def builder(context: ApplicationLoader.Context): GuiceApplicationBuilder = {
    val rdsIamConfig = play.api.db.slick.iam.RDSIAMLoader.setup(context)
    
    initialBuilder
      .disableCircularProxies()
      .in(context.environment)
      .loadConfig(context.initialConfiguration ++ rdsIamConfig)
      .overrides(overrides(context): _*)
  }
}
```


## Config

```hocon
slick.dbs.default {
  profile = "" # `slick.jdbc.MySQLProfile$` or `slick.jdbc.PostgresProfile$`
  db {
    user = ""
    region = ""
    hostname = ""
    port = "" #Optional Defaults, mysql -> 3306, postgresql -> 5432
    schema = ""
  }
}
```