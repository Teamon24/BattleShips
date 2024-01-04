package org.home.app

import org.home.mvc.view.AppView
import org.home.mvc.view.DebugView

class MainApp: AbstractApp<AppView>(AppView::class)
class BattleOnRunApp: AbstractApp<DebugView>(DebugView::class)