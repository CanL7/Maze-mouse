package maze.algorithm;

import maze.model.Cell;
import maze.model.Maze;
import maze.model.SearchStep;
import maze.util.StepType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * 所有可行路径遍历算法
 *
 * 使用 DFS + 回溯遍历从起点到终点的所有可行路径。
 * 每找到一条完整路径就记录一次 FOUND 步骤，
 * 全部遍历结束后再标记其中的最短路径。
 */
public class DFSAllPathFinder {

    private final Maze maze;
    private final List<SearchStep> steps;
    private final Stack<Cell> currentPath;

    private int pathCount;
    private int exploredNodeCount;
    private List<Cell> shortestPath;

    public DFSAllPathFinder(Maze maze) {
        this.maze = maze;
        this.steps = new ArrayList<>();
        this.currentPath = new Stack<>();
        this.shortestPath = Collections.emptyList();
    }

    /**
     * 遍历所有从起点到终点的可行路径。
     *
     * @return 动画步骤列表
     */
    public List<SearchStep> findAllPaths() {
        steps.clear();
        currentPath.clear();
        pathCount = 0;
        exploredNodeCount = 0;
        shortestPath = Collections.emptyList();

        maze.resetSearchState();
        dfs(maze.start);

        // 快速展示结果时，直接把最短路径高亮出来。
        maze.resetSearchState();
        if (!shortestPath.isEmpty()) {
            markPath(shortestPath);
            steps.add(new SearchStep(
                maze.end,
                StepType.SHORTEST_PATH,
                steps.size() + 1,
                shortestPath
            ));
        }

        return steps;
    }

    public int getPathCount() {
        return pathCount;
    }

    public int getExploredNodeCount() {
        return exploredNodeCount;
    }

    public int getShortestPathLength() {
        return shortestPath.size();
    }

    private void dfs(Cell current) {
        current.visited = true;
        currentPath.push(current);
        exploredNodeCount++;
        steps.add(new SearchStep(current, StepType.VISIT, steps.size() + 1));

        if (current == maze.end) {
            pathCount++;
            List<Cell> foundPath = new ArrayList<>(currentPath);
            if (shortestPath.isEmpty() || foundPath.size() < shortestPath.size()) {
                shortestPath = foundPath;
            }
            steps.add(new SearchStep(current, StepType.FOUND, steps.size() + 1, foundPath));
        } else {
            for (Cell next : maze.getAccessibleNeighbors(current)) {
                dfs(next);
            }
        }

        Cell backtracked = currentPath.pop();
        current.visited = false;
        steps.add(new SearchStep(backtracked, StepType.BACKTRACK, steps.size() + 1));
    }

    private void markPath(List<Cell> path) {
        for (Cell cell : path) {
            cell.isPath = true;
        }
    }
}
