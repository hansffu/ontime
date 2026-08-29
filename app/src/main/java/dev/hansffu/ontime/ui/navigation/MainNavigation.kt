package dev.hansffu.ontime.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.boards.BoardActivationStopScreen
import dev.hansffu.ontime.ui.boards.BoardAssignmentScreen
import dev.hansffu.ontime.ui.boards.BoardDistancePickerScreen
import dev.hansffu.ontime.ui.boards.BoardDistanceRequirementScreen
import dev.hansffu.ontime.ui.boards.BoardEditorScreen
import dev.hansffu.ontime.ui.boards.BoardTimetableScreen
import dev.hansffu.ontime.ui.boards.BoardTimePickerScreen
import dev.hansffu.ontime.ui.boards.BoardTimeRequirementScreen
import dev.hansffu.ontime.ui.boards.BoardsScreen
import dev.hansffu.ontime.ui.stoplist.StopsScreen
import dev.hansffu.ontime.ui.stoplist.nearby.NearbyStopsScreen
import dev.hansffu.ontime.ui.stoplist.search.SearchScreen
import dev.hansffu.ontime.ui.theme.OntimeTheme
import dev.hansffu.ontime.ui.timetable.TimetableUi
import dev.hansffu.ontime.viewmodels.BoardActivationStopViewModel
import dev.hansffu.ontime.viewmodels.BoardEditorViewModel
import dev.hansffu.ontime.viewmodels.TimetableViewModel

@Composable
fun MainNavigation(
    boardToOpen: Long? = null,
    onBoardOpened: () -> Unit = {},
) {
    OntimeTheme {
        val navController = rememberSwipeDismissableNavController()
        val currentEntry by navController.currentBackStackEntryAsState()
        LaunchedEffect(boardToOpen) {
            boardToOpen?.let { boardId ->
                navController.navigate(Screen.BoardTimetable(boardId).route()) {
                    launchSingleTop = true
                }
                onBoardOpened()
            }
        }
        val currentRoute = currentEntry?.destination?.route
        val pickerOpen =
            currentRoute == Screen.BoardTimePicker.route ||
                currentRoute == Screen.BoardDistancePicker.route
        AppScaffold(
            timeText = {
                if (!pickerOpen) TimeText()
            }
        ) {
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
                    BoardsScreen(
                        onBoardOpened = {
                            navController.navigate(Screen.BoardTimetable(it).route())
                        },
                        onBoardEdited = openBoardEditor,
                    )
                }
                composable(
                    route = Screen.BoardEditor.route,
                    arguments = listOf(navArgument("boardId") { type = NavType.LongType }),
                ) { entry ->
                    val boardId = checkNotNull(entry.arguments?.getLong("boardId"))
                    BoardEditorScreen(
                        onConfigureDistance = {
                            navController.navigate(
                                Screen.BoardDistanceRequirement(boardId).route()
                            )
                        },
                        onConfigureTime = {
                            navController.navigate(Screen.BoardTimeRequirement(boardId).route())
                        },
                        onDeleted = navController::popBackStack,
                    )
                }
                composable(
                    route = Screen.BoardDistanceRequirement.route,
                    arguments = listOf(navArgument("boardId") { type = NavType.LongType }),
                ) { entry ->
                    val boardId = checkNotNull(entry.arguments?.getLong("boardId"))
                    BoardDistanceRequirementScreen(
                        onPickStop = {
                            navController.navigate(Screen.BoardActivationStop(boardId).route())
                        },
                        onPickDistance = {
                            navController.navigate(Screen.BoardDistancePicker(boardId).route())
                        },
                    )
                }
                composable(
                    route = Screen.BoardDistancePicker.route,
                    arguments = listOf(navArgument("boardId") { type = NavType.LongType }),
                ) { entry ->
                    val editorViewModel = hiltViewModel<BoardEditorViewModel>(entry)
                    val board by editorViewModel.uiState.collectAsStateWithLifecycle()
                    BoardDistancePickerScreen(
                        initialDistanceMeters = board.board?.maxDistanceMeters ?: 3_000,
                        onDistanceConfirmed = { selectedKilometers ->
                            editorViewModel.setDistanceKilometers(
                                selectedKilometers,
                                navController::popBackStack,
                            )
                        },
                    )
                }
                composable(
                    route = Screen.BoardTimeRequirement.route,
                    arguments = listOf(navArgument("boardId") { type = NavType.LongType }),
                ) { entry ->
                    val boardId = checkNotNull(entry.arguments?.getLong("boardId"))
                    BoardTimeRequirementScreen(
                        onPickStartTime = {
                            navController.navigate(Screen.BoardTimePicker(boardId, true).route())
                        },
                        onPickEndTime = {
                            navController.navigate(Screen.BoardTimePicker(boardId, false).route())
                        },
                    )
                }
                composable(
                    route = Screen.BoardTimePicker.route,
                    arguments =
                        listOf(
                            navArgument("boardId") { type = NavType.LongType },
                            navArgument("timeField") { type = NavType.StringType },
                        ),
                ) { entry ->
                    val startTime = entry.arguments?.getString("timeField") == "start"
                    val editorViewModel = hiltViewModel<BoardEditorViewModel>(entry)
                    val board by editorViewModel.uiState.collectAsStateWithLifecycle()
                    val initialMinute =
                        if (startTime) board.board?.startMinuteOfDay ?: 6 * 60
                        else board.board?.endMinuteOfDay ?: 9 * 60
                    BoardTimePickerScreen(
                        title =
                            stringResource(
                                if (startTime) R.string.start_time else R.string.end_time
                            ),
                        initialMinuteOfDay = initialMinute,
                        onTimeConfirmed = { selected ->
                            if (startTime) {
                                editorViewModel.setStartTime(
                                    selected,
                                    navController::popBackStack,
                                )
                            } else {
                                editorViewModel.setEndTime(
                                    selected,
                                    navController::popBackStack,
                                )
                            }
                        },
                    )
                }
                composable(
                    route = Screen.BoardActivationStop.route,
                    arguments = listOf(navArgument("boardId") { type = NavType.LongType }),
                ) { entry ->
                    val selectionViewModel =
                        hiltViewModel<BoardActivationStopViewModel>(entry)
                    val selectionFailed by
                        selectionViewModel.selectionFailed.collectAsStateWithLifecycle()
                    BoardActivationStopScreen(
                        selectionFailed = selectionFailed,
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
