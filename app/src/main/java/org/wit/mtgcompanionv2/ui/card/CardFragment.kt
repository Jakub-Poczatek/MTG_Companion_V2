package org.wit.mtgcompanionv2.ui.card

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.NavigationUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import org.wit.mtgcompanion.helpers.showImagePicker
import org.wit.mtgcompanionv2.R
import org.wit.mtgcompanionv2.databinding.FragmentCardBinding
import org.wit.mtgcompanionv2.firebase.FirebaseDBManager
import org.wit.mtgcompanionv2.firebase.FirebaseImageManager
import org.wit.mtgcompanionv2.main.MTGCompanion
import org.wit.mtgcompanionv2.models.CardModel
import org.wit.mtgcompanionv2.models.Colour
import org.wit.mtgcompanionv2.models.Rarity
import org.wit.mtgcompanionv2.ui.auth.LoggedInViewModel
import org.wit.mtgcompanionv2.ui.cardList.CardListViewModel
import timber.log.Timber
import timber.log.Timber.i


class CardFragment : Fragment() {

    private var _fragBinding: FragmentCardBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var cardViewModel: CardViewModel
    private var card = CardModel()
    private lateinit var imageIntentLauncher: ActivityResultLauncher<Intent>
    private val args by navArgs<CardFragmentArgs>()
    private var fetchingImage: Boolean = false
    private val cardListViewModel: CardListViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        _fragBinding = FragmentCardBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        activity?.title = getString(R.string.cardActivityTitle)

        cardViewModel = ViewModelProvider(this).get(CardViewModel::class.java)
        cardViewModel.observableStatus.observe(viewLifecycleOwner, Observer { render() })

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

    private fun render() {
        Timber.i("This is the render function, dunno what it does")
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_card, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menuItemDeleteCard -> {
                //app.cards.delete(card)
                cardListViewModel.delete(cardListViewModel.liveFirebaseUser.value?.uid!!, card.uid!!)
                findNavController().popBackStack()
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
            if(!fetchingImage) {
                fragBinding.card = CardModel()
                defaultAllFields()
                fetchingImage = false
            }
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
        fragBinding.cardRaritySpinner.resources.getStringArray(R.array.rarities).forEachIndexed { index, element ->
            if (element == card.rarity.toString())
                fragBinding.cardRaritySpinner.setSelection(index)
        }
        fragBinding.card = card
        Picasso.get().load(Uri.parse(card.image)).into(fragBinding.cardArtImgVw)
    }

    private fun setAddButtonListener(layout: FragmentCardBinding) {
        layout.addCardBtn.setOnClickListener {
            card.name = layout.cardNameTxt.text.toString()
            card.type = layout.cardTypeSpinner.selectedItem.toString()
            card.power = layout.cardFragmentPowerSeeker.progress.toInt()
            card.toughness = layout.cardFragmentToughnessSeeker.progress.toInt()
            card.neutral = layout.cardFragmentNtrlPicker.value.toInt()
            card.white = layout.cardFragmentWhtPicker.value.toInt()
            card.black = layout.cardFragmentBlkPicker.value.toInt()
            card.red = layout.cardFragmentRedPicker.value.toInt()
            card.blue = layout.cardFragmentBluPicker.value.toInt()
            card.green = layout.cardFragmentGrnPicker.value.toInt()
            card.description = layout.cardDescriptionMLTxt.text.toString()
            card.rarity = Rarity.valueOf(layout.cardRaritySpinner.selectedItem.toString())
            card.set = layout.cardFragmentSetEdit.text.toString()
            if(layout.cardFragmentValueEdit.text.isNotEmpty())
                card.value = layout.cardFragmentValueEdit.text.toString().toDouble()
            else
                card.value = 0.0
            assignColors(card)

            if(card.name.isNotEmpty()) {
                if(args.edit) {
                    //app.cards.update(card.copy())
                    cardViewModel.updateCard(loggedInViewModel.liveFirebaseUser.value?.uid!!, card.uid!!, card)
                    findNavController().popBackStack()
                } else {
                    //app.cards.create(card.copy())
                    card.email = loggedInViewModel.liveFirebaseUser.value?.email!!
                    cardViewModel.addCard(loggedInViewModel.liveFirebaseUser, card)
                    FirebaseImageManager.updateCardArt(
                        cardListViewModel.liveFirebaseUser.value!!.uid,
                        card,
                        Uri.parse(card.image)
                    )
                    defaultAllFields()
                    if(args.quickAdd)
                        findNavController().popBackStack()
                }
            } else {
                Snackbar.make(it, R.string.InvalidCardName, Snackbar.LENGTH_LONG).show()

            }
        }
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

    private fun assignColors(card: CardModel){
        if(card.neutral > 0)
            card.colours.add(Colour.Neutral)
        if(card.white > 0)
            card.colours.add(Colour.White)
        if(card.black > 0)
            card.colours.add(Colour.Black)
        if(card.red > 0)
            card.colours.add(Colour.Red)
        if(card.blue > 0)
            card.colours.add(Colour.Blue)
        if(card.green > 0)
            card.colours.add(Colour.Green)
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
                        card.image = image.toString()
                        fetchingImage = true
                        Picasso.get().load(Uri.parse(card.image)).into(fragBinding.cardArtImgVw)
                    }
                }
                AppCompatActivity.RESULT_CANCELED -> {} else -> {}
            }
        }
    }
}