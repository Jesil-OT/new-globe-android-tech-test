## New Globe Android Tech Test
______________________________

### This is a application designed to administer pupil information from a mobile application, it involves users or business owner to See/Monitor every pupil in a school, the application has features like Creating pupil, Delete Pupil, Edit Pupil, and shows all the list of pupils in the app.

## Tools & Technologies 🛠️
- Kotlin
- Koin - Dependency Injection
- Retrofit - Network Layer
- Logging interceptor - Network Layer
- Room - Database Layer
- Glide - Image Loading Library
- CircleImageView - Image Loading Library
- SwipeRefreshLayout - Pull to Refresh
- Timber - Logging Library
- Kotlin Serialization - Serialization Library
- Flow: StateFlow, SharedFlow

## Code design and Architecture 🦾🛠️
___________________
- This Project is made using MVVM Architecture.
- The core layer holds `Utility` and `Helper Classes` with the `Dependency Injection Module`
- The data layer holds the `Mapper` class for mapping between the different layers of the app ie (data -> domain -> presentation), holds the general `Model`, the `Repository` that holds the different Abstractions and acts as the source of the single source of truth, then lastly the `Sources`, This layer is made up of two layers - `Network`(or `Remote`) and `Local`, the - The `Local layer` is the single source of truth of the UI. The data is gotten from the endpoint and then cached to the room database. The `Network layer` is the source that deals with managing tasks such as making API calls, sending and receiving data (e.g., JSON).
- The feature layer holds related information concerning the `features` of the app. eg (the create pupil feature) (the edit pupil feature) (the pupil list feature) etc.

## Assumptions Made
### To implement the Requirements for the project, I made these assumptions:
- Users always want to see the most recent updated cached data, even if the network fails.
- The Api constantly throws Exceptions like NotFound Exceptions, BadRequest Exceptions, Server Unavailable Exceptions.
- Network connectivity is unreliable, so offline-first design is critical.
- Project uses view based System or XML View System.

## Requirements Implemented
- **Single Source of Truth:** All pupil data is stored locally in a Room database. Whether online or offline, the UI always reads from RoomDB. **_List of Pupils:_** A PupilDao provides a `getAllPupils()` query that returns a flow of pupils from the database. **_Pupil Details:_** A `getPupil(pupilId)` query fetches detailed info from Room. **_Add New Pupil:_** A new record is inserted into Room via `insertPupil(pupil)` if online only. **_Delete Pupil:_** If online-> A record of pupil is removed from Room using `deletePupil(pupilId)` to avoid data inconsistencies. **_Delete all Pupils_** If online-> All records are removed from Room and re-inserted into the database for updated data using the `insertPupils(pupils)` query. **_Offline Support:_** When the network is unavailable, Room continues to serve cached pupil data. This ensures the user can still: _View the list of pupils_ and _View pupil details_. **_Sync on Reconnect:_** When the app detects connectivity again, it changes in Room and is synced with the remote server.
- **The API often throws errors like NotFound (404), BadRequest (400), or Server Unavailable (5xx):** To avoid crashing the app and to give the user a stable experience, I wrapped all API calls inside a safeApiCall helper function. **This function:** Runs the API request on Dispatchers.IO so it doesn’t block the UI. **_Checks the response code:_** `200–299` → success, `return data`. `404` → return a `NetworkError.NotFound`. `400` → return a `NetworkError.BadRequest`. `500-600` → return a `NetworkError.ServiceUnavailable`. Anything else → return a generic `ApiError`. **_Catches network exceptions like:_** `SocketTimeoutException` → maps to `ConnectionTimedOut`. `IOException (no internet)` → maps to `NoInternetConnection`. Any other unexpected error → `UnknownError`. Finally, every call returns a `NetworkResult`, which can either be: `Success(data or Unit)` if the API worked, or `Error(errorType)` if something went wrong. This way, instead of the app crashing or showing raw exceptions, the UI always gets a safe, predictable result and can display the right message.
- **Offline-first design:** Since network connectivity is unreliable, I designed the app to be offline-first. This means the UI never waits for the network to succeed before showing data. Instead, the Room database acts as the **_single source of truth_** while loading or trying to get new data. Whenever the app fetches pupils, it first returns cached data from Room so the list/details show instantly, even without internet Once the device comes back online, it's sync process runs to get local changes with the server. If the API call fails due to errors like _NotFound_, _BadRequest_, or _Server Unavailable_, the cached copy from Room is still displayed.
- **The project is built using the XML View System instead of Jetpack Compose:** To manage UI state and one-time events in a lifecycle-aware way, I used **StateFlow** and **SharedFlow** from Kotlin Coroutines: **StateFlow** was used to expose continuous UI state (like the list of pupils, pupils details). Whenever the data in Room or the network changes, the UI automatically reacts to the new state. **SharedFlow** was used for **one-time events** such as showing error messages, navigation triggers, toast messages or other UI actions. This prevents issues like the same error being shown again after configuration changes (e.g., screen rotation).
