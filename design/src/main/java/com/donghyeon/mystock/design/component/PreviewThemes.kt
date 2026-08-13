package com.donghyeon.mystock.design.component

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.donghyeon.mystock.design.token.DARK_BACKGROUND_ARGB
import com.donghyeon.mystock.design.token.LIGHT_BACKGROUND_ARGB

@Preview(
    name = "다크",
    showBackground = true,
    backgroundColor = DARK_BACKGROUND_ARGB,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    name = "라이트",
    showBackground = true,
    backgroundColor = LIGHT_BACKGROUND_ARGB,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
annotation class PreviewThemes
