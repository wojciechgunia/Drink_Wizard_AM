package pl.poznan.put.drinkwizard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Recipe::class], version = 1)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipesDio(): RecipesDao

    companion object {
        @Volatile private var db: RecipeDatabase? = null
        fun getInstance(context: Context): RecipeDatabase =
            db ?: synchronized(this) {
                db ?: Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "user_db"
                ).build().also { db = it }
            }
    }
}