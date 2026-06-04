package maze.ui;

import maze.model.Maze;
import maze.model.SearchStep;
import maze.util.StepType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 动画控制器
 * 
 * 使用 Swing Timer 逐帧播放搜索步骤。
 * 每帧更新 Maze 中对应 Cell 的状态，然后触发 MazePanel 重绘。
 * 
 * 核心设计：
 *   搜索算法产出 List<SearchStep>（纯数据）
 *   AnimationPlayer 消费步骤列表，逐帧应用到 Maze 上
 *   界面层（MazePanel）只负责渲染，不关心动画逻辑
 * 
 * 速度映射：滑块 1-10 → Timer 延迟 500ms-50ms
 */
public class AnimationPlayer {

    /** 全部搜索步骤 */
    private List<SearchStep> steps;

    /** 当前播放到的步骤索引 */
    private int currentIndex;

    /** Swing Timer，在 EDT 上定时触发 */
    private Timer timer;

    /** 是否处于暂停状态 */
    private boolean paused;

    /** 是否正在播放（timer 运行中） */
    private boolean playing;

    /** 缓存的延迟值，timer 重建时使用 */
    private int pendingDelay = 200;

    /** 关联的迷宫（用于修改 Cell 状态） */
    private Maze maze;

    /** 关联的绘制面板（用于刷新） */
    private MazePanel mazePanel;

    /** 每帧回调（用于更新状态/日志面板） */
    private StepCallback onStep;

    /** 播放完成回调 */
    private Runnable onFinish;

    // ============ 速度常量 ============

    /** 最慢速度对应的延迟（毫秒） */
    private static final int MAX_DELAY = 500;
    /** 最快速度对应的延迟（毫秒） */
    private static final int MIN_DELAY = 10;

    // ============ 构造方法 ============

    public AnimationPlayer(Maze maze, MazePanel mazePanel) {
        this.maze = maze;
        this.mazePanel = mazePanel;
        this.paused = false;
        this.playing = false;
    }

    // ============ 公共方法 ============

    /**
     * 设置要播放的步骤列表，并重置播放位置。
     */
    public void setSteps(List<SearchStep> steps) {
        this.steps = steps;
        this.currentIndex = 0;
    }

    /**
     * 设置每帧回调。
     * 回调参数：(当前步骤, 总步骤数)
     */
    public void setOnStep(StepCallback callback) {
        this.onStep = callback;
    }

    /**
     * 设置播放完成回调。
     */
    public void setOnFinish(Runnable callback) {
        this.onFinish = callback;
    }

    /**
     * 从当前位置开始播放。
     * 播放前会重置迷宫搜索状态，确保动画从干净状态开始。
     */
    public void play() {
        if (steps == null || steps.isEmpty()) return;

        // 重置迷宫状态，动画从头开始"揭示"每一步
        maze.resetSearchState();
        currentIndex = 0;
        paused = false;
        playing = true;

        startTimer();
    }

    /**
     * 暂停播放。
     */
    public void pause() {
        if (!playing || paused) return;
        paused = true;
        if (timer != null) timer.stop();
    }

    /**
     * 从暂停处继续播放。
     */
    public void resume() {
        if (!playing || !paused) return;
        paused = false;
        startTimer();
    }

    /**
     * 停止播放并重置。
     */
    public void stop() {
        playing = false;
        paused = false;
        if (timer != null) timer.stop();
        timer = null;
    }

    /**
     * 设置播放速度。
     *
     * @param speed 1-10，1 最慢，10 最快
     */
    public void setSpeed(int speed) {
        pendingDelay = MAX_DELAY - (speed - 1) * (MAX_DELAY - MIN_DELAY) / 9;
        if (timer != null) {
            timer.setDelay(pendingDelay);
        }
    }

    /**
     * 是否正在播放（包括暂停中）。
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * 是否处于暂停状态。
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * 获取当前步骤序号。
     */
    public int getCurrentStepNumber() {
        if (steps == null || currentIndex >= steps.size()) return 0;
        return steps.get(currentIndex).stepNumber;
    }

    /**
     * 获取总步骤数。
     */
    public int getTotalSteps() {
        return steps == null ? 0 : steps.size();
    }

    // ============ 私有方法 ============

    /**
     * 创建并启动 Timer。
     */
    private void startTimer() {
        if (timer != null) timer.stop();

        // 默认延迟 200ms
        timer = new Timer(pendingDelay, new TimerAction());
        timer.start();
    }

    /**
     * Timer 每帧回调。
     */
    private class TimerAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (steps == null || currentIndex >= steps.size()) {
                finish();
                return;
            }

            // 取当前步骤，应用到迷宫单元格
            SearchStep step = steps.get(currentIndex);

            // 根据步骤类型更新 Cell 状态
            applyStep(step);

            // 刷新绘制
            mazePanel.repaint();

            // 回调更新状态/日志
            if (onStep != null) {
                onStep.update(currentIndex + 1, steps.size());
            }

            currentIndex++;

            // 播放完毕
            if (currentIndex >= steps.size()) {
                finish();
            }
        }
    }

    /**
     * 将一个搜索步骤应用到迷宫中。
     */
    private void applyStep(SearchStep step) {
        if (step.type == StepType.VISIT) {
            step.cell.visited = true;
        } else if (step.type == StepType.BACKTRACK) {
            step.cell.isBacktracked = true;
        } else if (step.type == StepType.FOUND) {
            // FOUND 步骤：标记最终路径
            markFinalPath();
        }
    }

    /**
     * 沿 parent 链标记从终点到起点的路径。
     */
    private void markFinalPath() {
        maze.model.Cell current = maze.end;
        while (current != null) {
            current.isPath = true;
            current = current.parent;
        }
    }

    /**
     * 播放完成，停止 Timer，触发回调。
     */
    private void finish() {
        playing = false;
        paused = false;
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        mazePanel.repaint();
        if (onFinish != null) {
            onFinish.run();
        }
    }

    // ============ 回调接口 ============

    /**
     * 每帧回调接口。
     */
    @FunctionalInterface
    public interface StepCallback {
        /**
         * @param current 当前步骤序号（1-based）
         * @param total   总步骤数
         */
        void update(int current, int total);
    }
}