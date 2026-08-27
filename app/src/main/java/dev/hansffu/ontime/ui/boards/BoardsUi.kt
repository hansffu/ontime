package dev.hansffu.ontime.ui.boards

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
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
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import dev.hansffu.ontime.R
import dev.hansffu.ontime.database.dao.BoardDeparture
import dev.hansffu.ontime.ui.components.RemoteInputButton
import dev.hansffu.ontime.ui.components.listHeaderItem
import dev.hansffu.ontime.ui.components.messageItem
import dev.hansffu.ontime.viewmodels.BoardAssignmentViewModel
import dev.hansffu.ontime.viewmodels.BoardEditorViewModel
import dev.hansffu.ontime.viewmodels.BoardsViewModel
import java.util.Locale

@Composable
fun BoardsScreen(
    onBoardSelected: (Long) -> Unit,
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
            EdgeButton(onClick = { boardsViewModel.createBoard(onBoardSelected) }) {
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
                Button(
                    modifier =
                        Modifier.fillMaxWidth()
                            .minimumVerticalContentPadding(
                                ButtonDefaults.minimumVerticalListContentPadding
                            )
                            .transformedHeight(this, transformationSpec),
                    onClick = { onBoardSelected(board.id) },
                    transformation = SurfaceTransformation(transformationSpec),
                    label = { Text(board.name) },
                    secondaryLabel = {
                        Text(
                            when {
                                board.maxDistanceMeters != null &&
                                    board.startMinuteOfDay != null ->
                                    stringResource(R.string.distance_and_time)
                                board.maxDistanceMeters != null ->
                                    stringResource(R.string.distance_only)
                                board.startMinuteOfDay != null ->
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

@Composable
fun BoardEditorScreen(
    onSearchActivationStop: () -> Unit,
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
                Button(
                    onClick = onSearchActivationStop,
                    modifier =
                        Modifier.fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    label = {
                        Text(
                            if (board.activationStopName != null &&
                                board.maxDistanceMeters != null
                            ) {
                                stringResource(
                                    R.string.distance_condition_configured,
                                    board.maxDistanceMeters.asKilometers(),
                                    board.activationStopName,
                                )
                            } else {
                                stringResource(R.string.distance_condition_unset)
                            }
                        )
                    },
                )
            }
            if (board.activationStopName != null && board.maxDistanceMeters != null) {
                item("activation-radius") {
                    RemoteInputButton(
                        label = stringResource(R.string.activation_radius),
                        inputLabel = stringResource(R.string.radius_prompt),
                        value =
                            stringResource(
                                R.string.kilometers_format,
                                board.maxDistanceMeters / 1_000.0,
                            ),
                        onSubmit = boardEditorViewModel::setDistanceKilometers,
                        modifier =
                            Modifier.fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    )
                }
            }
            item("time-toggle") {
                CheckboxButton(
                    checked = board.startMinuteOfDay != null,
                    onCheckedChange = boardEditorViewModel::setTimeEnabled,
                    modifier =
                        Modifier.fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    label = { Text(stringResource(R.string.time_condition)) },
                )
            }
            if (board.startMinuteOfDay != null && board.endMinuteOfDay != null) {
                item("start-time") {
                    RemoteInputButton(
                        label = stringResource(R.string.start_time),
                        inputLabel = stringResource(R.string.time_prompt),
                        value = board.startMinuteOfDay.asTime(),
                        onSubmit = boardEditorViewModel::setStartTime,
                        modifier =
                            Modifier.fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    )
                }
                item("end-time") {
                    RemoteInputButton(
                        label = stringResource(R.string.end_time),
                        inputLabel = stringResource(R.string.time_prompt),
                        value = board.endMinuteOfDay.asTime(),
                        onSubmit = boardEditorViewModel::setEndTime,
                        modifier =
                            Modifier.fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    )
                }
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

private fun Int.asKilometers(): String =
    if (this % 1_000 == 0) (this / 1_000).toString()
    else String.format(Locale.getDefault(), "%.1f", this / 1_000.0)

private fun Int.asTime(): String =
    String.format(Locale.ROOT, "%02d:%02d", this / 60, this % 60)
