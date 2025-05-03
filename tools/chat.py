#!/usr/bin/env python3
import os
import subprocess
import tempfile
from pathlib import Path
from datetime import datetime

import chromadb
from chromadb import PersistentClient
from chromadb.config import Settings, DEFAULT_TENANT, DEFAULT_DATABASE
from openai import OpenAI

# ─── Base dirs ─────────────────────────────────────────────────────────────────
BASE_DIR = Path(__file__).resolve().parent.parent
VS_DIR = BASE_DIR / "vector_store"
RESP_DIR = BASE_DIR / "responses"

# ─── Config ────────────────────────────────────────────────────────────────────
OPENAI_KEY = os.getenv("OPENAI_API_KEY")
if not OPENAI_KEY:
    raise RuntimeError("Set OPENAI_API_KEY in your env before running.")

# ─── Ensure responses directory exists ──────────────────────────────────────────
RESP_DIR.mkdir(exist_ok=True)

# ─── Chroma client ─────────────────────────────────────────────────────────────
print("🚀 Initializing PersistentClient…")
client = PersistentClient(
    path=str(VS_DIR),
    settings=Settings(anonymized_telemetry=False),
    tenant=DEFAULT_TENANT,
    database=DEFAULT_DATABASE,
)
collection = client.get_collection("kitchensink-code")

# ─── LLM client ────────────────────────────────────────────────────────────────
llm = OpenAI(api_key=OPENAI_KEY)


def get_query_from_editor() -> str:
    editor = os.environ.get("VISUAL") or os.environ.get("EDITOR") or "vi"
    with tempfile.NamedTemporaryFile(suffix=".tmp", delete=False) as tf:
        tmp_path = tf.name
    subprocess.call([editor, tmp_path])
    content = Path(tmp_path).read_text().strip()
    os.unlink(tmp_path)
    return content


def answer(query: str) -> str:
    # 1) embed the query with OpenAI (1536-dim)
    emb_resp = llm.embeddings.create(input=query, model="text-embedding-ada-002")
    query_emb = emb_resp.data[0].embedding

    # 2) retrieve top 5 matching chunks by embedding
    docs = collection.query(query_embeddings=[query_emb], n_results=5)["documents"][0]

    # 3) build prompt
    context = "\n\n".join(docs)
    prompt = (
        "Use ONLY the following context to answer the question.\n\n"
        f"CONTEXT:\n{context}\n\n"
        f"QUESTION: {query}\n\n"
        "ANSWER:"
    )

    # 4) call LLM for answer
    resp = llm.chat.completions.create(
        model="gpt-4o-mini", messages=[{"role": "user", "content": prompt}]
    )
    return resp.choices[0].message.content.strip()


if __name__ == "__main__":
    print("🤖 kitchensink RAG chat (press Enter to compose question, editor will open)")
    try:
        while True:
            input("\nPress Enter to compose your question...")
            q = get_query_from_editor()
            if not q:
                print("No question entered—exiting.")
                break
            print(f"\nQ: {q}")
            ans = answer(q)
            print(f"\nA: {ans}\n")

            # Save Q/A to file
            ts = datetime.now().strftime("%Y%m%d_%H%M%S")
            file_path = RESP_DIR / f"{ts}.md"
            file_path.write_text(f"# Question\n{q}\n\n# Answer\n{ans}\n")
            print(f"Saved Q/A to {file_path}")
    except KeyboardInterrupt:
        print("\nGoodbye.")
