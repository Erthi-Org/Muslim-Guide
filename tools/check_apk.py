"""Verify the APK permissions and expected packaged assets."""
import hashlib
import re
import sys
import zipfile
from pathlib import Path

apk = Path(sys.argv[1])
permissions = Path(sys.argv[2]).read_text()
package = "org.erthi.guide"
allowed = {package + ".DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"}
assert re.search(r"(?m)^package: " + re.escape(package) + r"$", permissions), permissions
requested = set(re.findall(r"(?m)^uses-permission: name='([^']+)'", permissions))
assert requested <= allowed, requested - allowed
with zipfile.ZipFile(apk) as archive:
    libraries = {Path(name).name for name in archive.namelist()
                 if name.startswith("lib/") and name.endswith(".so")}
    unexpected = libraries - {"libandroidx.graphics.path.so"}
    assert not unexpected, f"Unexpected native libraries: {sorted(unexpected)}"
    if package == "org.erthi.noor":
        corpus = archive.read("assets/quran/uthmani-v1.1.txt")
        assert hashlib.sha256(corpus).hexdigest() == "6933e133dd56db778c801bf738848454e43648105a151e8d84d86a7cae39ec5f"
print("Built APK permission/content check passed")
