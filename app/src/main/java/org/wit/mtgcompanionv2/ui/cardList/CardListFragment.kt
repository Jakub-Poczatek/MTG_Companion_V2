package org.wit.mtgcompanionv2.ui.cardList

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.GridLayoutManager
import org.wit.mtgcompanionv2.R
import org.wit.mtgcompanionv2.adapters.CardAdapter
import org.wit.mtgcompanionv2.adapters.CardListener
import org.wit.mtgcompanionv2.databinding.FragmentCardListBinding
import org.wit.mtgcompanionv2.main.MTGCompanion
import org.wit.mtgcompanionv2.models.CardModel

class CardListFragment : Fragment(), CardListener {

    lateinit var app: MTGCompanion
    private var _fragBinding: FragmentCardListBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var cardListViewModel: CardListViewModel
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MTGCompanion
        navController = findNavController()
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        _fragBinding = FragmentCardListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        activity?.title = getString(R.string.cardListTitle)

        cardListViewModel = ViewModelProvider(this)[CardListViewModel::class.java]

        val layoutManager = GridLayoutManager(requireContext(), 2)
        fragBinding.cardListRecycleView.layoutManager = layoutManager
        cardListViewModel.observableCardList.observe(viewLifecycleOwner, Observer {
            cards ->
            cards.let {render(cards)}
        })
        fragBinding.cardListRecycleView.adapter = CardAdapter(cardListViewModel.observableCardList.value!!, this)


        textChangeListener()

        fragBinding.menuFloatingAddButton.setOnClickListener {
            val action = CardListFragmentDirections.actionCardListFragmentToCardFragment(false, null, true)
            navController.navigate(action)
        }
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_card_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }

    private fun render(cardsList: List<CardModel>) {
        fragBinding.cardListRecycleView.adapter = CardAdapter(cardsList, this)
        if (cardsList.isEmpty()) {
            fragBinding.cardListRecycleView.visibility = View.GONE
            fragBinding.cardsNotFound.visibility = View.VISIBLE
            fragBinding.cardListSearchBySpinner.isEnabled = false
            fragBinding.cardListSearchTxt.isEnabled = false
        } else {
            fragBinding.cardListRecycleView.visibility = View.VISIBLE
            fragBinding.cardsNotFound.visibility = View.GONE
            fragBinding.cardListSearchBySpinner.isEnabled = true
            fragBinding.cardListSearchTxt.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }

    override fun onCardClick(card: CardModel, position: Int) {
        fragBinding.cardListSearchTxt.text.clear()
        val action = CardListFragmentDirections.actionCardListFragmentToCardFragment(true, card, false)
        navController.navigate(action)
    }

    override fun onResume() {
        super.onResume()
        cardListViewModel.load()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                CardListFragment().apply {
                    arguments = Bundle().apply{}
                }
    }

    private fun textChangeListener(){
        fragBinding.cardListSearchTxt.addTextChangedListener {
            val query = fragBinding.cardListSearchTxt.text.toString().lowercase().trim()
            val filteredCards = ArrayList<CardModel>()
            if(query.isEmpty()) {
                fragBinding.cardListRecycleView.adapter = CardAdapter(cardListViewModel.observableCardList.value!!, this)
            } else if(fragBinding.cardListSearchBySpinner.selectedItem.toString() == "name") {
                for (card in cardListViewModel.observableCardList.value!!) {
                    if (query in card.name.lowercase().trim())
                        filteredCards.add(card)
                }
                fragBinding.cardListRecycleView.adapter = CardAdapter(filteredCards, this)
            } else if (fragBinding.cardListSearchBySpinner.selectedItem.toString() == "type") {
                for (card in cardListViewModel.observableCardList.value!!) {
                    if (query in card.type.lowercase().trim())
                        filteredCards.add(card)
                }
                fragBinding.cardListRecycleView.adapter = CardAdapter(filteredCards, this)
            } else {
                fragBinding.cardListRecycleView.adapter = CardAdapter(cardListViewModel.observableCardList.value!!, this)
            }
        }
    }
}