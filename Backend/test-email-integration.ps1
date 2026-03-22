# Test Email Integration Script for PowerShell
# This script tests the email notification functionality

Write-Host "🧪 Testing Email Integration for Ticket Booking System" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# Configuration
$BASE_URL = "http://localhost:8080/api"
$JWT_TOKEN = ""
$SCHEDULE_ID = 1
$SEAT_ID = 1
$TEST_EMAIL = "your-test-email@gmail.com"

# Function to print colored output
function Write-Status {
    param($Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param($Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Warning {
    param($Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param($Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Function to check if server is running
function Test-Server {
    Write-Status "Checking if server is running..."
    try {
        $response = Invoke-RestMethod -Uri "$BASE_URL/actuator/health" -Method Get -TimeoutSec 5
        Write-Success "Server is running"
        return $true
    }
    catch {
        Write-Error "Server is not running. Please start the application first."
        return $false
    }
}

# Function to get JWT token
function Get-JwtToken {
    Write-Status "Getting JWT token..."
    
    try {
        $loginData = @{
            email = "user@example.com"
            password = "password123"
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "$BASE_URL/auth/login" -Method Post -Body $loginData -ContentType "application/json"
        
        if ($response.token) {
            $script:JWT_TOKEN = $response.token
            Write-Success "JWT token obtained"
            return $true
        }
        else {
            Write-Error "Failed to get JWT token from response"
            return $false
        }
    }
    catch {
        Write-Error "Failed to get JWT token: $($_.Exception.Message)"
        Write-Warning "Please ensure test user exists or update credentials in script"
        return $false
    }
}

# Function to test booking confirmation email
function Test-BookingEmail {
    Write-Status "Testing booking confirmation email..."
    
    try {
        $bookingData = @{
            scheduleId = $SCHEDULE_ID
            seatId = $SEAT_ID
            passengerInfo = @{
                fullName = "Test Email User"
                phoneNumber = "0123456789"
                email = $TEST_EMAIL
                idCard = "123456789"
            }
            sessionId = "test_email_session_$(Get-Date -Format 'yyyyMMddHHmmss')"
        } | ConvertTo-Json -Depth 3
        
        $headers = @{
            "Authorization" = "Bearer $JWT_TOKEN"
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-RestMethod -Uri "$BASE_URL/user/tickets/book" -Method Post -Body $bookingData -Headers $headers
        
        if ($response.data.ticketId) {
            $ticketId = $response.data.ticketId
            Write-Success "Ticket booked successfully (ID: $ticketId)"
            Write-Status "Check your email ($TEST_EMAIL) for booking confirmation"
            Write-Host "Expected: Subject 'Xác nhận đặt vé - TK... - CK DatVeXe'" -ForegroundColor Cyan
            return $ticketId
        }
        else {
            Write-Error "Failed to get ticket ID from response"
            return 0
        }
    }
    catch {
        Write-Error "Failed to book ticket: $($_.Exception.Message)"
        return 0
    }
}

# Function to test payment reminder
function Test-PaymentReminder {
    param($TicketId)
    Write-Status "Testing payment reminder email..."
    Write-Warning "Payment reminder is scheduled for 3 minutes after booking"
    Write-Status "Wait 3 minutes or check logs for: '📧 Payment reminder email sent'"
}

# Function to test cancellation email
function Test-CancellationEmail {
    param($TicketId)
    Write-Status "Testing cancellation email..."
    
    try {
        $headers = @{
            "Authorization" = "Bearer $JWT_TOKEN"
        }
        
        $response = Invoke-RestMethod -Uri "$BASE_URL/user/tickets/$TicketId/cancel" -Method Post -Headers $headers
        
        if ($response.success) {
            Write-Success "Ticket cancelled successfully"
            Write-Status "Check your email for cancellation notification"
            Write-Host "Expected: Subject 'Hủy vé thành công - TK... - CK DatVeXe'" -ForegroundColor Cyan
        }
        else {
            Write-Error "Failed to cancel ticket: $($response.message)"
        }
    }
    catch {
        Write-Error "Failed to cancel ticket: $($_.Exception.Message)"
    }
}

# Function to check email logs
function Show-EmailLogs {
    Write-Status "Checking email logs..."
    Write-Status "Run this command to monitor email sending:"
    Write-Host "Get-Content logs/application.log -Wait | Select-String -Pattern '📧|💥|email'" -ForegroundColor Yellow
    Write-Host ""
    Write-Status "Look for these log messages:"
    Write-Host "  - 📧 Booking confirmation email sent successfully" -ForegroundColor Green
    Write-Host "  - 📧 Payment reminder email sent successfully" -ForegroundColor Green
    Write-Host "  - 📧 Cancellation email sent successfully" -ForegroundColor Green
    Write-Host "  - 💥 Failed to send ... email (if errors occur)" -ForegroundColor Red
}

# Function to verify email configuration
function Test-EmailConfig {
    Write-Status "Verifying email configuration..."
    
    $configFile = "Backend/src/main/resources/application.properties"
    if (Test-Path $configFile) {
        $content = Get-Content $configFile -Raw
        if ($content -match "spring\.mail\.host") {
            Write-Success "Email configuration found in application.properties"
        }
        else {
            Write-Warning "Email configuration not found in application.properties"
            Write-Status "Please add email configuration:"
            Write-Host "spring.mail.host=smtp.gmail.com" -ForegroundColor Yellow
            Write-Host "spring.mail.port=587" -ForegroundColor Yellow
            Write-Host "spring.mail.username=your-email@gmail.com" -ForegroundColor Yellow
            Write-Host "spring.mail.password=your-app-password" -ForegroundColor Yellow
            Write-Host "spring.mail.properties.mail.smtp.auth=true" -ForegroundColor Yellow
            Write-Host "spring.mail.properties.mail.smtp.starttls.enable=true" -ForegroundColor Yellow
        }
    }
    else {
        Write-Warning "application.properties file not found"
    }
}

# Main test execution
function Start-EmailTest {
    param($TestEmail)
    
    if ($TestEmail) {
        $script:TEST_EMAIL = $TestEmail
    }
    
    Write-Host "Starting email integration tests..." -ForegroundColor Cyan
    Write-Host "Test email: $TEST_EMAIL" -ForegroundColor Cyan
    Write-Host ""
    
    # Verify email configuration
    Test-EmailConfig
    Write-Host ""
    
    # Check server
    if (-not (Test-Server)) {
        return
    }
    Write-Host ""
    
    # Get JWT token
    if (-not (Get-JwtToken)) {
        return
    }
    Write-Host ""
    
    # Test booking email
    $ticketId = Test-BookingEmail
    Write-Host ""
    
    if ($ticketId -gt 0) {
        # Wait a moment for email to be sent
        Start-Sleep -Seconds 2
        
        # Test payment reminder info
        Test-PaymentReminder $ticketId
        Write-Host ""
        
        # Test cancellation email
        Read-Host "Press Enter to test cancellation email (this will cancel the ticket)"
        Test-CancellationEmail $ticketId
        Write-Host ""
    }
    
    # Show log monitoring info
    Show-EmailLogs
    Write-Host ""
    
    Write-Success "Email integration test completed!"
    Write-Status "Please check your email inbox and application logs"
    Write-Host ""
    Write-Host "📧 Email Test Summary:" -ForegroundColor Cyan
    Write-Host "  1. ✅ Booking confirmation - should be received immediately" -ForegroundColor Green
    Write-Host "  2. ⏰ Payment reminder - scheduled for 3 minutes after booking" -ForegroundColor Yellow
    Write-Host "  3. ❌ Cancellation notification - should be received after cancellation" -ForegroundColor Red
    Write-Host ""
    Write-Host "🔍 Troubleshooting:" -ForegroundColor Cyan
    Write-Host "  - Check spam/junk folder" -ForegroundColor Yellow
    Write-Host "  - Verify email configuration in application.properties" -ForegroundColor Yellow
    Write-Host "  - Monitor application logs for email sending status" -ForegroundColor Yellow
    Write-Host "  - Ensure SMTP credentials are correct" -ForegroundColor Yellow
}

# Check parameters and run
if ($args.Count -eq 0) {
    Write-Host "Usage: .\test-email-integration.ps1 [test-email]" -ForegroundColor Yellow
    Write-Host "Example: .\test-email-integration.ps1 your-test-email@gmail.com" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Using default test email: $TEST_EMAIL" -ForegroundColor Cyan
    Write-Host "You can change TEST_EMAIL variable in the script" -ForegroundColor Cyan
    Write-Host ""
    Start-EmailTest
}
elseif ($args.Count -eq 1) {
    Start-EmailTest $args[0]
}
else {
    Write-Host "Too many arguments. Usage: .\test-email-integration.ps1 [test-email]" -ForegroundColor Red
}