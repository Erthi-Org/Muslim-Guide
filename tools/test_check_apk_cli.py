"""CLI tests for check_apk.py — argument validation and APK content checks."""
import io
import subprocess
import sys
import tempfile
import unittest
import zipfile
from pathlib import Path

SCRIPT = Path(__file__).parent / "check_apk.py"
PERMISSIONS_OK = "package: org.erthi.guide\n"


def _make_apk(*lib_names: str) -> bytes:
    """Return a minimal valid zip (APK) optionally containing named .so entries."""
    buf = io.BytesIO()
    with zipfile.ZipFile(buf, "w") as zf:
        zf.writestr("AndroidManifest.xml", "")
        for name in lib_names:
            zf.writestr(name, "")
    return buf.getvalue()


class TestArgValidation(unittest.TestCase):
    def _run(self, *args):
        return subprocess.run(
            [sys.executable, str(SCRIPT), *args],
            capture_output=True, text=True,
        )

    def test_no_args_exits_2(self):
        r = self._run()
        self.assertEqual(r.returncode, 2)
        self.assertIn("usage", r.stderr.lower())

    def test_one_arg_exits_2(self):
        r = self._run("app.apk")
        self.assertEqual(r.returncode, 2)
        self.assertIn("usage", r.stderr.lower())

    def test_extra_arg_exits_2(self):
        r = self._run("app.apk", "perms.txt", "extra")
        self.assertEqual(r.returncode, 2)
        self.assertIn("usage", r.stderr.lower())

    def test_usage_printed_to_stderr_not_stdout(self):
        r = self._run()
        self.assertEqual(r.stdout, "")
        self.assertIn("usage", r.stderr.lower())


class TestApkChecks(unittest.TestCase):
    def setUp(self):
        self._tmp = tempfile.TemporaryDirectory()
        tmp = Path(self._tmp.name)
        self.perms_ok = tmp / "perms.txt"
        self.perms_ok.write_text(PERMISSIONS_OK)
        self.apk_ok = tmp / "app.apk"
        self.apk_ok.write_bytes(_make_apk())
        self._tmp_dir = tmp

    def tearDown(self):
        self._tmp.cleanup()

    def _run(self, apk, perms):
        return subprocess.run(
            [sys.executable, str(SCRIPT), str(apk), str(perms)],
            capture_output=True, text=True,
        )

    def test_valid_apk_passes(self):
        r = self._run(self.apk_ok, self.perms_ok)
        self.assertEqual(r.returncode, 0)
        self.assertIn("passed", r.stdout)

    def test_allowed_library_passes(self):
        apk = self._tmp_dir / "allowed_lib.apk"
        apk.write_bytes(_make_apk("lib/arm64-v8a/libandroidx.graphics.path.so"))
        r = self._run(apk, self.perms_ok)
        self.assertEqual(r.returncode, 0)

    def test_unexpected_library_fails(self):
        apk = self._tmp_dir / "bad_lib.apk"
        apk.write_bytes(_make_apk("lib/arm64-v8a/libevil.so"))
        r = self._run(apk, self.perms_ok)
        self.assertNotEqual(r.returncode, 0)

    def test_wrong_package_fails(self):
        perms = self._tmp_dir / "wrong_pkg.txt"
        perms.write_text("package: com.evil.app\n")
        r = self._run(self.apk_ok, perms)
        self.assertNotEqual(r.returncode, 0)

    def test_disallowed_permission_fails(self):
        perms = self._tmp_dir / "extra_perm.txt"
        perms.write_text(
            "package: org.erthi.guide\n"
            "uses-permission: name='android.permission.CAMERA'\n"
        )
        r = self._run(self.apk_ok, perms)
        self.assertNotEqual(r.returncode, 0)

    def test_allowed_permission_passes(self):
        perms = self._tmp_dir / "allowed_perm.txt"
        perms.write_text(
            "package: org.erthi.guide\n"
            "uses-permission: name='org.erthi.guide.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION'\n"
        )
        r = self._run(self.apk_ok, perms)
        self.assertEqual(r.returncode, 0)


if __name__ == "__main__":
    unittest.main()
