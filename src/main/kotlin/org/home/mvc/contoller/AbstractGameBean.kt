package org.home.mvc.contoller

import org.home.app.di.GameScope
import org.home.mvc.model.BattleModel
import tornadofx.Controller

abstract class AbstractGameBean : Controller() {

    protected val model: BattleModel by GameScope.inject()

    protected val applicationProperties = model.applicationProperties
    protected open val currentPlayer = model.currentPlayer
}