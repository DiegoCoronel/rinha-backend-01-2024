/*
 * This file is generated by jOOQ.
 */
package br.rinha.meta;


import br.rinha.meta.tables.Cliente;
import br.rinha.meta.tables.Databasechangeloglock;
import br.rinha.meta.tables.Transacao;
import br.rinha.meta.tables.records.ClienteRecord;
import br.rinha.meta.tables.records.DatabasechangeloglockRecord;
import br.rinha.meta.tables.records.TransacaoRecord;

import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<ClienteRecord> CLIENTE_PKEY = Internal.createUniqueKey(Cliente.CLIENTE, DSL.name("cliente_pkey"), new TableField[] { Cliente.CLIENTE.ID }, true);
    public static final UniqueKey<DatabasechangeloglockRecord> DATABASECHANGELOGLOCK_PKEY = Internal.createUniqueKey(Databasechangeloglock.DATABASECHANGELOGLOCK, DSL.name("databasechangeloglock_pkey"), new TableField[] { Databasechangeloglock.DATABASECHANGELOGLOCK.ID }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<TransacaoRecord, ClienteRecord> TRANSACAO__TRANSACAO_CLIENTE_ID_IDX = Internal.createForeignKey(Transacao.TRANSACAO, DSL.name("transacao_cliente_id_idx"), new TableField[] { Transacao.TRANSACAO.ID_CLIENTE }, Keys.CLIENTE_PKEY, new TableField[] { Cliente.CLIENTE.ID }, true);
}
