alter table tratamento_realizado
    add column if not exists dente varchar(3) null;
