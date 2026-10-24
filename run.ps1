# TravelGo - Spring Boot Run Script
$candidates = @(
    "C:\Program Files\Java\jdk-21",
    $env:JAVA_HOME,
    (Get-ChildItem -Path "c:\Users\pc\.antigravity-ide\extensions\redhat.java-*\jre\*" -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName -First 1),
    (Get-ChildItem -Path "C:\Program Files\Java\jdk*" -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName -First 1),
    (Get-ChildItem -Path "C:\Program Files\Eclipse Adoptium\jdk*" -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName -First 1)
)

$selectedJava = $null
foreach ($path in $candidates) {
    if ($path -and (Test-Path "$path\bin\java.exe")) {
        $selectedJava = $path
        break
    }
}

if (-not $selectedJava) {
    Write-Error "No valid JDK found. Please ensure Java 17+ or 21 is installed."
    exit 1
}

$env:JAVA_HOME = $selectedJava
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Write-Host "Using JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Cyan
& ".\mvnw.cmd" spring-boot:run

