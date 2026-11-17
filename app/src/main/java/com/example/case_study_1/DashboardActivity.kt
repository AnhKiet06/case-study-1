package com.example.case_study_1

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DashboardActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    // Header & 3 khu vực màn hình
    private lateinit var tvHeaderTitle: TextView
    private lateinit var layoutHomeSection: ConstraintLayout
    private lateinit var layoutTransactionsSection: LinearLayout
    private lateinit var layoutStatsSection: ScrollView
    private lateinit var fabAdd: FloatingActionButton

    // Tab 1: Trang chủ
    private lateinit var tvTotalBalance: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpense: TextView
    private lateinit var recentAdapter: TransactionAdapter
    private var isBalanceHidden = false
    private var currentBalanceText = "0 đ"

    // Tab 2: Tất cả giao dịch (tìm kiếm & lọc)
    private lateinit var etSearchTransaction: EditText
    private lateinit var tvFilterSummary: TextView
    private lateinit var allAdapter: TransactionAdapter
    private var currentFilterMode = 0 // 0: Tất cả, 1: Chi tiêu, 2: Thu nhập

    // Tab 3: Thống kê
    private lateinit var tvStatsRatioText: TextView
    private lateinit var progressExpenseRatio: ProgressBar
    private lateinit var tvStatsIncome: TextView
    private lateinit var tvStatsExpense: TextView
    private lateinit var containerCategoryStats: LinearLayout

    // Bottom Navigation
    private lateinit var ivNavHome: ImageView
    private lateinit var tvNavHome: TextView
    private lateinit var ivNavTransactions: ImageView
    private lateinit var tvNavTransactions: TextView
    private lateinit var ivNavStats: ImageView
    private lateinit var tvNavStats: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        dbHelper = DatabaseHelper(this)
        bindViews()
        setupAdapters()
        setupListeners()
    }

    private fun bindViews() {
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
        layoutHomeSection = findViewById(R.id.layoutHomeSection)
        layoutTransactionsSection = findViewById(R.id.layoutTransactionsSection)
        layoutStatsSection = findViewById(R.id.layoutStatsSection)
        fabAdd = findViewById(R.id.fabAdd)

        tvTotalBalance = findViewById(R.id.tvTotalBalance)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpense = findViewById(R.id.tvTotalExpense)

        etSearchTransaction = findViewById(R.id.etSearchTransaction)
        tvFilterSummary = findViewById(R.id.tvFilterSummary)

        tvStatsRatioText = findViewById(R.id.tvStatsRatioText)
        progressExpenseRatio = findViewById(R.id.progressExpenseRatio)
        tvStatsIncome = findViewById(R.id.tvStatsIncome)
        tvStatsExpense = findViewById(R.id.tvStatsExpense)
        containerCategoryStats = findViewById(R.id.containerCategoryStats)

        ivNavHome = findViewById(R.id.ivNavHome)
        tvNavHome = findViewById(R.id.tvNavHome)
        ivNavTransactions = findViewById(R.id.ivNavTransactions)
        tvNavTransactions = findViewById(R.id.tvNavTransactions)
        ivNavStats = findViewById(R.id.ivNavStats)
        tvNavStats = findViewById(R.id.tvNavStats)
    }

    private fun setupAdapters() {
        val openEdit: (Transaction) -> Unit = { item ->
            val intent = Intent(this, EditTransactionActivity::class.java).apply {
                putExtra("EXTRA_ID", item.id)
                putExtra("EXTRA_CATEGORY", item.category)
                putExtra("EXTRA_AMOUNT", item.amount.toString())
                putExtra("EXTRA_DATE", item.date)
                putExtra("EXTRA_NOTE", item.note)
                putExtra("EXTRA_IS_EXPENSE", item.isExpense)
            }
            startActivity(intent)
        }

        val confirmDelete: (Transaction) -> Unit = { item ->
            val title = item.note.ifEmpty { item.category }
            AlertDialog.Builder(this)
                .setTitle("Xóa giao dịch")
                .setMessage("Bạn có muốn xóa \"$title\" (${TransactionRepository.formatMoney(item.amount)})?")
                .setPositiveButton("Xóa") { _, _ ->
                    dbHelper.deleteTransaction(item.id)
                    Toast.makeText(this, "Đã xóa giao dịch!", Toast.LENGTH_SHORT).show()
                    refreshAllScreens()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        recentAdapter = TransactionAdapter(mutableListOf(), openEdit, confirmDelete)
        findViewById<RecyclerView>(R.id.rvTransactions).apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = recentAdapter
        }

        allAdapter = TransactionAdapter(mutableListOf(), openEdit, confirmDelete)
        findViewById<RecyclerView>(R.id.rvAllTransactions).apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = allAdapter
        }
    }

    private fun setupListeners() {
        // Ẩn/hiện số dư
        findViewById<ImageView>(R.id.ivToggleBalance).setOnClickListener {
            isBalanceHidden = !isBalanceHidden
            tvTotalBalance.text = if (isBalanceHidden) "•••••••• đ" else currentBalanceText
        }

        // Nút thêm giao dịch (+)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        // Nút "Xem tất cả" -> mở tab Giao dịch
        findViewById<TextView>(R.id.tvSeeAll).setOnClickListener {
            switchTab(1)
        }

        // Chuyển đổi 3 tab thanh điều hướng dưới
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener { switchTab(0) }
        findViewById<LinearLayout>(R.id.navTransactions).setOnClickListener { switchTab(1) }
        findViewById<LinearLayout>(R.id.navStats).setOnClickListener { switchTab(2) }

        // Lọc trong tab Giao dịch
        val filterAll = findViewById<TextView>(R.id.filterAll)
        val filterExpense = findViewById<TextView>(R.id.filterExpense)
        val filterIncome = findViewById<TextView>(R.id.filterIncome)

        fun updateFilterTabs(mode: Int) {
            currentFilterMode = mode
            val tabs = listOf(filterAll, filterExpense, filterIncome)
            tabs.forEachIndexed { index, tv ->
                if (index == mode) {
                    tv.setBackgroundResource(R.drawable.bg_tab_active)
                    tv.setTextColor(Color.WHITE)
                } else {
                    tv.setBackgroundResource(R.drawable.bg_input_box)
                    tv.setTextColor(Color.parseColor("#0F172A"))
                }
            }
            applyTransactionFilter()
        }

        filterAll.setOnClickListener { updateFilterTabs(0) }
        filterExpense.setOnClickListener { updateFilterTabs(1) }
        filterIncome.setOnClickListener { updateFilterTabs(2) }

        // Tìm kiếm theo từ khóa trong tab Giao dịch
        etSearchTransaction.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyTransactionFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun switchTab(index: Int) {
        layoutHomeSection.visibility = if (index == 0) View.VISIBLE else View.GONE
        layoutTransactionsSection.visibility = if (index == 1) View.VISIBLE else View.GONE
        layoutStatsSection.visibility = if (index == 2) View.VISIBLE else View.GONE
        fabAdd.visibility = if (index == 2) View.GONE else View.VISIBLE

        val activeColor = Color.parseColor("#2563EB")
        val inactiveColor = Color.parseColor("#64748B")

        ivNavHome.setColorFilter(if (index == 0) activeColor else inactiveColor)
        tvNavHome.setTextColor(if (index == 0) activeColor else inactiveColor)
        tvNavHome.setTypeface(null, if (index == 0) Typeface.BOLD else Typeface.NORMAL)

        ivNavTransactions.setColorFilter(if (index == 1) activeColor else inactiveColor)
        tvNavTransactions.setTextColor(if (index == 1) activeColor else inactiveColor)
        tvNavTransactions.setTypeface(null, if (index == 1) Typeface.BOLD else Typeface.NORMAL)

        ivNavStats.setColorFilter(if (index == 2) activeColor else inactiveColor)
        tvNavStats.setTextColor(if (index == 2) activeColor else inactiveColor)
        tvNavStats.setTypeface(null, if (index == 2) Typeface.BOLD else Typeface.NORMAL)

        tvHeaderTitle.text = when (index) {
            0 -> "Quản lý thu chi"
            1 -> "Tất cả giao dịch"
            else -> "Thống kê thu chi"
        }
    }

    override fun onResume() {
        super.onResume()
        refreshAllScreens()
    }

    private fun refreshAllScreens() {
        val totalIncome = dbHelper.getTotalAmount(isExpense = false)
        val totalExpense = dbHelper.getTotalAmount(isExpense = true)
        val balance = totalIncome - totalExpense

        // 1. Cập nhật Tab Trang chủ
        currentBalanceText = TransactionRepository.formatMoney(balance)
        tvTotalIncome.text = TransactionRepository.formatMoney(totalIncome)
        tvTotalExpense.text = TransactionRepository.formatMoney(totalExpense)
        tvTotalBalance.text = if (isBalanceHidden) "•••••••• đ" else currentBalanceText

        val allList = dbHelper.getAllTransactions()
        recentAdapter.updateData(allList.take(5))

        // 2. Cập nhật Tab Tất cả giao dịch
        applyTransactionFilter()

        // 3. Cập nhật Tab Thống kê
        updateStatisticsTab(allList, totalIncome, totalExpense)
    }

    private fun applyTransactionFilter() {
        val keyword = etSearchTransaction.text.toString().trim()
        val baseList = if (keyword.isEmpty()) {
            dbHelper.getAllTransactions()
        } else {
            dbHelper.searchTransactions(keyword)
        }

        val filtered = when (currentFilterMode) {
            1 -> baseList.filter { it.isExpense }
            2 -> baseList.filter { !it.isExpense }
            else -> baseList
        }

        tvFilterSummary.text = "Danh sách giao dịch (${filtered.size})"
        allAdapter.updateData(filtered)
    }

    private fun updateStatisticsTab(
        allList: List<Transaction>,
        totalIncome: Long,
        totalExpense: Long
    ) {
        tvStatsIncome.text = "Thu: ${TransactionRepository.formatMoney(totalIncome)}"
        tvStatsExpense.text = "Chi: ${TransactionRepository.formatMoney(totalExpense)}"

        val ratio = if (totalIncome > 0L) {
            ((totalExpense * 100) / totalIncome).toInt().coerceIn(0, 100)
        } else if (totalExpense > 0L) {
            100
        } else {
            0
        }
        progressExpenseRatio.progress = ratio
        tvStatsRatioText.text = "Đã chi $ratio% trên tổng thu nhập"

        // Nhóm các giao dịch chi tiêu theo danh mục
        containerCategoryStats.removeAllViews()
        val expenseItems = allList.filter { it.isExpense }
        if (expenseItems.isEmpty() || totalExpense <= 0L) {
            val emptyTv = TextView(this).apply {
                text = "Chưa có dữ liệu chi tiêu để thống kê."
                setTextColor(Color.parseColor("#64748B"))
                textSize = 13f
            }
            containerCategoryStats.addView(emptyTv)
            return
        }

        val grouped = expenseItems
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        val inflater = LayoutInflater.from(this)
        for ((category, sumAmount) in grouped) {
            val row = inflater.inflate(R.layout.item_category_stat, containerCategoryStats, false)
            val percent = ((sumAmount * 100) / totalExpense).toInt().coerceIn(1, 100)

            row.findViewById<ImageView>(R.id.ivStatIcon)
                .setImageResource(TransactionRepository.getCategoryIcon(category))
            row.findViewById<TextView>(R.id.tvStatCategory).text = category
            row.findViewById<TextView>(R.id.tvStatPercent).text = "$percent% tổng chi tiêu"
            row.findViewById<TextView>(R.id.tvStatAmount).text =
                TransactionRepository.formatMoney(sumAmount)
            row.findViewById<ProgressBar>(R.id.progressStatCategory).progress = percent

            containerCategoryStats.addView(row)
        }
    }
}