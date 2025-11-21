-- Chatbot QA Data
INSERT INTO chatbot_qa (question, answer, category, is_active, created_at) VALUES
                                                                               ('How do I pay my fees?', 'You can pay your fees online through your dashboard. Go to "Payments" section, select the amount due, and pay securely via credit card, bank transfer, or mobile payment.', 'payment', true, NOW()),

                                                                               ('How to make a reservation?', 'To make a reservation, log in to your account, go to "Reservations" in the menu, select the facility (gym, pool, party room), choose your date and time, and confirm. You''ll receive an instant confirmation.', 'reservation', true, NOW()),

                                                                               ('Contact support', 'You can contact our support team via:\n- Email: support@syndico.ma\n- Phone: +212 5XX-XXXXXX (Mon-Fri, 9AM-6PM)\n- Live chat on our website\nWe typically respond within 24 hours!', 'support', true, NOW()),

                                                                               ('How do I register?', 'Click on "Register" in the top menu, fill in your personal information, apartment details, and create a password. You''ll receive a verification email. Once verified, you can access all features!', 'general', true, NOW()),

                                                                               ('Forgot password', 'Click "Forgot Password" on the login page. Enter your email address and we''ll send you a reset link. You can also reset via SMS if you registered your phone number.', 'account', true, NOW()),

                                                                               ('What are the payment methods?', 'We accept:\n- Credit/Debit cards (Visa, Mastercard)\n- Bank transfer\n- Mobile payment (Orange Money, Maroc Telecom)\n- PayPal\nAll payments are secured with SSL encryption.', 'payment', true, NOW()),

                                                                               ('How to submit a complaint?', 'Go to your dashboard, click "Complaints", then "New Complaint". Describe your issue, add photos if needed, and submit. You can track the status in real-time and receive updates via email.', 'complaint', true, NOW()),

                                                                               ('What facilities can I reserve?', 'You can reserve:\n- Swimming pool\n- Gym\n- Party room\n- Meeting room\n- Parking spaces\n- Rooftop terrace\nReservations are free for residents!', 'reservation', true, NOW()),

                                                                               ('How do I change my profile?', 'Log in, click on your name in the top right, select "Profile Settings". You can update your personal info, contact details, password, and notification preferences.', 'account', true, NOW()),

                                                                               ('What is Syndico?', 'Syndico is a modern property management platform that simplifies condominium living. We help residents pay fees, make reservations, submit complaints, and stay connected with their community - all in one place!', 'general', true, NOW()),

                                                                               ('Operating hours', 'Our support team is available:\n- Monday to Friday: 9:00 AM - 6:00 PM\n- Saturday: 10:00 AM - 4:00 PM\n- Sunday: Closed\nOur online platform is available 24/7!', 'support', true, NOW()),

                                                                               ('How much does it cost?', 'Syndico is FREE for residents! Your property management company covers the subscription. You can use all features at no additional cost - payments, reservations, complaints, and more.', 'billing', true, NOW()),

                                                                               ('Is my data secure?', 'Absolutely! We use bank-level encryption (SSL/TLS), secure servers, and comply with international data protection standards. Your personal and payment information is always protected.', 'security', true, NOW()),

                                                                               ('Can I use mobile app?', 'Yes! Syndico is fully responsive and works perfectly on mobile browsers. We''re also developing native iOS and Android apps - coming soon! You''ll get notifications about the launch.', 'technical', true, NOW()),

                                                                               ('How to view documents?', 'Access your documents in the "Documents" section of your dashboard. You can view meeting minutes, financial reports, regulations, and personal documents. Download or share via QR code.', 'document', true, NOW());