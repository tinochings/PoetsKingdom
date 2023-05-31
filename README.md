<img src="app/src/main/res/drawable-v24/logo.png" width="170">

# Poets Kingdom

Poets Kingdom is an application targeted at making poems with a custom-selected user theme. 
Furthermore, it is designed to keep poems, poem data and images with access exclusive to the application.
Only saved poem images can be shared from the application. It is a passion project and is currently ongoing.

## About

Poets Kingdom is a passion project I started for myself. I am a lover of poetry, and I love writing poetry myself. Poets Kingdom is an application I made to help me design, view and write poems in a manner I see fit. Poets Kingdom can create a poem with a colour background, an image background, an image background with an outline and a colour background with an outline.

## Features
- Save poem as an image or PDF file
- Dynamic theme styling for a poem
- Exact phrase searching and highlighting using Lucene
- Poem image viewer for viewing saved poems
- Custom font types pre-built into application (Available on Android 8.0+)

## Jetpack Compose VS XML
The majority of Poets Kingdom's UI is in Jetpack Compose. Initially, this application had an MVI implementation. However, I upgraded most of it to an MVVM implementation with the help of Jetpack Compose. In contrast, I have decided to keep the XML implementation alongside the Jetpack Compose one. Currently, saving an image is tightly coupled with the XML implementation. Intricacy is needed to migrate the class CreatePoem to a fully working Jetpack Compose implementation. The ImageViewer class is minimal in logic and implementation, henceforth making it redundant to migrate to Compose. The default Debug APK makes use of all Jetpack Compose implementations.

## Debug APK File Download
### Android 7.0+
[APK File](https://github.com/tinochings/PoetsKingdom/blob/master/app/Debug%20APK/app-debug.apk)

## Screenshots

<img src="app/Application Screenshots/Home Screen.png" width="170"> <img src="app/Application Screenshots/Create Poem.png" width="170">
<img src="app/Application Screenshots/Poem Activity.png" width="170">
<img src="app/Application Screenshots/My Images.png" width="170"> <img src="app/Application Screenshots/My Poems.png" width="170">
