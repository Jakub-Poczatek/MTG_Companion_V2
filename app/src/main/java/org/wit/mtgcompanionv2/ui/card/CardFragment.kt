package org.wit.mtgcompanionv2.ui.card

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import org.wit.mtgcompanionv2.R
import org.wit.mtgcompanionv2.databinding.FragmentCardBinding
import org.wit.mtgcompanionv2.main.MTGCompanion
import org.wit.mtgcompanionv2.models.CardModel
import timber.log.Timber.i

class CardFragment : Fragment() {

    lateinit var app: MTGCompanion
    private var _fragBinding: FragmentCardBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var cardViewModel: CardViewModel
    private var card = CardModel()
    private var edit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MTGCompanion
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        _fragBinding = FragmentCardBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        activity?.title = getString(R.string.cardActivityTitle)
        cardViewModel = ViewModelProvider(this)[CardViewModel::class.java]
        cardViewModel.text.observe(viewLifecycleOwner, Observer{
        })
        setButtonListener(fragBinding)

        if (requireArguments().getBoolean("edit")) {
            i("We editing")
        }

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_card, menu)
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
            CardFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    private fun setButtonListener(layout: FragmentCardBinding) {
        layout.addCardBtn.setOnClickListener {
            defaultNumericFields(layout)
            card.name = layout.cardNameTxt.text.toString()
            card.type = layout.cardTypeSpinner.selectedItem.toString()
            card.power = layout.cardPowerNumTxt.text.toString().toShort()
            card.toughness = layout.cardToughnessNumTxt.text.toString().toShort()
            card.neutral = layout.cardNeuCostNumTxt.text.toString().toShort()
            card.white = layout.cardWhtCostNumTxt.text.toString().toShort()
            card.black = layout.cardBlkCostNumTxt.text.toString().toShort()
            card.red = layout.cardRedCostNumTxt.text.toString().toShort()
            card.blue = layout.cardBluCostNumTxt.text.toString().toShort()
            card.green = layout.cardGrnCostNumTxt.text.toString().toShort()
            card.description = layout.cardDescriptionMLTxt.text.toString()

            if(card.name.isNotEmpty()) {
                if(edit) {
                    app.cardStore.update(card.copy())
                } else {
                    app.cardStore.create(card.copy())
                }
                //setFragmentResult(AppCompatActivity.RESULT_OK)
                //finish()
            } else {
                Snackbar.make(it, R.string.InvalidCardName, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun defaultNumericFields(layout: FragmentCardBinding){
        if(layout.cardPowerNumTxt.text.isEmpty())
            layout.cardPowerNumTxt.setText("0")
        if(layout.cardToughnessNumTxt.text.isEmpty())
            layout.cardToughnessNumTxt.setText(("0"))

        if(layout.cardNeuCostNumTxt.text.isEmpty())
            layout.cardNeuCostNumTxt.setText(("0"))
        if(layout.cardWhtCostNumTxt.text.isEmpty())
            layout.cardWhtCostNumTxt.setText(("0"))
        if(layout.cardBlkCostNumTxt.text.isEmpty())
            layout.cardBlkCostNumTxt.setText(("0"))
        if(layout.cardRedCostNumTxt.text.isEmpty())
            layout.cardRedCostNumTxt.setText(("0"))
        if(layout.cardBluCostNumTxt.text.isEmpty())
            layout.cardBluCostNumTxt.setText(("0"))
        if(layout.cardGrnCostNumTxt.text.isEmpty())
            layout.cardGrnCostNumTxt.setText(("0"))
    }
}