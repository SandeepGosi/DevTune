# M0 platform constraints

M0 contains no device-setting control. A normal Play-distributed application does not receive the privileged permissions required to write `Settings.Global` entries such as animation scales or to toggle Developer Options. Those capabilities are therefore deliberately absent.

The foundation supports Android 6.0/API 23 and later and compiles/targets Android 16/API 36. Dynamic color is used only on Android 12/API 31 and later; a static Material 3 scheme is used otherwise.

Future milestones must classify each system operation as readable, writable, user-granted, privileged, ADB-capable, or root-only before adding UI or implementation.

The M0 `SystemControlProvider` is a capability-reporting boundary only. It has no implementation and cannot read or write device settings.
