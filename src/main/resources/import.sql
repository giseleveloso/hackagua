-- Inserir usuários de teste
INSERT INTO usuario (nome, email, senha) VALUES
('João Silva', 'joao@email.com', 'senha123'),
('Maria Santos', 'maria@email.com', 'senha123'),
('Pedro Oliveira', 'pedro@email.com', 'senha123');

-- Inserir medidores de teste
INSERT INTO medidor (nome, localizacao, usuario_id) VALUES
('Medidor Principal', 'Cozinha', 1),
('Medidor Banheiro', 'Banheiro Suite', 1),
('Medidor Jardim', 'Área Externa', 1),
('Medidor Casa', 'Entrada Principal', 2),
('Medidor Apartamento', 'Cozinha', 3);

-- Inserir leituras de teste (últimas 24 horas)
-- Medidor 1 - Consumo normal
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES
(1, 8.5, 100.0, NOW() - INTERVAL '24 hours'),
(1, 12.3, 150.0, NOW() - INTERVAL '20 hours'),
(1, 15.2, 220.0, NOW() - INTERVAL '16 hours'),
(1, 10.8, 280.0, NOW() - INTERVAL '12 hours'),
(1, 9.5, 340.0, NOW() - INTERVAL '8 hours'),
(1, 11.2, 395.0, NOW() - INTERVAL '4 hours'),
(1, 8.9, 445.0, NOW() - INTERVAL '2 hours'),
(1, 7.5, 480.0, NOW());

-- Medidor 2 - Consumo alto (possível vazamento)
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES
(2, 18.5, 200.0, NOW() - INTERVAL '24 hours'),
(2, 22.3, 350.0, NOW() - INTERVAL '20 hours'),
(2, 25.2, 520.0, NOW() - INTERVAL '16 hours'),
(2, 20.8, 680.0, NOW() - INTERVAL '12 hours'),
(2, 19.5, 840.0, NOW() - INTERVAL '8 hours'),
(2, 21.2, 995.0, NOW() - INTERVAL '4 hours'),
(2, 18.9, 1145.0, NOW() - INTERVAL '2 hours'),
(2, 17.5, 1280.0, NOW());

-- Medidor 3 - Consumo baixo (uso eficiente)
INSERT INTO leitura (medidor_id, vazao, consumo_total, data_hora) VALUES
(3, 3.5, 50.0, NOW() - INTERVAL '24 hours'),
(3, 4.3, 80.0, NOW() - INTERVAL '20 hours'),
(3, 5.2, 110.0, NOW() - INTERVAL '16 hours'),
(3, 3.8, 140.0, NOW() - INTERVAL '12 hours'),
(3, 4.5, 170.0, NOW() - INTERVAL '8 hours'),
(3, 3.2, 195.0, NOW() - INTERVAL '4 hours'),
(3, 2.9, 215.0, NOW() - INTERVAL '2 hours'),
(3, 3.5, 235.0, NOW());

