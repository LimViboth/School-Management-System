# School Management System

A comprehensive school management solution featuring a Python backend API and an Android mobile application for efficient administration and management of school operations.

## 📋 Project Overview

The School Management System is designed to streamline school operations with two main components:

- **Backend**: RESTful API built with Python (Flask/FastAPI) for handling authentication, database management, and business logic
- **Frontend**: Native Android application for user interaction and mobile access

## 🏗️ Project Structure

```
School-Management-System/
├── back-end/
│   ├── main.py
│   ├── app/
│   │   ├── app.py              # Application factory
│   │   ├── auth.py             # Authentication logic
│   │   ├── db.py               # Database configuration
│   │   ├── models.py           # Database models
│   │   ├── schemas.py          # Request/response schemas
│   │   └── routers/            # API route handlers
│   └── requirements.txt
├── front-end/                  # Android project
│   ├── app/                    # Android app module
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle/
└── README.md
```

## 🛠️ Tech Stack

### Backend
- **Framework**: Python with Flask/FastAPI
- **Database**: SQLAlchemy ORM
- **Authentication**: JWT or session-based auth
- **API**: RESTful architecture

### Frontend
- **Platform**: Android (Native)
- **Build System**: Gradle
- **Language**: Kotlin

## 🚀 Getting Started

### Prerequisites

#### Backend
- Python 3.8+
- pip package manager

#### Frontend
- Android Studio (latest version)
- Java Development Kit (JDK) 11+
- Android SDK (API level 21+)

### Backend Setup

1. **Navigate to the backend directory**
   ```bash
   cd back-end
   ```

2. **Create a virtual environment (recommended)**
   ```bash
   python -m venv venv
   source venv/bin/activate    # On Windows: venv\Scripts\activate
   ```

3. **Install dependencies**
   ```bash
   pip install -r requirements.txt
   ```

4. **Configure environment variables**
   - Create a `.env` file in the `back-end` directory with necessary configuration

5. **Run the application**
   ```bash
   python main.py
   ```
   The API will be available at `http://localhost:8000` (or your configured port)

### Frontend Setup

1. **Open Android Studio**
   - Select "Open" and navigate to the `front-end` directory

2. **Sync Gradle**
   - Android Studio will prompt you to sync Gradle files automatically

3. **Configure Backend URL**
   - Update the API endpoint in app configuration to match your backend server

4. **Build and Run**
   - Click "Run" in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

## 📚 API Documentation

The backend provides RESTful endpoints for:

- **Authentication**: Login, logout, user registration
- **User Management**: Profile management, role-based access
- **School Management**: Classes, subjects, schedules
- *Additional endpoints based on implementation*

For detailed API documentation, refer to the backend's API documentation or visit `/api/docs` when the server is running.

## 🔐 Authentication

The system uses JWT or session-based authentication. Include authentication tokens in request headers:

```
Authorization: Bearer <token>
```

## 📱 Features

- User authentication and authorization
- Role-based access control
- Dashboard for administrative tasks
- School data management
- Mobile-first user interface
- Secure API endpoints

## 🐛 Troubleshooting

### Backend Issues
- **Module not found**: Ensure all dependencies are installed with `pip install -r requirements.txt`
- **Database errors**: Check database configuration in `app/db.py`

### Frontend Issues
- **Gradle sync fails**: Update Android Studio and Gradle wrapper
- **Build errors**: Clean project with `./gradlew clean` then rebuild

## 🤝 Contributing

1. Create a feature branch (`git checkout -b feature/AmazingFeature`)
2. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
3. Push to the branch (`git push origin feature/AmazingFeature`)
4. Open a Pull Request

## 📄 License

[Add your license here]

## 👥 Authors

- **School Management System Team** - AUB Y3S1 Mobile Development Project

## 📧 Support

For issues, questions, or suggestions, please open an issue in the repository.

---

**Last Updated**: January 2026
