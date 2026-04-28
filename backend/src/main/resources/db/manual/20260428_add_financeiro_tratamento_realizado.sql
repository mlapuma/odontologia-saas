alter table tratamento_realizado
    add column if not exists valor_total numeric(12, 2) not null default 0,
    add column if not exists saldo numeric(12, 2) not null default 0,
    add column if not exists forma_pagamento varchar(40) null,
    add column if not exists parcelas integer null;

update tratamento_realizado
set valor_total = valor_pago,
    saldo = greatest(valor_total - valor_pago, 0)
where valor_total = 0;
