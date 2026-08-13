package com.donghyeon.mystock.design.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.R
import com.donghyeon.mystock.design.icon.MsIcons

@Composable
fun MsSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MyStockTheme.typography.numberSmall,
        color = MyStockTheme.colors.textTertiary,
        modifier = modifier.padding(top = MyStockTheme.spacing.sm),
    )
}

@Composable
fun MsInfoNote(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = MyStockTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = MsIcons.Info,
            contentDescription = null,
            tint = MyStockTheme.colors.textTertiary,
            modifier = Modifier
                .padding(top = MyStockTheme.spacing.xs)
                .size(MyStockTheme.iconSize.sm),
        )
        Text(
            text = text,
            style = MyStockTheme.typography.caption,
            color = MyStockTheme.colors.textTertiary,
        )
    }
}

@Composable
fun MsStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MyStockTheme.typography.caption,
            color = MyStockTheme.colors.textTertiary,
        )
        MsAmountText(text = value)
    }
}

// reserveSubtitle은 부제가 뒤늦게 오는 화면의 로딩 전환에 점프가 없게 한다
@Composable
fun MsBackTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    reserveSubtitle: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MyStockTheme.spacing.sm,
                    top = MyStockTheme.spacing.sm,
                    end = MyStockTheme.spacing.lg,
                    bottom = MyStockTheme.spacing.sm,
                ),
            horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(MyStockTheme.spacing.touchMin)
                    .clip(RoundedCornerShape(MyStockTheme.radius.full))
                    .clickable(role = Role.Button, onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MsIcons.ChevronLeft,
                    contentDescription = "뒤로",
                    tint = MyStockTheme.colors.textPrimary,
                    modifier = Modifier.size(MyStockTheme.iconSize.lg),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MyStockTheme.typography.title,
                    color = MyStockTheme.colors.textPrimary,
                )
                val caption = MyStockTheme.typography.caption
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = caption,
                        color = MyStockTheme.colors.textTertiary,
                    )
                } else if (reserveSubtitle) {
                    Spacer(modifier = Modifier.height(with(LocalDensity.current) { caption.lineHeight.toDp() }))
                }
            }
        }
        HorizontalDivider(thickness = MyStockTheme.stroke, color = MyStockTheme.colors.border)
    }
}

// 당겨서 새로고침이 되도록 스크롤 컨테이너에 담는다
@Composable
fun MsFailedContent(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            MsEmptyMessage(
                title = stringResource(R.string.ms_failed_title),
                description = stringResource(R.string.ms_failed_description),
                modifier = Modifier.fillParentMaxSize(),
            )
        }
    }
}

@Composable
fun MsEmptyCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    MsCard(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = MyStockTheme.spacing.lg, vertical = MyStockTheme.spacing.xl),
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MyStockTheme.typography.bodyMedium,
                color = MyStockTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
fun MsEmptyMessage(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MyStockTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = MyStockTheme.spacing.xs,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Text(
            text = title,
            style = MyStockTheme.typography.bodyMedium,
            color = MyStockTheme.colors.textPrimary,
        )
        Text(
            text = description,
            style = MyStockTheme.typography.caption,
            color = MyStockTheme.colors.textTertiary,
        )
    }
}

@PreviewThemes
@Composable
private fun TextsPreview() {
    MyStockTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MsBackTopBar(title = "버전 내역", subtitle = "이동평균 교차", onBack = {})
            MsSectionLabel("보유 종목 4")
            MsStat(label = "운용시간", value = "809h")
            MsInfoNote("위 종목 전부를 시장가로 즉시 매도하고 매매를 끝냅니다.")
            MsEmptyMessage(
                title = "서버에 연결할 수 없습니다",
                description = "아래로 당겨 다시 시도하세요",
            )
            MsEmptyCard("아직 운용한 적 없습니다")
        }
    }
}
