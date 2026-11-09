#!/usr/bin/env python3
"""Fail closed on source, content, locale and privacy regressions; no device claims."""
from pathlib import Path
import hashlib,json,re,xml.etree.ElementTree as ET
r=Path(__file__).resolve().parents[1]
base=ET.parse(r/'app/src/main/res/values/strings.xml').getroot()
strings={x.attrib['name']:x.text or '' for x in base}
for folder in ['values','values-en','values-fr','values-tr','values-ur','values-in','values-de','values-es']:
 t={x.attrib['name']:x.text or '' for x in ET.parse(r/'app/src/main/res'/folder/'strings.xml').getroot()}
 assert t.keys()==strings.keys(),f'Locale keys differ: {folder}'
 for k,v in t.items():
  assert v.strip(),(folder,k)
  assert sorted(re.findall(r'%\d+\$[ds]',v))==sorted(re.findall(r'%\d+\$[ds]',strings[k])),(folder,k)
manifest=ET.parse(r/'app/src/main/AndroidManifest.xml').getroot()
assert not manifest.findall('uses-permission'),'No network, recording, location or broad storage permissions allowed in foundation'
a=manifest.find('application');ns='{http://schemas.android.com/apk/res/android}'
assert a.get(ns+'allowBackup')=='false' and a.get(ns+'supportsRtl')=='true'
assert 'admin@erthi.org' in (r/'PRIVACY.md').read_text()
for f in r.rglob('*'):
 if '.git' in f.parts or 'build' in f.parts or not f.is_file():continue
 assert f.suffix not in {'.jks','.keystore','.p12','.pfx'},f
 if f.suffix in {'.kt','.xml','.yml','.json','.md','.properties'}:
  assert '-----BEGIN PRIVATE KEY-----' not in f.read_text(),f
registry=json.loads((r/'docs/CONTENT_REGISTRY.json').read_text())
for e in registry['entries']:
 assert all(e.get(k) for k in ['source','license','version','sha256','review_status'])
 assert hashlib.sha256((r/e['path']).read_bytes()).hexdigest() == e['sha256']
 assert e['review_status'] in ['pending','approved']
if (r/'docs/upstream/IMPORT_MANIFEST.json').exists():
 imp=json.loads((r/'docs/upstream/IMPORT_MANIFEST.json').read_text())
 assert hashlib.sha256((r/'docs/upstream'/imp['archive']).read_bytes()).hexdigest()==imp['archive_sha256']
 for e in imp['files']:
  assert hashlib.sha256((r/e['destination_path']).read_bytes()).hexdigest()==e['sha256'],e['destination_path']
 corpus=(r/'app/src/main/assets/quran/uthmani-v1.1.txt').read_bytes()
 assert hashlib.sha256(corpus).hexdigest()=='6933e133dd56db778c801bf738848454e43648105a151e8d84d86a7cae39ec5f'
 records=[line for line in corpus.decode().splitlines() if line and not line.startswith('#')]
 assert len(records)==6236
 assert len(set(x.split('|')[0] for x in records))==114
 assert b'CHANGING IT IS NOT ALLOWED' in corpus
 assert 'Copyright (c) 2026 NEXVARY' in (r/'docs/upstream/HIFZAI-MIT.txt').read_text()
 print('Source archive, migrated file digests, 6236 canonical records and license notices: PASS')
print('Eight locale sets, format arguments, privacy manifest, content registry and credential guards: PASS')
