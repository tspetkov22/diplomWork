-- Add normalized_name column
ALTER TABLE treatment_type ADD COLUMN normalized_name VARCHAR(100);

-- Update existing English treatment types to set their normalized_name to their name
UPDATE treatment_type SET normalized_name = name WHERE language = 'en';

-- Update Bulgarian treatment types to match their English counterparts
UPDATE treatment_type bg
SET normalized_name = (
    CASE 
        WHEN bg.name = 'МЕДИЦИНСКО' THEN 'MEDICAL'
        WHEN bg.name = 'ХОМЕОПАТИЧНО' THEN 'HOMEOPATHIC'
        WHEN bg.name = 'ФИТОТЕРАПЕВТИЧНО' THEN 'PHYTOTHERAPEUTIC'
    END
)
WHERE bg.language = 'bg';

-- Add unique constraint
ALTER TABLE treatment_type ADD CONSTRAINT uk_treatment_type_normalized_name_lang UNIQUE (normalized_name, language); 