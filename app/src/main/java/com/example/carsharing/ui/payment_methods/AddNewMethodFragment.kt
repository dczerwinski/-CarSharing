package com.example.carsharing.ui.payment_methods

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.braintreepayments.cardform.view.CardForm
import com.example.carsharing.R
import com.example.carsharing.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class AddNewMethodFragment : Fragment() {

    private lateinit var cardForm: CardForm
    private lateinit var addButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private var listSize: String = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listSize = it.getString(LIST_SIZE_KEY)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_add_new_method, container, false)
        (requireActivity() as MainActivity).updateBackButtonAction(
            PaymentMethodsFragment(),
            parentFragmentManager
        )
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        setHasOptionsMenu(false)
        cardForm = root.findViewById(R.id.card_form)
        cardForm.cardRequired(true)
            .expirationRequired(true)
            .cvvRequired(true)
            .setup(requireActivity() as AppCompatActivity)

        addButton = root.findViewById(R.id.addButton)
        addButton.setOnClickListener {
            if (cardForm.isValid) {
                val uid = auth.currentUser!!.uid

                val cardItem = PaymentMethodsRecyclerViewAdapter.Item(
                    "",
                    cardForm.cardNumber,
                    cardForm.expirationDateEditText.text.toString(),
                    cardForm.cvv,
                    listSize.toInt()
                )
                cardItem.document_name = listSize

                db.getReference("users_data")
                    .child(uid)
                    .child("cards")
                    .child(listSize)
                    .setValue(cardItem)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            R.string.added_card,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            requireContext(),
                            R.string.can_not_add_card,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.nav_host_fragment, PaymentMethodsFragment())
                transaction.addToBackStack(null)
                transaction.commit()
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.complete_all_fields,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return root
    }

    companion object {
        const val LIST_SIZE_KEY = "KEY"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment AddNewMethodFragment.
         */
        @JvmStatic
        fun newInstance(listSize: Int) =
            AddNewMethodFragment().apply {
                arguments = Bundle()
                requireArguments().putString(LIST_SIZE_KEY, listSize.toString())
            }
    }
}