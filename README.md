# PowerSaver - A NTU SC2006 Project done by TDDB-48 for AY24/25 S1


## The Application
PowerSaver is an Android (Supports from  Android 7 - API 24 and above) Application designed to help Singapore families cut down on their energy usage by providing them a easy-to-use App that allows users to calculate their power usage in the house and roughly how much it costs them as well as show how it compares to the rest of Singapore.

## This Repository
This repository holds the Android Studio project files for the PowerSaver app for use in development and testing. In addition, it holds the primary project documentation that was written by the development team throughout the initial creation of the app.
## Getting Started - Prerequisites 
In order to clone and run this project you will need the following (* are mandatory)

 - Computer System Capable of running Android Studio *
 - Android Studio (Latest version as time of writing - Android Studio Koala Feature Drop | 2024.1.2) *
	 - PowerSaver project is a Android Gradle Project utilising several Maven Repository Libraries
 - Java Runtime Environment (JRE-1.8) and Java Development Kit (JDK-23)
- Git installed on device

## Getting Started - Running Project in Android Studio

 - Downloading the project file ZIP via GitHub
	 - Currently while the app is in development stage the file can only be obtained via this method
	 - Once the ZIP file is downloaded navigate to your Android Studio file directory and Extract all contents into folder of your desire
	 - Upon launching Android Studio and navigating to the folder containing the PowerSaver project files you will be prompted to use the provided Gradle files to set up the project in Android Studio
	 - 	Ensure Project Structure of PowerSaver project is that of a Gradle project
	 - Ensure that the project detects and uses your local instance of Java
	 - Build the project to ensure there are no errors
	 - Upon initial build an online connection is needed for PowerSaver to import the required libraries


## Getting Started - Advisories when running Project
 - Note that in its current test phase there exists no means of which for external foreign devices from accessing the Gmail API and therefore the email feature - This is due to the Gmail API continuing to be on test mode while project is still in development
 
 - Project is not suited to running on devices with large screens like tablets or folding phones, the application (currently) is designed to run entirely in portrait mode on mobile devices
