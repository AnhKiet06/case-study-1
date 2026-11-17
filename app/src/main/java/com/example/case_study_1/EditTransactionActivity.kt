package com.example.case_study_1

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.Locale

class EditTransactionActivity : AppCompatActivity() {

    private var isExpense = true
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        dbHelper = DatabaseHelper(this)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val tabExpense = findViewById<TextView>(R.id.tabExpense)
        val tabIncome = findViewById<TextView>(R.id.tabIncome)
        val boxCategory = findViewById<LinearLayout>(R.id.boxCategory)
        val ivCategoryIcon = findViewById<ImageView>(R.id.ivCategoryIcon)
        val tvCategory = findViewById<TextView>(R.id.tvCategory)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val boxDate = findViewById<LinearLayout>(R.id.boxDate)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val etNote = findViewById<EditText>(R.id.etNote)
        val btnDelete = findViewById<MaterialButton>(R.id.btnDelete)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        val transactionId = intent.getLongExtra("EXTRA_ID", -1L)
        val initialCategory = intent.getStringExtra("EXTRA_CATEGORY") ?: "Ăn uống"
        isExpense = intent.getBooleanExtra("EXTRA_IS_EXPENSE", true)

        tvCategory.text = initialCategory
        ivCategoryIcon.setImageResource(TransactionRepository.getCategoryIcon(initialCategory))
        etAmount.setText(intent.getStringExtra("EXTRA_AMOUNT") ?: "50000")
        tvDate.text = intent.getStringExtra("EXTRA_DATE") ?: "03/09/2024"
        etNote.setText(intent.getStringExtra("EXTRA_NOTE") ?: "Ăn trưa")

        fun updateTabUI() {
            if (isExpense) {
                tabExpense.setBackgroundResource(R.drawable.bg_tab_active)
                tabExpense.setTextColor(Color.WHITE)
                tabIncome.setBackgroundResource(R.drawable.bg_input_box)
                tabIncome.setTextColor(Color.parseColor("#0F172A"))
            } else {
                tabIncome.setBackgroundResource(R.drawable.bg_tab_active)
                tabIncome.setTextColor(Color.WHITE)
                tabExpense.setBackgroundResource(R.drawable.bg_input_box)
                tabExpense.setTextColor(Color.parseColor("#0F172A"))
            }
        }
        updateTabUI()

        btnBack.setOnClickListener { finish() }

        tabExpense.setOnClickListener {
            isExpense = true
            updateTabUI()
        }

        tabIncome.setOnClickListener {
            isExpense = false
            updateTabUI()
        }

        val categories = TransactionRepository.allCategories
        boxCategory.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Chọn danh mục")
                .setItems(categories) { _, which ->
                    val chosen = categories[which]
                    tvCategory.text = chosen
                    ivCategoryIcon.setImageResource(TransactionRepository.getCategoryIcon(chosen))
                    if (chosen == "Thu nhập") {
                        isExpense = false
                        updateTabUI()
                    }
                }
                .show()
        }

        boxDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                tvDate.text = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
            }, 2024, 8, 3).show()
        }

        btnDelete.setOnClickListener {
            if (transactionId == -1L) return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa giao dịch này khỏi cơ sở dữ liệu?")
                .setPositiveButton("Xóa") { _, _ ->
                    dbHelper.deleteTransaction(transactionId)
                    Toast.makeText(this, "Đã xóa giao dịch!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        btnSave.setOnClickListener {
            val rawAmount = etAmount.text.toString().replace(".", "").trim()
            val amountValue = rawAmount.toLongOrNull() ?: 0L
            if (amountValue <= 0L) {
                Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (transactionId != -1L) {
                val updatedItem = Transaction(
                    id = transactionId,
                    category = tvCategory.text.toString(),
                    amount = amountValue,
                    date = tvDate.text.toString(),
                    note = etNote.text.toString().trim(),
                    isExpense = isExpense
                )
                dbHelper.updateTransaction(updatedItem)
            }
            Toast.makeText(this, "Đã cập nhật giao dịch!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}