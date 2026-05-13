# 10 Git 与 GitHub 小白教程

如果你是第一次接触 Git，本章请按顺序读完、按命令敲完。完成后你会拥有：

- 一个干净配好身份的 Git 客户端
- 把项目推到 GitHub 的能力
- 在本地拉、改、推、看历史的全套操作

## 1. 概念三件套：工作区 / 暂存区 / 仓库

```text
工作区(Working Directory)
    │  git add
    ▼
暂存区(Staging Area / Index)
    │  git commit
    ▼
本地仓库(Local Repository)
    │  git push
    ▼
远程仓库(Remote: GitHub)
```

- **工作区**：你电脑上能看到的文件夹。
- **暂存区**：`git add` 后，文件进入"准备提交"队列。
- **本地仓库**：`git commit` 后写入本地 `.git` 目录。
- **远程仓库**：GitHub 上的镜像，可以多人协作、随时拉取/恢复。

## 2. 安装与一次性配置

安装见 [01-环境准备与工具安装](./01-环境准备与工具安装.md)。

第一次用 Git 必须配身份（提交记录里会显示）：

```bash
git config --global user.name "你的名字"
git config --global user.email "你的邮箱@example.com"
```

> Windows 上推荐勾选 Git Bash，本章命令都在 Git Bash 里执行（更稳定，命令和 macOS/Linux 一致）。

## 3. 注册 GitHub 账号

- 网址：<https://github.com/>
- 用 QQ / 网易邮箱注册都可以
- 注册后到 `Settings → Emails` 把邮箱标记为 verified

## 4. 配 SSH 还是 HTTPS？

两种"和 GitHub 通信"的方式：

| 方式 | 验证 | 优点 | 缺点 |
| --- | --- | --- | --- |
| HTTPS | 用户名 + 个人访问令牌(PAT) | 简单，公司常用 | 每次推送可能要输 token |
| SSH | 本地公钥/私钥 | 一次配置永久免输入 | 略需配置 |

新手推荐 **HTTPS + 凭证管理器**，Windows 装 Git 默认就会装"Credential Manager"，第一次推送弹出输入用户名密码（密码用 PAT），之后免输。

### 生成 GitHub Personal Access Token

1. 登录 GitHub → 右上头像 → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token (classic)
3. 选 scope：勾选 `repo` 即可
4. 复制生成的 `ghp_xxxx`，**只显示一次**，找个本地密码本存好
5. 推送时 GitHub 会让你输用户名（GitHub 用户名）+ 密码（这里粘 PAT）

### 可选：配置 SSH

```bash
ssh-keygen -t ed25519 -C "你的邮箱@example.com"
# 一路回车
cat ~/.ssh/id_ed25519.pub
```

把公钥粘到 GitHub → Settings → SSH and GPG keys → New SSH key。

## 5. 把已有项目推到 GitHub（从 0 到 1）

### 5.1 在 GitHub 上新建一个空仓库

1. GitHub 右上角 + → New repository
2. 填名字（如 `junior-java-company-demo`）
3. **不要**勾选 "Initialize this repository with a README"（你的本地已经有 README 了）
4. Create repository

### 5.2 在本地把项目变成 Git 仓库

```bash
cd /path/to/junior-java-company-demo

# 初始化
git init

# 把所有文件加入暂存区（注意：先确认 .gitignore 写好了，不会把不该传的传上去）
git add .

# 看一眼状态
git status

# 提交
git commit -m "feat: initial commit"
```

### 5.3 绑定远程仓库

```bash
# 把 GitHub 给你的仓库地址（HTTPS）绑到本地，名字叫 origin
git remote add origin https://github.com/你的用户名/junior-java-company-demo.git

# 看看绑得对不对
git remote -v
```

### 5.4 改默认分支名为 main（可选，与 GitHub 一致）

```bash
git branch -M main
```

### 5.5 推送

```bash
git push -u origin main
```

- `-u` 表示"以后这条分支默认推到 origin 的 main"，下次直接 `git push` 就行。
- 第一次会弹出登录窗：用户名填你的 GitHub 用户名，密码粘 PAT。

成功后刷新 GitHub 页面，文件就上去了。

## 6. 日常工作流（最重要的 5 个命令）

99% 的时间你只会用这几个命令：

```bash
git status            # 看现在的改动状态
git diff              # 看具体改了哪几行
git add 文件名         # 把指定文件加入暂存区
git add .             # 把所有改动加入暂存区
git commit -m "消息"   # 提交到本地仓库
git push              # 推送到远程
git pull              # 拉取最新代码
```

### 配套查看

```bash
git log               # 看历史提交
git log --oneline     # 一行一个，看得多
git log --graph --oneline --all   # 图形化分支
```

## 7. 写好 commit message

公司里通常用 [Conventional Commits](https://www.conventionalcommits.org/zh-hans/v1.0.0/)：

```text
<类型>(<作用域>): <说明>

feat: 新增员工管理页面
fix(employee): 修复手机号校验失败
docs: 更新 README
refactor(employee): 提取员工表单组件
test: 增加员工创建测试
chore: 升级依赖版本
```

- 第一行不超过 50 字，简洁概括
- 必要时空一行写正文（解释为什么）

## 8. 分支：和别人协作

```bash
git branch                    # 看所有分支
git checkout -b feat/login    # 新建并切到分支 feat/login
git checkout main             # 切回主分支
git merge feat/login          # 把 feat/login 合并到当前分支
git branch -d feat/login      # 删除已合并的分支
```

公司里常见流程：

```text
main           ●─────●─────●─────●     稳定的"生产代码"
                  \                   \
feat/login         ●──●──●            ●  在分支上开发、提交
                              ↑       ↑
                          PR 合并    PR 合并
```

## 9. .gitignore：别把不该传的传上去

新建一个 `.gitignore` 文件（项目根目录已经有了）：

```gitignore
# Java
target/
*.class
*.jar
*.log
.idea/
*.iml

# Node
node_modules/
dist/
.env.local

# IDE
.vscode/*

# OS
.DS_Store
Thumbs.db

# Secrets
**/application-prod.yml
*.key
```

如果不小心已经 add 了不该传的：

```bash
git rm --cached <file>     # 从暂存区移除但保留本地文件
git commit -m "chore: remove accidentally tracked file"
```

## 10. 出错了怎么办（常见救场命令）

| 我刚做了什么 | 怎么撤回 |
| --- | --- |
| 改坏了一个还没 add 的文件，想还原 | `git checkout -- 文件名`（注意：会丢工作区改动！） |
| 已经 `git add` 但还没 commit，想撤出暂存区 | `git restore --staged 文件名` |
| 刚 commit 但还没 push，想改 commit 信息 | `git commit --amend` |
| 把多个 commit 揉成一个（变基） | `git rebase -i HEAD~3` |
| 不小心 push 错了文件 | 把它从仓库删掉再 commit，重要的话考虑使用 `git filter-repo` |

> **永远不要** `git push --force` 到 main / master 分支，团队会哭。

## 11. 拉取与冲突解决

```bash
git pull            # = fetch + merge
```

如果你和别人改了同一个文件，会出现 "merge conflict"：

```text
<<<<<<< HEAD
你这边的内容
=======
别人那边的内容
>>>>>>> branch-name
```

手动编辑文件，**只留下你想保留的内容**，然后：

```bash
git add 冲突文件
git commit            # 或 git rebase --continue
```

## 12. Pull Request（PR）

公司协作的核心：你改完代码不直接推 main，而是：

1. 在分支 `feat/xxx` 提交
2. 推到 GitHub
3. 在 GitHub 网页上点 "Compare & Pull Request"
4. 写明改了什么，谁来 review
5. 同事 review 通过、CI 通过后，由维护者点 "Merge"

### 怎么写好 PR 描述

- **What**：改了什么（一句话）
- **Why**：为什么改（业务背景 / bug 现象）
- **How**：怎么改（关键设计点）
- **Test**：怎么测试通过的（截图 / 命令）

## 13. GitHub 的两个好用功能

### 13.1 Issues

提需求、报 bug、记 TODO，可以打标签、指派给人。

### 13.2 GitHub Actions（CI / CD）

仓库根目录建 `.github/workflows/ci.yml` 就能在每次 push / PR 时自动跑构建：

```yaml
name: CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: mvn -B compile
```

第一次接触不用上 CI，等项目成型再说。

## 14. 把 GitHub 仓库拉到 E 盘备份

Windows 上：

```powershell
# 切到 E 盘
E:
cd \

# 把仓库克隆到 E:\junior-java-company-demo
git clone https://github.com/你的用户名/junior-java-company-demo.git
```

完成后 `E:\junior-java-company-demo` 就是远程仓库的完整副本。后续：

```powershell
cd E:\junior-java-company-demo
git pull
```

详见 [12-备份到 E 盘与日常工作流](./12-备份到E盘与日常工作流.md)。

## 15. 课后练习

1. 在本地随便改一个 README.md，做一次完整的 `add → commit → push`。
2. 在 GitHub 网页直接改一个文件并提交，然后本地 `git pull` 同步下来。
3. 新建一个 `feat/practice` 分支，添加一个 `notes.md` 文件，推上去，在 GitHub 上点出一个 PR 给自己 merge。

下一章：[11-从 0 到 1 全栈搭建步骤](./11-从0到1全栈搭建步骤.md)
