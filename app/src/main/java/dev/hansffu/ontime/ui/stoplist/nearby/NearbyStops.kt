package dev.hansffu.ontime.ui.stoplist.nearby

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import dev.hansffu.ontime.R
import dev.hansffu.ontime.model.Stop
import dev.hansffu.ontime.ui.components.RefreshOnResume
import dev.hansffu.ontime.ui.components.listHeaderItem
import dev.hansffu.ontime.ui.components.messageItem
import dev.hansffu.ontime.ui.components.retryItem
import dev.hansffu.ontime.ui.components.stopListSection

@Composable
fun NearbyStopsScreen(
    onStopSelected: (Stop) -> Unit,
    onDismissPermission: () -> Unit,
    nearbyViewModel: NearbyViewModel = hiltViewModel(),
) {
    val nearbyStopState = nearbyViewModel.nearbyStopState.collectAsStateWithLifecycle().value
    RefreshOnResume(nearbyViewModel::refresh)
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val header = stringResource(R.string.nearby_header)
    val loading = stringResource(R.string.loading_nearby)
    val error = stringResource(R.string.nearby_error)
    val retry = stringResource(R.string.retry)
    val empty = stringResource(R.string.no_stops_found)

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {
            when (nearbyStopState) {
                NearbyStopState.Loading -> {
                    listHeaderItem("nearby-header", header, transformationSpec)
                    messageItem("nearby-loading", loading, transformationSpec)
                }

                NearbyStopState.NoPermission -> Unit

                NearbyStopState.Error -> {
                    listHeaderItem("nearby-header", header, transformationSpec)
                    retryItem(
                        "nearby-error",
                        error,
                        retry,
                        transformationSpec,
                        nearbyViewModel::refresh,
                    )
                }

                is NearbyStopState.Content -> {
                    if (nearbyStopState.stops.isEmpty()) {
                        listHeaderItem("nearby-header", header, transformationSpec)
                        messageItem("nearby-empty", empty, transformationSpec)
                    } else {
                        stopListSection(
                            headerKey = "nearby",
                            header = header,
                            stops = nearbyStopState.stops,
                            transformationSpec = transformationSpec,
                            onStopClick = onStopSelected,
                        )
                    }
                    if (nearbyStopState.refreshFailed) {
                        retryItem(
                            "nearby-refresh-error",
                            error,
                            retry,
                            transformationSpec,
                            nearbyViewModel::refresh,
                        )
                    }
                }
            }
        }
    }

    if (nearbyStopState == NearbyStopState.NoPermission) {
        LocationPermissionPrompt(
            permissions = nearbyViewModel.locationPermissions,
            onPermissionAvailable = nearbyViewModel::refresh,
            onDismiss = onDismissPermission,
        )
    }
}

@Composable
internal fun LocationPermissionPrompt(
    permissions: List<String>,
    onPermissionAvailable: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var requestAttempted by rememberSaveable { mutableStateOf(false) }
    val transformationSpec = rememberTransformationSpec()

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            if (results.values.any { it }) onPermissionAvailable() else requestAttempted = true
        }
    val settingsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (context.hasAnyPermission(permissions)) onPermissionAvailable()
            else requestAttempted = true
        }

    AlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        transformationSpec = transformationSpec,
        icon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier,
            )
        },
        title = {
            Text(
                stringResource(
                    if (requestAttempted) R.string.permission_denied_title
                    else R.string.permission_title
                )
            )
        },
        text = {
            Text(
                stringResource(
                    if (requestAttempted) R.string.permission_denied_explanation
                    else R.string.permission_explanation
                )
            )
        },
        edgeButton = {
            AlertDialogDefaults.EdgeButton(
                onClick = {
                    if (requestAttempted) {
                        settingsLauncher.launch(context.applicationSettingsIntent())
                    } else {
                        permissionLauncher.launch(permissions.toTypedArray())
                    }
                }
            ) {
                Text(
                    stringResource(
                        if (requestAttempted) R.string.open_settings
                        else R.string.grant_permission
                    )
                )
            }
        },
    )
}

private fun Context.hasAnyPermission(permissions: List<String>): Boolean =
    permissions.any { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

private fun Context.applicationSettingsIntent(): Intent =
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    )
