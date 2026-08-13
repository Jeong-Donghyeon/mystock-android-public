package com.donghyeon.mystock.design.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private const val STROKE_WIDTH = 1.8f

object MsIcons {

    val Buy: ImageVector by lazy {
        strokeIcon("Buy", "M12 17V3", "m6 11 6 6 6-6", "M19 21H5")
    }

    val Sell: ImageVector by lazy {
        strokeIcon("Sell", "m18 9-6-6-6 6", "M12 3v14", "M5 21h14")
    }

    val Close: ImageVector by lazy {
        strokeIcon("Close", "M18 6 6 18", "m6 6 12 12")
    }

    val Bell: ImageVector by lazy {
        strokeIcon(
            "Bell",
            "M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9",
            "M13.73 21a2 2 0 0 1-3.46 0",
        )
    }

    val BellFilled: ImageVector by lazy {
        iconBuilder("BellFilled")
            .addPath(
                pathData = addPathNodes("M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"),
                fill = SolidColor(Color.White),
                stroke = SolidColor(Color.White),
                strokeLineWidth = STROKE_WIDTH,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            .addPath(
                pathData = addPathNodes("M13.73 21a2 2 0 0 1-3.46 0"),
                stroke = SolidColor(Color.White),
                strokeLineWidth = STROKE_WIDTH,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            .build()
    }

    val ChevronDown: ImageVector by lazy {
        strokeIcon("ChevronDown", "m6 9 6 6 6-6")
    }

    val ChevronRight: ImageVector by lazy {
        strokeIcon("ChevronRight", "m9 18 6-6-6-6")
    }

    val ChevronLeft: ImageVector by lazy {
        strokeIcon("ChevronLeft", "m15 18-6-6 6-6")
    }

    val Strategy: ImageVector by lazy {
        strokeIcon(
            "Strategy",
            "m12.83 2.18a2 2 0 0 0-1.66 0L2.6 6.08a1 1 0 0 0 0 1.83l8.58 3.91a2 2 0 0 0 1.66 0" +
                "l8.58-3.9a1 1 0 0 0 0-1.83Z",
            "m22 17.65-9.17 4.16a2 2 0 0 1-1.66 0L2 17.65",
            "m22 12.65-9.17 4.16a2 2 0 0 1-1.66 0L2 12.65",
        )
    }

    val Settings: ImageVector by lazy {
        strokeIcon(
            "Settings",
            "M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08" +
                "a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51" +
                "a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08" +
                "a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18" +
                "a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39" +
                "a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09" +
                "a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25" +
                "a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z",
            "M12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z",
        )
    }

    val Performance: ImageVector by lazy {
        strokeIcon(
            "Performance",
            "M3 3v16a2 2 0 0 0 2 2h16",
            "M18 17V9",
            "M13 17V5",
            "M8 17v-3",
        )
    }

    val Pause: ImageVector by lazy {
        strokeIcon(
            "Pause",
            "M15 4h2a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1h-2a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z",
            "M7 4h2a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z",
        )
    }

    val Resume: ImageVector by lazy {
        strokeIcon("Resume", "M6 3v18l14-9z")
    }

    val Recall: ImageVector by lazy {
        strokeIcon(
            "Recall",
            "M3 3h18a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z",
            "M4 8v11a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8",
            "M10 12h4",
        )
    }

    val Liquidate: ImageVector by lazy {
        strokeIcon(
            "Liquidate",
            "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z",
            "m15 9-6 6",
            "m9 15 6-6",
        )
    }

    val Info: ImageVector by lazy {
        strokeIcon(
            "Info",
            "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z",
            "M12 8v4",
            "M12 16h.01",
        )
    }

    private fun iconBuilder(name: String) = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )

    private fun strokeIcon(name: String, vararg pathData: String): ImageVector =
        iconBuilder(name).apply {
            pathData.forEach { data ->
                addPath(
                    pathData = addPathNodes(data),
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = STROKE_WIDTH,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()
}
