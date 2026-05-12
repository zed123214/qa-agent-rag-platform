# 系统架构说明

## 概述

qa-agent-rag-platform 分为两大模块：

### 模块 A：RAG 文档检索与问答（现有源码）
基于 Spring AI 1.1.0 的 RetrievalAugmentationAdvisor 实现，包含 Query Rewriting、PGVector 向量检索、JDBC 对话记忆和 SSE 流式输出。

### 模块 B：测试辅助 Agent（demo 扩展）
在原项目基础上新增的 demo 级模块，包括测试用例生成、AI Critic 评估、Agent 工具编排和安全检查。

## 分层架构

```
┌─────────────────────────────────────────────┐
│              Controller 层                   │
│  ChatController / DocumentController         │
│  DemoTestController / OpenAICompatible       │
├─────────────────────────────────────────────┤
│               Service 层                     │
│  DocumentService / TestCaseGenerationService │
│  TestCaseExportService / RagFilterService    │
│  KnowledgeFeedbackService                    │
├─────────────────────────────────────────────┤
│               Agent 层                       │
│  TestAnalysisAgent / AiCriticEvaluator       │
│  ToolRegistry / SafetyWatcher                │
├─────────────────────────────────────────────┤
│             Infrastructure 层                │
│  Spring AI (ChatClient / RAG Advisor)        │
│  PGVector (Vector Store)                     │
│  PostgreSQL (Chat Memory)                    │
└─────────────────────────────────────────────┘
```

## 关键设计决策

| 决策 | 原因 |
|------|------|
| PGVector 而非 Milvus | 原项目使用 PGVector，零额外依赖 |
| Demo 模块不做真实 LLM 调用 | 保持项目可运行不依赖付费 API |
| Agent 编排用硬编码 demo | 展示设计模式，LangGraph 复杂度留给后续 |
| SafetyWatcher 用规则检查 | Docker 沙箱更适合生产级实现 |
