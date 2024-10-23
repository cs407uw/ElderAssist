package com.cs407.lab5_milestone.data

import android.content.Context
import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import androidx.room.Upsert
import com.cs407.lab5_milestone.R
import java.util.Date

// Define your own @Entity, @Dao and @Database
@Entity(indices = [Index(value = ["userName"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val userName: String = "" // 用户名字段，带唯一索引
)
@Entity
data class Note(
    @PrimaryKey(autoGenerate = true) val noteId: Int = 0,
    val noteTitle: String, // 笔记标题
    val noteAbstract: String, // 笔记摘要
    @ColumnInfo(typeAffinity = ColumnInfo.TEXT) val noteDetail: String?, // 详细内容，可为null
    val notePath: String?, // 文件路径，可为null
    val lastEdited: Date // 笔记最后修改时间
)
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
@Entity(
    primaryKeys = ["userId", "noteId"],
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["userId"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Note::class, parentColumns = ["noteId"], childColumns = ["noteId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class UserNoteRelation(
    val userId: Int, // 外键，指向 User
    val noteId: Int  // 外键，指向 Note
)
data class NoteSummary(
    val noteId: Int,
    val noteTitle: String,
    val noteAbstract: String,
    val lastEdited: Date
)
@Dao
interface UserDao {

    @Query("SELECT * FROM user WHERE userName = :name")
    suspend fun getByName(name: String): User?

    @Query("SELECT * FROM user WHERE userId = :id")
    suspend fun getById(id: Int): User?

    @Query("""
        SELECT * FROM User, Note, UserNoteRelation
        WHERE User.userId = :id
        AND UserNoteRelation.userId = User.userId
        AND Note.noteId = UserNoteRelation.noteId
        ORDER BY Note.lastEdited DESC
    """)
    suspend fun getUsersWithNoteListsById(id: Int): List<NoteSummary>

    @Query("""
        SELECT * FROM User, Note, UserNoteRelation
        WHERE User.userId = :id
        AND UserNoteRelation.userId = User.userId
        AND Note.noteId = UserNoteRelation.noteId
        ORDER BY Note.lastEdited DESC
    """)
    fun getUsersWithNoteListsByIdPaged(id: Int): PagingSource<Int, NoteSummary>

    @Insert
    suspend fun insert(user: User): Long
}

@Dao
interface NoteDao {

    // 根据 noteId 获取 Note 实体
    @Query("SELECT * FROM note WHERE noteId = :id")
    suspend fun getById(id: Int): Note?

    // 根据 SQLite 的 rowId 获取 noteId
    @Query("SELECT noteId FROM note WHERE rowid = :rowId")
    suspend fun getByRowId(rowId: Long): Int

    // 插入或更新 Note，并返回 rowId
    @Upsert(entity=Note::class)
    suspend fun upsert(note: Note): Long

    // 插入 User 和 Note 之间的关系
    @Insert
    suspend fun insertRelation(userAndNote: UserNoteRelation)

    // 事务：插入或更新 Note，并更新用户和笔记的关系
    @Transaction
    suspend fun upsertNote(note: Note, userId: Int) {
        val rowId = upsert(note)
        if (note.noteId == 0) { // 如果 noteId 为 0，则是新笔记
            val noteId = getByRowId(rowId)
            insertRelation(UserNoteRelation(userId, noteId))
        }
    }

    // 查询某个用户的笔记数量
    @Query("""
        SELECT COUNT(*) 
        FROM User, Note, UserNoteRelation 
        WHERE User.userId = :userId 
        AND UserNoteRelation.userId = User.userId 
        AND Note.noteId = UserNoteRelation.noteId
    """)
    suspend fun userNoteCount(userId: Int): Int
}

@Dao
interface DeleteDao {

    @Query("DELETE FROM user WHERE userId = :userId")
    suspend fun deleteUser(userId: Int)

    @Query("""
        SELECT Note.noteId FROM User, Note, UserNoteRelation
        WHERE User.userId = :userId
        AND UserNoteRelation.userId = User.userId
        AND Note.noteId = UserNoteRelation.noteId
    """)
    suspend fun getAllNoteIdsByUser(userId: Int): List<Int>

    @Query("DELETE FROM note WHERE noteId IN (:notesIds)")
    suspend fun deleteNotes(notesIds: List<Int>)

    @Transaction
    suspend fun delete(userId: Int) {
        val notesIds = getAllNoteIdsByUser(userId)
        deleteNotes(notesIds)
        deleteUser(userId)
    }
}
@Database(entities = [User::class, Note::class, UserNoteRelation::class], version = 1)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
    abstract fun deleteDao(): DeleteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}