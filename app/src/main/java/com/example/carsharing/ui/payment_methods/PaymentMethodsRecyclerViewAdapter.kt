package com.example.carsharing.ui.payment_methods

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.carsharing.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PaymentMethodsRecyclerViewAdapter(
    private val context: Context,
    private val viewModel: PaymentMethodsViewModel,
    private val recyclerView: RecyclerView
) :
    RecyclerView.Adapter<PaymentMethodsRecyclerViewAdapter.PaymentMethodsViewHolder>(),
    PaymentMethodsFragment.ItemTouchHelperConnector {

    companion object {
        private const val TAG = "PaymentMethodsRVA"
    }

    private var itemsList = ArrayList<Item>()

    fun setList(map: HashMap<String, Item>) {
        val list = ArrayList(map.values.sortedBy {
            it.position
        })
        Log.d(TAG, "list= $list")
        itemsList = ArrayList(list)
        notifyDataSetChanged()
        recyclerView.scheduleLayoutAnimation()
    }

    private fun sortByPositionAndUpdateFirebase() {
        for (i in 0 until itemsList.size) {
            itemsList[i].position = i
            viewModel.updatePositions(itemsList[i].document_name, itemsList[i].toMap())
        }
    }

    fun changeEditMode(editMode: EditMode) {
        if (editMode == EditMode.OFF) {
            sortByPositionAndUpdateFirebase()
        }

        for (item in itemsList) {
            item.editMode = editMode
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodsViewHolder {
        val inflater: LayoutInflater? = LayoutInflater.from(parent.context)
        val view = inflater!!.inflate(R.layout.item_payment_method, parent, false)
        return PaymentMethodsViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentMethodsViewHolder, position: Int) {
        holder.setUp(
            position,
            itemsList[position].card_number,
            itemsList[position].editMode
        )

        holder.deleteIV.setOnClickListener {
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                if (DialogInterface.BUTTON_POSITIVE == which) {
                    Toast.makeText(context, R.string.card_removed, Toast.LENGTH_SHORT).show()
                    viewModel.remove(itemsList[position].document_name)
                    itemsList.removeAt(position)
                    notifyDataSetChanged()
                }
            }
            AlertDialog.Builder(context)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener)
                .show()
        }
    }

    override fun getItemCount() = itemsList.size


    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        Collections.swap(itemsList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(viewHolder: RecyclerView.ViewHolder?) {
    }

    override fun onRowClear(viewHolder: RecyclerView.ViewHolder) {
    }

    class PaymentMethodsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTV = itemView.findViewById<TextView>(R.id.titleTextView)
        private val typeTV = itemView.findViewById<TextView>(R.id.textView2)
        private val cardIcon = itemView.findViewById<ImageView>(R.id.cardIcon)
        private val masterCard = Regex("^5[1-5][0-9]{5,}\$")
        private val unfoldForeIV = itemView.findViewById<ImageView>(R.id.unfoldForeIV)
        val deleteIV = itemView.findViewById<ImageView>(R.id.deleteIV)!!

        fun setUp(
            position: Int,
            cardNumber: String,
            editMode: EditMode
        ) {
            titleTV.text = if (cardNumber.length < 12) "Brak"
            else itemView.context.getString(R.string.card, cardNumber.substring(12))

            if (masterCard.matches(cardNumber)) {
                cardIcon.setImageResource(R.drawable.logo_mastercard)
            } else {
                cardIcon.setImageResource(R.drawable.logo_visa)
            }

            typeTV.text = when (position) {
                0 -> itemView.context.getString(R.string.main_card)
                else -> itemView.context.getString(R.string.not_main_card)
            }

            when (editMode) {
                EditMode.ON -> {
                    unfoldForeIV.visibility = View.VISIBLE
                    deleteIV.visibility = View.VISIBLE
                }
                EditMode.OFF -> {
                    unfoldForeIV.visibility = View.INVISIBLE
                    deleteIV.visibility = View.INVISIBLE
                }
            }
        }

    }

    enum class EditMode {
        ON,
        OFF
    }

    data class Item(
        var document_name: String = "",
        val card_number: String = "",
        val exp_date: String = "",
        val cvv: String = "",
        var position: Int = 0,
        var editMode: EditMode = EditMode.OFF,
    ) {

        fun toMap(): Map<String, Any> {
            val dataMap = HashMap<String, Any>()
            dataMap["card_number"] = card_number
            dataMap["expiration_date"] = exp_date
            dataMap["cvv"] = cvv
            dataMap["position"] = position
            return dataMap
        }
    }

}