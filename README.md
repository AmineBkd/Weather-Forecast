# Weather Forecast App

## Features

- **Multi-city weather list** — track weather for any number of cities at once
- **Add city** — search cities via the OpenWeatherMap Geocoding API and add them to your list
- **Search saved cities** — filter your city list instantly with a real-time search bar
- **Current location** — detect and add your current GPS location automatically
- **Pull-to-refresh** — force-refresh all weather data with a swipe
- **Offline caching** — weather data persists in a local Room database for offline access
- **Remove cities** — swipe or tap to delete a city from your list
- **Detail screen** — tap any city card to view a full weather breakdown

---

## Tech Stack

| Layer | Technology |
|---|---|
| **UI** | Jetpack Compose, Material 3 |
| **Architecture** | MVVM, Repository pattern |
| **DI** | Dagger Hilt |
| **Networking** | Retrofit + Moshi |
| **Local DB** | Room |
| **Image Loading** | Coil |
| **Location** | Google Play Services Location |
| **Async** | Kotlin Coroutines + StateFlow |
| **Navigation** | Jetpack Navigation Compose |

---

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 30+
- An [OpenWeatherMap](https://openweathermap.org/api) API key (free tier)

### Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/AmineBkd/Weather-Forecast.git
   cd Weather-Forecast
   ```

2. **Add your API key** — create or open `local.properties` in the project root and add:
   ```properties
   WEATHER_API_KEY=your_api_key_here
   ```

3. **Build & run**.

---

## Architecture Overview

```
app/
├── data/
│   ├── api/          # Retrofit services (weather + geocoding)
│   ├── db/           # Room database, DAOs, entities
│   ├── model/        # API response models
│   └── repository/   # Single source of truth (network + cache)
├── di/               # Hilt modules
├── ui/
│   ├── home/         # City list screen (ViewModel + Composables)
│   ├── detail/       # City detail screen
│   └── theme/        # Material 3 color scheme & typography
└── util/             # Location helper utilities
```

The **Repository** layer serves as the single source of truth: it first attempts a network fetch and falls back to the Room cache if the network is unavailable.

## Screenshots
<img width="363" height="716" alt="WeatherForecast" src="https://github.com/user-attachments/assets/80e38ccd-dc1e-49c3-8028-21cf86288ef5" />

