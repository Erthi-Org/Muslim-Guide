"""CLI argument validation and smoke tests for check_apk.py."""
import io
import subprocess
import sys
import tempfile
import unittest
import zipfile
from pathlib import Path

SCRIPT = Path(__file__).parent / "check_apk.py"
VALID_PERMISSIONS = "package: org.erthi.guide\n"


def _make_minimal_apk() -> bytes:
    buf = io.BytesIO()
    with zipfile.ZipFile(buf, "w"):
        pass
    return buf.getvalue()


def _run(*args):
    return subprocess.run(
        [sys.executable, str(SCRIPT), *args],
        capture_output=True,
        text=True,
    )


class TestArgValidation(unittest.TestCase):
    def test_no_args_exits_2(self):
        result = _run()
        self.assertEqual(result.returncode, 2)
        self.assertIn("usage", result.stderr.lower())

    def test_one_arg_exits_2(self):
        result = _run("only_one")
        self.assertEqual(result.returncode, 2)
        self.assertIn("usage", result.stderr.lower())

    def test_extra_args_exit_2(self):
        result = _run("a", "b", "c")
        self.assertEqual(result.returncode, 2)
        self.assertIn("usage", result.stderr.lower())

    def test_usage_on_stderr_not_stdout(self):
        result = _run()
        self.assertIn("usage", result.stderr.lower())
        self.assertEqual(result.stdout, "")


class TestValidApk(unittest.TestCase):
    def setUp(self):
        self._tmp = tempfile.TemporaryDirectory()
        tmp = Path(self._tmp.name)
        self.apk = tmp / "app.apk"
        self.apk.write_bytes(_make_minimal_apk())
        self.perms = tmp / "permissions.txt"
        self.perms.write_text(VALID_PERMISSIONS)

    def tearDown(self):
        self._tmp.cleanup()

    def test_valid_apk_exits_0(self):
        result = _run(str(self.apk), str(self.perms))
        self.assertEqual(result.returncode, 0)

    def test_valid_apk_prints_passed(self):
        result = _run(str(self.apk), str(self.perms))
        self.assertIn("passed", result.stdout)


if __name__ == "__main__":
    unittest.main()
