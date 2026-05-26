ALTER TABLE file_metadata
  ADD COLUMN upload_status VARCHAR(40) NOT NULL DEFAULT 'COMPLETED',
  ADD COLUMN upload_expires_at TIMESTAMP WITH TIME ZONE,
  ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;

UPDATE file_metadata
SET upload_expires_at = created_at,
    completed_at = created_at
WHERE upload_expires_at IS NULL;

ALTER TABLE file_metadata
  ALTER COLUMN upload_expires_at SET NOT NULL,
  ADD CONSTRAINT file_metadata_upload_status_check
    CHECK (upload_status IN ('PENDING_UPLOAD', 'COMPLETED'));

CREATE INDEX idx_file_metadata_upload_status ON file_metadata (upload_status);
