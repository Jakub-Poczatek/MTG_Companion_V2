package org.wit.mtgcompanionv2.ui.cardList

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import org.wit.mtgcompanionv2.R
import org.wit.mtgcompanionv2.adapters.CardAdapter
import org.wit.mtgcompanionv2.databinding.FragmentCardListBinding
import org.wit.mtgcompanionv2.main.MTGCompanion

class CardListFragment : Fragment() {

    lateinit var app: MTGCompanion
    private var _fragBinding: FragmentCardListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var cardListViewModel: CardListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MTGCompanion
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        _fragBinding = FragmentCardListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        activity?.title = getString(R.string.cardListTitle)

        cardListViewModel = ViewModelProvider(this).get(CardListViewModel::class.java)
        cardListViewModel.text.observe(viewLifecycleOwner, Observer {})

        fragBinding.cardListRecycleView.layoutManager = LinearLayoutManager (activity)
        fragBinding.cardListRecycleView.adapter = CardAdapter(app.cardStore.findAll())

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_card_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item,
        requireView().findNavController()) || super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                CardListFragment().apply {
                    arguments = Bundle().apply{}
                }
    }
}