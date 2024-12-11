# Tutortoise (Android App)

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
