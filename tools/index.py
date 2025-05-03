#!/usr/bin/env python3
import os
import time
from pathlib import Path

from openai import OpenAI

import chromadb
from chromadb.config import Settings
from chromadb import PersistentClient
from chromadb.config import Settings, DEFAULT_TENANT, DEFAULT_DATABASE

# ─── Base dirs ─────────────────────────────────────────────────────────────────
BASE_DIR = Path(__file__).resolve().parent.parent
CODE_DIR = BASE_DIR / "legacy" / "kitchensink" / "src"
CHUNK_DIR = BASE_DIR / "chunks"
VS_DIR = BASE_DIR / "vector_store"

# ─── Chunk settings & env ──────────────────────────────────────────────────────
CHUNK_SIZE = 400
OVERLAP = 50
OPENAI_KEY = os.getenv("OPENAI_API_KEY")
if not OPENAI_KEY:
    raise RuntimeError("Set OPENAI_API_KEY in your env before running.")

# ─── Prepare dirs ───────────────────────────────────────────────────────────────
CHUNK_DIR.mkdir(exist_ok=True)
VS_DIR.mkdir(exist_ok=True)

# ─── 1) Chunk the code ──────────────────────────────────────────────────────────
print("🔪 Chunking code...")
for java_file in CODE_DIR.rglob("*.java"):
    lines = java_file.read_text().splitlines()
    for i in range(0, len(lines), CHUNK_SIZE - OVERLAP):
        chunk = "\n".join(lines[i : i + CHUNK_SIZE])
        out_fp = CHUNK_DIR / f"{java_file.stem}_{i//(CHUNK_SIZE-OVERLAP)}.txt"
        out_fp.write_text(chunk)
print(f"✅ Created {len(list(CHUNK_DIR.iterdir()))} chunks in {CHUNK_DIR}")

# ─── 2) Init PersistentClient ──────────────────────────────────────────────────
print("🚀 Initializing PersistentClient…")
client = PersistentClient(
    path=str(VS_DIR),  # where to store your DB files
    settings=Settings(anonymized_telemetry=False),  # optional
    tenant=DEFAULT_TENANT,
    database=DEFAULT_DATABASE,
)
collection = client.get_or_create_collection("kitchensink-code")

# ─── 3) Embed & upsert with batching & retry ────────────────────────────────────
print("🤖 Embedding & upserting chunks...")
llm = OpenAI(api_key=OPENAI_KEY)

BATCH_SIZE = 10
RETRIES = 5
DELAY = 2  # seconds
chunk_files = sorted(CHUNK_DIR.iterdir())

for batch_start in range(0, len(chunk_files), BATCH_SIZE):
    batch = chunk_files[batch_start : batch_start + BATCH_SIZE]
    texts = [cf.read_text() for cf in batch]
    print(
        f"  • Processing batch {batch_start+1}-{batch_start+len(batch)} of {len(chunk_files)}"
    )

    for attempt in range(1, RETRIES + 1):
        try:
            resp = llm.embeddings.create(input=texts, model="text-embedding-ada-002")
            embs = [d.embedding for d in resp.data]
            print(f"    ✓ Embedded batch on attempt {attempt}")
            break
        except Exception as e:
            print(f"    ⚠ Embed error on attempt {attempt}: {e}")
            if attempt < RETRIES:
                time.sleep(DELAY * attempt)
            else:
                raise RuntimeError("Failed to embed batch after retries") from e

    for cf, emb in zip(batch, embs):
        collection.upsert(
            ids=[cf.name],
            documents=[texts[batch.index(cf)]],
            embeddings=[emb],
            metadatas=[{"source": str(cf)}],
        )
        print(f"      • Upserted {cf.name}")

# ─── 4) Done & persist ─────────────────────────────────────────────────────────
print(f"🎉 Indexed all {len(chunk_files)} chunks into {VS_DIR}")
