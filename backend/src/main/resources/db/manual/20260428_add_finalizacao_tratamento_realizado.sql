alter table tratamento_realizado
    add column if not exists finalizado boolean not null default false,
    add column if not exists data_finalizacao timestamp null;
