package maze.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 迷宫数据管理类
 * 
 * 持有 Cell[][] 二维数组，管理迷宫的元信息（尺寸、起点、终点）。
 * 提供邻居查询、状态重置等工具方法，供算法和 UI 层使用。
 */
public class Maze {

    /** 迷宫行数 */
    public final int rows;
    /** 迷宫列数 */
    public final int cols;

    /** 二维网格，存储所有单元格 */
    public final Cell[][] grid;

    /** 起点（左上角） */
    public Cell start;
    /** 终点（右下角） */
    public Cell end;

    // ============ 构造方法 ============

    /**
     * 创建一个 rows x cols 的迷宫。
     * 起点默认设在 (0, 0)，终点默认设在 (rows-1, cols-1)。
     *
     * @param rows 行数
     * @param cols 列数
     */
    public Maze(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Cell[rows][cols];

        // 初始化所有格子为 PATH 类型
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell(r, c, CellType.PATH);
            }
        }

        // 设置起点和终点
        this.start = grid[0][0];
        this.start.type = CellType.START;

        this.end = grid[rows - 1][cols - 1];
        this.end.type = CellType.END;
    }

    // ============ 公共方法 ============

    /**
     * 获取指定坐标的单元格。
     *
     * @param row 行坐标
     * @param col 列坐标
     * @return 对应 Cell；越界返回 null
     */
    
    /**
     * 重新指定起点和终点位置。
     * 用于从文件导入时调整起终点。
     */
    public void setStartEnd(int startRow, int startCol, int endRow, int endCol) {
        // 恢复旧起点
        if (this.start != null) this.start.type = CellType.PATH;
        if (this.end != null) this.end.type = CellType.PATH;
        // 设置新起点
        this.start = grid[startRow][startCol];
        this.start.type = CellType.START;
        // 设置新终点
        this.end = grid[endRow][endCol];
        this.end.type = CellType.END;
    }
    public Cell getCell(int row, int col) {
        if (isInBounds(row, col)) {
            return grid[row][col];
        }
        return null;
    }

    /**
     * 获取当前单元格所有在迷宫范围内的邻居（上、下、左、右）。
     * 不检查墙体，仅检查坐标是否合法。
     *
     * @param cell 当前单元格
     * @return 邻居列表
     */
    public List<Cell> getNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int r = cell.row;
        int c = cell.col;

        // 上
        if (isInBounds(r - 1, c)) neighbors.add(grid[r - 1][c]);
        // 下
        if (isInBounds(r + 1, c)) neighbors.add(grid[r + 1][c]);
        // 左
        if (isInBounds(r, c - 1)) neighbors.add(grid[r][c - 1]);
        // 右
        if (isInBounds(r, c + 1)) neighbors.add(grid[r][c + 1]);

        return neighbors;
    }

    /**
     * 获取未访问过的邻居（用于 Prim 迷宫生成）。
     *
     * @param cell 当前单元格
     * @return 未访问的邻居列表
     */
    public List<Cell> getUnvisitedNeighbors(Cell cell) {
        List<Cell> neighbors = getNeighbors(cell);
        neighbors.removeIf(n -> n.visited);
        return neighbors;
    }

    /**
     * 获取可通过的邻居（用于寻路算法）。
     * 可通过 = 坐标合法 + 未被访问 + 中间没有墙阻挡。
     *
     * @param cell 当前单元格
     * @return 可通过的邻居列表
     */
    public List<Cell> getAccessibleNeighbors(Cell cell) {
        List<Cell> result = new ArrayList<>();
        int r = cell.row;
        int c = cell.col;

        // 上：目标格子在界内 + 中间无墙
        if (isInBounds(r - 1, c) && !cell.wallUp) {
            Cell up = grid[r - 1][c];
            if (!up.visited) result.add(up);
        }
        // 下
        if (isInBounds(r + 1, c) && !cell.wallDown) {
            Cell down = grid[r + 1][c];
            if (!down.visited) result.add(down);
        }
        // 左
        if (isInBounds(r, c - 1) && !cell.wallLeft) {
            Cell left = grid[r][c - 1];
            if (!left.visited) result.add(left);
        }
        // 右
        if (isInBounds(r, c + 1) && !cell.wallRight) {
            Cell right = grid[r][c + 1];
            if (!right.visited) result.add(right);
        }

        return result;
    }

    /**
     * 重置所有格子的搜索状态（保留墙体）。
     * 每次开始新搜索前调用。
     */
    public void resetSearchState() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c].resetSearchState();
            }
        }
    }

    /**
     * 判断坐标是否在迷宫范围内。
     */
    public boolean isInBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }
}