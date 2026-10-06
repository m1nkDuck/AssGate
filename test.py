#!/usr/bin/env python3
import pathlib, subprocess, os, zipfile, struct, sys
from tools.java_runtime import find_java
r=pathlib.Path(__file__).resolve().parent
try:
 java=find_java()
except RuntimeError as error:
 raise SystemExit(str(error))
for required in ['tools/ecj.jar','tools/microemulator.jar','dist/AshGate.jar','dist/AshGate.jad']:
 if not (r/required).is_file():raise SystemExit('Missing '+required+'. Run build.py before test.py.')
(r/'build/tests').mkdir(parents=True,exist_ok=True)
(r/'preview').mkdir(exist_ok=True)
cp=os.pathsep.join([str(r/'tools/microemulator.jar'),str(r/'dist/AshGate.jar'),str(r/'build/tests')])
def run(*a):
 p=subprocess.run(list(map(str,a)),cwd=r,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,timeout=35)
 print(p.stdout,end='');p.check_returncode();return p.stdout
log=run(sys.executable,'-m','unittest','discover','-s','tests','-p','test_*.py')
log+=run(java,'-jar',r/'tools/ecj.jar','-source','1.8','-target','1.8','-cp',cp,'-d',r/'build/tests',*sorted((r/'tests').glob('*.java')))
for name in ['CombatTest','InputTest','StoryTest','RenderTest','DirectionTest','BossArtTest','ArenaArtTest','DoubleTapTest','RollImmunityTest','AtlasTest','DeathTest']:
 log+=run(java,'-Djava.awt.headless=true','-cp',cp,name)
j=r/'dist/AshGate.jar';z=zipfile.ZipFile(j)
for name in z.namelist():
 if name.endswith('.class'):
  data=z.read(name);assert struct.unpack('>H',data[6:8])[0]==45
  if name!='Balance.class':assert b'StackMap' in data
assert not z.read('META-INF/MANIFEST.MF').startswith(b'\xef\xbb\xbf')
assert 'MIDlet-Jar-Size: '+str(j.stat().st_size) in (r/'dist/AshGate.jad').read_text()
log+='PASS: CLDC class version 45; StackMap preverification; BOM-free manifest; exact JAD size.\n'
(r/'TEST-RESULTS.txt').write_text(log,encoding='utf-8')
print('Report saved to TEST-RESULTS.txt')
