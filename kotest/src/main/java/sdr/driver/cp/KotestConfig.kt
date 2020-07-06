package sdr.driver.cp

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode

object KotestConfig : AbstractProjectConfig() {

    override val isolationMode = IsolationMode.InstancePerLeaf
}

