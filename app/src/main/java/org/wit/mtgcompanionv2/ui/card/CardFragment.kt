package org.wit.mtgcompanionv2.ui.card

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
    private val args by navArgs<CardFragmentArgs>()

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
        cardViewModel.observableStatus.observe(viewLifecycleOwner, Observer {
            status -> status?.let { render(status) }
        })

        setAddButtonListener(fragBinding)

        registerImagePickerCallback()
        fragBinding.cardArtImgVw.setOnClickListener{
            showImagePicker(imageIntentLauncher, requireContext())
        }

        setRangeOfNumberPickers(fragBinding.cardFragmentNtrlPicker)
        setRangeOfNumberPickers(fragBinding.cardFragmentWhtPicker)
        setRangeOfNumberPickers(fragBinding.cardFragmentBlkPicker)
        setRangeOfNumberPickers(fragBinding.cardFragmentRedPicker)
        setRangeOfNumberPickers(fragBinding.cardFragmentBluPicker)
        setRangeOfNumberPickers(fragBinding.cardFragmentGrnPicker)

        return root
    }

    private fun render(status: Boolean) {
        when (status) {
            true -> {
                view?.let {
                    //Uncomment this if you want to immediately return to Report
                    //findNavController().popBackStack()
                }
            }
            false -> Toast.makeText(context, getString(R.string.cardError), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_card, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menuItemDeleteCard -> {
                //app.cards.delete(card)
                cardViewModel.deleteCard(card)
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
        if (args.edit) {
            setCard(args.card!!)
            setHasOptionsMenu(true)
        } else {
            fragBinding.card = CardModel()
            defaultAllFields()
            setHasOptionsMenu(false)
        }
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
        //fragBinding.cardNameTxt.setText(card.name)
        fragBinding.cardTypeSpinner.resources.getStringArray(R.array.cardTypes).forEachIndexed{ index, element ->
            if (element == card.type) {
                fragBinding.cardTypeSpinner.setSelection(index)
            }
        }
        fragBinding.card = card
        Picasso.get().load(card.image).into(fragBinding.cardArtImgVw)
    }

    private fun setAddButtonListener(layout: FragmentCardBinding) {
        layout.addCardBtn.setOnClickListener {
            defaultNumericFieldsIfInvalid(layout)
            card.name = layout.cardNameTxt.text.toString()
            card.type = layout.cardTypeSpinner.selectedItem.toString()
            card.power = layout.cardFragmentPowerSeeker.progress.toShort()
            card.toughness = layout.cardFragmentToughnessSeeker.progress.toShort()
            card.neutral = layout.cardFragmentNtrlPicker.value.toShort()
            card.white = layout.cardFragmentWhtPicker.value.toShort()
            card.black = layout.cardFragmentBlkPicker.value.toShort()
            card.red = layout.cardFragmentRedPicker.value.toShort()
            card.blue = layout.cardFragmentBluPicker.value.toShort()
            card.green = layout.cardFragmentGrnPicker.value.toShort()
            card.description = layout.cardDescriptionMLTxt.text.toString()

            if(card.name.isNotEmpty()) {
                if(args.edit) {
                    //app.cards.update(card.copy())
                    cardViewModel.updateCard(card)
                    navController.popBackStack()
                } else {
                    //app.cards.create(card.copy())
                    cardViewModel.addCard(card)
                    defaultAllFields()
                    if(args.quickAdd)
                        navController.popBackStack()
                }
            } else {
                Snackbar.make(it, R.string.InvalidCardName, Snackbar.LENGTH_LONG).show()

            }
        }
    }

    private fun defaultNumericFieldsIfInvalid(layout: FragmentCardBinding){
        fun zeroField(element: EditText){
            if(element.text.isEmpty())
                element.setText("0")
        }
        //zeroField(layout.cardPowerNumTxt)
        //zeroField(layout.cardToughnessNumTxt)
        /*zeroField(layout.cardNeuCostNumTxt)
        zeroField(layout.cardWhtCostNumTxt)
        zeroField(layout.cardBlkCostNumTxt)
        zeroField(layout.cardRedCostNumTxt)
        zeroField(layout.cardBluCostNumTxt)
        zeroField(layout.cardGrnCostNumTxt)*/
    }

    private fun setRangeOfNumberPickers(picker: NumberPicker){
        picker.minValue = 0
        picker.maxValue = 10
    }

    private fun defaultAllFields(){
        fragBinding.cardTypeSpinner.setSelection(0)
        fragBinding.card = CardModel()
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