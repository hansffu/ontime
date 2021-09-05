package hansffu.ontime

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import hansffu.ontime.databinding.ActivityNavigationBinding
import hansffu.ontime.databinding.FragmentStopListNavigationBinding
import hansffu.ontime.model.StopListType

class NavigationActivity : FragmentActivity() {

    private lateinit var binding: ActivityNavigationBinding
    private val favoriteModel: FavoriteViewModel by viewModels()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        favoriteModel.getLocationHolder().observe(this) {
            if (it is LocationHolder.NoPermission) requestPermissions(
                arrayOf(
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION
                ), 123
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        favoriteModel.refreshPermissions()
    }
}

class StopListNavigationFragment : Fragment() {


    private var _binding: FragmentStopListNavigationBinding? = null
    private val binding: FragmentStopListNavigationBinding
        get() = _binding!!
    private lateinit var navigationAdapter: StopListNavigationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStopListNavigationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navigationAdapter = StopListNavigationAdapter(this)
        binding.pager.adapter = navigationAdapter
        val model: FavoriteViewModel by requireActivity().viewModels()
        model.currentList.observe(requireActivity()){
            binding.pager.setCurrentItem(it.ordinal, true)
        }
    }
}


class StopListNavigationAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = StopListType.values().size

    override fun createFragment(position: Int): Fragment {
        println("created fragment")
        val fragment = StopListFragment()
        fragment.arguments = Bundle().apply {
            putString("TYPE", StopListType.values()[position].name)
        }
        return fragment
    }
}

