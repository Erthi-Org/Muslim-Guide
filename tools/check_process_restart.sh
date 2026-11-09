#!/usr/bin/env bash
set -euo pipefail

package=org.erthi.guide
test_class="$package.ProcessRestartTest"
runner="$package.test/androidx.test.runner.AndroidJUnitRunner"
report=app/build/restart-report.txt

adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r -t app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

run_phase() {
  local phase="$1" result
  result=$(adb shell am instrument -w -e class "$test_class" -e restartPhase "$phase" "$runner")
  printf '%s\n' "$result"
  grep -Eq 'OK \(1 test\)' <<< "$result"
}

{
  run_phase seed
  adb shell am start -W -n "$package/.MainActivity"
  test -n "$(adb shell pidof "$package")"
  adb shell am force-stop "$package"
  test -z "$(adb shell pidof "$package" || true)"
  echo "Confirmed app process is absent after force-stop"
  run_phase verify
} 2>&1 | tee "$report"
