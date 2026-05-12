# 安全与脱敏说明

## SafetyWatcher
`SafetyWatcher` 对工具调用做基础安全检查：

### 检查项
1. 空参数检查
2. 危险命令检测（rm -rf、dd if=、fork bomb 等）
3. 敏感路径拦截（/etc/passwd、/etc/shadow 等）
4. Prompt Injection 模式检测（"ignore previous instructions" 等）
5. 内网 URL 拦截（10.x、172.x、192.168.x）

### 当前限制
- 基于规则匹配，非语义理解
- 不包含 Docker 沙箱隔离
- Docker 沙箱作为扩展设计，详见 docs/architecture.md

## 脱敏措施
1. application-example.yml 中所有密钥使用环境变量占位符
2. 不提交真实 application.yml（已加入 .gitignore）
3. compose.yaml 中密码使用默认值password，仅供本地测试
4. sample_data 全部 mock 生成
5. 所有文档示例为模拟数据
6. 不包含真实公司名、真实域名、真实 API 地址
