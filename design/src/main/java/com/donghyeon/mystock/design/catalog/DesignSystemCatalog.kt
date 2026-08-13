package com.donghyeon.mystock.design.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.donghyeon.mystock.design.MyStockTheme
import com.donghyeon.mystock.design.component.KeyAuthChip
import com.donghyeon.mystock.design.component.MsAccountTopBar
import com.donghyeon.mystock.design.component.MsBackTopBar
import com.donghyeon.mystock.design.component.MsBanner
import com.donghyeon.mystock.design.component.MsCard
import com.donghyeon.mystock.design.component.MsChip
import com.donghyeon.mystock.design.component.MsCondition
import com.donghyeon.mystock.design.component.MsDropdownChip
import com.donghyeon.mystock.design.component.MsEmptyCard
import com.donghyeon.mystock.design.component.MsEmptyMessage
import com.donghyeon.mystock.design.component.MsHoldingRow
import com.donghyeon.mystock.design.component.MsInfoNote
import com.donghyeon.mystock.design.component.MsListCard
import com.donghyeon.mystock.design.component.MsListPnlRow
import com.donghyeon.mystock.design.component.MsListRow
import com.donghyeon.mystock.design.component.MsOrderRow
import com.donghyeon.mystock.design.component.MsPrimaryButton
import com.donghyeon.mystock.design.component.MsQuickAction
import com.donghyeon.mystock.design.component.MsQuickActionSkeleton
import com.donghyeon.mystock.design.component.MsRunRow
import com.donghyeon.mystock.design.component.MsSecondaryButton
import com.donghyeon.mystock.design.component.MsSectionLabel
import com.donghyeon.mystock.design.component.MsSettingRow
import com.donghyeon.mystock.design.component.MsSheetActions
import com.donghyeon.mystock.design.component.MsSkeletonChip
import com.donghyeon.mystock.design.component.MsSkeletonText
import com.donghyeon.mystock.design.component.MsSnackbar
import com.donghyeon.mystock.design.component.MsStat
import com.donghyeon.mystock.design.component.MsStrategyCard
import com.donghyeon.mystock.design.component.MsSummaryCard
import com.donghyeon.mystock.design.component.MsTextField
import com.donghyeon.mystock.design.component.MsToggle
import com.donghyeon.mystock.design.component.MsTopBarAction
import com.donghyeon.mystock.design.component.OrderSideChip
import com.donghyeon.mystock.design.component.PnlFormat
import com.donghyeon.mystock.design.component.PnlPairText
import com.donghyeon.mystock.design.component.PnlText
import com.donghyeon.mystock.design.component.StatusChip
import com.donghyeon.mystock.design.icon.MsIcons
import com.donghyeon.mystock.design.model.AccountState
import com.donghyeon.mystock.design.model.OrderSide
import com.donghyeon.mystock.design.model.RunOutcome

enum class CatalogPage(val key: String, val title: String) {
    TOKEN("token", "토큰"),
    CONTROL("control", "컨트롤"),
    FEEDBACK("feedback", "안내"),
    CARD("card", "카드 · 목록"),
}

@Composable
fun DesignSystemCatalog(page: CatalogPage, modifier: Modifier = Modifier) {
    // 카탈로그 골든이 페이지 전체를 담도록 지연 목록 대신 한 번에 그린다
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MyStockTheme.colors.background)
            .padding(MyStockTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xl),
    ) {
        Text(
            "MyStock 디자인 시스템 · ${page.title}",
            style = MyStockTheme.typography.title,
            color = MyStockTheme.colors.textPrimary,
        )
        when (page) {
            CatalogPage.TOKEN -> TokenPage()
            CatalogPage.CONTROL -> ControlPage()
            CatalogPage.FEEDBACK -> FeedbackPage()
            CatalogPage.CARD -> CardPage()
        }
    }
}

@Composable
private fun ColumnScope.TokenPage() {
    val colors = MyStockTheme.colors
    val space = MyStockTheme.spacing

    Section("표면 · 텍스트") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(space.sm)) {
            Swatch("background", colors.background)
            Swatch("surface", colors.surface)
            Swatch("surfaceElevated", colors.surfaceElevated)
            Swatch("border", colors.border)
            Swatch("textPrimary", colors.textPrimary)
            Swatch("textSecondary", colors.textSecondary)
            Swatch("textTertiary", colors.textTertiary)
        }
    }

    Section("브랜드 · 등락 · 상태") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(space.sm)) {
            Swatch("accent", colors.accent)
            Swatch("priceUp", colors.priceUp)
            Swatch("priceDown", colors.priceDown)
            Swatch("success", colors.success)
            Swatch("warning", colors.warning)
            Swatch("danger", colors.danger)
        }
    }

    Section("타이포그래피") {
        Column(verticalArrangement = Arrangement.spacedBy(space.sm)) {
            TypeRow("title", MyStockTheme.typography.title, "화면 제목")
            TypeRow("subtitle", MyStockTheme.typography.subtitle, "카드 제목")
            TypeRow("body", MyStockTheme.typography.body, "본문 텍스트")
            TypeRow("bodyMedium", MyStockTheme.typography.bodyMedium, "종목 · 항목 이름")
            TypeRow("label", MyStockTheme.typography.label, "배지 · 라벨")
            TypeRow("caption", MyStockTheme.typography.caption, "보조 설명 · 캡션")
            TypeRow("numberLarge", MyStockTheme.typography.numberLarge, "6,018,000")
            TypeRow("number", MyStockTheme.typography.number, "1,109,300 · 17h")
            TypeRow("numberSmall", MyStockTheme.typography.numberSmall, "▲ 28,000 (+4.09%)")
            TypeRow("numberCaption", MyStockTheme.typography.numberCaption, "10주 · 68,400")
        }
    }
}

@Composable
private fun ColumnScope.ControlPage() {
    val space = MyStockTheme.spacing

    Section("칩") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(space.sm),
            verticalArrangement = Arrangement.spacedBy(space.sm),
        ) {
            StatusChip(AccountState.RUNNING)
            StatusChip(AccountState.CLOSING)
            StatusChip(AccountState.LIQUIDATING)
            StatusChip(AccountState.PAUSED)
            StatusChip(AccountState.WAITING)
            KeyAuthChip(authenticated = true)
            KeyAuthChip(authenticated = false)
            OrderSideChip(OrderSide.BUY)
            OrderSideChip(OrderSide.SELL)
            MsChip("KR")
            MsChip("US")
            MsChip("청산")
            MsChip("회수")
            MsChip("v0.2.0", selected = true, tabular = true)
            MsDropdownChip("전체 계좌", selected = false, onClick = {})
            MsDropdownChip("토스증권", selected = true, onClick = {})
        }
    }

    Section("등락 표기") {
        Column(verticalArrangement = Arrangement.spacedBy(space.xs)) {
            PnlText(3.42)
            PnlText(-1.08)
            PnlText(0.0)
            PnlText(128400.0, format = PnlFormat.AMOUNT)
            PnlText(-9300.0, format = PnlFormat.AMOUNT)
            PnlPairText(amount = 535000.0, percent = 9.76)
            PnlPairText(amount = -69000.0, percent = -5.58)
            PnlPairText(amount = 0.0, percent = 0.0)
        }
    }

    Section("버튼") {
        Column(verticalArrangement = Arrangement.spacedBy(space.sm)) {
            Row(horizontalArrangement = Arrangement.spacedBy(space.sm)) {
                MsPrimaryButton("시작", {})
                MsSecondaryButton("취소", {})
            }
            MsSheetActions(confirmText = "청산", onConfirm = {}, onCancel = {})
        }
    }

    Section("토글") {
        Row(horizontalArrangement = Arrangement.spacedBy(space.sm)) {
            MsToggle(checked = true, onCheckedChange = {})
            MsToggle(checked = false, onCheckedChange = {})
        }
    }

    Section("입력") {
        Column(verticalArrangement = Arrangement.spacedBy(space.sm)) {
            MsTextField(state = rememberTextFieldState(), label = "key-1", placeholder = "키를 붙여 넣으세요")
            MsTextField(state = rememberTextFieldState("abcd-1234-efgh"), label = "key-2")
        }
    }

    Section("아이콘") {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(space.md),
            verticalArrangement = Arrangement.spacedBy(space.md),
        ) {
            CatalogIcon("전략", MsIcons.Strategy)
            CatalogIcon("설정", MsIcons.Settings)
            CatalogIcon("성과", MsIcons.Performance)
            CatalogIcon("일시정지", MsIcons.Pause)
            CatalogIcon("재개", MsIcons.Resume)
            CatalogIcon("회수", MsIcons.Recall)
            CatalogIcon("청산", MsIcons.Liquidate)
            CatalogIcon("알림 켜짐", MsIcons.BellFilled)
            CatalogIcon("알림 꺼짐", MsIcons.Bell)
            CatalogIcon("안내", MsIcons.Info)
            CatalogIcon("매수", MsIcons.Buy)
            CatalogIcon("매도", MsIcons.Sell)
            CatalogIcon("닫기", MsIcons.Close)
            CatalogIcon("뒤로", MsIcons.ChevronLeft)
            CatalogIcon("펼치기", MsIcons.ChevronDown)
            CatalogIcon("이동", MsIcons.ChevronRight)
        }
    }
}

@Composable
private fun ColumnScope.FeedbackPage() {
    val space = MyStockTheme.spacing

    Section("스켈레톤") {
        Column(verticalArrangement = Arrangement.spacedBy(space.sm)) {
            MsSkeletonText(style = MyStockTheme.typography.label, chars = 4)
            MsSkeletonText(style = MyStockTheme.typography.numberLarge, chars = 6)
            MsSkeletonText(style = MyStockTheme.typography.body, chars = 8)
            MsSkeletonChip(chars = 4)
            MsCard { Row { repeat(4) { MsQuickActionSkeleton() } } }
        }
    }

    Section("배너 · 스낵바") {
        Column(verticalArrangement = Arrangement.spacedBy(space.sm)) {
            MsBanner(
                message = "서버가 일시정지했습니다 — 급격한 변동성 감지",
                onDismiss = {},
            )
            MsSnackbar(message = "요청을 보내지 못했습니다")
        }
    }

    Section("상단바") {
        Column(verticalArrangement = Arrangement.spacedBy(space.md)) {
            MsAccountTopBar(accountName = "토스증권", onAccountClick = {}) {
                MsTopBarAction(icon = MsIcons.Strategy, contentDescription = "전략", onClick = {})
                MsTopBarAction(icon = MsIcons.Settings, contentDescription = "설정", onClick = {})
            }
            MsAccountTopBar(accountName = null, onAccountClick = {})
            MsBackTopBar(title = "버전 내역", subtitle = "이동평균 교차", onBack = {})
        }
    }

    Section("안내 · 빈 화면") {
        Column(verticalArrangement = Arrangement.spacedBy(space.md)) {
            MsInfoNote("위 종목 전부를 시장가로 즉시 매도하고 매매를 끝냅니다.")
            MsEmptyMessage(
                title = "서버에 연결할 수 없습니다",
                description = "아래로 당겨 다시 시도하세요",
            )
            MsEmptyCard("아직 운용한 적 없습니다")
        }
    }
}

@Composable
private fun ColumnScope.CardPage() {
    val colors = MyStockTheme.colors
    val space = MyStockTheme.spacing

    Section("퀵 액션") {
        MsCard {
            Row {
                MsQuickAction(MsIcons.Pause, "일시정지", {})
                MsQuickAction(MsIcons.Recall, "회수", {})
                MsQuickAction(MsIcons.Liquidate, "청산", {})
                MsQuickAction(MsIcons.BellFilled, "알림", {})
            }
        }
    }

    Section("목록 카드") {
        Column(verticalArrangement = Arrangement.spacedBy(space.sm)) {
            MsSectionLabel("보유 종목 2")
            MsListCard(
                rows = listOf(
                    {
                        MsHoldingRow(
                            name = "삼성전자",
                            currentPrice = "71,200",
                            position = "10주 · 68,400",
                            valuation = "712,000",
                            pnlAmount = 28_000.0,
                            pnlPercent = 4.09,
                        )
                    },
                    {
                        MsHoldingRow(
                            name = "MSFT",
                            currentPrice = "\$394.20",
                            position = "3주 · \$402.60",
                            valuation = "1,587,641",
                            pnlAmount = -33_831.0,
                            pnlPercent = -2.09,
                        )
                    },
                    { MsListPnlRow("평가 손익", 33_370.0, 0.78) },
                    { MsListRow("평가금액 합계", "4,326,259") },
                ),
            )
            MsSectionLabel("주문 내역 12")
            MsListCard(
                rows = listOf(
                    { MsOrderRow("삼성전자", OrderSide.BUY, "10주 · 68,400", "684,000") },
                    {
                        MsOrderRow(
                            name = "NAVER",
                            side = OrderSide.SELL,
                            position = "4주 · 196,000",
                            amount = "784,000",
                            realizedPnlAmount = 0.0,
                            realizedPnlPercent = 0.0,
                        )
                    },
                ),
            )
            MsSectionLabel("운용 내역 2")
            MsListCard(
                rows = listOf(
                    {
                        MsRunRow(
                            name = "이동평균 교차",
                            version = "v0.2.0",
                            outcome = RunOutcome.RUNNING,
                            accountName = "토스증권",
                            hours = "17h",
                            pnlAmount = 983_259.0,
                            pnlPercent = 17.93,
                        )
                    },
                    {
                        MsRunRow(
                            name = "변동성 돌파",
                            version = "v0.3.1",
                            outcome = RunOutcome.CLOSED,
                            hours = "288h",
                            pnlAmount = -128_500.0,
                            pnlPercent = -2.41,
                        )
                    },
                ),
            )
            MsListCard(
                rows = listOf(
                    { MsSettingRow(label = "알림", checked = true, onCheckedChange = {}) },
                    { MsSettingRow(label = "오픈소스 라이선스", onClick = {}) },
                    { MsSettingRow(label = "버전", value = "1.0.0") },
                ),
            )
        }
    }

    Section("전략 카드") {
        Column(verticalArrangement = Arrangement.spacedBy(space.md)) {
            MsStrategyCard(
                name = "이동평균 교차",
                version = "v0.2.0",
                description = "단기·장기 이동평균 교차로 매수·매도",
                markets = listOf("KR", "US"),
                conditions = sampleConditions,
                onClick = {},
            )
            MsStrategyCard(
                name = "이동평균 교차",
                version = "v0.2.0",
                description = "단기·장기 이동평균 교차로 매수·매도",
                markets = listOf("KR", "US"),
                conditions = sampleConditions,
                note = "5일 이동평균이 20일 이동평균을 위로 지나면 추세 시작으로 보고 매수합니다.",
            )
        }
    }

    Section("카드 · 합산") {
        Column(verticalArrangement = Arrangement.spacedBy(space.md)) {
            MsCard(selected = true) {
                Text(
                    "선택된 카드",
                    style = MyStockTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                )
            }
            MsCard {
                Row(horizontalArrangement = Arrangement.spacedBy(space.sm)) {
                    MsStat(label = "운용시간", value = "809h", modifier = Modifier.weight(1f))
                    MsStat(label = "승률", value = "100%", modifier = Modifier.weight(1f))
                }
            }
            MsSummaryCard(
                label = "합산 손익",
                amount = 1_575_259.0,
                stats = listOf(
                    "운용시간" to "809h",
                    "승률" to "100%",
                    "중위 수익률" to "+7.80%",
                    "최고 수익률" to "+17.93%",
                    "최저 수익률" to "+3.20%",
                    "평균 수익률" to "+9.64%",
                ),
            )
        }
    }
}

private val sampleConditions = listOf(
    MsCondition("매수", "5일선이 20일선을 상향 돌파"),
    MsCondition("매도", "5일선이 20일선을 하향 돌파"),
    MsCondition("대상", "국내·미국 대형주"),
)

@Composable
private fun CatalogIcon(name: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MyStockTheme.colors.textSecondary,
            modifier = Modifier.size(MyStockTheme.iconSize.md),
        )
        Text(
            name,
            style = MyStockTheme.typography.label,
            color = MyStockTheme.colors.textTertiary,
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md)) {
        Text(
            title,
            style = MyStockTheme.typography.label,
            color = MyStockTheme.colors.textTertiary,
        )
        content()
    }
}

@Composable
private fun Swatch(name: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = MyStockTheme.spacing.sm),
    ) {
        SwatchBox(color)
        Text(
            name,
            style = MyStockTheme.typography.label,
            color = MyStockTheme.colors.textTertiary,
            modifier = Modifier.padding(top = MyStockTheme.spacing.xs),
        )
    }
}

@Composable
private fun SwatchBox(color: Color) {
    Box(
        Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(MyStockTheme.radius.md))
            .background(color)
            .border(MyStockTheme.stroke, MyStockTheme.colors.border, RoundedCornerShape(MyStockTheme.radius.md)),
    )
}

@Composable
private fun TypeRow(name: String, style: TextStyle, sample: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MyStockTheme.spacing.md),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            name,
            style = MyStockTheme.typography.label,
            color = MyStockTheme.colors.textTertiary,
            modifier = Modifier.weight(0.35f),
        )
        Text(
            sample,
            style = style,
            color = MyStockTheme.colors.textPrimary,
            modifier = Modifier.weight(0.65f),
        )
    }
}

private class CatalogPageProvider : PreviewParameterProvider<CatalogPage> {
    override val values = CatalogPage.entries.asSequence()
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CatalogDarkPreview(@PreviewParameter(CatalogPageProvider::class) page: CatalogPage) {
    MyStockTheme(darkTheme = true) {
        DesignSystemCatalog(page)
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CatalogLightPreview(@PreviewParameter(CatalogPageProvider::class) page: CatalogPage) {
    MyStockTheme(darkTheme = false) {
        DesignSystemCatalog(page)
    }
}
