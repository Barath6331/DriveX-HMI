# DriveX HMI – Electric Vehicle Landscape Digital Instrument Cluster

An automotive-grade Android Home/Ride digital instrument cluster (HMI) built with **Jetpack Compose**, **Kotlin Coroutines**, **StateFlow**, and **Clean Architecture**. Designed for landscape display on electric two-wheelers and automotive infotainment units, matching the visual guidance and dark cyberpunk aesthetics of modern EV dashboards (such as the Simple Energy digital cluster).

---

## 📸 Overview & Dashboard Features

The dashboard operates reactively from a simulated vehicle telemetry stream, delivering real-time telemetry updates approximately once per second across all standard operating scenarios and edge conditions.

### 1. Speedometer & Dynamic Dial Gauge
* **Digital Readout**: Prominent, high-contrast digital speed value (`0`, `25`, `50`, `-- km/h`).
* **Curved Radial Arc Gauge**: Custom Canvas-rendered dial gauge with radial tick marks and smooth animation (`animateFloatAsState`).
* **Dynamic Thruster Glow**: Arc changes hue dynamically based on vehicle state:
  * **Electric Cyan / Teal**: Eco / Ride modes (15–39 km/h)
  * **Sonic Orange**: Accelerating / Sonic mode (40+ km/h)
  * **Alert Red**: Motor Disabled / Safety Interlock
* **Mode & Range Badge**: Integrated pill displaying active drive mode (`PARK`, `RIDE`, `SONIC`, `DISABLED`) and calculated range (`122 km Range`).

### 2. Motor Status Module
* **Motor ON**: Illuminated emerald green halo, glowing power indicator, high-visibility "MOTOR ON" text, and "Inverter Engaged" sub-label.
* **Motor OFF**: Subdued dark graphite styling, muted indicator, and "Inverter Standby" sub-label.

### 3. Kill-Switch Status Module
* **Kill Switch Inactive**: Clean slate pill, green checkmark icon, "KILL SWITCH INACTIVE", and "Circuit Closed • Drive Allowed".
* **Kill Switch Active**: High-urgency warning card featuring a pulsing crimson alert halo (`infiniteRepeatable`), red warning border, "KILL SWITCH ACTIVE", and "Power Interlock • Motor Cut".

### 4. Cluster Telemetry & Indicator Bar
* **Top Status Bar**: DriveX vehicle brand mark, BLE pairing status, clock (`10:48 AM • FRI 24 OCT`), battery SOC pill (`94%` with dynamic fill level), and LTE signal.
* **Bottom Bar**: Odometer reading (`ODO 1324.6 km`), side stand warning (`STAND DOWN` in Parked mode), high beam indicator, and left/right turn blinkers.
* **Diagnostic Alert Banner**: Appears automatically whenever a telemetry anomaly occurs (e.g. sensor failure or stale signal).

### 5. Interactive Simulation Control Dock
A docked control strip at the bottom of the screen allowing candidates and reviewers to:
* **Toggle Auto-Cycle**: Pause / Resume the 1-second automatic sequence.
* **Jump to Any Scenario**: Direct buttons for `Parked`, `Ready`, `Riding`, `Accelerating`, and `Motor Disabled`.
* **Inject Edge Cases On-The-Fly**: Buttons to trigger **Negative Speed (-15 km/h)** and **Stale CAN Signal (Heartbeat Timeout)** to observe graceful UI degradation in real time.

---

## 🚗 Vehicle Data Scenarios

The simulated data provider emits data approximately once per second, cycling through the mandatory sequence:

| Scenario | Speed | Motor Status | Kill Switch | Ride Mode | Side Stand | Dashboard Visual Behavior |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1. Parked** | 0 km/h | **OFF** | **Inactive** | PARK | Down (Active) | Cyan/Muted gauge, Stand Down alert |
| **2. Ready** | 0 km/h | **ON** | **Inactive** | RIDE | Up | Motor ON illuminated green, Stand retracted |
| **3. Riding** | 25 km/h | **ON** | **Inactive** | ECO | Up | Speed animates to 25, teal/cyan arc fill |
| **4. Accelerating**| 50 km/h | **ON** | **Inactive** | SONIC | Up | Speed animates to 50, fiery orange thruster arc |
| **5. Motor Disabled** | 0 km/h | **OFF** | **Active** | DISABLED | Up | Motor OFF, pulsing red kill-switch warning |

---

## 🛡️ Edge Cases & Safety Handling

In automotive HMI systems, invalid telemetry or communication failures must **never** crash the UI or display misleading sensor data. The app demonstrates graceful handling of two primary edge conditions:

### Edge Case 1: Negative / Anomalous Speed Telemetry
* **Simulated Fault**: Sensor glitch emitting negative values (e.g. `-15 km/h`), `NaN`, or extreme spikes (`> 200 km/h`).
* **Repository Validation**: `VehicleRepositoryImpl.validateSpeed()` intercepts the raw reading and classifies it as `SpeedSensorStatus.Invalid`.
* **Graceful Degradation**:
  * Speedometer digital readout safely falls back to dashed representation (`-- km/h`).
  * Needle / gauge animation clamps to `0 km/h` preventing backwards or erratic arc movement.
  * An illuminated **Sensor Fault Diagnostic Banner** surfaces at the top of the screen (`⚠️ SENSOR FAULT: Negative speed sensor error (-15 km/h)`).
  * Zero application crashes.

### Edge Case 2: Stale / Missing CAN Telemetry (Signal Loss)
* **Simulated Fault**: Heartbeat silence exceeding 2.5 seconds (simulating wire disconnection, bus transceiver failure, or ECU lockup).
* **Watchdog Timer**: `VehicleRepositoryImpl` maintains a coroutine watchdog tick verifying `System.currentTimeMillis() - lastReceivedTimestamp > 2500ms`.
* **Graceful Degradation**:
  * State transitions from `DataFreshness.Fresh` to `DataFreshness.Stale`.
  * Speedometer readout switches to fallback dashes (`--`).
  * A persistent warning banner alerts the driver (`⚡ SIGNAL WARNING: CAN Heartbeat Lost (No signal for 3.5s)`).
  * When valid frames resume, freshness automatically restores to `Fresh` and normal telemetry resumes.

---

## 🏗️ Architecture & Technology Stack

The codebase follows **Clean Architecture** with strict separation between Presentation, Domain, and Data layers:

```
com.barath.drivexhmi
├── data
│   ├── VehicleDataSource.kt           // Data stream contract
│   ├── SimulatedVehicleDataSource.kt  // Coroutine/Flow-based CAN simulator (1s ticker)
│   └── VehicleRepository.kt           // Domain repository, validation & watchdog timer
├── model
│   ├── VehicleData.kt                 // Raw CAN/telemetry payload
│   ├── VehicleScenario.kt             // Parked, Ready, Riding, Accelerating, Disabled, Edge Cases
│   ├── ProcessedVehicleState.kt       // Validated, high-integrity domain model
│   ├── SpeedSensorStatus.kt           // Valid(speed) vs Invalid(raw, reason)
│   ├── DataFreshness.kt               // Fresh vs Stale(elapsedMs)
│   └── RideMode.kt                    // PARK, ECO, RIDE, SONIC, EMERGENCY
├── ui
│   ├── components
│   │   ├── SpeedometerGauge.kt        // Canvas curved radial arc & digital speed readout
│   │   ├── MotorStatusBadge.kt        // Motor ON / OFF status card with glowing halo
│   │   ├── KillSwitchBadge.kt         // Kill switch Active / Inactive card with pulsing alarm
│   │   ├── TopStatusBar.kt            // BLE, Clock, Date, Battery SOC %, LTE
│   │   ├── BottomIndicatorsBar.kt     // Odometer, Side Stand, Turn Signals, High Beam
│   │   ├── AlertBanner.kt             // Stale data & sensor glitch warning alert
│   │   └── SimulationControlPanel.kt  // Interactive test dock (Auto-cycle, Scenarios, Edge Injection)
│   ├── model
│   │   └── DashboardUiState.kt        // Immutable UI presentation state
│   ├── screens
│   │   └── DashboardScreen.kt         // Root landscape dashboard screen
│   ├── theme
│   │   ├── Color.kt                   // Automotive cyberpunk palette
│   │   ├── Theme.kt                   // Dark theme configuration
│   │   └── Type.kt                    // Cluster typography
│   └── viewmodel
│       └── DashboardViewModel.kt      // StateFlow state machine & user interaction handler
└── MainActivity.kt                    // Landscape lockdown & immersive full-screen setup
```

### Unidirectional Data Flow (UDF)
```
SimulatedVehicleDataSource (Flow<VehicleData>)
                 │
                 ▼
VehicleRepositoryImpl (Validates speed & runs CAN watchdog timer)
                 │
                 ▼
          StateFlow<ProcessedVehicleState>
                 │
                 ▼
DashboardViewModel (State machine & StateFlow mapping)
                 │
                 ▼
          StateFlow<DashboardUiState>
                 │
                 ▼
DashboardScreen (Jetpack Compose UI)
```

---

## 🧪 Unit Tests

The test suite thoroughly covers state updates across all required scenarios and edge conditions.

### Test Suites Included:
1. **`VehicleRepositoryTest`**:
   * `validateSpeed correctly classifies normal, negative, and extreme speeds`: Tests speed validation logic across 25 km/h, -15 km/h, NaN, and 250 km/h.
   * `selectScenario updates vehicle state across mandatory scenarios`: Tests end-to-end domain state for Parked, Ready, Riding, Accelerating, and Motor Disabled.
   * `edge case - stale CAN signal triggers watchdog and marks state as stale`: Verifies that heartbeat expiration transitions state to `DataFreshness.Stale`.

2. **`DashboardViewModelTest`**:
   * `viewModel correctly translates vehicle scenarios into reactive UI state`: Verifies reactive `StateFlow` emissions for speed text, motor status text, and kill-switch text.
   * `edge case - negative speed displays gracefully without crash`: Verifies UI fallback to `"--"` and diagnostic error display when negative speed occurs.
   * `edge case - stale CAN signal displays fallback and warning banner`: Verifies UI fallback to `"--"`, `isStaleData = true`, and warning banner message.
   * `toggleAutoCycle pauses and resumes automatic scenario updates`: Verifies interactive pause/resume controls.

### Running Unit Tests via CLI:
```bash
./gradlew testDebugUnitTest
```
*(On Windows PowerShell, use `.\gradlew.bat testDebugUnitTest`)*

**Test Report Summary:**
* **Total Tests**: 8
* **Failures**: 0
* **Ignored**: 0
* **Success Rate**: 100%

---

## 🚀 Build & Run Instructions

### Prerequisites:
* **JDK**: Java 17 or Java 21
* **Android SDK**: Compile SDK 35, Min SDK 24
* **Android Emulator**: Any landscape tablet or phone AVD (e.g. Pixel 9 / Pixel Tablet running API 34/35)

### Steps to Run:
1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd DriveXHMI
   ```

2. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Install and Launch on Connected Emulator / Device**:
   ```bash
   ./gradlew installDebug
   ```
   Or launch directly from **Android Studio** by clicking the **Run ▶** button.

4. **Observe the Simulation**:
   * The dashboard launches automatically in landscape orientation.
   * By default, **Auto-Cycle is ON (1s)**, smoothly advancing through:
     `Parked` → `Ready` → `Riding` → `Accelerating` → `Motor Disabled` → (loops).
   * Tap **"Scenario Switcher ▴"** at the bottom right to open the control drawer, pause auto-cycling, or trigger the **Negative Speed** and **Stale CAN Signal** edge cases.

---

## 📋 Assumptions & Limitations

1. **Simulation Timing**: Telemetry ticks are emitted every 1000ms via Coroutines. In production CAN environments, high-priority frames (speed, motor inverter status) typically cycle at 10ms–100ms.
2. **Screen Orientation**: Hard-locked to `SCREEN_ORIENTATION_SENSOR_LANDSCAPE` and configured with `FLAG_KEEP_SCREEN_ON` to prevent dimming during vehicle operation.
3. **Peripheral Signals**: Cellular LTE signal and BLE pairing pills are displayed as visual cluster elements; physical Bluetooth or CAN hardware buses are not required per assignment specifications.

