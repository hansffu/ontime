package dev.hansffu.ontime.ui.boards

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.RevealValue
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SplitSwitchButton
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.SwipeToReveal
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.material3.rememberRevealState
import dev.hansffu.ontime.R
import dev.hansffu.ontime.database.dao.BoardDeparture
import dev.hansffu.ontime.model.BoardDistance
import dev.hansffu.ontime.ui.components.RemoteInputButton
import dev.hansffu.ontime.ui.components.listHeaderItem
import dev.hansffu.ontime.ui.components.messageItem
import dev.hansffu.ontime.viewmodels.BoardAssignmentViewModel
import dev.hansffu.ontime.viewmodels.BoardEditorViewModel
import dev.hansffu.ontime.viewmodels.BoardsViewModel
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun BoardsScreen(
    onBoardOpened: (Long) -> Unit,
    onBoardEdited: (Long) -> Unit,
    boardsViewModel: BoardsViewModel = hiltViewModel(),
) {
    val boards = boardsViewModel.boards.collectAsStateWithLifecycle().value
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val manageBoardsLabel = stringResource(R.string.manage_boards)
    val noBoardsLabel = stringResource(R.string.no_boards)

    ScreenScaffold(
        scrollState = columnState,
        edgeButton = {
            EdgeButton(onClick = { boardsViewModel.createBoard(onBoardEdited) }) {
                Icon(Icons.Default.Add, stringResource(R.string.create_board))
            }
        },
    ) { contentPadding ->
        TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {
            listHeaderItem(
                "boards-header",
                manageBoardsLabel,
                transformationSpec,
            )
            if (boards.isEmpty()) {
                messageItem(
                    "boards-empty",
                    noBoardsLabel,
                    transformationSpec,
                )
            }
            items(boards, key = { it.id }) { board ->
                val revealState = rememberRevealState(RevealValue.Covered)
                val coroutineScope = rememberCoroutineScope()
                val editAndClose: () -> Unit = {
                    onBoardEdited(board.id)
                    coroutineScope.launch {
                        revealState.animateTo(RevealValue.Covered)
                    }
                }
                SwipeToReveal(
                    modifier =
                        Modifier.fillMaxWidth()
                            .minimumVerticalContentPadding(
                                ButtonDefaults.minimumVerticalListContentPadding
                            )
                            .transformedHeight(this, transformationSpec),
                    revealState = revealState,
                    primaryAction = {
                        PrimaryActionButton(
                            onClick = editAndClose,
                            icon = {
                                Icon(
                                    Icons.Default.Edit,
                                    stringResource(R.string.edit_board),
                                )
                            },
                            text = { Text(stringResource(R.string.edit)) },
                        )
                    },
                    secondaryAction = {
                        SecondaryActionButton(
                            onClick = { boardsViewModel.deleteBoard(board.id) },
                            icon = {
                                Icon(
                                    Icons.Default.Delete,
                                    stringResource(R.string.delete_board),
                                )
                            },
                        )
                    },
                    onSwipePrimaryAction = editAndClose,
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onBoardOpened(board.id) },
                        transformation = SurfaceTransformation(transformationSpec),
                        label = { Text(board.name) },
                        secondaryLabel = {
                            Text(
                                when {
                                    board.distanceEnabled && board.timeEnabled ->
                                        stringResource(R.string.distance_and_time)
                                    board.distanceEnabled ->
                                        stringResource(R.string.distance_only)
                                    board.timeEnabled ->
                                        stringResource(R.string.time_only)
                                    else -> stringResource(R.string.board_inactive)
                                }
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun BoardEditorScreen(
    onConfigureDistance: () -> Unit,
    onConfigureTime: () -> Unit,
    onDeleted: () -> Unit,
    boardEditorViewModel: BoardEditorViewModel = hiltViewModel(),
) {
    val state = boardEditorViewModel.uiState.collectAsStateWithLifecycle().value
    val board = state.board ?: return
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val departuresLabel = stringResource(R.string.board_departures)
    val noDeparturesLabel = stringResource(R.string.no_board_departures)

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {
            listHeaderItem("board-name", board.name, transformationSpec)
            item("rename") {
                RemoteInputButton(
                    label = stringResource(R.string.board_name),
                    inputLabel = stringResource(R.string.board_name_prompt),
                    value = board.name,
                    onSubmit = boardEditorViewModel::rename,
                    modifier =
                        Modifier.fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                )
            }
            item("distance-condition") {
                SplitSwitchButton(
                    checked = board.distanceEnabled,
                    onCheckedChange = boardEditorViewModel::setDistanceEnabled,
                    toggleContentDescription =
                        stringResource(R.string.toggle_distance_requirement),
                    onContainerClick = onConfigureDistance,
                    modifier =
                        Modifier.fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    label = { Text(stringResource(R.string.activation_radius)) },
                    secondaryLabel = {
                        Text(
                            if (board.activationStopName != null) {
                                stringResource(
                                    R.string.distance_condition_configured,
                                    BoardDistance.fromMeters(
                                        board.maxDistanceMeters ?: 3_000
                                    ),
                                    board.activationStopName,
                                )
                            } else {
                                stringResource(R.string.distance_condition_unset)
                            }
                        )
                    },
                )
            }
            item("time-toggle") {
                SplitSwitchButton(
                    checked = board.timeEnabled,
                    onCheckedChange = boardEditorViewModel::setTimeEnabled,
                    toggleContentDescription =
                        stringResource(R.string.toggle_time_requirement),
                    onContainerClick = onConfigureTime,
                    modifier =
                        Modifier.fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    label = { Text(stringResource(R.string.time_condition)) },
                    secondaryLabel = {
                        Text(
                            stringResource(
                                R.string.time_condition_configured,
                                (board.startMinuteOfDay ?: 6 * 60).asTime(),
                                (board.endMinuteOfDay ?: 9 * 60).asTime(),
                            )
                        )
                    },
                )
            }
            listHeaderItem(
                "board-departures-header",
                departuresLabel,
                transformationSpec,
            )
            if (state.departures.isEmpty()) {
                messageItem(
                    "board-departures-empty",
                    noDeparturesLabel,
                    transformationSpec,
                )
            }
            items(
                state.departures,
                key = { it.stopId + "-" + it.lineRef + "-" + it.destinationRef },
            ) { departure ->
                DepartureEditorButton(
                    departure,
                    { boardEditorViewModel.removeDeparture(departure) },
                    SurfaceTransformation(transformationSpec),
                    Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                )
            }
            item("delete-board") {
                Button(
                    onClick = { boardEditorViewModel.delete(onDeleted) },
                    modifier =
                        Modifier.fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    colors = ButtonDefaults.filledVariantButtonColors(),
                    transformation = SurfaceTransformation(transformationSpec),
                    icon = { Icon(Icons.Default.Delete, null) },
                    label = { Text(stringResource(R.string.delete_board)) },
                )
            }
        }
    }
}

@Composable
fun BoardDistanceRequirementScreen(
    onPickStop: () -> Unit,
    onPickDistance: () -> Unit,
    boardEditorViewModel: BoardEditorViewModel = hiltViewModel(),
) {
    val board = boardEditorViewModel.uiState.collectAsStateWithLifecycle().value.board ?: return
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val distanceLabel = stringResource(R.string.activation_radius)

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {
            listHeaderItem(
                "distance-requirement-header",
                distanceLabel,
                transformationSpec,
            )
            item("activation-stop") {
                Button(
                    onClick = onPickStop,
                    modifier =
                        Modifier.fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    label = { Text(stringResource(R.string.activation_stop)) },
                    secondaryLabel = {
                        Text(
                            board.activationStopName
                                ?: stringResource(R.string.choose_activation_stop)
                        )
                    },
                )
            }
            item("activation-radius") {
                Button(
                    onClick = onPickDistance,
                    modifier =
                        Modifier.fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    label = { Text(stringResource(R.string.activation_radius)) },
                    secondaryLabel = {
                        Text(
                            stringResource(
                                R.string.kilometers_format,
                                BoardDistance.fromMeters(board.maxDistanceMeters ?: 3_000),
                            )
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun BoardTimeRequirementScreen(
    onPickStartTime: () -> Unit,
    onPickEndTime: () -> Unit,
    boardEditorViewModel: BoardEditorViewModel = hiltViewModel(),
) {
    val board = boardEditorViewModel.uiState.collectAsStateWithLifecycle().value.board ?: return
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val timeLabel = stringResource(R.string.time_condition)

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {
            listHeaderItem(
                "time-requirement-header",
                timeLabel,
                transformationSpec,
            )
            item("start-time") {
                Button(
                    onClick = onPickStartTime,
                    modifier =
                        Modifier.fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    label = { Text(stringResource(R.string.start_time)) },
                    secondaryLabel = {
                        Text((board.startMinuteOfDay ?: 6 * 60).asTime())
                    },
                )
            }
            item("end-time") {
                Button(
                    onClick = onPickEndTime,
                    modifier =
                        Modifier.fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    label = { Text(stringResource(R.string.end_time)) },
                    secondaryLabel = {
                        Text((board.endMinuteOfDay ?: 9 * 60).asTime())
                    },
                )
            }
        }
    }
}

@Composable
private fun DepartureEditorButton(
    departure: BoardDeparture,
    onRemove: () -> Unit,
    transformation: SurfaceTransformation,
    modifier: Modifier,
) {
    Button(
        onClick = onRemove,
        modifier = modifier,
        colors = ButtonDefaults.filledTonalButtonColors(),
        transformation = transformation,
        label = { Text(departure.lineRef + " · " + departure.destinationRef) },
        secondaryLabel = {
            Text(departure.stopName + " · " + stringResource(R.string.tap_to_remove))
        },
    )
}

@Composable
fun BoardAssignmentScreen(
    onCreateBoard: (Long) -> Unit,
    boardAssignmentViewModel: BoardAssignmentViewModel = hiltViewModel(),
) {
    val options = boardAssignmentViewModel.options.collectAsStateWithLifecycle().value
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val assignmentLabel = stringResource(R.string.add_to_board)
    val emptyAssignmentLabel = stringResource(R.string.no_boards_create)
    ScreenScaffold(
        scrollState = columnState,
        edgeButton = {
            EdgeButton(onClick = { boardAssignmentViewModel.createBoard(onCreateBoard) }) {
                Icon(Icons.Default.Add, stringResource(R.string.create_board))
            }
        },
    ) { contentPadding ->
        TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {
            listHeaderItem(
                "assignment-header",
                assignmentLabel,
                transformationSpec,
            )
            if (options.isEmpty()) {
                messageItem(
                    "assignment-empty",
                    emptyAssignmentLabel,
                    transformationSpec,
                )
            }
            items(options, key = { it.board.id }) { option ->
                CheckboxButton(
                    checked = option.selected,
                    onCheckedChange = {
                        boardAssignmentViewModel.setSelected(option.board.id, it)
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    label = { Text(option.board.name) },
                )
            }
        }
    }
}

private fun Int.asTime(): String =
    String.format(Locale.ROOT, "%02d:%02d", this / 60, this % 60)
