package com.example.carsharing.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carsharing.R
import com.example.carsharing.ui.payment_methods.PaymentMethodsFragment

class HistoryOfRentalsFragment : Fragment() {

    private lateinit var viewModel: HistoryOfRentalsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: HistoryOfRentalsRecyclerViewAdapter
    private lateinit var viewManager: GridLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProvider(this).get(HistoryOfRentalsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_history_of_rentals, container, false)
        requireActivity().setTitle(R.string.history_of_rentals)

        viewManager = GridLayoutManager(requireContext(), 1)
        recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            addItemDecoration(PaymentMethodsFragment.PaymentMethodsItemDecoration())
        }
        viewAdapter = HistoryOfRentalsRecyclerViewAdapter(parentFragmentManager, recyclerView)
        recyclerView.adapter = viewAdapter

        viewModel.getRents().observe(viewLifecycleOwner, {
            viewAdapter.setList(it)
        })

        return root
    }
}