CREATE TABLE file_metadata (
  id UUID PRIMARY KEY,
  ticket_id UUID NOT NULL,
  uploader_id UUID NOT NULL,
  original_filename VARCHAR(255) NOT NULL,
  object_key VARCHAR(512) NOT NULL UNIQUE,
  content_type VARCHAR(120) NOT NULL,
  size_bytes BIGINT NOT NULL,
  validation_status VARCHAR(40) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
  CONSTRAINT file_metadata_size_positive CHECK (size_bytes > 0),
  CONSTRAINT file_metadata_validation_status_check
    CHECK (validation_status IN ('PENDING', 'VALIDATED', 'REJECTED', 'FAILED'))
);

CREATE INDEX idx_file_metadata_ticket_id ON file_metadata (ticket_id);
CREATE INDEX idx_file_metadata_uploader_id ON file_metadata (uploader_id);
CREATE INDEX idx_file_metadata_validation_status ON file_metadata (validation_status);
