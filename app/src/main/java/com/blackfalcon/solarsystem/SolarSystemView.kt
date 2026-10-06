package com.blackfalcon.solarsystem

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

data class Planet(
    val name: String,
    val distanceAU: Double,
    val radiusPx: Float,
    val color: Int,
    val funFact: String,
    var angle: Float = 0f // current orbital angle in degrees
)

class SolarSystemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 28f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 22f
    }
    private val orbitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        color = Color.argb(60, 255, 255, 255)
    }
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.argb(120, 255, 255, 255)
    }
    private val compassPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.argb(180, 79, 195, 247)
    }
    private val northPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700")
        textAlign = Paint.Align.CENTER
        textSize = 32f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private var centerX = 0f
    private var centerY = 0f
    private var maxRadius = 0f
    private var heading = 0f // device heading in degrees (0 = North)

    private val planets = listOf(
        Planet("Mercury", 0.39, 8f, Color.parseColor("#9E9E9E"), "Smallest planet, fastest orbit"),
        Planet("Venus", 0.72, 12f, Color.parseColor("#FFCC80"), "Hottest planet, thick atmosphere"),
        Planet("Earth", 1.00, 13f, Color.parseColor("#42A5F5"), "Our home, only known life"),
        Planet("Mars", 1.52, 10f, Color.parseColor("#E57373"), "The Red Planet, future colony?"),
        Planet("Jupiter", 5.20, 28f, Color.parseColor("#FFB74D"), "Largest planet, gas giant"),
        Planet("Saturn", 9.58, 24f, Color.parseColor("#FFECB3"), "Famous for its beautiful rings"),
        Planet("Uranus", 19.2, 18f, Color.parseColor("#80DEEA"), "Ice giant, tilted on its side"),
        Planet("Neptune", 30.1, 17f, Color.parseColor("#5C6BC0"), "Windiest planet in the system"),
        Planet("Pluto", 39.5, 7f, Color.parseColor("#B0BEC5"), "Dwarf planet, heart-shaped glacier")
    )

    // Approximate relative orbital distances scaled for display
    private val orbitScales = floatArrayOf(0.12f, 0.18f, 0.24f, 0.31f, 0.45f, 0.58f, 0.72f, 0.85f, 0.96f)

    private var selectedPlanet: Planet? = null
    private var onPlanetSelected: ((Planet) -> Unit)? = null

    private val animator = object : Runnable {
        override fun run() {
            // Slowly advance orbital positions
            planets.forEachIndexed { index, planet ->
                // Inner planets move faster
                val speed = when (index) {
                    0 -> 1.8f
                    1 -> 1.3f
                    2 -> 1.0f
                    3 -> 0.8f
                    4 -> 0.4f
                    5 -> 0.3f
                    6 -> 0.2f
                    7 -> 0.15f
                    else -> 0.1f
                }
                planet.angle = (planet.angle + speed) % 360f
            }
            invalidate()
            postDelayed(this, 30)
        }
    }

    init {
        // Start with some random-ish angles
        planets.forEachIndexed { i, p ->
            p.angle = (i * 40f) % 360f
        }
    }

    fun setHeading(degrees: Float) {
        heading = degrees
        invalidate()
    }

    fun setOnPlanetSelectedListener(listener: (Planet) -> Unit) {
        onPlanetSelected = listener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(animator)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(animator)
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        maxRadius = min(w, h) / 2f * 0.88f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Background stars (simple)
        paint.color = Color.WHITE
        paint.alpha = 80
        for (i in 0 until 40) {
            val sx = (i * 97 % width).toFloat()
            val sy = (i * 53 % height).toFloat()
            canvas.drawCircle(sx, sy, 1.5f, paint)
        }
        paint.alpha = 255

        // Outer compass ring
        canvas.drawCircle(centerX, centerY, maxRadius, compassPaint)

        // Cardinal directions (rotated opposite to heading so N stays north)
        val directions = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
        directions.forEach { (label, angle) ->
            val rad = Math.toRadians((angle - heading).toDouble())
            val dx = sin(rad).toFloat() * (maxRadius + 28)
            val dy = -cos(rad).toFloat() * (maxRadius + 28)
            if (label == "N") {
                northPaint.color = Color.parseColor("#FFD700")
                canvas.drawText(label, centerX + dx, centerY + dy + 10, northPaint)
            } else {
                labelPaint.color = Color.argb(180, 255, 255, 255)
                canvas.drawText(label, centerX + dx, centerY + dy + 8, labelPaint)
            }
        }

        // Draw orbits and planets
        canvas.save()
        // Rotate the entire solar system so that the current heading points "up" relative to device
        // Actually we keep solar system fixed relative to screen, and only rotate the compass labels.
        // For a true compass feel we rotate the system opposite to heading.
        canvas.rotate(-heading, centerX, centerY)

        planets.forEachIndexed { index, planet ->
            val orbitR = maxRadius * orbitScales[index]
            // Orbit path
            canvas.drawCircle(centerX, centerY, orbitR, orbitPaint)

            // Planet position
            val rad = Math.toRadians(planet.angle.toDouble())
            val px = centerX + sin(rad).toFloat() * orbitR
            val py = centerY - cos(rad).toFloat() * orbitR

            // Planet body
            paint.color = planet.color
            paint.style = Paint.Style.FILL
            canvas.drawCircle(px, py, planet.radiusPx, paint)

            // Small highlight
            paint.color = Color.argb(100, 255, 255, 255)
            canvas.drawCircle(px - planet.radiusPx * 0.3f, py - planet.radiusPx * 0.3f, planet.radiusPx * 0.35f, paint)

            // Saturn rings
            if (planet.name == "Saturn") {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                paint.color = Color.argb(180, 255, 236, 179)
                canvas.drawOval(
                    px - planet.radiusPx * 1.8f,
                    py - planet.radiusPx * 0.5f,
                    px + planet.radiusPx * 1.8f,
                    py + planet.radiusPx * 0.5f,
                    paint
                )
                paint.style = Paint.Style.FILL
            }
        }

        // Sun in the center
        val sunRadius = 36f
        // Glow
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val gradient = RadialGradient(
            centerX, centerY, sunRadius * 1.8f,
            intArrayOf(Color.parseColor("#FFFF9800"), Color.parseColor("#00FF9800")),
            floatArrayOf(0.3f, 1f),
            Shader.TileMode.CLAMP
        )
        glowPaint.shader = gradient
        canvas.drawCircle(centerX, centerY, sunRadius * 1.8f, glowPaint)

        paint.shader = null
        paint.color = Color.parseColor("#FF9800")
        canvas.drawCircle(centerX, centerY, sunRadius, paint)

        // Inner bright core
        paint.color = Color.parseColor("#FFFFEB3B")
        canvas.drawCircle(centerX, centerY, sunRadius * 0.55f, paint)

        canvas.restore()

        // Selected planet indicator (in screen space)
        selectedPlanet?.let { planet ->
            val index = planets.indexOf(planet)
            if (index >= 0) {
                val orbitR = maxRadius * orbitScales[index]
                val rad = Math.toRadians((planet.angle - heading).toDouble())
                val px = centerX + sin(rad).toFloat() * orbitR
                val py = centerY - cos(rad).toFloat() * orbitR
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                paint.color = Color.WHITE
                canvas.drawCircle(px, py, planet.radiusPx + 8f, paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val tx = event.x
            val ty = event.y

            // Check which planet was tapped (accounting for rotation)
            planets.forEachIndexed { index, planet ->
                val orbitR = maxRadius * orbitScales[index]
                val rad = Math.toRadians((planet.angle - heading).toDouble())
                val px = centerX + sin(rad).toFloat() * orbitR
                val py = centerY - cos(rad).toFloat() * orbitR

                val dist = hypot(tx - px, ty - py)
                if (dist < planet.radiusPx + 20) {
                    selectedPlanet = planet
                    onPlanetSelected?.invoke(planet)
                    invalidate()
                    return true
                }
            }

            // Also check sun
            val sunDist = hypot(tx - centerX, ty - centerY)
            if (sunDist < 50) {
                selectedPlanet = null
                onPlanetSelected?.invoke(
                    Planet("Sun", 0.0, 36f, Color.parseColor("#FF9800"),
                        "Our star — 99.8% of the Solar System's mass")
                )
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
