create table if not exists tratamento_realizado (
    id bigserial primary key,
    tenant_id bigint not null,
    paciente_id bigint not null,
    procedimento_id bigint null,
    tratamento varchar(255) not null,
    valor_pago numeric(12, 2) not null,
    data_realizacao date not null,
    observacoes text null,
    created_at timestamp null,
    updated_at timestamp null
);

create index if not exists idx_tratamento_realizado_tenant_data
    on tratamento_realizado (tenant_id, data_realizacao);

create index if not exists idx_tratamento_realizado_paciente
    on tratamento_realizado (tenant_id, paciente_id);
