package com.cs407.elderassist_tutorial.data

import android.content.Context

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

//travel information
@Entity(tableName = "travel_information")
data class TravelInformation(
    @PrimaryKey(autoGenerate = true) val travelId: Int = 0,
    val destinationName: String,
    val description: String,
    val attractions: String,
    val bestTimeToVisit: String,
    val transportOptions: String
)

@Dao
interface TravelInformationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTravelInformation(travelInformation: TravelInformation)

    @Query("SELECT * FROM travel_information WHERE destinationName = :name")
    suspend fun getTravelInformationByName(name: String): TravelInformation?

    @Query("SELECT * FROM travel_information")
    suspend fun getAllTravelInformation(): List<TravelInformation>
}

//map
@Entity(tableName = "saved_location")
data class SavedLocation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double
)
//map
@Dao
interface SavedLocationDao {

    @Insert
    suspend fun insertD(location: SavedLocation)

    @Query("SELECT * FROM saved_location")
    suspend fun getAllLocations(): List<SavedLocation>

    @Query("DELETE FROM saved_location WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Entity(indices = [Index(value = ["userName"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val userName: String,
    val passwd: String,
    val randomInfo: String // 随机生成的用户信息
)
@Entity
data class Pharmacy(
    @PrimaryKey val pharmacyId: Int, // 药房ID固定，从CSV读取
    val pharmacyName: String,
    val address: String,
    val website: String?,
    val operatingHours: String?,
    val phone: String?,
    val rating: String?,
    val insuranceLink: String?
)
@Entity(indices = [Index(value = ["medicineName"], unique = true)])
data class Medication(
    @PrimaryKey(autoGenerate = true) val medicationId: Int = 0,
    val medicineName: String,
    val medicationDescription: String // 药品信息从CSV中读取
)
@Entity(
    primaryKeys = ["pharmacyId", "medicationId"],
    foreignKeys = [
        ForeignKey(entity = Pharmacy::class, parentColumns = ["pharmacyId"], childColumns = ["pharmacyId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Medication::class, parentColumns = ["medicationId"], childColumns = ["medicationId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class PharmacyMedication(
    val pharmacyId: Int,
    val medicationId: Int
)
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM user WHERE userName = :userName")
    suspend fun getUserByName(userName: String): User?

    @Query("SELECT randomInfo FROM user WHERE userName = :userName")
    suspend fun getRandomInfoByUserName(userName: String): String?

    @Query("SELECT passwd FROM user WHERE userName = :userName")
    suspend fun getPasswdByUserName(userName: String): String?

    @Query("DELETE FROM user WHERE userId = :userId")
    suspend fun deleteUser(userId: Int)

    @Query("UPDATE user SET randomInfo = :randomInfo WHERE userId = :userId")
    suspend fun updateRandomInfo(userId: Int, randomInfo: String)

    @Query("SELECT * FROM user WHERE userId = :userId")
    suspend fun getUserById(userId: Int): User?
}
@Dao
interface PharmacyDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPharmacy(pharmacy: Pharmacy)

    @Query("SELECT * FROM pharmacy WHERE pharmacyName = :name")
    suspend fun getPharmacyByName(name: String): Pharmacy?

    @Query("SELECT * FROM pharmacy WHERE pharmacyId = :id")
    suspend fun getPharmacyById(id: Int): Pharmacy?

    @Query("SELECT * FROM Pharmacy")
    suspend fun getAllPharmacies(): List<Pharmacy> // 获取所有药房
    @Query("SELECT COUNT(*) FROM Pharmacy")
    suspend fun countPharmacies(): Int

}
@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMedication(medication: Medication): Long

    @Query("SELECT * FROM medication WHERE medicineName = :name")
    suspend fun getMedicationByName(name: String): Medication?

    @Query("SELECT * FROM Medication")
    suspend fun getAllMedications(): List<Medication> // 获取所有药品
}
@Dao
interface PharmacyMedicationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPharmacyMedication(pharmacyMedication: PharmacyMedication)

    @Query("""
        SELECT m.* FROM medication m
        JOIN PharmacyMedication pm ON m.medicationId = pm.medicationId
        WHERE pm.pharmacyId = :pharmacyId
    """)
    suspend fun getMedicationsByPharmacy(pharmacyId: Int): List<Medication>

    @Query("""
        SELECT p.* FROM pharmacy p
        JOIN PharmacyMedication pm ON p.pharmacyId = pm.pharmacyId
        WHERE pm.medicationId = :medicationId
    """)
    suspend fun getPharmaciesByMedication(medicationId: Int): List<Pharmacy>

    @Query("SELECT * FROM PharmacyMedication")
    suspend fun getAllPharmacyMedications(): List<PharmacyMedication>
}
@Database(
    entities = [User::class, Pharmacy::class, Medication::class, PharmacyMedication::class, SavedLocation::class, TravelInformation::class],
    version = 3
)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun pharmacyDao(): PharmacyDao
    abstract fun medicationDao(): MedicationDao
    abstract fun pharmacyMedicationDao(): PharmacyMedicationDao
    abstract fun savedLocationDao(): SavedLocationDao
    abstract fun travelInformationDao(): TravelInformationDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // Enable migration in case of schema changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

