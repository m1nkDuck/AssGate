"""Choose a usable Java runtime without depending on Windows launcher shims."""
import os
import pathlib
import re
import shutil
import subprocess


def find_java():
    configured = os.environ.get('JAVA')
    candidates = []
    if configured:
        candidates.append(configured)
    elif os.environ.get('JAVA_HOME'):
        candidates.append(str(pathlib.Path(os.environ['JAVA_HOME']) / 'bin' /
                              ('java.exe' if os.name == 'nt' else 'java')))
    else:
        if os.name == 'nt':
            for env_name in ('ProgramFiles', 'ProgramFiles(x86)'):
                directory = pathlib.Path(os.environ.get(env_name, 'C:/Program Files')) / 'Java'
                if directory.is_dir():
                    for home in sorted(directory.glob('*1.8*'), reverse=True):
                        candidates.append(str(home / 'bin/java.exe'))
                    for home in sorted(directory.glob('*'), reverse=True):
                        candidates.append(str(home / 'bin/java.exe'))
        fallback = shutil.which('java')
        if fallback:
            candidates.append(fallback)
    errors = []
    for candidate in dict.fromkeys(candidates):
        resolved = shutil.which(candidate)
        if not resolved:
            errors.append(candidate + ': executable not found')
            continue
        try:
            result = subprocess.run([resolved, '-version'], stdout=subprocess.PIPE,
                                    stderr=subprocess.STDOUT, text=True, timeout=5)
            match = re.search(r'version\s+"(\d+)(?:\.(\d+))?', result.stdout)
            major = int(match.group(2) or 0) if match and match.group(1) == '1' else int(match.group(1)) if match else 0
            if result.returncode == 0 and major >= 8:
                return resolved
            errors.append(candidate + ': Java 8 or later is required')
        except (OSError, subprocess.TimeoutExpired) as error:
            errors.append(candidate + ': ' + str(error))
    detail = '\n'.join(errors)
    raise RuntimeError('No usable Java 8+ runtime. Install Java or set JAVA to the full '
                       'java executable path (or JAVA_HOME to its installation folder).' +
                       ('\n' + detail if detail else ''))
