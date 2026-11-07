-- src/main/resources/schema.sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS public.vector_store (
                                                   id UUID PRIMARY KEY,
                                                   content TEXT,
                                                   metadata JSONB,
                                                   embedding VECTOR(1536)  -- 请根据你的嵌入模型维度调整
    );

CREATE INDEX IF NOT EXISTS idx_vector_store_embedding ON public.vector_store USING ivfflat (embedding vector_cosine_ops);