# WORKSPACE RULES & CODING STANDARDS

## 🛡️ Database Migration & Schema Integrity
1. **ZERO ALTER TABLE IN DEVELOPING MIGRATIONS**: Never rely on `ALTER TABLE` statements in follow-up Flyway migration files for schema changes that should exist in baseline files. All table schemas, constraints, and audit columns must be clean, standardized, complete, and correct from their initial `CREATE TABLE` definition.
2. **ZERO UNMAPPED MAPSTRUCT WARNINGS**: MapStruct mappers must explicitly map or ignore all target properties. Never leave unmapped target fields that could lead to subtle mapping bugs or runtime failures in production.
