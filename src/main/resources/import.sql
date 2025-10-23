-- Limpar dados existentes (opcional, mas recomendado)
-- DELETE FROM leitura;
-- DELETE FROM sugestao;
-- DELETE FROM medidor;
-- DELETE FROM usuario;

-- Inserir Usuários
INSERT INTO usuario (id, nome, email, senha, valorm) VALUES 
(1, 'João Silva', 'joao.silva@email.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 5.50),
(2, 'Maria Santos', 'maria.santos@email.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 5.50),
(3, 'Pedro Oliveira', 'pedro.oliveira@email.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 6.00),
(4, 'Ana Costa', 'ana.costa@email.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 5.80),
(5, 'Carlos Souza', 'carlos.souza@email.com', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 5.50);

-- Inserir Medidores 
INSERT INTO medidor (id, nome, localizacao, limite, interromper, ligado, usuario_id) VALUES 
(1, 'Medidor Principal', 'Cozinha', 15.0, true, true, 1),
(2, 'Medidor Banheiro', 'Banheiro Suite', 10.0, true, true, 1),
(3, 'Medidor Jardim', 'Área Externa', 20.0, true, true, 2),
(4, 'Medidor Lavanderia', 'Lavanderia', 12.0, false, false, 2),
(5, 'Medidor Cozinha', 'Cozinha', 15.0, false, true, 3),
(6, 'Medidor Piscina', 'Área de Lazer', 50.0, false, true, 4),
(7, 'Medidor Geral', 'Entrada Principal', 25.0, true, true, 5);

-- Inserir Leituras
INSERT INTO leitura (id, medidor_id, litros, litros_acumulado, data_hora) VALUES 
-- Medidor 1 (João - Cozinha)
(1, 1, 2.500, 2.50, '2025-10-01 08:30:00'),
(2, 1, 3.200, 5.70, '2025-10-01 12:45:00'),
(3, 1, 1.800, 7.50, '2025-10-01 18:20:00'),
(4, 1, 2.100, 9.60, '2025-10-02 09:15:00'),
(5, 1, 4.500, 14.10, '2025-10-02 19:30:00'),

-- Medidor 2 (João - Banheiro)
(6, 2, 5.300, 5.30, '2025-10-01 07:00:00'),
(7, 2, 4.800, 10.10, '2025-10-01 20:00:00'),
(8, 2, 6.200, 16.30, '2025-10-02 07:30:00'),

-- Medidor 3 (Maria - Jardim)
(9, 3, 15.500, 15.50, '2025-10-01 06:00:00'),
(10, 3, 18.200, 33.70, '2025-10-01 18:00:00'),
(11, 3, 12.800, 46.50, '2025-10-02 06:30:00'),

-- Medidor 4 (Maria - Lavanderia - desligado)
(12, 4, 8.500, 8.50, '2025-09-30 14:00:00'),

-- Medidor 5 (Pedro - Cozinha)
(13, 5, 3.100, 3.10, '2025-10-01 08:00:00'),
(14, 5, 2.900, 6.00, '2025-10-01 13:00:00'),
(15, 5, 3.500, 9.50, '2025-10-01 19:00:00'),
(16, 5, 2.800, 12.30, '2025-10-02 08:30:00'),

-- Medidor 6 (Ana - Piscina)
(17, 6, 45.000, 45.00, '2025-10-01 10:00:00'),
(18, 6, 32.500, 77.50, '2025-10-02 10:00:00'),

-- Medidor 7 (Carlos - Geral)
(19, 7, 18.500, 18.50, '2025-10-01 00:00:00'),
(20, 7, 22.300, 40.80, '2025-10-02 00:00:00');
