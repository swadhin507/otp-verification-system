let timerInterval = null;
let timeLeft = 300; // 5 minutes in seconds

// DOM Elements
const mobileInput = document.getElementById('mobileNumber');
const otpInput = document.getElementById('otp');
const sendBtn = document.getElementById('sendOtpBtn');
const verifyBtn = document.getElementById('verifyOtpBtn');
const resendBtn = document.getElementById('resendBtn');
const messageDiv = document.getElementById('message');
const step1 = document.getElementById('step1');
const step2 = document.getElementById('step2');
const timerSpan = document.getElementById('timer');

// Show message function
function showMessage(message, type = 'error') {
    messageDiv.textContent = message;
    messageDiv.className = `message ${type}`;
    messageDiv.style.display = 'block';
    
    // Auto hide after 5 seconds
    setTimeout(() => {
        if (messageDiv.style.display !== 'none') {
            messageDiv.style.display = 'none';
        }
    }, 5000);
}

// Set loading state for button
function setLoading(button, isLoading) {
    if (isLoading) {
        button.disabled = true;
        button.classList.add('loading');
        button.textContent = 'Loading...';
    } else {
        button.disabled = false;
        button.classList.remove('loading');
        // Restore original text
        if (button.id === 'sendOtpBtn') {
            button.textContent = 'Send OTP';
        } else if (button.id === 'verifyOtpBtn') {
            button.textContent = 'Verify OTP';
        }
    }
}

// Send OTP
async function sendOtp() {
    const mobileNumber = mobileInput.value.trim();
    
    if (!mobileNumber) {
        showMessage('Please enter your mobile number', 'error');
        mobileInput.focus();
        return;
    }
    
    if (!/^[0-9]{10}$/.test(mobileNumber)) {
        showMessage('Please enter a valid 10-digit mobile number', 'error');
        mobileInput.focus();
        return;
    }
    
    setLoading(sendBtn, true);
    
    try {
        const response = await fetch('/api/otp/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ mobileNumber })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showMessage(data.message, 'success');
            // Switch to OTP input step
            step1.classList.remove('active');
            step2.classList.add('active');
            // Store mobile number for verification
            sessionStorage.setItem('mobileNumber', mobileNumber);
            // Clear OTP input
            otpInput.value = '';
            // Start timer
            startTimer();
            // Focus on OTP input
            setTimeout(() => otpInput.focus(), 300);
        } else {
            showMessage(data.message, 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showMessage('Network error. Please check your connection and try again.', 'error');
    } finally {
        setLoading(sendBtn, false);
    }
}

// Verify OTP
async function verifyOtp() {
    const otp = otpInput.value.trim();
    const mobileNumber = sessionStorage.getItem('mobileNumber');
    
    if (!mobileNumber) {
        showMessage('Session expired. Please go back and try again.', 'error');
        goBack();
        return;
    }
    
    if (!otp) {
        showMessage('Please enter the OTP', 'error');
        otpInput.focus();
        return;
    }
    
    if (!/^[0-9]{6}$/.test(otp)) {
        showMessage('Please enter a valid 6-digit OTP', 'error');
        otpInput.focus();
        return;
    }
    
    setLoading(verifyBtn, true);
    
    try {
        const response = await fetch('/api/otp/verify', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ mobileNumber, otp })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showMessage(data.message, 'success');
            // Clear timer
            if (timerInterval) {
                clearInterval(timerInterval);
            }
            // Reset form after successful verification
            setTimeout(() => {
                resetForm();
                showMessage('🎉 Verification successful! You are now authenticated.', 'success');
            }, 2000);
        } else {
            showMessage(data.message, 'error');
            otpInput.value = '';
            otpInput.focus();
        }
    } catch (error) {
        console.error('Error:', error);
        showMessage('Network error. Please try again.', 'error');
    } finally {
        setLoading(verifyBtn, false);
    }
}

// Resend OTP
async function resendOtp() {
    const mobileNumber = sessionStorage.getItem('mobileNumber');
    
    if (!mobileNumber) {
        showMessage('Session expired. Please go back and try again.', 'error');
        goBack();
        return;
    }
    
    resendBtn.disabled = true;
    resendBtn.textContent = 'Sending...';
    
    try {
        const response = await fetch('/api/otp/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ mobileNumber })
        });
        
        const data = await response.json();
        
        if (data.success) {
            showMessage('OTP resent successfully!', 'success');
            // Reset timer
            resetTimer();
            startTimer();
            otpInput.value = '';
            otpInput.focus();
        } else {
            showMessage(data.message, 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showMessage('Network error. Please try again.', 'error');
    } finally {
        // Enable resend button after 30 seconds
        setTimeout(() => {
            if (resendBtn) {
                resendBtn.disabled = false;
                resendBtn.textContent = 'Resend OTP';
            }
        }, 30000);
    }
}

// Timer functions
function startTimer() {
    if (timerInterval) {
        clearInterval(timerInterval);
    }
    
    timeLeft = 300; // 5 minutes in seconds
    updateTimerDisplay();
    
    timerInterval = setInterval(() => {
        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            showMessage('OTP has expired. Please request a new OTP.', 'error');
            if (resendBtn) {
                resendBtn.disabled = false;
                resendBtn.textContent = 'Resend OTP';
            }
            timerSpan.textContent = '00:00';
            timerSpan.style.color = '#ef4444';
        } else {
            timeLeft--;
            updateTimerDisplay();
        }
    }, 1000);
}

function updateTimerDisplay() {
    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;
    timerSpan.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    
    // Change color based on remaining time
    if (timeLeft < 60) {
        timerSpan.style.color = '#ef4444';
        timerSpan.style.fontWeight = 'bold';
    } else if (timeLeft < 120) {
        timerSpan.style.color = '#f59e0b';
    } else {
        timerSpan.style.color = '#333';
    }
}

function resetTimer() {
    if (timerInterval) {
        clearInterval(timerInterval);
    }
    timeLeft = 300;
    updateTimerDisplay();
}

// Go back to mobile number input
function goBack() {
    if (timerInterval) {
        clearInterval(timerInterval);
    }
    step2.classList.remove('active');
    step1.classList.add('active');
    otpInput.value = '';
    sessionStorage.removeItem('mobileNumber');
    mobileInput.focus();
}

// Reset form completely
function resetForm() {
    mobileInput.value = '';
    otpInput.value = '';
    step2.classList.remove('active');
    step1.classList.add('active');
    sessionStorage.removeItem('mobileNumber');
    if (timerInterval) {
        clearInterval(timerInterval);
    }
    messageDiv.style.display = 'none';
}

// Input validation and formatting
mobileInput?.addEventListener('input', function(e) {
    this.value = this.value.replace(/[^0-9]/g, '').slice(0, 10);
});

otpInput?.addEventListener('input', function(e) {
    this.value = this.value.replace(/[^0-9]/g, '').slice(0, 6);
});

// Enter key support
mobileInput?.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        sendOtp();
    }
});

otpInput?.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        verifyOtp();
    }
});

// Auto-focus on mobile input on page load
document.addEventListener('DOMContentLoaded', () => {
    if (mobileInput) {
        setTimeout(() => mobileInput.focus(), 100);
    }
    
    // Check if there's a stored session
    const storedMobile = sessionStorage.getItem('mobileNumber');
    if (storedMobile && step2.classList.contains('active')) {
        // Resume session if needed
        startTimer();
    }
});

// Prevent form submission on enter
document.addEventListener('keypress', function(e) {
    if (e.key === 'Enter' && e.target.tagName !== 'TEXTAREA') {
        e.preventDefault();
    }
});