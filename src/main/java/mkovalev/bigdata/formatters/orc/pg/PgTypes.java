package mkovalev.bigdata.formatters.orc.pg;

public enum PgTypes {
    BPCHAR, TEXT, VARCHAR,
    // Floating point 8 bytes
    FLOAT8,
    // Floating point 4 bytes
    REAL, FLOAT4,
    // Integer
    INT4, SERIAL, SMALLSERIAL, BIGSERIAL, SERIAL8, INT8, SMALLINT, BIGINT, INTEGER, INT2,
    // Financial
    NUMERIC,  DECIMAL,
    TIMESTAMPTZ,
    POINT,
    JSONB
}
