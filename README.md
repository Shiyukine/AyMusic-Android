# AyMusic-Android
AyMusic allows you to create playlists with music on **Spotify, Youtube, Deezer, Soundcloud, Bandcamp and locally from your computer or mobile**. You can use this application if you want to create a playlist of music on Spotify but there's some music that you can't find on Spotify but on another platform.

With AyMusic you can do this without any problems. Create a playlist, add your music to one platform and then add it to other platforms and AyMusic will play your favourite music!

![AyMusic](docs/image.png)

This repository is the Android application for AyMusic.

## How to use the app (development mode)
1. In `app/src/main/assets/main.js`, modify the line `94` to `if (true)` to use our public production server
2. Compile

## How to do a release build
1. Go to `Build` > `Generate Signed App Bundle or APK`
2. Choose `APK`
3. Create a key
4. Choose `Release`
5. Click `Create`

## Repos used
- [AyMusic's WebAssets](https://github.com/Shiyukine/AyMusic-WebAssets)

## Other repos
- [Electron application of AyMusic](https://github.com/Shiyukine/AyMusic-Electron)
- [iOS application of AyMusic](https://github.com/Shiyukine/AyMusic-iOS)