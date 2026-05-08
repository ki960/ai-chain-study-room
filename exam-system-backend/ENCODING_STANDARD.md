# 项目文件编码标准

## 1. 编码标准概述

本项目**所有文件统一采用 UTF-8 编码格式**（无BOM），确保在文件创建、保存、传输和读取的全过程中保持编码一致性，避免因编码格式不统一导致的字符乱码、数据损坏或跨平台兼容性问题。

## 2. 编码规范要求

### 2.1 文件类型编码规则

| 文件类型 | 编码格式 | 备注 |
|---------|---------|------|
| Java 源文件 | UTF-8 | `.java` |
| XML 配置文件 | UTF-8 | `.xml` |
| YAML/yml 配置文件 | UTF-8 | `.yml`, `.yaml` |
| Properties 配置文件 | UTF-8 | `.properties` |
| SQL 脚本文件 | UTF-8 | `.sql` |
| Vue/JavaScript/TypeScript | UTF-8 | `.vue`, `.js`, `.jsx`, `.ts`, `.tsx` |
| CSS/SCSS/LESS | UTF-8 | `.css`, `.scss`, `.less` |
| HTML 文件 | UTF-8 | `.html`, `.htm` |
| JSON 配置文件 | UTF-8 | `.json` |
| Markdown 文档 | UTF-8 | `.md` |
| 文本文件 | UTF-8 | `.txt` |

### 2.2 二进制文件

以下文件类型视为二进制文件，不进行文本编码处理：
- 图片文件：`.png`, `.jpg`, `.jpeg`, `.gif`, `.ico`
- 字体文件：`.woff`, `.woff2`, `.ttf`, `.eot`
- 压缩文件：`.zip`, `.jar`, `.war`
- 可执行文件：`.exe`, `.class`

## 3. 编辑器/IDE配置要求

### 3.1 Visual Studio Code

```json
{
  "files.encoding": "utf8",
  "files.defaultLanguage": "en",
  "[java]": {
    "files.encoding": "utf8"
  },
  "[vue]": {
    "files.encoding": "utf8"
  },
  "files.trimTrailingWhitespace": true,
  "files.insertFinalNewline": true
}
```

### 3.2 IntelliJ IDEA

- **File > Settings > Editor > File Encodings**
  - Global Encoding: UTF-8
  - Project Encoding: UTF-8
  - Default encoding for properties files: UTF-8
  - 勾选 "Transparent native-to-ascii conversion"

### 3.3 Eclipse

- **Window > Preferences > General > Workspace**
  - Text file encoding: UTF-8
- **Window > Preferences > General > Content Types**
  - 设置所有文本类型的默认编码为 UTF-8

## 4. 后端（Spring Boot）配置

### 4.1 pom.xml 配置

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
</properties>
```

### 4.2 application.yml 配置

```yaml
server:
  servlet:
    encoding:
      charset: UTF-8
      enabled: true
      force: true

spring:
  http:
    encoding:
      charset: UTF-8
      enabled: true
      force: true
  datasource:
    url: jdbc:mysql://localhost:3306/example_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
```

### 4.3 响应编码过滤器

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        FilterRegistrationBean<CharacterEncodingFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
```

## 5. 前端（Vue）配置

### 5.1 Vite 配置

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

### 5.2 axios 请求配置

```javascript
import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

export default request
```

### 5.3 HTML 页面编码

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>页面标题</title>
</head>
```

## 6. 数据库连接配置

### 6.1 MySQL 连接

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/example_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: admin
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 6.2 SQLite 连接（如使用）

```yaml
spring:
  datasource:
    url: jdbc:sqlite:example_db.db
    driver-class-name: org.sqlite.JDBC
  sql:
    init:
      encoding: UTF-8
```

## 7. Git 配置

### 7.1 .gitattributes 文件

```
# 文本文件编码设置
*.java         text encoding=utf-8
*.xml          text encoding=utf-8
*.yml          text encoding=utf-8
*.yaml         text encoding=utf-8
*.properties   text encoding=utf-8
*.sql          text encoding=utf-8
*.md           text encoding=utf-8
*.txt          text encoding=utf-8
*.vue          text encoding=utf-8
*.js           text encoding=utf-8
*.ts           text encoding=utf-8
*.css          text encoding=utf-8
*.html         text encoding=utf-8
*.json         text encoding=utf-8

# 二进制文件
*.png          binary
*.jpg          binary
*.jpeg         binary
*.gif          binary
*.svg          text encoding=utf-8
*.ico          binary
*.zip          binary
*.jar          binary

# 换行符统一
*              text=auto eol=lf
```

### 7.2 .gitconfig 全局配置

```gitconfig
[core]
    autocrlf = false
    filemode = false
    symlinks = false
[gui]
    encoding = utf-8
[i18n]
    commitEncoding = utf-8
    logOutputEncoding = utf-8
```

## 8. 编码问题排查指南

### 8.1 常见编码问题

1. **中文乱码**：检查文件编码是否为 UTF-8，数据库连接是否配置了 `characterEncoding=UTF-8`
2. **Properties 文件乱码**：确保使用 UTF-8 编码，并在 IDE 中启用 "Transparent native-to-ascii conversion"
3. **SQL 脚本执行乱码**：检查脚本文件编码和数据库连接编码是否一致
4. **响应数据乱码**：检查 Spring Boot 的 `server.servlet.encoding` 配置

### 8.2 验证方法

```bash
# 检查文件编码（Linux/Mac）
file -I filename.java

# 检查文件编码（Windows PowerShell）
Get-Content filename.java -Encoding UTF8 | Select-Object -First 5

# 检查 MySQL 数据库编码
SHOW VARIABLES LIKE 'character_set_%';
SHOW VARIABLES LIKE 'collation_%';
```

## 9. 团队协作规范

1. **新成员入职**：必须配置好开发环境的 UTF-8 编码设置
2. **代码审查**：审查代码时需检查文件编码是否符合规范
3. **CI/CD 流程**：构建脚本需确保使用 UTF-8 编码处理所有文本文件
4. **文档更新**：编码标准有变更时，需及时更新此文档

## 10. 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|-----|------|------|---------|
| 1.0 | 2026-05-08 | 系统管理员 | 初始版本，定义项目UTF-8编码标准 |