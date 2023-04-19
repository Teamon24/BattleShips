package org.home.mvc.view.components

import tornadofx.ViewTransition
import tornadofx.seconds

fun slide(seconds: Double) = ViewTransition.Slide(seconds.seconds)
fun ViewTransition.Slide.right() = ViewTransition.Slide(this.duration, ViewTransition.Direction.RIGHT)