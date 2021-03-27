package com.example.wheelgame.views
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.Path.FillType
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.example.wheelgame.R
import com.example.wheelgame.models.Entry
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

class WheelView @JvmOverloads constructor(
    context: Context,
    data: List<Entry>

) : View(context) {
    //if further developed, can adjust offsets based on screen metrics
    private val RADIUS_OFFSET_LABEL = 0
    private val BORDER_OFFSET = 30
    private val TEXT_OFFSET_X = 50
    private val TEXT_OFFSET_Y = -20
    private var radius = 0.0f
    private var totalEntries = data.size
    private var entries = data
    private var fps: Long = 1000 / 60
    private var hasInit: Boolean = false
    private var isSpinning: Boolean = false
    private var currentRotation: Float = 0f
    private var stopperRotation: Float = 0f
    private var rotationTime: Long = 0
    private var rotationTimeLength: Long = 1000
    private var currentSpeed: Float = 0f
    private var maxSpeed: Float = 5f
    private var stopperMin: Float = 0f
    private var stopperMax: Float = 0f
    private var completionPercent: Float = 0f
    private var randomAdjuster: Float = 0f
    private lateinit var timer: Timer
    private lateinit var task: TimerTask
    private var pointPosition: PointF = PointF(0.0f, 0.0f)
    private var centerPoint: PointF = PointF(0.0f, 0.0f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        radius = (min(width, height) / 2.0 * 0.8).toFloat()
    }

    private fun resetValues(){
        stopperRotation = 0f
        rotationTime = 0
        rotationTimeLength = 1000
        currentSpeed = 0f
        maxSpeed = 5f
        stopperMin = 0f
        stopperMax= 0f
        completionPercent = 0f
        randomAdjuster = 0f
        postInvalidate()
    }

    private fun PointF.computeXYForSpeed(pos: Int, radius: Float) {
        // Angles are in radians.
        val startAngle = 270*Math.PI/180
        val angle: Double
        if (totalEntries % 2 == 0) {
            angle = (startAngle + pos * (Math.PI / (totalEntries / (2))))
        }
        else{ angle = (startAngle + pos * (Math.PI*2  / (totalEntries/(1))))
        }
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }
    fun PointF.calculateMidpoint(p1: PointF, p2: PointF)
    {
        x = (p1.x + p2.x)/2
        y = (p1.y + p2.y)/2
    }
    fun PointF.offsetXY()
    {
        x = x - TEXT_OFFSET_X
        y = y - TEXT_OFFSET_Y

    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if(!hasInit)
        drawInitialState(canvas)
        else
            drawRotatedState(canvas)


    }
    fun drawInitialState(canvas: Canvas){
        val res: Resources = resources
        val outerRingBitmap = BitmapFactory.decodeResource(res, R.drawable.wheel_ring)
        val unscaledStopperBitmap = BitmapFactory.decodeResource(res, R.drawable.wheel_ticker)
        val stopperBitmap = Bitmap.createScaledBitmap(
            unscaledStopperBitmap,
            unscaledStopperBitmap.width / 2,
            unscaledStopperBitmap.height / 2,
            false
        )
        centerPoint = PointF((width / 2).toFloat(), (height / 2).toFloat())
        paint.color = Color.BLACK
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)
        paint.color = Color.WHITE
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in 0..totalEntries - 1) {
            //draw triangle
            if (i % 2 == 0)
                paint.color = Color.parseColor("#040D14");
            else
                paint.color = Color.parseColor("#141A1E");

            paint.style = Paint.Style.FILL_AND_STROKE

            pointPosition.computeXYForSpeed(i, labelRadius)
            val a = PointF(pointPosition.x, pointPosition.y)
            pointPosition.computeXYForSpeed(i + 1, labelRadius)
            val b = PointF(pointPosition.x, pointPosition.y)
            val c = PointF(centerPoint.x, centerPoint.y)

            val path = Path()
            path.fillType = FillType.EVEN_ODD
            path.lineTo(c.x, c.y)
            path.lineTo(b.x, b.y)

            path.lineTo(a.x, a.y)
            path.lineTo(c.x, c.y)
            path.close()

            canvas.drawPath(path, paint)


        }
        paint.setTextSize(20f);
        //draw text
        for (i in 0..totalEntries-1) {
            paint.color = Color.GRAY
            paint.textSize = 50f
            val rotatedtext = entries.get(i).displayText


            //Draw bounding rect before rotating text:
            val startPoint = PointF(0f, 0f)
            val p1 = PointF(0f, 0f)
            val p2 = PointF(0f, 0f)
            val midpoint = PointF(0f, 0f)
            val tempPoint = PointF(0f, 0f)
            p1.computeXYForSpeed(i, labelRadius)
            p2.computeXYForSpeed(i + 1, labelRadius)
            midpoint.calculateMidpoint(p1, p2)
//            midpoint.calculateMidpoint(centerPoint, midpoint)
            startPoint.calculateMidpoint(centerPoint, midpoint)
            tempPoint.calculateMidpoint(p1, p2)
            startPoint.calculateMidpoint(startPoint, midpoint)
            startPoint.calculateMidpoint(startPoint, midpoint)

            startPoint.offsetXY()
            pointPosition.computeXYForSpeed(i + 1, labelRadius)

            val rect = Rect()
            paint.getTextBounds(rotatedtext, 0, rotatedtext.length, rect)
            canvas.translate(startPoint.x, startPoint.y)
            paint.style = Paint.Style.FILL


            canvas.translate((-startPoint.x).toFloat(), (-startPoint.y).toFloat())



            paint.color = Color.WHITE
            canvas.save()
            canvas.rotate(
                ((((360 / totalEntries) * (i + 1)) - (360 / totalEntries / 2)) - 90).toFloat(),
                startPoint.x + rect.exactCenterX(),
                startPoint.y + rect.exactCenterY()
            )
            paint.style = Paint.Style.FILL
            canvas.drawText(rotatedtext, startPoint.x, startPoint.y, paint)
            canvas.restore()
            canvas.save()
        }


        canvas.drawBitmap(
            outerRingBitmap, Rect(0, 0, outerRingBitmap.width, outerRingBitmap.height), Rect(
                ((width / 2) - (radius + BORDER_OFFSET)).toInt(),
                ((height / 2) - (radius + BORDER_OFFSET)).toInt(),
                ((width / 2) + ((radius + BORDER_OFFSET))).toInt(),
                ((height / 2) + ((radius + BORDER_OFFSET))).toInt()
            ), paint
        )
        pointPosition.computeXYForSpeed(0, labelRadius)
        canvas.drawBitmap(
            stopperBitmap, Rect(0, 0, stopperBitmap.width, stopperBitmap.height), Rect(
                pointPosition.x.toInt() - stopperBitmap.width / 2,
                pointPosition.y.toInt() - stopperBitmap.width / 2,
                (pointPosition.x.toInt() + stopperBitmap.width / 2).toInt(),
                (pointPosition.y.toInt() + stopperBitmap.height / 2).toInt()
            ), paint
        )
        hasInit = true
    }
    fun drawRotatedState(canvas: Canvas){

        canvas.save()
        canvas.rotate(
            currentRotation, (width / 2).toFloat(), (height / 2).toFloat()
        )

        val res: Resources = resources
        val outerRingBitmap = BitmapFactory.decodeResource(res, R.drawable.wheel_ring)
        val unscaledStopperBitmap = BitmapFactory.decodeResource(res, R.drawable.wheel_ticker)
        val stopperBitmap = Bitmap.createScaledBitmap(
            unscaledStopperBitmap,
            unscaledStopperBitmap.width / 2,
            unscaledStopperBitmap.height / 2,
            false
        )
        centerPoint = PointF((width / 2).toFloat(), (height / 2).toFloat())
        paint.color = Color.BLACK
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)
        paint.color = Color.WHITE
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in 0..totalEntries - 1) {
            //draw triangle
            if (i % 2 == 0)
                paint.color = Color.parseColor("#040D14");
            else
                paint.color = Color.parseColor("#141A1E");

            paint.style = Paint.Style.FILL_AND_STROKE

            pointPosition.computeXYForSpeed(i, labelRadius)
            val a = PointF(pointPosition.x, pointPosition.y)
            pointPosition.computeXYForSpeed(i + 1, labelRadius)
            val b = PointF(pointPosition.x, pointPosition.y)
            val c = PointF(centerPoint.x, centerPoint.y)

            val path = Path()
            path.fillType = FillType.EVEN_ODD
            path.lineTo(c.x, c.y)
            path.lineTo(b.x, b.y)

            path.lineTo(a.x, a.y)
            path.lineTo(c.x, c.y)
            path.close()

            canvas.drawPath(path, paint)


        }
        paint.setTextSize(20f);
        //draw text
        for (i in 0..totalEntries-1) {
            paint.color = Color.GRAY
            paint.textSize = 50f
            val rotatedtext = entries.get(i).displayText


            //Draw bounding rect before rotating text:
            val startPoint = PointF(0f, 0f)
            val p1 = PointF(0f, 0f)
            val p2 = PointF(0f, 0f)
            val midpoint = PointF(0f, 0f)
            val tempPoint = PointF(0f, 0f)
            p1.computeXYForSpeed(i, labelRadius)
            p2.computeXYForSpeed(i + 1, labelRadius)
            midpoint.calculateMidpoint(p1, p2)
            //midpoint.calculateMidpoint(centerPoint, midpoint)
            startPoint.calculateMidpoint(centerPoint, midpoint)
            tempPoint.calculateMidpoint(p1, p2)
            startPoint.calculateMidpoint(startPoint, midpoint)
            startPoint.calculateMidpoint(startPoint, midpoint)

            startPoint.offsetXY()
            pointPosition.computeXYForSpeed(i + 1, labelRadius)

            val rect = Rect()
            paint.getTextBounds(rotatedtext, 0, rotatedtext.length, rect)
            canvas.translate(startPoint.x, startPoint.y)
            paint.style = Paint.Style.FILL


            canvas.translate((-startPoint.x).toFloat(), (-startPoint.y).toFloat())

            paint.color = Color.WHITE
            canvas.save()
            canvas.rotate(
                ((((360 / totalEntries) * (i + 1)) - (360 / totalEntries / 2)) - 90).toFloat(),
                startPoint.x + rect.exactCenterX(),
                startPoint.y + rect.exactCenterY()
            )
            paint.style = Paint.Style.FILL
            canvas.drawText(rotatedtext, startPoint.x, startPoint.y, paint)
            canvas.restore()
        }

        canvas.restore();
        canvas.save();
        canvas.drawBitmap(
            outerRingBitmap, Rect(0, 0, outerRingBitmap.width, outerRingBitmap.height), Rect(
                ((width / 2) - (radius + BORDER_OFFSET)).toInt(),
                ((height / 2) - (radius + BORDER_OFFSET)).toInt(),
                ((width / 2) + ((radius + BORDER_OFFSET))).toInt(),
                ((height / 2) + ((radius + BORDER_OFFSET))).toInt()
            ), paint
        )
        canvas.save()
        canvas.rotate(
            stopperRotation * -1f, pointPosition.x, pointPosition.y
        )
        pointPosition.computeXYForSpeed(0, labelRadius)
        canvas.drawBitmap(
            stopperBitmap, Rect(0, 0, stopperBitmap.width, stopperBitmap.height), Rect(
                pointPosition.x.toInt() - stopperBitmap.width / 2,
                pointPosition.y.toInt() - stopperBitmap.width / 2,
                (pointPosition.x.toInt() + stopperBitmap.width / 2).toInt(),
                (pointPosition.y.toInt() + stopperBitmap.height / 2).toInt()
            ), paint
        )
        canvas.restore();
        canvas.save();
    }
    fun startSpinning() {
        isSpinning = true
        timer = Timer()
        task = timerTask {
            invalidateCanvas()
        }
        // set timer to run every 16 milliseconds (fps = 1000 / 60)
        timer.scheduleAtFixedRate(task, 0, fps)
    }

    fun doneSpinning() {
        isSpinning = false
        showToast()
        task.cancel()
        timer.cancel()
        resetValues()
    }
    fun showToast(){
        Looper.prepare();
        var rewardIdx = entries.size - (floor(currentRotation * entries.size.toFloat() / 360f)).toInt() - 1
        if(rewardIdx < 0) rewardIdx = entries.size + rewardIdx
        else if(rewardIdx > entries.size-1) rewardIdx = rewardIdx - entries.size -1
        var reward:Entry = entries.get(rewardIdx)
        val myToast = Toast.makeText(
            context,
            "You won " + reward.displayText + " from reward #" + reward.id,
            Toast.LENGTH_LONG
        )
        myToast.setGravity(Gravity.CENTER_HORIZONTAL, 200, 200)
        myToast.show()
    }
    private fun invalidateCanvas() {
        Thread(Runnable {

            rotationTime += 1
            completionPercent = ((rotationTime).toFloat()) / rotationTimeLength.toFloat()
            var temp = ((0..100).random() / 100f).toFloat()
            //randomly subtract an additional amount from the current speed, stored in a variable that can only be increased (logically the spinner cannot speed up)
            if (temp >= randomAdjuster)
                randomAdjuster = temp
            currentSpeed = maxSpeed - ((completionPercent) * maxSpeed) - randomAdjuster
            Log.d("median", completionPercent.toString())
            //stopper is likely to change rotation the closer it is to completion, stops unnatural wiggling motion near end of animation
            if ((0..100).random() > ((completionPercent * 150f).toInt())) {
                //as nearing completion, slowly reduce the median of stopper and increase range of motion
                var stopperMedian = (40f - (completionPercent * 40f))

                stopperMin = (stopperMedian - (10f))
                stopperMax = (stopperMedian + (10f))
                stopperRotation = (stopperMin.toInt()..stopperMax.toInt()).random().toFloat()
                if (stopperRotation < -10) stopperRotation = (0..10).random() * -1f
            }

            currentRotation += currentSpeed

            if (rotationTime >= rotationTimeLength || currentSpeed < 0) doneSpinning()

            if (currentRotation > 360) currentRotation = 0f
            postInvalidate()
        }).start()
    }


}