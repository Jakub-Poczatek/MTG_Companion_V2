package org.wit.mtgcompanionv2.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.wit.mtgcompanionv2.R
import org.wit.mtgcompanionv2.databinding.CardCardBinding
import org.wit.mtgcompanionv2.models.CardModel

interface CardListener {
    fun onCardClick(card: CardModel)
}

class CardAdapter constructor(
    private var cards: ArrayList<CardModel>,
    private val listener: CardListener,
    private val readOnly: Boolean)
    : RecyclerView.Adapter<CardAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardCardBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return MainHolder(binding, readOnly)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val card = cards[holder.adapterPosition]
        holder.bind(card, listener)
    }

    override fun getItemCount(): Int = cards.size

    inner class MainHolder(private val binding : CardCardBinding, private val readOnly: Boolean)
            : RecyclerView.ViewHolder(binding.root) {

        val readOnlyRow = readOnly

        fun bind(card: CardModel, listener: CardListener) {
            binding.root.tag = card
            binding.card = card
            Picasso.get().load(Uri.parse(card.image)).into(binding.cardCardArtImgView)
            binding.root.setOnClickListener { listener.onCardClick(card) }
        }
    }

    fun removeAt(position: Int){
        cards.removeAt(position)
        notifyItemRemoved(position)
    }
}