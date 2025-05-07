
# Screen Analyzer App

## Overview

**Screen Analyzer** is an Android application written in Kotlin using the Jetpack Compose framework. This app performs screen analysis to detect faces using Google’s ML Kit. It operates through a background service that captures the screen periodically, processes images to detect faces, and then overlays rectangles around detected faces on the screen.

![photo_2024-11-09_01-01-29](https://github.com/user-attachments/assets/90a8f5a6-f756-4e6d-8717-56a2438ba856)


## Features

- **Background Screen Capture**: Runs a background service to analyze the screen at regular intervals.
- **Face Detection**: Utilizes ML Kit’s face detection capabilities to identify faces in the captured images.
- **Results Display**: Shows detected face boundaries on the screen using an overlay view.
- **Real-time Notifications**: Notifies the user with the number of detected faces through ongoing notifications.

## Components

### MainActivity

- Contains the main UI that allows the user to start and stop the screen analysis.
- Requests permission to begin `MediaProjection` for screen capturing.

### ScreenCaptureService

- The **background service** that runs the screen analysis.
- Creates a `VirtualDisplay` to capture the screen and convert the image to a format suitable for face detection.
- Uses `ImageReader` to read and process the captured image.

### FaceOverlayView

- **Overlay view** that displays rectangles around detected faces.
- Draws rectangles using `Canvas` and `Paint` to highlight detected faces on the screen.

### FaceDetectionViewModel

- Maintains the state of detected faces and shares it with the UI using `StateFlow` to keep the interface updated.

### FaceDetectionReceiver

- A receiver for broadcasted detection results.
- Logs events related to detected faces for monitoring and debugging purposes.

## Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/mohamad-aljeiawi/screen-agent.git
   cd screen-agent
   ```

2. **Add ML Kit Dependency** in your `build.gradle` file:
   ```gradle
   implementation 'com.google.mlkit:face-detection:16.1.7'
   ```

3. **Permissions**:
   - Add the required permissions in `AndroidManifest.xml`:
     ```xml
     <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
     <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
     ```

## Usage

1. **Run the App**:
   - Open the app and press the "Start Analysis" button to begin screen analysis.

2. **Start Screen Capture**:
   - When "Start Analysis" is pressed, the service will request permission to capture the screen.
   - Once permission is granted, the background service will begin running and display the number of detected faces in notifications.

3. **Stop Screen Capture**:
   - Press the "Stop Analysis" button to stop the service and end the screen analysis.

## Code Overview

### 1. MainActivity

```kotlin
class MainActivity : ComponentActivity() {
    // Manages MediaProjection permissions and initializes the UI for starting and stopping analysis
}
```

### 2. ScreenCaptureService

```kotlin
class ScreenCaptureService : Service() {
    // Contains logic to capture and analyze the screen using ImageReader and ML Kit
}
```

### 3. FaceOverlayView

```kotlin
class FaceOverlayView(context: Context) : View(context) {
    // Draws rectangles around detected faces using Paint and Canvas
}
```

## Sample Output

When faces are detected, rectangles are drawn around them on the screen, and a notification will display the number of detected faces.

## Contributing

1. **Fork the repository**.
2. **Create a new branch** for your feature or bug fix.
3. **Commit your changes** with clear messages.
4. **Push to your branch** and create a pull request.
