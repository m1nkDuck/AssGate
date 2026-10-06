#!/usr/bin/env python3
"""Offline Java ME build. Requires Java (8+) and Python 3; tools are bundled."""
import pathlib, subprocess, zipfile, shutil
from tools.java_runtime import find_java
root=pathlib.Path(__file__).resolve().parent
try:
    java=find_java()
except RuntimeError as error:
    raise SystemExit(str(error))
for required in ['tools/ecj.jar','tools/proguard.jar','tools/cldc.jar','tools/midp.jar','res/hero-atlas.png','res/boss-atlas.png']:
    if not (root/required).is_file():
        raise SystemExit('Missing required file: '+required+'. Extract the complete AshGate source package.')
classes=root/'build/classes'
shutil.rmtree(root/'build',ignore_errors=True)
classes.mkdir(parents=True)
(root/'dist').mkdir(exist_ok=True)
def run(*args): subprocess.run(list(map(str,args)),cwd=root,check=True)
boot=str(root/'tools/cldc.jar')+__import__('os').pathsep+str(root/'tools/midp.jar')
run(java,'-jar',root/'tools/ecj.jar','-source','1.3','-target','1.1','-encoding','UTF-8','-bootclasspath',boot,'-d',classes,*sorted((root/'src').glob('*.java')))
manifest='\r\n'.join(['Manifest-Version: 1.0','MIDlet-Name: AshGate','MIDlet-Version: 1.9.0','MIDlet-Vendor: AshGate Studio','MIDlet-1: AshGate,,AshGate','MicroEdition-Configuration: CLDC-1.1','MicroEdition-Profile: MIDP-2.0','Nokia-MIDlet-Original-Display-Size: 320,240','Nokia-MIDlet-Target-Display-Size: 320,240','MIDlet-Description: The Black Oath - single boss action game','',''])
with zipfile.ZipFile(root/'build/raw.jar','w',zipfile.ZIP_DEFLATED) as z:
    z.writestr('META-INF/MANIFEST.MF',manifest.encode('ascii'))
    for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes))
    for p in (root/'res').rglob('*'):
        if p.is_file():z.write(p,p.relative_to(root/'res'))
(root/'build/preverify.pro').write_text('''-injars raw.jar
-outjars ../dist/AshGate.jar
-libraryjars ../tools/cldc.jar
-libraryjars ../tools/midp.jar
-microedition
-dontshrink
-dontoptimize
-dontobfuscate
-keep public class AshGate { public *; protected *; }
''')
run(java,'-jar',root/'tools/proguard.jar','@build/preverify.pro')
jar=root/'dist/AshGate.jar'
(root/'dist/AshGate.jad').write_bytes((manifest.rstrip()+'\r\nMIDlet-Jar-URL: AshGate.jar\r\nMIDlet-Jar-Size: '+str(jar.stat().st_size)+'\r\n').encode('ascii'))
print('Built:',jar,'bytes:',jar.stat().st_size)
