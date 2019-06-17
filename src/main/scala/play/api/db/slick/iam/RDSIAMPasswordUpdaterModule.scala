package play.api.db.slick.iam

import play.api.inject.{SimpleModule, bind}
class RDSIAMPasswordUpdaterModule extends SimpleModule(bind[RDSIAMPasswordUpdater].toSelf.eagerly())
