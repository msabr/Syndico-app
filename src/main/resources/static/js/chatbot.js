function toggleChatbot() {
    const widget = document.getElementById('chatbot-widget');
    const badge = document.querySelector('.chatbot-badge');
    widget.classList.toggle('open');

    if (widget.classList.contains('open')) {
        badge.style.display = 'none';
        document.getElementById('chatbot-input-field').focus();
    }
}

function sendMessage(event) {
    event.preventDefault();
    const input = document.getElementById('chatbot-input-field');
    const message = input.value.trim();

    if (message) {
        addUserMessage(message);
        input.value = '';

        // Show typing indicator
        document.getElementById('typing-indicator').style.display = 'flex';

        // Send to backend
        fetch('/api/chatbot/ask', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ question: message })
        })
            .then(response => response.json())
            .then(data => {
                document.getElementById('typing-indicator').style.display = 'none';
                addBotMessage(data.answer || "I'm sorry, I couldn't find an answer to that question. Please contact our support team.");
            })
            .catch(error => {
                document.getElementById('typing-indicator').style.display = 'none';
                addBotMessage("Sorry, I'm having trouble connecting. Please try again later.");
            });
    }
}

function sendQuickMessage(message) {
    document.querySelector('.quick-suggestions').style.display = 'none';
    addUserMessage(message);

    document.getElementById('typing-indicator').style.display = 'flex';

    fetch('/api/chatbot/ask', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ question: message })
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById('typing-indicator').style.display = 'none';
            addBotMessage(data.answer || "Let me connect you with our support team for that.");
        })
        .catch(() => {
            document.getElementById('typing-indicator').style.display = 'none';
            addBotMessage("I'm having trouble right now. Please try again!");
        });
}

function addUserMessage(text) {
    const messagesDiv = document.getElementById('chatbot-messages');
    const messageHTML = `
            <div class="message user-message">
                <div class="message-avatar">U</div>
                <div class="message-content">
                    <p>${text}</p>
                    <span class="message-time">${new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
                </div>
            </div>
        `;
    messagesDiv.insertAdjacentHTML('beforeend', messageHTML);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function addBotMessage(text) {
    const messagesDiv = document.getElementById('chatbot-messages');
    const messageHTML = `
            <div class="message bot-message">
                <div class="message-avatar">
                    <img src="/images/syndico_logo.png" alt="Bot">
                </div>
                <div class="message-content">
                    <p>${text}</p>
                    <span class="message-time">${new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
                </div>
            </div>
        `;
    messagesDiv.insertAdjacentHTML('beforeend', messageHTML);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}
