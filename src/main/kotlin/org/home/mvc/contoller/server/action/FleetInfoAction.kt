package org.home.mvc.contoller.server.action

import org.home.mvc.model.BattleModel

class FleetSettingsAction(
    val height: Int,
    val width: Int,
    val shipsTypes: Map<Int, Int>): Action()
{
    constructor(model: BattleModel) : this(
        model.height.value,
        model.width.value,
        model.battleShipsTypes.value.toMutableMap()
    )
}

class FleetsReadinessAction(val states: Map<String, Map<Int, Int>>): Action()