package com.dr.qck.activities

import android.os.Bundle
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dr.qck.activities.viewmodels.ExceptionViewModel
import com.dr.qck.adapter.ExceptionListAdapter
import com.dr.qck.database.ExceptionMessage
import com.dr.qck.databinding.ActivityExceptionListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExceptionList : AppCompatActivity() {
    private lateinit var binding: ActivityExceptionListBinding
    private lateinit var list: MutableList<ExceptionMessage>
    private val viewModel by viewModels<ExceptionViewModel>()
    private lateinit var adapter: ExceptionListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        binding = ActivityExceptionListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getExceptions()
        observeData()
    }

    private fun observeData() {
        viewModel.exceptionList.observe(this) { list ->
            this.list = list as MutableList<ExceptionMessage>
            if (list.isEmpty()) emptyList() else initRecyclerView()
        }
        viewModel.deleteException.observe(this) { position ->
            if (adapter.removeItem(position)) {
                emptyList()
            }
        }
    }

    private fun emptyList() {
        binding.emptyText.visibility = VISIBLE
    }

    private fun initRecyclerView() {
        adapter = ExceptionListAdapter(list) { position, message ->
            viewModel.deleteItem(position, message)
        }
        binding.listRecycler.layoutManager = LinearLayoutManager(this)
        binding.listRecycler.adapter = adapter
    }

    private fun getExceptions() {
        viewModel.getExceptionList()
    }
}