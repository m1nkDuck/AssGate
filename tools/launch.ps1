param([ValidateSet('build', 'desktop')][string]$Mode = 'desktop')
$ErrorActionPreference = 'Stop'
$ashgateRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $ashgateRoot

function Get-VersionText([string]$Executable, [string]$Arguments) {
    $probe = New-Object System.Diagnostics.Process
    $probe.StartInfo.FileName = $Executable
    $probe.StartInfo.Arguments = $Arguments
    $probe.StartInfo.UseShellExecute = $false
    $probe.StartInfo.CreateNoWindow = $true
    $probe.StartInfo.RedirectStandardOutput = $true
    $probe.StartInfo.RedirectStandardError = $true
    try {
        [void]$probe.Start()
        if (-not $probe.WaitForExit(5000)) {
            $probe.Kill()
            [void]$probe.WaitForExit(1000)
            return $null
        }
        $versionText = $probe.StandardOutput.ReadToEnd() + $probe.StandardError.ReadToEnd()
        if ($probe.ExitCode -eq 0) { return $versionText }
    } catch { return $null } finally { $probe.Dispose() }
    return $null
}

function Resolve-Executable([string]$Candidate) {
    if (Test-Path -LiteralPath $Candidate -PathType Leaf) {
        return (Resolve-Path -LiteralPath $Candidate).Path
    }
    $command = Get-Command -Name $Candidate -CommandType Application -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($command) { return $command.Source }
    return $null
}

function Find-Java {
    $candidates = @()
    if ($env:JAVA) { $candidates += $env:JAVA }
    elseif ($env:JAVA_HOME) { $candidates += Join-Path $env:JAVA_HOME 'bin\java.exe' }
    else {
        foreach ($installRoot in @($env:ProgramFiles, ${env:ProgramFiles(x86)})) {
            if (-not $installRoot) { continue }
            $homes = @(Get-ChildItem -LiteralPath (Join-Path $installRoot 'Java') -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending)
            foreach ($runtimeDirectory in $homes) {
                if ($runtimeDirectory.Name -match '^(jdk|jre)1\.8') { $candidates += Join-Path $runtimeDirectory.FullName 'bin\java.exe' }
            }
            foreach ($runtimeDirectory in $homes) { $candidates += Join-Path $runtimeDirectory.FullName 'bin\java.exe' }
        }
        $candidates += 'java'
    }
    foreach ($candidate in @($candidates | Select-Object -Unique)) {
        $executable = Resolve-Executable $candidate
        if (-not $executable) { continue }
        $versionText = Get-VersionText $executable '-version'
        if ($versionText -match 'version\s+"(\d+)(?:\.(\d+))?') {
            $major = [int]$Matches[1]
            if ($major -eq 1) { $major = [int]$Matches[2] }
            if ($major -ge 8) { return $executable }
        }
    }
    throw 'No usable Java 8+ runtime. Install Java, or set JAVA to the full java.exe path / JAVA_HOME to its installation folder.'
}

function Find-Python {
    $candidates = @()
    if ($env:ASHGATE_PYTHON) { $candidates += $env:ASHGATE_PYTHON }
    else {
        $candidates += @('py', 'python3', 'python')
        $pythonRoot = Join-Path $env:LOCALAPPDATA 'Programs\Python'
        foreach ($runtimeDirectory in @(Get-ChildItem -LiteralPath $pythonRoot -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending)) {
            $candidates += Join-Path $runtimeDirectory.FullName 'python.exe'
        }
        foreach ($runtimeDirectory in @(Get-ChildItem -LiteralPath $env:ProgramFiles -Directory -ErrorAction SilentlyContinue | Where-Object Name -Like 'Python*' | Sort-Object Name -Descending)) {
            $candidates += Join-Path $runtimeDirectory.FullName 'python.exe'
        }
        # Optional local Codex runtime; the package also works with any normal Python 3 installation.
        $candidates += Join-Path $env:USERPROFILE '.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
    }
    foreach ($candidate in @($candidates | Select-Object -Unique)) {
        $executable = Resolve-Executable $candidate
        if (-not $executable -or $executable -match '\\Microsoft\\WindowsApps\\') { continue }
        $flags = @()
        if ((Split-Path -Leaf $executable) -ieq 'py.exe') { $flags = @('-3') }
        $versionArgs = (($flags + @('--version')) -join ' ')
        $versionText = Get-VersionText $executable $versionArgs
        if ($versionText -match 'Python 3\.') { return @{Executable = $executable; Flags = $flags} }
    }
    throw 'No usable Python 3 runtime. Install Python 3, or set ASHGATE_PYTHON to its full python.exe path. Python is only needed to rebuild the game.'
}

try {
    if ($Mode -eq 'build') {
        $python = Find-Python
        $pythonFlags = $python.Flags
        & $python.Executable @pythonFlags (Join-Path $ashgateRoot 'build.py')
    } else {
        foreach ($required in @('tools\microemulator.jar', 'dist\AshGate.jar', 'dist\AshGate.jad')) {
            if (-not (Test-Path -LiteralPath (Join-Path $ashgateRoot $required) -PathType Leaf)) {
                throw ('Missing ' + $required + '. Extract the complete AshGate package.')
            }
        }
        $java = Find-Java
        & $java '-jar' (Join-Path $ashgateRoot 'tools\microemulator.jar') '--resizableDevice' '320' '240' (Join-Path $ashgateRoot 'dist\AshGate.jad')
    }
    exit $LASTEXITCODE
} catch {
    [Console]::Error.WriteLine($_.Exception.Message)
    exit 1
}
