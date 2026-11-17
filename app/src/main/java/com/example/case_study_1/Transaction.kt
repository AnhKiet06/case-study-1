package com.example.case_study_1

import java.util.Locale

data class Transaction(
    val id: Long = 0L,
    val category: String,
    val amount: Long,
    val date: String,
    val note: String,
    val isExpense: Boolean
)

object TransactionRepository {
    val allCategories = arrayOf(
        "Ăn uống",
        "Di chuyển",
        "Thu nhập",
        "Mua sắm",
        "Giáo dục",
        "Hóa đơn"
    )

    fun formatMoney(amount: Long): String {
        return String.format(Locale.US, "%,d", amount).replace(',', '.') + " đ"
    }

    fun getCategoryIcon(category: String): Int {
        return when (category) {
            "Ăn uống" -> R.drawable.ic_cat_food
            "Di chuyển" -> R.drawable.ic_cat_transport
            "Thu nhập", "Lương" -> R.drawable.ic_cat_income
            "Mua sắm" -> R.drawable.ic_cat_shopping
            "Giáo dục" -> R.drawable.ic_cat_education
            "Hóa đơn" -> R.drawable.ic_cat_bill
            else -> R.drawable.ic_cat_food
        }
    }
}