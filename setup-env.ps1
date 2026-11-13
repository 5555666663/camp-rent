# setup-env.ps1 — configure JDK21 + Maven + npm install for camp-rent
$ErrorActionPreference = 'Continue'
$ProgressPreference = 'SilentlyContinue'
$log = 'D:\DSH\camp-rent\setup-env.log'
Set-Content -Path $log -Value '' -Encoding UTF8
function Log($m) {
  $line = "[{0}] {1}" -f (Get-Date -Format 'HH:mm:ss'), $m
  Write-Output $line
  Add-Content -Path $log -Value $line -Encoding UTF8
}

Log "STEP 0: start"

# --- JDK 21 (Temurin, direct zip, no admin needed) ---
$jdkHome = 'D:\jdk-21'
if (-not (Test-Path "$jdkHome\bin\java.exe")) {
  Log "STEP 1: downloading Temurin JDK 21 (adoptium latest)..."
  $zip = 'D:\jdk21.zip'
  curl.exe -L --retry 3 -o $zip "https://mirrors.tuna.tsinghua.edu.cn/Adoptium/21/jdk/x64/windows/OpenJDK21U-jdk_x64_windows_hotspot_21.0.13_9.zip" 2>&1 | ForEach-Object { Log "curl: $_" }
  if (-not (Test-Path $zip)) { Log "FAIL: jdk zip not downloaded"; exit 1 }
  $sz = (Get-Item $zip).Length
  Log "jdk zip size: $sz"
  if ($sz -lt 50000000) { Log "FAIL: jdk zip too small ($sz)"; exit 1 }
  Log "STEP 2: extracting JDK..."
  Expand-Archive -Path $zip -DestinationPath 'D:\' -Force
  $extracted = Get-ChildItem 'D:\' -Directory -Filter 'jdk-21*' | Where-Object { $_.Name -like 'jdk-21*' } | Select-Object -First 1
  if ($extracted) {
    if ($extracted.Name -ne 'jdk-21') {
      Rename-Item -Path $extracted.FullName -NewName 'jdk-21' -Force
    }
    Log "JDK extracted to $jdkHome"
  } else {
    Log "FAIL: no jdk-21* folder found after extract"
    exit 1
  }
} else {
  Log "JDK21 already exists at $jdkHome"
}

# --- Maven 3.9.9 ---
$mvnHome = 'D:\apache-maven-3.9.9'
if (-not (Test-Path "$mvnHome\bin\mvn.cmd")) {
  Log "STEP 3: downloading Maven 3.9.9..."
  $zip = 'D:\maven.zip'
  curl.exe -L --retry 3 -o $zip "https://mirrors.huaweicloud.com/apache/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip" 2>&1 | ForEach-Object { Log "curl: $_" }
  if (Test-Path $zip) {
    Expand-Archive -Path $zip -DestinationPath 'D:\' -Force
    Log "Maven extracted"
  } else {
    Log "FAIL: maven download failed"
    exit 1
  }
} else {
  Log "Maven already exists at $mvnHome"
}

# --- env vars (session + persistent) ---
Log "STEP 4: setting env vars"
$env:JAVA_HOME = $jdkHome
$env:MAVEN_HOME = $mvnHome
$env:Path = "$mvnHome\bin;$jdkHome\bin;$env:Path"
setx JAVA_HOME "$jdkHome" | Out-Null
setx MAVEN_HOME "$mvnHome" | Out-Null
$curUser = [Environment]::GetEnvironmentVariable('Path','User')
if ($curUser -notlike '*apache-maven*') {
  [Environment]::SetEnvironmentVariable('Path', "$curUser;$mvnHome\bin", 'User')
  Log "PATH (user) updated"
}
Log "JAVA_HOME=$jdkHome"
Log "MAVEN_HOME=$mvnHome"

# --- verify ---
Log "STEP 5: java -version"
& "$jdkHome\bin\java.exe" -version 2>&1 | ForEach-Object { Log "  $_" }
Log "STEP 6: mvn -version"
& "$mvnHome\bin\mvn.cmd" -version 2>&1 | ForEach-Object { Log "  $_" }

# --- npm install ---
Log "STEP 7: npm install in camp-front"
Set-Location 'D:\DSH\camp-rent\camp-front'
npm.cmd install 2>&1 | ForEach-Object { Log "  $_" }
Log "npm install exit code: $LASTEXITCODE"

Log "DONE"
