package hansffu.ontime.ui.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.Scaffold
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerScope
import com.google.accompanist.pager.rememberPagerState


@Composable
fun <P> Pager(
    pages: List<P>,
    onFocusChange: ((P) -> Unit),
    renderPage: @Composable PagerScope.(P) -> Unit
) {
    val pagerState = rememberPagerState()
    val pageIndicatorState: PageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = pagerState.currentPageOffset
            override val selectedPage: Int
                get() = pagerState.currentPage
            override val pageCount: Int
                get() = pagerState.pageCount
        }
    }
    Scaffold(
        positionIndicator = { HorizontalPageIndicator(pageIndicatorState) }
    ) {
        LaunchedEffect(pagerState.currentPage) {
            onFocusChange(pages[pagerState.currentPage])
        }
        HorizontalPager(count = pages.size, state = pagerState) { page ->
            renderPage(pages[page])
        }

    }
}
