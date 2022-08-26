package com.lhr.common.ext

import android.content.res.Resources
import android.util.TypedValue

/**
 * @author lhr
 * @date 2021/11/2
 * @des 转换工具
 */

val Number.DipToPx
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics)

val Number.SpToPx
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics)

val Number.PxToDip
    get() = this.toFloat() / Resources.getSystem().displayMetrics.density + 1.0f

val Number.PxToSp
    get() = this.toFloat() / Resources.getSystem().displayMetrics.scaledDensity + 1.0f
