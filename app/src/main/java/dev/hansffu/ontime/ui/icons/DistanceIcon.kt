package dev.hansffu.ontime.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Material Symbols Outlined `distance`, 24dp, weight 400, grade 0, roundness 50. */
val DistanceIcon: ImageVector
    get() {
        if (cachedDistanceIcon != null) return cachedDistanceIcon!!
        cachedDistanceIcon =
            ImageVector.Builder(
                    name = "Distance",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(7.68f, 21.16f)
                        quadTo(6f, 20.33f, 6f, 19f)
                        quadTo(6f, 18.4f, 6.36f, 17.89f)
                        reflectiveQuadTo(7.38f, 17f)
                        lineToRelative(1.58f, 1.48f)
                        quadTo(8.73f, 18.58f, 8.46f, 18.7f)
                        reflectiveQuadTo(8.05f, 19f)
                        quadToRelative(0.32f, 0.4f, 1.5f, 0.7f)
                        reflectiveQuadTo(12f, 20f)
                        quadToRelative(1.28f, 0f, 2.46f, -0.3f)
                        reflectiveQuadTo(15.98f, 19f)
                        quadTo(15.8f, 18.8f, 15.53f, 18.68f)
                        reflectiveQuadTo(15f, 18.45f)
                        lineToRelative(1.55f, -1.5f)
                        quadToRelative(0.7f, 0.4f, 1.07f, 0.91f)
                        reflectiveQuadTo(18f, 19f)
                        quadToRelative(0f, 1.32f, -1.68f, 2.16f)
                        reflectiveQuadTo(12f, 22f)
                        reflectiveQuadTo(7.68f, 21.16f)
                        close()
                        moveTo(12.03f, 16.5f)
                        quadToRelative(2.47f, -1.83f, 3.72f, -3.66f)
                        reflectiveQuadTo(17f, 9.15f)
                        quadTo(17f, 6.6f, 15.38f, 5.3f)
                        reflectiveQuadTo(12f, 4f)
                        reflectiveQuadTo(8.63f, 5.3f)
                        reflectiveQuadTo(7f, 9.15f)
                        quadToRelative(0f, 1.68f, 1.22f, 3.49f)
                        reflectiveQuadToRelative(3.8f, 3.86f)
                        close()
                        moveTo(12f, 19f)
                        quadTo(8.48f, 16.4f, 6.74f, 13.95f)
                        reflectiveQuadTo(5f, 9.15f)
                        quadTo(5f, 7.38f, 5.64f, 6.04f)
                        reflectiveQuadTo(7.28f, 3.8f)
                        reflectiveQuadTo(9.53f, 2.45f)
                        reflectiveQuadTo(12f, 2f)
                        reflectiveQuadToRelative(2.48f, 0.45f)
                        reflectiveQuadTo(16.73f, 3.8f)
                        reflectiveQuadToRelative(1.64f, 2.24f)
                        quadTo(19f, 7.38f, 19f, 9.15f)
                        quadToRelative(0f, 2.35f, -1.74f, 4.8f)
                        quadTo(15.53f, 16.4f, 12f, 19f)
                        close()
                        moveToRelative(0f, -8f)
                        quadToRelative(0.83f, 0f, 1.41f, -0.59f)
                        reflectiveQuadTo(14f, 9f)
                        quadTo(14f, 8.17f, 13.41f, 7.59f)
                        reflectiveQuadTo(12f, 7f)
                        reflectiveQuadTo(10.59f, 7.59f)
                        reflectiveQuadTo(10f, 9f)
                        quadToRelative(0f, 0.82f, 0.59f, 1.41f)
                        reflectiveQuadTo(12f, 11f)
                        close()
                        moveTo(12f, 9f)
                        close()
                    }
                }
                .build()
        return cachedDistanceIcon!!
    }

private var cachedDistanceIcon: ImageVector? = null
