package pl.poznan.put.drinkwizard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val name: String,
    val ingredients: String,
    val steps: String,
    val shakingTime: Int,
    val note: String,
    val picture: String) {
}

data class RecipeListItem(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val name: String,
    val picture: String) {
}