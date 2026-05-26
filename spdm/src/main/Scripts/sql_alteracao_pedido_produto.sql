-- Alterações conforme análise técnica Speciale (item 6)
-- Executar em ambiente de desenvolvimento/teste antes de produção

CREATE SEQUENCE IF NOT EXISTS ped.sq_familia
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS ped.tb_familia (
    id_familia bigint NOT NULL,
    des_familia character varying(100) NOT NULL,
    flg_integral character varying(1) DEFAULT 'N' NOT NULL,
    id_externo bigint,
    CONSTRAINT pk_familia PRIMARY KEY (id_familia)
);

ALTER TABLE ped.tb_familia
    ADD COLUMN IF NOT EXISTS id_externo bigint;

CREATE UNIQUE INDEX IF NOT EXISTS uk_familia_id_externo ON ped.tb_familia (id_externo)
    WHERE id_externo IS NOT NULL;

COMMENT ON COLUMN ped.tb_familia.id_externo IS 'Código da família na Omie (codigo_familia)';

ALTER TABLE ped.tb_produto
    ADD COLUMN IF NOT EXISTS id_familia bigint;

ALTER TABLE ped.tb_produto
    ADD COLUMN IF NOT EXISTS flg_integral character varying(1) DEFAULT 'N' NOT NULL;

ALTER TABLE ped.tb_pedido_produto
    ADD COLUMN IF NOT EXISTS id_familia bigint,
    ADD COLUMN IF NOT EXISTS id_receita bigint,
    ADD COLUMN IF NOT EXISTS unidade character varying(10);

COMMENT ON COLUMN ped.tb_pedido_produto.id_familia IS 'Família do produto no momento do pedido';
COMMENT ON COLUMN ped.tb_pedido_produto.id_receita IS 'Receita do produto no momento do pedido';
COMMENT ON COLUMN ped.tb_pedido_produto.unidade IS 'Unidade de venda: UND ou PCT';
