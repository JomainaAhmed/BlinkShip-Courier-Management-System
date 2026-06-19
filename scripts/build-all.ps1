$services = @("eureka-server", "config-server", "auth-service", "delivery-service", "tracking-service", "admin-service", "api-gateway")

foreach ($service in $services) {
    Write-Host "Building $service..."
    Push-Location $service
    try {
        if (Test-Path "mvnw.cmd") {
            cmd /c "mvnw.cmd clean package -DskipTests"
        } else {
            mvn clean package -DskipTests
        }
    } finally {
        Pop-Location
    }
}
