package pl.poznan.put.drinkwizard

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import pl.poznan.put.drinkwizard.data.Recipe
import pl.poznan.put.drinkwizard.data.RecipeDatabase
import pl.poznan.put.drinkwizard.data.RecipeListItem
import pl.poznan.put.drinkwizard.data.RecipesDao

class Repository(context: Context): RecipesDao {
    private val dao = RecipeDatabase.getInstance(context).recipesDio()
    override suspend fun insertAll(recipes: List<Recipe>) = withContext(Dispatchers.IO) {
        dao.insertAll(recipes)
    }

    override suspend fun editNote(recipeName: String, note: String) = withContext(Dispatchers.IO) {
        dao.editNote(recipeName, note)
    }

    override fun getRecipesList(): Flow<List<RecipeListItem>> {
        return dao.getRecipesList()
    }

    override fun getRecipeInfo(name: String): Flow<Recipe> {
        return dao.getRecipeInfo(name)
    }

    override suspend  fun getRecipesCount(): Int {
        return withContext(Dispatchers.IO) {
            dao.getRecipesCount()
        }
    }

    override suspend fun clearRecipes() = withContext(Dispatchers.IO) {
        dao.clearRecipes()
    }

}