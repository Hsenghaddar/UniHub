package com.example.unihub

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * UserDatabaseHelper manages the local SQLite database for the UniHub application.
 * It handles tables for users, universities, marketplace items, and sales.
 */
class UserDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "unihub.db"
        private const val DATABASE_VERSION = 6

        // Table Names
        private const val TABLE_USERS = "users"
        private const val TABLE_UNIVERSITIES = "universities"
        private const val TABLE_MARKETPLACE = "marketplace_items"
        private const val TABLE_SALES = "sales"

        // Common Column Names
        private const val COL_ID = "id"
        private const val COL_FIREBASE_UID = "firebase_uid"
        private const val COL_FULL_NAME = "full_name"
        private const val COL_EMAIL = "email"
        private const val COL_UNIVERSITY_ID = "university_id"
        private const val COL_UNIVERSITY_NAME = "university_name"
        private const val COL_USER_IMAGE_URI = "user_image_uri"

        // Marketplace specific columns
        private const val COL_ITEM_TITLE = "title"
        private const val COL_ITEM_DESCRIPTION = "description"
        private const val COL_ITEM_CATEGORY = "category"
        private const val COL_ITEM_PRICE = "price"
        private const val COL_ITEM_STOCK = "stock"
        private const val COL_ITEM_USER_UID = "user_uid"
        private const val COL_ITEM_IMAGE_URI = "image_uri"
        private const val COL_HAS_NOTIFICATION = "has_notification"

        // Sales specific columns
        private const val COL_SALE_ITEM_ID = "item_id"
        private const val COL_SALE_BUYER_UID = "buyer_uid"
        private const val COL_SALE_QUANTITY = "quantity"
        private const val COL_SALE_TOTAL_PRICE = "total_price"
        private const val COL_SALE_TIMESTAMP = "timestamp"
    }

    /**
     * Called when the database is created for the first time.
     * Defines the schema for all tables and seeds the universities table.
     */
    override fun onCreate(db: SQLiteDatabase) {
        val createUniversitiesTable = """
            CREATE TABLE $TABLE_UNIVERSITIES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_UNIVERSITY_NAME TEXT UNIQUE NOT NULL
            )
        """.trimIndent()

        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FIREBASE_UID TEXT UNIQUE NOT NULL,
                $COL_FULL_NAME TEXT NOT NULL,
                $COL_EMAIL TEXT UNIQUE NOT NULL,
                $COL_UNIVERSITY_ID INTEGER NOT NULL,
                $COL_USER_IMAGE_URI TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COL_UNIVERSITY_ID) REFERENCES $TABLE_UNIVERSITIES($COL_ID)
            )
        """.trimIndent()

        val createMarketplaceTable = """
            CREATE TABLE $TABLE_MARKETPLACE (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ITEM_TITLE TEXT NOT NULL,
                $COL_ITEM_DESCRIPTION TEXT NOT NULL,
                $COL_ITEM_CATEGORY TEXT NOT NULL,
                $COL_ITEM_PRICE REAL NOT NULL,
                $COL_ITEM_STOCK INTEGER NOT NULL,
                $COL_ITEM_USER_UID TEXT NOT NULL,
                $COL_ITEM_IMAGE_URI TEXT,
                $COL_HAS_NOTIFICATION INTEGER DEFAULT 0,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COL_ITEM_USER_UID) REFERENCES $TABLE_USERS($COL_FIREBASE_UID)
            )
        """.trimIndent()

        val createSalesTable = """
            CREATE TABLE $TABLE_SALES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SALE_ITEM_ID INTEGER NOT NULL,
                $COL_SALE_BUYER_UID TEXT NOT NULL,
                $COL_SALE_QUANTITY INTEGER NOT NULL,
                $COL_SALE_TOTAL_PRICE REAL NOT NULL,
                $COL_SALE_TIMESTAMP TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COL_SALE_ITEM_ID) REFERENCES $TABLE_MARKETPLACE($COL_ID)
            )
        """.trimIndent()

        db.execSQL(createUniversitiesTable)
        db.execSQL(createUsersTable)
        db.execSQL(createMarketplaceTable)
        db.execSQL(createSalesTable)

        seedUniversities(db)
    }

    /**
     * Handles database schema upgrades between versions.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_MARKETPLACE ADD COLUMN $COL_ITEM_IMAGE_URI TEXT")
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE $TABLE_MARKETPLACE ADD COLUMN $COL_HAS_NOTIFICATION INTEGER DEFAULT 0")
        }
        if (oldVersion < 5) {
            val createSalesTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_SALES (
                    $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_SALE_ITEM_ID INTEGER NOT NULL,
                    $COL_SALE_BUYER_UID TEXT NOT NULL,
                    $COL_SALE_QUANTITY INTEGER NOT NULL,
                    $COL_SALE_TOTAL_PRICE REAL NOT NULL,
                    $COL_SALE_TIMESTAMP TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY ($COL_SALE_ITEM_ID) REFERENCES $TABLE_MARKETPLACE($COL_ID)
                )
            """.trimIndent()
            db.execSQL(createSalesTable)
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_USER_IMAGE_URI TEXT")
        }
    }

    /**
     * Seeds the universities table with a predefined list of Lebanese universities.
     */
    private fun seedUniversities(db: SQLiteDatabase) {
        val universities = listOf(
            "AUB", "LAU", "UOB", "BAU", "LU", "NDU", "USEK", "USJ", "RHU", "UA", "HU", "IUL", "AOU", "AUL", "JU", "MU", "OU", "MEU", "LIU", "MUBS", "PU", "TUT", "CU", "GU", "LCU", "MUT"
        )
        for (uni in universities) {
            val values = ContentValues().apply { put(COL_UNIVERSITY_NAME, uni) }
            db.insert(TABLE_UNIVERSITIES, null, values)
        }
    }

    /**
     * Returns a list of all university names.
     */
    fun getAllUniversities(): List<String> {
        val list = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_UNIVERSITY_NAME FROM $TABLE_UNIVERSITIES ORDER BY $COL_UNIVERSITY_NAME ASC", null)
        if (cursor.moveToFirst()) {
            do { list.add(cursor.getString(0)) } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    /**
     * Retrieves the database ID of a university by its name.
     */
    fun getUniversityIdByName(name: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_ID FROM $TABLE_UNIVERSITIES WHERE $COL_UNIVERSITY_NAME = ?", arrayOf(name))
        var id = -1
        if (cursor.moveToFirst()) id = cursor.getInt(0)
        cursor.close()
        return id
    }

    /**
     * Retrieves the university name by its database ID.
     */
    fun getUniversityNameById(universityId: Int): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_UNIVERSITY_NAME FROM $TABLE_UNIVERSITIES WHERE $COL_ID = ?", arrayOf(universityId.toString()))
        var name = ""
        if (cursor.moveToFirst()) name = cursor.getString(0)
        cursor.close()
        return name
    }

    /**
     * Inserts a new user record into the database.
     */
    fun insertUser(firebaseUid: String, fullName: String, email: String, universityId: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_FIREBASE_UID, firebaseUid)
            put(COL_FULL_NAME, fullName)
            put(COL_EMAIL, email)
            put(COL_UNIVERSITY_ID, universityId)
        }
        return db.insert(TABLE_USERS, null, values) != -1L
    }

    /**
     * Fetches a local user object by their Firebase UID.
     */
    fun getUserByFirebaseUid(firebaseUid: String): LocalUser? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_FIREBASE_UID, $COL_FULL_NAME, $COL_EMAIL, $COL_UNIVERSITY_ID, $COL_USER_IMAGE_URI FROM $TABLE_USERS WHERE $COL_FIREBASE_UID = ?", arrayOf(firebaseUid))
        var user: LocalUser? = null
        if (cursor.moveToFirst()) {
            user = LocalUser(
                firebaseUid = cursor.getString(0), 
                fullName = cursor.getString(1), 
                email = cursor.getString(2), 
                universityId = cursor.getInt(3),
                imageUri = cursor.getString(4)
            )
        }
        cursor.close()
        return user
    }

    /**
     * Updates user profile information.
     */
    fun updateUser(firebaseUid: String, fullName: String, universityId: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_FULL_NAME, fullName)
            put(COL_UNIVERSITY_ID, universityId)
        }
        return db.update(TABLE_USERS, values, "$COL_FIREBASE_UID = ?", arrayOf(firebaseUid)) > 0
    }

    /**
     * Updates the local path for the user's profile image.
     */
    fun updateUserImage(firebaseUid: String, imageUri: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_IMAGE_URI, imageUri)
        }
        return db.update(TABLE_USERS, values, "$COL_FIREBASE_UID = ?", arrayOf(firebaseUid)) > 0
    }

    /**
     * Counts how many items a specific user has posted in the marketplace.
     */
    fun getMarketplaceCountForUser(firebaseUid: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_MARKETPLACE WHERE $COL_ITEM_USER_UID = ?", arrayOf(firebaseUid))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    /**
     * Updates the stock level of a marketplace item.
     */
    fun updateMarketplaceStock(itemId: Int, newStock: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { 
            put(COL_ITEM_STOCK, newStock)
            put(COL_HAS_NOTIFICATION, 1) 
        }
        return db.update(TABLE_MARKETPLACE, values, "$COL_ID = ?", arrayOf(itemId.toString())) > 0
    }

    /**
     * Records a new sale in the sales table.
     */
    fun recordSale(itemId: Int, buyerUid: String, quantity: Int, totalPrice: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SALE_ITEM_ID, itemId)
            put(COL_SALE_BUYER_UID, buyerUid)
            put(COL_SALE_QUANTITY, quantity)
            put(COL_SALE_TOTAL_PRICE, totalPrice)
        }
        val result = db.insert(TABLE_SALES, null, values)
        if (result != -1L) {
            setMarketplaceNotification(itemId, true)
        }
        return result != -1L
    }

    /**
     * Retrieves all sales records for items posted by a specific user.
     */
    fun getSalesForUser(userUid: String): List<Sale> {
        val list = mutableListOf<Sale>()
        val db = readableDatabase
        val query = """
            SELECT s.*, m.$COL_ITEM_TITLE, u.$COL_FULL_NAME as buyer_name 
            FROM $TABLE_SALES s 
            JOIN $TABLE_MARKETPLACE m ON s.$COL_SALE_ITEM_ID = m.$COL_ID 
            JOIN $TABLE_USERS u ON s.$COL_SALE_BUYER_UID = u.$COL_FIREBASE_UID 
            WHERE m.$COL_ITEM_USER_UID = ? 
            ORDER BY s.$COL_SALE_TIMESTAMP DESC
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(userUid))
        if (cursor.moveToFirst()) {
            do {
                list.add(Sale(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    itemId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SALE_ITEM_ID)),
                    itemTitle = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_TITLE)),
                    buyerUid = cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_BUYER_UID)),
                    buyerName = cursor.getString(cursor.getColumnIndexOrThrow("buyer_name")),
                    quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SALE_QUANTITY)),
                    totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SALE_TOTAL_PRICE)),
                    timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_TIMESTAMP))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    /**
     * Updates the notification status for a marketplace item.
     */
    fun setMarketplaceNotification(itemId: Int, hasNotification: Boolean): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply { put(COL_HAS_NOTIFICATION, if (hasNotification) 1 else 0) }
        return db.update(TABLE_MARKETPLACE, values, "$COL_ID = ?", arrayOf(itemId.toString())) > 0
    }

    /**
     * Inserts a new marketplace item into the database.
     */
    fun insertMarketplaceItem(title: String, description: String, category: String, price: Double, stock: Int, userUid: String, imageUri: String? = null): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ITEM_TITLE, title)
            put(COL_ITEM_DESCRIPTION, description)
            put(COL_ITEM_CATEGORY, category)
            put(COL_ITEM_PRICE, price)
            put(COL_ITEM_STOCK, stock)
            put(COL_ITEM_USER_UID, userUid)
            put(COL_ITEM_IMAGE_URI, imageUri)
        }
        return db.insert(TABLE_MARKETPLACE, null, values) != -1L
    }

    /**
     * Retrieves all marketplace items available in the database.
     */
    fun getAllMarketplaceItems(): List<MarketplaceItem> {
        val list = mutableListOf<MarketplaceItem>()
        val db = readableDatabase
        val query = "SELECT m.*, u.$COL_FULL_NAME as creator_name FROM $TABLE_MARKETPLACE m LEFT JOIN $TABLE_USERS u ON m.$COL_ITEM_USER_UID = u.$COL_FIREBASE_UID ORDER BY m.created_at DESC"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToMarketplaceItem(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    /**
     * Retrieves marketplace items posted by a specific user.
     */
    fun getMarketplaceItemsByUser(userUid: String): List<MarketplaceItem> {
        val list = mutableListOf<MarketplaceItem>()
        val db = readableDatabase
        val query = "SELECT m.*, u.$COL_FULL_NAME as creator_name FROM $TABLE_MARKETPLACE m LEFT JOIN $TABLE_USERS u ON m.$COL_ITEM_USER_UID = u.$COL_FIREBASE_UID WHERE m.$COL_ITEM_USER_UID = ? ORDER BY m.created_at DESC"
        val cursor = db.rawQuery(query, arrayOf(userUid))
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToMarketplaceItem(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    /**
     * Fetches a single marketplace item by its database ID.
     */
    fun getMarketplaceItemById(itemId: Int): MarketplaceItem? {
        val db = readableDatabase
        val query = "SELECT m.*, u.$COL_FULL_NAME as creator_name FROM $TABLE_MARKETPLACE m LEFT JOIN $TABLE_USERS u ON m.$COL_ITEM_USER_UID = u.$COL_FIREBASE_UID WHERE m.$COL_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(itemId.toString()))
        var item: MarketplaceItem? = null
        if (cursor.moveToFirst()) {
            item = cursorToMarketplaceItem(cursor)
        }
        cursor.close()
        return item
    }

    /**
     * Helper method to convert a database cursor into a MarketplaceItem object.
     */
    private fun cursorToMarketplaceItem(cursor: android.database.Cursor): MarketplaceItem {
        return MarketplaceItem(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_TITLE)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_DESCRIPTION)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_CATEGORY)),
            price = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_ITEM_PRICE)),
            stock = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_STOCK)),
            userUid = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_USER_UID)),
            creatorName = cursor.getString(cursor.getColumnIndexOrThrow("creator_name")),
            imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_IMAGE_URI)),
            hasNotification = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HAS_NOTIFICATION)) == 1
        )
    }

    /**
     * Updates an existing marketplace item's details.
     */
    fun updateMarketplaceItem(itemId: Int, title: String, description: String, category: String, price: Double, stock: Int): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ITEM_TITLE, title)
            put(COL_ITEM_DESCRIPTION, description)
            put(COL_ITEM_CATEGORY, category)
            put(COL_ITEM_PRICE, price)
            put(COL_ITEM_STOCK, stock)
        }
        return db.update(TABLE_MARKETPLACE, values, "$COL_ID = ?", arrayOf(itemId.toString())) > 0
    }

    /**
     * Deletes a marketplace item from the database.
     */
    fun deleteMarketplaceItem(itemId: Int): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_MARKETPLACE, "$COL_ID = ?", arrayOf(itemId.toString())) > 0
    }
}

/**
 * Data class representing a Sale record.
 */
data class Sale(
    val id: Int,
    val itemId: Int,
    val itemTitle: String,
    val buyerUid: String,
    val buyerName: String,
    val quantity: Int,
    val totalPrice: Double,
    val timestamp: String
)
