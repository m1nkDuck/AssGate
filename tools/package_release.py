#!/usr/bin/env python3
"""Package and verify the built phone MIDlet without adding desktop tools."""
from pathlib import Path
import hashlib
import zipfile

root = Path(__file__).resolve().parents[1]
dist = root / "dist"
names = ("AshGate.jar", "AshGate.jad", "HUONG-DAN.txt")
payload = {name: (dist / name).read_bytes() for name in names}
with zipfile.ZipFile(dist / "AshGate.jar") as jar:
    manifest = jar.read("META-INF/MANIFEST.MF").decode("ascii")
    assert "MicroEdition-Configuration: CLDC-1.1" in manifest
    assert "MicroEdition-Profile: MIDP-2.0" in manifest
    version = next(line.split(": ", 1)[1] for line in manifest.splitlines()
                   if line.startswith("MIDlet-Version: "))
jad = payload["AshGate.jad"].decode("ascii")
assert "MIDlet-Version: " + version in jad
assert "MIDlet-Jar-Size: " + str(len(payload["AshGate.jar"])) in jad
assert "MIDlet-Jar-URL: AshGate.jar" in jad
assert version in payload["HUONG-DAN.txt"].decode("utf-8").splitlines()[0]
package = dist / ("AshGate-E72-" + version + ".zip")
with zipfile.ZipFile(package, "w", zipfile.ZIP_DEFLATED) as archive:
    for name in names:
        archive.write(dist / name, name)
with zipfile.ZipFile(package) as archive:
    assert set(archive.namelist()) == set(names)
    assert archive.testzip() is None
    for name in names:
        assert archive.read(name) == payload[name], name + " changed during packaging"
print("Verified package:", package)
print("Version:", version, "JAR bytes:", len(payload["AshGate.jar"]))
print("JAR SHA-256:", hashlib.sha256(payload["AshGate.jar"]).hexdigest())
