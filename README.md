# ManusClaw APK — Autonomous On-Device AI Agent

> **Standalone Android app. No PC required. No server needed.**

[![Release](https://img.shields.io/github/v/release/The-JDdev/manusclaw-apk?color=00ff88)](https://github.com/The-JDdev/manusclaw-apk/releases)
[![Android](https://img.shields.io/badge/Android-8.0%2B-00ff88)](https://github.com/The-JDdev/manusclaw-apk/releases)
[![License](https://img.shields.io/badge/License-MIT-00ff88)](LICENSE)

Built by **The-JDdev (SHS Shobuj)** · Bangladesh 🇧🇩  
Part of the [ManusClaw Ecosystem](https://github.com/The-JDdev/ManusClaw)

---

## What is ManusClaw APK?

ManusClaw APK is a **fully standalone autonomous AI agent** that runs entirely on your Android phone.

- **No PC** — runs directly on device
- **Accessibility Service** — reads and controls your screen
- **Storage access** — saves task logs to phone storage
- **Free LLM** — uses [Groq](https://console.groq.com) free API (no billing required)
- **Autonomous** — give it a task, it figures out the steps itself

---

## How it works

```
You type task → Agent calls LLM → LLM plans steps → Agent reads screen
→ Agent taps/types/swipes → Loop until done → Result shown
```

The agent uses your Groq API key (free) to think, and Android's Accessibility Service to act on your phone — like having an AI assistant that can actually *use* your apps.

---

## Install

### From Release (Recommended)

1. [Download APK](https://github.com/The-JDdev/manusclaw-apk/releases) from Releases
2. **Settings → Security → Install unknown apps → Allow**
3. Tap the APK → Install → Open

### Minimum Requirements

| | |
|---|---|
| **Android** | 8.0+ (API 26) |
| **RAM** | 2GB+ recommended |
| **Internet** | Required for LLM API calls |

---

## Setup (First Time)

### 1. Get a Free Groq API Key

1. Go to [console.groq.com](https://console.groq.com) — sign up free
2. Create an API key
3. Copy it

### 2. Configure the App

1. Open ManusClaw APK
2. Tap **⚙** (Settings)
3. Paste your Groq API key
4. Choose model (default: `llama3-8b-8192` — fast & free)
5. Tap **Save**

### 3. Enable Accessibility

1. In Settings → tap **Enable Accessibility Service**
2. Find **ManusClaw APK** in the list → Toggle **ON**
3. Accept the permission dialog

### 4. Grant Storage (Optional)

Tap **Grant Storage Access** to allow saving task logs to `/sdcard/ManusClaw/`.

---

## Usage

1. Open app
2. Type your task in the text box:
   ```
   Open Chrome and search for "ManusClaw AI agent"
   ```
3. Tap **▶ RUN AGENT**
4. Watch it work!

### Example Tasks

- `Open WhatsApp and check my messages`
- `Go to YouTube and search for lo-fi music`
- `Open Settings and check battery status`
- `Open the Calculator app and compute 347 × 29`
- `Open Chrome, go to github.com and check trending repos`

---

## Free Groq Models

| Model | Context | Speed | Best For |
|---|---|---|---|
| `llama3-8b-8192` | 8K | ⚡ Fast | Default, everyday tasks |
| `llama3-70b-8192` | 8K | 🧠 Smart | Complex tasks |
| `mixtral-8x7b-32768` | 32K | ⚖ Balanced | Long context |
| `gemma2-9b-it` | 8K | ⚡ Fast | General |

All free on Groq. No credit card needed.

---

## Architecture

```
SplashActivity
    └── MainActivity (task input + history)
        ├── AgentForegroundService (background agent runner)
        │   └── AgentEngine (LLM orchestration loop)
        │       ├── LlmClient (HTTP to Groq/OpenAI)
        │       └── Tool calls via ManusClawAccessibilityService
        └── SettingsActivity (API key, model, storage)

ManusClawAccessibilityService
    ├── dumpScreen()      — read all text on screen
    ├── listClickable()   — find tappable elements
    ├── tap(x,y)          — tap at coordinates
    ├── tapByText(text)   — find and tap by label
    ├── typeText(text)    — type into focused field
    ├── swipeUp/Down()    — scroll
    └── pressBack/Home()  — global actions
```

---

## Permissions Explained

| Permission | Why |
|---|---|
| `INTERNET` | LLM API calls (Groq) |
| `READ/WRITE_EXTERNAL_STORAGE` | Save task logs |
| `MANAGE_EXTERNAL_STORAGE` | Android 11+ storage access |
| `BIND_ACCESSIBILITY_SERVICE` | Read & control screen |
| `FOREGROUND_SERVICE` | Keep agent running in background |

---

## ManusClaw Ecosystem

| Repo | Description |
|---|---|
| [ManusClaw](https://github.com/The-JDdev/ManusClaw) | Core Python framework (PC/server) |
| [manusclaw-app](https://github.com/The-JDdev/manusclaw-app) | Android client for PC server |
| **manusclaw-apk** | **This — standalone on-device agent** |

---

## Support Development

| Method | Address |
|---|---|
| USDT TRC20 | `TH75J4zaMPwhyR3QxEFdwTCgU2Pp3yPUEr` |
| bKash | `01310211442` |
| WebMoney WMT | `T202226490170` |
| WebMoney WMZ | `Z430378899900` |

---

## License

MIT © 2025 The-JDdev (SHS Shobuj)
