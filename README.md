# Tutortoise (Android App)

The Tutortoise Android Application is developed using Kotlin Programming Language and Android Studio IDE.

## User Interface Overview

Tutortoise App is design to look simple, easy to use and understand, fitting with most modern apps.

[Images Here]

## Features

In Tutortoise, we focus on delivering tools and functionalities designed to enhance learning and teaching experiences. This section outlines the core aspects that make the app effective and user-friendly.

### 1. Explore and Find the Perfect Tutors 

Easily search for tutors based on various criteria such as category, location, hourly rate, rating, and lesson type (online/offline). This feature allows learners to quickly narrow down the options and find a tutor that fits their specific needs.

### 2. Perfect Tutor Recommendation

Receive tailored tutor recommendations based on your location and preferred learning style (visual, auditory, or kinesthetic). This personalized feature helps connect learners with tutors who match their learning preferences, ensuring a better educational experience.

### 3. Chat with your Tutor

Stay connected with your tutor through an in-app chat feature. You can discuss lessons, clarify doubts, and ask questions at any time. Instant chat notifications ensure you never miss an important message from your tutor or learner, making communication more seamless and effective.

### 4. Register with your Google Account

Sign up or log in quickly and securely using your Google account through OAuth. This streamlined registration process saves time and eliminates the need for remembering additional credentials.

### 5. Book your Tutor

Easily schedule and reserve lessons with your preferred tutor. This feature allows learners to book sessions directly through the app, ensuring flexibility and convenience in managing learning schedules.

### 6. Become a Tutor

Turn your expertise into an opportunity by becoming a tutor on the platform. You can create, update, and manage your tutoring profile and offer lessons to students seeking your skills, all while setting your own rates and availability.

### 7. Mark Your Calendar

Tutors can manage their schedules effectively. Tutors who accept a learner's reservation will automatically added on their calendar, ensuring no sessions are missed and everything is well-organized.

## Dependencies

* [Android Material](https://github.com/material-components/material-components-android/blob/master/docs/getting-started.md)
* [CircleImageView](https://github.com/hdodenhof/CircleImageView)
* [Firebase](https://firebase.google.com/docs/android/android-play-services)
* [Flexbox](https://github.com/google/flexbox-layout)
* [Glide](https://github.com/bumptech/glide)
* [Gson](https://github.com/google/gson)
* [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
* [Lifecycle & LiveData](https://developer.android.com/jetpack/androidx/releases/lifecycle)
* [Logging Interceptor](https://square.github.io/okhttp/)
* [Play Services](https://developers.google.com/android/guides/setup)
* [Retrofit](https://github.com/square/retrofit)
* [Shimmer](https://github.com/facebookarchive/shimmer-android)
* [Ucrop](https://github.com/Yalantis/uCrop)

# Tutortoise (Local Setup)

To set up the on your local environment, follow these steps:

## 1. Keystore Configuration

Before building the app, you need to generate and configure a Keystore:

- **Keystore File**:  
  Name the file `keystore.jks` and store it securely.

- **Keystore Properties**:  
  Create or edit a `keystore.properties` file and include the following details:

  ```properties
  store.file=path/to/keystore.jks
  store.password=your_keystore_password
  key.alias=your_key_alias
  key.password=your_key_password
  ```

- **Google Services**:  
  Place the `google-services.json` file in the `app/` directory.

## 2. Setting Up Base URL for API

To configure the app to connect to the API:

1. **Create or Edit the `local.properties` File**:  
   In the root directory of the project, either create or update the `local.properties` file.

2. **Add the API Base URL**:  
   Include the following entry in the `local.properties` file to set the base URL for the API:
   ```properties
   base.url=https://tutortoise-api.app/api/v1/
   ```

## 3. Setting Up OAuth Client

To configure OAuth authentication:

1. **Edit `local.properties`**:  
   Add the following entry to your `local.properties` file to include your OAuth client ID:

   ```properties
   google.oauth.client.id=your_oauth_client_id
   ```

   Replace `your_oauth_client_id` with the actual OAuth client ID provided for your app.

After completing these steps, the app will be configured and ready for local development.
