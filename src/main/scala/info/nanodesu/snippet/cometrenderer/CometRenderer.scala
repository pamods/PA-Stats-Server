package info.nanodesu.snippet.cometrenderer

import net.liftweb.util.CssSel

trait CometRenderer {
	def render: CssSel
}