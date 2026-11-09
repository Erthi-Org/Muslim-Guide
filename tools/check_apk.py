from pathlib import Path
import sys,zipfile,hashlib
apk=Path(sys.argv[1]); permissions=Path(sys.argv[2]).read_text()
allowed="package: "+("org.erthi.noor" if "Noor" in str(apk.resolve()) else "org.erthi.guide")
for line in permissions.splitlines():
 if "uses-permission:" in line:
  assert ".DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION" in line,line
with zipfile.ZipFile(apk) as z:
 assert not any(x.startswith("lib/") for x in z.namelist()),"No native ASR expected in foundation"
 if "assets/quran/uthmani-v1.1.txt" in z.namelist():
  assert hashlib.sha256(z.read("assets/quran/uthmani-v1.1.txt")).hexdigest()=="6933e133dd56db778c801bf738848454e43648105a151e8d84d86a7cae39ec5f"
print("Built APK permission/content check passed")
