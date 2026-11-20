"""CLI tests for check_apk.py."""
import subprocess
import sys
import tempfile
import unittest
import zipfile
from pathlib import Path

SCRIPT = Path(__file__).parent / "check_apk.py"
VALID_PERMISSIONS = "package: org.erthi.guide\n"


def make_minimal_apk(path: Path) -> None:
    with zipfile.ZipFile(path, "w") as zf:
        zf.writestr("AndroidManifest.xml", "")


class TestCheckApkCLI(unittest.TestCase):
    def setUp(self):
        self._tmp = tempfile.TemporaryDirectory()
        tmpdir = Path(self._tmp.name)
        self.apk = tmpdir / "app.apk"
        make_minimal_apk(self.apk)
        self.perms = tmpdir / "permissions.txt"
        self.perms.write_text(VALID_PERMISSIONS)

    def tearDown(self):
        self._tmp.cleanup()

    def _run(self, args):
        return subprocess.run(
            [sys.executable, str(SCRIPT)] + args,
            capture_output=True,
            text=True,
        )

    def test_no_args_exits_2(self):
        result = self._run([])
        self.assertEqual(result.returncode, 2)
        self.assertIn("usage", result.stderr.lower())

    def test_one_arg_exits_2(self):
        result = self._run([str(self.apk)])
        self.assertEqual(result.returncode, 2)
        self.assertIn("usage", result.stderr.lower())

    def test_extra_arg_exits_2(self):
        result = self._run([str(self.apk), str(self.perms), "extra"])
        self.assertEqual(result.returncode, 2)
        self.assertIn("usage", result.stderr.lower())

    def test_valid_args_passes(self):
        result = self._run([str(self.apk), str(self.perms)])
        self.assertEqual(result.returncode, 0)
        self.assertIn("passed", result.stdout)


if __name__ == "__main__":
    unittest.main()
