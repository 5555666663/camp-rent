$ErrorActionPreference = 'Continue'
$log = 'D:\DSH\camp-rent\build.log'
function Log($m) { $m | Out-File $log -Append -Encoding UTF8 }

$env:JAVA_HOME = 'D:\jdk-21'
$env:MAVEN_HOME = 'D:\apache-maven-3.9.9'
$env:Path = 'D:\apache-maven-3.9.9\bin;' + $env:Path

# 1. Add Aliyun mirror to Maven settings
$sp = 'D:\apache-maven-3.9.9\conf\settings.xml'
try {
    $c = Get-Content $sp -Raw -Encoding UTF8
    if ($c -notmatch 'aliyunmaven') {
        $mirror = "    <mirror>`r`n      <id>aliyunmaven</id>`r`n      <mirrorOf>*</mirrorOf>`r`n      <name>aliyun</name>`r`n      <url>https://maven.aliyun.com/repository/public</url>`r`n    </mirror>`r`n"
        $c = $c -replace '<mirrors>', ("<mirrors>`r`n" + $mirror)
        Set-Content $sp -Value $c -Encoding UTF8 -NoNewline
        Log 'STEP mirror: added aliyun mirror'
    } else {
        Log 'STEP mirror: already present'
    }
} catch {
    Log ("STEP mirror ERROR: " + $_.Exception.Message)
}

# 2. Build backend
Log 'STEP build: mvn compile camp-back'
Set-Location 'D:\DSH\camp-rent\camp-back'
& 'D:\apache-maven-3.9.9\bin\mvn.cmd' compile *>> $log
Log ("STEP build exit code: " + $LASTEXITCODE)
Log 'DONE'
