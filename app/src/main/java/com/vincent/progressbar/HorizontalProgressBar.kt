package com.vincent.progressbar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.RequiresApi
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class HorizontalProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    private val bgPaint: Paint by lazy {
        Paint().apply {
            color = bgColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }

    private val fontPaint: Paint by lazy {
        Paint().apply {
            color = Color.BLUE
            isAntiAlias = true
            style = Paint.Style.FILL
            shader = linearGradient
        }
    }

    private var progressLineColor = Color.parseColor("#33FFFFFF")

    private val pathPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
        color = progressLineColor
    }

    // 是否绘制条纹
    private var isStripe = true

    var progressBarHeight = 30f
    var progressBarWidth = 500f
    var drawableWidth = 80 * 1.5f
    var drawableHeight = 70 * 1.5f

    // 条纹宽度
    var pathWidth = 15
        set(value) {
            field = value
            invalidate()
        }

    // 条纹间隔宽度
    var pathSpace = 45
        set(value) {
            field = value
            invalidate()
        }

    // 是否以指示器中心为标准
    private var isPointCenter = true

    var onChangeProgress: ((progress: Int) -> Unit)? = null
    var onProgressFinish: (() -> Unit)? = null

    private var drawable: Drawable? = null

    //帧动画图片
    private var drawableIds: IntArray? = null
    private var drawableList = arrayListOf<Drawable>()
    private var frameIndex = 0
    private var isAnimFrame = false

    // 当前进度
    private var currentProgress = 0

    // 指示器图片
    private var barDrawable = -1

    // 动画时间
    private var animDuration = 120 * 1000L

    private val bgRectF = RectF()

    private val fontRectF: RectF by lazy {
        RectF()
    }

    private val barRectF: RectF by lazy {
        RectF()
    }

    private val radius: Float by lazy {
        bgRectF.height() / 2
    }

    var valueAnimator: ValueAnimator? = null

    fun setAnimDuration(duration: Long) {
        animDuration = duration
        valueAnimator?.duration = animDuration
    }

    fun startAnimation() {
        valueAnimator?.start()
    }

    fun resumeAnimation() {
        valueAnimator?.resume()
    }

    fun pauseAnimation() {
        valueAnimator?.pause()
    }

    fun cancelAnimation() {
        valueAnimator?.cancel()
    }

    private var startGradientColor = Color.parseColor("#2855FF")
    private var endGradientColor = Color.parseColor("#772EFF")
    private var bgColor = Color.LTGRAY

    //颜色渐变器
    private val linearGradient: LinearGradient by lazy {
        LinearGradient(
            bgRectF.left,
            bgRectF.top,
            bgRectF.right,
            bgRectF.bottom,
            startGradientColor,
            endGradientColor,
            Shader.TileMode.CLAMP
        )
    }

    init {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.HorizontalProgressBar,
            defStyleAttr,
            0
        )
        bgColor = typedArray.getColor(R.styleable.HorizontalProgressBar_bg_color, Color.LTGRAY)
        startGradientColor = typedArray.getColor(
            R.styleable.HorizontalProgressBar_start_color,
            Color.parseColor("#2855FF")
        )
        endGradientColor = typedArray.getColor(
            R.styleable.HorizontalProgressBar_end_color,
            Color.parseColor("#772EFF")
        )
        animDuration =
            typedArray.getInteger(R.styleable.HorizontalProgressBar_anim_duration, 3000).toLong()
        isPointCenter = typedArray.getBoolean(
            R.styleable.HorizontalProgressBar_is_point_center,
            true
        )
        barDrawable = typedArray.getResourceId(
            R.styleable.HorizontalProgressBar_bar_drawable,
            -1//R.mipmap.persion_icon
        )
        currentProgress = typedArray.getInteger(
            R.styleable.HorizontalProgressBar_current_progress,
            0
        )
        isAnimFrame = typedArray.getBoolean(R.styleable.HorizontalProgressBar_is_anim_frame, true)

        if (barDrawable != -1) {
            drawable = resources.getDrawable(barDrawable, null)
        }

        if (isAnimFrame) {
            valueAnimator = ValueAnimator.ofInt(currentProgress, 100).apply {
                addUpdateListener {
                    val progress = it.animatedValue.toString().toInt()
                    currentProgress = progress
                    invalidate()
                    onChangeProgress?.invoke(progress)
                    if (progress == 100) {
                        onProgressFinish?.invoke()
                        cancel()
                    }
                }
                duration = animDuration
                interpolator = DecelerateInterpolator(4f)
            }
        }

    }

    fun setProgress(progress: Int) {
        currentProgress = progress
        invalidate()
    }

    fun getProgress(): Int {
        return currentProgress
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setDrawableIds(drawableIds: IntArray) {
        this.drawableIds = drawableIds
        drawableList.clear()
        for (id in drawableIds) {
            drawableList.add(resources.getDrawable(id, null))
        }
        invalidate()
    }


    private val path = Path()
    private val fontPath = Path()
    private val bgPath = Path()
    private val tempPath = Path()

    private var pathSpaceTemp = -(pathSpace + pathWidth).toFloat()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specModeW = MeasureSpec.getMode(widthMeasureSpec)
        val specSizeW = MeasureSpec.getSize(widthMeasureSpec)

        val specModeH = MeasureSpec.getMode(heightMeasureSpec)
        val specSizeH = MeasureSpec.getSize(heightMeasureSpec)

        var targetW = 0
        var targetH = 0

        if (specModeW == MeasureSpec.EXACTLY) {
            targetW = specSizeW
            progressBarWidth = targetW.toFloat()
        } else if (specModeW == MeasureSpec.AT_MOST) {
            targetW = 1000
            progressBarWidth = targetW.toFloat()
        }

        if (specModeH == MeasureSpec.EXACTLY) {
            targetH = specSizeH
        } else if (specModeH == MeasureSpec.AT_MOST) {
            targetH = if (isAnimFrame) {
                (progressBarHeight + drawableHeight).toInt()
            } else {
                (drawableHeight).toInt()
            }
        }

        if (isAnimFrame) {
            bgRectF.left = drawableWidth / 2f
            bgRectF.top = drawableHeight
            bgRectF.right = progressBarWidth - drawableWidth / 2f
            bgRectF.bottom = drawableHeight + progressBarHeight
        } else if (isPointCenter) {
            bgRectF.left = drawableWidth / 2f
            bgRectF.top = drawableHeight / 2f
            bgRectF.right = progressBarWidth - drawableWidth / 2f
            bgRectF.bottom = progressBarHeight + progressBarHeight / 2f
        } else {
            bgRectF.left = 0f
            bgRectF.top = drawableHeight / 2f
            bgRectF.right = progressBarWidth
            bgRectF.bottom = progressBarHeight + progressBarHeight / 2f
        }

        setMeasuredDimension(targetW, targetH)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBackground(canvas)

        drawFontBar(canvas)

//        drawFrameBar(canvas)

    }

    /**
     * 画前景 进度
     */
    private fun drawFontBar(canvas: Canvas) {
        if (isStripe) {
            var subW = fontRectF.width()
            var left = fontRectF.left

            path.reset()
            while (subW > (pathSpaceTemp + pathSpace)) {
                path.moveTo(left + pathSpaceTemp + pathSpace, fontRectF.top)
                path.lineTo(left + pathSpaceTemp + pathSpace - pathSpace * 3 / 4f, fontRectF.bottom)
                path.lineTo(
                    left + pathSpaceTemp + pathSpace - pathSpace * 3 / 4f + pathWidth,
                    fontRectF.bottom
                )
                path.lineTo(left + pathSpaceTemp + pathSpace + pathWidth, fontRectF.top)
                path.close()
                subW -= (pathSpace + pathWidth)
                left += (pathSpace + pathWidth)
            }

            if (!path.isEmpty) {
                // 画前景
                canvas.drawPath(fontPath, fontPaint)

                // 取共同的部分
                tempPath.op(fontPath, path, Path.Op.INTERSECT)
                // 画斜线
                canvas.drawPath(tempPath, pathPaint)

//                tempPath.op(fontPath, path, Path.Op.DIFFERENCE)
//                // 画前景
//                canvas.drawPath(tempPath, fontPaint)
            }

            if (pathSpaceTemp < 0) {
                pathSpaceTemp += 2f
            } else {
                pathSpaceTemp = -(pathSpace + pathWidth).toFloat()
            }

        } else {
            // 画前景
            canvas.drawPath(fontPath, fontPaint)
        }
    }

    /**
     * 画背景
     */
    private fun drawBackground(canvas: Canvas) {
        bgPath.reset()
        bgPath.addRoundRect(bgRectF, radius, radius, Path.Direction.CW)

        val progress = bgRectF.width() * currentProgress / 100
        fontRectF.left = bgRectF.left
        fontRectF.top = bgRectF.top
        fontRectF.right = bgRectF.left + progress
        fontRectF.bottom = bgRectF.bottom

        fontPath.reset()
        fontPath.addRoundRect(fontRectF, radius, radius, Path.Direction.CW)

        // 取差集
        bgPath.op(fontPath, Path.Op.DIFFERENCE)
        // 画背景
        canvas.drawPath(bgPath, bgPaint)
    }

    private var frameTime = 0L

    /**
     *  处理帧动画
     */
    private fun drawFrameBar(canvas: Canvas) {
        if (!isAnimFrame)
            return
        barRectF.left = fontRectF.right - drawableWidth / 2f
        barRectF.top = 0f
        barRectF.right = fontRectF.right + drawableWidth / 2f
        barRectF.bottom = drawableHeight

        if (drawableList.isNotEmpty()) {
            drawable = drawableList[frameIndex]
            drawable?.also {
                it.bounds = Rect(
                    barRectF.left.toInt(),
                    barRectF.top.toInt(),
                    barRectF.right.toInt(),
                    barRectF.bottom.toInt()
                )
                it.draw(canvas)
                if (frameIndex >= drawableList.size - 1) {
                    frameIndex = 0
                } else {
                    if (System.currentTimeMillis() - frameTime > 40) {
                        frameIndex++
                        frameTime = System.currentTimeMillis()
                    }
                }

            }
        } else {
            canvas.drawCircle(
                barRectF.centerX(),
                barRectF.centerY(),
                drawableHeight / 2f,
                fontPaint
            )
        }
    }

}