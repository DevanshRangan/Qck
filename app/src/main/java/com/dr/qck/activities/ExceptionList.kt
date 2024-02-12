package com.dr.qck.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dr.qck.R
import com.dr.qck.activities.viewmodels.ExceptionViewModel
import com.dr.qck.adapter.ExceptionListAdapter
import com.dr.qck.application.QckApplication
import com.dr.qck.database.ExceptionDao
import com.dr.qck.database.ExceptionMessage
import com.dr.qck.databinding.ActivityExceptionListBinding
import com.dr.qck.databinding.NewExceptionDialogLayoutBinding
import com.dr.qck.utils.plain
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExceptionList : AppCompatActivity() {
    private val dao: ExceptionDao = QckApplication.instance.dao
    private lateinit var binding: ActivityExceptionListBinding
    private var list: ArrayList<ExceptionMessage> = arrayListOf()
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
        initRecyclerView()
        getExceptions()
        observeData()
        initOnClick()
    }

    private fun initOnClick() {
        binding.backArrow.setOnClickListener { finish() }
        binding.addException.setOnClickListener { showNewExceptionDialog() }
    }

    private fun showNewExceptionDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
        val binding = NewExceptionDialogLayoutBinding.inflate(layoutInflater)
        dialog.setTitle(getString(R.string.add_new_exception))
        dialog.setView(binding.root)
        dialog.setNegativeButton(getString(R.string.cancel)) { d, _ ->
            d.dismiss()
        }
        dialog.setPositiveButton(getString(R.string.add)) { _, _ ->
            if (binding.senderEdittext.text.toString().plain() != "") {
                CoroutineScope(Dispatchers.IO).launch {
                    dao.addToException(
                        ExceptionMessage(
                            System.currentTimeMillis(), binding.senderEdittext.text.toString()
                        )
                    )
                    getExceptions()
                }
                Toast.makeText(this, getString(R.string.added_successfully), Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, getString(R.string.empty_sender), Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeData() {
        viewModel.exceptionList.observe(this) { list ->
            this.list.clear()
            this.list.addAll(list)
            if (list.isEmpty()) emptyList() else adapter.notifyDataSetChanged()
                .also { emptyList(false) }
        }
        viewModel.deleteException.observe(this) { position ->
            if (adapter.removeItem(position)) {
                emptyList()
            }
        }
    }

    private fun emptyList(isIt: Boolean = true) {
        binding.emptyText.visibility = if (isIt) VISIBLE else GONE
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