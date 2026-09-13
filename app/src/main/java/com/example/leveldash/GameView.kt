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
    private val prefs = ctx.getSharedPreferences("save", Context.MODE_PRIVATE)

    private var screen = 0
    private var level = 1
    private var character = 0
    private var deaths = 0

    private var playerX = 80f
    private var playerY = 0f
    private var velocityY = 0f

    private var initialized = false

    private val names = arrayOf(
        "NOVA", "BOLT", "MINT",
        "SHADOW", "ROBO", "FLARE"
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

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
        value: String,
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

        canvas.drawText(value, x, y, paint)
    }

    private fun button(
        canvas: Canvas,
        rect: RectF,
        color: Int,
        title: String,
        size: Float
    ) {
        paint.style = Paint.Style.FILL
        paint.color = color

        canvas.drawRoundRect(
            rect,
            18f,
            18f,
            paint
        )

        drawText(
            canvas,
            title,
            rect.centerX(),
            rect.centerY() + size / 3f,
            size,
            Color.WHITE,
            true
        )
    }

    private fun drawMenu(canvas: Canvas) {
        val w = width.toFloat()

        canvas.drawColor(Color.rgb(8, 8, 18))

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

        button(
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

        button(
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

        button(
            canvas,
            RectF(
                w / 2f + 10f,
                325f,
                w / 2f + 180f,
                395f
            ),
            Color.rgb(30, 30, 52),
            "HEROES",
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
        val h = height.toFloat()

        canvas.drawColor(Color.rgb(8, 8, 18))

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

            val available = i + 1 <= unlocked

            val color = if (available) {
                Color.rgb(28, 28, 48)
            } else {
                Color.rgb(17, 17, 28)
            }

            button(
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
            h - 25f,
            17f,
            Color.rgb(170, 170, 190)
        )
    }

    private fun drawCharacters(canvas: Canvas) {
        val h = height.toFloat()

        canvas.drawColor(Color.rgb(8, 8, 18))

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
            h - 25f,
            17f,
            Color.rgb(170, 170, 190)
        )
    }

    private fun setupLevel() {
        platforms.clear()
        spikes.clear()

        val w = width.toFloat()
        val h = height.toFloat()

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

        val gap = 38f + min(
            55f,
            difficulty * 0.45f
        )

        val count = 8 + min(
            8,
            difficulty / 7
        )

        for (i in 0 until count) {
            val y = h - 115f - (i % 3) * 35f

            val platformWidth = max(
                60f,
                130f - difficulty * 0.45f
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
    }

    private fun drawGame(canvas: Canvas) {
        if (!initialized) {
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
            paint.color = Color.rgb(45, 45, 70)

            canvas.drawRoundRect(
                platform,
                7f,
                7f,
                paint
            )
        }

        for (spike in spikes) {
            paint.color = Color.rgb(255, 70, 100)

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

        paint.color = Color.rgb(80, 220, 140)

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

        button(
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

        button(
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

        if (playerY > h + 80f || playerX < -50f) {
            resetPlayer()
            return
        }

        for (platform in platforms) {
            val touching =
                playerX > platform.left - 18f &&
                playerX < platform.right + 18f &&
                playerY + 20f >= platform.top &&
                playerY + 20f <= platform.top + 28f &&
                velocityY >= 0f

            if (touching) {
                playerY = platform.top - 20f
                velocityY = 0f
            }
        }

        for (spike in spikes) {
            val hit =
                abs(playerX - spike.centerX()) < 22f &&
                playerY > spike.top - 25f &&
                playerY < spike.bottom + 15f

            if (hit) {
                resetPlayer()
                return
            }
        }

        if (
            playerX > w - 80f &&
            playerY > h - 190f
        ) {
            nextLevel()
        }
    }

    private fun resetPlayer() {
        deaths++

        playerX = 80f
        playerY = height.toFloat() - 120f
        velocityY = 0f
    }

    private fun nextLevel() {
        if (level < 100) {
            val unlocked = prefs.getInt("unlocked", 1)

            if (level >= unlocked) {
                prefs.edit()
                    .putInt(
                        "unlocked",
                        min(100, level + 1)
                    )
                    .apply()
            }

            level++
        }

        initialized = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {

            when (screen) {

                0 -> {
                    if (
                        y >= 210f &&
                        y <= 315f
                    ) {
                        screen = 1
                    } else if (
                        y >= 320f &&
                        y <= 410f &&
                        x > width / 2f
                    ) {
                        screen = 2
                    }
                }

                1 -> {
                    if (y > height.toFloat() - 70f) {
                        screen = 0
                    } else {
                        val column =
                            ((x - 40f) / 145f).toInt()

                        val row =
                            ((y - 145f) / 72f).toInt()

                        if (
                            column in 0..4 &&
                            row in 0..4
                        ) {
                            val selected =
                                row * 5 + column + 1

                            val unlocked =
                                prefs.getInt(
                                    "unlocked",
                                    1
                                )

                            if (selected <= unlocked) {
                                level = selected
                                initialized = false
                                screen = 3
                            }
                        }
                    }
                }

                2 -> {
                    if (y > height.toFloat() - 70f) {
                        screen = 0
                    } else {
                        val column =
                            ((x - 45f) / 235f).toInt()

                        val row =
                            ((y - 105f) / 155f).toInt()

                        val selected =
                            row * 3 + column

                        if (selected in names.indices) {
                            val available =
                                selected == 0 ||
                                prefs.getBoolean(
                                    "hero$selected",
                                    false
                                )

                            if (available) {
                                character = selected
                            }
                        }
                    }
                }

                3 -> {
                    if (
                        x > width.toFloat() - 180f &&
                        y > height.toFloat() - 120f
                    ) {
                        if (velocityY == 0f) {
                            velocityY = -14f
                        }
                    } else if (
                        x < 170f &&
                        y > height.toFloat() - 100f
                    ) {
                        playerX += 35f
                    } else if (
                        x > 170f &&
                        y > height.toFloat() - 120f
                    ) {
                        playerX += 22f
                    }
                }
            }
        }

        if (
            event.action == MotionEvent.ACTION_MOVE &&
            screen == 3 &&
            y > height.toFloat() - 110f
        ) {
            if (x < 180f) {
                playerX -= 8f
            } else {
                playerX += 8f
            }
        }

        return true
    }
}
