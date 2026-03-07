# PowerPath

<p align="center">
  <img src="docs/logo.png" width="160"/>
</p>

<p align="center">
A modern offline gym workout tracker built with Kotlin and Jetpack Compose.
</p>

<p align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?logo=kotlin)
![Android](https://img.shields.io/badge/Platform-Android-green)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange)
![Database](https://img.shields.io/badge/Database-Room-red)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

</p>

---

# Overview

**PowerPath** is a fully **offline Android gym activity tracker** that helps users:

- create workout templates
- organize training plans
- log workouts per day
- track sets individually
- analyze performance over time

The app is designed with **modern Android architecture** and **privacy-first principles**.  
All data is stored **locally on the device**.

---

# Features

## User Profiles

Create multiple users and track their workouts independently.

Each user stores:

- Name
- Age
- Weight
- Height

---

# Workout Library

Users define reusable workouts.

Each workout contains:

- Name
- Body part
- Optional description
- Optional image

### Image Features

- Images are stored internally
- Circular thumbnails in the list
- Tap image to open **fullscreen preview**

---

# Workout Plans

Plans are reusable workout templates.

A plan contains:

- A name
- Multiple workouts
- Default workout values:
  - Weight
  - Sets
  - Reps
  - Rest time

Plans are **templates** and never modified by daily training logs.

---

# Days (Training Sessions)

Days represent **actual training sessions**.

Each day can contain:

- Multiple plans
- The same plan multiple times

Example:

```
Day 1
├ Upper Body Plan
├ Upper Body Plan
└ Core Plan
```


---

## Day Detail Tracking

Inside a day, users track **actual performance**.

### Workout Execution

Each workout shows:

- Parent summary row
- Individual rows for each set

Example:

Bench Press
☐ 80kg × 10 × 3

Set 1 ☑ 80kg × 10

Set 2 ☑ 80kg × 10

Set 3 ☐ 80kg × 8



### Behavior

- Each set has its own:
  - checkbox
  - weight
  - reps

- Parent workout checkbox:
  - auto-checks when all sets complete
  - can toggle all sets

### Overrides

Users can override:

- Weight
- Sets
- Reps
- Rest

Overrides apply **only to that day**, not the original plan.

---

## Summary and Analytics

The summary screen provides insights into training.

### Volume per Day

Volume is calculated as:

Volume = weight × reps



Displayed as:

- bar chart
- list view

---

### Workout Progress

Users can select a workout and see:

- progress over time
- sets
- reps
- weight
- total volume

Charts show:

- volume per day
- progression trend

Tapping a bar displays the exact volume.

---

# Architecture

The app follows modern **Android recommended architecture**.



UI (Jetpack Compose)
↓
ViewModels (StateFlow)
↓
Repository / Data Layer
↓
Room Database




---

# Technologies Used

## Language

Kotlin

---

## UI

Jetpack Compose  
Material 3

---

## Architecture

MVVM

---

## Dependency Injection

Hilt

---

## Persistence

Room Database

---

## Async / Reactive

Kotlin Coroutines  
StateFlow

---

## Image Loading

Coil

---

## Preferences

Jetpack DataStore

---

## Charts

Custom Compose charts

---

# Project Structure

```
app
├ data
│ ├ local
│ │ ├ dao
│ │ ├ model
│ │ ├ entities
│ │ └ PowerPathDatabase
│ │
│ └ prefs
│ PrefsRepository
│
├ ui
│ ├ components
│ ├ screens
│ └ theme
│
├ di
│ AppModule
│
└ PowerPathApp
```


---

# Image Storage

Workout images are stored in:

```
/files/workout_images/
```


Process:

1. user selects image
2. image copied to internal storage
3. Room stores file path

Benefits:

- images persist after app restart
- safe from permission issues

---

# How to Build

## Requirements

Android Studio Hedgehog or newer  
Android SDK 24+

---

## Steps

Clone repository

```
https://github.com/ehsanr-r/PowerPath.git
```


Open project in Android Studio.

Run:

```
Build → Make Project
```


Then run on emulator or device.

---

# Running on Real Device

Enable developer mode.

Enable USB debugging.

Connect device via USB.

Run from Android Studio.

---

# Future Improvements

Possible future features:

- workout timers
- rest countdown
- plate calculator
- PR tracking
- exercise history
- export to CSV
- backup / restore
- cloud sync
- WearOS support

---

# Design Goals

PowerPath focuses on:

- simplicity
- offline reliability
- fast performance
- full user control of data

---

# License

MIT License

---

# Author

PowerPath  
Developed by **ER Developments**

---

# AI Assistance

This project was developed with the assistance of AI tools.

AI was used to help with:

- architectural guidance
- code generation
- debugging
- documentation
- UI ideas









