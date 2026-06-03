# Mobile App

React Native mobil istemci #54 kapsaminda Expo Managed + TypeScript olarak
scaffold edildi.

## Stack

- Expo Managed SDK 56
- React Native 0.85
- React Navigation 7
- Expo AuthSession + SecureStore
- Expo DocumentPicker
- Axios REST client
- Pure React Native StyleSheet + shared design tokens
- Vitest ve TypeScript typecheck

## Local Run

```powershell
Copy-Item .env.example .env
npm install
npm run start
```

Android emulator host makinedeki gateway'e erisecekse
`EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080` kullanin. Fiziksel cihazda
host makinenin LAN IP adresi gerekir.

## Security

Access ve refresh token degerleri SecureStore icinde tutulur. `.env` lokal
secret kabul edilir ve commit'lenmez. Mobil istemci authorization karari
vermez; gateway ve backend servis authorization kontrolleri authoritative
kalir.

Token claim'leri yalnizca role-aware navigation icin okunur. Bu okuma backend
authorization yerine gecmez.
