package pl.poznan.put.drinkwizard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Recipe::class], version = 1)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipesDio(): RecipesDao
}

object RecipesDb {
    private var db: RecipeDatabase? = null
    fun getInstance(context: Context): RecipeDatabase {

        if(db == null){
            db = Room.databaseBuilder(context, RecipeDatabase::class.java, "recipes-database").build()
        }
        return db!!
    }
}