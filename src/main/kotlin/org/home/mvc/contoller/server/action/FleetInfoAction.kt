package org.home.mvc.contoller.server.action

import org.home.mvc.model.BattleViewModel

class FleetSettingsAction(
    val height: Int,
    val width: Int,
    val shipsTypes: Map<Int, Int>): Action()
{
    constructor(modelView: BattleViewModel) : this(
        modelView.getHeight().value,
        modelView.getWidth().value,
        // ObserversMap не сериализуется, поэтому - toMutableMap()
        modelView.getShipsTypes().toMutableMap()
    )
}

class FleetsReadinessAction(val states: Map<String, Map<Int, Int>>): Action()