package pl.poznan.put.drinkwizard.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
public interface RecipesDao {

    @Insert
    suspend fun insertAll(recipes: List<Recipe>)

    @Query("SELECT uid, name, picture FROM recipes")
    fun getRecipesList(): Flow<List<RecipeListItem>>

    @Query("SELECT * FROM recipes WHERE name = :name")
    fun getRecipeInfo(name: String): Flow<Recipe>

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getRecipesCount(): Int

    @Query("DELETE FROM recipes")
    suspend fun clearRecipes()

    @Query("UPDATE recipes SET note = :note WHERE name = :name")
    suspend fun editNote(name: String, note: String)


}
