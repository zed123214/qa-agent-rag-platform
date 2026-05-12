# RAG 检索流水线

## 完整链路

```
文档上传 → 解析 → 分块 → 向量化 → 入库(PGVector)
   │
用户提问 → Query Rewriting → 向量检索(TopK=5) → 拼接Prompt → LLM生成 → SSE输出
```

## 关键代码路径

### 文档入库
`DocumentService.uploadDocument()` → `TokenTextSplitter` → `vectorStore.add(chunks)`

- 支持 PDF（PagePdfDocumentReader）
- 支持 Markdown（MarkdownDocumentReader）
- 支持纯文本
- 每个 chunk 携带 metadata：document_id, filename, source

### Query Rewriting
`ChatClientConfig` → `RewriteQueryTransformer`
目的：将用户口语化提问改写为更适合检索的查询

### 向量检索
`VectorStoreDocumentRetriever` → PGVector similarity search
- topK: 5
- similarityThreshold: 0.5
- distanceType: COSINE_DISTANCE
- indexType: HNSW

### 二次过滤（demo）
`RagFilterService.filter()` → 基于规则剔除目录页、版权声明等噪音

### Prompt 拼接与生成
检索到的 chunk 文本作为 context 注入 prompt → ChatClient.call() / stream()
