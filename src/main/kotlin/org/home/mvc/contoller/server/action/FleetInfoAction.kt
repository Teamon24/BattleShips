package org.home.mvc.contoller.server.action

import org.home.mvc.model.BattleModel

class FleetSettingsAction(
    val height: Int,
    val width: Int,
    val shipsTypes: Map<Int, Int>): Action()
{
    constructor(modelView: BattleModel) : this(
        modelView.height.value,
        modelView.width.value,
        // ObserversMap не сериализуется, поэтому - toMutableMap()
        modelView.shipsTypes.value.toMutableMap()
    )
}

class FleetsReadinessAction(val states: Map<String, Map<Int, Int>>): Action()