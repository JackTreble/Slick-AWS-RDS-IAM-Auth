package play.api.db.slick.iam

import java.util.Properties

import akka.actor.ActorSystem
import com.google.inject.Inject
import com.zaxxer.hikari.pool.HikariPool
import com.zaxxer.hikari.util.DriverDataSource
import javax.inject.Singleton
import org.apache.commons.lang3.reflect.FieldUtils
import play.api.db.slick.DatabaseConfigProvider
import play.api.{Configuration, Environment, Logger}
import slick.jdbc.JdbcProfile
import slick.jdbc.hikaricp.HikariCPJdbcDataSource

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class RDSIAMPasswordUpdater @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider,
    environment: Environment,
    configuration: Configuration,
    actorSystem: ActorSystem)(implicit executionContext: ExecutionContext) {
  private val forceAccess = true
  private val databaseDef = dbConfigProvider.get[JdbcProfile].db
  private val hikariDataSource =
    FieldUtils.readField(databaseDef, "source", forceAccess).asInstanceOf[HikariCPJdbcDataSource]
  private val hikariPool =
    FieldUtils.readField(hikariDataSource.ds, "fastPathPool", forceAccess).asInstanceOf[HikariPool]
  private val driverDataSource =
    FieldUtils.readField(hikariPool, "dataSource", forceAccess).asInstanceOf[DriverDataSource]
  private val driverProperties =
    FieldUtils.readField(driverDataSource, "driverProperties", forceAccess).asInstanceOf[Properties]
  private val authTokenGenerator = RDSIAMAuthTokenGenerator.getGenerator(configuration)

  actorSystem.scheduler.schedule(initialDelay = 0.seconds, interval = 14.minutes) {
    val rdsToken = authTokenGenerator()
    hikariDataSource.ds.setPassword(rdsToken)
    driverProperties.put("password", rdsToken)
    Logger.info("Password Updated")
  }

}
