package maze.algorithm;

import maze.model.Cell;
import maze.model.Maze;
import maze.model.SearchStep;
import maze.util.StepType;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * DFS 深度优先寻路算法
 * 
 * 使用 Stack 实现深度优先搜索，找出一条从起点到终点的可行路径。
 * 
 * 算法流程：
 * 1. 起点入栈，标记 visited
 * 2. 循环：peek 栈顶 → 找未访问可通过邻居
 *    - 有邻居 → 入栈，记录 VISIT 步骤
 *    - 无邻居 → pop 弹栈，记录 BACKTRACK 步骤
 * 3. 到达终点 → 沿 parent 链回溯标记最短路径，记录 FOUND 步骤
 * 
 * 每一步都产出 SearchStep，供动画播放使用。
 */
public class DFSPathFinder {

    private final Maze maze;

    /** 搜索过程中记录的全部步骤 */
    private final List<SearchStep> steps;

    /** DFS 栈 */
    private final Stack<Cell> stack;

    /** 是否找到终点 */
    private boolean found;

    public DFSPathFinder(Maze maze) {
        this.maze = maze;
        this.steps = new ArrayList<>();
        this.stack = new Stack<>();
        this.found = false;
    }

    /**
     * 执行 DFS 寻路。
     *
     * @return 搜索步骤列表，供 AnimationPlayer 播放
     */
    public List<SearchStep> find() {
        steps.clear();
        stack.clear();
        found = false;

        // 重置所有格子的搜索状态
        maze.resetSearchState();

        // 起点入栈
        Cell start = maze.start;
        start.visited = true;
        stack.push(start);
        steps.add(new SearchStep(start, StepType.VISIT, steps.size() + 1));

        // DFS 主循环
        // 2. DFS 主循环：深入探索 / 死路回溯
        while (!stack.isEmpty() && !found) {
            Cell current = stack.peek();

            // 找一个未访问的可通过邻居
            Cell next = findUnvisitedNeighbor(current);

            if (next != null) {
                // 有路可走：入栈，继续深入
                next.visited = true;
                next.parent = current;
                stack.push(next);
                steps.add(new SearchStep(next, StepType.VISIT, steps.size() + 1));

                // 检查是否到达终点
                if (next == maze.end) {
                    found = true;
                    markFinalPath();
                    steps.add(new SearchStep(next, StepType.FOUND, steps.size() + 1));
                }
            } else {
                // 死路：弹栈，回溯
                Cell backtracked = stack.pop();
                backtracked.isBacktracked = true;
                steps.add(new SearchStep(backtracked, StepType.BACKTRACK, steps.size() + 1));
            }
        }

        return steps;
    }

    /**
     * 获取搜索步骤列表（仅在 find() 调用后有效）。
     */
    public List<SearchStep> getSteps() {
        return steps;
    }

    /**
     * DFS 是否找到了终点。
     */
    public boolean isFound() {
        return found;
    }

    // ============ 私有辅助方法 ============

    /**
     * 找当前格子第一个未访问的可通过邻居。
     * 顺序：上 → 下 → 左 → 右。
     *
     * @return 找到的邻居；没有则返回 null
     */
    private Cell findUnvisitedNeighbor(Cell cell) {
        List<Cell> neighbors = maze.getAccessibleNeighbors(cell);
        // getAccessibleNeighbors 已经过滤了 visited 和墙体，
        // 这里直接取第一个即可
        if (!neighbors.isEmpty()) {
            return neighbors.get(0);
        }
        return null;
    }

    /**
     * 从终点沿 parent 链回溯到起点，标记最终路径。
     */
    private void markFinalPath() {
        Cell current = maze.end;
        while (current != null) {
            current.isPath = true;
            current = current.parent;
        }
    }
}