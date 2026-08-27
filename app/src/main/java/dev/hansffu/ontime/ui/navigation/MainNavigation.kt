package dev.hansffu.ontime.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.boards.BoardActivationStopScreen
import dev.hansffu.ontime.ui.boards.BoardAssignmentScreen
import dev.hansffu.ontime.ui.boards.BoardEditorScreen
import dev.hansffu.ontime.ui.boards.BoardTimetableScreen
import dev.hansffu.ontime.ui.boards.BoardsScreen
import dev.hansffu.ontime.ui.stoplist.StopsScreen
import dev.hansffu.ontime.ui.stoplist.nearby.NearbyStopsScreen
import dev.hansffu.ontime.ui.stoplist.search.SearchScreen
import dev.hansffu.ontime.ui.theme.OntimeTheme
import dev.hansffu.ontime.ui.timetable.TimetableUi
import dev.hansffu.ontime.viewmodels.BoardActivationStopViewModel
import dev.hansffu.ontime.viewmodels.TimetableViewModel

@Composable
fun MainNavigation() {
    OntimeTheme {
        AppScaffold {
            val navController = rememberSwipeDismissableNavController()
            val openTimetable = { stop: Stop ->
                navController.navigate(Screen.Timetable(stop).route())
            }
            val openBoardEditor = { boardId: Long ->
                navController.navigate(Screen.BoardEditor(boardId).route())
            }

            SwipeDismissableNavHost(
                navController = navController,
                startDestination = Screen.Favorites.route,
            ) {
                composable(Screen.Favorites.route) {
                    StopsScreen(
                        onSearch = { navController.navigate(Screen.TextSearch(it).route()) },
                        onNearby = { navController.navigate(Screen.Nearby.route) },
                        onStopSelected = openTimetable,
                        onBoardSelected = {
                            navController.navigate(Screen.BoardTimetable(it).route())
                        },
                        onManageBoards = {
                            navController.navigate(Screen.Boards.route)
                        },
                    )
                }
                composable(Screen.Boards.route) {
                    BoardsScreen(onBoardSelected = openBoardEditor)
                }
                composable(
                    route = Screen.BoardEditor.route,
                    arguments = listOf(navArgument("boardId") { type = NavType.LongType }),
                ) { entry ->
                    val boardId = checkNotNull(entry.arguments?.getLong("boardId"))
                    BoardEditorScreen(
                        onSearchActivationStop = {
                            navController.navigate(Screen.BoardActivationStop(boardId).route())
                        },
                        onDeleted = navController::popBackStack,
                    )
                }
                composable(
                    route = Screen.BoardActivationStop.route,
                    arguments = listOf(navArgument("boardId") { type = NavType.LongType }),
                ) { entry ->
                    val selectionViewModel =
                        hiltViewModel<BoardActivationStopViewModel>(entry)
                    BoardActivationStopScreen(
                        onStopSelected = {
                            selectionViewModel.select(it, navController::popBackStack)
                        }
                    )
                }
                composable(
                    route = Screen.BoardTimetable.route,
                    arguments = listOf(navArgument("boardId") { type = NavType.LongType }),
                ) {
                    BoardTimetableScreen(
                        onManageBoards = {
                            navController.navigate(Screen.BoardAssignment(it).route())
                        }
                    )
                }
                composable(
                    route = Screen.BoardAssignment.route,
                    arguments =
                        listOf(
                            navArgument("stopId") { type = NavType.StringType },
                            navArgument("stopName") { type = NavType.StringType },
                            navArgument("latitude") { type = NavType.StringType },
                            navArgument("longitude") { type = NavType.StringType },
                            navArgument("lineRef") { type = NavType.StringType },
                            navArgument("destinationRef") { type = NavType.StringType },
                        ),
                ) {
                    BoardAssignmentScreen(onCreateBoard = openBoardEditor)
                }
                composable(Screen.Nearby.route) {
                    NearbyStopsScreen(
                        onStopSelected = openTimetable,
                        onDismissPermission = navController::popBackStack,
                    )
                }
                composable(
                    route = Screen.TextSearch.route,
                    arguments = listOf(navArgument("searchString") { type = NavType.StringType }),
                ) { entry ->
                    SearchScreen(
                        searchString = entry.arguments?.getString("searchString").orEmpty(),
                        onStopSelected = openTimetable,
                    )
                }
                composable(
                    route = Screen.Timetable.route,
                    arguments =
                        listOf(
                            navArgument("stopId") { type = NavType.StringType },
                            navArgument("stopName") { type = NavType.StringType },
                        ),
                ) { entry ->
                    val viewModel = hiltViewModel<TimetableViewModel>(entry)
                    TimetableUi(
                        timetableViewModel = viewModel,
                        onManageBoards = { stop, line ->
                            navController.navigate(Screen.BoardAssignment(stop, line).route())
                        },
                    )
                }
            }
        }
    }
}
