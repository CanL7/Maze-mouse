package maze.ui;

import maze.algorithm.BFSPathFinder;
import maze.algorithm.DFSAllPathFinder;
import maze.algorithm.DFSPathFinder;
import maze.algorithm.MazeGenerator;
import maze.model.Maze;
import maze.model.SearchStep;
import maze.util.MazeIO;
import maze.util.StepType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * 主窗口
 * 
 * 布局（可折叠侧边栏）：
 *   WEST   — StatusPanel（可折叠）
 *   CENTER — MazePanel
 *   EAST   — LogPanel（可折叠）
 *   SOUTH  — 按钮栏 + 状态标签
 * 
 * 状态机：
 *   IDLE → GENERATED → FINISHED → SEARCHING ⇄ PAUSED → GENERATED
 */
public class MainFrame extends JFrame {

    // ============ 模型 ============
    private Maze maze;
    private DFSPathFinder dfsFinder;
    private BFSPathFinder bfsFinder;
    private DFSAllPathFinder allPathFinder;
    private AnimationPlayer animator;

    private List<SearchStep> lastSteps;
    private SearchMode lastSearchMode;
    private int lastVisitedCount;
    private int lastPathLength;
    private int lastPathCount;

    // 动画状态追踪
    private int animVisited, animPathLen, animStackSize, animQueueSize;
    private long animStartTime;

    // ============ UI 组件 ============
    private MazePanel mazePanel;
    private StatusPanel statusPanel;
    private LogPanel logPanel;
    private JPanel sideLeft, sideRight;

    private JButton btnGenerate, btnDFS, btnBFS, btnTraverse, btnPlay, btnPause, btnReset;
    private JButton btnImport, btnExport, btnToggleStatus, btnToggleLog;
    private JSlider speedSlider;
    private JLabel speedLabel, statusLabel;

    private boolean statusVisible = false;
    private boolean logVisible = false;

    // ============ 状态机 ============
    private enum State { IDLE, GENERATED, FINISHED, SEARCHING, PAUSED }
    private State state = State.IDLE;

    // ============ 构造方法 ============

    public MainFrame() {
        initUI();
        updateButtonStates();
    }

    private void initUI() {
        setTitle("电脑迷宫鼠 — Maze Mouse");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 780);
        setMinimumSize(new Dimension(650, 500));
        setLocationRelativeTo(null);

        // === 左侧栏（StatusPanel） ===
        statusPanel = new StatusPanel();
        sideLeft = new JPanel(new BorderLayout());
        sideLeft.setPreferredSize(new Dimension(310, 210));
        sideLeft.setMinimumSize(new Dimension(300, 210));
        sideLeft.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
            new EmptyBorder(6, 6, 6, 6)));
        sideLeft.add(statusPanel, BorderLayout.NORTH);
        sideLeft.setVisible(false);

        // === 右侧栏（LogPanel） ===
        logPanel = new LogPanel();
        sideRight = new JPanel(new BorderLayout());
        sideRight.setPreferredSize(new Dimension(300, 100));
        sideRight.setMinimumSize(new Dimension(270, 100));
        sideRight.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY),
            new EmptyBorder(6, 6, 6, 6)));
        sideRight.add(logPanel, BorderLayout.CENTER);
        sideRight.setVisible(false);

        // === 迷宫（中央） ===
        mazePanel = new MazePanel();
        mazePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // === 按钮栏 + 状态标签（底部） ===
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new EmptyBorder(8, 8, 4, 8));

        JPanel buttonBar = createButtonBar();
        southPanel.add(buttonBar, BorderLayout.NORTH);

        statusLabel = new JLabel("就绪 — 请点击「生成迷宫」开始");
        statusLabel.setBorder(new EmptyBorder(6, 4, 2, 4));
        southPanel.add(statusLabel, BorderLayout.SOUTH);

        // === 组装 ===
        JPanel content = new JPanel(new BorderLayout());
        content.add(sideLeft, BorderLayout.WEST);
        content.add(mazePanel, BorderLayout.CENTER);
        content.add(sideRight, BorderLayout.EAST);
        content.add(southPanel, BorderLayout.SOUTH);

        add(content);
        bindEvents();
    }

    private JPanel createButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));

        btnGenerate = new JButton("生成迷宫");
        btnDFS      = new JButton("DFS 寻路");
        btnBFS      = new JButton("BFS 最短路径");
        btnTraverse = new JButton("所有可行路径");
        btnPlay     = new JButton("▶ 播放");
        btnPause    = new JButton("⏸ 暂停");
        btnReset    = new JButton("↺ 重置");
        btnImport   = new JButton("导入迷宫");
        btnExport   = new JButton("导出迷宫");

        bar.add(btnGenerate);
        bar.add(btnDFS);
        bar.add(btnBFS);
        bar.add(btnTraverse);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(btnPlay);
        bar.add(btnPause);
        bar.add(btnReset);
        bar.add(Box.createHorizontalStrut(12));
        bar.add(btnImport);
        bar.add(btnExport);

        // 速度
        bar.add(Box.createHorizontalStrut(12));
        bar.add(new JLabel("速度:"));
        speedSlider = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
        speedSlider.setPreferredSize(new Dimension(90, 24));
        speedSlider.setToolTipText("1=慢  10=快");
        bar.add(speedSlider);
        speedLabel = new JLabel("5");
        speedLabel.setPreferredSize(new Dimension(20, 20));
        bar.add(speedLabel);

        // 侧边栏切换按钮
        bar.add(Box.createHorizontalStrut(12));
        btnToggleStatus = new JButton("◀ 状态");
        btnToggleLog    = new JButton("日志 ▶");
        bar.add(btnToggleStatus);
        bar.add(btnToggleLog);

        return bar;
    }

    private void bindEvents() {
        btnGenerate.addActionListener(e -> generateMaze());
        btnDFS.addActionListener(e -> runSearch(SearchMode.DFS_PATH));
        btnBFS.addActionListener(e -> runSearch(SearchMode.BFS_PATH));
        btnTraverse.addActionListener(e -> runSearch(SearchMode.ALL_PATHS));
        btnPlay.addActionListener(e -> startReplay());
        btnPause.addActionListener(e -> pauseAnimation());
        btnReset.addActionListener(e -> resetMaze());
        btnImport.addActionListener(e -> importMaze());
        btnExport.addActionListener(e -> exportMaze());

        speedSlider.addChangeListener(e -> {
            int val = speedSlider.getValue();
            speedLabel.setText(String.valueOf(val));
            if (animator != null && !speedSlider.getValueIsAdjusting())
                animator.setSpeed(val);
        });

        btnToggleStatus.addActionListener(e -> toggleStatusPanel());
        btnToggleLog.addActionListener(e -> toggleLogPanel());
    }

    // ============ 侧边栏折叠 ============

    private void toggleStatusPanel() {
        statusVisible = !statusVisible;
        sideLeft.setVisible(statusVisible);
        btnToggleStatus.setText(statusVisible ? "◀ 状态" : "状态 ▶");
        revalidate();
    }

    private void toggleLogPanel() {
        logVisible = !logVisible;
        sideRight.setVisible(logVisible);
        btnToggleLog.setText(logVisible ? "◀ 日志" : "日志 ▶");
        revalidate();
    }

    // ============ 迷宫生成 ============

    private void generateMaze() {
        maze = new Maze(20, 20);
        new MazeGenerator(maze).generate();
        setupMazeForUse();
        logPanel.append("迷宫生成完毕 — " + maze.rows + "×" + maze.cols);
        statusLabel.setText("迷宫生成完毕 — 请选择搜索算法");
    }

    // ============ 导入 / 导出 ============

    private void importMaze() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("导入迷宫文件");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                maze = MazeIO.load(file);
                setupMazeForUse();
                logPanel.append("迷宫已导入 — " + maze.rows + "×" + maze.cols);
                statusLabel.setText("迷宫已导入 — 请选择搜索算法");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "导入失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportMaze() {
        if (maze == null) return;
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("导出迷宫文件");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                if (!file.getName().contains(".")) {
                    file = new File(file.getAbsolutePath() + ".maze");
                }
                MazeIO.save(maze, file);
                logPanel.append("迷宫已导出 -> " + file.getName());
                statusLabel.setText("迷宫已导出 -> " + file.getName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "导出失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 生成或导入迷宫后的统一初始化。
     */
    private void setupMazeForUse() {
        mazePanel.setMaze(maze);
        lastSteps = null;
        lastSearchMode = null;
        lastVisitedCount = 0;
        lastPathLength = 0;
        lastPathCount = 0;
        logPanel.clear();
        statusPanel.reset();

        animator = new AnimationPlayer(maze, mazePanel);
        animator.setSpeed(speedSlider.getValue());
        animator.setOnStep(this::onAnimationStep);
        animator.setOnFinish(this::onReplayFinish);

        state = State.GENERATED;
        updateButtonStates();
    }

    // ============ 快速搜索 ============

    private void runSearch(SearchMode mode) {
        if (maze == null) return;

        lastSearchMode = mode;
        String algo = getModeLabel(mode);
        boolean isAllPaths = mode == SearchMode.ALL_PATHS;

        logPanel.clear();
        logPanel.append(algo + (isAllPaths ? " 启动" : " 搜索启动"));

        if (mode == SearchMode.DFS_PATH) {
            dfsFinder = new DFSPathFinder(maze);
            lastSteps = dfsFinder.find();
        } else if (mode == SearchMode.BFS_PATH) {
            bfsFinder = new BFSPathFinder(maze);
            lastSteps = bfsFinder.find();
        } else {
            allPathFinder = new DFSAllPathFinder(maze);
            lastSteps = allPathFinder.findAllPaths();
        }

        for (SearchStep step : lastSteps)
            logPanel.appendStep(step.stepNumber, step.cell.row, step.cell.col, step.type);

        mazePanel.repaint();

        int visited = isAllPaths ? allPathFinder.getExploredNodeCount() : countVisited();
        int pathLen = isAllPaths ? allPathFinder.getShortestPathLength() : countPathLength();
        boolean isDFSLike = isDFSLikeMode(mode);

        lastVisitedCount = visited;
        lastPathLength = pathLen;
        lastPathCount = isAllPaths ? allPathFinder.getPathCount() : 0;

        statusPanel.setState(isAllPaths ? "路径遍历完成" : "搜索完成");
        statusPanel.setAlgorithm(algo);
        statusPanel.setStackSize(mode == SearchMode.DFS_PATH ? pathLen : 0);
        statusPanel.setQueueSize(mode == SearchMode.BFS_PATH ? visited : 0);
        statusPanel.setVisited(visited);
        statusPanel.setPathLength(pathLen);

        state = State.FINISHED;
        updateButtonStates();
        if (isAllPaths) {
            statusLabel.setText(String.format(
                "%s 已完成！共找到 %d 条可行路径，最短路径长度 %d — 点击播放查看过程",
                algo, lastPathCount, pathLen));
        } else {
            statusLabel.setText(String.format(
                "%s 已找到路径！访问 %d 节点，路径长度 %d — 点击播放查看过程",
                algo, visited, pathLen));
        }
    }

    // ============ 动画回放 ============

    private void startReplay() {
        if (lastSteps == null || lastSteps.isEmpty()) return;

        String algo = getModeLabel(lastSearchMode);
        boolean isAllPaths = lastSearchMode == SearchMode.ALL_PATHS;
        logPanel.clear();
        logPanel.append("开始回放 " + algo + (isAllPaths ? " 过程..." : " 搜索过程..."));

        maze.resetSearchState();
        mazePanel.repaint();

        animVisited = 0;
        animPathLen = 0;
        animStackSize = 0;
        animQueueSize = 0;
        animStartTime = System.currentTimeMillis();

        statusPanel.setState("播放中");
        statusPanel.setAlgorithm(algo);
        statusPanel.setStackSize(0);
        statusPanel.setQueueSize(0);
        statusPanel.setVisited(0);
        statusPanel.setPathLength(0);
        statusPanel.setElapsedTime(0);

        animator.setSteps(lastSteps);
        animator.setSpeed(speedSlider.getValue());
        animator.play();

        state = State.SEARCHING;
        updateButtonStates();
        statusLabel.setText("播放中... 步骤 1 / " + lastSteps.size());
    }

    private void onAnimationStep(int current, int total) {
        statusLabel.setText(String.format("播放中... 步骤 %d / %d", current, total));

        if (lastSteps == null || current - 1 >= lastSteps.size()) return;
        SearchStep step = lastSteps.get(current - 1);

        logPanel.appendStep(step.stepNumber, step.cell.row, step.cell.col, step.type);

        if (step.type == StepType.VISIT) {
            animVisited++;
            if (isDFSLikeMode(lastSearchMode)) animStackSize++;
            else animQueueSize++;
        } else if (step.type == StepType.BACKTRACK) {
            animStackSize--;
        } else if (step.type == StepType.FOUND || step.type == StepType.SHORTEST_PATH) {
            animPathLen = !step.pathCells.isEmpty() ? step.pathCells.size() : countPathLength();
            if (lastSearchMode == SearchMode.BFS_PATH || lastSearchMode == SearchMode.DFS_PATH) {
                animStackSize = 0;
            }
            if (step.type == StepType.SHORTEST_PATH || lastSearchMode == SearchMode.BFS_PATH) {
                animQueueSize = 0;
            }
        }

        statusPanel.setVisited(animVisited);
        statusPanel.setPathLength(animPathLen);
        statusPanel.setStackSize(isDFSLikeMode(lastSearchMode) ? animStackSize : 0);
        statusPanel.setQueueSize(lastSearchMode == SearchMode.BFS_PATH ? animQueueSize : 0);
        statusPanel.setElapsedTime(System.currentTimeMillis() - animStartTime);
    }

    private void pauseAnimation() {
        if (animator == null || !animator.isPlaying() || animator.isPaused()) return;
        animator.pause();
        logPanel.append("已暂停");
        statusPanel.setState("已暂停");
        state = State.PAUSED;
        updateButtonStates();
        statusLabel.setText("已暂停");
    }

    private void onReplayFinish() {
        logPanel.append("回放完成");
        statusPanel.setState("回放完成");
        statusPanel.setVisited(animVisited);
        statusPanel.setPathLength(animPathLen);
        state = State.FINISHED;
        updateButtonStates();
        if (lastSearchMode == SearchMode.ALL_PATHS) {
            statusLabel.setText(String.format(
                "回放完成 — 共找到 %d 条可行路径，最短路径长度 %d",
                lastPathCount, lastPathLength));
        } else {
            statusLabel.setText(String.format(
                "回放完成 — 访问 %d 个节点，路径长度 %d", animVisited, animPathLen));
        }
    }

    // ============ 重置 ============

    private void resetMaze() {
        if (maze == null) return;
        if (animator != null) animator.stop();
        maze.resetSearchState();
        mazePanel.repaint();
        logPanel.clear();
        logPanel.append("已重置");
        statusPanel.reset();
        state = State.GENERATED;
        updateButtonStates();
        statusLabel.setText("已重置 — 可重新选择搜索算法");
    }

    // ============ 状态管理 ============

    private void updateButtonStates() {
        switch (state) {
            case IDLE      -> setButtons(true, false, false, false, false, false, false, true);
            case GENERATED -> setButtons(true, true, true, true, false, false, false, true);
            case FINISHED  -> setButtons(true, true, true, true, true, false, true, true);
            case SEARCHING -> setButtons(false, false, false, false, false, true, true, true);
            case PAUSED    -> setButtons(false, false, false, false, true, false, true, true);
        }
    }

    private void setButtons(boolean gen, boolean dfs, boolean bfs, boolean traverse,
                            boolean play, boolean pause, boolean reset, boolean speed) {
        btnGenerate.setEnabled(gen);
        btnDFS.setEnabled(dfs);
        btnBFS.setEnabled(bfs);
        btnTraverse.setEnabled(traverse);
        btnPlay.setEnabled(play);
        btnPause.setEnabled(pause);
        btnReset.setEnabled(reset);
        speedSlider.setEnabled(speed);
        btnImport.setEnabled(true);
        btnExport.setEnabled(maze != null);
    }

    private String getModeLabel(SearchMode mode) {
        if (mode == null) return "搜索";
        return switch (mode) {
            case DFS_PATH -> "DFS";
            case BFS_PATH -> "BFS";
            case ALL_PATHS -> "所有可行路径";
        };
    }

    private boolean isDFSLikeMode(SearchMode mode) {
        return mode == SearchMode.DFS_PATH || mode == SearchMode.ALL_PATHS;
    }

    // ============ 统计 ============

    private int countVisited() {
        int c = 0;
        for (int r = 0; r < maze.rows; r++)
            for (int col = 0; col < maze.cols; col++)
                if (maze.grid[r][col].visited) c++;
        return c;
    }

    private int countPathLength() {
        int c = 0;
        for (int r = 0; r < maze.rows; r++)
            for (int col = 0; col < maze.cols; col++)
                if (maze.grid[r][col].isPath) c++;
        return c;
    }
}
