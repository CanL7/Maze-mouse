package maze.algorithm;

import maze.model.Cell;
import maze.model.Maze;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 迷宫生成器 — Prim 算法
 * 
 * 使用随机 Prim 算法生成迷宫：
 * 1. 所有格子初始四面有墙
 * 2. 起点标记为已访问，将其相邻墙加入"候选墙"列表
 * 3. 随机选一堵候选墙，若对面格子未访问则打通该墙
 * 4. 将新访问格子的相邻墙加入候选列表
 * 5. 重复直到候选墙清空
 * 
 * 最终产出一个所有格子连通、无环的随机迷宫。
 */
public class MazeGenerator {

    private final Maze maze;
    private final Random random;

    /**
     * @param maze 待填充的迷宫对象
     */
    public MazeGenerator(Maze maze) {
        this.maze = maze;
        this.random = new Random();
    }

    /**
     * 执行 Prim 算法生成迷宫。
     * 生成前会重置所有格子的墙体状态。
     */
    public void generate() {
        // 1. 重置：所有墙都立起来，visited 全部置 false
        for (int r = 0; r < maze.rows; r++) {
            for (int c = 0; c < maze.cols; c++) {
                maze.grid[r][c].resetAll();
            }
        }

        // 2. 起点标记为已访问
        Cell start = maze.start;
        start.visited = true;

        // 3. 收集起点的相邻墙，加入候选列表
        List<Cell[]> wallList = new ArrayList<>();  // 每项: [visited侧, 对面侧]
        addAdjacentWalls(start, wallList);

        // 4. Prim 主循环：不断从候选墙中随机选一堵
        // 4. Prim 主循环：不断从候选墙中随机选一堵
        while (!wallList.isEmpty()) {
            // 随机选一堵候选墙
            int idx = random.nextInt(wallList.size());
            Cell[] pair = wallList.remove(idx);
            Cell visitedSide = pair[0];   // 已访问的一侧
            Cell oppositeSide = pair[1];  // 对面（待判断是否访问过）

            // 如果对面还没访问过，打通这堵墙
            if (!oppositeSide.visited) {
                removeWallBetween(visitedSide, oppositeSide);
                oppositeSide.visited = true;
                // 将新访问格子的相邻墙也加入候选
                addAdjacentWalls(oppositeSide, wallList);
            }
        }

        // 5. 清理 visited 标记（生成阶段借用了 visited，搜索阶段需要干净状态）
        maze.resetSearchState();
    }

    // ============ 私有辅助方法 ============

    /**
     * 将 cell 与所有未访问邻居之间的墙加入候选列表。
     * 每项为 {cell, neighbor}，其中 cell 是已访问侧。
     */
    private void addAdjacentWalls(Cell cell, List<Cell[]> wallList) {
        List<Cell> neighbors = maze.getNeighbors(cell);
        for (Cell neighbor : neighbors) {
            if (!neighbor.visited) {
                wallList.add(new Cell[]{cell, neighbor});
            }
        }
    }

    /**
     * 拆除两个相邻格子之间的墙。
     * 根据坐标差判断方向，同时修改两个格子的墙体标记。
     */
    private void removeWallBetween(Cell a, Cell b) {
        int dr = b.row - a.row;
        int dc = b.col - a.col;

        if (dr == -1) { // b 在 a 上方
            a.wallUp = false;
            b.wallDown = false;
        } else if (dr == 1) { // b 在 a 下方
            a.wallDown = false;
            b.wallUp = false;
        } else if (dc == -1) { // b 在 a 左侧
            a.wallLeft = false;
            b.wallRight = false;
        } else if (dc == 1) { // b 在 a 右侧
            a.wallRight = false;
            b.wallLeft = false;
        }
    }
}