param(
    [string]$Adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    [string]$Serial = 'emulator-5554',
    [switch]$Stop
)
$ErrorActionPreference = 'Stop'
$agentPackage = 'com.example.appfunctions.agent'
function Invoke-Adb {
    param([Parameter(ValueFromRemainingArguments=$true)][string[]]$Arguments)
    $output = & $Adb -s $Serial @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw "adb failed: $($Arguments[0])" }
    return $output
}
if ($Stop) {
    Invoke-Adb @('shell','am','force-stop',$agentPackage)
    Write-Output 'Agent stopped; its adopted shell identity no longer runs.'
    exit
}
# Windows equivalent of the pinned official agent/startAppFunctionTestingAgent.
$packageDump = (Invoke-Adb @('shell','dumpsys','package',$agentPackage)) -join "`n"
$signatureMatch = [regex]::Match($packageDump, 'signatures:\[([^\]]+)\]')
if (-not $signatureMatch.Success) { throw 'Install official standardDebug APK first; signing information unavailable.' }
$agentSignature = $signatureMatch.Groups[1].Value.ToLowerInvariant()
$sdkVersion = [int]((Invoke-Adb @('shell','getprop','ro.build.version.sdk')) -join '').Trim()
if ($sdkVersion -ge 37) {
    # Help may return exit code1, so inspect its output without treating that as execution success.
    $allowHelp = (& $Adb -s $Serial shell cmd allowlist help 2>&1) -join "`n"
    $functionHelp = (& $Adb -s $Serial shell cmd app_function help 2>&1) -join "`n"
    if ($functionHelp.Contains('purge-allowlist-cache')) { Invoke-Adb @('shell','cmd','app_function','purge-allowlist-cache') }
    $packageSignature = "${agentPackage}:$agentSignature"
    if ($allowHelp.Contains('add-package-with-metadata-multimap')) {
        Invoke-Adb @('shell','cmd','allowlist','add-package-with-metadata-multimap','2',$packageSignature,'android.allowlist.metadata.key.APP_FUNCTION_TYPE_DOWNLOADED:boolean:true',"'*'",'ignore:string:default')
    } else {
        Invoke-Adb @('shell','cmd','allowlist','add-package-multimap','2',$packageSignature,"'*'")
    }
    if ($functionHelp.Contains('set-temporary-caller')) { Invoke-Adb @('shell','cmd','app_function','flush-allowlist-changes') }
}
Invoke-Adb @('shell','am','force-stop',$agentPackage)
$logDir = Join-Path $PSScriptRoot '..\external\agent-session'
New-Item -ItemType Directory -Path $logDir -Force | Out-Null
$arguments = @('-s',$Serial,'shell','am','instrument','-w',"$agentPackage/.ShellIdentityInstrumentation")
$agentSession = Start-Process -FilePath $Adb -ArgumentList $arguments -WindowStyle Hidden -PassThru -RedirectStandardOutput (Join-Path $logDir 'instrumentation.log') -RedirectStandardError (Join-Path $logDir 'instrumentation-error.log')
Start-Sleep -Seconds 2
if ($agentSession.HasExited) { throw 'Instrumentation exited. Inspect external/agent-session logs; do not assume privileges.' }
Write-Output "Testing Agent instrumentation running (host PID $($agentSession.Id))."
Write-Output 'Keep it alive. Stop using this script -Stop. Do not run UIAutomator/instrumented tests concurrently.'
