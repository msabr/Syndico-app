-- ============================================
-- DATA INITIALIZATION SCRIPT FOR SYNDICO APP
-- 3 Moroccan Residents with Complete Test Data
-- ============================================

-- ============================================
-- 1. BUILDINGS (Immeubles)
-- ============================================
INSERT INTO buildings (name, address, city, postal_code, total_floors, total_apartments, created_at, updated_at) VALUES
('Résidence Al Manzah', 'Avenue Mohammed V, Quartier Agdal', 'Rabat', '10000', 8, 24, NOW(), NOW()),
('Résidence Palmiers', 'Boulevard Zerktouni, Maarif', 'Casablanca', '20100', 10, 30, NOW(), NOW());

-- ============================================
-- 2. APARTMENTS (Appartements)
-- ============================================
INSERT INTO apartments (building_id, apartment_number, floor, bedrooms, bathrooms, surface_area, status, created_at, updated_at) VALUES
-- Résidence Al Manzah (Rabat)
(1, 'A-201', 2, 3, 2, 120.00, 'OCCUPIED', NOW(), NOW()),
(1, 'B-305', 3, 2, 1, 85.00, 'OCCUPIED', NOW(), NOW()),
(1, 'C-401', 4, 4, 3, 150.00, 'OCCUPIED', NOW(), NOW()),
-- Résidence Palmiers (Casablanca)
(2, 'A-102', 1, 2, 1, 75.00, 'AVAILABLE', NOW(), NOW()),
(2, 'B-204', 2, 3, 2, 110.00, 'AVAILABLE', NOW(), NOW());

-- ============================================
-- 3. USERS - 3 MOROCCAN RESIDENTS
-- ============================================
-- Note: Password is "password123" (encrypted by BCrypt)
INSERT INTO users (email, password, first_name, last_name, phone_number, role, is_email_verified, preferred_language, created_at, updated_at) VALUES
-- Resident 1: Mohammed Alami (Rabat)
('mohammed.alami@gmail.com', '$2a$10$AFZ.ZlXHQtyG/l.GFJLhgeYPhZBZin6vTOEyHeFQf./zszvj1XspG', 'Mohammed', 'Alami', '+212661234567', 'RESIDENT', true, 'FR', NOW(), NOW()),
-- Resident 2: Fatima Benali (Rabat)
('fatima.benali@gmail.com', '$2a$10$AFZ.ZlXHQtyG/l.GFJLhgeYPhZBZin6vTOEyHeFQf./zszvj1XspG', 'Fatima', 'Benali', '+212662345678', 'RESIDENT', true, 'FR', NOW(), NOW()),
-- Resident 3: Youssef Chakir (Rabat)
('youssef.chakir@gmail.com', '$2a$10$AFZ.ZlXHQtyG/l.GFJLhgeYPhZBZin6vTOEyHeFQf./zszvj1XspG', 'Youssef', 'Chakir', '+212663456789', 'RESIDENT', true, 'AR', NOW(), NOW());

-- ============================================
-- 4. RESIDENTS (Link users to apartments)
-- ============================================
INSERT INTO residents (user_id, building_id, apartment_number, is_owner, move_in_date, emergency_contact) VALUES
-- Mohammed Alami - Propriétaire à Rabat
((SELECT id FROM users WHERE email = 'mohammed.alami@gmail.com'), 1, 'A-201', true, '2023-01-15', '+212661111111'),
-- Fatima Benali - Locataire à Rabat
((SELECT id FROM users WHERE email = 'fatima.benali@gmail.com'), 1, 'B-305', false, '2023-06-01', '+212662222222'),
-- Youssef Chakir - Propriétaire à Rabat
((SELECT id FROM users WHERE email = 'youssef.chakir@gmail.com'), 1, 'C-401', true, '2022-09-10', '+212663333333');

-- ============================================
-- 5. CHARGES (Frais mensuels)
-- ============================================
INSERT INTO charges (resident_id, description, amount, type, status, due_date, created_at) VALUES
-- Mohammed Alami
((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'mohammed.alami@gmail.com')),
 'Charges mensuelles - Décembre 2025', 2500.00, 'MENSUELLE', 'EN_ATTENTE', '2025-12-31', '2025-12-01 10:00:00'),

((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'mohammed.alami@gmail.com')),
 'Charges mensuelles - Novembre 2025', 2500.00, 'MENSUELLE', 'PAYEE', '2025-11-30', '2025-11-01 10:00:00'),

((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'mohammed.alami@gmail.com')),
 'Travaux ascenseur - Part exceptionnelle', 5000.00, 'EXCEPTIONNELLE', 'PAYEE', '2025-10-15', '2025-09-20 10:00:00'),

-- Fatima Benali
((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'fatima.benali@gmail.com')),
 'Charges mensuelles - Décembre 2025', 1800.00, 'MENSUELLE', 'EN_ATTENTE', '2025-12-31', '2025-12-01 10:00:00'),

((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'fatima.benali@gmail.com')),
 'Charges mensuelles - Novembre 2025', 1800.00, 'MENSUELLE', 'EN_RETARD', '2025-11-30', '2025-11-01 10:00:00'),

((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'fatima.benali@gmail.com')),
 'Charges mensuelles - Octobre 2025', 1800.00, 'MENSUELLE', 'PAYEE', '2025-10-31', '2025-10-01 10:00:00'),

-- Youssef Chakir
((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'youssef.chakir@gmail.com')),
 'Charges mensuelles - Décembre 2025', 3200.00, 'MENSUELLE', 'EN_ATTENTE', '2025-12-31', '2025-12-01 10:00:00'),

((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'youssef.chakir@gmail.com')),
 'Charges mensuelles - Novembre 2025', 3200.00, 'MENSUELLE', 'PAYEE', '2025-11-30', '2025-11-01 10:00:00'),

((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'youssef.chakir@gmail.com')),
 'Réfection peinture cage escalier', 8000.00, 'EXCEPTIONNELLE', 'PAYEE', '2025-09-30', '2025-09-01 10:00:00');

-- ============================================
-- 6. PAYMENTS (Paiements effectués)
-- ============================================
INSERT INTO payments (charge_id, amount, payment_date, payment_method, transaction_id, status) VALUES
-- Mohammed Alami - Novembre payé
((SELECT id FROM charges WHERE resident_id = (SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'mohammed.alami@gmail.com'))
  AND description = 'Charges mensuelles - Novembre 2025'),
 2500.00, '2025-11-05 14:30:00', 'Carte bancaire', 'TXN-2025-001', 'SUCCESS'),

-- Mohammed Alami - Travaux ascenseur payé
((SELECT id FROM charges WHERE resident_id = (SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'mohammed.alami@gmail.com'))
  AND description = 'Travaux ascenseur - Part exceptionnelle'),
 5000.00, '2025-10-20 10:15:00', 'Virement bancaire', 'TXN-2025-002', 'SUCCESS'),

-- Fatima Benali - Octobre payé
((SELECT id FROM charges WHERE resident_id = (SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'fatima.benali@gmail.com'))
  AND description = 'Charges mensuelles - Octobre 2025'),
 1800.00, '2025-10-28 16:45:00', 'Carte bancaire', 'TXN-2025-003', 'SUCCESS'),

-- Youssef Chakir - Novembre payé
((SELECT id FROM charges WHERE resident_id = (SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'youssef.chakir@gmail.com'))
  AND description = 'Charges mensuelles - Novembre 2025'),
 3200.00, '2025-11-03 09:20:00', 'Virement bancaire', 'TXN-2025-004', 'SUCCESS'),

-- Youssef Chakir - Travaux peinture payé
((SELECT id FROM charges WHERE resident_id = (SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'youssef.chakir@gmail.com'))
  AND description = 'Réfection peinture cage escalier'),
 8000.00, '2025-09-25 11:00:00', 'Virement bancaire', 'TXN-2025-005', 'SUCCESS');

-- ============================================
-- 7. RECLAMATIONS (Réclamations)
-- ============================================
INSERT INTO reclamations (resident_id, title, description, category, priority, status, created_at, updated_at) VALUES
-- Mohammed Alami - Réclamation plomberie
((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'mohammed.alami@gmail.com')),
 'Fuite d''eau dans la salle de bain',
 'Depuis 3 jours, il y a une fuite au niveau du lavabo. L''eau coule continuellement et cela augmente la facture. Besoin d''une intervention urgente.',
 'PLOMBERIE', 'URGENTE', 'EN_COURS', '2025-12-15 08:30:00', '2025-12-15 08:30:00'),

((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'mohammed.alami@gmail.com')),
 'Ampoule grillée dans le couloir',
 'L''éclairage du couloir du 2ème étage ne fonctionne plus. C''est dangereux la nuit.',
 'ELECTRICITE', 'MOYENNE', 'NOUVELLE', '2025-12-10 14:20:00', '2025-12-10 14:20:00'),

-- Fatima Benali - Réclamations
((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'fatima.benali@gmail.com')),
 'Ascenseur en panne',
 'L''ascenseur B est en panne depuis ce matin. Je suis au 3ème étage avec un enfant et c''est très difficile de monter les courses.',
 'AUTRE', 'HAUTE', 'ASSIGNEE', '2025-12-12 09:00:00', '2025-12-12 09:00:00'),

((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'fatima.benali@gmail.com')),
 'Nettoyage cage d''escalier insuffisant',
 'La cage d''escalier n''a pas été nettoyée correctement cette semaine. Il y a des déchets qui traînent.',
 'NETTOYAGE', 'BASSE', 'RESOLUE', '2025-11-28 16:30:00', '2025-12-05 10:00:00'),

-- Youssef Chakir - Réclamations
((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'youssef.chakir@gmail.com')),
 'Problème de sécurité - Porte principale',
 'La porte d''entrée principale reste ouverte toute la journée. Le système de fermeture automatique ne fonctionne pas. Risque de cambriolage.',
 'SECURITE', 'URGENTE', 'EN_COURS', '2025-12-14 07:45:00', '2025-12-14 07:45:00'),

((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'youssef.chakir@gmail.com')),
 'Demande d''installation caméra',
 'Je souhaiterais l''installation d''une caméra de surveillance au parking pour plus de sécurité.',
 'SECURITE', 'MOYENNE', 'NOUVELLE', '2025-12-08 13:15:00', '2025-12-08 13:15:00');

-- ============================================
-- 8. PRESTATAIRES (Service Providers)
-- ============================================
INSERT INTO prestataires (company_name, contact_person, email, phone, service_type, address, city, status, rating, created_at, updated_at) VALUES
('Plomberie Rabat Services', 'Ahmed Bensouda', 'contact@plomberie-rabat.ma', '+212537123456', 'PLOMBERIE', 'Rue Oued Fès, Agdal', 'Rabat', 'ACTIVE', 4.5, NOW(), NOW()),
('Électricité Pro Maroc', 'Karim Tazi', 'info@electro-pro.ma', '+212537234567', 'ELECTRICITE', 'Avenue Allal Ben Abdellah', 'Rabat', 'ACTIVE', 4.8, NOW(), NOW()),
('Nettoyage Premium', 'Nadia Amrani', 'contact@nettoyage-premium.ma', '+212537345678', 'NETTOYAGE', 'Hay Riad', 'Rabat', 'ACTIVE', 4.2, NOW(), NOW()),
('Sécurité Atlas', 'Hassan Benjelloun', 'security@atlas-maroc.ma', '+212537456789', 'SECURITE', 'Quartier Hassan', 'Rabat', 'ACTIVE', 4.7, NOW(), NOW());

-- ============================================
-- 9. WORK PROJECTS (Travaux en cours)
-- ============================================
INSERT INTO work_projects (building_id, title, description, start_date, end_date, budget, actual_cost, status, priority, created_at, updated_at) VALUES
-- Résidence Al Manzah
(1, 'Rénovation ascenseurs',
 'Remplacement complet des 2 ascenseurs du bâtiment avec système moderne et économe en énergie. Installation d''un système de monitoring à distance.',
 '2025-11-01', '2026-01-31', 250000.00, 120000.00, 'EN_COURS', 'HAUTE', NOW(), NOW()),

(1, 'Installation système de vidéosurveillance',
 'Installation de 8 caméras HD dans les parties communes : entrée principale, parking, couloirs, et toiture. Enregistrement 24/7 pendant 30 jours.',
 '2025-12-01', '2025-12-31', 80000.00, 45000.00, 'EN_COURS', 'HAUTE', NOW(), NOW()),

(1, 'Réfection façade Est',
 'Ravalement complet de la façade Est avec isolation thermique renforcée et nouvelle peinture.',
 '2026-03-01', '2026-06-30', 450000.00, 0.00, 'PLANIFIE', 'MOYENNE', NOW(), NOW()),

(1, 'Aménagement jardin commun',
 'Création d''un espace vert avec bancs, jeux pour enfants, et système d''arrosage automatique.',
 '2025-09-15', '2025-10-30', 120000.00, 115000.00, 'TERMINE', 'BASSE', NOW(), NOW());

-- ============================================
-- 10. ASSEMBLÉES GÉNÉRALES (General Assemblies)
-- ============================================
INSERT INTO general_assemblies (building_id, title, description, scheduled_date, location, type, status, created_at, updated_at) VALUES
(1, 'Assemblée Générale Ordinaire 2025',
 'Ordre du jour:\n1. Approbation des comptes 2024\n2. Vote du budget 2025\n3. Élection nouveau conseil syndical\n4. Travaux de rénovation ascenseurs\n5. Questions diverses',
 '2025-12-28 18:00:00', 'Salle des fêtes - Rez-de-chaussée', 'ORDINAIRE', 'PLANIFIEE', NOW(), NOW()),

(1, 'AG Extraordinaire - Travaux urgents',
 'Vote pour les travaux urgents de réparation de la toiture suite aux infiltrations d''eau.',
 '2025-10-15 19:00:00', 'Salle des fêtes - Rez-de-chaussée', 'EXTRAORDINAIRE', 'TERMINEE', NOW(), NOW());

-- ============================================
-- 11. VOTES (Pour les assemblées)
-- ============================================
INSERT INTO votes (assembly_id, title, description, start_date, end_date, status, created_at, updated_at) VALUES
-- Vote pour l'AG du 28 décembre
((SELECT id FROM general_assemblies WHERE title = 'Assemblée Générale Ordinaire 2025'),
 'Approbation budget 2026',
 'Approuvez-vous le budget prévisionnel 2026 d''un montant total de 480 000 MAD ?',
 '2025-12-28 18:30:00', '2025-12-28 19:00:00', 'EN_COURS', NOW(), NOW()),

((SELECT id FROM general_assemblies WHERE title = 'Assemblée Générale Ordinaire 2025'),
 'Travaux de rénovation ascenseurs',
 'Approuvez-vous les travaux de rénovation des ascenseurs pour un montant de 250 000 MAD ?',
 '2025-12-28 19:00:00', '2025-12-28 19:30:00', 'EN_COURS', NOW(), NOW()),

-- Vote pour l'AG extraordinaire (terminé)
((SELECT id FROM general_assemblies WHERE title = 'AG Extraordinaire - Travaux urgents'),
 'Réparation toiture urgente',
 'Approuvez-vous les travaux urgents de réparation de la toiture pour 85 000 MAD ?',
 '2025-10-15 19:15:00', '2025-10-15 19:45:00', 'TERMINE', NOW(), NOW());

-- ============================================
-- 12. VOTE RESPONSES (Réponses des résidents)
-- ============================================
INSERT INTO vote_responses (vote_id, resident_id, response, comment, voted_at) VALUES
-- Vote toiture (AG extraordinaire - terminé)
((SELECT id FROM votes WHERE title = 'Réparation toiture urgente'),
 (SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'mohammed.alami@gmail.com')),
 'POUR', 'Travaux nécessaires pour éviter plus de dégâts', '2025-10-15 19:20:00'),

((SELECT id FROM votes WHERE title = 'Réparation toiture urgente'),
 (SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'fatima.benali@gmail.com')),
 'POUR', 'C''est urgent, j''ai des infiltrations dans mon appartement', '2025-10-15 19:22:00'),

((SELECT id FROM votes WHERE title = 'Réparation toiture urgente'),
 (SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'youssef.chakir@gmail.com')),
 'POUR', NULL, '2025-10-15 19:25:00');

-- ============================================
-- 13. RESERVATIONS (Espaces communs)
-- ============================================
INSERT INTO reservations (resident_id, facility_type, facility_name, reservation_date, start_time, end_time, status, purpose, created_at, updated_at) VALUES
-- Mohammed Alami
((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'mohammed.alami@gmail.com')),
 'SALLE_FETE', 'Salle des fêtes', '2025-12-25', '18:00:00', '23:00:00', 'CONFIRMEE',
 'Fête de Noël en famille', NOW(), NOW()),

-- Fatima Benali
((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'fatima.benali@gmail.com')),
 'TERRAIN_SPORT', 'Terrain de basket', '2025-12-20', '16:00:00', '18:00:00', 'CONFIRMEE',
 'Entraînement basketball avec les enfants du quartier', NOW(), NOW()),

-- Youssef Chakir
((SELECT id FROM residents WHERE user_id = (SELECT id FROM users WHERE email = 'youssef.chakir@gmail.com')),
 'PARKING', 'Place parking visiteur n°5', '2025-12-22', '10:00:00', '20:00:00', 'CONFIRMEE',
 'Visite famille pour le weekend', NOW(), NOW());

-- ============================================
-- 14. DOCUMENTS (Documents partagés)
-- ============================================
INSERT INTO documents (building_id, title, description, file_path, document_type, uploaded_by, upload_date, is_public) VALUES
(1, 'Règlement intérieur 2025',
 'Règlement intérieur de la résidence Al Manzah mis à jour en janvier 2025',
 '/uploads/documents/reglement_interieur_2025.pdf', 'REGLEMENT', 1, NOW(), true),

(1, 'Procès-verbal AG Octobre 2025',
 'Compte-rendu de l''assemblée générale extraordinaire du 15 octobre 2025',
 '/uploads/documents/pv_ag_octobre_2025.pdf', 'PV_ASSEMBLEE', 1, NOW(), true),

(1, 'Bilan financier 2024',
 'Rapport financier complet de l''exercice 2024 avec détails des dépenses et recettes',
 '/uploads/documents/bilan_financier_2024.pdf', 'RAPPORT_FINANCIER', 1, NOW(), true),

(1, 'Contrat entretien ascenseurs',
 'Contrat de maintenance annuelle avec Otis Maroc',
 '/uploads/documents/contrat_ascenseurs.pdf', 'CONTRAT', 1, NOW(), false);

-- ============================================
-- 15. MESSAGES/NOTIFICATIONS
-- ============================================
INSERT INTO notifications (user_id, title, message, type, is_read, created_at) VALUES
-- Mohammed Alami
((SELECT id FROM users WHERE email = 'mohammed.alami@gmail.com'),
 'Nouvelle charge disponible',
 'Vos charges de décembre 2025 (2500 MAD) sont disponibles. Échéance: 31/12/2025',
 'CHARGE', false, '2025-12-01 09:00:00'),

((SELECT id FROM users WHERE email = 'mohammed.alami@gmail.com'),
 'Réclamation en cours de traitement',
 'Votre réclamation "Fuite d''eau dans la salle de bain" est en cours de traitement. Un plombier interviendra demain.',
 'RECLAMATION', false, '2025-12-16 10:30:00'),

-- Fatima Benali
((SELECT id FROM users WHERE email = 'fatima.benali@gmail.com'),
 'Rappel: Charges impayées',
 'Vos charges de novembre 2025 (1800 MAD) sont en retard. Merci de régulariser au plus vite.',
 'CHARGE', false, '2025-12-05 08:00:00'),

((SELECT id FROM users WHERE email = 'fatima.benali@gmail.com'),
 'Réclamation résolue',
 'Votre réclamation "Nettoyage cage d''escalier insuffisant" a été résolue. Le nettoyage a été effectué.',
 'RECLAMATION', true, '2025-12-05 15:00:00'),

-- Youssef Chakir
((SELECT id FROM users WHERE email = 'youssef.chakir@gmail.com'),
 'Assemblée Générale - 28 décembre',
 'Rappel: AG ordinaire le 28/12/2025 à 18h. Votre présence est importante pour les votes.',
 'ASSEMBLEE', false, '2025-12-18 09:00:00'),

((SELECT id FROM users WHERE email = 'youssef.chakir@gmail.com'),
 'Réservation confirmée',
 'Votre réservation de la place parking n°5 pour le 22/12/2025 est confirmée.',
 'RESERVATION', true, '2025-12-15 14:20:00');

-- ============================================
-- 16. CHATBOT QA DATA (Questions fréquentes)
-- ============================================
INSERT INTO chatbot_qa (question, answer, category, is_active, created_at) VALUES
('Comment payer mes charges ?', 'Vous pouvez payer vos charges en ligne via votre espace "Mes Charges". Cliquez sur le montant dû et payez par carte bancaire, virement ou mobile banking (Orange Money, Cash Plus).', 'payment', true, NOW()),

('Comment faire une réservation ?', 'Connectez-vous à votre compte, allez dans "Réservations", choisissez l''espace (salle des fêtes, parking, terrain), sélectionnez la date et l''heure, puis confirmez. C''est gratuit pour les résidents !', 'reservation', true, NOW()),

('Contacter le support', 'Contactez-nous:\n- Email: support@syndico.ma\n- Téléphone: +212 537-XXXXXX (Lun-Ven, 9h-18h)\n- Chat en direct sur le site\nNous répondons sous 24h !', 'support', true, NOW()),

('Comment s''inscrire ?', 'Cliquez sur "S''inscrire", remplissez vos informations personnelles et celles de votre appartement, créez un mot de passe. Vous recevrez un email de vérification.', 'general', true, NOW()),

('Mot de passe oublié ?', 'Cliquez sur "Mot de passe oublié" sur la page de connexion. Entrez votre email et nous vous enverrons un lien de réinitialisation.', 'account', true, NOW()),

('Quels moyens de paiement ?', 'Nous acceptons:\n- Cartes bancaires (Visa, Mastercard)\n- Virement bancaire\n- Mobile banking (Orange Money, Cash Plus, Maroc Telecom)\nTous les paiements sont sécurisés SSL.', 'payment', true, NOW()),

('Comment déposer une réclamation ?', 'Allez dans "Mes Réclamations", cliquez "Nouvelle Réclamation". Décrivez le problème, ajoutez des photos si nécessaire, et envoyez. Vous recevrez des mises à jour par email.', 'complaint', true, NOW()),

('Quels espaces puis-je réserver ?', 'Vous pouvez réserver:\n- Salle des fêtes\n- Terrain de sport\n- Places parking visiteurs\n- Salle de réunion\n- Toiture terrasse\nRéservations gratuites pour résidents !', 'reservation', true, NOW()),

('Modifier mon profil ?', 'Connectez-vous, cliquez sur votre nom en haut à droite, puis "Paramètres du profil". Vous pouvez modifier vos infos, mot de passe et préférences de notification.', 'account', true, NOW()),

('Qu''est-ce que Syndico ?', 'Syndico est une plateforme moderne de gestion de copropriété. Elle permet aux résidents de payer leurs charges, faire des réservations, déposer des réclamations et rester connectés avec leur communauté - tout en un seul endroit !', 'general', true, NOW()),

('Horaires du support', 'Notre équipe support est disponible:\n- Lundi au Vendredi: 9h00 - 18h00\n- Samedi: 10h00 - 16h00\n- Dimanche: Fermé\nLa plateforme en ligne est disponible 24/7 !', 'support', true, NOW()),

('Combien ça coûte ?', 'Syndico est GRATUIT pour les résidents ! Votre syndic couvre l''abonnement. Vous pouvez utiliser toutes les fonctionnalités sans frais - paiements, réservations, réclamations, etc.', 'billing', true, NOW()),

('Mes données sont-elles sécurisées ?', 'Absolument ! Nous utilisons un cryptage de niveau bancaire (SSL/TLS), des serveurs sécurisés et respectons les normes internationales de protection des données. Vos informations personnelles et de paiement sont toujours protégées.', 'security', true, NOW()),

('Application mobile disponible ?', 'Syndico est entièrement responsive et fonctionne parfaitement sur mobile. Nous développons aussi des apps iOS et Android natives - bientôt disponibles ! Vous serez notifié du lancement.', 'technical', true, NOW()),

('Consulter mes documents ?', 'Accédez à vos documents dans "Mes Documents". Vous pouvez consulter les PV d''assemblées, rapports financiers, règlements et documents personnels. Téléchargez ou partagez via QR code.', 'document', true, NOW());

-- ============================================
-- FIN DU SCRIPT
-- ============================================
