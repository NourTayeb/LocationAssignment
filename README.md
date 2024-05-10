# Bird Location Updates SDK

SDK built to integrate with our location tracking services.

## Language
- Kotlin

## Features:
- Regular capture of location updates and send to the backend service.
- On-demand location update requests for immediate data capture and transmission.
- Seamless integration with existing Android applications.
- Customizable configuration options for managing SDK capabilities.

## Installation
To integrate the SDK into your Android project, follow these steps:

1. Add the SDK to your project's dependencies.
   ```groovy
   dependencies {
       implementation 'com.bird.locations:1.0.0'
   }
   ```

2. Sync your project to ensure the SDK is successfully added.

## Usage

### Configuration:
To configure the SDK, you must provide the `API_KEY`. To do so, update your `local.properties` with the value provided to you by Bird.
```properties
LOCATIONS_API_KEY=<YOUR_OWN_API_KEY>
```

### Initialization
Before using the SDK, initialize it with the `applicationContext`.
It should happen before making any function call to the SDK.

```kotlin
LocationUpdatesSdk.init(applicationContext)
```

### On-demand location update request for immediate data capture and send
This is done by utilizing `LocationUpdateUseCase` which has the function `sendUpdatedLocation` which returns a Flow object.
This Flow will emit the status of the location update.

To retrieve this use case, use the function:

```kotlin
LocationUpdatesSdk.getLocationUpdateUseCase()
```

### Start continuous location updates
To start the Service that tracks location updates continuously and updates Bird's server, use this function:

```kotlin
LocationUpdatesSdk.startLocationUpdatesService(interval)
```

`interval` here is the time between location updates.

### Stop continuous location updates
When location updates service is running, there are two ways to stop it, either using the SDK function:

```kotlin
LocationUpdatesSdk.stopLocationUpdatesService()
```

Another way to stop location updates is by expanding the Foreground Service notification and clicking "Stop location updates".
