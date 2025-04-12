package pl.poznan.put.drinkwizard

import android.app.Application
import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import pl.poznan.put.drinkwizard.data.Recipe
import pl.poznan.put.drinkwizard.data.RecipeListItem

class MainViewModel(app: Application): AndroidViewModel(app) {
    private val repo = Repository(app.applicationContext)

    var selectedRecipe by mutableStateOf("Wybierz opcję")
        private set

    var isViewChange by mutableStateOf(false)
        private set

    var isViewList by mutableStateOf(true)
        private set

    var isWidgetVisible by mutableStateOf(false)
        private set

    var setWidgetTime by mutableStateOf("0")
        private set

    var widgetLastTime = "0"
        private set

    var timerSeconds by mutableIntStateOf(0)
        private set

    var isRunning by mutableStateOf(false)
        private set

    var timerState by mutableStateOf(TimerState.Stopped)
        private set

    var countDownTimer: CountDownTimer? by mutableStateOf(null)
        private set

    var shakeState by mutableStateOf(ShakerState.Start)
        private set

    var resetTimer by mutableStateOf(false)
        private set

    fun updateSelectedRecipe(value: String) {
        selectedRecipe = value
    }

    fun setIsViewChange(visible: Boolean) {
        isViewChange = visible
    }

    fun setIsViewList(visible: Boolean) {
        isViewList = visible
    }

    fun setWidgetVisibility(visible: Boolean) {
        isWidgetVisible = visible
    }

    fun updateSetWidgetTime(value: String) {
        setWidgetTime = value
    }

    fun updateWidgetLastTime(value: String) {
        widgetLastTime = value
    }

    fun setReset(value: Boolean) {
        resetTimer = value
    }

    fun updateTimerSeconds(value: Int) {
        timerSeconds = value
    }

    fun setIsRunning(visible: Boolean) {
        isRunning = visible
    }

    fun updateTimerState(value: TimerState) {
        timerState = value
    }

    fun updateCountDownTimer(value: CountDownTimer) {
        countDownTimer = value
    }

    fun updateShakeState(value: ShakerState) {
        shakeState = value
    }

    fun getRecipesList(): Flow<List<RecipeListItem>> {
        return repo.getRecipesList()
    }

    fun getRecipeInfo(name: String): Flow<Recipe> {
        return repo.getRecipeInfo(name)
    }

    fun editNote(name: String, note: String) {
        CoroutineScope(viewModelScope.coroutineContext).launch {
            repo.editNote(name, note)
        }
    }

    init {
        insertDatabase()
    }

    private fun insertDatabase() {
        val recipes: List<Recipe> = listOf(
            Recipe(name = "Mojito", ingredients = "40 ml białego rumu;20 ml soku z limonki;2 łyżeczki cukru trzcinowego;6-8 listków mięty;woda gazowana;kruszony lód", steps = "Do szklanki wrzuć miętę i cukier, a następnie delikatnie ugnieć muddlerem.;Wlej sok z limonki i dopełnij kruszonym lodem.;Dolej rum i delikatnie mieszaj (czas mieszania 30s).;Uzupełnij wodą gazowaną i udekoruj miętą.", shakingTime = 30, note = "", picture = "mojito"),
            Recipe(name = "Margarita", ingredients = "40 ml tequili; 20 ml likieru Triple Sec; 20 ml soku z limonki; sól do dekoracji; lód", steps = "Zwilż brzeg kieliszka limonką i obtocz w soli; W shakerze wymieszaj tequilę, Triple Sec, sok z limonki i lód (czas mieszania 30s); Przelej przez sitko do kieliszka i podawaj.", shakingTime = 30, note = "", picture = "margarita"),
            Recipe(name = "Pina Colada", ingredients = "40 ml białego rumu; 60 ml mleka kokosowego; 100 ml soku ananasowego; kruszony lód", steps = "Wszystkie składniki umieść w blenderze i miksuj (czas miksowania 70s) do uzyskania gładkiej konsystencji; Przelej do wysokiej szklanki i udekoruj kawałkiem ananasa.", shakingTime = 70, note = "", picture = "pina_colada"),
            Recipe(name = "Whisky Sour", ingredients = "50 ml whisky; 20 ml soku z cytryny; 15 ml syropu cukrowego; białko jajka (opcjonalnie); lód", steps = "W shakerze wymieszaj whisky, sok z cytryny, syrop cukrowy i białko (czas mieszania 45s); Wstrząśnij bez lodu, następnie dodaj lód i ponownie mocno wstrząśnij; Przelej do szklanki i udekoruj skórką cytryny.", shakingTime = 45, note = "", picture = "whisky_sour"),
            Recipe(name = "Caipirinha", ingredients = "50 ml cachaçy; 1 limonka; 2 łyżeczki cukru trzcinowego; kruszony lód", steps = "Pokrój limonkę na kawałki i wrzuć do szklanki; Dodaj cukier i ugnieć muddlerem; Wypełnij szklankę kruszonym lodem i dolej cachaçę; Wymieszaj (czas mieszania 20s) i podawaj.", shakingTime = 20, note = "", picture = "caipirinha"),
            Recipe(name = "Cosmopolitan", ingredients = "40 ml wódki cytrynowej; 20 ml likieru Triple Sec; 30 ml soku żurawinowego; 10 ml soku z limonki", steps = "W shakerze zmieszaj wszystkie składniki z lodem (czas mieszania 30s); Przelej do schłodzonego kieliszka koktajlowego; Udekoruj skórką pomarańczy.", shakingTime = 30, note = "", picture = "cosmopolitan"),
            Recipe(name = "Long Island Iced Tea", ingredients = "20 ml wódki; 20 ml rumu; 20 ml ginu; 20 ml tequili; 20 ml likieru Triple Sec; 20 ml soku z cytryny; 40 ml coli", steps = "Do szklanki z lodem wlej wszystkie alkohole i sok z cytryny; Delikatnie wymieszaj (czas mieszania 20s) i uzupełnij colą.", shakingTime = 20, note = "", picture = "long_island_iced_tea"),
            Recipe(name = "Sex on the Beach", ingredients =  "40 ml wódki; 20 ml likieru brzoskwiniowego; 50 ml soku pomarańczowego; 50 ml soku żurawinowego", steps = "Do szklanki z lodem wlej wszystkie składniki; Wymieszaj (czas mieszania 30s) i podawaj.", shakingTime = 30, note = "", picture = "sex_on_the_beach"),
            Recipe(name = "Blue Lagoon", ingredients = "40 ml wódki; 20 ml likieru Blue Curaçao; 100 ml lemoniady", steps = "Wlej składniki do szklanki z lodem; Delikatnie wymieszaj (czas mieszania 25s) i udekoruj cytryną.", shakingTime = 25, note = "", picture = "blue_lagoon"),
            Recipe(name = "Tequila Sunrise", ingredients = "40 ml tequili; 100 ml soku pomarańczowego; 10 ml grenadyny", steps = "Wlej tequilę i sok pomarańczowy do szklanki z lodem, a następnie zamieszaj (czas mieszania 5s); Powoli wlej grenadynę, aby opadła na dno.", shakingTime = 5, note = "", picture = "tequila_sunrise"),
            Recipe(name = "Daiquiri", ingredients = "50 ml białego rumu; 20 ml soku z limonki; 10 ml syropu cukrowego", steps = "Wymieszaj składniki w shakerze z lodem (czas mieszania 30s); Przelej do kieliszka i udekoruj limonką.", shakingTime = 30, note = "", picture = "daiquiri"),
            Recipe(name = "Negroni", ingredients = "30 ml ginu; 30 ml Campari; 30 ml czerwonego wermutu", steps = "Wlej wszystkie składniki do szklanki z lodem; Wymieszaj (czas mieszania 30s) i udekoruj skórką pomarańczy.", shakingTime = 30, note = "", picture = "negroni"),
            Recipe(name = "Espresso Martini", ingredients = "40 ml wódki; 20 ml likieru kawowego; 30 ml espresso; 10 ml syropu cukrowego", steps = "Wstrząśnij wszystkie składniki w shakerze z lodem (czas shakowania 50s); Przelej do kieliszka koktajlowego i udekoruj ziarnami kawy.", shakingTime = 50, note = "", picture = "espresso_martini"),
            Recipe(name = "Mai Tai", ingredients = "40 ml białego rumu; 20 ml ciemnego rumu; 20 ml likieru pomarańczowego; 20 ml soku z limonki; 10 ml syropu migdałowego", steps = "Wymieszaj składniki w shakerze z lodem (czas mieszania 35s); Przelej do szklanki i udekoruj miętą i limonką.", shakingTime = 35, note = "", picture = "mai_tai"),
            Recipe(name = "Cuba Libre", ingredients = "50 ml rumu; 100 ml coli; sok z ½ limonki", steps = "Do szklanki z lodem wlej rum i sok z limonki; Uzupełnij colą i delikatnie wymieszaj (czas mieszania 20s).", shakingTime = 20, note = "", picture = "cuba_libre"),
        )
        CoroutineScope(viewModelScope.coroutineContext).launch {
//            repo.clearRecipes()
            if(repo.getRecipesCount() == 0)
            {
                repo.insertAll(recipes)
            }
        }

    }
}