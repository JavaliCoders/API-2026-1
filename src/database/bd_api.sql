CREATE database bd_api;
USE bd_api;

CREATE TABLE tb_perfil (
  id_perfil INT auto_increment KEY NOT NULL,
  perfil VARCHAR(12) NOT NULL
   );
   
INSERT INTO tb_perfil (perfil) VALUES 
('DIRETOR'),
('FINANCEIRO'),
('ESTOQUE'),
('OPERACIONAL');

CREATE TABLE tb_usuario(
  id_usuario INT auto_increment PRIMARY KEY NOT NULL,
  nome VARCHAR(150) NOT NULL,
  usuario VARCHAR(30) NOT NULL,
  senha VARCHAR(45) NOT NULL,
  email VARCHAR(255) NOT NULL,
  status ENUM('ATIVO', 'INATIVO') NOT NULL,
  id_perfil INT NOT NULL,
  FOREIGN KEY (id_perfil)
  REFERENCES tb_perfil (id_perfil)
  );
  

CREATE TABLE tb_setor (
  id_setor INT auto_increment PRIMARY KEY NOT NULL,
  setor VARCHAR(45) NOT NULL
  );
  
Insert into tb_setor (setor) values
('Diretoria'),
('Financeiro'),
('Compras'),
('Operacional');

CREATE TABLE tb_centrocusto (
  id_centrocusto INT auto_increment NOT NULL,
  centro_custo VARCHAR(80) NOT NULL,
  PRIMARY KEY (id_centrocusto)
  );

Insert into tb_centrocusto (centro_custo) values
('Manutenção'),
('Limpeza'),
('Administrativo'),
('Informática'),
('Infraestrutura');


CREATE TABLE tb_produto (
  id_produto INT auto_increment NOT NULL,
  produto VARCHAR(60) NOT NULL,
  descricao VARCHAR(155) NOT NULL,
  unidade_medida CHAR(2) NOT NULL,
  nivel_minimo INT NOT NULL,
  valor_estimado DECIMAL(8,2) NOT NULL,
  status ENUM('ATIVO', 'INATIVO') NOT NULL,
  saldo INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id_produto`));

CREATE TABLE tb_pedido (
  id_pedido INT auto_increment NOT NULL,
  num_pedido VARCHAR(10) unique NOT NULL,
  data_abertura DATETIME NOT NULL,
  status ENUM('EM_APROVACAO', 'APROVADO', 'NEGADO', 'EM_COTACAO', 'EM_COMPRA', 'FINALIZADO', 'CANCELADO') NOT NULL default 'EM_APROVAÇÃO',
  valor_total_estimado DECIMAL(8,2) NOT NULL,
  data_aprovacao DATETIME NULL,
  parecer MEDIUMTEXT NULL,
  id_solicitante INT NOT NULL,
  id_aprovador INT NULL,
  id_centrocusto INT NOT NULL,
  id_setor INT NOT NULL,
  PRIMARY KEY (id_pedido),
  FOREIGN KEY (id_solicitante)
  REFERENCES tb_usuario (id_usuario),
  FOREIGN KEY (id_aprovador)
  REFERENCES tb_usuario (id_usuario),
  FOREIGN KEY (id_centrocusto)
  REFERENCES tb_centrocusto (id_centrocusto),
  FOREIGN KEY (id_setor)
  REFERENCES tb_setor (id_setor)
  );

CREATE TABLE tb_pedido_produto (
  id_pedido INT NOT NULL,
  id_produto INT NOT NULL,
  qtd_solicitada INT NOT NULL,
  id_pedido_produto INT auto_increment NOT NULL,
  qtd_aprovada INT NULL,
  qtd_recebida INT NULL DEFAULT 0,
  PRIMARY KEY (id_pedido_produto),
  FOREIGN KEY (id_pedido)
  REFERENCES tb_pedido (id_pedido),
  FOREIGN KEY (id_produto)
  REFERENCES tb_produto (id_produto)
  );

CREATE TABLE tb_fornecedor (
  id_fornecedor INT auto_increment NOT NULL,
  nome VARCHAR(45) NOT NULL,
  cnpj VARCHAR(14) NOT NULL,
  tipo_pagamento ENUM('PIX', 'CARTAO', 'TRANSFERENCIA', 'BOLETO', 'FATURADO') NOT NULL,
  pedido_minimo DECIMAL(8,2) NULL,
  status ENUM('ATIVO', 'INATIVO') NOT NULL,
  PRIMARY KEY (id_fornecedor)
  );

CREATE TABLE tb_fornecedor_produto (
  id_fornecedor INT NOT NULL,
  id_produto INT NOT NULL,
  id_produto_fornecedor INT auto_increment NOT NULL,
  preco_uni DECIMAL(8,2) NOT NULL,
  PRIMARY KEY (id_produto_fornecedor),
  FOREIGN KEY (id_fornecedor)
  REFERENCES tb_fornecedor (id_fornecedor),
  FOREIGN KEY (id_produto)
  REFERENCES tb_produto (id_produto)
  );

CREATE TABLE tb_anexo (
  id_anexo INT auto_increment NOT NULL,
  tipo ENUM('COTACAO', 'NOTA_FISCAL') NOT NULL,
  nome_arq VARCHAR(100) NOT NULL,
  caminho_arquivo VARCHAR(255) NOT NULL,
  data_upload DATETIME NOT NULL,
  PRIMARY KEY (id_anexo)
  );

CREATE TABLE tb_cotacao (
  id_cotacao INT auto_increment NOT NULL,
  status ENUM('AGUARDANDO_APROVACAO', 'APROVADO', 'APROVADO_PARCIALMENTE', 'NEGADO') NOT NULL,
  data_criacao DATETIME NOT NULL,
  data_aprovacao DATETIME NULL,
  parecer TEXT NULL,
  id_aprovador INT NULL,
  valor_total DECIMAL(8,2) NOT NULL,
  id_pedido INT NOT NULL,
  id_fornecedor INT NOT NULL,
  id_anexo INT NOT NULL,
  PRIMARY KEY (id_cotacao),
    FOREIGN KEY (id_aprovador)
    REFERENCES tb_usuario (id_usuario),
    FOREIGN KEY (id_pedido)
    REFERENCES tb_pedido (id_pedido),
    FOREIGN KEY (id_fornecedor)
    REFERENCES tb_fornecedor (id_fornecedor),
    FOREIGN KEY (id_anexo)
    REFERENCES tb_anexo (id_anexo)
    );

CREATE TABLE tb_compra (
  id_compra INT auto_increment NOT NULL,
  id_pedido INT NOT NULL,
  id_fornecedor INT NOT NULL,
  data DATETIME NOT NULL,
  id_comprador INT NOT NULL,
  valor_total DECIMAL(8,2) NOT NULL,
  data_prevista DATETIME NULL,
  status ENUM('REALIZADA', 'CANCELADA') NOT NULL,
  PRIMARY KEY (id_compra),
    FOREIGN KEY (id_pedido)
    REFERENCES tb_pedido (id_pedido),
    FOREIGN KEY (id_fornecedor)
    REFERENCES tb_fornecedor (id_fornecedor),
    FOREIGN KEY (id_comprador)
    REFERENCES tb_usuario (id_usuario)
    );

CREATE TABLE tb_notasfiscal (
  id_nota INT auto_increment NOT NULL,
  numero_nota VARCHAR(10) NOT NULL,
  data_emissao DATETIME NOT NULL,
  data_registro DATETIME NOT NULL,
  id_usuario_registro INT NOT NULL,
  id_compra INT NOT NULL,
  valor_nf DECIMAL(8,2) NOT NULL,
  id_anexo INT NOT NULL,
  status ENUM('REGISTRADA', 'CONFERIDA', 'RECUSADA') NOT NULL,
  id_usuario_conferencia INT NULL,
  data_conferencia DATETIME NULL,
  total_itens INT NOT NULL,
  PRIMARY KEY (id_nota),
    FOREIGN KEY (id_usuario_registro)
    REFERENCES tb_usuario (id_usuario),
    FOREIGN KEY (id_compra)
    REFERENCES tb_compra (id_compra),
    FOREIGN KEY (id_anexo)
    REFERENCES tb_anexo (id_anexo),
    FOREIGN KEY (id_usuario_conferencia)
    REFERENCES tb_usuario (id_usuario)
    );

CREATE TABLE tb_compra_item (
  id_compra_item INT auto_increment NOT NULL,
  id_compra INT NOT NULL,
  id_pedido_produto INT NOT NULL,
  valor_uni DECIMAL(8,2) NOT NULL,
  qtd_comprada DECIMAL(8,2) NOT NULL,
  valor_total DECIMAL(8,2) NOT NULL,
  PRIMARY KEY (id_compra_item),
    FOREIGN KEY (id_compra)
    REFERENCES tb_compra (id_compra),
    FOREIGN KEY (id_pedido_produto)
    REFERENCES tb_pedido_produto (id_pedido_produto)
    );

CREATE TABLE tb_nf_item (
  id_nf_item INT auto_increment NOT NULL,
  id_pedido_produto INT NOT NULL,
  qtd_recebida INT NOT NULL,
  qtd_rejeitada INT NOT NULL DEFAULT 0,
  motivo_divergencia VARCHAR(255) NULL,
  PRIMARY KEY (id_nf_item),
    FOREIGN KEY (id_pedido_produto)
    REFERENCES tb_pedido_produto (id_pedido_produto)
    );


CREATE TABLE tb_movimentacao (
  id_movimentacao INT auto_increment NOT NULL,
  id_produto INT NOT NULL,
  tipo_movimentação ENUM('ENTRADA', 'SAÍDA', 'ENTRADA_MANUAL', 'SAIDA_MANUAL') NOT NULL,
  quantidade INT NOT NULL,
  id_usuario INT NOT NULL,
  id_pedido INT NULL,
  id_nota INT NULL,
  data DATETIME NOT NULL,
  observacao VARCHAR(255) NULL,
  PRIMARY KEY (id_movimentacao),
    FOREIGN KEY (id_produto)
    REFERENCES tb_produto (id_produto),
    FOREIGN KEY (id_usuario)
    REFERENCES tb_usuario (id_usuario),
    FOREIGN KEY (id_pedido)
    REFERENCES tb_pedido (id_pedido),
    FOREIGN KEY (id_nota)
    REFERENCES tb_notasfiscal (id_nota)
	);

CREATE TABLE tb_historico (
  id_historico INT auto_increment NOT NULL,
  entidade_tipo VARCHAR(50) NOT NULL,
  entidade_id INT NOT NULL,
  acao VARCHAR(50) NOT NULL,
  descricao TEXT NULL,
  id_usuario INT NOT NULL,
  data DATETIME NOT NULL,
  PRIMARY KEY (id_historico),
    FOREIGN KEY (id_usuario)
    REFERENCES tb_usuario (id_usuario)
    );


CREATE TABLE tb_notificacao (
  id_notificacao INT auto_increment NOT NULL,
  id_usuario INT NOT NULL,
  titulo VARCHAR(150) NOT NULL,
  mensagem TEXT NOT NULL,
  entidade_tipo VARCHAR(50) NULL,
  entidade_id INT NULL,
  lida TINYINT NOT NULL DEFAULT 0,
  data DATETIME NOT NULL,
  PRIMARY KEY (id_notificacao),
    FOREIGN KEY (id_usuario)
    REFERENCES tb_usuario (id_usuario)
    );

