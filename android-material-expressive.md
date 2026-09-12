---
name: android-material-expressive
description: >-
  Use this skill whenever the user asks to build, scaffold, or improve an Android
  application using Jetpack Compose and Material Expressive 3 (Material 3).
  Covers: GitHub Actions CI/CD (no local build), Material Expressive 3 theming
  (color, shape, motion, typography), MVVM + Clean Architecture with Hilt,
  Kotlin Coroutines/Flow, Compose component selection, and adaptive layouts.
  Activate when the user mentions Android, Compose, Material 3, Hilt, MVVM,
  APK build, or GitHub Actions for Android.
version: "2.0"
author: antigravity-skill
---

# Android Material Expressive 3 — Ultimate Dev Skill

> **Golden Rule — No Local Builds.**
> Never install Android SDK, Gradle, JDK, or any Android toolchain locally.
> All compilation, testing, and APK packaging MUST happen exclusively via
> **GitHub Actions**. Read CI logs, patch code, push, repeat.

---

## Table of Contents

1. [Operational Constraints](#1-operational-constraints)
2. [Technology Stack & Versions](#2-technology-stack--versions)
3. [Project Structure](#3-project-structure)
4. [Phase 1 — Repository & CI/CD Setup](#4-phase-1--repository--cicd-setup)
5. [Phase 2 — Material Expressive 3 Theme System](#5-phase-2--material-expressive-3-theme-system)
6. [Phase 3 — Architecture Implementation (MVVM + Hilt)](#6-phase-3--architecture-implementation-mvvm--hilt)
7. [Phase 4 — UI Screens & Compose Components](#7-phase-4--ui-screens--compose-components)
8. [Phase 5 — Iterative CI/CD Debug Loop](#8-phase-5--iterative-cicd-debug-loop)
9. [Component Cheatsheet](#9-component-cheatsheet)
10. [Motion & Animation Patterns](#10-motion--animation-patterns)
11. [Adaptive Layout Patterns](#11-adaptive-layout-patterns)
12. [Common Pitfalls & Fixes](#12-common-pitfalls--fixes)

---

## 1. Operational Constraints

| Rule | Detail |
|------|--------|
| NO local ./gradlew | Never run gradle locally |
| NO apt-get / brew for Android tools | No local SDK installs |
| Git-first workflow | Every change -> commit -> push -> watch Actions |
| Debug via Actions logs | Read CI output to fix errors |
| Gradle property files committed | gradle.properties, settings.gradle.kts |

---

## 2. Technology Stack & Versions

```kotlin
// Compose BOM — always use BOM for version alignment
implementation(platform("androidx.compose:compose-bom:2026.09.00"))

// Core dependencies (September 2026)
// androidx.compose.material3:material3:1.5.0          — Material Expressive 3
// androidx.lifecycle:lifecycle-runtime-compose:2.9.0  — collectAsStateWithLifecycle
// com.google.dagger:hilt-android:2.52                 — DI
// androidx.hilt:hilt-navigation-compose:1.2.0         — Hilt + Nav
// androidx.navigation:navigation-compose:2.9.0        — Navigation
// androidx.room:room-runtime:2.7.0                    — Local DB
// com.squareup.retrofit2:retrofit:2.11.0              — Network
```

**Build config:**
- Language: Kotlin (latest stable)
- Min SDK: 24  |  Target/Compile SDK: 36
- JDK: 17 (Temurin)
- Build system: Gradle Kotlin DSL (build.gradle.kts)
- KSP (not KAPT) for annotation processing

---

## 3. Project Structure

Use a **feature-first package structure** under `com.<org>.<app>`:

```
app/src/main/kotlin/com/example/app/
|-- MainActivity.kt                    # Single activity, setContent { AppTheme { NavGraph() } }
|
|-- di/                                # Hilt modules
|   |-- NetworkModule.kt
|   |-- DatabaseModule.kt
|   `-- RepositoryModule.kt
|
|-- data/
|   |-- remote/
|   |   |-- ApiService.kt             # Retrofit interface
|   |   `-- dto/                      # Response DTOs
|   |-- local/
|   |   |-- AppDatabase.kt            # Room @Database
|   |   `-- dao/
|   `-- repository/                   # Impl of domain interfaces
|       `-- mapper/                   # DTO -> Domain model
|
|-- domain/
|   |-- model/                        # Pure Kotlin data classes
|   |-- repository/                   # Interfaces only
|   `-- usecase/                      # One UseCase per action
|
`-- presentation/
    |-- theme/                         # Material Expressive 3 theme files
    |   |-- Color.kt
    |   |-- Type.kt
    |   |-- Shape.kt
    |   `-- Theme.kt
    |-- navigation/
    |   `-- AppNavGraph.kt            # NavHost + all routes
    |-- components/                    # Reusable composables
    `-- features/
        |-- home/
        |   |-- HomeScreen.kt
        |   |-- HomeViewModel.kt
        |   `-- HomeUiState.kt
        `-- [feature]/
            |-- [Feature]Screen.kt
            |-- [Feature]ViewModel.kt
            `-- [Feature]UiState.kt
```

---

## 4. Phase 1 — Repository & CI/CD Setup

### Step 1.1: Initialize Git and push to GitHub

```bash
git init
git remote add origin https://github.com/<user>/<repo>.git
git add .
git commit -m "chore: initial scaffold"
git push -u origin main
```

### Step 1.2: GitHub Actions workflow — `.github/workflows/android.yml`

```yaml
name: Android CI

on:
  push:
    branches: [ "main", "develop" ]
  pull_request:
    branches: [ "main" ]

jobs:
  build:
    name: Build & Test
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Grant gradlew permissions
        run: chmod +x gradlew

      - name: Run unit tests
        run: ./gradlew testDebugUnitTest --no-daemon

      - name: Build debug APK
        run: ./gradlew assembleDebug --no-daemon --build-cache

      - name: Upload debug APK
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/*.apk
          retention-days: 7

  lint:
    name: Lint Check
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'
      - run: chmod +x gradlew
      - run: ./gradlew lintDebug --no-daemon
      - uses: actions/upload-artifact@v4
        with:
          name: lint-report
          path: app/build/reports/lint-results-debug.html
```

### Step 1.3: `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx4g -XX:+UseG1GC
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true
kotlin.incremental=true
kotlin.code.style=official
android.useAndroidX=true
android.nonTransitiveRClass=true
android.enableR8.fullMode=true
```

### Step 1.4: `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.activity.compose)
    implementation(libs.core.ktx)
}
```

---

## 5. Phase 2 — Material Expressive 3 Theme System

### 5.1 `Color.kt`

```kotlin
package com.example.app.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Purple10  = Color(0xFF21005D)
val Purple20  = Color(0xFF381E72)
val Purple40  = Color(0xFF6750A4)
val Purple80  = Color(0xFFD0BCFF)
val Purple90  = Color(0xFFEADDFF)
val PurpleGrey40 = Color(0xFF625B71)
val PurpleGrey80 = Color(0xFFCCC2DC)
val PurpleGrey90 = Color(0xFFE8DEF8)
val Pink40 = Color(0xFF7D5260)
val Pink80 = Color(0xFFEFB8C8)
val Pink90 = Color(0xFFFFD8E4)

val LightColorScheme = lightColorScheme(
    primary            = Purple40,
    onPrimary          = Color.White,
    primaryContainer   = Purple90,
    onPrimaryContainer = Purple10,
    secondary          = PurpleGrey40,
    onSecondary        = Color.White,
    secondaryContainer = PurpleGrey90,
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary           = Pink40,
    onTertiary         = Color.White,
    tertiaryContainer  = Pink90,
    onTertiaryContainer = Color(0xFF31111D),
)

val DarkColorScheme = darkColorScheme(
    primary            = Purple80,
    onPrimary          = Purple20,
    primaryContainer   = Color(0xFF4F378B),
    onPrimaryContainer = Purple90,
    secondary          = PurpleGrey80,
    onSecondary        = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = PurpleGrey90,
    tertiary           = Pink80,
    onTertiary         = Color(0xFF492532),
    tertiaryContainer  = Color(0xFF633B48),
    onTertiaryContainer = Pink90,
)
```

> **Dynamic Color:** When `Build.VERSION.SDK_INT >= S`, use `dynamicLightColorScheme(context)` /
> `dynamicDarkColorScheme(context)` to source palette from wallpaper (Material You).

### 5.2 `Type.kt`

```kotlin
package com.example.app.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    displayLarge   = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,   fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,   fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall   = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,   fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,   fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,   fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall  = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,   fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge     = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium    = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall     = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge      = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium     = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall      = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge     = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium    = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall     = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)
```

### 5.3 `Shape.kt`

```kotlin
package com.example.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // Tooltip, snackbar action
    small      = RoundedCornerShape(8.dp),   // Chip, text field
    medium     = RoundedCornerShape(12.dp),  // Card (default)
    large      = RoundedCornerShape(16.dp),  // Bottom sheet, nav drawer
    extraLarge = RoundedCornerShape(28.dp),  // Dialog, large modal card
)

// Pill: CircleShape or RoundedCornerShape(50)
// Asymmetric Expressive: RoundedCornerShape(topStart = 28.dp, topEnd = 4.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
```

### 5.4 `Theme.kt` — MaterialExpressiveTheme Entry Point

```kotlin
package com.example.app.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialExpressiveTheme(
        colorScheme  = colorScheme,
        typography   = AppTypography,
        shapes       = AppShapes,
        motionScheme = MotionScheme.expressive(),
        content      = content
    )
}
```

---

## 6. Phase 3 — Architecture Implementation (MVVM + Hilt)

### 6.1 Application & Activity

```kotlin
// App.kt
@HiltAndroidApp
class App : Application()

// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppTheme { AppNavGraph() } }
    }
}
```

### 6.2 DI Modules

```kotlin
// NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}

// RepositoryModule.kt — @Binds (preferred over @Provides for interfaces)
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
```

### 6.3 ViewModel Pattern

```kotlin
// HomeUiState.kt
data class HomeUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null
)

// HomeViewModel.kt
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getItemsUseCase: GetItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadItems() }

    fun loadItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getItemsUseCase()
                .onSuccess { items -> _uiState.update { it.copy(isLoading = false, items = items) } }
                .onFailure { e  -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
```

### 6.4 Screen Pattern (collectAsStateWithLifecycle)

```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onItemClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Home", style = MaterialTheme.typography.titleLarge) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                uiState.error != null -> Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        ItemCard(item = item, onClick = { onItemClick(item.id) })
                    }
                }
            }
        }
    }
}
```

### 6.5 Navigation

```kotlin
@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(onItemClick = { id -> navController.navigate("detail/$id") })
        }
        composable(
            route = "detail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStack ->
            val itemId = backStack.arguments?.getString("itemId") ?: return@composable
            DetailScreen(itemId = itemId, onBack = { navController.navigateUp() })
        }
    }
}
```

---

## 7. Phase 4 — UI & Material Expressive 3 Design Rules

| Rule | Implementation |
|------|----------------|
| Generous whitespace | Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(16.dp) |
| Tonal elevation (no heavy shadows) | ElevatedCard, Surface(tonalElevation = 2.dp) |
| Expressive corners | shape = MaterialTheme.shapes.extraLarge for hero cards |
| Pill buttons | Button is already pill-shaped in M3; use CircleShape for icon-only |
| Dynamic color | Always pass dynamicColor = true in AppTheme |
| Spring motion | MaterialExpressiveTheme + MotionScheme.expressive() gives this automatically |
| Edge-to-edge | Call enableEdgeToEdge() before setContent in MainActivity |

### Expressive Card

```kotlin
@Composable
fun ItemCard(item: Item, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Text(item.subtitle,
                 style = MaterialTheme.typography.bodyMedium,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
```

---

## 8. Phase 5 — Iterative CI/CD Debug Loop

```
PUSH -> GitHub Actions triggers -> Read logs -> Identify error -> Fix code -> PUSH again
```

### Debugging Checklist

- [ ] Build error "Could not find" -> version mismatch in libs.versions.toml, check BOM
- [ ] "Unresolved reference" -> missing implementation() in app/build.gradle.kts
- [ ] @HiltViewModel not found -> missing ksp(libs.hilt.compiler) or @HiltAndroidApp on App class
- [ ] KSP errors -> ensure ksp plugin applied, NEVER mix KSP + KAPT
- [ ] ExperimentalMaterial3ExpressiveApi -> add @OptIn at function or file scope
- [ ] Compose version mismatch -> use compose-bom and let it manage versions
- [ ] NetworkOnMainThreadException -> all network calls in viewModelScope.launch
- [ ] StateFlow not updating UI -> use collectAsStateWithLifecycle() not collectAsState()

---

## 9. Component Cheatsheet

### Buttons

```kotlin
Button(onClick = {}) { Text("Filled") }
FilledTonalButton(onClick = {}) { Text("Tonal") }
OutlinedButton(onClick = {}) { Text("Outlined") }
TextButton(onClick = {}) { Text("Text") }
ElevatedButton(onClick = {}) { Text("Elevated") }
```

### M3 Expressive — SplitButton (requires @OptIn)

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
SplitButton(
    leadingButton = { SplitButtonDefaults.LeadingButton(onClick = {}) { Text("Action") } },
    trailingButton = {
        SplitButtonDefaults.TrailingButton(checked = false, onCheckedChange = {}) {
            Icon(Icons.Default.ArrowDropDown, null)
        }
    }
)
```

### FAB Menu (replaces Speed Dial, requires @OptIn)

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
var fabExpanded by remember { mutableStateOf(false) }

FloatingActionButtonMenu(
    expanded = fabExpanded,
    button = {
        FloatingActionButton(onClick = { fabExpanded = !fabExpanded }) {
            Icon(if (fabExpanded) Icons.Default.Close else Icons.Default.Add, null)
        }
    }
) {
    FloatingActionButtonMenuItem(
        onClick = { fabExpanded = false },
        icon = { Icon(Icons.Default.Edit, null) },
        text = { Text("Edit") }
    )
    FloatingActionButtonMenuItem(
        onClick = { fabExpanded = false },
        icon = { Icon(Icons.Default.Share, null) },
        text = { Text("Share") }
    )
}
```

### Loading Indicator (shape-morphing, requires @OptIn)

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
LoadingIndicator()   // Animated morphing shapes — no parameters needed
```

### ButtonGroup / filter tabs (requires @OptIn)

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
var selected by remember { mutableIntStateOf(0) }

ButtonGroup {
    listOf("Day", "Week", "Month").forEachIndexed { index, label ->
        ToggleButton(
            checked = selected == index,
            onCheckedChange = { selected = index }
        ) { Text(label) }
    }
}
```

### Navigation (phone vs tablet)

```kotlin
// Phone: NavigationBar (bottom)
NavigationBar {
    items.forEachIndexed { index, item ->
        NavigationBarItem(
            selected = currentIndex == index,
            onClick = { currentIndex = index },
            icon = { Icon(item.icon, null) },
            label = { Text(item.label) }
        )
    }
}

// Tablet/Expanded: NavigationRail (side)
NavigationRail {
    items.forEachIndexed { index, item ->
        NavigationRailItem(
            selected = currentIndex == index,
            onClick = { currentIndex = index },
            icon = { Icon(item.icon, null) },
            label = { Text(item.label) }
        )
    }
}
```

### Emphasized Typography (M3 Expressive, requires @OptIn)

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
Text("Hero Title", style = MaterialTheme.typography.headlineLargeEmphasized)
Text("Body copy", style = MaterialTheme.typography.bodyLargeEmphasized)
```

---

## 10. Motion & Animation Patterns

### Spring-based via MotionScheme

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val motionScheme = MaterialTheme.motionScheme

AnimatedVisibility(
    visible = isVisible,
    enter = slideInVertically { it } + fadeIn(animationSpec = motionScheme.defaultEffectsSpec()),
    exit  = slideOutVertically { it } + fadeOut(animationSpec = motionScheme.defaultEffectsSpec())
) { content() }
```

### Micro-interaction (scale on press)

```kotlin
val interactionSource = remember { MutableInteractionSource() }
val isPressed by interactionSource.collectIsPressedAsState()
val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.96f else 1f,
    animationSpec = spring(stiffness = Spring.StiffnessMedium)
)
Box(
    Modifier
        .scale(scale)
        .clickable(interactionSource, indication = ripple()) { onClick() }
)
```

---

## 11. Adaptive Layout Patterns

```kotlin
@Composable
fun AppShell() {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isCompact = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT

    if (isCompact) {
        Scaffold(bottomBar = { AppNavigationBar() }) { padding ->
            AppNavHost(Modifier.padding(padding))
        }
    } else {
        Row {
            AppNavigationRail()
            AppNavHost(Modifier.weight(1f))
        }
    }
}
```

---

## 12. Common Pitfalls & Fixes

| Pitfall | Fix |
|---------|-----|
| Using KAPT with Hilt/Room | Remove KAPT entirely, use only ksp() |
| MaterialTheme instead of MaterialExpressiveTheme | Replace in Theme.kt for spring motion |
| Missing @OptIn(ExperimentalMaterial3ExpressiveApi::class) | Add at file or function level |
| Hard-coded colors | Use MaterialTheme.colorScheme.primary / surfaceContainer etc. |
| collectAsState() instead of collectAsStateWithLifecycle() | Import lifecycle-runtime-compose |
| Not calling enableEdgeToEdge() | Add before setContent in onCreate |
| Passing NavController deep into composables | Only at NavGraph level; pass lambdas down |
| No image loading library | Add coil3-compose for async image loading |
| viewModel() when Hilt is present | Use hiltViewModel() |
| Local gradle builds | Always commit + push to trigger GitHub Actions |

---

## References

- Now in Android: https://github.com/android/nowinandroid (canonical M3 production app)
- Reply sample: https://github.com/android/compose-samples/tree/main/Reply (adaptive nav)
- Compose Samples: https://github.com/android/compose-samples
- Material 3 Compose catalog: https://github.com/itssohaibahmed/compose-catalog-material-3
- Material Components Android: https://github.com/material-components/material-components-android
- Compose Material 3 API docs: https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary
