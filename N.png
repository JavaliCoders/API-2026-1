-- ============================================================
--  bd_api — SCRIPT COMPLETO
--  Estrutura + Dados de Exemplo
--  Projeto: JavaliCoders / API-2026-1
-- ============================================================

CREATE DATABASE IF NOT EXISTS bd_api;
USE bd_api;

-- ------------------------------------------------------------
-- ESTRUTURA DAS TABELAS
-- ------------------------------------------------------------

CREATE TABLE tb_perfil (
  id_perfil INT AUTO_INCREMENT KEY NOT NULL,
  perfil VARCHAR(12) NOT NULL
);

INSERT INTO tb_perfil (perfil) VALUES
('DIRETOR'),('FINANCEIRO'),('ESTOQUE'),('OPERACIONAL');
-- IDs: 1=DIRETOR, 2=FINANCEIRO, 3=ESTOQUE, 4=OPERACIONAL

CREATE TABLE tb_usuario (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  nome VARCHAR(150) NOT NULL,
  usuario VARCHAR(30) NOT NULL,
  senha VARCHAR(45) NOT NULL,
  email VARCHAR(255) NOT NULL,
  status ENUM('ATIVO','INATIVO') NOT NULL,
  id_perfil INT NOT NULL,
  FOREIGN KEY (id_perfil) REFERENCES tb_perfil (id_perfil)
);

CREATE TABLE tb_setor (
  id_setor INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  setor VARCHAR(45) NOT NULL
);

INSERT INTO tb_setor (setor) VALUES
('Diretoria'),('Financeiro'),('Compras'),('Operacional');
-- IDs: 1=Diretoria, 2=Financeiro, 3=Compras, 4=Operacional

CREATE TABLE tb_centrocusto (
  id_centrocusto INT AUTO_INCREMENT NOT NULL,
  centro_custo VARCHAR(80) NOT NULL,
  PRIMARY KEY (id_centrocusto)
);

INSERT INTO tb_centrocusto (centro_custo) VALUES
('Manutenção'),('Limpeza'),('Administrativo'),('Informática'),('Infraestrutura');
-- IDs: 1=Manut, 2=Limpeza, 3=Admin, 4=Info, 5=Infra

CREATE TABLE tb_produto (
  id_produto INT AUTO_INCREMENT NOT NULL,
  produto VARCHAR(60) NOT NULL,
  descricao VARCHAR(155) NOT NULL,
  unidade_medida CHAR(2) NOT NULL,
  nivel_minimo INT NOT NULL,
  valor_estimado DECIMAL(8,2) NOT NULL,
  status ENUM('ATIVO','INATIVO') NOT NULL,
  saldo INT NOT NULL DEFAULT 0,
  PRIMARY KEY (id_produto)
);

CREATE TABLE tb_pedido (
  id_pedido INT AUTO_INCREMENT NOT NULL,
  num_pedido VARCHAR(10) UNIQUE NOT NULL,
  data_abertura DATETIME NOT NULL,
  status VARCHAR(25) NOT NULL,
  valor_total_estimado DECIMAL(8,2) NOT NULL,
  data_aprovacao DATETIME NULL,
  parecer MEDIUMTEXT NULL,
  id_solicitante INT NOT NULL,
  id_aprovador INT NULL,
  id_centrocusto INT NOT NULL,
  id_setor INT NOT NULL,
  PRIMARY KEY (id_pedido),
  FOREIGN KEY (id_solicitante) REFERENCES tb_usuario (id_usuario),
  FOREIGN KEY (id_aprovador)   REFERENCES tb_usuario (id_usuario),
  FOREIGN KEY (id_centrocusto) REFERENCES tb_centrocusto (id_centrocusto),
  FOREIGN KEY (id_setor)       REFERENCES tb_setor (id_setor)
);

CREATE TABLE tb_pedido_produto (
  id_pedido_produto INT AUTO_INCREMENT NOT NULL,
  id_pedido INT NOT NULL,
  id_produto INT NOT NULL,
  qtd_solicitada INT NOT NULL,
  qtd_aprovada INT NULL,
  qtd_recebida INT NULL DEFAULT 0,
  PRIMARY KEY (id_pedido_produto),
  FOREIGN KEY (id_pedido)  REFERENCES tb_pedido (id_pedido),
  FOREIGN KEY (id_produto) REFERENCES tb_produto (id_produto)
);

CREATE TABLE tb_fornecedor (
  id_fornecedor INT AUTO_INCREMENT NOT NULL,
  nome VARCHAR(45) NOT NULL,
  cnpj VARCHAR(14) NOT NULL,
  pedido_minimo DECIMAL(8,2) NULL,
  status ENUM('ATIVO','INATIVO') NOT NULL,
  PRIMARY KEY (id_fornecedor)
);

CREATE TABLE tb_forma_pagamento (
  id_forma_pagamento INT AUTO_INCREMENT PRIMARY KEY,
  forma VARCHAR(30) NOT NULL
);

INSERT INTO tb_forma_pagamento (forma) VALUES
('PIX'),('CARTÃO DE DÉBITO'),('CARTÃO DE CRÉDITO'),('TRANSFERÊNCIA'),('BOLETO'),('FATURADO');

CREATE TABLE tb_fornecedor_pagamento (
  id_fornecedor      INT NOT NULL,
  id_forma_pagamento INT NOT NULL,
  PRIMARY KEY (id_fornecedor, id_forma_pagamento),
  FOREIGN KEY (id_fornecedor)      REFERENCES tb_fornecedor (id_fornecedor),
  FOREIGN KEY (id_forma_pagamento) REFERENCES tb_forma_pagamento (id_forma_pagamento)
);

CREATE TABLE tb_anexo (
  id_anexo INT AUTO_INCREMENT NOT NULL,
  tipo ENUM('COTACAO','NOTA_FISCAL') NOT NULL,
  nome_arq VARCHAR(100) NOT NULL,
  caminho_arquivo VARCHAR(255) NOT NULL,
  data_upload DATETIME NOT NULL,
  PRIMARY KEY (id_anexo)
);

CREATE TABLE tb_cotacao (
  id_cotacao INT AUTO_INCREMENT NOT NULL,
  status ENUM('AGUARDANDO_APROVACAO','APROVADO','APROVADO_PARCIALMENTE','NEGADO') NOT NULL,
  data_criacao DATETIME NOT NULL,
  data_aprovacao DATETIME NULL,
  parecer TEXT NULL,
  id_aprovador INT NULL,
  valor_total DECIMAL(8,2) NOT NULL,
  id_pedido INT NOT NULL,
  id_fornecedor INT NOT NULL,
  id_anexo INT NOT NULL,
  PRIMARY KEY (id_cotacao),
  FOREIGN KEY (id_aprovador)  REFERENCES tb_usuario (id_usuario),
  FOREIGN KEY (id_pedido)     REFERENCES tb_pedido (id_pedido),
  FOREIGN KEY (id_fornecedor) REFERENCES tb_fornecedor (id_fornecedor),
  FOREIGN KEY (id_anexo)      REFERENCES tb_anexo (id_anexo)
);

CREATE TABLE tb_compra (
  id_compra INT AUTO_INCREMENT NOT NULL,
  id_pedido INT NOT NULL,
  id_fornecedor INT NOT NULL,
  data DATETIME NOT NULL,
  id_comprador INT NOT NULL,
  valor_total DECIMAL(8,2) NOT NULL,
  data_prevista DATETIME NULL,
  status ENUM('REALIZADA','CANCELADA') NOT NULL,
  PRIMARY KEY (id_compra),
  FOREIGN KEY (id_pedido)     REFERENCES tb_pedido (id_pedido),
  FOREIGN KEY (id_fornecedor) REFERENCES tb_fornecedor (id_fornecedor),
  FOREIGN KEY (id_comprador)  REFERENCES tb_usuario (id_usuario)
);

CREATE TABLE tb_notasfiscal (
  id_nota INT AUTO_INCREMENT NOT NULL,
  numero_nota VARCHAR(10) NOT NULL,
  data_emissao DATETIME NOT NULL,
  data_registro DATETIME NOT NULL,
  id_usuario_registro INT NOT NULL,
  id_compra INT NOT NULL,
  valor_nf DECIMAL(8,2) NOT NULL,
  id_anexo INT NOT NULL,
  status ENUM('REGISTRADA','CONFERIDA','RECUSADA') NOT NULL,
  id_usuario_conferencia INT NULL,
  data_conferencia DATETIME NULL,
  total_itens INT NOT NULL,
  PRIMARY KEY (id_nota),
  FOREIGN KEY (id_usuario_registro)    REFERENCES tb_usuario (id_usuario),
  FOREIGN KEY (id_compra)              REFERENCES tb_compra (id_compra),
  FOREIGN KEY (id_anexo)               REFERENCES tb_anexo (id_anexo),
  FOREIGN KEY (id_usuario_conferencia) REFERENCES tb_usuario (id_usuario)
);

CREATE TABLE tb_compra_item (
  id_compra_item INT AUTO_INCREMENT NOT NULL,
  id_compra INT NOT NULL,
  id_pedido_produto INT NOT NULL,
  valor_uni DECIMAL(8,2) NOT NULL,
  qtd_comprada DECIMAL(8,2) NOT NULL,
  valor_total DECIMAL(8,2) NOT NULL,
  PRIMARY KEY (id_compra_item),
  FOREIGN KEY (id_compra)         REFERENCES tb_compra (id_compra),
  FOREIGN KEY (id_pedido_produto) REFERENCES tb_pedido_produto (id_pedido_produto)
);

CREATE TABLE tb_nf_item (
  id_nf_item INT AUTO_INCREMENT NOT NULL,
  id_nota INT NOT NULL,
  id_pedido_produto INT NOT NULL,
  qtd_recebida INT NOT NULL,
  qtd_rejeitada INT NOT NULL DEFAULT 0,
  motivo_divergencia VARCHAR(255) NULL,
  PRIMARY KEY (id_nf_item),
  FOREIGN KEY (id_nota)           REFERENCES tb_notasfiscal (id_nota),
  FOREIGN KEY (id_pedido_produto) REFERENCES tb_pedido_produto (id_pedido_produto)
);





CREATE TABLE tb_movimentacao (
  id_movimentacao INT AUTO_INCREMENT NOT NULL,
  id_produto INT NOT NULL,
  tipo_movimentação ENUM('ENTRADA','SAÍDA','ENTRADA_MANUAL','SAIDA_MANUAL') NOT NULL,
  quantidade INT NOT NULL,
  id_usuario INT NOT NULL,
  id_pedido INT NULL,
  id_nota INT NULL,
  data DATETIME NOT NULL,
  observacao VARCHAR(255) NULL,
  PRIMARY KEY (id_movimentacao),
  FOREIGN KEY (id_produto) REFERENCES tb_produto (id_produto),
  FOREIGN KEY (id_usuario) REFERENCES tb_usuario (id_usuario),
  FOREIGN KEY (id_pedido)  REFERENCES tb_pedido (id_pedido),
  FOREIGN KEY (id_nota)    REFERENCES tb_notasfiscal (id_nota)
);

CREATE TABLE tb_historico (
  id_historico INT AUTO_INCREMENT NOT NULL,
  entidade_tipo ENUM('Usuário','Pedido','Compra','Cotação','Nota fiscal','Produto','Fornecedor') NOT NULL,
  acao ENUM('Cadastro','Alteração','Cancelamento','Exclusão','Aprovação','Negação','Entrada','Conferência','Saída') NOT NULL,
  entidade_id INT NULL,
  descricao VARCHAR(255) NULL,
  id_usuario INT NOT NULL,
  data DATETIME NOT NULL,
  PRIMARY KEY (id_historico),
  FOREIGN KEY (id_usuario) REFERENCES tb_usuario (id_usuario)
);

CREATE TABLE tb_notificacao (
  id_notificacao INT AUTO_INCREMENT NOT NULL,
  id_usuario INT NOT NULL,
  titulo VARCHAR(150) NOT NULL,
  mensagem TEXT NOT NULL,
  entidade_tipo VARCHAR(50) NULL,
  entidade_id INT NULL,
  lida TINYINT NOT NULL DEFAULT 0,
  data DATETIME NOT NULL,
  PRIMARY KEY (id_notificacao),
  FOREIGN KEY (id_usuario) REFERENCES tb_usuario (id_usuario)
);


-- ============================================================
--  DADOS DE EXEMPLO
-- ============================================================

-- tb_usuario
-- IDs: 1=Daniel, 2=Ana(diretora), 3=Carlos(financeiro),
--      4=Fernanda(estoque), 5=Rafael(op), 6=Juliana(op), 7=Marcos(inativo)
INSERT INTO tb_usuario (nome, usuario, senha, email, status, id_perfil) VALUES
('Daniel',           'daniel',     '1234',     'danielnathan2005@gmail.com', 'ATIVO',   1),
('Ana Paula Mendes', 'ana.mendes', 'senha123', 'ana.mendes@empresa.com',     'ATIVO',   1),
('Carlos Eduardo',   'carlos.edu', 'senha123', 'carlos.edu@empresa.com',     'ATIVO',   2),
('Fernanda Lima',    'fernanda.l', 'senha123', 'fernanda.l@empresa.com',     'ATIVO',   3),
('Rafael Souza',     'rafael.s',   'senha123', 'rafael.s@empresa.com',       'ATIVO',   4),
('Juliana Costa',    'juliana.c',  'senha123', 'juliana.c@empresa.com',      'ATIVO',   4),
('Marcos Oliveira',  'marcos.o',   'senha123', 'marcos.o@empresa.com',       'INATIVO', 2);

-- tb_produto
-- IDs: 1=Papel, 2=Caneta, 3=Toner, 4=Água, 5=Álcool, 6=Cabo, 7=Lâmpada, 8=Vassoura
INSERT INTO tb_produto (produto, descricao, unidade_medida, nivel_minimo, valor_estimado, status, saldo) VALUES
('Papel A4',          'Resma de papel A4 500 folhas 75g',           'RS', 10,  25.90, 'ATIVO',   45),
('Caneta Azul',       'Caneta esferográfica azul ponta média',       'UN', 20,   2.50, 'ATIVO',   80),
('Toner HP 85A',      'Cartucho de toner para impressora HP P1102',  'UN',  3, 120.00, 'ATIVO',    5),
('Água Mineral 20L',  'Galão de água mineral 20 litros',             'UN',  5,  12.00, 'ATIVO',   12),
('Álcool 70% 1L',     'Álcool etílico 70% frasco de 1 litro',        'UN', 10,   8.50, 'ATIVO',   30),
('Cabo de Rede Cat6', 'Cabo de rede categoria 6, rolo 50m',          'RL',  2,  85.00, 'ATIVO',    4),
('Lâmpada LED 9W',    'Lâmpada LED bulbo 9W bivolt',                 'UN', 10,  14.90, 'ATIVO',   22),
('Vassoura',          'Vassoura de nylon cabo longo',                'UN',  5,  18.00, 'INATIVO',  0);

-- tb_fornecedor
-- IDs: 1=Papelaria, 2=TechSupr, 3=AquaPura, 4=CleanMax, 5=Infotech
INSERT INTO tb_fornecedor (nome, cnpj, pedido_minimo, status) VALUES
('Papelaria Central Ltda', '12345678000191', 150.00, 'ATIVO'),
('TechSuprimentos S.A.',   '98765432000155', 300.00, 'ATIVO'),
('AquaPura Distribuidora', '11223344000177',  50.00, 'ATIVO'),
('CleanMax Produtos',      '55667788000133', 200.00, 'ATIVO'),
('Infotech Redes Ltda',    '33445566000199', 400.00, 'INATIVO');

-- tb_fornecedor_pagamento
INSERT INTO tb_fornecedor_pagamento VALUES (1,1),(1,5);
INSERT INTO tb_fornecedor_pagamento VALUES (2,3),(2,4),(2,6);
INSERT INTO tb_fornecedor_pagamento VALUES (3,1),(3,2);
INSERT INTO tb_fornecedor_pagamento VALUES (4,5),(4,4);
INSERT INTO tb_fornecedor_pagamento VALUES (5,6);

-- tb_pedido
INSERT INTO tb_pedido (num_pedido, data_abertura, status, valor_total_estimado, data_aprovacao, parecer, id_solicitante, id_aprovador, id_centrocusto, id_setor) VALUES
('PED-0001','2026-01-10 08:30:00','FINALIZADO',   310.00,'2026-01-11 09:00:00','Aprovado conforme orçamento disponível.',             5, 2, 3, 4),
('PED-0002','2026-02-05 10:15:00','APROVADO',     240.00,'2026-02-06 08:45:00','Itens necessários para manutenção da infraestrutura.', 6, 2, 5, 4),
('PED-0003','2026-03-01 14:00:00','EM_APROVACAO',  95.00, NULL,                 NULL,                                                  5, NULL, 4, 4),
('PED-0004','2026-03-10 09:00:00','NEGADO',       500.00,'2026-03-11 10:30:00','Valor acima do limite permitido sem cotação prévia.',  6, 2, 1, 4),
('PED-0005','2026-04-01 11:00:00','EM_COTACAO',   180.00,'2026-04-02 08:00:00','Aprovado. Aguardar cotação de fornecedores.',          5, 2, 4, 3),
('PED-0006','2026-04-15 16:00:00','CANCELADO',     60.00, NULL,                 NULL,                                                  6, NULL, 2, 4);

-- tb_pedido_produto
INSERT INTO tb_pedido_produto (id_pedido, id_produto, qtd_solicitada, qtd_aprovada, qtd_recebida) VALUES
(1,1, 5, 5, 5),(1,2,20,20,20),(1,3, 2, 2, 2),
(2,6, 2, 2, 0),(2,7,10,10, 0),
(3,5,10,NULL,0),(3,4, 1,NULL,0),
(4,3, 5,NULL,0),(4,6, 3,NULL,0),
(5,1,10,10, 0),(5,2,50,50, 0),
(6,4, 5,NULL,0);

-- tb_anexo
-- IDs: 1=cotacao_papelaria, 2=cotacao_techsupr, 3=cotacao_infotech, 4=nf_00123
INSERT INTO tb_anexo (tipo, nome_arq, caminho_arquivo, data_upload) VALUES
('COTACAO',    'cotacao_papelaria_ped0001.pdf', '/anexos/cotacoes/cotacao_papelaria_ped0001.pdf', '2026-01-12 10:00:00'),
('COTACAO',    'cotacao_techsupr_ped0001.pdf',  '/anexos/cotacoes/cotacao_techsupr_ped0001.pdf',  '2026-01-12 10:30:00'),
('COTACAO',    'cotacao_infotech_ped0005.pdf',  '/anexos/cotacoes/cotacao_infotech_ped0005.pdf',  '2026-04-05 09:00:00'),
('NOTA_FISCAL','nf_00123_ped0001.pdf',          '/anexos/nfs/nf_00123_ped0001.pdf',               '2026-01-20 14:00:00');

-- tb_cotacao (usa id_anexo 1, 2, 3)
INSERT INTO tb_cotacao (status, data_criacao, data_aprovacao, parecer, id_aprovador, valor_total, id_pedido, id_fornecedor, id_anexo) VALUES
('APROVADO',            '2026-01-12 10:00:00','2026-01-13 08:00:00','Melhor preço e prazo de entrega.', 3, 310.00, 1, 1, 1),
('NEGADO',              '2026-01-12 10:30:00','2026-01-13 08:00:00','Preço superior ao concorrente.',   3, 380.00, 1, 2, 2),
('AGUARDANDO_APROVACAO','2026-04-05 09:00:00', NULL,                 NULL,                           NULL, 175.00, 5, 5, 3);

-- tb_compra
INSERT INTO tb_compra (id_pedido, id_fornecedor, data, id_comprador, valor_total, data_prevista, status) VALUES
(1, 1, '2026-01-14 11:00:00', 3, 310.00, '2026-01-18 00:00:00', 'REALIZADA');

-- tb_compra_item
INSERT INTO tb_compra_item (id_compra, id_pedido_produto, valor_uni, qtd_comprada, valor_total) VALUES
(1, 1, 25.90,  5, 129.50),
(1, 2,  2.50, 20,  50.00),
(1, 3, 65.25,  2, 130.50);

-- tb_notasfiscal (usa id_anexo=4)
INSERT INTO tb_notasfiscal (numero_nota, data_emissao, data_registro, id_usuario_registro, id_compra, valor_nf, id_anexo, status, id_usuario_conferencia, data_conferencia, total_itens) VALUES
('NF-00123','2026-01-18 08:00:00','2026-01-18 14:30:00', 4, 1, 310.00, 4, 'CONFERIDA', 4, '2026-01-19 09:00:00', 3);

-- tb_nf_item
INSERT INTO tb_nf_item (id_nota, id_pedido_produto, qtd_recebida, qtd_rejeitada, motivo_divergencia) VALUES
(1, 1, 5, 0, NULL),(1, 2, 20, 0, NULL),(1, 3, 2, 0, NULL);

-- tb_movimentacao
INSERT INTO tb_movimentacao (id_produto, tipo_movimentação, quantidade, id_usuario, id_pedido, id_nota, data, observacao) VALUES
(1, 'ENTRADA',        5, 4, 1,    1,    '2026-01-19 09:10:00', 'Recebimento NF-00123'),
(2, 'ENTRADA',       20, 4, 1,    1,    '2026-01-19 09:10:00', 'Recebimento NF-00123'),
(3, 'ENTRADA',        2, 4, 1,    1,    '2026-01-19 09:10:00', 'Recebimento NF-00123'),
(2, 'SAIDA_MANUAL',   5, 4, NULL, NULL, '2026-02-01 10:00:00', 'Distribuição para setor Operacional'),
(7, 'ENTRADA_MANUAL',10, 4, NULL, NULL, '2026-02-15 16:00:00', 'Ajuste de inventário — contagem física');

-- tb_historico
INSERT INTO tb_historico (entidade_tipo, acao, id_usuario, data) VALUES
('Pedido',      'Cadastro',    5, '2026-01-10 08:30:00'),
('Pedido',      'Aprovação',   2, '2026-01-11 09:00:00'),
('Cotação',     'Cadastro',    3, '2026-01-12 10:00:00'),
('Cotação',     'Aprovação',   3, '2026-01-13 08:00:00'),
('Compra',      'Cadastro',    3, '2026-01-14 11:00:00'),
('Nota fiscal', 'Entrada',     4, '2026-01-18 14:30:00'),
('Nota fiscal', 'Conferência', 4, '2026-01-19 09:00:00'),
('Pedido',      'Cadastro',    6, '2026-02-05 10:15:00'),
('Pedido',      'Aprovação',   2, '2026-02-06 08:45:00'),
('Usuário',     'Cadastro',    2, '2026-01-05 08:00:00'),
('Fornecedor',  'Cadastro',    3, '2026-01-06 09:00:00'),
('Produto',     'Alteração',   4, '2026-02-15 16:00:00');

-- tb_notificacao
INSERT INTO tb_notificacao (id_usuario, titulo, mensagem, entidade_tipo, entidade_id, lida, data) VALUES
(2, 'Novo pedido para aprovação',   'O pedido PED-0003 foi aberto por Rafael Souza e aguarda sua aprovação.',  'Pedido',      3, 0, '2026-03-01 14:05:00'),
(3, 'Cotação aguardando aprovação', 'Uma cotação do PED-0005 foi registrada e aguarda revisão.',               'Cotação',     3, 0, '2026-04-05 09:05:00'),
(4, 'Nota fiscal conferida',        'A NF-00123 referente ao PED-0001 foi conferida com sucesso.',             'Nota fiscal', 1, 1, '2026-01-19 09:10:00'),
(5, 'Pedido PED-0004 negado',       'Seu pedido PED-0004 foi negado. Valor acima do limite permitido.',        'Pedido',      4, 0, '2026-03-11 10:35:00'),
(6, 'Pedido PED-0002 aprovado',     'Seu pedido PED-0002 foi aprovado e encaminhado para compra.',             'Pedido',      2, 1, '2026-02-06 08:50:00');


-- ============================================================
--  CONSULTAS DE VERIFICAÇÃO
-- ============================================================

SELECT p.num_pedido, p.status, u_sol.nome AS solicitante, u_apr.nome AS aprovador,
       cc.centro_custo, s.setor, p.valor_total_estimado
FROM tb_pedido p
JOIN tb_usuario u_sol ON p.id_solicitante = u_sol.id_usuario
LEFT JOIN tb_usuario u_apr ON p.id_aprovador = u_apr.id_usuario
JOIN tb_centrocusto cc ON p.id_centrocusto = cc.id_centrocusto
JOIN tb_setor s ON p.id_setor = s.id_setor
ORDER BY p.data_abertura;

SELECT p.num_pedido, pr.produto, pp.qtd_solicitada, pp.qtd_aprovada, pp.qtd_recebida
FROM tb_pedido_produto pp
JOIN tb_pedido p   ON pp.id_pedido  = p.id_pedido
JOIN tb_produto pr ON pp.id_produto = pr.id_produto
ORDER BY p.num_pedido;

SELECT produto, unidade_medida, saldo, nivel_minimo,
  CASE WHEN saldo <= nivel_minimo THEN '⚠ ABAIXO DO MÍNIMO' ELSE 'OK' END AS situacao
FROM tb_produto WHERE status = 'ATIVO'
ORDER BY situacao DESC, produto;

SELECT u.nome, n.titulo, n.data
FROM tb_notificacao n
JOIN tb_usuario u ON n.id_usuario = u.id_usuario
WHERE n.lida = 0
ORDER BY n.data DESC;
