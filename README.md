# RAG Document Q&A System

A production-ready **Retrieval-Augmented Generation (RAG)** system built with Spring Boot that enables intelligent question-answering over uploaded documents using local LLMs and vector search.

---

## 🎯 Project Overview

This system demonstrates end-to-end RAG implementation in Java, allowing users to:

* Upload PDF and TXT documents
* Automatically chunk, embed, and store document content in a vector database
* Ask natural language questions and receive contextually accurate answers
* View source chunks with similarity scores for transparency

**Built as a learning project to master RAG concepts and showcase backend development skills.**

---

## 🏗️ Architecture

```
User Upload (PDF/TXT)
↓
Text Extraction (Apache PDFBox)
↓
Chunking (800 chars, 200 overlap, sentence-boundary detection)
↓
Embedding Generation (Ollama: nomic-embed-text)
↓
Vector Storage (ChromaDB)

User Query
↓
Query Embedding (Ollama: nomic-embed-text)
↓
Similarity Search (ChromaDB top-5 chunks)
↓
Prompt Construction (Context + Question)
↓
LLM Response (Ollama: llama3.2)
↓
Answer + Source Attribution
```

---

## 🛠️ Tech Stack

| Component           | Technology                          |
| ------------------- | ----------------------------------- |
| **Backend**         | Spring Boot 3.4, Java 21            |
| **Vector DB**       | ChromaDB 0.4.24                     |
| **LLM Inference**   | Ollama (llama3.2, nomic-embed-text) |
| **LLM Integration** | LangChain4j 0.36.2                  |
| **Database**        | MySQL 8.0                           |
| **PDF Processing**  | Apache PDFBox 3.0.1                 |
| **API Docs**        | Springdoc OpenAPI 2.3.0             |
| **Build Tool**      | Maven                               |

---

## 🚀 Setup Instructions

### Prerequisites

* Java 21
* Maven 3.8+
* Docker
* MySQL 8.0
* Ollama

---

### 1. Install Ollama and Pull Models

```bash
curl -fsSL https://ollama.com/install.sh | sh
ollama pull llama3.2
ollama pull nomic-embed-text
```

---

### 2. Start ChromaDB

```bash
docker run -d -p 8000:8000 --name chromadb chromadb/chroma:0.4.24
```

---

### 3. Setup MySQL Database

```sql
CREATE DATABASE rag_db;
```

---

### 4. Configure Application

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/rag_db
    username: root
    password: YOUR_PASSWORD
```

---

### 5. Run Application

```bash
mvn clean install
mvn spring-boot:run
```

Application runs on:
http://localhost:9000

---

### 6. Access Swagger UI

http://localhost:9000/swagger-ui.html

---

## 📌 API Endpoints

### Document Management

#### Upload Document

```bash
POST /api/documents/upload
Content-Type: multipart/form-data

curl -X POST http://localhost:9000/api/documents/upload \
  -F "file=@document.pdf"
```

#### List All Documents

```bash
GET /api/documents
```

#### Get Document by ID

```bash
GET /api/documents/{id}
```

#### Delete Document

```bash
DELETE /api/documents/{id}
```

---

### Query (RAG)

#### Ask a Question

```bash
POST /api/documents/query
Content-Type: application/json

{
  "question": "What are the main topics discussed in the document?"
}
```

#### Response

```json
{
  "answer": "The document discusses three main topics: ...",
  "sources": [
    {
      "documentId": 1,
      "filename": "document.pdf",
      "text": "Relevant chunk text...",
      "score": 0.89
    }
  ]
}
```

---

## 🧠 How RAG Works (My Understanding)

RAG solves the LLM hallucination problem by grounding responses in actual document content:

1. **Ingestion**: Documents are split into overlapping chunks to preserve context
2. **Embedding**: Each chunk is converted into a vector using a sentence transformer
3. **Storage**: Vectors are stored in ChromaDB with metadata (doc ID, filename, chunk index)
4. **Retrieval**: User queries are embedded and compared against stored vectors using cosine similarity
5. **Generation**: Top-K similar chunks are injected into the LLM prompt as context, forcing factual answers

This approach combines semantic search (vector similarity) with generative AI (LLM completion) — hence "Retrieval-Augmented Generation."

---

## 🎓 Key Learnings

- **Vector embeddings** represent semantic meaning, enabling similarity search beyond keyword matching
- **Chunking strategy** impacts retrieval quality — sentence boundaries prevent mid-sentence splits
- **Prompt engineering** is critical — explicit instructions like "answer ONLY from context" reduce hallucinations
- **Metadata handling** in LangChain4j requires type-safe getters (`getString()`, `getLong()`) to avoid runtime errors
- **ChromaDB versioning** matters — v2 API breaks compatibility with LangChain4j 0.36.2 (use v0.4.24)

---

## ⚠️ Known Limitations

1. **ChromaDB Cleanup**
   Deleted documents are removed from MySQL but embeddings remain in ChromaDB due to LangChain4j API constraints.
   Production implementation would use ChromaDB's native HTTP API for metadata-based deletion.

3. **Single Collection**
   All documents share one ChromaDB collection. Multi-tenancy would require collection-per-user or metadata filtering.

4. **No Authentication**
  API is open. Production deployment needs JWT or API key authentication.

5. **Local LLM Dependency**
   Requires Ollama running locally. Cloud deployment would use HuggingFace Inference API or managed LLM services.

---

## 🔧 Configuration

### Chunking Parameters

```java
private static final int MAX_CHUNK_SIZE = 1000;
private static final int OVERLAP_SIZE = 200;
```

---

### LLM Temperature

```java
.temperature(0.7)
```

---

### Retrieval Count

```java
vectorStoreService.searchSimilar(question, 5);
```

---

## 📂 Project Structure

```
src/main/java/com/sarthak/rag/
├── config/
├── controller/
├── dto/
├── entity/
├── exception/
├── repository/
└── service/
```

---

## 🧪 Testing

```bash
mvn test
```

Covers:

* Document upload
* Query execution
* Error handling
* Input validation

---

## 🚧 Future Enhancements

* [ ] DOCX, PPTX, CSV support
* [ ] Hybrid search (BM25 + vector)
* [ ] Query history
* [ ] Multi-language support
* [ ] Conversation memory
* [ ] Web UI

---

## 👤 Author

**Sarthak Thakor**

* GitHub: https://github.com/thakorsarthak
* LinkedIn: https://www.linkedin.com/in/sarthak-thakor-29s/
* Email: thakorsarthak618@gmail.com

---

## 🙏 Acknowledgments

* LangChain4j
* Ollama
* ChromaDB
* Apache PDFBox
