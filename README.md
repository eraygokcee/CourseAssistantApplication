# Course Assistant Application
This is a mobile application designed to assist students and teachers in managing courses, attendance, surveys, and communication in a classroom environment. The app integrates Firebase for real-time data management, authentication, and offers functionalities like surveys, reporting, and attendance tracking.

# Features
* User Authentication: Registration and login functionalities using Firebase Authentication.
* Course Management: Teachers can create and manage courses, and students are automatically enrolled in classes.
* Survey Module: Teachers can create surveys, and students can participate, with results displayed through charts.
* Attendance Module: Tracks student attendance using geolocation data.
* Reporting: Generates reports for course activities and attendance.
# Technology Stack
* Languages: Java
* Platform: Android
* Backend: Firebase Realtime Database, Firebase Authentication
* Charts: MPAndroidChart (for graphical survey results)
* Location Services: Google Play Services Location API
* Other Libraries: Android Mail service, RecyclerView, and External File Storage

# Eray Gökçe's Contributions
1. Course Module
* Developed functionalities for creating and managing courses.
* Worked on integrating Firebase Realtime Database to automatically enroll students and teachers into their respective courses.
* Implemented layout and UI components for course listings and course details.
2. Survey Module
* Survey Creation: Implemented the functionality allowing teachers to create surveys and send them to students.
* Survey Participation: Developed the student interface for receiving and participating in surveys.
* Results and Analysis: Created the logic to display survey results in the form of pie and bar charts using the MPAndroidChart library.
* Anonymous Results: Ensured that survey results are displayed anonymously for both teachers and students.
* Exporting Results: Added functionality to save survey results as .csv files, accessible only by the survey creator.
