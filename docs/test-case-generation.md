# 测试用例生成

## 输入
- PRD 文本（`prdText`）
- API 文档内容（通过 RAG 检索获得，作为 `ragContext`）
- 用户补充指令（可选 `userInstruction`）

## 输出
结构化测试用例列表，每个用例包含：
- caseId：唯一标识
- title：用例标题
- precondition：前置条件
- steps：测试步骤列表
- expected：预期结果
- priority：优先级（P0/P1/P2）
- tags：标签（smoke/normal/exception/boundary/permission）

## 当前实现
`TestCaseGenerationService.generate()` 返回 demo 用例。
生产级实现应集成 LLM + test-case-generation-prompt.txt 生成结构化用例。

## 导出
`TestCaseExportService.export()` 支持 JSON / YAML / Markdown 三种格式。
使用 Jackson ObjectMapper 和 YAMLMapper 序列化。

## 知识回流
`KnowledgeFeedbackService.feedback()` 模拟将确认后的用例向量化存入 PGVector。
