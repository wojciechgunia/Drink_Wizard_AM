package pl.poznan.put.drinkwizard

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import pl.poznan.put.drinkwizard.data.Recipe
import pl.poznan.put.drinkwizard.data.RecipeListItem
import pl.poznan.put.drinkwizard.ui.theme.DrinkWizardTheme
import java.util.Locale
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val mainVm by viewModels<MainViewModel>()
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_DrinkWizard)
        super.onCreate(savedInstanceState)
        val isTablet = resources.configuration.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        if (!isTablet) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            setContent {
                DrinkWizardTheme {
                    val navController = rememberNavController()
                    AppNavHost(navController, mainVm)
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    if (mainVm.isSheetOpen) {
                        AppInfoBottomSheet(
                            sheetState = sheetState,
                            isTablet = true,
                            onDismiss = { mainVm.setWigSheetOpen(false) }
                        )
                    }
                }
            }
        } else {
            setContent {
                DrinkWizardTheme {
                    val navController = rememberNavController()
                    AppNavHost(navController, mainVm, true)
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    if (mainVm.isSheetOpen) {
                        AppInfoBottomSheet(
                            sheetState = sheetState,
                            isTablet = true,
                            onDismiss = { mainVm.setWigSheetOpen(false) }
                        )
                    }
                }
            }
        }
    }
}

//====================================== Nav Hosts ===============================================

@Composable
fun AppNavHost(navController: NavHostController, mainVm: MainViewModel, isTablet: Boolean = false) {
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController, mainVm, isTablet) }
        composable("home-tab") { HomeTabScreen(navController, mainVm) }
    }
}

@Composable
fun AppNavHome(navController: NavHostController, mainVm: MainViewModel, isTablet: Boolean) {
    var isBigger = 1.0f
    if(isTablet)
        isBigger = 1.8f
    NavHost(navController, startDestination = "list") {
        composable("list") {
            RecipesList(mainVm, navController, isBigger = isBigger)
        }
        composable("recipe") {
            RecipeInfo(mainVm, navController, isBigger = isBigger)
            if (mainVm.selectedRecipe != "Wybierz opcję") {
                val recipe = mainVm.getRecipeInfo(mainVm.selectedRecipe)
                    .collectAsState(Recipe(0, "", "", "", 0, "", "")).value
                ShareFabButton(
                    ingredients = recipe.ingredients, name = recipe.name, isTablet = isTablet
                )
            }
        }
        composable("timer") {
            if(mainVm.setWidgetTime != mainVm.widgetLastTime)
            {
                mainVm.setReset(true)
                mainVm.updateWidgetLastTime(mainVm.setWidgetTime)
            }
            TimerScreen(mainVm, isBigger = isBigger, navController = navController)
            mainVm.setReset(false)
        }
    }
}

//====================================== Home Screen ===============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, mainVm: MainViewModel, isTablet: Boolean) {
    val configuration = LocalConfiguration.current
    if (configuration.orientation != Configuration.ORIENTATION_PORTRAIT) {
        navController.navigate("home-tab") {
            popUpTo("home") { inclusive = true }
        }
        if(mainVm.isViewList)
        {
            mainVm.updateSelectedRecipe("Wybierz opcję")
        }


    }
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        )
        {
            val navController = rememberNavController()
            HeaderComp(mainVm, isTablet, navController)
            AppNavHome(navController, mainVm, isTablet)
        }
    }
}

@Composable
fun HomeTabScreen(navController: NavHostController, mainVm: MainViewModel) {
    val configuration = LocalConfiguration.current
    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        mainVm.setIsViewChange(true)
        navController.navigate("home") {
            popUpTo("home-tab") { inclusive = true }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column (
            modifier = Modifier.fillMaxSize()
        ){
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp))
            {
                HeaderComp(mainVm, true, navController)
            }
            Row(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp))
            {

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(11f)
                ) {
                    RecipesList(mainVm, null, true, isBigger = 1.2f)
                }

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(16f)
                ) {
                    if(mainVm.selectedRecipe == "Wybierz opcję") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = mainVm.selectedRecipe, fontSize = 22.sp)
                        }

                    } else {
                        RecipeInfo(
                            mainVm,
                            isTablet = true,
                            isBigger = 1.2f
                        )
                    }
                }
            }
        }
        if(mainVm.setWidgetTime != mainVm.widgetLastTime)
        {
            mainVm.setReset(true)
            mainVm.updateWidgetLastTime(mainVm.setWidgetTime)
        }
        SlideInWidget(
            mainVm
        )
        if((mainVm.selectedRecipe != "Wybierz opcję" && !mainVm.isWidgetVisible)){
            val recipe = mainVm.getRecipeInfo(mainVm.selectedRecipe)
                .collectAsState(Recipe(0, "", "", "", 0, "", "")).value
            ShareFabButton(
                ingredients = recipe.ingredients, name = recipe.name, isTablet = true,
            )
        }
    }

}

//====================================== Header component ===============================================

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("DiscouragedApi")
@Composable
fun HeaderComp(mainVm: MainViewModel, isTablet: Boolean = false, navController: NavHostController? = null) {
    var isBigger = 1.0f
    val configuration = LocalConfiguration.current
    if (isTablet && configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        isBigger = 1.5f
    }
    var isHome = true
    if(navController != null) {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        if(currentRoute == "recipe" || currentRoute == "timer")
            isHome = false
    }
    val context = LocalContext.current
    val imageId = context.resources.getIdentifier("logo", "drawable", context.packageName)
    val isMainScreen = navController == null || configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || isHome
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp * isBigger)
            .padding(bottom = 20.dp * isBigger),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(
            targetState = isMainScreen,
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally { it } + fadeIn())
                        .togetherWith(slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn())
                        .togetherWith(slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "AnimatedLeftContent"
        ) { main ->
            if (main) {
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(50.dp * isBigger)
                        .clickable { mainVm.setWigSheetOpen(true) }
                )
            } else {
                Button(
                    onClick = {
                        navController?.popBackStack()
                        if(mainVm.isWidgetVisible && mainVm.selectedRecipe != "Wybierz opcję")
                        {
                            mainVm.setWidgetVisibility(!mainVm.isWidgetVisible)
                        } else if(!mainVm.isWidgetVisible && mainVm.selectedRecipe != "Wybierz opcję") {
                            mainVm.setIsViewList(true)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x00000000)),
                    shape = RoundedCornerShape(8.dp * isBigger),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_back),
                        contentDescription = "wróć",
                        modifier = Modifier.size(32.dp * isBigger),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Text(
            text = "Drink Wizard",
            fontSize = 28.sp * isBigger,
            fontWeight = FontWeight.Bold
        )

        AnimatedContent(
            targetState = isMainScreen,
            transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally { it } + fadeIn())
                            .togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn())
                            .togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
            },
            label = "AnimatedRightContent"
        ) { main ->
            if (main) {
                Spacer(Modifier.width(20.dp * isBigger)) // rezerwacja miejsca
            } else {
                Image(
                    painter = painterResource(id = imageId),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(50.dp * isBigger)
                        .clickable { mainVm.setWigSheetOpen(true) }
                )
            }
        }
    }
}

//====================================== Drink List Component ===============================================

@Composable
fun RecipesList(
    mainVm: MainViewModel,
    navController: NavController? = null,
    isTablet: Boolean = false,
    isBigger: Float = 1.0f
)
{
    if(mainVm.selectedRecipe != "Wybierz opcję" && navController != null && mainVm.isViewChange) {
        mainVm.setIsViewList(false)
        navController.popBackStack()
        navController.navigate("list") {
            launchSingleTop = true
        }
        navController.navigate("recipe") {
            launchSingleTop = true
        }
        if(mainVm.isWidgetVisible) {
            navController.navigate("timer") {
                launchSingleTop = true
            }
        }
        mainVm.setIsViewChange(false)
    }
    val recipesList = mainVm.getRecipesList().collectAsState(initial = emptyList())
    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RecipesLazyColumn(recipesList.value, navController, isTablet, isBigger, mainVm)
    }
}

@Composable
fun RecipesLazyColumn(
    recipesList: List<RecipeListItem>,
    navController: NavController?,
    isTablet: Boolean,
    isBigger: Float,
    mainVm: MainViewModel
) {
    LazyColumn (modifier = Modifier
        .fillMaxSize()
        .padding(10.dp * isBigger)){
        items(items = recipesList, key = { it.uid }) { recipe ->
            RecipesRow(recipe, navController, isTablet,isBigger, mainVm)
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun RecipesRow(
    recipeItem: RecipeListItem,
    navController: NavController?,
    isTablet: Boolean,
    isBigger: Float,
    mainVm: MainViewModel
) {
    val imageResId = LocalContext.current.resources.getIdentifier(recipeItem.picture, "drawable", LocalContext.current.packageName)
    Surface (
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp * isBigger)
            .padding(vertical = 10.dp * isBigger)
            .border(1.dp * isBigger, Color.LightGray, shape = RoundedCornerShape(10.dp * isBigger))
            .clickable {
                mainVm.updateSelectedRecipe(recipeItem.name)
                if (navController != null && !isTablet) {
                    navController.navigate("recipe")
                    mainVm.setIsViewList(false)
                }

            },
        shape = RoundedCornerShape(10.dp * isBigger),
        tonalElevation = 1.dp * isBigger,
    ) {
        Row(
            modifier = Modifier
                .background(Color(0xAA35A3C0))
                .padding(horizontal = 16.dp * isBigger),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (imageResId != 0) {
                Row (modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = "Drink photo",
                        modifier = Modifier
                            .size(124.dp * isBigger)
                            .padding(end = 20.dp * isBigger)
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Row (modifier = Modifier.weight(2f),
                horizontalArrangement = Arrangement.Start) {
                Text(
                    text = recipeItem.name,
                    fontSize = 21.sp * isBigger,
                    lineHeight = 22.sp * isBigger,
                    fontWeight = FontWeight.W600,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

    }
}

// ================================= Recipe Info Component ============================================
@Composable
fun RecipeInfo(
    mainVm: MainViewModel,
    navController: NavHostController? = null,
    isTablet: Boolean = false,
    isBigger: Float = 1.0f
) {
    if(navController != null) {
        BackHandler {
            mainVm.setIsViewList(true)
            navController.popBackStack()
        }
    }
    val recipe = mainVm.getRecipeInfo(mainVm.selectedRecipe).collectAsState(initial = Recipe(name="", ingredients = "", steps = "", shakingTime = 0, note= "", picture = "")).value
    if(recipe.name != "") {
        RecipeShow(mainVm, recipe, navController, isTablet, isBigger)
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun RecipeShow(
    mainVm: MainViewModel,
    recipe: Recipe,
    navController: NavHostController?,
    isTablet: Boolean,
    isBigger: Float,
) {
    val imageResId = LocalContext.current.resources.getIdentifier(recipe.picture, "drawable", LocalContext.current.packageName)
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp * isBigger)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = "Drink photo",
                        modifier = Modifier
                            .size(200.dp * isBigger)
                            .padding(1.dp * isBigger)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 25.dp * isBigger)
                    .drawBehind {
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 3.dp.toPx() * isBigger
                        )
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
                
            ) {
                Text(
                    text = recipe.name,
                    fontSize = 28.sp * isBigger,
                    fontWeight = FontWeight.Medium)
            }
        }
        item {
            Text(text = "Składniki:", fontSize = 23.sp * isBigger, fontWeight = FontWeight.Medium)
            val ingredients = recipe.ingredients.split(";")
            ingredients.forEach { ingredient ->
                Text(text = "• $ingredient", modifier = Modifier.padding(4.dp * isBigger), fontSize = 17.sp * isBigger, lineHeight = 21.sp * isBigger)
            }
        }
        item {
            Text(text = "Przepis:", fontSize = 23.sp * isBigger, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 10.dp * isBigger))
            val steps = recipe.steps.split(";")
            var i = 1
            steps.forEach { step ->
                Text(text = "$i. $step", modifier = Modifier.padding(4.dp * isBigger), fontSize = 17.sp * isBigger, lineHeight = 21.sp * isBigger)
                i++
            }
        }
        item {
            Button(
                onClick = {
                        mainVm.updateSetWidgetTime(recipe.shakingTime.toString())
                        mainVm.setWidgetVisibility(true)
                        if(navController != null && !isTablet) {
                            navController.navigate("timer")
                        }
                    },
                modifier = Modifier
                    .padding(15.dp * isBigger)
                    .size(width = 220.dp * isBigger, height = 55.dp * isBigger),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xAA35A3C0)),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp * isBigger, Color.LightGray)
            ) {
                Icon(painter = painterResource(id = R.drawable.timer), contentDescription = "Timer Icon", modifier = Modifier.size(30.dp * isBigger), tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp * isBigger))
                Text(text = "Timer ${recipe.shakingTime}s" , fontSize = 23.sp * isBigger, color = Color.White)
            }
        }
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            EditNoteComponent(recipe, isBigger, mainVm)
            Spacer(modifier = Modifier.height(40.dp * isBigger))
        }
    }
}

@Composable
fun EditNoteComponent(recipe: Recipe, isBigger: Float, mainVm: MainViewModel?) {
    var isEditing by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf(recipe.note) }

    val textColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Notatki:",
            fontSize = 23.sp * isBigger,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 10.dp * isBigger)
        )
        Button(
            onClick = {
                if(isEditing && mainVm != null) {
                    mainVm.editNote(recipe.name,noteText)
                }
                isEditing = !isEditing
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xAA35A3C0)),
            shape = RoundedCornerShape(8.dp * isBigger),
            border = BorderStroke(1.dp * isBigger, Color.LightGray)
        ) {
            Icon(
                painter = painterResource(id = if (isEditing) R.drawable.save else R.drawable.edit),
                contentDescription = if (isEditing) "Save Icon" else "Edit Icon",
                modifier = Modifier.size(28.dp * isBigger),
                tint = Color.White
            )
        }
    }

    if (isEditing) {
        BasicTextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp * isBigger)
                .heightIn(min = 50.dp * isBigger)
                .border(1.dp * isBigger, textColor, RoundedCornerShape(4.dp * isBigger)),
            textStyle = TextStyle(fontSize = 19.sp * isBigger, color = textColor),
            maxLines = Int.MAX_VALUE,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.padding(8.dp * isBigger)
                ) {
                    innerTextField()
                }
            }
        )
    } else {
        Text(
            text = noteText,
            modifier = Modifier.padding(4.dp * isBigger),
            fontSize = 19.sp * isBigger,
            lineHeight = 22.sp * isBigger
        )
    }
}

@Composable
fun ShareFabButton(ingredients: String, name: String, isTablet: Boolean) {
    var isBigger = 1.0f
    if(isTablet)
        isBigger = 1.5f
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize().padding(top = 24.dp, end = 10.dp).zIndex(2f),
        contentAlignment = Alignment.TopEnd,
    ) {
        FloatingActionButton(
            onClick = {
                val message = buildString {
                    append("Lista składników ($name):\n")
                    ingredients.split(";").forEach { ingredient ->
                        append("● $ingredient\n")
                    }
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                }
                val chooser = Intent.createChooser(intent, "Udostępnij składniki przez:")
                context.startActivity(chooser)
            },
            modifier = Modifier.size(55.dp * isBigger),
            containerColor = Color(0xFF35A3C0),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.share),
                contentDescription = "Udostępnij",
                tint = Color.White,
                modifier = Modifier.size(30.dp * isBigger).background(Color(0xFF35A3C0)),
            )
        }
    }
}

//===================================== Timer Component ===============================================
@Composable
fun SlideInWidget(mainVm: MainViewModel) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp * 0.5f
    val targetOffsetX = if (mainVm.isWidgetVisible) screenWidth else screenWidth * 2
    val animatedOffsetX by animateDpAsState(
        targetValue = targetOffsetX,
        animationSpec = tween(durationMillis = 1500), label = "slide"
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(screenWidth)
            .offset(x = animatedOffsetX)
            .background(Color.LightGray, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
            .zIndex(3.0f),
    ) {
        Button(
            onClick = {mainVm.setWidgetVisibility(false)},
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xAA35A3C0)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White)) {
            Text("X", fontSize = 20.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        TimerScreen(mainVm, 0.5f, 1.2f, true)
        mainVm.setReset(false)
    }
}

@Composable
fun TimerScreen(mainVm: MainViewModel, screenWidth: Float = 1.0f, isBigger: Float = 1.0f, isWidget: Boolean = false, navController: NavController? = null) {

    if(navController != null) {
        BackHandler {
            mainVm.setWidgetVisibility(false)
            navController.popBackStack()
        }
    }
    var colorText = MaterialTheme.colorScheme.onBackground
    if(isWidget)
    {
        colorText = Color.Black
    }

    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.GERMAN, "%02d:%02d", minutes, remainingSeconds)
    }

    fun startTimer(seconds: Int) {
        mainVm.countDownTimer?.cancel()
        mainVm.updateTimerSeconds(mainVm.setWidgetTime.toInt())
        var countDownTimer = object : CountDownTimer((seconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mainVm.updateTimerSeconds((millisUntilFinished / 1000).toInt())
            }

            override fun onFinish() {
                mainVm.updateTimerState(TimerState.End)
                mainVm.setIsRunning(false)
                mainVm.updateTimerSeconds(0)
                mainVm.updateShakeState(ShakerState.End)
            }
        }.start()
        mainVm.updateCountDownTimer(countDownTimer)
        mainVm.updateTimerState(TimerState.Running)
        mainVm.setIsRunning(true)
        mainVm.updateShakeState(ShakerState.Shaking)
    }

    fun pauseTimer() {
        mainVm.countDownTimer?.cancel()
        mainVm.updateTimerState(TimerState.Paused)
        mainVm.setIsRunning(false)
        mainVm.updateShakeState(ShakerState.Start)
    }

    fun resetTimer() {
        mainVm.countDownTimer?.cancel()
        mainVm.updateTimerSeconds(mainVm.setWidgetTime.toInt())
        mainVm.updateTimerState(TimerState.Stopped)
        mainVm.setIsRunning(false)
        mainVm.updateShakeState(ShakerState.Start)
    }

    if(mainVm.resetTimer)
    {
        resetTimer()
    }

    val infiniteTransition = rememberInfiniteTransition()
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (mainVm.shakeState == ShakerState.Shaking) 10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                -10f at 0 using FastOutSlowInEasing
                10f at 500 using FastOutSlowInEasing
                -10f at 1000 using FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart
        )
    )

    val rotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue =  45f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                -25f at 0 using FastOutSlowInEasing
                45f at 500 using FastOutSlowInEasing
                -25f at 1000 using FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart
        )
    )


    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(screenWidth)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var soffsetX = 0.dp
        var srotate = 0f
        var sid = R.drawable.shaker
        if(mainVm.shakeState == ShakerState.Start)
        {
            soffsetX = 0.dp
            srotate = 0f
            sid = R.drawable.shaker
        } else if(mainVm.shakeState == ShakerState.Shaking) {
            soffsetX = offsetX.dp
            srotate = rotate
            sid = R.drawable.shaker_shaking
        } else {
            soffsetX = 0.dp
            srotate = 0f
            sid = R.drawable.shaker_fill
        }
        Box(
            modifier = Modifier
                .offset(x = soffsetX)
                .rotate(srotate)
        ) {
            Image(
                painter = painterResource(id = sid),
                contentDescription = "Shaker",
                modifier = Modifier.size(200.dp * isBigger)
            )
        }

        Spacer(modifier = Modifier.height(32.dp * isBigger))

        Text(
            text = formatTime(mainVm.timerSeconds),
            fontSize = 48.sp * isBigger,
            fontWeight = FontWeight.Bold,
            color = colorText
        )

        Spacer(modifier = Modifier.height(32.dp * isBigger))

        Button(
            onClick = {
                when (mainVm.timerState) {
                    TimerState.Stopped -> startTimer(mainVm.timerSeconds)
                    TimerState.Running -> pauseTimer()
                    TimerState.Paused -> startTimer(mainVm.timerSeconds)
                    TimerState.End -> {}
                }
            },
            modifier = Modifier.padding(8.dp * isBigger),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xAA35A3C0)),
            shape = RoundedCornerShape(8.dp * isBigger),
            border = BorderStroke(1.dp, Color.White)
        ) {
            when (mainVm.timerState) {
                TimerState.Stopped -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = "Start",
                        modifier = Modifier.size(36.dp * isBigger),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp * isBigger))
                    Text(text = "Start", color = Color.White, fontSize = 16.sp * isBigger)
                }
                TimerState.Running -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pause),
                        contentDescription = "Pause",
                        modifier = Modifier.size(36.dp * isBigger),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp * isBigger))
                    Text(text = "Pauza", color = Color.White, fontSize = 16.sp * isBigger)
                }
                TimerState.Paused -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = "Start",
                        modifier = Modifier.size(36.dp * isBigger),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp * isBigger))
                    Text(text = "Wznów", color = Color.White, fontSize = 16.sp * isBigger)
                }
                TimerState.End -> {
                    Text(text = "Koniec czasu", color = Color.White, fontSize = 16.sp * isBigger)
                }
            }
        }

        if (mainVm.timerState == TimerState.Paused || mainVm.timerState == TimerState.End) {
            Button(
                onClick = { resetTimer() },
                modifier = Modifier.padding(8.dp * isBigger),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(8.dp * isBigger),
                border = BorderStroke(1.dp, Color.White)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_reset),
                    contentDescription = "Reset",
                    modifier = Modifier.size(36.dp * isBigger),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp * isBigger))
                Text(text = "Reset", color = Color.White, fontSize = 16.sp * isBigger)
            }
        }
    }
}

enum class TimerState {
    Running, Paused, Stopped, End
}

enum class ShakerState {
    Shaking, Start, End
}

//====================================== Bottom AppInfo Component ===============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInfoBottomSheet(
    sheetState: SheetState,
    isTablet: Boolean,
    onDismiss: () -> Unit
) {
    var isBigger = 1.0f
    val configuration = LocalConfiguration.current
    if (isTablet && configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        isBigger = 1.5f
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .padding(28.dp * isBigger)
                .fillMaxWidth()
        ) {
            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Autor: ")
                }
                append("Wojciech Gunia")
            }, fontSize = 16.sp * isBigger, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Wersja: ")
                }
                append("1.1.0")
            }, fontSize = 16.sp * isBigger, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("O aplikacji: ")
                        }
                        append("Aplikacja z przepisami popularnych drinków nie tylko dla doświadczonych barmanów.")
                    },
                    fontSize = 16.sp * isBigger,
                    lineHeight = 20.sp * isBigger,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}