# SpotShare 📍📷  
**An Android app for sharing and discovering fun places**

SpotShare is a social recommendation app built for Android. Users can upload posts about their favorite hangout spots — including a photo, location (via GPS), description, and category. Others can like posts, view them on a map, and filter by category.

---

## ✨ Features

- 🔐 Firebase Authentication (Login & Sign up)
- 📸 Upload posts with images and location (Google Maps)
- 📝 Add title, description, and select category (Friends / Dating / Family)
- ❤️ Like posts (saved in Firebase Realtime Database)
- ✏️ Edit and delete your own posts
- 🌍 View post location on map (embedded or full screen)
- 🔄 Real-time updates using Firebase Realtime Database
- 📁 Image upload to Firebase Storage

---

## 🛠 Tech Stack

- **Language**: Kotlin  
- **Platform**: Android  
- **Backend**: Firebase (Auth, Realtime Database, Storage)  
- **UI**: RecyclerView, ViewBinding, BottomNavigationView  
- **Map**: Google Maps SDK  
- **Image Loading**: Glide

---

## 📱 Screenshots

> Add screenshots of the main screens here:
- Home Feed  
- Add Post Screen  
- Edit Post Screen  
- Map View  
- Profile Page  

---

## ▶️ Demo Video

> [Insert a link to your YouTube or Google Drive demo here]

---

## 🔧 How to Run

1. Clone the repository  
2. Open in Android Studio  
3. Add your `google-services.json` file (Firebase project setup)  
4. Make sure your Firebase Realtime Database and Storage rules allow testing  
5. Run the app on an emulator or device

---

## 📁 Folder Structure

- `MainActivity.kt` – main feed with all public posts  
- `AddPostActivity.kt` – add new post with photo and details  
- `EditPostActivity.kt` – edit your own posts  
- `ProfileActivity.kt` – view user's posts  
- `PostAdapter.kt` – displays posts in RecyclerView  
- `FirebaseUtils.kt` – (optional) helper functions  
- `MapActivity.kt` – shows full-screen map with selected location  

---

## 👨‍💻 Author

Created as part of an Android development course.  
Feel free to fork or contribute!

---

## 📄 License

This project is open-source and free to use.
