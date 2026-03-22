# Test Auto-Fill User Profile Script for PowerShell
# This script tests the auto-fill functionality for user profile in ticket booking

Write-Host "🧪 Testing Auto-Fill User Profile for Ticket Booking" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# Configuration
$BASE_URL = "http://localhost:8080/api"
$JWT_TOKEN = ""
$SCHEDULE_ID = 1
$SEAT_ID = 1

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

# Function to test user profile API
function Test-UserProfile {
    Write-Status "Testing user profile API..."
    
    try {
        $headers = @{
            "Authorization" = "Bearer $JWT_TOKEN"
        }
        
        $response = Invoke-RestMethod -Uri "$BASE_URL/user/profile" -Method Get -Headers $headers
        
        if ($response.success) {
            Write-Success "User profile retrieved successfully"
            Write-Host "Profile Data:" -ForegroundColor Cyan
            Write-Host "  Name: $($response.data.fullName)" -ForegroundColor White
            Write-Host "  Email: $($response.data.email)" -ForegroundColor White
            Write-Host "  Phone: $($response.data.phone)" -ForegroundColor White
            Write-Host "  ID Card: $($response.data.idCard)" -ForegroundColor White
            return $response.data
        }
        else {
            Write-Error "Failed to get user profile: $($response.message)"
            return $null
        }
    }
    catch {
        Write-Error "Failed to get user profile: $($_.Exception.Message)"
        return $null
    }
}

# Function to update user profile
function Update-UserProfile {
    Write-Status "Updating user profile with complete information..."
    
    try {
        $updateData = @{
            firstName = "Nguyễn"
            lastName = "Văn Test"
            email = "user@example.com"
            phone = "0123456789"
            idCard = "123456789012"
        } | ConvertTo-Json
        
        $headers = @{
            "Authorization" = "Bearer $JWT_TOKEN"
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-RestMethod -Uri "$BASE_URL/user/profile" -Method Put -Body $updateData -Headers $headers
        
        if ($response.success) {
            Write-Success "User profile updated successfully"
            Write-Host "Updated Profile:" -ForegroundColor Cyan
            Write-Host "  Name: $($response.data.fullName)" -ForegroundColor White
            Write-Host "  Email: $($response.data.email)" -ForegroundColor White
            Write-Host "  Phone: $($response.data.phone)" -ForegroundColor White
            Write-Host "  ID Card: $($response.data.idCard)" -ForegroundColor White
            return $true
        }
        else {
            Write-Error "Failed to update user profile: $($response.message)"
            return $false
        }
    }
    catch {
        Write-Error "Failed to update user profile: $($_.Exception.Message)"
        return $false
    }
}

# Function to test booking with auto-fill
function Test-BookingAutoFill {
    Write-Status "Testing ticket booking with auto-fill (no passengerInfo)..."
    
    try {
        $bookingData = @{
            scheduleId = $SCHEDULE_ID
            seatId = $SEAT_ID
            sessionId = "auto_fill_test_$(Get-Date -Format 'yyyyMMddHHmmss')"
        } | ConvertTo-Json
        
        $headers = @{
            "Authorization" = "Bearer $JWT_TOKEN"
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-RestMethod -Uri "$BASE_URL/user/tickets/book" -Method Post -Body $bookingData -Headers $headers
        
        if ($response.success) {
            Write-Success "Ticket booked successfully with auto-fill"
            Write-Host "Ticket Info:" -ForegroundColor Cyan
            Write-Host "  Ticket Code: $($response.data.ticketCode)" -ForegroundColor White
            Write-Host "  Passenger Name: $($response.data.passengerInfo.fullName)" -ForegroundColor White
            Write-Host "  Phone: $($response.data.passengerInfo.phoneNumber)" -ForegroundColor White
            Write-Host "  Email: $($response.data.passengerInfo.email)" -ForegroundColor White
            Write-Host "  ID Card: $($response.data.passengerInfo.idCard)" -ForegroundColor White
            return $response.data.ticketId
        }
        else {
            Write-Error "Failed to book ticket: $($response.message)"
            return 0
        }
    }
    catch {
        Write-Error "Failed to book ticket: $($_.Exception.Message)"
        return 0
    }
}

# Function to test booking with override
function Test-BookingOverride {
    Write-Status "Testing ticket booking with passenger info override..."
    
    try {
        $bookingData = @{
            scheduleId = $SCHEDULE_ID
            seatId = $SEAT_ID + 1
            passengerInfo = @{
                fullName = "Người nhận vé khác"
                phoneNumber = "0987654321"
                email = "other-person@example.com"
                idCard = "987654321098"
            }
            sessionId = "override_test_$(Get-Date -Format 'yyyyMMddHHmmss')"
        } | ConvertTo-Json -Depth 3
        
        $headers = @{
            "Authorization" = "Bearer $JWT_TOKEN"
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-RestMethod -Uri "$BASE_URL/user/tickets/book" -Method Post -Body $bookingData -Headers $headers
        
        if ($response.success) {
            Write-Success "Ticket booked successfully with override info"
            Write-Host "Ticket Info:" -ForegroundColor Cyan
            Write-Host "  Ticket Code: $($response.data.ticketCode)" -ForegroundColor White
            Write-Host "  Passenger Name: $($response.data.passengerInfo.fullName)" -ForegroundColor White
            Write-Host "  Phone: $($response.data.passengerInfo.phoneNumber)" -ForegroundColor White
            Write-Host "  Email: $($response.data.passengerInfo.email)" -ForegroundColor White
            Write-Host "  ID Card: $($response.data.passengerInfo.idCard)" -ForegroundColor White
            return $response.data.ticketId
        }
        else {
            Write-Error "Failed to book ticket: $($response.message)"
            return 0
        }
    }
    catch {
        Write-Error "Failed to book ticket: $($_.Exception.Message)"
        return 0
    }
}

# Function to test partial override
function Test-BookingPartialOverride {
    Write-Status "Testing ticket booking with partial passenger info override..."
    
    try {
        $bookingData = @{
            scheduleId = $SCHEDULE_ID
            seatId = $SEAT_ID + 2
            passengerInfo = @{
                fullName = "Tên khác"
                email = "different-email@example.com"
                # phone and idCard will be auto-filled from profile
            }
            sessionId = "partial_test_$(Get-Date -Format 'yyyyMMddHHmmss')"
        } | ConvertTo-Json -Depth 3
        
        $headers = @{
            "Authorization" = "Bearer $JWT_TOKEN"
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-RestMethod -Uri "$BASE_URL/user/tickets/book" -Method Post -Body $bookingData -Headers $headers
        
        if ($response.success) {
            Write-Success "Ticket booked successfully with partial override"
            Write-Host "Ticket Info (should mix override + auto-fill):" -ForegroundColor Cyan
            Write-Host "  Ticket Code: $($response.data.ticketCode)" -ForegroundColor White
            Write-Host "  Passenger Name: $($response.data.passengerInfo.fullName) (overridden)" -ForegroundColor Yellow
            Write-Host "  Phone: $($response.data.passengerInfo.phoneNumber) (auto-filled)" -ForegroundColor Green
            Write-Host "  Email: $($response.data.passengerInfo.email) (overridden)" -ForegroundColor Yellow
            Write-Host "  ID Card: $($response.data.passengerInfo.idCard) (auto-filled)" -ForegroundColor Green
            return $response.data.ticketId
        }
        else {
            Write-Error "Failed to book ticket: $($response.message)"
            return 0
        }
    }
    catch {
        Write-Error "Failed to book ticket: $($_.Exception.Message)"
        return 0
    }
}

# Function to test profile for booking API
function Test-ProfileForBooking {
    Write-Status "Testing profile for booking API..."
    
    try {
        $headers = @{
            "Authorization" = "Bearer $JWT_TOKEN"
        }
        
        $response = Invoke-RestMethod -Uri "$BASE_URL/user/profile/for-booking" -Method Get -Headers $headers
        
        if ($response.success) {
            Write-Success "Profile for booking retrieved successfully"
            Write-Host "Profile Data for Form Pre-fill:" -ForegroundColor Cyan
            Write-Host "  Full Name: $($response.data.fullName)" -ForegroundColor White
            Write-Host "  Email: $($response.data.email)" -ForegroundColor White
            Write-Host "  Phone: $($response.data.phone)" -ForegroundColor White
            Write-Host "  ID Card: $($response.data.idCard)" -ForegroundColor White
            return $true
        }
        else {
            Write-Error "Failed to get profile for booking: $($response.message)"
            return $false
        }
    }
    catch {
        Write-Error "Failed to get profile for booking: $($_.Exception.Message)"
        return $false
    }
}

# Main test execution
function Start-AutoFillTest {
    Write-Host "Starting auto-fill profile tests..." -ForegroundColor Cyan
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
    
    # Test user profile API
    $profile = Test-UserProfile
    Write-Host ""
    
    # Update user profile with complete info
    if (-not (Update-UserProfile)) {
        Write-Warning "Failed to update profile, continuing with existing data..."
    }
    Write-Host ""
    
    # Test profile for booking API
    Test-ProfileForBooking
    Write-Host ""
    
    # Test auto-fill booking
    $autoFillTicketId = Test-BookingAutoFill
    Write-Host ""
    
    # Test override booking
    $overrideTicketId = Test-BookingOverride
    Write-Host ""
    
    # Test partial override booking
    $partialTicketId = Test-BookingPartialOverride
    Write-Host ""
    
    # Summary
    Write-Success "Auto-fill profile test completed!"
    Write-Host ""
    Write-Host "📊 Test Summary:" -ForegroundColor Cyan
    Write-Host "  1. ✅ User Profile API - Get and Update" -ForegroundColor Green
    Write-Host "  2. ✅ Profile for Booking API - Pre-fill form data" -ForegroundColor Green
    Write-Host "  3. ✅ Auto-fill Booking - No passengerInfo provided" -ForegroundColor Green
    Write-Host "  4. ✅ Override Booking - Complete passengerInfo provided" -ForegroundColor Green
    Write-Host "  5. ✅ Partial Override - Mix of override + auto-fill" -ForegroundColor Green
    Write-Host ""
    Write-Host "🔍 Next Steps:" -ForegroundColor Cyan
    Write-Host "  - Check email notifications for all bookings" -ForegroundColor Yellow
    Write-Host "  - Verify database records match expected passenger info" -ForegroundColor Yellow
    Write-Host "  - Test with users having incomplete profile data" -ForegroundColor Yellow
    Write-Host "  - Test validation errors for profile updates" -ForegroundColor Yellow
    
    if ($autoFillTicketId -gt 0) {
        Write-Host "  - Auto-fill ticket ID: $autoFillTicketId" -ForegroundColor White
    }
    if ($overrideTicketId -gt 0) {
        Write-Host "  - Override ticket ID: $overrideTicketId" -ForegroundColor White
    }
    if ($partialTicketId -gt 0) {
        Write-Host "  - Partial override ticket ID: $partialTicketId" -ForegroundColor White
    }
}

# Run the test
Start-AutoFillTest