# SPDX-License-Identifier: EPL-2.0
<#
.SYNOPSIS
Builds and installs Fast Button into an explicitly acknowledged test EDT installation.

.DESCRIPTION
Runs the complete Maven quality gate and uses the Equinox p2 director for uninstall/install. The script never edits
bundles.info or copies plug-in JARs manually. It refuses installations under Program Files and does not stop or launch
EDT unless the corresponding switch is supplied.

.PARAMETER EdtHome
Path to a disposable EDT installation that contains 1cedtc.exe.

.PARAMETER AcknowledgeTestInstallation
Required acknowledgement that EdtHome is a disposable test installation.

.EXAMPLE
.\tools\redeploy-edt.ps1 -EdtHome E:\edt-test -AcknowledgeTestInstallation
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [string]$EdtHome,

    [string]$JavaHome = $env:JAVA_HOME,
    [string]$Maven = 'mvn',
    [string]$Workspace,
    [switch]$SkipBuild,
    [switch]$StopRunningEdt,
    [switch]$Launch,
    [switch]$AcknowledgeTestInstallation
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$bundleId = 'ru.jimmo.edt.fastbutton.ui'
$installableUnit = 'ru.jimmo.edt.fastbutton.feature.feature.group'
$repositoryRoot = Split-Path -Parent $PSScriptRoot

function Resolve-RequiredPath {
    param([string]$Path, [string]$Description)

    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path)) {
        throw "$Description does not exist: $Path"
    }
    return (Resolve-Path -LiteralPath $Path).Path
}

function Assert-TestInstallation {
    param([string]$Path)

    if (-not $AcknowledgeTestInstallation) {
        throw 'Pass -AcknowledgeTestInstallation after confirming that EdtHome is a disposable test installation.'
    }

    foreach ($programFiles in @($env:ProgramFiles, ${env:ProgramFiles(x86)})) {
        if ([string]::IsNullOrWhiteSpace($programFiles)) {
            continue
        }
        $protectedRoot = [IO.Path]::GetFullPath($programFiles).TrimEnd('\') + '\'
        if ($Path.StartsWith($protectedRoot, [StringComparison]::OrdinalIgnoreCase)) {
            throw "Refusing to modify an EDT installation under Program Files: $Path"
        }
    }
}

function Invoke-Director {
    param([string[]]$Arguments)

    & $script:edtCli '-nosplash' '-application' 'org.eclipse.equinox.p2.director' @Arguments '-vm' $script:javaExe
    if ($LASTEXITCODE -ne 0) {
        throw "p2 director failed with exit code $LASTEXITCODE."
    }
}

$resolvedEdtHome = Resolve-RequiredPath $EdtHome 'EDT installation'
Assert-TestInstallation $resolvedEdtHome
$edtCli = Resolve-RequiredPath (Join-Path $resolvedEdtHome '1cedtc.exe') 'EDT command-line launcher'

if ([string]::IsNullOrWhiteSpace($JavaHome)) {
    throw 'Set JAVA_HOME or pass -JavaHome with a JDK 17 installation.'
}
$resolvedJavaHome = Resolve-RequiredPath $JavaHome 'Java installation'
$javaExe = Resolve-RequiredPath (Join-Path $resolvedJavaHome 'bin\javaw.exe') 'Java launcher'

$runningEdt = @(Get-CimInstance Win32_Process | Where-Object {
    $_.Name -match '^(1cedt|1cedtc|java|javaw)\.exe$' -and
        $_.CommandLine -and
        $_.CommandLine.IndexOf($resolvedEdtHome, [StringComparison]::OrdinalIgnoreCase) -ge 0
})
if ($runningEdt.Count -gt 0 -and -not $StopRunningEdt) {
    $processIds = ($runningEdt.ProcessId -join ', ')
    throw "The test EDT is running (PID: $processIds). Close it or pass -StopRunningEdt."
}
if ($StopRunningEdt) {
    foreach ($process in $runningEdt) {
        Write-Host "Stopping test EDT process $($process.ProcessId) ($($process.Name))..."
        Stop-Process -Id $process.ProcessId -Force
    }
}

if (-not $SkipBuild) {
    $env:JAVA_HOME = $resolvedJavaHome
    Write-Host 'Running clean verification build...'
    & $Maven -f (Join-Path $repositoryRoot 'pom.xml') -B -ntp clean verify
    if ($LASTEXITCODE -ne 0) {
        throw "Maven build failed with exit code $LASTEXITCODE."
    }
}

$p2Repository = Resolve-RequiredPath `
    (Join-Path $repositoryRoot 'repositories\ru.jimmo.edt.fastbutton.repository\target\repository') `
    'Built p2 repository'
$repositoryUri = [Uri]::new($p2Repository).AbsoluteUri

$installedRoots = & $edtCli '-nosplash' '-application' 'org.eclipse.equinox.p2.director' `
    '-listInstalledRoots' '-vm' $javaExe 2>&1
if ($LASTEXITCODE -ne 0) {
    throw "Could not inspect installed EDT features (exit code $LASTEXITCODE)."
}
if ($installedRoots -match [regex]::Escape($installableUnit)) {
    Write-Host 'Uninstalling the previous Fast Button feature...'
    Invoke-Director @('-uninstallIU', $installableUnit)
}

Write-Host 'Installing Fast Button through p2...'
Invoke-Director @('-repository', $repositoryUri, '-installIU', $installableUnit)

$publishedBundles = @(Get-ChildItem -LiteralPath (Join-Path $p2Repository 'plugins') `
    -Filter "${bundleId}_*.jar")
if ($publishedBundles.Count -ne 1) {
    throw "Expected exactly one published $bundleId bundle."
}
$publishedBundle = $publishedBundles[0]
$bundlesInfo = Resolve-RequiredPath `
    (Join-Path $resolvedEdtHome 'configuration\org.eclipse.equinox.simpleconfigurator\bundles.info') `
    'EDT bundle registry'
$bundleLine = Get-Content -LiteralPath $bundlesInfo | Where-Object { $_ -like "$bundleId,*" }
if (@($bundleLine).Count -ne 1) {
    throw "Expected exactly one active $bundleId entry in bundles.info."
}
$installedRelativePath = ($bundleLine -split ',')[2].Replace('/', '\')
$installedBundle = Resolve-RequiredPath (Join-Path $resolvedEdtHome $installedRelativePath) 'Installed bundle'
if ((Get-FileHash $publishedBundle.FullName).Hash -ne (Get-FileHash $installedBundle).Hash) {
    throw 'Installed bundle hash does not match the bundle from the built p2 repository.'
}
Write-Host "Installation verified: $installedRelativePath" -ForegroundColor Green

if ($Launch) {
    if ([string]::IsNullOrWhiteSpace($Workspace)) {
        throw 'Pass -Workspace when using -Launch.'
    }
    $resolvedWorkspace = Resolve-RequiredPath $Workspace 'EDT workspace'
    $edtExecutable = Resolve-RequiredPath (Join-Path $resolvedEdtHome '1cedt.exe') 'EDT launcher'
    Start-Process $edtExecutable -ArgumentList @('-nosplash', '-clean', '-data', $resolvedWorkspace, '-vm', $javaExe)
    Write-Host "EDT started with workspace $resolvedWorkspace."
}
