package org.wit.mtgcompanionv2.ui.cardList

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
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
import org.wit.mtgcompanionv2.ui.auth.LoggedInViewModel
import org.wit.mtgcompanionv2.utils.createLoader
import org.wit.mtgcompanionv2.utils.hideLoader
import org.wit.mtgcompanionv2.utils.showLoader

class CardListFragment : Fragment(), CardListener {

    private var _fragBinding: FragmentCardListBinding? = null
    private val fragBinding get() = _fragBinding!!
    lateinit var loader : AlertDialog
    private val cardListViewModel: CardListViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        _fragBinding = FragmentCardListBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        setupMenu()
        loader = createLoader(requireActivity())
        activity?.title = getString(R.string.cardListTitle)

        val layoutManager = GridLayoutManager(requireContext(), 2)
        fragBinding.cardListRecycleView.layoutManager = layoutManager

        fragBinding.menuFloatingAddButton.setOnClickListener {
            val action = CardListFragmentDirections.actionCardListFragmentToCardFragment(false, null, true)
            findNavController().navigate(action)
        }

        showLoader(loader, "Downloading Cards")
        cardListViewModel.observableCardList.observe(viewLifecycleOwner, Observer {
            cards ->
            cards?.let {
                render(cards as ArrayList<CardModel>)
                hideLoader(loader)
            }
        })
        //fragBinding.cardListRecycleView.adapter = CardAdapter(cardListViewModel.observableCardList.value!!, this)


        textChangeListener()


        return root
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_card_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
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

    override fun onCardClick(card: CardModel, position: Int) {
        fragBinding.cardListSearchTxt.text.clear()
        val action = CardListFragmentDirections.actionCardListFragmentToCardFragment(true, card, false)
        findNavController().navigate(action)
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_card_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                return NavigationUI.onNavDestinationSelected(menuItem,
                    requireView().findNavController())
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onResume() {
        super.onResume()
        showLoader(loader, "Downloading Cards")
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner, Observer { firebaseUser ->
            if (firebaseUser != null) {
                cardListViewModel.liveFirebaseUser.value = firebaseUser
                cardListViewModel.load()
            }
        })
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

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}