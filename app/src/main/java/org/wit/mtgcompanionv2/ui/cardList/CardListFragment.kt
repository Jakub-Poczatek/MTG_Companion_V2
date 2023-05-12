package org.wit.mtgcompanionv2.ui.cardList

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.wit.mtgcompanionv2.R
import org.wit.mtgcompanionv2.adapters.CardAdapter
import org.wit.mtgcompanionv2.adapters.CardListener
import org.wit.mtgcompanionv2.databinding.FragmentCardListBinding
import org.wit.mtgcompanionv2.firebase.SortTerm
import org.wit.mtgcompanionv2.models.CardModel
import org.wit.mtgcompanionv2.ui.auth.LoggedInViewModel
import org.wit.mtgcompanionv2.utils.*
import timber.log.Timber

class CardListFragment : Fragment(), CardListener {

    private var _fragBinding: FragmentCardListBinding? = null
    private val fragBinding get() = _fragBinding!!
    lateinit var loader : AlertDialog
    private val cardListViewModel: CardListViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private var sortTerm: SortTerm = SortTerm.None

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

        setSwipeRefresh()

        showLoader(loader, "Downloading Cards")
        cardListViewModel.observableCardList.observe(viewLifecycleOwner, Observer {
            cards ->
            cards?.let {
                render(cards as ArrayList<CardModel>)
                hideLoader(loader)
                checkSwipeRefresh()
            }
        })

        textChangeListener()

        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                showLoader(loader, "Deleting Card")
                val adapter = fragBinding.cardListRecycleView.adapter as CardAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                cardListViewModel.delete(cardListViewModel.liveFirebaseUser.value?.uid!!, (viewHolder.itemView.tag as CardModel).uid!!)
                hideLoader(loader)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.cardListRecycleView)

        val swipeEditHandler = object : SwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onCardClick(viewHolder.itemView.tag as CardModel)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.cardListRecycleView)


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

    // Swipe Functions

    private fun setSwipeRefresh() {
        fragBinding.swipeRefresh.setOnRefreshListener {
            fragBinding.swipeRefresh.isRefreshing = true
            showLoader(loader,"Downloading Cards")
            if (cardListViewModel.readOnly.value!!) cardListViewModel.loadAll()
            else cardListViewModel.load(sortTerm)
        }
    }

    private fun checkSwipeRefresh() {
        if (fragBinding.swipeRefresh.isRefreshing)
            fragBinding.swipeRefresh.isRefreshing = false
    }

    private fun render(cardsList: ArrayList<CardModel>) {
        fragBinding.cardListRecycleView.adapter = CardAdapter(cardsList, this, cardListViewModel.readOnly.value!!)
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

    override fun onCardClick(card: CardModel) {
        fragBinding.cardListSearchTxt.text.clear()
        val action = CardListFragmentDirections.actionCardListFragmentToCardFragment(true, card, false)
        if(!cardListViewModel.readOnly.value!!)
            findNavController().navigate(action)
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_card_list, menu)

                val item = menu.findItem(R.id.toggleCards) as MenuItem
                item.setActionView(R.layout.togglebutton_layout)
                val toggleCards: SwitchCompat = item.actionView!!.findViewById(R.id.toggleButton)
                toggleCards.isChecked = false

                toggleCards.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) cardListViewModel.loadAll()
                    else cardListViewModel.load(sortTerm)
                }

                var sortItem = menu.findItem(R.id.sortMenuParent).subMenu as Menu
                sortItem.children.forEach { itemInMenu -> itemInMenu.setOnMenuItemClickListener {
                    sortTerm = SortTerm.valueOf(it.title.toString().replace(" ", ""))
                    showLoader(loader,"Downloading Cards")
                    if (cardListViewModel.readOnly.value!!) cardListViewModel.loadAll()
                    else cardListViewModel.load(sortTerm)
                    true
                } }
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
                cardListViewModel.load(sortTerm)
            }
        })
    }

    private fun textChangeListener(){
        fragBinding.cardListSearchTxt.addTextChangedListener {
            val query = fragBinding.cardListSearchTxt.text.toString().lowercase().trim()
            val filteredCards = ArrayList<CardModel>()
            if(query.isEmpty()) {
                fragBinding.cardListRecycleView.adapter = CardAdapter(
                    ArrayList<CardModel>(cardListViewModel.observableCardList.value!!), this, cardListViewModel.readOnly.value!!)
            } else if(fragBinding.cardListSearchBySpinner.selectedItem.toString() == "name") {
                for (card in cardListViewModel.observableCardList.value!!) {
                    if (query in card.name.lowercase().trim())
                        filteredCards.add(card)
                }
                fragBinding.cardListRecycleView.adapter = CardAdapter(filteredCards, this, cardListViewModel.readOnly.value!!)
            } else if (fragBinding.cardListSearchBySpinner.selectedItem.toString() == "type") {
                for (card in cardListViewModel.observableCardList.value!!) {
                    if (query in card.type.lowercase().trim())
                        filteredCards.add(card)
                }
                fragBinding.cardListRecycleView.adapter = CardAdapter(filteredCards, this, cardListViewModel.readOnly.value!!)
            } else {
                fragBinding.cardListRecycleView.adapter = CardAdapter(
                    ArrayList<CardModel>(cardListViewModel.observableCardList.value!!), this, cardListViewModel.readOnly.value!!)
            }
        }
    }

    private fun sortCardList(){

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}