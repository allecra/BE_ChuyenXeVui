#!/bin/bash

# Test Email Integration Script
# This script tests the email notification functionality

echo "🧪 Testing Email Integration for Ticket Booking System"
echo "=================================================="

# Configuration
BASE_URL="http://localhost:8080/api"
JWT_TOKEN=""
SCHEDULE_ID=1
SEAT_ID=1
TEST_EMAIL="your-test-email@gmail.com"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if server is running
check_server() {
    print_status "Checking if server is running..."
    if curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        print_success "Server is running"
        return 0
    else
        print_error "Server is not running. Please start the application first."
        exit 1
    fi
}

# Function to get JWT token
get_jwt_token() {
    print_status "Getting JWT token..."
    
    # Try to login with test user
    response=$(curl -s -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d '{
            "email": "user@example.com",
            "password": "password123"
        }')
    
    JWT_TOKEN=$(echo $response | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    
    if [ -n "$JWT_TOKEN" ]; then
        print_success "JWT token obtained"
        return 0
    else
        print_error "Failed to get JWT token. Response: $response"
        print_warning "Please ensure test user exists or update credentials in script"
        exit 1
    fi
}

# Function to test booking confirmation email
test_booking_email() {
    print_status "Testing booking confirmation email..."
    
    response=$(curl -s -X POST "$BASE_URL/user/tickets/book" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
            \"scheduleId\": $SCHEDULE_ID,
            \"seatId\": $SEAT_ID,
            \"passengerInfo\": {
                \"fullName\": \"Test Email User\",
                \"phoneNumber\": \"0123456789\",
                \"email\": \"$TEST_EMAIL\",
                \"idCard\": \"123456789\"
            },
            \"sessionId\": \"test_email_session_$(date +%s)\"
        }")
    
    ticket_id=$(echo $response | grep -o '"ticketId":[0-9]*' | cut -d':' -f2)
    
    if [ -n "$ticket_id" ]; then
        print_success "Ticket booked successfully (ID: $ticket_id)"
        print_status "Check your email ($TEST_EMAIL) for booking confirmation"
        echo "Expected: Subject 'Xác nhận đặt vé - TK... - CK DatVeXe'"
        return $ticket_id
    else
        print_error "Failed to book ticket. Response: $response"
        return 0
    fi
}

# Function to test payment reminder (manual trigger)
test_payment_reminder() {
    local ticket_id=$1
    print_status "Testing payment reminder email..."
    print_warning "Payment reminder is scheduled for 3 minutes after booking"
    print_status "Wait 3 minutes or check logs for: '📧 Payment reminder email sent'"
}

# Function to test cancellation email
test_cancellation_email() {
    local ticket_id=$1
    print_status "Testing cancellation email..."
    
    response=$(curl -s -X POST "$BASE_URL/user/tickets/$ticket_id/cancel" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    if echo $response | grep -q "success.*true"; then
        print_success "Ticket cancelled successfully"
        print_status "Check your email for cancellation notification"
        echo "Expected: Subject 'Hủy vé thành công - TK... - CK DatVeXe'"
    else
        print_error "Failed to cancel ticket. Response: $response"
    fi
}

# Function to check email logs
check_email_logs() {
    print_status "Checking email logs..."
    print_status "Run this command to monitor email sending:"
    echo "tail -f logs/application.log | grep -E '(📧|💥|email)'"
    echo ""
    print_status "Look for these log messages:"
    echo "  - 📧 Booking confirmation email sent successfully"
    echo "  - 📧 Payment reminder email sent successfully"
    echo "  - 📧 Cancellation email sent successfully"
    echo "  - 💥 Failed to send ... email (if errors occur)"
}

# Function to verify email configuration
verify_email_config() {
    print_status "Verifying email configuration..."
    
    # Check if email properties are set
    if grep -q "spring.mail.host" Backend/src/main/resources/application.properties 2>/dev/null; then
        print_success "Email configuration found in application.properties"
    else
        print_warning "Email configuration not found in application.properties"
        print_status "Please add email configuration:"
        echo "spring.mail.host=smtp.gmail.com"
        echo "spring.mail.port=587"
        echo "spring.mail.username=your-email@gmail.com"
        echo "spring.mail.password=your-app-password"
        echo "spring.mail.properties.mail.smtp.auth=true"
        echo "spring.mail.properties.mail.smtp.starttls.enable=true"
    fi
}

# Main test execution
main() {
    echo "Starting email integration tests..."
    echo "Test email: $TEST_EMAIL"
    echo ""
    
    # Verify email configuration
    verify_email_config
    echo ""
    
    # Check server
    check_server
    echo ""
    
    # Get JWT token
    get_jwt_token
    echo ""
    
    # Test booking email
    ticket_id=$(test_booking_email)
    echo ""
    
    if [ "$ticket_id" -gt 0 ]; then
        # Wait a moment for email to be sent
        sleep 2
        
        # Test payment reminder info
        test_payment_reminder $ticket_id
        echo ""
        
        # Test cancellation email
        read -p "Press Enter to test cancellation email (this will cancel the ticket)..."
        test_cancellation_email $ticket_id
        echo ""
    fi
    
    # Show log monitoring info
    check_email_logs
    echo ""
    
    print_success "Email integration test completed!"
    print_status "Please check your email inbox and application logs"
    echo ""
    echo "📧 Email Test Summary:"
    echo "  1. ✅ Booking confirmation - should be received immediately"
    echo "  2. ⏰ Payment reminder - scheduled for 3 minutes after booking"
    echo "  3. ❌ Cancellation notification - should be received after cancellation"
    echo ""
    echo "🔍 Troubleshooting:"
    echo "  - Check spam/junk folder"
    echo "  - Verify email configuration in application.properties"
    echo "  - Monitor application logs for email sending status"
    echo "  - Ensure SMTP credentials are correct"
}

# Check if required parameters are provided
if [ "$#" -eq 0 ]; then
    echo "Usage: $0 [test-email]"
    echo "Example: $0 your-test-email@gmail.com"
    echo ""
    echo "Using default test email: $TEST_EMAIL"
    echo "You can change TEST_EMAIL variable in the script"
    echo ""
elif [ "$#" -eq 1 ]; then
    TEST_EMAIL=$1
fi

# Run main function
main