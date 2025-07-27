/**
 * Weather Dashboard JavaScript
 * Handles WebSocket connections, notifications, and UI interactions
 */

// Global variables
let stompClient = null;
let selectedCity = 'all';
let selectedCondition = 'all';
let temperatureHistory = [];
let cityHistory = {};
let chart = null;
let darkMode = false;

// DOM Elements
const elements = {
    connectButton: () => document.getElementById('connectButton'),
    disconnectButton: () => document.getElementById('disconnectButton'),
    status: () => document.getElementById('status'),
    notificationList: () => document.getElementById('notificationList'),
    citySelect: () => document.getElementById('citySelect'),
    weatherTemp: () => document.getElementById('weatherTemp'),
    weatherCondition: () => document.getElementById('weatherCondition'),
    weatherCity: () => document.getElementById('weatherCity'),
    weatherIcon: () => document.getElementById('weatherIcon'),
    themeToggle: () => document.getElementById('themeToggle')
};

/**
 * Initialize the application
 */
function initApp() {
    initChart();
    setupEventListeners();
    loadUserPreferences();
    updateDateTime();
    setInterval(updateDateTime, 1000); // Update time every second
    setupChartControls();
    
    // Load saved notifications from database
   loadSavedNotifications().then(() => {
        // Connect to WebSocket after loading saved notifications
        connect();
   });
}

/**
 * Load saved notifications from the database
 */
async function loadSavedNotifications() {
    try {
        const response = await fetch('/api/notifications');
        if (!response.ok) {
            throw new Error('Failed to fetch notifications');
        }
        
        const notifications = await response.json();
        console.log('Loaded saved notifications:', notifications);
        
        // Clear existing notifications
        const notificationList = elements.notificationList();
        if (notificationList) {
            notificationList.innerHTML = '';
        }
        
        // Display notifications in reverse order (newest first)
        notifications.forEach(notification => {
            showNotification(notification);
            
            // Update current weather with the most recent notification
            if (notifications.indexOf(notification) === 0) {
                updateCurrentWeather(notification);
                updateChart(notification);
            }
            
            // Store city data for filtering
            if (!cityHistory[notification.city]) {
                cityHistory[notification.city] = true;
            }
        });
        
        // Update city select dropdown
        updateCitySelect();
        
        // Update weather display for the selected city
        updateWeatherDisplayForCity(selectedCity);
        
    } catch (error) {
        console.error('Error loading saved notifications:', error);
    }
}

/**
 * Update date and time display
 */
function updateDateTime() {
    const dateElement = document.getElementById('currentDate');
    const timeElement = document.getElementById('currentTime');
    
    if (dateElement && timeElement) {
        const now = new Date();
        
        // Format date: Monday, Jan 1
        const dateOptions = { weekday: 'long', month: 'short', day: 'numeric' };
        dateElement.textContent = now.toLocaleDateString(undefined, dateOptions);
        
        // Format time: 12:34:56
        const timeOptions = { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false };
        timeElement.textContent = now.toLocaleTimeString(undefined, timeOptions);
    }
}

/**
 * Setup chart control buttons
 */
function setupChartControls() {
    const toggleChartTypeBtn = document.getElementById('toggleChartType');
    const clearChartDataBtn = document.getElementById('clearChartData');
    const noDataMessage = document.getElementById('noDataMessage');
    
    if (toggleChartTypeBtn) {
        toggleChartTypeBtn.addEventListener('click', function() {
            if (!chart) return;
            
            // Toggle between line and bar chart
            const newType = chart.config.type === 'line' ? 'bar' : 'line';
            chart.config.type = newType;
            
            // Update icon
            this.innerHTML = newType === 'line' ? 
                '<i class="fas fa-chart-bar"></i>' : 
                '<i class="fas fa-chart-line"></i>';
                
            chart.update();
        });
    }
    
    if (clearChartDataBtn) {
        clearChartDataBtn.addEventListener('click', function() {
            if (!chart) return;
            
            // Clear chart data
            chart.data.labels = [];
            chart.data.datasets[0].data = [];
            chart.update();
            
            // Show no data message
            if (noDataMessage) {
                noDataMessage.style.display = 'block';
            }
        });
    }
    
    // Initially hide no data message if we have data
    if (noDataMessage && chart && chart.data.labels.length > 0) {
        noDataMessage.style.display = 'none';
    }
}

/**
 * Set up event listeners
 */
function setupEventListeners() {
    // Theme toggle
    if (elements.themeToggle()) {
        elements.themeToggle().addEventListener('click', toggleDarkMode);
    }
    
    // City filter change
    if (elements.citySelect()) {
        elements.citySelect().addEventListener('change', updateUserPreferences);
    }
}

/**
 * Toggle dark mode
 */
function toggleDarkMode() {
    darkMode = !darkMode;
    document.body.classList.toggle('dark-mode', darkMode);
    
    // Update icon
    if (elements.themeToggle()) {
        elements.themeToggle().innerHTML = darkMode ? 
            '<i class="fas fa-sun"></i>' : 
            '<i class="fas fa-moon"></i>';
    }
    
    // Save preference
    localStorage.setItem('weatherDashboardDarkMode', darkMode);
    
    // Update chart theme if it exists
    if (chart) {
        chart.options.plugins.legend.labels.color = darkMode ? '#f4f4f4' : '#666';
        chart.options.scales.x.grid.color = darkMode ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)';
        chart.options.scales.y.grid.color = darkMode ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)';
        chart.options.scales.x.ticks.color = darkMode ? '#f4f4f4' : '#666';
        chart.options.scales.y.ticks.color = darkMode ? '#f4f4f4' : '#666';
        chart.update();
    }
}

/**
 * Load user preferences from localStorage
 */
function loadUserPreferences() {
    // Load dark mode preference
    const savedDarkMode = localStorage.getItem('weatherDashboardDarkMode');
    if (savedDarkMode === 'true') {
        darkMode = true;
        document.body.classList.add('dark-mode');
        if (elements.themeToggle()) {
            elements.themeToggle().innerHTML = '<i class="fas fa-sun"></i>';
        }
    }
    
    // Load city preference or default to Athlone
    const savedCity = localStorage.getItem('weatherDashboardCity');
    if (savedCity && elements.citySelect()) {
        selectedCity = savedCity;
        elements.citySelect().value = savedCity;
    } else if (elements.citySelect()) {
        selectedCity = 'Athlone';
        elements.citySelect().value = 'Athlone';
        localStorage.setItem('weatherDashboardCity', 'Athlone');
    }
    
    // Load condition preference
    const savedCondition = localStorage.getItem('weatherDashboardCondition');
    if (savedCondition) {
        selectedCondition = savedCondition;
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.classList.toggle('active', btn.getAttribute('data-condition') === savedCondition);
        });
    }
    
    // Update weather display for the selected city after a short delay
    // to ensure notifications are loaded
    setTimeout(() => updateWeatherDisplayForCity(selectedCity), 500);
}

/**
 * Initialize the temperature chart
 */
function initChart() {
    const ctx = document.getElementById('tempChart');
    if (!ctx) return;
    
    // Show no data message initially
    const noDataMessage = document.getElementById('noDataMessage');
    if (noDataMessage) {
        noDataMessage.style.display = 'block';
    }
    
    const chartConfig = {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Temperature (°C)',
                data: [],
                borderColor: '#3498db',
                backgroundColor: 'rgba(52, 152, 219, 0.2)',
                borderWidth: 2,
                tension: 0.4,
                fill: true,
                pointBackgroundColor: '#3498db',
                pointBorderColor: '#fff',
                pointRadius: 5,
                pointHoverRadius: 7,
                pointHoverBackgroundColor: '#2980b9',
                pointHoverBorderColor: '#fff',
                pointHoverBorderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: false,
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)',
                        drawBorder: false
                    },
                    ticks: {
                        font: {
                            family: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
                            size: 12
                        },
                        color: '#666'
                    }
                },
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        font: {
                            family: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
                            size: 12
                        },
                        color: '#666',
                        maxRotation: 45,
                        minRotation: 45
                    }
                }
            },
            animation: {
                duration: 1000,
                easing: 'easeOutQuart'
            },
            plugins: {
                legend: {
                    display: false // We're using our custom legend
                },
                tooltip: {
                    backgroundColor: 'rgba(0, 0, 0, 0.8)',
                    titleFont: {
                        family: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
                        size: 14,
                        weight: 'bold'
                    },
                    bodyFont: {
                        family: "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif",
                        size: 13
                    },
                    padding: 12,
                    cornerRadius: 6,
                    displayColors: false,
                    callbacks: {
                        title: function(tooltipItems) {
                            return 'Time: ' + tooltipItems[0].label;
                        },
                        label: function(context) {
                            return 'Temperature: ' + context.raw + '°C';
                        }
                    }
                }
            },
            interaction: {
                mode: 'index',
                intersect: false
            },
            elements: {
                line: {
                    tension: 0.4
                }
            }
        }
    };
    
    // Apply dark mode styles if active
    if (darkMode) {
        chartConfig.options.scales.y.grid.color = 'rgba(255, 255, 255, 0.1)';
        chartConfig.options.scales.y.ticks.color = '#f4f4f4';
        chartConfig.options.scales.x.ticks.color = '#f4f4f4';
    }
    
    chart = new Chart(ctx, chartConfig);
}

/**
 * Update chart with new temperature data
 */
function updateChart(notification) {
    if (!chart) return;
    
    // Handle different timestamp formats
    let ts = notification.timeStamp;
    let date;
    
    if (typeof ts === 'string') {
        date = new Date(ts);
    } else if (typeof ts === 'number') {
        date = new Date(ts);
    } else if (ts && ts.year && ts.monthValue) {
        // Handle Java LocalDateTime format from database
        date = new Date(
            ts.year, 
            ts.monthValue - 1, // JavaScript months are 0-indexed
            ts.dayOfMonth,
            ts.hour,
            ts.minute,
            ts.second
        );
    } else {
        date = new Date();
    }
    
    const time = date.toLocaleTimeString();
    
    // Add new data point
    chart.data.labels.push(time);
    chart.data.datasets[0].data.push(notification.temperatureCelsius);
    
    // Keep only the last 10 data points
    if (chart.data.labels.length > 10) {
        chart.data.labels.shift();
        chart.data.datasets[0].data.shift();
    }
    
    chart.update();
    
    // Hide no data message if we have data
    const noDataMessage = document.getElementById('noDataMessage');
    if (noDataMessage && chart.data.labels.length > 0) {
        noDataMessage.style.display = 'none';
    }
}

/**
 * Update current weather display
 */
function updateCurrentWeather(notification) {
    if (!elements.weatherTemp()) return;
    
    // Handle potential null values from database
    if (!notification.temperatureCelsius && notification.temperatureCelsius !== 0) {
        notification.temperatureCelsius = 0;
    }
    
    elements.weatherTemp().innerText = `${notification.temperatureCelsius.toFixed(1)}°C`;
    elements.weatherCondition().innerText = notification.condition;
    elements.weatherCity().innerText = notification.city;
    
    // Update weather icon based on condition
    let iconClass = getWeatherIconClass(notification.condition);
    elements.weatherIcon().innerHTML = `<i class="fas ${iconClass} fa-3x"></i>`;
    
    // Add animation
    elements.weatherIcon().classList.add('pulse-animation');
    setTimeout(() => {
        elements.weatherIcon().classList.remove('pulse-animation');
    }, 1000);
    
    // Update weather display background
    const conditionType = getConditionType(notification.condition);
    const weatherDisplay = document.getElementById('weatherDisplay');
    
    // Remove all condition classes
    weatherDisplay.classList.remove('rain', 'clear', 'extreme', 'snow', 'cloud');
    
    // Add the current condition class
    weatherDisplay.classList.add(conditionType);
    
    // Update date and time
    updateDateTime();
}

/**
 * Get appropriate weather icon class based on condition
 */
function getWeatherIconClass(condition) {
    if (!condition) return 'fa-cloud';
    
    const conditionLower = condition.toLowerCase();
    
    if (conditionLower.includes('rain') || conditionLower.includes('shower')) {
        return 'fa-cloud-rain';
    } else if (conditionLower.includes('clear') || conditionLower.includes('sun')) {
        return 'fa-sun';
    } else if (conditionLower.includes('cloud')) {
        return 'fa-cloud';
    } else if (conditionLower.includes('snow')) {
        return 'fa-snowflake';
    } else if (conditionLower.includes('thunder') || conditionLower.includes('storm')) {
        return 'fa-bolt';
    } else if (conditionLower.includes('wind')) {
        return 'fa-wind';
    } else if (conditionLower.includes('fog') || conditionLower.includes('mist')) {
        return 'fa-smog';
    }
    
    return 'fa-cloud';
}

/**
 * Get condition type for styling
 */
function getConditionType(condition) {
    if (!condition) return 'default';
    
    const conditionLower = condition.toLowerCase();
    
    if (conditionLower.includes('rain') || conditionLower.includes('shower')) {
        return 'rain';
    } else if (conditionLower.includes('clear') || conditionLower.includes('sun')) {
        return 'clear';
    } else if (conditionLower.includes('storm') || conditionLower.includes('extreme')) {
        return 'extreme';
    } else if (conditionLower.includes('snow')) {
        return 'snow';
    } else if (conditionLower.includes('wind')) {
        return 'wind';
    }
    
    return 'default';
}

/**
 * Set connection status
 */
function setConnected(connected) {
    elements.connectButton().disabled = connected;
    elements.disconnectButton().disabled = !connected;
    
    const statusElement = elements.status();
    statusElement.innerText = connected ? 'Connected' : 'Disconnected';
    statusElement.className = connected ? 'connected' : 'disconnected';
    
    if (!connected) {
        // Reset weather display when disconnected
        elements.weatherTemp().innerText = '--°C';
        elements.weatherCondition().innerText = '--';
        elements.weatherCity().innerText = '--';
        elements.weatherIcon().innerHTML = '<i class="fas fa-cloud fa-3x"></i>';
        
        // Clear chart
        if (chart) {
            chart.data.labels = [];
            chart.data.datasets[0].data = [];
            chart.update();
        }
    }
}

/**
 * Connect to WebSocket
 */
function connect() {
    // Establish a SockJS connection to the /ws endpoint defined in WebSocketConfig
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    // Disable debug logging
    stompClient.debug = null;

    stompClient.connect({}, function(frame) {
        setConnected(true);
        console.log('Connected: ' + frame);

        // Subscribe to the public weather notifications topic
        stompClient.subscribe('/topic/weather-notifications', function(notificationMessage) {
            console.log("Received raw message:", notificationMessage.body);
            const notification = JSON.parse(notificationMessage.body);
                console.log("Parsed weather data:", notification);
            showNotification(notification);
            updateCurrentWeather(notification);
            updateChart(notification);
            
            // Store city data for filtering
            if (!cityHistory[notification.city]) {
                cityHistory[notification.city] = true;
                updateCitySelect();
            }
        });

    }, function(error) {
        console.error('STOMP Error: ' + error);
        setConnected(false);
    });
}

/**
 * Disconnect from WebSocket
 */
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

/**
 * Update city select dropdown with cities from notifications
 */
function updateCitySelect() {
    const citySelect = elements.citySelect();
    if (!citySelect) return;
    
    const currentValue = citySelect.value;
    
    // Clear existing options except 'All Cities'
    while (citySelect.options.length > 1) {
        citySelect.remove(1);
    }
    
    // Always add Athlone
    if (!cityHistory['Athlone']) {
        cityHistory['Athlone'] = true;
    }
    
    // Add cities from history
    Object.keys(cityHistory).sort().forEach(city => {
        const option = document.createElement('option');
        option.value = city;
        option.text = city;
        citySelect.add(option);
    });
    
    // Restore selected value if it still exists
    if (Array.from(citySelect.options).some(opt => opt.value === currentValue)) {
        citySelect.value = currentValue;
    } else {
        // Default to Athlone if the current value doesn't exist
        citySelect.value = 'Athlone';
        selectedCity = 'Athlone';
    }
}

/**
 * Filter notifications by city and update current weather display
 */
function updateUserPreferences() {
    selectedCity = elements.citySelect().value;
    localStorage.setItem('weatherDashboardCity', selectedCity);
    applyFilters();
    
    // Update current weather and chart based on selected city
    updateWeatherDisplayForCity(selectedCity);
}

/**
 * Update weather display and chart for the selected city
 */
function updateWeatherDisplayForCity(city) {
    // If 'all' is selected, find the most recent notification
    if (city === 'all') {
        const notifications = document.querySelectorAll('.notification-item');
        if (notifications.length > 0) {
            // Use the first (most recent) notification
            return;
        }
    }
    
    // Find notifications for the selected city
    const cityNotifications = document.querySelectorAll(`.notification-item[data-city="${city}"]`);
    
    if (cityNotifications.length > 0) {
        // Use the first (most recent) notification for this city
        const firstNotification = cityNotifications[0];
        
        // Extract data from the notification
        const cityName = city;
        const condition = firstNotification.getAttribute('data-condition');
        const messageElement = firstNotification.querySelector('.notification-message');
        const message = messageElement ? messageElement.textContent : '';
        
        // Try to extract temperature from the message
        let temperature = 0;
        const tempMatch = message.match(/([\d.]+)°C/);
        if (tempMatch && tempMatch[1]) {
            temperature = parseFloat(tempMatch[1]);
        }
        
        // Create a notification object
        const notification = {
            city: cityName,
            condition: condition || 'Clear',
            temperatureCelsius: temperature,
            message: message,
            timeStamp: new Date()
        };
        
        // Update the current weather display and chart
        updateCurrentWeather(notification);
        updateChart(notification);
    }
}

/**
 * Filter notifications by condition
 */
function filterByCondition(button) {
    // Remove active class from all buttons
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    
    // Add active class to clicked button
    button.classList.add('active');
    
    selectedCondition = button.getAttribute('data-condition');
    localStorage.setItem('weatherDashboardCondition', selectedCondition);
    applyFilters();
}

/**
 * Apply all filters to notifications
 */
function applyFilters() {
    const items = document.querySelectorAll('.notification-item');
    
    items.forEach(item => {
        const cityMatch = selectedCity === 'all' || item.getAttribute('data-city') === selectedCity;
        const conditionMatch = selectedCondition === 'all' || item.getAttribute('data-condition') === selectedCondition;
        
        if (cityMatch && conditionMatch) {
            item.style.display = 'flex';
        } else {
            item.style.display = 'none';
        }
    });
}

/**
 * Clear all notifications
 */
function clearNotifications() {
    elements.notificationList().innerHTML = '';
}

/**
 * Show a notification
 */
function showNotification(notification) {
    console.log("Displaying notification:", notification);
    
    const notificationList = elements.notificationList();
    if (!notificationList) return;
    
    const notificationItem = document.createElement('div');
    
    // Determine notification type based on condition
    const conditionType = getConditionType(notification.condition);
    const iconClass = getWeatherIconClass(notification.condition);
    
    notificationItem.className = `notification-item ${conditionType}`;
    notificationItem.setAttribute('data-city', notification.city);
    notificationItem.setAttribute('data-condition', conditionType);
    
    // Add animation
    notificationItem.style.animation = 'fadeIn 0.5s ease-out';

    // Add weather icon
    const iconDiv = document.createElement('div');
    iconDiv.className = `weather-icon ${conditionType}`;
    iconDiv.innerHTML = `<i class="fas ${iconClass}"></i>`;
    
    const messageDiv = document.createElement('div');
    messageDiv.className = 'notification-message';
    messageDiv.innerHTML = `<strong>${notification.city}:</strong> ${notification.message}`;
    
    if (notification.temperatureCelsius) {
        messageDiv.innerHTML += ` <span style="color: #e67e22;">${notification.temperatureCelsius.toFixed(1)}°C</span>`;
    }

    const timestampSpan = document.createElement('span');
    timestampSpan.className = 'notification-timestamp';

    let ts = notification.timeStamp;
    let date;

    if (typeof ts === 'string') {
        date = new Date(ts);
    } else if (typeof ts === 'number') {
        date = new Date(ts);
    } else if (ts && ts.year && ts.monthValue) {
        // Handle Java LocalDateTime format from database
        date = new Date(
            ts.year, 
            ts.monthValue - 1, // JavaScript months are 0-indexed
            ts.dayOfMonth,
            ts.hour,
            ts.minute,
            ts.second
        );
    }

    if (date && !isNaN(date.getTime())) {
        const options = {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        };
        timestampSpan.innerText = date.toLocaleTimeString(undefined, options);
    } else {
        timestampSpan.innerText = "Invalid Date/Time";
        console.error("Failed to create valid Date object from timestamp:", ts);
    }

    notificationItem.appendChild(iconDiv);
    notificationItem.appendChild(messageDiv);
    notificationItem.appendChild(timestampSpan);

    notificationList.prepend(notificationItem); // Add new notifications at the top
    
    // Apply current filters
    applyFilters();
    
    // Limit the number of notifications to prevent performance issues
    const maxNotifications = 50;
    const items = notificationList.querySelectorAll('.notification-item');
    if (items.length > maxNotifications) {
        for (let i = maxNotifications; i < items.length; i++) {
            items[i].remove();
        }
    }
}

// Initialize when the page loads
document.addEventListener('DOMContentLoaded', initApp);