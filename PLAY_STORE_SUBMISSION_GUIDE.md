# Card Battle RPG — Google Play Submission Guide

Package: `com.pegasus.cardbattlerpg` · Version 1.0 (versionCode 1) · minSdk 24 · compileSdk/targetSdk 37
Prepared: 25 Sep 2026

This guide covers everything Play Console asks for, in the order you'll meet it, with answers pre-filled from the app's actual code. Items marked **⛔ BLOCKER** will get the app rejected (or can't be submitted) if left as-is.

---

## 0. Compliance work — status

### ✅ Done in code
| Requirement | What was implemented | Where |
|---|---|---|
| Account deletion (in-app) | Profile → *Privacy & Account* → **Hapus Akun (Delete Account)**. Re-verifies password (email accounts), then deletes Firestore user doc + sub-collections, ranking, guild membership, own guild chat (RTDB + Firestore mirror), empty guilds the user leads, RTDB presence/matchmaking/battle rooms, Storage avatars, the Firebase Auth user, and the local save. | `online/AccountDeletionManager.kt`, `ui/profile/ProfileScreen.kt` |
| Account deletion (web) | Ready-to-host page | `legal/delete-account.html` |
| UGC: report | ⋮ menu on every guild chat message → *Laporkan pesan*; *Lapor* on each guild in the list; *Laporkan Guild* inside a guild. Reports saved to Firestore `reports/`. | `online/ModerationManager.kt`, `ui/common/ReportDialog.kt`, `ui/guild/GuildScreen.kt` |
| UGC: block | ⋮ → *Blokir*, or tick "block too" when reporting. Blocked players' messages are hidden. Unblock in Profile → *Privacy & Account*. | same |
| UGC: filtering | Profanity filter (EN + ID) masks chat, rejects bad player/guild names and descriptions; chat limited to 200 chars, names to 20. | `utils/ContentFilter.kt` |
| Terms acceptance + 13+ age gate | Required checkbox before Register / Guest, with links to Terms and Privacy Policy. | `ui/login/LoginScreen.kt` |
| Privacy policy / Terms in-app | Buttons on Login and Profile. | `utils/LegalLinks.kt` |
| Legal pages | Privacy Policy, Terms of Use (community rules), Delete Account. | `legal/*.html` |
| Target API | `compileSdk`/`targetSdk` **37** (exceeds Play's API 36 minimum). Toolchain (newest supported by Android Studio 2025.3.1): AGP 9.0.1 (built-in Kotlin), Gradle 9.6.1, Kotlin 2.3.0 + Compose compiler plugin, KSP 2.3.4 (replaces kapt); `android.suppressUnsupportedCompileSdk=37.0` in gradle.properties. Libraries: Compose BOM 2026.06.01 (Compose 1.11), Activity 1.12.4, Navigation 2.9.7, Room 2.8.5, Firebase BOM 34.19.0 (non-KTX artifacts). After upgrading Android Studio, AGP 9.1+ allows Compose 1.12 / Navigation 2.10. | `app/build.gradle.kts`, `gradle/libs.versions.toml` |
| API 36 edge-to-edge | `enableEdgeToEdge()` with light system-bar icons; root `safeDrawing` insets keep every screen clear of status bar, nav bar, cutout and keyboard; removed no-op `window.statusBarColor`. | `MainActivity.kt`, `ui/theme/Theme.kt` |
| API 36 predictive back / large screens | `enableOnBackInvokedCallback="true"`, `appCategory="game"`, `configChanges` so rotation/resizing on tablets & foldables keeps game state; `adjustResize` for chat keyboard; dark window background (no white launch flash). | `AndroidManifest.xml`, `themes.xml` |
| Backup rules (Android 12+) | `dataExtractionRules` / `fullBackupContent` exclude all app data, matching `allowBackup="false"`. | `res/xml/` |
| Verified on device | API 37 emulator, 16 KB pages (after the AGP 9 / API 37 upgrade): fresh install, launch, login, guest sign-in (Firebase 34), menu, Battle; earlier build also Story, back navigation, rotation — no crashes. Lint on API 37: no errors, no API/target issues. | — |
| 16 KB page size | `packaging.jniLibs.useLegacyPackaging = false` (native libs stored uncompressed). Verified on release + debug APKs: `zipalign -c -P 16` passes and every `.so` (`libandroidx.graphics.path.so`, `libdatastore_shared_counter.so`, 4 ABIs) has 16 KB ELF LOAD alignment. Runs on a 16 KB-page API 37 emulator. | `app/build.gradle.kts` |
| App Bundle + signing | `signingConfigs.release` reads `keystore.properties`; `./gradlew bundleRelease` produces a signed `.aab`. | `app/build.gradle.kts`, `keystore.properties` |
| R8 / shrinking | Minify + resource shrink enabled; keep rules for Firebase models; `res/raw/keep.xml` keeps drawables/audio loaded by name. | `app/proguard-rules.pro` |
| Advertising ID | `AD_ID` permission explicitly removed. Merged manifest has only INTERNET, ACCESS_NETWORK_STATE and Firebase internals. | `AndroidManifest.xml` |
| App name | "Card Battle RPG" everywhere. | `strings.xml` |
| Security rules | Firestore `reports` (create-only, as yourself); RTDB guild chat: only author can write/delete own messages, 200-char limit, indexes for deletion queries; removed unused open `world_chat`. | `firebase-rules/` |
| Loot-box odds | Already shown on Summon screen (Legendary 0.6%, Epic 5.1%, pity 75/90). | — |

### ⛔ You must still do (cannot be done from code)
1. **Fill in `keystore.properties`** (project root) with the real `storePassword` and `keyPassword` (alias is `key0`). Then run `./gradlew bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`.
   - Recommended: move `keystore.jks` out of `app/src/main/java/...` (e.g. to `~/keys/cardbattlerpg.jks`) and update `storeFile`. Back it up in two places. `keystore.properties` and `*.jks` are now git-ignored.
2. **Replace placeholders** in `legal/*.html`: `[DEVELOPER NAME]` and the support email `support@cardbattlerpg.online` (make sure the inbox exists).
3. **Upload** the three pages so these URLs work: `https://cardbattlerpg.online/privacy-policy`, `/terms`, `/delete-account` (configure the server to serve `privacy-policy.html` at `/privacy-policy`, or change the URLs in `utils/LegalLinks.kt`).
4. **Deploy the Firebase rules** from `firebase-rules/` (Firebase console → Firestore → Rules, Realtime Database → Rules). The older `firebase/` folder is outdated — don't deploy it.
5. **Moderate reports**: check Firestore `reports` (status `open`) at least daily; delete offending messages in RTDB `guild_chat/<guildId>` and rename/ban accounts as needed.
6. **Test on a device** (release build): register, guest, report/block, delete account (email + guest), and confirm data disappears in the Firebase console.
7. Increment `versionCode` for every upload.

---

## 1. Developer account prerequisites

- **Google Play Console account**: one-time **US $25** fee, identity verification (government ID; organisations also need a **D-U-N-S number**).
- **Verified contact details**: developer email + phone.
- **Personal accounts created after 13 Nov 2023** must run a **closed test with at least 12 testers opted in for 14 consecutive days** before they can apply for Production access. Plan for about 2–3 weeks. Organisation accounts are exempt.
- Payments profile: only needed if you later sell IAP or paid apps.

---

## 2. Create the app (Console → *Create app*)

| Field | Value |
|---|---|
| App name | `Card Battle RPG` (max 30 chars) |
| Default language | English (United States) **or** Indonesian — see item 9 above |
| App or game | **Game** |
| Free or paid | **Free** (can't be changed to paid later) |
| Declarations | Tick Developer Program Policies + US export laws |

---

## 3. "Set up your app" — App content declarations

Go to **Policy → App content**. Every section must say *Completed*.

### 3.1 Privacy policy ⛔ required
- URL, e.g. `https://cardbattlerpg.online/privacy-policy`. It must be public, not a PDF, not geo-blocked, and must name the app/developer.
- It must cover: what data is collected (see 3.6), why, that **Google Firebase** (Auth, Firestore, Realtime Database, Storage) processes it, how long it's kept, how to **delete the account/data**, a contact email, and a children's statement (not directed at under-13s).
- Already linked inside the app (Login screen + Profile → Privacy & Account). Source file: `legal/privacy-policy.html`.

### 3.2 App access ⛔ required (the app needs login for online features)
Choose **"All or some functionality is restricted"** and give the reviewer:

```
Name: Reviewer test account
Username/email: playreview@cardbattlerpg.online   (create a real account)
Password: <strong password>
Instructions:
1. Open the app, wait for the splash screen.
2. On the login screen choose LOGIN, enter the credentials above.
   (Alternatively tick the Terms checkbox and tap "PLAY AS GUEST ONLINE".)
3. Main menu gives access to Story, Arena (realtime PvP), Summon, Shop,
   Guild (with chat), Collection, Deck, Hero, Equipment, Missions, Ranking.
4. Realtime PvP needs a second player; use "Private Room" or Story mode to
   see battles.
5. Account deletion: Profile → Privacy & Account → Hapus Akun (Delete Account).
6. Report/block: Guild → join a guild → tap ⋮ next to any chat message.
```
Pre-level the test account (some cards, a guild) so reviewers see everything.

### 3.3 Ads
**"No, my app does not contain ads."** The code has no ad SDK. The home-screen event banners open your own website (`cardbattlerpg.online`). If they ever promote other apps or products, change this answer to *Yes*.

### 3.4 Content rating (IARC questionnaire)
- Email: your developer email. Category: **Game**.
- Suggested answers (check them against your actual art):

| Question | Answer |
|---|---|
| Violence | **Yes, fantasy violence**: characters and monsters fighting, no realistic humans harmed, no blood/gore (answer "blood" only if the attack GIFs show it) |
| Fear / horror | No (answer yes if the necromancer, skeleton or reaper art is gory or scary) |
| Sexuality / nudity | No (check the witch, fairy, priestess and angel art for suggestive outfits) |
| Language / crude humour | No |
| Controlled substances | No (potions aren't drugs) |
| Gambling (real or simulated) | No real-money gambling. On *"random items / loot boxes"*: **Yes, randomized items obtained with in-game currency only** |
| Users can interact / communicate | **Yes** (guild chat, usernames, PvP) |
| Shares user location | No |
| Digital purchases | **No** (no Google Play Billing in the app) |
| Unrestricted internet | No |

Expected outcome: roughly **PEGI 7–12 / ESRB Everyone 10+ / IARC 12+**, plus the interactive elements *"Users Interact"* and *"In-Game Randomized Items"* (if that's asked).

### 3.5 Target audience and content
- Target age groups: **13–15, 16–17, 18+**. **Do not select any group under 13.** Selecting kids pulls you into the Families policy, which bans open chat with strangers and needs certified SDKs, and gacha mechanics would get scrutinised.
- "Could your app unintentionally appeal to children?" → **No**, and say why: *"Mid-core fantasy card RPG with realtime PvP, guild chat and gacha systems; designed for teens and adults."* Keep store screenshots and wording aimed at teens and adults, not young kids.

### 3.6 Data safety ⛔ required
Based on the code (Firebase Auth email/password + anonymous, Firestore, RTDB, Storage avatar upload, guild chat):

**Overview answers**
- Does your app collect or share any required user data types? **Yes**
- Is all user data encrypted in transit? **Yes** (Firebase uses HTTPS/TLS)
- Do you provide a way for users to request data deletion? **Yes**: in-app + web URL
- Account creation methods: **Username & password (email)** + **Other: anonymous guest**
- Delete-account URL: `https://cardbattlerpg.online/delete-account` (source: `legal/delete-account.html`)

**Data types — all "Collected", none "Shared"** (Firebase acting for you is not "sharing"; neither is content users deliberately post for others to see)

| Category → Type | Collected | Required / Optional | Purposes | Ephemeral? |
|---|---|---|---|---|
| Personal info → **Email address** | Yes | Optional (guests skip it) | App functionality, Account management | No |
| Personal info → **Name** (player/display name) | Yes | Required | App functionality, Account management | No |
| Personal info → **User IDs** (Firebase UID) | Yes | Required | App functionality, Account management | No |
| Messages → **Other in-app messages** (guild chat) | Yes | Optional | App functionality | No |
| App activity → **Other user-generated content** (guild name/description) | Yes | Optional | App functionality | No |
| App activity → **App interactions / Other actions** (game progress, cloud save, rankings, battle actions) | Yes | Required | App functionality | No |

Not collected: **photos** (the custom avatar stays on the device — `uploadAvatar` is never called), location, contacts, financial info, health, device/advertising ID (no Analytics/Ads/Crashlytics SDK), crash logs, files, audio, calendar, web history.

In **"Messages → Other in-app messages"** and **"Other user-generated content"** also include moderation reports (content of reported messages).
→ Before finalising, check Google's own disclosure list for the Firebase SDKs: <https://firebase.google.com/docs/android/play-data-disclosure>. If Firebase Auth's abuse protection collects device/IP data, add **Device or other IDs → App functionality / Fraud prevention, security**.

### 3.7 Other declarations (answer them all, even when the answer is No)
| Declaration | Answer |
|---|---|
| Government app | No |
| Financial features | "My app doesn't provide any financial features" |
| Health apps | None |
| News app | No |
| Advertising ID | **No**. The app doesn't use the Ad ID (targetSdk 33+; no `AD_ID` permission in the manifest). If a merged manifest adds it, remove it with `tools:node="remove"`. |
| COVID-19 / contact tracing (if shown) | Not a contact-tracing app |
| Sensitive permissions | None to declare. Only `INTERNET` is used. The avatar uses `GetContent()` (system picker), so no photo/media permission is needed and you're compliant with the Photo & Video Permissions policy. |

---

## 4. Store presence → Main store listing

### 4.1 Text (ready to paste; adjust as you like)

**App name (≤30):** `Card Battle RPG`

**Short description (≤80):**
```
Collect heroes & cards, build your deck and battle players in realtime PvP!
```

**Full description (≤4000):**
```
Summon legendary heroes, collect powerful elemental cards and rise to the top
of the ranking in Card Battle RPG — a fantasy card battler with story
adventures, realtime PvP and guilds.

⚔️ STORY ADVENTURE
Journey through stage after stage of forests, ruins, crystal caves and dragon
lairs. Defeat bosses like the Dragon Lord and Crystal Emperor to earn Gold,
EXP and Diamonds.

🃏 COLLECT & BUILD
Collect 30+ cards across Fire, Water, Ice, Thunder, Wind, Earth, Forest,
Void and Celestial elements — from the Tiny Fairy to the Dragon King. Every
card has active, passive and ultimate skills. Build the perfect deck.

🦸 20 HEROES
Recruit heroes such as Arka, Lyra, Ignis and the Phoenix Queen. Level them up,
equip swords, armor, rings and amulets, and unlock their full power.

✨ SUMMON
Use Diamonds and tickets to summon Rare, Epic and Legendary cards, with a
pity system that raises your Legendary chance the longer you summon.

🏟️ REALTIME PVP ARENA
Jump into matchmaking and battle real players turn by turn, or create a
private room to challenge your friends. Climb the online ranking!

🛡️ GUILDS
Create or join a guild, chat with members, donate Gold and level up
your guild together.

🎯 MISSIONS & ACHIEVEMENTS
Complete daily missions and unlock dozens of achievements for extra rewards.

☁️ CLOUD SAVE
Register with email to keep your progress safe online, or jump in instantly
as a guest.

Card Battle RPG is free to play. No real-money purchases.
```
Don't use "best", "#1", "free gems", emoji spam, other games' names or keyword stuffing. Play rejects metadata like that.

### 4.2 Graphics

| Asset | Spec | Notes |
|---|---|---|
| **App icon** ⛔ | 512 × 512 px, 32-bit PNG (alpha ok), ≤ 1 MB | Play applies the rounded mask; don't add your own rounded corners or shadow. Export from `ic_launcher_foreground` + background at high resolution. |
| **Feature graphic** ⛔ | 1024 × 500 px, JPG or 24-bit PNG (**no alpha**), ≤ 15 MB | Key art + logo. Keep important content away from the edges. |
| **Phone screenshots** ⛔ | 2–8 images, JPG/24-bit PNG, each side 320–3840 px, long side ≤ 2× short side | Games: supply **at least 4 at 1080p or higher** (16:9 landscape or 9:16 portrait) to be eligible for featuring. Suggested shots: Main menu, Battle, Summon result, Collection, Arena PvP, Guild, Hero/Equipment, Story map. |
| 7-inch & 10-inch tablet screenshots | Optional; min 1080 px on the shortest side (recommended if the app runs on tablets) | |
| Promo video | Optional; public/unlisted YouTube URL, no ads on it, not age-restricted | |

Screenshots must show real in-app gameplay. Don't advertise features the app doesn't have.

### 4.3 Store settings (Console → *Store settings*)
| Field | Value |
|---|---|
| App category | **Game → Card** (alternative: Role Playing) |
| Tags | Up to 5: Card battler, Collectible card game, Fantasy, PvP, Deck building |
| Contact email ⛔ | A monitored support email (shown publicly) |
| Website | `https://cardbattlerpg.online/` |
| Phone | Optional |
| External marketing | Your choice |

---

## 5. Build and release

### 5.1 Build the bundle
```bash
./gradlew clean bundleRelease
# output: app/build/outputs/bundle/release/app-release.aab
```
Before uploading, install a release build on a real device and test: register, log in, guest login, cloud save/restore, summon, story battle, PvP (two devices), guild chat, avatar upload, logout, **delete account**.

### 5.2 Testing tracks (in this order)
1. **Internal testing** (up to 100 testers, available within minutes). Sanity-check the Play-signed build.
2. **Closed testing** (email list or Google Group). **Personal accounts need at least 12 testers opted in for 14 straight days.** Ask the testers to actually open and play the app during that time.
3. **Production access application** (Dashboard → *Apply for production*). You'll answer questions about how you recruited testers, what feedback you got and what you changed. Answer specifically.
4. **Production release**.

### 5.3 Each release needs
- The **.aab**, with `versionCode` higher than any previous upload
- **Release name**: e.g. `1.0 (1)`
- **Release notes** (≤500 chars per language), e.g.:
  ```
  <en-US>
  First release of Card Battle RPG! Story mode, 20 heroes, 30+ cards,
  summon with pity, realtime PvP arena, private rooms, guilds with chat,
  missions, achievements, rankings and cloud save.
  </en-US>
  ```
- **Countries/regions**: pick your markets (e.g. Pakistan, Indonesia, or worldwide)
- **Staged rollout**: optional % for production (e.g. start at 20%)

### 5.4 Review
- First review usually takes **a few days, and up to 7+ days**. Updates are faster.
- Watch **Inbox** and **Policy status** in Console. If rejected, fix the specific policy cited and resubmit, or appeal if you believe it's wrong.

---

## 6. Data checklist (what to have ready)

- [ ] Play Console account verified, $25 paid
- [ ] 12+ closed testers (Gmail addresses) lined up (personal accounts)
- [ ] `keystore.properties` filled (alias `key0`) + keystore backed up in 2 places
- [ ] Signed `app-release.aab`, versionCode 1
- [ ] Privacy policy URL live (`legal/privacy-policy.html`)
- [ ] Account deletion URL live (`legal/delete-account.html`); in-app button ✅
- [ ] Terms of Use URL live (`legal/terms.html`)
- [ ] Firebase rules from `firebase-rules/` deployed
- [ ] Reviewer test account (email + password), pre-levelled
- [ ] Support email address
- [ ] Website URL
- [ ] App icon 512×512 PNG
- [ ] Feature graphic 1024×500 PNG/JPG (no alpha)
- [ ] 4–8 phone screenshots ≥1080p
- [ ] (Optional) tablet screenshots, YouTube promo video
- [ ] App name, short description, full description (EN, and ID if you target Indonesia)
- [ ] Release notes
- [ ] Answers ready for: Content rating, Target audience, Data safety, Ads, App access, Financial features, Health, Government, News, Advertising ID
- [ ] Proof of rights for art and music assets
- [ ] Target countries list

---

## 7. Common rejection reasons for this type of game (avoid them)

1. **Missing account deletion**: implemented. Keep the web page live.
2. **UGC without report/block/moderation**: implemented. You must actually review `reports`.
3. **Data safety form doesn't match app behaviour**, e.g. forgetting avatar photos or chat messages.
4. **Broken login for reviewers**: test account missing, wrong password, or Firebase down or over quota.
5. **Metadata policy**: misleading screenshots, keyword stuffing, other games' names.
6. **IP infringement**: art or music that isn't yours.
7. **Crashes on review devices**: test on Android 7 (API 24) through Android 15/16, and check Pre-launch report results after uploading to internal testing.
8. **Loot box disclosure**: not required while summons use only earned currency, but **if you ever add real-money purchases of Diamonds/tickets, you must show summon odds (0.6% Legendary base, 5.1% Epic, …) before purchase** and use Google Play Billing.
