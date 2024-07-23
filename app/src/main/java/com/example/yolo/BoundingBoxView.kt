package com.example.yolo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.pow
import kotlin.math.sqrt

class BoundingBoxView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results = listOf<BoundingBox>()
    private val boxPaint = Paint()
    private val textBackgroundPaint = Paint()
    private val textPaint = Paint()
    private val bounds = Rect()

    init {
        initPaints()
    }

    // Função para limpar as caixas e reiniciar os Paints
    fun clear() {
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        initPaints()
        invalidate()
    }

    // Função para inicializar os Paints com as propriedades desejadas
    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 8f
        boxPaint.style = Paint.Style.STROKE
    }

    // Função para desenhar as caixas delimitadoras e os textos no ecrã
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        results.forEach {
            val left = it.x1 * width
            val top = it.y1 * height
            val right = it.x2 * width
            val bottom = it.y2 * height

            // Desenha a caixa delimitadora
            canvas.drawRect(left, top, right, bottom, boxPaint)

            // Cria o texto com o nome do objeto e a cor
            val drawableText = "${it.clsName} - ${getColorName(it.color)}"

            // Calcula as dimensões do texto
            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()

            // Desenha o fundo do texto
            canvas.drawRect(
                left,
                top,
                left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                textBackgroundPaint
            )

            // Desenha o texto
            canvas.drawText(drawableText, left, top + bounds.height(), textPaint)
        }
    }

    fun setResults(boundingBoxes: List<BoundingBox>) {
        results = boundingBoxes
        invalidate()
    }

    fun getColorName(color: Int): String {
        // Utiliza a conversão RGB para Lab
        val labColor = rgbToLab(color)

        // Verifica a proximidade com cores conhecidas
        return when {
            isColorClose(labColor, Color.BLACK) -> "preto"
            isColorClose(labColor, Color.WHITE) -> "branco"
            isColorClose(labColor, Color.RED) -> "vermelho"
            isColorClose(labColor, Color.GREEN) -> "verde"
            isColorClose(labColor, Color.BLUE) -> "azul"
            isColorClose(labColor, Color.YELLOW) -> "amarelo"
            isColorClose(labColor, Color.rgb(255, 165, 0)) -> "laranja"  // Cor laranja
            isColorClose(labColor, Color.MAGENTA) -> "magenta"
            isColorClose(labColor, Color.CYAN) -> "ciano"
            isColorClose(labColor, Color.GRAY) -> "cinza"
            isColorClose(labColor, Color.LTGRAY) -> "cinza claro"
            isColorClose(labColor, Color.DKGRAY) -> "cinza escuro"
            else -> "desconhecido"
        }
    }

    private fun isColorClose(colorLab: FloatArray, referenceColor: Int): Boolean {
        // Converte a cor de referência para Lab
        val referenceLab = rgbToLab(referenceColor)

        // Calcula a distância euclidiana no espaço LAB
        val deltaL = colorLab[0] - referenceLab[0]
        val deltaA = colorLab[1] - referenceLab[1]
        val deltaB = colorLab[2] - referenceLab[2]
        val distance = sqrt(
            (deltaL * deltaL + deltaA * deltaA + deltaB * deltaB).toDouble()
        ).toFloat()

        // Define um limite de tolerância maior para a comparação
        val tolerance = 10.0f  // Ajuste a tolerância conforme necessário
        return distance < tolerance
    }

    // Converte uma cor RGB para o espaço de cor Lab
    private fun rgbToLab(color: Int): FloatArray {
        val r = Color.red(color) / 255.0f
        val g = Color.green(color) / 255.0f
        val b = Color.blue(color) / 255.0f

        val x = rgbToXyz(r, g, b)[0]
        val y = rgbToXyz(r, g, b)[1]
        val z = rgbToXyz(r, g, b)[2]

        return xyzToLab(x, y, z)
    }

    private fun rgbToXyz(r: Float, g: Float, b: Float): FloatArray {
        val x = r * 0.4124564f + g * 0.3575761f + b * 0.1804375f
        val y = r * 0.2126729f + g * 0.7151522f + b * 0.0721750f
        val z = r * 0.0193339f + g * 0.1191920f + b * 0.9503041f
        return floatArrayOf(x, y, z)
    }

    private fun xyzToLab(x: Float, y: Float, z: Float): FloatArray {
        val xNorm = x / 95.047f
        val yNorm = y / 100.000f
        val zNorm = z / 108.883f

        val l = if (yNorm > 0.008856) {
            116.0f * (yNorm.pow(1 / 3.0f)) - 16.0f
        } else {
            903.3f * yNorm
        }

        val a = if (xNorm > 0.008856) {
            500.0f * (xNorm.pow(1 / 3.0f) - yNorm.pow(1 / 3.0f))
        } else {
            500.0f * (xNorm / 0.12841855f - yNorm.pow(1 / 3.0f))
        }

        val b = if (zNorm > 0.008856) {
            200.0f * (yNorm.pow(1 / 3.0f) - zNorm.pow(1 / 3.0f))
        } else {
            200.0f * (yNorm.pow(1 / 3.0f) - zNorm / 0.12841855f)
        }

        return floatArrayOf(l, a, b)
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}
