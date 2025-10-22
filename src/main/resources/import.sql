

-- Inserir usuários (senha hash SHA-256 de "senha123")
INSERT INTO usuario (nome, email, senha, tipo_uso) VALUES ('João Silva', 'joao@email.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 1);
INSERT INTO usuario (nome, email, senha, tipo_uso) VALUES ('Maria Santos', 'maria@email.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 2);
INSERT INTO usuario (nome, email, senha, tipo_uso) VALUES ('Pedro Oliveira', 'pedro@email.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 4);


-- Inserir medidores
INSERT INTO medidor (nome, localizacao, usuario_id) VALUES ('Medidor Chuveiro', 'Banheiro', 1);
INSERT INTO medidor (nome, localizacao, usuario_id) VALUES ('Medidor Cozinha', 'Banheiro', 1);
INSERT INTO medidor (nome, localizacao, usuario_id) VALUES ('Medidor Jardim', 'Banheiro', 1);
INSERT INTO medidor (nome, localizacao, usuario_id) VALUES ('Medidor Banheiro Empresa', 'Banheiro', 2);
INSERT INTO medidor (nome, localizacao, usuario_id) VALUES ('Medidor Bebedouro', 'Banheiro', 2);
INSERT INTO medidor (nome, localizacao, usuario_id) VALUES ('Medidor Irrigação', 'Banheiro', 3);

-- Inserir leituras - Medidor 1 (Chuveiro)
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (1, 12.5, 100.0, CURRENT_TIMESTAMP - INTERVAL '24' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (1, 15.3, 180.0, CURRENT_TIMESTAMP - INTERVAL '20' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (1, 14.2, 250.0, CURRENT_TIMESTAMP - INTERVAL '16' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (1, 13.8, 320.0, CURRENT_TIMESTAMP - INTERVAL '12' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (1, 12.5, 385.0, CURRENT_TIMESTAMP - INTERVAL '8' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (1, 11.2, 445.0, CURRENT_TIMESTAMP - INTERVAL '4' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (1, 10.9, 495.0, CURRENT_TIMESTAMP - INTERVAL '2' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (1, 9.5, 535.0, CURRENT_TIMESTAMP);

-- Inserir leituras - Medidor 2 (Torneira)
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (2, 5.5, 50.0, CURRENT_TIMESTAMP - INTERVAL '24' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (2, 6.3, 85.0, CURRENT_TIMESTAMP - INTERVAL '20' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (2, 7.2, 125.0, CURRENT_TIMESTAMP - INTERVAL '16' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (2, 5.8, 160.0, CURRENT_TIMESTAMP - INTERVAL '12' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (2, 6.5, 195.0, CURRENT_TIMESTAMP - INTERVAL '8' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (2, 5.2, 225.0, CURRENT_TIMESTAMP - INTERVAL '4' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (2, 4.9, 250.0, CURRENT_TIMESTAMP - INTERVAL '2' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (2, 5.5, 278.0, CURRENT_TIMESTAMP);

-- Inserir leituras - Medidor 3 (Mangueira)
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (3, 25.5, 200.0, CURRENT_TIMESTAMP - INTERVAL '24' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (3, 28.3, 450.0, CURRENT_TIMESTAMP - INTERVAL '20' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (3, 30.2, 720.0, CURRENT_TIMESTAMP - INTERVAL '16' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (3, 27.8, 980.0, CURRENT_TIMESTAMP - INTERVAL '12' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (3, 26.5, 1235.0, CURRENT_TIMESTAMP - INTERVAL '8' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (3, 25.2, 1475.0, CURRENT_TIMESTAMP - INTERVAL '4' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (3, 24.9, 1700.0, CURRENT_TIMESTAMP - INTERVAL '2' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (3, 25.5, 1930.0, CURRENT_TIMESTAMP);

-- Inserir leituras - Medidor 4 (Banheiro Empresa)
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (4, 18.5, 300.0, CURRENT_TIMESTAMP - INTERVAL '24' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (4, 22.3, 580.0, CURRENT_TIMESTAMP - INTERVAL '20' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (4, 20.2, 850.0, CURRENT_TIMESTAMP - INTERVAL '16' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (4, 19.8, 1100.0, CURRENT_TIMESTAMP - INTERVAL '12' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (4, 21.5, 1370.0, CURRENT_TIMESTAMP - INTERVAL '8' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (4, 20.2, 1625.0, CURRENT_TIMESTAMP - INTERVAL '4' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (4, 18.9, 1860.0, CURRENT_TIMESTAMP - INTERVAL '2' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (4, 17.5, 2075.0, CURRENT_TIMESTAMP);

-- Inserir leituras - Medidor 5 (Bebedouro)
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (5, 3.5, 30.0, CURRENT_TIMESTAMP - INTERVAL '24' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (5, 4.3, 55.0, CURRENT_TIMESTAMP - INTERVAL '20' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (5, 5.2, 82.0, CURRENT_TIMESTAMP - INTERVAL '16' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (5, 3.8, 105.0, CURRENT_TIMESTAMP - INTERVAL '12' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (5, 4.5, 130.0, CURRENT_TIMESTAMP - INTERVAL '8' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (5, 3.2, 150.0, CURRENT_TIMESTAMP - INTERVAL '4' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (5, 2.9, 167.0, CURRENT_TIMESTAMP - INTERVAL '2' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (5, 3.5, 185.0, CURRENT_TIMESTAMP);

-- Inserir leituras - Medidor 6 (Irrigação)
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (6, 45.5, 1000.0, CURRENT_TIMESTAMP - INTERVAL '24' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (6, 48.3, 2200.0, CURRENT_TIMESTAMP - INTERVAL '20' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (6, 50.2, 3500.0, CURRENT_TIMESTAMP - INTERVAL '16' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (6, 47.8, 4750.0, CURRENT_TIMESTAMP - INTERVAL '12' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (6, 46.5, 5950.0, CURRENT_TIMESTAMP - INTERVAL '8' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (6, 45.2, 7100.0, CURRENT_TIMESTAMP - INTERVAL '4' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (6, 44.9, 8200.0, CURRENT_TIMESTAMP - INTERVAL '2' HOUR);
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES (6, 45.5, 9325.0, CURRENT_TIMESTAMP);

-- Inserir sugestões
INSERT INTO sugestao (usuario_id, mensagem, data_hora) VALUES (1, '🚿 Dica: Reduza o tempo no chuveiro em 2 minutos e economize até 40 litros por dia!', CURRENT_TIMESTAMP - INTERVAL '2' DAY);
INSERT INTO sugestao (usuario_id, mensagem, data_hora) VALUES (1, '💧 Mangueira com alto consumo. Use balde para lavar o carro.', CURRENT_TIMESTAMP - INTERVAL '1' DAY);
INSERT INTO sugestao (usuario_id, mensagem, data_hora) VALUES (2, '🏢 Consumo empresarial elevado. Considere instalar torneiras com sensor automático.', CURRENT_TIMESTAMP - INTERVAL '3' DAY);
INSERT INTO sugestao (usuario_id, mensagem, data_hora) VALUES (3, '🌱 Irrigação: Implemente sistema de gotejamento para economizar 50%.', CURRENT_TIMESTAMP);