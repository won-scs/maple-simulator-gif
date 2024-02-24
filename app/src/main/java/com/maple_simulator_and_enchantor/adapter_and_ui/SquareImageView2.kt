package com.maple_simulator_and_enchantor.adapter_and_ui

import android.content.Context
import android.util.AttributeSet

class SquareImageView2 : androidx.appcompat.widget.AppCompatImageView {
	constructor(context: Context?) : super(context!!) {}
	constructor(context: Context?, attrs: AttributeSet?) : super(
		context!!, attrs
	) {
	}

	constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
		context!!, attrs, defStyle
	) {
	}
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(heightMeasureSpec, heightMeasureSpec)
	}
}