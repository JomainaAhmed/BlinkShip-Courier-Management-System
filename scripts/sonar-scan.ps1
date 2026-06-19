$services = @("eureka-server", "config-server", "auth-service", "delivery-service", "tracking-service", "admin-service", "api-gateway")

$SONAR_URL = "http://localhost:9000"

# ============================================================
# AUTHENTICATION CONFIGURATION
# ============================================================
# Option 1 (RECOMMENDED): Use a SonarQube User Token.
#   1. Log in to SonarQube at http://localhost:9000
#   2. Go to: My Account -> Security -> Generate Tokens
#   3. Create a token of type "User Token"
#   4. Paste it below and set $USE_TOKEN = $true
#
# Option 2: Use username/password (only works if you know the
#   current admin password — SonarQube forces a change on first login).
# ============================================================

$USE_TOKEN = $false
$SONAR_TOKEN = ""            # Paste your token here if using Option 1

$SONAR_LOGIN = "admin"
$SONAR_PASS  = "jom123"       # Update this if you changed the password

# Build the authentication argument
if ($USE_TOKEN -and $SONAR_TOKEN -ne "") {
    $AUTH_ARGS = "-Dsonar.token=$SONAR_TOKEN"
    Write-Host "Using token-based authentication." -ForegroundColor Green
} else {
    $AUTH_ARGS = "-Dsonar.login=$SONAR_LOGIN -Dsonar.password=$SONAR_PASS"
    Write-Host "Using username/password authentication." -ForegroundColor Yellow
    Write-Host "TIP: If this fails, update `$SONAR_PASS or switch to a token." -ForegroundColor Yellow
}

Write-Host ""

foreach ($service in $services) {
    Write-Host "----------------------------------------------------"
    Write-Host "Running SonarQube Analysis for $service..."
    Write-Host "----------------------------------------------------"
    Push-Location $service
    try {
        if (Test-Path "mvnw.cmd") {
            cmd /c "mvnw.cmd clean verify sonar:sonar -Dsonar.host.url=$SONAR_URL $AUTH_ARGS -Dsonar.projectName=$service -Dsonar.projectKey=$service -Dspring.config.import=optional:configserver:http://localhost:8888 -Dspring.cloud.config.uri=http://localhost:8888 -Dspring.cloud.config.fail-fast=false -Dspring.datasource.url=jdbc:postgresql://localhost:5432/$($service -replace '-service','')_db2 -Dspring.datasource.username=postgres -Dspring.datasource.password=root"
        } else {
            Invoke-Expression "mvn clean verify sonar:sonar -Dsonar.host.url=$SONAR_URL $AUTH_ARGS -Dsonar.projectName=$service -Dsonar.projectKey=$service -Dspring.config.import=optional:configserver:http://localhost:8888 -Dspring.cloud.config.uri=http://localhost:8888 -Dspring.cloud.config.fail-fast=false -Dspring.datasource.url=jdbc:postgresql://localhost:5432/$($service -replace '-service','')_db2 -Dspring.datasource.username=postgres -Dspring.datasource.password=root"
        }
    } catch {
        Write-Error "Failed to scan $service"
    } finally {
        Pop-Location
    }
}
