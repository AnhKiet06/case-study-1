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
import java.util.Calendar
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {

    private var isExpense = true
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

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
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        // Mặc định hiển thị ngày hôm nay
        val cal = Calendar.getInstance()
        tvDate.text = String.format(
            Locale.getDefault(),
            "%02d/%02d/%d",
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR)
        )

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
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    tvDate.text = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnSave.setOnClickListener {
            val rawAmount = etAmount.text.toString().replace(".", "").trim()
            val amountValue = rawAmount.toLongOrNull() ?: 0L
            if (amountValue <= 0L) {
                Toast.makeText(this, "Vui lòng nhập số tiền hợp lệ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newItem = Transaction(
                category = tvCategory.text.toString(),
                amount = amountValue,
                date = tvDate.text.toString(),
                note = etNote.text.toString().trim(),
                isExpense = isExpense
            )

            val insertedId = dbHelper.addTransaction(newItem)
            if (insertedId > 0) {
                Toast.makeText(this, "Đã lưu giao dịch!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Lỗi khi lưu dữ liệu!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}