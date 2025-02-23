# ePUTNI - Digital Travel Warrant Management System


## About the Project
**ePUTNI** is a software solution designed to digitize and automate the travel warrant process for student association [eSTUDENT](https://www.estudent.hr). It replaces manual paperwork with a mobile-based system, allowing members to create, manage, and track travel warrants efficiently while enabling the accounting team to approve and monitor requests through a [web app - ePUTNI Web](https://github.com/MatejStrlek/ePUTNI-web).

This project was developed as part of a final thesis at **Algebra University** by **Matej Galić**, under the mentorship of **Danijel Kučak**.

## Features
- **Mobile application (Android)**
  - Secure login using Google Authentication
  - Create and manage vehicles
  - Create and manage travel warrants
  - Track the status of travel warrants
  - Scan and upload receipts directly from the mobile app
  - Automatic email notifications to the finance team

- **Cloud-based backend**
  - Real-time synchronization of travel warrant data
  - Secure user authentication and data storage
 
## Modules
- **Authentication Module:** Handles user login and authentication via Google Authentication (**must** be association's domain)
- **Travel Warrant Management Module:** Allows users to create, edit, and track travel warrants
- **Vehicles Management Module:** Allows users to create, edit, and delete vehicles
- **Receipt Scanning Module:** Uses Google ML Kit to scan and process receipts
- **Data Synchronization Module:** Syncs user data with Firebase Firestore for real-time updates
- **Notification Module:** Sends automated email notification to the finance team after trip is finished

## Technologies used
- **Mobile App:** Kotlin, Android SDK, Firebase Authentication, Firebase Firestore, Firebase Storage, Google ML Kit (for receipt scanning)
- **Backend:** Firebase Cloud Functions
- **Development Tools:** Android Studio
