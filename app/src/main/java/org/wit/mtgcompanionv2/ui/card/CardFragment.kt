package org.wit.mtgcompanionv2.ui.card

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import org.wit.mtgcompanion.helpers.showImagePicker
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
    private lateinit var imageIntentLauncher: ActivityResultLauncher<Intent>
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MTGCompanion
        navController = findNavController()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        _fragBinding = FragmentCardBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        activity?.title = getString(R.string.cardActivityTitle)
        cardViewModel = ViewModelProvider(this)[CardViewModel::class.java]
        cardViewModel.text.observe(viewLifecycleOwner) {
        }
        setAddButtonListener(fragBinding)

        registerImagePickerCallback()
        fragBinding.cardArtImgVw.setOnClickListener{
            showImagePicker(imageIntentLauncher, requireContext())
        }

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_card, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menuItemDeleteCard -> {
                app.cards.delete(card)
                navController.popBackStack()
            }
        }
        return NavigationUI.onNavDestinationSelected(item, requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetViewAttributes()
    }

    override fun onResume() {
        super.onResume()
        if (requireArguments().getBoolean("edit")) {
            setCard(requireArguments().getParcelable("card")!!)
            setHasOptionsMenu(true)
        } else {
            defaultAllFields()
            setHasOptionsMenu(false)
        }
        val cardViewModel = ViewModelProvider(this)[CardViewModel::class.java]
        cardViewModel.text.observe(viewLifecycleOwner, Observer{
        })
    }

    private fun resetViewAttributes() {
        requireArguments().putBoolean("edit", false)
        requireArguments().putParcelable("card", null)
        requireArguments().putBoolean("quickAdd", false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CardFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    private fun setCard(card: CardModel){
        this.card = card
        fragBinding.addCardBtn.setText(R.string.saveCard)
        fragBinding.cardNameTxt.setText(card.name)
        fragBinding.cardTypeSpinner.resources.getStringArray(R.array.cardTypes).forEachIndexed{ index, element ->
            if (element == card.type) {
                fragBinding.cardTypeSpinner.setSelection(index)
            }
        }
        fragBinding.cardPowerNumTxt.setText(card.power.toString())
        fragBinding.cardToughnessNumTxt.setText(card.toughness.toString())
        fragBinding.cardNeuCostNumTxt.setText(card.neutral.toString())
        fragBinding.cardWhtCostNumTxt.setText(card.white.toString())
        fragBinding.cardBlkCostNumTxt.setText(card.black.toString())
        fragBinding.cardRedCostNumTxt.setText(card.red.toString())
        fragBinding.cardBluCostNumTxt.setText(card.blue.toString())
        fragBinding.cardGrnCostNumTxt.setText(card.green.toString())
        fragBinding.cardDescriptionMLTxt.setText(card.description)
        Picasso.get().load(card.image).into(fragBinding.cardArtImgVw)
    }

    private fun setAddButtonListener(layout: FragmentCardBinding) {
        layout.addCardBtn.setOnClickListener {
            defaultNumericFieldsIfInvalid(layout)
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
                if(requireArguments().getBoolean("edit")) {
                    app.cards.update(card.copy())
                    navController.popBackStack()
                } else {
                    app.cards.create(card.copy())
                    defaultAllFields()
                    if(requireArguments().getBoolean("quickAdd"))
                        navController.popBackStack()
                }
            } else {
                Snackbar.make(it, R.string.InvalidCardName, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun defaultNumericFieldsIfInvalid(layout: FragmentCardBinding){
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

    private fun defaultAllFields(){
        fragBinding.cardNameTxt.text.clear()
        fragBinding.cardTypeSpinner.setSelection(0)
        fragBinding.cardPowerNumTxt.text.clear()
        fragBinding.cardToughnessNumTxt.text.clear()
        fragBinding.cardNeuCostNumTxt.text.clear()
        fragBinding.cardWhtCostNumTxt.text.clear()
        fragBinding.cardBlkCostNumTxt.text.clear()
        fragBinding.cardRedCostNumTxt.text.clear()
        fragBinding.cardBluCostNumTxt.text.clear()
        fragBinding.cardGrnCostNumTxt.text.clear()
        fragBinding.cardDescriptionMLTxt.text.clear()
        fragBinding.cardArtImgVw.setImageResource(R.drawable.ic_default_card_art)
    }

    private fun registerImagePickerCallback() {
        imageIntentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
                result ->
            when(result.resultCode){
                AppCompatActivity.RESULT_OK -> {
                    if(result.data != null){
                        i("Got Result ${result.data!!.data}")
                        val image = result.data!!.data!!
                        requireContext().contentResolver.takePersistableUriPermission(image, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        card.image = image

                        Picasso.get().load(card.image).into(fragBinding.cardArtImgVw)
                    }
                }
                AppCompatActivity.RESULT_CANCELED -> {} else -> {}
            }
        }
    }
}