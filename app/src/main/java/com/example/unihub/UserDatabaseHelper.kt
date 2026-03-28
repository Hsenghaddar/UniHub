package com.example.unihub

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "unihub.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_USERS = "users"
        private const val TABLE_UNIVERSITIES = "universities"

        private const val COL_ID = "id"
        private const val COL_FIREBASE_UID = "firebase_uid"
        private const val COL_FULL_NAME = "full_name"
        private const val COL_EMAIL = "email"
        private const val COL_UNIVERSITY_ID = "university_id"
        private const val COL_UNIVERSITY_NAME = "university_name"
    }

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
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($COL_UNIVERSITY_ID) REFERENCES $TABLE_UNIVERSITIES($COL_ID)
            )
        """.trimIndent()

        db.execSQL(createUniversitiesTable)
        db.execSQL(createUsersTable)

        seedUniversities(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_UNIVERSITIES")
        onCreate(db)
    }

    private fun seedUniversities(db: SQLiteDatabase) {
        val universities = listOf(
            "AUB",
            "LAU",
            "UOB",
            "BAU",
            "LU",
            "NDU",
            "USEK",
            "USJ",
            "RHU",
            "UA",
            "HU",
            "IUL",
            "AOU",
            "AUL",
            "JU",
            "MU",
            "OU",
            "MEU",
            "LIU",
            "MUBS",
            "PU",
            "TUT",
            "CU",
            "GU",
            "LCU",
            "MUT"
        )

        for (uni in universities) {
            val values = ContentValues().apply {
                put(COL_UNIVERSITY_NAME, uni)
            }
            db.insert(TABLE_UNIVERSITIES, null, values)
        }
    }

    fun getAllUniversities(): List<String> {
        val list = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_UNIVERSITY_NAME FROM $TABLE_UNIVERSITIES ORDER BY $COL_UNIVERSITY_NAME ASC",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    fun getUniversityIdByName(name: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_ID FROM $TABLE_UNIVERSITIES WHERE $COL_UNIVERSITY_NAME = ?",
            arrayOf(name)
        )

        var id = -1
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0)
        }

        cursor.close()
        return id
    }

    fun getUniversityNameById(universityId: Int): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_UNIVERSITY_NAME FROM $TABLE_UNIVERSITIES WHERE $COL_ID = ?",
            arrayOf(universityId.toString())
        )

        var name = ""
        if (cursor.moveToFirst()) {
            name = cursor.getString(0)
        }

        cursor.close()
        return name
    }

    fun insertUser(
        firebaseUid: String,
        fullName: String,
        email: String,
        universityId: Int
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_FIREBASE_UID, firebaseUid)
            put(COL_FULL_NAME, fullName)
            put(COL_EMAIL, email)
            put(COL_UNIVERSITY_ID, universityId)
        }

        return db.insert(TABLE_USERS, null, values) != -1L
    }

    fun getUserByFirebaseUid(firebaseUid: String): LocalUser? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT $COL_FIREBASE_UID, $COL_FULL_NAME, $COL_EMAIL, $COL_UNIVERSITY_ID
            FROM $TABLE_USERS
            WHERE $COL_FIREBASE_UID = ?
            """.trimIndent(),
            arrayOf(firebaseUid)
        )

        var user: LocalUser? = null

        if (cursor.moveToFirst()) {
            user = LocalUser(
                firebaseUid = cursor.getString(0),
                fullName = cursor.getString(1),
                email = cursor.getString(2),
                universityId = cursor.getInt(3)
            )
        }

        cursor.close()
        return user
    }
    fun updateUser(
        firebaseUid: String,
        fullName: String,
        universityId: Int
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_FULL_NAME, fullName)
            put(COL_UNIVERSITY_ID, universityId)
        }

        return db.update(
            TABLE_USERS,
            values,
            "$COL_FIREBASE_UID = ?",
            arrayOf(firebaseUid)
        ) > 0
    }

    fun getMarketplaceCountForUser(firebaseUid: String): Int {
        return 0
    }

    fun getRideCountForUser(firebaseUid: String): Int {
        return 0
    }

}