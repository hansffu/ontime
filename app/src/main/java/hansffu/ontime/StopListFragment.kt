package hansffu.ontime

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.wear.compose.material.*
import hansffu.ontime.model.Stop
import hansffu.ontime.model.StopListType
import hansffu.ontime.ui.stoplist.StopListUi
import hansffu.ontime.ui.theme.OntimeTheme
import hansffu.ontime.utils.rememberScrollingScalingLazyListState

class StopListFragment : Fragment() {

    private lateinit var favoriteModel: FavoriteViewModel

    @OptIn(ExperimentalWearMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val model: FavoriteViewModel by requireActivity().viewModels()
        favoriteModel = model
        val stopListType = StopListType.valueOf(arguments?.getString(Arguments.STOP_TYPE)!!)
        return ComposeView(requireContext()).apply {
            setContent {
                val scalingLazyListState = rememberScrollingScalingLazyListState()
                OntimeTheme {
                    Scaffold(
                        timeText = { TimeText() },
                        modifier = Modifier.fillMaxSize() then Modifier.background(MaterialTheme.colors.background),
                        positionIndicator = { PositionIndicator(scalingLazyListState) }
                    ) {
                        StopListUi(
                            favoriteModel = favoriteModel,
                            scalingLazyListState = scalingLazyListState,
                            stopListType = stopListType,
                            onStopSelected = ::onStopSelected
                        )
                    }
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoriteModel.getLocationHolder().observe(this.requireActivity()) {
            if (it is LocationHolder.NoPermission) requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 123
            )
        }
    }


//    private fun createListItems(type: StopListType): LiveData<List<StopViewItem>> =
//        when (type) {
//            StopListType.FAVORITES -> createFavoriteItems()
//            StopListType.NEARBY -> createNearbyItems()
//        }
//
//    private fun createFavoriteItems(): LiveData<List<StopViewItem>> =
//        favoriteModel.favoriteStops.map { stops ->
//            listOf(
//                listOf(HeaderItem(resources.getString(R.string.favorites_header))),
//                stops.map { StopItem(it, ::onItemSelected) },
//                listOf(ButtonItem(resources.getString(R.string.find_more)) {
//                    favoriteModel.setCurrentList(
//                        StopListType.NEARBY
//                    )
//                })
//            ).flatten()
//        }
//
//
//    private fun createNearbyItems(): LiveData<List<StopViewItem>> =
//        favoriteModel.nearbyStops.map { stops ->
//            listOf(HeaderItem(resources.getString(R.string.nearby_header))) + stops.map {
//                StopItem(it, ::onItemSelected)
//            }
//        }


    private fun onStopSelected(stop: Stop) {
        val startTimetableActivity = Intent(requireContext(), TimetableActivity::class.java)
        startTimetableActivity.putExtra(TimetableActivity.STOP_ID, stop.id)
        startTimetableActivity.putExtra(TimetableActivity.STOP_NAME, stop.name)
        startActivity(startTimetableActivity)
    }

    object Arguments {
        const val STOP_TYPE = "STOP_TYPE"
    }
}