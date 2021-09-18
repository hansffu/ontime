package hansffu.ontime

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import hansffu.ontime.adapter.*
import hansffu.ontime.databinding.FragmentStopListBinding
import hansffu.ontime.model.StopListType
import hansffu.ontime.utils.ListLayout
import hansffu.ontime.utils.RotatingInputListener

class StopListFragment() : Fragment() {

    private var _binding: FragmentStopListBinding? = null
    private val binding: FragmentStopListBinding
        get() = _binding!!
    private var stopViewAdapter = StopViewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStopListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private lateinit var favoriteModel: FavoriteViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model: FavoriteViewModel by requireActivity().viewModels()
        favoriteModel = model
        val timeModel: TimeViewModel by requireActivity().viewModels()
        timeModel.shortTime.observe(this.requireActivity()) {
            binding.clock.text = it ?: ""
        }
        binding.stopList.apply {
            adapter = stopViewAdapter
            isEdgeItemsCenteringEnabled = false
            layoutManager = WearableLinearLayoutManager(requireContext(), ListLayout())
            setOnGenericMotionListener(RotatingInputListener(context))
        }
        favoriteModel.getLocationHolder().observe(this.requireActivity()) {
            if (it is LocationHolder.NoPermission) requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 123
            )
        }
        startBackgroundTasks()
    }

    override fun onResume() {
        super.onResume()
        binding.stopList.requestFocus()
    }

    private fun startBackgroundTasks() {
        binding.stopList.setOnScrollChangeListener { view, _, _, _, _ ->
            if (view is WearableRecyclerView) {
                binding.clock.y = -view.computeVerticalScrollOffset().toFloat()
            }
        }
        createListItems(StopListType.valueOf(arguments?.getString(Arguments.STOP_TYPE)!!))
            .observe(this.requireActivity()) { items ->
                stopViewAdapter.items = items
                binding.stopList.requestFocus()
            }

    }


    private fun createListItems(type: StopListType): LiveData<List<StopViewItem>> =
        when (type) {
            StopListType.FAVORITES -> createFavoriteItems()
            StopListType.NEARBY -> createNearbyItems()
        }

    private fun createFavoriteItems(): LiveData<List<StopViewItem>> =
        favoriteModel.favoriteStops.map { stops ->
            listOf(
                listOf(HeaderItem(resources.getString(R.string.favorites_header))),
                stops.map { StopItem(it, ::onItemSelected) },
                listOf(ButtonItem(resources.getString(R.string.find_more)) {
                    favoriteModel.setCurrentList(
                        StopListType.NEARBY
                    )
                })
            ).flatten()
        }


    private fun createNearbyItems(): LiveData<List<StopViewItem>> =
        favoriteModel.nearbyStops.map { stops ->
            listOf(HeaderItem(resources.getString(R.string.nearby_header))) + stops.map {
                StopItem(it, ::onItemSelected)
            }
        }


    private fun onItemSelected(item: StopViewItem) {
        if (item !is StopItem) return
        val startTimetableActivity = Intent(requireContext(), TimetableActivity::class.java)
        startTimetableActivity.putExtra(TimetableActivity.STOP_ID, item.stop.id)
        startTimetableActivity.putExtra(TimetableActivity.STOP_NAME, item.stop.name)
        startActivity(startTimetableActivity)
    }

    object Arguments {
        const val STOP_TYPE = "STOP_TYPE"
    }
}