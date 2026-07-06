CREATE UNIQUE INDEX IF NOT EXISTS uk_user_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_user_status ON users (status);

CREATE INDEX IF NOT EXISTS idx_project_user_id_deleted_at ON projects (user_id, deleted_at);

CREATE INDEX IF NOT EXISTS fts_tech_stack_name ON tech_stacks USING gin (to_tsvector('simple', name));

CREATE INDEX IF NOT EXISTS idx_dependency_tech_stack_id ON dependencies (tech_stack_id);
CREATE INDEX IF NOT EXISTS fts_dependency_name ON dependencies USING gin (to_tsvector('simple', name));

CREATE INDEX IF NOT EXISTS idx_field_stack_mapping_field_id ON field_stack_mappings (field_id);
CREATE INDEX IF NOT EXISTS idx_field_stack_mapping_tech_stack_id ON field_stack_mappings (tech_stack_id);

CREATE INDEX IF NOT EXISTS idx_project_field_project_id ON project_fields (project_id);

CREATE INDEX IF NOT EXISTS idx_project_tech_stack_project_id ON project_tech_stacks (project_id);

CREATE INDEX IF NOT EXISTS idx_project_dependency_project_id ON project_dependencies (project_id);
