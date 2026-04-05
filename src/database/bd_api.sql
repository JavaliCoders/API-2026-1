CREATE DATABASE IF NOT EXISTS bd_api;
USE bd_api;

CREATE TABLE IF NOT EXISTS tb_perfil (
  id_perfil INT AUTO_INCREMENT PRIMARY KEY,
  perfil VARCHAR(20) NOT NULL UNIQUE
);

INSERT IGNORE INTO tb_perfil (id_perfil, perfil) VALUES
  (1, 'DIRETOR'),
  (2, 'FINANCEIRO'),
  (3, 'ESTOQUE'),
  (4, 'OPERACIONAL');

CREATE TABLE IF NOT EXISTS tb_usuario (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(150) NOT NULL,
  usuario VARCHAR(30) NOT NULL UNIQUE,
  senha VARCHAR(45) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  status ENUM('ATIVO', 'INATIVO') NOT NULL DEFAULT 'ATIVO',
  id_perfil INT NOT NULL,
  FOREIGN KEY (id_perfil) REFERENCES tb_perfil (id_perfil)
);

INSERT IGNORE INTO tb_usuario (nome, usuario, senha, email, status, id_perfil) VALUES
  ('Diretor Geral', 'diretor', '123', 'diretor@email.com', 'ATIVO', 1),
  ('Usuario Financeiro', 'financeiro', '123', 'financeiro@email.com', 'ATIVO', 2),
  ('Usuario Estoque', 'estoque', '123', 'estoque@email.com', 'ATIVO', 3),
  ('Usuario Operacional', 'operacional', '123', 'operacional@email.com', 'ATIVO', 4);

CREATE TABLE IF NOT EXISTS tb_setor (
  id_setor INT AUTO_INCREMENT PRIMARY KEY,
  setor VARCHAR(45) NOT NULL UNIQUE
);

INSERT IGNORE INTO tb_setor (setor) VALUES
  ('Diretoria'),
  ('Financeiro'),
  ('Compras'),
  ('Operacional');

CREATE TABLE IF NOT EXISTS tb_centrocusto (
  id_centrocusto INT AUTO_INCREMENT PRIMARY KEY,
  centro_custo VARCHAR(80) NOT NULL UNIQUE
);

INSERT IGNORE INTO tb_centrocusto (centro_custo) VALUES
  ('Manutencao'),
  ('Limpeza'),
  ('Administrativo'),
  ('Informatica'),
  ('Infraestrutura');

CREATE TABLE IF NOT EXISTS tb_produto (
  id_produto INT AUTO_INCREMENT PRIMARY KEY,
  produto VARCHAR(60) NOT NULL,
  descricao VARCHAR(155) NOT NULL,
  unidade_medida CHAR(2) NOT NULL,
  nivel_minimo INT NOT NULL,
  valor_estimado DECIMAL(8, 2) NOT NULL,
  status ENUM('ATIVO', 'INATIVO') NOT NULL,
  saldo INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS tb_pedido (
  id_pedido INT AUTO_INCREMENT PRIMARY KEY,
  num_pedido VARCHAR(10) NOT NULL UNIQUE,
  data_abertura DATETIME NOT NULL,
  status ENUM('EM_APROVACAO', 'APROVADO', 'NEGADO', 'EM_COTACAO', 'EM_COMPRA', 'FINALIZADO', 'CANCELADO') NOT NULL DEFAULT 'EM_APROVACAO',
  valor_total_estimado DECIMAL(8, 2) NOT NULL,
  data_aprovacao DATETIME NULL,
  parecer MEDIUMTEXT NULL,
  id_solicitante INT NOT NULL,
  id_aprovador INT NULL,
  id_centrocusto INT NOT NULL,
  id_setor INT NOT NULL,
  FOREIGN KEY (id_solicitante) REFERENCES tb_usuario (id_usuario),
  FOREIGN KEY (id_aprovador) REFERENCES tb_usuario (id_usuario),
  FOREIGN KEY (id_centrocusto) REFERENCES tb_centrocusto (id_centrocusto),
  FOREIGN KEY (id_setor) REFERENCES tb_setor (id_setor)
);

CREATE TABLE IF NOT EXISTS tb_pedido_produto (
  id_pedido_produto INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido INT NOT NULL,
  id_produto INT NOT NULL,
  qtd_solicitada INT NOT NULL,
  qtd_aprovada INT NULL,
  qtd_recebida INT NULL DEFAULT 0,
  FOREIGN KEY (id_pedido) REFERENCES tb_pedido (id_pedido),
  FOREIGN KEY (id_produto) REFERENCES tb_produto (id_produto)
);

CREATE TABLE IF NOT EXISTS tb_fornecedor (
  id_fornecedor INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(45) NOT NULL,
  cnpj VARCHAR(14) NOT NULL UNIQUE,
  tipo_pagamento ENUM('PIX', 'CARTAO', 'TRANSFERENCIA', 'BOLETO', 'FATURADO') NOT NULL,
  pedido_minimo DECIMAL(8, 2) NULL,
  status ENUM('ATIVO', 'INATIVO') NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_fornecedor_produto (
  id_produto_fornecedor INT AUTO_INCREMENT PRIMARY KEY,
  id_fornecedor INT NOT NULL,
  id_produto INT NOT NULL,
  preco_uni DECIMAL(8, 2) NOT NULL,
  FOREIGN KEY (id_fornecedor) REFERENCES tb_fornecedor (id_fornecedor),
  FOREIGN KEY (id_produto) REFERENCES tb_produto (id_produto)
);

CREATE TABLE IF NOT EXISTS tb_anexo (
  id_anexo INT AUTO_INCREMENT PRIMARY KEY,
  tipo ENUM('COTACAO', 'NOTA_FISCAL') NOT NULL,
  nome_arq VARCHAR(100) NOT NULL,
  caminho_arquivo VARCHAR(255) NOT NULL,
  data_upload DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_cotacao (
  id_cotacao INT AUTO_INCREMENT PRIMARY KEY,
  status ENUM('AGUARDANDO_APROVACAO', 'APROVADO', 'APROVADO_PARCIALMENTE', 'NEGADO') NOT NULL,
  data_criacao DATETIME NOT NULL,
  data_aprovacao DATETIME NULL,
  parecer TEXT NULL,
  id_aprovador INT NULL,
  valor_total DECIMAL(8, 2) NOT NULL,
  id_pedido INT NOT NULL,
  id_fornecedor INT NOT NULL,
  id_anexo INT NOT NULL,
  FOREIGN KEY (id_aprovador) REFERENCES tb_usuario (id_usuario),
  FOREIGN KEY (id_pedido) REFERENCES tb_pedido (id_pedido),
  FOREIGN KEY (id_fornecedor) REFERENCES tb_fornecedor (id_fornecedor),
  FOREIGN KEY (id_anexo) REFERENCES tb_anexo (id_anexo)
);

CREATE TABLE IF NOT EXISTS tb_compra (
  id_compra INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido INT NOT NULL,
  id_fornecedor INT NOT NULL,
  data DATETIME NOT NULL,
  id_comprador INT NOT NULL,
  valor_total DECIMAL(8, 2) NOT NULL,
  data_prevista DATETIME NULL,
  status ENUM('REALIZADA', 'CANCELADA') NOT NULL,
  FOREIGN KEY (id_pedido) REFERENCES tb_pedido (id_pedido),
  FOREIGN KEY (id_fornecedor) REFERENCES tb_fornecedor (id_fornecedor),
  FOREIGN KEY (id_comprador) REFERENCES tb_usuario (id_usuario)
);

CREATE TABLE IF NOT EXISTS tb_notasfiscal (
  id_nota INT AUTO_INCREMENT PRIMARY KEY,
  numero_nota VARCHAR(10) NOT NULL,
  data_emissao DATETIME NOT NULL,
  data_registro DATETIME NOT NULL,
  id_usuario_registro INT NOT NULL,
  id_compra INT NOT NULL,
  valor_nf DECIMAL(8, 2) NOT NULL,
  id_anexo INT NOT NULL,
  status ENUM('REGISTRADA', 'CONFERIDA', 'RECUSADA') NOT NULL,
  id_usuario_conferencia INT NULL,
  data_conferencia DATETIME NULL,
  total_itens INT NOT NULL,
  FOREIGN KEY (id_usuario_registro) REFERENCES tb_usuario (id_usuario),
  FOREIGN KEY (id_compra) REFERENCES tb_compra (id_compra),
  FOREIGN KEY (id_anexo) REFERENCES tb_anexo (id_anexo),
  FOREIGN KEY (id_usuario_conferencia) REFERENCES tb_usuario (id_usuario)
);

CREATE TABLE IF NOT EXISTS tb_compra_item (
  id_compra_item INT AUTO_INCREMENT PRIMARY KEY,
  id_compra INT NOT NULL,
  id_pedido_produto INT NOT NULL,
  valor_uni DECIMAL(8, 2) NOT NULL,
  qtd_comprada DECIMAL(8, 2) NOT NULL,
  valor_total DECIMAL(8, 2) NOT NULL,
  FOREIGN KEY (id_compra) REFERENCES tb_compra (id_compra),
  FOREIGN KEY (id_pedido_produto) REFERENCES tb_pedido_produto (id_pedido_produto)
);

CREATE TABLE IF NOT EXISTS tb_nf_item (
  id_nf_item INT AUTO_INCREMENT PRIMARY KEY,
  id_pedido_produto INT NOT NULL,
  qtd_recebida INT NOT NULL,
  qtd_rejeitada INT NOT NULL DEFAULT 0,
  motivo_divergencia VARCHAR(255) NULL,
  FOREIGN KEY (id_pedido_produto) REFERENCES tb_pedido_produto (id_pedido_produto)
);

CREATE TABLE IF NOT EXISTS tb_movimentacao (
  id_movimentacao INT AUTO_INCREMENT PRIMARY KEY,
  id_produto INT NOT NULL,
  tipo_movimentacao ENUM('ENTRADA', 'SAIDA', 'ENTRADA_MANUAL', 'SAIDA_MANUAL') NOT NULL,
  quantidade INT NOT NULL,
  id_usuario INT NOT NULL,
  id_pedido INT NULL,
  id_nota INT NULL,
  data DATETIME NOT NULL,
  observacao VARCHAR(255) NULL,
  FOREIGN KEY (id_produto) REFERENCES tb_produto (id_produto),
  FOREIGN KEY (id_usuario) REFERENCES tb_usuario (id_usuario),
  FOREIGN KEY (id_pedido) REFERENCES tb_pedido (id_pedido),
  FOREIGN KEY (id_nota) REFERENCES tb_notasfiscal (id_nota)
);

CREATE TABLE IF NOT EXISTS tb_historico (
  id_historico INT AUTO_INCREMENT PRIMARY KEY,
  entidade_tipo VARCHAR(50) NOT NULL,
  entidade_id INT NOT NULL,
  acao VARCHAR(50) NOT NULL,
  descricao TEXT NULL,
  id_usuario INT NOT NULL,
  data DATETIME NOT NULL,
  FOREIGN KEY (id_usuario) REFERENCES tb_usuario (id_usuario)
);

CREATE TABLE IF NOT EXISTS tb_notificacao (
  id_notificacao INT AUTO_INCREMENT PRIMARY KEY,
  id_usuario INT NOT NULL,
  titulo VARCHAR(150) NOT NULL,
  mensagem TEXT NOT NULL,
  entidade_tipo VARCHAR(50) NULL,
  entidade_id INT NULL,
  lida TINYINT NOT NULL DEFAULT 0,
  data DATETIME NOT NULL,
  FOREIGN KEY (id_usuario) REFERENCES tb_usuario (id_usuario)
);
