# Changelog

## 1.3.3 — 2026-11-22

- Replace the abrupt end of the startup animation with a smooth 540 ms crossfade into the main screen.
- Reveal the interface with a subtle upward motion while the splash gently enlarges and fades away.
- Blend status/navigation bar colors during the transition, and finish instantly when animations are disabled or the app is backgrounded.
- Add a standalone regression test covering 144 overlapping opacity and motion states.

## 1.3.2 — 2026-11-22

- New modern TeamUp launcher icon with the two-team shuffle motif.
- Animated opening screen: six players flow into teams along continuously interpolated paths, with a glowing ring and the text "Shuffle into teams".
- Smooth screen-refresh animation (about 60 fps), 2.3 seconds, tap to skip; does not replay when returning from background.
- Respect reduced-motion settings and leave player name fields unfocused.

## 1.3.1 — 2026-11-22

- Keep the keyboard hidden on app launch and resume unless a player name field is selected.
- Dismiss the keyboard and clear input focus when finishing name entry, using controls, opening history, or leaving the app.

## 1.3.0 — 2026-10-31

- Shuffle player names into fixed, balanced, contiguous team blocks instead of only changing row colors.
- Animate entire player rows moving to their new positions, with smooth color transitions.
- Reduce cards to 86% during movement and restore their size on arrival; keep edges inside the player list.
- Speed up the animation to 1,190 ms and remove numeric team badges from the main list and history.
- Preserve player names, settings, and draw history across app updates.

## 1.2.1 — 2026-10-18

- Avoid repeated player pairs in consecutive team draws when possible.
- Publish a signed release APK with a clear filename.

## 1.2.0 — 2026-10-17

- Added a TeamUp launcher icon.
- Added animated feedback and simultaneous team reassignment.
- Added team draw history with individual and full-history deletion.
- Saved the latest player names, settings, and team assignments locally.
- Updated the Android target to API 36 for current Google Play requirements.
- Prepared the project for a signed Play Store App Bundle.
- Reduced repeated player pairings between consecutive team draws.

## 1.1

- Added animated generate-button feedback and a local history of draws.

## 1.0

- Initial release with player and team controls, color-coded team assignments, and random team generation.
