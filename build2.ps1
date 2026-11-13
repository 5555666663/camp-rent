$env:JAVA_HOME = 'D:\jdk-21'
$env:MAVEN_HOME = 'D:\apache-maven-3.9.9'
$env:Path = 'D:\apache-maven-3.9.9\bin;D:\jdk-21\bin;' + $env:Path

$log = New-Object System.Collections.Generic.List[string]
$log.Add('===== java -version =====')
$log.Add((java -version 2>&1) -join "`r`n")
$log.Add('===== mvn -version =====')
$log.Add((mvn -version 2>&1) -join "`r`n")
$log.Add('===== mvn compile =====')
$out = & mvn.cmd -B -f 'D:\DSH\camp-rent\camp-back\pom.xml' compile 2>&1
$log.Add(($out -join "`r`n"))
$log.Add('===== exit code: ' + $LASTEXITCODE + ' =====')
[System.IO.File]::WriteAllText('D:\DSH\camp-rent\build2.log', ($log -join "`r`n"), (New-Object System.Text.UTF8Encoding($false)))
