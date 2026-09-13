package com.example.leveldash

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class GameView(ctx: Context) : View(ctx) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var screen = 0
    private var level = 1
    private var character = 0
    private var deaths = 0

    private var playerX = 100f
    private var playerY = 0f
    private var velocityY = 0f

    private var initialized = false
    private var gameRunning = false

    private val names = arrayOf(
        "NOVA",
        "BOLT",
        "MINT",
        "SHADOW",
        "ROBO",
        "FLARE"
    )

    private val colors = intArrayOf(
        Color.rgb(124, 77, 255),
        Color.rgb(0, 229, 255),
        Color.rgb(80, 220, 120),
        Color.rgb(80, 80, 95),
        Color.rgb(255, 190, 50),
        Color.rgb(255, 70, 120)
    )

    private val platforms = mutableListOf<RectF>()
    private val spikes = mutableListOf<RectF>()

    private val prefs =
        ctx.getSharedPreferences("save", Context.MODE_PRIVATE)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.rgb(8, 8, 18))

        when (screen) {
            0 -> drawMenu(canvas)
            1 -> drawLevels(canvas)
            2 -> drawCharacters(canvas)
            3 -> drawGame(canvas)
        }

        if (screen == 3) {
            postInvalidateDelayed(16)
        }
    }

    private fun drawText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        size: Float,
        color: Int,
        center: Boolean = false
    ) {
        paint.style = Paint.Style.FILL
        paint.color = color
        paint.textSize = size
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign =
            if (center) Paint.Align.CENTER else Paint.Align.LEFT

        canvas.drawText(text, x, y, paint)
    }

    private fun drawButton(
        canvas: Canvas,
        rect: RectF,
        color: Int,
        text: String,
        textSize: Float
    ) {
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawRoundRect(rect, 18f, 18f, paint)

        drawText(
            canvas,
            text,
            rect.centerX(),
            rect.centerY() + textSize / 3f,
            textSize,
            Color.WHITE,
            true
        )
    }

    private fun drawMenu(canvas: Canvas) {
        val w = width.toFloat()

        drawText(
            canvas,
            "LEVEL DASH",
            w / 2f,
            120f,
            52f,
            Color.WHITE,
            true
        )

        drawText(
            canvas,
            "TRAPS AREN'T FAIR",
            w / 2f,
            158f,
            18f,
            Color.rgb(170, 170, 190),
            true
        )

        drawButton(
            canvas,
            RectF(
                w / 2f - 180f,
                220f,
                w / 2f + 180f,
                300f
            ),
            Color.rgb(124, 77, 255),
            "PLAY",
            30f
        )

        drawButton(
            canvas,
            RectF(
                w / 2f - 180f,
                325f,
                w / 2f - 10f,
                395f
            ),
            Color.rgb(30, 30, 52),
            "SKINS",
            20f
        )

        drawButton(
            canvas,
            RectF(
                w / 2f + 10f,
                325f,
                w / 2f + 180f,
                395f
            ),
            Color.rgb(30, 30, 52),
            "SETTINGS",
            17f
        )

        drawText(
            canvas,
            "100 LEVELS • 6 HEROES • 5 WORLDS",
            w / 2f,
            450f,
            15f,
            Color.rgb(120, 120, 145),
            true
        )
    }

    private fun drawLevels(canvas: Canvas) {
        drawText(
            canvas,
            "SELECT LEVEL",
            40f,
            65f,
            30f,
            Color.WHITE
        )

        drawText(
            canvas,
            "WORLD 1 — TRAINING",
            40f,
            105f,
            16f,
            Color.rgb(160, 160, 180)
        )

        val unlocked = prefs.getInt("unlocked", 1)

        for (i in 0 until 25) {

            val column = i % 5
            val row = i / 5

            val x = 40f + column * 145f
            val y = 145f + row * 72f

            val isUnlocked = i + 1 <= unlocked

            val color =
                if (isUnlocked) {
                    Color.rgb(28, 28, 48)
                } else {
                    Color.rgb(17, 17, 28)
                }

            drawButton(
                canvas,
                RectF(
                    x,
                    y,
                    x + 115f,
                    y + 52f
                ),
                color,
                (i + 1).toString(),
                20f
            )
        }

        drawText(
            canvas,
            "← BACK",
            40f,
            height.toFloat() - 25f,
            17f,
            Color.rgb(170, 170, 190)
        )
    }

    private fun drawCharacters(canvas: Canvas) {

        drawText(
            canvas,
            "HEROES",
            40f,
            65f,
            30f,
            Color.WHITE
        )

        for (i in names.indices) {

            val x = 45f + (i % 3) * 235f
            val y = 105f + (i / 3) * 155f

            paint.color = Color.rgb(25, 25, 45)

            canvas.drawRoundRect(
                RectF(
                    x,
                    y,
                    x + 205f,
                    y + 125f
                ),
                18f,
                18f,
                paint
            )

            paint.color = colors[i]

            canvas.drawCircle(
                x + 50f,
                y + 62f,
                27f,
                paint
            )

            drawText(
                canvas,
                names[i],
                x + 92f,
                y + 55f,
                20f,
                Color.WHITE
            )

            val available =
                i == 0 || prefs.getBoolean("hero$i", false)

            drawText(
                canvas,
                if (available) "AVAILABLE" else "LOCKED",
                x + 92f,
                y + 82f,
                13f,
                Color.rgb(150, 150, 170)
            )
        }

        drawText(
            canvas,
            "← BACK",
            40f,
            height.toFloat() - 25f,
            17f,
            Color.rgb(170, 170, 190)
        )
    }

    private fun setupLevel() {

        platforms.clear()
        spikes.clear()

        val h = height.toFloat()
        val w = width.toFloat()

        platforms.add(
            RectF(
                0f,
                h - 70f,
                w,
                h
            )
        )

        val difficulty = min(level, 100)

        var x = 180f

        val gap =
            38f + min(
                55f,
                difficulty * 0.45f
            )

        val count =
            8 + min(
                8,
                difficulty / 7
            )

        for (i in 0 until count) {

            val y =
                h - 115f -
                (i % 3) * 35f

            val platformWidth =
                max(
                    60f,
                    130f -
                    difficulty * 0.45f
                )

            platforms.add(
                RectF(
                    x,
                    y,
                    x + platformWidth,
                    y + 22f
                )
            )

            if (i % 2 == 1 || difficulty > 12) {

                spikes.add(
                    RectF(
                        x + platformWidth / 2f - 15f,
                        y - 20f,
                        x + platformWidth / 2f + 15f,
                        y
                    )
                )
            }

            x += platformWidth + gap

            if (x > w + 100f) {
                break
            }
        }

        playerX = 80f
        playerY = h - 120f
        velocityY = 0f

        initialized = true
        gameRunning = true
    }

    private fun drawGame(canvas: Canvas) {

        if (!initialized || !gameRunning) {
            setupLevel()
        }

        val w = width.toFloat()
        val h = height.toFloat()

        paint.color = when ((level - 1) / 20) {
            0 -> Color.rgb(12, 12, 28)
            1 -> Color.rgb(8, 20, 28)
            2 -> Color.rgb(25, 12, 25)
            3 -> Color.rgb(25, 20, 8)
            else -> Color.rgb(10, 8, 20)
        }

        canvas.drawRect(
            0f,
            0f,
            w,
            h,
            paint
        )

        for (platform in platforms) {

            paint.color = Color.rgb(
                45,
                45,
                70
            )

            canvas.drawRoundRect(
                platform,
                7f,
                7f,
                paint
            )
        }

        for (spike in spikes) {

            paint.color = Color.rgb(
                255,
                70,
                100
            )

            val path = Path()

            path.moveTo(
                spike.centerX(),
                spike.bottom
            )

            path.lineTo(
                spike.left,
                spike.top
            )

            path.lineTo(
                spike.right,
                spike.top
            )

            path.close()

            canvas.drawPath(
                path,
                paint
            )
        }

        val goal = RectF(
            w - 75f,
            h - 145f,
            w - 30f,
            h - 70f
        )

        paint.color = Color.rgb(
            80,
            220,
            140
        )

        canvas.drawRoundRect(
            goal,
            8f,
            8f,
            paint
        )

        drawText(
            canvas,
            "EXIT",
            w - 52f,
            h - 155f,
            11f,
            Color.rgb(80, 220, 140),
            true
        )

        updatePhysics()

        paint.color = colors[character]

        canvas.drawCircle(
            playerX,
            playerY,
            20f,
            paint
        )

        drawText(
            canvas,
            names[character],
            playerX,
            playerY - 28f,
            11f,
            Color.WHITE,
            true
        )

        drawButton(
            canvas,
            RectF(
                25f,
                h - 65f,
                145f,
                h - 20f
            ),
            Color.argb(
                90,
                255,
                255,
                255
            ),
            "MOVE",
            14f
        )

        drawButton(
            canvas,
            RectF(
                w - 155f,
                h - 85f,
                w - 25f,
                h - 20f
            ),
            Color.argb(
                90,
                124,
                77,
                255
            ),
            "JUMP",
            16f
        )

        drawText(
            canvas,
            "LEVEL $level • DEATHS $deaths",
            25f,
            35f,
            15f,
            Color.WHITE
        )
    }

    private fun updatePhysics() {

        val h = height.toFloat()
        val w = width.toFloat()

        velocityY += 0.8f
        playerY += velocityY

        if (playerY > h + 80f ||
            playerX < -50f
        ) {
            die()
            return
        }

        for (platform in platforms) {

            val touching =
                playerX > platform.left - 18
