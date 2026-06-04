# 电脑迷宫鼠 — 项目总体设计方案

## 一、需求分析总结

**核心目标**：实现一个 Java Swing 桌面应用，可视化展示迷宫生成、DFS 寻路、BFS 最短路径的完整过程。

**强制技术栈**：Java 17+ / Swing / Stack / Queue / 二维数组

**必须实现的 7 项功能**：

| # | 功能 | 关键约束 |
|---|------|---------|
| 1 | Prim 迷宫生成 | 随机、起点终点可达 |
| 2 | DFS 寻路 | 展示探索 + 回溯 |
| 3 | BFS 最短路径 | 统计路径长度、访问节点数 |
| 4 | 实时动画 | Swing Timer、暂停/继续/调速 |
| 5 | 日志系统 | 时间戳、实时刷新 |
| 6 | 状态面板 | Stack/Queue/Visited 实时数值 |
| 7 | Swing 图形界面 | 自适应布局、直观操作 |

---

## 二、程序状态机

+"`"+
IDLE ──[生成迷宫]──▶ GENERATED ──[DFS/BFS]──▶ SEARCHING
                         ▲                        │
                         │              ┌─────────┤
                         │              ▼         ▼
                         ◀──[重置]── RESET    PAUSED ──[继续]──▶ SEARCHING
                                                  │
                                            FINISHED ──[重置]──▶ IDLE
+"`"+

---

## 三、类设计（10 个类）

### 1. Cell — 迷宫单元格

职责：封装单个格子的全部状态

字段：
| 字段 | 类型 | 说明 |
|------|------|------|
| row | int | 行坐标 |
| col | int | 列坐标 |
| wallUp | boolean | 上墙 |
| wallDown | boolean | 下墙 |
| wallLeft | boolean | 左墙 |
| wallRight | boolean | 右墙 |
| visited | boolean | 是否被访问过 |
| isPath | boolean | 是否在最终路径上 |
| isBacktracked | boolean | 是否被回溯过 |
| parent | Cell | BFS/DFS 父节点（路径恢复用） |
| type | CellType | WALL / PATH / START / END |

方法：
- esetSearchState() — 重置搜索状态（保留墙体）

### 2. Maze — 迷宫数据管理

职责：持有 Cell[][] 二维数组，管理迷宫元信息

字段：
| 字段 | 类型 | 说明 |
|------|------|------|
| rows | int | 行数 |
| cols | int | 列数 |
| grid | Cell[][] | 二维网格 |
| start | Cell | 起点 |
| end | Cell | 终点 |

方法：
- getCell(r, c) / getNeighbors(cell) / esetSearchState()

### 3. MazeGenerator — Prim 迷宫生成器

职责：用 Prim 算法生成随机迷宫

算法流程：
1. 所有格子初始四面有墙
2. 从起点开始，维护一个"候选墙"列表
3. 随机选一堵墙，若对面格子未访问则打通，并将对面的墙加入候选
4. 重复直到所有格子被访问

输入：ows, cols
输出：填充好墙信息的 Cell[][]

### 4. DFSPathFinder — 深度优先寻路

职责：用 DFS + Stack 找一条可行路径

核心数据结构：Stack<Cell>

算法流程：
1. 起点入栈，标记 visited
2. 循环：peek 栈顶 → 找未访问邻居 → 有则入栈标记 / 无则 pop 回溯
3. 找到终点或栈空时结束

动画数据：每一步产生一个 SearchStep 记录（当前格、操作类型）
- 操作类型：VISIT（探索）、BACKTRACK（回溯）、FOUND（找到）

### 5. BFSPathFinder — 广度优先最短路径

职责：用 BFS + Queue 找最短路径

核心数据结构：Queue<Cell>

算法流程：
1. 起点入队，标记 visited
2. 循环：poll 队首 → 遍历所有未访问邻居 → 入队标记 + 记录 parent
3. 找到终点时停止
4. 从终点沿 parent 链回溯得到最短路径

动画数据：每一步产生一个 SearchStep（当前格、操作类型）

### 6. SearchStep — 搜索步骤记录

职责：记录搜索动画的每一步

字段：
| 字段 | 类型 | 说明 |
|------|------|------|
| cell | Cell | 当前操作的格子 |
| type | StepType | VISIT / BACKTRACK / FOUND_PATH |
| stepNumber | int | 步骤序号 |

### 7. AnimationPlayer — 动画控制器

职责：用 Swing Timer 逐帧播放搜索步骤

字段：
| 字段 | 类型 | 说明 |
|------|------|------|
| steps | List\<SearchStep\> | 全部步骤 |
| currentIndex | int | 当前播放位置 |
| timer | Timer | Swing Timer |
| speed | int | 延迟毫秒数 |
| paused | boolean | 暂停状态 |

方法：
- play() / pause() / esume() / setSpeed(int)
- 每帧回调：更新 MazePanel 绘制 + StatusPanel + LogPanel

### 8. MazePanel — 迷宫绘制组件

职责：JPanel 子类，绘制迷宫图形

绘制逻辑：
- 遍历 Cell[][]，根据 wallUp/Down/Left/Right 画线段
- 根据 Cell 状态填充颜色
- 支持窗口缩放时重新计算格子尺寸

颜色映射：

| 元素 | 颜色 |
|------|------|
| 墙体（线条） | 黑色 |
| 通路（填充） | 白色 |
| 起点（填充） | 绿色 |
| 终点（填充） | 红色 |
| DFS 探索中 | 蓝色 |
| 回溯 | 橙色 |
| 最终最短路径 | 黄色 |
| 背景 | 浅灰色 |

### 9. LogPanel — 日志面板

职责：JTextArea（只读），实时输出搜索日志

格式：[时间戳] 操作描述

示例：
- [10:23:45.123] DFS 搜索启动，起点 (0,0)
- [10:23:45.456] 访问节点 (0,1)
- [10:23:45.789] 回溯节点 (0,1) ← 死路
- [10:23:46.012] 找到终点 (9,9)！

### 10. StatusPanel — 状态面板

职责：JPanel，显示实时运行状态

显示项：
| 项目 | 说明 |
|------|------|
| 当前状态 | IDLE / GENERATED / SEARCHING / PAUSED / FINISHED |
| 当前算法 | DFS / BFS / 无 |
| Stack 大小 | DFS 栈中元素数 |
| Queue 大小 | BFS 队列中元素数 |
| 已访问节点数 | visited 总数 |
| 当前路径长度 | 当前已走步数 |
| 运行耗时 | 搜索用时 |

### 11. MainFrame — 主窗口

职责：JFrame，组装所有组件，管理按钮事件

布局（BorderLayout）：

+"`"+
┌──────────────────────────────────────────┐
│               MazePanel                   │  CENTER
├──────────────────────────────────────────┤
│ [生成迷宫] [DFS寻路] [BFS最短路径]         │
│ [▶播放] [⏸暂停] [↺重置] [速度: ===o====] │  SOUTH(top)
├───────────────────┬──────────────────────┤
│    StatusPanel    │      LogPanel        │  SOUTH(bottom)
└───────────────────┴──────────────────────┘
+"`"+

---

## 四、数据流设计

+"`"+
用户点击 [生成迷宫]
  → MainFrame 调用 MazeGenerator.generate()
  → 返回 Cell[][] 给 Maze
  → MazePanel.repaint() 绘制迷宫

用户点击 [DFS]
  → MainFrame 调用 DFSPathFinder.find(maze, start, end)
  → 返回 List<SearchStep>
  → AnimationPlayer.play(steps)
  → 每帧：更新 Cell 状态 → MazePanel.repaint() → StatusPanel.update() → LogPanel.append()

用户点击 [BFS]
  → 同上，使用 BFSPathFinder
  → 额外：路径恢复（parent 链回溯）
+"`"+

---

## 五、动画实现机制

+"`"+
Swing Timer (javax.swing.Timer)
  │
  ├─ delay = 200ms (默认)，可调速 50ms ~ 1000ms
  │
  └─ ActionListener: 每 tick 执行
       ├─ 取 SearchStep[currentIndex]
       ├─ 更新对应 Cell 的状态（visited / backtracked / isPath）
       ├─ MazePanel.repaint()
       ├─ StatusPanel 更新数值
       ├─ LogPanel 追加日志
       └─ currentIndex++
            └─ 若 currentIndex >= steps.size() → stop + 状态→FINISHED
+"`"+

**关键原则**：
- 只用 javax.swing.Timer，不用 Thread.sleep（会阻塞 EDT）
- 搜索算法在**后台线程**（SwingWorker）中运行，产出 List<SearchStep>
- 动画播放在主线程（EDT）通过 Timer 驱动

---

## 六、包结构

+"`"+
maze/
├── Main.java                程序入口
├── model/
│   ├── Cell.java            单元格
│   ├── CellType.java        单元格类型枚举
│   ├── Maze.java            迷宫数据
│   └── SearchStep.java      搜索步骤
├── algorithm/
│   ├── MazeGenerator.java   Prim 生成
│   ├── DFSPathFinder.java   DFS 寻路
│   └── BFSPathFinder.java   BFS 寻路
├── ui/
│   ├── MainFrame.java       主窗口
│   ├── MazePanel.java       迷宫绘制
│   ├── LogPanel.java        日志面板
│   ├── StatusPanel.java     状态面板
│   └── AnimationPlayer.java 动画控制器
└── util/
    └── StepType.java        步骤类型枚举
+"`"+

---

## 七、开发顺序（按 9 个 Phase）

| Phase | 内容 | 产出 |
|-------|------|------|
| 1 | Cell + Maze | 数据层就绪 |
| 2 | MazeGenerator (Prim) | 迷宫可生成 |
| 3 | DFSPathFinder | DFS 搜索步骤生成 |
| 4 | BFSPathFinder | BFS 搜索步骤 + 最短路径 |
| 5 | MainFrame + MazePanel | 可视化界面 |
| 6 | AnimationPlayer | 动画播放 |
| 7 | LogPanel | 日志输出 |
| 8 | StatusPanel | 状态展示 |
| 9 | 界面美化 + 注释 + 测试 | 答辩就绪 |

---

## 八、与任务书的对齐检查

| 任务书要求 | 设计方案覆盖 |
|-----------|-------------|
| Prim 迷宫生成 | MazeGenerator |
| DFS + Stack | DFSPathFinder + Stack\<Cell\> |
| BFS + Queue | BFSPathFinder + Queue\<Cell\> |
| 实时动画 + 暂停/继续/调速 | AnimationPlayer + Swing Timer |
| 日志系统 | LogPanel |
| 状态面板 | StatusPanel |
| 颜色规范（黑/白/绿/红/蓝/橙/黄/浅灰） | MazePanel 全覆盖 |
| 状态机（IDLE→...→RESET） | MainFrame 状态管理 |
| 每个类职责单一 | 10 个类各司其职 |
| 注释 ≥ 1/3 | Phase 9 补全 |