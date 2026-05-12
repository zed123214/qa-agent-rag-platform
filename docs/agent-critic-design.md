# AI Critic 评估设计


## 当前实现
`AiCriticEvaluator` 是一个 demo 级评估器，基于规则而非 LLM：

### 评估维度
1. **覆盖率（0-30 分）**
   - 检查是否覆盖正常流、异常流、边界值、权限校验
   - 通过 tags 字段判断

2. **逻辑自洽性（0-30 分）**
   - 检查 steps 和 expected 字段是否为空
   - 生产级应检查步骤间逻辑一致性

3. **重复检测（0-20 分）**
   - 基于 title 字段的完全匹配检测

4. **规范性（0-20 分）**
   - 检查 caseId、priority 等字段完整性

### 输出
`EvaluationReport` 包含 score、coverageIssues、logicIssues、duplicateIssues、suggestions、summary

## 生产级扩展方向
- 接入 LLM 做语义级评估（使用 ai-critic-evaluation-prompt.txt）
- 和标准用例库对比（Golden Sample 比对）
- 基于历史缺陷数据做遗漏检测
