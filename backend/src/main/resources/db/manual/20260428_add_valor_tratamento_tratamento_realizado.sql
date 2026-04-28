alter table tratamento_realizado
    add column if not exists valor_tratamento numeric(12, 2) not null default 0;

update tratamento_realizado
set valor_tratamento = valor_total
where valor_tratamento = 0;
