package com.example.case_study_1

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "expense_manager.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_TRANSACTIONS = "transactions"
        const val COLUMN_ID = "id"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_DATE = "date"
        const val COLUMN_NOTE = "note"
        const val COLUMN_IS_EXPENSE = "is_expense"
    }

    // Chạy duy nhất 1 lần khi database chưa tồn tại trên thiết bị
    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_AMOUNT INTEGER NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_NOTE TEXT,
                $COLUMN_IS_EXPENSE INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)

        // Khởi tạo 5 giao dịch mẫu (xếp từ cũ đến mới để ORDER BY id DESC hiển thị mới nhất lên đầu)
        insertInitialRow(db, "Giáo dục", 500000L, "30/08/2024", "Học phí", true)
        insertInitialRow(db, "Mua sắm", 300000L, "31/08/2024", "Mua sắm", true)
        insertInitialRow(db, "Thu nhập", 8000000L, "01/09/2024", "Lương tháng 9", false)
        insertInitialRow(db, "Di chuyển", 100000L, "03/09/2024", "Xăng xe", true)
        insertInitialRow(db, "Ăn uống", 50000L, "03/09/2024", "Ăn trưa", true)
    }

    private fun insertInitialRow(
        db: SQLiteDatabase,
        category: String,
        amount: Long,
        date: String,
        note: String,
        isExpense: Boolean
    ) {
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY, category)
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_DATE, date)
            put(COLUMN_NOTE, note)
            put(COLUMN_IS_EXPENSE, if (isExpense) 1 else 0)
        }
        db.insert(TABLE_TRANSACTIONS, null, values)
    }

    // Tự động kích hoạt khi tăng số DATABASE_VERSION
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        onCreate(db)
    }

    // 1. CREATE: Thêm một giao dịch mới, trả về ID dòng vừa tạo (-1 nếu lỗi)
    fun addTransaction(transaction: Transaction): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY, transaction.category)
            put(COLUMN_AMOUNT, transaction.amount)
            put(COLUMN_DATE, transaction.date)
            put(COLUMN_NOTE, transaction.note)
            put(COLUMN_IS_EXPENSE, if (transaction.isExpense) 1 else 0)
        }
        return db.insert(TABLE_TRANSACTIONS, null, values)
    }

    // 2. READ: Lấy toàn bộ danh sách giao dịch sắp xếp mới nhất lên trên
    fun getAllTransactions(): MutableList<Transaction> {
        val list = mutableListOf<Transaction>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_TRANSACTIONS ORDER BY $COLUMN_ID DESC"

        db.rawQuery(query, null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    list.add(readTransactionFromCursor(cursor))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    // 3. UPDATE: Cập nhật giao dịch theo khóa chính ID
    fun updateTransaction(transaction: Transaction): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY, transaction.category)
            put(COLUMN_AMOUNT, transaction.amount)
            put(COLUMN_DATE, transaction.date)
            put(COLUMN_NOTE, transaction.note)
            put(COLUMN_IS_EXPENSE, if (transaction.isExpense) 1 else 0)
        }
        return db.update(
            TABLE_TRANSACTIONS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(transaction.id.toString())
        )
    }

    // 4. DELETE: Xóa giao dịch theo khóa chính ID
    fun deleteTransaction(id: Long): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_TRANSACTIONS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    // 5. AGGREGATE: Tính tổng Thu nhập (isExpense = false) hoặc Tổng Chi tiêu (isExpense = true) bằng SQL
    fun getTotalAmount(isExpense: Boolean): Long {
        val db = readableDatabase
        val flag = if (isExpense) "1" else "0"
        val query = "SELECT COALESCE(SUM($COLUMN_AMOUNT), 0) FROM $TABLE_TRANSACTIONS WHERE $COLUMN_IS_EXPENSE = ?"

        db.rawQuery(query, arrayOf(flag)).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(0)
            }
        }
        return 0L
    }

    // 6. SEARCH: Tìm kiếm giao dịch theo từ khóa ghi chú hoặc danh mục
    fun searchTransactions(keyword: String): MutableList<Transaction> {
        val list = mutableListOf<Transaction>()
        val db = readableDatabase
        val pattern = "%${keyword.trim()}%"
        val query = """
            SELECT * FROM $TABLE_TRANSACTIONS
            WHERE $COLUMN_CATEGORY LIKE ? OR $COLUMN_NOTE LIKE ?
            ORDER BY $COLUMN_ID DESC
        """.trimIndent()

        db.rawQuery(query, arrayOf(pattern, pattern)).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    list.add(readTransactionFromCursor(cursor))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    private fun readTransactionFromCursor(cursor: Cursor): Transaction {
        return Transaction(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
            amount = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)),
            date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
            note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE)) ?: "",
            isExpense = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_EXPENSE)) == 1
        )
    }
}