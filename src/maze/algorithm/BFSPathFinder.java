package maze.algorithm;

import maze.model.Cell;
import maze.model.Maze;
import maze.model.SearchStep;
import maze.util.StepType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * BFS 广度优先最短路径算法
 * 
 * 使用 Queue 实现广度优先搜索，找到从起点到终点的最短路径。
 * 
 * 算法流程：
 * 1. 起点入队，标记 visited
 * 2. 循环：poll 队首 → 遍历所有未访问可通过邻居
 *    - 每个邻居入队，标记 visited，记录 parent，产生 VISIT 步骤
 * 3. 到达终点（首次到达即最短路径）→ 沿 parent 链回溯标记路径
 * 
 * 与 DFS 的关键区别：
 * - 使用 Queue（FIFO）而非 Stack（LIFO）
 * - 层序遍历，保证首次遇到终点时路径最短
 * - 没有回溯步骤（BACKTRACK），所有访问都是前向探索
 */
public class BFSPathFinder {

    private final Maze maze;

    /** 搜索过程中记录的全部步骤 */
    private final List<SearchStep> steps;

    /** BFS 队列 */
    private final Queue<Cell> queue;

    /** 是否找到终点 */
    private boolean found;

    public BFSPathFinder(Maze maze) {
        this.maze = maze;
        this.steps = new ArrayList<>();
        this.queue = new LinkedList<>();
        this.found = false;
    }

    /**
     * 执行 BFS 寻路。
     *
     * @return 搜索步骤列表，供 AnimationPlayer 播放
     */
    public List<SearchStep> find() {
        steps.clear();
        queue.clear();
        found = false;

        // 重置所有格子的搜索状态
        maze.resetSearchState();

        // 起点入队
        Cell start = maze.start;
        start.visited = true;
        queue.add(start);
        steps.add(new SearchStep(start, StepType.VISIT, steps.size() + 1));

        // BFS 主循环
        // 2. BFS 主循环：层序遍历
        while (!queue.isEmpty() && !found) {
            Cell current = queue.poll();

            // 遍历当前格子的所有可通过邻居
            List<Cell> neighbors = maze.getAccessibleNeighbors(current);
            for (Cell next : neighbors) {
                // 检查是否已被访问（getAccessibleNeighbors 已过滤 visited，
                // 但 BFS 并发入队时可能重复，这里做二次检查）
                if (next.visited) continue;

                // 标记并记录
                next.visited = true;
                next.parent = current;
                queue.add(next);
                steps.add(new SearchStep(next, StepType.VISIT, steps.size() + 1));

                // 到达终点
                if (next == maze.end) {
                    found = true;
                    markFinalPath();
                    steps.add(new SearchStep(next, StepType.FOUND, steps.size() + 1));
                    break;  // 首次到达即为最短，停止探索
                }
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
     * BFS 是否找到了终点。
     */
    public boolean isFound() {
        return found;
    }

    // ============ 私有辅助方法 ============

    /**
     * 从终点沿 parent 链回溯到起点，标记最短路径。
     */
    private void markFinalPath() {
        Cell current = maze.end;
        while (current != null) {
            current.isPath = true;
            current = current.parent;
        }
    }
}