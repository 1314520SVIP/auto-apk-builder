# Demo Android App

这是一个演示 Android 项目，用于测试 auto-apk-builder 的自动构建功能。

## 项目信息

- **类型**: Android 原生项目 (Kotlin)
- **最小 SDK**: API 24
- **目标 SDK**: API 34
- **构建工具**: Gradle 8.2

## 自动构建

推送代码到 main 分支后，GitHub Actions 会自动：
1. 检测项目类型（Android/Kotlin）
2. 配置 Android SDK 和 JDK
3. 构建 APK
4. 发布到 Release

## 触发构建

```bash
git push origin main
```

构建完成后，可以在 Releases 页面下载 APK。
