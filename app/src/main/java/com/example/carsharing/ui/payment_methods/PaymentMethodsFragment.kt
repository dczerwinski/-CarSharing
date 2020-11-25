package com.example.carsharing.ui.payment_methods

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.carsharing.R
import com.example.carsharing.ui.MainActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PaymentMethodsFragment : Fragment() {

    private lateinit var paymentMethodsViewModel: PaymentMethodsViewModel
    private lateinit var fab: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: PaymentMethodsRecyclerViewAdapter
    private lateinit var viewManager: GridLayoutManager
    private var callback: ItemMoveCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        paymentMethodsViewModel =
            ViewModelProvider(this).get(PaymentMethodsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_payment_methods, container, false)
        requireActivity().setTitle(R.string.payment_menthods)

        fab = root.findViewById(R.id.fab)
        fab.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            transaction.replace(
                R.id.nav_host_fragment,
                AddNewMethodFragment.newInstance(viewAdapter.itemCount)
            )
            transaction.addToBackStack(null)
            transaction.commit()
        }

        viewManager = GridLayoutManager(requireContext(), 1)

        (requireActivity() as MainActivity).updateApi(object : MainActivity.EditModeListener {
            override fun setEditMode(editMode: PaymentMethodsRecyclerViewAdapter.EditMode) {
                viewAdapter.changeEditMode(editMode)
                when (editMode) {
                    PaymentMethodsRecyclerViewAdapter.EditMode.ON -> callback!!.isEnabled = true
                    PaymentMethodsRecyclerViewAdapter.EditMode.OFF -> callback!!.isEnabled = false
                }
            }
        })

        recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            addItemDecoration(PaymentMethodsItemDecoration())
        }
        viewAdapter = PaymentMethodsRecyclerViewAdapter(
            requireContext(),
            paymentMethodsViewModel,
            recyclerView
        )
        recyclerView.adapter = viewAdapter

        callback = ItemMoveCallback()
        val itemTouchHelper = ItemTouchHelper(callback!!)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        paymentMethodsViewModel.getCards().observe(viewLifecycleOwner, {
            viewAdapter.setList(it)
        })

        return root
    }

    interface ItemTouchHelperConnector {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(viewHolder: RecyclerView.ViewHolder?)
        fun onRowClear(viewHolder: RecyclerView.ViewHolder)
    }

    private class ItemMoveCallback(
        var isEnabled: Boolean = false
    ) : ItemTouchHelper.Callback() {

        override fun isLongPressDragEnabled(): Boolean = isEnabled
        override fun isItemViewSwipeEnabled(): Boolean = isEnabled

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int = makeMovementFlags(ItemTouchHelper.DOWN or ItemTouchHelper.UP, 0)

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder?.itemView?.alpha = 0.5f
                viewHolder?.itemView?.bringToFront()
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            viewHolder.itemView.alpha = 1.0f
            recyclerView.post(recyclerView.adapter!!::notifyDataSetChanged)
            super.clearView(recyclerView, viewHolder)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            (recyclerView.adapter as PaymentMethodsRecyclerViewAdapter)
                .onRowMoved(viewHolder.adapterPosition, target.adapterPosition)
            return isEnabled
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            return
        }
    }

    class PaymentMethodsItemDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.top = 10
            outRect.bottom = 10
            outRect.left = 10
            outRect.right = 10
        }
    }
}