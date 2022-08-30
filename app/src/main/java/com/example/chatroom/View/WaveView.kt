package com.example.chatroom.View

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class WaveView : View {
    constructor(context: Context?) : super(context)
    constructor(context: Context?,  attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private var waveType //波形展示类型
            : Int? = null
    private var centerLineColor: Int = Color.BLACK
    private var centerLineWidth = 1
    private var lineColor: Int = Color.GREEN
    private var lineWidth = 10 //竖线的宽度
    private var lineSpace = 30 //竖线之间的间隔宽度
    private var values //存放数值
            : MutableList<Int>? = null
    private val fullValue = 100 //相对最大值
    private var mScale = 0f //传入值转换为有效数值需要使用的比例
    private var maxValue = 100000 //当前数组中的最大值 该值乘以scale应等于fullValue
    private var maxLineCount = 0
    private var hasOver //值记录是否已完毕
            = false
    private var paintCenterLine: Paint? = null
    private var paintLine: Paint? = null
    private fun init() {

        waveType = 0
        centerLineColor = Color.BLUE
        centerLineWidth =1
        lineColor =  Color.GREEN
        lineWidth = 5
        lineSpace = 5
        paintCenterLine = Paint()
        paintCenterLine!!.strokeWidth = centerLineWidth.toFloat()
        paintCenterLine!!.color = centerLineColor
        paintLine = Paint()
        paintLine!!.strokeWidth = lineWidth.toFloat()
        paintLine!!.isAntiAlias = true
        paintLine!!.color = lineColor
        maxValue=300000
    }

    fun putValue(value: Int) {
        if (value > maxValue) {
            maxValue = value
            mScale = fullValue.toFloat() / maxValue
        }
        if (values == null) {
            values = ArrayList()
        }
        values!!.add(value)
        invalidate()
    }

    fun clearValue(){
        values?.clear()
        invalidate()
    }

    private var lastX = 0
    private var moveX = 0
    private var hasBeenEnd = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> lastX = event.rawX.toInt()
            MotionEvent.ACTION_MOVE -> {
                val x = event.rawX.toInt()
                //到达边缘时不能向该方向继续移动
                if (!hasBeenEnd || moveX > 0 && lastX - x < 0 || moveX < 0 && lastX - x > 0) {
                    moveX += ((lastX - x) * 0.7).toInt()
                    lastX = x
                    invalidate()
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        val yCenter = height / 2
        if (maxLineCount == 0) {
            maxLineCount = width / (lineSpace + lineWidth)
        }
        if (waveType == WVTYPE_CENTER_LINE) {
            /***************画中线 */
            paintCenterLine?.let {
                canvas.drawLine(0F, yCenter.toFloat(),
                    width.toFloat(), yCenter.toFloat(),
                    it
                )
            }
        }
        /***************画竖线 */
        //判断当前数组中的数据是否超出了可画竖线最大条数
        if (values != null) {
            /**找出当前第一条竖线以及偏移量 */
            var startIndex = 0 //第一条线
            var startOffset = 0 //第一条线的偏移
            if (!hasOver || moveX == 0) { //仍在记录中或未手动滑动过
                //线条数量超出最大数 只画后面的线
                if (values!!.size > maxLineCount) {
                    startIndex = values!!.size - maxLineCount
                }
            } else { //已结束录值 且x轴有过移动
                //先得到第一条线原本应该的位置
                if (values!!.size > maxLineCount) {
                    startIndex = values!!.size - maxLineCount
                }
                //计算移动线条数
                val moveLineSize = moveX / (lineWidth + lineSpace)
                startOffset = moveX % (lineWidth + lineSpace)
                val currentIndex = startIndex + moveLineSize
                if (currentIndex < 0) { //到达最左边
                    startIndex = 0
                    startOffset = 0
                    hasBeenEnd = true
                } else if (currentIndex >= values!!.size) {
                    startIndex = values!!.size - 1
                    startOffset = 0
                    hasBeenEnd = true
                } else {
                    startIndex = currentIndex
                    hasBeenEnd = false
                }
            }
            //画竖线
            for (i in startIndex until values!!.size) {
                var startX = 0
                var endX = 0
                var startY = 0
                var endY = 0
                val lineHeight = (values!![i].toFloat()  /maxValue  * height).toInt()
                when (waveType) {
                    WVTYPE_CENTER_LINE -> {
                        startX =
                            (i - startIndex) * (lineSpace + lineWidth) + lineWidth / 2 - startOffset
                        endX = startX
                        startY = (height - lineHeight) / 2
                        endY = (height - lineHeight) / 2 + lineHeight
                    }
                    WVTYPE_SINGLE -> {
                        startX =
                            (i - startIndex) * (lineSpace + lineWidth) + lineWidth / 2 - startOffset
                        endX = startX
                        startY = height - lineHeight
                        endY = height
                    }
                }
                paintLine?.let {
                    canvas.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(),
                        endY.toFloat(), it
                    )
                }
            }
        }
    }

    companion object {
        const val WVTYPE_CENTER_LINE = 0 //竖线从中间开始 向上向下长度相同
        const val WVTYPE_SINGLE = 1 //竖线从底部开始向上计算
    }
}