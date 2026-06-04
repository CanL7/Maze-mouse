# 电脑迷宫鼠 — Maze Mouse

> Java Swing 桌面应用 · 可视化迷宫生成与寻路算法演示

## 运行

```bash
javac -encoding UTF-8 -d out src/maze/**/*.java
java -cp out maze.Main
```

或者双击 `run.bat`。

需要 **Java 17+**，Windows / macOS / Linux 均可。

## 功能

| 按钮 | 功能 |
|------|------|
| **生成迷宫** | 随机 Prim 算法生成 20×20 迷宫 |
| **导入迷宫** | 从文本文件加载迷宫 |
| **DFS 寻路** | 深度优先搜索，展示探索 + 回溯过程 |
| **BFS 最短路径** | 广度优先搜索，找到最短路径并统计长度 |
| **▶ 播放** | 从头回放搜索动画 |
| **⏸ 暂停** | 暂停动画 |
| **↺ 重置** | 清除搜索标记，保留墙体 |
| **导出迷宫** | 将当前迷宫保存为文件 |

## 动画

- 点击 DFS/BFS 先直接展示最终路径，再点 **播放** 逐帧回放搜索过程
- 速度 1-10 可调（10 最快，约 10ms/帧）
- 暂停后可重置

## 颜色

| 元素 | 颜色 |
|------|------|
| 墙体 | 黑色 |
| 通路 | 白色 |
| 起点 | 绿色 |
| 终点 | 红色 |
| 探索中 | 蓝色 |
| 回溯 | 橙色 |
| 最终路径 | 黄色 |

## 界面

点击「◀ 状态」「日志 ▶」可展开侧边栏，实时显示：

- 当前状态 / 算法 / Stack 大小 / Queue 大小
- 已访问节点数 / 路径长度 / 运行耗时
- 时间戳搜索日志

## 文件格式

**导出/导入**使用 `.maze` 文本文件，格式如下：

```
# 注释行（可选）
5 5          ← 行 列
0 0          ← 起点坐标
4 4          ← 终点坐标
7 13 9 11 3  ← 每格一个 0-15 整数（bit 编码四墙）
5 10 12 6 15
...
```

墙编码：`bit0=上 bit1=下 bit2=左 bit3=右`。记事本即可编辑。

示例文件：[sample.maze](sample.maze)

## 项目结构

```
src/maze/
├── Main.java                 ← 入口
├── model/                    ← 数据层
│   ├── Cell.java             ← 单元格
│   ├── Maze.java             ← 迷宫管理
│   └── SearchStep.java       ← 搜索步骤
├── algorithm/                ← 算法层
│   ├── MazeGenerator.java    ← Prim 生成
│   ├── DFSPathFinder.java    ← DFS + Stack
│   └── BFSPathFinder.java    ← BFS + Queue
├── ui/                       ← 界面层
│   ├── MainFrame.java        ← 主窗口
│   ├── MazePanel.java        ← 迷宫绘制
│   ├── AnimationPlayer.java  ← 动画引擎
│   ├── StatusPanel.java      ← 状态面板
│   └── LogPanel.java         ← 日志面板
└── util/
    └── MazeIO.java           ← 文件读写
```

## 许可

课程设计项目，仅供学习参考。